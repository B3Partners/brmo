/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import com.vividsolutions.jts.geom.Dimension;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.TopologyException;
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
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.ParserConfigurationException;
import static nl.b3p.brmo.loader.gml.GMLLightFeatureTransformer.BEGINTIJD_NAME;
import static nl.b3p.brmo.loader.gml.GMLLightFeatureTransformer.BIJWERKDATUM_NAME;
import static nl.b3p.brmo.loader.gml.GMLLightFeatureTransformer.DEFAULT_GEOM_NAME;
import static nl.b3p.brmo.loader.gml.GMLLightFeatureTransformer.ID_NAME;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.xml.sax.SAXException;
import org.apache.commons.io.input.CloseShieldInputStream;
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
     * Maak automatisch tabellen aan; normaal niet/default {@code false}.
     */
    private boolean createTables = false;

    private Parser parser;

    /**
     * deze geometrie wordt gebruikt om het gebied van de mutaties in een
     * ziefile bij te houden.
     */
    private Geometry omhullendeVanZipFile = null;

    /**
     * {@code true} als er een update wordt geladen, {@code false} voor een
     * stand, default is: {@code true}
     */
    private boolean loadingUpdate = true;

    /**
     * metadata, datum van laden.
     */
    private Date bijwerkDatum = null;

    private final StringBuilder opmerkingen = new StringBuilder();

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
        if (this.bijwerkDatum == null) {
            this.bijwerkDatum = new Date();
        }

        int result = 0, total = 0;
        String eName = "";
        if (this.isValidZipFile(zipExtract)) {
            try (ZipInputStream zip = new ZipInputStream(new FileInputStream(zipExtract))) {
                ZipEntry entry = zip.getNextEntry();
                if (entry == null) {
                    LOG.error("Geen bestanden in zipfile (" + zipExtract + ") gevonden.");
                }
                // for each gml in zip
                while (entry != null) {
                    if (!entry.getName().toLowerCase().endsWith(".gml")) {
                        LOG.warn("Overslaan zip entry geen GML bestand: " + entry.getName());
                    } else {
                        eName = entry.getName();
                        LOG.info("Lezen GML bestand: " + eName + " uit zip file: " + zipExtract.getCanonicalPath());
                        result = storeFeatureCollection(new CloseShieldInputStream(zip), eName.toLowerCase());
                        opmerkingen.append(result)
                                .append(" features geladen uit: ")
                                .append(eName).append(", zipfile: ")
                                .append(zipExtract.getCanonicalPath())
                                .append("\n");
                        total += result;
                    }
                    entry = zip.getNextEntry();
                }

                if (this.loadingUpdate) {
                    // nadat zipfile is geladen
                    //                LOG.debug("omhullende van zipfile: " + omhullendeVanZipFile);
                    //                LOG.debug("convex omhullende van zipfile(" + zipExtract + "): " + omhullendeVanZipFile.convexHull());
                    //                LOG.debug("union omhullende van zipfile(" + zipExtract + "): " + omhullendeVanZipFile.union());

                    // org.opensphere.geometry.algorithm.ConcaveHull, zie ook: pom.xml
                    //ConcaveHull ch = new ConcaveHull(omhullendeVanZipFile, 1d);
                    //LOG.debug("concave omhullende van zipfile(" + zipExtract + "): " + ch.getConcaveHull());
                    // verwijderen van de onderliggend aan omhullendeVanZipFile, verouderde objecten in iedere tabel
                    // self union lijkt vooranlog het beste masker te geven voor verwijderen
                    deleteOldData(this.bijwerkDatum, omhullendeVanZipFile.union());
                }
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

    private void deleteOldData(Date deleteBeforeDatum, Geometry deleteOverlaps) throws IOException {
        JDBCDataStore dataStore = (JDBCDataStore) DataStoreFinder.getDataStore(dbConnProps);
        if (dataStore == null) {
            throw new IllegalStateException("Datastore mag niet 'null' zijn voor verwijderen van van data.");
        }

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
        Filter filter = ff.and(
                ff.before(ff.property(
                        (this.isOracle ? BIJWERKDATUM_NAME.toUpperCase() : BIJWERKDATUM_NAME)
                ), ff.literal(deleteBeforeDatum)),
                ff.overlaps(ff.property(
                        (this.isOracle ? DEFAULT_GEOM_NAME.toUpperCase() : DEFAULT_GEOM_NAME)
                ), ff.literal(deleteOverlaps))
        );
        LOG.info("Opruimen van verouderde data uit tabellen met filters: " + filter);

        FeatureStore store;
        try (Transaction deletetransaction = new DefaultTransaction("delete-bgt")) {
            for (BGTGMLLightTransformerFactory t : BGTGMLLightTransformerFactory.values()) {
                if (t.getGmlFileName().isEmpty()) {
                    continue;
                }
                String tableName = this.isOracle ? t.name().toUpperCase() : t.name();
                opmerkingen.append("Opruimen van verouderde data uit tabel: ").append(tableName).append("\n");
                LOG.info("Opruimen van verouderde data uit tabel: " + tableName);
                store = (FeatureStore) dataStore.getFeatureSource(tableName, deletetransaction);
                store.removeFeatures(filter);
                deletetransaction.commit();
            }
        } finally {
            dataStore.dispose();
        }
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
     * @param gmlFeatCollection te laden features
     * @param gmlFileName naam input gml bestand
     * @return aantal geschreven features
     *
     * @throws SAXException als parsen van input mislukt
     * @throws IOException als er een database fout optreedt
     * @throws ParserConfigurationException als gml parser config niet deugd
     * @throws IllegalStateException als er iets mis is in de configuratie
     */
    private int storeFeatureCollection(InputStream in, String gmlFileName) throws IOException, IllegalStateException, SAXException, ParserConfigurationException {
        int writtenFeatures = 0;
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
            opmerkingen.append("Geen features gevonden in bestand: ").append(gmlFileName).append("\n");
            LOG.info("Geen features gevonden in bestand: " + gmlFileName);
            dataStore.dispose();
            return writtenFeatures;
        }

        SimpleFeatureType sft = gmlFeatCollection.getSchema();
        GMLLightFeatureTransformer featTransformer = BGTGMLLightTransformerFactory.getTransformer(sft.getTypeName());
        if (featTransformer == null) {
            LOG.error("Opzoeken van FeatureTransformer voor " + sft.getTypeName() + " is mislukt; er geen transformer beschikbaar voor dit bestand.");
            dataStore.dispose();
            return writtenFeatures;
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

        Transaction transaction = new DefaultTransaction("add-bgt");
        Transaction updatetransaction = new DefaultTransaction("update-bgt");
        try (FeatureIterator<SimpleFeature> feats = gmlFeatCollection.features()) {
            FeatureStore store = (FeatureStore) dataStore.getFeatureSource(targetSchema.getTypeName(), transaction);
            FeatureStore store2 = (FeatureStore) dataStore.getFeatureSource(targetSchema.getTypeName(), updatetransaction);

            SimpleFeature gmlSF;
            SimpleFeature transformed;
            while (feats.hasNext()) {
                gmlSF = feats.next();
                // als object niet meer bestaat of nog niet bestaat overslaan!
                if ((/* bestaat nog niet*/((Date) gmlSF.getAttribute("objectBeginTijd")).after(new Date()))
                        | (/* bestaat niet meer */gmlSF.getAttribute("objectEindTijd") != null && ((Date) gmlSF.getAttribute("objectEindTijd")).before(new Date()))) {
                    // mogelijk toch nog de geom eruit halen voor de delete...
                    LOG.info("Vervallen of nog niet bestaand object gevonden met GML ID: " + gmlSF.getID());
                    continue;
                }

                transformed = featTransformer.transform(gmlSF, targetSchema, this.isOracle, dataStore.isExposePrimaryKeyColumns(), this.bijwerkDatum);
                if (transformed == null) {
                    continue;
                }
                if (this.loadingUpdate) {
                    // bijwerken omhullende omhullendeVanZipFile met vlakken in geval van een update
                    Geometry geom = (Geometry) transformed.getDefaultGeometry();
                    if (geom != null && geom.getDimension() > Dimension.L) {
                        if (omhullendeVanZipFile != null) {
                            try {
                                omhullendeVanZipFile = omhullendeVanZipFile.union(geom);
                            } catch (IllegalArgumentException | TopologyException ex) {
                                LOG.error("Union fout bij verwerking van " + transformed.getID() + " (GML ID: " + gmlSF.getID() + ", bestand: " + gmlFileName
                                        + ")", ex);
                                LOG.debug("geom voor union :" + geom);
                                LOG.debug("omhullendeVanZipFile voor union: " + omhullendeVanZipFile);
                            }
                        } else {
                            omhullendeVanZipFile = geom.union();
                        }
                    }
                }

                try {
                    store.addFeatures(DataUtilities.collection(transformed));
                    writtenFeatures++;
                    // commit per feature
                    transaction.commit();
                    LOG.debug("Feature toegevoegd in database met NEN3610ID: " + transformed.getID());
                } catch (IOException ioe) {
                    // als primary key violation bij insert dan afvangen
                    if (isDuplicateKeyViolationMessage(ioe.getCause().getMessage())) {
                        LOG.info("Duplicaat gevonden tijdens insert van feature: " + transformed.getID());
                        // bij mssql transactie sluiten ander blijft de boel hangen, voor orcl + pg ook
                        transaction.close();
                        // object opzoeken,
                        Filter filter = CommonFactoryFinder.getFilterFactory2(null).id(transformed.getIdentifier());
                        FeatureIterator<SimpleFeature> bestaandeFeats = store2.getFeatures(filter).features();
                        SimpleFeature bestaandeFeat = bestaandeFeats.next();

                        final String beginAttr = this.isOracle ? BEGINTIJD_NAME.toUpperCase() : BEGINTIJD_NAME;
                        final String idAttr = this.isOracle ? ID_NAME.toUpperCase() : ID_NAME;
                        // update als datum jonger dan bestaand
                        if (((Date) transformed.getAttribute(beginAttr)).after((Date) bestaandeFeat.getAttribute(beginAttr))) {
                            for (AttributeDescriptor attr : transformed.getFeatureType().getAttributeDescriptors()) {
                                Name attrName = attr.getName();
                                if (!attrName.getLocalPart().equals(idAttr)) {
                                    store2.modifyFeatures(attrName, transformed.getAttribute(attrName), filter);
                                }
                            }
                            LOG.debug("Klaar met update van feature: " + transformed.getID());
                            updatetransaction.commit();
                        } else {
                            LOG.debug("Geen update van feature: " + transformed.getID());
                        }
                        bestaandeFeats.close();
                        // maak een nieuw transactie voor toevoegen, de eerdere is aborted
                        transaction = new DefaultTransaction("add-bgt");
                        store.setTransaction(transaction);
                    } else {
                        throw ioe;
                    }
                }
            }
            opmerkingen.append("Aantal ingevoegde features voor: ").append(gmlFileName).append(": ").append(writtenFeatures).append("\n");
            LOG.info("Aantal ingevoegde features voor " + gmlFileName + ": " + writtenFeatures);
        } catch (IOException ioe) {
            LOG.error("I/O database probleem tijdens insert van features", ioe);
            this.status = STATUS.NOK;
            transaction.rollback();
        } finally {
            transaction.close();
            updatetransaction.close();
            dataStore.dispose();
        }
        return writtenFeatures;
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
     *
     * @param loadingUpdate {@code true} als er een update wordt geladen,
     * {@code false} voor een stand
     */
    public void setLoadingUpdate(boolean loadingUpdate) {
        this.loadingUpdate = loadingUpdate;
    }

    public void setBijwerkDatum(Date bijwerkDatum) {
        this.bijwerkDatum = bijwerkDatum;
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
    }
}
