package nl.b3p.brmo.soap.eigendom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for eigendomMutatie complex type.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "eigendomMutatie", propOrder = {
    "identificatienummer",
    "gemeentecode",
    "appartementsindex",
    "perceelnummer",
    "sectie",
    "historischeRelaties",
    "brondocumenten",
    "zakelijkeRechten",
    "benoemdObjecten"
})
public class EigendomMutatie {

    protected String identificatienummer;
    @XmlElement(required = true)
    protected String gemeentecode;
    protected String appartementsindex;
    @XmlElement(required = true)
    protected String perceelnummer;
    @XmlElement(required = true)
    protected String sectie;
    @XmlElement(name = "historische_relaties")
    protected HistorischeRelaties historischeRelaties;
    protected Brondocumenten brondocumenten;
    @XmlElement(name = "zakelijke_rechten")
    protected ZakelijkeRechten zakelijkeRechten;
    @XmlElement(name = "benoemd_objecten")
    protected BenoemdObjecten benoemdObjecten;

    /**
     * Gets the value of the identificatienummer property.
     *
     */
    public String getIdentificatienummer() {
        return identificatienummer;
    }

    /**
     * Sets the value of the identificatienummer property.
     *
     */
    public void setIdentificatienummer(String value) {
        this.identificatienummer = value;
    }

    /**
     * Gets the value of the gemeentecode property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getGemeentecode() {
        return gemeentecode;
    }

    /**
     * Sets the value of the gemeentecode property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setGemeentecode(String value) {
        this.gemeentecode = value;
    }

    /**
     * Gets the value of the appartementsindex property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getAppartementsindex() {
        return appartementsindex;
    }

    /**
     * Sets the value of the appartementsindex property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setAppartementsindex(String value) {
        this.appartementsindex = value;
    }

    /**
     * Gets the value of the perceelnummer property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getPerceelnummer() {
        return perceelnummer;
    }

    /**
     * Sets the value of the perceelnummer property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setPerceelnummer(String value) {
        this.perceelnummer = value;
    }

    /**
     * Gets the value of the sectie property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getSectie() {
        return sectie;
    }

    /**
     * Sets the value of the sectie property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setSectie(String value) {
        this.sectie = value;
    }

    /**
     * Gets the value of the historischeRelaties property.
     *
     * @return possible object is {@link HistorischeRelaties }
     *
     */
    public HistorischeRelaties getHistorischeRelaties() {
        return historischeRelaties;
    }

    /**
     * Sets the value of the historischeRelaties property.
     *
     * @param value allowed object is {@link HistorischeRelaties }
     *
     */
    public void setHistorischeRelaties(HistorischeRelaties value) {
        this.historischeRelaties = value;
    }

    /**
     * Gets the value of the brondocumenten property.
     *
     * @return possible object is {@link Brondocumenten }
     *
     */
    public Brondocumenten getBrondocumenten() {
        return brondocumenten;
    }

    /**
     * Sets the value of the brondocumenten property.
     *
     * @param value allowed object is {@link Brondocumenten }
     *
     */
    public void setBrondocumenten(Brondocumenten value) {
        this.brondocumenten = value;
    }

    /**
     * Gets the value of the zakelijkeRechten property.
     *
     * @return possible object is {@link ZakelijkeRechten }
     *
     */
    public ZakelijkeRechten getZakelijkeRechten() {
        return zakelijkeRechten;
    }

    /**
     * Sets the value of the zakelijkeRechten property.
     *
     * @param value allowed object is {@link ZakelijkeRechten }
     *
     */
    public void setZakelijkeRechten(ZakelijkeRechten value) {
        this.zakelijkeRechten = value;
    }

    /**
     * Gets the value of the benoemdObjecten property.
     *
     * @return possible object is {@link BenoemdObjecten }
     *
     */
    public BenoemdObjecten getBenoemdObjecten() {
        return benoemdObjecten;
    }

    /**
     * Sets the value of the benoemdObjecten property.
     *
     * @param value allowed object is {@link BenoemdObjecten }
     *
     */
    public void setBenoemdObjecten(BenoemdObjecten value) {
        this.benoemdObjecten = value;
    }

