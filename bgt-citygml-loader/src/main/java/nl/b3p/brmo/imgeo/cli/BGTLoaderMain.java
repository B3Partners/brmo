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
        resourceBundle = "BGTCityGMLLoader")
public class BGTLoaderMain {

    enum SQLDialectEnum {
        postgis,
        oracle,
        mssql
    }

    enum IMGeoTableSet {
        bgt,
        plus,
        all
    }

    @Command(name = "schema")
    public int schema(
            @Option(names={"-h","--help"}, usageHelp = true) boolean showHelp,
            @Option(names="--dialect", paramLabel="<dialect>", defaultValue = "postgis") SQLDialectEnum dialectEnum,
            @Option(names="--tables", paramLabel="<tables>", defaultValue="all") IMGeoTableSet tables) throws SQLException {
        createDialect(dialectEnum);
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

        createConnectionFactory(dbOptions);

        IMGeoObjectTableWriter writer = new IMGeoObjectTableWriter(connectionFactory.get(), dialect);

        if (loadOptions == null) {
            loadOptions = new LoadOptions();
        }
        writer.setBatchSize(dialect.getDefaultOptimalBatchSize());
        writer.setObjectLimit(loadOptions.maxObjects);
        writer.setLinearizeCurves(loadOptions.linearizeCurves);
        writer.setCurrentObjectsOnly(!loadOptions.includeHistory);

        writer.processFile(file);

        return 1;
    }

    Supplier<Connection> connectionFactory = null;
    SQLDialect dialect;

    private void createDialect(SQLDialectEnum dialectEnum) throws SQLException {
        switch(dialectEnum) {
            case postgis: dialect = new PostGISDialect(); return;
            case oracle: dialect = new OracleDialect(); return;
            case mssql: dialect = new MSSQLDialect(); return;
        }
        throw new IllegalArgumentException(String.format("Invalid dialect: \"%s\"", dialectEnum));
    }

    private void createConnectionFactory(DatabaseOptions dbOptions) throws SQLException, ClassNotFoundException {
        SQLDialectEnum sqlDialectEnum;
        if (dbOptions.connectionString.startsWith("jdbc:postgresql:")) {
            sqlDialectEnum = SQLDialectEnum.postgis;
        } else if (dbOptions.connectionString.startsWith("jdbc:oracle:thin:")) {
            sqlDialectEnum = SQLDialectEnum.oracle;
        } else if (dbOptions.connectionString.startsWith("jdbc:sqlserver:")) {
            sqlDialectEnum = SQLDialectEnum.mssql;
        } else {
            throw new IllegalArgumentException(String.format("Can't determine database dialect from connection string \"%s\"", dbOptions.connectionString));
        }

        createDialect(sqlDialectEnum);

        dialect.loadDriver();

        connectionFactory = () -> {
            try {
                return DriverManager.getConnection(dbOptions.connectionString, dbOptions.user, dbOptions.password);
            } catch (SQLException e) {
                throw new RuntimeException(String.format("Error connecting to the database with connection string \"%s\"", dbOptions.connectionString), e);
            }
        };
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BGTLoaderMain()).execute(args);
        System.exit(exitCode);
    }
}
