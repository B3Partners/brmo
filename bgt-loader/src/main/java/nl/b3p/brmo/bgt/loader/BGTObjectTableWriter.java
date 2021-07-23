/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package nl.b3p.brmo.bgt.loader;

import nl.b3p.brmo.sql.GeometryHandlingPreparedStatementBatch;
import nl.b3p.brmo.sql.PostGISCopyInsertBatch;
import nl.b3p.brmo.sql.PreparedStatementQueryBatch;
import nl.b3p.brmo.sql.QueryBatch;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import nl.b3p.brmo.sql.mapping.AttributeColumnMapping;
import nl.b3p.brmo.sql.mapping.GeometryAttributeColumnMapping;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.b3p.brmo.bgt.loader.BGTObject.MutatieStatus.WAS_WORDT;
import static nl.b3p.brmo.bgt.loader.BGTObject.MutatieStatus.WORDT;
import static nl.b3p.brmo.bgt.loader.BGTSchema.EIND_REGISTRATIE;
import static nl.b3p.brmo.bgt.loader.BGTSchemaMapper.getColumnNameForObjectType;
import static nl.b3p.brmo.bgt.loader.BGTSchemaMapper.getCreateGeometryIndexStatements;
import static nl.b3p.brmo.bgt.loader.BGTSchemaMapper.getCreateGeometryMetadataStatements;
import static nl.b3p.brmo.bgt.loader.BGTSchemaMapper.getCreatePrimaryKeyStatements;
import static nl.b3p.brmo.bgt.loader.BGTSchemaMapper.getCreateTableStatements;
import static nl.b3p.brmo.bgt.loader.BGTSchemaMapper.getTableNameForObjectType;

public class BGTObjectTableWriter {
    private static final Log log = LogFactory.getLog(BGTObjectTableWriter.class);

    public enum Stage {
        PARSE_INHOUD,
        LOAD_OBJECTS,
        CREATE_PRIMARY_KEY,
        CREATE_GEOMETRY_INDEX,
        FINISHED,
    };

    private final Connection connection;
    private final SQLDialect dialect;

    private int batchSize = 100;
    private boolean multithreading = true;
    private boolean usePgCopy = true;
    private Integer objectLimit = null;
    private boolean linearizeCurves = false;
    private boolean currentObjectsOnly = true;
    private boolean createSchema = false;
    private String tablePrefix = null;

    private Consumer<Progress> progressUpdater;

    private Progress progress = null;

    public class Progress {
        private CountingInputStream counter;
        private Map<String, QueryBatch> insertBatches = new HashMap<>();
        private Map<String, QueryBatch> deleteBatches = new HashMap<>();

        // If we are only inserting into a single table, we can use the faster Postgres COPY
        private boolean singleTableInserts = false;

        private final BlockingQueue<BGTObject> bgtObjects;

        private Stage stage = Stage.PARSE_INHOUD;
        private long objectCount = 0;
        private long objectUpdatedCount = 0;
        private long objectRemovedCount = 0;
        private long historicObjectsCount = 0;
        private Instant lastProgressUpdate = null;
        private BGTObjectStreamer.MutatieInhoud mutatieInhoud;

        private Progress(int batchSize) {
            if (batchSize <= 0) {
                batchSize = 2500;
            }
            bgtObjects = new ArrayBlockingQueue<>(batchSize);
        }

        public BGTObjectTableWriter getWriter() {
            return BGTObjectTableWriter.this;
        }

        public Stage getStage() {
            return stage;
        }

        public long getObjectCount() {
            return objectCount;
        }

        public long getObjectUpdatedCount() {
            return objectUpdatedCount;
        }

        public long getObjectRemovedCount() {
            return objectRemovedCount;
        }

        public long getHistoricObjectsCount() {
            return historicObjectsCount;
        }

        public long getBytesRead() {
            return counter.getByteCount();
        }

        public BGTObjectStreamer.MutatieInhoud getMutatieInhoud() {
            return mutatieInhoud;
        }
    }

