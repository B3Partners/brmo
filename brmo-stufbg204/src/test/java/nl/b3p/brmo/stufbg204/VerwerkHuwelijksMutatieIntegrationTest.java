/*
 * Copyright (C) 2018 B3Partners B.V.
 */
package nl.b3p.brmo.stufbg204;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.stufbg204.util.StUFbg204Util;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

/**
 * Deze testcase verwerkt een soap bericht uit een bestand als kennisgeving. Het
 * bestand bevat een HuwelijksMutatie welke naar de stufbg204 async service
 * wordt gepost, hierna wordt in de staging gechecked voor laadproces en bericht
 * en wordt na trasformatie in de rsgb gechecked.
 * @code mvn -Dit.test=VerwerkHuwelijksMutatieIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 *
 * @author Mark Prins
 */
public class VerwerkHuwelijksMutatieIntegrationTest extends WebTestStub {

    private static final Log LOG = LogFactory.getLog(VerwerkHuwelijksMutatieIntegrationTest.class);
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

    @Test
    public void testBericht() throws IOException, DataSetException, SQLException, BrmoException, InterruptedException, ParseException {
        InputStreamEntity reqEntity = new InputStreamEntity(
                VerwerkHuwelijksMutatieIntegrationTest.class.getResourceAsStream("/OpnemenHuwelijkScenario/StUF_request.xml"),
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

        assertThat("Response status is OK.", response.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_OK));

        // check staging database inhoud
        assertTrue("Er zijn anders dan 1 STAGING_OK laadprocessen", 1l <= brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));

        ITable laadproces = staging.createDataSet().getTable("laadproces");
        assertEquals("datum klopt niet", /* StUFbg204Util.sdf.parse("201812211418450000") */ "2018-12-21 14:18:45.0", laadproces.getValue(0, "bestand_datum").toString());

        assertTrue("Er zijn anders dan 1 STAGING_OK berichten", 1l <= brmo.getCountBerichten(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));
        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("object ref klopt niet", "df2e41b72f8a3421ef575617fc247a77018a573f", bericht.getValue(0, "object_ref"));
        assertEquals("datum klopt niet", "2018-12-21 14:18:45.0", bericht.getValue(0, "datum").toString());

        // transformeren van bericht en check rsgb database inhoud
        Thread t = brmo.toRsgb();
        t.join();

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertTrue("Aantal rijen klopt niet", subject.getRowCount() == 1);
        assertEquals("naam niet als verwacht", "J de Cuykelaer", subject.getValue(0, "naam"));

        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertTrue("Aantal rijen klopt niet", nat_prs.getRowCount() == 1);
        assertEquals("geslacht niet als verwacht", "M", nat_prs.getValue(1, "geslachtsaand"));
        assertEquals("geslachtsnaam niet als verwacht", "Cuykelaer", nat_prs.getValue(0, "nm_geslachtsnaam"));

        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertTrue("Aantal rijen klopt niet", ingeschr_nat_prs.getRowCount() == 1);
        assertEquals("a nummer niet als verwacht", "9173658014", ingeschr_nat_prs.getValue(0, "a_nummer"));
        assertEquals("bsn nummer niet als verwacht", "301571818", ingeschr_nat_prs.getValue(0, "bsn"));
    }
}
