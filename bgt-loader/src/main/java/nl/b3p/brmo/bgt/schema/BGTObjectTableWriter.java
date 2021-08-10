/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bgt.schema;

import nl.b3p.brmo.bgt.loader.BGTObjectStreamer;
import nl.b3p.brmo.schema.ObjectTableWriter;
import nl.b3p.brmo.schema.ObjectType;
import nl.b3p.brmo.schema.SchemaSQLMapper;
import nl.b3p.brmo.sql.PreparedStatementQueryBatch;
import nl.b3p.brmo.sql.QueryBatch;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static nl.b3p.brmo.bgt.schema.BGTObject.MutatieStatus.WAS_WORDT;
import static nl.b3p.brmo.bgt.schema.BGTObject.MutatieStatus.WORDT;
import static nl.b3p.brmo.bgt.schema.BGTSchema.EIND_REGISTRATIE;

public class BGTObjectTableWriter extends ObjectTableWriter {
    private static final Log log = LogFactory.getLog(BGTObjectTableWriter.class);

    private boolean currentObjectsOnly = true;

    public boolean isCurrentObjectsOnly() {
        return currentObjectsOnly;
    }

    public void setCurrentObjectsOnly(boolean currentObjectsOnly) {
        this.currentObjectsOnly = currentObjectsOnly;
    }

    public class BGTProgress extends ObjectTableWriter.Progress {
        private CountingInputStream counter;

        private Map<ObjectType, QueryBatch> deleteBatches = new HashMap<>();

        private long objectUpdatedCount = 0;
        private long objectRemovedCount = 0;
        private long historicObjectsCount = 0;

        private BGTObjectStreamer.MutatieInhoud mutatieInhoud;

        public long getBytesRead() {
            return counter.getByteCount();
        }

        public long getObjectUpdatedCount() {
            return objectUpdatedCount;
        }

        public long getObjectRemovedCount() {
            return objectRemovedCount;
        }

        public long getHistoricObjectsCount() {
            return historicObjectsCount;
        }

        public BGTObjectStreamer.MutatieInhoud getMutatieInhoud() {
            return mutatieInhoud;
        }

        public BGTObjectTableWriter getWriter() {
            return BGTObjectTableWriter.this;
        }
    }

    public BGTObjectTableWriter(Connection connection, SQLDialect dialect, SchemaSQLMapper schemaSQLMapper) {
        super(connection, dialect, schemaSQLMapper);
    }

    public BGTProgress getProgress() {
        return (BGTProgress) super.getProgress();
    }

    private void deletePreviousVersion(BGTObject object) throws Exception {
        BGTObjectType objectType = object.getObjectType();
        String idAttributeName = objectType.getPrimaryKeys().get(0).getName();
        String tableName = getSchemaSQLMapper().getTableNameForObjectType(objectType, getTablePrefix());
        Map<ObjectType,QueryBatch> deleteBatches = getProgress().deleteBatches;
        if(!deleteBatches.containsKey(objectType)) {
            String sql = "delete from " + tableName + " where " + getSchemaSQLMapper().getColumnNameForObjectType(objectType, idAttributeName) + " = ?";
            deleteBatches.put(objectType, new PreparedStatementQueryBatch(getConnection(), sql, getBatchSize()));
        }
        QueryBatch batch = deleteBatches.get(objectType);

        boolean executed = batch.addBatch(new Object[] {object.getMutatiePreviousVersionGmlId()});

        for(ObjectType oneToManyObjectType: objectType.getOneToManyAttributeObjectTypes()) {
            if(!deleteBatches.containsKey(oneToManyObjectType)) {
                String tableNameNoPrefix = getSchemaSQLMapper().getTableNameForObjectType(objectType, "");
                String sql = "delete from " + getSchemaSQLMapper().getTableNameForObjectType(oneToManyObjectType, getTablePrefix()) + " where "
                        + getSchemaSQLMapper().getColumnNameForObjectType(oneToManyObjectType, tableNameNoPrefix + idAttributeName) + " = ?";
                deleteBatches.put(oneToManyObjectType, new PreparedStatementQueryBatch(getConnection(), sql, getBatchSize()));
            }
            QueryBatch deleteBatch = deleteBatches.get(oneToManyObjectType);
            executed = executed | deleteBatch.addBatch(new Object[] {object.getMutatiePreviousVersionGmlId()});
        }

        if (executed) {
            updateProgress();
        }
    }

    public void write(InputStream bgtXml) throws Exception {
        BGTProgress progress = this.new BGTProgress();
        super.start(progress);
        updateProgress(Stage.PARSE_INPUT);

        try(CountingInputStream counter = new CountingInputStream(bgtXml)) {
            progress.counter = counter;

            BGTObjectStreamer streamer = new BGTObjectStreamer(counter);
            progress.mutatieInhoud = streamer.getMutatieInhoud();
            updateProgress(Stage.LOAD_OBJECTS);

            progress.setInitialLoad(progress.mutatieInhoud == null || "initial".equals(progress.getMutatieInhoud().getMutatieType()));

            for (BGTObject object: streamer) {
                // We must prepare even if all objects are historic (such as can happen with "ongeclassificeerdobject"),
                // otherwise there won't be a table for updates
                prepareDatabaseForObject(object);

                boolean skipHistoricObject = object.getAttributes().get(EIND_REGISTRATIE) != null && currentObjectsOnly;
                if (object.getMutatieStatus() == WAS_WORDT) {
                    // Deletes and inserts do not need to be DELETE'd and INSERT-ed in order, because different versions
                    // still have a unique primary key value (gml ID)

                    // Also delete oneToMany
                    deletePreviousVersion(object);

                    if (skipHistoricObject) {
                        progress.objectRemovedCount++;
                    } else {
                        progress.objectUpdatedCount++;
                    }
                } else if(object.getMutatieStatus() == WORDT) {
                    if (skipHistoricObject) {
                        progress.historicObjectsCount++;
                    } else {
                        progress.incrementObjectCount();
                    }
                }

                if (skipHistoricObject) {
                    continue;
                }

                addObjectToBatch(object);

                if (getObjectLimit() != null && progress.getObjectCount() == getObjectLimit()) {
                    break;
                }
            }

            super.endOfObjects();
            for(QueryBatch batch: progress.deleteBatches.values()) {
                batch.executeBatch();
            }

            super.complete();
        } finally {
            super.closeBatches();
            progress.deleteBatches.values().stream().forEach(QueryBatch::closeQuietly);
        }
    }
}
