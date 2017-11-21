package nl.b3p.brmo.soap.brk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import nl.b3p.brmo.service.util.ConfigUtil;
import nl.b3p.brmo.soap.db.BrkInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Chris
 */
@XmlType
public class NatuurlijkPersoonResponse {

    private static final Log LOG = LogFactory.getLog(NatuurlijkPersoonResponse.class);

    private String identificatie;
    private Integer geboortedatum;
    private Integer overlijdensdatum;
    private String bsn;
    private String geboorteplaats;
    private String locatieBeschrijving;
    private String aanduidingNaamgebruik;
    private String geslachtsaanduiding;
    private String geslachtsnaam;
    private String voornamen;
    private String voorvoegsel;

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
     * @return the geboortedatum
     */
    public Integer getGeboortedatum() {
        return geboortedatum;
    }

    /**
     * @param geboortedatum the geboortedatum to set
     */
    public void setGeboortedatum(Integer geboortedatum) {
        this.geboortedatum = geboortedatum;
    }

    /**
     * @return the overlijdensdatum
     */
    public Integer getOverlijdensdatum() {
        return overlijdensdatum;
    }

    /**
     * @param overlijdensdatum the overlijdensdatum to set
     */
    public void setOverlijdensdatum(Integer overlijdensdatum) {
        this.overlijdensdatum = overlijdensdatum;
    }

    /**
     * @return the bsn
     */
    public String getBsn() {
        return bsn;
    }

    /**
     * @param bsn the bsn to set
     */
    public void setBsn(String bsn) {
        this.bsn = bsn;
    }

    /**
     * @return the geboorteplaats
     */
    public String getGeboorteplaats() {
        return geboorteplaats;
    }

    /**
     * @param geboorteplaats the geboorteplaats to set
     */
    public void setGeboorteplaats(String geboorteplaats) {
        this.geboorteplaats = geboorteplaats;
    }

    /**
     * @return the locatieBeschrijving
     */
    public String getLocatieBeschrijving() {
        return locatieBeschrijving;
    }

    /**
     * @param locatieBeschrijving the locatieBeschrijving to set
     */
    public void setLocatieBeschrijving(String locatieBeschrijving) {
        this.locatieBeschrijving = locatieBeschrijving;
    }

    /**
     * @return the aanduidingNaamgebruik
     */
    public String getAanduidingNaamgebruik() {
        return aanduidingNaamgebruik;
    }

    /**
     * @param aanduidingNaamgebruik the aanduidingNaamgebruik to set
     */
    public void setAanduidingNaamgebruik(String aanduidingNaamgebruik) {
        this.aanduidingNaamgebruik = aanduidingNaamgebruik;
    }

    /**
     * @return the geslachtsaanduiding
     */
    public String getGeslachtsaanduiding() {
        return geslachtsaanduiding;
    }

    /**
     * @param geslachtsaanduiding the geslachtsaanduiding to set
     */
    public void setGeslachtsaanduiding(String geslachtsaanduiding) {
        this.geslachtsaanduiding = geslachtsaanduiding;
    }

    /**
     * @return the geslachtsnaam
     */
    public String getGeslachtsnaam() {
        return geslachtsnaam;
    }

    /**
     * @param geslachtsnaam the geslachtsnaam to set
     */
    public void setGeslachtsnaam(String geslachtsnaam) {
        this.geslachtsnaam = geslachtsnaam;
    }

    /**
     * @return the voornamen
     */
    public String getVoornamen() {
        return voornamen;
    }

    /**
     * @param voornamen the voornamen to set
     */
    public void setVoornamen(String voornamen) {
        this.voornamen = voornamen;
    }

    /**
     * @return the voorvoegsel
     */
    public String getVoorvoegsel() {
        return voorvoegsel;
    }

    /**
     * @param voorvoegsel the voorvoegsel to set
     */
    public void setVoorvoegsel(String voorvoegsel) {
        this.voorvoegsel = voorvoegsel;
    }

    private static StringBuilder createFullColumnsSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("    nat_prs.geslachtsaand,");
        sql.append("    nat_prs.nm_geslachtsnaam,");
        sql.append("    nat_prs.nm_voornamen,");
        sql.append("    nat_prs.nm_voorvoegsel_geslachtsnaam,");
        sql.append("    ander_nat_prs.geboortedatum,");
        sql.append("    ander_nat_prs.overlijdensdatum,");
        sql.append("    ingeschr_nat_prs.gb_geboortedatum,");
        sql.append("    ingeschr_nat_prs.bsn,");
        sql.append("    ingeschr_nat_prs.gb_geboorteplaats,");
        sql.append("    ingeschr_nat_prs.va_loc_beschrijving ");
        return sql;
    }

    private static StringBuilder createFromSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("    nat_prs ");
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
        return sql;
    }

    private static StringBuilder createWhereSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("    nat_prs.sc_identif = ? ");
        return sql;
    }

    public static NatuurlijkPersoonResponse getRecordById(String id,
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

            NatuurlijkPersoonResponse np = new NatuurlijkPersoonResponse();
            if (rs.next()) {

                Boolean gi = (Boolean) searchContext.get(BrkInfo.GEVOELIGEINFOOPHALEN);
                if (gi != null && gi) {
                    np.setBsn(rs.getString("bsn"));
                }
                np.setGeboortedatum(rs.getInt("gb_geboortedatum"));
                np.setGeboorteplaats(rs.getString("gb_geboorteplaats"));
                np.setGeslachtsaanduiding(rs.getString("geslachtsaand"));
                np.setGeslachtsnaam(rs.getString("nm_geslachtsnaam"));
                np.setLocatieBeschrijving(rs.getString("va_loc_beschrijving"));
                np.setOverlijdensdatum(rs.getInt("overlijdensdatum"));
                np.setVoornamen(rs.getString("nm_voornamen"));
                np.setVoorvoegsel(rs.getString("nm_voorvoegsel_geslachtsnaam"));
            } else {
                return null;
            }

            return np;
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
