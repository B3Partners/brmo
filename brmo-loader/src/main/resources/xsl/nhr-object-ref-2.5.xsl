<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:cat="http://schemas.kvk.nl/schemas/hrip/catalogus/2015/01">


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
	
	<xsl:template match="*[cat:kvkNummer]" mode="object_ref">
		<xsl:text>nhr.</xsl:text><xsl:call-template name="get-name-abbr"/><xsl:text>.kvk.</xsl:text>
		<xsl:value-of select="cat:kvkNummer"/>
	</xsl:template>

	<xsl:template match="*[cat:vestigingsnummer]" mode="object_ref">
		<xsl:text>nhr.</xsl:text><xsl:call-template name="get-name-abbr"/><xsl:text>.</xsl:text>
		<xsl:value-of select="cat:vestigingsnummer"/></xsl:template>

	<xsl:template match="*[cat:bsn or cat:rsin]" mode="object_ref">
		<xsl:text>nhr.</xsl:text><xsl:call-template name="get-name-abbr"/><xsl:text>.</xsl:text>
		<xsl:if test="cat:bsn">
			<xsl:text>bsn.</xsl:text><xsl:value-of select="cat:bsn"/>
		</xsl:if>
		<xsl:if test="cat:rsin">
			<xsl:text>rsin.</xsl:text><xsl:value-of select="cat:rsin"/>
		</xsl:if>
	</xsl:template>

	<!-- lege template voor gevallen zonder kvkNummer, vestigingsnummer, bsn of rsin bij heeftAlsEigenaar -->
	<xsl:template match="*" mode="object_ref" />
</xsl:stylesheet>
