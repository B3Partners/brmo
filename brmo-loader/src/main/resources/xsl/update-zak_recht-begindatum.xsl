<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:snp="http://www.kadaster.nl/schemas/brk-levering/snapshot/v20120901"
                xmlns:ko="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-kadastraalobject/v20120701"
                xmlns:typ="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-typen/v20120201"
                xmlns:nen="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-NEN3610-2011/v20120201"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:recht="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-recht/v20120201"
                xmlns:rechtref="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-recht-ref/v20120201"
                xmlns:pers="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-persoon/v20120201"
                xmlns:nhr="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-nhr-rechtspersoon/v20120201"
                xmlns:gba="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-gba-persoon/v20120901"
                xmlns:GbaPersoonRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-gba-persoon-ref/v20120201"
                xmlns:Stuk="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-stuk/v20120201"
                xmlns:StukRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-stuk-ref/v20120201" 
                xmlns:bagadres="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-bag-adres/v20120201"
                xmlns:fn="http://www.w3.org/2005/xpath-functions">
    <!-- parameters van het bericht -->
    <xsl:param name="objectRef" select="'NL.KAD.OnroerendeZaak:onbekend'"/>
    <xsl:param name="datum" select="'datum-onbekend'"/>
    <xsl:param name="volgordeNummer" select="'volgordeNummer-onbekend'"/>
    <xsl:param name="soort" select="'soort-onbekend'"/>
    <xsl:variable name="kad_oz_id" select="/snp:KadastraalObjectSnapshot/ko:Perceel/ko:identificatie/nen:lokaalId | /snp:KadastraalObjectSnapshot/ko:Appartementsrecht/ko:identificatie/nen:lokaalId"/>
    <xsl:variable name="toestandsdatum" select="/snp:KadastraalObjectSnapshot/snp:toestandsdatum"/>
    <xsl:variable name="persoonId"/>
    <xsl:template match="/">
        <root>
            <xsl:comment>
                <xsl:text>objectRef: </xsl:text>
                <xsl:value-of select="$objectRef"/>
                <xsl:text>, datum: </xsl:text>
                <xsl:value-of select="$datum"/>
                <xsl:text>, volgordeNummer: </xsl:text>
                <xsl:value-of select="$volgordeNummer"/>
                <xsl:text>, soort: </xsl:text>
                <xsl:value-of select="$soort"/>
            </xsl:comment>
            <data>
                <xsl:for-each select="/snp:KadastraalObjectSnapshot/recht:Tenaamstelling">
                    <xsl:apply-templates select="."/>
                </xsl:for-each>
                <xsl:for-each select="/snp:KadastraalObjectSnapshot/recht:ZakelijkRecht">
                    <xsl:variable name="parent-id">
                        <xsl:call-template name="nen_identificatie">
                            <xsl:with-param name="id" select="recht:identificatie"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <zak_recht column-dat-beg-geldh="ingangsdatum_recht" column-datum-einde-geldh="eindd_recht">
                        <kadaster_identif>
                            <xsl:value-of select="$parent-id"/>
                        </kadaster_identif>
                        <eindd_recht alleen-archief="true"></eindd_recht>
                        <xsl:call-template name="zakelijk_recht">
                            <xsl:with-param name="zr" select="."/>
                        </xsl:call-template>
                        <!-- "fk_8pes_sc_identif" ("heeft als gerechtigde") kan maar één keer gevuld worden; het kan zijn dat er èn een ontstaanUit
                        èn een betrokkenBij record is. Alleen de "betrokkenBij" VVE wordt in deze kolom ingevuld. Wel wordt er een brondocument
                        voor het ontstaanUit element gemaakt  -->
                        <xsl:for-each select="*[local-name() = 'betrokkenBij']//recht:verenigingVanEigenaren">
                            <xsl:for-each select="PersoonRef:KADNietNatuurlijkPersoonRef | NhrRechtspersoonRef:RechtspersoonRef"
                                          xmlns:NhrRechtspersoonRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-nhr-rechtspersoon-ref/v20120201"
                                          xmlns:PersoonRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-persoon-ref/v20120201">
                                <xsl:variable name="id" select="substring(@xlink:href,2)"/>
                                <fk_8pes_sc_identif>
                                    <xsl:call-template name="nen_identificatie">
                                        <xsl:with-param name="id" select="//*[@id = $id]/pers:identificatie"/>
                                    </xsl:call-template>
                                </fk_8pes_sc_identif>
                            </xsl:for-each>
                        </xsl:for-each>
                        <ingangsdatum_recht>
                            <xsl:value-of select="$toestandsdatum"/>
                        </ingangsdatum_recht>
                    </zak_recht>
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

    <xsl:template match="/snp:KadastraalObjectSnapshot/recht:Tenaamstelling">
        <xsl:variable name="zakRechtId" select="substring(recht:van/rechtref:ZakelijkRechtRef/@*[local-name() = 'href'],2)"/>

        <xsl:variable name="parent-id">
            <xsl:call-template name="nen_identificatie">
                <xsl:with-param name="id" select="recht:identificatie"/>
            </xsl:call-template>
        </xsl:variable>
        <zak_recht column-dat-beg-geldh="ingangsdatum_recht" column-datum-einde-geldh="eindd_recht">
            <kadaster_identif>
                <xsl:value-of select="$parent-id"/>
            </kadaster_identif>
            <eindd_recht alleen-archief="true"></eindd_recht>
            <xsl:choose>
                <xsl:when test="recht:aandeel">
                    <ar_teller>
                        <xsl:value-of select="recht:aandeel/recht:teller"/>
                    </ar_teller>
                    <ar_noemer>
                        <xsl:value-of select="recht:aandeel/recht:noemer"/>
                    </ar_noemer>
                </xsl:when>
                <xsl:when test="recht:geldtVoor/recht:GezamenlijkAandeel/recht:aandeel">
                    <ar_teller>
                        <xsl:value-of select="recht:geldtVoor/recht:GezamenlijkAandeel/recht:aandeel/recht:teller"/>
                    </ar_teller>
                    <ar_noemer>
                        <xsl:value-of select="recht:geldtVoor/recht:GezamenlijkAandeel/recht:aandeel/recht:noemer"/>
                    </ar_noemer>
                </xsl:when>
            </xsl:choose>
            <xsl:call-template name="zakelijk_recht">
                <xsl:with-param name="zr" select="/snp:KadastraalObjectSnapshot/recht:ZakelijkRecht[@id = $zakRechtId]"/>
            </xsl:call-template>

            <xsl:for-each select="recht:vanPersoon">
                <xsl:for-each select="PersoonRef:KADNatuurlijkPersoonRef | GbaPersoonRef:IngezeteneRef | GbaPersoonRef:NietIngezeteneRef | PersoonRef:KADNietNatuurlijkPersoonRef | NhrRechtspersoonRef:RechtspersoonRef" xmlns:NhrRechtspersoonRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-nhr-rechtspersoon-ref/v20120201"  xmlns:PersoonRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-persoon-ref/v20120201">
                    <xsl:variable name="id" select="substring(@xlink:href,2)"/>
                    <fk_8pes_sc_identif>
                        <xsl:call-template name="nen_identificatie">
                            <xsl:with-param name="id" select="//*[@id = $id]/pers:identificatie"/>
                        </xsl:call-template>
                    </fk_8pes_sc_identif>
                </xsl:for-each>
            </xsl:for-each>
            <xsl:for-each select="recht:isGebaseerdOp">
                <xsl:for-each select="StukRef:StukdeelRef">
                    <xsl:variable name="id" select="substring(@xlink:href,2)"/>
                    <xsl:choose>
                        <xsl:when test="//Stuk:TerInschrijvingAangebodenStuk/Stuk:omvat/Stuk:Stukdeel[@id=$id]/../../Stuk:tijdstipAanbieding">
                            <ingangsdatum_recht>
                                <xsl:value-of select="substring(//Stuk:TerInschrijvingAangebodenStuk/Stuk:omvat/Stuk:Stukdeel[@id=$id]/../../Stuk:tijdstipAanbieding,0,11)"/>
                            </ingangsdatum_recht>
                        </xsl:when>
                        <xsl:otherwise>
                            <ingangsdatum_recht>
                                <xsl:value-of select="$toestandsdatum"/>
                            </ingangsdatum_recht>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:for-each>
        </zak_recht>
    </xsl:template>

    <xsl:template name="nen_identificatie">
        <xsl:param name="id"/>
        <xsl:value-of select="$id/nen:namespace"/>.<xsl:value-of select="$id/nen:lokaalId"/>
    </xsl:template>

</xsl:stylesheet>
