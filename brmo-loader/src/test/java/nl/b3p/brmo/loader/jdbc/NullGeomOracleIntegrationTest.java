/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Struct;
import java.util.Arrays;
import java.util.Collection;
import nl.b3p.AbstractDatabaseIntegrationTest;
import oracle.jdbc.OracleConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.factory.GeoTools;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;
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
public class NullGeomOracleIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(NullGeomOracleIntegrationTest.class);

    @Parameterized.Parameters(name = "{index}: testwaarde: {0}")
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][]{
            {""}, {null}
        });
    }

    @BeforeClass
    public static void geotoolsInit() {
        GeoTools.init();
    }
    private final String testVal;

    public NullGeomOracleIntegrationTest(String testVal) {
        this.testVal = testVal;
    }

    /**
     * set up test object.
     *
     * @throws IOException als laden van property file mislukt
     */
    @Before
    @Override
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
        assumeTrue("Deze test alleen op Oracle uitvoeren", isOracle);
        LOG.info("Testen van 'null' geometrie voor Oracle");

        Connection connection = DriverManager.getConnection(
                params.getProperty("rsgb.jdbc.url"),
                params.getProperty("rsgb.user"),
                params.getProperty("rsgb.passwd")
        );

        OracleConnection oc = OracleConnectionUnwrapper.unwrap(connection);
        OracleJdbcConverter c = new OracleJdbcConverter(oc);
        Struct s = (Struct) c.convertToNativeGeometryObject(this.testVal);
        assertEquals("Verwacht een sdo geometry type", "MDSYS.SDO_GEOMETRY", s.getSQLTypeName());
        for (Object o : s.getAttributes()) {
            assertNull("verwacht 'null'voor attribuut", o);
        }
    }
}
