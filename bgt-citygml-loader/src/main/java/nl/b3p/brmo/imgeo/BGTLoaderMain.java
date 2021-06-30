package nl.b3p.brmo.imgeo;

import nl.b3p.brmo.sql.dialect.MSSQLDialect;
import nl.b3p.brmo.sql.dialect.OracleDialect;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import nl.b3p.jdbc.util.converter.OracleConnectionUnwrapper;
import org.apache.commons.lang3.builder.ToStringBuilder;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Help;
import picocli.CommandLine.Option;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "bgt-citygml-loader", mixinStandardHelpOptions = true, version = "${ROOT-COMMAND-NAME} ${bundle:app.version}",
         resourceBundle = "BGTCityGMLLoader")
public class BGTLoaderMain implements Callable<Integer> {

    @ArgGroup(multiplicity = "1")
    Mode mode;

    @Option(names="--dialect", defaultValue="postgis", paramLabel = "<dialect>", showDefaultValue = Help.Visibility.ALWAYS)
    SQLDialectEnum dialectEnum;

    @Option(names={"-f","--file"})
    File file;

    enum SQLDialectEnum {
        postgis,
        oracle,
        mssql
    }

    static class Mode {
        @Option(names = "--print-schema") boolean printSchema;
        @Option(names = "--load-citygml") boolean loadCityGml;
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
    }

    Connection connection = null;

    private SQLDialect createDialect() throws SQLException {
        switch(dialectEnum) {
            case postgis: return new PostGISDialect();
            case oracle: return new OracleDialect(connection == null ? null : OracleConnectionUnwrapper.unwrap(connection));
            case mssql: return new MSSQLDialect();
        }
        throw new IllegalArgumentException("Invalid dialect: " + dialectEnum);
    }

    @Override
    public Integer call() throws Exception {

        if (mode.printSchema) {
            SQLDialect dialect = createDialect();
            IMGeoSchemaMapper.printSchema(dialect, true, true);
        } else if(mode.loadCityGml) {
            System.err.println("Not implemented");
            return -1;
        }
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BGTLoaderMain()).execute(args);
        System.exit(exitCode);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
