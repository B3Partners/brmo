package nl.b3p.brmo.bgt.loader.cli;

import nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadRequest;
import nl.b3p.brmo.bgt.loader.BGTObjectTableWriter;
import nl.b3p.brmo.bgt.loader.BGTSchemaMapper;
import nl.b3p.brmo.bgt.loader.Utils;
import nl.b3p.brmo.sql.PostGISCopyInsertBatch;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.log4j.PropertyConfigurator;
import org.geotools.util.logging.Logging;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import static nl.b3p.brmo.bgt.loader.BGTSchemaMapper.Metadata;
import static nl.b3p.brmo.bgt.loader.BGTSchemaMapper.getCreateMetadataTableStatements;
import static nl.b3p.brmo.bgt.loader.Utils.formatTimeSince;
import static nl.b3p.brmo.bgt.loader.Utils.getLoaderVersion;

@Command(name = "bgt-loader", mixinStandardHelpOptions = true, version = "${ROOT-COMMAND-NAME} ${bundle:app.version}",
        resourceBundle = Utils.BUNDLE_NAME, subcommands = {DownloadCommand.class})
public class BGTLoaderMain {
    static {
        PropertyConfigurator.configure(BGTLoaderMain.class.getResourceAsStream("/bgt-loader-cli-log4.properties"));
        try {
            Logging.ALL.setLoggerFactory("org.geotools.util.logging.Log4JLoggerFactory");
        } catch (ClassNotFoundException ignored) {
        }
    }

    @Command(name = "schema", sortOptions = false)
    public int schema(
            @Option(names="--dialect", paramLabel="<dialect>", defaultValue = "postgis") IMGeoDb.SQLDialectEnum dialectEnum,
            @Mixin FeatureTypeSelectionOptions featureTypeSelectionOptions,
            @Option(names="--table-prefix", defaultValue = "") String tablePrefix,
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp) throws SQLException {
        SQLDialect dialect = IMGeoDb.createDialect(dialectEnum);
        // For schema generation include plaatsbepalingspunt with 'all' and 'bgt'
        if (featureTypeSelectionOptions.featureTypes.contains("all") || featureTypeSelectionOptions.featureTypes.contains("bgt")) {
            featureTypeSelectionOptions.getFeatureTypes().add(DeltaCustomDownloadRequest.FeaturetypesEnum.PLAATSBEPALINGSPUNT.getValue());
        }
        Set<String> tableNames = featureTypeSelectionOptions.getFeatureTypesList().stream()
                .map(DeltaCustomDownloadRequest.FeaturetypesEnum::getValue)
                .collect(Collectors.toSet());
        BGTSchemaMapper.printSchema(dialect, tablePrefix, objectType ->
                tableNames.contains(BGTSchemaMapper.getTableNameForObjectType(objectType, ""))
        );
        return ExitCode.OK;
    }

    @Command(name = "load", sortOptions = false)
    public int load(
            @Mixin DatabaseOptions dbOptions,
            @Mixin LoadOptions loadOptions,
            @Mixin FeatureTypeSelectionOptions featureTypeSelectionOptions,
            @Parameters(paramLabel = "<file>") File file,
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp) throws Exception {

        IMGeoDb db = new IMGeoDb(dbOptions);
        BGTObjectTableWriter writer = db.createObjectTableWriter(loadOptions, dbOptions);

        if (loadOptions.createSchema) {
            System.out.println("Creating metadata table...");
            for(String sql: getCreateMetadataTableStatements(db.getDialect(), loadOptions.tablePrefix).collect(Collectors.toList())) {
                new QueryRunner().update(db.getConnection(), sql);
            }
        }

        if (file.getName().endsWith(".zip")) {
            loadZip(file, writer, featureTypeSelectionOptions);
        } else if (file.getName().matches(".*\\.[xg]ml")) {
            loadXml(file, writer);
        } else {
            System.err.printf("Expected zip, gml or xml file: cannot load file \"%s\"\n", file.getName());
            return ExitCode.USAGE;
        }

        db.setMetadataValue(Metadata.LOADER_VERSION, getLoaderVersion());
        // Set feature types list from options, not MutatieInhoud (if input has it)...
        // FIXME if downloaded initial extract has less object types, update will fail -- should set to only encountered
        // feature types
        db.setFeatureTypesEnumMetadata(featureTypeSelectionOptions.getFeatureTypesList());
        db.setMetadataValue(Metadata.INCLUDE_HISTORY, loadOptions.includeHistory + "");
        db.setMetadataValue(Metadata.LINEARIZE_CURVES, loadOptions.linearizeCurves + "");
        db.setMetadataValue(Metadata.TABLE_PREFIX, loadOptions.tablePrefix);
        BGTObjectTableWriter.Progress progress = writer.getProgress();
        if (progress.getMutatieInhoud() != null) {
            db.setMetadataForMutaties(progress.getMutatieInhoud());
            db.setMetadataValue(Metadata.GEOM_FILTER, progress.getMutatieInhoud().getGebied());

            System.out.printf("Mutatie type %s loaded with deltaId %s\n",
                    progress.getMutatieInhoud().getMutatieType(),
                    progress.getMutatieInhoud().getLeveringsId()
            );
        }
        db.getConnection().commit();

        return ExitCode.OK;
    }

