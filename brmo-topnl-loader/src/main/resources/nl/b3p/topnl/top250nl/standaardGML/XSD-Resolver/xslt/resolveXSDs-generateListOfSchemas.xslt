<?xml version="1.0" encoding="UTF-8"?>
<!--	This XSLT stylesheet belongs to a set of XSLT stylesheets which are used to proces a set of XML-Schema's to enable them to be used in code generation.
		 
		Copyright (C) 2013  Robert Melskens, KING

		This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
		License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.

		This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
		MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.

		You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software
		Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA


		2012-12-06	0.1	Robert Melskens	Ten behoeve van een beter overzicht zijn de templates m.b.t. het genereren van de schemaList bestanden uit het hoofd stylesheet in dit stylesheet geplaatst. 
																Daarnaast maakt het ook nog gebruik van enkele generieke templates die in het hoofd stylesheet staan.
		2012-12-10	0.11	Robert Melskens	Template toegevoegd dat ook tot deze set behoorde.
																Documentatie verbeterd. -->
		
<!-- LET OP! Het afhandelen van http adressen gaat nog niet goed. Hier moet beslist nog tijd in gestoken worden. -->



<xsl:stylesheet exclude-result-prefixes="fn schemaReferences application xs xsd" version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:application="http://www.kinggemeenten.nl/2011/applicationConfiguration" xmlns:schemaReferences="http://www.kinggemeenten.nl/2011/schemaReferences">

	<xsl:include href="resolveXSDs-processListOfSchemas.xslt"/>


	<!-- Dit template wordt alleen gebruikt voor het verwerken van het rootschema (het schema dat in het configuratie bestand is gedefinieerd).
		  Het maakt eerst een variabele aan die gevuld wordt met een xs:schema element met daarin alleen de in het schema voorkomende 
		  xs:include en xs:import elementen (verrijkt met extra informatie).
		  Daarna worden 2 bestanden aangemaakt, het bestand ('schemaList_Lp...') met de inhoud van deze variabele en een ready bestand ('ready_Lp....') 
		  dat nodig is om het schemaList bestand vrij te geven voor verdere verwerking.
		  Mogelijk kunnen we onderstaande template op een later tijdstip nog samenvoegen met het generateListOfSchemas template. -->
	<xsl:template match="xs:schema" mode="root" exclude-result-prefixes="xs xsd">
		<xsl:param name="path"/>
		<xsl:param name="schemaPath"/>
		<xsl:param name="loop" select="1"/>

		<xsl:variable name="schema">
			<xsl:copy copy-namespaces="no"  exclude-result-prefixes="xs xsd">
				<rootSchema schemaLocation="{$schemaPath}" namespace="{@targetNamespace}"/>
				<xsl:apply-templates select="xs:import" mode="buildListOfImportsOrIncludes">
					<xsl:with-param name="path" select="$path"/>
				</xsl:apply-templates>
				<xsl:apply-templates select="xs:include" mode="buildListOfImportsOrIncludes">
					<xsl:with-param name="path" select="$path"/>
				</xsl:apply-templates>
			</xsl:copy>
		</xsl:variable>

