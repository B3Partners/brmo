<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright (C) 2021 B3Partners B.V. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:woz="http://www.waarderingskamer.nl/StUF/0312"
                xmlns:bg="http://www.egem.nl/StUF/sector/bg/0310"
                xmlns:s="http://www.egem.nl/StUF/StUF0301"
                version="1.0"
>
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

    <!-- @see nl.b3p.brmo.loader.xml.WozXMLReader#NPS_PREFIX -->
    <xsl:variable name="PREFIX" select="'WOZ.NPS.'"/>
    <!-- <xsl:param name="objectRef" select="'WOZ.NPS.8837b49b4c4f459a0ff7a39582bb6ab7b252125e'" /> -->
    <!-- <xsl:param name="datum" select"'2020-07-12 07:23:07.439'" /> -->
    <xsl:param name="objectRef" select="'WOZ.WOZ.800000793120'"/>
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
                <xsl:apply-templates select="//woz:object"/>
            </data>
        </root>
    </xsl:template>


    <xsl:template name="NPS" match="woz:object[@s:entiteittype='NPS']">
        <xsl:call-template name="comfortPerson">
            <xsl:with-param name="snapshot-date" select="$datum"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="NNP" match="woz:object[@s:entiteittype='NNP']">
        <!--        TODO -->
        <!--        <xsl:call-template name="comfortPerson">-->
        <!--            <xsl:with-param name="snapshot-date" select="$datum"/>-->
        <!--        </xsl:call-template>-->
    </xsl:template>

    <xsl:template name="WOZ" match="woz:object[@s:entiteittype='WOZ'] | woz:object[@s:entiteittype='SWO']">
        <woz_obj column-dat-beg-geldh="dat_beg_geldh" column-datum-einde-geldh="datum_einde_geldh">
            <dat_beg_geldh>
                <xsl:for-each select="s:tijdvakGeldigheid/s:beginGeldigheid">
                    <xsl:call-template name="date-numeric"/>
                </xsl:for-each>
            </dat_beg_geldh>
            <nummer>
                <xsl:value-of select="woz:wozObjectNummer"/>
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
            <soort_obj_code>
                <!-- 4 cijfer code; lijkt niet voor te komen in XML schema... -->
            </soort_obj_code>
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
                <xsl:value-of select="woz:wozObjectGeometrie"/>
            </geom>
            <!--
            dit is een letter, bijv. G voor gebouwd, of O, of... maar zit niet in RSGB 2
            <xsl:value-of select="woz:codeGebouwdOngebouwd"/>
            -->
        </woz_obj>

        <woz_waarde column-dat-beg-geldh="waardepeildatum">
            <waardepeildatum>
                <xsl:for-each select="woz:waardepeildatum">
                    <xsl:call-template name="date-numeric"/>
                </xsl:for-each>
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
                <xsl:value-of select="woz:wozObjectNummer"/>
            </fk_1woz_nummer>
        </woz_waarde>

        <!-- conditioneel, alleen als er een brondocument identificatie is -->
        <xsl:if test="woz:isBeschiktVoor/woz:brondocument/bg:identificatie">
            <brondocument ignore-duplicates="yes">
                <!-- brondocument voor beschikking woz waarde -->
                <tabel>
                    <xsl:value-of select="'woz_waarde'"/>
                </tabel>
                <tabel_identificatie>
                    <xsl:value-of select="woz:wozObjectNummer"/>
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


        <!--
            TODO waarschijnlijk moeten we de WOZ:heeftSluimerendObject onderbrengen in "woz_deelobj"
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

        <xsl:for-each select="woz:heeftBelanghebbende">
            <!--  TODO voorafgaand aan belangen moeten de subjecten en objecten aangemaakt zijn omdat
                    woz_belang in essentie een koppeltabel is
            -->
            <xsl:call-template name="woz_belang"/>
        </xsl:for-each>
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
        <!-- woz:object/woz:heeftBelanghebbende/ -->
        <woz_belang>
            <fk_sc_lh_sub_identif>
                <xsl:call-template name="getHash">
                    <xsl:with-param name="bsn" select="woz:gerelateerde/woz:natuurlijkPersoon/woz:soFiNummer"/>
                </xsl:call-template>
            </fk_sc_lh_sub_identif>
            <fk_sc_rh_woz_nummer>
                <xsl:value-of select="../woz:wozObjectNummer"/>
            </fk_sc_rh_woz_nummer>
            <aand_eigenaargebruiker>
                <xsl:value-of select="woz:aanduidingEigenaarGebruiker"/>
            </aand_eigenaargebruiker>
        </woz_belang>
        <!--  TODO  call comfortPerson ? of bericht uitsplitsen voor verwerken -->
    </xsl:template>

    <xsl:template name="comfortPerson">
        <xsl:param name="snapshot-date"/>
        <xsl:variable name="class">INGESCHREVEN NATUURLIJK PERSOON</xsl:variable>
        <xsl:variable name="searchcol">
            <xsl:call-template name="getHash">
                <xsl:with-param name="bsn" select="woz:soFiNummer"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:if test="$searchcol != ''">
            <xsl:variable name="datum">
                <!-- 2020-07-12 07:23:07.439 naar 2020-07-12T07:23:07.439 -->
                <xsl:value-of select="substring($snapshot-date,0,11)"/>
                <xsl:value-of select="'T'"/>
                <xsl:value-of select="substring($snapshot-date,12)"/>
            </xsl:variable>

            <comfort search-table="subject" search-column="identif" search-value="{$searchcol}"
                     snapshot-date="{$datum}">

                <xsl:for-each select="//woz:gerelateerde[@s:entiteittype='NPS']">
                    <xsl:call-template name="persoon">
                        <xsl:with-param name="key" select="$searchcol"/>
                        <xsl:with-param name="class" select="$class"/>
                    </xsl:call-template>
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
            <!-- DONE /compleet -->
            <sc_identif>
                <xsl:value-of select="$key"/>
            </sc_identif>
            <clazz>
                <xsl:value-of select="$class"/>
            </clazz>
        </prs>

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
            <!-- na_ gegevens vullen met nm_ -->
            <!-- <na_aanhef_aanschrijving></na_aanhef_aanschrijving>-->
            <na_geslachtsnaam_aanschrijving>
                <xsl:value-of select="bg:geslachtsnaam"/>
            </na_geslachtsnaam_aanschrijving>
            <na_voorletters_aanschrijving>
                <xsl:value-of select="bg:voorletters"/>
            </na_voorletters_aanschrijving>
            <na_voornamen_aanschrijving>
                <xsl:value-of select="bg:voornamen"/>
            </na_voornamen_aanschrijving>
            <!-- [FK] AN3, FK naar academische_titel.code: "Referentielijst NATUURLIJK PERSOON.Academische titel" -->
            <!-- <fk_2acd_code></fk_2acd_code>-->
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
            <!-- TODO -->
            <!--    <btnlnds_rsdoc></btnlnds_rsdoc>-->
            <!--    <burgerlijke_staat></burgerlijke_staat>-->
            <!--    <dat_beg_geldh_verblijfpl></dat_beg_geldh_verblijfpl>-->
            <!--    <datum_inschrijving_in_gemeente></datum_inschrijving_in_gemeente>-->
            <!--    <datum_opschorting_bijhouding></datum_opschorting_bijhouding>-->
            <!--    <datum_verkr_nation></datum_verkr_nation>-->
            <!--    <datum_verlies_nation></datum_verlies_nation>-->
            <!--    <datum_vertrek_uit_nederland></datum_vertrek_uit_nederland>-->
            <!--    <datum_vestg_in_nederland></datum_vestg_in_nederland>-->
            <!--    <gemeente_van_inschrijving></gemeente_van_inschrijving>-->
            <!--    <handelingsbekwaam></handelingsbekwaam>-->
            <indic_geheim>
                <xsl:value-of select="bg:inp.indicatieGeheim"/>
            </indic_geheim>
            <!-- TODO -->
            <!--    <rechtstoestand></rechtstoestand>-->
            <!--    <reden_opschorting_bijhouding></reden_opschorting_bijhouding>-->
            <!--    <signalering_rsdoc></signalering_rsdoc>-->
            <!--    <fk_27lpl_sc_identif></fk_27lpl_sc_identif>-->
            <!--    <fk_28nra_sc_identif></fk_28nra_sc_identif>-->
            <!--    <fk_29wpl_identif></fk_29wpl_identif>-->
            <!--    <fk_30spl_sc_identif></fk_30spl_sc_identif>-->
            <!--    <fk_31vbo_sc_identif></fk_31vbo_sc_identif>-->
            <!--    <fk_1rsd_nummer></fk_1rsd_nummer>-->
            <gb_geboortedatum>
                <xsl:value-of select="bg:geboortedatum"/>
            </gb_geboortedatum>
            <!-- TODO -->
            <!--    <fk_gb_lnd_code_iso></fk_gb_lnd_code_iso>-->
            <!--    <gb_geboorteplaats></gb_geboorteplaats>-->
            <!--    <nt_aand_bijzonder_nlschap></nt_aand_bijzonder_nlschap>-->
            <!--    <fk_nt_nat_code></fk_nt_nat_code>-->
            <!--    <nt_reden_verkr_nlse_nation></nt_reden_verkr_nlse_nation>-->
            <!--    <nt_reden_verlies_nlse_nation></nt_reden_verlies_nlse_nation>-->
            <!--    <fk_ol_lnd_code_iso></fk_ol_lnd_code_iso>-->
            <ol_overlijdensdatum>
                <xsl:value-of select="bg:datumOverlijden"/>
            </ol_overlijdensdatum>
            <ol_overlijdensplaats>
                <xsl:value-of select="bg:plaatsOverlijden"/>
            </ol_overlijdensplaats>
            <!-- TODO -->
            <!--    <va_adresherkomst></va_adresherkomst>-->
            <!--    <va_loc_beschrijving></va_loc_beschrijving>-->
            <!--    <fk_va_3_vbo_sc_identif></fk_va_3_vbo_sc_identif>-->
            <!--    <fk_va_4_spl_sc_identif></fk_va_4_spl_sc_identif>-->
            <!--    <fk_va_5_nra_sc_identif></fk_va_5_nra_sc_identif>-->
            <!--    <fk_va_6_wpl_identif></fk_va_6_wpl_identif>-->
            <!--    <fk_va_7_lpl_sc_identif></fk_va_7_lpl_sc_identif>-->
            <!--    <fk_3nat_code></fk_3nat_code>-->
            <!--    <fk_17lnd_code_iso></fk_17lnd_code_iso>-->
            <!--    <fk_18lnd_code_iso></fk_18lnd_code_iso>-->
        </ingeschr_nat_prs>
    </xsl:template>

    <xsl:template name="subject">
        <naam>
            <!-- voor NPS-->
            <xsl:value-of select="bg:voorletters"/>
            <xsl:if test="bg:voorvoegselGeslachtsnaam != ''">
                <xsl:value-of select="' '"/>
            </xsl:if>
            <xsl:value-of select="bg:voorvoegselGeslachtsnaam"/>
            <xsl:value-of select="' '"/>
            <xsl:value-of select="bg:geslachtsnaam"/>
            <!-- TODO naam van NNP -->
        </naam>
        <!-- TODO -->
        <!--    <adres_binnenland></adres_binnenland>   AN257 - Adres binnenland  -->
        <!--    <adres_buitenland></adres_buitenland>   AN500 - Adres buitenland  -->
        <!--    <emailadres></emailadres>   AN254 - Emailadres  -->
        <!--    <fax_nummer></fax_nummer>  -->
        <!--    <kvk_nummer></kvk_nummer>  -->
        <!--    <telefoonnummer></telefoonnummer>  -->
        <!--    <website_url></website_url>  -->
        <!--    <fk_13wpl_identif></fk_13wpl_identif>   [FK] AN4, FK naar wnplts.identif: "heeft als correspondentieadres"  -->
        <!--    <fk_14aoa_identif></fk_14aoa_identif>   [FK] AN16, FK naar addresseerb_obj_aand.identif: "heeft als factuuradres"  -->
        <!--    <fk_15aoa_identif></fk_15aoa_identif>   [FK] AN16, FK naar addresseerb_obj_aand.identif: "heeft als correspondentieadres"  -->
        <!--    <pa_postadres_postcode></pa_postadres_postcode> Groepsattribuut Postadres SUBJECT.Postadres postcode - Postadres postcode  -->
        <!--    <pa_postadrestype></pa_postadrestype>   Groepsattribuut Postadres SUBJECT.Postadrestype - Postadrestype  -->
        <!--    <pa_postbus__of_antwoordnummer></pa_postbus__of_antwoordnummer> Groepsattribuut Postadres SUBJECT.Postbus- of antwoordnummer - Postbus- of antwoordnummer  -->
        <!--    <fk_pa_4_wpl_identif></fk_pa_4_wpl_identif> [FK] AN4, FK naar wnplts.identif: "Groepsattribuut Postadres SUBJECT.woonplaats"  -->
        <!--    <rn_bankrekeningnummer></rn_bankrekeningnummer> Groepsattribuut Rekeningnummer SUBJECT.Bankrekeningnummer - Bankrekeningnummer  -->
        <!--    <rn_bic></rn_bic>   Groepsattribuut Rekeningnummer SUBJECT.BIC - BIC  -->
        <!--    <rn_iban></rn_iban> Groepsattribuut Rekeningnummer SUBJECT.IBAN - IBAN  -->
        <!--    <vb_adres_buitenland_1></vb_adres_buitenland_1> Groepsattribuut Verblijf buitenland SUBJECT.Adres buitenland 1 - Adres buitenland 1  -->
        <!--    <vb_adres_buitenland_2></vb_adres_buitenland_2> Groepsattribuut Verblijf buitenland SUBJECT.Adres buitenland 2 - Adres buitenland 2  -->
        <!--    <vb_adres_buitenland_3></vb_adres_buitenland_3> Groepsattribuut Verblijf buitenland SUBJECT.Adres buitenland 3 - Adres buitenland 3  -->
        <!--    <fk_vb_lnd_code_iso></fk_vb_lnd_code_iso>   [FK] A2, FK naar land.code_iso: "Groepsattribuut referentielijst Land verblijfadres"  -->
    </xsl:template>

    <!-- jjjj-mm-dd -> jjjjmmdd -->
    <xsl:template name="numeric-date">
        <xsl:value-of select="concat(substring(.,1,4),substring(.,6,2),substring(.,9,2))"/>
    </xsl:template>

    <!-- jjjjmmdd -> jjjj-mm-dd -->
    <xsl:template name="date-numeric">
        <xsl:choose>
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
            <xsl:value-of select="$PREFIX"/>
            <xsl:value-of select="$bsn"/>
        </xsl:variable>
        <xsl:variable name="hashedbsn">
            <xsl:value-of select="$PREFIX"/>
            <xsl:value-of select="/root/bsnhashes/*[name()=$bsnwithprefix]"/>
        </xsl:variable>
        <xsl:value-of select="$hashedbsn"/>
    </xsl:template>
</xsl:stylesheet>