/*
 * Copyright (C) 2022 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */
package nl.b3p;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
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
import org.gaul.modernizer_maven_annotations.SuppressModernizer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Draaien met: {@code mvn -Dit.test=Brk2ToStagingToRsgbBrkIntegrationTest -Dtest.onlyITs=true
 * verify -Ppostgresql -pl brmo-loader > /tmp/postgresql.log} of {@code mvn
 * -Dit.test=Brk2ToStagingToRsgbBrkIntegrationTest -Dtest.onlyITs=true verify -Poracle -pl
 * brmo-loader > /tmp/oracle.log} voor Oracle.
 *
 * @author mprins
 */
@SuppressModernizer
class Brk2ToStagingToRsgbBrkIntegrationTest extends AbstractDatabaseIntegrationTest {

  private static final Log LOG = LogFactory.getLog(Brk2ToStagingToRsgbBrkIntegrationTest.class);
  private final Lock sequential = new ReentrantLock(true);
  private BasicDataSource dsRsgbBrk;
  private BasicDataSource dsStaging;
  private BrmoFramework brmo;
  private IDatabaseConnection staging;
  private IDatabaseConnection rsgbBrk;

  static Stream<Arguments> argumentsProvider() {
    return Stream.of(
        // { "filename", objectRef, aantalRecht, aantalStuk, aantalStukdeel, aantalNP,
        // aantalNNP, aantalAdres (Adres:*), aantalKadObjLocatie, aantalPubliekRBeperking,
        // aantalOnrndZkBeperking, aantalFiliatie, aantalAantekeningRecht,
        // aantalIsbelastmetRecht, aantalIsbeperkttotRecht},
        arguments(
            "/brk2/stand-appre-1.anon.xml",
            "NL.IMKAD.KadastraalObject:53761288010001",
            3,
            2,
            2,
            1,
            1,
            (3),
            1,
            0,
            0,
            0,
            0,
            0,
            0),
        arguments(
            "/brk2/stand-perceel-1.anon.xml",
            "NL.IMKAD.KadastraalObject:50247970000",
            2,
            1,
            1,
            0,
            1,
            (2),
            0,
            0,
            0,
            0,
            0,
            0,
            0),
        arguments(
            "/brk2/stand-perceel-2.anon.xml",
            "NL.IMKAD.KadastraalObject:53730000170000",
            2,
            1,
            1,
            0,
            1,
            (2),
            0,
            0,
            0,
            0,
            0,
            0,
            0),
        arguments(
            "/brk2/stand-perceel-3.anon.xml",
            "NL.IMKAD.KadastraalObject:89760037170000",
            2,
            2,
            2,
            1,
            1,
            (4),
            1,
            1,
            1,
            1,
            0,
            0,
            0,
            0),
        arguments(
            "/brk2/MUTKX02-ABG00F1856-20211012-1.anon.xml",
            "NL.IMKAD.KadastraalObject:5260185670000",
            3,
            2,
            2,
            2,
            0,
            (2),
            0,
            0,
            0,
            0,
            1,
            0,
            0),
        arguments(
            "/brk2/MUTKX02-ABG00F1856-20211102-1.anon.xml",
            "NL.IMKAD.KadastraalObject:5260185670000",
            3,
            2,
            2,
            1,
            1,
            (2),
            0,
            0,
            0,
            0,
            1,
            0,
            0),
        // buitenlands adres
        arguments(
            "/brk2/stand-appre-2.anon.xml",
            "NL.IMKAD.KadastraalObject:53850184110001",
            18,
            4,
            7,
            6,
            2,
            (19),
            11,
            0,
            0,
            0,
            4 + 3,
            1,
            6),
        // met ligplaatsen
        arguments(
            "/brk2/stand-perceel-4.anon.xml",
            "NL.IMKAD.KadastraalObject:53830384970000",
            3,
            2,
            2,
            0,
            2,
            (14),
            11,
            0,
            0,
            1,
            0,
            0,
            0),
        // samenvoeging van 3 percelen
        arguments(
            "/brk2/stand-perceel-5.anon.xml",
            "NL.IMKAD.KadastraalObject:53750049870000",
            2,
            1,
            1,
            2,
            0,
            (1),
            0,
            0,
            0,
            3,
            0,
            0,
            0),
        // app.re met ondersplitsing en ontbrekende hoofdsplitsing referentie
        arguments(
            "/brk2/stand-appre-3.anon.xml",
            "NL.IMKAD.KadastraalObject:53830693710057",
            4,
            3,
            3,
            1,
            1,
            (1),
            0,
            0,
            0,
            0,
            0,
            0,
            0),
        // erfpacht
        arguments(
            "/brk2/stand-perceel-6.anon.xml",
            "NL.IMKAD.KadastraalObject:53810161070000",
            7,
            3,
            4,
            0,
            2,
            (3),
            0,
            0,
            0,
            1,
            1,
            1,
            0),
        // nevenadres
        arguments(
            "/brk2/stand-perceel-7.anon.xml",
            "NL.IMKAD.KadastraalObject:53850231870000",
            2,
            4,
            4,
            0,
            1,
            (6),
            4,
            0,
            0,
            1,
            0,
            0,
            0),
        // meerdere stukdelen onder tenaamstelling
        arguments(
            "/brk2/stand-perceel-8.anon.xml",
            "NL.IMKAD.KadastraalObject:53730012470000",
            8,
            7,
            8,
            2,
            3,
            (3),
            0,
            0,
            0,
            0,
            0,
            2,
            0)
        // { "filename", objectRef, aantalRecht, aantalStuk, aantalStukdeel, aantalNP,
        // aantalNNP, aantalAdres (Adres:*), aantalKadObjLocatie, aantalPubliekRBeperking,
        // aantalOnrndZkBeperking, aantalFiliatie, aantalAantekeningRecht,
        // aantalIsbelastmetRecht, aantalIsbeperkttotRecht}
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
      staging =
          new DatabaseConnection(
              OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()),
              params.getProperty("staging.user").toUpperCase());
      staging
          .getConfig()
          .setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
      staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);

