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
     * @throws org.dbunit.DatabaseUnitException
     * @throws java.sql.SQLException als er iets misgaat in de database
     */
    public static void cleanRSGB(final IDatabaseConnection rsgb) throws DatabaseUnitException, SQLException {
        /* cleanup rsgb, doet:
                DELETE FROM brondocument;
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
            new DefaultTable("herkomst_metadata"),
            new DefaultTable("brondocument")}
        ));
    }

    /**
     * leegt de bericht, laadproces en job tabellen in het statging schema. kan
     * worden gebruikt in een {@code @After} van een test case.
     *
     * @param staging database welke geleegd moet worden
     * @throws org.dbunit.DatabaseUnitException
     * @throws java.sql.SQLException als er iets misgaat in de database
     */
    public static void cleanSTAGING(final IDatabaseConnection staging) throws DatabaseUnitException, SQLException {
        DatabaseOperation.DELETE_ALL.execute(staging, new DefaultDataSet(new DefaultTable[]{
            new DefaultTable("laadproces"),
            new DefaultTable("bericht"),
            new DefaultTable("job")}));
    }

    /**
     * private by design.
     */
    private CleanUtil() {
    }
}
