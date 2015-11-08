package nl.b3p.brmo.soap.brk;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Chris
 */
@XmlType
public class ZakelijkRechtResponse {
    
    private Integer noemer;
    private Integer teller;
    private Boolean indicatieBetrokkenInSplitsing;
    private String aardVerkregenRecht;
    
    private NatuurlijkPersoonResponse natuurlijkPersoon;
    private NietNatuurlijkPersoonResponse nietNatuurlijkPersoon;


    /**
     * @return the noemer
     */
    @XmlElement
    public Integer getNoemer() {
        return noemer;
    }

    /**
     * @param noemer the noemer to set
     */
    public void setNoemer(Integer noemer) {
        this.noemer = noemer;
    }

    /**
     * @return the teller
     */
    @XmlElement
    public Integer getTeller() {
        return teller;
    }

    /**
     * @param teller the teller to set
     */
    public void setTeller(Integer teller) {
        this.teller = teller;
    }

    /**
     * @return the indicatieBetrokkenInSplitsing
     */
    public boolean isIndicatieBetrokkenInSplitsing() {
        return indicatieBetrokkenInSplitsing;
    }

    /**
     * @param indicatieBetrokkenInSplitsing the indicatieBetrokkenInSplitsing to set
     */
    public void setIndicatieBetrokkenInSplitsing(boolean indicatieBetrokkenInSplitsing) {
        this.indicatieBetrokkenInSplitsing = indicatieBetrokkenInSplitsing;
    }

    /**
     * @return the aardVerkregenRecht
     */
    @XmlElement
    public String getAardVerkregenRecht() {
        return aardVerkregenRecht;
    }

    /**
     * @param aardVerkregenRecht the aardVerkregenRecht to set
     */
    public void setAardVerkregenRecht(String aardVerkregenRecht) {
        this.aardVerkregenRecht = aardVerkregenRecht;
    }
    
    /**
     * @return the natuurlijkPersoon
     */
    public NatuurlijkPersoonResponse getNatuurlijkPersoon() {
        return natuurlijkPersoon;
    }

    /**
     * @param natuurlijkPersoon the natuurlijkPersoon to set
     */
    public void setNatuurlijkPersoon(NatuurlijkPersoonResponse natuurlijkPersoon) {
        this.natuurlijkPersoon = natuurlijkPersoon;
    }

    /**
     * @return the nietNatuurlijkPersoon
     */
    public NietNatuurlijkPersoonResponse getNietNatuurlijkPersoon() {
        return nietNatuurlijkPersoon;
    }

    /**
     * @param nietNatuurlijkPersoon the nietNatuurlijkPersoon to set
     */
    public void setNietNatuurlijkPersoon(NietNatuurlijkPersoonResponse nietNatuurlijkPersoon) {
        this.nietNatuurlijkPersoon = nietNatuurlijkPersoon;
    }

 }
