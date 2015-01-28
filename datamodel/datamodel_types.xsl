<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:b3p="http://www.b3partners.nl/" xmlns:fn="http://www.w3.org/2005/xpath-functions">

	<!-- Functies voor het omzetten van simpele RSGB types naar SQL types -->
	
	<!-- Geeft het SQL kolomtype voor een datatype uit het RSGB model -->
	<xsl:function name="b3p:get-column-type">
		<xsl:param name="typeEAID"/>	
		<xsl:param name="typeName"/>
		
		<xsl:variable name="elementType" select="b3p:get-element-type($typeEAID)"/>
		<xsl:choose>
			<!-- Ga er van uit dat een kolom type voor een enumeration altijd string van max 255 lengte is -->
			<xsl:when test="$elementType = 'Enumeratiesoort - Values'">
				varchar(255) /* enumeratie, maar zou niet meer gebruikt moeten worden! refDef moet true zijn! */</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="b3p:to-sql-type($typeName)"/> 
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	
	<xsl:function name="b3p:getColumnType">
		<xsl:param name="typeEAID"/>	
		<xsl:param name="typeName"/>
		<xsl:value-of select="b3p:get-column-type($typeEAID,$typeName)"/>
	</xsl:function>
		
	<xsl:function name="b3p:get-element-type">
		<xsl:param name="typeEAID"/>
		
		<xsl:value-of select="$rsgb/Objecttypes/Property[@classId = $typeEAID][1]/@elementType"/>
	</xsl:function>	

	<!-- Geeft een algemeen SQL type (wordt nog vervangen door database-specifiek type) op basis
         van een enterprise architect type 
    -->
	<xsl:function name="b3p:to-sql-type">
		<xsl:param name="type"/>

		<xsl:choose>
			<xsl:when test="$type = 'AN'">
				<xsl:text>varchar(255)</xsl:text>
			</xsl:when>
			<xsl:when test="fn:matches($type, '^AN?[0-9]+$')">
				<xsl:variable name="length" select="fn:replace($type,'AN?','')"/>
				<xsl:value-of select="string-join(('varchar(',$length,')'),'')"/>
			</xsl:when>
			<xsl:when test="fn:matches($type, '^N[0-9]+$')">
				<xsl:variable name="length" select="fn:replace($type,'N','')"/>
				<xsl:value-of select="string-join(('decimal(',$length,',0)'),'')"/>
			</xsl:when>
			<xsl:when test="not ($type)">
				<xsl:value-of select="'varchar(255)'"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="ea-type" select="$ea-types[@name=$type]"/>
				
				<xsl:choose>
					<!-- expliciete vervanging van bepaald datatype -->
					<xsl:when test="$ea-type/@vervanging"><xsl:value-of select="$ea-type/@vervanging"/></xsl:when>
					<xsl:otherwise><xsl:value-of select="$type"/></xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:function>	
		
	<xsl:variable name="ea-types" as="element(type)*">
		<type name="long" vervanging="integer"/>
		<type name="boolean" vervanging="char(1)"/>
		<type name="NEN3610ID" vervanging="varchar(255)"/>
		<!-- onvolledige datum: https://new.kinggemeenten.nl/sites/default/files/document/gr_4679/Advies%20issue%20120%20Generiek%20domein%20datum-tijd%20v0%209.pdf 
			2010-01-31T12:30:00
			2010-01-31
			2010-01
			2010
			in bag wordt ander format gebruikt jjjjmmddhhmmssmm
			2011030811410622
			voorlopig varchar(19) voor volledige datumtijd zonder tijdzone, daar past bag ook in
		-->
		<!-- Fout in xml.. Aanname dat // een onvolledige datum is -->
		<type name="//" vervanging="varchar(19)"/>
		<type name="OnvolledigeDatum" vervanging="varchar(19)"/>
		<type name="Onvolledigedatum" vervanging="varchar(19)"/>
		<type name="Datum" vervanging="date"/>
		<type name="datum/tijd" vervanging="timestamp"/>
		<type name="Jaar" vervanging="integer"/>	
		
		<type name="AN11 (NNNNN-NNNNN)" vervanging="varchar(11)"/>
		
		<type name="GM_Surface" vervanging="multipolygon"/>
		<type name="GM_Curve" vervanging="linestring"/>
		<type name="GM_Point" vervanging="point"/>
		<type name="GM_MultiSurface" vervanging="multipolygon"/>
		<type name="LijnVlak" vervanging="geometry"/>
		<type name="PuntLijnVlak" vervanging="geometry"/>
	</xsl:variable> 
</xsl:stylesheet>
