<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:fn="http://www.w3.org/2005/xpath-functions"
 xmlns:ns1="http://www.egem.nl/StUF/StUF0204" xmlns:ns2="http://www.egem.nl/StUF/sector/bg/0204" 
 xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
exclude-result-prefixes="ns1 ns2"
 >
    <xsl:output indent="yes" method="xml"/>

    <xsl:template match="*">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[@ns1:noValue='geenWaarde']">
        <xsl:copy select=".">
            <xsl:apply-templates select="@*|node()"/>geenWaarde</xsl:copy>
    </xsl:template>


    <xsl:template match="@*|text()|comment()|processing-instruction()">
        <xsl:copy-of select="."/>
    </xsl:template>


</xsl:stylesheet>