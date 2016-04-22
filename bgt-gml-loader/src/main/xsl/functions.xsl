<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:db="http://www.b3partners.nl/db-specific"
                xmlns:md="http://www.b3partners.nl/model-specific"
                xmlns:fn="http://www.w3.org/2005/xpath-functions">

    <xsl:output method="text" encoding="utf-8"/>

    <xsl:variable name="geometryTypes" select="'polygon multipolygon linestring multilinestring point multipoint multicurve geometry'"/>

    <xsl:variable name="static-col-names">
        <!-- java static finals in GMLLightFeatureTransformer interface -->
        <name java="ID_NAME" mapped="identif"/>
        <name java="DEFAULT_GEOM_NAME" mapped="geom2d"/>
        <name java="KRUINLIJN_GEOM_NAME" mapped="kruinlijn"/>
        <name java="LOD0_GEOM_NAME" mapped="lod0geom"/>
        <name java="LOD1_GEOM_NAME" mapped="lod1geom"/>
        <name java="LOD2_GEOM_NAME" mapped="lod2geom"/>
        <name java="LOD3_GEOM_NAME" mapped="lod3geom"/>
        <name java="BIJWERKDATUM_NAME" mapped="bijwerkdatum"/>
        <name java="BEGINTIJD_NAME" mapped="dat_beg_geldh"/>
    </xsl:variable>

    <xsl:function name="md:colName">
        <!-- vervangen van kolom namen met die uit de lijst, mits ze voorkomen
        @param colName kolomnaam
        @returns al dan niet vervangen kolomnaam
        -->
        <xsl:param name="colName"/>
        <xsl:choose>
            <xsl:when test="$static-col-names/name[@java=$colName]">
                <xsl:value-of select="$static-col-names/name[@java=$colName]/@mapped"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$colName" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
</xsl:stylesheet>