    private void loadZip(File file, BGTObjectTableWriter writer, FeatureTypeSelectionOptions featureTypeSelectionOptions) throws IOException {
        Instant loadStart = Instant.now();
        ZipFile zipFile = new ZipFile(file);
        Pattern p = Pattern.compile("bgt_(.+).[xg]ml");
        Set<DeltaCustomDownloadRequest.FeaturetypesEnum> featureTypes = featureTypeSelectionOptions.getFeatureTypesList();
        zipFile.stream().filter(entry -> {
            Matcher m = p.matcher(entry.getName());
            if (!m.matches()) {
                System.out.println("Skipping zip entry: " + entry.getName());
                return false;
            }
            String tableName = m.group(1);
            try {
                DeltaCustomDownloadRequest.FeaturetypesEnum featureType = DeltaCustomDownloadRequest.FeaturetypesEnum.fromValue(tableName);
                if (!featureTypes.contains(featureType)) {
                    // System.out.printf("Skipping non-selected feature type: %s (%s)\n", tableName, FileUtils.byteCountToDisplaySize(entry.getSize()));
                    return false;
                } else {
                    return true;
                }
            } catch(IllegalArgumentException e) {
                System.out.printf("Skipping unknown feature type for zip entry \"%s\"", entry.getName());
                return false;
            }
        }).forEach(entry -> {
            try(InputStream in = zipFile.getInputStream(entry)) {
                // getSize() will not return -1 because ZipFile uses random access to read the ZIP central directory
                loadInputStream(entry.getName(), in, entry.getSize(), writer);
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.out.printf("Finished writing all tables in %s\n", formatTimeSince(loadStart));
        System.out.printf("Total copy duration: %s\n", PostGISCopyInsertBatch.copyDuration.toString());
    }

    private void loadXml(File file, BGTObjectTableWriter writer) throws Exception {
        try(FileInputStream in = new FileInputStream(file)) {
            loadInputStream(file.getName(), in, file.length(), writer);
        }
    }

    private void loadInputStream(String name, InputStream input, long size, BGTObjectTableWriter writer) throws Exception {
        final String sizeString = FileUtils.byteCountToDisplaySize(size);
        final Instant start = Instant.now();
        final CountingInputStream countingInputStream = new CountingInputStream(input);

        writer.setProgressUpdater((progress) -> {
            switch(progress.getStage()) {
                case LOAD_OBJECTS:
                    System.out.printf("\r%s (%s): %.1f%% - time %s, %,d objects",
                            name,
                            sizeString,
                            100.0 / size * countingInputStream.getByteCount(),
                            formatTimeSince(start),
                            progress.getObjectCount()
                    );
                    break;
                case CREATE_PRIMARY_KEY:
                    System.out.print("\r" + " ".repeat(50) + "\rCreating primary keys...");
                    break;
                case CREATE_GEOMETRY_INDEX:
                    System.out.print("\r" + " ".repeat(50) + "\rCreating geometry indexes...");
                    break;
            }
        });
        writer.write(countingInputStream);

        String historicObjects = writer.isCurrentObjectsOnly() ? String.format(", %,d historic objects skipped", writer.getProgress().getHistoricObjectsCount()) : "";
        double loadTimeSeconds = Duration.between(start, Instant.now()).toMillis() / 1000.0;
        System.out.printf("\r%s (%s): time %s, %,d objects%s, %,.0f objects/s%s\n",
                name,
                sizeString,
                formatTimeSince(start),
                writer.getProgress().getObjectCount(),
                historicObjects,
                writer.getProgress().getObjectCount() / loadTimeSeconds,
                " ".repeat(50)
        );
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new BGTLoaderMain())
                .setUsageHelpAutoWidth(true);
        System.exit(cmd.execute(args));
    }
}
