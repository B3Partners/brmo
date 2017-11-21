package nl.b3p.brmo.soap.db;

import com.vividsolutions.jts.io.ParseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import nl.b3p.brmo.service.util.ConfigUtil;
import nl.b3p.brmo.soap.eigendom.BenoemdObject;
import nl.b3p.brmo.soap.eigendom.BenoemdObjecten;
import nl.b3p.brmo.soap.eigendom.Brondocumenten;
import nl.b3p.brmo.soap.eigendom.Document;
import nl.b3p.brmo.soap.eigendom.EigendomMutatie;
import nl.b3p.brmo.soap.eigendom.EigendomMutatieException;
import nl.b3p.brmo.soap.eigendom.EigendomMutatieRequest;
import nl.b3p.brmo.soap.eigendom.EigendomMutatieResponse;
import nl.b3p.brmo.soap.eigendom.HistorischeRelatie;
import nl.b3p.brmo.soap.eigendom.HistorischeRelaties;
import nl.b3p.brmo.soap.eigendom.MutatieEntry;
import nl.b3p.brmo.soap.eigendom.MutatieListRequest;
import nl.b3p.brmo.soap.eigendom.MutatieListResponse;
import nl.b3p.brmo.soap.eigendom.ZakelijkRecht;
import nl.b3p.brmo.soap.eigendom.ZakelijkeRechten;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class voor het afhandelen van de database calls ten bate van de
 * EigendomMutatieService.
 * 
 * @author Chris
 */
public class EigendomInfo {

    private static final Log LOG = LogFactory.getLog(EigendomInfo.class);

    private static final String DB_POSTGRES = "postgres";
    private static final String DB_ORACLE = "oracle";
    private static final String DB_MSSQL = "mssql";

    private static final String ID = "id";
    private static final String FROMDATE = "fromDate";
    private static final String TODATE = "toDate";
    private static final String OBJECT_PREFIX = "objectPrefix";

    private static final String IDENTIFICATIE = "identificatie";
    private static final String NAMESPACE = "namespace";

    public static final String MAXAANTALRESULTATEN = "MaxAantalResultaten";
    public static final Integer DBMAXRESULTS = 10000;

    /**
     * Berekend Soap antwoord op vraag.
     *
     * @param eigendomMutatieContext map met parameters met vraag
     * @return berekende antwoord op vraag in map met parameters
     * @throws EigendomMutatieException if any
     */
    public static EigendomMutatieResponse createEigendomMutatieResponse(Map<String, Object> eigendomMutatieContext) throws EigendomMutatieException {

        EigendomMutatieResponse eir = new EigendomMutatieResponse();

        ArrayList<EigendomMutatie> entries;
        try {
            entries = findEigendomMutatie(eigendomMutatieContext);
            for (EigendomMutatie e : entries) {
                eir.getEigendomMutatie().add(e);
            }
        } catch (Exception ex) {
            LOG.error("EigendomMutatie opzoeken is mislukt.", ex);
        }

//        EigendomMutatie em = EigendomMutatie.createDummy();
//        eir.getEigendomMutatie().add(em);
        try {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
            eir.setTimestamp(now);
        } catch (DatatypeConfigurationException ex) {
            LOG.error("Datatype configuratie fout tijdens instellen datum.", ex);
        }

        return eir;
    }

    /**
     * Berekend Soap antwoord op vraag.
     *
     * @param mutatieListContext map met parameters met vraag
     * @return berekende antwoord op vraag in map met parameters
     * @throws EigendomMutatieException if any
     */
    public static MutatieListResponse createMutatieListResponse(Map<String, Object> mutatieListContext) throws EigendomMutatieException {

        MutatieListResponse mlr = new MutatieListResponse();

        ArrayList<MutatieEntry> entries;
        try {
            entries = findMutatieEntries(mutatieListContext);
            for (MutatieEntry e : entries) {
                mlr.getMutatieEntry().add(e);
            }
        } catch (Exception ex) {
            LOG.error("MutatieEntry opzoeken is mislukt.", ex);
        }

        XMLGregorianCalendar now = null;
        try {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
            mlr.setTimestamp(now);
        } catch (DatatypeConfigurationException ex) {
            LOG.error("Datatype configuratie fout tijdens instellen datum.", ex);
        }

        return mlr;
    }

