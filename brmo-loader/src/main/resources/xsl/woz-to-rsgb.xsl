<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright (C) 2021 B3Partners B.V. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:woz="http://www.waarderingskamer.nl/StUF/0312"
                xmlns:bg="http://www.egem.nl/StUF/sector/bg/0310"
                xmlns:s="http://www.egem.nl/StUF/StUF0301"
                xmlns:gml="http://www.opengis.net/gml"
                version="1.0"
>
    <xsl:output method="xml" indent="no" omit-xml-declaration="yes" encoding="UTF-8"/>

    <!-- @see nl.b3p.brmo.loader.xml.WozXMLReader#NPS_PREFIX -->
    <xsl:variable name="PREFIX_NPS" select="'WOZ.NPS.'"/>
    <xsl:variable name="PREFIX_NNP" select="'WOZ.NNP.'"/>
    <xsl:variable name="PREFIX_WOZ" select="'WOZ.WOZ.'"/>
    <xsl:variable name="PREFIX_VES" select="'WOZ.VES.'"/>

    <!-- <xsl:param name="objectRef" select="'WOZ.NPS.8837b49b4c4f459a0ff7a39582bb6ab7b252125e'" /> -->
    <!-- <xsl:param name="datum" select"'2020-07-12 07:23:07.439'" /> -->
    <xsl:param name="objectRef" select="'WOZ.WOZ.800000793120'"/>
    <xsl:param name="objectNum" select="'800000793120'"/>
    <xsl:param name="datum" select="'2020-07-12 07:21:47.894'"/>

    <!--    <xsl:param name="objectRef" select="'WOZ:onbekend'"/>-->
    <!--    <xsl:param name="datum"/>-->
    <xsl:param name="volgordeNummer" select="'0'"/>
    <xsl:param name="soort" select="'woz'"/>
    <xsl:param name="rsgb-version" select="2.2"/>

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
                <xsl:apply-templates select="//woz:object[not(ancestor::woz:historie)]"/>
            </data>
        </root>
    </xsl:template>


    <xsl:template name="NPS" match="woz:object[@s:entiteittype='NPS'] | woz:nieuweGemeenteNPS[@s:entiteittype='NPS']">
        <xsl:variable name="searchcol">
            <xsl:call-template name="getHash">
                <xsl:with-param name="bsn" select="woz:soFiNummer"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:comment>
            <xsl:text>natuurlijk persoon bericht: </xsl:text>
            <xsl:value-of select="$searchcol"/>
        </xsl:comment>

        <xsl:call-template name="comfortPerson">
            <xsl:with-param name="snapshot-date" select="$datum"/>
            <xsl:with-param name="comfort-search-value" select="$searchcol"/>
            <xsl:with-param name="class">INGESCHREVEN NATUURLIJK PERSOON</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="NNP" match="woz:object[@s:entiteittype='NNP']">
        <xsl:comment>
            <xsl:text>niet-natuurlijk persoon bericht: </xsl:text>
            <xsl:value-of select="$objectRef"/>
        </xsl:comment>

        <xsl:call-template name="comfortPerson">
            <xsl:with-param name="snapshot-date" select="$datum"/>
            <xsl:with-param name="comfort-search-value" select="$objectRef"/>
            <xsl:with-param name="class">INGESCHREVEN NIET-NATUURLIJK PERSOON</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="VES" match="woz:object[@s:entiteittype='VES']">
        <xsl:comment>
            <xsl:text>vestiging bericht nummer: </xsl:text>
            <xsl:value-of select="$objectRef"/>
        </xsl:comment>

        <xsl:call-template name="comfortVestiging">
            <xsl:with-param name="snapshot-date" select="$datum"/>
            <xsl:with-param name="comfort-search-value" select="$objectRef"/>
            <xsl:with-param name="class">VESTIGING</xsl:with-param>
        </xsl:call-template>

    </xsl:template>


    <xsl:template name="WOZ" match="woz:object[@s:entiteittype='WOZ'] | woz:object[@s:entiteittype='SWO']">
        <xsl:variable name="objectNum">
            <xsl:value-of select="woz:wozObjectNummer"/>
        </xsl:variable>

        <xsl:comment>
            <xsl:text>woz object bericht: </xsl:text>
            <xsl:value-of select="$objectNum"/>
        </xsl:comment>

        <woz_obj column-dat-beg-geldh="dat_beg_geldh" column-datum-einde-geldh="datum_einde_geldh">
            <dat_beg_geldh>
                <xsl:for-each select="s:tijdvakGeldigheid/s:beginGeldigheid">
                    <xsl:call-template name="date-numeric"/>
                </xsl:for-each>
            </dat_beg_geldh>
            <nummer>
                <xsl:value-of select="$objectNum"/>
            </nummer>
            <datum_einde_geldh>
                <xsl:for-each select="s:tijdvakGeldigheid/s:eindGeldigheid">
                    <xsl:call-template name="date-numeric"/>
                </xsl:for-each>
            </datum_einde_geldh>
            <gebruikscode>
                <xsl:value-of select="woz:gebruikscode"/>
            </gebruikscode>
            <grondoppervlakte>
                <xsl:value-of select="woz:grondoppervlakte"/>
            </grondoppervlakte>
            <!-- <xsl:value-of select="woz:codeGebouwdOngebouwd"/>is een letter, bijv. G voor gebouwd, of O, of... maar zit niet in woz_obj, wel in woz_deelobj             -->
            <soort_obj_code><!-- 4 cijfer code; lijkt niet voor te komen in XML schema... --></soort_obj_code>
            <status>
                <xsl:value-of select="woz:statusWozObject"/>
            </status>
            <vastgestelde_waarde>
                <xsl:value-of select="woz:vastgesteldeWaarde"/>
            </vastgestelde_waarde>
            <waardepeildatum>
                <xsl:for-each select="woz:waardepeildatum">
                    <xsl:call-template name="date-numeric"/>
                </xsl:for-each>
            </waardepeildatum>
            <geom>
                <xsl:copy-of select="woz:wozObjectGeometrie/gml:Polygon"/>
            </geom>
            <waterschap>
                <xsl:value-of select="woz:ligtIn/woz:gerelateerde/woz:betrokkenWaterschap"/>
            </waterschap>
            <fk_verantw_gem_code>
                <xsl:value-of select="woz:verantwoordelijkeGemeente/bg:gemeenteCode"/>
            </fk_verantw_gem_code>
        </woz_obj>

        <xsl:call-template name="wozWaarde">
            <xsl:with-param name="objectNum" select="$objectNum"/>
        </xsl:call-template>

        <xsl:for-each select="woz:heeftSluimerendObject">
            <!-- WOZ:heeftSluimerendObject onderbrengen in "woz_deelobj" -->
            <xsl:call-template name="sluimerendObject">
                <xsl:with-param name="objectNum" select="$objectNum"/>
            </xsl:call-template>
        </xsl:for-each>

        <xsl:for-each select="woz:heeftBelanghebbende">
            <xsl:variable name="key">
                <xsl:if test="woz:gerelateerde/woz:natuurlijkPersoon/woz:soFiNummer">
                    <xsl:call-template name="getHash">
                        <xsl:with-param name="bsn" select="woz:gerelateerde/woz:natuurlijkPersoon/woz:soFiNummer"/>
                    </xsl:call-template>
                </xsl:if>
                <xsl:if test="woz:gerelateerde/woz:nietNatuurlijkPersoon/woz:isEen/woz:gerelateerde/bg:inn.nnpId">
                    <xsl:value-of select="$PREFIX_NNP"/>
                    <xsl:value-of
                            select="woz:gerelateerde/woz:nietNatuurlijkPersoon/woz:isEen/woz:gerelateerde/bg:inn.nnpId"/>
                </xsl:if>
                <xsl:if test="woz:gerelateerde/woz:vestiging/woz:isEen/woz:gerelateerde/bg:vestigingsNummer">
                    <xsl:value-of select="$PREFIX_VES"/>
                    <xsl:value-of
                            select="woz:gerelateerde/woz:vestiging/woz:isEen/woz:gerelateerde/bg:vestigingsNummer"/>
                </xsl:if>
            </xsl:variable>

            <xsl:if test="woz:gerelateerde/woz:natuurlijkPersoon/woz:soFiNummer">
                <xsl:call-template name="comfortPerson">
                    <xsl:with-param name="snapshot-date" select="$datum"/>
                    <xsl:with-param name="comfort-search-value" select="$key"/>
                    <xsl:with-param name="class" select="'INGESCHREVEN NATUURLIJK PERSOON'"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="woz:gerelateerde/woz:nietNatuurlijkPersoon/woz:isEen/woz:gerelateerde/bg:inn.nnpId">
                <xsl:call-template name="comfortPerson">
                    <xsl:with-param name="snapshot-date" select="$datum"/>
                    <xsl:with-param name="comfort-search-value" select="$key"/>
                    <xsl:with-param name="class" select="'INGESCHREVEN NIET NATUURLIJK PERSOON'"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="woz:gerelateerde/woz:vestiging/woz:isEen/woz:gerelateerde/bg:vestigingsNummer">
                <xsl:text>VESTIGING</xsl:text>
                <xsl:call-template name="comfortVestiging">
                    <xsl:with-param name="snapshot-date" select="$datum"/>
                    <xsl:with-param name="comfort-search-value" select="$key"/>
                    <xsl:with-param name="class" select="'VESTIGING'"/>
                </xsl:call-template>
            </xsl:if>



            <xsl:call-template name="woz_belang">
                <xsl:with-param name="key" select="$key"/>
            </xsl:call-template>
        </xsl:for-each>

        <xsl:for-each
                select="woz:omvat/woz:gerelateerde/bg:kadastraleIdentificatie| woz:bevatKadastraleObjecten/woz:gerelateerde/bg:kadastraleIdentificatie">
            <!-- https://b3partners.atlassian.net/browse/BRMO-204 lege kadaster identificatie -->
            <xsl:if test=". !=''">
                <woz_omvat>
                    <fk_sc_lh_kad_identif>
                        <xsl:value-of select="."/>
                    </fk_sc_lh_kad_identif>
                    <fk_sc_rh_woz_nummer>
                        <xsl:value-of select="$objectNum"/>
                    </fk_sc_rh_woz_nummer>
                    <toegekende_opp>
                        <xsl:value-of select="../../woz:toegekendeOppervlakte"/>
                    </toegekende_opp>
                </woz_omvat>
            </xsl:if>
            <xsl:if test=". = ''">
                <xsl:comment>
                    <xsl:text>kadaster identificatie is leeg, kan woz object niet opnemen in woz_omvat koppeltabel</xsl:text>
                </xsl:comment>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="WRD" match="woz:object[@s:entiteittype='WRD']">
        <xsl:variable name="objectNum">
            <xsl:value-of select="woz:isVoor/woz:gerelateerde/woz:wozObjectNummer"/>
        </xsl:variable>

        <xsl:comment>
            <xsl:text>woz waarde bericht voor </xsl:text>
            <xsl:value-of select="$objectNum"/>
        </xsl:comment>

        <xsl:call-template name="wozWaarde">
            <xsl:with-param name="objectNum" select="$objectNum"/>
        </xsl:call-template>

    </xsl:template>

    <xsl:template name="wozWaarde">
        <!-- maak woz_waarde en bijbehorend brondocument -->
        <!-- TODO   nog niet duidelijk waar de
                    WOZ:isBeschiktVoor/WOZ:gerelateerde/WOZ:natuurlijkPersoon/WOZ:isEen/WOZ:gerelateerde
                    in moet komen
        -->
        <xsl:param name="objectNum"/>
        <!-- conditioneel, alleen als er een waardepeildatum is... -->
        <xsl:if test="woz:waardepeildatum">
            <woz_waarde column-dat-beg-geldh="waardepeildatum">
                <waardepeildatum>
                    <xsl:value-of select="woz:waardepeildatum"/>
                </waardepeildatum>
                <status_beschikking>
                    <xsl:value-of select="woz:isBeschiktVoor/woz:statusBeschikking"/>
                </status_beschikking>
                <toestandspeildatum>
                    <xsl:value-of select="woz:toestandspeildatum"/>
                </toestandspeildatum>
                <vastgestelde_waarde>
                    <xsl:value-of select="woz:vastgesteldeWaarde"/>
                </vastgestelde_waarde>
                <fk_1woz_nummer>
                    <xsl:value-of select="$objectNum"/>
                </fk_1woz_nummer>
            </woz_waarde>
        </xsl:if>

        <!-- conditioneel, alleen als er een brondocument identificatie is -->
        <xsl:if test="woz:isBeschiktVoor/woz:brondocument/bg:identificatie">
            <brondocument ignore-duplicates="yes">
                <!-- brondocument voor beschikking woz waarde -->
                <tabel>
                    <xsl:value-of select="'woz_waarde'"/>
                </tabel>
                <tabel_identificatie>
                    <xsl:value-of select="$objectNum"/>
                </tabel_identificatie>
                <identificatie>
                    <xsl:value-of select="woz:isBeschiktVoor/woz:brondocument/bg:identificatie"/>
                </identificatie>
                <datum>
                    <xsl:for-each select="woz:isBeschiktVoor/woz:brondocument/bg:datum">
                        <xsl:call-template name="date-numeric"/>
                    </xsl:for-each>
                </datum>
            </brondocument>
        </xsl:if>
    </xsl:template>

    <xsl:template name="sluimerendObject">
        <xsl:param name="objectNum"/>
        <!--
              maar het WOZ:object zelf heeft al een aanduiding met BAG verwijzing (WOZ:aanduidingWOZobject) en
              kadaster verwijzing (WOZ:bevatKadastraleObjecten) en dat zijn typisch gegevens die in "woz_deelobj"
              komen en niet in "woz_obj" passen...

            woz_deelobj (woz_deelobj_archief)

            WOZ-deelobject
            ==============
            Het WOZ-DEELOBJECT betreft een element van een WOZ-OBJECT; meerdere WOZdeelobjecten
            (bijvoorbeeld de woning, de losstaande schuur en de grond) vormen gezamenlijk
            een WOZ-object en/of onderbouwen de waarde ervan nader (bijvoorbeeld de waarde-invloed
            van bodemverontreiniging). Voor een WOZ-deelobject geldt telkens één van de volgende
            situaties:
            - het komt overeen met een gebouwd object, benoemd terrein of is een gedeelte daarvan,
            - het komt overeen met een pand of is een gedeelte daarvan of
            - het is geen van beide, het WOZ-deelobject betreft geen gebouwd object, pand, benoemd
              terrein of gedeelte daarvan; een voorbeeld hiervan is een bouwkavel waarop nog geen
              bouwvergunning verleend is.

            Het is bovendien zo dat een WOZ-deelobject nooit overeen kan komen met meer dan één (deel
            van een) gebouwd object, benoemd terrein of pand. En, indien een WOZ-deelobject een relatie
            heeft met een pand, dan kan het alleen gaan om panden waarbinnen geen verblijfsobjecten
            afgebakend zijn, zoals garages en schuren bij woningen, en gedeelten van panden die niet
            afgebakend zijn als verblijfsobject, zoals een niet-afsluitbare parkeergarage onder een
            appartementencomplex.

            zie: https://www.gemmaonline.nl/index.php/Rsgb_3.0/doc/objecttype/woz-deelobject
            zie ook: https://www.gemmaonline.nl/images/gemmaonline/f/f9/Gegevenswoordenboek_StUF_woz_03.12.02.pdf
        -->
        <woz_deelobj column-dat-beg-geldh="dat_beg_geldh_deelobj" column-datum-einde-geldh="datum_einde_geldh_deelobj">
            <dat_beg_geldh_deelobj>
                <xsl:for-each select="s:tijdvakGeldigheid/s:beginGeldigheid">
                    <xsl:call-template name="date-numeric"/>
                </xsl:for-each>
            </dat_beg_geldh_deelobj>
            <nummer>
                <!-- [PK] N12 - Nummer WOZ-deelobject -->
                <xsl:value-of select="woz:gerelateerde/woz:wozObjectNummer"/>
            </nummer>
            <code>
                <!-- AN4 - Code WOZ-deelobject-->
                <xsl:value-of select="woz:codeGebouwdOngebouwd"/>
            </code>
            <datum_einde_geldh_deelobj>
                <!-- N8 - Datum einde geldigheid deelobject-->
                <xsl:for-each select="s:tijdvakGeldigheid/s:eindGeldigheid">
                    <xsl:call-template name="date-numeric"/>
                </xsl:for-each>
            </datum_einde_geldh_deelobj>
            <status>
                <!-- N2 - Status WOZ-deelobject -->
                <xsl:value-of select="woz:statusWozObject"/>
            </status>
            <fk_6woz_nummer>
                <!-- [FK] N12, FK naar woz_obj.nummer: "is onderdeel van" -->
                <xsl:value-of select="$objectNum"/>
            </fk_6woz_nummer>
            <fk_4pnd_identif><!-- TODO [FK] AN16, FK naar pand.identif: "bestaat uit"--></fk_4pnd_identif>
            <fk_5tgo_identif><!-- TODO [FK] AN16, FK naar benoemd_obj.identif: "bestaat uit" --></fk_5tgo_identif>
        </woz_deelobj>
    </xsl:template>

    <xsl:template name="aanduiding">
        <locaand_openb_rmte>
            <fk_sc_lh_opr_identifcode>
                <xsl:value-of select="woz:heeftAlsAanduiding/woz:gerelateerde/bg:identificatie"/>
            </fk_sc_lh_opr_identifcode>
            <fk_sc_rh_woz_nummer>
                <xsl:value-of select="woz:wozObjectNummer"/>
            </fk_sc_rh_woz_nummer>
            <locomschr>
                <xsl:value-of select="woz:heeftAlsAanduiding/woz:locatieOmschrijving"/>
            </locomschr>
        </locaand_openb_rmte>

        <locaand_adres>
            <fk_sc_lh_aoa_identif>
                <xsl:value-of select="woz:aanduidingWOZobject/bg:aoa.identificatie"/>
            </fk_sc_lh_aoa_identif>
            <fk_sc_rh_woz_nummer>
                <xsl:value-of select="woz:wozObjectNummer"/>
            </fk_sc_rh_woz_nummer>
            <locomschr>
                <xsl:value-of select="woz:aanduidingWOZobject/bg:locatieOmschrijving"/>
            </locomschr>
        </locaand_adres>
    </xsl:template>

    <xsl:template name="woz_belang">
        <xsl:param name="key"/>

        <xsl:choose>
            <xsl:when test="not($key) and not($key='')">
                <xsl:comment>geen waarde voor fk_sc_lh_sub_identif - woz_belang kan niet gekoppeld worden</xsl:comment>
            </xsl:when>
            <xsl:when test="$key = $PREFIX_NPS">
                <xsl:comment>geen geldige waarde voor fk_sc_lh_sub_identif - woz_belang kan niet gekoppeld worden
                </xsl:comment>
            </xsl:when>
            <xsl:when test="$key = $PREFIX_NNP">
                <xsl:comment>geen geldige waarde voor fk_sc_lh_sub_identif - woz_belang kan niet gekoppeld worden
                </xsl:comment>
            </xsl:when>
            <xsl:when test="$key = $PREFIX_VES">
                <xsl:comment>geen geldige waarde voor fk_sc_lh_sub_identif - woz_belang kan niet gekoppeld worden
                </xsl:comment>
            </xsl:when>
            <xsl:otherwise>
                <!-- woz:object/woz:heeftBelanghebbende/ -->
                <woz_belang>
                    <fk_sc_lh_sub_identif>
                        <!-- subject -->
                        <xsl:value-of select="$key"/>
                    </fk_sc_lh_sub_identif>
                    <fk_sc_rh_woz_nummer>
                        <xsl:value-of select="../woz:wozObjectNummer"/>
                    </fk_sc_rh_woz_nummer>
                    <aand_eigenaargebruiker>
                        <xsl:value-of select="woz:aanduidingEigenaarGebruiker"/>
                    </aand_eigenaargebruiker>
                </woz_belang>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="comfortPerson">
        <xsl:param name="snapshot-date"/>
        <xsl:param name="class"/>
        <xsl:param name="comfort-search-value"/>

        <xsl:if test="$comfort-search-value != ''">
            <xsl:variable name="datum">
                <!-- 2020-07-12 07:23:07.439 naar 2020-07-12T07:23:07.439 -->
                <xsl:value-of select="substring($snapshot-date,0,11)"/>
                <xsl:value-of select="'T'"/>
                <xsl:value-of select="substring($snapshot-date,12)"/>
            </xsl:variable>

            <comfort search-table="subject" search-column="identif" search-value="{$comfort-search-value}"
                     snapshot-date="{$datum}">

                <xsl:for-each select="//woz:gerelateerde[@s:entiteittype='NPS']">
                    <xsl:call-template name="persoon">
                        <xsl:with-param name="key" select="$comfort-search-value"/>
                        <xsl:with-param name="class" select="$class"/>
                    </xsl:call-template>
                </xsl:for-each>

                <xsl:for-each select="//woz:gerelateerde[@s:entiteittype='NNP']">
                    <xsl:call-template name="persoon">
                        <xsl:with-param name="key" select="$comfort-search-value"/>
                        <xsl:with-param name="class" select="$class"/>
                    </xsl:call-template>
                </xsl:for-each>

            </comfort>
        </xsl:if>
    </xsl:template>

    <xsl:template name="comfortVestiging">
        <xsl:param name="snapshot-date"/>
        <xsl:param name="class"/>
        <xsl:param name="comfort-search-value"/>

        <xsl:if test="$comfort-search-value != ''">
            <xsl:variable name="datum">
                <!-- 2020-07-12 07:23:07.439 naar 2020-07-12T07:23:07.439 -->
                <xsl:value-of select="substring($snapshot-date,0,11)"/>
                <xsl:value-of select="'T'"/>
                <xsl:value-of select="substring($snapshot-date,12)"/>
            </xsl:variable>

            <comfort search-table="subject" search-column="identif" search-value="{$comfort-search-value}"
                     snapshot-date="{$datum}">

                <xsl:for-each select="//woz:gerelateerde[@s:entiteittype='VES']">
                    <xsl:call-template name="persoon">
                        <xsl:with-param name="key" select="$comfort-search-value"/>
                        <xsl:with-param name="class" select="$class"/>
                    </xsl:call-template>

                    <vestg>
                        <sc_identif>
                            <xsl:value-of select="$comfort-search-value"/>
                        </sc_identif>
                        <fk_15ond_kvk_nummer><!-- RSGB 'betreft uitoefening van activiteiten door' fk naar onderneming --></fk_15ond_kvk_nummer>
                        <fk_17mac_kvk_nummer><!-- RSGB 'betreft uitoefening van activiteiten door' fk naar maatschappelijke activiteit --></fk_17mac_kvk_nummer>
                        <fk_20aoa_identif>
                            <xsl:value-of select="bg:verblijfsadres/bg:aoa.identificatie"/>
                        </fk_20aoa_identif>
                        <typering>
                            <!-- <xsl:choose>
                                <xsl:when test="local-name(.) = 'nietCommercieleVestiging'">Niet-commerciele vestiging</xsl:when>
                                <xsl:otherwise>Commerciele vestiging</xsl:otherwise>
                            </xsl:choose> -->
                        </typering>
                        <verkorte_naam>
                            <xsl:value-of select="substring(bg:handelsnaam,1,45)"/>
                        </verkorte_naam>
                        <!--
                        <datum_aanvang></datum_aanvang>
                        <datum_beeindiging></datum_beeindiging>
                        <datum_voortzetting></datum_voortzetting>
                        <fulltime_werkzame_mannen></fulltime_werkzame_mannen>
                        <parttime_werkzame_mannen></parttime_werkzame_mannen>
                        <toevoeging_adres></toevoeging_adres>
                        <activiteit_omschr></activiteit_omschr>
                        -->
                    </vestg>

                    <vestg_naam>
                        <naam>
                            <xsl:value-of select="bg:handelsnaam"/>
                        </naam>
                        <fk_ves_sc_identif>
                            <xsl:value-of select="$comfort-search-value"/>
                        </fk_ves_sc_identif>
                    </vestg_naam>

                </xsl:for-each>
            </comfort>
        </xsl:if>
    </xsl:template>

    <xsl:template name="persoon">
        <xsl:param name="key"/>
        <xsl:param name="class"/>

        <subject>
            <identif>
                <xsl:value-of select="$key"/>
            </identif>
            <clazz>
                <xsl:value-of select="$class"/>
            </clazz>
            <typering>
                <xsl:value-of select="substring($class,1,35)"/>
            </typering>

            <xsl:call-template name="subject"/>
        </subject>

        <prs>
            <sc_identif>
                <xsl:value-of select="$key"/>
            </sc_identif>
            <clazz>
                <xsl:value-of select="$class"/>
            </clazz>
        </prs>

        <xsl:if test="@s:entiteittype='NPS'">
            <xsl:call-template name="nps">
                <xsl:with-param name="key" select="$key"/>
                <xsl:with-param name="class" select="$class"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="@s:entiteittype='NNP'">
            <xsl:call-template name="nnp">
                <xsl:with-param name="key" select="$key"/>
                <xsl:with-param name="class" select="$class"/>
            </xsl:call-template>
        </xsl:if>

    </xsl:template>

    <xsl:template name="subject">
        <naam>
            <xsl:if test="bg:geslachtsnaam">
                <!-- NPS-->
                <xsl:value-of select="bg:voorletters"/>
                <xsl:if test="bg:voorvoegselGeslachtsnaam != ''">
                    <xsl:value-of select="' '"/>
                </xsl:if>
                <xsl:value-of select="bg:voorvoegselGeslachtsnaam"/>
                <xsl:value-of select="' '"/>
                <xsl:value-of select="bg:geslachtsnaam"/>
            </xsl:if>
            <xsl:if test="bg:statutaireNaam">
                <!-- NNP -->
                <xsl:value-of select="bg:statutaireNaam"/>
            </xsl:if>
        </naam>
        <!-- TODO -->
        <!--    <adres_binnenland></adres_binnenland>   AN257 - Adres binnenland  -->
        <!--    <adres_buitenland></adres_buitenland>   AN500 - Adres buitenland  -->
        <!--    <emailadres></emailadres>   AN254 - Emailadres  -->
        <!--    <fax_nummer></fax_nummer>  -->
        <!--    <telefoonnummer></telefoonnummer>  -->
        <!--    <website_url></website_url>  -->
        <!--    <rn_bankrekeningnummer></rn_bankrekeningnummer> Groepsattribuut Rekeningnummer SUBJECT.Bankrekeningnummer - Bankrekeningnummer  -->
        <!--    <rn_bic></rn_bic>   Groepsattribuut Rekeningnummer SUBJECT.BIC - BIC  -->
        <!--    <rn_iban></rn_iban> Groepsattribuut Rekeningnummer SUBJECT.IBAN - IBAN  -->
        <!-- <fk_13wpl_identif/> [FK] AN4, FK naar wnplts.identif: "heeft als correspondentieadres"-->
        <!-- <fk_14aoa_identif/>   [FK] AN16, FK naar addresseerb_obj_aand.identif: "heeft als factuuradres"  -->
        <!-- <fk_pa_4_wpl_identif/> [FK] AN4, FK naar wnplts.identif: "Groepsattribuut Postadres SUBJECT.woonplaats"  -->

        <kvk_nummer>
            <!-- 9 cijfers; is te groot voor kvk nummer <xsl:value-of select="bg:inn.nnpId"/>-->
        </kvk_nummer>
        <fk_15aoa_identif>
            <xsl:value-of select="bg:sub.correspondentieAdres/bg:aoa.identificatie"/>
        </fk_15aoa_identif>
        <pa_postadres_postcode>
            <xsl:value-of select="bg:sub.correspondentieAdres/bg:postcode"/>
        </pa_postadres_postcode>
        <pa_postadrestype>
            <xsl:value-of select="bg:sub.correspondentieAdres/sub.postadresType"/>
        </pa_postadrestype>
        <pa_postbus__of_antwoordnummer>
            <xsl:value-of select="bg:sub.correspondentieAdres/sub.postadresNummer"/>
        </pa_postbus__of_antwoordnummer>
        <vb_adres_buitenland_1>
            <xsl:value-of select="bg:verblijfsadres/bg:sub.adresBuitenland1"/>
        </vb_adres_buitenland_1>
        <vb_adres_buitenland_2>
            <xsl:value-of select="bg:verblijfsadres/bg:sub.adresBuitenland2"/>
        </vb_adres_buitenland_2>
        <vb_adres_buitenland_3>
            <xsl:value-of select="bg:verblijfsadres/bg:sub.adresBuitenland3"/>
        </vb_adres_buitenland_3>
        <fk_vb_lnd_code_iso>
            <xsl:value-of select="bg:verblijfsadres/bg:landcode"/>
        </fk_vb_lnd_code_iso>
    </xsl:template>

    <xsl:template name="nps">
        <xsl:param name="key"/>
        <xsl:param name="class"/>
        <nat_prs>
            <!-- DONE /compleet -->
            <sc_identif>
                <xsl:value-of select="$key"/>
            </sc_identif>
            <clazz>
                <xsl:value-of select="$class"/>
            </clazz>
            <aand_naamgebruik>
                <xsl:value-of select="bg:aanduidingNaamgebruik"/>
            </aand_naamgebruik>
            <geslachtsaand>
                <xsl:value-of select="bg:geslachtsaanduiding"/>
            </geslachtsaand>
            <nm_adellijke_titel_predikaat>
                <xsl:value-of select="bg:adellijkeTitelPredikaat"/>
            </nm_adellijke_titel_predikaat>
            <nm_geslachtsnaam>
                <xsl:value-of select="bg:geslachtsnaam"/>
            </nm_geslachtsnaam>
            <nm_voornamen>
                <xsl:value-of select="bg:voornamen"/>
            </nm_voornamen>
            <nm_voorvoegsel_geslachtsnaam>
                <xsl:value-of select="bg:voorvoegselGeslachtsnaam"/>
            </nm_voorvoegsel_geslachtsnaam>
            <na_aanhef_aanschrijving>
                <xsl:value-of select="bg:aanhefAanschrijving"/>
            </na_aanhef_aanschrijving>
            <na_geslachtsnaam_aanschrijving>
                <xsl:value-of select="bg:geslachtsnaamAanschrijving"/>
            </na_geslachtsnaam_aanschrijving>
            <na_voorletters_aanschrijving>
                <xsl:value-of select="bg:voorletters"/>
            </na_voorletters_aanschrijving>
            <na_voornamen_aanschrijving>
                <xsl:value-of select="bg:voornamen"/>
            </na_voornamen_aanschrijving>
        </nat_prs>
        <ingeschr_nat_prs>
            <sc_identif>
                <xsl:value-of select="$key"/>
            </sc_identif>
            <clazz>
                <xsl:value-of select="$class"/>
            </clazz>
            <!-- <a_nummer></a_nummer>-->
            <bsn>
                <xsl:value-of select="bg:inp.bsn"/>
            </bsn>
            <indic_geheim>
                <xsl:value-of select="bg:inp.indicatieGeheim"/>
            </indic_geheim>
            <gb_geboortedatum>
                <xsl:for-each select="bg:geboortedatum"><xsl:call-template name="numeric-date"/></xsl:for-each>
            </gb_geboortedatum>
            <ol_overlijdensdatum>
                <xsl:for-each select="bg:datumOverlijden"><xsl:call-template name="numeric-date"/></xsl:for-each>
            </ol_overlijdensdatum>
            <ol_overlijdensplaats>
                <xsl:value-of select="bg:plaatsOverlijden"/>
            </ol_overlijdensplaats>
            <va_loc_beschrijving>
                <!-- Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.Locatie beschrijving - Locatie beschrijving -->
                <xsl:choose>
                    <xsl:when test="bg:verblijfsadres/bg:inp.locatiebeschrijving != ''">
                        <xsl:value-of select="bg:verblijfsadres/bg:inp.locatiebeschrijving"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="bg:verblijfsadres/bg:gor.openbareRuimteNaam"/>
                        <xsl:value-of select="' '"/>
                        <xsl:value-of select="bg:verblijfsadres/bg:aoa.huisnummer"/>
                        <xsl:value-of select="bg:verblijfsadres/bg:aoa.huisnummertoevoeging"/>
                        <xsl:value-of select="bg:verblijfsadres/bg:aoa.huisletter"/>
                        <xsl:value-of select="', '"/>
                        <xsl:value-of select="bg:verblijfsadres/bg:aoa.postcode"/>
                        <xsl:value-of select="' '"/>
                        <xsl:value-of select="bg:verblijfsadres/bg:wpl.woonplaatsNaam"/>
                    </xsl:otherwise>
                </xsl:choose>
            </va_loc_beschrijving>
            <fk_va_5_nra_sc_identif>
                <xsl:value-of select="bg:inp.verblijftIn/bg:gerelateerde/bg:num.identificatie"/>
            </fk_va_5_nra_sc_identif>
            <!-- niet in schema of niet is demo data te vinden -->
            <!-- [FK] AN16, FK naar ligplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "verblijft op"-->
            <!-- <fk_27lpl_sc_identif />-->
            <!-- [FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "is ingeschreven op"    -->
            <!-- <fk_28nra_sc_identif />-->
            <!-- [FK] AN4, FK naar wnplts.identif: "verblijft op locatie in" -->
            <!-- <fk_29wpl_identif /> -->
            <!-- [FK] AN16, FK naar standplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "verblijft op"-->
            <!-- <fk_30spl_sc_identif />-->
            <!-- [FK] AN16, FK naar verblijfsobj.sc_identif (is FK naar superclass BENOEMD OBJECT):"Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.verblijfsobject" -->
            <!-- <fk_31vbo_sc_identif />-->
            <!-- [FK] AN16, FK naar verblijfsobj.sc_identif (is FK naar superclass BENOEMD OBJECT): "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.verblijfsobject"-->
            <!-- <fk_va_3_vbo_sc_identif /> -->
            <!-- [FK] AN16, FK naar standplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.standplaats"                -->
            <!-- <fk_va_4_spl_sc_identif/>-->
            <!-- [FK] AN4, FK naar wnplts.identif: "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.woonplaats" -->
            <!-- <fk_va_6_wpl_identif /> -->
            <!-- [FK] AN16, FK naar ligplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.ligplaats" -->
            <!-- <fk_va_7_lpl_sc_identif />-->
        </ingeschr_nat_prs>
    </xsl:template>

    <xsl:template name="nnp">
        <xsl:param name="key"/>
        <xsl:param name="class"/>
        <niet_nat_prs>
            <sc_identif>
                <xsl:value-of select="$key"/>
            </sc_identif>
            <clazz>
                <xsl:value-of select="$class"/>
            </clazz>
            <naam>
                <xsl:value-of select="bg:statutaireNaam"/>
            </naam>
            <datum_aanvang></datum_aanvang>
            <datum_beeindiging></datum_beeindiging>
            <verkorte_naam></verkorte_naam>
        </niet_nat_prs>
        <ingeschr_niet_nat_prs>
            <sc_identif>
                <xsl:value-of select="$key"/>
            </sc_identif>
            <typering>
                <xsl:value-of select="substring($class,1,35)"/>
            </typering>
            <!-- <ovrg_privaatr_rechtsvorm></ovrg_privaatr_rechtsvorm>-->
            <!-- <publiekrechtelijke_rechtsvorm></publiekrechtelijke_rechtsvorm>-->
            <!-- <rechtstoestand></rechtstoestand>-->
            <!-- <rechtsvorm></rechtsvorm>-->
            <rsin>
                <!--  mogelijk niet correct... schema is onduidelijk of dit rsin of kvk nummer is, met 9 cijfers is het te groot voor kvk nummer  -->
                <xsl:value-of select="bg:inn.nnpId"/>
            </rsin>
            <!-- <statutaire_zetel/>-->
            <!-- <fk_8aoa_identif/>  [FK] AN16, FK naar addresseerb_obj_aand.identif: "heeft als bezoekadres" -->
        </ingeschr_niet_nat_prs>
    </xsl:template>

    <!-- jjjj-mm-dd -> jjjjmmdd -->
    <xsl:template name="numeric-date">
        <xsl:value-of select="concat(substring(.,1,4),substring(.,6,2),substring(.,9,2))"/>
    </xsl:template>

    <!-- jjjjmmdd -> jjjj-mm-dd -->
    <xsl:template name="date-numeric">
        <xsl:choose>
            <xsl:when test="current() = 'geenWaarde'">
                <xsl:value-of select="''"/>
            </xsl:when>
            <xsl:when test="string-length(.) &gt; 9">
                <xsl:value-of select="concat(substring(.,1,4),'-',substring(.,5,2),'-',substring(.,7,2))"/>
            </xsl:when>
            <xsl:when test="string-length(.) &gt; 7">
                <xsl:value-of select="concat(substring(.,1,4),'-',substring(.,5,2),'-01')"/>
            </xsl:when>
            <xsl:when test="string-length(.) &gt; 5">
                <xsl:value-of select="concat(substring(.,1,4),'-01-01')"/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- zoek hash op in mapping tabel -->
    <xsl:template name="getHash">
        <xsl:param name="bsn"/>
        <xsl:variable name="bsnwithprefix">
            <xsl:value-of select="$PREFIX_NPS"/>
            <xsl:value-of select="$bsn"/>
        </xsl:variable>
        <xsl:variable name="hashedbsn">
            <xsl:value-of select="$PREFIX_NPS"/>
            <xsl:value-of select="/root/bsnhashes/*[name()=$bsnwithprefix]"/>
        </xsl:variable>
        <xsl:value-of select="$hashedbsn"/>
    </xsl:template>
</xsl:stylesheet>