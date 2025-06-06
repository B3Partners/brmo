//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2018.12.31 at 10:32:54 AM CET
//

package nl.b3p.topnl.top50nl;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for FeatureMemberType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="FeatureMemberType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml/3.2}AbstractFeatureMemberType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element ref="{http://register.geostandaarden.nl/gmlapplicatieschema/top50nl/1.1.1}_Top50nlObject"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.opengis.net/gml/3.2}AssociationAttributeGroup"/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "FeatureMemberType",
    namespace = "http://register.geostandaarden.nl/gmlapplicatieschema/top50nl/1.1.1",
    propOrder = {"top50NlObject"})
public class FeatureMemberType extends AbstractFeatureMemberType {

  @XmlElementRef(
      name = "_Top50nlObject",
      namespace = "http://register.geostandaarden.nl/gmlapplicatieschema/top50nl/1.1.1",
      type = JAXBElement.class,
      required = false)
  protected JAXBElement<? extends Top50NlObjectType> top50NlObject;

  @XmlAttribute(name = "nilReason")
  protected List<String> nilReason;

  @XmlAttribute(name = "remoteSchema", namespace = "http://www.opengis.net/gml/3.2")
  @XmlSchemaType(name = "anyURI")
  protected String remoteSchema;

  @XmlAttribute(name = "type", namespace = "http://www.w3.org/1999/xlink")
  protected String type;

  @XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
  @XmlSchemaType(name = "anyURI")
  protected String href;

  @XmlAttribute(name = "role", namespace = "http://www.w3.org/1999/xlink")
  @XmlSchemaType(name = "anyURI")
  protected String role;

  @XmlAttribute(name = "arcrole", namespace = "http://www.w3.org/1999/xlink")
  @XmlSchemaType(name = "anyURI")
  protected String arcrole;

  @XmlAttribute(name = "title", namespace = "http://www.w3.org/1999/xlink")
  protected String title;

  @XmlAttribute(name = "show", namespace = "http://www.w3.org/1999/xlink")
  protected String show;

  @XmlAttribute(name = "actuate", namespace = "http://www.w3.org/1999/xlink")
  protected String actuate;

  /**
   * Gets the value of the top50NlObject property.
   *
   * @return possible object is {@link JAXBElement }{@code <}{@link SpoorbaandeelType }{@code >}
   *     {@link JAXBElement }{@code <}{@link HoogteType }{@code >} {@link JAXBElement }{@code
   *     <}{@link InrichtingselementType }{@code >} {@link JAXBElement }{@code <}{@link TerreinType
   *     }{@code >} {@link JAXBElement }{@code <}{@link WegdeelType }{@code >} {@link JAXBElement
   *     }{@code <}{@link ReliefType }{@code >} {@link JAXBElement }{@code <}{@link
   *     FunctioneelGebiedType }{@code >} {@link JAXBElement }{@code <}{@link WaterdeelType }{@code
   *     >} {@link JAXBElement }{@code <}{@link RegistratiefGebiedType }{@code >} {@link JAXBElement
   *     }{@code <}{@link GebouwType }{@code >} {@link JAXBElement }{@code <}{@link
   *     GeografischGebiedType }{@code >} {@link JAXBElement }{@code <}{@link Top50NlObjectType
   *     }{@code >}
   */
  public JAXBElement<? extends Top50NlObjectType> getTop50NlObject() {
    return top50NlObject;
  }

  /**
   * Sets the value of the top50NlObject property.
   *
   * @param value allowed object is {@link JAXBElement }{@code <}{@link SpoorbaandeelType }{@code >}
   *     {@link JAXBElement }{@code <}{@link HoogteType }{@code >} {@link JAXBElement }{@code
   *     <}{@link InrichtingselementType }{@code >} {@link JAXBElement }{@code <}{@link TerreinType
   *     }{@code >} {@link JAXBElement }{@code <}{@link WegdeelType }{@code >} {@link JAXBElement
   *     }{@code <}{@link ReliefType }{@code >} {@link JAXBElement }{@code <}{@link
   *     FunctioneelGebiedType }{@code >} {@link JAXBElement }{@code <}{@link WaterdeelType }{@code
   *     >} {@link JAXBElement }{@code <}{@link RegistratiefGebiedType }{@code >} {@link JAXBElement
   *     }{@code <}{@link GebouwType }{@code >} {@link JAXBElement }{@code <}{@link
   *     GeografischGebiedType }{@code >} {@link JAXBElement }{@code <}{@link Top50NlObjectType
   *     }{@code >}
   */
  public void setTop50NlObject(JAXBElement<? extends Top50NlObjectType> value) {
    this.top50NlObject = value;
  }

  /**
   * Gets the value of the nilReason property.
   *
   * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the nilReason property.
   *
   * <p>For example, to add a new item, do as follows:
   *
   * <pre>
   *    getNilReason().add(newItem);
   * </pre>
   *
   * <p>Objects of the following type(s) are allowed in the list {@link String }
   */
  public List<String> getNilReason() {
    if (nilReason == null) {
      nilReason = new ArrayList<String>();
    }
    return this.nilReason;
  }

  /**
   * Gets the value of the remoteSchema property.
   *
   * @return possible object is {@link String }
   */
  public String getRemoteSchema() {
    return remoteSchema;
  }

  /**
   * Sets the value of the remoteSchema property.
   *
   * @param value allowed object is {@link String }
   */
  public void setRemoteSchema(String value) {
    this.remoteSchema = value;
  }

  /**
   * Gets the value of the type property.
   *
   * @return possible object is {@link String }
   */
  public String getType() {
    if (type == null) {
      return "simple";
    } else {
      return type;
    }
  }

  /**
   * Sets the value of the type property.
   *
   * @param value allowed object is {@link String }
   */
  public void setType(String value) {
    this.type = value;
  }

  /**
   * Gets the value of the href property.
   *
   * @return possible object is {@link String }
   */
  public String getHref() {
    return href;
  }

  /**
   * Sets the value of the href property.
   *
   * @param value allowed object is {@link String }
   */
  public void setHref(String value) {
    this.href = value;
  }

  /**
   * Gets the value of the role property.
   *
   * @return possible object is {@link String }
   */
  public String getRole() {
    return role;
  }

  /**
   * Sets the value of the role property.
   *
   * @param value allowed object is {@link String }
   */
  public void setRole(String value) {
    this.role = value;
  }

  /**
   * Gets the value of the arcrole property.
   *
   * @return possible object is {@link String }
   */
  public String getArcrole() {
    return arcrole;
  }

  /**
   * Sets the value of the arcrole property.
   *
   * @param value allowed object is {@link String }
   */
  public void setArcrole(String value) {
    this.arcrole = value;
  }

  /**
   * Gets the value of the title property.
   *
   * @return possible object is {@link String }
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the value of the title property.
   *
   * @param value allowed object is {@link String }
   */
  public void setTitle(String value) {
    this.title = value;
  }

  /**
   * Gets the value of the show property.
   *
   * @return possible object is {@link String }
   */
  public String getShow() {
    return show;
  }

  /**
   * Sets the value of the show property.
   *
   * @param value allowed object is {@link String }
   */
  public void setShow(String value) {
    this.show = value;
  }

  /**
   * Gets the value of the actuate property.
   *
   * @return possible object is {@link String }
   */
  public String getActuate() {
    return actuate;
  }

  /**
   * Sets the value of the actuate property.
   *
   * @param value allowed object is {@link String }
   */
  public void setActuate(String value) {
    this.actuate = value;
  }
}
