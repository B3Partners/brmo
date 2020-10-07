/*
 * Copyright (C) 2018 B3Partners B.V.
 */
package nl.b3p.brmo.stufbg204;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.jupiter.api.AfterEach;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Deze testcase verwerkt een soap bericht uit een bestand als kennisgeving. Het
 * bestand bevat een HuwelijksMutatie welke naar de stufbg204 async service
 * wordt gepost, hierna wordt in de staging gechecked voor laadproces en bericht
 * en wordt na trasnformatie in de rsgb gechecked.
 *
 * @author Mark Prins
 * @code mvn -Dit.test=VerwerkToevoegingMutatieIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 * @code mvn -Dit.test=VerwerkToevoegingMutatieIntegrationTest -Dtest.onlyITs=true verify -Pmssql -pl brmo-stufbg204 > target/mssql.log}
 */
public class VerwerkToevoegingMutatieIntegrationTest extends WebTestStub {

    private static final Log LOG = LogFactory.getLog(VerwerkToevoegingMutatieIntegrationTest.class);
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;
    private final Lock sequential = new ReentrantLock();
    private BrmoFramework brmo;

    @BeforeEach
    @Override
    public void setUp() throws SQLException, BrmoException, DatabaseUnitException {
        brmo = new BrmoFramework(dsStaging, dsRsgb, null);
        // brmo.setEnablePipeline(true);

        staging = new DatabaseDataSourceConnection(dsStaging);
        rsgb = new DatabaseDataSourceConnection(dsRsgb);
        if (this.isMsSQL) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            dsStaging.getConnection().setAutoCommit(true);
            dsRsgb.getConnection().setAutoCommit(true);
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), DBPROPS.getProperty("staging.username").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
            rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()), DBPROPS.getProperty("rsgb.username").toUpperCase());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        } else {
            fail("Geen ondersteunde database aangegegeven");
        }

        sequential.lock();
    }

    @AfterEach
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();
        CleanUtil.cleanSTAGING(staging, true);
        CleanUtil.cleanRSGB_BRP(rsgb);
        staging.close();
        rsgb.close();
        sequential.unlock();
    }

    @Test
    public void testToevoeging() throws IOException, DataSetException, SQLException, BrmoException, InterruptedException, ParseException {
        doRequest("/testdata-tnt/mergetest/toevoeging.xml");
        // check staging database inhoud
        assertEquals(1l, brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRP, "STAGING_OK"), "Er zijn anders dan 1 STAGING_OK laadprocessen");

        ITable laadproces = staging.createDataSet().getTable("laadproces");
        assertEquals("2019-01-14 15:54:26.0", laadproces.getValue(0, "bestand_datum").toString(), "datum klopt niet");

        assertTrue(1l <= brmo.getCountBerichten(null, null, BrmoFramework.BR_BRP, "STAGING_OK"), "Er zijn anders dan 1 STAGING_OK berichten");
        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("NL.BRP.Persoon.2275c97ea44fceef93fde351a9ff06eeb3a1ecf3", bericht.getValue(0, "object_ref"), "object ref klopt niet");
        assertEquals("2019-01-14 15:54:26.0", bericht.getValue(0, "datum").toString(), "datum klopt niet");

        // transformeren van bericht en check rsgb database inhoud
        Thread t = brmo.toRsgb();
        t.join();

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals(1, subject.getRowCount(), "Aantal rijen klopt niet");
        assertEquals("5 568cf", subject.getValue(0, "naam"), "naam niet als verwacht");
        assertEquals("Poolmanweg 22 2767GP", subject.getValue(0, "adres_binnenland"), "adres_binnenland niet als verwacht");

        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertEquals(1, nat_prs.getRowCount(), "Aantal rijen klopt niet");
        assertEquals("M", nat_prs.getValue(0, "geslachtsaand"), "geslacht niet als verwacht");
        assertEquals("568cf", nat_prs.getValue(0, "nm_geslachtsnaam"), "geslachtsnaam niet als verwacht");

        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertEquals(1, ingeschr_nat_prs.getRowCount(), "Aantal rijen klopt niet");
        assertEquals(new BigDecimal("3056876036"), ingeschr_nat_prs.getValue(0, "a_nummer"), "a nummer niet als verwacht");
        assertEquals(new BigDecimal("902215443"), ingeschr_nat_prs.getValue(0, "bsn"), "bsn nummer niet als verwacht");
    }

    @Test
    public void testMutatieAanpassing() throws IOException, DataSetException, SQLException, BrmoException, InterruptedException, ParseException {
        doRequest("/testdata-tnt/mergetest/toevoeging.xml");
        doRequest("/testdata-tnt/mergetest/wijziging_aanpassing.xml");

        // check staging database inhoud
        assertEquals(2l, brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRP, "STAGING_OK"), "Er zijn anders dan 2 STAGING_OK laadprocessen");

        ITable laadproces = staging.createDataSet().getTable("laadproces");
        assertEquals("2019-01-14 15:54:26.0", laadproces.getValue(0, "bestand_datum").toString(), "datum klopt niet");

        assertTrue(1l <= brmo.getCountBerichten(null, null, BrmoFramework.BR_BRP, "STAGING_OK"), "Er zijn anders dan 1 STAGING_OK berichten");
        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("NL.BRP.Persoon.2275c97ea44fceef93fde351a9ff06eeb3a1ecf3", bericht.getValue(0, "object_ref"), "object ref klopt niet");
        assertEquals("2019-01-14 15:54:26.0", bericht.getValue(0, "datum").toString(), "datum klopt niet");

        // transformeren van bericht en check rsgb database inhoud
        Thread t = brmo.toRsgb();
        t.join();

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals(1, subject.getRowCount(), "Aantal rijen klopt niet");
        assertEquals("5 568cf", subject.getValue(0, "naam"), "naam niet als verwacht");
        assertEquals("Roemeenmanweg 22 2767GP", subject.getValue(0, "adres_binnenland"), "adres_binnenland niet als verwacht");

        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertEquals(1, nat_prs.getRowCount(), "Aantal rijen klopt niet");
        assertEquals("M", nat_prs.getValue(0, "geslachtsaand"), "geslacht niet als verwacht");
        assertEquals("568cf", nat_prs.getValue(0, "nm_geslachtsnaam"), "geslachtsnaam niet als verwacht");

        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertEquals(1, ingeschr_nat_prs.getRowCount(), "Aantal rijen klopt niet");
        assertEquals(new BigDecimal("3056876036"), ingeschr_nat_prs.getValue(0, "a_nummer"), "a nummer niet als verwacht");
        assertEquals(new BigDecimal("902215443"), ingeschr_nat_prs.getValue(0, "bsn"), "bsn nummer niet als verwacht");
    }

    /**
     * element geslachtsaanduiding weggehaald --> is dan onbekend bij verzender. Laten bestaan, want is op een of andere
     * manier wel in  rsgb gekomen via een andere weg en mag dus niet weg. Moet weg bij StUF:noValue="geenWaarde"
     */
    @Test
    public void testMutatieGeenElement() throws IOException, DataSetException, SQLException, BrmoException, InterruptedException, ParseException {
        doRequest("/testdata-tnt/mergetest/toevoeging.xml");
        doRequest("/testdata-tnt/mergetest/wijziging_waardeonbekend_geenelement.xml");

        // check staging database inhoud
        assertEquals(2l, brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRP, "STAGING_OK"), "Er zijn anders dan 2 STAGING_OK laadprocessen");

        ITable laadproces = staging.createDataSet().getTable("laadproces");
        assertEquals("2019-01-14 15:54:26.0", laadproces.getValue(0, "bestand_datum").toString(), "datum klopt niet");
        assertEquals("2019-01-14 16:11:52.0", laadproces.getValue(1, "bestand_datum").toString(), "datum klopt niet");

        assertEquals(3l, brmo.getCountBerichten(null, null, BrmoFramework.BR_BRP, "STAGING_OK"), "Er zijn anders dan 3 STAGING_OK berichten");
        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("NL.BRP.Persoon.2275c97ea44fceef93fde351a9ff06eeb3a1ecf3", bericht.getValue(0, "object_ref"), "object ref klopt niet");
        assertEquals("2019-01-14 15:54:26.0", bericht.getValue(0, "datum").toString(), "datum klopt niet");
        assertEquals("2019-01-14 16:11:52.0", bericht.getValue(1, "datum").toString(), "datum klopt niet");
        assertEquals("2019-01-14 16:11:52.0", bericht.getValue(2, "datum").toString(), "datum klopt niet");

        // transformeren van bericht en check rsgb database inhoud
        Thread t = brmo.toRsgb();
        t.join();

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals(1, subject.getRowCount(), "Aantal rijen klopt niet");
        assertEquals("5 568cf", subject.getValue(0, "naam"), "naam niet als verwacht");
        assertEquals("Roemeenmanweg 22 2767GP", subject.getValue(0, "adres_binnenland"), "adres_binnenland niet als verwacht");

        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertEquals(1, nat_prs.getRowCount(), "Aantal rijen klopt niet");
        assertEquals("M", nat_prs.getValue(0, "geslachtsaand"), "geslacht niet als verwacht");
        assertEquals("568cf", nat_prs.getValue(0, "nm_geslachtsnaam"), "geslachtsnaam niet als verwacht");

        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertEquals(1, ingeschr_nat_prs.getRowCount(), "Aantal rijen klopt niet");
        assertEquals(new BigDecimal("3056876036"), ingeschr_nat_prs.getValue(0, "a_nummer"), "a nummer niet als verwacht");
        assertEquals(new BigDecimal("902215443"), ingeschr_nat_prs.getValue(0, "bsn"), "bsn nummer niet als verwacht");
    }


    @Test
    public void testMutatieGeenWaarde() throws IOException, DataSetException, SQLException, BrmoException, InterruptedException, ParseException {
        doRequest("/testdata-tnt/mergetest/toevoeging.xml");
        //  assertTrue("Pijplijn moet aan staan", brmo.isEnablePipeline());
     /*  Thread t = brmo.toRsgb();
        t.join();*/

        doRequest("/testdata-tnt/mergetest/wijziging_geenWaarde.xml");

        // check staging database inhoud
        assertEquals(2l, brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRP, "STAGING_OK"), "Er zijn anders dan 1 STAGING_OK laadprocessen");

        ITable laadproces = staging.createDataSet().getTable("laadproces");
        assertEquals("2019-01-14 15:54:26.0", laadproces.getValue(0, "bestand_datum").toString(), "datum klopt niet");
        assertEquals("2019-01-14 16:11:52.0", laadproces.getValue(1, "bestand_datum").toString(), "datum klopt niet");

        assertTrue(1l <= brmo.getCountBerichten(null, null, BrmoFramework.BR_BRP, "STAGING_OK"), "Er zijn anders dan 1 STAGING_OK berichten");
        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("NL.BRP.Persoon.2275c97ea44fceef93fde351a9ff06eeb3a1ecf3", bericht.getValue(0, "object_ref"), "object ref klopt niet");
        assertEquals("2019-01-14 15:54:26.0", bericht.getValue(0, "datum").toString(), "datum klopt niet");
        assertEquals("2019-01-14 16:11:52.0", bericht.getValue(1, "datum").toString(), "datum klopt niet");
        assertEquals("2019-01-14 16:11:52.0", bericht.getValue(2, "datum").toString(), "datum klopt niet");

        // transformeren van bericht en check rsgb database inhoud
        Thread t2 = brmo.toRsgb();
        t2.join();

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals(1, subject.getRowCount(), "Aantal rijen klopt niet");
        assertEquals("5 568cf", subject.getValue(0, "naam"), "naam niet als verwacht");
        assertEquals("Roemeenmanweg 22 2767GP", subject.getValue(0, "adres_binnenland"), "adres_binnenland niet als verwacht");

        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertEquals(1, nat_prs.getRowCount(), "Aantal rijen klopt niet");
        assertNull(nat_prs.getValue(0, "geslachtsaand"), "geslacht moet leeg zijn");
        assertEquals("568cf", nat_prs.getValue(0, "nm_geslachtsnaam"), "geslachtsnaam niet als verwacht");

        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertEquals(1, ingeschr_nat_prs.getRowCount(), "Aantal rijen klopt niet");
        assertEquals(new BigDecimal("3056876036"), ingeschr_nat_prs.getValue(0, "a_nummer"), "a nummer niet als verwacht");
        assertEquals(new BigDecimal("902215443"), ingeschr_nat_prs.getValue(0, "bsn"), "bsn nummer niet als verwacht");
    }


    @Test
    public void testMutatieNietGeautoriseerd() throws IOException, DataSetException, SQLException, BrmoException, InterruptedException, ParseException {
        doRequest("/testdata-tnt/mergetest/toevoeging.xml");


        doRequest("/testdata-tnt/mergetest/wijziging_nietmeesturen_ongeauthoriseerd.xml");

        // check staging database inhoud
        assertEquals(2l, brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRP, "STAGING_OK"), "Er zijn anders dan 1 STAGING_OK laadprocessen");

        assertTrue(1l <= brmo.getCountBerichten(null, null, BrmoFramework.BR_BRP, "STAGING_OK"), "Er zijn anders dan 1 STAGING_OK berichten");
        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("NL.BRP.Persoon.2275c97ea44fceef93fde351a9ff06eeb3a1ecf3", bericht.getValue(0, "object_ref"), "object ref klopt niet");


        // transformeren van bericht en check rsgb database inhoud
        Thread t = brmo.toRsgb();
        t.join();

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals(1, subject.getRowCount(), "Aantal rijen klopt niet");
        assertEquals("5 568cf", subject.getValue(0, "naam"), "naam niet als verwacht");
        assertEquals("Roemeenmanweg 22 2767GP", subject.getValue(0, "adres_binnenland"), "adres_binnenland niet als verwacht");

        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertEquals(1, nat_prs.getRowCount(), "Aantal rijen klopt niet");
        assertEquals("M", nat_prs.getValue(0, "geslachtsaand"), "geslacht niet als verwacht");

        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertEquals(1, ingeschr_nat_prs.getRowCount(), "Aantal rijen klopt niet");
        assertEquals(new BigDecimal("3056876036"), ingeschr_nat_prs.getValue(0, "a_nummer"), "a nummer niet als verwacht");
        assertEquals(new BigDecimal("902215443"), ingeschr_nat_prs.getValue(0, "bsn"), "bsn nummer niet als verwacht");
    }


    @Test
    public void testMutatieGevuldNaarLeeg() throws IOException, DataSetException, SQLException, BrmoException, InterruptedException, ParseException {
        doRequest("/testdata-tnt/mergetest/toevoeging.xml");
        doRequest("/testdata-tnt/mergetest/wijziging_gevuldveldleeg.xml");

        // check staging database inhoud
        assertEquals(2l, brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRP, "STAGING_OK"), "Er zijn anders dan 1 STAGING_OK laadprocessen");
        assertTrue(1l <= brmo.getCountBerichten(null, null, BrmoFramework.BR_BRP, "STAGING_OK"), "Er zijn anders dan 1 STAGING_OK berichten");
        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("NL.BRP.Persoon.2275c97ea44fceef93fde351a9ff06eeb3a1ecf3", bericht.getValue(0, "object_ref"), "object ref klopt niet");

        // transformeren van bericht en check rsgb database inhoud
        Thread t = brmo.toRsgb();
        t.join();

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals(1, subject.getRowCount(), "Aantal rijen klopt niet");
        assertEquals("5 568cf", subject.getValue(0, "naam"), "naam niet als verwacht");
        assertEquals("Roemeenmanweg 22 2767GP", subject.getValue(0, "adres_binnenland"), "adres_binnenland niet als verwacht");

        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertEquals(1, nat_prs.getRowCount(), "Aantal rijen klopt niet");
        assertNull(nat_prs.getValue(0, "geslachtsaand"), "geslacht niet als verwacht");

        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertEquals(1, ingeschr_nat_prs.getRowCount(), "Aantal rijen klopt niet");
        assertEquals(new BigDecimal("3056876036"), ingeschr_nat_prs.getValue(0, "a_nummer"), "a nummer niet als verwacht");
        assertEquals(new BigDecimal("902215443"), ingeschr_nat_prs.getValue(0, "bsn"), "bsn nummer niet als verwacht");
    }


    private void doRequest(String file) throws IOException {
        InputStreamEntity reqEntity = new InputStreamEntity(
                VerwerkToevoegingMutatieIntegrationTest.class.getResourceAsStream(file),
                -1,
                ContentType.create("text/xml", "utf-8")
        );
        reqEntity.setChunked(true);
        HttpPost httppost = new HttpPost(BASE_TEST_URL + "StUFBGAsynchroon");
        httppost.setEntity(reqEntity);
        // httppost.addHeader("SOAPAction", "ontvangKennisgeving");
        LOG.debug("SOAP request uitvoeren; POST " + file + " naar: " + httppost.getRequestLine());

        // set-up preemptive auth voor request en stuur bericht
        CloseableHttpResponse response = client.execute(target, httppost, localContext);

        LOG.debug(("Request done."));
        assertThat("Response status is not OK.", response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        response.close();
    }
}
