//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2017.01.10 at 11:33:39 AM CET
//

package nl.b3p.topnl.top10nl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for EnvelopeType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="EnvelopeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;sequence>
 *           &lt;element name="lowerCorner" type="{http://www.opengis.net/gml/3.2}DirectPositionType"/>
 *           &lt;element name="upperCorner" type="{http://www.opengis.net/gml/3.2}DirectPositionType"/>
 *         &lt;/sequence>
 *         &lt;element ref="{http://www.opengis.net/gml/3.2}pos" maxOccurs="2" minOccurs="2"/>
 *         &lt;element ref="{http://www.opengis.net/gml/3.2}coordinates"/>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://www.opengis.net/gml/3.2}SRSReferenceGroup"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "EnvelopeType",
    propOrder = {"lowerCorner", "upperCorner", "pos", "coordinates"})
public class EnvelopeType {

  protected DirectPositionType lowerCorner;
  protected DirectPositionType upperCorner;
  protected List<DirectPositionType> pos;
  protected CoordinatesType coordinates;

  @XmlAttribute(name = "srsName")
  @XmlSchemaType(name = "anyURI")
  protected String srsName;

  @XmlAttribute(name = "srsDimension")
  @XmlSchemaType(name = "positiveInteger")
  protected BigInteger srsDimension;

  @XmlAttribute(name = "axisLabels")
  protected List<String> axisLabels;

  @XmlAttribute(name = "uomLabels")
  protected List<String> uomLabels;

  /**
   * Gets the value of the lowerCorner property.
   *
   * @return possible object is {@link DirectPositionType }
   */
  public DirectPositionType getLowerCorner() {
    return lowerCorner;
  }

  /**
   * Sets the value of the lowerCorner property.
   *
   * @param value allowed object is {@link DirectPositionType }
   */
  public void setLowerCorner(DirectPositionType value) {
    this.lowerCorner = value;
  }

  /**
   * Gets the value of the upperCorner property.
   *
   * @return possible object is {@link DirectPositionType }
   */
  public DirectPositionType getUpperCorner() {
    return upperCorner;
  }

  /**
   * Sets the value of the upperCorner property.
   *
   * @param value allowed object is {@link DirectPositionType }
   */
  public void setUpperCorner(DirectPositionType value) {
    this.upperCorner = value;
  }

  /**
   * Gets the value of the pos property.
   *
   * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the pos property.
   *
   * <p>For example, to add a new item, do as follows:
   *
   * <pre>
   *    getPos().add(newItem);
   * </pre>
   *
   * <p>Objects of the following type(s) are allowed in the list {@link DirectPositionType }
   */
  public List<DirectPositionType> getPos() {
    if (pos == null) {
      pos = new ArrayList<DirectPositionType>();
    }
    return this.pos;
  }

  /**
   * Gets the value of the coordinates property.
   *
   * @return possible object is {@link CoordinatesType }
   */
  public CoordinatesType getCoordinates() {
    return coordinates;
  }

  /**
   * Sets the value of the coordinates property.
   *
   * @param value allowed object is {@link CoordinatesType }
   */
  public void setCoordinates(CoordinatesType value) {
    this.coordinates = value;
  }

  /**
   * Gets the value of the srsName property.
   *
   * @return possible object is {@link String }
   */
  public String getSrsName() {
    return srsName;
  }

  /**
   * Sets the value of the srsName property.
   *
   * @param value allowed object is {@link String }
   */
  public void setSrsName(String value) {
    this.srsName = value;
  }

  /**
   * Gets the value of the srsDimension property.
   *
   * @return possible object is {@link BigInteger }
   */
  public BigInteger getSrsDimension() {
    return srsDimension;
  }

  /**
   * Sets the value of the srsDimension property.
   *
   * @param value allowed object is {@link BigInteger }
   */
  public void setSrsDimension(BigInteger value) {
    this.srsDimension = value;
  }

  /**
   * Gets the value of the axisLabels property.
   *
   * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the axisLabels property.
   *
   * <p>For example, to add a new item, do as follows:
   *
   * <pre>
   *    getAxisLabels().add(newItem);
   * </pre>
   *
   * <p>Objects of the following type(s) are allowed in the list {@link String }
   */
  public List<String> getAxisLabels() {
    if (axisLabels == null) {
      axisLabels = new ArrayList<String>();
    }
    return this.axisLabels;
  }

  /**
   * Gets the value of the uomLabels property.
   *
   * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
   * modification you make to the returned list will be present inside the JAXB object. This is why
   * there is not a <CODE>set</CODE> method for the uomLabels property.
   *
   * <p>For example, to add a new item, do as follows:
   *
   * <pre>
   *    getUomLabels().add(newItem);
   * </pre>
   *
   * <p>Objects of the following type(s) are allowed in the list {@link String }
   */
  public List<String> getUomLabels() {
    if (uomLabels == null) {
      uomLabels = new ArrayList<String>();
    }
    return this.uomLabels;
  }
}
