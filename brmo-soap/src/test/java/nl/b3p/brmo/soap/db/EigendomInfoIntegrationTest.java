/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.soap.db;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import nl.b3p.brmo.soap.eigendom.Document;
import nl.b3p.brmo.soap.eigendom.EigendomMutatie;
import nl.b3p.brmo.soap.eigendom.EigendomMutatieRequest;
import nl.b3p.brmo.soap.eigendom.EigendomMutatieResponse;
import nl.b3p.brmo.soap.eigendom.MutatieEntry;
import nl.b3p.brmo.soap.eigendom.MutatieListRequest;
import nl.b3p.brmo.soap.eigendom.MutatieListResponse;
import nl.b3p.brmo.test.util.database.JTDSDriverBasedFailures;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * Draaien met:
 * {@code mvn -Dit.test=EigendomInfoIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 * voor bijvoorbeeld PostgreSQL.
 *
 * <strong>Werkt niet met MS SQl server omdat de JTDS een
 * {@code java.lang.AbstractMethodError} opwerpt op aanroep van
 * {@code JtdsConnection.isValid(..)}</strong>
 *
 * @author mprins
 */
@RunWith(Parameterized.class)
@Category(JTDSDriverBasedFailures.class)
public class EigendomInfoIntegrationTest extends TestUtil {

    @Parameterized.Parameters(name = "{index}: verwerken bestand: {0}")
    public static Collection params() {
        return Arrays.asList(new Object[][]{
            // {"rBestandsNaam", "objNamespace", "kadId", aantalBronDoc, "sBestandsNaam", "volgordeNummer"},
            {"/rsgb.xml", "NL.KAD.OnroerendeZaak", "66860489870000", 3, "/staging-flat.xml", "0"}
        });
    }

    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;

    private final Lock sequential = new ReentrantLock(true);

    /*
     * test parameters.
     */
    private final String rBestandsNaam;
    private final String objNamespace;
    private final String objId;
    private final int aantalBronDoc;
    private final String sBestandsNaam;
    private final String volgordeNummer;

    public EigendomInfoIntegrationTest(final String rBestandsNaam, final String objNamespace, final String objId,
            final int aantalBronDoc, final String sBestandsNaam, final String volgordeNummer) {

        this.rBestandsNaam = rBestandsNaam;
        this.objNamespace = objNamespace;
        this.objId = objId;
        this.aantalBronDoc = aantalBronDoc;
        this.sBestandsNaam = sBestandsNaam;
        this.volgordeNummer = volgordeNummer;
    }

    @Before
    @Override
    public void setUp() throws Exception {
        rsgb = new DatabaseDataSourceConnection(this.dsRsgb);
        staging = new DatabaseDataSourceConnection(dsStaging);
        IDataSet rsgbDataSet = new XmlDataSet(new FileInputStream(new File(BrkInfoIntegrationTest.class.getResource(rBestandsNaam).toURI())));

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(BrkInfoIntegrationTest.class.getResource(sBestandsNaam).toURI())));

        if (this.isMsSQL) {
            // TODO kijken of en hoe dit op mssql werkt, vooralsnog problemen met jDTS driver
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            rsgb = new DatabaseConnection(dsRsgb.getConnection().unwrap(oracle.jdbc.OracleConnection.class), DBPROPS.getProperty("rsgb.username").toUpperCase());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);

            staging = new DatabaseConnection(dsStaging.getConnection().unwrap(oracle.jdbc.OracleConnection.class), DBPROPS.getProperty("staging.username").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);

            rsgbDataSet = new XmlDataSet(new FileInputStream(new File(BrkInfoIntegrationTest.class.getResource("/oracle-" + rBestandsNaam.substring(1)).toURI())));
        } else if (this.isPostgis) {
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        }

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        DatabaseOperation.CLEAN_INSERT.execute(rsgb, rsgbDataSet);
    }

    @After
    public void cleanup() throws Exception {
        CleanUtil.cleanRSGB_BRK(rsgb, true);
        CleanUtil.cleanSTAGING(staging);

        rsgb.close();
        staging.close();

        sequential.unlock();
    }

    /**
     * testcase voor
     * {@link EigendomInfo#createEigendomMutatieContext(nl.b3p.brmo.soap.eigendom.EigendomMutatieRequest)}
     * en {@link EigendomInfo#createEigendomMutatieResponse(java.util.Map)} .
     *
     * @throws Exception if any
     */
    @Test
    public void testEigendomMutatie() throws Exception {
        EigendomMutatieRequest request = new EigendomMutatieRequest();
        request.setMaxAantalResultaten(10);
        request.setIdentificatie(objNamespace + ":" + objId);
        Map<String, Object> context = EigendomInfo.createEigendomMutatieContext(request);

        EigendomMutatieResponse response = EigendomInfo.createEigendomMutatieResponse(context);
        List<EigendomMutatie> mutaties = response.getEigendomMutatie();

        assertFalse(mutaties.isEmpty());
        EigendomMutatie mutatie = mutaties.get(0);
        assertEquals(objId, mutatie.getIdentificatienummer());

        List<Document> documenten = mutatie.getBrondocumenten().getDocument();
        assertFalse(documenten.isEmpty());
        assertEquals(aantalBronDoc, documenten.size());
    }

    /**
     * testcase voor
     * {@link EigendomInfo#createMutatieListContext(nl.b3p.brmo.soap.eigendom.MutatieListRequest)}
     * en {@link EigendomInfo#createMutatieListResponse(java.util.Map)} .
     *
     * @throws Exception if any
     */
    @Test
    public void testMutatieList() throws Exception {
        final GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
        gregorianCalendar.set(2015, 1, 1);
        XMLGregorianCalendar then = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);

        MutatieListRequest request = new MutatieListRequest();
        request.setFromDate(then);
        request.setToDate(now);
        request.setObjectprefix(objNamespace);

        Map<String, Object> context = EigendomInfo.createMutatieListContext(request);
        MutatieListResponse response = EigendomInfo.createMutatieListResponse(context);
        List<MutatieEntry> mutatieentries = response.getMutatieEntry();
        assertFalse(mutatieentries.isEmpty());

        MutatieEntry mutatieentry = mutatieentries.get(0);
        assertEquals(objNamespace + ":" + objId, mutatieentry.getObjectRef());
        assertEquals(volgordeNummer, mutatieentry.getVolgnummer());
    }
}
