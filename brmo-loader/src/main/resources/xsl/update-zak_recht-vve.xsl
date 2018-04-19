<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:snp="http://www.kadaster.nl/schemas/brk-levering/snapshot/v20120901"
                xmlns:ko="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-kadastraalobject/v20120701"
                xmlns:typ="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-typen/v20120201"
                xmlns:nen="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-NEN3610-2011/v20120201"
                xmlns:recht="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-recht/v20120201"
                xmlns:pers="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-persoon/v20120201"
                xmlns:NhrRechtspersoonRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-nhr-rechtspersoon-ref/v20120201"
                xmlns:PersoonRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-persoon-ref/v20120201">
    <!-- vul rechthebbende VVE opnieuw in zak_recht tabel, zie: https://github.com/B3Partners/brmo/issues/430 -->
    <xsl:variable name="kad_oz_id"
                  select="/snp:KadastraalObjectSnapshot/ko:Perceel/ko:identificatie/nen:lokaalId | /snp:KadastraalObjectSnapshot/ko:Appartementsrecht/ko:identificatie/nen:lokaalId"/>
    <xsl:template match="/">
        <root>
            <data>
                <xsl:for-each select="/snp:KadastraalObjectSnapshot/recht:ZakelijkRecht">
                    <!-- alleen de 'betrokkenBij' VVE rechten bijwerken, andere raken we niet aan -->
                    <xsl:if test="*[local-name() = 'betrokkenBij']//recht:verenigingVanEigenaren">

                        <xsl:variable name="parent-id">
                            <xsl:call-template name="nen_identificatie">
                                <xsl:with-param name="id" select="recht:identificatie"/>
                            </xsl:call-template>
                        </xsl:variable>

                        <zak_recht>
                            <kadaster_identif>
                                <xsl:value-of select="$parent-id"/>
                            </kadaster_identif>
                            <xsl:call-template name="zakelijk_recht">
                                <xsl:with-param name="zr" select="."/>
                            </xsl:call-template>
                            <xsl:for-each select="*[local-name() = 'betrokkenBij']//recht:verenigingVanEigenaren">
                                <xsl:for-each
                                        select="PersoonRef:KADNietNatuurlijkPersoonRef | NhrRechtspersoonRef:RechtspersoonRef">
                                    <xsl:variable name="id" select="substring(@xlink:href,2)"/>
                                    <fk_8pes_sc_identif>
                                        <xsl:call-template name="nen_identificatie">
                                            <xsl:with-param name="id" select="//*[@id = $id]/pers:identificatie"/>
                                        </xsl:call-template>
                                    </fk_8pes_sc_identif>
                                </xsl:for-each>
                            </xsl:for-each>
                        </zak_recht>

                    </xsl:if>
                </xsl:for-each>
            </data>
        </root>
    </xsl:template>

    <xsl:template name="zakelijk_recht">
        <xsl:param name="zr"/>
        <fk_7koz_kad_identif>
            <xsl:value-of select="$kad_oz_id"/>
        </fk_7koz_kad_identif>
        <fk_3avr_aand>
            <xsl:value-of select="$zr/recht:aard/typ:code"/>
        </fk_3avr_aand>
        <indic_betrokken_in_splitsing>
            <xsl:choose>
                <xsl:when test="$zr/recht/betrokkenBij">Ja</xsl:when>
                <xsl:otherwise>Nee</xsl:otherwise>
            </xsl:choose>
        </indic_betrokken_in_splitsing>
    </xsl:template>

    <xsl:template name="nen_identificatie">
        <xsl:param name="id"/>
        <xsl:value-of select="$id/nen:namespace"/>.<xsl:value-of select="$id/nen:lokaalId"/>
    </xsl:template>
</xsl:stylesheet>