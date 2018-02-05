<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:gwr-bestand="http://www.kadaster.nl/schemas/bag-verstrekkingen/gwr-deelbestand-lvc/v20120701"
                xmlns:selecties-extract="http://www.kadaster.nl/schemas/bag-verstrekkingen/extract-selecties/v20110901" 
                xmlns:bagtype="http://www.kadaster.nl/schemas/imbag/imbag-types/v20110901"
                xmlns:gwr-product="http://www.kadaster.nl/schemas/bag-verstrekkingen/gwr-producten-lvc/v20120701"
                xmlns:gwr_LVC="http://www.kadaster.nl/schemas/bag-gwr-model/lvc/v20120701"
                xmlns:gwr_gemeente="http://www.kadaster.nl/schemas/bag-gwr-model/gemeente/v20120701"
                xsi:schemaLocation="http://www.kadaster.nl/schemas/bag-verstrekkingen/gwr-deelbestand-lvc/v20120701 http://www.kadaster.nl/schemas/bag-verstrekkingen/gwr-deelbestand-lvc/v20120701/BagvsGwrDeelbestandLvc-1.4.xsd">
    <xsl:output method="text" encoding="utf-8" />
    <xsl:strip-space elements="*" />
    <xsl:variable name="separator" select="'&#59;'" />
    <xsl:variable name="newline" select="'&#10;'" />

    <xsl:template match="/">
        <xsl:text>-- Update gemeente/woonplaats relatie koppelscript</xsl:text>
        <xsl:value-of select="$newline" />
        <xsl:text>-- Gegenereerd op: </xsl:text>
        <xsl:value-of select="current-dateTime()"/>
        <xsl:value-of select="$newline" />
        <xsl:text>-- StandTechnischeDatum van bronbestand: </xsl:text>
        <xsl:value-of select="//selecties-extract:StandTechnischeDatum"/>
        <xsl:value-of select="$newline" />    

        <!-- selecteer alle GemeenteWoonplaatsRelatie welke defnitief zijn en geen einddatumTijdvakGeldigheid hebben -->
        <xsl:for-each select="//gwr_LVC:GemeenteWoonplaatsRelatie">
            <xsl:if test="gwr_LVC:status = 'definitief'">
                <xsl:if test="not(./gwr_LVC:tijdvakgeldigheid/bagtype:einddatumTijdvakGeldigheid)" >
                    <!--
                    <xsl:text></xsl:text>-<xsl:text>- begindatumTijdvakGeldigheid:  </xsl:text>
                    <xsl:value-of select="./gwr_LVC:tijdvakgeldigheid/bagtype:begindatumTijdvakGeldigheid" />
                    <xsl:value-of select="$newline" />
                    -->
                    <xsl:text>UPDATE wnplts SET fk_7gem_code = </xsl:text>
                    <xsl:value-of select="number(./gwr_LVC:gerelateerdeGemeente/gwr_LVC:identificatie)" />
                    <xsl:text> WHERE identif = '</xsl:text>
                    <xsl:value-of select="./gwr_LVC:gerelateerdeWoonplaats/gwr_LVC:identificatie" />
                    <xsl:text>'</xsl:text>
                    <xsl:value-of select="$separator" />
                    <xsl:value-of select="$newline" />
                </xsl:if>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
