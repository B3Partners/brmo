<?xml version="1.0" encoding="UTF-8"?>
<!--	This XSLT stylesheet belongs to a set of XSLT stylesheets which are used to proces a set of XML-Schema's to enable them to be used in code generation.
		 
		Copyright (C) 2013  Robert Melskens, KING

		This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
		License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.

		This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
		MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.

		You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software
		Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA


		2012-03-06	0.1	 Robert Melskens	M.b.v. dit stylesheet worden de StUF schema's van een sectormodel omgezet zodat deze
																wel bruikbaar zijn in bepaalde software pakketten

																Voor een correcte werking van dit stylesheet moet het opgestart worden vanuit het configuratie 
																bestand.

																Het moge duidelijk zijn dat de inhoud en de verwijzingen in alle van het sectormodel deel 
																uitmakende XML schema's eveneens correct moeten zijn om tot het juiste resultaat te komen.

																In dit stylesheet en ook in de resulterende tussenresultaten worden elementen gegenereerd 
																binnen de XML-Schema namespace die daar eigenlijk niet toe behoren.
																De tussenresultaten mogen dan ook niet als valide XML-Schema's beschouwd worden en 
																kunnen dus ook niet worden gevalideerd. 

																Het proces bestaat uit 2 stappen. Als eerste wordt, startend vanuit het rootschema dat in het 
																configuratiebestand is gedefinieerd, per namespace een lijst gegenereerd van schema's die 
																deel uitmaken van die namespace en schema's die daarin geimporteerd worden.
																Daarna worden op basis van die lijst per namespace een schema gegenereerd. Daarbij worden 
																afhankelijk van de waarde van de variabele 'restrictionMode' restriction complexTypes verwerkt 
																tot normale complexTypes.
		2012-05-03	0.2	 Robert Melskens	Stylesheet genereert nu schema's waarin de XML-schema elementen geen prefix hebben. 
																Daarnaast werden de attributen bij de compelxTypes in versie 0.1 nog niet correct geresolved 
																en hadden de 'import' elementen nog het ongewenste 'xmlns' attribuut. Dit is nu gecorrigeerd. 
		2012-05-08	0.3	 Robert Melskens	Stylesheet resolved nu ook complexTypes met simpleContent met dien verstande dat dit alleen 
																gebeurd daar waar het complexType of diens voorouders (tot aan de eerste voorouder met een 
																'extension' element) alleen attributes definieren. 
																Ook worden nu attributeGroup elementen binnen attributeGroup elementen geresolved en alle 
																'...-basis' complexTypes die niet meer in gebruik zijn eruit gefilterd. 
		2012-05-11	0.4	 Robert Melskens	Templates opgeschoond en een aantal daarvan geintegreerd met elkaar.
																Tijdens het genereren worden nu status berichten gegenereerd.
																Tenslotte de performance verbeterd door gebruik te maken van 'xsl:key' constructies.
		2012-05-16	0.5	 Robert Melskens	Indien de restrictionmode gelijk is aan 'false' dan worden restrictions niet verwijderd en blijven tevens
																'...-basis' complexTypes in het schema staan.
		2012-06-11	0.6	 Robert Melskens	Ook schema's waarnaar met een hard pad naar een lokale locatie of naar een http adres worden nu goed geresolved.
																LET OP: Het kan zijn dat daarvoor in Saxon de proxy instellingen geconfigureerd moeten worden.
																In de opbouw van 'basisComplexTypes.xml' (waaruit kan worden afgeleid welke complexTypes daadwerkelijk in
																in gebruik zijn) zaten nog foutjes. Zo werd er bijv. niet gecheckt of een complexType als basis type voor een restriction 
																of extension in gebruik was. Daarnaast wordt nu ook gecheckt of een group element in gebruik is en alleen dan gereproduceerd.
		2012-08-16	0.611 Robert Melskens	Bij het samenstellen van de variabele 'basisComplexTypes' wordt nu bij het bepalen of een complexType wel of niet meegenomen 
																moet worden ook rekening gehouden met of complexTypes gebruikt worden als 'base' voor een extension ergens in een element.
		2012-08-17	0.620 Robert Melskens	Eerste versie van een nieuwe methodiek voor het vaststellen van de gebruikte componenten.
		2012-12-10	0.631 Robert Melskens	Vaststellen van gebruikte componenten werkt nu ook voor de grotere schema's. 
																Verder is dat proces ook verbeterd in de zin dat de kwaliteit van de gegenereerde schema's beter is en de performance van de 
																XSD Resolver sterk verbeterd is.
		2013-04-11 	0.640 Robert Melskens	Het proces voor het vaststellen van gebruikte componenten is verder geoptimaliseerd. In de oude situatie konden er meer dan 150 
																loops plaatsvinden waarna het proces alsnog stuk liep.
																Daarnaast wordt nu ook rekening gehouden met situaties waarbij er meerdere schema's gebruik maken van een default namespace.
																Doordat alle schema's binnen 1 bestand geconsolideerd werden leverde dat problemen op. Nu kan voor zo'n schema in het configuratie 
																bestand alsnog een prefix gedefinieerd worden.
		2013-09-25	0.650 Robert Melskens	Voorzieningen getroffen zodat aan de bestandsnaam van de te genereren bestanden een waarde kan worden toegevoegd. -->
		
		
		
