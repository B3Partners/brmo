<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:b3p="http://www.b3partners.nl/">

	<xsl:variable name="rsgb-db-identifiers" select="document('generated_scripts/rsgb_db_identifiers.xml')"/>
	
	<xsl:function name="b3p:class-to-table">
		<xsl:param name="class"/>
		<xsl:value-of select="$rsgb-db-identifiers/identifiers/class[@name=$class]/@table"/>
	</xsl:function>
	
	<xsl:function name="b3p:property-to-column">
		<xsl:param name="property"/>
		<xsl:param name="class"/>
		<xsl:value-of select="$rsgb-db-identifiers/identifiers/class[@name=$class]/property[@name=$property]/@column"/>
	</xsl:function>
	
	<!-- Class afkortingen, afkomsting uit RSGB PDF (Mnemonic objecttype) -->
	<!-- "rsg  basisgegevens 2.2  deel ii _in ontwikkeling_ 20 december 2011.pdf" -->
	<xsl:variable name="class-afkortingen" as="element(afko)*">
		<afko kort="AVR" class="AARD VERKREGEN RECHT"/>
		<afko kort="ACD" class="ACADEMISCHE TITEL"/>
		<afko kort="AOA" class="ADRESSEERBAAR OBJECT AANDUIDING"/>
		<afko kort="ANN" class="ANDER BUITENLANDS NIET-NATUURLIJK PERSOON"/>
		<afko kort="ANP" class="ANDER NATUURLIJK PERSOON"/>
		<afko kort="APR" class="APPARTEMENTSRECHT"/>
		<afko kort="BTD" class="BEGROEID TERREINDEEL"/>
		<afko kort="BTV" class="BEGROEID TERREINVAKONDERDEEL"/>
		<afko kort="TGO" class="BENOEMD OBJECT"/>
		<afko kort="BTR" class="BENOEMD TERREIN"/>
		<afko kort="BCE" class="BRUGCONSTRUCTIE ELEMENT"/>
		<afko kort="BRT" class="BUURT"/>
		<afko kort="STD" class="FUNCTIONEEL GEBIED"/>
		<afko kort="GBO" class="GEBOUWD OBJECT"/>
		<afko kort="GBI" class="GEBOUWINSTALLATIE"/>
		<afko kort="GEM" class="GEMEENTE"/>
		<afko kort="GOR" class="GEMEENTELIJKE OPENBARE RUIMTE"/>
		<afko kort="HHD" class="HUISHOUDEN"/>
		<afko kort="INN" class="INGESCHREVEN NIET-NATUURLIJK PERSOON"/>
		<afko kort="INP" class="INGESCHREVEN NATUURLIJK PERSOON"/>
		<afko kort="ING" class="INGEZETENE"/>
		<afko kort="IRE" class="INRICHTINGSELEMENT"/>
		<afko kort="KDP" class="KADASTRAAL PERCEEL"/>
		<afko kort="KDG" class="KADASTRALE GEMEENTE"/>
		<afko kort="KOZ" class="KADASTRALE ONROERENDE ZAAK"/>
		<afko kort="KZA" class="KADASTRALE ONROERENDE ZAAK AANTEKENING"/>
		<afko kort="KWD" class="KUNSTWERKDEEL"/>
		<afko kort="LND" class="LAND"/>
		<afko kort="LPL" class="LIGPLAATS"/>
		<afko kort="MAC" class="MAATSCHAPPELIJKE ACTIVITEIT"/>
		<afko kort="NAT" class="NATIONALITEIT"/>
		<afko kort="NPS" class="NATUURLIJK PERSOON"/>
		<afko kort="NIN" class="NIET-INGEZETENE"/>
		<afko kort="NNP" class="NIET-NATUURLIJK PERSOON"/>
		<afko kort="NRA" class="NUMMERAANDUIDING"/>
		<afko kort="OBT" class="ONBEGROEID TERREINDEEL"/>
		<afko kort="OTV" class="ONBEGROEID TERREINVAKONDERDEEL"/>
		<afko kort="OND" class="ONDERNEMING"/>
		<afko kort="OWD" class="ONDERSTEUNEND WEGDEEL"/>
		<afko kort="OPR" class="OPENBARE RUIMTE"/>
		<afko kort="OAO" class="OVERIGE ADRESSEERBAAR OBJECT AANDUIDING"/>
		<afko kort="OBW" class="OVERIG BOUWWERK"/>
		<afko kort="OGO" class="OVERIG GEBOUWD OBJECT"/>
		<afko kort="OSD" class="OVERIGE SCHEIDING"/>
		<afko kort="OTR" class="OVERIG TERREIN"/>
		<afko kort="PND" class="PAND"/>
		<afko kort="PES" class="PERSOON"/>
		<afko kort="RSD" class="REISDOCUMENT"/>
		<afko kort="RDS" class="REISDOCUMENTSOORT"/>
		<afko kort="SCD" class="SCHEIDING"/>
		<afko kort="SPO" class="SPOOR"/>
		<afko kort="SPL" class="STANDPLAATS"/>
		<afko kort="STD" class="STADSDEEL"/>
		<afko kort="SUB" class="SUBJECT"/>
		<afko kort="VBO" class="VERBLIJFSOBJECT"/>
		<afko kort="VBT" class="VERBLIJFSTITEL"/>
		<afko kort="VES" class="VESTIGING"/>
		<afko kort="VVO" class="VRIJSTAAND VEGETATIE OBJECT"/>
		<afko kort="WAD" class="WATERDEEL"/>
		<afko kort="WAS" class="WATERSCHAP"/>
		<afko kort="WAV" class="WATERVAKONDERDEEL"/>
		<afko kort="WGD" class="WEGDEEL"/>
		<afko kort="WVD" class="WEGVAKONDERDEEL"/>
		<afko kort="WYK" class="WIJK"/>
		<afko kort="WPL" class="WOONPLAATS"/>
		<afko kort="WDO" class="WOZ-DEELOBJECT"/>
		<afko kort="WOZ" class="WOZ-OBJECT"/>
		<afko kort="WRD" class="WOZ-WAARDE"/>
		<afko kort="ZKR" class="ZAKELIJK RECHT"/>
		<afko kort="ZRA" class="ZAKELIJK RECHT AANTEKENING"/>
	</xsl:variable>
	
	<!-- geef de afkorting van een class terug -->
	<xsl:function name="b3p:get-class-mnemonic">
		<xsl:param name="class-name"/>
		<xsl:variable name="mnemonic" select="fn:lower-case($class-afkortingen[@class=$class-name]/@kort)"/>
		<xsl:choose>
			<xsl:when test="$mnemonic"><xsl:value-of select="$mnemonic"/></xsl:when>
			<xsl:otherwise><xsl:value-of select="b3p:class-to-table($class-name)"/></xsl:otherwise>
		</xsl:choose>
	</xsl:function>
		
	<!-- PREFIXES ========================================================= -->

	<!-- prefixes voor groepsattributen. -->
	<xsl:variable name="groepattribuut-prefixes" as="element(afko)*">
		<afko lang="Koopsom" kort="ks"/>
		<afko lang="Landinrichtingsrente" kort="lr"/>
		<afko lang="Locatie onroerende zaak" kort="lo"/>
		<afko lang="Kadastrale aanduiding" kort="ka"/>
		<afko lang="Geboorte" kort="gb"/>
		<afko lang="Verblijfadres" kort="va"/>
		<afko lang="Overlijden" kort="ol"/>
		<afko lang="Nationaliteit" kort="nt"/>
		<afko lang="Europees kiesrecht" kort="ek"/>
		<afko lang="Uitsluiting kiesrecht" kort="uk"/>
		<afko lang="Cultuur onbebouwd" kort="cu"/>
		<afko lang="Naam" kort="nm"/>
		<afko lang="Postadres" kort="pa"/>
		<afko lang="Rekeningnummer" kort="rn"/>
		<afko lang="SBI Activiteit" kort="sa"/>
		<afko lang="Aandeel in recht" kort="ar"/>
		<afko lang="Beperkte volmacht" kort="bv"/>
		<afko lang="Huwelijkssluiting/aangaan geregistreerd partnerschap" kort="hs"/>
		<afko lang="Ontbinding huwelijk/geregistreerd partnerschap" kort="ho"/>
		<afko lang="Land" kort="ln"/>
		<afko lang="Verblijf buitenland" kort="vb"/>
		<afko lang="Naam aanschrijving" kort="na"/>
	</xsl:variable>
	
	<!-- geef de afkorting van het groepsattribuut class terug -->
	<!-- param prop-name De naam van de class van het groepsattribuut-->
	<xsl:function name="b3p:get-groupattribute-abbreviation">
		<xsl:param name="prop-name"/>
		<xsl:value-of select="$groepattribuut-prefixes[@lang=$prop-name]/@kort" />
	</xsl:function>
	
</xsl:stylesheet>
