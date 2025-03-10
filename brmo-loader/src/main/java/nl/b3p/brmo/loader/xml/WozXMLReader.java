/*
 * Copyright (C) 2021 B3Partners B.V.
 */
package nl.b3p.brmo.loader.xml;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.StagingProxy;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.WozBericht;
import nl.b3p.brmo.loader.util.RsgbTransformer;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WozXMLReader extends BrmoXMLReader {
  public static final String PREFIX_PRS = "WOZ.NPS.";
  public static final String PREFIX_NNP = "WOZ.NNP.";
  public static final String PREFIX_WOZ = "WOZ.WOZ.";
  public static final String PREFIX_VES = "WOZ.VES.";
  private static final Log LOG = LogFactory.getLog(WozXMLReader.class);
  private final String pathToXsl = "/xsl/woz-brxml-preprocessor.xsl";
  private final StagingProxy staging;
  private final XPathFactory xPathfactory = XPathFactory.newInstance();
  private InputStream in;
  private Templates template;
  private NodeList objectNodes = null;
  private int index;
  private String brOrigXML = null;
  private String betrokkenWaterschap = null;
  private String gemeenteCode = null;

  public WozXMLReader(InputStream in, Date bestandsDatum, StagingProxy staging) throws Exception {
    this.in = in;
    this.staging = staging;
    setBestandsDatum(bestandsDatum);
    init();
  }

  @Override
  public void init() throws Exception {
    soort = BrmoFramework.BR_WOZ;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    in = new TeeInputStream(in, bos, true);

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(in);

    brOrigXML = bos.toString(StandardCharsets.UTF_8);
    LOG.trace("Originele WOZ xml is: \n" + brOrigXML);

    TransformerFactory tf = TransformerFactory.newInstance();
    tf.setURIResolver(
        (href, base) -> {
          LOG.debug("looking for: " + href + " base: " + base);
          return new StreamSource(RsgbTransformer.class.getResourceAsStream("/xsl/" + href));
        });

    Source xsl = new StreamSource(WozXMLReader.class.getResourceAsStream(pathToXsl));
    this.template = tf.newTemplates(xsl);

    XPath xpath = xPathfactory.newXPath();

    if (this.getBestandsDatum() == null) {
      // probeer datum nog uit doc te halen..
      LOG.debug("Tijdstip bericht was niet gegeven; alsnog proberen op te zoeken in bericht.");
      XPathExpression tijdstipBericht = xpath.compile("//*[local-name()='tijdstipBericht']");
      Node datum = (Node) tijdstipBericht.evaluate(doc, XPathConstants.NODE);
      setDatumAsString(datum.getTextContent(), "yyyyMMddHHmmssSSS");
      LOG.debug("Tijdstip bericht ingesteld op " + getBestandsDatum());
    }

    XPathExpression eersteGemeente =
        xpath.compile(
            "//*[local-name()='verantwoordelijkeGemeente']/*[local-name()='gemeenteCode'][1]");
    Node gem = (Node) eersteGemeente.evaluate(doc, XPathConstants.NODE);
    gemeenteCode = null != gem ? gem.getTextContent() : null;
    if (null == gemeenteCode) {
      eersteGemeente = xpath.compile("//*[local-name()='verantwoordelijkeGemeente'][1]");
      gem = (Node) eersteGemeente.evaluate(doc, XPathConstants.NODE);
      gemeenteCode = null != gem ? gem.getTextContent() : null;
    }
    XPathExpression eersteWaterschap =
        xpath.compile(
            "//*[local-name()='ligtIn']/*[local-name()='gerelateerde']/*[local-name()='betrokkenWaterschap'][1]");
    Node ws = (Node) eersteWaterschap.evaluate(doc, XPathConstants.NODE);
    betrokkenWaterschap = null != ws ? ws.getTextContent() : null;

    // actuele woz:object nodes
    XPathExpression objectNode =
        xpath.compile("//*[local-name()='object'][not(ancestor::*[local-name()='historie'])]");
    objectNodes = (NodeList) objectNode.evaluate(doc, XPathConstants.NODESET);

    // mogelijk zijn er omhang berichten (WGEM_hangSubjectOm_Di01)
    if (objectNodes.getLength() < 1) {
      objectNode = xpath.compile("//*[local-name()='nieuweGemeenteNPS']");
      objectNodes = (NodeList) objectNode.evaluate(doc, XPathConstants.NODESET);
      if (LOG.isDebugEnabled() && objectNodes.getLength() > 0) {
        LOG.debug("nieuweGemeente NPS omhangbericht");
      }
    }
    if (objectNodes.getLength() < 1) {
      objectNode = xpath.compile("//*[local-name()='nieuweGemeenteNNP']");
      objectNodes = (NodeList) objectNode.evaluate(doc, XPathConstants.NODESET);
      if (LOG.isDebugEnabled() && objectNodes.getLength() > 0) {
        LOG.debug("nieuweGemeente NNP omhangbericht");
      }
    }
    if (objectNodes.getLength() < 1) {
      objectNode = xpath.compile("//*[local-name()='nieuweGemeenteVES']");
      objectNodes = (NodeList) objectNode.evaluate(doc, XPathConstants.NODESET);
      if (LOG.isDebugEnabled() && objectNodes.getLength() > 0) {
        LOG.debug("nieuweGemeente VES omhangbericht");
      }
    }
    index = 0;
  }

  @Override
  public boolean hasNext() throws Exception {
    return index < objectNodes.getLength();
  }

  @Override
  public WozBericht next() throws Exception {
    Node n = objectNodes.item(index);
    index++;
    String object_ref = getObjectRef(n);
    StringWriter sw = new StringWriter();

    // kijk hier of dit bericht een voorganger heeft: zo niet, dan moet niet de preprocessor
    // template gebruikt worden, maar de gewone.
    Bericht old =
        staging.getPreviousBericht(object_ref, getBestandsDatum(), -1L, new StringBuilder());
    Transformer t;
    if (old != null) {
      LOG.debug("gebruik preprocessor xsl");
      t = this.template.newTransformer();
    } else {
      LOG.debug("gebruik extractie xsl");
      t = TransformerFactory.newInstance().newTransformer();
    }

    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    t.setOutputProperty(OutputKeys.INDENT, "no");
    t.setOutputProperty(OutputKeys.METHOD, "xml");
    t.transform(new DOMSource(n), new StreamResult(sw));

    Map<String, String> bsns = extractBSN(n);
    String origXML = sw.toString();
    StringBuilder brXML =
        new StringBuilder("<root>").append(origXML).append(getXML(bsns)).append("<fallback>");
    if (null != gemeenteCode) {
      brXML.append("<gemeenteCode>").append(gemeenteCode).append("</gemeenteCode>");
    }
    if (null != betrokkenWaterschap) {
      brXML
          .append("<betrokkenWaterschap>")
          .append(betrokkenWaterschap)
          .append("</betrokkenWaterschap>");
    }
    brXML.append("</fallback>").append("</root>");

    WozBericht b = new WozBericht(brXML.toString());
    b.setDatum(getBestandsDatum());
    if (index == 1) {
      // alleen op 1e brmo bericht van mogelijk meer uit originele bericht
      b.setBrOrgineelXml(brOrigXML);
    }
    // TODO volgorde nummer:
    //  bepaal aan de hand van de object_ref of volgordenummer opgehoogd moet worden. Een soap
    // bericht kan meerdere
    //  object entiteiten bevatten die een eigen type objectref krijgen. bijv. een
    // entiteittype="WOZ" en een entiteittype="NPS"
    //  bovendien kan een entiteittype="WOZ" een genests gerelateerde hebben die een apart
    // bericht moet/zou kunnen opleveren met objectref
    //  van een NPS, maar met een hoger volgordenummer...
    //  vooralsnog halen we niet de geneste entiteiten uit het bericht
    b.setVolgordeNummer(index);
    if (index > 1) {
      // om om het probleem van 2 subjecten uit 1 bericht op zelfde tijdstip dus heen te
      // werken hoger volgordenummer ook iets later maken
      b.setDatum(new Date(getBestandsDatum().getTime() + 10));
    }
    b.setObjectRef(object_ref);
    if (StringUtils.isEmpty(b.getObjectRef())) {
      // geen object_ref kunnen vaststellen; dan ook niet transformeren
      b.setStatus(Bericht.STATUS.STAGING_NOK);
      b.setOpmerking(Bericht.GEEN_OBJECT_REF_MSG);
    }

    LOG.trace("bericht: " + b);
    return b;
  }

  private String getObjectRef(Node wozObjectNode) throws XPathExpressionException {
    // WOZ:object StUF:entiteittype="WOZ"/WOZ:wozObjectNummer
    XPathExpression wozObjectNummer =
        xPathfactory.newXPath().compile("./*[local-name()='wozObjectNummer']");
    NodeList obRefs = (NodeList) wozObjectNummer.evaluate(wozObjectNode, XPathConstants.NODESET);
    if (obRefs.getLength() > 0) {
      return PREFIX_WOZ + obRefs.item(0).getTextContent();
    }

    // WOZ:object StUF:entiteittype="NPS"/WOZ:isEen/WOZ:gerelateerde/BG:inp.bsn
    XPathExpression bsn =
        xPathfactory
            .newXPath()
            .compile("./*/*[local-name()='gerelateerde']/*[local-name()='inp.bsn']");
    obRefs = (NodeList) bsn.evaluate(wozObjectNode, XPathConstants.NODESET);
    if (obRefs.getLength() > 0) {
      return PREFIX_PRS + getHash(obRefs.item(0).getTextContent());
    }

    // ./WOZ:object
    // StUF:entiteittype="WOZ"/WOZ:heeftBelanghebbende/WOZ:gerelateerde/WOZ:natuurlijkPersoon/WOZ:soFiNummer
    bsn =
        xPathfactory
            .newXPath()
            .compile(
                "//*[local-name()='heeftBelanghebbende']/*[local-name()='gerelateerde']/*[local-name()='natuurlijkPersoon']/*[local-name()='soFiNummer']");
    obRefs = (NodeList) bsn.evaluate(wozObjectNode, XPathConstants.NODESET);
    if (obRefs.getLength() > 0 && !StringUtils.isEmpty(obRefs.item(0).getTextContent())) {
      return PREFIX_PRS + obRefs.item(0).getTextContent();
    }

    // WOZ:object StUF:entiteittype="NNP"/WOZ:isEen/WOZ:gerelateerde/BG:inn.nnpId
    XPathExpression nnpIdXpath =
        xPathfactory
            .newXPath()
            .compile("./*/*[local-name()='gerelateerde']/*[local-name()='inn.nnpId']");
    obRefs = (NodeList) nnpIdXpath.evaluate(wozObjectNode, XPathConstants.NODESET);
    if (obRefs.getLength() > 0 && !StringUtils.isEmpty(obRefs.item(0).getTextContent())) {
      return PREFIX_NNP + obRefs.item(0).getTextContent();
    }
    // er komen berichten voor in test set waarin geen nnpId zit, maar wel
    // "aanvullingSoFiNummer" is gevuld...
    // WOZ:object StUF:entiteittype="NNP"/WOZ:aanvullingSoFiNummer
    nnpIdXpath =
        xPathfactory
            .newXPath()
            .compile("*[@StUF:entiteittype='NNP']/*[local-name()='aanvullingSoFiNummer']");
    obRefs = (NodeList) nnpIdXpath.evaluate(wozObjectNode, XPathConstants.NODESET);
    if (obRefs.getLength() > 0 && !StringUtils.isEmpty(obRefs.item(0).getTextContent())) {
      LOG.warn("WOZ NNP zonder `inn.nnpId`, gebruik `aanvullingSoFiNummer` voor id.");
      return PREFIX_NNP + obRefs.item(0).getTextContent();
    }

    // WOZ:object StUF:entiteittype="WRD"/WOZ:isVoor/WOZ:gerelateerde/WOZ:wozObjectNummer
    XPathExpression wrd =
        xPathfactory
            .newXPath()
            .compile("./*/*[local-name()='gerelateerde']/*[local-name()='wozObjectNummer']");
    obRefs = (NodeList) wrd.evaluate(wozObjectNode, XPathConstants.NODESET);
    if (obRefs.getLength() > 0 && !StringUtils.isEmpty(obRefs.item(0).getTextContent())) {
      return PREFIX_WOZ + obRefs.item(0).getTextContent();
    }

    // WOZ:object StUF:entiteittype="VES"/WOZ:isEen/WOZ:gerelateerde/BG:vestigingsNummer
    XPathExpression ves =
        xPathfactory
            .newXPath()
            .compile("./*/*[local-name()='gerelateerde']/*[local-name()='vestigingsNummer']");
    obRefs = (NodeList) ves.evaluate(wozObjectNode, XPathConstants.NODESET);
    if (obRefs.getLength() > 0 && !StringUtils.isEmpty(obRefs.item(0).getTextContent())) {
      return PREFIX_VES + obRefs.item(0).getTextContent();
    }

    return null;
  }

  /**
   * maakt een map met bsn,bsnhash.
   *
   * @param n document node met bsn-nummer
   * @return hashmap met bsn,bsnhash
   * @throws XPathExpressionException if any
   */
  public Map<String, String> extractBSN(Node n) throws XPathExpressionException {
    Map<String, String> hashes = new HashMap<>();
    XPath xpath = xPathfactory.newXPath();
    XPathExpression expr = xpath.compile("//*[local-name() = 'inp.bsn']");
    NodeList nodelist = (NodeList) expr.evaluate(n, XPathConstants.NODESET);
    for (int i = 0; i < nodelist.getLength(); i++) {
      Node bsn = nodelist.item(i);
      String bsnString = bsn.getTextContent();
      String hash = getHash(bsnString);
      hashes.put(bsnString, hash);
    }
    return hashes;
  }

  public String getXML(Map<String, String> map) throws ParserConfigurationException {
    if (map.isEmpty()) {
      // als in bericht geen personen zitten
      return "";
    }
    String root = "<bsnhashes>";
    for (Map.Entry<String, String> entry : map.entrySet()) {
      if (!entry.getKey().isEmpty() && !entry.getValue().isEmpty()) {
        String hash = entry.getValue();
        String el =
            "<"
                + PREFIX_PRS
                + entry.getKey()
                + ">"
                + hash
                + "</"
                + PREFIX_PRS
                + entry.getKey()
                + ">";
        root += el;
      }
    }
    root += "</bsnhashes>";
    return root;
  }
}
