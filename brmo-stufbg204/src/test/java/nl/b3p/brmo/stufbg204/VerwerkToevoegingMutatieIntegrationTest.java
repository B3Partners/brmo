/*
 * Copyright (C) 2018 B3Partners B.V.
 */
package nl.b3p.brmo.stufbg204;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Deze testcase verwerkt een soap bericht uit een bestand als kennisgeving. Het
 * bestand bevat een HuwelijksMutatie welke naar de stufbg204 async service
 * wordt gepost, hierna wordt in de staging gechecked voor laadproces en bericht
 * en wordt na trasnformatie in de rsgb gechecked.
 * @code mvn -Dit.test=VerwerkToevoegingMutatieIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 *
 * @author Mark Prins
 */
public class VerwerkToevoegingMutatieIntegrationTest extends WebTestStub {

    private static final Log LOG = LogFactory.getLog(VerwerkToevoegingMutatieIntegrationTest.class);
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;
    private final Lock sequential = new ReentrantLock();
    private BrmoFramework brmo;

    @Before
    @Override
    public void setUp() throws SQLException, BrmoException, DatabaseUnitException {
        BasicDataSource dsStaging = new BasicDataSource();
        dsStaging.setUrl(DBPROPS.getProperty("staging.url"));
        dsStaging.setUsername(DBPROPS.getProperty("staging.username"));
        dsStaging.setPassword(DBPROPS.getProperty("staging.password"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);

        BasicDataSource dsRsgb = new BasicDataSource();
        dsRsgb.setUrl(DBPROPS.getProperty("rsgb.url"));
        dsRsgb.setUsername(DBPROPS.getProperty("rsgb.username"));
        dsRsgb.setPassword(DBPROPS.getProperty("rsgb.password"));
        dsRsgb.setAccessToUnderlyingConnectionAllowed(true);

        setupJNDI(dsRsgb, dsStaging);

        brmo = new BrmoFramework(dsStaging, dsRsgb, null);
        staging = new DatabaseDataSourceConnection(dsStaging);
        rsgb = new DatabaseDataSourceConnection(dsRsgb);

        sequential.lock();
        CleanUtil.cleanSTAGING(staging);
        CleanUtil.cleanRSGB_BRP(rsgb);
    }

    @After
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();
//        CleanUtil.cleanSTAGING(staging);
//        CleanUtil.cleanRSGB_BRP(rsgb);
        staging.close();
        rsgb.close();
        sequential.unlock();
    }

    //@Test
    public void testBericht() throws IOException, DataSetException, SQLException, BrmoException, InterruptedException, ParseException {
        doRequest("/testdata-tnt/SoapBerichten/toevoeging_huwelijk_als_comfort.xml");

        // check staging database inhoud
        assertTrue("Er zijn anders dan 1 STAGING_OK laadprocessen", 1l == brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));

        ITable laadproces = staging.createDataSet().getTable("laadproces");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SS");
        //assertEquals("datum klopt niet", /* StUFbg204Util.sdf.parse("201812211418450000") */ sdf.format(new Date())  /*"2019-01-14 16:04:37.0"*/, laadproces.getValue(0, "bestand_datum").toString());

        assertTrue("Er zijn anders dan 1 STAGING_OK berichten", 1l <= brmo.getCountBerichten(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));
        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("object ref klopt niet", "NL.BRP.Persoon.fcd17242d8211342df48efd67341ca9765de8347", bericht.getValue(0, "object_ref"));
      //  assertEquals("datum klopt niet", "2019-01-14 16:04:37.0", bericht.getValue(0, "datum").toString());

