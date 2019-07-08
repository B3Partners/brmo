/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.Geometry;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.ParserConfigurationException;
import nl.b3p.brmo.bgt.util.JDBCDataStoreUtil;
import static nl.b3p.brmo.loader.gml.GMLLightFeatureTransformer.ID_NAME;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.xsd.Configuration;
import org.geotools.xsd.Parser;
import org.xml.sax.SAXException;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

/**
 *
 * @author mprins
 */
public class BGTGMLLightLoader {

    private static final Log LOG = LogFactory.getLog(BGTGMLLightLoader.class);

    /**
     * directory met zip files.
     */
    private File scanDirectory;
    /**
     * gegevens voor database verbinding tbv. Geotools.
     */
    private Properties dbConnProps;

    /**
     * {@code true} als we tegen een oracle database werken, default
     * {@code false}
     */
    private boolean isOracle = false;
    /**
     * {@code true} als we tegen een MS SQL database werken, default
     * {@code false}
     */
    private boolean isMSSQL = false;

    /**
     * Bewaar voor elk BGT feature type een map met feature type ids nodig in
     * storeFeatureCollection()
     */
    private final Map<String, Map<String,Pair<Date,Boolean>>> allRecords = new HashMap();

    /**
     * Maak automatisch tabellen aan; normaal niet/default {@code false}.
     */
    private boolean createTables = false;

    private Parser parser;

    /**
     * deze geometrie wordt gebruikt om het gebied van de mutaties in een
     * ziefile bij te houden.
     */
    private Geometry omhullendeVanZipFile = null;

    private StringBuilder opmerkingen = new StringBuilder();

    private STATUS status = STATUS.OK;

    /**
     * Default constructor initaliseert de GML parser.
     */
    public BGTGMLLightLoader() {
        final String schemaLocation = BGTGMLLightLoader.class.getResource("/imgeo-simple_resolved.xsd").toString();

        Configuration configuration = new ApplicationSchemaConfiguration("http://www.geostandaarden.nl/imgeo/2.1/simple/gml31", schemaLocation);
        configuration.getContext().registerComponentInstance(new GeometryFactory(new PrecisionModel(), 28992));

        parser = new Parser(configuration);
        parser.setValidating(true);
        parser.setStrict(false);
        parser.setFailOnValidationError(false);
    }

    /**
     * Maak alle bekende tabellen in de database leeg, voordat alle BGT
     * kaartbladen worden ingeladen. Wordt aangeroepen voordat meerdere keren
     * processZipFile() wordt aangeroepen voor alle nieuwe BGT kaartbladen.
     *
     * @throws java.sql.SQLException als legen van de tabellen niet lukt
     * @throws java.io.IOException als opzetten van database verbinding niet
     * lukt
     */
    public void truncateTables() throws SQLException, IOException {
        final JDBCDataStore dataStore = (JDBCDataStore) DataStoreFinder.getDataStore(dbConnProps);
        if (dataStore == null) {
            throw new IllegalStateException("Datastore mag niet 'null' zijn voor wissen van data.");
        }

        try {
            String name;
            for (BGTGMLLightTransformerFactory t : BGTGMLLightTransformerFactory.values()) {
                name = isOracle ? t.name().toUpperCase() : t.name();
                JDBCDataStoreUtil.truncateTable(dataStore, name, isOracle, LOG);
            }
        } finally {
            dataStore.dispose();
        }
    }

    /**
     *
     * @param scanDirectory te scannen directory met zip files
     * @param dbConnProps getools verbindings gegevens
     *
     * @see #setDbConnProps(java.util.Properties)
     * @see #setScanDirectory(java.io.File)
     *
     */
    public BGTGMLLightLoader(File scanDirectory, Properties dbConnProps) {
        this.scanDirectory = scanDirectory;
        this.dbConnProps = dbConnProps;

        this.isOracle = "oracle".equalsIgnoreCase(dbConnProps.getProperty("dbtype"));
        this.isMSSQL = ("jtds-sqlserver".equalsIgnoreCase(dbConnProps.getProperty("dbtype")) | "sqlserver".equalsIgnoreCase(dbConnProps.getProperty("dbtype")));
    }

