package nl.b3p.brmo.soap.brk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlElement;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Chris
 */
public class AdressenResponse {
    private static final Log LOG = LogFactory.getLog(AdressenResponse.class);

    private List<AdresResponse> BAGAdres = null;

    /**
     * @return the BAGAdres
     */
    @XmlElement
    public List<AdresResponse> getBAGAdres() {
        return BAGAdres;
    }

    /**
     * @param BAGAdres the BAGAdres to set
     */
    public void setBAGAdres(List<AdresResponse> BAGAdres) {
        this.BAGAdres = BAGAdres;
    }

    private static StringBuilder createFullColumnsSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("    addresseerb_obj_aand.huinummer,");
        sql.append("    addresseerb_obj_aand.huinummertoevoeging,");
        sql.append("    addresseerb_obj_aand.huisletter,");
        sql.append("    addresseerb_obj_aand.postcode,");
        sql.append("    gem_openb_rmte.naam_openb_rmte,");
        sql.append("    wnplts.naam ");
        return sql;
    }

    private static StringBuilder createFromSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("    benoemd_obj_kad_onrrnd_zk ");
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

    private static StringBuilder createWhereSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("    benoemd_obj_kad_onrrnd_zk.fk_nn_rh_koz_kad_identif = ? ");
        return sql;
    }

    public static AdressenResponse getAdressenByKoz(Long kozId) throws Exception {

        DataSource ds = ConfigUtil.getDataSourceRsgb();
        PreparedStatement stm = null;
        Connection connRsgb = null;
        ResultSet rs = null;
        try {
            connRsgb = ds.getConnection();

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            sql.append(createFullColumnsSQL());
            sql.append("FROM ");
            sql.append(createFromSQL());
            sql.append("WHERE ");
            sql.append(createWhereSQL());
            LOG.trace(sql);
            LOG.trace(kozId);
            stm = connRsgb.prepareStatement(sql.toString());
            stm.setObject(1, kozId);
            rs = stm.executeQuery();

            AdressenResponse ar = null;
            List<AdresResponse> arl = null;
            AdresResponse bagAdres = null;
            while (rs.next()) {
                if (ar == null) {
                    ar = new AdressenResponse();
                    arl = new ArrayList<>();
                    ar.setBAGAdres(arl);
                }
                bagAdres = new AdresResponse();

                bagAdres.setWoonplaatsNaam(rs.getString("naam"));
                bagAdres.setHuisLetter(rs.getString("huisletter"));
                bagAdres.setHuisLetterToevoeging(rs.getString("huinummertoevoeging"));
                bagAdres.setHuisnummer(rs.getInt("huinummer"));
                bagAdres.setPostcode(rs.getString("postcode"));
                bagAdres.setStraatNaam(rs.getString("naam_openb_rmte"));

                arl.add(bagAdres);
            }
 
            return ar;
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

}
