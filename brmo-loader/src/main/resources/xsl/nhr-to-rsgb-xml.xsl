<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:cat="http://schemas.kvk.nl/schemas/hrip/catalogus/2013/01"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:fn="http://www.w3.org/2005/xpath-functions">
                
    <xsl:param name="rsgb-version" select="2.2"/>
    
    <xsl:variable name="hoofdvestiging" select="//cat:maatschappelijkeActiviteit/cat:wordtGeleidVanuit//cat:vestigingsnummer"/>
    
    <xsl:template match="/">
        <root>
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
		<xsl:comment>Maatschappelijke activiteit manifesteert zich als onderneming, met niet-hoofdvestigingen</xsl:comment>
		<xsl:apply-templates select="cat:manifesteertZichAls/cat:onderneming" mode="rsgb2.2"/>
	
		<xsl:comment>Hoofdvestiging (maatschappelijkeActiviteit wordtGeleidVanuit)</xsl:comment>
		<xsl:apply-templates select="cat:wordtGeleidVanuit"/>
		
		<maatschapp_activiteit column-dat-beg-geldh="datum_aanvang" column-datum-einde-geldh="datum_einde_geldig">
			<kvk_nummer><xsl:value-of select="cat:kvkNummer"/></kvk_nummer>
			<xsl:call-template name="registratie-datum"/>
			<!--datum_aanvang><xsl:value-of select="cat:registratie/cat:datumAanvang"/></datum_aanvang>
			<datum_einde_geldig><xsl:value-of select="cat:registratie/cat:datumEinde"/></datum_einde_geldig-->

			<xsl:for-each select="cat:manifesteertZichAls/cat:onderneming/cat:kvkNummer">
				<fk_3ond_kvk_nummer><xsl:value-of select="."/></fk_3ond_kvk_nummer>
			</xsl:for-each>
			
			<!-- RSGB todo:
			heeftAlsEigenaar (fk_4pes_sc_identif)
            -->
			<!-- elementen niet in RSGB: 
			nonMailing
			hoofdSbiActiviteit
			nevenSbiActiviteit
			incidenteelUitlenenArbeidskrachten
			bezoekLocatie
			postLocatie
			communicatiegegevens
			naam
			wordtUitgeoefendIn
			wordtGeleidVanuit
			-->
		</maatschapp_activiteit>
	</xsl:template>
	

	<xsl:template match="cat:maatschappelijkeActiviteit" mode="rsgb3.0">
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
	
	<xsl:template match="cat:nietCommercieleVestiging[cat:vestigingsnummer] | cat:commercieleVestiging[cat:vestigingsnummer]">
		<xsl:variable name="key">NHR.Vestiging.<xsl:value-of select="cat:vestigingsnummer"/></xsl:variable>
		<subject>
			<identif><xsl:value-of select="$key"/></identif>
			<clazz>VESTIGING</clazz>
			<typering>VESTIGING</typering>
		</subject>
		<vestg>
			<sc_identif><xsl:value-of select="$key"/></sc_identif>
			<xsl:call-template name="registratie-datum">
				<xsl:with-param name="einde" select="'datum_beeindiging'"/>
			</xsl:call-template>

			<!-- RSGB 'betreft uitoefening van activiteiten door' fk naar onderneming -->
			<!-- van wordtUitgeoefendIn terug naar onderneming of van wordtGeleidVanuit terug naar maatschappelijkeActiviteit -->
			<xsl:variable name="kvk" select="../../cat:kvkNummer"/>
            <xsl:choose>
				<xsl:when test="$kvk">
					<fk_15ond_kvk_nummer><xsl:value-of select="$kvk"/></fk_15ond_kvk_nummer>
				</xsl:when>
				<xsl:otherwise>
					<xsl:message terminate="yes">Vestiging <xsl:value-of select="$key"/> alleen ondersteund als onderdeel van onderneming of maatschappelijke activiteit! wordtUitgeoefendDoor niet geimplementeerd</xsl:message>
				</xsl:otherwise>
			</xsl:choose>

			<!-- RSGB 'betreft uitoefening van activiteiten door' fk naar maatschappelijke activiteit -->
			<!-- van wordtUitgeoefendIn terug naar onderneming, manifesteertZichAls, maatschappelijkeActiviteit -->
			<!-- van wordtGeleidVanuit terug naar maatschappelijkeActiviteit -->
			<!-- Is dit ooit anders dan $kvk? -->
			<xsl:variable name="kvk-ma" select="../../../../cat:kvkNummer | ../../cat:kvkNummer"/>
            <xsl:choose>
				<xsl:when test="$kvk-ma">
					<fk_17mac_kvk_nummer><xsl:value-of select="$kvk"/></fk_17mac_kvk_nummer>
				</xsl:when>
				<xsl:otherwise>
					<xsl:message terminate="yes">Vestiging <xsl:value-of select="$key"/> alleen ondersteund als onderdeel van onderneming als onderdeel van maatschappelijke activiteit! wordtUitgeoefendDoor niet geimplementeerd</xsl:message>
				</xsl:otherwise>
			</xsl:choose>
						
			<typering>
				<xsl:choose>
					<xsl:when test="local-name(.) = 'nietCommercieleVestiging'">Niet-commerciele vestiging</xsl:when>
					<xsl:otherwise>Commerciele vestiging</xsl:otherwise>
				</xsl:choose>
			</typering>
			<datum_voortzetting><xsl:value-of select="cat:datumVoortzetting"/></datum_voortzetting>
			<verkorte_naam>
				<xsl:choose>
					<xsl:when test="cat:eersteHandelsnaam">
						<xsl:value-of select="substring(cat:eersteHandelsnaam,1,45)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="substring(cat:handeltOnder[position()=1]/cat:handelsnaam/cat:naam,1,45)"/>
					</xsl:otherwise>
				</xsl:choose>
			</verkorte_naam>
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
			<xsl:comment> Gaat uit van fixed issue #139, #140</xsl:comment>
			<xsl:for-each select="cat:activiteiten/cat:hoofdSbiActiviteit | cat:activiteiten/cat:sbiActiviteit">
				<xsl:if test="position() = 1">
					<vestg_activiteit delete-rows="true">
						<fk_vestg_nummer><xsl:value-of select="$key"/></fk_vestg_nummer>
					</vestg_activiteit>
				</xsl:if>
				<comfort>
					<sbi_activiteit>
						<sbi_code><xsl:value-of select="cat:sbiCode"/></sbi_code>
						<omschr><xsl:value-of select="cat:omschrijving"/></omschr>
					</sbi_activiteit>
				</comfort>
				<vestg_activiteit>
					<fk_vestg_nummer><xsl:value-of select="$key"/></fk_vestg_nummer>
					<fk_sbi_activiteit_code><xsl:value-of select="cat:sbiCode"/></fk_sbi_activiteit_code>
					<indicatie_hoofdactiviteit><xsl:value-of select="boolean(local-name() = 'hoofdSbiActiviteit')"/></indicatie_hoofdactiviteit>
				</vestg_activiteit>
			</xsl:for-each>

		</vestg>
		<xsl:for-each select="cat:handeltOnder/cat:handelsnaam/cat:naam">
			<xsl:if test="position() = 1">
				<xsl:comment> Gaat uit van fixed issue #139</xsl:comment>
				<vestg_naam delete-rows="true">
					<fk_ves_sc_identif><xsl:value-of select="$key"/></fk_ves_sc_identif>
				</vestg_naam>
			</xsl:if>
			<vestg_naam>			
				<naam><xsl:value-of select="."/></naam> <!-- NHR max 625 lang, BRMO 500 -->
				<fk_ves_sc_identif><xsl:value-of select="$key"/></fk_ves_sc_identif>
			</vestg_naam>
		</xsl:for-each>
		
	</xsl:template>
</xsl:stylesheet>