    /**
     * Omzetten van Soap request naar map met parameters met enige error
     * checking.
     *
     * @param request binnenkomend request met vraagstelling
     * @return map met parameters
     * @throws EigendomMutatieException if any
     */
    public static Map<String, Object> createMutatieListContext(MutatieListRequest request) throws EigendomMutatieException {
        Map<String, Object> searchContext = new HashMap<String, Object>();
        if (request == null) {
            return searchContext;
        }
        
        if (request.getFromDate() != null) {
            java.sql.Timestamp fromDate = null;
            XMLGregorianCalendar xmlFromDate = request.getFromDate();
            if (xmlFromDate != null) {
                fromDate = new java.sql.Timestamp(xmlFromDate.toGregorianCalendar().getTimeInMillis());
            }
            searchContext.put(EigendomInfo.FROMDATE, fromDate);
        }
        if (request.getToDate() != null) {
            java.sql.Timestamp toDate = null;
            XMLGregorianCalendar xmlToDate = request.getToDate();
            if (xmlToDate != null) {
                toDate = new java.sql.Timestamp(xmlToDate.toGregorianCalendar().getTimeInMillis());
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
            searchContext.put(EigendomInfo.OBJECT_PREFIX, objectprefix);
        }

        return searchContext;
    }

    /**
     * Omzetten van Soap request naar map met parameters met enige error
     * checking.
     *
     * @param request binnenkomend request met vraagstelling
     * @return map met parameters
     * @throws EigendomMutatieException if any
     */
    public static Map<String, Object> createEigendomMutatieContext(EigendomMutatieRequest request) throws EigendomMutatieException {
        Map<String, Object> searchContext = new HashMap<String, Object>();
        if (request == null) {
            return searchContext;
        }

        if (request.getIdentificatie() != null) {
            String[] sa = request.getIdentificatie().split(":");
            if (sa.length != 2) {
                throw new EigendomMutatieException("Ongeldige invoer", "De identificatie moet bestaan uit een getal met een namespace gescheiden door ':', zoals VBO:0000001!");
            }
            if (sa[0].equalsIgnoreCase("VBO") || sa[0].equalsIgnoreCase("NL.KAD.OnroerendeZaak")) {
                searchContext.put(EigendomInfo.NAMESPACE, sa[0]);
            } else {
                throw new EigendomMutatieException("Ongeldige invoer", "Geldige namespaces zijn: VBO en NL.KAD.OnroerendeZaak");
            }
            searchContext.put(EigendomInfo.IDENTIFICATIE, sa[1]);
        } else {
            throw new EigendomMutatieException("Ongeldige invoer", "Voor het opvragen is een identificatie vereist!");
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

    private static ArrayList<MutatieEntry> findMutatieEntries(Map<String, Object> searchContext) throws Exception {
        DataSource ds = ConfigUtil.getDataSourceStaging();
        PreparedStatement stm = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            String dbType = getDbType(conn);

            StringBuilder sql = createSelectStagingSQL(searchContext, dbType);
            LOG.trace(sql);
            LOG.trace(searchContext);
            stm = conn.prepareStatement(sql.toString());
            stm = addParamsSQL(stm, searchContext);
            rs = stm.executeQuery();

            ArrayList<MutatieEntry> entries = new ArrayList<>();
            while (rs.next()) {
                MutatieEntry entry = new MutatieEntry();
                entry.setObjectRef(rs.getString("object_ref"));
                GregorianCalendar gcalendar = new GregorianCalendar();
                gcalendar.setTime(rs.getTimestamp("datum"));
                XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcalendar);
                entry.setDatum(xmlDate);
                entry.setVolgnummer(rs.getString("volgordenummer"));
                gcalendar.setTime(rs.getTimestamp("status_datum"));
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

    private static ArrayList<EigendomMutatie> findEigendomMutatie(Map<String, Object> searchContext) throws Exception {

        DataSource ds = ConfigUtil.getDataSourceRsgb();
        StringBuilder sql = null;
        PreparedStatement stm = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            String dbType = getDbType(conn);

            sql = createSelectRsgbSQL(searchContext, dbType);
            LOG.trace(sql);
            LOG.trace(searchContext);
            stm = conn.prepareStatement(sql.toString());
            stm = addParamsSQL(stm, searchContext);
            rs = stm.executeQuery();

            ArrayList<EigendomMutatie> entries = new ArrayList<>();
            while (rs.next()) {
                EigendomMutatie entry = new EigendomMutatie();
                if (rs.getString("app_appartementsindex") != null) {
                    entry.setAppartementsindex(rs.getString("app_appartementsindex"));
                }
                if (rs.getString("app_gemeentecode") != null) {
                    entry.setGemeentecode(rs.getString("app_gemeentecode"));
                }
                if (rs.getString("app_identif") != null) {
                    entry.setIdentificatienummer(rs.getString("app_identif"));
                }
                if (rs.getString("app_perceelnummer") != null) {
                    entry.setPerceelnummer(rs.getString("app_perceelnummer"));
                }
                if (rs.getString("app_sectie") != null) {
                    entry.setSectie(rs.getString("app_sectie"));
                }
                if (rs.getString("perceel_gemeentecode") != null) {
                    entry.setGemeentecode(rs.getString("perceel_gemeentecode"));
                }
                if (rs.getString("perceel_identif") != null) {
                    entry.setIdentificatienummer(rs.getString("perceel_identif"));
                }
                if (rs.getString("perceel_perceelnummer") != null) {
                    entry.setPerceelnummer(rs.getString("perceel_perceelnummer"));
                }
                if (rs.getString("perceel_sectie") != null) {
                    entry.setSectie(rs.getString("perceel_sectie"));
                }

                long kad_id = rs.getLong("kad_identif");
                String vo_id = null;
                if (rs.getString("vo_identif") != null) {
                    vo_id = rs.getString("vo_identif");
                }

                Brondocumenten bdbo = findBrondocumenten(Long.toString(kad_id));
                entry.setBrondocumenten(bdbo);
                BenoemdObjecten bon = findBenoemdObjecten(vo_id);
                entry.setBenoemdObjecten(bon);
                HistorischeRelaties hrs = findHistorischeRelaties(kad_id);
                entry.setHistorischeRelaties(hrs);
                ZakelijkeRechten zrn = findZakelijkeRechten(kad_id);
                entry.setZakelijkeRechten(zrn);

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
     * SELECT identificatie, datum FROM brondocument where tabel = 'APP_RE' or
     * tabel = 'KAD_PERCEEL' and tabel_identificatie = ?; 
     * 
     * SELECT identificatie, datum FROM brondocument where tabel = 'verblijfsobject' or tabel =
     * 'standplaats' or tabel = 'ligplaats' and tabel_identificatie = ?; 
     * 
     * overige tabel waarden brondocument: openbareruimte KAD_ONRRND_ZAAK_AANTEK
     * nummeraanduiding ZAK_RECHT woonplaats BRONDOCUMENT pand
     *
     */
    private static Brondocumenten findBrondocumenten(String value) throws Exception {
        DataSource ds = ConfigUtil.getDataSourceRsgb();
        StringBuilder sql = null;
        PreparedStatement stm = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            String dbType = getDbType(conn);

            StringBuilder resultNames = new StringBuilder();
            resultNames.append(" bd.identificatie as identificatie,  ");
            resultNames.append(" bd.datum as datum ");
            StringBuilder fromSQL = new StringBuilder(" brondocument bd ");
            StringBuilder whereSQL = new StringBuilder();
            whereSQL.append(" (bd.tabel = 'APP_RE' or bd.tabel = 'KAD_PERCEEL' or bd.tabel = 'verblijfsobject') ");
            whereSQL.append(" and bd.tabel_identificatie = ? ");

            sql = createSelectSQL(resultNames, fromSQL,
                    whereSQL, null, dbType);
            LOG.trace(sql);
            LOG.trace(value);
            stm = conn.prepareStatement(sql.toString());
            stm.setString(1, value);
            rs = stm.executeQuery();

            Brondocumenten bdbo = new Brondocumenten();
            while (rs.next()) {
                Document entry = new Document();
                if (rs.getString("identificatie") != null) {
                    entry.setValue(rs.getString("identificatie"));
                }
                if (rs.getString("datum") != null) {
                    entry.setDatum(rs.getString("datum"));
                }
                bdbo.getDocument().add(entry);
            }

            return bdbo;
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

    private static BenoemdObjecten findBenoemdObjecten(String vo_id) throws Exception {
        DataSource ds = ConfigUtil.getDataSourceRsgb();
        StringBuilder sql = null;
        PreparedStatement stm = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            String dbType = getDbType(conn);

            StringBuilder resultNames = new StringBuilder();
            resultNames.append(" gobjd.gebruiksdoel_gebouwd_obj as gebruiksdoel,  ");
            resultNames.append(" gobj.oppervlakte_obj as oppervlakte, ");
            resultNames.append(" gobj.sc_identif as identificatie ");

            StringBuilder fromSQL = new StringBuilder();
            fromSQL.append(" gebouwd_obj_gebruiksdoel gobjd ");
            fromSQL.append(" INNER JOIN gebouwd_obj gobj ");
            fromSQL.append(" ON (gobjd.fk_gbo_sc_identif = gobj.sc_identif) ");

            StringBuilder whereSQL = new StringBuilder(" gobj.sc_identif = ? ");

            sql = createSelectSQL(resultNames, fromSQL,
                    whereSQL, null, dbType);
            LOG.trace(sql);
            LOG.trace(vo_id);
            stm = conn.prepareStatement(sql.toString());
            stm.setString(1, vo_id);
            rs = stm.executeQuery();

            BenoemdObjecten bon = new BenoemdObjecten();
            while (rs.next()) {
                BenoemdObject entry = new BenoemdObject();
                if (rs.getString("gebruiksdoel") != null) {
                    entry.setGebouwdObjectGebruiksdoel(rs.getString("gebruiksdoel"));
                }
                if (rs.getString("oppervlakte") != null) {
                    entry.setGebouwdObjectOppervlakte(rs.getString("oppervlakte"));
                }
                if (rs.getString("identificatie") != null) {
                    entry.setIdentificatienummer(rs.getString("identificatie"));
                }

                Brondocumenten bdn = findBrondocumenten(vo_id);
                entry.setBrondocumenten(bdn);
                bon.getBenoemdObject().add(entry);
            }

            return bon;
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

    private static HistorischeRelaties findHistorischeRelaties(long value) throws Exception {
        DataSource ds = ConfigUtil.getDataSourceRsgb();
        StringBuilder sql = null;
        PreparedStatement stm = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            String dbType = getDbType(conn);

            StringBuilder resultNames = new StringBuilder();
            resultNames.append(" hr.fk_sc_rh_koz_kad_identif as identificatie,  ");
            resultNames.append(" hr.aard as aard ");
            StringBuilder fromSQL = new StringBuilder(" kad_onrrnd_zk_his_rel hr ");
            StringBuilder whereSQL
                    = new StringBuilder(" hr.fk_sc_lh_koz_kad_identif = ? ");

            sql = createSelectSQL(resultNames, fromSQL,
                    whereSQL, null, dbType);
            LOG.trace(sql);
            LOG.trace(value);
            stm = conn.prepareStatement(sql.toString());
            stm.setLong(1, value);
            rs = stm.executeQuery();

            HistorischeRelaties hrs = new HistorischeRelaties();
            while (rs.next()) {
                HistorischeRelatie entry = new HistorischeRelatie();
                if (rs.getString("aard") != null) {
                    entry.setAard(rs.getString("aard"));
                }
                entry.setIdentificatienummer(Long.toString(rs.getLong("identificatie")));
                hrs.getHistorischeRelatie().add(entry);
            }

            return hrs;
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
     * Naast de rechten van het kadastrale objecten moeten bij appartementsrechten
     * ook nog de rechten van het grondperceel worden opgevraagd. Het bijbehorende
     * grondperceel wordt verkregen via de view: v_bd_app_re_all_kad_perceel.
     * 
     */
    private static ZakelijkeRechten findZakelijkeRechten(long kad_id) throws Exception {

        ZakelijkeRechten zrn = new ZakelijkeRechten();
        addZakelijkeRechten(zrn, kad_id, null);
        
        // zoek grondperceel grondperceel_kad_id
        long grondperceel_kad_id = 0l;
        DataSource ds = ConfigUtil.getDataSourceRsgb();
        StringBuilder sql = null;
        PreparedStatement stm = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            String dbType = getDbType(conn);

            StringBuilder resultNames = new StringBuilder();
            resultNames.append(" gpa.perceel_identif as perceel_identif ");
            StringBuilder fromSQL = new StringBuilder(" v_bd_app_re_all_kad_perceel gpa ");
            StringBuilder whereSQL = new StringBuilder();
            whereSQL.append(" gpa.app_re_identif = ? ");

            sql = createSelectSQL(resultNames, fromSQL,
                    whereSQL, null, dbType);
            LOG.trace(sql);
            LOG.trace(kad_id);
            stm = conn.prepareStatement(sql.toString());
            stm.setString(1, Long.toString(kad_id));
            rs = stm.executeQuery();

            if (rs.next()) {
                grondperceel_kad_id = rs.getLong("perceel_identif");
            }
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

        if (grondperceel_kad_id != 0l) {
            addZakelijkeRechten(zrn, grondperceel_kad_id, "grondperceel");
        }
        return zrn;
    }
    
    private static ZakelijkeRechten addZakelijkeRechten(ZakelijkeRechten zrn, long kad_id, String type) throws Exception {
        DataSource ds = ConfigUtil.getDataSourceRsgb();
        StringBuilder sql = null;
        PreparedStatement stm = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            String dbType = getDbType(conn);

            StringBuilder resultNames = new StringBuilder();
            resultNames.append(" zr.kadaster_identif as identificatie,  ");
            resultNames.append(" ar.omschr_aard_verkregenr_recht as aardrecht, ");
            resultNames.append(" zr.fk_8pes_sc_identif as subject_id, ");
            resultNames.append(" zr.ar_teller as teller, ");
            resultNames.append(" zr.ar_noemer as noemer, ");
            resultNames.append(" inp.bsn as bsn, ");
            resultNames.append(" innp.rsin as rsin, ");
            resultNames.append(" su.kvk_nummer as kvk, ");
            resultNames.append(" zra.aard_aantek_recht as aard, ");
            resultNames.append(" zra.beschrijving_aantek_recht as beschrijving, ");
            resultNames.append(" zra.eindd_aantek_recht as einddatum ");

            StringBuilder fromSQL = new StringBuilder("  zak_recht zr ");
            fromSQL.append(" LEFT OUTER JOIN aard_verkregen_recht ar ");
            fromSQL.append(" ON (zr.fk_3avr_aand = ar.aand) ");
            fromSQL.append(" LEFT OUTER JOIN zak_recht_aantek zra ");
            fromSQL.append(" ON ( zr.kadaster_identif = zra.fk_5zkr_kadaster_identif) ");
            fromSQL.append(" LEFT OUTER JOIN subject su ");
            fromSQL.append(" ON (su.identif = zr.fk_8pes_sc_identif) ");
            fromSQL.append(" LEFT OUTER JOIN ingeschr_nat_prs inp ");
            fromSQL.append(" ON (su.identif = inp.sc_identif) ");
            fromSQL.append(" LEFT OUTER JOIN ingeschr_niet_nat_prs innp ");
            fromSQL.append(" ON (su.identif = innp.sc_identif)  ");

            StringBuilder whereSQL = new StringBuilder();
//            if (type!=null && type.equals("grondperceel")) {
//                //van grondperceel bij app_re is alleen erfpacht interessant
//                //onderscheid tussen tenaamstelling en ander zakelijk recht onduidelijk
//                whereSQL.append(" zr.fk_3avr_aand = '3' ");
//            } else {
//                whereSQL.append(" zr.kadaster_identif like 'NL.KAD.Tenaamstelling%' ");
//            }
//            whereSQL.append(" and ");
            //zra.aard_aantek_recht <> 'Raadpleeg brondocument' ;            

            whereSQL.append(" zr.fk_7koz_kad_identif = ? ");
            
            sql = createSelectSQL(resultNames, fromSQL,
                    whereSQL, null, dbType);
            LOG.trace(sql);
            LOG.trace(kad_id);
            stm = conn.prepareStatement(sql.toString());
            stm.setLong(1, kad_id);
            rs = stm.executeQuery();

            while (rs.next()) {
                ZakelijkRecht entry = new ZakelijkRecht();
                if (rs.getString("identificatie") != null) {
                    entry.setIdentificatienummer(rs.getString("identificatie"));
                }
                if (rs.getString("aardrecht") != null) {
                    entry.setAardRecht(rs.getString("aardrecht"));
                }
                if (rs.getString("subject_id") != null) {
                    entry.setBSN("Kad-ID: " + rs.getString("subject_id"));
                }
                if (rs.getString("kvk")!=null) {
                    entry.setBSN("KvK: " + rs.getString("kvk"));
                }
                if (rs.getString("rsin")!=null) {
                    entry.setBSN("RSIN: " + rs.getString("rsin"));
                }
                if (rs.getString("bsn")!=null) {
                    entry.setBSN(rs.getString("bsn"));
                }
                if (rs.getString("teller") != null) {
                    entry.setTeller(rs.getString("teller"));
                }
                if (rs.getString("noemer") != null) {
                    entry.setNoemer(rs.getString("noemer"));
                }

                if (rs.getString("aard") != null) {
                    entry.setAardRecht(entry.getAardRecht() + 
                            " - " + rs.getString("aard"));
                }
                if (rs.getString("beschrijving") != null) {
                    entry.setAardRecht(entry.getAardRecht() + 
                            " - " + rs.getString("beschrijving"));
                }
                if (rs.getString("einddatum") != null) {
                    entry.setAardRecht(entry.getAardRecht() + 
                            " - " + rs.getString("einddatum"));
                }
                if (type!=null && type.equals("grondperceel")) {
                    entry.setAardRecht(entry.getAardRecht() + 
                            " [" + type + "]");
                }
                
                zrn.getZakelijkRecht().add(entry);
            }

            return zrn;
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

        StringBuilder resultNames = new StringBuilder();
        resultNames.append("koz.kad_identif as kad_identif, ");
        resultNames.append("ar.ka_appartementsindex as app_appartementsindex, ");
        resultNames.append("ar.ka_kad_gemeentecode as app_gemeentecode, ");
        resultNames.append("ar.sc_kad_identif as app_identif, ");
        resultNames.append("ar.ka_perceelnummer as app_perceelnummer, ");
        resultNames.append("ar.ka_sectie as app_sectie, ");
        resultNames.append("kp.ka_kad_gemeentecode as perceel_gemeentecode, ");
        resultNames.append("kp.sc_kad_identif as perceel_identif, ");
        resultNames.append("kp.ka_perceelnummer as perceel_perceelnummer, ");
        resultNames.append("kp.ka_sectie as perceel_sectie, ");
        resultNames.append("vo.sc_identif as vo_identif");

        StringBuilder fromSQL = new StringBuilder(" kad_onrrnd_zk koz ");
        fromSQL.append("LEFT OUTER JOIN kad_perceel kp ");
        fromSQL.append("ON ( koz.kad_identif = kp.sc_kad_identif) ");
        fromSQL.append("LEFT OUTER JOIN app_re ar ");
        fromSQL.append("ON ( koz.kad_identif = ar.sc_kad_identif) ");
        fromSQL.append("LEFT OUTER JOIN benoemd_obj_kad_onrrnd_zk bokoz ");
        fromSQL.append("ON ( koz.kad_identif = bokoz.fk_nn_rh_koz_kad_identif) ");
        fromSQL.append("LEFT OUTER JOIN verblijfsobj vo ");
        fromSQL.append("ON ( bokoz.fk_nn_lh_tgo_identif = vo.sc_identif) ");

        StringBuilder whereSQL = createWhereRsgbSQL(searchContext);

        return createSelectSQL(resultNames, fromSQL,
                whereSQL, searchContext, dbType);
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

        StringBuilder resultNames = new StringBuilder();
        resultNames.append(" b.object_ref as object_ref, ");
        resultNames.append(" b.datum as datum, ");
        resultNames.append(" b.volgordenummer as volgordenummer, ");
        resultNames.append(" b.status_datum as status_datum");

        StringBuilder fromSQL = new StringBuilder(" bericht b ");

        StringBuilder whereSQL = createWhereStagingSQL(searchContext);

        return createSelectSQL(resultNames, fromSQL,
                whereSQL, searchContext, dbType);

    }

    private static StringBuilder createSelectSQL(StringBuilder resultNames, StringBuilder fromSQL,
            StringBuilder whereSQL, Map<String, Object> searchContext, String dbType) throws ParseException {

        Integer maxrows = DBMAXRESULTS;
        if (searchContext != null) {
            maxrows = (Integer) searchContext.get(MAXAANTALRESULTATEN);
            if (maxrows == null) {
                maxrows = DBMAXRESULTS;
                searchContext.put(MAXAANTALRESULTATEN, DBMAXRESULTS);
            } else if (maxrows > DBMAXRESULTS) {
                searchContext.put(MAXAANTALRESULTATEN, DBMAXRESULTS);
            }
        }
        // één meer ophalen zodat je kunt weten of er meer zijn
        maxrows += 1;

        StringBuilder sql = new StringBuilder();
        switch (dbType) {
            case DB_POSTGRES:
                sql.append(" SELECT ");
                sql.append(resultNames);
                sql.append(" FROM ");
                sql.append(fromSQL);
                sql.append(" WHERE ");
                sql.append(whereSQL);
                sql.append(" LIMIT ");
                sql.append(maxrows);
                return sql;
            case DB_ORACLE:
                sql.append(" SELECT ");
                sql.append(resultNames);
                sql.append(" FROM ");
                sql.append(fromSQL);
                sql.append(" WHERE ");
                sql.append(whereSQL);
                StringBuilder tempSql = new StringBuilder();
                tempSql.append(" SELECT * FROM ( ");
                tempSql.append(sql);
                tempSql.append(" ) WHERE ROWNUM <= ");
                tempSql.append(maxrows);
                sql = tempSql;
                return sql;
            case DB_MSSQL:
                sql.append(" SELECT ");
                sql.append(" TOP ");
                sql.append(" ( ");
                sql.append(maxrows);
                sql.append(" ) ");
                sql.append(resultNames);
                sql.append(" FROM ");
                sql.append(fromSQL);
                sql.append(" WHERE ");
                sql.append(whereSQL);
                return sql;
            default:
                throw new UnsupportedOperationException("Unknown database!");
        }
    }

    private static StringBuilder createWhereRsgbSQL(
            Map<String, Object> searchContext) throws ParseException {
        StringBuilder sql = new StringBuilder();

        boolean first = true;
        String condition = null;
        if (searchContext.get(EigendomInfo.NAMESPACE).equals("VBO")) {
            condition = "    vo.sc_identif = ? ";
        } else if (searchContext.get(EigendomInfo.NAMESPACE).equals("NL.KAD.OnroerendeZaak")) {
            condition = "    koz.kad_identif = ? ";
            //hack: waarom hier numeric en hierboven string?
            Long i = null;
            if (searchContext.get(IDENTIFICATIE) instanceof String) {
                String id = (String) searchContext.get(IDENTIFICATIE);
                try {
                    i = Long.parseLong(id);
                } catch (NumberFormatException nfe) {
                    LOG.error("Identificatie is geen nummer. ", nfe);
                }
                searchContext.put(IDENTIFICATIE, i);
            }
        }
        addTerm(first, searchContext.get(IDENTIFICATIE), sql, condition);

        return sql;
    }

    private static StringBuilder createWhereStagingSQL(
            Map<String, Object> searchContext) throws ParseException {
        StringBuilder sql = new StringBuilder();

        boolean first = true;
        String condition;
        condition = "    b.object_ref like ? ";
        first = addTerm(first, searchContext.get(OBJECT_PREFIX), sql, condition);

        condition = "    b.status_datum >= ? ";
        first = addTerm(first, searchContext.get(FROMDATE), sql, condition);

        condition = "    b.status_datum <= ? ";
        addTerm(first, searchContext.get(TODATE), sql, condition);

        return sql;
    }

    private static boolean addTerm(boolean first, Object o, StringBuilder sql, String condition) {
        if (o != null) {
            if (!(o instanceof String) || !((String) o).isEmpty()) {
                if (!first) {
                    sql.append("AND ");
                }
                sql.append(condition);
                first = false;
            }
        }
        return first;
    }

    private static PreparedStatement addParamsSQL(PreparedStatement stm,
            Map<String, Object> searchContext) throws SQLException {

        int index = 1;
        index = addParam(index, searchContext.get(OBJECT_PREFIX), stm);
        index = addParam(index, searchContext.get(FROMDATE), stm);
        index = addParam(index, searchContext.get(TODATE), stm);
        addParam(index, searchContext.get(IDENTIFICATIE), stm);

        return stm;
    }

    private static int addParam(int index, Object o, PreparedStatement stm) throws SQLException {
        if (o != null) {
            if (!(o instanceof String) || !((String) o).isEmpty()) {
                if (o instanceof String) {
                    stm.setString(index++, "" + ((String) o) + "%");
                } else if (o instanceof java.sql.Timestamp) {
                    stm.setTimestamp(index++, (java.sql.Timestamp) o);
                } else {
                    stm.setObject(index++, o);
                }
            }
        }
        return index;
    }

    private static String getDbType(Connection conn) throws SQLException {
        String databaseProductName = conn.getMetaData().getDatabaseProductName();
        if (databaseProductName.contains("PostgreSQL")) {
            return DB_POSTGRES;
        } else if (databaseProductName.contains("Oracle")) {
            return DB_ORACLE;
        } else if (databaseProductName.contains("Microsoft")) {
            return DB_MSSQL;
        } else {
            throw new UnsupportedOperationException("Unknown database: " + conn.getMetaData().getDatabaseProductName());
        }
    }

}
