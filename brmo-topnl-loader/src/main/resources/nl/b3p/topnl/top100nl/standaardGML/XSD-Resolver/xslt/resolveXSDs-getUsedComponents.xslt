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
																Daarnaast maakt het ook nog gebruik van enkele generieke templates die in het hoofd stylesheet staan.
		2012-12-11	0.11	Robert Melskens	Bij het bepalen van de in gebruik zijnde componenten worden nu ook de in het configuratiebestand gedefinieerde standaard van toepassing zijnde berichten 
																('application:standardApplicableMessages') meegenomen.
		2013-09-25	0.12	Robert Melskens	Voorzieningen getroffen zodat de bestandsnaam van de te genereren bestanden kan eindigen op '_resolved.xsd'. -->
		
		
<xsl:stylesheet exclude-result-prefixes="fn schemaReferences application xs xsd" version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:application="http://www.kinggemeenten.nl/2011/applicationConfiguration" xmlns:schemaReferences="http://www.kinggemeenten.nl/2011/schemaReferences">

	<!-- Bevat alle berichten die standaard beschikbaar moeten zijn in alle berichtencatalogi en koppelvlakken. -->
	<xsl:variable name="standardApplicableMessages">
		<application:standardApplicableMessages>
			<xsl:copy-of select="//application:standardApplicableMessages" exclude-result-prefixes="xs xsd"/>
		</application:standardApplicableMessages>
	</xsl:variable>

	<!-- Het volgende template managed het vervaardigen van een lijst met in gebruik zijnde componenten.
		  De eerste keer dat dit template wordt afgevuurd wordt de otherwise tak doorlopen die in feite de toplevel elementen bepaald.
		  De volgende loops zakken op basis van de toplevel elementen laag voor laag de structuur af aldoende de in gebruik zijnde componenten bepalend.-->
	<xsl:template name="getUsedComponents">
		<xsl:param name="loop" select="0"/>
		<xsl:param name="allSchemas"/>
		<xsl:param name="previousUsedComponents" >
			<xs:Used/>
		</xsl:param>
		<xsl:param name="usedComponents"/>
		<xsl:choose>
			<xsl:when test="$usedComponents/xs:Used/*">
				<xsl:message>
					<xsl:text>    #######    loop </xsl:text><xsl:value-of select="$loop"/>
				</xsl:message>
				<xsl:if test="$debug='yes'">
					<xsl:choose>
						<xsl:when test="$loop = 1">
							<xsl:message>
								<xsl:text>               start controle componenten verkregen uit het mainschema.
								</xsl:text>
							</xsl:message>
						</xsl:when>
						<xsl:otherwise>
							<xsl:message>
								<xsl:text>               start controle componenten verkregen uit de voorgaande loop.
								</xsl:text>
							</xsl:message>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
				<!-- In de onderstaande variabele worden alle elementen in de variabele 'usedComponents' die nog niet gecheckt zijn (en dus niet binnen het 'xs:checked' element staan, verwerkt.
					  Checken houdt in dit verband in dat gekeken wordt naar welke componenten de bewuste elementen verwijzen (en die dus gebruikt worden in het sectormodel of koppelvlak). -->
				<xsl:variable name="checkingUsedComponents">
					<xs:Used>
						<!-- De volgende templates zijn nog niet optimaal aangezien hierdoor bepaalde elementen dubbel verwerkt worden. Verder is het de vraag of het 3e en 4e template aanroep wel noodzakelijk is. -->
						<xsl:apply-templates select="$usedComponents/xs:Used/*[not(local-name()='checked')]//*[1]" mode="processUsedComponents" exclude-result-prefixes="xs xsd">
							<xsl:with-param name="allSchemas" select="$allSchemas"/>
						</xsl:apply-templates>
						<xsl:apply-templates select="$usedComponents/xs:Used/*[not(local-name()='checked')]//*[not(@id=preceding::*/@id)]" mode="processUsedComponents" exclude-result-prefixes="xs xsd">
							<xsl:with-param name="allSchemas" select="$allSchemas"/>
						</xsl:apply-templates>
						<xsl:apply-templates select="$usedComponents/xs:Used/*[not(local-name()='checked') and not(*)][1]" mode="processUsedComponents" exclude-result-prefixes="xs xsd">
							<xsl:with-param name="allSchemas" select="$allSchemas"/>
						</xsl:apply-templates>
						<xsl:apply-templates select="$usedComponents/xs:Used/*[not(local-name()='checked') and not(*) and not(@id=preceding::*/@id)]" mode="processUsedComponents" exclude-result-prefixes="xs xsd">
							<xsl:with-param name="allSchemas" select="$allSchemas"/>
						</xsl:apply-templates>
					</xs:Used>
				</xsl:variable>
				<xsl:if test="$debug='yes'">
					<xsl:message>
						<xsl:text>               eind controle.
						</xsl:text>
					</xsl:message>
				</xsl:if>


				<xsl:choose>
					<!-- Deze when tak komt in werking zodra er geen nieuwe content meer wordt toegevoegd aan de variabele 'usedComponents' wat betekend dat alle gebruikte componenten zijn gevonden.
						  Het genereert de uiteindelijke content van de variabele 'usedComponents' in het stylesheet 'resolveXSDs-processListOfSchemas.xslt'. -->
						  
					<!-- Volgende starttag alleen gebruiken voor Debug doeleinden. -->
					<!--<xsl:when test="$loop = 101">-->
					<xsl:when test="deep-equal($usedComponents,$previousUsedComponents)">
						<xs:Used>
							<xsl:for-each select="$usedComponents/xs:Used/xs:checked/*[@name and @prefix!='' and not(@id=preceding::*/@id)]">
								<xsl:element name="{name()}" exclude-result-prefixes="xs xsd">
									<xsl:copy-of select="@*"/>
								</xsl:element>
							</xsl:for-each>
							<xsl:for-each select="$usedComponents/xs:Used/*[not(parent::xs:checked or name()='xs:checked') and @name and @prefix!='' and not(@id=preceding::*/@id)]">
								<xsl:element name="{name()}" exclude-result-prefixes="xs xsd">
									<xsl:copy-of select="@*"/>
								</xsl:element>
							</xsl:for-each>
						</xs:Used>
						
						<!-- Start: voor debug doeleinden -->
						<xsl:if test="$debug='yes'">
							<xsl:result-document href="{concat('file:////',$applicationPath,'/consolidated/previousUsedComponents.xml')}" indent="yes" method="xml">
								<xsl:copy-of select="$previousUsedComponents"/>
							</xsl:result-document>
							<xsl:result-document href="{concat('file:////',$applicationPath,'/consolidated/currentUsedComponents.xml')}" indent="yes" method="xml">
								<xsl:copy-of select="$usedComponents"/>
							</xsl:result-document>
						</xsl:if>
						<!-- Eind: voor debug doeleinden -->

						</xsl:when>
					<!-- Deze otherwise tak roept het huidige template weer aan waardoor de content van de variabele 'usedComponents' worden verwerkt. 
						  De componenten waarvan al gecheckt is of ze verwijzen naar andere componenten (die dus in gebruik zijn) worden daarvoor eerst in een 'xs:checked' element geplaatst zodat deze in 
						  een volgende slag niet nogmaals verwerkt worden.
						  De componenten die nog niet gecheckt zijn blijven daarbuiten zodat daarvan wel gecheckt wordt of ze verwijzen naar andere componenten. -->
					<xsl:otherwise>
						<xsl:variable name="newUsedComponents">
							<xs:Used>
								<xs:checked>
									<xsl:choose>
										<xsl:when test="$usedComponents/xs:Used/xs:checked">
											<xsl:copy-of select="$usedComponents/xs:Used/xs:checked/*"/>
											<xsl:copy-of select="$usedComponents/xs:Used/*[name()!='xs:checked'][1]"/>
											<xsl:copy-of select="$usedComponents/xs:Used/*[name()!='xs:checked' and @id!=preceding::*/@id]"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:copy-of select="$usedComponents/xs:Used/*[1]"/>
											<xsl:copy-of select="$usedComponents/xs:Used/*[position()!=1 and @id!=preceding::*/@id]"/>
										</xsl:otherwise>
									</xsl:choose>
								</xs:checked>
								<xsl:copy-of select="$checkingUsedComponents/xs:Used/*[1]"/>   
								<xsl:copy-of select="$checkingUsedComponents/xs:Used/*[@id!=preceding::*/@id and @id!=$usedComponents//*/@id]"/>   
							</xs:Used>
						</xsl:variable>
						<xsl:call-template name="getUsedComponents">
							<xsl:with-param name="loop" select="$loop + 1"/>
							<xsl:with-param name="previousUsedComponents" select="$usedComponents"/>
							<xsl:with-param name="usedComponents">
								<xs:Used>
									<xs:checked>
										<xsl:copy-of select="$newUsedComponents/xs:Used/xs:checked/*"/>
									</xs:checked>
									<xsl:copy-of select="$newUsedComponents/xs:Used/*[name()!='xs:checked' and not(@id=preceding::*/@id)]"/>   
								</xs:Used>
							</xsl:with-param>
							<xsl:with-param name="allSchemas" select="$allSchemas"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<!-- Doordat het element 'xs:Used' in de variabele 'usedComponents' in eerste instantie nog leeg is trapt deze otherwise tak in feite het proces, waarbij de gebruikte componenten worden bepaald, af. 
				  Hier wordt het mainschema, de ingang voor de te verwerken berichtencatalogus, doorlopen en doorzocht op alle 'xs:element' elementen op root niveau.
				  Echter in het configuratie bestand kunnen berichten zijn gedefinieerd die naast de berichten in het mainschema ook meegenomen moeten worden.
				  Denk daarbij aan fout- of bevestigingsberichten. -->
			<xsl:otherwise>
				<xsl:variable name="checkingUsedComponents">
					<xs:Used>
						<!-- Eerst worden de standaard van toepassing zijnde berichten verwerkt. Deze zijn gedefinieerd in het configuratiebestand. -->
						<xsl:for-each select="$standardApplicableMessages//application:message">
							<xsl:variable name="namespace" select="@namespace"/>
							<xsl:variable name="messageName" select="."/>
							<xsl:apply-templates select="$allSchemas//xs:mySchema[@namespace=$namespace]/xs:schema/xs:element[@name=$messageName]" mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
						</xsl:for-each>
						<!-- Daarna alle elementen op toplevel niveau binnen het mainschema. -->
						<xsl:apply-templates select="$allSchemas//xs:mySchema[contains(@path,concat(substring-before($mainSchema,'.xsd'),'_resolved',$versie,'.xsd'))]/xs:schema/xs:element" mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
						
					</xs:Used>
				</xsl:variable>
				<xsl:call-template name="getUsedComponents">
					<xsl:with-param name="loop" select="$loop + 1"/>
					<xsl:with-param name="usedComponents" select="$checkingUsedComponents"/>
					<xsl:with-param name="allSchemas" select="$allSchemas"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Onderstaand template verwerkt de te checken componenten. 
		  Afhankelijk van het type element en de gebruikte attributen worden in de te genereren structuur bepaalde attributen gegenereert die de verdere verwerking weer sturen.
		  Daarna worden de gerelateerde componenten verwerkt. -->
	<xsl:template match="*" mode="processUsedComponents" exclude-result-prefixes="xs xsd">
		<xsl:param name="allSchemas"/>
		<xsl:variable name="namespace" select="@namespace"/>
		<xsl:choose>
			<xsl:when test="@elementtype">
				<xsl:variable name="type" select="@elementtype"/>
				<xsl:apply-templates select="$allSchemas//xs:mySchema[@namespace = $namespace]/xs:schema/xs:complexType[@name=substring-after($type,':')]" mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
				<xsl:apply-templates select="$allSchemas//xs:mySchema[@namespace = $namespace]/xs:schema/xs:simpleType[@name=substring-after($type,':')]" mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
			</xsl:when>
			<xsl:when test="@elementref">
				<xsl:variable name="ref" select="@elementref"/>
				<xsl:apply-templates select="$allSchemas//xs:mySchema[@namespace = $namespace]/xs:schema/xs:element[@name=substring-after($ref,':')]" mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
			</xsl:when>
			<xsl:when test="@groupref">
				<xsl:variable name="ref" select="@groupref"/>
				<xsl:apply-templates select="$allSchemas//xs:mySchema[@namespace = $namespace]/xs:schema/xs:group[@name=substring-after($ref,':')]" mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
			</xsl:when>
			<xsl:when test="@restrictiontype">
				<xsl:variable name="type" select="@restrictiontype"/>
				<xsl:apply-templates select="$allSchemas//xs:mySchema[@namespace = $namespace]/xs:schema/xs:complexType[@name=substring-after($type,':')]" mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
				<xsl:apply-templates select="$allSchemas//xs:mySchema[@namespace = $namespace]/xs:schema/xs:simpleType[@name=substring-after($type,':')]" mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
			</xsl:when>
			<xsl:when test="@extensiontype">
				<xsl:variable name="type" select="@extensiontype"/>
				<xsl:apply-templates select="$allSchemas//xs:mySchema[@namespace = $namespace]/xs:schema/xs:complexType[@name=substring-after($type,':')]" mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
				<xsl:apply-templates select="$allSchemas//xs:mySchema[@namespace = $namespace]/xs:schema/xs:simpleType[@name=substring-after($type,':')]" mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
			</xsl:when>
			<xsl:when test="@attributetype">
				<xsl:variable name="type" select="@attributetype"/>
				<xsl:apply-templates select="$allSchemas//xs:mySchema[@namespace = $namespace]/xs:schema/xs:simpleType[@name=substring-after($type,':')]" mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
			</xsl:when>
			<xsl:when test="@attributeref">
				<xsl:variable name="ref" select="@attributeref"/>
				<xsl:apply-templates select="$allSchemas//xs:mySchema[@namespace = $namespace]/xs:schema/xs:attribute[@name=substring-after($ref,':')]" mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
			</xsl:when>
			<xsl:when test="@attributegroupref">
				<xsl:variable name="ref" select="@attributegroupref"/>
				<xsl:apply-templates select="$allSchemas//xs:mySchema[@namespace = $namespace]/xs:schema/xs:attributeGroup[@name=substring-after($ref,':')]" mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="*"  mode="getUsedComponents"/>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>

	<xsl:template match="*" mode="getUsedComponents" exclude-result-prefixes="xs xsd">
		<xsl:apply-templates select="*"  mode="getUsedComponents"/>
	</xsl:template>

	<!-- Indien het gerelateerde component een 'xs:element' element is dan wordt afhankelijk van de aanwezige attributen een placeholder voor het/de (gerelateerde) element(en) gegenereert. -->
	<xsl:template match="xs:element" mode="getUsedComponents" exclude-result-prefixes="xs xsd">
		<xsl:choose>
			<!-- Indien het 'xs:element' naast een 'type' attribuut ook een 'substitutionGroup' attribuut bevat verwijst het dus naar 2 componenten en worden er dus 2 placeholders aangemaakt. -->
			<xsl:when test="@substitutionGroup and @type">
				<xs:element>
					<xsl:attribute name="elementref" select="@substitutionGroup"/>
					<xsl:attribute name="id" select="concat('element',@substitutionGroup)"/>
					<xsl:attribute name="namespace" select="namespace-uri-for-prefix(substring-before(@substitutionGroup,':'),.)"/>
				</xs:element>
				<xs:element>
					<xsl:attribute name="name" select="@name"/>
					<xsl:attribute name="elementtype" select="@type"/>
					<xsl:attribute name="prefix" select="ancestor::xs:mySchema/@prefix"/>
					<xsl:attribute name="id" select="concat('element',ancestor::xs:mySchema/@prefix,@name,@type)"/>
					<xsl:attribute name="namespace" select="namespace-uri-for-prefix(substring-before(@type,':'),.)"/>
				</xs:element>
			</xsl:when>
			<!-- Indien het 'xs:element' naast een 'ref' attribuut ook een 'substitutionGroup' attribuut bevat verwijst het dus naar 2 componenten en worden er dus 2 placeholders aangemaakt. -->
			<xsl:when test="@substitutionGroup and @ref">
				<xs:element>
					<xsl:attribute name="elementref" select="@substitutionGroup"/>
					<xsl:attribute name="id" select="concat('element',@substitutionGroup)"/>
					<xsl:attribute name="namespace" select="namespace-uri-for-prefix(substring-before(@substitutionGroup,':'),.)"/>
				</xs:element>
				<xs:element>
					<xsl:attribute name="elementref" select="@ref"/>
					<xsl:attribute name="id" select="concat('element',@ref)"/>
					<xsl:attribute name="namespace" select="namespace-uri-for-prefix(substring-before(@ref,':'),.)"/>
				</xs:element>
			</xsl:when>
			<!-- Ook indien het 'xs:element' alleen een 'substitutionGroup' attribuut bevat worden er 2 placeholders aangemaakt. 1 voor het element zelf en 1 voor de substitutionGroup. -->
			<xsl:when test="@substitutionGroup">
				<xs:element>
					<xsl:attribute name="elementref" select="@substitutionGroup"/>
					<xsl:attribute name="id" select="concat('element',@substitutionGroup)"/>
					<xsl:attribute name="namespace" select="namespace-uri-for-prefix(substring-before(@substitutionGroup,':'),.)"/>
				</xs:element>
				<xs:element>
					<xsl:attribute name="name" select="@name"/>
					<xsl:attribute name="prefix" select="ancestor::xs:mySchema/@prefix"/>
					<xsl:attribute name="id" select="concat('element',ancestor::xs:mySchema/@prefix,@name)"/>
					<xsl:attribute name="namespace" select="namespace-uri-for-prefix(ancestor::xs:mySchema/@prefix,.)"/>
					<xsl:apply-templates select="*"  mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
				</xs:element>
			</xsl:when>
			<!-- Indien het 'xs:element' een 'type' attribuut bevat wordt er 1 placeholder aangemaakt. -->
			<xsl:when test="@type">
				<xs:element>
					<xsl:attribute name="name" select="@name"/>
					<xsl:attribute name="elementtype" select="@type"/>
					<xsl:attribute name="prefix" select="ancestor::xs:mySchema/@prefix"/>
					<xsl:attribute name="id" select="concat('element',ancestor::xs:mySchema/@prefix,@name,@type)"/>
					<xsl:attribute name="namespace" select="namespace-uri-for-prefix(substring-before(@type,':'),.)"/>
				</xs:element>
			</xsl:when>
			<!-- Indien het 'xs:element' een 'ref' attribuut bevat wordt er 1 placeholder aangemaakt. -->
			<xsl:when test="@ref">
				<xs:element>
					<xsl:attribute name="elementref" select="@ref"/>
					<xsl:attribute name="id" select="concat('element',@ref)"/>
					<xsl:attribute name="namespace" select="namespace-uri-for-prefix(substring-before(@ref,':'),.)"/>
				</xs:element>
			</xsl:when>
			<!-- In alle andere gevallen wordt in ieder geval voor het element zelf een placeholder aangemaakt. -->
			<xsl:otherwise>
				<xs:element>
					<xsl:attribute name="name" select="@name"/>
					<xsl:attribute name="prefix" select="ancestor::xs:mySchema/@prefix"/>
					<xsl:attribute name="id" select="concat('element',ancestor::xs:mySchema/@prefix,@name)"/>
					<xsl:attribute name="namespace" select="namespace-uri-for-prefix(ancestor::xs:mySchema/@prefix,.)"/>
					<xsl:apply-templates select="*"  mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
				</xs:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Indien het gerelateerde component een 'xs:group' element is dan wordt er een placeholder voor het (gerelateerde) element en, afhankelijk van de aanwezige attributen, attributen gegenereert.
		  Ook wordt de daaronder liggende structuur verder verkend. -->
	<xsl:template match="xs:group" mode="getUsedComponents" exclude-result-prefixes="xs xsd">
		<xs:group>
			<xsl:choose>
				<xsl:when test="@ref">
					<xsl:attribute name="groupref" select="@ref"/>
					<xsl:attribute name="id" select="concat('group',@ref)"/>
					<xsl:attribute name="namespace" select="namespace-uri-for-prefix(substring-before(@ref,':'),.)"/>
					<xsl:apply-templates select="*"  mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="name" select="@name"/>
					<xsl:attribute name="prefix" select="ancestor::xs:mySchema/@prefix"/>
					<xsl:attribute name="id" select="concat('group',ancestor::xs:mySchema/@prefix,@name)"/>
					<xsl:attribute name="namespace" select="namespace-uri-for-prefix(ancestor::xs:mySchema/@prefix,.)"/>
					<xsl:apply-templates select="*"  mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
				</xsl:otherwise>
			</xsl:choose>
		</xs:group>
	</xsl:template>

	<!-- Indien het gerelateerde component een 'xs:extension' element is dan wordt een placeholder voor het element gegenereert. -->
	<xsl:template match="xs:extension" mode="getUsedComponents" exclude-result-prefixes="xs xsd">
		<xs:extension>
			<xsl:if test="@base">
				<xsl:attribute name="extensiontype" select="@base"/>
				<xsl:attribute name="prefix" select="ancestor::xs:mySchema/@prefix"/>
				<xsl:attribute name="id" select="concat('extension',ancestor::xs:mySchema/@prefix,@base)"/>
				<xsl:attribute name="namespace" select="namespace-uri-for-prefix(substring-before(@base,':'),.)"/>
				<xsl:apply-templates select="*"  mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
			</xsl:if>
		</xs:extension>
	</xsl:template>

	<!-- Indien het gerelateerde component een 'xs:restriction' element is dan wordt een placeholder voor het element gegenereert. -->
	<xsl:template match="xs:restriction" mode="getUsedComponents" exclude-result-prefixes="xs xsd">
		<xs:restriction>
			<xsl:if test="@base">
				<xsl:attribute name="restrictiontype" select="@base"/>
				<xsl:attribute name="prefix" select="ancestor::xs:mySchema/@prefix"/>
				<xsl:attribute name="id" select="concat('restriction',ancestor::xs:mySchema/@prefix,@base)"/>
				<xsl:attribute name="namespace" select="namespace-uri-for-prefix(substring-before(@base,':'),.)"/>
				<xsl:apply-templates select="*"  mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
			</xsl:if>
		</xs:restriction>
	</xsl:template>

	<!-- Indien het gerelateerde component een 'xs:simpleType' element is met een 'name' attribuut dan wordt een placeholder voor het element in alle andere gevallen wordt de structuur daaronder verder verwerkt. -->
	<xsl:template match="xs:simpleType" mode="getUsedComponents" exclude-result-prefixes="xs xsd">
		<xsl:choose>
			<xsl:when test="@name">
				<xs:simpleType>
					<xsl:attribute name="name" select="@name"/>
					<xsl:attribute name="prefix" select="ancestor::xs:mySchema/@prefix"/>
					<xsl:attribute name="id" select="concat('simpleType',ancestor::xs:mySchema/@prefix,@name)"/>
					<xsl:attribute name="namespace" select="namespace-uri-for-prefix(ancestor::xs:mySchema/@prefix,.)"/>
					<xsl:apply-templates select="*"  mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
				</xs:simpleType>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="*"  mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- Indien het gerelateerde component een 'xs:complexType' element is met een 'name' attribuut dan wordt een placeholder voor het element in alle andere gevallen wordt de structuur daaronder verder verwerkt. -->
	<xsl:template match="xs:complexType" mode="getUsedComponents" exclude-result-prefixes="xs xsd">
		<xsl:choose>
			<xsl:when test="@name">
				<xs:complexType>
					<xsl:attribute name="name" select="@name"/>
					<xsl:attribute name="prefix" select="ancestor::xs:mySchema/@prefix"/>
					<xsl:attribute name="id" select="concat('complexType',ancestor::xs:mySchema/@prefix,@name)"/>
					<xsl:attribute name="namespace" select="namespace-uri-for-prefix(ancestor::xs:mySchema/@prefix,.)"/>
					<xsl:apply-templates select="*"  mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
				</xs:complexType>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="*"  mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Indien het gerelateerde component een 'xs:attribute' element is dan wordt er een placeholder voor het (gerelateerde) element gegenereert en, afhankelijk van de aanwezige attributen, attributen gegenereert.
		  Ook wordt de eventuele daaronder liggende structuur verder verkend. -->
	<xsl:template match="xs:attribute" mode="getUsedComponents" exclude-result-prefixes="xs xsd">
		<xs:attribute>
			<xsl:choose>
				<!-- Indien het 'xs:element' een 'type' attribuut bevat wordt er 1 placeholder aangemaakt. -->
				<xsl:when test="@type">
					<xsl:attribute name="name" select="@name"/>
					<xsl:attribute name="attributetype" select="@type"/>
					<xsl:attribute name="prefix" select="ancestor::xs:mySchema/@prefix"/>
					<xsl:attribute name="id" select="concat('attribute',ancestor::xs:mySchema/@prefix,@name,@type)"/>
					<xsl:attribute name="namespace" select="namespace-uri-for-prefix(substring-before(@type,':'),.)"/>
				</xsl:when>
				<!-- Indien het 'xs:element' een 'ref' attribuut bevat wordt er 1 placeholder aangemaakt. -->
				<xsl:when test="@ref">
					<xsl:attribute name="attributeref" select="@ref"/>
					<xsl:attribute name="id" select="concat('attribute',@ref)"/>
					<xsl:attribute name="namespace" select="namespace-uri-for-prefix(substring-before(@ref,':'),.)"/>
				</xsl:when>
				<!-- In alle andere gevallen wordt in ieder geval voor het element zelf een placeholder aangemaakt. -->
				<xsl:otherwise>
					<xsl:attribute name="name" select="@name"/>
					<xsl:attribute name="prefix" select="ancestor::xs:mySchema/@prefix"/>
					<xsl:attribute name="id" select="concat('attribute',ancestor::xs:mySchema/@prefix,@name)"/>
					<xsl:attribute name="namespace" select="namespace-uri-for-prefix(ancestor::xs:mySchema/@prefix,.)"/>
					<xsl:apply-templates select="*"  mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
				</xsl:otherwise>
			</xsl:choose>
		</xs:attribute>
	</xsl:template>

	<!-- Indien het gerelateerde component een 'xs:attributeGroup' element is dan wordt er een placeholder voor het (gerelateerde) element gegenereert en, afhankelijk van de aanwezige attributen, attributen gegenereert.
		  Ook wordt de daaronder liggende structuur verder verkend. -->
	<xsl:template match="xs:attributeGroup" mode="getUsedComponents" exclude-result-prefixes="xs xsd">
		<xs:attributeGroup>
			<xsl:choose>
				<xsl:when test="@ref">
					<xsl:attribute name="attributegroupref" select="@ref"/>
					<xsl:attribute name="id" select="concat('attributeGroup',@ref)"/>
					<xsl:attribute name="namespace" select="namespace-uri-for-prefix(substring-before(@ref,':'),.)"/>
					<xsl:apply-templates select="*"  mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="name" select="@name"/>
					<xsl:attribute name="prefix" select="ancestor::xs:mySchema/@prefix"/>
					<xsl:attribute name="id" select="concat('attributeGroup',ancestor::xs:mySchema/@prefix,@name)"/>
					<xsl:attribute name="namespace" select="namespace-uri-for-prefix(ancestor::xs:mySchema/@prefix,.)"/>
					<xsl:apply-templates select="*"  mode="getUsedComponents" exclude-result-prefixes="xs xsd"/>
				</xsl:otherwise>
			</xsl:choose>
		</xs:attributeGroup>
	</xsl:template>
	
</xsl:stylesheet>