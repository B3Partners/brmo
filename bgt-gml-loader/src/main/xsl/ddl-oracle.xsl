<?xml version="1.0" encoding="UTF-8"?>
<!--
    Created on : April 18, 2016, 10:24 AM
    Author     : mark
    Description: maakt DDL voor BGT / RSGB 3 in Oracle.
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:db="http://www.b3partners.nl/db-specific"
                xmlns:md="http://www.b3partners.nl/model-specific"
                xmlns:fn="http://www.w3.org/2005/xpath-functions">

    <xsl:import href="datamodel_ddl.xsl"/>

    <xsl:variable name="dbtype">oracle</xsl:variable>

    <xsl:variable name="geom-types">
        <geom rsgb="polygon" db="POLYGON"/>
        <geom rsgb="multipolygon" db="MULTIPOLYGON"/>
        <geom rsgb="point" db="POINT"/>
        <geom rsgb="multipoint" db="MULTIPOINT"/>
        <geom rsgb="linestring" db="LINE"/>
        <geom rsgb="multilinestring" db="MULTILINE"/>
        <geom rsgb="multicurve" db="MULTICURVE"/>
        <geom rsgb="geometry" db="COLLECTION"/>
    </xsl:variable>
    
    <xsl:function name="db:type">
        <xsl:param name="type"/>

        <!-- vervangen van type namen:
        varchar -> varchar2
        boolean -> VARCHAR2(5)
        integer -> NUMBER(10)
        -->
        <xsl:value-of select="fn:replace(
                    fn:replace(
                    fn:replace($type,'varchar','VARCHAR2'),
                                'boolean','VARCHAR2(5)'),
                                'integer','NUMBER(10)'
                    )"/>
    </xsl:function>

    <xsl:function name="db:addColumn">
        <xsl:param name="column"/>
        <xsl:param name="type"/>

        <xsl:value-of select="upper-case($column)"/>
        <xsl:text> </xsl:text>
        <xsl:choose>
            <xsl:when test="not (fn:contains($geometryTypes,$type))">
                <xsl:value-of select="db:type($type)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>MDSYS.SDO_GEOMETRY</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>,
        </xsl:text>
    </xsl:function>

    <xsl:function name="db:shortName">
        <xsl:param name="longName"/>

        <xsl:value-of select="fn:replace(
        fn:replace(
            fn:replace($longName,'OPENBARERUIMTELABEL','OPRLBL','i'),
                                 'ONGECLASSIFICEERD', 'OCLAS','i'),
                                 'ONDERSTEUNEND', 'ODRSTEUN','i')" />
    </xsl:function>

    <xsl:function name="db:addGeometryMetaData">
        <xsl:param name="table"/>
        <xsl:param name="column"/>
        <xsl:param name="type"/>

        <xsl:text>INSERT INTO USER_SDO_GEOM_METADATA VALUES('</xsl:text>
        <xsl:value-of select="upper-case($table)"/>
        <xsl:text>', '</xsl:text>
        <xsl:value-of select="upper-case($column)"/>
        <xsl:text>',
        MDSYS.SDO_DIM_ARRAY(
            MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
            MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)
        ), 28992);            
</xsl:text>
    
        <xsl:text>CREATE INDEX </xsl:text>
        <xsl:value-of select="upper-case(fn:substring(concat(db:shortName($table),'_',$column),1,26))"/>
        <xsl:text>_IDX ON </xsl:text>
        <xsl:value-of select="upper-case($table)"/>
        <xsl:text> (</xsl:text>
        <xsl:value-of select="upper-case($column)"/>
        <xsl:text>)
        INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ('SDO_INDX_DIMS=2 LAYER_GTYPE=</xsl:text>
        <xsl:value-of select="$geom-types/geom[@rsgb=$type]/@db"/>
        <xsl:text>');
</xsl:text>
    </xsl:function>
    
</xsl:stylesheet>