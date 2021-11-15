/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.schema;

import nl.b3p.brmo.bag2.loader.BAG2GMLMutatieGroepStream;
import nl.b3p.brmo.bag2.loader.BAG2Mutatie;
import nl.b3p.brmo.bag2.loader.BAG2MutatieGroep;
import nl.b3p.brmo.bag2.loader.BAG2ToevoegingMutatie;
import nl.b3p.brmo.bag2.loader.BAG2WijzigingMutatie;
import nl.b3p.brmo.schema.ObjectTableWriter;
import nl.b3p.brmo.schema.ObjectType;
import nl.b3p.brmo.schema.SchemaObjectInstance;
import nl.b3p.brmo.schema.SchemaSQLMapper;
import nl.b3p.brmo.schema.mapping.AttributeColumnMapping;
import nl.b3p.brmo.sql.PreparedStatementQueryBatch;
import nl.b3p.brmo.sql.QueryBatch;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static nl.b3p.brmo.bag2.schema.BAG2Schema.TIJDSTIP_NIETBAGLV;

public class BAG2ObjectTableWriter extends ObjectTableWriter {
    private static final Log log = LogFactory.getLog(BAG2ObjectTableWriter.class);

    private boolean ignoreDuplicates;

    /**
     * Set of seen keys per object type to enable skipping of duplicates
     */
    private Map<BAG2ObjectType, Set<Pair<Object,Object>>> keysPerObjectType = null;

    public class BAG2Progress extends Progress {
        private Map<ObjectType, QueryBatch> deleteBatches = new HashMap<>();

        private long updatedCount = 0;

        private BAG2GMLMutatieGroepStream.BagInfo bagInfo;

        private BAG2ObjectType currentObjectType = null;

        public long getUpdatedCount() {
            return updatedCount;
        }

        public BAG2ObjectTableWriter getWriter() {
            return BAG2ObjectTableWriter.this;
        }

        public BAG2GMLMutatieGroepStream.BagInfo getMutatieInfo() {
            return bagInfo;
        }

        public BAG2ObjectType getCurrentObjectType() {
            return currentObjectType;
        }
    }

    public BAG2ObjectTableWriter(Connection connection, SQLDialect dialect, SchemaSQLMapper schemaSQLMapper) {
        super(connection, dialect, schemaSQLMapper);
        this.setProgress(this.new BAG2Progress());
    }

    public void setIgnoreDuplicates(boolean ignoreDuplicates) {
        this.ignoreDuplicates = ignoreDuplicates;
    }

    public boolean getIgnoreDuplicates() {
        return ignoreDuplicates;
    }

    public BAG2Progress getProgress() {
        return (BAG2Progress) super.getProgress();
    }

    public Map<BAG2ObjectType, Set<Pair<Object,Object>>> getKeysPerObjectType() {
        return keysPerObjectType;
    }

    public void setKeysPerObjectType(Map<BAG2ObjectType, Set<Pair<Object,Object>>> keysPerObjectType) {
        this.keysPerObjectType = keysPerObjectType;
    }

    public void start() throws SQLException {
        BAG2Progress progress = this.new BAG2Progress();
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
        BAG2GMLMutatieGroepStream bag2Objects = new BAG2GMLMutatieGroepStream(counter);
        getProgress().bagInfo = bag2Objects.getBagInfo();
        updateProgress(Stage.LOAD_OBJECTS);

        try {
            for (BAG2MutatieGroep mutatieGroep: bag2Objects) {
                for (BAG2Mutatie mutatie: mutatieGroep.getMutaties()) {
                    if (mutatie instanceof BAG2WijzigingMutatie) {
                        // Don't do an update but a simpler delete and insert of the updated version
                        // Executed on main thread, but worker thread will not be executing new versions of the record we're
                        // deleting

                        // No check for duplicates for wijzigingen: no harm in doing the same wijziging twice and we
                        // can't tell by only the keys if it is exactly the same wijziging but in the maandmutaties for
                        // a different gemeente or the same version changed twice

                        BAG2WijzigingMutatie wijzigingMutatie = (BAG2WijzigingMutatie) mutatie;
                        deletePreviousVersion(wijzigingMutatie.getWas());
                        addObjectToBatch(wijzigingMutatie.getWordt());
                    } else if(mutatie instanceof BAG2ToevoegingMutatie) {
                        BAG2ToevoegingMutatie toevoegingMutatie = (BAG2ToevoegingMutatie) mutatie;

                        if (ignoreDuplicates && isDuplicate(toevoegingMutatie.getToevoeging())) {
                            continue;
                        }

                        prepareDatabaseForObject(toevoegingMutatie.getToevoeging());
                        getProgress().incrementObjectCount();
                        addObjectToBatch(toevoegingMutatie.getToevoeging());
                    }

                    if (getObjectLimit() != null && getProgress().getObjectCount() == getObjectLimit()) {
                        break;
                    }
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

    private boolean isDuplicate(BAG2Object object) {
        if (keysPerObjectType == null) {
            throw new IllegalStateException("keysPerObject type must be set to enable ignoring of duplicates");
        }
        // Primary keys for all BAG2 objects are always same
        Pair<Object,Object> keys = Pair.of(
                object.getAttributes().get("identificatie"),
                object.getAttributes().get("voorkomenidentificatie")
        );
        Set<Pair<Object, Object>> seenKeys = keysPerObjectType.computeIfAbsent(object.getObjectType(), k -> new HashSet<>());
        if (seenKeys.contains(keys)) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("\rIgnoring duplicate %s %s", object.getObjectType().getName(), keys));
            }
            return true;
        }
        seenKeys.add(keys);
        return false;
    }

    public void complete() throws Exception {
        super.endOfObjects();
        for(QueryBatch batch: getProgress().deleteBatches.values()) {
            batch.executeBatch();
        }
        super.complete();
        super.closeBatches();
    }

    @Override
    public void createKeys(ObjectType objectType) throws Exception {
        this.getProgress().currentObjectType = (BAG2ObjectType) objectType;
        super.createKeys(objectType);
    }

    @Override
    public void createIndexes(ObjectType objectType) throws Exception {
        this.getProgress().currentObjectType = (BAG2ObjectType) objectType;
        super.createIndexes(objectType);
    }
}
