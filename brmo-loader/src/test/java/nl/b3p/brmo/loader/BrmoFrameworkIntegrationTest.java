/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader;

import nl.b3p.AbstractDatabaseIntegrationTest;
import nl.b3p.brmo.loader.util.BrmoException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testcases voor {@link BrmoFramework}. Om de tests te runnen gebruik je:
 * {@code mvn -Dit.test=BrmoFrameworkIntegrationTest -Dtest.onlyITs=true integration-test -Poracle > target/oracle.log}
 * voor bijvoorbeeld Oracle.
 *
 * @author mprins
 */
public class BrmoFrameworkIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static String currentVersion;

    @BeforeAll
    public static void getEnvironment() {
        currentVersion = System.getProperty("project.version");
        if ("true".equalsIgnoreCase(System.getProperty("database.upgrade"))) {
            currentVersion = currentVersion.replace("-SNAPSHOT", "");
        }
    }

    private BrmoFramework brmo;

    /**
     * setup brmo framework met basic datasources.
     *
     * @throws BrmoException if any
     */
    @BeforeEach
    @Override
    public void setUp() throws BrmoException {
        BasicDataSource dsStaging = new BasicDataSource();
        dsStaging.setUrl(params.getProperty("staging.jdbc.url"));
        dsStaging.setUsername(params.getProperty("staging.user"));
        dsStaging.setPassword(params.getProperty("staging.passwd"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);

        BasicDataSource dsRsgb = new BasicDataSource();
        dsRsgb.setUrl(params.getProperty("rsgb.jdbc.url"));
        dsRsgb.setUsername(params.getProperty("rsgb.user"));
        dsRsgb.setPassword(params.getProperty("rsgb.passwd"));
        dsRsgb.setAccessToUnderlyingConnectionAllowed(true);

        BasicDataSource dsRsgbBgt = new BasicDataSource();
        dsRsgbBgt.setUrl(params.getProperty("rsgbbgt.jdbc.url"));
        dsRsgbBgt.setUsername(params.getProperty("rsgbbgt.user"));
        dsRsgbBgt.setPassword(params.getProperty("rsgbbgt.passwd"));
        dsRsgbBgt.setAccessToUnderlyingConnectionAllowed(true);

        brmo = new BrmoFramework(dsStaging, dsRsgb, dsRsgbBgt, null);
    }

    @AfterEach
    public void tearDown() {
        brmo.closeBrmoFramework();
    }

    @Test
    public void testRsgbVersion() {
        assertEquals(currentVersion, brmo.getRsgbVersion(), "Versies komen niet overeen");
    }

    @Test
    @Disabled
    public void testRsgbBgtVersion() {
        assertEquals(currentVersion, brmo.getRsgbBgtVersion(), "Versies komen niet overeen");
    }

    @Test
    public void testStagingVersion() {
        assertEquals(currentVersion, brmo.getStagingVersion(), "Versies komen niet overeen");
    }
}
