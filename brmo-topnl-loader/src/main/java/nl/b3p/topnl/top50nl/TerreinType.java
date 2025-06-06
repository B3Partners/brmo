//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2018.12.31 at 10:32:54 AM CET
//

package nl.b3p.topnl.top50nl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;

/**
 * Java class for TerreinType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="TerreinType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://register.geostandaarden.nl/gmlapplicatieschema/top50nl/1.1.1}_Top50nlObjectType">
 *       &lt;sequence>
 *         &lt;element name="geometrie" type="{http://www.opengis.net/gml/3.2}SurfacePropertyType"/>
 *         &lt;element name="typeLandgebruik" type="{http://register.geostandaarden.nl/gmlapplicatieschema/top50nl/1.1.1}TypeLandgebruikT50Type"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "TerreinType",
    namespace = "http://register.geostandaarden.nl/gmlapplicatieschema/top50nl/1.1.1",
    propOrder = {"geometrie", "typeLandgebruik"})
public class TerreinType extends Top50NlObjectType {

  @XmlAnyElement protected Element geometrie;

  @XmlElement(required = true)
  @XmlSchemaType(name = "string")
  protected TypeLandgebruikT50Type typeLandgebruik;

  /**
   * Gets the value of the geometrie property.
   *
   * @return possible object is {@link Element }
   */
  public Element getGeometrie() {
    return geometrie;
  }

  /**
   * Sets the value of the geometrie property.
   *
   * @param value allowed object is {@link Element }
   */
  public void setGeometrie(Element value) {
    this.geometrie = value;
  }

  /**
   * Gets the value of the typeLandgebruik property.
   *
   * @return possible object is {@link TypeLandgebruikT50Type }
   */
  public TypeLandgebruikT50Type getTypeLandgebruik() {
    return typeLandgebruik;
  }

  /**
   * Sets the value of the typeLandgebruik property.
   *
   * @param value allowed object is {@link TypeLandgebruikT50Type }
   */
  public void setTypeLandgebruik(TypeLandgebruikT50Type value) {
    this.typeLandgebruik = value;
  }
}
