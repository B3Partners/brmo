<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
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
                xmlns:bagadres="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-bag-adres/v20120201"
                xmlns:adres="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-adres/v20120201"
                xmlns:fn="http://www.w3.org/2005/xpath-functions">

    <xsl:variable name="toestandsdatum" select="/snp:KadastraalObjectSnapshot/snp:toestandsdatum"/>

    <xsl:template match="/">
        <root>
            <data>
                <xsl:for-each select="/snp:KadastraalObjectSnapshot/ko:Perceel | /snp:KadastraalObjectSnapshot/ko:Appartementsrecht">
                    <kad_onrrnd_zk column-dat-beg-geldh="dat_beg_geldh" column-datum-einde-geldh="datum_einde_geldh">
                        <dat_beg_geldh>
                            <xsl:value-of select="$toestandsdatum"/>
                        </dat_beg_geldh>
                        <datum_einde_geldh></datum_einde_geldh>
                        <kad_identif>
                            <xsl:value-of select="./ko:identificatie/nen:lokaalId"/>
                        </kad_identif>
                        <clazz>
                            <xsl:choose>
                                <xsl:when test="local-name(.) = 'Perceel'">KADASTRAAL PERCEEL</xsl:when>
                                <xsl:otherwise>APPARTEMENTSRECHT</xsl:otherwise>
                            </xsl:choose>
                        </clazz>
                        <ks_bedrag>
                            <xsl:value-of select="./ko:koopsom/ko:bedrag/typ:som"/>
                        </ks_bedrag>
                        <ks_valutasoort>
                            <xsl:value-of select="./ko:koopsom/ko:bedrag/typ:valuta/typ:waarde"/>
                        </ks_valutasoort>
                        <xsl:if test="./ko:koopsom/ko:koopjaar">
                            <ks_koopjaar>
                                <xsl:value-of select="./ko:koopsom/ko:koopjaar"/>
                            </ks_koopjaar>
                        </xsl:if>
                        <ks_meer_onroerendgoed>
                            <xsl:if test="./ko:koopsom/ko:indicatieMeerObjecten">J</xsl:if>
                            <xsl:if test="not(./ko:koopsom/ko:indicatieMeerObjecten)">N</xsl:if>
                        </ks_meer_onroerendgoed>

                        <cu_aard_bebouwing>
                            <xsl:value-of select="./ko:heeftLocatie/ko:LocatieKadastraalObject/ko:cultuurBebouwd/ko:code"/>
                        </cu_aard_bebouwing>
                        <cu_aard_cultuur_onbebouwd>
                            <xsl:value-of select="./ko:aardCultuurOnbebouwd/typ:waarde"/>
                        </cu_aard_cultuur_onbebouwd>

                        <!-- construeer extra adres wanneer geen BAG koppeling; 
                            het is mogelijk dat er zowel een bagadres als 1 of meer gba en/of imkad adressen in een bericht voorkomen; in
                            dat geval vullen we 'lo_loc__omschr' met het eerste niet-bagadres dat we tegenkomen en wordt daar aangeplakt 
                            dat er "x meer adressen" zijn, het bagadres wordt dan ook verwerkt natuurlijk.
                        -->
                        <lo_loc__omschr>
                            <xsl:variable name="countadressen" select="count(./ko:heeftLocatie/ko:LocatieKadastraalObject/ko:adres[not(bagadres:*)])" />
                            <!-- <xsl:for-each select="./ko:heeftLocatie/ko:LocatieKadastraalObject/ko:adres/adres:KADBuitenlandsAdres/..|
                                                ./ko:heeftLocatie/ko:LocatieKadastraalObject/ko:adres/adres:KADBinnenlandsAdres/..|
                                                ./ko:heeftLocatie/ko:LocatieKadastraalObject/ko:adres/adres:PostbusAdres/..|
                                                ./ko:heeftLocatie/ko:LocatieKadastraalObject/ko:adres/gba:BuitenlandsAdres/.."
                            > -->
                            <xsl:for-each select="./ko:heeftLocatie/ko:LocatieKadastraalObject/ko:adres[not(bagadres:*)]">
                                <xsl:variable name="count" select="position()"/>
                                <xsl:choose>
                                    <xsl:when test="($count = $countadressen) and ($countadressen > 1)">
                                        <xsl:text>  (</xsl:text>
                                        <xsl:value-of select="$count - 1"/>
                                        <xsl:text> meer adressen)</xsl:text>
                                    </xsl:when>
                                    <xsl:when test="$count = 1">
                                        <xsl:call-template name="describe-locatie"/>
                                    </xsl:when>
                                </xsl:choose>
                            </xsl:for-each>
                        </lo_loc__omschr>
                    </kad_onrrnd_zk>
                </xsl:for-each>
            </data>
        </root>
    </xsl:template>
    <!-- 
    kopie 'describe-locatie' template uit brk-snapshot-to-rsgb-xml.xsl
    -->
    <xsl:template name="describe-locatie">
        <xsl:for-each select="adres:PostbusAdres">
            Postbus <xsl:value-of select="adres:postbusnummer"/>, <xsl:value-of select="adres:postcode"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="adres:woonplaatsNaam"/>
        </xsl:for-each>

        <xsl:for-each select="adres:KADBinnenlandsAdres">
            <xsl:value-of select="adres:openbareRuimteNaam"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="adres:huisNummer"/>
            <xsl:for-each select="adres:huisNummerToevoeging">
                <xsl:text> </xsl:text>
                <xsl:value-of select="."/>
            </xsl:for-each>
            <xsl:for-each select="adres:huisLetter">
                <xsl:text> </xsl:text>
                <xsl:value-of select="."/>
            </xsl:for-each>, <xsl:value-of select="adres:postcode"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="adres:woonplaatsNaam"/>
        </xsl:for-each>
        <xsl:for-each select="bagadres:Ligplaats | bagadres:Standplaats | bagadres:Verlijfsobject">
            BAG ID: <xsl:value-of select="bagadres:BAGIdentificatie"/>
        </xsl:for-each>
        <xsl:for-each select="gba:BuitenlandsAdres">
            <xsl:value-of select="gba:adres"/>, <xsl:value-of select="gba:woonplaats"/>
            <xsl:for-each select="gba:regio">, <xsl:value-of select="."/></xsl:for-each>, <xsl:value-of select="gba:land/typ:waarde"/>
        </xsl:for-each>
        <xsl:for-each select="adres:KADBuitenlandsAdres">
            <xsl:value-of select="adres:adres"/>, <xsl:value-of select="adres:woonplaats"/>
            <xsl:for-each select="adres:regio">, <xsl:value-of select="adres:regio"/></xsl:for-each>, <xsl:value-of select="adres:land"/>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