<!-- LET OP! 	Het afhandelen van http adressen gaat nog niet goed. Hier moet beslist nog tijd in gestoken worden. Echter pas op het moment dat er besloten wordt dat binnen StUF 
					dit soort urls ook ondersteund moet worden.  -->



<xsl:stylesheet exclude-result-prefixes="fn schemaReferences application xs xsd" version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:application="http://www.kinggemeenten.nl/2011/applicationConfiguration" xmlns:schemaReferences="http://www.kinggemeenten.nl/2011/schemaReferences">

	<xsl:include href="resolveXSDs-functies.xslt"/>
	<xsl:include href="resolveXSDs-generateListOfSchemas.xslt"/>
	
	<xsl:output method="xml" indent="yes" encoding="utf-8" exclude-result-prefixes="fn schemaReferences application xs xsd" />
	
	<!-- Door in het configuratie bestand het element '//application:restrictionMode' op 'false' te zetten kan voorkomen worden dat alle 'restrictions' uit de schema's worden omgezet. -->
	<xsl:param name="restrictionMode" select="//application:restrictionMode"/>
	
	<!-- Door de parameter 'debug' de waarde 'yes' te geven zal het stylesheet tussenresultaten genereren zoals 'allschemas.xml' en 'usedComponents.xml'. -->
	<xsl:param name="debug" select="'no'"/>
	
	<!-- Indien deze parameter een waarde heeft dan wordt deze aan de naam van de te genereren schema toegevoegd. -->
	<xsl:param name="versie" select="''"/>

	<!-- Er zijn op dit moment 6 fases die met de volgende parameter ingeschakeld dan wel uitgeschakeld kunnen worden.
		  Alle fases scheiden met een ';', dus 'enabledStage' heeft maximaal de volgende waarde '1;2;3;4;5;6;'. -->
	<!--<xsl:param name="enabledStage" select="'1;2;3;'"/>-->
	<xsl:param name="enabledStage" select="'1;2;3;4;5;6;'"/>
	
	<!-- Bevat de foldernaam van de folder waar zich het te converteren schema bevindt. -->
	<xsl:variable name="applicationPath" select="replace(//application:path,'\\','/')"/>
	
	<!-- Bevat prefixes voor het geval er voor namespaces geen prefix is gedefinieerd. -->
	<xsl:variable name="prefixes">
		<application:namespacePrefixes>
			<xsl:copy-of select="//application:namespacePrefixes" exclude-result-prefixes="xs xsd"/>
		</application:namespacePrefixes>
	</xsl:variable>

	<!-- Bevat alle namespaces waarvan de schema's niet van restrictions ontdaan moeten worden. -->
	<xsl:variable name="doNotResolve">
		<application:doNotResolve>
			<xsl:copy-of select="//application:doNotResolve" exclude-result-prefixes="xs xsd"/>
		</application:doNotResolve>
	</xsl:variable>

	<!-- Bevat de naam van het te converteren schema bestand. -->
	<xsl:variable name="mainSchema" select="//application:configurationSectorModel[@active='yes'][1]/application:resolve/application:schema/application:schemaName"/>
		
	<!--Root van het configuratie bestand dient als vertrekpunt voor dit stylesheet. -->
	<xsl:template match="/" exclude-result-prefixes="xs xsd">
		<xsl:apply-templates select="//application:configurationSectorModel[@active='yes'][1]"/>
	</xsl:template>
	
	<xsl:template match="application:configurationSectorModel" exclude-result-prefixes="xs xsd">
		<xsl:call-template name="startConversion">
			<xsl:with-param name="schemaPath" select="replace(concat(replace(application:pathSectormodel,'\\','/'),'/',replace(./application:resolve/application:schema/application:schemaName,'\\','/')),'//','/')"/>
		</xsl:call-template>
	</xsl:template>
	
	<!-- Het volgende template start de eigenlijke conversie op basis van het in het configuratiebestand gedefinieerde schema (berichtencatalogus of koppelvlak). 
		  Dit schema wordt eerst verwerkt tot een XML fragment waarin alleen de imports en includes zijn opgenomen. 
		  Daarna wordt dit schema gebruikt als input voor het verwerken van alle schema's die hierin geimporteerd of geinclude worden. -->
	<xsl:template name="startConversion" exclude-result-prefixes="xs xsd">
		<xsl:param name="schemaPath"/>
		<xsl:message><xsl:text>
