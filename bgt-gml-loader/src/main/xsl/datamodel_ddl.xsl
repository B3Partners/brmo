<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:db="http://www.b3partners.nl/db-specific"
                xmlns:md="http://www.b3partners.nl/model-specific"
                xmlns:fn="http://www.w3.org/2005/xpath-functions">

    <xsl:import href="functions.xsl"/>

    <xsl:param name="versie">developer</xsl:param>

    <xsl:template match="/">
        <xsl:call-template name="header"/>
        <xsl:call-template name="metatables" />
        <xsl:apply-templates select="/datamodel/objecttype" mode="generateSQL" />
    </xsl:template>

    <xsl:template name="header">
        <xsl:text>--
-- BRMO BGT/RSGB3 create script voor: </xsl:text>
        <xsl:value-of select="$dbtype"/>
        <xsl:text>
-- Applicatie versie: </xsl:text>
        <xsl:value-of select="$versie"/>
        <xsl:text>
-- Gegenereerd op: </xsl:text>
        <xsl:value-of select="current-dateTime()"/>
        <xsl:text>
--
</xsl:text>
    </xsl:template>

    <xsl:template name="metatables">
        <xsl:value-of select="db:addMetaTables()"/>
        <xsl:text>
-- brmo versienummer
INSERT INTO brmo_metadata (naam, waarde) VALUES ('brmoversie','</xsl:text><xsl:value-of select="$versie"/><xsl:text>');
-- gemeente data versie
INSERT INTO brmo_metadata (naam, waarde) VALUES ('update_gem_tabel', '</xsl:text><xsl:value-of select="$versie"/><xsl:text>');
</xsl:text>
    </xsl:template>

    <xsl:template match="objecttype" mode="generateSQL">
        <xsl:variable name="schema" select="'BRMO_RSGBBGT'"/>
        <xsl:variable name="tableName" select="@table"/>
        <xsl:text>
-- Klasse: </xsl:text>
        <xsl:value-of select="@clazz"/>
        <xsl:text>
CREATE TABLE </xsl:text>
        <xsl:value-of select="$tableName"/>
        <xsl:text> (
        </xsl:text>
        <xsl:value-of select="md:colName('ID_NAME')" />
        <xsl:text> </xsl:text>
        <xsl:value-of select="db:type('varchar(255)')"/>
        <xsl:text> NOT NULL,
        </xsl:text>
        <xsl:value-of select="md:colName('BEGINTIJD_NAME')" />
        <xsl:text> date,
        datum_einde_geldh date,
        relve_hoogteligging </xsl:text>
        <xsl:value-of select="db:type('integer')"/>
        <xsl:text>,
        bgt_status </xsl:text>
        <xsl:value-of select="db:type('varchar(255)')"/>
        <xsl:text>,
        plus_status </xsl:text>
        <xsl:value-of select="db:type('varchar(255)')"/>
        <xsl:text>,
        </xsl:text>
        <!-- specifieke objecttype velden -->
        <xsl:for-each select="attribuut">
            <xsl:value-of select="db:addColumn(md:colName(@sqlname),@sqltype)"/>
        </xsl:for-each>
        <!-- brmo metadata velden -->
        <xsl:value-of select="md:colName('BIJWERKDATUM_NAME')" />
        <xsl:text> date,
        PRIMARY KEY (</xsl:text>
        <xsl:value-of select="md:colName('ID_NAME')" />
        <xsl:text>)
);

</xsl:text>

        <!-- PK metadata -->
        <xsl:value-of select="db:addPKMetaData($tableName, md:colName(@sqlname), $schema)"/>

        <!-- geometrie metadata en geom.indexen -->
        <xsl:for-each select="attribuut">
            <xsl:if test="(fn:contains($geometryTypes,@sqltype))">
                <xsl:value-of select="db:addGeometryMetaData($tableName, md:colName(@sqlname), @sqltype, $schema)"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
