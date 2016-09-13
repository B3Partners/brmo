<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:db="http://www.b3partners.nl/db-specific" xmlns:fn="http://www.w3.org/2005/xpath-functions">
	
	<xsl:import href="datamodel_ddl.xsl"/>

    <xsl:param name="versie">developer</xsl:param>
	
	<xsl:variable name="dbtype">sqlserver</xsl:variable>
	<xsl:variable name="dbpkdef">primary key clustered</xsl:variable>
	<xsl:variable name="addcomment">false</xsl:variable>
	
	<xsl:variable name="geom-types">
		<geom rsgb="polygon" db="geometry"/>
		<geom rsgb="multipolygon" db="geometry"/>
		<geom rsgb="point" db="geometry"/>
		<geom rsgb="multipoint" db="geometry"/>
		<geom rsgb="linestring" db="geometry"/>
		<geom rsgb="multilinestring" db="geometry"/>
		<geom rsgb="geometry" db="geometry"/>
	</xsl:variable>
	
	<xsl:function name="db:type">
		<xsl:param name="type"/>
		<xsl:value-of select="fn:replace(fn:replace($type,'timestamp','datetime'),'default','varchar(255)')"/>
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

		<xsl:variable name="bbox">
			<xsl:text>(</xsl:text>
			<xsl:text>12000</xsl:text>
			<xsl:text>,</xsl:text>
			<xsl:text>304000</xsl:text>
			<xsl:text>,</xsl:text>
			<xsl:text>280000</xsl:text>
			<xsl:text>,</xsl:text>
			<xsl:text>620000</xsl:text>
			<xsl:text>)</xsl:text>
		</xsl:variable>
		
		<xsl:text>alter table </xsl:text><xsl:value-of select="$table"/><xsl:text> add </xsl:text><xsl:value-of select="$column"/><xsl:text> geometry;
</xsl:text>
		<xsl:text>CREATE SPATIAL INDEX </xsl:text><xsl:value-of select="fn:substring(concat($table,'_',$column),1,25)"/><xsl:value-of select="$pos"/><xsl:text>_idx ON </xsl:text><xsl:value-of select="$table"/><xsl:text> (</xsl:text><xsl:value-of select="$column"/><xsl:text>) WITH ( BOUNDING_BOX = </xsl:text><xsl:value-of select="$bbox"/><xsl:text>);
</xsl:text>
	</xsl:function>
</xsl:stylesheet>
