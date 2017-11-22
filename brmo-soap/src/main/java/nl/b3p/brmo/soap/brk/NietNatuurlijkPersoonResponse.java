package nl.b3p.brmo.soap.brk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Chris
 */
@XmlType
public class NietNatuurlijkPersoonResponse {
    private static final Log LOG = LogFactory.getLog(NietNatuurlijkPersoonResponse.class);
    private String identificatie;
    private String rechtsvorm;
    private String statutaireZetel;
    private String naam;

    /**
     * @return the identificatie
     */
    @XmlElement
    public String getIdentificatie() {
        return identificatie;
    }

    /**
     * @param identificatie the identificatie to set
     */
    public void setIdentificatie(String identificatie) {
        this.identificatie = identificatie;
    }

    /**
     * @return the rechtsvorm
     */
    public String getRechtsvorm() {
        return rechtsvorm;
    }

    /**
     * @param rechtsvorm the rechtsvorm to set
     */
    public void setRechtsvorm(String rechtsvorm) {
        this.rechtsvorm = rechtsvorm;
    }

    /**
     * @return the statutaireZetel
     */
    public String getStatutaireZetel() {
        return statutaireZetel;
    }

    /**
     * @param statutaireZetel the statutaireZetel to set
     */
    public void setStatutaireZetel(String statutaireZetel) {
        this.statutaireZetel = statutaireZetel;
    }

    /**
     * @return the naam
     */
    public String getNaam() {
        return naam;
    }

    /**
     * @param naam the naam to set
     */
    public void setNaam(String naam) {
        this.naam = naam;
    }

    private static StringBuilder createFullColumnsSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("    niet_nat_prs.naam,");
        sql.append("    ingeschr_niet_nat_prs.rechtsvorm,");
        sql.append("    ingeschr_niet_nat_prs.statutaire_zetel ");
        return sql;
    }

    private static StringBuilder createFromSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("    niet_nat_prs ");
        sql.append("LEFT OUTER JOIN ");
        sql.append("    ingeschr_niet_nat_prs ");
        sql.append("ON ");
        sql.append("    ( ");
        sql.append("        niet_nat_prs.sc_identif = ingeschr_niet_nat_prs.sc_identif) ");
        return sql;
    }

    private static StringBuilder createWhereSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("    niet_nat_prs.sc_identif = ? ");
        return sql;
    }

    public static NietNatuurlijkPersoonResponse getRecordById(String id,
            Map<String, Object> searchContext) throws Exception {

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
            LOG.trace(id);
            stm = connRsgb.prepareStatement(sql.toString());
            stm.setString(1, id);
            rs = stm.executeQuery();

            NietNatuurlijkPersoonResponse nnp = new NietNatuurlijkPersoonResponse();
            if (rs.next()) {
                nnp.setNaam(rs.getString("naam"));
                nnp.setRechtsvorm(rs.getString("rechtsvorm"));
                nnp.setStatutaireZetel(rs.getString("statutaire_zetel"));

            } else {
                return null;
            }

            return nnp;
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
