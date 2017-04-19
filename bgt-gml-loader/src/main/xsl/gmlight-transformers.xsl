<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : gmlight-transformers.xsl
    Created on : April 18, 2016, 10:01 AM
    Author     : mark
    Description: maak transformer klassen aan voor de BGT objecttypen
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:db="http://www.b3partners.nl/db-specific"
                xmlns:md="http://www.b3partners.nl/model-specific"
                xmlns:fn="http://www.w3.org/2005/xpath-functions">

    <xsl:import href="functions.xsl"/>
    
    <xsl:output method="text" encoding="utf-8"/>
    
    <xsl:param name="baseDirectory">target/generated-sources/xslt/</xsl:param>
    
    <xsl:template match="/">
        <xsl:apply-templates select="/datamodel/objecttype" mode="generateClass" />
    </xsl:template>

    <xsl:template match="objecttype" mode="generateClass">
        
        <xsl:variable name="className" select="@clazz"/>

        <xsl:variable name="filename" select="concat($baseDirectory,'nl/b3p/brmo/loader/gml/light/',$className,'.java')" />
        <xsl:value-of select="$filename" />

        <xsl:result-document href="{$filename}" method="text">
            
            <xsl:call-template name="fileheader"/>
            
            <xsl:text>
/** BGT Light GML transformer voor RSGB 3.0 objecttype </xsl:text>
            <xsl:value-of select="$className"/>
            <xsl:text>. */
public class </xsl:text>
            <xsl:value-of select="$className" />
            <xsl:text> extends GMLLightFeatureTransformerImpl {

    public </xsl:text>
            <xsl:value-of select="$className"/>
            <xsl:text>() {
</xsl:text>
            <xsl:for-each select="attribuut">
                <xsl:value-of select="md:addTransformer(@xslname,@sqlname)"/>
            </xsl:for-each>
            <xsl:text>
    }
}
</xsl:text>
        </xsl:result-document>
    </xsl:template>

    <xsl:function name="md:addTransformer">
        <xsl:param name="xslname"/>
        <xsl:param name="sqlname"/>

        <xsl:text>        attrMapping.put("</xsl:text>
        <xsl:value-of select="$xslname"/>
        <xsl:text>", </xsl:text>

        <xsl:choose>
            <xsl:when test="$sqlname eq 'null'">
                <xsl:value-of select="$sqlname"/>
            </xsl:when>
            <xsl:when test="$static-col-names/name[@java=$sqlname]">
                <xsl:value-of select="$sqlname"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>"</xsl:text>
                <xsl:value-of select="$sqlname"/>
                <xsl:text>"</xsl:text>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:text>);
</xsl:text>
    </xsl:function>


    <xsl:template name="fileheader">
        <!-- file header -->
        <xsl:text>/*
*  Copyright (C) </xsl:text>
        <xsl:value-of select="year-from-date(current-date())"/>
        <xsl:text> B3Partners B.V.
*
* Gegenereerde code, niet aanpassen.
*
* Gegenereerd op: </xsl:text>
        <xsl:value-of select="current-dateTime()"/>
        <xsl:text>
*/
package nl.b3p.brmo.loader.gml.light;
        </xsl:text>
    </xsl:template>
</xsl:stylesheet>
