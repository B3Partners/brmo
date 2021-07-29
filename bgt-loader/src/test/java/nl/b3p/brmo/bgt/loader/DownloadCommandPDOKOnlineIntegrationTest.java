/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integrates with loading BGT into a database and downloads from the PDOK download service.
 */
//@EnabledIfSystemProperty(named="onlineTest", matches=".*")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DownloadCommandPDOKOnlineIntegrationTest extends CommandLineTestBase {

    @Override
    void tearDown() throws SQLException {
        // Do not drop tables
    }

    @Test
    @Order(1)
    void downloadInitial() throws SQLException {
        try {
            run("download initial", "--no-geo-filter", "--include-history", "--feature-types=stadsdeel,waterschap");
            String initialDeltaId = db.getMetadata(BGTSchemaMapper.Metadata.INITIAL_LOAD_DELTA_ID);
            assertNotNull(initialDeltaId);
            String deltaId = db.getMetadata(BGTSchemaMapper.Metadata.DELTA_ID);
            assertEquals(initialDeltaId, deltaId);
        } catch(Exception e) {
            try {
                super.tearDown();
            } catch(Exception ignored) {

            }
            throw e;
        }
    }

    @Test
    @Order(2)
    void downloadUpdate() {
        try {
            run("download update");
        } finally {
            try {
                super.tearDown();
            } catch(Exception ignored) {
            }
        }
    }
}