    public BGTObjectTableWriter(Connection connection, SQLDialect dialect) {
        this.connection = connection;
        this.dialect = dialect;
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

    public boolean isCurrentObjectsOnly() {
        return currentObjectsOnly;
    }

    public void setCurrentObjectsOnly(boolean currentObjectsOnly) {
        this.currentObjectsOnly = currentObjectsOnly;
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

    private synchronized QueryBatch getInsertBatch(BGTObject object, boolean initialLoad) throws Exception {
        Map<String,QueryBatch> insertBatches = progress.insertBatches;
        if(insertBatches.isEmpty()) {
            if (initialLoad) {
                if (isCreateSchema()) {
                    QueryRunner qr = new QueryRunner();
                    for (String sql: Stream.concat(
                            getCreateTableStatements(object.getObjectType(), dialect, tablePrefix),
                            getCreateGeometryMetadataStatements(object.getObjectType(), dialect, tablePrefix)).collect(Collectors.toList())) {
                        qr.update(connection, sql);
                    }
                } else {
                    truncateTable(connection, object.getObjectType());
                    for (BGTSchema.BGTObjectType oneToManyObjectType: object.getObjectType().getOneToManyAttributeObjectTypes()) {
                        truncateTable(connection, oneToManyObjectType);
                    }
                }
            }
        }

        if(!insertBatches.containsKey(object.getObjectType().getName())) {
            QueryBatch queryBatch;
            if (this.dialect instanceof PostGISDialect && this.usePgCopy) {
                String sql = buildPgCopySql(object, initialLoad);
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
            insertBatches.put(object.getObjectType().getName(), queryBatch);
        }
        return insertBatches.get(object.getObjectType().getName());
    }

    private void addObjectToBatch(BGTObject object, boolean initialLoad) throws Throwable {
        addObjectToBatch(object, initialLoad, false);
    }

    private void addObjectToBatch(BGTObject object, boolean initialLoad, boolean fromWorkerThread) throws Throwable {
        if (multithreading && !fromWorkerThread) {
            if (getExceptionFromWorkerThread() != null) {
                throw getExceptionFromWorkerThread();
            }

            progress.bgtObjects.put(object);
            return;
        }

        // This always returns a value without creating the table because it has been called before (so no table
        // creation in worker thread)
        QueryBatch batch = getInsertBatch(object, initialLoad);

        Map<String, Object> attributes = object.getAttributes();
        List<Object> params = new ArrayList<>();
        for(AttributeColumnMapping attributeColumnMapping: object.getObjectType().getDirectAttributes()) {
            Object attribute = attributes.get(attributeColumnMapping.getName());
            params.add(attributeColumnMapping.toQueryParameter(attribute));
        }

        for(BGTSchema.BGTObjectType oneToManyAttribute: object.getObjectType().getOneToManyAttributeObjectTypes()) {
            List<BGTObject> objects = (List<BGTObject>) attributes.get(oneToManyAttribute.getName());
            if (objects != null && !objects.isEmpty()) {
                for(int i = 0; i < objects.size(); i++) {
                    BGTObject oneToMany = objects.get(i);
                    // Add FK and index
                    String tableName = getTableNameForObjectType(object.getObjectType(), "");
                    String idColumnName = object.getObjectType().getPrimaryKeys().get(0).getName();
                    oneToMany.getAttributes().put(getColumnNameForObjectType(oneToMany.getObjectType(),tableName + idColumnName), object.getAttributes().get(idColumnName));
                    oneToMany.getAttributes().put(BGTSchema.INDEX, i);
                    Object eindRegistratie = object.getAttributes().get("eindRegistratie");
                    oneToMany.getAttributes().put(getColumnNameForObjectType(oneToMany.getObjectType(),tableName + "eindRegistratie"), eindRegistratie != null);
                    addObjectToBatch(oneToMany, initialLoad, fromWorkerThread);
                }
            }
        }

        batch.addBatch(params.toArray());
        updateProgress();
    }

    private void deletePreviousVersion(BGTObject object) throws Exception {
        BGTSchema.BGTObjectType objectType = object.getObjectType();
        String idAttributeName = objectType.getPrimaryKeys().get(0).getName();
        String tableName = getTableNameForObjectType(objectType, tablePrefix);
        Map<String,QueryBatch> deleteBatches = progress.deleteBatches;
        if(!deleteBatches.containsKey(objectType.getName())) {
            String sql = "delete from " + tableName + " where " + getColumnNameForObjectType(objectType, idAttributeName) + " = ?";
            deleteBatches.put(objectType.getName(), new PreparedStatementQueryBatch(connection, sql, batchSize));
        }
        QueryBatch batch = deleteBatches.get(objectType.getName());

        boolean executed = batch.addBatch(new Object[] {object.getMutatiePreviousVersionGmlId()});

        for(BGTSchema.BGTObjectType oneToManyObjectType: objectType.getOneToManyAttributeObjectTypes()) {
            if(!deleteBatches.containsKey(oneToManyObjectType.getName())) {
                String tableNameNoPrefix =  getTableNameForObjectType(objectType, "");
                String sql = "delete from " + getTableNameForObjectType(oneToManyObjectType, tablePrefix) + " where "
                        + getColumnNameForObjectType(oneToManyObjectType, tableNameNoPrefix + idAttributeName) + " = ?";
                deleteBatches.put(oneToManyObjectType.getName(), new PreparedStatementQueryBatch(connection, sql, batchSize));
            }
            QueryBatch deleteBatch = deleteBatches.get(oneToManyObjectType.getName());
            executed = executed | deleteBatch.addBatch(new Object[] {object.getMutatiePreviousVersionGmlId()});
        }

        if (executed) {
            updateProgress();
        }
    }

    private Throwable exceptionFromWorkerThread = null;

    public synchronized Throwable getExceptionFromWorkerThread() {
        return exceptionFromWorkerThread;
    }

    public synchronized void setExceptionFromWorkerThread(Throwable exceptionFromWorkerThread) {
        this.exceptionFromWorkerThread = exceptionFromWorkerThread;
    }

    private final Runnable worker = () -> {
        try {
            while (true) {
                List<BGTObject> objects = new ArrayList<>(progress.bgtObjects.size());
                objects.add(progress.bgtObjects.take());
                progress.bgtObjects.drainTo(objects);
                for(BGTObject object: objects) {
                    if (object.getObjectType() == null) {
                        return;
                    }
                    addObjectToBatch(object, false, true);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Throwable e) {
            log.error("Exception in object writing thread", e);
            setExceptionFromWorkerThread(e);
        }
    };

    public void write(InputStream bgtXml) throws Exception {
        this.progress = this.new Progress(batchSize);
        updateProgress(Stage.PARSE_INHOUD);
        connection.setAutoCommit(false);

        try(CountingInputStream counter = new CountingInputStream(bgtXml)) {
            progress.counter = counter;

            BGTObjectStreamer streamer = new BGTObjectStreamer(counter);
            progress.mutatieInhoud = streamer.getMutatieInhoud();
            updateProgress(Stage.LOAD_OBJECTS);
            boolean initialLoad = progress.mutatieInhoud == null || "initial".equals(progress.getMutatieInhoud().getMutatieType());
            boolean first = true;
            Thread workerThread = null;
            if (multithreading) {
                workerThread = new Thread(worker);
                workerThread.start();
            }
            for (BGTObject object: streamer) {
                // Do creation of QueryBatch on main thread, so exceptions stop processing immediately
                // This creates/truncates the table. Do it even if all objects are historic (such as can happen with
                // "ongeclassificeerdobject")
                getInsertBatch(object, initialLoad);

                // Determine if we can use only a single table COPY without buffering
                if (first) {
                    progress.singleTableInserts = initialLoad && object.getObjectType().getOneToManyAttributeObjectTypes().isEmpty();
                    first = false;
                }

                boolean skipHistoricObject = object.getAttributes().get(EIND_REGISTRATIE) != null && currentObjectsOnly;
                if (object.getMutatieStatus() == WAS_WORDT) {
                    // Deletes and new versions do not need to be DELETE'd and INSERT-ed in order

                    // Also delete oneToMany
                    deletePreviousVersion(object);

                    if (skipHistoricObject) {
                        progress.objectRemovedCount++;
                    } else {
                        progress.objectUpdatedCount++;
                    }
                } else if(object.getMutatieStatus() == WORDT) {
                    if (skipHistoricObject) {
                        progress.historicObjectsCount++;
                    } else {
                        progress.objectCount++;
                    }
                }

                if (skipHistoricObject) {
                    continue;
                }

                try {
                    addObjectToBatch(object, initialLoad);
                } catch(Throwable e) {
                    String message = "Exception writing object to database, BGT object: ";
                    if (batchSize > 1) {
                        message = "Exception adding parameters to database write batch, may be caused by previous batches. BGT object: ";
                    }
                    throw new Exception(message + object, e);
                }

                if (objectLimit != null && progress.objectCount == objectLimit) {
                    break;
                }
            }

            if (workerThread != null) {
                // Signal end of objects to thread
                progress.bgtObjects.put(new BGTObject(null, Collections.emptyMap()));
                workerThread.join();
            }

            Stream.concat(progress.insertBatches.values().stream(), progress.deleteBatches.values().stream()).forEach(batch -> {
                try {
                    batch.executeBatch();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            if (isCreateSchema() && initialLoad) {
                QueryRunner qr = new QueryRunner();
                updateProgress(Stage.CREATE_PRIMARY_KEY);
                for(String name: progress.insertBatches.keySet()) {
                    for (String sql: getCreatePrimaryKeyStatements(BGTSchema.getObjectTypeByName(name), dialect, tablePrefix,false).collect(Collectors.toList())) {
                        qr.update(connection, sql);
                    }
                }
                updateProgress(Stage.CREATE_GEOMETRY_INDEX);
                for(String name: progress.insertBatches.keySet()) {
                    for(String sql: getCreateGeometryIndexStatements(BGTSchema.getObjectTypeByName(name), dialect, tablePrefix, false).collect(Collectors.toList())) {
                        qr.update(connection, sql);
                    }
                }
            }
            connection.commit();
            updateProgress(Stage.FINISHED);
        } finally {
            Stream.concat(progress.insertBatches.values().stream(), progress.deleteBatches.values().stream()).forEach(batch -> {
                try {
                    batch.close();
                } catch (Exception ignored) {
                }
            });
        }
    }

    private void updateProgress(Stage stage) {
        this.progress.stage = stage;
        updateProgress(true);
    }

    private void updateProgress() {
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

    private void truncateTable(Connection c, BGTSchema.BGTObjectType objectType) throws SQLException {
        // TODO like jdbc-util, truncate may fail but 'delete from' may succeed
        new QueryRunner().execute(c, String.format("truncate table %s", getTableNameForObjectType(objectType, tablePrefix)));
    }

    private String buildInsertSql(BGTObject object) {
        StringBuilder sql = new StringBuilder("insert into ");
        String tableName = getTableNameForObjectType(object.getObjectType(), tablePrefix);
        sql.append(tableName).append("(");
        sql.append(buildColumnList(object));
        sql.append(") values (");
        String paramPlaceholders = object.getObjectType().getDirectAttributes().stream()
                .map(c -> "?")
                .collect(Collectors.joining(", "));
        sql.append(paramPlaceholders).append(")");
        return sql.toString();
    }

    private String buildPgCopySql(BGTObject object, boolean initialLoad) {
        String copySql = "copy " + getTableNameForObjectType(object.getObjectType(), tablePrefix) + "(" + buildColumnList(object) + ") from stdin";
        return copySql + (initialLoad ? " with freeze" : "");
    }

    private static String buildColumnList(BGTObject object) {
        return object.getObjectType().getDirectAttributes().stream()
                .map(column -> getColumnNameForObjectType(object.getObjectType(), column.getName()))
                .collect(Collectors.joining(", "));
    }
}