        // transformeren van bericht en check rsgb database inhoud
        Thread t = brmo.toRsgb();
        t.join();

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals("Aantal rijen klopt niet", 5, subject.getRowCount());
        assertEquals("naam niet als verwacht", "PXXX", subject.getValue(0, "naam"));

        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertEquals("Aantal rijen klopt niet", 5, nat_prs.getRowCount() );
        assertEquals("geslacht niet als verwacht", "V", nat_prs.getValue(0, "geslachtsaand"));
       // assertEquals("geslachtsnaam niet als verwacht", "Cuykelaer", nat_prs.getValue(0, "nm_geslachtsnaam"));

        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertEquals("Aantal rijen klopt niet", 5, ingeschr_nat_prs.getRowCount() );
        assertEquals("a nummer niet als verwacht", null, ingeschr_nat_prs.getValue(0, "a_nummer"));
        assertEquals("bsn nummer niet als verwacht", "66600666", ingeschr_nat_prs.getValue(0, "bsn"));
    }
    
    @Test
    public void testToevoeging() throws IOException, DataSetException, SQLException, BrmoException, InterruptedException, ParseException {
        doRequest("/testdata-tnt/mergetest/toevoeging.xml");
        // check staging database inhoud
        assertTrue("Er zijn anders dan 1 STAGING_OK laadprocessen", 1l == brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));

        ITable laadproces = staging.createDataSet().getTable("laadproces");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SS");
        //assertEquals("datum klopt niet", /* StUFbg204Util.sdf.parse("201812211418450000") */ sdf.format(new Date())  /*"2019-01-14 16:04:37.0"*/, laadproces.getValue(0, "bestand_datum").toString());

        assertTrue("Er zijn anders dan 1 STAGING_OK berichten", 1l <= brmo.getCountBerichten(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));
        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("object ref klopt niet", "NL.BRP.Persoon.2275c97ea44fceef93fde351a9ff06eeb3a1ecf3", bericht.getValue(0, "object_ref"));
      //  assertEquals("datum klopt niet", "2019-01-14 16:04:37.0", bericht.getValue(0, "datum").toString());

        // transformeren van bericht en check rsgb database inhoud
        Thread t = brmo.toRsgb();
        t.join();

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals("Aantal rijen klopt niet", 1, subject.getRowCount());
        assertEquals("naam niet als verwacht", "5 568cf", subject.getValue(0, "naam"));
        assertEquals("adres_binnenland niet als verwacht", "Poolmanweg 22 2767GP", subject.getValue(0, "adres_binnenland"));

        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertEquals("Aantal rijen klopt niet", 1, nat_prs.getRowCount() );
        assertEquals("geslacht niet als verwacht", "M", nat_prs.getValue(0, "geslachtsaand"));
       // assertEquals("geslachtsnaam niet als verwacht", "Cuykelaer", nat_prs.getValue(0, "nm_geslachtsnaam"));

        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertEquals("Aantal rijen klopt niet", 1, ingeschr_nat_prs.getRowCount() );
        assertEquals("a nummer niet als verwacht", new BigDecimal("3056876036"), ingeschr_nat_prs.getValue(0, "a_nummer"));
        assertEquals("bsn nummer niet als verwacht", new BigDecimal("902215443"), ingeschr_nat_prs.getValue(0, "bsn"));
    }
    
    @Test
    public void testMutatieAanpassing() throws IOException, DataSetException, SQLException, BrmoException, InterruptedException, ParseException {
        doRequest("/testdata-tnt/mergetest/toevoeging.xml");
        
        Thread t = brmo.toRsgb();
        t.join();

        doRequest("/testdata-tnt/mergetest/wijziging_aanpassing.xml");

        // check staging database inhoud
        assertTrue("Er zijn anders dan 1 STAGING_OK laadprocessen", 2l == brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));

        ITable laadproces = staging.createDataSet().getTable("laadproces");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SS");
        //assertEquals("datum klopt niet", /* StUFbg204Util.sdf.parse("201812211418450000") */ sdf.format(new Date())  /*"2019-01-14 16:04:37.0"*/, laadproces.getValue(0, "bestand_datum").toString());

        assertTrue("Er zijn anders dan 1 STAGING_OK berichten", 1l <= brmo.getCountBerichten(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));
        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("object ref klopt niet", "NL.BRP.Persoon.2275c97ea44fceef93fde351a9ff06eeb3a1ecf3", bericht.getValue(0, "object_ref"));
      //  assertEquals("datum klopt niet", "2019-01-14 16:04:37.0", bericht.getValue(0, "datum").toString());

        // transformeren van bericht en check rsgb database inhoud
        t = brmo.toRsgb();
        t.join();

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals("Aantal rijen klopt niet", 1, subject.getRowCount());
        assertEquals("naam niet als verwacht", "5 568cf", subject.getValue(0, "naam"));
        assertEquals("adres_binnenland niet als verwacht", "Roemeenmanweg 22 2767GP", subject.getValue(0, "adres_binnenland"));

        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertEquals("Aantal rijen klopt niet", 1, nat_prs.getRowCount() );
        assertEquals("geslacht niet als verwacht", "M", nat_prs.getValue(0, "geslachtsaand"));
       // assertEquals("geslachtsnaam niet als verwacht", "Cuykelaer", nat_prs.getValue(0, "nm_geslachtsnaam"));

        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertEquals("Aantal rijen klopt niet", 1, ingeschr_nat_prs.getRowCount() );
        assertEquals("a nummer niet als verwacht", new BigDecimal("3056876036"), ingeschr_nat_prs.getValue(0, "a_nummer"));
        assertEquals("bsn nummer niet als verwacht", new BigDecimal("902215443"), ingeschr_nat_prs.getValue(0, "bsn"));
    }
    
    /*
    element geslachtsaanduiding weggehaald --> is dan onbekend bij verzender. Laten bestaan, want is op een of andere manier wel in rsgb gekomen via iets anders
    en mag dus niet weg. Moet weg bij StUF:noValue="geenWaarde"
    */
    @Test
    public void testMutatieGeenElement() throws IOException, DataSetException, SQLException, BrmoException, InterruptedException, ParseException {
        doRequest("/testdata-tnt/mergetest/toevoeging.xml");
        
        Thread t = brmo.toRsgb();
        t.join();

        doRequest("/testdata-tnt/mergetest/wijziging_waardeonbekend_geenelement.xml");

        // check staging database inhoud
        assertTrue("Er zijn anders dan 1 STAGING_OK laadprocessen", 2l == brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));

        ITable laadproces = staging.createDataSet().getTable("laadproces");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SS");
        //assertEquals("datum klopt niet", /* StUFbg204Util.sdf.parse("201812211418450000") */ sdf.format(new Date())  /*"2019-01-14 16:04:37.0"*/, laadproces.getValue(0, "bestand_datum").toString());

        assertTrue("Er zijn anders dan 1 STAGING_OK berichten", 1l <= brmo.getCountBerichten(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));
        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("object ref klopt niet", "NL.BRP.Persoon.2275c97ea44fceef93fde351a9ff06eeb3a1ecf3", bericht.getValue(0, "object_ref"));
      //  assertEquals("datum klopt niet", "2019-01-14 16:04:37.0", bericht.getValue(0, "datum").toString());

        // transformeren van bericht en check rsgb database inhoud
        t = brmo.toRsgb();
        t.join();

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals("Aantal rijen klopt niet", 1, subject.getRowCount());
        assertEquals("naam niet als verwacht", "5 568cf", subject.getValue(0, "naam"));
        assertEquals("adres_binnenland niet als verwacht", "Roemeenmanweg 22 2767GP", subject.getValue(0, "adres_binnenland"));

        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertEquals("Aantal rijen klopt niet", 1, nat_prs.getRowCount() );
        assertEquals("geslacht niet als verwacht", "M", nat_prs.getValue(0, "geslachtsaand"));
       // assertEquals("geslachtsnaam niet als verwacht", "Cuykelaer", nat_prs.getValue(0, "nm_geslachtsnaam"));

        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertEquals("Aantal rijen klopt niet", 1, ingeschr_nat_prs.getRowCount() );
        assertEquals("a nummer niet als verwacht", new BigDecimal("3056876036"), ingeschr_nat_prs.getValue(0, "a_nummer"));
        assertEquals("bsn nummer niet als verwacht", new BigDecimal("902215443"), ingeschr_nat_prs.getValue(0, "bsn"));
    }
    
    
    @Test
    public void testMutatieGeenWaarde() throws IOException, DataSetException, SQLException, BrmoException, InterruptedException, ParseException {
        doRequest("/testdata-tnt/mergetest/toevoeging.xml");
      
        Thread t = brmo.toRsgb();
        t.join();

        doRequest("/testdata-tnt/mergetest/wijziging_geenWaarde.xml");

        // check staging database inhoud
        assertTrue("Er zijn anders dan 1 STAGING_OK laadprocessen", 2l == brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));

        ITable laadproces = staging.createDataSet().getTable("laadproces");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SS");
        //assertEquals("datum klopt niet", /* StUFbg204Util.sdf.parse("201812211418450000") */ sdf.format(new Date())  /*"2019-01-14 16:04:37.0"*/, laadproces.getValue(0, "bestand_datum").toString());

        assertTrue("Er zijn anders dan 1 STAGING_OK berichten", 1l <= brmo.getCountBerichten(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));
        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("object ref klopt niet", "NL.BRP.Persoon.2275c97ea44fceef93fde351a9ff06eeb3a1ecf3", bericht.getValue(0, "object_ref"));
      //  assertEquals("datum klopt niet", "2019-01-14 16:04:37.0", bericht.getValue(0, "datum").toString());

        // transformeren van bericht en check rsgb database inhoud
        Thread t2 = brmo.toRsgb();
        t2.join();

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals("Aantal rijen klopt niet", 1, subject.getRowCount());
      //  assertEquals("naam niet als verwacht", "5568cf", subject.getValue(0, "naam"));
        //assertEquals("adres_binnenland niet als verwacht", "Roemeenmanweg 22 2767GP", subject.getValue(0, "adres_binnenland"));

        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertEquals("Aantal rijen klopt niet", 1, nat_prs.getRowCount() );
        assertNull("geslacht moet leeg zijn", nat_prs.getValue(0, "geslachtsaand"));
       // assertEquals("geslachtsnaam niet als verwacht", "Cuykelaer", nat_prs.getValue(0, "nm_geslachtsnaam"));

        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertEquals("Aantal rijen klopt niet", 1, ingeschr_nat_prs.getRowCount() );
        assertEquals("a nummer niet als verwacht", new BigDecimal("3056876036"), ingeschr_nat_prs.getValue(0, "a_nummer"));
        assertEquals("bsn nummer niet als verwacht", new BigDecimal("902215443"), ingeschr_nat_prs.getValue(0, "bsn"));
    }
    
    
    @Test
    public void testMutatieNietGeautoriseerd() throws IOException, DataSetException, SQLException, BrmoException, InterruptedException, ParseException {
        doRequest("/testdata-tnt/mergetest/toevoeging.xml");
        
        Thread t = brmo.toRsgb();
        t.join();

        doRequest("/testdata-tnt/mergetest/wijziging_nietmeesturen_ongeauthoriseerd.xml");

        // check staging database inhoud
        assertTrue("Er zijn anders dan 1 STAGING_OK laadprocessen", 2l == brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));

        ITable laadproces = staging.createDataSet().getTable("laadproces");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SS");
        //assertEquals("datum klopt niet", /* StUFbg204Util.sdf.parse("201812211418450000") */ sdf.format(new Date())  /*"2019-01-14 16:04:37.0"*/, laadproces.getValue(0, "bestand_datum").toString());

        assertTrue("Er zijn anders dan 1 STAGING_OK berichten", 1l <= brmo.getCountBerichten(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));
        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("object ref klopt niet", "NL.BRP.Persoon.2275c97ea44fceef93fde351a9ff06eeb3a1ecf3", bericht.getValue(0, "object_ref"));
      //  assertEquals("datum klopt niet", "2019-01-14 16:04:37.0", bericht.getValue(0, "datum").toString());

        // transformeren van bericht en check rsgb database inhoud
        t = brmo.toRsgb();
        t.join();

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals("Aantal rijen klopt niet", 1, subject.getRowCount());
        assertEquals("naam niet als verwacht", "5 568cf", subject.getValue(0, "naam"));
        assertEquals("adres_binnenland niet als verwacht", "Roemeenmanweg 22 2767GP", subject.getValue(0, "adres_binnenland"));

        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertEquals("Aantal rijen klopt niet", 1, nat_prs.getRowCount() );
        assertEquals("geslacht niet als verwacht", "M", nat_prs.getValue(0, "geslachtsaand"));
       // assertEquals("geslachtsnaam niet als verwacht", "Cuykelaer", nat_prs.getValue(0, "nm_geslachtsnaam"));

        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertEquals("Aantal rijen klopt niet", 1, ingeschr_nat_prs.getRowCount() );
        assertEquals("a nummer niet als verwacht", new BigDecimal("3056876036"), ingeschr_nat_prs.getValue(0, "a_nummer"));
        assertEquals("bsn nummer niet als verwacht", new BigDecimal("902215443"), ingeschr_nat_prs.getValue(0, "bsn"));
    }
    
    
    @Test
    public void testMutatieGevuldNaarLeeg() throws IOException, DataSetException, SQLException, BrmoException, InterruptedException, ParseException {
        doRequest("/testdata-tnt/mergetest/toevoeging.xml");

        Thread t = brmo.toRsgb();
        t.join();

        doRequest("/testdata-tnt/mergetest/wijziging_gevuldveldleeg.xml");

        // check staging database inhoud
        assertTrue("Er zijn anders dan 1 STAGING_OK laadprocessen", 2l == brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));

        ITable laadproces = staging.createDataSet().getTable("laadproces");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SS");
        //assertEquals("datum klopt niet", /* StUFbg204Util.sdf.parse("201812211418450000") */ sdf.format(new Date())  /*"2019-01-14 16:04:37.0"*/, laadproces.getValue(0, "bestand_datum").toString());

        assertTrue("Er zijn anders dan 1 STAGING_OK berichten", 1l <= brmo.getCountBerichten(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));
        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("object ref klopt niet", "NL.BRP.Persoon.2275c97ea44fceef93fde351a9ff06eeb3a1ecf3", bericht.getValue(0, "object_ref"));
      //  assertEquals("datum klopt niet", "2019-01-14 16:04:37.0", bericht.getValue(0, "datum").toString());

        // transformeren van bericht en check rsgb database inhoud
        t = brmo.toRsgb();
        t.join();

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals("Aantal rijen klopt niet", 1, subject.getRowCount());
        assertEquals("naam niet als verwacht", "5 568cf", subject.getValue(0, "naam"));
        assertEquals("adres_binnenland niet als verwacht", "Roemeenmanweg 22 2767GP", subject.getValue(0, "adres_binnenland"));

        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertEquals("Aantal rijen klopt niet", 1, nat_prs.getRowCount() );
        assertNull("geslacht niet als verwacht", nat_prs.getValue(0, "geslachtsaand"));
       // assertEquals("geslachtsnaam niet als verwacht", "Cuykelaer", nat_prs.getValue(0, "nm_geslachtsnaam"));

        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertEquals("Aantal rijen klopt niet", 1, ingeschr_nat_prs.getRowCount() );
        assertEquals("a nummer niet als verwacht", new BigDecimal("3056876036"), ingeschr_nat_prs.getValue(0, "a_nummer"));
        assertEquals("bsn nummer niet als verwacht", new BigDecimal("902215443"), ingeschr_nat_prs.getValue(0, "bsn"));
    }
    
    
    private void doRequest(String file) throws IOException{
           InputStreamEntity reqEntity = new InputStreamEntity(
                VerwerkToevoegingMutatieIntegrationTest.class.getResourceAsStream(file),
                -1,
                ContentType.create("text/xml", "utf-8")
        );
        reqEntity.setChunked(true);
        HttpPost httppost = new HttpPost(BASE_TEST_URL + "StUFBGAsynchroon");
        httppost.setEntity(reqEntity);
        // httppost.addHeader("SOAPAction", "ontvangKennisgeving");
        LOG.debug("SOAP request uitvoeren: " + httppost.getRequestLine());

        // set-up preemptive auth voor request en stuur bericht
        CloseableHttpResponse response = client.execute(target, httppost, localContext);
        
        LOG.debug(("Request done."));
        assertThat("Response status is not OK.", response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        response.close();
    }
}
