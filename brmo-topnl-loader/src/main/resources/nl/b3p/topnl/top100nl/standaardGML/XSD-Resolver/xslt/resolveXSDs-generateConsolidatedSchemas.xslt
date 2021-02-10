<?xml version="1.0" encoding="UTF-8"?>
<!--	This XSLT stylesheet belongs to a set of XSLT stylesheets which are used to proces a set of XML-Schema's to enable them to be used in code generation.
		 
		Copyright (C) 2013  Robert Melskens, KING

		This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
		License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.

		This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
		MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.

		You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software
		Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA


		2012-12-10	0.1	Robert Melskens	Ten behoeve van een beter overzicht zijn de templates m.b.t. het genereren van de per namespace geconsolideerde schema bestanden uit het hoofd stylesheet in dit stylesheet geplaatst. 
																Daarnaast maakt het ook nog gebruik van enkele generieke templates die in het hoofd stylesheet staan. 
		2013-09-25	0.11	Robert Melskens	Voorzieningen getroffen zodat de bestandsnaam van de te genereren bestanden kan eindigen op '_resolved.xsd'. -->
																
																
<xsl:stylesheet exclude-result-prefixes="fn schemaReferences application xs xsd" version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:application="http://www.kinggemeenten.nl/2011/applicationConfiguration" xmlns:schemaReferences="http://www.kinggemeenten.nl/2011/schemaReferences">
	<!-- Dit template start de generatie van het consolideren van de schema's per namespace. Hierbinnen worden ook evt. de aanwezige restrictions ge-resolved. -->
	<xsl:template match="xs:schemaSet" mode="generateConsolidatedSchemas" exclude-result-prefixes="xs xsd">
		<xsl:variable name="namespace" select="@namespace"/>
		<xsl:choose>
			<!-- Volgende when dient voor het verwerken van een evt. rootSchema element. In een schemaSet met zo'n element moet dat element nl. 
				  altijd als ingang dienen voor het genereren van een nieuw schema. -->
			<xsl:when test="xs:rootSchema">
				<xsl:variable name="schemaFile">
					<xsl:call-template name="extractDirOrFileName">
						<xsl:with-param name="action" select="'extractFileName'"/>
						<xsl:with-param name="filePath" select="xs:rootSchema/@schemaLocation"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="schema" select="document(concat('file:////',xs:rootSchema/@schemaLocation))"/>
				<!-- De volgende variabele bevat alle tot de gerelateerde namespace behorende onbewerkte schema componenten. -->
				<xsl:variable name="newschema">
					<schema xmlns="http://www.w3.org/2001/XMLSchema">
						<!-- Eerst worden alle benodigde namespace declaraties gegenereerd. -->
						<xsl:for-each select="$schema//namespace::*">
							<xsl:if test="local-name()!='xs' and local-name()!='xsd'">
								<xsl:namespace name="{local-name()}" select="."/>
							</xsl:if>
						</xsl:for-each>
						<xsl:for-each select="xs:include">
							<xsl:variable name="includeSchema" select="document(concat('file:////',@schemaLocation))"/>
							<xsl:for-each select="$includeSchema//namespace::*">
								<xsl:variable name="this" select="string()"/>
								<xsl:choose>
									<xsl:when test="local-name()='xs' or local-name()='xsd'"/>
									<xsl:when test="local-name()=''">
										<xsl:namespace name="{$prefixes//application:prefix[@namespace=$this]}" select="."/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:namespace name="{local-name()}" select="."/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:for-each>
						</xsl:for-each>
						<xsl:for-each select="//xs:schemaSet/xs:import[@importingNamespace=$namespace]">
							<xsl:variable name="importSchema" select="document(concat('file:////',@schemaLocation))"/>
							<xsl:for-each select="$importSchema//namespace::*">
								<xsl:variable name="this" select="string()"/>
								<xsl:choose>
									<xsl:when test="local-name()='xs' or local-name()='xsd'"/>
									<xsl:when test="local-name()=''">
										<xsl:namespace name="{$prefixes//application:prefix[@namespace=$this]}" select="."/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:namespace name="{local-name()}" select="."/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:for-each>
						</xsl:for-each>
						<!-- Daarna worden alle attributen van het root element gerepliceerd. -->
						<xsl:apply-templates select="$schema/xs:schema/@*"/>
						<!-- Vervolgens worden er xs:import elementen gegenereerd voor all xs:import elementen die in de huidige namespace geimporteerd worden
							 (importingNamespace attribuut is in dat geval gelijk aan de in bewerking zijnde namespace).. -->
						<xsl:apply-templates select="//xs:schemaSet[xs:import/@importingNamespace=$namespace]"/>
						<!-- Dan worden alle elementen die geen include of import elementen zijn gerepliceerd. -->
						<xsl:apply-templates select="$schema/xs:schema/node()[local-name()!='import' and local-name()!='include']" exclude-result-prefixes="xs xsd"/>
						<!-- Tenslotte wordt de inhoud van alle in de huidige schemaSet voorkomende schema bestanden waaraan wordt gerefereerd middels een 
							  xs:include of xs:import in het geconsolideerde schema gerepliceerd (op alle daar weer in voorkomende xs:include en xs:import elementen na). -->
						<xsl:apply-templates select="xs:include | xs:import" mode="resolve"/>
					</schema>
				</xsl:variable>
				<!-- De variabele 'newschema' wordt evt. met ge-resolvde restrictions opgeslagen in een geconsolideerd schemabestand. -->
				<xsl:result-document href="{concat('file:////',$applicationPath,'/consolidated/',$schemaFile)}" indent="yes" method="xml">
					<schema xmlns="http://www.w3.org/2001/XMLSchema">
						<!-- Eerst worden alle benodigde namespace declaraties gegenereerd. -->
						<xsl:namespace name="xs" select="'http://www.w3.org/2001/XMLSchema'"/>
						<xsl:for-each select="$newschema//namespace::*">
							<xsl:namespace name="{local-name()}" select="."/>
						</xsl:for-each>
						<!-- Daarna worden alle attributen van het root element gerepliceerd. -->
						<xsl:apply-templates select="$newschema/xs:schema/@*"/>
						<!-- En tenslotte alle elementen van het root element. -->
						<xsl:apply-templates select="$newschema/xs:schema/node()" mode="resolveRestrictions" exclude-result-prefixes="xs xsd"/>
					</schema>
				</xsl:result-document>
			</xsl:when>
			<!-- Indien het xs:schemaSet slechts 1 kind bevat wordt de volgende when tak uitgevoerd. 
				  In dat geval behoud het bestand zijn naam en wordt de inhoud van het schema gerepliceerd. -->
			<xsl:when test="count(*)=1">
				<xsl:variable name="schemaFile">
					<xsl:call-template name="extractDirOrFileName">
						<xsl:with-param name="action" select="'extractFileName'"/>
						<xsl:with-param name="filePath" select="*/@schemaLocation"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="schema">
					<xsl:choose>
						<xsl:when test="contains(*/@schemaLocation,'http:')">
							<xsl:copy-of select="document(*[1]/@schemaLocation)"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:copy-of select="document(concat('file:////',*/@schemaLocation))"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<!-- De volgende variabele bevat alle tot de gerelateerde namespace behorende onbewerkte schema componenten. -->
				<xsl:variable name="newschema">
					<schema xmlns="http://www.w3.org/2001/XMLSchema">
						<!-- Eerst worden alle benodigde namespace declaraties gegenereerd. -->
						<xsl:for-each select="$schema//namespace::*">
							<xsl:if test="local-name()!='xs' and local-name()!='xsd'">
								<xsl:namespace name="{local-name()}" select="."/>
							</xsl:if>
						</xsl:for-each>
						<!-- Daarna worden alle attributen van het root element gerepliceerd. -->
						<xsl:apply-templates select="$schema/xs:schema/@*"/>
						<!-- Vervolgens worden er xs:import elementen gegenereerd voor all xs:import elementen die in de huidige namespace geimporteerd worden
							 (importingNamespace attribuut is gelijk aan de in bewerking zijnde namespace).. -->
						<xsl:apply-templates select="//xs:schemaSet[xs:import/@importingNamespace=$namespace]"/>
						<!-- Dan worden alle elementen die geen include of import elementen zijn gerepliceerd. 
							   TODO: In principe zou hier niet gecheckt hoeven te worden op local-name()!='include' aangezien er in deze namespace slechts 1 bestand 
							   voorkomt en er daardoor ook geen sprake kan zijn van een include. Wellicht nog verwijderen! -->
						<xsl:apply-templates select="$schema/xs:schema/node()[local-name()!='import' and local-name()!='include']" exclude-result-prefixes="xs xsd"/>
						<!-- Hier wordt niet op xs:import en xs:include elementen gecheckt er komt immers maar 1 schema voor in deze namespace. -->
					</schema>
				</xsl:variable>
				<!-- De variabele 'newschema' wordt evt. met ge-resolvde restrictions opgeslagen in een geconsolideerd schemabestand. -->
				<xsl:result-document href="{concat('file:////',$applicationPath,'/consolidated/',$schemaFile)}" indent="yes" method="xml">
					<schema xmlns="http://www.w3.org/2001/XMLSchema">
						<!-- Eerst worden alle benodigde namespace declaraties gegenereerd. -->
						<xsl:namespace name="xs" select="'http://www.w3.org/2001/XMLSchema'"/>
						<xsl:for-each select="$newschema//namespace::*">
							<xsl:namespace name="{local-name()}" select="."/>
						</xsl:for-each>
						<!-- Daarna worden alle attributen van het root element gerepliceerd. -->
						<xsl:apply-templates select="$newschema/xs:schema/@*"/>
						<!-- En tenslotte alle elementen van het root element. -->
						<xsl:apply-templates select="$newschema/xs:schema/node()" mode="resolveRestrictions" exclude-result-prefixes="xs xsd"/>
					</schema>
				</xsl:result-document>
			</xsl:when>
			<!-- In alle andere gevallen (als de schemaSet geen rootSchema bevat maar wel meerdere entries) dan wordt deze otherwise tak gebruikt. -->
			<xsl:otherwise>
				<xsl:variable name="firstSchemaId" select="generate-id(*[1])"/>
				<xsl:variable name="schemaFile">
					<xsl:call-template name="extractDirOrFileName">
						<xsl:with-param name="action" select="'extractFileName'"/>
						<xsl:with-param name="filePath" select="*[1]/@schemaLocation"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="schema">
					<xsl:choose>
						<xsl:when test="contains(*[1]/@schemaLocation,'http:')">
							<xsl:copy-of select="document(*[1]/@schemaLocation)"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:copy-of select="document(concat('file:////',*[1]/@schemaLocation))"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<!-- De volgende variabele bevat alle tot de gerelateerde namespace behorende onbewerkte schema componenten. -->

		<xsl:variable name="base-uri" select="base-uri(.)"/>	
				<xsl:variable name="newschema">
					<schema xmlns="http://www.w3.org/2001/XMLSchema">
						<!-- Eerst worden alle benodigde namespace declaraties gegenereerd. -->
						<xsl:for-each select="$schema//namespace::*">
							<xsl:if test="local-name()!='xs' and local-name()!='xsd'">
								<xsl:namespace name="{local-name()}" select="."/>
							</xsl:if>
						</xsl:for-each>
						<xsl:for-each select="xs:include | xs:import">
							<xsl:variable name="includeSchema">
								<xsl:choose>
									<xsl:when test="contains(*[1]/@schemaLocation,'http:')">
										<xsl:copy-of select="document(@schemaLocation)"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:copy-of select="document(concat('file:////',@schemaLocation))"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							<xsl:for-each select="$includeSchema//namespace::*">
								<xsl:if test="local-name()!='xs' and local-name()!='xsd'">
									<xsl:namespace name="{local-name()}" select="."/>
								</xsl:if>
							</xsl:for-each>
						</xsl:for-each>
						<!-- Daarna worden alle attributen van het root element gerepliceerd. -->
						<xsl:apply-templates select="$schema/xs:schema/@*"/>
						<!-- Vervolgens worden er xs:import elementen gegenereerd voor all xs:import elementen die in de huidige namespace geimporteerd worden
							 (importingNamespace attribuut is gelijk aan de in bewerking zijnde namespace).. -->
						<xsl:apply-templates select="//xs:schemaSet[xs:import/@importingNamespace=$namespace]"/>
						<!-- Dan worden alle elementen die geen include of import elementen zijn gerepliceerd. -->
						<xsl:apply-templates select="$schema/xs:schema/node()[local-name()!='import' and local-name()!='include']" exclude-result-prefixes="xs xsd"/>
						<!-- Tenslotte wordt de inhoud van alle in de huidige schemaSet voorkomende schema bestanden waaraan wordt gerefereerd middels een 
							  xs:include of xs:import in het geconsolideerde schema gerepliceerd (op alle daar weer in voorkomende xs:include en xs:import elementen na). -->
						<xsl:apply-templates select="xs:include[generate-id()!=$firstSchemaId and not(@schemaLocation=preceding-sibling::xs:import/@schemaLocation) and not(@schemaLocation=preceding-sibling::xs:include/@schemaLocation)]" mode="resolve" exclude-result-prefixes="xs xsd"/>
						<xsl:apply-templates select="xs:import[generate-id()!=$firstSchemaId and not(@schemaLocation=preceding-sibling::xs:import/@schemaLocation) and not(@schemaLocation=preceding-sibling::xs:include/@schemaLocation)]" mode="resolve" exclude-result-prefixes="xs xsd"/>
					</schema>
				</xsl:variable>
				<!-- De variabele 'newschema' wordt evt. met ge-resolvde restrictions opgeslagen in een geconsolideerd schemabestand. -->
				<xsl:result-document href="{concat('file:////',$applicationPath,'/consolidated/',$schemaFile)}" indent="yes" method="xml">
					<schema xmlns="http://www.w3.org/2001/XMLSchema">
						<!-- Eerst worden alle benodigde namespace declaraties gegenereerd. -->
						<xsl:namespace name="xs" select="'http://www.w3.org/2001/XMLSchema'"/>
						<xsl:for-each select="$newschema//namespace::*">
							<xsl:variable name="this" select="string()"/>
							<xsl:choose>
								<xsl:when test="local-name()='xs' or local-name()='xsd'"/>
								<xsl:when test="local-name()=''">
									<xsl:namespace name="{$prefixes//application:prefix[@namespace=$this]}" select="."/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:namespace name="{local-name()}" select="."/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
						<!-- Daarna worden alle attributen van het root element gerepliceerd. -->
						<xsl:apply-templates select="$newschema/xs:schema/@*"/>
						<!-- En tenslotte alle elementen van het root element. -->
						<xsl:variable name="targetNamespace" select="$newschema/xs:schema/@targetNamespace"/>
						<xsl:apply-templates select="$newschema/xs:schema/node()" mode="resolveRestrictions" exclude-result-prefixes="xs xsd">
							<xsl:with-param name="prefix2Brepaired">
								<xsl:choose>
									<!-- De volgende instructie werkt prima maar ik verwacht dat als een namespace gedefinieerd staat in de lijst met namespaces zonder prefix terwijl er in het bewuste
										  schema wel een prefix is gedefinieerd er prefixes aan componentnamem worden toegevoegd terwijl die prefix niet wordt gedeclareerd. In dat geval mag het 
										  stylesheet dus niets met de nieuwe prefix doen.-->
									<xsl:when test="local-name(namespace::*[.=$targetNamespace])='' and $prefixes//application:prefix/@namespace=$targetNamespace">
										<xsl:value-of select="$prefixes//application:prefix[@namespace=$targetNamespace]"/>
									</xsl:when>
									<xsl:otherwise/>
								</xsl:choose>
							</xsl:with-param>
						</xsl:apply-templates>
					</schema>
				</xsl:result-document>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- Het volgende template zorgt er voor dat alle relevante xs:import element in het te genereren schema gerepliceerd worden. -->
	<xsl:template match="xs:schemaSet" exclude-result-prefixes="xs xsd">
		<xsl:apply-templates select="xs:import" mode="replicate"/>
	</xsl:template>
	<xsl:template match="xs:include|xs:import" mode="resolve" exclude-result-prefixes="xs xsd">
		<xsl:variable name="schema" select="document(concat('file:////',@schemaLocation))"/>
		<xsl:apply-templates select="$schema/xs:schema" mode="include"/>
	</xsl:template>
	<xsl:template match="xs:schema" mode="include" exclude-result-prefixes="xs xsd">
		<xsl:apply-templates select="node()[local-name()!='import' and local-name()!='include']"/>
	</xsl:template>
	<!-- Dit template reproduceert het xs:import element indien er geen xs:import element binnen het xs:schemaSet bestaat met dezelfde waarde voor het namespace 
		   attribuut wat vóór dit xs:import element staat. De waarde van het schemaLocation attribuut wordt hierbij evt. ontdaan van een path.
		   Het bevat dus alleen een bestandsnaam.  -->
	<xsl:template match="xs:import" mode="replicate" exclude-result-prefixes="xs xsd">
		<xsl:variable name="namespace" select="@namespace"/>
		<xsl:if test="not(preceding-sibling::xs:import[@namespace=$namespace])">
			<xsl:variable name="schemaFile">
				<xsl:choose>
					<xsl:when test="contains(@schemaLocation,'/') or contains(@schemaLocation,'\')">
						<xsl:call-template name="extractDirOrFileName">
							<xsl:with-param name="action" select="'extractFileName'"/>
							<xsl:with-param name="filePath" select="@schemaLocation"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="@schemaLocation"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<import>
				<xsl:apply-templates select="@namespace"/>
				<xsl:attribute name="schemaLocation" select="concat(substring-before($schemaFile,'.xsd'),'_resolved',$versie,'.xsd')"/>
			</import>
		</xsl:if>
	</xsl:template>
	<!-- Als er een 'xs:complexContent' element wordt gevonden met een 'xs:restriction' element in zich en de restrictionMode staat op 'true' dan wordt dit niet gerepliceerd. 
		  Wel wordt het template voor het onderliggende 'xs:restriction' element afgevuurd. -->
	<xsl:template match="xs:complexContent[xs:restriction and $restrictionMode='true' and not(/xs:schema/@targetNamespace=$doNotResolve//application:namespace)]" exclude-result-prefixes="xs xsd" mode="resolveRestrictions">
		<xsl:param name="prefix2Brepaired" select="''"/>
		<xsl:apply-templates mode="resolveRestrictions">
			<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
		</xsl:apply-templates>
	</xsl:template>
	<!-- Een 'xs:simpleContent' element met een 'xs:restriction' element in zich wordt gewoon gerepliceerd. -->
	<xsl:template match="xs:simpleContent[xs:restriction[(xs:attribute or xs:attributeGroup) and not(*[name()!='xs:attribute']) and not(*[name()!='xs:attributeGroup'])] and $restrictionMode='true' and not(/xs:schema/@targetNamespace=$doNotResolve//application:namespace)]" exclude-result-prefixes="xs xsd" mode="resolveRestrictions">
		<xsl:param name="prefix2Brepaired" select="''"/>
		<xsl:copy>
			<xsl:apply-templates mode="resolveRestrictions">
				<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
			</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>
	<!-- Zorgt er voor dat een evt. hoger niveau xs:complexType ook wordt verwerkt. -->
	<xsl:template match="xs:extension|xs:restriction" exclude-result-prefixes="xs xsd" mode="resolveAttributes">
		<xsl:param name="schemaList"/>
		<xsl:call-template name="resolveAttributes">
			<xsl:with-param name="base" select="substring-after(@base,':')"/>
			<xsl:with-param name="schemaList" select="$schemaList"/>
		</xsl:call-template>
	</xsl:template>
	<!-- 'xs:restriction' elementen binnen 'xs:simpleContent' elementen kunnen onder bepaalde omstandigheden omgezet worden naar 'xs:extension' elementen.
		  Indien dat mogelijk is dan gebeurd dat hier anders wordt de structuur gerepliceerd. --> 
	<xsl:template match="xs:restriction[parent::xs:simpleContent and $restrictionMode='true' and not(/xs:schema/@targetNamespace=$doNotResolve//application:namespace)]" exclude-result-prefixes="xs xsd" mode="resolveRestrictions">
		<xsl:param name="prefix2Brepaired" select="''"/>
		<!-- Deze variabele is nodig om de evt. 'xs:attributeGroup' elementen op te kunnen halen. -->
		<xsl:variable name="schemaList" select="doc(concat('file:////',$applicationPath,'/generated/schemaList.xml'))"/>
		<!-- In dit deel wordt de lijst met attributen opgebouwd. De meest van toepassing zijnde attributen staan daarbij vooraan.
			  Indien de ancestor 'complexType' andere elementen dan 'xs:restriction' of 'xs:extension' bevaten dan wordt een 'stop' element gegenereerd.	
			  Indien deze  'xs:restriction' of 'xs:extension' elementen op hun beurt andere elementen dan 'xs:attribute' bevatten dan worden deze ook gerepliceerd.
			  Namespaces op het 'groups' element zijn noodzakelijk om in staat te zijn de gewenste schema's op te halen. -->
		<xsl:variable name="children">
			<groups>
				<xsl:for-each select="namespace::*">
					<xsl:namespace name="{local-name()}" select="."/>
				</xsl:for-each>
				<xsl:apply-templates select="*[name()!='xs:attributeGroup']">
					<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
				</xsl:apply-templates>
				<xsl:apply-templates select="xs:attributeGroup" mode="resolveAttributes">
					<xsl:with-param name="schemaList" select="$schemaList"/>
					<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
				</xsl:apply-templates>
				<xsl:call-template name="resolveChildren">
					<xsl:with-param name="base" select="substring-after(@base,':')"/>
					<xsl:with-param name="schemaList" select="$schemaList"/>
					<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
				</xsl:call-template>
			</groups>
		</xsl:variable>
		<!-- In de volgende choose wordt de beslissing genomen om of de gehele 'xs:restriction' tree te repliceren of om deze om te bouwen naar een 'xs:extension' structuur. 
			  Dit is afhankelijk van het voorkomen van andere elementen dan 'attribute' en 'xs:extensionBase'. -->
		<xsl:choose>
			<xsl:when test="$children/xs:groups/*[name()!='attribute' and name()!='xs:extensionBase']">
				<xsl:copy>
					<xsl:apply-templates select="@* | node()"/>
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
				<extension base="{$children//xs:extensionBase}">
					<xsl:for-each select="$children//xs:attribute">
						<xsl:variable name="name" select="@name"/>
						<xsl:variable name="ref" select="@ref"/>
						<xsl:if test="not(preceding-sibling::xs:attribute[@ref = $ref]) and not(preceding-sibling::xs:attribute[@name = $name])">
							<xsl:choose>
								<xsl:when test="@use='prohibited'"/>
								<xsl:otherwise>
									<xsl:copy exclude-result-prefixes="xs xsd">
										<xsl:apply-templates select="@*"/>
									</xsl:copy>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:if>
					</xsl:for-each>
				</extension>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- Als een 'xs:restriction' element wordt gevonden in een 'xs:complexContent' en de restrictionMode staat op 'true' dan wordt dit niet gerepliceerd.
		  Wel worden de onderliggende 'structuren gerepliceerd. -->
	<xsl:template match="xs:restriction[parent::xs:complexContent and $restrictionMode='true' and not(/xs:schema/@targetNamespace=$doNotResolve//application:namespace)]" exclude-result-prefixes="xs xsd" mode="resolveRestrictions">
		<xsl:param name="prefix2Brepaired" select="''"/>
		<!-- Eerst worden de 'xs:element' elementen geresolved. -->
		<xsl:apply-templates select="*[local-name()!='attribute' and local-name()!='attributeGroup']">
			<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
		</xsl:apply-templates>
		<!-- Daarna de attributen. Dit is echter wat bewerkelijker omdat de attributen niet herhaald hoeven te worden om toegekend te kunnen worden. 
			  Daarom wordt eerst een lijst van attributen opgebouwd die in de huidige complexType en in al diens voorouders voorkomen.
			  Als extra complexiteit dienen hierin ook nog de 'xs:attributeGroup' elementen meegenomen worden en de 'xs:attribute' elementen daarvan opgehaald worden.
			  Extra complex omdat deze ook in andere namespaces voor kunnen komen en dus in andere schema's gecheckt moeten worden.
			  
			  De zo verkregen lijst moet worden doorgelopen en van duplicaten worden ontdaan. Daarbij is het attribuut dat op het laagste level is gedefinieerd van toepassing.
			  Tenslotte moeten ook alleen die attributen geplaatst worden die niet de status prohibited hebben op het huidige complexType of op diens voorouders).
			   -->
		<!-- Deze variabele is nodig om de evt. 'xs:attributeGroup' elementen op te kunnen halen. -->
		<xsl:variable name="schemaList" select="doc(concat('file:////',$applicationPath,'/generated/schemaList.xml'))"/>
		<!-- In dit deel wordt de lijst met attributen opgebouwd. De meest van toepassing zijnde attributen staan daarbij vooraan.
			  Namespaces op het 'groups' element zijn noodzakelijk om in staat te zijn de gewenste schema's op te halen. -->
		<xsl:variable name="attributes">
			<groups>
				<xsl:for-each select="namespace::*">
					<xsl:namespace name="{local-name()}" select="."/>
				</xsl:for-each>
				<xsl:apply-templates select="xs:attribute">
					<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
				</xsl:apply-templates>
				<xsl:apply-templates select="xs:attributeGroup" mode="resolveAttributes">
					<xsl:with-param name="schemaList" select="$schemaList"/>
					<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
				</xsl:apply-templates>
				<xsl:call-template name="resolveAttributes">
					<xsl:with-param name="base" select="substring-after(@base,':')"/>
					<xsl:with-param name="schemaList" select="$schemaList"/>
					<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
				</xsl:call-template>
			</groups>
		</xsl:variable>
		<!-- Hieronder wordt de lijst met attributen ontdaan van dubbelen en van attributen die prohibited zijn. -->
		<xsl:for-each select="$attributes//xs:attribute">
			<xsl:variable name="name" select="@name"/>
			<xsl:variable name="ref" select="@ref"/>
			<xsl:if test="not(preceding-sibling::xs:attribute[@ref = $ref]) and not(preceding-sibling::xs:attribute[@name = $name])">
				<xsl:choose>
					<xsl:when test="@use='prohibited'"/>
					<xsl:otherwise>
						<xsl:copy exclude-result-prefixes="xs xsd">
							<xsl:apply-templates select="@*">
								<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
							</xsl:apply-templates>
						</xsl:copy>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<!-- Volgende template start de verwerking van kinderen van xs:simpleContent elementen. -->
	<xsl:template name="resolveChildren" exclude-result-prefixes="xs xsd">
		<xsl:param name="base"/>
		<xsl:param name="schemaList"/>
		<xsl:param name="prefix2Brepaired" select="''"/>
		<!-- Volgende choose zorgt voor afhandeling van 3 situaties:
			  * Indien het 'xs:simpleContent' element een 'xs:extension' element heeft dan worden alle kinderen gerepliceerd.
				Eventuele 'xs:attributeGroup' elementen worden geresolved naar 'xs:attribute' elementen verder wordt er een 'xs:extensionBase' element gegenereerd
				met als waarde de waarde van het 'base' attribuut van het 'xs:extension' element.
			  * Indien het 'xs:simpleContent' element een 'xs:restriction' element heeft dan worden alle kinderen gerepliceerd.
				Eventuele 'xs:attributeGroup' elementen worden geresolved naar 'xs:attribute' elementen.
				Tenslotte wordt het 'resolveChildren' template gestart voor de verwerking van het volgende level 'xs:complexType'.
			  * In alle andere gevallen wordt er een 'stop' element gegenereerd. -->
		<xsl:choose>
			<xsl:when test="//xs:complexType[@name=$base]/xs:simpleContent/xs:extension">
				<xs:extensionBase>
					<xsl:if test="contains(//xs:complexType[@name=$base]/xs:simpleContent/xs:extension/@base,':') and $prefix2Brepaired!=''">
						<xsl:value-of select="concat($prefix2Brepaired,':')"/>
					</xsl:if>					
					<xsl:value-of select="//xs:complexType[@name=$base]/xs:simpleContent/xs:extension/@base"/>
				</xs:extensionBase>
				<xsl:apply-templates select="//xs:complexType[@name=$base]/xs:simpleContent/xs:extension/*[name()!='xs:attributeGroup']">
					<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
				</xsl:apply-templates>
				<xsl:apply-templates select="//xs:complexType[@name=$base]/xs:simpleContent/xs:extension/xs:attributeGroup" mode="resolveAttributes">
					<xsl:with-param name="schemaList" select="$schemaList"/>
					<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test="//xs:complexType[@name=$base]/xs:simpleContent/xs:restriction">
				<xsl:apply-templates select="//xs:complexType[@name=$base]/xs:simpleContent/xs:restriction/*[name()!='xs:attributeGroup']">
					<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
				</xsl:apply-templates>
				<xsl:apply-templates select="//xs:complexType[@name=$base]/xs:simpleContent/xs:restriction/xs:attributeGroup" mode="resolveAttributes">
					<xsl:with-param name="schemaList" select="$schemaList"/>
					<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
				</xsl:apply-templates>
				<xsl:call-template name="resolveChildren">
					<xsl:with-param name="base" select="substring-after(//xs:complexType[@name=$base]/xs:simpleContent/xs:restriction/@base,':')"/>
					<xsl:with-param name="schemaList" select="$schemaList"/>
					<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<stop/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- Volgende template start de verwerking van xs:attribute elementen in het eerst hogere xs:complexType element. -->
	<xsl:template name="resolveAttributes" exclude-result-prefixes="xs xsd">
		<xsl:param name="base"/>
		<xsl:param name="schemaList"/>
		<xsl:param name="prefix2Brepaired" select="''"/>
		<!-- Volgende twee apply-templates dragen zorg voor het plaatsen van de xs:attribute elementen binnen dit level in de lijst met attribute elementen. -->
		<xsl:apply-templates select="//xs:complexType[@name=$base]//xs:attribute">
			<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
		</xsl:apply-templates>
		<xsl:apply-templates select="//xs:complexType[@name=$base]//xs:attributeGroup" mode="resolveAttributes">
			<xsl:with-param name="schemaList" select="$schemaList"/>
			<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
		</xsl:apply-templates>
		<xsl:apply-templates select="//xs:complexType[@name=$base]//xs:extension" mode="resolveAttributes">
			<xsl:with-param name="schemaList" select="$schemaList"/>
			<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
		</xsl:apply-templates>
		<xsl:apply-templates select="//xs:complexType[@name=$base]//xs:restriction" mode="resolveAttributes">
			<xsl:with-param name="schemaList" select="$schemaList"/>
			<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
		</xsl:apply-templates>
	</xsl:template>
	<!-- Zorgt voor het ophalen van xs:attribute elementen binnen een xs:attributeGroup element in een ander schema. -->
	<xsl:template match="xs:attributeGroup" mode="resolveAttributes" exclude-result-prefixes="xs xsd">
		<xsl:param name="schemaList"/>
		<xsl:variable name="name" select="substring-after(@ref,':')"/>
		<xsl:variable name="prefix" select="substring-before(@ref,':')"/>
		<xsl:variable name="attGrpNamespace" select="namespace::*[name()=$prefix]"/>
		<xsl:for-each select="$schemaList//xs:schemaSet[@namespace=$attGrpNamespace]/*">
			<xsl:variable name="schema" select="doc(@schemaLocation)"/>
			<xsl:apply-templates select="$schema//xs:attributeGroup[@name=$name]//xs:attribute"/>
			<xsl:apply-templates select="$schema//xs:attributeGroup[@name=$name]//xs:attributeGroup" mode="resolveAttributes">
				<xsl:with-param name="schemaList" select="$schemaList"/>
			</xsl:apply-templates>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="node()" mode="resolveRestrictions" exclude-result-prefixes="xs xsd">
		<xsl:param name="prefix2Brepaired" select="''"/>
		<xsl:copy copy-namespaces="no" exclude-result-prefixes="xs xsd">
			<xsl:apply-templates select="@*|node()" mode="resolveRestrictions">
				<xsl:with-param name="prefix2Brepaired" select="$prefix2Brepaired"/>
			</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="@*" mode="resolveRestrictions" exclude-result-prefixes="xs xsd">
		<xsl:param name="prefix2Brepaired" select="''"/>
		<xsl:choose>
			<xsl:when test="$prefix2Brepaired!='' and (not(contains(.,':'))) and (name()='ref' or name()='type' or name()='base' or name()='itemType' or name()='substitutionGroup')">
				<xsl:attribute name="{name()}">
					<xsl:value-of select="concat($prefix2Brepaired,':',.)"/>
				</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy copy-namespaces="no" exclude-result-prefixes="xs xsd"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>