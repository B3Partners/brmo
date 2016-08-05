<?xml version="1.0" encoding="UTF-8"?>
<!--
     Document   : firsttwo.xsl
     Created on : April 26, 2016, 9:08 AM
     Author     : mark
     Description: haal de eerste 10 featureMember elementen uit een gml
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:imgeo-s="http://www.geostandaarden.nl/imgeo/2.1/simple/gml31" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gml="http://www.opengis.net/gml" xsi:schemaLocation="http://www.geostandaarden.nl/imgeo/2.1/simple/gml31 imgeo-simple-2.1-gml31.xsd http://www.opengis.net/gml http://schemas.opengis.net/gml/3.1.1/base/gml.xsd" version="2.0">
  <xsl:output method="xml" omit-xml-declaration ="no" indent="yes" />
  <xsl:template match="node()|@*" name="identity">
    <xsl:copy>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates />
    </xsl:copy>
  </xsl:template>
  <xsl:template match="gml:featureMember" />
  <xsl:template match="gml:featureMember[position() &lt; 11]">
    <xsl:call-template name="identity" />
  </xsl:template>
</xsl:stylesheet>
