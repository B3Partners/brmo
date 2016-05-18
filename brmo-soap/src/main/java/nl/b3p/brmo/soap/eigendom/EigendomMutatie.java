package nl.b3p.brmo.soap.eigendom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;
import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlType;
import nl.b3p.brmo.soap.db.BrkInfo;

/**
 *
 * @author Chris
 */
@XmlType
public class EigendomMutatie {

    private String gemeentecode;
    private String sectie;
    private String perceelnummer;
    private String appReVolgnummer;
    private String identificatie;
    private Date datumBeginGeldigheid;
    private Date datumEindeGeldigheid;
    private String type; // appartement of perceel

    public EigendomMutatie() {
    }

    /**
     * Afkorting voor kadastrale gemeente als gebruikt in AKR
     *
     * @return the gemeentecode
     */
    public String getGemeentecode() {
        return gemeentecode;
    }

    /**
     * Afkorting voor kadastrale gemeente als gebruikt in AKR
     *
     *
     * @param gemeentecode the gemeentecode to set
     */
    public void setGemeentecode(String gemeentecode) {
        this.gemeentecode = gemeentecode;
    }

    /**
     * Letter van sectie
     *
     * @return the sectie
     */
    public String getSectie() {
        return sectie;
    }

    /**
     * Letter van sectie
     *
     * @param sectie the sectie to set
     */
    public void setSectie(String sectie) {
        this.sectie = sectie;
    }

    /**
     * nummer van het perceel
     *
     * @return the perceelnummer
     */
    public String getPerceelnummer() {
        return perceelnummer;
    }

    /**
     * nummer van het perceel
     *
     * @param perceelnummer the perceelnummer to set
     */
    public void setPerceelnummer(String perceelnummer) {
        this.perceelnummer = perceelnummer;
    }

    /**
     * In geval van een appartement kan hier het volgnummer worden opgegeven
     *
     * @return the appReVolgnummer
     */
    public String getAppReVolgnummer() {
        return appReVolgnummer;
    }

    /**
     * In geval van een appartement kan hier het volgnummer worden opgegeven
     *
     * @param appReVolgnummer the appReVolgnummer to set
     */
    public void setAppReVolgnummer(String appReVolgnummer) {
        this.appReVolgnummer = appReVolgnummer;
    }

    /**
     * Formele kadatstrale object nummer
     *
     * @return the identificatie
     */
    public String getIdentificatie() {
        return identificatie;
    }

    /**
     * Formele kadatstrale object nummer
     *
     * @param identificatie the identificatie to set
     */
    public void setIdentificatie(String identificatie) {
        this.identificatie = identificatie;
    }

    /**
     * @return the datumBeginGeldigheid
     */
    public Date getDatumBeginGeldigheid() {
        return datumBeginGeldigheid;
    }

    /**
     * @param datumBeginGeldigheid the datumBeginGeldigheid to set
     */
    public void setDatumBeginGeldigheid(Date datumBeginGeldigheid) {
        this.datumBeginGeldigheid = datumBeginGeldigheid;
    }

    /**
     * @return the datumEindeGeldigheid
     */
    public Date getDatumEindeGeldigheid() {
        return datumEindeGeldigheid;
    }

    /**
     * @param datumEindeGeldigheid the datumEindeGeldigheid to set
     */
    public void setDatumEindeGeldigheid(Date datumEindeGeldigheid) {
        this.datumEindeGeldigheid = datumEindeGeldigheid;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    private static StringBuilder createFullColumnsSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("    app_re.ka_sectie AS app_re_sectie,");
        sql.append("    app_re.ka_perceelnummer  AS app_re_perceelnummer,");
        sql.append("    app_re.ka_kad_gemeentecode AS app_re_kad_gemeentecode,");
        sql.append("    app_re.ka_appartementsindex,");
        sql.append("    kad_perceel.ka_sectie,");
        sql.append("    kad_perceel.ka_perceelnummer,");
        sql.append("    kad_perceel.ka_kad_gemeentecode,");
        sql.append("    kad_perceel.ka_deelperceelnummer,");
        sql.append("    kad_onrrnd_zk.dat_beg_geldh,");
        sql.append("    kad_onrrnd_zk.datum_einde_geldh,");
        sql.append("    kad_onrrnd_zk.kad_identif,");
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
        return sql;
    }

    private static StringBuilder createWhereSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("    kad_onrrnd_zk.kad_identif = ? ");
        return sql;
    }

    public static EigendomMutatie getRecord(Long id,
            Map<String, Object> searchContext) throws Exception {

        DataSource ds = BrkInfo.getDataSourceRsgb();
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

            stm = connRsgb.prepareStatement(sql.toString());
            stm.setLong(1, id);
            rs = stm.executeQuery();

            EigendomMutatie koz = new EigendomMutatie();
            if (rs.next()) {
                koz.setIdentificatie(Long.toString(rs.getLong("kad_identif")));
                koz.setDatumBeginGeldigheid(rs.getDate("dat_beg_geldh"));
                koz.setDatumEindeGeldigheid(rs.getDate("datum_einde_geldh"));
 
                String type = "perceel";
                if (rs.getString("ka_appartementsindex") != null) {
                    type = "appartement";
                }
                koz.setType(type);
                if (type.equalsIgnoreCase("appartement")) {
                    koz.setGemeentecode(rs.getString("app_re_kad_gemeentecode"));
                    koz.setPerceelnummer(rs.getString("app_re_perceelnummer"));
                    koz.setSectie(rs.getString("app_re_sectie"));
                    koz.setAppReVolgnummer(rs.getString("ka_appartementsindex"));
                } else {
                    koz.setGemeentecode(rs.getString("ka_kad_gemeentecode"));
                    koz.setPerceelnummer(rs.getString("ka_perceelnummer"));
                    koz.setSectie(rs.getString("ka_sectie"));
                }


            } else {
                return null;
            }
            return koz;

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
