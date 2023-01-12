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
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Draaien met:
 * {@code mvn -Dit.test=Brk2MutatieVerwijderVerwerkingIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql -pl
 * brmo-loader > /tmp/postgresql.log} of
 * {@code mvn -Dit.test=Brk2MutatieVerwijderVerwerkingIntegrationTest -Dtest.onlyITs=true verify -Poracle -pl brmo-loader
 * > /tmp/oracle.log}
 * voor Oracle.
 *
 * @author mprins
 */
class Brk2MutatieVerwijderVerwerkingIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(Brk2MutatieVerwijderVerwerkingIntegrationTest.class);
    private final Lock sequential = new ReentrantLock(true);
    private BasicDataSource dsRsgbBrk;
    private BasicDataSource dsStaging;
    private BrmoFramework brmo;
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgbBrk;

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                // { "1 bericht-filename",
                // "filenames-mutaties",
                // objectRef,
                // rechtenStand,
                // stukkenStand,
                // stukdelenStand,
                // rechtenMutaties 1e,
                // rechtenMutaties laatste,
                // stukkenMutaties,
                // stukdelenMutaties,
                // aantalKadObjLocatie, aantalPubliekRBeperking, aantalOnrndZkBeperking, aantalFiliatie},

                arguments(
                        "/brk2/mutatie-5230168170000-1.anon.xml",
                        new String[]{"/brk2/vervallen-5230168170000-2.anon.xml"},
                        "NL.IMKAD.KadastraalObject:5230168170000",
                        // 1e bericht
                        Set.of("NL.IMKAD.ZakelijkRecht:1001274173", "NL.IMKAD.Tenaamstelling:1007585455"),
                        Set.of("NL.IMKAD.TIAStuk:20040115000632", "NL.IMKAD.TIAStuk:20090402001713"),
                        Set.of("NL.IMKAD.Stukdeel:1018080241", "NL.IMKAD.Stukdeel:1018231900"),
                        // laatste bericht
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        0, 0, 0, 0
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
                                                Brk2MutatieVerwijderVerwerkingIntegrationTest.class.getResource("/staging-empty-flat.xml")).toURI())
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
    @ParameterizedTest(name = "testBrk2XMLToStagingToRsgb #{index}: bestand: {0}, object ref: {1}")
    @MethodSource("argumentsProvider")
    void testBericht(
            String bestandNaam, String[] mutatieBestandNamen, String objectRef,
            Set<String> rechtenStand, Set<String> stukkenStand, Set<String> stukdelenStand,
            Set<String> rechtenMutaties, Set<String> rechtenMutatiesLaatste, Set<String> stukkenMutaties, Set<String> stukdelenMutaties,
            //int aantalRecht, int aantalStuk, int aantalStukdeel,
            int aantalKadObjLocatie, int aantalPubliekRBeperking, int aantalOnrndZkBeperking, int aantalFiliatie
    ) throws Exception {
        assumeFalse(
                null == Brk2MutatieVerwijderVerwerkingIntegrationTest.class.getResource(bestandNaam),
                () -> "Het test bestand '" + bestandNaam + "' moet er zijn."
        );

        // 1e bericht laden
        this.bestandLadenEnValideren(objectRef, 0, bestandNaam);
        this.transformerenEnValideren(1);

        ITable recht = rsgbBrk.createDataSet().getTable("recht");
        checkIdentificatiesEnAantal(recht, rechtenStand, 0);

        ITable stuk = rsgbBrk.createDataSet().getTable("stuk");
        checkIdentificatiesEnAantal(stuk, stukkenStand, 0);

        ITable stukdeel = rsgbBrk.createDataSet().getTable("stukdeel");
        checkIdentificatiesEnAantal(stukdeel, stukdelenStand, 0);

        ITable onroerendezaak = rsgbBrk.createDataSet().getTable("onroerendezaak");
        checkIdentificatiesEnAantal(onroerendezaak, Set.of(objectRef), 0);

        // mutatie bericht(en) laden
        this.bestandLadenEnValideren(objectRef, 1, mutatieBestandNamen);
        this.transformerenEnValideren(1 + mutatieBestandNamen.length);

        // BRK inhoud valideren na toepassen mutaties
        onroerendezaak = rsgbBrk.createDataSet().getTable("onroerendezaak");
        checkIdentificatiesEnAantal(onroerendezaak, Collections.emptySet(), 0);

        ITable onroerendezaak_archief = rsgbBrk.createDataSet().getTable("onroerendezaak_archief");
        checkIdentificatiesEnAantal(onroerendezaak_archief, Set.of(objectRef), mutatieBestandNamen.length);

        recht = rsgbBrk.createDataSet().getTable("recht");
        checkIdentificatiesEnAantal(recht, rechtenMutatiesLaatste, mutatieBestandNamen.length);

        ITable recht_archief = rsgbBrk.createDataSet().getTable("recht_archief");
        List<String> all = new ArrayList<>(rechtenStand);
        all.addAll(rechtenMutaties);
        checkIdentificatiesEnAantal(recht_archief, all, 1);

        stuk = rsgbBrk.createDataSet().getTable("stuk");
        // van stuk geen archief tabel, dus stand erbij nemen
        all = new ArrayList<>(stukkenStand);
        all.addAll(stukkenMutaties);
        checkIdentificatiesEnAantal(stuk, all, mutatieBestandNamen.length);

        stukdeel = rsgbBrk.createDataSet().getTable("stukdeel");
        // van stukdeel geen archief tabel, dus stand erbij nemen
        all = new ArrayList<>(stukdelenStand);
        all.addAll(stukdelenMutaties);
        checkIdentificatiesEnAantal(stukdeel, all, mutatieBestandNamen.length);

        ITable publiekrechtelijkebeperking = rsgbBrk.createDataSet().getTable("publiekrechtelijkebeperking");
        ITable publiekrechtelijkebeperking_archief = rsgbBrk.createDataSet().getTable(
                "publiekrechtelijkebeperking_archief");
        assertEquals(aantalPubliekRBeperking, publiekrechtelijkebeperking.getRowCount(),
                "Aantal publiekrechtelijkebeperking klopt niet"
        );

        ITable onroerendezaakbeperking = rsgbBrk.createDataSet().getTable("onroerendezaakbeperking");
//        ITable onroerendezaakbeperking_archief = rsgbBrk.createDataSet().getTable("onroerendezaakbeperking_archief");
        assertEquals(aantalOnrndZkBeperking, onroerendezaakbeperking.getRowCount(),
                "Aantal onroerendezaakbeperking klopt niet"
        );

        ITable onroerendezaakfiliatie = rsgbBrk.createDataSet().getTable("onroerendezaakfiliatie");
        ITable onroerendezaakfiliatie_archief = rsgbBrk.createDataSet().getTable("onroerendezaakfiliatie_archief");
        assertEquals(aantalFiliatie, onroerendezaakfiliatie.getRowCount(), "Aantal onroerendezaakfiliatie klopt niet");
        // check dat alle records de objectRef als "onroerendezaak" hebben
        for (int i = 0; i < onroerendezaakfiliatie.getRowCount(); i++) {
            assertEquals(objectRef, onroerendezaakfiliatie.getValue(i, "onroerendezaak"),
                    "onroerendezaakfiliatie.onroerendezaak is niet gelijk aan objectRef"
            );
        }
        for (int i = 0; i < onroerendezaakfiliatie_archief.getRowCount(); i++) {
            assertEquals(objectRef, onroerendezaakfiliatie_archief.getValue(i, "onroerendezaak"),
                    "onroerendezaakfiliatie_archief.onroerendezaak is niet gelijk aan objectRef"
            );
        }

        ITable perceelOfAppRe;
        ITable perceelOfAppRe_archief;
        if (objectRef.endsWith("0000")) {
            // perceel
            perceelOfAppRe = rsgbBrk.createDataSet().getTable("perceel");
            perceelOfAppRe_archief = rsgbBrk.createDataSet().getTable("perceel_archief");
            assertAll(
                    "perceel geometrie",
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
        checkIdentificatiesEnAantal(perceelOfAppRe, Collections.emptySet(), mutatieBestandNamen.length);
        checkIdentificatiesEnAantal(perceelOfAppRe_archief, Set.of(objectRef), mutatieBestandNamen.length);

        ITable objectlocatie = rsgbBrk.createDataSet().getTable("objectlocatie");
        assertAll("objectlocatie", () -> assertEquals(aantalKadObjLocatie, objectlocatie.getRowCount(),
                        "Het aantal objectlocaties is niet als verwacht."
                ),
                () -> {
                    // check dat alle records de objectRef als "heeft" hebben
                    for (int i = 0; i < objectlocatie.getRowCount(); i++) {
                        assertEquals(objectRef, objectlocatie.getValue(i, "heeft"),
                                "objectlocatie.heeft is niet gelijk aan objectRef"
                        );
                    }
                }
        );
        ITable objectlocatie_archief = rsgbBrk.createDataSet().getTable("objectlocatie_archief");
        for (int i = 0; i < objectlocatie_archief.getRowCount(); i++) {
            assertEquals(objectRef, objectlocatie_archief.getValue(i, "heeft"),
                    "objectlocatie.heeft is niet gelijk aan objectRef"
            );
        }

    }

    private void transformerenEnValideren(int expected) throws BrmoException, InterruptedException {
        LOG.debug("Transformeren berichten naar rsgb DB.");
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals(expected, brmo.getCountBerichten(BrmoFramework.BR_BRK2, "RSGB_OK"),
                "Niet alle berichten zijn OK getransformeerd"
        );
        List<Bericht> berichten = brmo.listBerichten();
        berichten.forEach(b -> {
            assertNotNull(b, "Bericht is 'null'");
            assertNotNull(b.getDbXml(), "'db-xml' van bericht is 'null'");
        });
    }

    /**
     * Check of de identificaties van de objecten in een tabel gelijk zijn aan de verwachte identificaties.
     *
     * @param tableToCheck           de te controleren tabel
     * @param expectedIdentificaties de verwachte identificaties (normaal ook primary keys)
     * @throws Exception if any
     */
    private void checkIdentificatiesEnAantal(ITable tableToCheck, Collection<String> expectedIdentificaties, int aantalMutaties) throws Exception {
        int aantal = expectedIdentificaties.size();
        if (tableToCheck.getTableMetaData().getTableName().toLowerCase().contains("_archief")) {
            // bijvoorbeeld 1 object en 2 mutaties
            aantal = aantalMutaties * expectedIdentificaties.size();
        }
        assertEquals(aantal, tableToCheck.getRowCount(),
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

    /**
     * laden en valideren van brk 2 bestand(en).
     *
     * @param objectRef     objectRef van het te laden bericht
     * @param bestandsNamen 1 of meer berichten
     * @throws Exception if any
     */
    private void bestandLadenEnValideren(String objectRef, int eerderGeladen, String... bestandsNamen) throws Exception {
        int aantal = bestandsNamen.length + eerderGeladen;

        for (String bestandsNaam : bestandsNamen) {
            LOG.debug("Laden van " + bestandsNaam + "bericht in staging DB.");
            brmo.loadFromFile(BrmoFramework.BR_BRK2,
                    Objects.requireNonNull(Brk2MutatieVerwijderVerwerkingIntegrationTest.class.getResource(bestandsNaam)).getFile(), null
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
                    () -> assertEquals(objectRef, b.getObjectRef(),
                            "Het bericht uit de database heeft niet de juiste objectRef."
                    )
            );
        }
    }
}
