package nl.b3p.brmo.soap.brk;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Chris
 */
@XmlType
public class ZakelijkRechtResponse {
    
    private int noemer;
    private int teller;
    private boolean indicatieBetrokkenInSplitsing;
    private String aardVerkregenRecht;
    
    private NatPersonenResponse natuurlijkPersonen;
    private NietNatPersonenResponse nietNatuurlijkPersonen;


    /**
     * @return the noemer
     */
    @XmlElement(required = true)
    public int getNoemer() {
        return noemer;
    }

    /**
     * @param noemer the noemer to set
     */
    public void setNoemer(int noemer) {
        this.noemer = noemer;
    }

    /**
     * @return the teller
     */
    @XmlElement(required = true)
    public int getTeller() {
        return teller;
    }

    /**
     * @param teller the teller to set
     */
    public void setTeller(int teller) {
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
    @XmlElement(required = true)
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
    public NatPersonenResponse getNatuurlijkPersonen() {
        return natuurlijkPersonen;
    }

    /**
     * @param natuurlijkPersonen the natuurlijkPersonen to set
     */
    public void setNatuurlijkPersonen(NatPersonenResponse natuurlijkPersonen) {
        this.natuurlijkPersonen = natuurlijkPersonen;
    }

    /**
     * @return the nietNatuurlijkPersonen
     */
    public NietNatPersonenResponse getNietNatuurlijkPersonen() {
        return nietNatuurlijkPersonen;
    }

    /**
     * @param nietNatuurlijkPersonen the nietNatuurlijkPersonen to set
     */
    public void setNietNatuurlijkPersonen(NietNatPersonenResponse nietNatuurlijkPersonen) {
        this.nietNatuurlijkPersonen = nietNatuurlijkPersonen;
    }

}
