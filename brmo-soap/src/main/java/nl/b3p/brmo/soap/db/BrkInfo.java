package nl.b3p.brmo.soap.db;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.service.util.ConfigUtil;
import nl.b3p.brmo.soap.brk.BrkInfoRequest;
import nl.b3p.brmo.soap.brk.BrkInfoResponse;
import nl.b3p.brmo.soap.brk.KadOnrndZkInfoRequest;
import nl.b3p.brmo.soap.brk.KadOnrndZkInfoResponse;
import nl.b3p.brmo.soap.brk.PerceelAdresInfoRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Chris
 */
public class BrkInfo {

    private static final Log LOG = LogFactory.getLog(BrkInfo.class);

    private static final String JNDI_NAME = "java:comp/env";
    private static final String JDBC_NAME_RSGB = "jdbc/brmo/rsgb";

    public static final String DB_POSTGRES = "postgres";
    public static final String DB_ORACLE = "oracle";
    public static final String DB_MSSQL = "mssql";

    public static final String APPREVOLGNUMMER = "appReVolgnummer";
    public static final String WOONPLAATS = "gemeenteNaam";
    public static final String GEMEENTECODE = "gemeentecode";
    public static final String HUISNUMMER = "huisnummer";
    public static final String IDENTIFICATIE = "identificatie";
    public static final String PERCEELNUMMER = "perceelnummer";
    public static final String POSTCODE = "postcode";
    public static final String SECTIE = "sectie";
    public static final String STRAATNAAM = "straatNaam";
    public static final String SUBJECTNAAM = "subjectNaam";
    public static final String ZOEKGEBIED = "zoekgebied";
    public static final String BUFFERLENGTE = "bufferLengte";

    public static final String MAXAANTALRESULTATEN = "MaxAantalResultaten";
    public static final String ADRESSENTOEVOEGEN = "AdressenToevoegen";
    public static final String GEVOELIGEINFOOPHALEN = "GevoeligeInfoOphalen";
    public static final String SUBJECTSTOEVOEGEN = "SubjectsToevoegen";

    public static final Integer DBMAXRESULTS = 1000;

