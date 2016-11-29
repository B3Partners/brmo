<?xml version="1.0" encoding="UTF-8"?>
<!--
    Created on : April 18, 2016, 10:24 AM
    Author     : mark
    Description: maakt DDL voor BGT / RSGB 3 in MS SQL
-->
   
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:db="http://www.b3partners.nl/db-specific"
                xmlns:md="http://www.b3partners.nl/model-specific"
                xmlns:fn="http://www.w3.org/2005/xpath-functions">

    <xsl:import href="datamodel_ddl.xsl"/>
        
    <xsl:variable name="dbtype">sqlserver</xsl:variable>

    <xsl:variable name="geom-types">
        <geom rsgb="polygon" db="geometry"/>
        <geom rsgb="multipolygon" db="geometry"/>
        <geom rsgb="point" db="geometry"/>
        <geom rsgb="multipoint" db="geometry"/>
        <geom rsgb="linestring" db="geometry"/>
        <geom rsgb="multilinestring" db="geometry"/>
        <geom rsgb="multicurve" db="geometry"/>
        <geom rsgb="geometry" db="geometry"/>
    </xsl:variable>
        

    <xsl:function name="db:type">
        <!-- vervangen van type namen:
        timestamp -> datetime
        boolean -> varchar(5)
        -->
        <xsl:param name="type"/>

        <xsl:value-of select="fn:replace(fn:replace($type,'timestamp','datetime'),'boolean','varchar(5)')"/>
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
                <xsl:text>geometry</xsl:text>
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

        <xsl:text>CREATE SPATIAL INDEX </xsl:text>
        <xsl:value-of select="concat($table,'_',$column)"/>
        <xsl:text>_idx ON </xsl:text>
        <xsl:value-of select="$table"/>
        <xsl:text>(</xsl:text>
        <xsl:value-of select="$column"/>
        <xsl:text>) WITH ( BOUNDING_BOX = (12000,304000,280000,620000));
</xsl:text>
    </xsl:function>

    <xsl:function name="db:addMetaTables">
        <xsl:text>CREATE TABLE brmo_metadata (
        naam VARCHAR(255) NOT NULL,
        waarde VARCHAR(255),
        PRIMARY KEY (naam)
);

GO

EXEC sys.sp_addextendedproperty @name=N'comment', @value=N'BRMO metadata en versie gegevens' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'brmo_metadata'

GO
</xsl:text>
    </xsl:function>
</xsl:stylesheet>
