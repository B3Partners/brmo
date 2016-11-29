<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:db="http://www.b3partners.nl/db-specific"
                xmlns:md="http://www.b3partners.nl/model-specific"
                xmlns:fn="http://www.w3.org/2005/xpath-functions">
    <xsl:import href="drop_ddl.xsl"/>

    <xsl:variable name="dbtype">oracle</xsl:variable>

    <xsl:template match="objecttype" mode="dropSQL">
        <xsl:text>

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = '</xsl:text>
        <xsl:value-of select="upper-case(@table)"/>
        <xsl:text>';</xsl:text>

        <xsl:text>
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = '</xsl:text>
        <xsl:value-of select="upper-case(@table)"/>
        <xsl:text>';</xsl:text>

        <xsl:text>
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = '</xsl:text>
        <xsl:value-of select="upper-case(@table)"/>
        <xsl:text>';</xsl:text>

        <xsl:text>
DROP TABLE </xsl:text>
        <xsl:value-of select="upper-case(@table)"/>
        <xsl:text> CASCADE CONSTRAINTS;
</xsl:text>
    </xsl:template>

    <xsl:function name="db:dropMetaTables">
        <xsl:text>
DROP TABLE GEOMETRY_COLUMNS CASCADE CONSTRAINTS;
DROP TABLE GT_PK_METADATA CASCADE CONSTRAINTS;
DROP TABLE BRMO_METADATA CASCADE CONSTRAINTS;
</xsl:text>
    </xsl:function>


</xsl:stylesheet>
