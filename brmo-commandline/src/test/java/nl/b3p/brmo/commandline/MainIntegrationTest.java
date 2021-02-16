/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.commandline;

import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * run met:
 * {@code mvn -Dit.test=MainIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql -pl brmo-commandline > target/postgresql.log}
 * NB. de zipfile wordt uitgepakt door Maven.
 *
 * @author mprins
 */
public class MainIntegrationTest {
    private static final Log LOG = LogFactory.getLog(MainIntegrationTest.class);
    private static final String BASE_COMMAND = "java -Dlog4j.configuration=file:./conf/test-log4j.xml -jar ./bin/brmo-commandline.jar --dbprops ./conf/test.properties ";
    private static File WORKDIR;

    @BeforeAll
    public static void getWorkDir() {
        WORKDIR = new File(MainIntegrationTest.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "../itest/");
        LOG.info("work dir set: " + WORKDIR);
    }

    @AfterAll
    public static void cleanupDB() throws Exception {
        final Properties params = new Properties();
        params.load(MainIntegrationTest.class.getClassLoader()
                .getResourceAsStream(System.getProperty("database.properties.file")));
        BasicDataSource dsStaging = new BasicDataSource();
        dsStaging.setUrl(params.getProperty("staging.url"));
        dsStaging.setUsername(params.getProperty("staging.user"));
        dsStaging.setPassword(params.getProperty("staging.password"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);

        IDatabaseConnection staging = new DatabaseDataSourceConnection(dsStaging);
        if (params.getProperty("dbtype").equalsIgnoreCase("oracle")) {
            staging = new DatabaseConnection(dsStaging.getConnection().unwrap(oracle.jdbc.OracleConnection.class), params.getProperty("staging.user").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        }
        CleanUtil.cleanSTAGING(staging, false);
        // omdat de insert van het bag object mislukt vanwege referentie check hoeft er niet opgeruimd in rsgb
        staging.close();
        dsStaging.close();
    }

    /**
     * Log de naam van de test als deze begint.
     */
    @BeforeEach
    public void startTest(TestInfo testInfo) {
        LOG.info("==== Start test methode: " + testInfo.getDisplayName());
    }

    /**
     * Log de naam van de test als deze eindigt.
     */
    @AfterEach
    public void endTest(TestInfo testInfo) {
        LOG.info("==== Einde test methode: " + testInfo.getDisplayName());
    }

    @ParameterizedTest(name = "test commandline argument: {0}")
    @ValueSource(strings = {
            "--versieinfo",
            "--versieinfo json",
            "--load ../../../brmo-loader/src/test/resources/GH-275/OPR-1884300000000464.xml bag",
            "-l",
            "--list json", "-s",
            "--berichtstatus json",
            "-t"
    })
    public void commandLine(String args) throws Exception {
        Process p = Runtime.getRuntime().exec(BASE_COMMAND + args, new String[]{}, WORKDIR);
        LOG.info(IOUtils.toString(p.getInputStream(), Charset.defaultCharset()));
        String err = IOUtils.toString(p.getErrorStream(), Charset.defaultCharset());
        if (!err.isEmpty()) {
            LOG.error(err);
        }
        p.waitFor(30, TimeUnit.SECONDS);
        assertEquals(0, p.exitValue(), "exit value is geen 0");
    }
}
