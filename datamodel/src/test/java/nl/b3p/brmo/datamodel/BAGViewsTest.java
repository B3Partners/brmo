/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */
package nl.b3p.brmo.datamodel;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Test de BAG 2 basis views.
 *
 * @author mprins
 */
public class BAGViewsTest {
    private static final Log LOG = LogFactory.getLog(BAGViewsTest.class);
    private static final String dbUrl = System.getProperty("bag.dburl");
    private static final String dbUser = System.getProperty("bag.dbuser", "rsgb");
    private static final String dbPass = System.getProperty("bag.dbpassword", "rsgb");
    private IDatabaseConnection bag;

    @BeforeEach
    void setUp() throws Exception {
        assumeFalse(StringUtils.isEmpty(dbUrl), "skipping integration test: missing database url");
        bag = new DatabaseConnection(DriverManager.getConnection(dbUrl, dbUser, dbPass));

        if (dbUrl.contains("postgresql")) {
            bag.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            bag.getConfig().setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, false);
        }
        if (dbUrl.contains("oracle")) {
            bag.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            bag.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
            bag.getConfig().setProperty(DatabaseConfig.FEATURE_CASE_SENSITIVE_TABLE_NAMES, false);
        }
    }

    @AfterEach
    void cleanup() throws SQLException {
        if (null != bag) bag.close();
    }

    /**
     * test of de bekende set met views in de database bestaan.
     *
     * @throws SQLException als opzoeken in de dabase mislukt
     */
    @Test
    public void testBasisViewsBAG() throws SQLException, DataSetException {
        ITable views = null;
        if (dbUrl.contains("oracle")) {
            views = bag.createQueryTable("views", "SELECT VIEW_NAME AS name FROM USER_VIEWS");
        }
        if (dbUrl.contains("postgresql")) {
            views = bag.createQueryTable("views", "SELECT oid::regclass::text as name FROM pg_class WHERE relkind = 'v'");
        }

        List<String> viewsFound = new ArrayList<>();
        String vName;
        for (int i = 0; i < views.getRowCount(); i++) {
            vName = views.getValue(i, "name").toString().toLowerCase(Locale.ROOT);
            if (vName.startsWith("vb_")) {
                viewsFound.add(vName);
            }
        }

        LOG.debug("gevonden views: " + viewsFound);
        assertNotNull(viewsFound, "Geen views gevonden");
        assertFalse(viewsFound.isEmpty(), "Geen views gevonden");

        List<String> expectedViews = Arrays.asList(
                // bag 2 views
                "vb_adresseerbaar_object_geometrie", "vb_verblijfsobject_adres", "vb_standplaats_adres", "vb_ligplaats_adres", "vb_adres");

        // alles lower-case (ORACLE!) en gesorteerd vergelijken
        viewsFound.replaceAll(String::toLowerCase);
        expectedViews.replaceAll(String::toLowerCase);
        Collections.sort(viewsFound);
        Collections.sort(expectedViews);
        assertEquals(expectedViews, viewsFound, "lijsten met basis views zijn ongelijk");
    }
}

