<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:db="http://www.b3partners.nl/db-specific"
                xmlns:md="http://www.b3partners.nl/model-specific"
                xmlns:fn="http://www.w3.org/2005/xpath-functions">

    <xsl:import href="drop_ddl.xsl"/>

    <xsl:variable name="dbtype">sqlserver</xsl:variable>

    <xsl:template match="objecttype" mode="dropSQL">
        <xsl:text>DROP TABLE </xsl:text>
        <xsl:value-of select="@table"/>
        <xsl:text>;
</xsl:text>
    </xsl:template>

    <xsl:function name="db:dropMetaTables">
        <xsl:text>DROP TABLE brmo_metadata;
</xsl:text>
    </xsl:function>
</xsl:stylesheet>
