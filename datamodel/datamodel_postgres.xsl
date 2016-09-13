<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:db="http://www.b3partners.nl/db-specific" xmlns:fn="http://www.w3.org/2005/xpath-functions">
	
	<xsl:import href="datamodel_ddl.xsl"/>

    <xsl:param name="versie">developer</xsl:param>
	
	<xsl:variable name="dbtype">postgresql</xsl:variable>
	<xsl:variable name="dbpkdef">primary key</xsl:variable>
	<xsl:variable name="addcomment">true</xsl:variable>
	
	<xsl:variable name="geom-types">
		<geom rsgb="polygon" db="POLYGON"/>
		<geom rsgb="multipolygon" db="MULTIPOLYGON"/>
		<geom rsgb="point" db="POINT"/>
		<geom rsgb="multipoint" db="MULTIPOINT"/>
		<geom rsgb="linestring" db="LINESTRING"/>
		<geom rsgb="multilinestring" db="MULTILINESTRING"/>
		<geom rsgb="geometry" db="GEOMETRY"/>
	</xsl:variable>
	
	<xsl:function name="db:type">
		<xsl:param name="type"/>
		<xsl:value-of select="fn:replace(fn:replace($type,'varchar','character varying'),'default','character varying(255)')"/>
	</xsl:function>
	
	<xsl:function name="db:string-literal">
		<xsl:param name="s"/>
		<xsl:value-of select="concat('''',fn:replace($s,'''',''''''),'''')"/>
	</xsl:function>	
	
	<xsl:function name="db:addGeometryColumn">
		<xsl:param name="table"/>	
		<xsl:param name="column"/>	
		<xsl:param name="type"/>
		<xsl:param name="pos"/>
		<xsl:text>select addgeometrycolumn('</xsl:text><xsl:value-of select="$table"/><xsl:text>', '</xsl:text><xsl:value-of select="$column"/><xsl:text>', 28992, '</xsl:text><xsl:value-of select="$geom-types/geom[@rsgb=$type]/@db"/><xsl:text>', 2);
</xsl:text>
		<xsl:text>create index </xsl:text><xsl:value-of select="$table"/><xsl:text>_</xsl:text><xsl:value-of select="$column"/><xsl:text>_idx on </xsl:text><xsl:value-of select="$table"/><xsl:text> USING GIST (</xsl:text><xsl:value-of select="$column"/><xsl:text>);
</xsl:text>
	</xsl:function>		
</xsl:stylesheet>
