/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.schema;

import nl.b3p.brmo.schema.mapping.AttributeColumnMapping;
import nl.b3p.brmo.schema.mapping.GeometryAttributeColumnMapping;
import nl.b3p.brmo.sql.GeometryHandlingPreparedStatementBatch;
import nl.b3p.brmo.sql.PostGISCopyInsertBatch;
import nl.b3p.brmo.sql.QueryBatch;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObjectTableWriter {
    private static final Log log = LogFactory.getLog(ObjectTableWriter.class);

    public enum Stage {
        PARSE_INPUT,
        LOAD_OBJECTS,
        CREATE_PRIMARY_KEY,
        CREATE_GEOMETRY_INDEX,
        FINISHED,
    }

    private final Connection connection;
    private final SQLDialect dialect;
    private final SchemaSQLMapper schemaSQLMapper;

    private int batchSize = 100;
    private boolean multithreading = true;
    private boolean usePgCopy = true;
    private Integer objectLimit = null;
    private boolean linearizeCurves = false;
    private boolean createSchema = false;
    private String tablePrefix = "";

    private Consumer<ObjectTableWriter.Progress> progressUpdater;

    private ObjectTableWriter.Progress progress = null;

    public class Progress {
        private Map<ObjectType, QueryBatch> insertBatches = new HashMap<>();

        private boolean initialLoad = true;

        private boolean firstObject = true;

        // If we are only inserting into a single table, we can use the faster Postgres COPY
        private boolean singleTableInserts = false;

        private final BlockingQueue<SchemaObjectInstance> objectsToWrite;

        private Stage stage = Stage.PARSE_INPUT;
        private long objectCount = 0;
        private Instant lastProgressUpdate = null;

        protected Progress() {
            int batchSize = getBatchSize();
            if (batchSize <= 0) {
                batchSize = 2500;
            }
            objectsToWrite = new ArrayBlockingQueue<>(batchSize);
        }

        public void setSingleTableInserts(boolean singleTableInserts) {
            this.singleTableInserts = singleTableInserts;
        }

        public void setInitialLoad(boolean initialLoad) {
            this.initialLoad = initialLoad;
        }

        public ObjectTableWriter getWriter() {
            return ObjectTableWriter.this;
        }

        public Stage getStage() {
            return stage;
        }

        public long getObjectCount() {
            return objectCount;
        }

        public void setObjectCount(long objectCount) {
            this.objectCount = objectCount;
        }

        public void incrementObjectCount() {
            this.objectCount++;
        }
    }

    public ObjectTableWriter(Connection connection, SQLDialect dialect, SchemaSQLMapper schemaSQLMapper) {
        this.connection = connection;
        this.dialect = dialect;
        this.schemaSQLMapper = schemaSQLMapper;
    }

    public Connection getConnection() {
        return connection;
    }

    public SQLDialect getDialect() {
        return dialect;
    }

    public SchemaSQLMapper getSchemaSQLMapper() {
        return schemaSQLMapper;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isMultithreading() {
        return multithreading;
    }

    public void setMultithreading(boolean multithreading) {
        this.multithreading = multithreading;
    }

    public boolean isUsePgCopy() {
        return usePgCopy;
    }

    public void setUsePgCopy(boolean usePgCopy) {
        this.usePgCopy = usePgCopy;
    }

    public Integer getObjectLimit() {
        return objectLimit;
    }

    public void setObjectLimit(Integer objectLimit) {
        this.objectLimit = objectLimit;
    }

    public boolean isLinearizeCurves() {
        return linearizeCurves;
    }

    public void setLinearizeCurves(boolean linearizeCurves) {
        this.linearizeCurves = linearizeCurves;
    }

    public boolean isCreateSchema() {
        return createSchema;
    }

    public void setCreateSchema(boolean createSchema) {
        this.createSchema = createSchema;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    public Consumer<Progress> getProgressUpdater() {
        return progressUpdater;
    }

    public void setProgressUpdater(Consumer<Progress> progressUpdater) {
        this.progressUpdater = progressUpdater;
    }

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    protected void prepareDatabaseForObject(SchemaObjectInstance object) throws Exception {
        // Do creation of QueryBatch on main thread, so exceptions stop processing immediately
        // This creates/truncates the table
        getInsertBatch(object);
    }

    protected synchronized QueryBatch getInsertBatch(SchemaObjectInstance object) throws Exception {
        Map<ObjectType,QueryBatch> insertBatches = progress.insertBatches;
        if(insertBatches.isEmpty()) {
            if (progress.initialLoad) {
                if (isCreateSchema()) {
                    QueryRunner qr = new QueryRunner();
                    for (String sql: Stream.concat(
                            schemaSQLMapper.getCreateTableStatements(object.getObjectType(), dialect, tablePrefix),
                            schemaSQLMapper.getCreateGeometryMetadataStatements(object.getObjectType(), dialect, tablePrefix)).collect(Collectors.toList())) {
                        qr.update(connection, sql);
                    }
                } else {
                    truncateTable(connection, object.getObjectType());
                    for (ObjectType oneToManyObjectType: object.getObjectType().getOneToManyAttributeObjectTypes()) {
                        truncateTable(connection, oneToManyObjectType);
                    }
                }
            }
        }

        if(!insertBatches.containsKey(object.getObjectType())) {
            QueryBatch queryBatch;
            if (this.dialect instanceof PostGISDialect && this.usePgCopy) {
                String sql = buildPgCopySql(object, progress.initialLoad);
                // Using Postgres COPY while buffering the copy stream is not faster than using batched inserts with the
                // reWriteBatchedInserts=true connection parameter. Buffering is required when a one-to-many attribute
                // exists, because simultaneous COPY statements (even with separate connections) are not supported by
                // the JDBC driver
                boolean bufferCopy = !progress.singleTableInserts;
                queryBatch = new PostGISCopyInsertBatch(connection, sql, batchSize, dialect, bufferCopy, linearizeCurves);
            } else {
                String sql = buildInsertSql(object);
                Boolean[] parameterIsGeometry = object.getObjectType().getDirectAttributes().stream()
                        .map(attributeColumnMapping -> attributeColumnMapping instanceof GeometryAttributeColumnMapping)
                        .toArray(Boolean[]::new);
                queryBatch = new GeometryHandlingPreparedStatementBatch(connection, sql, batchSize, dialect, parameterIsGeometry, linearizeCurves);
            }
            insertBatches.put(object.getObjectType(), queryBatch);
        }
        return insertBatches.get(object.getObjectType());
    }

    protected void addObjectToBatch(SchemaObjectInstance object) throws Exception {
        // Determine if we can use only a single table COPY without buffering
        if (progress.firstObject) {
            progress.setSingleTableInserts(progress.initialLoad && object.getObjectType().getOneToManyAttributeObjectTypes().isEmpty());
            progress.firstObject = false;
        }

        try {
            addObjectToBatch(object, false);
        } catch(Throwable e) {
            String message = "Exception writing object to database, object: ";
            if (getBatchSize() > 1) {
                message = "Exception adding parameters to database write batch, may be caused by previous batches. Object: ";
            }
            throw new Exception(message + object, e);
        }
    }

    private void addObjectToBatch(SchemaObjectInstance object, boolean fromWorkerThread) throws Throwable {

        if (multithreading && !fromWorkerThread) {
            while(exceptionFromWorkerThread == null) {
                // We can't use the blocking put() method because the worker thread may be interrupted because of an
                // exception and never drain the full queue
                if(progress.objectsToWrite.offer(object, 500, TimeUnit.MILLISECONDS)) {
                    return;
                }
            }
            throw exceptionFromWorkerThread;
        }

        // This always returns a value without creating the table because it has been called before (so no table
        // creation in worker thread)
        QueryBatch batch = getInsertBatch(object);

        Map<String, Object> attributes = object.getAttributes();
        List<Object> params = new ArrayList<>();
        for(AttributeColumnMapping attributeColumnMapping: object.getObjectType().getDirectAttributes()) {
            Object attribute = attributes.get(attributeColumnMapping.getName());
            params.add(attributeColumnMapping.toQueryParameter(attribute));
        }

        for(ObjectType oneToManyAttribute: object.getObjectType().getOneToManyAttributeObjectTypes()) {
            List<SchemaObjectInstance> objects = (List<SchemaObjectInstance>) attributes.get(oneToManyAttribute.getName());
            if (objects != null && !objects.isEmpty()) {
                for(int i = 0; i < objects.size(); i++) {
                    SchemaObjectInstance oneToMany = objects.get(i);
                    // Add FK and index
                    String tableName = schemaSQLMapper.getTableNameForObjectType(object.getObjectType(), "");
                    String idColumnName = object.getObjectType().getPrimaryKeys().get(0).getName();
                    oneToMany.getAttributes().put(schemaSQLMapper.getColumnNameForObjectType(oneToMany.getObjectType(),tableName + idColumnName), object.getAttributes().get(idColumnName));
                    oneToMany.getAttributes().put(Schema.INDEX, i);
                    // TODO call overridable method to add attributes to one to many
                    Object eindRegistratie = object.getAttributes().get("eindRegistratie");
                    oneToMany.getAttributes().put(schemaSQLMapper.getColumnNameForObjectType(oneToMany.getObjectType(),tableName + "eindRegistratie"), eindRegistratie != null);
                    addObjectToBatch(oneToMany, fromWorkerThread);
                }
            }
        }

        batch.addBatch(params.toArray());
        updateProgress();
    }

    private Throwable exceptionFromWorkerThread = null;

    private synchronized Throwable getExceptionFromWorkerThread() {
        return exceptionFromWorkerThread;
    }

    private synchronized void setExceptionFromWorkerThread(Throwable exceptionFromWorkerThread) {
        this.exceptionFromWorkerThread = exceptionFromWorkerThread;
    }

    private final Runnable worker = () -> {
        try {
            while (true) {
                List<SchemaObjectInstance> objects = new ArrayList<>(progress.objectsToWrite.size());
                objects.add(progress.objectsToWrite.take());
                progress.objectsToWrite.drainTo(objects);
                for(SchemaObjectInstance object: objects) {
                    // Check for end of objects marker
                    if (object.getObjectType() == null) {
                        return;
                    }
                    addObjectToBatch(object, true);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Throwable e) {
            log.error("Exception in object writing thread", e);
            setExceptionFromWorkerThread(e);
        }
    };

    protected void updateProgress(Stage stage) {
        this.progress.stage = stage;
        updateProgress(true);
    }

    protected void updateProgress() {
        updateProgress(false);
    }

    private void updateProgress(boolean always) {
        if (progressUpdater == null) {
            return;
        }
        boolean timeForProgress = progress.lastProgressUpdate == null ||
                (progress.objectCount % 500 == 0 && Duration.between(progress.lastProgressUpdate, Instant.now()).getNano() > 250e6);
        if (always || timeForProgress) {
            progressUpdater.accept(progress);
            progress.lastProgressUpdate = Instant.now();
        }
    }

    private void truncateTable(Connection c, ObjectType objectType) throws SQLException {
        // TODO like jdbc-util, truncate may fail but 'delete from' may succeed
        new QueryRunner().execute(c, String.format("truncate table %s", schemaSQLMapper.getTableNameForObjectType(objectType, tablePrefix)));
    }

    private String buildInsertSql(SchemaObjectInstance object) {
        StringBuilder sql = new StringBuilder("insert into ");
        String tableName = schemaSQLMapper.getTableNameForObjectType(object.getObjectType(), tablePrefix);
        sql.append(tableName).append("(");
        sql.append(buildColumnList(object.getObjectType()));
        sql.append(") values (");
        String paramPlaceholders = object.getObjectType().getDirectAttributes().stream()
                .map(c -> "?")
                .collect(Collectors.joining(", "));
        sql.append(paramPlaceholders).append(")");
        return sql.toString();
    }

    private String buildPgCopySql(SchemaObjectInstance object, boolean initialLoad) {
        String tableName = schemaSQLMapper.getTableNameForObjectType(object.getObjectType(), tablePrefix);
        String copySql = "copy " + tableName + "(" + buildColumnList(object.getObjectType()) + ") from stdin";
        return copySql + (initialLoad ? " with freeze" : "");
    }

    private String buildColumnList(ObjectType objectType) {
        return objectType.getDirectAttributes().stream()
                .map(column -> schemaSQLMapper.getColumnNameForObjectType(objectType, column.getName()))
                .collect(Collectors.joining(", "));
    }

    private Thread workerThread = null;

    protected void start(Progress progress) throws SQLException {
        this.progress = progress;

        getConnection().setAutoCommit(false);

        if (multithreading) {
            workerThread = new Thread(worker);
            workerThread.start();
        }
    }

    protected void endOfObjects() throws Exception {
        if (workerThread != null) {
            // Signal end of objects to thread
            progress.objectsToWrite.put(new SchemaObjectInstance(null, Collections.emptyMap()));
            workerThread.join();
        }

        for(QueryBatch batch: progress.insertBatches.values()) {
            batch.executeBatch();
        }
    }

    protected void complete() throws SQLException {
        if (isCreateSchema() && progress.initialLoad) {
            QueryRunner qr = new QueryRunner();
            updateProgress(Stage.CREATE_PRIMARY_KEY);
            for(ObjectType objectType: progress.insertBatches.keySet()) {
                for (String sql: schemaSQLMapper.getCreatePrimaryKeyStatements(objectType, dialect, tablePrefix,false).collect(Collectors.toList())) {
                    qr.update(connection, sql);
                }
            }
            updateProgress(Stage.CREATE_GEOMETRY_INDEX);
            for(ObjectType objectType: progress.insertBatches.keySet()) {
                for(String sql: schemaSQLMapper.getCreateGeometryIndexStatements(objectType, dialect, tablePrefix, false).collect(Collectors.toList())) {
                    qr.update(connection, sql);
                }
            }
        }

        connection.commit();
        updateProgress(Stage.FINISHED);
    }

    protected void closeBatches() {
        progress.insertBatches.values().forEach(QueryBatch::closeQuietly);
    }
}
