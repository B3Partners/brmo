/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bgt.loader;

import nl.b3p.brmo.bgt.loader.cli.LoadOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public class BGTObjectTableWriterIntegrationTest extends DBTestBase {
    @Test
    public void loadStadsdeel() throws Exception {
        LoadOptions loadOptions = new LoadOptions();
        loadOptions.setIncludeHistory(true);
        BGTObjectTableWriter writer = db.createObjectTableWriter(loadOptions, dbOptions);
        writer.write(BGTTestFiles.getTestInputStream("bgt_stadsdeel.gml"));
        db.close();
        assertDataSetEquals("stadsdeel", "bgt_stadsdeel");
    }

    @Test
    @EnabledIfSystemProperty(named = "db.connectionString", matches = ".*postgresql.*")
    public void loadWijkLinearized() throws Exception {
        LoadOptions loadOptions = new LoadOptions();
        loadOptions.setLinearizeCurves(true);
        BGTObjectTableWriter writer = db.createObjectTableWriter(loadOptions, dbOptions);
        writer.write(BGTTestFiles.getTestInputStream("bgt_wijk_curve.gml"));
        db.close();
        assertDataSetEquals("wijk", "bgt_wijk_linearized");
    }
}