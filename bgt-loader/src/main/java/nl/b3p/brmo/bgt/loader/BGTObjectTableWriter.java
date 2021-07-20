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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private boolean usePgCopy = true;
    private Integer objectLimit = null;
    private boolean linearizeCurves = false;
    private boolean currentObjectsOnly = true;
    private boolean createSchema = false;

    private CountingInputStream counter;
    private Map<String, QueryBatch> insertBatches = new HashMap<>();
    private Map<String, QueryBatch> deleteBatches = new HashMap<>();

    private Stage stage = Stage.LOAD_OBJECTS;
    private long objectCount = 0;
    private long objectUpdatedCount = 0;
    private long objectRemovedCount = 0;
    private long historicObjectsCount = 0;
    private Instant lastProgress = null;
    private BGTObjectStreamer.MutatieInhoud mutatieInhoud;

    private Runnable progressUpdater;

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

    public BGTObjectStreamer.MutatieInhoud getMutatieInhoud() {
        return mutatieInhoud;
    }

    public Runnable getProgressUpdater() {
        return progressUpdater;
    }

    public void setProgressUpdater(Runnable progressUpdater) {
        this.progressUpdater = progressUpdater;
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

    private void addObjectToBatch(BGTObject object, boolean initialLoad) throws Exception {
        if(insertBatches.isEmpty()) {
            // Create / truncate the table for the object and possible one-to-many tables (can't do one-to-many table
            // later, as a Postgres COPY will block)

            if (initialLoad) {
                if (isCreateSchema()) {
                    QueryRunner qr = new QueryRunner();
                    for (String sql: Stream.concat(
                            getCreateTableStatements(object.getObjectType(), dialect),
                            getCreateGeometryMetadataStatements(object.getObjectType(), dialect)).collect(Collectors.toList())) {
                        qr.update(connection, sql);
                    }
                } else {
                    truncateTable(connection, object.getObjectType());
                    for (BGTSchema.BGTObjectType oneToManyObjectType: object.getObjectType().getOneToManyAttributeObjectTypes().collect(Collectors.toList())) {
                        truncateTable(connection, oneToManyObjectType);
                    }
                }
            }
        }

        if(!insertBatches.containsKey(object.getObjectType().getName())) {
            QueryBatch queryBatch;
            if (this.dialect instanceof PostGISDialect && this.usePgCopy) {
                String sql = buildPgCopySql(object, initialLoad);
                queryBatch = new PostGISCopyInsertBatch(connection, sql, batchSize, dialect, linearizeCurves);
            } else {
                String sql = buildInsertSql(object);
                Boolean[] parameterIsGeometry = object.getObjectType().getDirectAttributes()
                        .map(attributeColumnMapping -> attributeColumnMapping instanceof GeometryAttributeColumnMapping)
                        .toArray(Boolean[]::new);
                queryBatch = new GeometryHandlingPreparedStatementBatch(connection, sql, batchSize, dialect, parameterIsGeometry, linearizeCurves);
            }
            insertBatches.put(object.getObjectType().getName(), queryBatch);
        }
        QueryBatch batch = insertBatches.get(object.getObjectType().getName());

        Map<String, Object> attributes = object.getAttributes();

        List<Object> params = new ArrayList<>();
        for(AttributeColumnMapping attributeColumnMapping: object.getObjectType().getDirectAttributes().collect(Collectors.toList())) {
            Object attribute = attributes.get(attributeColumnMapping.getName());
            params.add(attributeColumnMapping.toQueryParameter(attribute));
        }

        for(BGTSchema.BGTObjectType oneToManyAttribute: object.getObjectType().getOneToManyAttributeObjectTypes().collect(Collectors.toList())) {
            List<BGTObject> objects = (List<BGTObject>) attributes.get(oneToManyAttribute.getName());
            if (objects != null && !objects.isEmpty()) {
                for(int i = 0; i < objects.size(); i++) {
                    BGTObject oneToMany = objects.get(i);
                    // Add FK and index
                    String tableName = getTableNameForObjectType(object.getObjectType());
                    String idColumnName = object.getObjectType().getPrimaryKeys().findFirst().get().getName();
                    oneToMany.getAttributes().put(getColumnNameForObjectType(oneToMany.getObjectType(),tableName + idColumnName), object.getAttributes().get(idColumnName));
                    oneToMany.getAttributes().put(BGTSchema.INDEX, i);
                    Object eindRegistratie = object.getAttributes().get("eindRegistratie");
                    oneToMany.getAttributes().put(getColumnNameForObjectType(oneToMany.getObjectType(),tableName + "eindRegistratie"), eindRegistratie != null);
                    addObjectToBatch(oneToMany, initialLoad);
                }
            }
        }

        boolean executed = batch.addBatch(params.toArray());

        if (executed && progressUpdater != null) {
            progressUpdater.run();
        }
/*
        if (progressUpdater != null && this.objectCount % 500 == 0) {
            if (lastProgress == null || Duration.between(lastProgress, Instant.now()).getNano() > 500e6) {
                progressUpdater.run();
                lastProgress = Instant.now();
            }
        }
*/
    }

    private void deletePreviousVersion(BGTObject object) throws Exception {
        BGTSchema.BGTObjectType objectType = object.getObjectType();
        String idAttributeName = objectType.getPrimaryKeys().findFirst().get().getName();
        String tableName = getTableNameForObjectType(objectType);
        if(!deleteBatches.containsKey(objectType.getName())) {
            String sql = "delete from " + tableName + " where " + getColumnNameForObjectType(objectType, idAttributeName) + " = ?";
            deleteBatches.put(objectType.getName(), new PreparedStatementQueryBatch(connection, sql, batchSize));
        }
        QueryBatch batch = deleteBatches.get(objectType.getName());

        boolean executed = batch.addBatch(new Object[] {object.getMutatiePreviousVersionGmlId()});

        for(BGTSchema.BGTObjectType oneToManyObjectType: objectType.getOneToManyAttributeObjectTypes().collect(Collectors.toList())) {
            if(!deleteBatches.containsKey(oneToManyObjectType.getName())) {
                String sql = "delete from " + getTableNameForObjectType(oneToManyObjectType) + " where "
                        + getColumnNameForObjectType(oneToManyObjectType, tableName + idAttributeName) + " = ?";
                deleteBatches.put(oneToManyObjectType.getName(), new PreparedStatementQueryBatch(connection, sql, batchSize));
            }
            QueryBatch deleteBatch = deleteBatches.get(oneToManyObjectType.getName());
            executed = executed | deleteBatch.addBatch(new Object[] {object.getMutatiePreviousVersionGmlId()});
        }

        if (executed && progressUpdater != null) {
            progressUpdater.run();
        }
    }

    public void write(InputStream bgtXml) throws Exception {
        this.objectCount = 0;
        this.objectUpdatedCount = 0;
        this.objectRemovedCount = 0;
        this.historicObjectsCount = 0;
        this.lastProgress = null;
        this.mutatieInhoud = null;
        updateProgress(Stage.PARSE_INHOUD);
        connection.setAutoCommit(false);

        try(CountingInputStream counter = new CountingInputStream(bgtXml)) {
            this.counter = counter;

            BGTObjectStreamer streamer = new BGTObjectStreamer(counter);
            this.mutatieInhoud = streamer.getMutatieInhoud();
            updateProgress(Stage.LOAD_OBJECTS);
            boolean initialLoad = this.mutatieInhoud == null || "initial".equals(this.getMutatieInhoud().getMutatieType());
            for (BGTObject object: streamer) {
                boolean skipHistoricObject = object.getAttributes().get(EIND_REGISTRATIE) != null && currentObjectsOnly;
                if (object.getMutatieStatus() == WAS_WORDT) {
                    // Deletes and new versions do not need to be DELETE'd and INSERT-ed in order

                    // Also delete oneToMany
                    deletePreviousVersion(object);

                    if (skipHistoricObject) {
                        this.objectRemovedCount++;
                    } else {
                        this.objectUpdatedCount++;
                    }
                } else if(object.getMutatieStatus() == WORDT) {
                    if (skipHistoricObject) {
                        this.historicObjectsCount++;
                    } else {
                        this.objectCount++;
                    }
                }

                if (skipHistoricObject) {
                    continue;
                }

                try {
                    addObjectToBatch(object, initialLoad);
                } catch(Exception e) {
                    String message = "Exception writing object to database, IMGeo object: ";
                    if (batchSize > 1) {
                        message = "Exception adding parameters to database write batch, may be caused by previous batches. IMGeo object: ";
                    }
                    throw new Exception(message + object, e);
                }

                if (objectLimit != null && this.objectCount == objectLimit) {
                    break;
                }
            }

            Stream.concat(insertBatches.values().stream(), deleteBatches.values().stream()).forEach(batch -> {
                try {
                    batch.executeBatch();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            if (isCreateSchema() && initialLoad) {
                QueryRunner qr = new QueryRunner();
                updateProgress(Stage.CREATE_PRIMARY_KEY);
                for(String name: insertBatches.keySet()) {
                    for (String sql: getCreatePrimaryKeyStatements(BGTSchema.getObjectTypeByName(name), dialect, false).collect(Collectors.toList())) {
                        qr.update(connection, sql);
                    }
                }
                updateProgress(Stage.CREATE_GEOMETRY_INDEX);
                for(String name: insertBatches.keySet()) {
                    for(String sql: getCreateGeometryIndexStatements(BGTSchema.getObjectTypeByName(name), dialect, false).collect(Collectors.toList())) {
                        qr.update(connection, sql);
                    }
                }
            }
            connection.commit();
            updateProgress(Stage.FINISHED);
        } finally {
            Stream.concat(insertBatches.values().stream(), deleteBatches.values().stream()).forEach(batch -> {
                try {
                    batch.close();
                } catch (Exception ignored) {
                }
            });
            insertBatches = new HashMap<>();
            deleteBatches = new HashMap<>();
        }
    }

    private void updateProgress(Stage stage) {
        this.stage = stage;
        updateProgress();
    }

    private void updateProgress() {
        if (progressUpdater != null) {
            progressUpdater.run();
        }
    }

    private static void truncateTable(Connection c, BGTSchema.BGTObjectType objectType) throws SQLException {
        // TODO like jdbc-util, truncate may fail but 'delete from' may succeed
        new QueryRunner().execute(c, String.format("truncate table %s", getTableNameForObjectType(objectType)));
    }

    private static String buildInsertSql(BGTObject object) {
        StringBuilder sql = new StringBuilder("insert into ");
        String tableName = getTableNameForObjectType(object.getObjectType());
        sql.append(tableName).append("(");
        sql.append(buildColumnList(object));
        sql.append(") values (");
        String paramPlaceholders = object.getObjectType().getDirectAttributes()
                .map(c -> "?")
                .collect(Collectors.joining(", "));
        sql.append(paramPlaceholders).append(")");
        return sql.toString();
    }

    private String buildPgCopySql(BGTObject object, boolean initialLoad) {
        String copySql = "copy " + getTableNameForObjectType(object.getObjectType()) + "(" + buildColumnList(object) + ") from stdin";
        return copySql + (initialLoad ? " with freeze" : "");
    }

    private static String buildColumnList(BGTObject object) {
        return object.getObjectType().getDirectAttributes()
                .map(column -> getColumnNameForObjectType(object.getObjectType(), column.getName()))
                .collect(Collectors.joining(", "));
    }
}