    public static EigendomMutatie createDummy() {
        EigendomMutatie em = new EigendomMutatie();
        em.setAppartementsindex("0001");
        BenoemdObject bo = new BenoemdObject();
        Brondocumenten bdbo = new Brondocumenten();
        Document d = new Document();
        d.setDatum("23-05-2016");
        d.setValue("GM/2006/3448");
        bdbo.getDocument().add(d);
        bdbo.getDocument().add(d);
        bo.setBrondocumenten(bdbo);
        bo.setGebouwdObjectGebruiksdoel("woonfunctie");
        bo.setGebouwdObjectOppervlakte("101");
        bo.setIdentificatienummer("0153010000393935");
        BenoemdObjecten bon = new BenoemdObjecten();
        bon.getBenoemdObject().add(bo);
        bon.getBenoemdObject().add(bo);
        em.setBenoemdObjecten(bon);
        em.setBrondocumenten(bdbo);
        em.setGemeentecode("ANA02");
        HistorischeRelaties hrs = new HistorischeRelaties();
        HistorischeRelatie hr = new HistorischeRelatie();
        hr.setAard("Vernummering");
        hr.setIdentificatienummer("46750053540001");
        hrs.getHistorischeRelatie().add(hr);
        hrs.getHistorischeRelatie().add(hr);
        em.setHistorischeRelaties(hrs);
        em.setIdentificatienummer("55770450970000");
        em.setPerceelnummer("1755");
        em.setSectie("I");
        ZakelijkeRechten zrn = new ZakelijkeRechten();
        ZakelijkRecht zr = new ZakelijkRecht();
        zr.setAardRecht("Eigendom (recht van)");
        zr.setBSN("0000001");
        zr.setIdentificatienummer("NL.KAD.Tenaamstelling.AKR1.8683641");
        zr.setNoemer("2");
        zr.setTeller("1");
        zrn.getZakelijkRecht().add(zr);
        zr.setAardRecht("Eigendom (recht van)");
        zr.setBSN("0000011");
        zr.setIdentificatienummer("NL.KAD.Tenaamstelling.AKR1.8683651");
        zr.setNoemer("2");
        zr.setTeller("1");
        zrn.getZakelijkRecht().add(zr);
        zr = new ZakelijkRecht();
        zr.setAardRecht("Erfpacht (recht van)");
        zr.setBSN("0000002");
        zr.setIdentificatienummer("NL.KAD.Tenaamstelling.AKR1.8683642");
        zr.setNoemer("2");
        zr.setTeller("1");
        zrn.getZakelijkRecht().add(zr);
        zr = new ZakelijkRecht();
        zr.setAardRecht("Gebruik en bewoning (recht van)");
        zr.setBSN("0000003");
        zr.setIdentificatienummer("NL.KAD.Tenaamstelling.AKR1.8683643");
        zr.setNoemer("2");
        zr.setTeller("1");
        zrn.getZakelijkRecht().add(zr);
        zr = new ZakelijkRecht();
        zr.setAardRecht("Grondrente (recht van)");
        zr.setBSN("0000004");
        zr.setIdentificatienummer("NL.KAD.Tenaamstelling.AKR1.8683644");
        zr.setNoemer("2");
        zr.setTeller("1");
        zrn.getZakelijkRecht().add(zr);
        zr = new ZakelijkRecht();
        zr.setAardRecht("Huurrecht (zakelijk)");
        zr.setBSN("0000005");
        zr.setIdentificatienummer("NL.KAD.Tenaamstelling.AKR1.8683645");
        zr.setNoemer("2");
        zr.setTeller("1");
        zrn.getZakelijkRecht().add(zr);
        zr = new ZakelijkRecht();
        zr.setAardRecht("Opstal (recht van)");
        zr.setBSN("0000006");
        zr.setIdentificatienummer("NL.KAD.Tenaamstelling.AKR1.8683646");
        zr.setNoemer("2");
        zr.setTeller("1");
        zrn.getZakelijkRecht().add(zr);
        zr = new ZakelijkRecht();
        zr.setAardRecht("Optierecht (zakelijk)");
        zr.setBSN("0000007");
        zr.setIdentificatienummer("NL.KAD.Tenaamstelling.AKR1.8683647");
        zr.setNoemer("2");
        zr.setTeller("1");
        zrn.getZakelijkRecht().add(zr);
        zr = new ZakelijkRecht();
        zr.setAardRecht("Vruchtgebruik (recht van)");
        zr.setBSN("0000008");
        zr.setIdentificatienummer("NL.KAD.Tenaamstelling.AKR1.8683648");
        zr.setNoemer("2");
        zr.setTeller("1");
        zrn.getZakelijkRecht().add(zr);
        zr = new ZakelijkRecht();
        zr.setAardRecht("Erfpacht en Opstal (recht van)");
        zr.setBSN("0000009");
        zr.setIdentificatienummer("NL.KAD.Tenaamstelling.AKR1.8683649");
        zr.setNoemer("2");
        zr.setTeller("1");
        zrn.getZakelijkRecht().add(zr);
        zr = new ZakelijkRecht();
        zr.setAardRecht("Opstalrecht Nutsvoorzieningen");
        zr.setBSN("0000010");
        zr.setIdentificatienummer("NL.KAD.Tenaamstelling.AKR1.8683649");
        zr.setNoemer("1");
        zr.setTeller("1");
        zrn.getZakelijkRecht().add(zr);

        em.setZakelijkeRechten(zrn);
        return em;
    }

}
