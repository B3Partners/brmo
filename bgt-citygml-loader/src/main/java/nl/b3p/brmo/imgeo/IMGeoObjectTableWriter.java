package nl.b3p.brmo.imgeo;

import nl.b3p.brmo.PGGeometryString;
import nl.b3p.brmo.sql.AttributeColumnMapping;
import nl.b3p.brmo.sql.OneToManyColumnMapping;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.io.input.CountingInputStream;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipFile;

import static nl.b3p.brmo.imgeo.IMGeoSchemaMapper.getColumnNameForObjectType;
import static nl.b3p.brmo.imgeo.IMGeoSchemaMapper.getTableNameForObjectType;

public class IMGeoObjectTableWriter {

    private static final int batchSize = 2500;

    private final Connection c;

    private CountingInputStream counter;
    private long size;

    private Map<String,InsertBatch> batches = new HashMap<>();

    private long objects = 0;
    private long intermediateStartTime = -1;
    private long intermediateCount = 0;
    private long intermediateObjects = 0;

    private Set<String> allUnusedAttributes = new HashSet<>();
    private Map<String,Integer> columnErrors = new HashMap<>();

    public IMGeoObjectTableWriter(Connection c) {
        this.c = c;
    }

    private static class InsertBatch {
        private final Connection c;
        private final QueryRunner db = new QueryRunner();
        private final String insertSql;
        private final Object[][] batch = new Object[batchSize][];
        private int index = 0;

        public InsertBatch(Connection c, String insertSql) {
            this.c = c;
            this.insertSql = insertSql;
        }

        public boolean addBatch(Object[] params) throws SQLException {
            this.batch[this.index++] = params;

            if (index == batch.length) {
                this.executeBatch();
                return true;
            } else {
                return false;
            }
        }

        public void executeBatch() throws SQLException {
            if (index > 0) {
                Object[][] thisBatch = batch;
                if (index < batch.length) {
                    thisBatch = Arrays.copyOfRange(batch, 0, index);
                }

                db.batch(c, insertSql, thisBatch);
                this.index = 0;
            }
        }
    }

    private void updateProgress() {
        long interval = System.currentTimeMillis() - intermediateStartTime;
        if (interval > 1000) {
            double seconds = interval / 1000.0;
            long byteCount = counter.getByteCount();
            double percent = (100.0 / size) * byteCount;
            System.out.printf("\r%3.1f%%, read %.0f MiB, %d objects, %.1f MiB/s, %.0f objects/s         ",
                    percent,
                    byteCount / 1024.0 / 1024,
                    objects,
                    (byteCount - intermediateCount) / 1024.0 / 1024 / seconds,
                    (objects - intermediateObjects) / seconds);
            intermediateObjects = objects;
            intermediateCount = counter.getByteCount();
            intermediateStartTime = System.currentTimeMillis();
        }

    }

    private void addObjectBatch(IMGeoObject object) throws SQLException {
        if(!batches.containsKey(object.getName())) {
            if (!tableExists(c, object)) {
                System.out.printf("Table for object %s does not exist, skipping\n", object.getName());
                batches.put(object.getName(), null);
                return;
            }

            truncateTable(c, object);

            String sql = buildInsertSql(object);
            batches.put(object.getName(), new InsertBatch(c, sql));
        }
        InsertBatch batch = batches.get(object.getName());
        if(batch == null) {
            // Table did not exist
            return;
        }

        // TODO: get table metadata, only insert existing columns

        // TODO: log if attribute has no column

        // TODO openbareRuimteNaam is list, multiply other attributes
        // of al IMGeoObject multiplyen?

        List<AttributeColumnMapping> columns = IMGeoSchema.objectTypeAttributes.get(object.getName());
        Map<String, Object> attributes = object.getAttributes();
        Object[] params = new Object[(int) columns.stream().filter(c -> !(c instanceof OneToManyColumnMapping)).count()];
        Set<String> usedAttributes = new HashSet<>();
        int columnIndex = 0;
        for (AttributeColumnMapping column: columns) {
            usedAttributes.add(column.getName());

            if (column instanceof OneToManyColumnMapping) {
                List<IMGeoObject> objects = (List<IMGeoObject>) attributes.get(column.getName());
                if (objects != null && !objects.isEmpty()) {
                    for(int j = 0; j < objects.size(); j++) {
                        IMGeoObject oneToMany = objects.get(j);

                        // Add FK and index
                        String tableName = getTableNameForObjectType(object.getName());
                        String idColumnName = columns.stream().filter(AttributeColumnMapping::isPrimaryKey).findFirst().get().getName();
                        oneToMany.getAttributes().put(getColumnNameForObjectType(oneToMany.getName(),tableName + idColumnName), object.getAttributes().get(idColumnName));
                        oneToMany.getAttributes().put("index", j);
                        Object eindRegistratie = object.getAttributes().get("eindRegistratie");
                        oneToMany.getAttributes().put(getColumnNameForObjectType(oneToMany.getName(),tableName + "eindRegistratie"), eindRegistratie != null);
                        addObjectBatch(oneToMany);
                    }
                }
            } else {
                try {
                    Object attribute = attributes.get(column.getName());
                    params[columnIndex] = column.toQueryParameter(attribute);
                } catch (Exception e) {
                    Integer errors = columnErrors.get(column.getName());
                    if (errors == null) {
                        errors = 0;
                    }
                    columnErrors.put(column.getName(), errors + 1);
                    params[columnIndex] = null;
                }
                columnIndex++;
            }
        }

        Set<String> unusedAttributes = object.getAttributes().keySet();
        unusedAttributes.removeAll(usedAttributes);
        allUnusedAttributes.addAll(unusedAttributes);

        boolean executed = batch.addBatch(params);

        if (executed) {
            updateProgress();
        }
    }