    /**
     * Verwerk een set gmllight extract zipfiles.
     *
     * @param zipFiles lijst met zipfiles met gml bestanden
     * @throws FileNotFoundException als zipExtract niet gevonden kan worden
     * @throws IOException als ophalen next zipentry mislukt
     * @see #processZipFile(File)
     */
    public void processZipFiles(List<File> zipFiles) throws FileNotFoundException, IOException {
        for (File zip : zipFiles) {
            LOG.debug("Verwerken zipfile: " + zip.getName());
            processZipFile(zip);
        }
    }

    private int getRecordsCacheSize() {
        int size = 0;
        size = allRecords.values().stream().map((recordMap) -> recordMap.size()).reduce(size, Integer::sum);
        return size;
    }

    /**
     * Verwerk een gmllight extract zipfile.
     *
     * @param zipExtract zipfile met gml bestanden
     * @return het aantal geschreven features voor de zipfile
     *
     * @throws FileNotFoundException als zipExtract niet gevonden kan worden
     * @throws IOException als ophalen next zipentry mislukt of als de database
     * verbinding wegvalt
     */
    public int processZipFile(File zipExtract) throws FileNotFoundException, IOException {
        this.omhullendeVanZipFile = null;
        this.resetStatus();
        LOG.info("Lezen van ZIP bestand " + zipExtract);
        int result = 0, total = 0;
        String eName = "";
        if (this.isValidZipFile(zipExtract)) {
            try (ZipInputStream zip = new ZipInputStream(new FileInputStream(zipExtract))) {
                ZipEntry entry = zip.getNextEntry();
                if (entry == null) {
                    LOG.error("Geen bestanden in zipfile (" + zipExtract + ") gevonden.");
                }
                int beforeRecords = getRecordsCacheSize();
                // for each gml in zip
                while (entry != null) {
                    if (!entry.getName().toLowerCase().endsWith(".gml")) {
                        LOG.warn("Overslaan zip entry geen GML bestand: " + entry.getName());
                    } else {
                        eName = entry.getName();
                        LOG.debug("Lezen GML bestand: " + eName + " uit zip file: " + zipExtract.getCanonicalPath());
                        result = storeFeatureCollection(new CloseShieldInputStream(zip), eName.toLowerCase());
                        if(result != 0 ) {
                            opmerkingen.append(result)
                                    .append(" features geladen uit: ")
                                    .append(eName).append(", zipfile: ")
                                    .append(zipExtract.getCanonicalPath())
                                    .append("\n");
                        }
                        total += result;

                    }
                    entry = zip.getNextEntry();
                }
                int afterRecords = getRecordsCacheSize();
                LOG.info(String.format("Aantal ID's van alle BGT feature types in geheugen: %d (verschil dit bestand: %+d)", afterRecords, afterRecords - beforeRecords));

            } catch (SAXException | ParserConfigurationException ex) {
                LOG.error("Er is een parse fout opgetreden tijdens verwerken van " + eName + " uit " + zipExtract.getCanonicalPath(), ex);
                opmerkingen.append("Er is een parse fout opgetreden tijdens verwerken van ").append(eName)
                        .append(" Foutmelding: ").append(ex).append("\n");
                this.status = STATUS.NOK;
            }
        } else {
            LOG.error("Ongeldige of corrupte zipfile: " + zipExtract.getCanonicalPath());
            opmerkingen.append("Ongeldige of corrupte zipfile: ").append(zipExtract.getCanonicalPath())
                    .append("\nHet bestand kan niet verwerkt worden. Download het bestand opnieuw.");
            this.status = STATUS.NOK;
        }
        return total;
    }

    /**
     * Verwerk een enkel GML bestand. <strong>Let op:</strong> alleen voor
     * "stand"     * verwerking omdat de onderliggende geometrische data niet betrouwbaar kan
     * worden verwijderd in dit geval. Waarschijnlijk wil je ook de status
     * resetten voor deze Loader, gebruik {@link #resetStatus() }.
     *
     * @param gml GML Light bestand
     * @return aantal geschreven features
     *
     * @throws IOException als ophalen gml mislukt
     */
    public int processGMLFile(File gml) throws IOException {
        int result = 0;
        try {
            result = storeFeatureCollection(new FileInputStream(gml), gml.getName().toLowerCase());
        } catch (SAXException | ParserConfigurationException ex) {
            LOG.error("Er is een parse fout opgetreden tijdens verwerken van: " + gml.getCanonicalPath(), ex);
            this.status = STATUS.NOK;
        }
        return result;
    }