********************************************************************************
 Start conversie van de berichtencatalogus/koppelvlak: </xsl:text><xsl:value-of select="$mainSchema"/><xsl:text>  
</xsl:text>
		</xsl:message>		
		<xsl:if test="contains($enabledStage,'1;')">
			<xsl:message>
			<xsl:text> 1. Genereren van lijsten met schema's binnen de taxonomie.</xsl:text>
			</xsl:message>
		</xsl:if>
		<!-- Het te converteren schemabestand wordt in de variabele 'schema' geplaatst. -->
		<xsl:variable name="schema" select="document(concat('file:////',$schemaPath))"/>
		<xsl:variable name="path">
			<xsl:call-template name="extractDirOrFileName">
				<xsl:with-param name="action" select="'extractDir'"/>
				<xsl:with-param name="filePath" select="$schemaPath"/>
			</xsl:call-template>
		</xsl:variable>
		<!-- Onderstaand aangeroepen templates staan in 'resolveXSDs-generateListOfSchemas.xslt' en inventariseren alle gebruikte schema's. -->
		<xsl:if test="contains($enabledStage,'1;')">
			<xsl:apply-templates select="$schema/xs:schema" mode="root">
				<xsl:with-param name="path" select="$path"/>
				<xsl:with-param name="schemaPath" select="$schemaPath"/>
			</xsl:apply-templates>
		</xsl:if>
		<xsl:call-template name="generateListOfSchemas">
			<xsl:with-param name="path" select="$path"/>
			<xsl:with-param name="currentLoop" select="1"/>
		</xsl:call-template>
		<xsl:message>
			<xsl:text>
 Einde conversie.
********************************************************************************</xsl:text>
		</xsl:message>		
	</xsl:template>


	<!--	****************************************************************************************************************
			Generieke templates
			****************************************************************************************************************-->

	<!-- Volgende templates worden door meerdere subprocessen gebruikt om elementen, waarvoor in dit stylesheet geen templates zijn opgenomen, en evt. diepere structuren te repliceren. -->
	<xsl:template match="@*|node()" exclude-result-prefixes="xs xsd">
		<xsl:copy copy-namespaces="no" exclude-result-prefixes="xs xsd">
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	
</xsl:stylesheet>