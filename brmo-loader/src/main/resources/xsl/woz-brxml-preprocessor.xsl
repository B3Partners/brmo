<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright (C) 2021 B3Partners B.V. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:WOZ="http://www.waarderingskamer.nl/StUF/0312"
                xmlns:BG="http://www.egem.nl/StUF/sector/bg/0310"
                xmlns:StUF="http://www.egem.nl/StUF/StUF0301"
                version="1.0"
                exclude-result-prefixes="WOZ BG StUF"
>
    <xsl:output indent="yes" method="xml"/>

    <xsl:template match="*">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*[@StUF:noValue='geenWaarde']">
        <xsl:copy><xsl:apply-templates select="@*|node()"/>geenWaarde
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@*|text()|comment()|processing-instruction()">
        <xsl:copy-of select="."/>
    </xsl:template>

</xsl:stylesheet>