<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cat="http://schemas.kvk.nl/schemas/hrip/catalogus/2015/02">

    <!-- zoek hash op in mapping tabel -->
    <xsl:template name="getHash">
        <xsl:param name="bsn" />
        <xsl:param name="hashsoort" />

        <xsl:variable name="bsnwithprefix">
            <xsl:text>nhr.</xsl:text>
            <xsl:value-of select="$hashsoort" />
            <xsl:text>natPers.</xsl:text>
            <xsl:value-of select="$bsn" />
        </xsl:variable>
        <xsl:variable name="hashedbsn">
            <xsl:value-of select="//cat:bsnhashes/*[local-name()=$bsnwithprefix]" />
        </xsl:variable>
        <xsl:value-of select="$hashedbsn" />
    </xsl:template>

    <xsl:template name="get-name-abbr">
        <xsl:variable name="n" select="local-name(.)"/>
        <xsl:choose>
            <!-- slecht idee, zie hieronder:-->
<!--            <xsl:when test="$n ='volledigeNaam'">naamPers</xsl:when>-->
            <xsl:when test="$n ='natuurlijkPersoon'">natPers</xsl:when>
            <xsl:when test="$n ='buitenlandseVennootschap'">buitenlVenn</xsl:when>
            <xsl:when test="$n ='eenmanszaakMetMeerdereEigenaren'">eenmZMeerEig</xsl:when>
            <xsl:when test="$n ='rechtspersoon'">rechtspersoon</xsl:when>
            <xsl:when test="$n ='rechtspersoonInOprichting'">rechtspersOpr</xsl:when>
            <xsl:when test="$n ='samenwerkingsverband'">samenwvb</xsl:when>
            <xsl:when test="$n ='maatschappelijkeActiviteit'">maatschAct</xsl:when>
            <xsl:when test="$n ='onderneming'">onderneming</xsl:when>
            <xsl:when test="$n ='commercieleVestiging'">comVestg</xsl:when>
            <xsl:when test="$n ='nietCommercieleVestiging'">nietComVestg</xsl:when>
            <!--<xsl:when test="$n ='binnenlandsAdres'">bnnldsAdres</xsl:when>-->
            <!--<xsl:when test="$n ='deponeringen'">deponering</xsl:when>-->
            <xsl:otherwise><xsl:value-of select="$n"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[cat:kvkNummer]" mode="object_ref">
        <xsl:text>nhr.</xsl:text><xsl:call-template name="get-name-abbr"/><xsl:text>.kvk.</xsl:text>
        <xsl:value-of select="cat:kvkNummer"/>
    </xsl:template>

    <xsl:template match="*[cat:vestigingsnummer]" mode="object_ref">
        <xsl:text>nhr.</xsl:text><xsl:call-template name="get-name-abbr"/><xsl:text>.</xsl:text>
        <xsl:value-of select="cat:vestigingsnummer"/>
    </xsl:template>

    <xsl:template match="*[cat:bsn or cat:rsin or cat:volledigeNaam]" mode="object_ref">
        <xsl:text>nhr.</xsl:text><xsl:call-template name="get-name-abbr"/><xsl:text>.</xsl:text>
        <xsl:choose>
            <xsl:when test="cat:rsin">
                <xsl:text>rsin.</xsl:text><xsl:value-of select="cat:rsin"/>
            </xsl:when>
            <xsl:when test="cat:bsn">
                <xsl:variable name="hashsoort" select="'bsn.'"/>

                <xsl:value-of select="$hashsoort"/>
                <xsl:call-template name="getHash">
                    <xsl:with-param name="bsn" select="cat:bsn" />
                    <xsl:with-param name="hashsoort" select="$hashsoort" />
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="cat:volledigeNaam">
                <!-- verwijder ongeldige chars zoals spatie, apostrof, -->
                <xsl:variable name="APOS">'</xsl:variable>
                <xsl:variable name="lookfor" select="translate(translate(cat:volledigeNaam,' ',''),$APOS,'')"/>
                <xsl:variable name="hashsoort" select="'naam.'"/>

                <xsl:value-of select="$hashsoort"/>
                <xsl:call-template name="getHash">
                    <xsl:with-param name="bsn" select="$lookfor" />
                    <xsl:with-param name="hashsoort" select="$hashsoort" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise />
        </xsl:choose>
<!--        <xsl:if test="cat:bsn">-->
<!--            <xsl:text>bsn.</xsl:text>-->
<!--            <xsl:call-template name="getHash">-->
<!--                <xsl:with-param name="bsn" select="cat:bsn" />-->
<!--            </xsl:call-template>-->
<!--        </xsl:if>-->
<!--        <xsl:if test="cat:rsin">-->
<!--            <xsl:text>rsin.</xsl:text><xsl:value-of select="cat:rsin"/>-->
<!--        </xsl:if>-->
    </xsl:template>

    <!-- slecht idee, we will geen eigennaam gebruiken als sleutel, zie bijvoorbeeld testbestand -->
<!--    <xsl:template match="*[cat:natuurlijkPersoon/cat:volledigeNaam]" mode="object_ref">-->
<!--        <xsl:text>nhr.</xsl:text>-->
<!--        <xsl:call-template name="get-name-abbr"/>-->
<!--        <xsl:text>.</xsl:text>-->
<!--        <xsl:text>naam.</xsl:text>-->
<!--        <xsl:text>nhr.naam.natPers</xsl:text>-->
<!--        <xsl:value-of select="translate(cat:natuurlijkPersoon/cat:volledigeNaam,' ','')"/>-->
<!--    </xsl:template>-->


    <!--<xsl:template match="*[cat:rechtspersoon/cat:bsn or cat:rechtspersoon/cat:rsin]" mode="object_ref">-->
        <!--<xsl:text>nhr.</xsl:text><xsl:call-template name="get-name-abbr"/><xsl:text>.</xsl:text>-->
        <!--<xsl:if test="cat:rechtspersoon/cat:bsn">-->
            <!--<xsl:text>bsn.</xsl:text><xsl:value-of select="cat:rechtspersoon/cat:bsn"/>-->
        <!--</xsl:if>-->
        <!--<xsl:if test="cat:rechtspersoon/cat:rsin">-->
            <!--<xsl:text>rsin.</xsl:text><xsl:value-of select="cat:rechtspersoon/cat:rsin"/>-->
        <!--</xsl:if>-->
    <!--</xsl:template>-->



    <!-- lege template voor gevallen zonder kvkNummer, vestigingsnummer, bsn of rsin bij heeftAlsEigenaar -->
    <xsl:template match="*" mode="object_ref" />
</xsl:stylesheet>
