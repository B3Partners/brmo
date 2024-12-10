/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.test.util.database.dbunit;

import java.sql.SQLException;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.operation.DatabaseOperation;

/**
 * DBunit utility om databases leeg te maken.
 *
 * @author mprins
 */
public final class CleanUtil {

  /** private by design. */
  private CleanUtil() {}

  /**
   * leegt de BRK 2 tabellen in het BRK schema. kan worden gebruikt in een {@code @After} van een
   * test case.
   *
   * @param rsgbbrk database welke geleegd moet worden moeten worden
   * @throws org.dbunit.DatabaseUnitException als er een DBunit fout optreedt
   * @throws java.sql.SQLException als er iets misgaat in de database
   */
  public static void cleanRSGB_BRK2(final IDatabaseConnection rsgbbrk)
      throws DatabaseUnitException, SQLException {

    DatabaseOperation.DELETE_ALL.execute(
        rsgbbrk,
        new DefaultDataSet(
            new DefaultTable[] {
              // TODO mogelijk de volgorde nog aanpassen
              new DefaultTable("onroerendezaak"),
              new DefaultTable("nietnatuurlijkpersoon"),
              new DefaultTable("adres"),
              new DefaultTable("natuurlijkpersoon"),
              new DefaultTable("stuk"),
              new DefaultTable("stukdeel"),
              new DefaultTable("persoon"),
              new DefaultTable("recht"),
              new DefaultTable("publiekrechtelijkebeperking"),
              new DefaultTable("onroerendezaakbeperking_archief"),
              new DefaultTable("onroerendezaakfiliatie"),
              new DefaultTable("recht_isbeperkttot"),
              new DefaultTable("objectlocatie"),
              new DefaultTable("onroerendezaakbeperking"),
              new DefaultTable("recht_aantekeningrecht"),
              new DefaultTable("recht_archief"),
              new DefaultTable("recht_isbelastmet"),
              new DefaultTable("appartementsrecht"),
              new DefaultTable("appartementsrecht_archief"),
              new DefaultTable("objectlocatie_archief"),
              new DefaultTable("onroerendezaak_archief"),
              new DefaultTable("perceel"),
              new DefaultTable("perceel_archief"),
              new DefaultTable("recht_aantekeningrecht_archief"),
              new DefaultTable("recht_isbelastmet_archief"),
              new DefaultTable("recht_isbeperkttot_archief"),
            }));
  }

  /**
   * Leeg de subject en onderliggende tabellen die betrokken zijn bij BRP.
   *
   * @param rsgb database welke geleegd moet worden
   * @throws org.dbunit.DatabaseUnitException als er een DBunit fout optreedt
   * @throws java.sql.SQLException als er iets misgaat in de database
   */
  public static void cleanRSGB_BRP(final IDatabaseConnection rsgb)
      throws DatabaseUnitException, SQLException {
    CleanUtil.cleanRSGB_BRP(rsgb, true);
  }

  /**
   * Leeg de subject en onderliggende tabellen die betrokken zijn bij BRP. NB. deze cleanup is niet
   * bijzonder slim, alle (natuurlijke) personen worden verwijderd, dus ook uit brk.
   *
   * @param rsgb database welke geleegd moet worden
   * @param deleteBrondocument {@code true} als brondocumenten ook verwijderd moeten worden
   * @throws org.dbunit.DatabaseUnitException als er een DBunit fout optreedt
   * @throws java.sql.SQLException als er iets misgaat in de database
   */
  public static void cleanRSGB_BRP(final IDatabaseConnection rsgb, final boolean deleteBrondocument)
      throws DatabaseUnitException, SQLException {
    if (deleteBrondocument) {
      DatabaseOperation.DELETE_ALL.execute(
          rsgb, new DefaultDataSet(new DefaultTable[] {new DefaultTable("brondocument")}));
    }
    /* cleanup rsgb, doet:

     DELETE FROM herkomst_metadata;
     ...
     DELETE FROM subject;
    dus omgekeerde volgorde tov. onderstaande array
     */
    DatabaseOperation.DELETE_ALL.execute(
        rsgb,
        new DefaultDataSet(
            new DefaultTable[] {
              new DefaultTable("subject"),
              new DefaultTable("prs"),
              new DefaultTable("nat_prs"),
              new DefaultTable("ingeschr_nat_prs"),
              new DefaultTable("niet_ingezetene"),
              new DefaultTable("ander_nat_prs"),
              new DefaultTable("niet_nat_prs"),
              new DefaultTable("ingeschr_niet_nat_prs"),
              new DefaultTable("ouder_kind_rel"),
              new DefaultTable("huw_ger_partn"),
              new DefaultTable("herkomst_metadata")
            }));
  }