    /**
     * Zoek onroerende zaak ids.
     *
     * @param searchContext zoek parameters
     * @return lijst met onroerende zaak ids.
     * @throws SQLException bij een SQL/database fout
     * @throws ParseException als parsen van de geometrie mislukt
     * @throws javax.naming.NamingException als er een fout optreedt bij het
     * opzoeken van de JNDI naam
     */
    public static ArrayList<Long> findKozIDs(Map<String, Object> searchContext) throws SQLException, ParseException, NamingException, BrmoException {

        DataSource ds = ConfigUtil.getDataSourceRsgb();
        PreparedStatement stm = null;
        Connection connRsgb = null;
        ResultSet rs = null;
        try {
            connRsgb = ds.getConnection();
            String dbType = getDbType(connRsgb);

            StringBuilder sql = createSelectSQL(searchContext, dbType);
            LOG.trace(sql);
            LOG.trace(searchContext);
            stm = connRsgb.prepareStatement(sql.toString());
            stm = addParamsSQL(stm, searchContext, dbType);
            rs = stm.executeQuery();
            ArrayList<Long> ids = new ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getLong("identificatie"));
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

    /**
     * Gezien de omvang van de datassets moet deze methode maar niet gebruikt worden.
     */
    public static Integer countResults(Map<String, Object> searchContext) throws Exception {

        DataSource ds = ConfigUtil.getDataSourceRsgb();
        PreparedStatement stm = null;
        Connection connRsgb = null;
        ResultSet rs = null;
        try {
            connRsgb = ds.getConnection();
            String dbType = getDbType(connRsgb);

            StringBuilder sql = createCountSQL(searchContext, dbType);
            LOG.trace(sql);
            LOG.trace(searchContext);
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

    private static StringBuilder createSelectSQL(
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
                sql.append(createFromSQL());
                sql.append("WHERE ");
                sql.append(createWhereSQL(searchContext, dbType));
                sql.append("LIMIT ");
                sql.append(maxrows);
                return sql;
            case DB_ORACLE:
                sql.append("SELECT ");
                sql.append("    DISTINCT kad_onrrnd_zk.kad_identif AS identificatie ");
                sql.append("FROM ");
                sql.append(createFromSQL());
                sql.append("WHERE ");
                sql.append(createWhereSQL(searchContext, dbType));
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
                sql.append(createFromSQL());
                sql.append("WHERE ");
                sql.append(createWhereSQL(searchContext, dbType));
                return sql;
            default:
                throw new UnsupportedOperationException("Unknown database!");
        }
    }

    private static StringBuilder createCountSQL(
            Map<String, Object> searchContext, String dbType) throws ParseException {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    COUNT(DISTINCT kad_onrrnd_zk.kad_identif) AS aantal ");
        sql.append("FROM ");
        sql.append(createFromSQL());
        sql.append("WHERE ");
        sql.append(createWhereSQL(searchContext, dbType));
        return sql;
    }

    private static StringBuilder createFromSQL() {
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

    private static StringBuilder createWhereSQL(
            Map<String, Object> searchContext, String dbType) throws ParseException {
        StringBuilder sql = new StringBuilder();

        boolean first = true;
        String condition;
        condition = "    kad_onrrnd_zk.kad_identif = ? "; //1 Long
        first = addTerm(first, searchContext.get(IDENTIFICATIE), sql, condition);

        condition = "   (    nat_prs.nm_geslachtsnaam like ? "
                + "    OR niet_nat_prs.naam like ?)"; //2 string
        first = addTerm(first, searchContext.get(SUBJECTNAAM), sql, condition);

        //3+4 wkt en bufferlengte
        first = addGeoTerm(first, sql, searchContext, dbType);

        condition = "app_re.ka_appartementsindex like ? "; //5 string
        first = addTerm(first, searchContext.get(APPREVOLGNUMMER), sql, condition);

        condition = "   (    app_re.ka_kad_gemeentecode like ? "
                + "    OR kad_perceel.ka_kad_gemeentecode like ?) "; //6 string
        first = addTerm(first, searchContext.get(GEMEENTECODE), sql, condition);

        condition = "   (    app_re.ka_perceelnummer like ? "
                + "    OR kad_perceel.ka_perceelnummer like ?) "; //7 string
        first = addTerm(first, searchContext.get(PERCEELNUMMER), sql, condition);

        condition = "   (    app_re.ka_sectie like ? "
                + "    OR kad_perceel.ka_sectie like ?) "; //8 string
        first = addTerm(first, searchContext.get(SECTIE), sql, condition);

        condition = "wnplts.naam like ? "; //9 string
        first = addTerm(first, searchContext.get(WOONPLAATS), sql, condition);

        condition = "gem_openb_rmte.naam_openb_rmte like ? "; //10 string 
        first = addTerm(first, searchContext.get(STRAATNAAM), sql, condition);

        condition = "addresseerb_obj_aand.huinummer = ? "; //11 Integer
        first = addTerm(first, searchContext.get(HUISNUMMER), sql, condition);

        condition = "addresseerb_obj_aand.postcode like ? "; //12 string
        addTerm(first, searchContext.get(POSTCODE), sql, condition);

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

    private static boolean addGeoTerm(boolean first, StringBuilder sql,
            Map<String, Object> searchContext, String dbType) throws ParseException {

        String zg = (String) searchContext.get(ZOEKGEBIED);
        if (zg == null) {
            return first;
        }
        WKTReader r = new WKTReader();
        //test om injectie te voorkomen, exception indien niet geldig
        Geometry g = r.read(zg);
        Integer bl = (Integer) searchContext.get(BUFFERLENGTE);

        String condition;
        switch (dbType) {
            case DB_POSTGRES:
                if (bl != null) {
                    condition = "ST_Intersects(kad_perceel.begrenzing_perceel, "
                            + "ST_Buffer(ST_GeomFromEWKT('SRID=28992;"
                            + zg
                            + "'), "
                            + bl
                            + ") ) ";
                } else {
                    condition = "ST_Intersects(kad_perceel.begrenzing_perceel, "
                            + "ST_GeomFromEWKT('SRID=28992;"
                            + zg
                            + "') ) ";
                }
                break;
            case DB_ORACLE:
                if (bl != null) {
                    condition = "SDO_GEOM.RELATE(kad_perceel.begrenzing_perceel, 'ANYINTERACT', "
                            + "SDO_GEOM.SDO_BUFFER(SDO_GEOMETRY(('"
                            + zg
                            + "'), 28992), 2, "
                            + bl
                            + "), 0.005 )  = 'TRUE' ";
                } else {
                    condition = "SDO_GEOM.RELATE(kad_perceel.begrenzing_perceel, 'ANYINTERACT', "
                            + "SDO_GEOMETRY(('"
                            + zg
                            + "'), 28992), 0.005 ) = 'TRUE' ";
                }
                break;
            case DB_MSSQL:
                if (bl != null) {
                    condition = "kad_perceel.begrenzing_perceel.STIntersects( "
                            + "STGeomFromText('"
                            + zg
                            + "',28992).STBuffer("
                            + bl
                            + ") ) ";
                } else {
                    condition = "kad_perceel.begrenzing_perceel.STIntersects( "
                            + "STGeomFromText('"
                            + zg
                            + "',28992) ) ";
                    break;
                }
            default:
                throw new UnsupportedOperationException("Unknown database!");
        }
        if (!first) {
            sql.append("AND ");
        }
        sql.append(condition);
        first = false;
        return first;
    }

    private static PreparedStatement addParamsSQL(PreparedStatement stm,
            Map<String, Object> searchContext, String dbType) throws SQLException {

        int index = 1;
        index = addParam(index, searchContext.get(IDENTIFICATIE), stm, dbType);
        index = addParam(index, searchContext.get(SUBJECTNAAM), stm, dbType);
        index = addParam(index, searchContext.get(SUBJECTNAAM), stm, dbType);
        //bufferlengte en zoekgebied direct in de Where!
        index = addParam(index, searchContext.get(APPREVOLGNUMMER), stm, dbType);
        index = addParam(index, searchContext.get(GEMEENTECODE), stm, dbType);
        index = addParam(index, searchContext.get(GEMEENTECODE), stm, dbType);
        index = addParam(index, searchContext.get(PERCEELNUMMER), stm, dbType);
        index = addParam(index, searchContext.get(PERCEELNUMMER), stm, dbType);
        index = addParam(index, searchContext.get(SECTIE), stm, dbType);
        index = addParam(index, searchContext.get(SECTIE), stm, dbType);
        index = addParam(index, searchContext.get(WOONPLAATS), stm, dbType);
        index = addParam(index, searchContext.get(STRAATNAAM), stm, dbType);
        index = addParam(index, searchContext.get(HUISNUMMER), stm, dbType);
        addParam(index, searchContext.get(POSTCODE), stm, dbType);

        return stm;
    }

    private static int addParam(int index, Object o, PreparedStatement stm,
            String dbType) throws SQLException {
        if (o != null) {
            if (o instanceof String) {
                stm.setString(index++, "%" + ((String) o) + "%");
            } else {
                stm.setObject(index++, o);
            }
        }
        return index;
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

    public static Map<String, Object> createSearchContext(BrkInfoRequest request) {
        Map<String, Object> searchContext = new HashMap<>();
        if (request == null) {
            return searchContext;
        }
        if (request.getSubjectNaam() != null) {
            searchContext.put(BrkInfo.SUBJECTNAAM, request.getSubjectNaam());
        }
        if (request.getZoekgebied() != null) {
            searchContext.put(BrkInfo.ZOEKGEBIED, request.getZoekgebied());
        }
        if (request.getBufferAfstand() != null) {
            searchContext.put(BrkInfo.BUFFERLENGTE, request.getBufferAfstand());
        }

        KadOnrndZkInfoRequest koz = request.getKadOnrndZk();
        if (koz != null) {
            if (koz.getIdentificatie() != null) {
                searchContext.put(BrkInfo.IDENTIFICATIE, Long.parseLong(koz.getIdentificatie()));
            }
            if (koz.getAppReVolgnummer() != null) {
                searchContext.put(BrkInfo.APPREVOLGNUMMER, koz.getAppReVolgnummer());
            }
            if (koz.getGemeentecode() != null) {
                searchContext.put(BrkInfo.GEMEENTECODE, koz.getGemeentecode());
            }
            if (koz.getPerceelnummer() != null) {
                searchContext.put(BrkInfo.PERCEELNUMMER, koz.getPerceelnummer());
            }
            if (koz.getSectie() != null) {
                searchContext.put(BrkInfo.SECTIE, koz.getSectie());
            }
        }
        PerceelAdresInfoRequest pa = request.getPerceelAdres();
        if (pa != null) {
            if (pa.getWoonplaatsNaam() != null) {
                searchContext.put(BrkInfo.WOONPLAATS, pa.getWoonplaatsNaam());
            }
            if (pa.getHuisnummer() != null) {
                searchContext.put(BrkInfo.HUISNUMMER, pa.getHuisnummer());
            }
            if (pa.getPostcode() != null) {
                searchContext.put(BrkInfo.POSTCODE, pa.getPostcode());
            }
            if (pa.getStraatNaam() != null) {
                searchContext.put(BrkInfo.STRAATNAAM, pa.getStraatNaam());
            }
        }
        if (!searchContext.isEmpty()) {
            if (request.getMaxAantalResultaten() != null) {
                searchContext.put(BrkInfo.MAXAANTALRESULTATEN, request.getMaxAantalResultaten());
            }
            if (request.getAdressenToevoegen() != null) {
                searchContext.put(BrkInfo.ADRESSENTOEVOEGEN, request.getAdressenToevoegen());
            }
            if (request.getGevoeligeInfoOphalen() != null) {
                searchContext.put(BrkInfo.GEVOELIGEINFOOPHALEN, request.getGevoeligeInfoOphalen());
            }
            if (request.getSubjectsToevoegen() != null) {
                searchContext.put(BrkInfo.SUBJECTSTOEVOEGEN, request.getSubjectsToevoegen());
            }
        }
        return searchContext;
    }

    public static BrkInfoResponse createResponse(List<Long> ids,
            Map<String, Object> searchContext) throws Exception {
        BrkInfoResponse result = new BrkInfoResponse();

        Boolean at = (Boolean) searchContext.get(BrkInfo.SUBJECTSTOEVOEGEN);
        if (at != null && at) {
            at = (Boolean) searchContext.get(BrkInfo.GEVOELIGEINFOOPHALEN);
            if (at != null && at) {
                result.setBevatGevoeligeInfo(true);
            }
        }
        result.setTimestamp(new Date());

        for (Long id : ids) {
            KadOnrndZkInfoResponse koz
                    = KadOnrndZkInfoResponse.getRecord(id, searchContext);
            result.addKadOnrndZk(koz);
        }

        return result;
    }

}
