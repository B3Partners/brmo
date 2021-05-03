<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:DatatypenNEN3610="www.kadaster.nl/schemas/lvbag/imbag/datatypennen3610/v20180601"
                xmlns:Objecten="www.kadaster.nl/schemas/lvbag/imbag/objecten/v20180601"
                xmlns:Historie="www.kadaster.nl/schemas/lvbag/imbag/historie/v20180601"
                xmlns:Objecten-ref="www.kadaster.nl/schemas/lvbag/imbag/objecten-ref/v20180601"
                xmlns:nen5825="www.kadaster.nl/schemas/lvbag/imbag/nen5825/v20180601"
                xmlns:sl-bag-extract="http://www.kadaster.nl/schemas/lvbag/extract-deelbestand-lvc/v20180601"
                xmlns:mlm="http://www.kadaster.nl/schemas/lvbag/extract-deelbestand-mutaties-lvc/v20180601"
                xmlns:sl="http://www.kadaster.nl/schemas/standlevering-generiek/1.0"
                xmlns:ml="http://www.kadaster.nl/schemas/mutatielevering-generiek/1.0"
                xsi:schemaLocation="http://www.kadaster.nl/schemas/lvbag/extract-deelbestand-mutaties-lvc/v20180601 http://www.kadaster.nl/schemas/lvbag/extract-deelbestand-mutaties-lvc/v20180601/BagvsExtractDeelbestandMutatieLvc-2.0.xsd">
    <xsl:output method="xml" indent="yes"/>
    <!-- parameters van het bericht -->
    <xsl:param name="objectRef"/>
    <xsl:param name="datum"/>
    <xsl:param name="volgordeNummer"/>
    <xsl:param name="soort"/>
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
                <xsl:apply-templates select="*"/>
            </data>
        </root>
    </xsl:template>
    <xsl:template match="sl-bag-extract:bagInfo"><!-- ignore --></xsl:template>
    <xsl:template match="sl:standBestand/sl:dataset"><!-- ignore --></xsl:template>
    <xsl:template match="sl:standBestand/sl:inhoud"><!-- ignore --></xsl:template>
    <xsl:template match="mlm:bagInfo"><!-- ignore --></xsl:template>
    <xsl:template match="ml:mutatieBericht/ml:dataset"><!-- ignore --></xsl:template>
    <xsl:template match="ml:mutatieBericht/ml:inhoud"><!-- ignore --></xsl:template>

    <!-- stand -->
    <xsl:template match="sl:standBestand/sl:stand">
        <xsl:for-each select="sl-bag-extract:bagObject">
            <xsl:apply-templates select="."/>
        </xsl:for-each>
    </xsl:template>
    <!-- mutatie -->
    <xsl:template match="ml:mutatieGroep/ml:toevoeging|ml:mutatieGroep/ml:wijziging|ml:mutatieGroep/ml:verwijdering">
        <xsl:comment>
            <xsl:value-of select="local-name()"/>
        </xsl:comment>
        <xsl:for-each select="ml:wordt">
            <xsl:comment>
                <xsl:value-of select="local-name(./mlm:bagObject/*)"/>
            </xsl:comment>
            <xsl:apply-templates select="."/>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="Objecten:Pand">
        <!-- in BAG 1.0 was er <xsl:if test="bag:aanduidingRecordInactief = 'N'">
        in BAG 2.0 is dat vervangen door een DateTime (Objecten:voorkomen/Historie:Voorkomen/Historie:tijdstipInactief)
        met daarin moment van besluit inactief maken, als dat niet aanwezig is dan is object dus actief.
        zie ook: https://zakelijk.kadaster.nl/transformatieregels
        -->
        <xsl:if test="not(Objecten:voorkomen/Historie:Voorkomen/Historie:tijdstipInactief)">
            <pand column-dat-beg-geldh="dat_beg_geldh" column-datum-einde-geldh="datum_einde_geldh">
                <identif>
                    <xsl:value-of select="Objecten:identificatie/DatatypenNEN3610:lokaalID"/>
                </identif>
                <status>
                    <xsl:value-of select="Objecten:status"/>
                </status>
                <dat_beg_geldh>
                    <xsl:value-of
                            select="Objecten:voorkomen/Historie:Voorkomen/tijdvakgeldigheid/Historie:beginGeldigheid"/>
                </dat_beg_geldh>
                <datum_einde_geldh>
                    <xsl:value-of
                            select="Objecten:voorkomen/Historie:Voorkomen/tijdvakgeldigheid/Historie:eindGeldigheid"/>
                </datum_einde_geldh>
                <indic_geconstateerd>
                    <xsl:value-of select="Objecten:geconstateerd"/>
                </indic_geconstateerd>
                <geom_bovenaanzicht>
                    <xsl:copy-of select="Objecten:geometrie/gml:Polygon"/>
                </geom_bovenaanzicht>
                <oorspronkelijk_bouwjaar>
                    <xsl:value-of select="Objecten:oorspronkelijkBouwjaar"/>
                </oorspronkelijk_bouwjaar>
            </pand>

            <brondocument ignore-duplicates="yes">
                <tabel>
                    <xsl:value-of select="'pand'"/>
                </tabel>
                <tabel_identificatie>
                    <xsl:value-of select="Objecten:identificatie/DatatypenNEN3610:lokaalID"/>
                </tabel_identificatie>
                <identificatie>
                    <xsl:value-of select="Objecten:documentnummer"/>
                </identificatie>
                <datum>
                    <xsl:value-of select="Objecten:documentdatum"/>
                </datum>
            </brondocument>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Objecten:Standplaats">
        <xsl:if test="not(Objecten:voorkomen/Historie:Voorkomen/Historie:tijdstipInactief)">
            <xsl:variable name="bagid">
                <xsl:value-of select="Objecten:identificatie/DatatypenNEN3610:lokaalID"/>
            </xsl:variable>
            <xsl:variable name="begindate">
                <xsl:value-of
                        select="Objecten:voorkomen/Historie:Voorkomen/tijdvakgeldigheid/Historie:beginGeldigheid"/>
            </xsl:variable>

            <benoemd_obj>
                <identif>
                    <xsl:value-of select="$bagid"/>
                </identif>
                <clazz>STANDPLAATS</clazz>
            </benoemd_obj>

            <benoemd_terrein column-dat-beg-geldh="dat_beg_geldh" column-datum-einde-geldh="datum_einde_geldh">
                <dat_beg_geldh>
                    <xsl:value-of select="$begindate"/>
                </dat_beg_geldh>
                <sc_identif>
                    <xsl:value-of select="$bagid"/>
                </sc_identif>
                <clazz>STANDPLAATS</clazz>
                <geom>
                    <xsl:copy-of select="Objecten:geometrie/gml:Polygon"/>
                </geom>
                <datum_einde_geldh>
                    <xsl:value-of
                            select="Objecten:voorkomen/Historie:Voorkomen/tijdvakgeldigheid/Historie:eindGeldigheid"/>
                </datum_einde_geldh>
            </benoemd_terrein>

            <standplaats column-dat-beg-geldh="sc_dat_beg_geldh">
                <sc_dat_beg_geldh alleen-archief="true">
                    <xsl:value-of select="$begindate"/>
                </sc_dat_beg_geldh>
                <sc_identif>
                    <xsl:value-of select="$bagid"/>
                </sc_identif>
                <indic_geconst>
                    <xsl:value-of select="Objecten:geconstateerd"/>
                </indic_geconst>
                <status>
                    <xsl:value-of select="Objecten:status"/>
                </status>
                <fk_4nra_sc_identif>
                    <xsl:for-each select="Objecten:heeftAlsHoofdadres/Objecten-ref:NummeraanduidingRef[@xlink:href]">
                        <xsl:call-template name="aanduiding-referentie">
                            <xsl:with-param name="input" select="./@xlink:href"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </fk_4nra_sc_identif>
            </standplaats>

            <xsl:for-each select="Objecten:heeftAlsNevenadres/Objecten-ref:NummeraanduidingRef[@xlink:href]">
                <ligplaats_nummeraand>
                    <fk_nn_lh_lpl_sc_identif>
                        <xsl:value-of select="$bagid"/>
                    </fk_nn_lh_lpl_sc_identif>
                    <fk_nn_lh_lpl_sc_dat_beg_geldh>
                        <xsl:value-of select="$begindate"/>
                    </fk_nn_lh_lpl_sc_dat_beg_geldh>
                    <fk_nn_rh_nra_sc_identif>
                        <xsl:call-template name="aanduiding-referentie">
                            <xsl:with-param name="input" select="./@xlink:href"/>
                        </xsl:call-template>
                    </fk_nn_rh_nra_sc_identif>
                </ligplaats_nummeraand>
            </xsl:for-each>

            <brondocument ignore-duplicates="yes">
                <tabel>
                    <xsl:value-of select="'standplaats'"/>
                </tabel>
                <tabel_identificatie>
                    <xsl:value-of select="$bagid"/>
                </tabel_identificatie>
                <identificatie>
                    <xsl:value-of select="Objecten:documentnummer"/>
                </identificatie>
                <datum>
                    <xsl:value-of select="Objecten:documentdatum"/>
                </datum>
            </brondocument>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Objecten:Ligplaats">
        <xsl:if test="not(Objecten:voorkomen/Historie:Voorkomen/Historie:tijdstipInactief)">
            <xsl:variable name="bagid">
                <xsl:value-of select="Objecten:identificatie/DatatypenNEN3610:lokaalID"/>
            </xsl:variable>
            <xsl:variable name="begindate">
                <xsl:value-of
                        select="Objecten:voorkomen/Historie:Voorkomen/tijdvakgeldigheid/Historie:beginGeldigheid"/>
            </xsl:variable>

            <benoemd_obj>
                <identif>
                    <xsl:value-of select="$bagid"/>
                </identif>
                <clazz>LIGPLAATS</clazz>
            </benoemd_obj>

            <benoemd_terrein column-dat-beg-geldh="dat_beg_geldh" column-datum-einde-geldh="datum_einde_geldh">
                <dat_beg_geldh>
                    <xsl:value-of select="$begindate"/>
                </dat_beg_geldh>
                <sc_identif>
                    <xsl:value-of select="$bagid"/>
                </sc_identif>
                <clazz>LIGPLAATS</clazz>
                <geom>
                    <xsl:copy-of select="Objecten:geometrie/gml:Polygon"/>
                </geom>
                <datum_einde_geldh>
                    <xsl:value-of
                            select="Objecten:voorkomen/Historie:Voorkomen/tijdvakgeldigheid/Historie:eindGeldigheid"/>
                </datum_einde_geldh>
            </benoemd_terrein>

            <ligplaats column-dat-beg-geldh="sc_dat_beg_geldh">
                <sc_dat_beg_geldh alleen-archief="true">
                    <xsl:value-of select="$begindate"/>
                </sc_dat_beg_geldh>
                <sc_identif>
                    <xsl:value-of select="$bagid"/>
                </sc_identif>
                <indic_geconst>
                    <xsl:value-of select="Objecten:geconstateerd"/>
                </indic_geconst>
                <status>
                    <xsl:value-of select="Objecten:status"/>
                </status>
                <fk_4nra_sc_identif>
                    <xsl:for-each select="Objecten:heeftAlsHoofdadres/Objecten-ref:NummeraanduidingRef[@xlink:href]">
                        <xsl:call-template name="aanduiding-referentie">
                            <xsl:with-param name="input" select="./@xlink:href"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </fk_4nra_sc_identif>
            </ligplaats>

            <xsl:for-each select="Objecten:heeftAlsNevenadres/Objecten-ref:NummeraanduidingRef[@xlink:href]">
                <ligplaats_nummeraand>
                    <fk_nn_lh_lpl_sc_identif>
                        <xsl:value-of select="$bagid"/>
                    </fk_nn_lh_lpl_sc_identif>
                    <fk_nn_lh_lpl_sc_dat_beg_geldh>
                        <xsl:value-of select="$begindate"/>
                    </fk_nn_lh_lpl_sc_dat_beg_geldh>
                    <fk_nn_rh_nra_sc_identif>
                        <xsl:call-template name="aanduiding-referentie">
                            <xsl:with-param name="input" select="./@xlink:href"/>
                        </xsl:call-template>
                    </fk_nn_rh_nra_sc_identif>
                </ligplaats_nummeraand>
            </xsl:for-each>

            <brondocument ignore-duplicates="yes">
                <tabel>
                    <xsl:value-of select="'ligplaats'"/>
                </tabel>
                <tabel_identificatie>
                    <xsl:value-of select="$bagid"/>
                </tabel_identificatie>
                <identificatie>
                    <xsl:value-of select="Objecten:documentnummer"/>
                </identificatie>
                <datum>
                    <xsl:value-of select="Objecten:documentdatum"/>
                </datum>
            </brondocument>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Objecten:Nummeraanduiding">
        <xsl:if test="not(Objecten:voorkomen/Historie:Voorkomen/Historie:tijdstipInactief)">
            <xsl:variable name="begindate">
                <xsl:value-of
                        select="Objecten:voorkomen/Historie:Voorkomen/tijdvakgeldigheid/Historie:beginGeldigheid"/>
            </xsl:variable>
            <xsl:variable name="bagid">
                <xsl:value-of select="Objecten:identificatie/DatatypenNEN3610:lokaalID"/>
            </xsl:variable>

            <addresseerb_obj_aand column-dat-beg-geldh="dat_beg_geldh" column-datum-einde-geldh="datum_einde_geldh">
                <dat_beg_geldh>
                    <xsl:value-of select="$begindate"/>
                </dat_beg_geldh>
                <identif>
                    <xsl:value-of select="$bagid"/>
                </identif>
                <clazz>NUMMERAANDUIDING</clazz>
                <dat_eind_geldh>
                    <xsl:value-of
                            select="Objecten:voorkomen/Historie:Voorkomen/tijdvakgeldigheid/Historie:eindGeldigheid"/>
                </dat_eind_geldh>
                <huisletter>
                    <xsl:value-of select="Objecten:huisletter"/>
                </huisletter>
                <huinummer>
                    <xsl:value-of select="Objecten:huisnummer"/>
                </huinummer>
                <huinummertoevoeging>
                    <xsl:value-of select="Objecten:huisnummertoevoeging"/>
                </huinummertoevoeging>
                <postcode>
                    <xsl:value-of select="Objecten:postcode"/>
                </postcode>
                <fk_6wpl_identif>
                    <xsl:for-each select="Objecten:ligtIn/Objecten-ref:WoonplaatsRef[@xlink:href]">
                        <xsl:call-template name="aanduiding-referentie">
                            <xsl:with-param name="input" select="./@xlink:href"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </fk_6wpl_identif>
                <fk_7opr_identifcode>
                    <xsl:for-each select="Objecten:ligtAan/Objecten-ref:OpenbareRuimteRef[@xlink:href]">
                        <xsl:call-template name="aanduiding-referentie">
                            <xsl:with-param name="input" select="./@xlink:href"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </fk_7opr_identifcode>
            </addresseerb_obj_aand>

            <nummeraand column-dat-beg-geldh="sc_dat_beg_geldh">
                <sc_dat_beg_geldh alleen-archief="true">
                    <xsl:value-of select="$begindate"/>
                </sc_dat_beg_geldh>
                <sc_identif>
                    <xsl:value-of select="$bagid"/>
                </sc_identif>
                <indic_geconst>
                    <xsl:value-of select="Objecten:geconstateerd"/>
                </indic_geconst>
                <indic_hoofdadres>
                    <xsl:value-of select="'?'"/>
                </indic_hoofdadres>
                <status>
                    <xsl:value-of select="Objecten:status"/>
                </status>
            </nummeraand>

            <brondocument ignore-duplicates="yes">
                <tabel>
                    <xsl:value-of select="'nummeraand'"/>
                </tabel>
                <tabel_identificatie>
                    <xsl:value-of select="$bagid"/>
                </tabel_identificatie>
                <identificatie>
                    <xsl:value-of select="Objecten:documentnummer"/>
                </identificatie>
                <datum>
                    <xsl:value-of select="Objecten:documentdatum"/>
                </datum>
            </brondocument>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Objecten:Woonplaats">
        <xsl:if test="not(Objecten:voorkomen/Historie:Voorkomen/Historie:tijdstipInactief)">
            <wnplts column-dat-beg-geldh="dat_beg_geldh" column-datum-einde-geldh="datum_einde_geldh">
                <dat_beg_geldh>
                    <xsl:value-of
                            select="Objecten:voorkomen/Historie:Voorkomen/tijdvakgeldigheid/Historie:beginGeldigheid"/>
                </dat_beg_geldh>
                <identif>
                    <xsl:value-of select="Objecten:identificatie/DatatypenNEN3610:lokaalID"/>
                </identif>
                <datum_einde_geldh>
                    <xsl:value-of
                            select="Objecten:voorkomen/Historie:Voorkomen/tijdvakgeldigheid/Historie:eindGeldigheid"/>
                </datum_einde_geldh>
                <indic_geconst>
                    <xsl:value-of select="Objecten:geconstateerd"/>
                </indic_geconst>
                <naam>
                    <xsl:value-of select="Objecten:naam"/>
                </naam>
                <geom>
                    <xsl:copy-of select="Objecten:geometrie/Objecten:vlak/gml:Polygon"/>
                </geom>
                <status>
                    <xsl:value-of select="Objecten:status"/>
                </status>
                <fk_7gem_code/>
                <!-- via 05_update_wnplts_gemcode.sql -->
            </wnplts>
            <brondocument ignore-duplicates="yes">
                <tabel>
                    <xsl:value-of select="'wnplts'"/>
                </tabel>
                <tabel_identificatie>
                    <xsl:value-of select="Objecten:identificatie/DatatypenNEN3610:lokaalID"/>
                </tabel_identificatie>
                <identificatie>
                    <xsl:value-of select="Objecten:documentnummer"/>
                </identificatie>
                <datum>
                    <xsl:value-of select="Objecten:documentdatum"/>
                </datum>
            </brondocument>
        </xsl:if>
    </xsl:template>


    <xsl:template match="Objecten:Verblijfsobject">
        <xsl:if test="not(Objecten:voorkomen/Historie:Voorkomen/Historie:tijdstipInactief)">
            <xsl:variable name="bagid">
                <xsl:value-of select="Objecten:identificatie/DatatypenNEN3610:lokaalID"/>
            </xsl:variable>
            <xsl:variable name="begindate">
                <xsl:value-of
                        select="Objecten:voorkomen/Historie:Voorkomen/tijdvakgeldigheid/Historie:beginGeldigheid"/>
            </xsl:variable>

            <benoemd_obj>
                <identif>
                    <xsl:value-of select="$bagid"/>
                </identif>
                <clazz>VERBLIJFSOBJECT</clazz>
            </benoemd_obj>

            <gebouwd_obj column-dat-beg-geldh="dat_beg_geldh" column-datum-einde-geldh="datum_einde_geldh">
                <dat_beg_geldh>
                    <xsl:value-of select="$begindate"/>
                </dat_beg_geldh>
                <sc_identif>
                    <xsl:value-of select="bagid"/>
                </sc_identif>
                <clazz>VERBLIJFSOBJECT</clazz>
                <datum_einde_geldh>
                    <xsl:value-of
                            select="Objecten:voorkomen/Historie:Voorkomen/tijdvakgeldigheid/Historie:eindGeldigheid"/>
                </datum_einde_geldh>
                <oppervlakte_obj>
                    <xsl:value-of select="Objecten:gebruiksdoel"/>
                </oppervlakte_obj>
                <puntgeom>
                    <xsl:copy-of select="Objecten:geometrie/Objecten:punt/gml:Point"/>
                </puntgeom>
            </gebouwd_obj>
            <verblijfsobj column-dat-beg-geldh="sc_dat_beg_geldh">
                <sc_dat_beg_geldh alleen-archief="true">
                    <xsl:value-of select="$begindate"/>
                </sc_dat_beg_geldh>
                <sc_identif>
                    <xsl:value-of select="$bagid"/>
                </sc_identif>
                <indic_geconstateerd>
                    <xsl:value-of select=" Objecten:geconstateerd"/>
                </indic_geconstateerd>
                <status>
                    <xsl:value-of select="Objecten:status"/>
                </status>
                <fk_11nra_sc_identif>
                    <xsl:for-each select="Objecten:heeftAlsHoofdadres/Objecten-ref:NummeraanduidingRef[@xlink:href]">
                        <xsl:call-template name="aanduiding-referentie">
                            <xsl:with-param name="input" select="./@xlink:href"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </fk_11nra_sc_identif>
            </verblijfsobj>

            <xsl:for-each select="Objecten:gebruiksdoel">
                <gebouwd_obj_gebruiksdoel>
                    <gebruiksdoel_gebouwd_obj>
                        <xsl:value-of select="."/>
                    </gebruiksdoel_gebouwd_obj>
                    <fk_gbo_sc_identif>
                        <xsl:value-of select="$bagid"/>
                    </fk_gbo_sc_identif>
                </gebouwd_obj_gebruiksdoel>
            </xsl:for-each>

            <xsl:for-each select="Objecten:heeftAlsNevenadres/Objecten-ref:NummeraanduidingRef[@xlink:href]">
                <verblijfsobj_nummeraand>
                    <fk_nn_lh_vbo_sc_identif>
                        <xsl:value-of select="$bagid"/>
                    </fk_nn_lh_vbo_sc_identif>
                    <fk_nn_lh_vbo_sc_dat_beg_geldh>
                        <xsl:value-of select="$begindate"/>
                    </fk_nn_lh_vbo_sc_dat_beg_geldh>
                    <fk_nn_rh_nra_sc_identif>
                        <xsl:call-template name="aanduiding-referentie">
                            <xsl:with-param name="input" select="./@xlink:href"/>
                        </xsl:call-template>
                    </fk_nn_rh_nra_sc_identif>
                </verblijfsobj_nummeraand>
            </xsl:for-each>

            <xsl:for-each select="Objecten:maaktDeelUitVan/Objecten-ref:PandRef[@xlink:href]">
                <verblijfsobj_pand>
                    <fk_nn_lh_vbo_sc_identif>
                        <xsl:value-of select="$bagid"/>
                    </fk_nn_lh_vbo_sc_identif>
                    <fk_nn_lh_vbo_sc_dat_beg_geldh>
                        <xsl:value-of select="$begindate"/>
                    </fk_nn_lh_vbo_sc_dat_beg_geldh>
                    <fk_nn_rh_pnd_identif>
                        <xsl:call-template name="aanduiding-referentie">
                            <xsl:with-param name="input" select="./@xlink:href"/>
                        </xsl:call-template>
                    </fk_nn_rh_pnd_identif>
                </verblijfsobj_pand>
            </xsl:for-each>

            <brondocument ignore-duplicates="yes">
                <tabel>
                    <xsl:value-of select="'verblijfsobj'"/>
                </tabel>
                <tabel_identificatie>
                    <xsl:value-of select="$bagid"/>
                </tabel_identificatie>
                <identificatie>
                    <xsl:value-of select="Objecten:documentnummer"/>
                </identificatie>
                <datum>
                    <xsl:value-of select="Objecten:documentdatum"/>
                </datum>
            </brondocument>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Objecten:OpenbareRuimte">
        <xsl:if test="not(Objecten:voorkomen/Historie:Voorkomen/Historie:tijdstipInactief)">
            <xsl:variable name="bagid">
                <xsl:value-of select="Objecten:identificatie/DatatypenNEN3610:lokaalID"/>
            </xsl:variable>

            <xsl:variable name="begindate">
                <xsl:value-of
                        select="Objecten:voorkomen/Historie:Voorkomen/tijdvakgeldigheid/Historie:beginGeldigheid"/>
            </xsl:variable>

            <gem_openb_rmte column-dat-beg-geldh="dat_beg_geldh" column-datum-einde-geldh="datum_einde_geldh">
                <dat_beg_geldh>
                    <xsl:value-of select="$begindate"/>
                </dat_beg_geldh>
                <identifcode>
                    <xsl:value-of select="$bagid"/>
                </identifcode>
                <datum_einde_geldh>
                    <xsl:value-of
                            select="Objecten:voorkomen/Historie:Voorkomen/tijdvakgeldigheid/Historie:eindGeldigheid"/>
                </datum_einde_geldh>
                <indic_geconst_openb_rmte>
                    <xsl:value-of select="Objecten:geconstateerd"/>
                </indic_geconst_openb_rmte>
                <naam_openb_rmte>
                    <xsl:value-of select="Objecten:naam"/>
                </naam_openb_rmte>
                <status_openb_rmte>
                    <xsl:value-of select="Objecten:status"/>
                </status_openb_rmte>
                <straatnaam>
                    <xsl:value-of select="nen5825:VerkorteOpenbareruimteNaam"/>
                </straatnaam>
                <type_openb_rmte>
                    <xsl:value-of select="Objecten:type"/>
                </type_openb_rmte>
            </gem_openb_rmte>

            <openb_rmte>
                <identifcode>
                    <xsl:value-of select="$bagid"/>
                </identifcode>
            </openb_rmte>

            <openb_rmte_wnplts>
                <fk_nn_lh_opr_identifcode>
                    <xsl:value-of select="$bagid"/>
                </fk_nn_lh_opr_identifcode>
                <fk_nn_rh_wpl_identif>
                    <xsl:for-each select="Objecten:ligtIn/Objecten-ref:WoonplaatsRef[@xlink:href]">
                        <xsl:call-template name="aanduiding-referentie">
                            <xsl:with-param name="input" select="./@xlink:href"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </fk_nn_rh_wpl_identif>
            </openb_rmte_wnplts>

            <openb_rmte_gem_openb_rmte>
                <fk_nn_lh_opr_identifcode>
                    <xsl:value-of select="$bagid"/>
                </fk_nn_lh_opr_identifcode>
                <fk_nn_rh_gor_identifcode>
                    <xsl:value-of select="$bagid"/>
                </fk_nn_rh_gor_identifcode>
            </openb_rmte_gem_openb_rmte>

            <brondocument ignore-duplicates="yes">
                <tabel>
                    <xsl:value-of select="'gem_openb_rmte'"/>
                </tabel>
                <tabel_identificatie>
                    <xsl:value-of select="$bagid"/>
                </tabel_identificatie>
                <identificatie>
                    <xsl:value-of select="Objecten:documentnummer"/>
                </identificatie>
                <datum>
                    <xsl:value-of select="Objecten:documentdatum"/>
                </datum>
            </brondocument>
        </xsl:if>
    </xsl:template>


    <!-- jjjj-mm-dd -> jjjjmmdd -->
    <!--    <xsl:template name="numeric-date">-->
    <!--        <xsl:value-of select="concat(substring(.,1,4),substring(.,6,2),substring(.,9,2))"/>-->
    <!--    </xsl:template>-->

    <!-- jjjjmmdd -> jjjj-mm-dd -->
    <!--    <xsl:template name="date-numeric">-->
    <!--        <xsl:choose>-->
    <!--            <xsl:when test="string-length(.) &gt; 9">-->
    <!--                <xsl:value-of select="concat(substring(.,1,4),'-',substring(.,5,2),'-',substring(.,7,2))"/>-->
    <!--            </xsl:when>-->
    <!--            <xsl:when test="string-length(.) &gt; 7">-->
    <!--                <xsl:value-of select="concat(substring(.,1,4),'-',substring(.,5,2),'-01')"/>-->
    <!--            </xsl:when>-->
    <!--            <xsl:when test="string-length(.) &gt; 5">-->
    <!--                <xsl:value-of select="concat(substring(.,1,4),'-01-01')"/>-->
    <!--            </xsl:when>-->
    <!--        </xsl:choose>-->
    <!--    </xsl:template>-->

    <!-- bijv. NL.IMBAG.NUMMERAANDUIDING.0003200000140496 -> 0003200000140496 -->
    <xsl:template name="aanduiding-referentie">
        <xsl:param name="input"/>
        <!--        <xsl:comment>input: <xsl:value-of select="$input" /></xsl:comment>-->
        <xsl:choose>
            <xsl:when test="contains($input,'.')">
                <xsl:call-template name="aanduiding-referentie">
                    <xsl:with-param name="input" select="substring-after($input,'.')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$input"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
