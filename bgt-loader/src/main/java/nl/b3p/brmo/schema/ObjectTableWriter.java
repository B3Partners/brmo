/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.schema;

import nl.b3p.brmo.schema.mapping.ArrayAttributeMapping;
import nl.b3p.brmo.schema.mapping.AttributeColumnMapping;
import nl.b3p.brmo.schema.mapping.GeometryAttributeColumnMapping;
import nl.b3p.brmo.sql.GeometryHandlingPreparedStatementBatch;
import nl.b3p.brmo.sql.LoggingQueryRunner;
import nl.b3p.brmo.sql.PostGISCopyInsertBatch;
import nl.b3p.brmo.sql.QueryBatch;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private boolean dropIfExists = true;
    private boolean createKeysAndIndexes = true;
    private String tablePrefix = "";

    private Consumer<ObjectTableWriter.Progress> progressUpdater;

    private ObjectTableWriter.Progress progress = null;

    public class Progress {
        private Map<ObjectType, QueryBatch> insertBatches = new HashMap<>();
        private Map<Pair<ObjectType, ArrayAttributeMapping>, QueryBatch> arrayAttributeInsertBatches = new HashMap<>();

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

    public boolean isDropIfExists() {
        return dropIfExists;
    }

    public void setDropIfExists(boolean dropIfExists) {
        this.dropIfExists = dropIfExists;
    }

    public boolean isCreateKeysAndIndexes() {
        return createKeysAndIndexes;
    }

    public void setCreateKeysAndIndexes(boolean createKeysAndIndexes) {
        this.createKeysAndIndexes = createKeysAndIndexes;
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
                    QueryRunner qr = new LoggingQueryRunner();
                    for (String sql: Stream.concat(
                            schemaSQLMapper.getCreateTableStatements(object.getObjectType(), dialect, tablePrefix, dropIfExists),
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
                Boolean[] parameterIsGeometry = object.getObjectType().getDirectNonDefaultInsertAttributes().stream()
                        .map(attributeColumnMapping -> attributeColumnMapping instanceof GeometryAttributeColumnMapping)
                        .toArray(Boolean[]::new);
                queryBatch = new GeometryHandlingPreparedStatementBatch(connection, sql, batchSize, dialect, parameterIsGeometry, linearizeCurves);
            }
            insertBatches.put(object.getObjectType(), queryBatch);
        }
        return insertBatches.get(object.getObjectType());
    }

    protected synchronized QueryBatch getArrayAttributeInsertBatch(SchemaObjectInstance object, ArrayAttributeMapping attribute) throws Exception {
        Map<Pair<ObjectType,ArrayAttributeMapping>,QueryBatch> insertBatches = progress.arrayAttributeInsertBatches;

        Pair<ObjectType,ArrayAttributeMapping> batchKey = ImmutablePair.of(object.getObjectType(), attribute);
        if(!insertBatches.containsKey(batchKey)) {
            QueryBatch queryBatch;
/*            if (this.dialect instanceof PostGISDialect && this.usePgCopy) {
                String sql = buildPgCopySql(object, progress.initialLoad);
                // Using Postgres COPY while buffering the copy stream is not faster than using batched inserts with the
                // reWriteBatchedInserts=true connection parameter. Buffering is required when a one-to-many attribute
                // exists, because simultaneous COPY statements (even with separate connections) are not supported by
                // the JDBC driver
                boolean bufferCopy = !progress.singleTableInserts;
                queryBatch = new PostGISCopyInsertBatch(connection, sql, batchSize, dialect, bufferCopy, linearizeCurves);
            } else {*/
                String sql = buildInsertSql(object.getObjectType(), attribute);
                Boolean[] parameterIsGeometry = object.getObjectType().getDirectNonDefaultInsertAttributes().stream()
                        .map(attributeColumnMapping -> attributeColumnMapping instanceof GeometryAttributeColumnMapping)
                        .toArray(Boolean[]::new);
                queryBatch = new GeometryHandlingPreparedStatementBatch(connection, sql, batchSize, dialect, parameterIsGeometry, linearizeCurves);
//            }
            insertBatches.put(batchKey, queryBatch);
        }
        return insertBatches.get(batchKey);
    }

    protected void addObjectToBatch(SchemaObjectInstance object) throws Exception {
        // Determine if we can use only a single table COPY without buffering
        if (progress.firstObject) {
            progress.setSingleTableInserts(progress.initialLoad && object.getObjectType().hasOnlyDirectAttributes());
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
        for(AttributeColumnMapping attributeColumnMapping: object.getObjectType().getDirectNonDefaultInsertAttributes()) {
            Object attribute = attributes.get(attributeColumnMapping.getName());
            params.add(attributeColumnMapping.toQueryParameter(attribute));
        }

        // The main object batch must be executed before oneToMany and arrayAttribute batches which reference the main
        // object when foreign key constraints are in place. During "stand" loading this is deferrered to after all
        // batches are fully executed so we don't need to care about order. For BAG2 mutaties batch size is set to 1 to
        // correctly order all batches for foreign key integrity
        batch.addBatch(params.toArray());

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

        for(ArrayAttributeMapping arrayAttribute: object.getObjectType().getArrayAttributes()) {
            insertArrayAttribute(object, arrayAttribute);
        }

        updateProgress();
    }

    private void insertArrayAttribute(SchemaObjectInstance object, ArrayAttributeMapping attribute) throws Exception {
        Set values = (Set) object.getAttributes().get(attribute.getName());
        if (values != null && !values.isEmpty()) {
            QueryBatch insertBatch = getArrayAttributeInsertBatch(object, attribute);
            Object[] keys = object.getObjectType().getPrimaryKeys().stream()
                    .map(key -> {
                        try {
                            return key.toQueryParameter(object.getAttributes().get(key.getName()));
                        } catch(Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray();
            for (Object value: values) {
                Object[] params = new Object[keys.length+1];
                System.arraycopy(keys, 0, params, 0, keys.length);
                params[params.length-1] = value;
                insertBatch.addBatch(params);
            }
        }
    }

    private Throwable exceptionFromWorkerThread = null;

    private synchronized Throwable getExceptionFromWorkerThread() {
        return exceptionFromWorkerThread;
    }

    private synchronized void setExceptionFromWorkerThread(Throwable exceptionFromWorkerThread) {
        this.exceptionFromWorkerThread = exceptionFromWorkerThread;
    }

    public void abortWorkerThread() throws Exception {
        if (workerThread != null) {
            // Remove all objects already queued
            progress.objectsToWrite.clear();
            endOfObjects();
        }
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
        if (this.progress != null) {
            this.progress.stage = stage;
            updateProgress(true);
        }
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
        new LoggingQueryRunner().execute(c, String.format("truncate table %s", schemaSQLMapper.getTableNameForObjectType(objectType, tablePrefix)));
    }

    private String buildInsertSql(SchemaObjectInstance object) {
        StringBuilder sql = new StringBuilder("insert into ");
        String tableName = schemaSQLMapper.getTableNameForObjectType(object.getObjectType(), tablePrefix);
        sql.append(tableName).append("(");
        sql.append(buildColumnList(object.getObjectType()));
        sql.append(") values (");
        String paramPlaceholders = object.getObjectType().getDirectNonDefaultInsertAttributes().stream()
                .map(c -> "?")
                .collect(Collectors.joining(", "));
        sql.append(paramPlaceholders).append(")");
        return sql.toString();
    }

    private String buildInsertSql(ObjectType objectType, ArrayAttributeMapping attribute) {
        String referencingKeys = objectType.getPrimaryKeys().stream()
                .map(pk -> schemaSQLMapper.getColumnNameForObjectType(objectType, pk.getName()))
                .collect(Collectors.joining(", "));
        return String.format("insert into %s (%s, %s) values (%s)",
                schemaSQLMapper.getTableNameForArrayAttribute(objectType, attribute, tablePrefix),
                referencingKeys,
                schemaSQLMapper.getColumnNameForObjectType(objectType, attribute.getName()),
                String.join(", ", Collections.nCopies(objectType.getPrimaryKeys().size() + 1, "?"))
        );
    }

    private String buildPgCopySql(SchemaObjectInstance object, boolean initialLoad) {
        String tableName = schemaSQLMapper.getTableNameForObjectType(object.getObjectType(), tablePrefix);
        String copySql = "copy " + tableName + "(" + buildColumnList(object.getObjectType()) + ") from stdin";
        return copySql + (initialLoad ? " with freeze" : "");
    }

    private String buildColumnList(ObjectType objectType) {
        return objectType.getDirectNonDefaultInsertAttributes().stream()
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
    }

    protected void complete() throws Exception {
        for(QueryBatch batch: progress.insertBatches.values()) {
            batch.executeBatch();
        }

        for(QueryBatch batch: progress.arrayAttributeInsertBatches.values()) {
            batch.executeBatch();
        }

        if (isCreateSchema() && isCreateKeysAndIndexes() && progress.initialLoad) {
            for(ObjectType objectType: progress.insertBatches.keySet()) {
                createKeys(objectType);
            }
            for(ObjectType objectType: progress.insertBatches.keySet()) {
                createIndexes(objectType);
            }
        }

        connection.commit();
        updateProgress(Stage.FINISHED);
    }

    public void createKeys(ObjectType objectType) throws Exception {
        QueryRunner qr = new LoggingQueryRunner();
        updateProgress(Stage.CREATE_PRIMARY_KEY);
        // XXX why includeOneToMany is false here?
        for (String sql: schemaSQLMapper.getCreatePrimaryKeyStatements(objectType, dialect, tablePrefix, false).collect(Collectors.toList())) {
            qr.update(connection, sql);
        }
    }

    public void createIndexes(ObjectType objectType) throws Exception {
        QueryRunner qr = new LoggingQueryRunner();
        updateProgress(Stage.CREATE_GEOMETRY_INDEX);
        // XXX why includeOneToMany is false here?
        for(String sql: schemaSQLMapper.getCreateGeometryIndexStatements(objectType, dialect, tablePrefix, false).collect(Collectors.toList())) {
            qr.update(connection, sql);
        }
    }

    protected void closeBatches() {
        progress.insertBatches.values().forEach(QueryBatch::closeQuietly);
        progress.arrayAttributeInsertBatches.values().forEach(QueryBatch::closeQuietly);
    }
}
