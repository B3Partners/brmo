<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:top50nl="http://register.geostandaarden.nl/gmlapplicatieschema/top50nl/1.1.1"
                xmlns:brt="http://register.geostandaarden.nl/gmlapplicatieschema/brt-algemeen/1.2.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gss="http://www.isotc211.org/2005/gss"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:gsr="http://www.isotc211.org/2005/gsr">
    <xsl:output method="xml" version="1.0" indent="yes" />

    <!-- eerste 5 top50nl features uit de input -->
    <xsl:template match="/top50nl:FeatureCollectionTop50">
        <xsl:variable name="gmlid" select="./@gml:id" />
        <top50nl:FeatureCollectionTop50 gml:id="{$gmlid}">
            <xsl:apply-templates select="top50nl:FeatureMember[position() &lt;= 5]" />
        </top50nl:FeatureCollectionTop50>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