  /**
   * ruimt personen en kvk tabellen op.
   *
   * @param rsgb database welke opgeruimd moet worden
   * @throws DatabaseUnitException als er een DBunit fout optreedt
   * @throws SQLException als er iets misgaat in de database
   */
  public static void cleanRSGB_NHR(final IDatabaseConnection rsgb)
      throws DatabaseUnitException, SQLException {
    cleanRSGB_NHR(rsgb, true);
  }

  /**
   * ruimt personen en kvk tabellen op (en ook BRP tabellen).
   *
   * @param rsgb database welke opgeruimd moet worden
   * @param deleteBrondocument {@code true} als brondocumenten ook verwijderd
   * @throws DatabaseUnitException als er iets mis gaat met DBunit, bijv verkeerde volgorde van
   *     verwijderen
   * @throws SQLException als er iets mis gaat met uitvieren van de deletes
   */
  public static void cleanRSGB_NHR(final IDatabaseConnection rsgb, final boolean deleteBrondocument)
      throws DatabaseUnitException, SQLException {

    DatabaseOperation.DELETE_ALL.execute(
        rsgb,
        new DefaultDataSet(
            new DefaultTable[] {
              new DefaultTable("sbi_activiteit"),
              new DefaultTable("functionaris"),
              new DefaultTable("ondrnmng"),
              new DefaultTable("maatschapp_activiteit"),
              new DefaultTable("vestg"),
              new DefaultTable("vestg_activiteit"),
              new DefaultTable("vestg_naam"),
              new DefaultTable("ander_btnlnds_niet_nat_prs"),
            }));
    cleanRSGB_BRP(rsgb, deleteBrondocument);
  }

  /**
   * ruimt WOZ en subject tabellen op (dus ook BRP tabellen).
   *
   * @param rsgb database welke opgeruimd moet worden
   * @param deleteBrondocument {@code true} als alle brondocumenten ook verwijderd moeten worden
   * @throws DatabaseUnitException als er iets mis gaat met DBunit, bijv verkeerde volgorde van
   *     verwijderen
   * @throws SQLException als er iets mis gaat met uitvieren van de deletes
   */
  public static void cleanRSGB_WOZ(final IDatabaseConnection rsgb, final boolean deleteBrondocument)
      throws DatabaseUnitException, SQLException {

    DatabaseOperation.DELETE_ALL.execute(
        rsgb,
        new DefaultDataSet(
            new DefaultTable[] {
              new DefaultTable("woz_obj"),
              new DefaultTable("woz_deelobj"),
              new DefaultTable("woz_waarde"),
              new DefaultTable("woz_omvat"),
              new DefaultTable("woz_belang"),
              new DefaultTable("woz_deelobj_archief"),
              new DefaultTable("woz_obj_archief"),
              new DefaultTable("woz_waarde_archief"),
              new DefaultTable("locaand_adres"),
              new DefaultTable("locaand_openb_rmte"),
            }));
    // WOZ bevat ook vestigingen, NHR leegt ook BRP
    cleanRSGB_NHR(rsgb, deleteBrondocument);
  }

  /**
   * leegt de bericht, laadproces en job tabellen en de automatsiche processen in het staging
   * schema. kan worden gebruikt in een {@code @After} van een test case.
   *
   * @param staging database welke geleegd moet worden
   * @param includeProcessen {@code true} als alle automatische processen ook verwijderd moeten
   *     worden
   * @throws org.dbunit.DatabaseUnitException als er een DBunit fout optreedt
   * @throws java.sql.SQLException als er iets misgaat in de database
   */
  public static void cleanSTAGING(final IDatabaseConnection staging, final boolean includeProcessen)
      throws DatabaseUnitException, SQLException {
    if (includeProcessen) {
      DatabaseOperation.DELETE_ALL.execute(
          staging,
          new DefaultDataSet(
              new DefaultTable[] {
                new DefaultTable("automatisch_proces"),
                new DefaultTable("automatisch_proces_config")
              }));
    }
    DatabaseOperation.DELETE_ALL.execute(
        staging,
        new DefaultDataSet(
            new DefaultTable[] {
              new DefaultTable("laadproces"), new DefaultTable("bericht"), new DefaultTable("job")
            }));
  }
}
