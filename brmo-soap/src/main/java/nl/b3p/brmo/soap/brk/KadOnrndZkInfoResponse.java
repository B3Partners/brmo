package nl.b3p.brmo.soap.brk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;
import javax.sql.DataSource;
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
public class KadOnrndZkInfoResponse {

    private static final Log LOG = LogFactory.getLog(KadOnrndZkInfoResponse.class);

    private String gemeentecode;
    private String sectie;
    private String perceelnummer;
    private String appReVolgnummer;
    private String identificatie;
    private String aandSoortGrootte;
    private String begrenzingPerceel;
    private Float groottePerceel;
    private String omschr_deelperceel;
    private String aardCultuurOnbebouwd;
    private Date datumBeginGeldigheid;
    private Date datumEindeGeldigheid;
    private Float bedrag;
    private Integer koopjaar;
    private Boolean meerOnroerendgoed;
    private String type; // appartement of perceel
    private AdressenResponse adressen;
    private RechtenResponse rechten;
    private RelatiesResponse relaties;

    public KadOnrndZkInfoResponse() {
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
     * @return the aandSoortGrootte
     */
    public String getAandSoortGrootte() {
        return aandSoortGrootte;
    }

    /**
     * @param aandSoortGrootte the aandSoortGrootte to set
     */
    public void setAandSoortGrootte(String aandSoortGrootte) {
        this.aandSoortGrootte = aandSoortGrootte;
    }

    /**
     * @return the begrenzingPerceel
     */
    public String getBegrenzingPerceel() {
        return begrenzingPerceel;
    }

    /**
     * @param begrenzingPerceel the begrenzingPerceel to set
     */
    public void setBegrenzingPerceel(String begrenzingPerceel) {
        this.begrenzingPerceel = begrenzingPerceel;
    }

    /**
     * @return the groottePerceel
     */
    public Float getGroottePerceel() {
        return groottePerceel;
    }

    /**
     * @param groottePerceel the groottePerceel to set
     */
    public void setGroottePerceel(Float groottePerceel) {
        this.groottePerceel = groottePerceel;
    }

    /**
     * @return the omschr_deelperceel
     */
    public String getOmschr_deelperceel() {
        return omschr_deelperceel;
    }

    /**
     * @param omschr_deelperceel the omschr_deelperceel to set
     */
    public void setOmschr_deelperceel(String omschr_deelperceel) {
        this.omschr_deelperceel = omschr_deelperceel;
    }

    /**
     * @return the aardCultuurOnbebouwd
     */
    public String getAardCultuurOnbebouwd() {
        return aardCultuurOnbebouwd;
    }

    /**
     * @param aardCultuurOnbebouwd the aardCultuurOnbebouwd to set
     */
    public void setAardCultuurOnbebouwd(String aardCultuurOnbebouwd) {
        this.aardCultuurOnbebouwd = aardCultuurOnbebouwd;
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
     * @return the bedrag
     */
    public Float getBedrag() {
        return bedrag;
    }

    /**
     * @param bedrag the bedrag to set
     */
    public void setBedrag(Float bedrag) {
        this.bedrag = bedrag;
    }

    /**
     * @return the koopjaar
     */
    public Integer getKoopjaar() {
        return koopjaar;
    }

    /**
     * @param koopjaar the koopjaar to set
     */
    public void setKoopjaar(Integer koopjaar) {
        this.koopjaar = koopjaar;
    }

    /**
     * @return the meerOnroerendgoed
     */
    public Boolean isMeerOnroerendgoed() {
        return meerOnroerendgoed;
    }

    /**
     * @param meerOnroerendgoed the meerOnroerendgoed to set
     */
    public void setMeerOnroerendgoed(Boolean meerOnroerendgoed) {
        this.meerOnroerendgoed = meerOnroerendgoed;
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

    /**
     * @return the adressen
     */
    public AdressenResponse getAdressen() {
        return adressen;
    }

    /**
     * @param adressen the adressen to set
     */
    public void setAdressen(AdressenResponse adressen) {
        this.adressen = adressen;
    }

    /**
     * @return the rechten
     */
    public RechtenResponse getRechten() {
        return rechten;
    }

    /**
     * @param rechten the rechten to set
     */
    public void setRechten(RechtenResponse rechten) {
        this.rechten = rechten;
    }

    /**
     * @return the relaties
     */
    public RelatiesResponse getRelaties() {
        return relaties;
    }

    /**
     * @param relaties the relaties to set
     */
    public void setRelaties(RelatiesResponse relaties) {
        this.relaties = relaties;
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
        sql.append("    kad_perceel.aand_soort_grootte,");
        sql.append("    kad_onrrnd_zk.cu_aard_cultuur_onbebouwd,");
        sql.append("    kad_onrrnd_zk.lr_bedrag,");
        sql.append("    kad_perceel.begrenzing_perceel,");
        sql.append("    kad_perceel.grootte_perceel,");
        sql.append("    kad_onrrnd_zk.ks_koopjaar,");
        sql.append("    kad_onrrnd_zk.ks_meer_onroerendgoed,");
        sql.append("    kad_perceel.omschr_deelperceel ");
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

    public static KadOnrndZkInfoResponse getRecord(Long id,
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
            stm.setLong(1, id);
            rs = stm.executeQuery();

            KadOnrndZkInfoResponse koz = new KadOnrndZkInfoResponse();
            if (rs.next()) {
                koz.setIdentificatie(Long.toString(rs.getLong("kad_identif")));
                koz.setAandSoortGrootte(rs.getString("aand_soort_grootte"));
                koz.setAardCultuurOnbebouwd(rs.getString("cu_aard_cultuur_onbebouwd"));
                koz.setBedrag(rs.getFloat("lr_bedrag"));
                koz.setDatumBeginGeldigheid(rs.getDate("dat_beg_geldh"));
                koz.setDatumEindeGeldigheid(rs.getDate("datum_einde_geldh"));
                koz.setKoopjaar(rs.getInt("ks_koopjaar"));
                // ks_meer_onroerendgoed is 'J' of 'N' string, geen boolean
                koz.setMeerOnroerendgoed(rs.getString("ks_meer_onroerendgoed").equalsIgnoreCase("J"));
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
//                koz.setBegrenzingPerceel(rs.getString("begrenzing_perceel"));
                    koz.setGroottePerceel(rs.getFloat("grootte_perceel"));
                    koz.setOmschr_deelperceel(rs.getString("omschr_deelperceel"));
                }

                koz.setRechten(RechtenResponse.getRechtenByKoz(id, searchContext));
                Boolean at = (Boolean) searchContext.get(BrkInfo.ADRESSENTOEVOEGEN);
                if (at != null && at) {
                    koz.setAdressen(AdressenResponse.getAdressenByKoz(id));
                }
//            koz.setRelaties(null);

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
