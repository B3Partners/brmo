/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.commandline;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.dbcp.BasicDataSource;
import static org.junit.Assert.assertTrue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;

/**
 * run met:
 * {@code mvn -Dit.test=MainIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 * NB. de zipfile wordt uitgepakt door Maven.
 *
 * @author mprins
 */
@RunWith(Parameterized.class)
public class MainIntegrationTest {

    private static final Log LOG = LogFactory.getLog(MainIntegrationTest.class);
    private static File WORKDIR;
    private static final String BASE_COMMAND = "java -Dlog4j.configuration=file:./conf/test-log4j.xml -jar ./bin/brmo-commandline.jar --dbprops ./conf/test.properties ";

    @Parameterized.Parameters(name = "param {index}: args: {0}")
    public static Collection params() {
        return Arrays.asList(new Object[][]{
            // {"args"},
            {"--versieinfo"},
            {"--versieinfo json"},
            {"--load ../../../brmo-loader/src/test/resources/GH-275/OPR-1884300000000464.xml bag"},
            {"-l"},
            {"--list json"}, {"-s"},
            {"--berichtstatus json"},
            {"-t"}
        });
    }

    @BeforeClass
    public static void getWorkDir() {
        WORKDIR = new File(MainIntegrationTest.class.getResource("/").getPath() + "../itest/");
    }

    @AfterClass
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
        CleanUtil.cleanSTAGING(staging);
        // omdat de insert van het bag object mislukt vanwege referentie check hoeft er niet opgeruimd in rsgb
        staging.close();
        dsStaging.close();
    }

    /**
     * logging rule.
     */
    @Rule
    public TestName name = new TestName();

    private final String args;

    public MainIntegrationTest(final String args) {
        this.args = args;
    }

    /**
     * Log de naam van de test als deze begint.
     */
    @Before
    public void startTest() {
        LOG.info("==== Start test methode: " + name.getMethodName());
    }

    /**
     * Log de naam van de test als deze eindigt.
     */
    @After
    public void endTest() {
        LOG.info("==== Einde test methode: " + name.getMethodName());
    }

    @Test
    public void commandLine() throws Exception {
        Process p = Runtime.getRuntime().exec(BASE_COMMAND + args, new String[]{}, WORKDIR);
        LOG.info(IOUtils.toString(p.getInputStream(), Charset.defaultCharset()));
        LOG.error(IOUtils.toString(p.getErrorStream(), Charset.defaultCharset()));
        p.waitFor(30, TimeUnit.SECONDS);
        assertTrue("exit value is geen 0", p.exitValue() == 0);
    }
}
