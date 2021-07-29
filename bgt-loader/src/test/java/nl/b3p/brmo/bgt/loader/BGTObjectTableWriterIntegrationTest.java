/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bgt.loader;

import nl.b3p.brmo.bgt.loader.cli.LoadOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
    @Disabled
    public void loadPand() throws Exception {
        LoadOptions loadOptions = new LoadOptions();
        loadOptions.setIncludeHistory(true);
        BGTObjectTableWriter writer = db.createObjectTableWriter(loadOptions, dbOptions);
        writer.write(BGTTestFiles.getTestInputStream("bgt_mutatie_initial_pand.xml"));
        db.close();
        assertDataSetEquals("pand,nummeraanduidingreeks", "bgt_mutatie_initial_pand");
    }
}