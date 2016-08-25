<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:db="http://www.b3partners.nl/db-specific"
                xmlns:md="http://www.b3partners.nl/model-specific"
                xmlns:fn="http://www.w3.org/2005/xpath-functions">
    <xsl:output method="text" encoding="utf-8"/>

    <xsl:param name="versie">developer</xsl:param>

    <xsl:template match="/">
        <xsl:call-template name="header"/>
        <xsl:apply-templates select="/datamodel/objecttype" mode="dropSQL" />
        <xsl:call-template name="metatables" />
    </xsl:template>


    <xsl:template name="header">
        <xsl:text>--
-- BRMO BGT/RSGB3 drop script voor: </xsl:text>
        <xsl:value-of select="$dbtype"/>
        <xsl:text>
-- Applicatie versie: </xsl:text>
        <xsl:value-of select="$versie"/>
        <xsl:text>
-- Gegenereerd op: </xsl:text>
        <xsl:value-of select="current-dateTime()"/>
        <xsl:text>
--
</xsl:text>
    </xsl:template>

    <xsl:template name="metatables">
        <xsl:value-of select="db:dropMetaTables()"/>
    </xsl:template>

</xsl:stylesheet>