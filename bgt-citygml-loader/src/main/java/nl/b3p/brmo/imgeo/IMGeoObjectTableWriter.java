package nl.b3p.brmo.imgeo;

import nl.b3p.brmo.sql.AttributeColumnMapping;
import nl.b3p.brmo.sql.GeometryAttributeColumnMapping;
import nl.b3p.brmo.sql.InsertBatch;
import nl.b3p.brmo.sql.OneToManyColumnMapping;
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

    private final Map<String,InsertBatch> batches = new HashMap<>();

    private long objectCount = 0;
    private long endedObjectsCount = 0;

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

    public Runnable getProgressUpdater() {
        return progressUpdater;
    }

    public void setProgressUpdater(Runnable progressUpdater) {
        this.progressUpdater = progressUpdater;
    }

    public long getObjectCount() {
        return objectCount;
    }

    public long getEndedObjectsCount() {
        return endedObjectsCount;
    }

    public long getBytesRead() {
        return counter.getByteCount();
    }

    private void addObjectBatch(IMGeoObject object) throws Exception {
        if(!batches.containsKey(object.getName())) {
            truncateTable(c, object);

            String sql = buildInsertSql(object);
            List<AttributeColumnMapping> attributeColumnMappings = IMGeoSchema.objectTypeAttributes.get(object.getName());
            List<Boolean> geometryParameterIndexes = new ArrayList<>();
            for(AttributeColumnMapping mapping: attributeColumnMappings) {
                if (!(mapping instanceof OneToManyColumnMapping)) {
                    geometryParameterIndexes.add(mapping instanceof GeometryAttributeColumnMapping);
                }
            }
            batches.put(object.getName(), new InsertBatch(c, sql, dialect, batchSize, geometryParameterIndexes.toArray(new Boolean[0]), linearizeCurves));
        }
        InsertBatch batch = batches.get(object.getName());

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
                        addObjectBatch(oneToMany);
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

    public void write(InputStream bgtXml) throws Exception {
        this.objectCount = 0;
        this.endedObjectsCount = 0;
        try(CountingInputStream counter = new CountingInputStream(bgtXml)) {
            this.counter = counter;

            IMGeoObjectStreamer streamer = new IMGeoObjectStreamer(counter);
            for (IMGeoObject object: streamer) {

                if (object.getAttributes().get(EIND_REGISTRATIE) != null && currentObjectsOnly) {
                    this.endedObjectsCount++;
                    continue;
                }

                this.objectCount++;

                try {
                    addObjectBatch(object);
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

            for(InsertBatch batch: batches.values()) {
                batch.executeBatch();
            }
        } finally {
            for(InsertBatch batch: batches.values()) {
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