<xsl:variable name="base-uri" select="base-uri(.)"/>
		<xsl:result-document href="{concat('file:////',$applicationPath,'/generated/schemaList_Lp',$loop,'.xml')}" indent="yes" method="xml">
			<xsl:copy-of select="$schema" exclude-result-prefixes="xs xsd"/>
		</xsl:result-document>
		<xsl:result-document href="{concat('file:////',$applicationPath,'/generated/ready_Lp',$loop,'.xml')}" indent="yes" method="xml">
			<ready>yes</ready>
		</xsl:result-document>
	</xsl:template>
	
	<!-- Het onderstaande template verwerkt alle schema's die in een ander schema via een 'xs:import' of 'xs:include' geimporteerd of geinclude worden.
		  Als input voor deze verwerking dient het in de voorgaande loop aangemaakte schemaList bestand ('schemaList_Lp...xml').
		  Initiele input wordt echter gegenereerd door het hierboven staande template dat het 'schemaList_Lp1.xml' bestand genereert. -->
	<xsl:template name="generateListOfSchemas" exclude-result-prefixes="fn schemaReferences xs xsd">
		<xsl:param name="path"/>
		<xsl:param name="currentLoop"/>
		<xsl:variable name="referenceSet">
			<!-- Volgende call-template haalt het laatst gegenereerde 'schemaList_Lp...xml' bestand op. -->
			<xsl:call-template name="getLargestSchemaList_Lp">
				<xsl:with-param name="currentLoop" select="$currentLoop"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:choose>
			<!-- Zolang er nog xs:include en xs:import elementen voorkomen die nog niet geprocessed zijn worden deze eerst geprocessed en wordt de 
				  generateListOfSchemas template (het huidige template dus) opgestart. 
				  Dit leidt tot nieuwe schemaList ('schemaList_Lp...xml') en ready ('ready_Lp...xml') bestanden. -->
			<xsl:when test="($referenceSet//xs:include[not(@processed='yes')] or $referenceSet//xs:import[not(@processed='yes')]) and contains($enabledStage,'1;')">
				<xsl:variable name="loop" select="$currentLoop + 1"/>
				<xsl:result-document href="{concat('file:////',$applicationPath,'/generated/schemaList_Lp',$loop,'.xml')}" indent="yes" method="xml">
					<schema  xmlns="http://www.w3.org/2001/XMLSchema">
						<!-- Volgende apply-templates repliceert alle aanwezige elementen waarbij xs:import en xs:include de indicatie processed='yes' krijgen. -->
						<xsl:apply-templates select="$referenceSet//xs:import
																							[not(preceding-sibling::xs:import
																											[@schemaLocation=current()/@schemaLocation and 
																											 @namespace=current()/@namespace and 
																											 @importingNamespace=current()/@importingNamespace])] | 
																		$referenceSet//xs:include
																							[not(@schemaLocation = preceding-sibling::xs:include/@schemaLocation) and 
																							 not(@schemaLocation = preceding-sibling::xs:import/@schemaLocation)] | 
																		$referenceSet//xs:div | 
																		$referenceSet//xs:rootSchema" mode="indicateProcessed"/>
						<div/>
						<!-- Volgende apply-templates verwerkt alle in de nog niet verwerkte xs:includes en xs:import elementen aangegeven schema's. -->
						<xsl:apply-templates select="$referenceSet//xs:import
																							[not(@processed='yes') and 
																							 not(@schemaLocation = preceding-sibling::xs:import/@schemaLocation)] | 
																   $referenceSet//xs:include
																							[not(@processed='yes') and 
																							 not(@schemaLocation = preceding-sibling::xs:include/@schemaLocation) and 
																							 not(@schemaLocation = preceding-sibling::xs:import/@schemaLocation)]" mode="processIncludes">
						</xsl:apply-templates>
					</schema>
				</xsl:result-document>
				<xsl:result-document href="{concat('file:////',$applicationPath,'/generated/ready_Lp',$loop,'.xml')}" indent="yes" method="xml">
					<ready>yes</ready>
				</xsl:result-document>
				<!-- Het huidge template wordt weer aangeroepen ter verwerking van de volgende laag. -->
				<xsl:call-template name="generateListOfSchemas">
					<xsl:with-param name="path" select="$path"/>
					<xsl:with-param name="currentLoop" select="$currentLoop + 1"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<!-- Onderstaand template staat in 'resolveXSDs-processListOfSchemas.xslt'. 
					   Indien de lijst met alle te verwerken schema's compleet is kan begonnen worden met de verwerking van de schema's. -->
				<xsl:call-template name="processListOfSchemas">
					<xsl:with-param name="referenceSet" select="$referenceSet"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Dit template reproduceert het xs:rootSchema element. -->
	<xsl:template match="xs:rootSchema" mode="indicateProcessed" exclude-result-prefixes="fn schemaReferences xs xsd">
			<rootSchema>
				<xsl:apply-templates select="@*"/>
			</rootSchema>
	</xsl:template>	

	<!-- Dit template reproduceert een xs:include element en voegt daar het processed attribuut met de waarde 'yes' aan toe. -->
	<xsl:template match="xs:include|xs:import" mode="indicateProcessed" exclude-result-prefixes="fn schemaReferences xs xsd">
			<xsl:copy>
				<xsl:apply-templates select="@*"/>
				<xsl:if test="not(@processed='yes')">
					<xsl:attribute name="processed" select="'yes'"/>
				</xsl:if>
			</xsl:copy>
	</xsl:template>
	
	<!-- Dit template reproduceert een xs:div element. -->
	<xsl:template match="xs:div" mode="indicateProcessed" exclude-result-prefixes="fn schemaReferences xs xsd">
			<div/>
	</xsl:template>

	<!-- Dit template verwerkt de xml-schema bestanden waaraan d.m.v. xs:include elementen wordt gerefereerd. 
		   Je zou zeggen dat het al oveweg kan met 'http' adressen. Dat is volgens mij echter niet het geval. -->
	<xsl:template match="xs:include|xs:import" mode="processIncludes" exclude-result-prefixes="fn schemaReferences xs xsd">
		<xsl:variable name="schemaPath"><xsl:value-of select="@schemaLocation"/></xsl:variable>
		<xsl:variable name="document">
			<xsl:choose>
				<xsl:when test="contains($schemaPath,'http:')">
					<xsl:copy-of select="document($schemaPath)"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:copy-of select="document(concat('file:////',$schemaPath))"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="path">
			<xsl:call-template name="extractDirOrFileName">
				<xsl:with-param name="action" select="'extractDir'"/>
				<xsl:with-param name="filePath" select="$schemaPath"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:apply-templates select="$document/xs:schema/xs:include" mode="buildListOfImportsOrIncludes">
			<xsl:with-param name="path" select="$path"/>
		</xsl:apply-templates>
		<xsl:apply-templates select="$document/xs:schema/xs:import" mode="buildListOfImportsOrIncludes">
			<xsl:with-param name="path" select="$path"/>
		</xsl:apply-templates>
	</xsl:template>

	<!-- Dit template wordt gebruikt om de xs:include en xs:import elementen die in de diverse schema's staan op te nemen in de schemaList bestanden.
		   Het genereert voor elk gevonden xs:include of xs:import element een placeholder. 
		   Voor elk gevonden xs:import element wordt tevens vastgelegd in welke namespace ze geimporteerd moeten worden.-->
	<xsl:template match="xs:include|xs:import" mode="buildListOfImportsOrIncludes" exclude-result-prefixes="fn schemaReferences xs xsd">
		<xsl:param name="path"/>
		<xsl:variable name="schemaPath">
			<xsl:choose>
				<xsl:when test="substring(@schemaLocation,1,4)!='file' and substring(@schemaLocation,1,4)!='http' and 
			substring(@schemaLocation,2,2)!=':\' and substring(@schemaLocation,2,2)!=':/'">
					<xsl:value-of select="concat($path,'/',@schemaLocation)"/>
				</xsl:when>
				<xsl:otherwise><xsl:value-of select="@schemaLocation"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="newPath">
			<xsl:call-template name="extractDirOrFileName">
				<xsl:with-param name="action" select="'extractDir'"/>
				<xsl:with-param name="filePath" select="$schemaPath"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="schemaFile">
			<xsl:call-template name="extractDirOrFileName">
				<xsl:with-param name="action" select="'extractFileName'"/>
				<xsl:with-param name="filePath" select="$schemaPath"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="local-name()='include'">
				<include>
					<xsl:attribute name="schemaLocation" select="concat($newPath,'/',$schemaFile)"/>
					<xsl:attribute name="namespace" select="//@targetNamespace"/>
					<xsl:attribute name="processed" select="'no'"/>
				</include>
			</xsl:when>
			<xsl:otherwise>
				<import>
					<xsl:attribute name="schemaLocation" select="concat($newPath,'/',$schemaFile)"/>
					<xsl:attribute name="importingNamespace" select="//@targetNamespace"/>
					<xsl:attribute name="namespace" select="@namespace"/>
					<xsl:attribute name="processed" select="'no'"/>
				</import>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>