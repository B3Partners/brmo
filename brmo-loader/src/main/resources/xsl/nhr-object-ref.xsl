<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:cat="http://schemas.kvk.nl/schemas/hrip/catalogus/2013/01">


	<xsl:template name="get-name-abbr">
		<xsl:variable name="n" select="local-name(.)"/>
		<xsl:choose>
			<xsl:when test="$n ='maatschappelijkeActiviteit'">maatschAct</xsl:when>
			<xsl:when test="$n ='commercieleVestiging'">comVestg</xsl:when>
			<xsl:when test="$n ='nietCommercieleVestiging'">nietComVestg</xsl:when>
			<xsl:when test="$n ='rechtspersoon'">rechtspersoon</xsl:when>
			<xsl:when test="$n ='natuurlijkPersoon'">natPers</xsl:when>
			<xsl:when test="$n ='buitenlandseVennootschap'">buitenlVenn</xsl:when>
			<xsl:when test="$n ='eenmanszaakMetMeerdereEigenaren'">eenmZMeerEig</xsl:when>
			<xsl:when test="$n ='rechtspersoonInOprichting'">rechtspersOpr</xsl:when>
			<xsl:when test="$n ='samenwerkingsverband'">samenwvb</xsl:when>
			<xsl:when test="$n ='buitenlandseVennootschap'">buitenlVenn</xsl:when>
			<xsl:when test="$n ='onderneming'">onderneming</xsl:when>
			<xsl:otherwise><xsl:value-of select="$n"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="*[cat:kvkNummer]" mode="object_ref">nhr.<xsl:call-template name="get-name-abbr"/>.kvk.<xsl:value-of select="cat:kvkNummer"/></xsl:template>
	<xsl:template match="*[cat:vestigingsnummer]" mode="object_ref">nhr.<xsl:call-template name="get-name-abbr"/>.<xsl:value-of select="cat:vestigingsnummer"/></xsl:template>
	<xsl:template match="*[cat:bsn or cat:rsin]" mode="object_ref">nhr.<xsl:call-template name="get-name-abbr"/>.<xsl:if test="cat:bsn">bsn.<xsl:value-of select="cat:bsn"/></xsl:if><xsl:if test="cat:rsin">rsin.<xsl:value-of select="cat:rsin"/></xsl:if></xsl:template>
</xsl:stylesheet>
