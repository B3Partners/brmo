package nl.b3p.brmo.soap.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Chris
 */
public class BrkInfo {

    private static final Log log = LogFactory.getLog(BrkInfo.class);

    private static final String JNDI_NAME = "java:comp/env";
    private static final String JDBC_NAME_RSGB = "jdbc/brmo/rsgb";

    public static ArrayList<Integer> findKozIDs(String appReVolgnummer, String gemeenteNaam, String gemeentecode,
            Integer huisnummer, Integer identificatie, String perceelnummer,
            String postcode, String sectie, String straatNaam,
            String subjectNaam, String zoekgebied) throws Exception {
        
        DataSource ds = getDataSourceRsgb();
        Connection connRsgb = ds.getConnection();

        String sql = createSelectSQL(
            appReVolgnummer, gemeenteNaam, gemeentecode,
            huisnummer, identificatie, perceelnummer,
            postcode, sectie, straatNaam,
            subjectNaam, zoekgebied);

        PreparedStatement stm = connRsgb.prepareStatement(sql);
        
        stm = addParamsSQL(stm,
            appReVolgnummer, gemeenteNaam, gemeentecode,
            huisnummer, identificatie, perceelnummer,
            postcode, sectie, straatNaam,
            subjectNaam, zoekgebied);
        
        ResultSet rs = stm.executeQuery();
        
        ArrayList<Integer> ids = new ArrayList<Integer>();
        while (rs.next()) {
            Integer id = rs.getInt("identificatie");
            ids.add(id);
        }
        rs.close();

        return ids;
    }

