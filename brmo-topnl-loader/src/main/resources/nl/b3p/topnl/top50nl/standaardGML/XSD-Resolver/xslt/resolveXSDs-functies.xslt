<?xml version="1.0" encoding="UTF-8"?>
<!--	This XSLT stylesheet belongs to a set of XSLT stylesheets which are used to proces a set of XML-Schema's to enable them to be used in code generation.
		 
		Copyright (C) 2013  Robert Melskens, KING

		This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
		License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.

		This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
		MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.

		You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software
		Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA


		2012-12-10	0.1	Robert Melskens	Bevat alle in de XSD-Resolver gebruikte functies. -->
		
<!-- LET OP! Het afhandelen van http adressen gaat nog niet goed. Hier moet beslist nog tijd in gestoken worden. -->



<xsl:stylesheet exclude-result-prefixes="fn schemaReferences application xs xsd" version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/2001/XMLSchema" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:application="http://www.kinggemeenten.nl/2011/applicationConfiguration" xmlns:schemaReferences="http://www.kinggemeenten.nl/2011/schemaReferences">

	<!--	****************************************************************************************************************
			Functie templates
			****************************************************************************************************************-->

	<!-- Dit template dient om een filenaam of een absoluut path zonder filenaam te extraheren
			Parameters:
			action:		Bevat de uit te voeren actie.
							'extractDir' betekend dat een path zonder filenaam uit het opgegeven path wordt geextraheerd.
							'extractFileName' betekend dat de filenaam uit het opgegeven path wordt geextraheerd.
			filePath:	Bevat het path waarop de gewenste actie moet worden uitgevoerd.
			 
			Output van deze functie is een filenaam of een path niet eindigend op een '/'.-->
	<xsl:template name="extractDirOrFileName" exclude-result-prefixes="xs xsd">
		<xsl:param name="action"/>
		<xsl:param name="filePath"/>
		<xsl:param name="filePathWithoutFileName" select="''"/>
		<xsl:choose>
			<xsl:when test="contains(substring-after($filePath,'/'),'/')">
				<xsl:call-template name="extractDirOrFileName">
					<xsl:with-param name="action" select="$action"/>
					<xsl:with-param name="filePath" select="substring-after($filePath,'/')"/>
					<xsl:with-param name="filePathWithoutFileName" select="concat($filePathWithoutFileName,substring-before($filePath,'/'),'/')"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="$action='extractDir'">
						<xsl:call-template name="transformRelative2AbsolutePath">
							<xsl:with-param name="filePathOrRemainder" select="concat($filePathWithoutFileName,substring-before($filePath,'/'))"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="$action='extractFileName'">
						<xsl:value-of select="substring-after($filePath,'/')"/>
					</xsl:when>				
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="constructRelativePath" exclude-result-prefixes="xs xsd">
		<xsl:param name="path"/>
		<xsl:param name="schemaLocation"/>
		<xsl:choose>
			<xsl:when test="contains($schemaLocation,$path)">
				<xsl:value-of select="substring-after($schemaLocation,concat($path,'/'))"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="commonPart">
					<xsl:call-template name="constructCommonPart">
						<xsl:with-param name="schemaLocation" select="$schemaLocation"/>
						<xsl:with-param name="path" select="$path"/>
						<xsl:with-param name="commonPart" select="substring-before($path,'/')"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="fixedPart" select="substring-after($schemaLocation,concat($commonPart,'/'))"/>
				<xsl:variable name="relativePart">
					<xsl:call-template name="constructRelativePart">
						<xsl:with-param name="Part2BConverted" select="substring-after($path,concat($commonPart,'/'))"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:value-of select="concat($relativePart,$fixedPart)"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="constructRelativePart" exclude-result-prefixes="xs xsd">
		<xsl:param name="Part2BConverted"/>
		<xsl:param name="relativePart" select="''"/>
		<xsl:choose>
			<xsl:when test="contains($Part2BConverted,'/')">
				<xsl:call-template name="constructRelativePart">
					<xsl:with-param name="Part2BConverted" select="substring-after($Part2BConverted,'/')"/>
					<xsl:with-param name="relativePart" select="concat($relativePart,'../')"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise><xsl:value-of select="concat($relativePart,'../')"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="constructCommonPart" exclude-result-prefixes="xs xsd">
		<xsl:param name="schemaLocation"/>
		<xsl:param name="path"/>
		<xsl:param name="commonPart"/>
		<xsl:choose>
			<xsl:when test="not(contains($schemaLocation,concat($commonPart,'/',substring-before(substring-after($path,concat($commonPart,'/')),'/'))))">
				<xsl:value-of select="$commonPart"/>
			</xsl:when>
			<xsl:when test="substring-before(substring-after($path,concat($commonPart,'/')),'/')=''">
				<xsl:value-of select="$commonPart"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="constructCommonPart">
					<xsl:with-param name="schemaLocation" select="$schemaLocation"/>
					<xsl:with-param name="path" select="$path"/>
					<xsl:with-param name="commonPart" select="concat($commonPart,'/',substring-before(substring-after($path,concat($commonPart,'/')),'/'))"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Onderstaande templates dienen om een path zodanig op te schonen dat alle evt. '/..' strings uit het pad verwijderd worden waarbij het pad toch correct blijft.
	
		Parameters:
		filePathOrRemainder:		Bevat het path dat naar een absoluut path omgezet moet worden.
											In het path moeten vooraf natuurlijk wel alle '\' omgezet worden naar '/'. 
		filePathRemainderPart1:	Bevat het reeds verwerkte deel van het path. In de initiele aanroep hoeft deze paramter niet meegenomen te worden.
		
		Output van deze functie is een correct path waarin alle '/..' verwijderd zijn.-->
	 
	<xsl:template name="transformRelative2AbsolutePath" exclude-result-prefixes="xs xsd">
		<xsl:param name="filePathOrRemainder"/>
		<xsl:param name="filePathRemainderPart1" select="''"/>
		<xsl:choose>
			<xsl:when test="contains($filePathOrRemainder,'/..')">
				<xsl:variable name="Part1">
					<xsl:call-template name="cleanPart1">
						<xsl:with-param name="Part1" select="concat($filePathRemainderPart1,substring-before($filePathOrRemainder,'/..'))"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:call-template name="transformRelative2AbsolutePath">
					<xsl:with-param name="filePathOrRemainder" select="substring-after($filePathOrRemainder,'/..')"/>
					<xsl:with-param name="filePathRemainderPart1" select="$Part1"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="concat($filePathRemainderPart1,$filePathOrRemainder)"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--Daadwerkelijke verwijdering van de '/..' string vindt in dit template plaats. -->
	<xsl:template name="cleanPart1" exclude-result-prefixes="xs xsd">
		<xsl:param name="Part1"/> 
		<xsl:param name="Part1a" select="''"/>
		<xsl:choose>
			<xsl:when test="contains($Part1,'/')">
				<xsl:call-template name="cleanPart1">
					<xsl:with-param name="Part1" select="substring-after($Part1,'/')"/>
					<xsl:with-param name="Part1a" select="concat(translate(translate($Part1a,'#','/'),'//','/'),substring-before($Part1,'/'),'#')"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="contains($Part1a,'#')">
				<xsl:value-of select="substring-before($Part1a,'#')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$Part1a"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
		<xsl:template name="getLargestSchemaList_Lp">
		<xsl:param name="currentLoop" select="'1'"/>
		<xsl:choose>
			<xsl:when test="doc-available(concat('file:////',$applicationPath,'/generated/schemaList_Lp',$currentLoop,'.xml'))">
				<xsl:call-template name="getLargestSchemaList_Lp">
					<xsl:with-param name="currentLoop" select="$currentLoop + 1"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="document(concat('file:////',$applicationPath,'/generated/schemaList_Lp',$currentLoop - 1,'.xml'))"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


</xsl:stylesheet>