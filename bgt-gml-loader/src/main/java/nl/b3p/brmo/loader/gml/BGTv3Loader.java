/*
 * Copyright (C) 2018 B3Partners B.V.
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
package nl.b3p.brmo.loader.gml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.datatype.DatatypeFactory;
import nl.b3p.brmo.bgt.util.JDBCDataStoreUtil;
import nl.b3p.brmo.loader.gml.BGTGMLLightLoader.STATUS;
import static nl.b3p.brmo.loader.gml.GMLLightFeatureTransformer.DEFAULT_GEOM_NAME;
import nl.b3p.brmo.loader.gml.bgt.BGTv3Mapping;
import nl.b3p.brmo.loader.gml.bgt.BGTv3Mapping.Attribute;
import nl.b3p.brmo.loader.gml.bgt.BGTv3Mappings;
import nl.b3p.brmo.loader.gml.bgt.BGTv3Object;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.util.factory.Hints;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

/**
 *
 * @author matthijsln
 */
public class BGTv3Loader {

    private static final Log log = LogFactory.getLog(BGTv3Loader.class);

    /**
     * gegevens voor database verbinding tbv. Geotools.
     */
    private final Properties dbConnProps;

    private final StringBuilder opmerkingen = new StringBuilder();

    private STATUS status = STATUS.OK;

    private final boolean isOracle;
    private final boolean isMSSQL;

    public StringBuilder getOpmerkingen() {
        return opmerkingen;
    }

    public BGTv3Loader(Properties dbConnProps) {
        this.dbConnProps = dbConnProps;

        this.isOracle = "oracle".equalsIgnoreCase(dbConnProps.getProperty("dbtype"));
        this.isMSSQL = ("jtds-sqlserver".equalsIgnoreCase(dbConnProps.getProperty("dbtype")) | "sqlserver".equalsIgnoreCase(dbConnProps.getProperty("dbtype")));
    }