    /**
     * transformeert de input en slaat op in database.
     *
     * @param in Stream met te laden features
     * @param gmlFileName naam input gml bestand
     * @return aantal geschreven features
     *
     * @throws SAXException als parsen van input mislukt
     * @throws IOException als er een database fout optreedt
     * @throws ParserConfigurationException als gml parser config niet deugd
     * @throws IllegalStateException als er iets mis is in de configuratie
     */
    private int storeFeatureCollection(InputStream in, String gmlFileName) throws IOException, IllegalStateException, SAXException, ParserConfigurationException {
        int inserts = 0, updates = 0, deletes = 0, features = 0;
        JDBCDataStore dataStore = (JDBCDataStore) DataStoreFinder.getDataStore(dbConnProps);
        if (dataStore == null) {
            throw new IllegalStateException("Datastore mag niet 'null' zijn voor opslaan van data.");
        }
        // TODO boolean mapping voor mssql en oracle
        if (this.isOracle) {
            dataStore.getClassToSqlTypeMappings().put(java.lang.Boolean.class, Types.VARCHAR);
        }
        if (this.isMSSQL) {
            dataStore.getClassToSqlTypeMappings().put(java.lang.Boolean.class, Types.VARCHAR);
        }

        SimpleFeatureCollection gmlFeatCollection = (SimpleFeatureCollection) parser.parse(in);
        
        List<Exception> validationErrors = parser.getValidationErrors();
        if (!validationErrors.isEmpty()) {
            LOG.warn("Er zijn validatie fouten opgetreden tijdens verwerking van bestand: " + gmlFileName);
            for (Exception e : validationErrors) {
                LOG.warn("validatie fout: " + e.getMessage());
            }
        }

        if (gmlFeatCollection.isEmpty()) {
            //opmerkingen.append("Geen features gevonden in bestand: ").append(gmlFileName).append("\n");
            LOG.debug("Geen features gevonden in bestand: " + gmlFileName);
            dataStore.dispose();
            return 0;
        } else {
            LOG.debug("Verwerken features uit GML bestand: " + gmlFileName);
        }

        SimpleFeatureType sft = gmlFeatCollection.getSchema();
        GMLLightFeatureTransformer featTransformer = BGTGMLLightTransformerFactory.getTransformer(sft.getTypeName());
        if (featTransformer == null) {
            LOG.error("Opzoeken van FeatureTransformer voor " + sft.getTypeName() + " is mislukt; er geen transformer beschikbaar voor dit bestand.");
            dataStore.dispose();
            return 0;
        }

        // check table exists
        String tableName = BGTGMLLightTransformerFactory.getTableName(sft.getTypeName());
        boolean exists = false;
        String[] typeNames = dataStore.getTypeNames();
        for (String name : typeNames) {
            if (tableName.equalsIgnoreCase(name)) {
                LOG.debug("De tabel '" + tableName + "' is gevonden in de database als: " + name);
                exists = true;
            }
        }

        SimpleFeatureType targetSchema = featTransformer.getTargetSchema(sft, tableName, this.isOracle);
        LOG.debug("Doel tabel schema: " + targetSchema);

        if (!exists) {
            if (createTables) {
                dataStore.createSchema(targetSchema);
                LOG.warn("De volgende tabel is aangemaakt in de database: " + targetSchema.getTypeName());
                if (this.isOracle) {
                    try {
                        dataStore.getConnection(Transaction.AUTO_COMMIT).createStatement().execute("UPDATE USER_SDO_GEOM_METADATA SET SRID=28992");
                    } catch (SQLException ex) {
                        LOG.error("Bijwerken ruimtelijke metadata is mislukt", ex);
                    }
                }
            } else {
                LOG.error("Tabel: " + tableName + " is niet beschikbaar in de database.");
                throw new IllegalStateException("De tabel " + tableName + " ontbreekt in de database; opslaan van gegevens uit GML " + gmlFileName + " is niet mogelijk.");
            }
        } else {
            // als tabel bestaat aannemen dat deze de juiste primary key heeft
            dataStore.setExposePrimaryKeyColumns(true);
        }

        // Map om per ID bij te houden wat meest recente tijdstipRegistratie en beeindigd status is
        // Niet in map: niet in tabel
        // TRUE: beeindigd en niet in tabel
        // FALSE: niet beeindigd, en in tabel
        Map<String,Pair<Date,Boolean>> records = allRecords.get(tableName);
        if(records == null) {
            records = new HashMap();
            allRecords.put(tableName, records);
        } else {
            LOG.debug("Aantal ID's voor deze tabel al bekend in geheugen van vorige GML bestanden: " + records.size());
        }

        Transaction transaction = new DefaultTransaction("add-bgt");
        Transaction updatetransaction = new DefaultTransaction("update-bgt");
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        try (FeatureIterator<SimpleFeature> feats = gmlFeatCollection.features()) {
            FeatureStore store = (FeatureStore) dataStore.getFeatureSource(targetSchema.getTypeName(), transaction);
            FeatureStore store2 = (FeatureStore) dataStore.getFeatureSource(targetSchema.getTypeName(), updatetransaction);

            SimpleFeature gmlSF;
            SimpleFeature transformed;
            while (feats.hasNext()) {
                gmlSF = feats.next();
                features++;

                transformed = featTransformer.transform(gmlSF, targetSchema, this.isOracle, dataStore.isExposePrimaryKeyColumns());
                if (transformed == null) {
                    LOG.warn("Fout bij transformeren feature op index " + features + ", null geretourneerd");
                    continue;
                }

                String id = transformed.getID();
                Pair<Date,Boolean> record = records.get(id);
                Date tijdstipRegistratie = (Date) gmlSF.getAttribute("tijdstipRegistratie");
                if(tijdstipRegistratie == null) {
                    LOG.error("Feature voor object " + id + " zonder tijdstipRegistratie gevonden, genegeerd");
                    continue;
                }
                boolean beeindigd = gmlSF.getAttribute("objectEindTijd") != null;
                if(LOG.isDebugEnabled()) {
                    LOG.info(String.format("Object %s, beeindigd=%s, tijdstipRegistratie=%tc, eerder tijdstip %tc, eerder beeindigd %s", id, beeindigd ? "ja":"nee", tijdstipRegistratie, record != null ? record.getLeft() : null, record != null ? (record.getRight() ? "ja":"nee") : "-"));
                }
                if(record != null && tijdstipRegistratie.before(record.getLeft())) {
                    LOG.warn(String.format("Feature voor object %s gevonden met tijdstipRegistratie %tc eerder dan vorig record van %tc, genegeerd", id, tijdstipRegistratie, record.getLeft()));
                    continue;
                }

                if(beeindigd) {
                    Date eindTijd = (Date) gmlSF.getAttribute("objectEindTijd");
                    if(eindTijd.after(new Date())) {
                        LOG.warn(String.format("Object %s vervallen in de toekomst op %tc, wordt alvast verwijderd!", id, eindTijd));
                    }

                    if(record != null) {
                        if(!record.getRight()) {
                            // Verwijder eerder geinsert record
                            LOG.info(String.format("Object %s vervallen met tijdstipRegistratie %tc, verwijder eerder geinsert record met laatste tijdstipRegistratie %tc", id, tijdstipRegistratie, record.getLeft()));

                            // bij mssql transactie sluiten ander blijft de boel hangen, voor orcl + pg ook
                            transaction.close();
                            Filter filter = ff.id(transformed.getIdentifier());
                            store2.removeFeatures(filter);
                            updatetransaction.commit();
                            // maak een nieuw transactie voor toevoegen, de eerdere is aborted
                            transaction = new DefaultTransaction("add-bgt");
                            store.setTransaction(transaction);
                            deletes++;
                        }
                    }
                    records.put(id, new ImmutablePair<>(tijdstipRegistratie, true));
                } else {

                    if(record != null) {
                        if(record.getRight()) {
                            LOG.error(String.format("Object %s was eerder vervallen met tijdstipRegistratie %tc, nu weer niet met tijdstipRegistratie %tc, genegeerd", id, record.getLeft(), tijdstipRegistratie));
                            // Alternatief weer inserten?
                            continue;
                        } else {
                            // bij mssql transactie sluiten ander blijft de boel hangen, voor orcl + pg ook
                            transaction.close();
                            // object opzoeken,
                            Filter filter = CommonFactoryFinder.getFilterFactory2(null).id(transformed.getIdentifier());
                            FeatureIterator<SimpleFeature> bestaandeFeats = store2.getFeatures(filter).features();
                            SimpleFeature bestaandeFeat = bestaandeFeats.next();

                            final String idAttr = this.isOracle ? ID_NAME.toUpperCase() : ID_NAME;
                            for (AttributeDescriptor attr : transformed.getFeatureType().getAttributeDescriptors()) {
                                Name attrName = attr.getName();
                                if (!attrName.getLocalPart().equals(idAttr)) {
                                    store2.modifyFeatures(attrName, transformed.getAttribute(attrName), filter);
                                }
                            }
                            updatetransaction.commit();
                            LOG.info(String.format("Object %s geupdate van tijdstip %tc naar nieuw tijdstipRegistratie %tc", id, record.getLeft(), tijdstipRegistratie));
                            bestaandeFeats.close();
                            // maak een nieuw transactie voor toevoegen, de eerdere is aborted
                            transaction = new DefaultTransaction("add-bgt");
                            store.setTransaction(transaction);
                            updates++;
                        }
                    } else {
                        store.addFeatures(DataUtilities.collection(transformed));
                        inserts++;
                        // commit per feature
                        transaction.commit();
                        LOG.debug(String.format("Object toegevoegd in database met NEN3610ID: %s en tijdstipRegistratie %tc", id, tijdstipRegistratie));
                    }
                    records.put(id, new ImmutablePair<>(tijdstipRegistratie, false));
                }
            }
            LOG.info(String.format("Totaal verwerkte features voor %s: %d, inserts: %d, updates: %d, deletes: %d", gmlFileName, features, inserts, updates, deletes));
        } catch (IOException ioe) {
            String s = String.format("Fout opgetreden, hiervoor verwerkte features voor %s: %d, inserts: %d, updates: %d, deletes: %d", gmlFileName, features, inserts, updates, deletes);
            opmerkingen.append(s).append("\n");
            LOG.info(s);

            LOG.error("I/O database probleem tijdens insert van features", ioe);
            this.status = STATUS.NOK;
            transaction.rollback();
        } finally {
            try {
                transaction.close();
                updatetransaction.close();
                dataStore.dispose();
            } catch(Exception e) {
                LOG.error("Fout sluiten datastores", e);
            }
        }
        return inserts;
    }

