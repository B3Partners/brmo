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
		2013-09-25	0.11	Robert Melskens	Voorzieningen getroffen zodat de bestandsnaam van de te genereren bestanden kan eindigen op '_resolved.xsd'. -->
		
		
<xsl:stylesheet exclude-result-prefixes="fn schemaReferences application xs xsd" version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:application="http://www.kinggemeenten.nl/2011/applicationConfiguration" xmlns:schemaReferences="http://www.kinggemeenten.nl/2011/schemaReferences">

	<!-- Het volgende template plaatst alle geconsolideerde schema's bij elkaar in 1 bestand. 
		  Elk 'schema' element wordt daarbij binnen een 'mySchema' element geplaatst met daarbij een 'path' attribuut met als waarde het path waarop het uiteindelijke schema bewaard moet worden. -->
	<xsl:template match="xs:schemaSet" mode="generateConsolidatedSchemaSet">
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
				<xs:mySchema path="{concat('file:////',$applicationPath,'/finalized/',substring-before($schemaFile,'.xsd'),'_resolved',$versie,'.xsd')}">
					<xsl:attribute name="prefix">
						<xsl:variable name="schema" select="document(concat('file:////',$applicationPath,'/consolidated/',$schemaFile))"/>
						<xsl:value-of select="name($schema/xs:schema/namespace::*[.=../@targetNamespace])"/>
					</xsl:attribute>
					<xsl:attribute name="namespace">
						<xsl:variable name="schema" select="document(concat('file:////',$applicationPath,'/consolidated/',$schemaFile))"/>
						<xsl:value-of select="$schema/xs:schema/namespace::*[.=../@targetNamespace]"/>
					</xsl:attribute>
					<xsl:copy-of select="document(concat('file:////',$applicationPath,'/consolidated/',$schemaFile))"/>
				</xs:mySchema>
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
				<xs:mySchema path="{concat('file:////',$applicationPath,'/finalized/',substring-before($schemaFile,'.xsd'),'_resolved',$versie,'.xsd')}">
					<xsl:attribute name="prefix">
						<xsl:variable name="schema" select="document(concat('file:////',$applicationPath,'/consolidated/',$schemaFile))"/>
						<xsl:value-of select="name($schema/xs:schema/namespace::*[.=../@targetNamespace])"/>
					</xsl:attribute>
					<xsl:attribute name="namespace">
						<xsl:variable name="schema" select="document(concat('file:////',$applicationPath,'/consolidated/',$schemaFile))"/>
						<xsl:value-of select="$schema/xs:schema/namespace::*[.=../@targetNamespace]"/>
					</xsl:attribute>
					<xsl:copy-of select="document(concat('file:////',$applicationPath,'/consolidated/',$schemaFile))"/>
				</xs:mySchema>
			</xsl:when>
			<!-- In alle andere gevallen (als de schemaSet geen rootSchema bevat maar wel meerdere entries) dan wordt deze otherwise tak gebruikt. -->
			<xsl:otherwise>
				<xsl:variable name="schemaFile">
					<xsl:call-template name="extractDirOrFileName">
						<xsl:with-param name="action" select="'extractFileName'"/>
						<xsl:with-param name="filePath" select="*[1]/@schemaLocation"/>
					</xsl:call-template>
				</xsl:variable>
				<xs:mySchema path="{concat('file:////',$applicationPath,'/finalized/',substring-before($schemaFile,'.xsd'),'_resolved',$versie,'.xsd')}">
					<xsl:attribute name="prefix">
						<xsl:variable name="schema" select="document(concat('file:////',$applicationPath,'/consolidated/',$schemaFile))"/>
						<xsl:value-of select="name($schema/xs:schema/namespace::*[.=../@targetNamespace])"/>
					</xsl:attribute>
					<xsl:attribute name="namespace">
						<xsl:variable name="schema" select="document(concat('file:////',$applicationPath,'/consolidated/',$schemaFile))"/>
						<xsl:value-of select="$schema/xs:schema/namespace::*[.=../@targetNamespace]"/>
					</xsl:attribute>
					<xsl:copy-of select="document(concat('file:////',$applicationPath,'/consolidated/',$schemaFile))"/>
				</xs:mySchema>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


</xsl:stylesheet>