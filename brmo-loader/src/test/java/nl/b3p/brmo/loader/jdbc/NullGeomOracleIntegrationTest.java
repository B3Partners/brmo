/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import nl.b3p.brmo.loader.gml.BGTGMLLightLoader;
import oracle.jdbc.OracleConnection;
import oracle.sql.STRUCT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.factory.GeoTools;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeNotNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import static org.junit.Assert.assertNull;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * gebruik
 * {@code mvn -Dit.test=NullGeomOracleIntegrationTest verify -Poracle -Dtest.onlyITs=true}
 * om deze test te draaien.
 *
 * @author mprins
 */
@RunWith(Parameterized.class)
public class NullGeomOracleIntegrationTest {

    private static final Log LOG = LogFactory.getLog(NullGeomOracleIntegrationTest.class);

    protected final Properties params = new Properties();

    protected boolean isOracle;

    private String testVal;

    @Rule
    public TestName name = new TestName();

    @Parameterized.Parameters(name = "{index}: testwaarde: {0}")
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][]{
            {""}, {null}
        });
    }

    public NullGeomOracleIntegrationTest(String testVal) {
        this.testVal = testVal;
    }

    /**
     * test of de database properties zijn aangegeven, zo niet dan skippen we
     * alle tests in deze test.
     */
    @BeforeClass
    public static void checkDatabaseIsProvided() {
        assumeNotNull("Verwacht database omgeving te zijn aangegeven.", System.getProperty("database.properties.file"));
        GeoTools.init();
    }

    @Before
    public void startTest() {
        LOG.info("==== Start test methode: " + name.getMethodName());
    }

    @After
    public void endTest() {
        LOG.info("==== Einde test methode: " + name.getMethodName());
    }

    /**
     * set up test object.
     *
     * @throws IOException als laden van property file mislukt
     */
    @Before
    public void setUp() throws Exception {
        loadProps();
    }

    /**
     * Test next() methode met klein mutatie bestand.
     *
     * @throws Exception if any
     */
    @Test
    public void testNullGeomXML() throws Exception {
        if (isOracle) {
            Connection connection = DriverManager.getConnection(
                    params.getProperty("jdbc.url"),
                    params.getProperty("user"),
                    params.getProperty("passwd"));

            OracleConnection oc = OracleConnectionUnwrapper.unwrap(connection);
            OracleJdbcConverter c = new OracleJdbcConverter(oc);
            STRUCT s = (STRUCT) c.convertToNativeGeometryObject(this.testVal);
            assertEquals("verwacht een sdo geometry", "MDSYS.SDO_GEOMETRY", s.getSQLTypeName());
            for (Object o : s.getAttributes()) {
                assertNull("verwacht 'null'", o);
            }
        }
    }

    /**
     * Laadt de database propery file en eventuele overrides.
     *
     * @throws IOException als laden van property file mislukt
     */
    protected void loadProps() throws IOException {
        // de `database.properties.file` is in de pom.xml of via commandline ingesteld
        params.load(NullGeomOracleIntegrationTest.class.getClassLoader()
                .getResourceAsStream(System.getProperty("database.properties.file")));
        try {
            // probeer een local (override) versie te laden als die bestaat
            params.load(NullGeomOracleIntegrationTest.class.getClassLoader()
                    .getResourceAsStream("local." + System.getProperty("database.properties.file")));
        } catch (IOException | NullPointerException e) {
            // negeren; het override bestand is normaal niet aanwezig
        }
        isOracle = "oracle".equalsIgnoreCase(params.getProperty("dbtype"));
    }
}
