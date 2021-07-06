package nl.b3p.brmo.imgeo.cli;

import nl.b3p.brmo.imgeo.IMGeoObjectTableWriter;
import nl.b3p.brmo.imgeo.IMGeoSchemaMapper;
import nl.b3p.brmo.sql.dialect.MSSQLDialect;
import nl.b3p.brmo.sql.dialect.OracleDialect;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Supplier;

@Command(name = "bgt-citygml-loader", mixinStandardHelpOptions = true, version = "${ROOT-COMMAND-NAME} ${bundle:app.version}",
        resourceBundle = "BGTCityGMLLoader", subcommands = {DownloadCommand.class})
public class BGTLoaderMain {

    enum IMGeoTableSet {
        bgt,
        plus,
        all
    }

    @Command(name = "schema")
    public int schema(
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp,
            @Option(names="--dialect", paramLabel="<dialect>", defaultValue = "postgis") IMGeoDb.SQLDialectEnum dialectEnum,
            @Option(names="--tables", paramLabel="<tables>", defaultValue="all") IMGeoTableSet tables) throws SQLException {
        SQLDialect dialect = IMGeoDb.createDialect(dialectEnum);
        IMGeoSchemaMapper.printSchema(dialect,
                tables == IMGeoTableSet.all || tables == IMGeoTableSet.bgt,
                tables == IMGeoTableSet.all || tables == IMGeoTableSet.plus
        );
        return 0;
    }

    @Command(name = "load")
    public int load(
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp,
            @Mixin DatabaseOptions dbOptions,
            @Mixin LoadOptions loadOptions,
            @Parameters(paramLabel = "<file>") File file) throws Exception {

        IMGeoDb db = new IMGeoDb(dbOptions);
        IMGeoObjectTableWriter writer = db.createObjectTableWriter(loadOptions);

        writer.processFile(file);

        return 1;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BGTLoaderMain()).execute(args);
        System.exit(exitCode);
    }
}
