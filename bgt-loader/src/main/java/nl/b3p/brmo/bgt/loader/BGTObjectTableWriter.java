package nl.b3p.brmo.bgt.loader;

import nl.b3p.brmo.sql.GeometryHandlingPreparedStatementBatch;
import nl.b3p.brmo.sql.mapping.AttributeColumnMapping;
import nl.b3p.brmo.sql.mapping.GeometryAttributeColumnMapping;
import nl.b3p.brmo.sql.PreparedStatementQueryBatch;
import nl.b3p.brmo.sql.mapping.OneToManyColumnMapping;
import nl.b3p.brmo.sql.QueryBatch;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.io.input.CountingInputStream;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
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

    private final Connection c;
    private final SQLDialect dialect;

    private int batchSize = 100;
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
    private BGTObjectStreamer.MutatieInhoud mutatieInhoud;

    private Runnable progressUpdater;

    public BGTObjectTableWriter(Connection c, SQLDialect dialect) {
        this.c = c;
        this.dialect = dialect;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
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

    private void addObjectBatch(BGTObject object, boolean initialLoad) throws Exception {
        if(!insertBatches.containsKey(object.getName())) {
            if (initialLoad) {
                if (isCreateSchema()) {
                    QueryRunner qr = new QueryRunner();
                    for(String sql: Stream.concat(
                            getCreateTableStatements(object.getName(), dialect),
                            getCreateGeometryMetadataStatements(object.getName(), dialect)).collect(Collectors.toList())) {
                        qr.update(c, sql);
                    }
                } else {
                    truncateTable(c, object);
                }
            }

            String sql = buildInsertSql(object);
            List<AttributeColumnMapping> attributeColumnMappings = BGTSchema.objectTypeAttributes.get(object.getName());
            List<Boolean> geometryParameterIndexes = new ArrayList<>();
            for(AttributeColumnMapping mapping: attributeColumnMappings) {
                if (!(mapping instanceof OneToManyColumnMapping)) {
                    geometryParameterIndexes.add(mapping instanceof GeometryAttributeColumnMapping);
                }
            }
            insertBatches.put(object.getName(), new GeometryHandlingPreparedStatementBatch(c, sql, batchSize, dialect, geometryParameterIndexes.toArray(new Boolean[0]), linearizeCurves));
        }
        QueryBatch batch = insertBatches.get(object.getName());

        List<AttributeColumnMapping> columns = BGTSchema.objectTypeAttributes.get(object.getName());
        Map<String, Object> attributes = object.getAttributes();
        Object[] params = new Object[(int) columns.stream().filter(c -> !(c instanceof OneToManyColumnMapping)).count()];
        int columnIndex = 0;
        for (AttributeColumnMapping column: columns) {
            if (column instanceof OneToManyColumnMapping) {
                List<BGTObject> objects = (List<BGTObject>) attributes.get(column.getName());
                if (objects != null && !objects.isEmpty()) {
                    for(int j = 0; j < objects.size(); j++) {
                        BGTObject oneToMany = objects.get(j);

                        // Add FK and index
                        String tableName = getTableNameForObjectType(object.getName());
                        String idColumnName = columns.stream().filter(AttributeColumnMapping::isPrimaryKey).findFirst().get().getName();
                        oneToMany.getAttributes().put(getColumnNameForObjectType(oneToMany.getName(),tableName + idColumnName), object.getAttributes().get(idColumnName));
                        oneToMany.getAttributes().put(BGTSchema.INDEX, j);
                        Object eindRegistratie = object.getAttributes().get("eindRegistratie");
                        oneToMany.getAttributes().put(getColumnNameForObjectType(oneToMany.getName(),tableName + "eindRegistratie"), eindRegistratie != null);
                        addObjectBatch(oneToMany, initialLoad);
                    }
                }
            } else {
                Object attribute = attributes.get(column.getName());
                params[columnIndex] = column.toQueryParameter(attribute);
                columnIndex++;
            }
        }

        boolean executed = batch.addBatch(params);

        if (executed && progressUpdater != null) {
            progressUpdater.run();
        }
    }

    private void deletePreviousVersion(BGTObject object) throws Exception {
        List<AttributeColumnMapping> columns = BGTSchema.objectTypeAttributes.get(object.getName());
        String idColumnName = columns.stream().filter(AttributeColumnMapping::isPrimaryKey).findFirst().get().getName();
        String tableName = getTableNameForObjectType(object.getName());
        if(!deleteBatches.containsKey(object.getName())) {
            String sql = "delete from " + tableName + " where " + getColumnNameForObjectType(object.getName(), idColumnName) + " = ?";
            deleteBatches.put(object.getName(), new PreparedStatementQueryBatch(c, sql, batchSize));
        }
        QueryBatch batch = deleteBatches.get(object.getName());

        boolean executed = batch.addBatch(new Object[] {object.getMutatiePreviousVersionGmlId()});

        for (AttributeColumnMapping column: columns) {
            if (column instanceof OneToManyColumnMapping) {
                OneToManyColumnMapping oneToManyColumnMapping = (OneToManyColumnMapping) column;

                if(!deleteBatches.containsKey(oneToManyColumnMapping.getName())) {
                    String sql = "delete from " + getTableNameForObjectType(oneToManyColumnMapping.getName()) + " where "
                            + getColumnNameForObjectType(oneToManyColumnMapping.getName(), tableName + idColumnName) + " = ?";
                    deleteBatches.put(oneToManyColumnMapping.getName(), new PreparedStatementQueryBatch(c, sql, batchSize));
                }
                executed = executed | deleteBatches.get(object.getName()).addBatch(new Object[] {object.getMutatiePreviousVersionGmlId()});
            }
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
        this.mutatieInhoud = null;
        updateProgress(Stage.PARSE_INHOUD);

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
                    addObjectBatch(object, initialLoad);
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
                    for (String sql: getCreatePrimaryKeyStatements(name, dialect, false).collect(Collectors.toList())) {
                        qr.update(c, sql);
                    }
                }
                updateProgress(Stage.CREATE_GEOMETRY_INDEX);
                for(String name: insertBatches.keySet()) {
                    for(String sql: getCreateGeometryIndexStatements(name, dialect, false).collect(Collectors.toList())) {
                        qr.update(c, sql);
                    }
                }
            }
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

    private static void truncateTable(Connection c, BGTObject object) throws SQLException {
        // TODO like jdbc-util, truncate may fail but 'delete from' may succeed
        new QueryRunner().execute(c, String.format("truncate table %s", getTableNameForObjectType(object.getName())));
    }

    private static String buildInsertSql(BGTObject object) {
        StringBuilder sql = new StringBuilder("insert into ");
        String tableName = getTableNameForObjectType(object.getName());
        sql.append(tableName);
        sql.append(" (");
        AtomicBoolean first = new AtomicBoolean(true);
        List<AttributeColumnMapping> columns = BGTSchema.objectTypeAttributes.get(object.getName());
        StringBuilder params = new StringBuilder();
        columns.forEach(column -> {
            if (!(column instanceof OneToManyColumnMapping)) {
                if (first.get()) {
                    first.set(false);
                } else {
                    sql.append(", ");
                    params.append(", ");
                }
                params.append("?");
                sql.append(getColumnNameForObjectType(object.getName(), column.getName()));
            }
        });
        sql.append(") values (");
        sql.append(params);
        sql.append(")");
        return sql.toString();
    }
}
