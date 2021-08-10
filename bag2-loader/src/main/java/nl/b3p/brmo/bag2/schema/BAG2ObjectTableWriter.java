/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.schema;

import nl.b3p.brmo.bag2.loader.BAG2GMLObjectStream;
import nl.b3p.brmo.schema.ObjectTableWriter;
import nl.b3p.brmo.schema.SchemaSQLMapper;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.sql.Connection;

public class BAG2ObjectTableWriter extends ObjectTableWriter {
    private static final Log log = LogFactory.getLog(BAG2ObjectTableWriter.class);

    public class BAG2Progress extends ObjectTableWriter.Progress {
        private CountingInputStream counter;

        public BAG2ObjectTableWriter getWriter() {
            return BAG2ObjectTableWriter.this;
        }
    }

    public BAG2ObjectTableWriter(Connection connection, SQLDialect dialect, SchemaSQLMapper schemaSQLMapper) {
        super(connection, dialect, schemaSQLMapper);
    }

    public void write(InputStream bagXml) throws Exception {
        BAG2ObjectTableWriter.BAG2Progress progress = this.new BAG2Progress();
        super.start(progress);
        updateProgress(Stage.PARSE_INPUT);

        try(CountingInputStream counter = new CountingInputStream(bagXml)) {
            progress.counter = counter;
            BAG2GMLObjectStream bag2Objects = new BAG2GMLObjectStream(counter);
            updateProgress(Stage.LOAD_OBJECTS);

            progress.setInitialLoad(true);

            for (BAG2Object object: bag2Objects) {
                prepareDatabaseForObject(object);

                progress.incrementObjectCount();

                addObjectToBatch(object);

                if (getObjectLimit() != null && progress.getObjectCount() == getObjectLimit()) {
                    break;
                }
            }

            super.endOfObjects();
            super.complete();
        } finally {
            super.closeBatches();
        }
    }
}
