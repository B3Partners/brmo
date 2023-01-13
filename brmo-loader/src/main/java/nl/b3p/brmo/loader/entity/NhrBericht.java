package nl.b3p.brmo.loader.entity;

import static nl.b3p.brmo.loader.BrmoFramework.BR_NHR;

import org.w3c.dom.Element;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;

/** @author Matthijs Laan */
@XmlAccessorType(XmlAccessType.FIELD)
public class NhrBericht extends Bericht {

    @XmlAnyElement private Element node;

    private boolean hasAddedBSNHashes = false;

    public NhrBericht() {
        super(null);
    }

    public NhrBericht(String brXml) {
        super(brXml);
        setSoort(BR_NHR);
    }

    public Element getNode() {
        return node;
    }

    public void setNode(Element node) {
        this.node = node;
    }
}
