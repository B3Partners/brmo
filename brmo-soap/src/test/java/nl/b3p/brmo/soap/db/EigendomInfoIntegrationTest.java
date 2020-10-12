/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.soap.db;

import nl.b3p.brmo.soap.eigendom.*;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.mssql.InsertIdentityOperation;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.FileInputStream;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Draaien met:
 * {@code mvn -Dit.test=EigendomInfoIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 * voor bijvoorbeeld PostgreSQL.
 *
 * @author mprins
 */
public class EigendomInfoIntegrationTest extends TestUtil {


    private final Lock sequential = new ReentrantLock(true);
    /*
     * test parameters.
     */
    private final String rBestandsNaam = "/rsgb.xml";
    private final String objNamespace = "NL.KAD.OnroerendeZaak";
    private final String objId = "66860489870000";
    private final int aantalBronDoc = 3;
    private final String sBestandsNaam = "/staging-flat.xml";
    private final String volgordeNummer = "0";
    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        rsgb = new DatabaseDataSourceConnection(this.dsRsgb);
        staging = new DatabaseDataSourceConnection(dsStaging);
        IDataSet rsgbDataSet = new XmlDataSet(new FileInputStream(new File(BrkInfoIntegrationTest.class.getResource(rBestandsNaam).toURI())));

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(BrkInfoIntegrationTest.class.getResource(sBestandsNaam).toURI())));

        if (this.isMsSQL) {
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());

            rsgbDataSet = new XmlDataSet(new FileInputStream(new File(BrkInfoIntegrationTest.class.getResource("/mssql-" + rBestandsNaam.substring(1)).toURI())));
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

        if (this.isMsSQL) {
            InsertIdentityOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        } else {
            DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        }
        DatabaseOperation.CLEAN_INSERT.execute(rsgb, rsgbDataSet);
        refreshMViews(new String[]{"mb_util_app_re_kad_perceel"}, this.dsRsgb);
    }

    @AfterEach
    public void cleanup() throws Exception {
        CleanUtil.cleanRSGB_BRK(rsgb, true);
        CleanUtil.cleanSTAGING(staging, false);

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

        Assertions.assertFalse(mutaties.isEmpty());
        EigendomMutatie mutatie = mutaties.get(0);
        Assertions.assertEquals(objId, mutatie.getIdentificatienummer());

        List<Document> documenten = mutatie.getBrondocumenten().getDocument();
        Assertions.assertFalse(documenten.isEmpty());
        Assertions.assertEquals(aantalBronDoc, documenten.size());
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
        Assertions.assertFalse(mutatieentries.isEmpty());

        MutatieEntry mutatieentry = mutatieentries.get(0);
        Assertions.assertEquals(objNamespace + ":" + objId, mutatieentry.getObjectRef());
        Assertions.assertEquals(volgordeNummer, mutatieentry.getVolgnummer());
    }
}
