/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader;

import org.dbunit.dataset.DataSetException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.sql.SQLException;

import static nl.b3p.brmo.bgt.loader.BGTTestFiles.extractRowCounts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integrates with loading BGT into a database and downloads from the PDOK download service.
 */
@DisabledOnOs(OS.WINDOWS) // Does not have NL overheid SSL root certificate
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DownloadCommandPDOKOnlineIntegrationTest extends CommandLineTestBase {
    @Test
    @Order(1)
    void downloadInitial() throws SQLException, DataSetException, IOException {
        run("download initial", "--include-history", "--feature-types=all,plaatsbepalingspunt", "--geo-filter=Polygon ((131021 458768, 131021 459259, 131694 459259, 131694 458768, 131021 458768))");
        String initialDeltaId = db.getMetadata(BGTSchemaMapper.Metadata.INITIAL_LOAD_DELTA_ID);
        assertNotNull(initialDeltaId);
        String deltaId = db.getMetadata(BGTSchemaMapper.Metadata.DELTA_ID);
        assertEquals(initialDeltaId, deltaId);

        // Test minimum row counts, which should only grow because historic objects are also loaded
        for(BGTTestFiles.BGTRowCount tableMinRowCount: extractRowCounts) {
            Integer actualRowCount = dbTestConnection.getRowCount(tableMinRowCount.table);
            assertTrue(actualRowCount >= tableMinRowCount.rowCountWithHistory,
                    String.format("Expected a minimum row count of %d for table %s, actual %d",
                            tableMinRowCount.rowCountWithHistory,
                            tableMinRowCount.table,
                            actualRowCount)
            );
        }
    }

    @Test
    @Order(2)
    @SkipDropTables
    void downloadUpdate() {
        run("download update");
    }
}
