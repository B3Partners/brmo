<?xml version="1.0" encoding="UTF-8"?>
<!--
    Created on : April 18, 2016, 10:24 AM
    Author     : mark
    Description: maakt DDL voor BGT / RSGB 3 in Postgis.
-->

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                    xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:db="http://www.b3partners.nl/db-specific"
                xmlns:md="http://www.b3partners.nl/model-specific"
                    xmlns:fn="http://www.w3.org/2005/xpath-functions">

    <xsl:import href="datamodel_ddl.xsl"/>

    <xsl:variable name="dbtype">postgresql</xsl:variable>

    <xsl:variable name="geom-types">
        <geom rsgb="polygon" db="POLYGON"/>
        <geom rsgb="multipolygon" db="MULTIPOLYGON"/>
        <geom rsgb="point" db="POINT"/>
        <geom rsgb="multipoint" db="MULTIPOINT"/>
        <geom rsgb="linestring" db="LINESTRING"/>
        <geom rsgb="multilinestring" db="MULTILINESTRING"/>
        <geom rsgb="multicurve" db="GEOMETRY"/>
        <geom rsgb="geometry" db="GEOMETRY"/>
    </xsl:variable>

    <xsl:function name="db:type">
        <!-- vervangen van type namen:
        varchar -> character varying
        -->
        <xsl:param name="type"/>

        <xsl:value-of select="fn:replace($type,'varchar','character varying')"/>
    </xsl:function>

    <xsl:function name="db:addColumn">
        <xsl:param name="column"/>
        <xsl:param name="type"/>

        <xsl:value-of select="$column"/>
        <xsl:text> </xsl:text>
        <xsl:choose>
            <xsl:when test="not (fn:contains($geometryTypes,$type))">
                <xsl:value-of select="db:type($type)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>geometry(</xsl:text>
                <xsl:value-of select="$geom-types/geom[@rsgb=$type]/@db"/>
                <xsl:text>,28992)</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>,
        </xsl:text>
    </xsl:function>

    <xsl:function name="db:addPKMetaData">
        <xsl:param name="table"/>
        <xsl:param name="column"/>
        <xsl:param name="schema"/>
    </xsl:function>

    <xsl:function name="db:addGeometryMetaData">
        <xsl:param name="table"/>
        <xsl:param name="column"/>
        <xsl:param name="type"/>
        <xsl:param name="schema"/>

        <xsl:text>CREATE INDEX </xsl:text>
        <xsl:value-of select="concat($table,'_',$column)"/>
        <xsl:text>_idx on </xsl:text>
        <xsl:value-of select="$table"/>
        <xsl:text> USING GIST (</xsl:text>
        <xsl:value-of select="$column"/>
        <xsl:text>);
</xsl:text>
    </xsl:function>

    <xsl:function name="db:addMetaTables">
        <xsl:text>CREATE TABLE brmo_metadata (
        naam CHARACTER VARYING(255) NOT NULL,
        waarde CHARACTER VARYING(255),
        CONSTRAINT brmo_metadata_pk PRIMARY KEY (naam)
);
COMMENT ON TABLE brmo_metadata IS 'BRMO metadata en versie gegevens';
</xsl:text>
    </xsl:function>
</xsl:stylesheet>