    private void write(InputStream bgtXml, long size) throws Exception {
        this.size = size;
        try(CountingInputStream counter = new CountingInputStream(bgtXml)) {
            this.counter = counter;

            IMGeoObjectStreamer streamer = new IMGeoObjectStreamer(counter);

            objects = 0;
            boolean aborted = false;
            boolean log = false;
            long startTime = System.currentTimeMillis();
            intermediateStartTime = startTime;
            intermediateCount = 0;
            intermediateObjects = 0;

            for (IMGeoObject object: streamer) {
                objects++;

                if (log) System.out.printf("cityObjectMember #%d: %s\n",
                        objects,
                        object);

                addObjectBatch(object);

//                if (objects == 1000000) {
//                    break;
//                }
            }

            for(InsertBatch batch: batches.values()) {
                batch.executeBatch();
            }

            if (!aborted) {
                System.out.print("\r                                                                                  \r");
                if (!columnErrors.isEmpty()) {
                    System.out.println("  Column errors: " + columnErrors);
                }
                if (!allUnusedAttributes.isEmpty()) {
                    System.out.println("  Unused attributes: " + allUnusedAttributes);
                }

                double time = (System.currentTimeMillis() - startTime) / 1000.0;
                System.out.printf("Finished writing: %d objects, %.1f s, %.0f objects/s, %.1f MiB/s\n", objects, time, objects / time, counter.getByteCount() / 1024.0 / 1024 / time);
            }
        }
    }

    private static boolean tableExists(Connection c, IMGeoObject object) {
        String sql = String.format("select 1 from \"%s\"", getTableNameForObjectType(object.getName()));
        try {
            new QueryRunner().query(c, sql, new ScalarHandler<>());
            return true;
        } catch(Exception e) {
            // Could also be another error than the table not existing, anyway the table is not reachable
            return false;
        }
    }

    private static void truncateTable(Connection c, IMGeoObject object) throws SQLException {
        new QueryRunner().execute(c, String.format("truncate table \"%s\"", getTableNameForObjectType(object.getName())));
    }

    private static String buildInsertSql(IMGeoObject object) {
        StringBuilder sql = new StringBuilder("insert into \"");
        String tableName = getTableNameForObjectType(object.getName());
        sql.append(tableName);
        sql.append("\" (");
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
                sql.append('"');
                sql.append(getColumnNameForObjectType(object.getName(), column.getName()));
                sql.append('"');
            }
        });
        sql.append(") values (");
        sql.append(params);
        sql.append(")");
        return sql.toString();
    }

    public static void main(String[] args) throws Exception {

        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql:bgt";
        final String user = "bgt";
        final String password = "bgt";
        Connection c = DriverManager.getConnection(url, user, password);
//        PGGeometryString.register(c);

        String version = new QueryRunner().query(c,"select postgis_version()", new ScalarHandler<>());
        System.out.println("PostGIS version: " + version);

        long startTime = System.currentTimeMillis();

        ZipFile file = new ZipFile(new File("/media/ssd/files/bgt/2021/bgt-citygml-nl-nopbp.zip"));
        IMGeoObjectTableWriter writer = new IMGeoObjectTableWriter(c);
        file.stream().filter(entry -> "bgt_stadsdeel.gml".equals(entry.getName())).forEach(entry -> {
            try {
                System.out.printf("Processing ZIP entry: %s, size %.0f MiB, compressed size %.0f MiB\n", entry.getName(), entry.getSize() / 1024.0 / 1024, entry.getCompressedSize() / 1024.0 / 1024);
                writer.write(file.getInputStream(entry), entry.getSize());
            } catch (SQLException e) {
                System.out.println("");
                String message = e.getMessage();
                int index = message.indexOf("Parameters: ");
                if (index != -1) {
                    message = message.substring(0, index) + ", parameters omitted...";
                }
                System.err.println("SQLException: " + message);
            } catch(Exception e) {
                e.printStackTrace();
            }
        });

        double time = (System.currentTimeMillis() - startTime) / 1000.0 / 60;
        System.out.printf("===\nFinished writing all tables: %.1f minutes\n", time);
    }
}
