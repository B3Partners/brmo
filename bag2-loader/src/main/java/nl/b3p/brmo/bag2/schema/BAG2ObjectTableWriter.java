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
import java.sql.SQLException;

import static nl.b3p.brmo.bag2.schema.BAG2Schema.TIJDSTIP_NIETBAGLV;

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

    public void start() throws SQLException {
        BAG2ObjectTableWriter.BAG2Progress progress = this.new BAG2Progress();
        progress.setInitialLoad(true);
        super.start(progress);
        updateProgress(Stage.PARSE_INPUT);
    }

    public void write(InputStream bagXml) throws Exception {
        CountingInputStream counter = new CountingInputStream(bagXml);
        BAG2GMLObjectStream bag2Objects = new BAG2GMLObjectStream(counter);
        updateProgress(Stage.LOAD_OBJECTS);

        try {
            for (BAG2Object object: bag2Objects) {
                if (object.getAttributes().containsKey(TIJDSTIP_NIETBAGLV)) {
                    // See BAG2Schema for explanation
                    throw new IllegalArgumentException("\"Niet BAG\" objects not supported");
                }

                prepareDatabaseForObject(object);

                getProgress().incrementObjectCount();

                addObjectToBatch(object);

                if (getObjectLimit() != null && getProgress().getObjectCount() == getObjectLimit()) {
                    break;
                }
            }
        } catch(Exception e) {
            if (isMultithreading()) {
                // Make sure worker thread exits
                abortWorkerThread();
            }
            throw e;
        }
    }

    public void complete() throws Exception {
        super.endOfObjects();
        super.complete();
        super.closeBatches();
    }
}
