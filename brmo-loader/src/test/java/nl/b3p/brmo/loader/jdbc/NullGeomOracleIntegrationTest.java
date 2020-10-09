/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.jdbc;

import nl.b3p.AbstractDatabaseIntegrationTest;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
import nl.b3p.loader.jdbc.OracleJdbcConverter;
import oracle.jdbc.OracleConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.util.factory.GeoTools;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Struct;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * gebruik
 * {@code mvn -Dit.test=NullGeomOracleIntegrationTest verify -Poracle -Dtest.onlyITs=true}
 * om deze test te draaien.
 *
 * @author mprins
 */
public class NullGeomOracleIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(NullGeomOracleIntegrationTest.class);

    /**
     * test of de database properties zijn aangegeven, zo niet dan skippen we
     * alle tests in deze test.
     */
    @BeforeAll
    public static void checkDatabaseIsProvided() {
        GeoTools.init();
    }

    /**
     * set up test object.
     *
     * @throws IOException als laden van property file mislukt
     */
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        loadProps();
    }

    /**
     *
     * @throws Exception if any
     */
    @DisplayName("test null geometrie XML")
    @ParameterizedTest(name = "#{index} - waarde: {0}")
    @NullAndEmptySource
    @ValueSource(strings = {"", " ", "   "})
    public void testNullGeomXML(String testVal) throws Exception {
        if (isOracle) {
            Connection connection = DriverManager.getConnection(
                    params.getProperty("rsgb.jdbc.url"),
                    params.getProperty("rsgb.user"),
                    params.getProperty("rsgb.passwd"));

            OracleConnection oc = OracleConnectionUnwrapper.unwrap(connection);
            OracleJdbcConverter c = new OracleJdbcConverter(oc);
            Struct s = (Struct) c.convertToNativeGeometryObject(testVal);
            assertEquals("MDSYS.SDO_GEOMETRY", s.getSQLTypeName(), "verwacht een sdo geometry");
            for (Object o : s.getAttributes()) {
                assertNull(o, "verwacht 'null'");
            }
        }
    }
}
