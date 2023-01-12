/*
 * Copyright (C) 2022 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */
package nl.b3p;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.jdbc.util.converter.OracleConnectionUnwrapper;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Testcases voor 1 of meer objecten uit stand en per object 1 mutatie.
 * Draaien met:
 * {@code mvn -Dit.test=Brk2StandMutatieGedeeldeStukkenVerwerkingIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql -pl brmo-loader > /tmp/postgresql.log}
 * of
 * {@code mvn -Dit.test=Brk2StandMutatieGedeeldeStukkenVerwerkingIntegrationTest -Dtest.onlyITs=true verify -Poracle -pl brmo-loader > /tmp/oracle.log}
 * voor Oracle.
 *
 * @author mprins
 */
class Brk2StandMutatieGedeeldeStukkenVerwerkingIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(Brk2StandMutatieGedeeldeStukkenVerwerkingIntegrationTest.class);
    private final Lock sequential = new ReentrantLock(true);
    private BasicDataSource dsRsgbBrk;
    private BasicDataSource dsStaging;
    private BrmoFramework brmo;
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgbBrk;

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                // "filenames-stand",
                // "filenames-mutaties",
                // objectRefs,
                // rechtenStand,
                // stukkenStand,
                // stukdelenStand,
                // rechtenMutaties,
                // stukkenMutaties,
                // stukdelenMutaties,
                // int aantalKadObjLocatie, int aantalPubliekRBeperking, int aantalOnrndZkBeperking, int aantalFiliatie
                arguments(
                        new String[]{"/brk2/stand-53880252670000.anon.xml", "/brk2/stand-53880252170000.anon.xml"},
                        new String[]{"/brk2/mutatie-53880252670000-1.anon.xml", "/brk2/mutatie-53880252170000-1.anon.xml"},
                        Set.of("NL.IMKAD.KadastraalObject:53880252670000", "NL.IMKAD.KadastraalObject:53880252170000"),
                        Set.of("NL.IMKAD.ZakelijkRecht:50036403", "NL.IMKAD.Tenaamstelling:50111282", /*2e*/"NL.IMKAD.ZakelijkRecht:50036398", "NL.IMKAD.Tenaamstelling:50111273", "NL.IMKAD.Aantekening:50007474"),
                        // stukken NL.IMKAD.TIAStuk:20020924000346 en NL.IMKAD.TIAStuk:20081017002053 worden gedeeld in de stand
                        Stream.of("NL.IMKAD.TIAStuk:20020924000346", "NL.IMKAD.TIAStuk:20081017002053", /*2e*/  "NL.IMKAD.TIAStuk:18011026015603", "NL.IMKAD.TIAStuk:20020924000346", "NL.IMKAD.TIAStuk:20081017002053").collect(Collectors.toCollection(HashSet::new)),
                        // stukdeel NL.IMKAD.Stukdeel:AKR1.10630844 wordt gedeeld in de stand
                        // stukdeel NL.IMKAD.Stukdeel:1022805638 komt uit 1e bericht/53880252670000
                        Stream.of("NL.IMKAD.Stukdeel:1022805638", "NL.IMKAD.Stukdeel:AKR1.10630844",/*2e*/"NL.IMKAD.TIAStuk:20081017002053", "NL.IMKAD.Stukdeel:AKR1.10630844", "NL.IMKAD.Stukdeel:AKR1.10754757").collect(Collectors.toCollection(HashSet::new)),
                        // mutaties
                        Stream.of("NL.IMKAD.ZakelijkRecht:50036403", "NL.IMKAD.Tenaamstelling:1013515840", "NL.IMKAD.Tenaamstelling:1013515841", /*2*/"NL.IMKAD.ZakelijkRecht:50036398", "NL.IMKAD.Tenaamstelling:1013515838", "NL.IMKAD.Tenaamstelling:1013515839", "NL.IMKAD.Aantekening:50007474").collect(Collectors.toCollection(HashSet::new)),
                        Stream.of("NL.IMKAD.TIAStuk:20221117001513", /*2e*/  "NL.IMKAD.TIAStuk:18011026015603", "NL.IMKAD.TIAStuk:20221117001513").collect(Collectors.toCollection(HashSet::new)),
                        Stream.of("NL.IMKAD.Stukdeel:500006680324",/*2e*/"NL.IMKAD.Stukdeel:500006680324", "NL.IMKAD.Stukdeel:AKR1.10754757").collect(Collectors.toCollection(HashSet::new)),
                        2, 0, 0, 2
                )
        );
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        dsStaging = new BasicDataSource();
        dsStaging.setUrl(params.getProperty("staging.jdbc.url"));
        dsStaging.setUsername(params.getProperty("staging.user"));
        dsStaging.setPassword(params.getProperty("staging.passwd"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);

        dsRsgbBrk = new BasicDataSource();
        dsRsgbBrk.setUrl(params.getProperty("rsgbbrk.jdbc.url"));
        dsRsgbBrk.setUsername(params.getProperty("rsgbbrk.user"));
        dsRsgbBrk.setPassword(params.getProperty("rsgbbrk.passwd"));
        dsRsgbBrk.setAccessToUnderlyingConnectionAllowed(true);

        if (this.isOracle) {
            staging = new DatabaseConnection(
                    OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()),
                    params.getProperty("staging.user").toUpperCase()
            );
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);

            rsgbBrk = new DatabaseConnection(
                    OracleConnectionUnwrapper.unwrap(dsRsgbBrk.getConnection()),
                    params.getProperty("rsgbbrk.schema").toUpperCase()
            );
            rsgbBrk.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgbBrk.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            staging = new DatabaseDataSourceConnection(dsStaging);
            staging.getConfig().setProperty(
                    DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                    new PostgresqlDataTypeFactory()
            );
            rsgbBrk = new DatabaseDataSourceConnection(dsRsgbBrk, params.getProperty("rsgbbrk.schema"));
            rsgbBrk.getConfig().setProperty(
                    DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                    new PostgresqlDataTypeFactory()
            );
        }

        brmo = new BrmoFramework(dsStaging, null, dsRsgbBrk);

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet =
                fxdb.build(new FileInputStream(
                                new File(
                                        Objects.requireNonNull(
                                                Brk2StandMutatieGedeeldeStukkenVerwerkingIntegrationTest.class.getResource("/staging-empty-flat.xml")).toURI())
                        )
                );

        sequential.lock();
        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        // CleanUtil.cleanRSGB_BRK2(rsgbBrk);

        assumeTrue(0L == brmo.getCountBerichten(BrmoFramework.BR_BRK2, "STAGING_OK"),
                "Er zijn geen STAGING_OK berichten");
        assumeTrue(0L == brmo.getCountLaadProcessen(BrmoFramework.BR_BRK2, "STAGING_OK"),
                "Er zijn geen STAGING_OK laadprocessen");
    }

    @AfterEach
    void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanSTAGING(staging, false);
        CleanUtil.cleanRSGB_BRK2(rsgbBrk);
        staging.close();
        dsStaging.close();
        rsgbBrk.close();
        dsRsgbBrk.close();

        sequential.unlock();
    }

    @DisplayName("BRK2 XML in staging laden en transformeren, daarna mutaties toepassen")
    @ParameterizedTest(name = "testBerichten #{index}: object refs: {1}")
    @MethodSource("argumentsProvider")
    void testBerichten(
            String[] bestandNamen, String[] mutatieBestandNamen, Set<String> objectRefs,
            Set<String> rechtenStand, Set<String> stukkenStand, Set<String> stukdelenStand,
            Set<String> rechtenMutaties, Set<String> stukkenMutaties, Set<String> stukdelenMutaties,
            int aantalKadObjLocatie, int aantalPubliekRBeperking, int aantalOnrndZkBeperking, int aantalFiliatie
    ) throws Exception {

        final boolean isPerceel = objectRefs.iterator().next().endsWith("0000");

        assumeFalse(
                null == Brk2StandMutatieGedeeldeStukkenVerwerkingIntegrationTest.class.getResource(bestandNamen[0]),
                () -> "Het test bestand '" + bestandNamen[0] + "' moet er zijn."
        );

        // stand bericht laden
        bestandenLadenEnValideren(objectRefs, 0, bestandNamen);
        // voor stand, maar op volgorde van database
        brmo.setOrderBerichten(false);
        brmo.setEnablePipeline(false);
        brmo.setTransformPipelineCapacity(2);
        brmo.setBatchCapacity(1);
        brmo.setLimitStandBerichtenToTransform(2);
        transformerenEnValideren(bestandNamen.length);

        // controle BRK inhoud stand
        ITable recht = rsgbBrk.createDataSet().getTable("recht");
        checkIdentificatiesEnAantal(recht, rechtenStand);

        ITable stuk = rsgbBrk.createDataSet().getTable("stuk");
        checkIdentificatiesEnAantal(stuk, stukkenStand);

        ITable stukdeel = rsgbBrk.createDataSet().getTable("stukdeel");
        checkIdentificatiesEnAantal(stukdeel, stukdelenStand);

        ITable onroerendezaak = rsgbBrk.createDataSet().getTable("onroerendezaak");
        checkIdentificatiesEnAantal(onroerendezaak, objectRefs);

        // mutatie bericht(en) laden
        brmo.setOrderBerichten(true);
        bestandenLadenEnValideren(objectRefs, bestandNamen.length, mutatieBestandNamen);
        transformerenEnValideren(bestandNamen.length + mutatieBestandNamen.length);

        // BRK inhoud mutaties valideren
        onroerendezaak = rsgbBrk.createDataSet().getTable("onroerendezaak");
        checkIdentificatiesEnAantal(onroerendezaak, objectRefs);

        ITable onroerendezaak_archief = rsgbBrk.createDataSet().getTable("onroerendezaak_archief");
        checkIdentificatiesEnAantal(onroerendezaak_archief, objectRefs);

        recht = rsgbBrk.createDataSet().getTable("recht");
        checkIdentificatiesEnAantal(recht, rechtenMutaties);

        ITable recht_archief = rsgbBrk.createDataSet().getTable("recht_archief");
        checkIdentificatiesEnAantal(recht_archief, rechtenStand);

        stuk = rsgbBrk.createDataSet().getTable("stuk");
        // van stuk geen archief tabel, dus stand erbij nemen
        stukkenMutaties.addAll(stukkenStand);
        checkIdentificatiesEnAantal(stuk, stukkenMutaties);

        stukdeel = rsgbBrk.createDataSet().getTable("stukdeel");
        // van stukdeel geen archief tabel, dus stand erbij nemen
        stukdelenMutaties.addAll(stukdelenStand);
        checkIdentificatiesEnAantal(stukdeel, stukdelenMutaties);


        ITable publiekrechtelijkebeperking = rsgbBrk.createDataSet().getTable("publiekrechtelijkebeperking");
        ITable publiekrechtelijkebeperking_archief = rsgbBrk.createDataSet().getTable(
                "publiekrechtelijkebeperking_archief");
        assertEquals(aantalPubliekRBeperking, publiekrechtelijkebeperking.getRowCount(),
                "Aantal publiekrechtelijkebeperking klopt niet"
        );

        ITable onroerendezaakbeperking = rsgbBrk.createDataSet().getTable("onroerendezaakbeperking");
        ITable onroerendezaakbeperking_archief = rsgbBrk.createDataSet().getTable("onroerendezaakbeperking_archief");
        assertEquals(aantalOnrndZkBeperking, onroerendezaakbeperking.getRowCount(),
                "Aantal onroerendezaakbeperking klopt niet"
        );

        ITable onroerendezaakfiliatie = rsgbBrk.createDataSet().getTable("onroerendezaakfiliatie");
        ITable onroerendezaakfiliatie_archief = rsgbBrk.createDataSet().getTable("onroerendezaakfiliatie_archief");
        assertEquals(aantalFiliatie, onroerendezaakfiliatie.getRowCount(), "Aantal onroerendezaakfiliatie klopt niet");
        // check dat alle records de objectRef als "onroerendezaak" hebben
        String onroerendezaakRef;
        for (int i = 0; i < onroerendezaakfiliatie.getRowCount(); i++) {
            onroerendezaakRef = onroerendezaakfiliatie.getValue(i, "onroerendezaak").toString();
            assertTrue(objectRefs.contains(onroerendezaakRef),
                    "onroerendezaakfiliatie.onroerendezaak " + onroerendezaakRef + " is niet gelijk aan objectRef"
            );
        }
        for (int i = 0; i < onroerendezaakfiliatie_archief.getRowCount(); i++) {
            onroerendezaakRef = onroerendezaakfiliatie_archief.getValue(i, "onroerendezaak").toString();
            assertTrue(objectRefs.contains(onroerendezaakRef),
                    "onroerendezaakfiliatie_archief.onroerendezaak " + onroerendezaakRef + " is niet gelijk aan objectRef"
            );
        }

        ITable perceelOfAppRe;
        ITable perceelOfAppRe_archief;
        if (isPerceel) {
            perceelOfAppRe = rsgbBrk.createDataSet().getTable("perceel");
            perceelOfAppRe_archief = rsgbBrk.createDataSet().getTable("perceel_archief");
            assertAll(
                    "perceel geometrie",
                    () -> assertNotNull(
                            perceelOfAppRe.getValue(0, "begrenzing_perceel"),
                            "Perceel begrenzing geometrie is 'null'"
                    ),
                    () -> assertNotNull(
                            perceelOfAppRe.getValue(0, "plaatscoordinaten"),
                            "Plaatscoordinaten geometrie is 'null'"
                    ),
                    () -> assertNotNull(
                            perceelOfAppRe_archief.getValue(0, "begrenzing_perceel"),
                            "Perceel begrenzing geometrie is 'null'"
                    ),
                    () -> assertNotNull(
                            perceelOfAppRe_archief.getValue(0, "plaatscoordinaten"),
                            "Plaatscoordinaten geometrie is 'null'"
                    )
            );
        } else {
            perceelOfAppRe = rsgbBrk.createDataSet().getTable("appartementsrecht");
            perceelOfAppRe_archief = rsgbBrk.createDataSet().getTable("appartementsrecht_archief");
        }
        checkIdentificatiesEnAantal(perceelOfAppRe, objectRefs);
        checkIdentificatiesEnAantal(perceelOfAppRe_archief, objectRefs);

//        ITable aantekeningrecht = rsgbBrk.createDataSet().getTable("recht_aantekeningrecht");
//        ITable isbelastmet = rsgbBrk.createDataSet().getTable("recht_isbelastmet");
//        ITable isbeperkttot = rsgbBrk.createDataSet().getTable("recht_isbeperkttot");

//        ITable aantekeningrecht_archief = rsgbBrk.createDataSet().getTable("recht_aantekeningrecht_archief");
//        ITable isbelastmet_archief = rsgbBrk.createDataSet().getTable("recht_isbelastmet_archief");
//        ITable isbeperkttot_archief = rsgbBrk.createDataSet().getTable("recht_isbeperkttot_archief");

//        ITable persoon = rsgbBrk.createDataSet().getTable("persoon");
//        ITable natuurlijkpersoon = rsgbBrk.createDataSet().getTable("natuurlijkpersoon");
//        ITable nietnatuurlijkpersoon = rsgbBrk.createDataSet().getTable("nietnatuurlijkpersoon");
//        ITable adres = rsgbBrk.createDataSet().getTable("adres");

        ITable objectlocatie = rsgbBrk.createDataSet().getTable("objectlocatie");
        ITable objectlocatie_archief = rsgbBrk.createDataSet().getTable("objectlocatie_archief");
        assertAll("objectlocatie",
                () -> assertEquals(aantalKadObjLocatie, objectlocatie.getRowCount(),
                        "Het aantal objectlocaties is niet als verwacht."),
                () -> {
                    // check dat alle records de objectRef als "heeft" hebben
                    for (int i = 0; i < objectlocatie.getRowCount(); i++) {
                        assertTrue(objectRefs.contains(objectlocatie.getValue(i, "heeft")),
                                "objectlocatie.heeft is niet gelijk aan objectRef"
                        );
                    }
                },
                () -> {
                    for (int i = 0; i < objectlocatie_archief.getRowCount(); i++) {
                        assertTrue(objectRefs.contains(objectlocatie_archief.getValue(i, "heeft")),
                                "objectlocatie.heeft is niet gelijk aan objectRef"
                        );
                    }
                }
        );
    }

    /**
     * Check of de identificaties van de objecten in een tabel gelijk zijn aan de verwachte identificaties.
     *
     * @param tableToCheck           de te controleren tabel
     * @param expectedIdentificaties de verwachte identificaties (nromaal ook primary keys)
     * @throws Exception if any
     */
    private void checkIdentificatiesEnAantal(ITable tableToCheck, Set<String> expectedIdentificaties) throws Exception {
        assertEquals(expectedIdentificaties.size(), tableToCheck.getRowCount(),
                () -> "Het aantal records in de tabel " + tableToCheck.getTableMetaData().getTableName() + " is niet gelijk aan het aantal verwachte identificaties"
        );
        for (int i = 0; i < tableToCheck.getRowCount(); i++) {
            String identificatie = (String) tableToCheck.getValue(i, "identificatie");
            assertTrue(
                    expectedIdentificaties.contains(identificatie),
                    () -> "identificatie " + identificatie + " komt niet voor in " + tableToCheck.getTableMetaData().getTableName()
            );
        }
    }


    private void transformerenEnValideren(int expected) throws BrmoException, InterruptedException {
        LOG.info("Transformeren berichten naar rsgb DB met optie 'orderberichten': " + brmo.isOrderBerichten());
        Thread t = brmo.toRsgb();
        t.join();

        List<Bericht> berichten = brmo.listBerichten();
        if (LOG.isInfoEnabled()) {
            berichten.forEach(b -> LOG.debug(b.toString()));
        }

        assertEquals(expected, brmo.getCountBerichten(BrmoFramework.BR_BRK2, "RSGB_OK"),
                "Niet alle berichten zijn OK getransformeerd."
        );

        berichten.forEach(b -> {
            assertNotNull(b, "Bericht is 'null'");
            assertNotNull(b.getDbXml(), "'db-xml' van bericht is 'null'");
        });
    }

    /**
     * laden en valideren van brk 2 bestand(en).
     *
     * @param objectRefs    objectRefs van de te laden berichten
     * @param eerderGeladen aantal eerder geladen berichten (uit stand bijv.)
     * @param bestandsNamen 1 of meer berichten
     * @throws Exception if any
     */
    private void bestandenLadenEnValideren(Set<String> objectRefs, int eerderGeladen, String... bestandsNamen) throws Exception {
        int aantal = bestandsNamen.length + eerderGeladen;

        for (String bestandsNaam : bestandsNamen) {
            LOG.info("Laden van " + bestandsNaam + " bericht in staging DB.");
            brmo.loadFromFile(BrmoFramework.BR_BRK2,
                    Objects.requireNonNull(Brk2StandMutatieGedeeldeStukkenVerwerkingIntegrationTest.class.getResource(bestandsNaam)).getFile(), null
            );
        }

        assertEquals(aantal, brmo.getCountLaadProcessen(BrmoFramework.BR_BRK2, "STAGING_OK"),
                "Het aantal laadprocessen is niet correct."
        );
        List<LaadProces> processen = brmo.listLaadProcessen();
        assertNotNull(processen, "De verzameling processen bestaat niet.");
        assertEquals(aantal, processen.size(), "Het aantal processen is niet 1.");

        List<Bericht> berichten = brmo.listBerichten();
        assertFalse(berichten.isEmpty(), "De verzameling berichten bestaat niet.");
        assertEquals(aantal, berichten.size(), "Het aantal berichten is niet correct.");

        for (Bericht b : berichten) {
            assertAll(
                    "bericht",
                    () -> assertNotNull(b, "Bericht is 'null'"),
                    () -> assertNotNull(b.getBrXml(), "'br_xml' van bericht is 'null'"),
                    () -> assertTrue(
                            objectRefs.contains(b.getObjectRef()),
                            "Het bericht uit de database heeft niet de juiste objectRef."
                    )
            );
        }
    }
}