      rsgbBrk =
          new DatabaseConnection(
              OracleConnectionUnwrapper.unwrap(dsRsgbBrk.getConnection()),
              params.getProperty("rsgbbrk.schema").toUpperCase());
      rsgbBrk
          .getConfig()
          .setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
      rsgbBrk.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
    } else if (this.isPostgis) {
      staging = new DatabaseDataSourceConnection(dsStaging);
      staging
          .getConfig()
          .setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
      rsgbBrk = new DatabaseDataSourceConnection(dsRsgbBrk, params.getProperty("rsgbbrk.schema"));
      rsgbBrk
          .getConfig()
          .setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
    }

    brmo = new BrmoFramework(dsStaging, null, dsRsgbBrk);

    FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
    fxdb.setCaseSensitiveTableNames(false);
    IDataSet stagingDataSet =
        fxdb.build(
            new FileInputStream(
                new File(
                    Objects.requireNonNull(
                            Brk2ToStagingToRsgbBrkIntegrationTest.class.getResource(
                                "/staging-empty-flat.xml"))
                        .toURI())));

    sequential.lock();
    DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
    // CleanUtil.cleanRSGB_BRK2(rsgbBrk);

    assumeTrue(
        0L == brmo.getCountBerichten(BrmoFramework.BR_BRK2, "STAGING_OK"),
        "Er zijn geen STAGING_OK berichten");
    assumeTrue(
        0L == brmo.getCountLaadProcessen(BrmoFramework.BR_BRK2, "STAGING_OK"),
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

  @DisplayName("BRK2 XML in staging laden en transformeren")
  @ParameterizedTest(name = "testBrk2XMLToStagingToRsgb #{index}: bestand: {0}, object ref: {1}")
  @MethodSource("argumentsProvider")
  void testBericht(
      String bestandNaam,
      String objectRef,
      int aantalRecht,
      int aantalStuk,
      int aantalStukdeel,
      int aantalNP,
      int aantalNNP,
      int aantalAdres,
      int aantalKadObjLocatie,
      int aantalPubliekRBeperking,
      int aantalOnrndZkBeperking,
      int aantalFiliatie,
      int aantalAantekeningRecht,
      int aantalIsbelastmetRecht,
      int aantalIsbeperkttotRecht)
      throws Exception {

    final boolean isPerceel = objectRef.endsWith("0000");

    assumeFalse(
        null == Brk2ToStagingToRsgbBrkIntegrationTest.class.getResource(bestandNaam),
        () -> "Het test bestand '" + bestandNaam + "' moet er zijn.");

    brmo.loadFromFile(
        BrmoFramework.BR_BRK2,
        Objects.requireNonNull(Brk2ToStagingToRsgbBrkIntegrationTest.class.getResource(bestandNaam))
            .getFile(),
        null);
    LOG.debug("klaar met laden van berichten in staging DB.");

    List<Bericht> berichten = brmo.listBerichten();
    List<LaadProces> processen = brmo.listLaadProcessen();
    assertNotNull(berichten, "De verzameling berichten bestaat niet.");
    assertEquals(1, berichten.size(), "Het aantal berichten is niet 1.");
    assertEquals(
        1,
        brmo.getCountLaadProcessen(BrmoFramework.BR_BRK2, "STAGING_OK"),
        "Het aantal berichten is niet 1.");
    assertNotNull(processen, "De verzameling processen bestaat niet.");
    assertEquals(1, processen.size(), "Het aantal processen is niet 1.");

    for (Bericht b : berichten) {
      assertNotNull(b, "Bericht is 'null'");
      assertNotNull(b.getBrXml(), "'br-xml' van bericht is 'null'");
    }
    assertEquals(
        objectRef,
        berichten.get(0).getObjectRef(),
        "Het bericht uit de database heeft niet de juiste objectRef.");

    LOG.debug("Transformeren berichten naar rsgb DB.");
    Thread t = brmo.toRsgb();
    t.join();

    assertEquals(
        1,
        brmo.getCountBerichten(BrmoFramework.BR_BRK2, "RSGB_OK"),
        "Niet alle berichten zijn OK getransformeerd");
    berichten = brmo.listBerichten();
    for (Bericht b : berichten) {
      assertNotNull(b, "Bericht is 'null'");
      assertNotNull(b.getDbXml(), "'db-xml' van bericht is 'null'");
    }

    ITable onroerendezaak = rsgbBrk.createDataSet().getTable("onroerendezaak");
    assertEquals(1, onroerendezaak.getRowCount(), "Er is geen (of teveel) onroerendezaak");
    assertEquals(
        objectRef,
        onroerendezaak.getValue(0, "identificatie"),
        "identificatie is niet gelijk aan objectRef");

    ITable publiekrechtelijkebeperking =
        rsgbBrk.createDataSet().getTable("publiekrechtelijkebeperking");
    assertEquals(
        aantalPubliekRBeperking,
        publiekrechtelijkebeperking.getRowCount(),
        "Aantal publiekrechtelijkebeperking klopt niet");

    ITable onroerendezaakbeperking = rsgbBrk.createDataSet().getTable("onroerendezaakbeperking");
    assertEquals(
        aantalOnrndZkBeperking,
        onroerendezaakbeperking.getRowCount(),
        "Aantal onroerendezaakbeperking klopt niet");

    ITable onroerendezaakfiliatie = rsgbBrk.createDataSet().getTable("onroerendezaakfiliatie");
    assertEquals(
        aantalFiliatie,
        onroerendezaakfiliatie.getRowCount(),
        "Aantal onroerendezaakfiliatie klopt niet");
    // check dat alle records de objectRef als "onroerendezaak" hebben
    for (int i = 0; i < onroerendezaakfiliatie.getRowCount(); i++) {
      assertEquals(
          objectRef,
          onroerendezaakfiliatie.getValue(i, "onroerendezaak"),
          "onroerendezaakfiliatie.onroerendezaak is niet gelijk aan objectRef");
    }

    ITable perceelOfAppRe;
    if (isPerceel) {
      perceelOfAppRe = rsgbBrk.createDataSet().getTable("perceel");
      assertEquals(
          objectRef,
          perceelOfAppRe.getValue(0, "identificatie"),
          "Perceel identificatie is niet gelijk aan objectRef");
      assertNotNull(
          perceelOfAppRe.getValue(0, "begrenzing_perceel"),
          "Perceel begrenzing geometrie is 'null'");
      assertNotNull(
          perceelOfAppRe.getValue(0, "plaatscoordinaten"), "Plaatscoordinaten geometrie is 'null'");
    } else {
      perceelOfAppRe = rsgbBrk.createDataSet().getTable("appartementsrecht");
      assertEquals(
          objectRef,
          perceelOfAppRe.getValue(0, "identificatie"),
          "Appartementsrecht identificatie is niet gelijk aan objectRef");
    }

    ITable recht = rsgbBrk.createDataSet().getTable("recht");
    ITable aantekeningrecht = rsgbBrk.createDataSet().getTable("recht_aantekeningrecht");
    ITable isbelastmet = rsgbBrk.createDataSet().getTable("recht_isbelastmet");
    ITable isbeperkttot = rsgbBrk.createDataSet().getTable("recht_isbeperkttot");
    assertAll(
        "rechten",
        () -> assertEquals(aantalRecht, recht.getRowCount(), "Er is geen/teveel/te weinig recht"),
        () ->
            assertEquals(
                aantalAantekeningRecht,
                aantekeningrecht.getRowCount(),
                "Er zijn geen/teveel/te weinig aantekeningrecht relaties"),
        () ->
            assertEquals(
                aantalIsbelastmetRecht,
                isbelastmet.getRowCount(),
                "Er zijn geen/teveel/te weinig isbelastmet relaties"),
        () ->
            assertEquals(
                aantalIsbeperkttotRecht,
                isbeperkttot.getRowCount(),
                "Er zijn geen/teveel/te weinig isbeperkttot relaties"));

    ITable stuk = rsgbBrk.createDataSet().getTable("stuk");
    assertEquals(aantalStuk, stuk.getRowCount(), "Er is geen/teveel/te weinig stuk");

    ITable stukdeel = rsgbBrk.createDataSet().getTable("stukdeel");
    assertEquals(
        aantalStukdeel, stukdeel.getRowCount(), "Het aantal stukdeel is niet als verwacht.");

    ITable persoon = rsgbBrk.createDataSet().getTable("persoon");
    assertEquals(
        aantalNP + aantalNNP, persoon.getRowCount(), "Het aantal persoon is niet als verwacht.");

    ITable natuurlijkpersoon = rsgbBrk.createDataSet().getTable("natuurlijkpersoon");
    assertEquals(
        aantalNP, natuurlijkpersoon.getRowCount(), "Het aantal nat.persoon is niet als verwacht.");

    ITable nietnatuurlijkpersoon = rsgbBrk.createDataSet().getTable("nietnatuurlijkpersoon");
    assertEquals(
        aantalNNP,
        nietnatuurlijkpersoon.getRowCount(),
        "Het aantal niet-nat.persoon is niet als verwacht.");

    ITable adres = rsgbBrk.createDataSet().getTable("adres");
    assertEquals(aantalAdres, adres.getRowCount(), "Het aantal adressen is niet als verwacht.");

    ITable objectlocatie = rsgbBrk.createDataSet().getTable("objectlocatie");
    assertAll(
        "objectlocatie",
        () ->
            assertEquals(
                aantalKadObjLocatie,
                objectlocatie.getRowCount(),
                "Het aantal objectlocatie is niet als verwacht."),
        () -> {
          // check dat alle records de objectRef als "heeft" hebben
          for (int i = 0; i < objectlocatie.getRowCount(); i++) {
            assertEquals(
                objectRef,
                objectlocatie.getValue(i, "heeft"),
                "objectlocatie.heeft is niet gelijk aan objectRef");
          }
        });
  }
}
