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

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.BRKScannerProces;
import nl.b3p.brmo.service.testutil.TestUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

import static nl.b3p.brmo.loader.BrmoFramework.BR_BRK;
import static nl.b3p.brmo.persistence.staging.LaadProces.STATUS.STAGING_DUPLICAAT;
import static nl.b3p.brmo.persistence.staging.LaadProces.STATUS.STAGING_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

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

    @BeforeEach
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
        assumeFalse(BRKDirectoryScannerIntegrationTest.class.getResource("/duplicaatberichten/MUTBX01.xml") == null, "Het test bestand moet er zijn.");
        String scanDirectory = BRKDirectoryScannerIntegrationTest.class.getResource("/duplicaatberichten/MUTBX01.xml").getPath();

        BRKScannerProces config = new BRKScannerProces();
        config.setScanDirectory(scanDirectory);
        config.setArchiefDirectory(scanDirectory + File.separatorChar + "archief");
        scanner = new BRKDirectoryScanner(config);
    }

    @AfterEach
    public void tearDown() throws BrmoException {
//        Stripersist.requestComplete();
        brmo.emptyStagingDb();
        brmo.closeBrmoFramework();
    }

    @Test
    @Disabled("TODO deze test werkt nu niet omdat de stripes entity manager niet geinitialiseerd wordt, de test draait niet in de servlet container.")
    public void testDuplicaatBerichten() throws BrmoException {
        scanner.execute();
        assertEquals(2L, brmo.getCountBerichten(null, null, BR_BRK, STAGING_OK.toString()), "Aantal berichten is niet gelijk.");
        assertEquals(2L, brmo.getCountLaadProcessen(null, null, BR_BRK, STAGING_OK.toString()), "Aantal OK laadprocessen is niet gelijk.");
        assertEquals(3L, brmo.getCountLaadProcessen(null, null, BR_BRK, STAGING_DUPLICAAT.toString()), "Aantal duplicaat laadprocessen is niet gelijk.");
    }
}
