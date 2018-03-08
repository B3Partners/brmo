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

    /**
     * leegt de BRK tabellen in het RSGB schema. kan worden gebruikt in een
     * {@code @After} van een test case.
     *
     * @param rsgb database welke geleegd moet worden
     *
     * @throws org.dbunit.DatabaseUnitException als er een DBunit fout optreedt
     * @throws java.sql.SQLException als er iets misgaat in de database
     * @deprecated gebruik {@link #cleanRSGB_BRK(org.dbunit.database.IDatabaseConnection, boolean)
     * }
     */
    @Deprecated
    public static void cleanRSGB(final IDatabaseConnection rsgb)
            throws DatabaseUnitException, SQLException {

        cleanRSGB_BRK(rsgb, true);
    }

    /**
     * leegt de BRK tabellen in het RSGB schema. kan worden gebruikt in een
     * {@code @After} van een test case.
     *
     * @param rsgb database welke geleegd moet worden
     * @param deleteBrondocument {@code true} als brondocumenten ook verwijderd
     * moeten worden
     * @throws org.dbunit.DatabaseUnitException als er een DBunit fout optreedt
     * @throws java.sql.SQLException als er iets misgaat in de database
     */
    public static void cleanRSGB_BRK(final IDatabaseConnection rsgb, final boolean deleteBrondocument)
            throws DatabaseUnitException, SQLException {

        if (deleteBrondocument) {
            DatabaseOperation.DELETE_ALL.execute(rsgb, new DefaultDataSet(new DefaultTable[]{
                new DefaultTable("brondocument")}
            ));
        }
        /* cleanup rsgb, doet:
                DELETE FROM herkomst_metadata;
                DELETE FROM zak_recht;
                DELETE FROM ingeschr_niet_nat_prs;
                ...
                DELETE FROM kad_perceel_archief;
                DELETE FROM kad_perceel;
                DELETE FROM kad_onrrnd_zk_archief;
                DELETE FROM kad_onrrnd_zk;
         dus omgekeerde volgorde tov. onderstaande array
         */
        DatabaseOperation.DELETE_ALL.execute(rsgb, new DefaultDataSet(new DefaultTable[]{
            new DefaultTable("kad_onrrnd_zk"),
            new DefaultTable("kad_onrrnd_zk_his_rel"),
            new DefaultTable("kad_onrrnd_zk_aantek"),
            new DefaultTable("kad_onrrnd_zk_archief"),
            new DefaultTable("kad_onrrnd_zk_aantek_archief"),
            new DefaultTable("kad_perceel"),
            new DefaultTable("kad_perceel_archief"),
            new DefaultTable("subject"),
            new DefaultTable("prs"),
            new DefaultTable("nat_prs"),
            new DefaultTable("ingeschr_nat_prs"),
            new DefaultTable("niet_ingezetene"),
            new DefaultTable("ander_nat_prs"),
            new DefaultTable("niet_nat_prs"),
            new DefaultTable("ingeschr_niet_nat_prs"),
            new DefaultTable("app_re"),
            new DefaultTable("app_re_archief"),
            new DefaultTable("zak_recht"),
            new DefaultTable("zak_recht_aantek"),
            new DefaultTable("benoemd_obj_kad_onrrnd_zk"),
            new DefaultTable("herkomst_metadata")}
        ));
    }

    /**
     * Leeg de subject en onderliggende tabellen die betrokken zijn bij BRP.
     *
     * @param rsgb database welke geleegd moet worden
     * @throws org.dbunit.DatabaseUnitException als er een DBunit fout optreedt
     * @throws java.sql.SQLException als er iets misgaat in de database
     */
    public static void cleanRSGB_BRP(final IDatabaseConnection rsgb) throws DatabaseUnitException, SQLException {
        CleanUtil.cleanRSGB_BRP(rsgb, true);
    }

    /**
     * Leeg de subject en onderliggende tabellen die betrokken zijn bij BRP. NB.
     * deze cleanup is niet bijzonder slim, alle (natuurlijke) personen worden
     * verwijderd, dus ook uit brk.
     *
     * @param rsgb database welke geleegd moet worden
     * @param deleteBrondocument {@code true} als brondocumenten ook verwijderd
     * moeten worden
     * @throws org.dbunit.DatabaseUnitException als er een DBunit fout optreedt
     * @throws java.sql.SQLException als er iets misgaat in de database
     */
    public static void cleanRSGB_BRP(final IDatabaseConnection rsgb, final boolean deleteBrondocument)
            throws DatabaseUnitException, SQLException {
        if (deleteBrondocument) {
            DatabaseOperation.DELETE_ALL.execute(rsgb, new DefaultDataSet(new DefaultTable[]{
                new DefaultTable("brondocument")}
            ));
        }
        /* cleanup rsgb, doet:

         DELETE FROM herkomst_metadata;
         ...
         DELETE FROM subject;
        dus omgekeerde volgorde tov. onderstaande array
         */
        DatabaseOperation.DELETE_ALL.execute(rsgb, new DefaultDataSet(new DefaultTable[]{
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
            new DefaultTable("herkomst_metadata")}
        ));

    }

    /**
     * leegt de BAG tabellen in het RSGB schema. kan worden gebruikt in een
     * {@code @After} van een test case.
     *
     * @param rsgb database welke geleegd moet worden
     * @param deleteBrondocument {@code true} als brondocumenten ook verwijderd
     * moeten worden
     *
     * @throws org.dbunit.DatabaseUnitException als er een DBunit fout optreedt
     * @throws java.sql.SQLException als er iets misgaat in de database
     */
    public static void cleanRSGB_BAG(final IDatabaseConnection rsgb, final boolean deleteBrondocument)
            throws DatabaseUnitException, SQLException {

        if (deleteBrondocument) {
            DatabaseOperation.DELETE_ALL.execute(rsgb, new DefaultDataSet(new DefaultTable[]{
                new DefaultTable("brondocument")}
            ));
        }
        // cleanup doet deletes in omgekeerde volgorde dan in onderstaande array met tabellen
        DatabaseOperation.DELETE_ALL.execute(rsgb, new DefaultDataSet(new DefaultTable[]{
            // TODO volgorde van de tabellen aanpassen aan constraints
            new DefaultTable("addresseerb_obj_aand"),
            new DefaultTable("addresseerb_obj_aand_archief"),
            new DefaultTable("benoemd_obj"),
            new DefaultTable("benoemd_terrein"),
            new DefaultTable("benoemd_terrein_archief"),
            new DefaultTable("benoemd_terrein_benoem_archief"),
            new DefaultTable("benoemd_terrein_benoemd_terrei"),
            new DefaultTable("gebouwd_obj"),
            new DefaultTable("gebouwd_obj_archief"),
            new DefaultTable("gebouwd_obj_gebruiksdoel"),
            new DefaultTable("gem_openb_rmte"),
            new DefaultTable("gem_openb_rmte_archief"),
            new DefaultTable("ligplaats"),
            new DefaultTable("ligplaats_archief"),
            new DefaultTable("ligplaats_nummeraand"),
            new DefaultTable("ligplaats_nummeraand_archief"),
            new DefaultTable("nummeraand"),
            new DefaultTable("nummeraand_archief"),
            new DefaultTable("openb_rmte"),
            new DefaultTable("openb_rmte_gem_openb_rmte"),
            new DefaultTable("openb_rmte_wnplts"),
            new DefaultTable("overig_bouwwerk"),
            new DefaultTable("overig_bouwwerk_archief"),
            new DefaultTable("overig_gebouwd_obj"),
            new DefaultTable("overig_gebouwd_obj_archief"),
            new DefaultTable("overig_terrein"),
            new DefaultTable("overig_terrein_archief"),
            new DefaultTable("overig_terrein_gebruiksdoel"),
            new DefaultTable("ovrg_addresseerb_obj_a_archief"),
            new DefaultTable("ovrg_addresseerb_obj_aand"),
            new DefaultTable("pand"),
            new DefaultTable("pand_archief"),
            new DefaultTable("standplaats"),
            new DefaultTable("standplaats_archief"),
            new DefaultTable("standplaats_nummeraand"),
            new DefaultTable("standplaats_nummeraand_archief"),
            new DefaultTable("verblijfsobj"),
            new DefaultTable("verblijfsobj_archief"),
            new DefaultTable("verblijfsobj_nummeraan_archief"),
            new DefaultTable("verblijfsobj_nummeraand"),
            new DefaultTable("verblijfsobj_pand"),
            new DefaultTable("verblijfsobj_pand_archief"),
            new DefaultTable("wnplts"),
            new DefaultTable("wnplts_archief")}
        ));
    }

    /**
     * leegt de bericht, laadproces en job tabellen in het stating schema. kan
     * worden gebruikt in een {@code @After} van een test case.
     *
     * @param staging database welke geleegd moet worden
     * @throws org.dbunit.DatabaseUnitException als er een DBunit fout optreedt
     * @throws java.sql.SQLException als er iets misgaat in de database
     */
    public static void cleanSTAGING(final IDatabaseConnection staging) throws DatabaseUnitException, SQLException {
        DatabaseOperation.DELETE_ALL.execute(staging, new DefaultDataSet(new DefaultTable[]{
            new DefaultTable("laadproces"),
            new DefaultTable("bericht"),
            new DefaultTable("job")}
        ));
    }

    /**
     * private by design.
     */
    private CleanUtil() {
    }
}
