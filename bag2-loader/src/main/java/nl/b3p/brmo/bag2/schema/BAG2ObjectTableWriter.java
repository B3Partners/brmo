/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.schema;

import nl.b3p.brmo.bag2.loader.BAG2GMLObjectStream;
import nl.b3p.brmo.schema.ObjectTableWriter;
import nl.b3p.brmo.schema.ObjectType;
import nl.b3p.brmo.schema.SchemaObjectInstance;
import nl.b3p.brmo.schema.SchemaSQLMapper;
import nl.b3p.brmo.schema.mapping.AttributeColumnMapping;
import nl.b3p.brmo.sql.PreparedStatementQueryBatch;
import nl.b3p.brmo.sql.QueryBatch;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static nl.b3p.brmo.bag2.schema.BAG2Schema.TIJDSTIP_NIETBAGLV;

public class BAG2ObjectTableWriter extends ObjectTableWriter {
    private static final Log log = LogFactory.getLog(BAG2ObjectTableWriter.class);

    public class BAG2Progress extends ObjectTableWriter.Progress {
        private Map<ObjectType, QueryBatch> deleteBatches = new HashMap<>();

        private long updatedCount = 0;

        public long getUpdatedCount() {
            return updatedCount;
        }

        public BAG2ObjectTableWriter getWriter() {
            return BAG2ObjectTableWriter.this;
        }
    }

    public BAG2ObjectTableWriter(Connection connection, SQLDialect dialect, SchemaSQLMapper schemaSQLMapper) {
        super(connection, dialect, schemaSQLMapper);
    }


    public BAG2ObjectTableWriter.BAG2Progress getProgress() {
        return (BAG2ObjectTableWriter.BAG2Progress) super.getProgress();
    }

    public void start() throws SQLException {
        BAG2ObjectTableWriter.BAG2Progress progress = this.new BAG2Progress();
        progress.setInitialLoad(true);
        super.start(progress);
        updateProgress(Stage.PARSE_INPUT);
    }

    private void deletePreviousVersion(BAG2Object object) throws Exception {
        BAG2ObjectType objectType = object.getObjectType();
        Map<ObjectType,QueryBatch> deleteBatches = getProgress().deleteBatches;
        if (!deleteBatches.containsKey(objectType)) {
            String args = objectType.getPrimaryKeys().stream()
                    .map(k -> getSchemaSQLMapper().getColumnNameForObjectType(objectType, k.getName()) + " = ?")
                    .collect(Collectors.joining(" and "));
            // Array attributes are deleted because of 'on delete cascade' on the foreign key
            String sql = String.format("delete from %s where %s", getSchemaSQLMapper().getTableNameForObjectType(objectType, getTablePrefix()), args);
            // Set batch size to 1 so deletes are executed immediately, for performance deletes could be batched but
            // they would need to be executed before inserts of updated versions (with the same key), that is more
            // complicated
            deleteBatches.put(objectType, new PreparedStatementQueryBatch(getConnection(), sql, 1));
        }
        QueryBatch batch = deleteBatches.get(objectType);

        Object[] params = objectType.getPrimaryKeys().stream().map(pk -> {
            try {
                AttributeColumnMapping mapping = objectType.getAttributeByName(pk.getName());
                Object attribute = object.getAttributes().get(pk.getName());
                return mapping.toQueryParameter(attribute);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).toArray();

        boolean executed = batch.addBatch(params);
        getProgress().updatedCount++;

        if (executed) {
            updateProgress();
        }
    }

    @Override
    protected void addObjectToBatch(SchemaObjectInstance object) throws Exception {
        // Never write NIET BAG objects to database
        if (!object.getAttributes().containsKey(TIJDSTIP_NIETBAGLV)) {
            super.addObjectToBatch(object);
        }
    }

    public void write(InputStream bagXml) throws Exception {
        CountingInputStream counter = new CountingInputStream(bagXml);
        BAG2GMLObjectStream bag2Objects = new BAG2GMLObjectStream(counter);
        updateProgress(Stage.LOAD_OBJECTS);

        try {
            for (BAG2Object object: bag2Objects) {

                prepareDatabaseForObject(object);

                getProgress().incrementObjectCount();

                if (object.getWijzigingWordt() != null) {
                    // Don't do an update but a simpler delete and insert of the updated version
                    // Executed on main thread, but worker thread will not be executing new versions of the record we're
                    // deleting
                    deletePreviousVersion(object.getWijzigingWas());
                    addObjectToBatch(object.getWijzigingWordt());
                }

                if (object.getMutatieStatus() == BAG2Object.MutatieStatus.TOEVOEGING) {
                    addObjectToBatch(object);
                }

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
        for(QueryBatch batch: getProgress().deleteBatches.values()) {
            batch.executeBatch();
        }
        super.complete();
        super.closeBatches();
    }
}
