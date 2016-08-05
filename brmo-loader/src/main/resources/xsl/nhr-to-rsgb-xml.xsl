<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:cat="http://schemas.kvk.nl/schemas/hrip/catalogus/2013/01"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:fn="http://www.w3.org/2005/xpath-functions">
                
	<xsl:import href="nhr-object-ref.xsl"/>

 	<!-- parameters van het bericht -->
    <xsl:param name="objectRef" />
    <xsl:param name="datum" />
    <xsl:param name="volgordeNummer" />
    <xsl:param name="soort" />
	
    <xsl:param name="rsgb-version" select="2.2"/>
    
    <xsl:variable name="hoofdvestiging" select="/cat:maatschappelijkeActiviteit/cat:wordtGeleidVanuit//cat:vestigingsnummer"/>
    <xsl:variable name="peilmoment" select="/*/@peilmoment"/>
	<xsl:variable name="peilmoment-dateTime"><xsl:value-of select="substring($peilmoment,1,4)"/>-<xsl:value-of select="substring($peilmoment,5,2)"/>-<xsl:value-of select="substring($peilmoment,7,2)"/>T<xsl:value-of select="substring($peilmoment,9,2)"/>:<xsl:value-of select="substring($peilmoment,11,2)"/>:<xsl:value-of select="substring($peilmoment,13,2)"/></xsl:variable>
    
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
				<xsl:for-each select="*">
					<xsl:choose>
						<xsl:when test="$rsgb-version = '2.2'">
							<xsl:apply-templates select="." mode="rsgb2.2"/>
						</xsl:when>
						<xsl:when test="$rsgb-version = '3.0'">
							<xsl:apply-templates select="." mode="rsgb3.0"/>
						</xsl:when>
					</xsl:choose>
				</xsl:for-each>
            </data>
        </root>
    </xsl:template>

	<xsl:template name="registratie-datum">
		<xsl:param name="begin" select="'datum_aanvang'"/>
		<xsl:param name="einde" select="'datum_einde_geldig'"/>
		<xsl:element name="{$begin}">
			<xsl:value-of select="cat:registratie/cat:datumAanvang"/>
		</xsl:element>
		<xsl:element name="{$einde}">
			<xsl:value-of select="cat:registratie/cat:datumEinde"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="cat:maatschappelijkeActiviteit" mode="rsgb2.2">
		<xsl:for-each select="cat:manifesteertZichAls/cat:onderneming">
			<xsl:comment>Maatschappelijke activiteit manifesteert zich als onderneming, met niet-hoofdvestigingen</xsl:comment>
			<xsl:apply-templates select="." mode="rsgb2.2"/>
		</xsl:for-each>
	
		<maatschapp_activiteit column-dat-beg-geldh="datum_aanvang" column-datum-einde-geldh="datum_einde_geldig">
			<kvk_nummer><xsl:value-of select="cat:kvkNummer"/></kvk_nummer>
			<xsl:call-template name="registratie-datum"/>
			<!--datum_aanvang><xsl:value-of select="cat:registratie/cat:datumAanvang"/></datum_aanvang>
			<datum_einde_geldig><xsl:value-of select="cat:registratie/cat:datumEinde"/></datum_einde_geldig-->

			<xsl:for-each select="cat:manifesteertZichAls/cat:onderneming/cat:kvkNummer">
				<fk_3ond_kvk_nummer><xsl:value-of select="."/></fk_3ond_kvk_nummer>
			</xsl:for-each>
			
			<fk_4pes_sc_identif>
				<xsl:apply-templates select="cat:heeftAlsEigenaar" mode="object_ref"/>
			</fk_4pes_sc_identif>

			<!-- elementen niet in RSGB: 
			nonMailing
			hoofdSbiActiviteit
			nevenSbiActiviteit
			incidenteelUitlenenArbeidskrachten
			-->
		</maatschapp_activiteit>

		<!-- TODO: wordtUitgeoefendIn nietCommercieleVestiging -->

		<xsl:variable name="key"><xsl:apply-templates select="." mode="object_ref"/></xsl:variable>
		<xsl:variable name="class">INGESCHREVEN NIET-NATUURLIJK PERSOON</xsl:variable>
		<subject>
			<identif><xsl:value-of select="$key"/></identif>
			<clazz><xsl:value-of select="$class"/></clazz>
			<typering><xsl:value-of select="substring($class,1,35)"/></typering>
			<naam><xsl:value-of select="cat:naam"/></naam>

			<xsl:call-template name="subject"/>
		</subject>
		<prs>
			<sc_identif><xsl:value-of select="$key"/></sc_identif>
			<clazz><xsl:value-of select="$class"/></clazz>
		</prs>
		<niet_nat_prs>
			<sc_identif><xsl:value-of select="$key"/></sc_identif>
			<clazz><xsl:value-of select="$class"/></clazz>
			<naam><xsl:value-of select="cat:naam"/></naam>
			<verkorte_naam><xsl:value-of select="substring(cat:naam,1,45)"/></verkorte_naam>
			<xsl:call-template name="registratie-datum">
				<xsl:with-param name="einde" select="'datum_beeindiging'"/>
			</xsl:call-template>
		</niet_nat_prs>
		<ingeschr_niet_nat_prs>
			<sc_identif><xsl:value-of select="$key"/></sc_identif>
			<typering><xsl:value-of select="substring($class,1,35)"/></typering>

			<fk_8aoa_identif><xsl:value-of select="cat:bezoekLocatie//cat:bagId//cat:identificatieAdresseerbaarObject"/></fk_8aoa_identif>

			<!-- Afgesplitst -->
			<!-- Is dit correct, of moeten deze velden leeg geladen worden en heeftAlsEigenaar als apart object worden
				vastgelegd en heeftAlsEignaar relatie via andere koppeling worden vastgelegd? -->
			<!--xsl:for-each select="cat:heeftAlsEigenaar/*">
				<rsin><xsl:value-of select="cat:rsin"/></rsin>
				<rechtsvorm><xsl:value-of select="cat:persoonRechtsvorm"/></rechtsvorm>
				<statutaire_zetel><xsl:value-of select="cat:statutaireZetel"/></statutaire_zetel>

				<!- - TODO: rechtstoestand - ->
			</xsl:for-each-->

			<!-- TODO heeftAlsEigenaar/rechtspersoon/heeft -->
		</ingeschr_niet_nat_prs>

		<!-- Afgesplitst -->
		<!--xsl:for-each select="cat:wordtGeleidVanuit">
			<xsl:comment>Hoofdvestiging (maatschappelijkeActiviteit wordtGeleidVanuit)</xsl:comment>
			<xsl:apply-templates select="."/>
		</xsl:for-each-->
	</xsl:template>

	<xsl:template match="/cat:maatschappelijkeActiviteit" mode="rsgb3.0">
		<maatschapp_activiteit column-dat-beg-geldh="datum_aanvang" column-datum-einde-geldh="datum_einde_geldig">
			<kvk_nummer><xsl:value-of select="cat:kvkNummer"/></kvk_nummer>
			<xsl:call-template name="registratie-datum"/>
		
			<indicatie_economisch_actief><xsl:value-of select="boolean(cat:manifesteertZichAls/cat:onderneming/cat:kvkNummer)"/></indicatie_economisch_actief>
		</maatschapp_activiteit>
	</xsl:template>
		
	<xsl:template match="cat:onderneming" mode="rsgb2.2">
		<xsl:if test="not(cat:kvkNummer)">
			<xsl:message terminate="yes">
				Onderneming zonder kvk nummer!
			</xsl:message>
		</xsl:if>
			
		<ondrnmng column-dat-beg-geldh="datum_aanvang" column-datum-einde-geldh="datum_einde">
			<kvk_nummer><xsl:value-of select="cat:kvkNummer"/></kvk_nummer>
			<xsl:call-template name="registratie-datum">
				<xsl:with-param name="einde" select="'datum_einde'"/>
			</xsl:call-template>
			
			<!-- Overgedragen aan onderneming zonder kvknummer negeren -->
			<xsl:for-each select="cat:isOvergedragenAan/cat:onderneming/cat:kvkNummer">
				<!-- TODO: constraint droppen of deze onderneming eerst inserten? Testcase nodig om te bepalen of volledige onderneming meegeleverd wordt -->
				<!-- Niet meer in RSGB 3.0. Voorlopig negeren -->
				<!--fk_1ond_kvk_nummer><xsl:value-of select="."/></fk_1ond_kvk_nummer-->
			</xsl:for-each>
		</ondrnmng>
		<xsl:apply-templates select="cat:wordtUitgeoefendIn[cat:vestigingsnummer != $hoofdvestiging]"/>
	</xsl:template>
	
	<xsl:template match="cat:nietCommercieleVestiging[cat:vestigingsnummer] | cat:commercieleVestiging[cat:vestigingsnummer]" mode="rsgb2.2">
		<xsl:variable name="key"><xsl:apply-templates select="." mode="object_ref"/></xsl:variable>

		<xsl:variable name="naam">
			<xsl:choose>
				<xsl:when test="cat:eersteHandelsnaam">
					<xsl:value-of select="substring(cat:eersteHandelsnaam,1,45)"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="substring(cat:handeltOnder[position()=1]/cat:handelsnaam/cat:naam,1,45)"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<subject>
			<identif><xsl:value-of select="$key"/></identif>
			<clazz>VESTIGING</clazz>
			<typering>VESTIGING</typering>
			<naam><xsl:value-of select="$naam"/></naam>

			<xsl:call-template name="subject"/>
		</subject>
		<xsl:for-each select="cat:activiteiten/cat:hoofdSbiActiviteit | cat:activiteiten/cat:sbiActiviteit">
			<comfort search-table="sbi_activiteit"  search-column="sbi_code" search-value="{cat:sbiCode}" snapshot-date="{$peilmoment-dateTime}">
				<sbi_activiteit>
					<sbi_code><xsl:value-of select="cat:sbiCode"/></sbi_code>
					<omschr><xsl:value-of select="cat:omschrijving"/></omschr>
				</sbi_activiteit>
			</comfort>
		</xsl:for-each>
		<vestg>
			<sc_identif><xsl:value-of select="$key"/></sc_identif>
			<xsl:call-template name="registratie-datum">
				<xsl:with-param name="einde" select="'datum_beeindiging'"/>
			</xsl:call-template>

			<!-- RSGB 'betreft uitoefening van activiteiten door' fk naar onderneming -->
			<xsl:for-each select="cat:wordtUitgeoefendDoor/cat:onderneming">
					<fk_15ond_kvk_nummer><xsl:value-of select="cat:kvkNummer"/></fk_15ond_kvk_nummer>
			</xsl:for-each>

			<!-- RSGB 'betreft uitoefening van activiteiten door' fk naar maatschappelijke activiteit -->
			<xsl:for-each select="cat:wordtUitgeoefendDoor/cat:maatschappelijkeActiviteit">
					<fk_17mac_kvk_nummer><xsl:value-of select="cat:kvkNummer"/></fk_17mac_kvk_nummer>
			</xsl:for-each>
						
			<typering>
				<xsl:choose>
					<xsl:when test="local-name(.) = 'nietCommercieleVestiging'">Niet-commerciele vestiging</xsl:when>
					<xsl:otherwise>Commerciele vestiging</xsl:otherwise>
				</xsl:choose>
			</typering>
			<datum_voortzetting><xsl:value-of select="cat:datumVoortzetting"/></datum_voortzetting>
			<verkorte_naam><xsl:value-of select="$naam"/></verkorte_naam>
			<xsl:choose>
				<xsl:when test="cat:fulltimeWerkzamePersonen and cat:parttimeWerkzamePersonen">
					<fulltime_werkzame_mannen><xsl:value-of select="cat:fulltimeWerkzamePersonen"/></fulltime_werkzame_mannen>
					<parttime_werkzame_mannen><xsl:value-of select="cat:parttimeWerkzamePersonen"/></parttime_werkzame_mannen>
				</xsl:when>
				<xsl:when test="cat:totaalWerkzamePersonen">
					<fulltime_werkzame_mannen><xsl:value-of select="cat:totaalWerkzamePersonen"/></fulltime_werkzame_mannen>
				</xsl:when>
			</xsl:choose>
			
			<toevoeging_adres><xsl:value-of select="cat:bezoekLocatie/cat:toevoegingAdres"/></toevoeging_adres>

			<activiteit_omschr><xsl:value-of select="cat:activiteiten/cat:omschrijving"/></activiteit_omschr>
		</vestg>
		<!--xsl:comment> Gaat uit van fixed issue #139, #140</xsl:comment-->
		<xsl:for-each select="cat:activiteiten/cat:hoofdSbiActiviteit | cat:activiteiten/cat:sbiActiviteit">
			<!--xsl:if test="position() = 1">
				<vestg_activiteit delete-rows="true">
					<fk_vestg_nummer><xsl:value-of select="$key"/></fk_vestg_nummer>
				</vestg_activiteit>
			</xsl:if-->
			<vestg_activiteit>
				<fk_vestg_nummer><xsl:value-of select="$key"/></fk_vestg_nummer>
				<fk_sbi_activiteit_code><xsl:value-of select="cat:sbiCode"/></fk_sbi_activiteit_code>
				<indicatie_hoofdactiviteit>
					<xsl:if test="local-name() = 'hoofdSbiActiviteit'">1</xsl:if>
					<xsl:if test="local-name() != 'hoofdSbiActiviteit'">0</xsl:if>
				</indicatie_hoofdactiviteit>
			</vestg_activiteit>
		</xsl:for-each>
		
		<xsl:for-each select="cat:handeltOnder/cat:handelsnaam/cat:naam">
			<!--xsl:if test="position() = 1">
				<xsl:comment> Gaat uit van fixed issue #139</xsl:comment>
				<vestg_naam delete-rows="true">
					<fk_ves_sc_identif><xsl:value-of select="$key"/></fk_ves_sc_identif>
				</vestg_naam>
			</xsl:if-->
			<vestg_naam>			
				<naam><xsl:value-of select="."/></naam> <!-- NHR max 625 lang, BRMO 500 -->
				<fk_ves_sc_identif><xsl:value-of select="$key"/></fk_ves_sc_identif>
			</vestg_naam>
		</xsl:for-each>
		
	</xsl:template>

	<!-- Werkt voor elementen met cat:bezoekLocatie -->
	<xsl:template name="subject">
		<!-- Lengte mismatch: NHR 500, RSGB 257 -->
		<adres_binnenland><xsl:value-of select="cat:bezoekLocatie[cat:binnenlandsAdres]/cat:volledigAdres"/></adres_binnenland>
		<!-- Lengte mismatch: NHR 500, RSGB 149 (???) -->
		<adres_buitenland><xsl:value-of select="cat:bezoekLocatie[cat:buitenlandsAdres]/cat:volledigAdres"/></adres_buitenland>

		<xsl:for-each select="cat:postLocatie/cat:binnenlandsAdres[cat:postbusnummer]">
			<pa_postadres_postcode><xsl:value-of select="cat:postcode/cat:cijfercombinatie"/><xsl:value-of select="cat:postcode/cat:lettercombinatie"/></pa_postadres_postcode>
			<pa_postadrestype>P</pa_postadrestype>
			<pa_postbus__of_antwoordnummer><xsl:value-of select="cat:postbusnummer"/></pa_postbus__of_antwoordnummer>
		</xsl:for-each>

		<fk_15aoa_identif><xsl:value-of select="cat:postLocatie/cat:binnenlandsAdres/cat:bagId/cat:identificatieAdresseerbaarObject"/></fk_15aoa_identif>

		<emailadres><xsl:value-of select="cat:communicatiegegevens/cat:emailAdres[position()=1]"/></emailadres>
		<xsl:for-each select="cat:communicatiegegevens/cat:communicatienummer[cat:soort = 'Fax']/cat:nummer">
			<xsl:if test="position() = 1">
				<fax_nummer><xsl:value-of select="."/></fax_nummer>
			</xsl:if>
		</xsl:for-each>
		<xsl:for-each select="cat:communicatiegegevens/cat:communicatienummer[cat:soort = 'Telefoon']/cat:nummer">
			<xsl:if test="position() = 1">
				<telefoonnummer><xsl:value-of select="."/></telefoonnummer>
			</xsl:if>
		</xsl:for-each>
		<website_url><xsl:value-of select="cat:communicatiegegevens/cat:domeinNaam[position()=1]"/></website_url>
	</xsl:template>
	
	<xsl:template match="cat:rechtspersoon | cat:samenwerkingsverband" mode="rsgb2.2">

		<xsl:variable name="key"><xsl:apply-templates select="." mode="object_ref"/></xsl:variable>
		<xsl:variable name="class">INGESCHREVEN NIET-NATUURLIJK PERSOON</xsl:variable>
		<subject>
			<identif><xsl:value-of select="$key"/></identif>
			<clazz><xsl:value-of select="$class"/></clazz>
			<typering><xsl:value-of select="substring($class,1,35)"/></typering>
			<naam><xsl:value-of select="cat:volledigeNaam"/></naam>

			<xsl:call-template name="subject"/>
		</subject>
		<prs>
			<sc_identif><xsl:value-of select="$key"/></sc_identif>
			<clazz><xsl:value-of select="$class"/></clazz>
		</prs>
		<niet_nat_prs>
			<sc_identif><xsl:value-of select="$key"/></sc_identif>
			<clazz><xsl:value-of select="$class"/></clazz>
			<naam><xsl:value-of select="cat:volledigeNaam"/></naam>
			<verkorte_naam><xsl:value-of select="substring(cat:volledigeNaam,1,45)"/></verkorte_naam>
			<xsl:call-template name="registratie-datum">
				<xsl:with-param name="einde" select="'datum_beeindiging'"/>
			</xsl:call-template>
		</niet_nat_prs>
		<ingeschr_niet_nat_prs>
			<sc_identif><xsl:value-of select="$key"/></sc_identif>
			<typering><xsl:value-of select="substring($class,1,35)"/></typering>

			<fk_8aoa_identif><xsl:value-of select="cat:bezoekLocatie//cat:bagId//cat:identificatieAdresseerbaarObject"/></fk_8aoa_identif>

			<rsin><xsl:value-of select="cat:rsin"/></rsin>
			<rechtsvorm><xsl:value-of select="cat:persoonRechtsvorm"/></rechtsvorm>
			<statutaire_zetel><xsl:value-of select="cat:statutaireZetel"/></statutaire_zetel>

			<!-- TODO heeft (comfortdata) -->
		</ingeschr_niet_nat_prs>
	</xsl:template>
	
	<xsl:template match="cat:natuurlijkPersoon" mode="rsgb2.2">

		<xsl:variable name="key"><xsl:apply-templates select="." mode="object_ref"/></xsl:variable>
		<xsl:variable name="class">NATUURLIJK PERSOON</xsl:variable>
		<subject>
			<identif><xsl:value-of select="$key"/></identif>
			<clazz><xsl:value-of select="$class"/></clazz>
			<typering><xsl:value-of select="substring($class,1,35)"/></typering>
			<naam><xsl:value-of select="cat:volledigeNaam"/></naam>

			<!-- TODO postLocatiePersoon -->
			<xsl:call-template name="subject"/>
		</subject>
		<prs>
			<sc_identif><xsl:value-of select="$key"/></sc_identif>
			<clazz><xsl:value-of select="$class"/></clazz>
		</prs>
		<nat_prs>
			<sc_identif><xsl:value-of select="$key"/></sc_identif>
			<clazz><xsl:value-of select="$class"/></clazz>
			<aand_naamgebruik>
				<xsl:choose>
					<xsl:when test="cat:aanduidingNaamgebruik = 'EigenGeslachtsnaam'">E</xsl:when>
					<xsl:when test="cat:aanduidingNaamgebruik = 'GeslachtsnaamPartner'">P</xsl:when>
					<xsl:when test="cat:aanduidingNaamgebruik = 'GeslachtsnaamPartnerNaEigenGeslachtsnaam'">N</xsl:when>
					<xsl:when test="cat:aanduidingNaamgebruik = 'GeslachtsnaamPartnerVoorEigenGeslachtsnaam'">V</xsl:when>
				</xsl:choose>
			</aand_naamgebruik>
			<geslachtsaand>
				<xsl:choose>
					<xsl:when test="cat:geslachtsaanduiding = 'Man'">M</xsl:when>
					<xsl:when test="cat:geslachtsaanduiding = 'Vrouw'">V</xsl:when>
					<xsl:otherwise>O</xsl:otherwise>
				</xsl:choose>
			</geslachtsaand>
			<!-- TODO nm_adellijke_titel_predikaat/-->
			<nm_geslachtsnaam><xsl:value-of select="cat:geslachtsnaam"/></nm_geslachtsnaam>
			<nm_voornamen><xsl:value-of select="cat:voornamen"/></nm_voornamen>
			<nm_voorvoegsel_geslachtsnaam><xsl:value-of select="cat:voorvoegselGeslachtsnaam"/></nm_voorvoegsel_geslachtsnaam>
		</nat_prs>
		
		<ingeschr_nat_prs>
			<sc_identif><xsl:value-of select="$key"/></sc_identif>
			<clazz><xsl:value-of select="$class"/></clazz>			

			<bsn><xsl:value-of select="cat:bsn"/></bsn>
			
			<fk_28nra_sc_identif><xsl:value-of select="cat:woonLocatie//cat:bagId//cat:identificatieAdresseerbaarObject"/></fk_28nra_sc_identif>
		</ingeschr_nat_prs>
	</xsl:template>
		
	<xsl:template match="*" mode="rsgb2.2">
		<xsl:comment>Catch-all template voor onbekend element <xsl:value-of select="local-name()"/></xsl:comment>
	</xsl:template>
</xsl:stylesheet>