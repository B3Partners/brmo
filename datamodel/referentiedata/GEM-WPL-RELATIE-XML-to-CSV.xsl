<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:gwr-bestand="www.kadaster.nl/schemas/lvbag/gem-wpl-rel/gwr-deelbestand-lvc/v20200601"
                xmlns:selecties-extract="http://www.kadaster.nl/schemas/lvbag/extract-selecties/v20200601"
                xmlns:bagtypes="www.kadaster.nl/schemas/lvbag/gem-wpl-rel/bag-types/v20200601"
                xmlns:gwr-product="www.kadaster.nl/schemas/lvbag/gem-wpl-rel/gwr-producten-lvc/v20200601"
                xmlns:DatatypenNEN3610="www.kadaster.nl/schemas/lvbag/imbag/datatypennen3610/v20200601"
                xsi:schemaLocation="www.kadaster.nl/schemas/lvbag/gem-wpl-rel/gwr-deelbestand-lvc/v20200601 https://developer.kadaster.nl/schemas/lvbag/gem-wpl-rel/gwr-deelbestand-lvc/v20200601/BagvsGwrDeelbestandLvc-2.1.0.xsd">
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

        <!-- selecteer alle GemeenteWoonplaatsRelatie welke definitief zijn en geen einddatumTijdvakGeldigheid hebben -->
        <xsl:for-each select="//gwr-product:GemeenteWoonplaatsRelatie">
            <xsl:if test="gwr-product:status = 'definitief'">
                <xsl:if test="not(./gwr-product:tijdvakgeldigheid/bagtypes:einddatumTijdvakGeldigheid)" >
                    <xsl:text>UPDATE wnplts SET fk_7gem_code = </xsl:text>
                    <xsl:value-of select="number(./gwr-product:gerelateerdeGemeente/gwr-product:identificatie)" />
                    <xsl:text> WHERE identif = '</xsl:text>
                    <xsl:value-of select="./gwr-product:gerelateerdeWoonplaats/gwr-product:identificatie" />
                    <xsl:text>'</xsl:text>
                    <xsl:value-of select="$separator" />
                    <xsl:value-of select="$newline" />
                </xsl:if>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
