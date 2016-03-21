/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.feature.FeatureCollection;
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
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureIterator;
import org.geotools.jdbc.JDBCFeatureSource;
import org.geotools.jdbc.JDBCInsertFeatureWriter;
import org.geotools.jdbc.JDBCUpdateFeatureWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

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
     * {@code true} als we tegene een oracle database werken, default
     * {@code false}
     */
    private boolean isOracle = false;

    /**
     * Maak automatisch tabellen aan; normaal niet/default {@code false}.
     */
    private boolean createTables = false;

    private Parser parser;

    /**
     * Default constructor initaliseert de GML parser.
     */
    public BGTGMLLightLoader() {
        final String schemaLocation = BGTGMLLightLoader.class.getResource("/imgeo-simple_resolved.xsd").getFile();

        Configuration configuration = new ApplicationSchemaConfiguration("http://www.geostandaarden.nl/imgeo/2.1/simple/gml31", schemaLocation);
        configuration.getContext().registerComponentInstance(new GeometryFactory(new PrecisionModel(), 28992));

        // SchemaResolver resolver = new SchemaResolver(SchemaCatalog.build(BGTGMLLightLoader.class.getResource("/resolved-catalog.xml")));
        // configuration.getContext().registerComponentInstance(resolver);
        parser = new Parser(configuration);
        parser.setValidating(true);
        parser.setStrict(true);
        parser.setFailOnValidationError(true);
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
     * @throws IOException als ophalen next zipentry mislukt
     */
    public int processZipFile(File zipExtract) throws FileNotFoundException, IOException {
        int result = 0;
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
                    LOG.debug("Lezen GML bestand " + entry.getName() + " uit zip " + zipExtract.getCanonicalPath());
                    FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = parseGML(new CloseShieldInputStream(zip));
                    if (!featureCollection.isEmpty()) {
                        result += storeFeatureCollection(featureCollection, entry.getName().toLowerCase());
                    } else {
                        LOG.debug("Geen features gevonden in bestand: " + entry.getName());
                    }
                }
                entry = zip.getNextEntry();
            }
        } catch (SAXException | ParserConfigurationException ex) {
            LOG.error("Er is een parse fout opgetreden.", ex);
        }
        return result;
    }

    /**
     * verwerk een GML bestand.
     *
     * @param gml GML Light bestand
     * @return aantal geschreven features
     *
     * @throws IOException als ophalen gml mislukt
     */
    public int processGMLFile(File gml) throws IOException {
        int result = 0;
        try {
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = parseGML(new FileInputStream(gml));
            if (!featureCollection.isEmpty()) {
                result = storeFeatureCollection(featureCollection, gml.getName().toLowerCase());
            } else {
                LOG.debug("Geen features gevonden in bestand: " + gml.getName());
            }
        } catch (SAXException | ParserConfigurationException ex) {
            LOG.error("Er is een parse fout opgetreden.", ex);
        }
        return result;
    }

    /**
     * Parse GML bestand uit de stream.
     *
     * @param in GML inputstream
     * @return verzameling SimpleFeatures (mogelijk leeg)
     *
     * @throws IOException als openen van inputstream mislukt
     * @throws SAXException als parsen van in mislukt
     * @throws ParserConfigurationException als gml parser config niet deugd
     */
    private FeatureCollection<SimpleFeatureType, SimpleFeature> parseGML(InputStream in) throws IOException, SAXException, ParserConfigurationException {
        SimpleFeatureCollection featureCollection = (SimpleFeatureCollection) parser.parse(in);
//        LOG.debug("Parser schema's: " + Arrays.toString(parser.getSchemas()));
//        if (LOG.isDebugEnabled()) {
//            featureCollection.accepts(new AbstractFeatureVisitor() {
//                private Collection<Property> props;
//
//                @Override
//                public void visit(Feature feature) {
//                    props = feature.getProperties();
//                    String s = "Feature " + feature.getIdentifier() + "\n";
//                    for (Property p : props) {
//                        s = s.concat(" -> " + p.getName() + " (" + p.getType() + "): value " + p.getValue() + "\n");
//                    }
//                    LOG.debug(s);
//                }
//            }, new NullProgressListener());
//        }
        return featureCollection;
    }

    /**
     * transformeert de input en slaat op in database.
     *
     * @param collection te laden features
     * @param gmlFileName naam input gml bestand
     * @return aantal geschreven features
     *
     * @throws IOException als er een database fout optreedt
     * @throws IllegalStateException als er iets mis is in de configuratie
     */
    private int storeFeatureCollection(FeatureCollection<SimpleFeatureType, SimpleFeature> collection, String gmlFileName) throws IOException, IllegalStateException {
        int writtenFeatures = 0;
        GMLLightFeatureTransformer featTransformer = BGTGMLLightTransformerFactory.getTransformer(gmlFileName);
        if (featTransformer == null) {
            LOG.error("Opzoeken van FeatureTransformer voor " + gmlFileName + " is mislukt; er geen transformer beschikbaar voor dit bestand.");
            return writtenFeatures;
        }

        JDBCDataStore dataStore = (JDBCDataStore) DataStoreFinder.getDataStore(dbConnProps);
        if (dataStore == null) {
            throw new IllegalStateException("Datastore mag niet null voor opslaan van data.");
        }
        dataStore.setExposePrimaryKeyColumns(true);

        // check table exists
        String tableName = BGTGMLLightTransformerFactory.getTableName(gmlFileName);
        boolean exists = false;
        String[] typeNames = dataStore.getTypeNames();
        for (String name : typeNames) {
            if (tableName.equalsIgnoreCase(name)) {
                LOG.debug("De tabel '" + tableName + "' is gevonden in de database als: " + name);
                exists = true;
            }
        }

        SimpleFeatureType sft = (SimpleFeatureType) collection.getSchema();
        SimpleFeatureType targetSchema = featTransformer.getTargetSchema(sft, tableName, this.isOracle);
        if (!exists) {
            if (createTables) {
                dataStore.createSchema(targetSchema);
                dataStore.setExposePrimaryKeyColumns(false);
                LOG.info("De volgende tabel is aangemaakt in de database: " + targetSchema.getTypeName());
            } else {
                LOG.error("Tabel: " + tableName + " is niet beschikbaar in de database.");
                throw new IllegalStateException("De tabel " + tableName + " ontbreekt in de database; opslaan van gegevens uit GML " + gmlFileName + " is niet mogelijk.");
            }
        }
        Transaction transaction = new DefaultTransaction("add-bgt");
        try (FeatureIterator<SimpleFeature> feats = collection.features()) {
            FeatureStore store = (FeatureStore) dataStore.getFeatureSource(targetSchema.getTypeName(), transaction);
            while (feats.hasNext()) {
                SimpleFeature transformed = featTransformer.transform(feats.next(), targetSchema, this.isOracle);
                store.addFeatures(DataUtilities.collection(transformed));
                writtenFeatures++;
            }
            transaction.commit();
            LOG.info("Aantal ingevoegde features: " + writtenFeatures);
        } catch (IOException ioe) {
            LOG.error("I/O database probleem tijdens insert van features", ioe);
            transaction.rollback();
        } finally {
            transaction.close();
            dataStore.dispose();
        }
        return writtenFeatures;
    }

    /**
     * scan directory for zipfiles in de geconfigueerde directory.
     *
     * @return lijst met zipfiles
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
    }

    /**
     * Maak automatisch tabellen aan.
     *
     * @param createTables {@code true} om automatisch tabellen aan te maken,
     * default is {@code false}
     */
    public void setCreateTables(boolean createTables) {
        this.createTables = createTables;
    }

    /**
     * omdat oracle uppercase heeft voor tabellen en velden...
     *
     * @param isOracle {@code true} als het zo is
     */
    public void setIsOracle(boolean isOracle) {
        this.isOracle = isOracle;
    }
}
