/*
 * Copyright (C) 2019 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.brmo.service.scanner;

import java.io.File;
import nl.b3p.brmo.persistence.staging.BRKScannerProces;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.service.testutil.TestUtil;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static nl.b3p.brmo.loader.BrmoFramework.BR_BRK;
import static nl.b3p.brmo.persistence.staging.LaadProces.STATUS.STAGING_OK;
import static nl.b3p.brmo.persistence.staging.LaadProces.STATUS.STAGING_DUPLICAAT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * run:
 * {@code mvn -Dit.test=BRKDirectoryScannerIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql}
 *
 * @author mprins
 */
public class BRKDirectoryScannerIntegrationTest extends TestUtil {

    private static final Log LOG = LogFactory.getLog(BRKDirectoryScannerIntegrationTest.class);

    private BrmoFramework brmo;
    private BRKDirectoryScanner scanner;

    @Before
    @Override
    public void setUp() throws BrmoException {
        brmo = new BrmoFramework(dsStaging, null);

        /*
         * TODO werkend maken...
         * Stripersist.requestInit();
         * of
         * @Mock
         * private EntityManager entityManager;
         * Mockito.when(METHOD_EXPECTED_TO_BE_CALLED).thenReturn(AnyObjectoftheReturnType);
         *
         */
        assumeNotNull("Het test bestand moet er zijn.", BRKDirectoryScannerIntegrationTest.class.getResource("/duplicaatberichten/MUTBX01.xml"));
        String scanDirectory = BRKDirectoryScannerIntegrationTest.class.getResource("/duplicaatberichten/MUTBX01.xml").getPath();

        BRKScannerProces config = new BRKScannerProces();
        config.setScanDirectory(scanDirectory);
        config.setArchiefDirectory(scanDirectory + File.separatorChar + "archief");
        scanner = new BRKDirectoryScanner(config);
    }

    @After
    public void tearDown() throws BrmoException {
//        Stripersist.requestComplete();
        brmo.emptyStagingDb();
        brmo.closeBrmoFramework();
    }

    @Test
    @Ignore("TODO deze test werkt nu niet omdat de stripes entity manager niet geinitialiseerd wordt, de test draait niet in de servlet container.")
    public void testDuplicaatBerichten() throws BrmoException {
        scanner.execute();
        assertEquals("Aantal berichten is niet gelijk.", 2L, brmo.getCountBerichten(null, null, BR_BRK, STAGING_OK.toString()));
        assertEquals("Aantal OK laadprocessen is niet gelijk.", 2L, brmo.getCountLaadProcessen(null, null, BR_BRK, STAGING_OK.toString()));
        assertEquals("Aantal duplicaat laadprocessen is niet gelijk.", 3L, brmo.getCountLaadProcessen(null, null, BR_BRK, STAGING_DUPLICAAT.toString()));
    }
}
