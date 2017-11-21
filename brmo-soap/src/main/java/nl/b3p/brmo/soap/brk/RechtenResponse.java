package nl.b3p.brmo.soap.brk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlElement;
import nl.b3p.brmo.service.util.ConfigUtil;
import nl.b3p.brmo.soap.db.BrkInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Chris
 */
public class RechtenResponse {
    private static final Log LOG = LogFactory.getLog(RechtenResponse.class);
    private List<ZakelijkRechtResponse> zakelijkRecht = null;

    /**
     * @return the zakelijkRecht
     */
    @XmlElement
    public List<ZakelijkRechtResponse> getZakelijkRecht() {
        return zakelijkRecht;
    }

    /**
     * @param zakelijkRecht the zakelijkRecht to set
     */
    public void setZakelijkRecht(List<ZakelijkRechtResponse> zakelijkRecht) {
        this.zakelijkRecht = zakelijkRecht;
    }

    private static StringBuilder createFullColumnsSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("    zak_recht.indic_betrokken_in_splitsing,");
        sql.append("    zak_recht.ar_noemer,");
        sql.append("    zak_recht.ar_teller,");
        sql.append("    zak_recht.fk_3avr_aand,");
        sql.append("    zak_recht.fk_8pes_sc_identif, ");
        sql.append("    aard_recht_verkort.omschr ");
        return sql;
    }

    private static StringBuilder createFromSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("    zak_recht ");
        sql.append("INNER JOIN ");
        sql.append("    aard_recht_verkort ");
        sql.append("ON ");
        sql.append("    ( ");
        sql.append("        zak_recht.fk_3avr_aand = aard_recht_verkort.aand) ");
        return sql;
    }

    private static StringBuilder createWhereSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("    zak_recht.fk_7koz_kad_identif = ? ");
        // alleen rechten gekoppeld aan subjecten tonen
        sql.append("AND    fk_8pes_sc_identif is not null ");
        return sql;
    }

    public static RechtenResponse getRechtenByKoz(Long kozId,
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
            LOG.trace(kozId);
            stm = connRsgb.prepareStatement(sql.toString());
            stm.setObject(1, kozId);
            rs = stm.executeQuery();

            RechtenResponse rr = null;
            List<ZakelijkRechtResponse> zrl = null;
            ZakelijkRechtResponse zkRecht = null;
            while (rs.next()) {
                if (rr == null) {
                    rr = new RechtenResponse();
                    zrl = new ArrayList<ZakelijkRechtResponse>();
                    rr.setZakelijkRecht(zrl);
                }
                zkRecht = new ZakelijkRechtResponse();

//                zkRecht.setAardVerkregenRecht(rs.getString("fk_3avr_aand"));
                zkRecht.setAardVerkregenRecht(rs.getString("omschr"));
                zkRecht.setIndicatieBetrokkenInSplitsing(
                        rs.getString("indic_betrokken_in_splitsing")
                        .equalsIgnoreCase("nee") ? false : true);
                zkRecht.setNoemer(rs.getInt("ar_noemer"));
                zkRecht.setTeller(rs.getInt("ar_teller"));

                Boolean st = (Boolean) searchContext.get(BrkInfo.SUBJECTSTOEVOEGEN);
                if (st != null && st) {
                    String pid = rs.getString("fk_8pes_sc_identif");
                    NatuurlijkPersoonResponse np
                            = NatuurlijkPersoonResponse.getRecordById(pid, searchContext);
                    zkRecht.setNatuurlijkPersoon(np);
                    if (np == null) {
                        NietNatuurlijkPersoonResponse nnp
                                = NietNatuurlijkPersoonResponse.getRecordById(pid, searchContext);
                        zkRecht.setNietNatuurlijkPersoon(nnp);
                    }
                }
                zrl.add(zkRecht);

            }

            return rr;
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
