<?xml version="1.0" encoding="UTF-8"?>
<!--	This XSLT stylesheet belongs to a set of XSLT stylesheets which are used to proces a set of XML-Schema's to enable them to be used in code generation.
		 
		Copyright (C) 2013  Robert Melskens, KING

		This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
		License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.

		This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
		MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.

		You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software
		Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA


		2012-12-06	0.1	Robert Melskens	Ten behoeve van een beter overzicht zijn de templates m.b.t. het processen van de schemaList bestanden uit het hoofd stylesheet in dit stylesheet geplaatst. 
																Daarnaast maakt het ook nog gebruik van enkele generieke templates die in het hoofd stylesheet staan. -->
		
		
<xsl:stylesheet exclude-result-prefixes="fn schemaReferences application xs xsd" version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:application="http://www.kinggemeenten.nl/2011/applicationConfiguration" xmlns:schemaReferences="http://www.kinggemeenten.nl/2011/schemaReferences">

	<xsl:include href="resolveXSDs-generateConsolidatedSchemas.xslt"/>
	<xsl:include href="resolveXSDs-generateConsolidatedSchemaSet.xslt"/>
	<xsl:include href="resolveXSDs-getUsedComponents.xslt"/>
	
	<xsl:template name="processListOfSchemas">
		<xsl:param name="referenceSet"/>

			<!-- Indien alle xs:import en xs:include elementen geprocessed zijn, en er dus een compleet overzicht van alle binnen de taxonomy voorkomende 
				  schema bestanden is, wordt gestart met de uiteindelijke transformatie.
				  Daarvoor worden binnen het laatst aangemaakte schemaList bestand ('schemaList_Lp...xml') alle schema's gesorteerd op namespace en per namespace in een 
				  schemaSet element geplaatst (stap 2). Het resultaat wordt in het bestand 'schemaList.xml' geplaatst wat dient als bron voor het produceren van de per namespace geconsolideerde schema's (stap 3).
				  Daarmee is de transformatie echter nog niet afgerond aangezien in deze bestanden nog componenten aanwezig zullen zijn die niet in gebruik zijn.
				  De op een na laatste stap bestaat dan ook uit het verwijderen van deze componenten (stap 5). Daarvoor dient echter eerst geinventariseerd te worden welke componenten nog gebruikt worden (stap 4).
				  In de laatste stap (stap 6) worden tenslotte alle platgeslagen schema's gegenereerd. -->
				<xsl:if test="contains($enabledStage,'2;')">
				
					<xsl:message>
					<xsl:text> 2. Genereren van een op namespaces gesorteerde lijst met schema's binnen de
    taxonomie op basis van de voorgaande stap.</xsl:text>
					</xsl:message>		
					
					<xsl:result-document href="{concat('file:////',$applicationPath,'/generated/schemaList.xml')}" indent="yes" method="xml">
						<schema  xmlns="http://www.w3.org/2001/XMLSchema">
							<!-- Onderstaande for-each statements zorgen er voor dat er eerst een schemaSet element wordt aangemaakt voor de namespace 
								  waarbinnen het rootschema zich bevindt en daarna een schemaSet element voor de andere namespaces. 
								  De buitenste for-each statements zorgen er daarbij voor dat er voor elke unieke namespace een 'schemaSet' wordt aangemaakt.
								  De binnenste for-each statements zorgen er voor dat alle bij de in verwerking zijnde namespace behorende schema's binnen de 'schemaSet' worden geplaatst. -->
							<xsl:for-each select="$referenceSet/xs:schema/*[@namespace=//xs:rootSchema/@namespace and not(@namespace=preceding-sibling::*/@namespace)]">
								<xsl:variable name="namespace" select="@namespace"/>
								<schemaSet namespace="{$namespace}">
									<xsl:for-each select="$referenceSet/xs:schema/*[local-name()!='div' and @namespace=$namespace]">
										<xsl:copy  exclude-result-prefixes="xs xsd">
											<xsl:apply-templates select="@*"/>
										</xsl:copy>
									</xsl:for-each>
								</schemaSet>
							</xsl:for-each>
							<xsl:for-each select="$referenceSet/xs:schema/*[@namespace!=//xs:rootSchema/@namespace and not(@namespace=preceding-sibling::*/@namespace)]">
								<xsl:variable name="namespace" select="@namespace"/>
								<schemaSet namespace="{$namespace}">
									<xsl:for-each select="$referenceSet/xs:schema/*[local-name()!='div' and @namespace=$namespace]">
										<xsl:copy exclude-result-prefixes="xs xsd">
											<xsl:apply-templates select="@*"/>
										</xsl:copy>
									</xsl:for-each>
								</schemaSet>
							</xsl:for-each>
						</schema>
					</xsl:result-document>
					<!-- Het aanmaken van het 'ready.xml' bestand dient voor het vrijgeven van het 'schemaList.xml' bestand voor verdere verwerking. -->
					<xsl:result-document href="{concat('file:////',$applicationPath,'/generated/ready.xml')}" indent="yes" method="xml">
						<ready>yes</ready>
					</xsl:result-document>
				</xsl:if>
				<xsl:variable name="completeReferenceSet" select="if (doc-available(concat('file:////',$applicationPath,'/generated/schemaList.xml'))) 
																							 then (document(concat('file:////',$applicationPath,'/generated/schemaList.xml')) )
																							 else ''"/>
				<xsl:if test="contains($enabledStage,'3;')">
					<!-- In stap 3 wordt de generatie van de geconsolideerde schema's opgestart. Dit betekent dat er per namespace 1 groot schema wordt gegenereerd. -->
					
					<xsl:message>
						<xsl:text> 3. Genereren van de geconsolideerde schema's.</xsl:text>
					</xsl:message>		
					
					<!-- Onderstaand template staat in 'resolveXSDs-generateConsolidatedSchemas.xslt'. -->
					<xsl:apply-templates select="$completeReferenceSet//xs:schemaSet" mode="generateConsolidatedSchemas"/>
				</xsl:if>
					<!-- Nu de geconsolideerde schema's zijn gegenereerd kan bepaald worden welke schema-componenten niet meer in gebruik zijn zodat deze verwijderd kunnen worden. 
					  Om dit eenvoudig te kunnen doen worden eerst alle geconsolideerde schema's bij elkaar in 1 super consolidatie instance geplaatst. -->
				<xsl:if test="$restrictionMode='true' and contains($enabledStage,'4;')">
				
					<xsl:message>
						<xsl:text> 4. Genereren van een superconsolidatie instance.</xsl:text>
					</xsl:message>		
					
					<!-- Samenstellen van de superconsolidatie instance. -->
					<xsl:variable name="allSchemas">
						<xs:allSchemas>
							<!-- Onderstaand template staat in 'resolveXSDs-generateConsolidatedSchemaSet.xslt'. -->
							<xsl:apply-templates select="$completeReferenceSet//xs:schemaSet" mode="generateConsolidatedSchemaSet"/>
						</xs:allSchemas>
					</xsl:variable>
	
					<!-- Start: voor debug doeleinden -->
					<xsl:if test="$debug='yes'">
						<xsl:result-document href="{concat('file:////',$applicationPath,'/consolidated/allschemas.xml')}" indent="yes" method="xml">
							<xsl:copy-of select="$allSchemas"/>
						</xsl:result-document>
					</xsl:if>
					<!-- Eind: voor debug doeleinden -->
					
					
					<xsl:if test="$restrictionMode='true' and contains($enabledStage,'5;')">	
					
						<xsl:message>
							<xsl:text> 5. Genereren van een lijst met in gebruik zijnde componenten.</xsl:text>
						</xsl:message>		
						
						<!-- Op basis van het super consolidatie bestand wordt de variabele 'usedComponents' gevuld. 
							  Hiervoor wordt voor alle root 'element' elementen van de schema's gecontroleerd of ze uiteindelijk in de onderliggende 
							  structuur van de root 'element' elementen van het mainschema van het sectormodel worden gebruikt. -->
							  
						<xsl:variable name="usedComponents">
							<!-- Onderstaand template staat in 'resolveXSDs-getUsedComponents.xslt'. -->
							<xsl:call-template name="getUsedComponents">
								<xsl:with-param name="loop" select="0"/>
								<xsl:with-param name="allSchemas" select="$allSchemas"/>
								<xsl:with-param name="usedComponents">
									<xs:Used/>
								</xsl:with-param>
							</xsl:call-template>
						</xsl:variable>
	
						<!-- Start: voor debug doeleinden -->
						<xsl:if test="$debug='yes'">
							<xsl:result-document href="{concat('file:////',$applicationPath,'/consolidated/usedComponents.xml')}" indent="yes" method="xml">
								<xsl:copy-of select="$usedComponents"/>
							</xsl:result-document>
						</xsl:if>
						<!-- Eind: voor debug doeleinden -->
	
						<!-- Tenslotte worden alle platgeslagen schema's gegenereerd en in de 'finalized' folder opgeslagen waarbij de overbodige complexTypes uit het schema verwijderd worden. -->
						<xsl:if test="contains($enabledStage,'6;')">
						
							<xsl:message>
								<xsl:text> 6. Genereren definitieve schema's zonder ongebruikte complexTypes op basis van
    de voorgaande stap.</xsl:text>
							</xsl:message>
							
							<xsl:for-each select="$allSchemas//xs:mySchema/xs:schema">
								<xsl:variable name="namespace" select="@targetNamespace"/>
								<xsl:variable name="prefix" select="../@prefix"/>
								<xsl:result-document href="{../@path}" indent="yes" method="xml" exclude-result-prefixes="xs xsd">
									<schema>
										<xsl:for-each select="namespace::*">
											<xsl:namespace name="xs"  select="'http://www.w3.org/2001/XMLSchema'"/>
											<xsl:namespace name="xsd"  select="'http://www.w3.org/2001/XMLSchema'"/>
											<xsl:if test="local-name()!='xs' and local-name()!='xsd'">
												<xsl:namespace name="{local-name()}" select="."/>
											</xsl:if>
										</xsl:for-each>
										<xsl:apply-templates select="@*" exclude-result-prefixes="xs xsd"/>
										<!-- TODO Robert: bepalen of het voldoende is om op local-name te checken, wellicht toch op volledige naam. --> 
										<xsl:apply-templates select="*[not(local-name()='element') and not(local-name()='complexType') and not(local-name()='group') and not(local-name()='attributeGroup') and not(local-name()='attribute') and not(local-name()='simpleType')]" exclude-result-prefixes="xs xsd"/>
										<xsl:for-each select="*">
											<xsl:variable name="elementname" select="local-name()"/>
											<xsl:variable name="name" select="@name"/>
											<xsl:if test="$usedComponents/xs:Used/*[local-name()=$elementname and @name=$name and @prefix=$prefix]">
												<xsl:copy-of select="." exclude-result-prefixes="xs xsd"/>
											</xsl:if>
										</xsl:for-each>
										
										<!-- TODO Robert: Volgorde van plaatsen van elementen corrigeren. Daarvoor dient eerst de gewenste volgorde bepaald te worden: 
														element, complexType, group, attributeGroup, attribute, simpleType? --> 

									</schema>
								</xsl:result-document>
							</xsl:for-each>
						</xsl:if>
					</xsl:if>
				</xsl:if>
	</xsl:template>

</xsl:stylesheet>