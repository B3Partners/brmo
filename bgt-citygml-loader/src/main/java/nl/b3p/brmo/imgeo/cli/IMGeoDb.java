package nl.b3p.brmo.imgeo.cli;

import nl.b3p.brmo.imgeo.IMGeoObjectTableWriter;
import nl.b3p.brmo.sql.dialect.MSSQLDialect;
import nl.b3p.brmo.sql.dialect.OracleDialect;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.brmo.sql.dialect.SQLDialect;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Supplier;

public class IMGeoDb {

    public enum SQLDialectEnum {
        postgis,
        oracle,
        mssql
    }

    private SQLDialect dialect;
    private DatabaseOptions dbOptions;

    public IMGeoDb(DatabaseOptions dbOptions) throws SQLException, ClassNotFoundException {
        this.dbOptions = dbOptions;
        String connectionString = dbOptions.connectionString;
        SQLDialectEnum sqlDialectEnum;
        if (connectionString.startsWith("jdbc:postgresql:")) {
            sqlDialectEnum = SQLDialectEnum.postgis;
        } else if (connectionString.startsWith("jdbc:oracle:thin:")) {
            sqlDialectEnum = SQLDialectEnum.oracle;
        } else if (connectionString.startsWith("jdbc:sqlserver:")) {
            sqlDialectEnum = SQLDialectEnum.mssql;
        } else {
            throw new IllegalArgumentException(String.format("Can't determine database dialect from connection string \"%s\"", connectionString));
        }

        dialect = createDialect(sqlDialectEnum);
        dialect.loadDriver();
    }

    public SQLDialect getDialect() {
        return dialect;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(dbOptions.connectionString, dbOptions.user, dbOptions.password);
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Error connecting to the database with connection string \"%s\"", dbOptions.connectionString), e);
        }
    }

    public IMGeoObjectTableWriter createObjectTableWriter(LoadOptions loadOptions) throws SQLException, ClassNotFoundException {
        IMGeoObjectTableWriter writer = new IMGeoObjectTableWriter(this.getConnection(), this.getDialect());

        if (loadOptions == null) {
            loadOptions = new LoadOptions();
        }
        writer.setBatchSize(this.getDialect().getDefaultOptimalBatchSize());
        writer.setObjectLimit(loadOptions.maxObjects);
        writer.setLinearizeCurves(loadOptions.linearizeCurves);
        writer.setCurrentObjectsOnly(!loadOptions.includeHistory);
        return writer;
    }

    public static SQLDialect createDialect(SQLDialectEnum dialectEnum) throws SQLException {
        switch(dialectEnum) {
            case postgis: return new PostGISDialect();
            case oracle: return new OracleDialect();
            case mssql: return new MSSQLDialect();
        }
        throw new IllegalArgumentException(String.format("Invalid dialect: \"%s\"", dialectEnum));
    }
}
