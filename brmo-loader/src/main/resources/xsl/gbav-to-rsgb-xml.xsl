<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:java="http://xml.apache.org/xslt/java" exclude-result-prefixes="java">
    <xsl:output method="xml" indent="yes" omit-xml-declaration="no" />
    <!-- @see nl.b3p.brmo.loader.xml.GbavXMLReader#PREFIX -->
    <xsl:variable name="PREFIX" select="'NL.GBA.Persoon.'" />
    <xsl:param name="objectRef" select="concat($PREFIX,'onbekend')" />
    <!--<xsl:param name="datum" select="java:format(java:java.text.SimpleDateFormat.new('yyyy-MM-dd'), java:java.util.Date.new())" />-->
    <xsl:param name="datum" />
    <xsl:param name="volgordeNummer" select="'0'" />
    <xsl:param name="soort" select="'gbav'" />
    <xsl:param name="rsgb-version" select="2.2" />
    <xsl:template match="/">
        <root>
            <xsl:comment>
                <xsl:text>objectRef:</xsl:text>
                <xsl:value-of select="$objectRef" />
                <xsl:text>, datum:</xsl:text>
                <xsl:value-of select="$datum" />
                <xsl:text>, volgordeNummer:</xsl:text>
                <xsl:value-of select="$volgordeNummer" />
                <xsl:text>, soort:</xsl:text>
                <xsl:value-of select="$soort" />
            </xsl:comment>
            <data>
                <xsl:apply-templates select="root/persoon" />
            </data>
        </root>
    </xsl:template>
    <xsl:template name="persoon" match="persoon">
        <xsl:for-each select="./categorieen/categorie">
            <xsl:comment>
                <xsl:text>Parse categorie naam:</xsl:text>
                <xsl:value-of select="./naam" />
                <xsl:text>, categorie nummer:</xsl:text>
                <xsl:value-of select="./nummer" />
            </xsl:comment>
            <xsl:choose>
                <xsl:when test="./nummer = '01'">
                    <xsl:call-template name="maakPersoon">
                        <xsl:with-param name="key" select="$objectRef" />
                        <xsl:with-param name="clazz" select='"INGESCHREVEN NATUURLIJK PERSOON"' />
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="./nummer = '02'">
                    <xsl:call-template name="ouder" />
                </xsl:when>
                <xsl:when test="./nummer = '03'">
                    <xsl:call-template name="ouder" />
                </xsl:when>
                <!--/persoon/categorieen/categorie/nummer == 04-->
                <xsl:when test="./nummer = '05'">
                    <xsl:call-template name="partner" />
                </xsl:when>
                <!--/persoon/categorieen/categorie/nummer == 06-->
                <!--/persoon/categorieen/categorie/nummer == 07-->
                <!--/persoon/categorieen/categorie/nummer == 08-->
                <xsl:when test="./nummer = '09'">
                    <xsl:call-template name="kind" />
                </xsl:when>
                <!--/persoon/categorieen/categorie/nummer == 10-->
                <!--/persoon/categorieen/categorie/nummer == 11-->
                <!--/persoon/categorieen/categorie/nummer == 12-->
                <!--/persoon/categorieen/categorie/nummer == 13-->
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <!-- maak een persoon als comfort data -->
    <xsl:template name="comfortPerson">
        <!-- een persoon categorie (ouder, partner, kind, ...) node -->
        <xsl:param name="snapshot-date" />
        <xsl:param name="bsn-nummer" />
        <xsl:param name="clazz" />
        <xsl:variable name="searchcol">
            <xsl:call-template name="getHash">
                <xsl:with-param name="bsn" select="$bsn-nummer" />
            </xsl:call-template>
        </xsl:variable>
        <comfort search-table="subject" search-column="identif" search-value="{$searchcol}" snapshot-date="{$snapshot-date}">
            <xsl:call-template name="maakPersoon">
                <xsl:with-param name="key" select="$searchcol" />
                <xsl:with-param name="clazz" select="$clazz" />
            </xsl:call-template>
        </comfort>
    </xsl:template>

    <!-- maak de ouder-kind relatie en comfortdata ouder/persoon aan -->
    <xsl:template name="ouder">
        <xsl:variable name="snapshot-date">
            <xsl:call-template name="snapshot-date" />
        </xsl:variable>
        <xsl:call-template name="comfortPerson">
            <xsl:with-param name="snapshot-date" select="$snapshot-date" />
            <xsl:with-param name="bsn-nummer" select="./rubrieken/rubriek[nummer='0120']/waarde" />
            <xsl:with-param name="clazz" select='"NATUURLIJK PERSOON"' />
        </xsl:call-template>
        <!-- cat. 02 en 03 (ouder1 en ouder2) -->
        <ouder_kind_rel>
            <fk_sc_lh_inp_sc_identif>
                <xsl:value-of select="$objectRef" />
            </fk_sc_lh_inp_sc_identif>
            <fk_sc_rh_inp_sc_identif>
                <xsl:call-template name="getHash">
                    <xsl:with-param name="bsn" select="./rubrieken/rubriek[nummer='0120']/waarde" />
                </xsl:call-template>
            </fk_sc_rh_inp_sc_identif>
            <datum_einde_fam_recht_betr><!--TODO <xsl:value-of select="einddatum"/>--></datum_einde_fam_recht_betr>
            <datum_ingang_fam_recht_betr>
                <xsl:value-of select="./rubrieken/rubriek[nummer='6210']/waarde" />
            </datum_ingang_fam_recht_betr>
            <ouder_aand>
                <xsl:value-of select="'OUDER'" />
            </ouder_aand>
        </ouder_kind_rel>
        <!-- TODO
             <brondocument ignore-duplicates="yes">
             <tabel>OUDER_KIND_REL</tabel>
             <tabel_identificatie><xsl:value-of select="$objectRef"/></tabel_identificatie>
             <identificatie></identificatie>
             <gemeente></gemeente>
             <omschrijving></omschrijving>
             <datum></datum>
             <ref_id></ref_id>
             </brondocument>
        -->
    </xsl:template>

    <!-- maak de ouder-kind relatie en comfortdata kind/persoon aan -->
    <xsl:template name="kind">
        <xsl:variable name="snapshot-date">
            <xsl:call-template name="snapshot-date" />
        </xsl:variable>
        <xsl:call-template name="comfortPerson">
            <xsl:with-param name="snapshot-date" select="$snapshot-date" />
            <xsl:with-param name="bsn-nummer" select="./rubrieken/rubriek[nummer='0120']/waarde" />
            <xsl:with-param name="clazz" select='"INGESCHREVEN NATUURLIJK PERSOON"' />
        </xsl:call-template>
        <!-- cat. 09 -->
        <ouder_kind_rel>
            <fk_sc_lh_inp_sc_identif>
                <xsl:value-of select="$objectRef" />
            </fk_sc_lh_inp_sc_identif>
            <fk_sc_rh_inp_sc_identif>
                <xsl:call-template name="getHash">
                    <xsl:with-param name="bsn" select="./rubrieken/rubriek[nummer='0120']/waarde" />
                </xsl:call-template>
            </fk_sc_rh_inp_sc_identif>
            <datum_einde_fam_recht_betr><!--TODO --></datum_einde_fam_recht_betr>
            <datum_ingang_fam_recht_betr>
                <!--TODO / CHECK -->
                <xsl:value-of select="./rubrieken/rubriek[nummer='6210']/waarde" />
            </datum_ingang_fam_recht_betr>
            <ouder_aand>
                <xsl:value-of select="'KIND'" />
            </ouder_aand>
        </ouder_kind_rel>
        <!-- TODO
             <brondocument ignore-duplicates="yes">
             <tabel>OUDER_KIND_REL</tabel>
             <tabel_identificatie><xsl:value-of select="$objectRef"/></tabel_identificatie>
             <identificatie></identificatie>
             <gemeente></gemeente>
             <omschrijving></omschrijving>
             <datum></datum>
             <ref_id></ref_id>
             </brondocument>
        -->
    </xsl:template>

    <!-- maak de partner relatie en comfortdata partner/persoon aan -->
    <xsl:template name="partner">
        <xsl:variable name="snapshot-date">
            <xsl:call-template name="snapshot-date" />
        </xsl:variable>
        <xsl:call-template name="comfortPerson">
            <xsl:with-param name="snapshot-date" select="$snapshot-date" />
            <xsl:with-param name="bsn-nummer" select="./rubrieken/rubriek[nummer='0120']/waarde" />
            <xsl:with-param name="clazz" select='"NATUURLIJK PERSOON"' />
        </xsl:call-template>
        <!-- cat. 05 -->
        <huw_ger_partn>
            <fk_sc_lh_inp_sc_identif>
                <xsl:value-of select="$objectRef" />
            </fk_sc_lh_inp_sc_identif>
            <fk_sc_rh_inp_sc_identif>
                <xsl:call-template name="getHash">
                    <xsl:with-param name="bsn" select="./rubrieken/rubriek[nummer='0120']/waarde" />
                </xsl:call-template>
            </fk_sc_rh_inp_sc_identif>
            <hs_datum_aangaan>
                <!--TODO / CHECK zit in categorie 06.06.10 ??? en niet in cat 05 -->
                <xsl:value-of select="../categorie[nummer='06']/rubrieken/rubriek[nummer='0610']/waarde" />
            </hs_datum_aangaan>
        <!-- TODO dit werkt niet omdat GBA niet de iso code levert, maar naam of 4-cijfer code
        <fk_hs_lnd_code_iso><xsl:value-of select="./rubrieken/rubriek[nummer='0630']/waarde" /></fk_hs_lnd_code_iso>
        -->
        <hs_plaats>
            <xsl:value-of select="./rubrieken/rubriek[nummer='0620']/waarde" />
        </hs_plaats>
        <ho_datum_ontb_huw_ger_partn>
            <xsl:value-of select="./rubrieken/rubriek[nummer='0710']/waarde" />
        </ho_datum_ontb_huw_ger_partn>
        <!-- TODO dit werkt niet omdat GBA niet de iso code levert, maar naam of 4-cijfer code
            <fk_ho_lnd_code_iso><xsl:value-of select="./rubrieken/rubriek[nummer='0730']/waarde" /></fk_ho_lnd_code_iso>
            -->
            <ho_plaats_ontb_huw_ger_partn>
                <xsl:value-of select="./rubrieken/rubriek[nummer='0720']/waarde" />
            </ho_plaats_ontb_huw_ger_partn>
            <ho_reden_ontb_huw_ger_partn>
                <xsl:value-of select="./rubrieken/rubriek[nummer='0740']/waarde" />
            </ho_reden_ontb_huw_ger_partn>
            <soort_verbintenis>
                <xsl:value-of select="./rubrieken/rubriek[nummer='1510']/waarde" />
            </soort_verbintenis>
        </huw_ger_partn>
        <!-- TODO
             <brondocument ignore-duplicates="yes">
             <tabel>HUW_GER_PARTN</tabel>
             <tabel_identificatie><xsl:value-of select="$objectRef"/></tabel_identificatie>
             <identificatie></identificatie>
             <gemeente></gemeente>
             <omschrijving></omschrijving>
             <datum></datum>
             <ref_id></ref_id>
             </brondocument>
        -->
    </xsl:template>

    <!-- maak de tabbellen voor een persoon (subject, prs, nat_prs, ingeschr_nat_prs -->
    <xsl:template name="maakPersoon">
        <!-- sc identif NL.GBA.Persoon.[BSN HexHash] -->
        <xsl:param name="key" />
        <!-- klasse persoon bijv. 'INGESCHREVEN NATUURLIJK PERSOON' -->
        <xsl:param name="clazz" />
        <xsl:variable name="rubrieken" select="./rubrieken" />
        <subject>
            <identif>
                <xsl:value-of select="$key" />
            </identif>
            <clazz>
                <xsl:value-of select="$clazz" />
            </clazz>
            <!--
                 adres_binnenland character varying(257), - AN257 - Adres binnenland
                 adres_buitenland character varying(149), - AN149 - Adres buitenland
            -->
            <naam>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0210']/waarde" />
                <xsl:if test="not($rubrieken/rubriek[nummer='0230']/waarde)">
                    <xsl:value-of select="' '" />
                </xsl:if>
                <xsl:if test="$rubrieken/rubriek[nummer='0230']/waarde != ''">
                    <xsl:value-of select="' '" />
                </xsl:if>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0230']/waarde" />
                <xsl:if test="$rubrieken/rubriek[nummer='0230']/waarde != ''">
                    <xsl:value-of select="' '" />
                </xsl:if>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0240']/waarde" />
            </naam>
            <typering>
                <xsl:value-of select="substring($clazz,1,50)" />
            </typering>
            <!--
                 telefoonnummer character varying(20), - AN20 - Telefoonnummer
                 website_url character varying(200), - AN200 - Website-URL
                 fk_13wpl_identif character varying(4), - [FK] AN4, FK naar wnplts.identif: "heeft als correspondentieadres"
                 fk_14aoa_identif character varying(16), - [FK] AN16, FK naar addresseerb_obj_aand.identif: "heeft als factuuradres"
                 fk_15aoa_identif character varying(16), - [FK] AN16, FK naar addresseerb_obj_aand.identif: "heeft als correspondentieadres"
                 pa_postadres_postcode character varying(6), - Groepsattribuut Postadres SUBJECT.Postadres postcode - Postadres postcode
                 pa_postadrestype character varying(1), - Groepsattribuut Postadres SUBJECT.Postadrestype - Postadrestype
                 pa_postbus__of_antwoordnummer numeric(5,0), - Groepsattribuut Postadres SUBJECT.Postbus- of antwoordnummer - Postbus- of antwoordnummer
                 fk_pa_4_wpl_identif character varying(4), - [FK] AN4, FK naar wnplts.identif: "Groepsattribuut Postadres SUBJECT.woonplaats"
                 rn_bankrekeningnummer numeric(10,0), - Groepsattribuut Rekeningnummer SUBJECT.Bankrekeningnummer - Bankrekeningnummer
                 rn_bic character varying(11), - Groepsattribuut Rekeningnummer SUBJECT.BIC - BIC
                 rn_iban character varying(34), - Groepsattribuut Rekeningnummer SUBJECT.IBAN - IBAN
                 vb_adres_buitenland_1 character varying(35), - Groepsattribuut Verblijf buitenland SUBJECT.Adres buitenland 1 - Adres buitenland 1
                 vb_adres_buitenland_2 character varying(35), - Groepsattribuut Verblijf buitenland SUBJECT.Adres buitenland 2 - Adres buitenland 2
                 vb_adres_buitenland_3 character varying(35), - Groepsattribuut Verblijf buitenland SUBJECT.Adres buitenland 3 - Adres buitenland 3
                 fk_vb_lnd_code_iso character varying(2), - [FK] A2, FK naar land.code_iso: "Groepsattribuut referentielijst Land verblijfadres"
            -->
        </subject>
        <prs>
            <sc_identif>
                <xsl:value-of select="$key" />
            </sc_identif>
            <clazz>
                <xsl:value-of select="$clazz" />
            </clazz>
        </prs>
        <nat_prs>
            <sc_identif>
                <xsl:value-of select="$key" />
            </sc_identif>
            <clazz>
                <xsl:value-of select="$clazz" />
            </clazz>
            <aand_naamgebruik>
                <xsl:value-of select="$rubrieken/rubriek[nummer='6110']/waarde" />
            </aand_naamgebruik>
            <geslachtsaand>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0410']/waarde" />
            </geslachtsaand>
            <nm_adellijke_titel_predikaat>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0220']/waarde" />
            </nm_adellijke_titel_predikaat>
            <nm_geslachtsnaam>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0240']/waarde" />
            </nm_geslachtsnaam>
            <nm_voornamen>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0210']/waarde" />
            </nm_voornamen>
            <nm_voorvoegsel_geslachtsnaam>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0230']/waarde" />
            </nm_voorvoegsel_geslachtsnaam>
            <!--
                 na_aanhef_aanschrijving character varying(50), - Groepsattribuut Naam aanschrijving NATUURLIJK PERSOON.Aanhef aanschrijving - Aanhef aanschrijving
                 na_geslachtsnaam_aanschrijving character varying(200), - Groepsattribuut Naam aanschrijving NATUURLIJK PERSOON.Geslachtsnaam aanschrijving - Geslachtsnaam aanschrijving
                 na_voorletters_aanschrijving character varying(20), - Groepsattribuut Naam aanschrijving NATUURLIJK PERSOON.Voorletters aanschrijving - Voorletters aanschrijving
                 na_voornamen_aanschrijving character varying(200), - Groepsattribuut Naam aanschrijving NATUURLIJK PERSOON.Voornamen aanschrijving - Voornamen aanschrijving
                 fk_2acd_code character varying(3), - [FK] AN3, FK naar academische_titel.code: "Referentielijst NATUURLIJK PERSOON.Academische titel"
            -->
        </nat_prs>
        <ingeschr_nat_prs>
            <sc_identif>
                <xsl:value-of select="$key" />
            </sc_identif>
            <clazz>
                <xsl:value-of select="$clazz" />
            </clazz>
            <a_nummer>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0110']/waarde" />
            </a_nummer>
            <xsl:if test="$key = $objectRef">
                <!-- alleen voor authentieke persoon, niet voor comfort data persoon -->

                <!-- in categorie 12-->
                <btnlnds_rsdoc>
                    <xsl:value-of select="../categorie[nummer='12']/rubrieken/rubriek[nummer='3710']/waarde" />
                </btnlnds_rsdoc>
                <signalering_rsdoc>
                    <xsl:value-of select="../categorie[nummer='12']/rubrieken/rubriek[nummer='3610']/waarde" />
                </signalering_rsdoc>

                <!-- in categorie 06 -->
                <!-- TODO dit werkt niet omdat GBA niet de iso code levert, maar naam of 4-cijfer code
                <fk_ol_lnd_code_iso><xsl:value-of select="$rubrieken/rubriek[nummer='0830']/waarde" /></fk_ol_lnd_code_iso>
                -->
                <ol_overlijdensdatum>
                    <xsl:value-of select="$rubrieken/rubriek[nummer='0810']/waarde" />
                </ol_overlijdensdatum>
                <ol_overlijdensplaats>
                    <xsl:value-of select="$rubrieken/rubriek[nummer='0810']/waarde" />
                </ol_overlijdensplaats>

                <!-- in categorie 07 -->
                <indic_geheim>
                    <xsl:value-of select="../categorie[nummer='07']/rubrieken/rubriek[nummer='7010']/waarde" />
                </indic_geheim>
                <reden_opschorting_bijhouding>
                    <xsl:value-of select="../categorie[nummer='07']/rubrieken/rubriek[nummer='6720']/waarde" />
                </reden_opschorting_bijhouding>

                <!-- in categorie 08
                     08.14.10 fk_17lnd_code_iso character varying(2), - [FK] A2, FK naar land.code_iso: "Referentielijst INGESCHREVEN NATUURLIJK PERSOON.Land vanwaar ingeschreven"
                     08.13.10 fk_18lnd_code_iso character varying(2), - [FK] A2, FK naar land.code_iso: "Referentielijst INGESCHREVEN NATUURLIJK PERSOON.Land waarnaar vertrokken"
                -->
                <!-- TODO dit werkt niet omdat GBA niet de iso code levert, maar naam of 4-cijfer code
                <fk_17lnd_code_iso><xsl:value-of select="../categorie[nummer='08']/rubrieken/rubriek[nummer='1410']/waarde" /></fk_17lnd_code_iso>
                <fk_18lnd_code_iso><xsl:value-of select="../categorie[nummer='08']/rubrieken/rubriek[nummer='1310']/waarde" /></fk_18lnd_code_iso>
                -->
                <gemeente_van_inschrijving>
                    <xsl:value-of select="../categorie[nummer='08']/rubrieken/rubriek[nummer='0910']/waarde" />
                </gemeente_van_inschrijving>
            </xsl:if>
            <!--
                 burgerlijke_staat numeric(1,0), - N1 - Burgerlijke staat
            -->
            <bsn>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0120']/waarde" />
            </bsn>
            <!--
                 dat_beg_geldh_verblijfpl character varying(19), - OnvolledigeDatum - Datum begin geldigheid verblijfplaats
                 datum_inschrijving_in_gemeente character varying(19), - OnvolledigeDatum - Datum inschrijving in gemeente
                 67.10 datum_opschorting_bijhouding character varying(19), - OnvolledigeDatum - Datum opschorting bijhouding
                 datum_verkr_nation character varying(19), - OnvolledigeDatum - Datum verkrijging nationaliteit
                 datum_verlies_nation character varying(19), - OnvolledigeDatum - Datum verlies nationaliteit
                 datum_vertrek_uit_nederland character varying(19), - OnvolledigeDatum - Datum vertrek uit Nederland
                 datum_vestg_in_nederland character varying(19), - OnvolledigeDatum - Datum vestiging in Nederland
                 fk_27lpl_sc_identif character varying(16), - [FK] AN16, FK naar ligplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "verblijft op"
                 fk_28nra_sc_identif character varying(16), - [FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "is ingeschreven op"
                 fk_29wpl_identif character varying(4), - [FK] AN4, FK naar wnplts.identif: "verblijft op locatie in"
                 fk_30spl_sc_identif character varying(16), - [FK] AN16, FK naar standplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "verblijft op"
                 fk_31vbo_sc_identif character varying(16), - [FK] AN16, FK naar verblijfsobj.sc_identif (is FK naar superclass BENOEMD OBJECT): "verblijft in"
                 fk_1rsd_nummer character varying(9), - [FK] AN9, FK naar rsdoc.nummer
            -->
            <gb_geboortedatum>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0306']/waarde" />
            </gb_geboortedatum>
            <!-- TODO dit werkt niet omdat GBA niet de iso code levert, maar naam of 4-cijfer code
            <fk_gb_lnd_code_iso><xsl:value-of select="$rubrieken/rubriek[nummer='0330']/waarde" /></fk_gb_lnd_code_iso>
            -->
            <gb_geboorteplaats>
                <!-- waarde is de plaats code met voorloop-0, dus neem omschrijving en clip naar varchar40 -->
                <xsl:value-of select="substring($rubrieken/rubriek[nummer='0320']/omschrijving,1,40)" />
            </gb_geboorteplaats>
            <!--
                 nt_aand_bijzonder_nlschap character varying(1), - Groepsattribuut Nederlandse nationaliteit INGESCHREVEN NATUURLIJK PERSOON.Aanduiding bijzonder Nederlanderschap - Aanduiding bijzonder Nederlanderschap
                 fk_nt_nat_code numeric(4,0), - [FK] N4, FK naar nation.code: "Groepsattribuut referentielijst Nationaliteit"
                 nt_reden_verkr_nlse_nation numeric(3,0), - Groepsattribuut Nederlandse nationaliteit INGESCHREVEN NATUURLIJK PERSOON.Reden verkrijging Nederlandse nationaliteit - Reden verkrijging Nederlandse nationaliteit
                 nt_reden_verlies_nlse_nation numeric(3,0), - Groepsattribuut Nederlandse nationaliteit INGESCHREVEN NATUURLIJK PERSOON.Reden verlies Nederlandse nationaliteit - Reden verlies Nederlandse nationaliteit

                 va_adresherkomst character varying(1), - Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.Adresherkomst - Adresherkomst
                 va_loc_beschrijving character varying(255), - Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.Locatie beschrijving - Locatie beschrijving
                 fk_va_3_vbo_sc_identif character varying(16), - [FK] AN16, FK naar verblijfsobj.sc_identif (is FK naar superclass BENOEMD OBJECT): "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.verblijfsobject"
                 fk_va_4_spl_sc_identif character varying(16), - [FK] AN16, FK naar standplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.standplaats"
                 fk_va_5_nra_sc_identif character varying(16), - [FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.nummeraanduiding"
                 fk_va_6_wpl_identif character varying(4), - [FK] AN4, FK naar wnplts.identif: "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.woonplaats"
                 fk_va_7_lpl_sc_identif character varying(16), - [FK] AN16, FK naar ligplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.ligplaats"
                 fk_3nat_code numeric(4,0), - [FK] N4, FK naar nation.code: "Referentielijst INGESCHREVEN NATUURLIJK PERSOON.Buitenlandse nationaliteit"
            -->
        </ingeschr_nat_prs>
    </xsl:template>

    <!-- zoek hash op in mapping tabel -->
    <xsl:template name="getHash">
        <xsl:param name="bsn" />
        <xsl:variable name="bsnwithprefix">
            <xsl:value-of select="$PREFIX" />
            <xsl:value-of select="$bsn" />
        </xsl:variable>
        <xsl:variable name="hashedbsn">
            <xsl:value-of select="$PREFIX" />
            <xsl:value-of select="/root/bsnhashes/*[name()=$bsnwithprefix]" />
        </xsl:variable>
        <xsl:value-of select="$hashedbsn" />
    </xsl:template>

    <!-- parse datum uit 'Datum document' rubriek, bij gebrek de bericht datum gebruiken -->
    <xsl:template name="snapshot-date">
        <xsl:choose>
            <xsl:when test="./rubrieken/rubriek[nummer='8220']/waarde != ''">
                <xsl:call-template name="date-numeric">
                    <xsl:with-param name="strDate" select="./rubrieken/rubriek[nummer='8220']/waarde" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="date-numeric">
                    <xsl:with-param name="strDate" select="$datum" />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Format datum,  jjjjmmdd etc. -> jjjj-mm-dd -->
    <xsl:template name="date-numeric">
        <xsl:param name="strDate" />
        <xsl:choose>
            <xsl:when test="contains($strDate,'-')">
                <!-- veronderstel jjjj-mm-dd hh:mm-->
                <xsl:value-of select="substring($strDate,1,(4+1+2+1+2))" />
            </xsl:when>
            <xsl:when test="string-length($strDate) &gt; 9">
                <xsl:value-of select="concat(substring($strDate,1,4),'-',substring($strDate,5,2),'-',substring($strDate,7,2))" />
            </xsl:when>
            <xsl:when test="string-length($strDate) &gt; 7">
                <xsl:value-of select="concat(substring($strDate,1,4),'-',substring($strDate,5,2),'-01')" />
            </xsl:when>
            <xsl:when test="string-length($strDate) &gt; 5">
                <xsl:value-of select="concat(substring($strDate,1,4),'-01-01')" />
            </xsl:when>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>