/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static nl.b3p.brmo.loader.gml.GMLLightFeatureTransformer.ID_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author mprins
 */
public class BGTNullGeomIntegrationTest extends TestingBase {

    private static final Log LOG = LogFactory.getLog(BGTNullGeomIntegrationTest.class);
    private final Lock sequential = new ReentrantLock();
    private BGTGMLLightLoader ldr;

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                arguments("/gmllight/kruinlijntest/bgt_ondersteunendwegdeel.gml", "ondersteunend_wegdeel",
                        431 - 28 /* duplicaten */ - 6 /* vervallen of nog niet bestaand */,
                        2 /* kruinlijn elementen */),
                arguments("/gmllight/mantis6028/bgt_begroeidterreindeel.gml", "begroeid_terreindeel", 1, 1)
        );
    }

    /**
     * set up test object.
     *
     * @throws IOException als laden van property file mislukt
     */
    @BeforeEach
    public void setUp() throws Exception {
        loadProps();
        sequential.lock();

        ldr = new BGTGMLLightLoader();
        ldr.setDbConnProps(params);
        ldr.setCreateTables(false);

        clearTables();
    }

    @AfterEach
    public void cleanUp() throws Exception {
        clearTables();
        sequential.unlock();
    }

    /**
     * test parsen en laden van 1 GML bestand in bestaande tabel.
     *
     * @param gmlFileName               filename of the test resource (GML)
     * @param tableName                 tabel naam
     * @param expectedNumOfElements     number of elements in resources
     * @param expectedKruinlijnElements aantal kruinlijn elementen
     * @throws Exception if any
     */
    @DisplayName("Process GML file")
    @ParameterizedTest(name = "{index}: gml file: ''{0}''")
    @MethodSource("argumentsProvider")
    public void testProcessGMLFile(String gmlFileName, String tableName, int expectedNumOfElements,
                                   int expectedKruinlijnElements) throws Exception {

        int nullGeomCount = expectedNumOfElements - expectedKruinlijnElements;

        File gml = new File(BGTLightKruinlijnIntegrationTest.class.getResource(gmlFileName).toURI());
        int actualElements = ldr.processGMLFile(gml);

        assertEquals(BGTGMLLightLoader.STATUS.OK, ldr.getStatus());
        assertEquals(expectedNumOfElements, actualElements, "Aantal geschreven features");

        try (Connection connection = DriverManager.getConnection(
                params.getProperty("jdbc.url"),
                params.getProperty("user"),
                params.getProperty("passwd"))
        ) {
            connection.setAutoCommit(true);
            String sql = "SELECT COUNT(" + ID_NAME + ") FROM \""
                    + params.getProperty("schema")
                    + "\".\"" + tableName + "\" WHERE kruinlijn is null";
            sql = isOracle ? sql.toUpperCase() : sql;

            try {
                ResultSet count = connection.createStatement().executeQuery(sql);
                count.next();
                int counted = count.getInt(1);
                assertEquals(nullGeomCount, counted, "Aantal 'null' kruinlijn elementen");
            } catch (SQLException se) {
                LOG.error("Fout tijdens tellen 'null' kruinlijn elementen: ", se);
                fail("Fout tijdens tellen 'null' kruinlijn elementen: " + se.getLocalizedMessage());
            }
        }
    }
}
