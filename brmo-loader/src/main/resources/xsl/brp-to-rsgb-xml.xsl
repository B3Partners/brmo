<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                 xmlns:ns1="http://www.egem.nl/StUF/StUF0204" 
                 xmlns:ns2="http://www.egem.nl/StUF/sector/bg/0204">
    <xsl:output method="xml" indent="yes"/>
 
                
 	<!-- parameters van het bericht -->
    <xsl:param name="objectRef" />
    <xsl:param name="datum" />
    <xsl:param name="volgordeNummer" />
    <xsl:param name="soort" />
	
    <xsl:param name="rsgb-version" select="2.2"/>
    <xsl:template match="/root">
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
                <xsl:apply-templates select="ns2:PRS"/>                
            </data>
        </root>
    </xsl:template>

	<xsl:template match="ns2:PRS">
		<xsl:variable name="objRef"><xsl:value-of select="$objectRef"/></xsl:variable>
		<xsl:variable name="class">NATUURLIJK PERSOON</xsl:variable>
		<xsl:call-template name="persoon">
            <xsl:with-param name="key" select="$objRef"/>
            <xsl:with-param name="class" select="$class"/>
        </xsl:call-template>

        <xsl:apply-templates select="ns2:PRSIDB"/>
        <xsl:apply-templates select="ns2:PRSPRSHUW"/>
        <xsl:apply-templates select="ns2:PRSPRSKND"/>
        <xsl:apply-templates select="ns2:PRSPRSOUD"/>
    </xsl:template>

    <xsl:template name="persoon">
        <xsl:param name="key"/>
        <xsl:param name="class"/>

        <subject>
            <identif><xsl:value-of select="$key"/></identif>
            <clazz><xsl:value-of select="$class"/></clazz>
            <typering><xsl:value-of select="substring($class,1,35)"/></typering>
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
        <nat_prs>
            <sc_identif>
                <xsl:value-of select="$key"/>
            </sc_identif>
            <clazz>
                <xsl:value-of select="$class"/>
            </clazz>

            <aand_naamgebruik><xsl:value-of select="ns2:aanduidingNaamgebruik"/></aand_naamgebruik>
            <geslachtsaand><xsl:value-of select="ns2:geslachtsaanduiding"/></geslachtsaand>
            <nm_adellijke_titel_predikaat><xsl:value-of select="ns2:adellijkeTitelPredikaat"/></nm_adellijke_titel_predikaat>
            <nm_geslachtsnaam><xsl:value-of select="ns2:geslachtsnaam"/></nm_geslachtsnaam>
            <nm_voornamen><xsl:value-of select="ns2:voornamen"/></nm_voornamen>
            <nm_voorvoegsel_geslachtsnaam><xsl:value-of select="ns2:voorvoegselGeslachtsnaam"/></nm_voorvoegsel_geslachtsnaam>
            <na_voorletters_aanschrijving><xsl:value-of select="ns2:voorletters"/></na_voorletters_aanschrijving>
        </nat_prs>
        <ingeschr_nat_prs>
            <sc_identif>
                <xsl:value-of select="$key"/>
            </sc_identif>

            <clazz>
                <xsl:value-of select="$class"/>
            </clazz>
            <bsn><xsl:value-of select="ns2:bsn-nummer"/></bsn>
            <a_nummer><xsl:value-of select="ns2:a-nummer"/></a_nummer>
            <gb_geboortedatum><xsl:value-of select="ns2:geboortedatum"/></gb_geboortedatum>
            <gb_geboorteplaats><xsl:value-of select="ns2:geboorteplaats"/></gb_geboorteplaats>
            <!--fk_gb_lnd_code_iso><xsl:value-of select="ns2:codeGeboorteland"/></fk_gb_lnd_code_iso-->
            <ol_overlijdensdatum><xsl:value-of select="ns2:datumOverlijden"/></ol_overlijdensdatum>
            <ol_overlijdensplaats><xsl:value-of select="ns2:plaatsOverlijden"/></ol_overlijdensplaats>
            <!--fk_ol_lnd_code_iso><xsl:value-of select="ns2:codeLandOverlijden"/></fk_ol_lnd_code_iso-->
        </ingeschr_nat_prs>
    </xsl:template>

    <xsl:template name="subject">
        <naam>
            <xsl:value-of select="ns2:voorletters"/>
            <xsl:if test="ns2:voorvoegselGeslachtsnaam != ''"><xsl:value-of select="' '"/></xsl:if><xsl:value-of select="ns2:voorvoegselGeslachtsnaam"/>
            <xsl:if test="ns2:voorvoegselGeslachtsnaam != ''"><xsl:value-of select="' '"/></xsl:if>
            <xsl:value-of select="ns2:geslachtsnaam"/>
        </naam>
        <xsl:apply-templates select="ns2:PRSADRVBL"/>
    </xsl:template>

    <xsl:template name="comfortPerson">
        <xsl:param name="snapshot-date"/>
        <xsl:variable name="class">NATUURLIJK PERSOON</xsl:variable>
        <xsl:variable name="searchcol">
            <xsl:call-template name="getHash"><xsl:with-param name="bsn" select="ns2:PRS/ns2:bsn-nummer"/></xsl:call-template>
        </xsl:variable>
        <xsl:variable name="datum">
            <xsl:value-of select="substring($snapshot-date,0,5)"/><xsl:value-of select="'-'"/>
            <xsl:value-of select="substring($snapshot-date,5,2)"/><xsl:value-of select="'-'"/>
            <xsl:value-of select="substring($snapshot-date,7)"/>
        </xsl:variable>

        <comfort search-table="subject" search-column="identif" search-value="{$searchcol}" snapshot-date="{$datum}">
            <xsl:for-each select="ns2:PRS">
                <xsl:call-template name="persoon" >
                    <xsl:with-param name="key" select="$searchcol"/>
                    <xsl:with-param name="class" select="$class"/>
                </xsl:call-template>
            </xsl:for-each>
        </comfort>
    </xsl:template>

    <xsl:template match="ns2:PRSPRSKND">
        <xsl:call-template name="comfortPerson"><xsl:with-param name="snapshot-date" select="ns2:ingangsdatum"/></xsl:call-template>
        <ouder_kind_rel>
            <fk_sc_lh_inp_sc_identif><xsl:value-of select="$objectRef"/></fk_sc_lh_inp_sc_identif>
            <fk_sc_rh_inp_sc_identif><xsl:call-template name="getHash"><xsl:with-param name="bsn" select="ns2:PRS/ns2:bsn-nummer"/></xsl:call-template></fk_sc_rh_inp_sc_identif>
            <datum_einde_fam_recht_betr><xsl:value-of select="ns2:ingangsdatum"/></datum_einde_fam_recht_betr>
            <datum_ingang_fam_recht_betr><xsl:value-of select="ns2:einddatum"/></datum_ingang_fam_recht_betr>
            <ouder_aand><xsl:value-of select="'KIND'"/></ouder_aand>
        </ouder_kind_rel>
    </xsl:template>

    <xsl:template match="ns2:PRSPRSOUD">
        <xsl:call-template name="comfortPerson"><xsl:with-param name="snapshot-date" select="ns2:ingangsdatum"/></xsl:call-template>
        <ouder_kind_rel>
            <fk_sc_lh_inp_sc_identif><xsl:value-of select="$objectRef"/></fk_sc_lh_inp_sc_identif>
            <fk_sc_rh_inp_sc_identif><xsl:call-template name="getHash"><xsl:with-param name="bsn" 
                select="ns2:PRS/ns2:bsn-nummer"/></xsl:call-template></fk_sc_rh_inp_sc_identif>
            <datum_einde_fam_recht_betr><xsl:value-of select="ns2:ingangsdatum"/></datum_einde_fam_recht_betr>
            <datum_ingang_fam_recht_betr><xsl:value-of select="ns2:einddatum"/></datum_ingang_fam_recht_betr>
            <ouder_aand><xsl:value-of select="'OUDER'"/></ouder_aand>
        </ouder_kind_rel>
    </xsl:template>

    <xsl:template match="ns2:PRSPRSHUW">
        <xsl:call-template name="comfortPerson"><xsl:with-param name="snapshot-date" select="ns2:datumSluiting"/></xsl:call-template>
        <huw_ger_partn>
            <fk_sc_lh_inp_sc_identif><xsl:value-of select="$objectRef"/></fk_sc_lh_inp_sc_identif>
            <fk_sc_rh_inp_sc_identif><xsl:call-template name="getHash"><xsl:with-param name="bsn" select="ns2:PRS/ns2:bsn-nummer"/></xsl:call-template></fk_sc_rh_inp_sc_identif>
            <hs_datum_aangaan><xsl:value-of select="ns2:datumSluiting"/></hs_datum_aangaan>
            <!--fk_hs_lnd_code_iso><xsl:value-of select="ns2:landSluiting"/></fk_hs_lnd_code_iso-->
            <hs_plaats><xsl:value-of select="ns2:plaatsSluiting"/></hs_plaats>
            <ho_datum_ontb_huw_ger_partn><xsl:value-of select="ns2:datumOntbinding"/></ho_datum_ontb_huw_ger_partn>
            <!--fk_ho_lnd_code_iso><xsl:value-of select="ns2:landOntbinding"/></fk_ho_lnd_code_iso-->
            <ho_plaats_ontb_huw_ger_partn><xsl:value-of select="ns2:plaatsOntbinding"/></ho_plaats_ontb_huw_ger_partn>
            <ho_reden_ontb_huw_ger_partn><xsl:value-of select="ns2:redenOntbinding"/></ho_reden_ontb_huw_ger_partn>
            <soort_verbintenis><xsl:value-of select="ns2:soortVerbintenis"/></soort_verbintenis>
        </huw_ger_partn>

    </xsl:template>


    <xsl:template match="ns2:PRSIDB">  
        <rsdoc>
            <nummer><xsl:value-of select="ns2:nummerIdentiteitsbewijs"/></nummer>
            <aand_inhouding_of_vermissing><xsl:value-of select="ns2:extraElementen/ns1:extraElement[@naam='aanduidingInhoudingVermissing']"/></aand_inhouding_of_vermissing>
            <datum_inhouding_of_vermissing><xsl:value-of select="ns2:extraElementen/ns1:extraElement[@naam='datumInhoudingVermissing']"/></datum_inhouding_of_vermissing>
            <datum_uitgifte><xsl:value-of select="ns2:extraElementen/ns1:extraElement[@naam='datumAfgifte']"/></datum_uitgifte>
            <eindd_geldh_document><xsl:value-of select="ns2:extraElementen/ns1:extraElement[@naam='datumEindeGeldigheid']"/></eindd_geldh_document>
            <fk_7rds_rsdoccode><xsl:value-of select="ns2:SIB/ns2:soort"/></fk_7rds_rsdoccode>
        </rsdoc>
        <rsdoc_ingeschr_nat_prs>
            <fk_nn_lh_rsd_nummer><xsl:value-of select="ns2:nummerIdentiteitsbewijs"/></fk_nn_lh_rsd_nummer>
            <fk_nn_rh_inp_sc_identif><xsl:value-of select="$objectRef"/></fk_nn_rh_inp_sc_identif>
        </rsdoc_ingeschr_nat_prs>
    </xsl:template>

    <xsl:template match="ns2:PRSADRVBL">
        <adres_binnenland>
            <xsl:value-of select="ns2:ADR/ns2:straatnaam"/><xsl:value-of select="' '"/>
            <xsl:value-of select="ns2:ADR/ns2:huisnummer"/>
            <xsl:if test="ns2:ADR/ns2:huisletter != ''"><xsl:value-of select="' '"/></xsl:if>
            <xsl:value-of select="ns2:ADR/ns2:huisletter"/>
            <xsl:if test="ns2:ADR/ns2:huisnummertoevoeging != ''"><xsl:value-of select="' '"/></xsl:if>
            <xsl:value-of select="ns2:ADR/ns2:huisnummertoevoeging"/>
            <xsl:if test="ns2:ADR/ns2:aanduidingBijHuisnummer != ''"><xsl:value-of select="' '"/></xsl:if>
            <xsl:value-of select="ns2:ADR/ns2:aanduidingBijHuisnummer"/>
            <xsl:value-of select="' '"/>
            <xsl:value-of select="ns2:ADR/ns2:postcode"/>
            <xsl:value-of select="' '"/>
            <xsl:value-of select="ns2:ADR/ns2:woonplaatsnaam"/>
        </adres_binnenland>
        <adres_buitenland><xsl:value-of select="ns2:ADR/ns2:adresBuitenland1"/><xsl:if test="ns2:ADR/ns2:adresBuitenland2 != ''">,</xsl:if><xsl:value-of select="ns2:ADR/ns2:adresBuitenland2"/><xsl:if test="ns2:ADR/ns2:adresBuitenland3 != ''">,</xsl:if><xsl:value-of select="ns2:ADR/ns2:adresBuitenland3"/></adres_buitenland>

        <pa_postadres_postcode><xsl:value-of select="ns2:ADR/ns2:postcode"/></pa_postadres_postcode>
        <pa_postbus__of_antwoordnummer><xsl:value-of select="ns2:ADR/ns2:postbusnummer"/><xsl:if test="ns2:ADR/ns2:antwoordnummer != ''"> </xsl:if><xsl:value-of select="ns2:ADR/ns2:antwoordnummer"/></pa_postbus__of_antwoordnummer>
        <!--fk_vb_lnd_code_iso><xsl:value-of select="ns2:ADR/ns2:landcode"/></fk_vb_lnd_code_iso-->

        <vb_adres_buitenland_1><xsl:value-of select="ns2:ADR/ns2:adresBuitenland1"/></vb_adres_buitenland_1>
        <vb_adres_buitenland_2><xsl:value-of select="ns2:ADR/ns2:adresBuitenland2"/></vb_adres_buitenland_2>
        <vb_adres_buitenland_3><xsl:value-of select="ns2:ADR/ns2:adresBuitenland3"/></vb_adres_buitenland_3>        
        
        <fk_14aoa_identif><xsl:value-of select="ns2:ADR/ns2:extraElementen/ns1:extraElement[@naam='identificatieAOA']"/></fk_14aoa_identif>
        <fk_15aoa_identif><xsl:value-of select="ns2:ADR/ns2:extraElementen/ns1:extraElement[@naam='identificatieAOA']"/></fk_15aoa_identif>
    </xsl:template>

    <xsl:template name="getHash">
        <xsl:param name="bsn"/>
        <xsl:variable name="bsnwithprefix"><xsl:value-of select="'NL.BRP.Persoon.'"/><xsl:value-of select="$bsn"/></xsl:variable>
        <xsl:variable name="hashedbsn"><xsl:value-of select="/root/bsnhashes/*[name() =$bsnwithprefix]"/></xsl:variable>
        <xsl:value-of select="$hashedbsn"/>
    </xsl:template>

</xsl:stylesheet>