    public boolean processZipFile(File f) throws Exception {
        log.info(String.format("Processing file %s (%s)", f.getAbsolutePath(), FileUtils.byteCountToDisplaySize(f.length())));
        boolean delta = f.getName().contains("_delta_");
        int result = 0, total = 0;
        String eName = "";
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(f))) {
            ZipEntry entry = zip.getNextEntry();
            if(entry == null) {
                log.warn("Geen bestanden in zipfile");
                return false;
            }
            eName = entry.getName();
            do {
                String ext = FilenameUtils.getExtension(eName);
                if(!ext.equals("gml") && !ext.equals("xml")) {
                    log.warn("Overslaan zip entry geen GML/XML bestand: " + eName);
                } else {
                    log.info("Lezen bestand: " + eName);

                    result = transformFeatures(new CloseShieldInputStream(zip), eName.toLowerCase(), delta);
                    opmerkingen.append(String.format("%d features geladen uit: %s, zipfile: %s\n", result, eName, f.getCanonicalPath()));
                    total += result;
                }
                entry = zip.getNextEntry();
            } while(entry != null);
        } catch(Exception e) {
            String msg = String.format("Er is een fout opgetreden tijdens verwerken van %s uit %s: %s: %s", eName, f.getCanonicalPath(), e.getClass(), e.getMessage());
            log.error(msg, e);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            opmerkingen.append(msg).append("\n").append(sw.toString()).append("\n");
            this.status = BGTGMLLightLoader.STATUS.NOK;
        }

        return true;
    }

    private int transformFeatures(InputStream xml, String filename, boolean delta) throws Exception {
        // Statistics
        int inserts = 0, skipped = 0, updates = 0, deletes = 0, features = 0;

        BGTv3Mappings mappings = new BGTv3Mappings();

        BGTv3XMLReader reader = null;
        try {
            reader = new BGTv3XMLReader(xml);
        } catch(Exception e) {
            throw new Exception("Fout bij het lezen van BGTv3 XML", e);
        }

        OutputContext context = new OutputContext(delta);
        BGTv3Object lastObject = null;
        BGTv3Mapping lastMapping = null;
        try {
            Map<String,MutableInt> skippedObjectTypes = new HashMap();

            // Map om per ID bij te houden wat meest recente tijdstipRegistratie en beeindigd status is
            // Niet in map: niet in tabel (bij delta: mogelijk wel in tabel)
            // TRUE: beeindigd en niet in tabel (ook bij delta dan verwijderd)
            // FALSE: niet beeindigd, en in tabel
            Map<String,Pair<Date,Boolean>> records = new HashMap();

            for(BGTv3Object object: reader) {
                lastObject = object;
                lastMapping = null;
                features++;
                if(features % 100 == 0) {
                    log.info("Aantal BGTv3 objecten gelezen: " + features);
                }

                String bgtObjectType = object.getObjectType();

                if(skippedObjectTypes.containsKey(bgtObjectType)) {
                    skippedObjectTypes.get(bgtObjectType).increment();
                    skipped++;
                    continue;
                }
                if(log.isDebugEnabled()) {
                    log.debug("BGTv3 object: " + object);
                }
                BGTv3Mapping mapping = mappings.getMapping(bgtObjectType);
                if(mapping == null) {
                    log.warn("Geen mapping voor BGTv3 objecttype " + bgtObjectType + ", deze objecten worden genegeerd");
                    skippedObjectTypes.put(bgtObjectType, new MutableInt(1));
                    skipped++;
                    continue;
                }
                lastMapping = mapping;
                if(log.isDebugEnabled()) {
                    log.debug("Mapping datamodel: " + mapping);
                }

                if(!context.initializeOutput(bgtObjectType, mapping)) {
                    log.warn("Geen tabel " + mapping.getTable() + " gevonden voor BGTv3 objecttype " + bgtObjectType + ", deze objecten worden genegeerd");
                    skippedObjectTypes.put(bgtObjectType, new MutableInt(1));
                    skipped++;
                    continue;
                }

                SimpleFeature sf = transformObject(object, mapping, context);
                String id = sf.getID();

                Pair<Date,Boolean> record = records.get(id);
                Date tijdstipRegistratie = null;
                try {
                    tijdstipRegistratie = DatatypeFactory.newInstance().newXMLGregorianCalendar(object.getAttributes().get("tijdstipRegistratie")).toGregorianCalendar().getTime();
                } catch(Exception e) {
                    log.error("BGTv3 object " + id + " zonder tijdstipRegistratie gevonden, genegeerd");
                    continue;
                }
                boolean beeindigd = object.getAttributes().get(BGTv3Object.ATTRIBUTE_TERMINATION_DATE) != null;
                if(log.isDebugEnabled()) {
                    log.info(String.format("Object %s, beeindigd=%s, tijdstipRegistratie=%tc, eerder tijdstip %tc, eerder beeindigd %s", id, beeindigd ? "ja":"nee", tijdstipRegistratie, record != null ? record.getLeft() : null, record != null ? (record.getRight() ? "ja":"nee") : "-"));
                }
                if(record != null && tijdstipRegistratie.before(record.getLeft())) {
                    log.warn(String.format("Object %s gevonden met tijdstipRegistratie %tc eerder dan vorig record van %tc, genegeerd", id, tijdstipRegistratie, record.getLeft()));
                    continue;
                }

                if(beeindigd) {
                    Date eindTijd = new SimpleDateFormat("yyyy-MM-dd").parse(object.getAttributes().get(BGTv3Object.ATTRIBUTE_TERMINATION_DATE));
                    if(eindTijd.after(new Date())) {
                        log.warn(String.format("Object %s vervallen in de toekomst op %tc, wordt alvast verwijderd!", id, eindTijd));
                    }

                    if(record != null) {
                        if(!record.getRight()) {
                            // Verwijder eerder geinsert record
                            log.info(String.format("Object %s vervallen met tijdstipRegistratie %tc, verwijder eerder geinsert record met laatste tijdstipRegistratie %tc", id, tijdstipRegistratie, record.getLeft()));

                            context.remove(id);
                            deletes++;
                        } else {
                            log.warn(String.format("Object %s vervallen met tijdstipRegistratie %tc, was al eerder verwijderd met laatste tijdstipRegistratie %tc", id, tijdstipRegistratie, record.getLeft()));
                        }
                    } else if(delta) {
                        context.remove(id);
                        deletes++;
                    }
                    records.put(id, new ImmutablePair<>(tijdstipRegistratie, true));
                } else {
                    if(record != null) {
                        if(record.getRight()) {
                            log.error(String.format("Object %s was eerder vervallen met tijdstipRegistratie %tc, nu weer niet met tijdstipRegistratie %tc, genegeerd", id, record.getLeft(), tijdstipRegistratie));
                            // Alternatief weer inserten?
                            continue;
                        } else {
                            context.update(sf);
                            log.info(String.format("Object %s geupdate van tijdstip %tc naar nieuw tijdstipRegistratie %tc", id, record.getLeft(), tijdstipRegistratie));
                            updates++;
                        }
                    } else {
                        boolean inserted = context.insertOrUpdate(sf);
                        if(inserted) {
                            inserts++;
                        } else {
                            updates++;
                        }
                        log.debug(String.format("Object %s in database met NEN3610ID: %s en tijdstipRegistratie %tc", inserted ? "toegevoegd" : "geupdate", id, tijdstipRegistratie));
                    }
                    records.put(id, new ImmutablePair<>(tijdstipRegistratie, false));
                }
            }
            String msg;
            if(features == 0) {
                msg = "Geen BGT objecten gevonden in bestand: " + filename;
            } else {
                msg = String.format("Totaal verwerkte BGT objecten voor %s: %d, inserts: %d, updates: %d, deletes: %d\n", filename, features, inserts, updates, deletes);
            }
            if(skipped > 0) {
                msg += "Totaal aantal overgeslagen ongemapte objecten: " + skipped + "\n";
                for(String objectType: skippedObjectTypes.keySet()) {
                    msg += String.format("    %5d %s\n", skippedObjectTypes.get(objectType).intValue(), objectType);
                }
            }
            opmerkingen.append(msg).append("\n");
            log.info(msg);
        } catch (IOException ioe) {
            String s = String.format("Fout opgetreden, hiervoor verwerkte BGT objecten voor %s: %d, inserts: %d, updates: %d, deletes: %d\n", filename, features, inserts, updates, deletes);
            if(lastObject != null) {
                s += "Laast verwerkte BGT object: " + lastObject + "\n";
            }
            if(lastMapping != null) {
                s += "Mapping naar database voor laatste verwerkte BGT object: " + lastMapping + "\n";
            }
            opmerkingen.append(s).append("\n");
            log.info(s);

            log.error("I/O database probleem tijdens insert van BGT objecten", ioe);
            this.status = STATUS.NOK;
            context.transaction.rollback();
        } finally {
            context.close();
        }
        return features;
    }

    private SimpleFeature transformObject(BGTv3Object object, BGTv3Mapping mapping, OutputContext context) throws IOException {
        for(PropertyDescriptor property: context.builder.getFeatureType().getDescriptors()) {
            log.trace("Property: " + property);
            String propertyName = property.getName().getLocalPart();

            // Common / special mappings
            if(propertyName.equalsIgnoreCase(DEFAULT_GEOM_NAME)) {
                String geomProperty = mapping.getAttributes().get("DEFAULT_GEOM_NAME").getXml3Name();
                if(geomProperty != null) {
                    log.trace("Set geom2d to geometry property " + geomProperty);
                    context.builder.set(property.getName(), object.getGeometries().get(geomProperty));
                }
            } else {
                // Find mapping property
                Attribute mappedAttribute = null;
                for(String mappedColumn: mapping.getAttributes().keySet()) {
                    if(propertyName.equalsIgnoreCase(mappedColumn)) {
                        mappedAttribute = mapping.getAttributes().get(mappedColumn);
                        log.trace("Found mapped column: " + mappedAttribute);
                    }
                }

                if(mappedAttribute != null) {
                    if(object.getGeometries().containsKey(mappedAttribute.getXml3Name())) {
                        log.trace("Set " + propertyName + "(" +  property.getType().toString() + ") to geometry " + object.getGeometries().get(mappedAttribute.getXml3Name()));
                        context.builder.set(property.getName(), object.getGeometries().get(mappedAttribute.getXml3Name()));
                    } else {
                        // XXX Mapping String -> Type
                        log.trace("Set " + propertyName + "(" +  property.getType().toString() + ") to " + object.getAttributes().get(mappedAttribute.getXml3Name()));
                        context.builder.set(property.getName(), object.getAttributes().get(mappedAttribute.getXml3Name()));
                    }
                } else {
                    log.trace("Column " + property.getName() + " not mapped, leaving empty");
                }
            }
        }
        String id = object.getAttributes().get("namespace") + "." + object.getObjectId();
        context.builder.featureUserData(Hints.USE_PROVIDED_FID, Boolean.TRUE);
        SimpleFeature sf = context.builder.buildFeature(id);
        if(log.isDebugEnabled()) {
            log.debug("Built feature with ID " + id + ": " + sf.toString());
        } else {
            log.debug("Built feature with ID " + id);
        }
        return sf;
    }

    private class OutputContext {
        private boolean delta;

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

        /** Destination */
        private JDBCDataStore dataStore = null;

        /** Transaction for adding features */
        private Transaction transaction = null;

        /** Transaction for updating/deleting features */
        private Transaction updateTransaction = null;

        /** FeatureStore for adding features per BGTv3 object type */
        Map<String,FeatureStore> addStores = new HashMap();

        /** FeatureStore for updating/deleting features per BGTv3 object type */
        Map<String,FeatureStore> updateStores = new HashMap();

        /** SimpleFeatureBuilders per BGTv3 object type */
        Map<String,SimpleFeatureBuilder> builders = new HashMap();

        /** For current input object, reset after initializeOutput */
        FeatureStore store, updateStore;
        SimpleFeatureBuilder builder;

        public OutputContext(boolean delta) throws IOException {
            this.delta = delta;
            this.dataStore = (JDBCDataStore) DataStoreFinder.getDataStore(dbConnProps);
            if(this.dataStore == null) {
                throw new IllegalStateException("Datastore mag niet 'null' zijn voor opslaan van data");
            }
/* XXX needed?
            if (this.isOracle || this.isMSSQL) {
                dataStore.getClassToSqlTypeMappings().put(java.lang.Boolean.class, Types.VARCHAR);
            }
*/

            this.transaction = new DefaultTransaction("add-bgt");
            this.updateTransaction = new DefaultTransaction("update-bgt");
        }

        public boolean initializeOutput(String bgtObjectType, BGTv3Mapping mapping) throws IOException {
            store = this.addStores.get(bgtObjectType);
            updateStore = this.updateStores.get(bgtObjectType);
            builder = this.builders.get(bgtObjectType);

            String typeName = mapping.getTable();
            if(isOracle) {
                typeName = typeName.toUpperCase();
            }

            if(store == null) {
                store = (FeatureStore)dataStore.getFeatureSource(typeName, this.transaction);

                if(store == null) {
                    return false;
                }

                if(!delta) {
                    // huidige records verwijderen
                    JDBCDataStoreUtil.truncateTable(dataStore, typeName, isOracle, log);
                }

                addStores.put(bgtObjectType, store);
                updateStore = (FeatureStore)dataStore.getFeatureSource(typeName, updateTransaction);
                updateStores.put(bgtObjectType, updateStore);
                builder = new SimpleFeatureBuilder((SimpleFeatureType)store.getSchema());
                builders.put(bgtObjectType, builder);
            }
            return true;
        }

        public boolean insertOrUpdate(SimpleFeature sf) throws IOException {
            try {
                store.addFeatures(DataUtilities.collection(sf));

                // commit per feature
                transaction.commit();
                return true;
            } catch(Exception e) {
                log.debug("Caught exception inserting, trying update", e);
                update(sf);
                return false;
            }
        }

        public void remove(String id) throws IOException {
            // bij mssql transactie sluiten ander blijft de boel hangen, voor orcl + pg ook
            transaction.close();
            Filter filter = ff.id(ff.featureId(id));
            updateStore.removeFeatures(filter);
            updateTransaction.commit();
            // maak een nieuw transactie voor toevoegen, de eerdere is aborted
            transaction = new DefaultTransaction("add-bgt");
            store.setTransaction(transaction);
        }

        public void update(SimpleFeature sf) throws IOException {
            // bij mssql transactie sluiten ander blijft de boel hangen, voor orcl + pg ook
            transaction.close();
            // object opzoeken,
            Filter filter = CommonFactoryFinder.getFilterFactory2(null).id(sf.getIdentifier());
            FeatureIterator<SimpleFeature> bestaandeFeats = updateStore.getFeatures(filter).features();
            SimpleFeature bestaandeFeat = bestaandeFeats.next();

            // No primary key exposed, no need to skip
            List<AttributeDescriptor> attributes = sf.getFeatureType().getAttributeDescriptors();
            Name[] names = new Name[attributes.size()];
            Object[] values = new Object[attributes.size()];
            for(int i = 0; i < attributes.size(); i++) {
                AttributeDescriptor attribute = attributes.get(i);
                names[i] = attribute.getName();
                values[i] = sf.getAttribute(names[i]);
            }
            updateStore.modifyFeatures(names, values, filter);
            updateTransaction.commit();
            bestaandeFeats.close();
            // maak een nieuw transactie voor toevoegen, de eerdere is aborted
            transaction = new DefaultTransaction("add-bgt");
            store.setTransaction(transaction);
        }

        public void close() {
            try {
                if(transaction != null) {
                    transaction.close();
                }
                if(updateTransaction != null) {
                    updateTransaction.close();
                }
                if(dataStore != null) {
                    dataStore.dispose();
                }
            } catch(Exception e) {
                log.error("Error closing datastore", e);
            }

        }
    }
}
