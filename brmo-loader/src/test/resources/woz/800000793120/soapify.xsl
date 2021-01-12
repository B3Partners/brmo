<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dgl="http://www.digilevering.nl/digilevering.xsd"
                xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:WOZ="http://www.waarderingskamer.nl/StUF/0312"
                xmlns:StUF="http://www.egem.nl/StUF/StUF0301"
                xmlns:BG="http://www.egem.nl/StUF/sector/bg/0310"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gml="http://www.opengis.net/gml"
                version="1.0">

    <xsl:output omit-xml-declaration="no" method="xml" version="1.0" indent="yes"/>

    <!-- omkatten van digilevering naar soap bericht -->
    <xsl:template match="dgl:VerstrekkingAanAfnemer">
        <soapenv:Envelope>
            <soapenv:Header/>
            <soapenv:Body>
                <xsl:comment>soapified uit gebeurtenisinhoud</xsl:comment>
                <xsl:apply-templates select="gebeurtenisinhoud"/>
            </soapenv:Body>
        </soapenv:Envelope>
    </xsl:template>

    <xsl:template match="gebeurtenisinhoud">
        <xsl:copy-of select="*"/>
    </xsl:template>

</xsl:stylesheet>