    private static String createSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT");
        sql.append("    app_re.ka_sectie,");
        sql.append("    app_re.ka_perceelnummer,");
        sql.append("    app_re.ka_kad_gemeentecode,");
        sql.append("    app_re.ka_appartementsindex,");
        sql.append("    kad_perceel.ka_sectie,");
        sql.append("    kad_perceel.ka_perceelnummer,");
        sql.append("    kad_perceel.ka_kad_gemeentecode,");
        sql.append("    kad_perceel.ka_deelperceelnummer,");
        sql.append("    kad_onrrnd_zk.dat_beg_geldh,");
        sql.append("    kad_onrrnd_zk.datum_einde_geldh,");
        sql.append("    kad_onrrnd_zk.kad_identif,");
        sql.append("    kad_perceel.aand_soort_grootte,");
        sql.append("    kad_onrrnd_zk.cu_aard_cultuur_onbebouwd,");
        sql.append("    kad_onrrnd_zk.lr_bedrag,");
        sql.append("    kad_perceel.begrenzing_perceel,");
        sql.append("    kad_perceel.grootte_perceel,");
        sql.append("    kad_onrrnd_zk.ks_koopjaar,");
        sql.append("    kad_onrrnd_zk.ks_meer_onroerendgoed,");
        sql.append("    kad_perceel.omschr_deelperceel,");
        sql.append("    zak_recht.indic_betrokken_in_splitsing,");
        sql.append("    zak_recht.ar_noemer,");
        sql.append("    zak_recht.ar_teller,");
        sql.append("    zak_recht.fk_3avr_aand,");
        sql.append("    nat_prs.geslachtsaand,");
        sql.append("    nat_prs.nm_geslachtsnaam,");
        sql.append("    nat_prs.nm_voornamen,");
        sql.append("    nat_prs.nm_voorvoegsel_geslachtsnaam,");
        sql.append("    ander_nat_prs.geboortedatum,");
        sql.append("    ander_nat_prs.overlijdensdatum,");
        sql.append("    ingeschr_nat_prs.gb_geboortedatum,");
        sql.append("    ingeschr_nat_prs.bsn,");
        sql.append("    ingeschr_nat_prs.gb_geboorteplaats,");
        sql.append("    ingeschr_nat_prs.va_loc_beschrijving,");
        sql.append("    ingeschr_niet_nat_prs.rechtsvorm,");
        sql.append("    ingeschr_niet_nat_prs.statutaire_zetel,");
        sql.append("    addresseerb_obj_aand.huinummer,");
        sql.append("    addresseerb_obj_aand.postcode,");
        sql.append("    gem_openb_rmte.straatnaam,");
        sql.append("    wnplts.naam");
        sql.append("FROM");
        sql.append("    kad_onrrnd_zk");
        sql.append("LEFT OUTER JOIN");
        sql.append("    kad_perceel");
        sql.append("ON");
        sql.append("    (");
        sql.append("        kad_onrrnd_zk.kad_identif = kad_perceel.sc_kad_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    app_re");
        sql.append("ON");
        sql.append("    (");
        sql.append("        kad_onrrnd_zk.kad_identif = app_re.sc_kad_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    zak_recht");
        sql.append("ON");
        sql.append("    (");
        sql.append("        kad_onrrnd_zk.kad_identif = zak_recht.fk_7koz_kad_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    niet_nat_prs");
        sql.append("ON");
        sql.append("    (");
        sql.append("        zak_recht.fk_8pes_sc_identif = niet_nat_prs.sc_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    nat_prs");
        sql.append("ON");
        sql.append("    (");
        sql.append("        zak_recht.fk_8pes_sc_identif = nat_prs.sc_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    ander_nat_prs");
        sql.append("ON");
        sql.append("    (");
        sql.append("        nat_prs.sc_identif = ander_nat_prs.sc_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    ingeschr_nat_prs");
        sql.append("ON");
        sql.append("    (");
        sql.append("        nat_prs.sc_identif = ingeschr_nat_prs.sc_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    ingeschr_niet_nat_prs");
        sql.append("ON");
        sql.append("    (");
        sql.append("        niet_nat_prs.sc_identif = ingeschr_niet_nat_prs.sc_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    benoemd_obj_kad_onrrnd_zk");
        sql.append("ON");
        sql.append("    (");
        sql.append("        kad_onrrnd_zk.kad_identif =");
        sql.append("        benoemd_obj_kad_onrrnd_zk.fk_nn_rh_koz_kad_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    verblijfsobj");
        sql.append("ON");
        sql.append("    (");
        sql.append("        benoemd_obj_kad_onrrnd_zk.fk_nn_lh_tgo_identif = verblijfsobj.sc_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    addresseerb_obj_aand");
        sql.append("ON");
        sql.append("    (");
        sql.append("        verblijfsobj.fk_11nra_sc_identif = addresseerb_obj_aand.identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    gem_openb_rmte");
        sql.append("ON");
        sql.append("    (");
        sql.append("        addresseerb_obj_aand.fk_7opr_identifcode = gem_openb_rmte.identifcode)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    openb_rmte_wnplts");
        sql.append("ON");
        sql.append("    (");
        sql.append("        gem_openb_rmte.identifcode = openb_rmte_wnplts.fk_nn_lh_opr_identifcode)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    wnplts");
        sql.append("ON");
        sql.append("    (");
        sql.append("        openb_rmte_wnplts.fk_nn_rh_wpl_identif = wnplts.identif)");
        sql.append("WHERE");
        sql.append("    kad_onrrnd_zk.kad_identif = ?"); //1 int
        sql.append("AND (");
        sql.append("        nat_prs.nm_geslachtsnaam = ?"); //2 string
        sql.append("    OR niet_nat_prs.naam = ?)"); //2a string
        sql.append("AND kad_perceel.begrenzing_perceel = ?"); //4 geom->wkt
        sql.append("AND app_re.ka_appartementsindex = ?"); //5 string
        sql.append("AND (");
        sql.append("        app_re.ka_kad_gemeentecode = ?"); //6 string
        sql.append("    OR kad_perceel.ka_kad_gemeentecode = ?)"); //6a string
        sql.append("AND (");
        sql.append("        app_re.ka_perceelnummer = ?"); //7 string
        sql.append("    OR kad_perceel.ka_perceelnummer = ?)"); //7a string
        sql.append("AND (");
        sql.append("        app_re.ka_sectie = ?"); //8 string
        sql.append("    OR kad_perceel.ka_sectie = ?)"); //8a string
        sql.append("AND wnplts.naam = ?"); //9 string
        sql.append("AND gem_openb_rmte.straatnaam = ?"); //10 string
        sql.append("AND addresseerb_obj_aand.huinummer = ?"); //11 int
        sql.append("AND addresseerb_obj_aand.postcode = ? ;"); //12 string
        return sql.toString();
    }

    private static String createSelectSQL(
            String appReVolgnummer, String gemeenteNaam, String gemeentecode,
            Integer huisnummer, Integer identificatie, String perceelnummer,
            String postcode, String sectie, String straatNaam,
            String subjectNaam, String zoekgebied) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT");
        sql.append("    kad_onrrnd_zk.kad_identif AS identificatie");
        sql.append("FROM");
        sql.append("    kad_onrrnd_zk");
        sql.append("LEFT OUTER JOIN");
        sql.append("    kad_perceel");
        sql.append("ON");
        sql.append("    (");
        sql.append("        kad_onrrnd_zk.kad_identif = kad_perceel.sc_kad_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    app_re");
        sql.append("ON");
        sql.append("    (");
        sql.append("        kad_onrrnd_zk.kad_identif = app_re.sc_kad_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    zak_recht");
        sql.append("ON");
        sql.append("    (");
        sql.append("        kad_onrrnd_zk.kad_identif = zak_recht.fk_7koz_kad_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    niet_nat_prs");
        sql.append("ON");
        sql.append("    (");
        sql.append("        zak_recht.fk_8pes_sc_identif = niet_nat_prs.sc_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    nat_prs");
        sql.append("ON");
        sql.append("    (");
        sql.append("        zak_recht.fk_8pes_sc_identif = nat_prs.sc_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    ander_nat_prs");
        sql.append("ON");
        sql.append("    (");
        sql.append("        nat_prs.sc_identif = ander_nat_prs.sc_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    ingeschr_nat_prs");
        sql.append("ON");
        sql.append("    (");
        sql.append("        nat_prs.sc_identif = ingeschr_nat_prs.sc_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    ingeschr_niet_nat_prs");
        sql.append("ON");
        sql.append("    (");
        sql.append("        niet_nat_prs.sc_identif = ingeschr_niet_nat_prs.sc_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    benoemd_obj_kad_onrrnd_zk");
        sql.append("ON");
        sql.append("    (");
        sql.append("        kad_onrrnd_zk.kad_identif =");
        sql.append("        benoemd_obj_kad_onrrnd_zk.fk_nn_rh_koz_kad_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    verblijfsobj");
        sql.append("ON");
        sql.append("    (");
        sql.append("        benoemd_obj_kad_onrrnd_zk.fk_nn_lh_tgo_identif = verblijfsobj.sc_identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    addresseerb_obj_aand");
        sql.append("ON");
        sql.append("    (");
        sql.append("        verblijfsobj.fk_11nra_sc_identif = addresseerb_obj_aand.identif)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    gem_openb_rmte");
        sql.append("ON");
        sql.append("    (");
        sql.append("        addresseerb_obj_aand.fk_7opr_identifcode = gem_openb_rmte.identifcode)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    openb_rmte_wnplts");
        sql.append("ON");
        sql.append("    (");
        sql.append("        gem_openb_rmte.identifcode = openb_rmte_wnplts.fk_nn_lh_opr_identifcode)");
        sql.append("LEFT OUTER JOIN");
        sql.append("    wnplts");
        sql.append("ON");
        sql.append("    (");
        sql.append("        openb_rmte_wnplts.fk_nn_rh_wpl_identif = wnplts.identif)");
        sql.append("WHERE");

        boolean first = true;
        String condition = "";
        condition = "    kad_onrrnd_zk.kad_identif = ?"; //1 int
        first = addTerm(first, identificatie, sql, condition);

        condition = "   (    nat_prs.nm_geslachtsnaam = ?"
                + "    OR niet_nat_prs.naam = ?)"; //2 string
        first = addTerm(first, subjectNaam, sql, condition);

        condition = "kad_perceel.begrenzing_perceel = ?"; //4 geom->wkt
        first = addTerm(first, zoekgebied, sql, condition);

        condition = "app_re.ka_appartementsindex = ?"; //5 string
        first = addTerm(first, appReVolgnummer, sql, condition);

        condition = "   (    app_re.ka_kad_gemeentecode = ?"
                + "    OR kad_perceel.ka_kad_gemeentecode = ?)"; //6 string
        first = addTerm(first, gemeentecode, sql, condition);

        condition = "   (    app_re.ka_perceelnummer = ?"
                + "    OR kad_perceel.ka_perceelnummer = ?)"; //7 string
        first = addTerm(first, perceelnummer, sql, condition);

        condition = "   (    app_re.ka_sectie = ?"
                + "    OR kad_perceel.ka_sectie = ?)"; //8 string
        first = addTerm(first, sectie, sql, condition);

        condition = "wnplts.naam = ?"; //9 string is woonplaats!
        first = addTerm(first, gemeenteNaam, sql, condition);

        condition = "gem_openb_rmte.straatnaam = ?"; //10 string geeft null!
        first = addTerm(first, straatNaam, sql, condition);

        condition = "addresseerb_obj_aand.huinummer = ?"; //11 int
        first = addTerm(first, huisnummer, sql, condition);

        condition = "addresseerb_obj_aand.postcode = ?"; //12 string
        addTerm(first, postcode, sql, condition);

        return sql.toString();
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
            String appReVolgnummer, String gemeenteNaam, String gemeentecode,
            Integer huisnummer, Integer identificatie, String perceelnummer,
            String postcode, String sectie, String straatNaam,
            String subjectNaam, String zoekgebied) throws SQLException {

        int index = 1;
        index = addParam(index, identificatie, stm);
        index = addParam(index, subjectNaam, stm);
        index = addParam(index, subjectNaam, stm);
        index = addParam(index, zoekgebied, stm);
        index = addParam(index, appReVolgnummer, stm);
        index = addParam(index, gemeentecode, stm);
        index = addParam(index, gemeentecode, stm);
        index = addParam(index, perceelnummer, stm);
        index = addParam(index, perceelnummer, stm);
        index = addParam(index, sectie, stm);
        index = addParam(index, sectie, stm);
        index = addParam(index, gemeenteNaam, stm);
        index = addParam(index, straatNaam, stm);
        index = addParam(index, huisnummer, stm);
        addParam(index, postcode, stm);

        return stm;
    }

    private static int addParam(int index, Object o, PreparedStatement stm) throws SQLException {
        if (o != null) {
            if (o instanceof Integer) {
                stm.setInt(index++, (Integer) o);
            } else if (o instanceof String) {
                stm.setString(index++, (String) o);
            } else {
                stm.setObject(index++, o);
            }
        }
        return index;
    }

    public static DataSource getDataSourceRsgb() throws Exception {
        DataSource ds = null;
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

}
