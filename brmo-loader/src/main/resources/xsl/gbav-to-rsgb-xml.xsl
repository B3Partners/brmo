<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                >

    <xsl:output method="xml" indent="yes" omit-xml-declaration="no"/>

    <!-- parameters van het bericht -->
    <xsl:param name="objectRef" select="'NL.GBA.Persoon:onbekend'"/>
    <xsl:param name="datum" select="'datum-onbekend'"/>
    <xsl:param name="volgordeNummer" select="'volgordeNummer-onbekend'"/>
    <xsl:param name="soort" select="'soort-onbekend'"/>
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
                <xsl:apply-templates select="*"/>
            </data>
        </root>
    </xsl:template>

    <xsl:template name="ouder1" >
        <!--<ouder1></ouder1>-->
    </xsl:template>

    <xsl:template name="ouder2" >
        <!--<ouder2></ouder2>-->
    </xsl:template>

    <xsl:template name="kind" >
        <!--<kind></kind>-->
    </xsl:template>

    <xsl:template name="partner" >
        <partner></partner>
    </xsl:template>
   
    <xsl:template name="persoon" match="persoon">
        <xsl:for-each select="./categorieen/categorie">
            <xsl:comment>
                <xsl:text>categorie naam: </xsl:text>
                <xsl:value-of select="./naam"/>
                <xsl:text> categorie nummer: </xsl:text>
                <xsl:value-of select="./nummer"/>
            </xsl:comment>
            <xsl:choose>
                <xsl:when test="./nummer = '01'">
                    <xsl:call-template name="ingeschrevenpersoon" />
                </xsl:when>
                <xsl:when test="./nummer = '02'">
                    <xsl:call-template name="ouder1" />
                </xsl:when>
                <xsl:when test="./nummer = '03'">
                    <xsl:call-template name="ouder2" />
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

    <xsl:template name="ingeschrevenpersoon">
        <!--/persoon/categorieen/categorie/nummer == 01-->
        <xsl:variable name="clazz">INGESCHREVEN NATUURLIJK PERSOON</xsl:variable>
        <!--<xsl:variable name="rubrieken" select="./categorieen/categorie/rubrieken" />-->
        <xsl:variable name="rubrieken" select="./rubrieken" />

        <subject>
            <identif>
                <xsl:value-of select="$objectRef"/>
            </identif>
            <clazz>
                <xsl:value-of select="$clazz"/>
            </clazz>
            <!--
            adres_binnenland character varying(257), - AN257 - Adres binnenland
            adres_buitenland character varying(149), - AN149 - Adres buitenland
            emailadres character varying(254), - AN254 - Emailadres
            fax_nummer character varying(20), - AN20 - Fax-nummer
            kvk_nummer numeric(8,0), - N8 - KvK-nummer
            -->
            <naam>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0210']/waarde"/>
                <xsl:if test="not($rubrieken/rubriek[nummer='0230']/waarde)">
                    <xsl:value-of select="' '"/>
                </xsl:if>
                <xsl:if test="$rubrieken/rubriek[nummer='0230']/waarde != ''">
                    <xsl:value-of select="' '"/>
                </xsl:if>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0230']/waarde"/>
                <xsl:if test="$rubrieken/rubriek[nummer='0230']/waarde != ''">
                    <xsl:value-of select="' '"/>
                </xsl:if>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0240']/waarde"/>
            </naam>
            <typering>
                <xsl:value-of select="substring($clazz,1,50)"/>
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
                <xsl:value-of select="$objectRef"/>
            </sc_identif>
            <clazz>
                <xsl:value-of select="$clazz"/>
            </clazz>
        </prs>

        <nat_prs>
            <sc_identif>
                <xsl:value-of select="$objectRef"/>
            </sc_identif>
            <clazz>
                <xsl:value-of select="$clazz"/>
            </clazz>
            <aand_naamgebruik>
                <xsl:value-of select="$rubrieken/rubriek[nummer='6110']/waarde"/>
            </aand_naamgebruik>
            <geslachtsaand>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0410']/waarde"/>
            </geslachtsaand>
            <nm_adellijke_titel_predikaat>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0220']/waarde"/>
            </nm_adellijke_titel_predikaat>
            <nm_geslachtsnaam>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0240']/waarde"/>
            </nm_geslachtsnaam>
            <nm_voornamen>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0210']/waarde"/>
            </nm_voornamen>
            <nm_voorvoegsel_geslachtsnaam>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0230']/waarde"/>
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
                <xsl:value-of select="$objectRef"/>
                <!-- <xsl:call-template name="nen_identificatie">
                    <xsl:with-param name="id" select="$rubrieken/rubriek[nummer='0120']/waarde"/>
                </xsl:call-template> -->
            </sc_identif>
            <clazz>
                <xsl:value-of select="$clazz"/>
            </clazz>
            <a_nummer>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0110']/waarde"/>
            </a_nummer>
            <!--
            btnlnds_rsdoc numeric(1,0), - N1 - Buitenlands reisdocument
            burgerlijke_staat numeric(1,0), - N1 - Burgerlijke staat
            -->
            <bsn>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0120']/waarde"/>
            </bsn>
            <!--
            dat_beg_geldh_verblijfpl character varying(19), - OnvolledigeDatum - Datum begin geldigheid verblijfplaats
            datum_inschrijving_in_gemeente character varying(19), - OnvolledigeDatum - Datum inschrijving in gemeente
            datum_opschorting_bijhouding character varying(19), - OnvolledigeDatum - Datum opschorting bijhouding
            datum_verkr_nation character varying(19), - OnvolledigeDatum - Datum verkrijging nationaliteit
            datum_verlies_nation character varying(19), - OnvolledigeDatum - Datum verlies nationaliteit
            datum_vertrek_uit_nederland character varying(19), - OnvolledigeDatum - Datum vertrek uit Nederland
            datum_vestg_in_nederland character varying(19), - OnvolledigeDatum - Datum vestiging in Nederland
            gemeente_van_inschrijving numeric(4,0), - N4 - Gemeente van inschrijving
            handelingsbekwaam character varying(3), - AN3 - Handelingsbekwaam
            indic_geheim numeric(1,0), - N1 - Indicatie geheim
            rechtstoestand character varying(22), - AN22 - Rechtstoestand
            reden_opschorting_bijhouding character varying(1), - AN1 - Reden opschorting bijhouding
            signalering_rsdoc numeric(1,0), - N1 - Signalering reisdocument
            fk_27lpl_sc_identif character varying(16), - [FK] AN16, FK naar ligplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "verblijft op"
            fk_28nra_sc_identif character varying(16), - [FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "is ingeschreven op"
            fk_29wpl_identif character varying(4), - [FK] AN4, FK naar wnplts.identif: "verblijft op locatie in"
            fk_30spl_sc_identif character varying(16), - [FK] AN16, FK naar standplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "verblijft op"
            fk_31vbo_sc_identif character varying(16), - [FK] AN16, FK naar verblijfsobj.sc_identif (is FK naar superclass BENOEMD OBJECT): "verblijft in"
            fk_1rsd_nummer character varying(9), - [FK] AN9, FK naar rsdoc.nummer
            -->
            <gb_geboortedatum>
                <xsl:value-of select="$rubrieken/rubriek[nummer='0310']/waarde"/>
            </gb_geboortedatum>
            <!-- fk_gb_lnd_code_iso character varying(2), - [FK] A2, FK naar land.code_iso: "Groepsattribuut referentielijst Geboorteland" -->
            <gb_geboorteplaats>
                <!-- waarde is de plaats code met voorloop-0, varchar40 -->
                <xsl:value-of select="substring($rubrieken/rubriek[nummer='0320']/omschrijving,1,40)"/>
            </gb_geboorteplaats>
            <!--
            nt_aand_bijzonder_nlschap character varying(1), - Groepsattribuut Nederlandse nationaliteit INGESCHREVEN NATUURLIJK PERSOON.Aanduiding bijzonder Nederlanderschap - Aanduiding bijzonder Nederlanderschap
            fk_nt_nat_code numeric(4,0), - [FK] N4, FK naar nation.code: "Groepsattribuut referentielijst Nationaliteit"
            nt_reden_verkr_nlse_nation numeric(3,0), - Groepsattribuut Nederlandse nationaliteit INGESCHREVEN NATUURLIJK PERSOON.Reden verkrijging Nederlandse nationaliteit - Reden verkrijging Nederlandse nationaliteit
            nt_reden_verlies_nlse_nation numeric(3,0), - Groepsattribuut Nederlandse nationaliteit INGESCHREVEN NATUURLIJK PERSOON.Reden verlies Nederlandse nationaliteit - Reden verlies Nederlandse nationaliteit
            fk_ol_lnd_code_iso character varying(2), - [FK] A2, FK naar land.code_iso: "Groepsattribuut referentielijst Land overlijden"
            -->
            <ol_overlijdensdatum>
                <!--  <xsl:value-of select="$rubrieken/rubriek[nummer='XXXX']/omschrijving"/> -->
            </ol_overlijdensdatum>
            <!--
            ol_overlijdensplaats character varying(40), - Groepsattribuut Overlijden INGESCHREVEN NATUURLIJK PERSOON.Overlijdensplaats - Overlijdensplaats
            va_adresherkomst character varying(1), - Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.Adresherkomst - Adresherkomst
            va_loc_beschrijving character varying(255), - Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.Locatie beschrijving - Locatie beschrijving
            fk_va_3_vbo_sc_identif character varying(16), - [FK] AN16, FK naar verblijfsobj.sc_identif (is FK naar superclass BENOEMD OBJECT): "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.verblijfsobject"
            fk_va_4_spl_sc_identif character varying(16), - [FK] AN16, FK naar standplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.standplaats"
            fk_va_5_nra_sc_identif character varying(16), - [FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.nummeraanduiding"
            fk_va_6_wpl_identif character varying(4), - [FK] AN4, FK naar wnplts.identif: "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.woonplaats"
            fk_va_7_lpl_sc_identif character varying(16), - [FK] AN16, FK naar ligplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.ligplaats"
            fk_3nat_code numeric(4,0), - [FK] N4, FK naar nation.code: "Referentielijst INGESCHREVEN NATUURLIJK PERSOON.Buitenlandse nationaliteit"
            fk_17lnd_code_iso character varying(2), - [FK] A2, FK naar land.code_iso: "Referentielijst INGESCHREVEN NATUURLIJK PERSOON.Land vanwaar ingeschreven"
            fk_18lnd_code_iso character varying(2), - [FK] A2, FK naar land.code_iso: "Referentielijst INGESCHREVEN NATUURLIJK PERSOON.Land waarnaar vertrokken"
            -->
        </ingeschr_nat_prs>
        
    </xsl:template>

    <xsl:template name="nen_identificatie">
        <xsl:param name="id"/>
        <xsl:text>NL.GBA.Persoon:</xsl:text>
        <xsl:value-of select="$id"/>
    </xsl:template>

</xsl:stylesheet>
