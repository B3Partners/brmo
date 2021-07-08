package nl.b3p.brmo.imgeo.cli;

import nl.b3p.brmo.bgt.download.model.DeltaCustomDownloadRequest;
import nl.b3p.brmo.imgeo.IMGeoObjectTableWriter;
import nl.b3p.brmo.imgeo.IMGeoSchemaMapper;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CountingInputStream;
import picocli.CommandLine;
import picocli.CommandLine.Command;
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

import static nl.b3p.brmo.imgeo.cli.Utils.formatTimeSince;

@Command(name = "bgt-loader", mixinStandardHelpOptions = true, version = "${ROOT-COMMAND-NAME} ${bundle:app.version}",
        resourceBundle = "BGTCityGMLLoader", subcommands = {DownloadCommand.class})
public class BGTLoaderMain {
    @Command(name = "schema")
    public int schema(
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp,
            @Option(names="--dialect", paramLabel="<dialect>", defaultValue = "postgis") IMGeoDb.SQLDialectEnum dialectEnum,
            @Mixin FeatureTypeSelectionOptions featureTypeSelectionOptions) throws SQLException {
        SQLDialect dialect = IMGeoDb.createDialect(dialectEnum);
        Set<String> tableNames = featureTypeSelectionOptions.getFeatureTypesList().stream()
                .map(DeltaCustomDownloadRequest.FeaturetypesEnum::getValue)
                .collect(Collectors.toSet());
        IMGeoSchemaMapper.printSchema(dialect, tableNames::contains);
        return 0;
    }

    @Command(name = "load")
    public int load(
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp,
            @Mixin DatabaseOptions dbOptions,
            @Mixin LoadOptions loadOptions,
            @Mixin FeatureTypeSelectionOptions featureTypeSelectionOptions,
            @Parameters(paramLabel = "<file>") File file) throws Exception {

        IMGeoDb db = new IMGeoDb(dbOptions);
        IMGeoObjectTableWriter writer = db.createObjectTableWriter(loadOptions);

        if (file.getName().endsWith(".zip")) {
            loadZip(file, writer, featureTypeSelectionOptions);
        } else if (file.getName().matches(".*\\.[xg]ml")) {
            loadXml(file, writer);
        } else {
            System.out.printf("Expected zip, gml or xml file: cannot load file \"%s\"", file.getName());
            return 1;
        }
        return 0;
    }

    private void loadZip(File file, IMGeoObjectTableWriter writer, FeatureTypeSelectionOptions featureTypeSelectionOptions) throws IOException {
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
                if(!featureTypes.contains(featureType)) {
                    System.out.printf("Skipping non-selected feature type: %s (%s)\n", tableName, FileUtils.byteCountToDisplaySize(entry.getSize()));
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
    }

    private void loadXml(File file, IMGeoObjectTableWriter writer) throws Exception {
        try(FileInputStream in = new FileInputStream(file)) {
            loadInputStream(file.getName(), in, file.length(), writer);
        }
    }

    private void loadInputStream(String name, InputStream input, long size, IMGeoObjectTableWriter writer) throws Exception {
        final String sizeString = FileUtils.byteCountToDisplaySize(size);
        final Instant start = Instant.now();
        final CountingInputStream countingInputStream = new CountingInputStream(input);

        writer.setProgressUpdater(() -> System.out.printf("\r%s (%s): %.1f%% - time %s, %,d objects",
                name,
                sizeString,
                100.0 / size * countingInputStream.getByteCount(),
                formatTimeSince(start),
                writer.getObjectCount()
        ));
        writer.write(countingInputStream);

        String endedObjects = writer.isCurrentObjectsOnly() ? String.format(", %,d ended objects skipped", writer.getEndedObjectsCount()) : "";
        double loadTimeSeconds = Duration.between(start, Instant.now()).toMillis() / 1000.0;
        System.out.printf("\r%s (%s): time %s, %,d objects%s, %,.0f objects/s%s\n",
                name,
                sizeString,
                formatTimeSince(start),
                writer.getObjectCount(),
                endedObjects,
                writer.getObjectCount() / loadTimeSeconds,
                " ".repeat(50)
        );
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BGTLoaderMain()).execute(args);
        System.exit(exitCode);
    }
}
