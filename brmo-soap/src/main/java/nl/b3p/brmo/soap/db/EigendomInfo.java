package nl.b3p.brmo.soap.db;

import com.vividsolutions.jts.io.ParseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import nl.b3p.brmo.soap.eigendom.EigendomMutatie;
import nl.b3p.brmo.soap.eigendom.EigendomMutatieException;
import nl.b3p.brmo.soap.eigendom.EigendomMutatieRequest;
import nl.b3p.brmo.soap.eigendom.EigendomMutatieResponse;
import nl.b3p.brmo.soap.eigendom.EigendomMutatieService;
import nl.b3p.brmo.soap.eigendom.MutatieEntry;
import nl.b3p.brmo.soap.eigendom.MutatieListRequest;
import nl.b3p.brmo.soap.eigendom.MutatieListResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Chris
 */
public class EigendomInfo {

    private static final Log log = LogFactory.getLog(EigendomInfo.class);

    private static final String JNDI_NAME = "java:comp/env";
    private static final String JDBC_NAME_RSGB = "jdbc/brmo/rsgb";
    private static final String JDBC_NAME_STAGING = "jdbc/brmo/staging";

    private static final String DB_POSTGRES = "postgres";
    private static final String DB_ORACLE = "oracle";
    private static final String DB_MSSQL = "mssql";

    public static final String ID = "identificatie";
    public static final String FROMDATE = "fromDate";
    public static final String TODATE = "toDate";
    public static final String OBJECT_PREFIX = "objectPrefix";
    
    public static final String OBJECT_REF = "object_ref"; 
    public static final String DATUM = "datum"; 
    public static final String VOLGORDENUMMER = "volgordenummer";
    public static final String STATUS_DATUM = "status_datum";

            
    public static final String MAXAANTALRESULTATEN = "MaxAantalResultaten";
    public static final Integer DBMAXRESULTS = 1000;

    private static DataSource ds = null;

    public static ArrayList<Long> findKozIDs(Map<String, Object> searchContext) throws Exception {

        DataSource ds = getDataSourceRsgb();
        PreparedStatement stm = null;
        Connection connRsgb = null;
        ResultSet rs = null;
        try {
            connRsgb = ds.getConnection();
            String dbType = getDbType(connRsgb);

            StringBuilder sql = createSelectRsgbSQL(searchContext, dbType);

            stm = connRsgb.prepareStatement(sql.toString());
            stm = addParamsSQL(stm, searchContext, dbType);
            rs = stm.executeQuery();

            ArrayList<Long> ids = new ArrayList<Long>();
            while (rs.next()) {
                Long id = rs.getLong("identificatie");
                ids.add(id);
            }

            return ids;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (stm != null) {
                stm.close();
            }
            if (connRsgb != null) {
                connRsgb.close();
            }
        }
    }
    
