package nl.b3p.brmo.imgeo;

import nl.b3p.brmo.sql.AttributeColumnMapping;
import nl.b3p.brmo.sql.GeometryAttributeColumnMapping;
import nl.b3p.brmo.sql.PreparedStatementQueryBatch;
import nl.b3p.brmo.sql.OneToManyColumnMapping;
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

import static nl.b3p.brmo.imgeo.IMGeoObject.MutatieStatus.WAS_WORDT;
import static nl.b3p.brmo.imgeo.IMGeoObject.MutatieStatus.WORDT;
import static nl.b3p.brmo.imgeo.IMGeoSchema.EIND_REGISTRATIE;
import static nl.b3p.brmo.imgeo.IMGeoSchemaMapper.getColumnNameForObjectType;
import static nl.b3p.brmo.imgeo.IMGeoSchemaMapper.getTableNameForObjectType;

public class IMGeoObjectTableWriter {

    private final Connection c;
    private final SQLDialect dialect;

    private int batchSize = 100;
    private Integer objectLimit = null;
    private boolean linearizeCurves = false;
    private boolean currentObjectsOnly = true;

    private CountingInputStream counter;
    private final Map<String, QueryBatch> insertBatches = new HashMap<>();
    private final Map<String, QueryBatch> deleteBatches = new HashMap<>();
    private long objectCount = 0;
    private long objectUpdatedCount = 0;
    private long objectRemovedCount = 0;
    private long historicObjectsCount = 0;
    private IMGeoObjectStreamer.MutatieInhoud mutatieInhoud;

    private Runnable progressUpdater;

    public IMGeoObjectTableWriter(Connection c, SQLDialect dialect) {
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

    public IMGeoObjectStreamer.MutatieInhoud getMutatieInhoud() {
        return mutatieInhoud;
    }

    public Runnable getProgressUpdater() {
        return progressUpdater;
    }

    public void setProgressUpdater(Runnable progressUpdater) {
        this.progressUpdater = progressUpdater;
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

    private void addObjectBatch(IMGeoObject object, boolean initialLoad) throws Exception {
        if(!insertBatches.containsKey(object.getName())) {
            if (initialLoad) {
                truncateTable(c, object);
            }

            String sql = buildInsertSql(object);
            List<AttributeColumnMapping> attributeColumnMappings = IMGeoSchema.objectTypeAttributes.get(object.getName());
            List<Boolean> geometryParameterIndexes = new ArrayList<>();
            for(AttributeColumnMapping mapping: attributeColumnMappings) {
                if (!(mapping instanceof OneToManyColumnMapping)) {
                    geometryParameterIndexes.add(mapping instanceof GeometryAttributeColumnMapping);
                }
            }
            insertBatches.put(object.getName(), new PreparedStatementQueryBatch(c, sql, dialect, batchSize, geometryParameterIndexes.toArray(new Boolean[0]), linearizeCurves));
        }
        QueryBatch batch = insertBatches.get(object.getName());

        List<AttributeColumnMapping> columns = IMGeoSchema.objectTypeAttributes.get(object.getName());
        Map<String, Object> attributes = object.getAttributes();
        Object[] params = new Object[(int) columns.stream().filter(c -> !(c instanceof OneToManyColumnMapping)).count()];
        int columnIndex = 0;
        for (AttributeColumnMapping column: columns) {
            if (column instanceof OneToManyColumnMapping) {
                List<IMGeoObject> objects = (List<IMGeoObject>) attributes.get(column.getName());
                if (objects != null && !objects.isEmpty()) {
                    for(int j = 0; j < objects.size(); j++) {
                        IMGeoObject oneToMany = objects.get(j);

                        // Add FK and index
                        String tableName = getTableNameForObjectType(object.getName());
                        String idColumnName = columns.stream().filter(AttributeColumnMapping::isPrimaryKey).findFirst().get().getName();
                        oneToMany.getAttributes().put(getColumnNameForObjectType(oneToMany.getName(),tableName + idColumnName), object.getAttributes().get(idColumnName));
                        oneToMany.getAttributes().put(IMGeoSchema.INDEX, j);
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

    private void deletePreviousVersion(IMGeoObject object) throws Exception {
        List<AttributeColumnMapping> columns = IMGeoSchema.objectTypeAttributes.get(object.getName());
        String idColumnName = columns.stream().filter(AttributeColumnMapping::isPrimaryKey).findFirst().get().getName();
        String tableName = getTableNameForObjectType(object.getName());
        if(!deleteBatches.containsKey(object.getName())) {
            String sql = "delete from " + tableName + " where " + getColumnNameForObjectType(object.getName(), idColumnName) + " = ?";
            deleteBatches.put(object.getName(), new PreparedStatementQueryBatch(c, sql, dialect, batchSize, new Boolean[] {false}, false));
        }
        QueryBatch batch = deleteBatches.get(object.getName());

        boolean executed = batch.addBatch(new Object[] {object.getMutatiePreviousVersionGmlId()});

        for (AttributeColumnMapping column: columns) {
            if (column instanceof OneToManyColumnMapping) {
                OneToManyColumnMapping oneToManyColumnMapping = (OneToManyColumnMapping) column;

                if(!deleteBatches.containsKey(oneToManyColumnMapping.getName())) {
                    String sql = "delete from " + getTableNameForObjectType(oneToManyColumnMapping.getName()) + " where "
                            + getColumnNameForObjectType(oneToManyColumnMapping.getName(), tableName + idColumnName) + " = ?";
                    deleteBatches.put(oneToManyColumnMapping.getName(), new PreparedStatementQueryBatch(c, sql, dialect, batchSize, new Boolean[]{false}, false));
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

        try(CountingInputStream counter = new CountingInputStream(bgtXml)) {
            this.counter = counter;

            IMGeoObjectStreamer streamer = new IMGeoObjectStreamer(counter);
            this.mutatieInhoud = streamer.getMutatieInhoud();
            if (this.progressUpdater != null) {
                this.progressUpdater.run();
            }
            boolean initialLoad = this.mutatieInhoud == null || "initial".equals(this.getMutatieInhoud().getMutatieType());
            for (IMGeoObject object: streamer) {
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

            for(QueryBatch batch: insertBatches.values()) {
                batch.executeBatch();
            }
            for(QueryBatch batch: deleteBatches.values()) {
                batch.executeBatch();
            }
        } finally {
            for(QueryBatch batch: insertBatches.values()) {
                try {
                    batch.close();
                } catch (Exception ignored) {
                }
            }
            for(QueryBatch batch: deleteBatches.values()) {
                try {
                    batch.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static void truncateTable(Connection c, IMGeoObject object) throws SQLException {
        // TODO like jdbc-util, truncate may fail but 'delete from' may succeed
        new QueryRunner().execute(c, String.format("truncate table %s", getTableNameForObjectType(object.getName())));
    }

    private static String buildInsertSql(IMGeoObject object) {
        StringBuilder sql = new StringBuilder("insert into ");
        String tableName = getTableNameForObjectType(object.getName());
        sql.append(tableName);
        sql.append(" (");
        AtomicBoolean first = new AtomicBoolean(true);
        List<AttributeColumnMapping> columns = IMGeoSchema.objectTypeAttributes.get(object.getName());
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
