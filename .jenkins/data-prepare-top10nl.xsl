<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:top10nl="http://register.geostandaarden.nl/gmlapplicatieschema/top10nl/1.2.0"
                xmlns:brt="http://register.geostandaarden.nl/gmlapplicatieschema/brt-algemeen/1.2.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gss="http://www.isotc211.org/2005/gss"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:gsr="http://www.isotc211.org/2005/gsr">
    <xsl:output method="xml" version="1.0" indent="yes" />

    <!-- eerste 5 top10nl features uit de input -->
    <xsl:template match="/top10nl:FeatureCollectionT10NL">
        <xsl:variable name="gmlid" select="./@gml:id" />
        <top10nl:FeatureCollectionT10NL gml:id="{$gmlid}">
            <xsl:apply-templates select="top10nl:FeatureMember[position() &lt;= 5]" />
        </top10nl:FeatureCollectionT10NL>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