    public static ArrayList<MutatieEntry> findMutatieEntries(Map<String, Object> searchContext) throws Exception {

        DataSource ds = getDataSourceStaging();
        PreparedStatement stm = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            String dbType = getDbType(conn);

            StringBuilder sql = createSelectStagingSQL(searchContext, dbType);

            stm = conn.prepareStatement(sql.toString());
            stm = addParamsSQL(stm, searchContext, dbType);
            rs = stm.executeQuery();

            ArrayList<MutatieEntry> entries = new ArrayList<>();
            while (rs.next()) {
                MutatieEntry entry = new MutatieEntry();
                entry.setObjectRef(rs.getString(OBJECT_REF));
                GregorianCalendar gcalendar = new GregorianCalendar();
                gcalendar.setTime(rs.getDate(DATUM));
                XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcalendar);
                entry.setDatum(xmlDate);
                entry.setVolgnummer(rs.getString(VOLGORDENUMMER));
                gcalendar.setTime(rs.getDate(STATUS_DATUM));
                xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcalendar);
                entry.setStatusDatum(xmlDate);
                entries.add(entry);
            }

            return entries;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (stm != null) {
                stm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    /**
     * Gezien de omvang van de datassets moet deze methode maar niet gebruikt worden.
     * @param searchContext
     * @return
     * @throws Exception 
     */
    public static Integer countRsgbResults(Map<String, Object> searchContext) throws Exception {

        DataSource ds = getDataSourceRsgb();
        PreparedStatement stm = null;
        Connection connRsgb = null;
        ResultSet rs = null;
        try {
            connRsgb = ds.getConnection();
            String dbType = getDbType(connRsgb);

            StringBuilder sql = createCountRsgbSQL(searchContext, dbType);

            stm = connRsgb.prepareStatement(sql.toString());
            stm = addParamsSQL(stm, searchContext, dbType);
            rs = stm.executeQuery();

            if (rs.next()) {
                return rs.getInt("aantal");
            }

            return null;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (stm != null) {
                stm.close();
            }
            if (connRsgb != null) {
                connRsgb.close();
            }
        }
    }

    private static StringBuilder createSelectRsgbSQL(
            Map<String, Object> searchContext, String dbType) throws ParseException {

        Integer maxrows = (Integer) searchContext.get(MAXAANTALRESULTATEN);
        if (maxrows == null) {
            maxrows = DBMAXRESULTS;
            searchContext.put(MAXAANTALRESULTATEN, DBMAXRESULTS);
        } else if (maxrows > DBMAXRESULTS) {
            searchContext.put(MAXAANTALRESULTATEN, DBMAXRESULTS);
        }
        // één meer ophalen zodat je kunt weten of er meer zijn
        maxrows += 1;

        StringBuilder sql = new StringBuilder();
        switch (dbType) {
            case DB_POSTGRES:
                sql.append("SELECT ");
                sql.append("    DISTINCT kad_onrrnd_zk.kad_identif AS identificatie ");
                sql.append("FROM ");
                sql.append(createFromRsgbSQL());
                sql.append("WHERE ");
                sql.append(createWhereRsgbSQL(searchContext, dbType));
                sql.append("LIMIT ");
                sql.append(maxrows);
                return sql;
            case DB_ORACLE:
                sql.append("SELECT ");
                sql.append("    DISTINCT kad_onrrnd_zk.kad_identif AS identificatie ");
                sql.append("FROM ");
                sql.append(createFromRsgbSQL());
                sql.append("WHERE ");
                sql.append(createWhereRsgbSQL(searchContext, dbType));
                StringBuilder tempSql = new StringBuilder();
                tempSql.append("SELECT * FROM ( ");
                tempSql.append(sql);
                tempSql.append(" ) WHERE ROWNUM <= ");
                tempSql.append(maxrows);
                sql = tempSql;
                return sql;
            case DB_MSSQL:
                sql.append("SELECT ");
                sql.append("TOP ");
                sql.append("( ");
                sql.append(maxrows);
                sql.append(") ");
                sql.append("    DISTINCT kad_onrrnd_zk.kad_identif AS identificatie ");
                sql.append("FROM ");
                sql.append(createFromRsgbSQL());
                sql.append("WHERE ");
                sql.append(createWhereRsgbSQL(searchContext, dbType));
                return sql;
            default:
                throw new UnsupportedOperationException("Unknown database!");
        }
    }

    private static StringBuilder createSelectStagingSQL(
            Map<String, Object> searchContext, String dbType) throws ParseException {

        Integer maxrows = (Integer) searchContext.get(MAXAANTALRESULTATEN);
        if (maxrows == null) {
            maxrows = DBMAXRESULTS;
            searchContext.put(MAXAANTALRESULTATEN, DBMAXRESULTS);
        } else if (maxrows > DBMAXRESULTS) {
            searchContext.put(MAXAANTALRESULTATEN, DBMAXRESULTS);
        }
        // één meer ophalen zodat je kunt weten of er meer zijn
        maxrows += 1;

        StringBuilder sql = new StringBuilder();
        switch (dbType) {
            case DB_POSTGRES:
                sql.append("SELECT ");
                sql.append("    DISTINCT bericht.object_ref, bericht.datum, bericht.volgordenummer, bericht.status_datum ");
                sql.append("FROM ");
                sql.append(createFromStagingSQL());
                sql.append("WHERE ");
                sql.append(createWhereStagingSQL(searchContext, dbType));
                sql.append("LIMIT ");
                sql.append(maxrows);
                return sql;
            case DB_ORACLE:
                sql.append("SELECT ");
                sql.append("    DISTINCT bericht.object_ref, bericht.datum, bericht.volgordenummer, bericht.status_datum ");
                sql.append("FROM ");
                sql.append(createFromStagingSQL());
                sql.append("WHERE ");
                sql.append(createWhereStagingSQL(searchContext, dbType));
                StringBuilder tempSql = new StringBuilder();
                tempSql.append("SELECT * FROM ( ");
                tempSql.append(sql);
                tempSql.append(" ) WHERE ROWNUM <= ");
                tempSql.append(maxrows);
                sql = tempSql;
                return sql;
            case DB_MSSQL:
                sql.append("SELECT ");
                sql.append("TOP ");
                sql.append("( ");
                sql.append(maxrows);
                sql.append(") ");
                sql.append("    DISTINCT bericht.object_ref, bericht.datum, bericht.volgordenummer, bericht.status_datum ");
                sql.append("FROM ");
                sql.append(createFromStagingSQL());
                sql.append("WHERE ");
                sql.append(createWhereStagingSQL(searchContext, dbType));
                return sql;
            default:
                throw new UnsupportedOperationException("Unknown database!");
        }
    }

    private static StringBuilder createCountRsgbSQL(
            Map<String, Object> searchContext, String dbType) throws ParseException {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    COUNT(DISTINCT kad_onrrnd_zk.kad_identif) AS aantal ");
        sql.append("FROM ");
        sql.append(createFromRsgbSQL());
        sql.append("WHERE ");
        sql.append(createWhereRsgbSQL(searchContext, dbType));
        return sql;
    }

    private static StringBuilder createFromStagingSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("    bericht ");
        return sql;
    }

    private static StringBuilder createFromRsgbSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("    kad_onrrnd_zk ");
        sql.append("LEFT OUTER JOIN ");
        sql.append("    kad_perceel ");
        sql.append("ON ");
        sql.append("    ( ");
        sql.append("        kad_onrrnd_zk.kad_identif = kad_perceel.sc_kad_identif) ");
        sql.append("LEFT OUTER JOIN ");
        sql.append("    app_re ");
        sql.append("ON ");
        sql.append("    ( ");
        sql.append("        kad_onrrnd_zk.kad_identif = app_re.sc_kad_identif) ");
        sql.append("LEFT OUTER JOIN ");
        sql.append("    zak_recht ");
        sql.append("ON ");
        sql.append("    ( ");
        sql.append("        kad_onrrnd_zk.kad_identif = zak_recht.fk_7koz_kad_identif) ");
        sql.append("LEFT OUTER JOIN ");
        sql.append("    niet_nat_prs ");
        sql.append("ON ");
        sql.append("    ( ");
        sql.append("        zak_recht.fk_8pes_sc_identif = niet_nat_prs.sc_identif) ");
        sql.append("LEFT OUTER JOIN ");
        sql.append("    nat_prs ");
        sql.append("ON ");
        sql.append("    ( ");
        sql.append("        zak_recht.fk_8pes_sc_identif = nat_prs.sc_identif) ");
        sql.append("LEFT OUTER JOIN ");
        sql.append("    ander_nat_prs ");
        sql.append("ON ");
        sql.append("    ( ");
        sql.append("        nat_prs.sc_identif = ander_nat_prs.sc_identif) ");
        sql.append("LEFT OUTER JOIN ");
        sql.append("    ingeschr_nat_prs ");
        sql.append("ON ");
        sql.append("    ( ");
        sql.append("        nat_prs.sc_identif = ingeschr_nat_prs.sc_identif) ");
        sql.append("LEFT OUTER JOIN ");
        sql.append("    ingeschr_niet_nat_prs ");
        sql.append("ON ");
        sql.append("    ( ");
        sql.append("        niet_nat_prs.sc_identif = ingeschr_niet_nat_prs.sc_identif) ");
        sql.append("LEFT OUTER JOIN ");
        sql.append("    benoemd_obj_kad_onrrnd_zk ");
        sql.append("ON ");
        sql.append("    ( ");
        sql.append("        kad_onrrnd_zk.kad_identif = ");
        sql.append("        benoemd_obj_kad_onrrnd_zk.fk_nn_rh_koz_kad_identif) ");
        sql.append("LEFT OUTER JOIN ");
        sql.append("    verblijfsobj ");
        sql.append("ON ");
        sql.append("    ( ");
        sql.append("        benoemd_obj_kad_onrrnd_zk.fk_nn_lh_tgo_identif = verblijfsobj.sc_identif) ");
        sql.append("LEFT OUTER JOIN ");
        sql.append("    addresseerb_obj_aand ");
        sql.append("ON ");
        sql.append("    ( ");
        sql.append("        verblijfsobj.fk_11nra_sc_identif = addresseerb_obj_aand.identif) ");
        sql.append("LEFT OUTER JOIN ");
        sql.append("    gem_openb_rmte ");
        sql.append("ON ");
        sql.append("    ( ");
        sql.append("        addresseerb_obj_aand.fk_7opr_identifcode = gem_openb_rmte.identifcode) ");
        sql.append("LEFT OUTER JOIN ");
        sql.append("    openb_rmte_wnplts ");
        sql.append("ON ");
        sql.append("    ( ");
        sql.append("        gem_openb_rmte.identifcode = openb_rmte_wnplts.fk_nn_lh_opr_identifcode) ");
        sql.append("LEFT OUTER JOIN ");
        sql.append("    wnplts ");
        sql.append("ON ");
        sql.append("    ( ");
        sql.append("        openb_rmte_wnplts.fk_nn_rh_wpl_identif = wnplts.identif) ");

        return sql;
    }

    private static StringBuilder createWhereRsgbSQL(
            Map<String, Object> searchContext, String dbType) throws ParseException {
        StringBuilder sql = new StringBuilder();

        boolean first = true;
        String condition;
//        condition = "    kad_onrrnd_zk.kad_identif = ? "; //1 Long
//        first = addTerm(first, searchContext.get(IDENTIFICATIE), sql, condition);
//
//        condition = "   (    nat_prs.nm_geslachtsnaam like ? "
//                + "    OR niet_nat_prs.naam like ?)"; //2 string
//        first = addTerm(first, searchContext.get(SUBJECTNAAM), sql, condition);

 
        return sql;
    }
    
    private static StringBuilder createWhereStagingSQL(
            Map<String, Object> searchContext, String dbType) throws ParseException {
        StringBuilder sql = new StringBuilder();

        boolean first = true;
        String condition;
        condition = "    bericht.object_ref like ? "; 
        first = addTerm(first, searchContext.get(OBJECT_PREFIX), sql, condition);

        condition = "    bericht.status_datum >= ? "; 
        first = addTerm(first, searchContext.get(FROMDATE), sql, condition);

        condition = "    bericht.status_datum <= ? "; 
        addTerm(first, searchContext.get(TODATE), sql, condition);
        
        return sql;
    }

    private static boolean addTerm(boolean first, Object o, StringBuilder sql, String condition) {
        if (o != null) {
            if (!first) {
                sql.append("AND ");
            }
            sql.append(condition);
            first = false;
        }
        return first;
    }

    private static PreparedStatement addParamsSQL(PreparedStatement stm,
            Map<String, Object> searchContext, String dbType) throws SQLException {

        int index = 1;
        index = addParam(index, searchContext.get(OBJECT_PREFIX), stm, dbType);
        index = addParam(index, searchContext.get(FROMDATE), stm, dbType);
        addParam(index, searchContext.get(FROMDATE), stm, dbType);

        return stm;
    }

    private static int addParam(int index, Object o, PreparedStatement stm,
            String dbType) throws SQLException {
        if (o != null) {
            if (o instanceof String) {
                stm.setString(index++, "" + ((String) o) + "%");
            } else {
                stm.setObject(index++, o);
            }
        }
        return index;
    }

    public static DataSource getDataSourceRsgb() throws Exception {
        if (ds != null) {
            return ds;
        }
        try {
            InitialContext ic = new InitialContext();
            Context xmlContext = (Context) ic.lookup(JNDI_NAME);
            ds = (DataSource) xmlContext.lookup(JDBC_NAME_RSGB);
        } catch (Exception ex) {
            log.error("Fout verbinden naar rsgb db. ", ex);
            throw ex;
        }

        return ds;
    }
    
    public static DataSource getDataSourceStaging() throws Exception {
        if (ds != null) {
            return ds;
        }
        try {
            InitialContext ic = new InitialContext();
            Context xmlContext = (Context) ic.lookup(JNDI_NAME);
            ds = (DataSource) xmlContext.lookup(JDBC_NAME_STAGING);
        } catch (Exception ex) {
            log.error("Fout verbinden naar rsgb db. ", ex);
            throw ex;
        }

        return ds;
    }

    public static String getDbType(Connection connRsgb) throws SQLException {
        String databaseProductName = connRsgb.getMetaData().getDatabaseProductName();
        if (databaseProductName.contains("PostgreSQL")) {
            return DB_POSTGRES;
        } else if (databaseProductName.contains("Oracle")) {
            return DB_ORACLE;
        } else if (databaseProductName.contains("Microsoft")) {
            return DB_MSSQL;
        } else {
            throw new UnsupportedOperationException("Unknown database: " + connRsgb.getMetaData().getDatabaseProductName());
        }
    }

    public static EigendomMutatieResponse createEigendomMutatieResponse(Map<String, Object> eigendomMutatieContext) throws EigendomMutatieException {
        
        EigendomMutatieResponse eir = new EigendomMutatieResponse();
        try {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            XMLGregorianCalendar now
                    = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
           eir.setTimestamp(now);

        } catch (DatatypeConfigurationException ex) {
            Logger.getLogger(EigendomMutatieService.class.getName()).log(Level.SEVERE, null, ex);
        }
        EigendomMutatie em = EigendomMutatie.createDummy();
//        em.setIdentificatienummer(id);
        
        eir.getEigendomMutatie().add(em);
        return eir;
    }

    public static MutatieListResponse createMutatieListResponse(Map<String, Object> mutatieListContext) throws EigendomMutatieException {
        
        MutatieListResponse mlr = new MutatieListResponse();
        
        ArrayList<MutatieEntry> entries;
        try {
            entries = findMutatieEntries(mutatieListContext);
            for (MutatieEntry e : entries) {
                mlr.getMutatieEntry().add(e);
            }
        } catch (Exception ex) {
            Logger.getLogger(EigendomInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        XMLGregorianCalendar now = null;
        try {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
            mlr.setTimestamp(now);

        } catch (DatatypeConfigurationException ex) {
            Logger.getLogger(EigendomInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return mlr;
    }

    public static Map<String, Object> createMutatieListContext(MutatieListRequest request)  throws EigendomMutatieException {
        Map<String, Object> searchContext = new HashMap<String, Object>();
        if (request == null) {
            return searchContext;
        }
        
        if (request.getFromDate() != null) {
            Date fromDate = null;
            XMLGregorianCalendar xmlFromDate = request.getFromDate();
            if (xmlFromDate != null) {
                fromDate = xmlFromDate.toGregorianCalendar().getTime();
            }
            searchContext.put(EigendomInfo.FROMDATE, fromDate);
        }
        if (request.getToDate() != null) {
            Date toDate = null;
            XMLGregorianCalendar xmlToDate = request.getToDate();
            if (xmlToDate != null) {
                toDate = xmlToDate.toGregorianCalendar().getTime();
            }
            searchContext.put(EigendomInfo.TODATE, toDate);
        }
 
//        Elk BAG-object heeft een unieke identificatie.
//            * Woonplaats – NNNN
//            Woonplaatsen hebben een unieke woonplaatscode van vier cijfers.
//                    
//            * Overige BAG-objecten – GGGGTTNNNNNNNNNN
//            Alle overige objecten hebben een unieke identificatie van dit formaat:
//            * GGGG = gemeentecode
//            De gemeentecode van de gemeente die het object als eerste heeft opgevoerd. Bij een
//            gemeentelijke herindeling blijft de identificatie van de objecten binnen die gemeente ongewijzigd.
//            * TT = objecttypecode
//                01 verblijfsobject
//                02 ligplaats
//                03 standplaats
//                10 pand
//                20 nummeraanduiding
//                30 openbare ruimte
//                40 woonplaats
//            * NNNNNNNNNN = binnen een gemeente uniek tiencijferig nummer

        if (request.getObjectprefix() != null) {
            String objectprefix = request.getObjectprefix();
            if (objectprefix.equalsIgnoreCase("VBO:007")) {
                throw new EigendomMutatieException("Ongeldige invoer", "VBO:007 is geen geldige waarde!");
            }
            searchContext.put(EigendomInfo.OBJECT_PREFIX, objectprefix);
        }
        
        return searchContext;
    }
    
    public static Map<String, Object> createEigendomMutatieContext(EigendomMutatieRequest request) throws EigendomMutatieException {
        Map<String, Object> searchContext = new HashMap<String, Object>();
        if (request == null) {
            return searchContext;
        }

        if (request.getIdentificatie() != null) {
            searchContext.put(EigendomInfo.ID, request.getIdentificatie());
        }
        if (request.getMaxAantalResultaten() != null) {
            Integer maxNum = request.getMaxAantalResultaten();
            if (maxNum > 100) {
                throw new EigendomMutatieException("Ongeldige invoer", "Er kunnen maximaal 100 objecten opgevraagd worden");
            }
            searchContext.put(EigendomInfo.MAXAANTALRESULTATEN, maxNum);
        }
        return searchContext;
    }

}
