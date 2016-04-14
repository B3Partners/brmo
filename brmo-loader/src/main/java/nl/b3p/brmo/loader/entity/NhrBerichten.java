package nl.b3p.brmo.loader.entity;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Matthijs Laan
 */
@XmlRootElement(namespace="http://www.b3partners.nl/brmo/bericht", name="berichten")
public class NhrBerichten {
    public static final String NS_BRMO_BERICHT = "http://www.b3partners.nl/brmo/bericht";

    @XmlElement(namespace=NS_BRMO_BERICHT, name="bericht")
    public List<NhrBericht> berichten;
}