    private boolean isDuplicateKeyViolationMessage(String message) {
        return message != null
                /* oracle */
                && (message.startsWith("ORA-00001:")
                /* postgresql */
                | message.startsWith("ERROR: duplicate key value violates unique constraint")
                /* mssql */
                | message.contains("Cannot insert duplicate key in object"));
    }

    /**
     * scan directory for zipfiles in de geconfigueerde directory.
     *
     * @return lijst met zipfiles, mogelijk leeg
     * @see #setScanDirectory(java.io.File)
     * @see #setScanDirectory(java.lang.String)
     */
    public List<File> scanDirectory() {
        List<File> zipfiles = Collections.emptyList();

        if (scanDirectory != null && scanDirectory.isDirectory() && scanDirectory.canExecute()) {
            FilenameFilter filter = new FilenameFilter() {
                /**
                 * accepteer alleen zip files.
                 *
                 * @param dir the directory in which the file was found.
                 * @param name the name of the file
                 * @return {@code true} if and only if the name should be
                 * included in the file list; {@code false} otherwise.
                 */
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith("zip");
                }
            };
            File files[] = scanDirectory.listFiles(filter);
            zipfiles = Arrays.asList(files);
        } else {
            LOG.fatal("De directory (" + scanDirectory + ") kan niet worden gelezen of doorbladerd.");
            this.status = STATUS.NOK;
        }
        return zipfiles;
    }

    /**
     * Stel directory met zip files in.
     *
     * @param scanDirectory pad van te scannen directory
     *
     * @see #processGMLFile(java.io.File)
     */
    public void setScanDirectory(String scanDirectory) {
        this.scanDirectory = new File(scanDirectory);
    }

    /**
     * Stel directory met zip files in.
     *
     * @param scanDirectory te scannen directory
     *
     * @see #setScanDirectory(java.lang.String)
     */
    public void setScanDirectory(File scanDirectory) {
        this.scanDirectory = scanDirectory;
    }

    /**
     * gegevens voor maken van verbinding met daatabase door geotools.
     * Afhankelijk van de soort store dienen verschillende params te worden
     * gegeven, zie:
     * <a href="http://docs.geotools.org/latest/userguide/library/jdbc/index.html">JDBC</a>.
     *
     * @param dbConnProps getools verbindings gegevens
     */
    public void setDbConnProps(Properties dbConnProps) {
        this.dbConnProps = dbConnProps;
        this.isOracle = "oracle".equalsIgnoreCase(dbConnProps.getProperty("dbtype"));
        this.isMSSQL = ("jtds-sqlserver".equalsIgnoreCase(dbConnProps.getProperty("dbtype")) | "sqlserver".equalsIgnoreCase(dbConnProps.getProperty("dbtype")));
    }

    /**
     * Maak automatisch tabellen aan.
     *
     * @param createTables {@code true} om automatisch tabellen aan te maken,
     * default is {@code true}
     */
    public void setCreateTables(boolean createTables) {
        this.createTables = createTables;
    }

    /**
     * omdat oracle uppercase heeft voor tabellen en velden en geen boolean
     * heeft...
     *
     * @param isOracle {@code true} als het zo is
     */
    public void setIsOracle(boolean isOracle) {
        this.isOracle = isOracle;
    }

    /**
     * omdat sqlserver geen boolean heeft voor velden...
     *
     * @param isMSSQL {@code true} als het zo is
     */
    public void setIsMSSQL(boolean isMSSQL) {
        this.isMSSQL = isMSSQL;
    }

    /**
     * Test of de zipFile een geldige zipfile is.
     *
     * @param file de te testen zipfile
     * @return {@code true} als de zipfile OK is, aders {@code false}
     */
    private boolean isValidZipFile(final File file) {
        ZipFile zipfile = null;
        ZipInputStream zis = null;
        String path = null;
        try {
            path = file.getCanonicalPath();
            zipfile = new ZipFile(file, ZipFile.OPEN_READ);
            zis = new ZipInputStream(new FileInputStream(file));
            ZipEntry ze = zis.getNextEntry();
            if (ze == null) {
                return false;
            }
            while (ze != null) {
                // als er een exception bij 1 van volgende optreed is het bestand corrupt
                zipfile.getInputStream(ze);
                ze.getCrc();
                ze.getCompressedSize();
                ze.getName();
                ze = zis.getNextEntry();
            }
            return true;
        } catch (IOException e) {
            LOG.error("Ongeldige zipfile: " + path, e);
            return false;
        } finally {
            try {
                if (zipfile != null) {
                    zipfile.close();
                    zipfile = null;
                }
            } catch (IOException e) {
                LOG.warn("Fout tijdens sluiten zipfile, mogelijk corrupt bestand: " + path, e);
                // return false;
            }
            try {
                if (zis != null) {
                    zis.close();
                    zis = null;
                }
            } catch (IOException e) {
                LOG.warn("Fout tijdens sluiten zipstream, mogelijk corrupt bestand: " + path, e);
                // return false;
            }
        }
    }
    /**
     * Zipfile omhullende geometrie.
     *
     * @return omhullende van alle vlakgeometrieen van de bestanden in een
     * zipfile. In het gavel van een stand {@code null}.
     */
    public Geometry getOmhullendeVanZipFile() {
        return omhullendeVanZipFile;
    }

    /**
     * geeft een log van de verwerking.
     *
     * @return log van de verwerking
     */
    public String getOpmerkingen() {
        return this.opmerkingen.toString();
    }

    /**
     * geeft de verwerkingsstatus. Voorafgaand aan de verwerking moet de status
     * reset worden als er een set losse GML bestanden wordt verwerkt (bij zip
     * files gaat dat vanzelf).
     *
     * @return status van de verwerking
     */
    public STATUS getStatus() {
        return this.status;
    }

    /**
     * verwerkingsstatus
     */
    public enum STATUS {
        OK, NOK
    }

    public void resetStatus() {
        this.status = STATUS.OK;
        opmerkingen = new StringBuilder();
    }
}
