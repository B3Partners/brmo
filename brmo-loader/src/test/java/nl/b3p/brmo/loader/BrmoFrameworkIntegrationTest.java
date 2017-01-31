/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader;

import nl.b3p.AbstractDatabaseIntegrationTest;
import nl.b3p.brmo.loader.util.BrmoException;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Testcases voor {@link BrmoFramework}. Om de tests te runnen gebruik je:
 * {@code mvn -Dit.test=BrmoFrameworkIntegrationTest -Dtest.onlyITs=true integration-test -Poracle > target/oracle.log}
 * voor bijvoorbeeld Oracle.
 *
 * @author mprins
 */
public class BrmoFrameworkIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static String currentVersion;

    @BeforeClass
    public static void getEnvironment() {
        currentVersion = System.getProperty("project.version");
    }

    private BrmoFramework brmo;

    /**
     * setup brmo framework met basic datasources.
     *
     * @throws BrmoException if any
     */
    @Before
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

        brmo = new BrmoFramework(dsStaging, dsRsgb, dsRsgbBgt);
    }

    @After
    public void tearDown() {
        brmo.closeBrmoFramework();
    }

    @Test
    public void testRsgbVersion() {
        assertEquals("Versies komen niet overeen", currentVersion, brmo.getRsgbVersion());
    }

    @Test
    public void testRsgbBgtVersion() {
        assertEquals("Versies komen niet overeen", currentVersion, brmo.getRsgbBgtVersion());
    }

    @Test
    public void testStagingVersion() {
        assertEquals("Versies komen niet overeen", currentVersion, brmo.getStagingVersion());
    }
}
