//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2018.12.31 at 10:32:54 AM CET
//

package nl.b3p.topnl.top50nl;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for StatusT50Type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <p>
 *
 * <pre>
 * &lt;simpleType name="StatusT50Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="realisatie: nog niet in uitvoering"/>
 *     &lt;enumeration value="realisatie: in uitvoering"/>
 *     &lt;enumeration value="in gebruik"/>
 *     &lt;enumeration value="buiten gebruik"/>
 *     &lt;enumeration value="onbekend"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(
    name = "StatusT50Type",
    namespace = "http://register.geostandaarden.nl/gmlapplicatieschema/top50nl/1.1.1")
@XmlEnum
public enum StatusT50Type {
  @XmlEnumValue("realisatie: nog niet in uitvoering")
  REALISATIE_NOG_NIET_IN_UITVOERING("realisatie: nog niet in uitvoering"),
  @XmlEnumValue("realisatie: in uitvoering")
  REALISATIE_IN_UITVOERING("realisatie: in uitvoering"),
  @XmlEnumValue("in gebruik")
  IN_GEBRUIK("in gebruik"),
  @XmlEnumValue("buiten gebruik")
  BUITEN_GEBRUIK("buiten gebruik"),
  @XmlEnumValue("onbekend")
  ONBEKEND("onbekend");
  private final String value;

  StatusT50Type(String v) {
    value = v;
  }

  public String value() {
    return value;
  }

  public static StatusT50Type fromValue(String v) {
    for (StatusT50Type c : StatusT50Type.values()) {
      if (c.value.equals(v)) {
        return c;
      }
    }
    throw new IllegalArgumentException(v);
  }
}
