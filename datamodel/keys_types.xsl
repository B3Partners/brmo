<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:b3p="http://www.b3partners.nl/">
	<!-- Primary keys en types daarvan -->
	<!-- primary key property per RSGB class -->
	<xsl:variable name="class-primary-keys" as="element(entry)*">
		<entry class="ADRESSEERBAAR OBJECT AANDUIDING"><key>Identificatie adresseerbaar object aanduiding</key><key archief="true">Datum begin geldigheid addresserbaar object aanduiding</key></entry>
		<entry class="ANDER BUITENLANDS NIET-NATUURLIJK PERSOON"><key>Nummer ander buitenlands niet-natuurlijk persoon</key></entry>
		<entry class="ANDER NATUURLIJK PERSOON"><key>Nummer ander natuurlijk persoon</key></entry>
		<entry class="BENOEMD OBJECT"><key>Benoemd object identificatie</key></entry>
		<entry class="KADASTRALE GEMEENTE"><key>Kadastrale gemeentecode</key></entry>
		<entry class="KADASTRALE ONROERENDE ZAAK AANTEKENING"><key>Kadaster identificatie aantekening</key><key archief="true">Begindatum aantekening kadastraal object</key></entry>
		<entry class="LIGPLAATS"><key>Ligplaatsidentificatie</key></entry>
		<entry class="MAATSCHAPPELIJKE ACTIVITEIT"><key>KvK-nummer</key></entry>
		<entry class="NUMMERAANDUIDING"><key>Identificatiecode nummeraanduiding</key></entry>
		<entry class="ONDERNEMING"><key>KvK-nummer</key></entry>
		<entry class="OPENBARE RUIMTE"><key>Identificatiecode openbare ruimte</key></entry>
		<entry class="OVERIGE ADRESSEERBAAR OBJECT AANDUIDING"><key>Identificatiecode overig adresseerbaar object aanduiding</key></entry>
		<entry class="GEBOUWD OBJECT"><key archief="true">Datum begin geldigheid gebouwd object</key></entry>
		<entry class="OVERIG GEBOUWD OBJECT"><key>Overig gebouwd object identificatie</key></entry>
		<entry class="OVERIG TERREIN"><key>Overig  terrein identificatie</key></entry>
		<entry class="REISDOCUMENT"><key>Reisdocumentnummer</key></entry>
		<entry class="STANDPLAATS"><key>Standplaatsidentificatie</key></entry>
		<entry class="SUBJECT"><key>Identificatie</key></entry>
		<entry class="VERBLIJFSOBJECT"><key>Verblijfsobjectidentificatie</key></entry>
		<entry class="VESTIGING"><key>Vestigingsnummer</key></entry>
		<entry class="ZAKELIJK RECHT"><key>Kadaster identificatie zakelijk recht</key></entry>
		<entry class="ZAKELIJK RECHT AANTEKENING"><key>Kadaster identificatie aantekening recht</key></entry>
		<entry class="BEGROEID TERREINDEEL"><key>Identificatie begroeid terreindeel</key><key archief="true">Datum begin geldigheid begroeid terreindeel</key></entry>
		<entry class="BUURT"><key>Buurtcode</key><key>wijk</key><key archief="true">Datum begin geldigheid buurt</key></entry>
		<entry class="FUNCTIONEEL GEBIED"><key>Identificatie functioneel gebied</key><key archief="true">Datum begin geldigheid functioneel gebied</key></entry>
		<entry class="GEBOUWINSTALLATIE"><key>Identificatie gebouwinstallatie</key><key archief="true">Datum begin geldigheid gebouwinstallatie</key></entry>
		<entry class="GEMEENTE"><key>Gemeentecode</key><key archief="true">Datum begin geldigheid gemeente</key></entry>
		<entry class="GEMEENTELIJKE OPENBARE RUIMTE"><key>Identificatiecode gemeentelijke openbare ruimte</key><key archief="true">Datum begin geldigheid gemeentelijke openbare ruimte</key></entry>
		<entry class="HUISHOUDEN"><key>Huishoudennummer</key><key archief="true">Datum begin geldigheid huishouden</key></entry>
		<entry class="INRICHTINGSELEMENT"><key>Identificatie inrichtingselement</key><key archief="true">Datum begin geldigheid inrichtingselement</key></entry>
		<entry class="KADASTRALE ONROERENDE ZAAK"><key>Kadastrale identificatie</key><key archief="true">Datum begin geldigheid kadastrale onroerende zaak</key></entry>
		<entry class="KUNSTWERKDEEL"><key>Identificatie kunstwerkdeel</key><key archief="true">Datum begin geldigheid kunstwerkdeel</key></entry>
		<entry class="ONBEGROEID TERREINDEEL"><key>Identificatie onbegroeid terreindeel</key><key archief="true">Datum begin geldigheid onbegroeid terreindeel</key></entry>
		<entry class="ONDERSTEUNEND WEGDEEL"><key>Identificatie ondersteunend wegdeel</key><key archief="true">Datum begin geldigheid ondersteunend wegdeel</key></entry>
		<entry class="OVERIG BOUWWERK"><key>Identificatie overig bouwwerk</key><key archief="true">Datum begin geldigheid overig bouwwerk</key></entry>
		<entry class="OVERIGE SCHEIDING"><key>Identificatie overige scheiding</key><key archief="true">Datum begin geldigheid overige scheiding</key></entry>
		<entry class="PAND"><key>Pandidentificatie</key><key archief="true">Datum begin geldigheid pand</key></entry>
		<entry class="SCHEIDING"><key>Identificatie scheiding</key><key archief="true">Datum begin geldigheid scheiding</key></entry>
		<entry class="SPOOR"><key>Identificatie spoor</key><key archief="true">Datum begin geldigheid spoor</key></entry>
		<entry class="STADSDEEL"><key>Identificatie stadsdeel</key><key archief="true">Datum begin geldigheid stadsdeel</key></entry>
		<entry class="VRIJSTAAND VEGETATIE OBJECT"><key>Identificatie vrijstaand vegetatie object</key><key archief="true">Datum begin geldigheid vrijstaand vegetatie object</key></entry>
		<entry class="WATERDEEL"><key>Identificatie waterdeel</key><key archief="true">Datum begin geldigheid waterdeel</key></entry>
		<entry class="WATERSCHAP"><key>Identificatie waterschap</key><key archief="true">Datum begin geldigheid waterschap</key></entry>
		<entry class="WEGDEEL"><key>Identificatie wegdeel</key><key archief="true">Datum begin geldigheid wegdeel</key></entry>
		<entry class="WIJK"><key>Wijkcode</key><key>gemeente</key><key archief="true">Datum begin geldigheid wijk</key></entry>
		<entry class="WOONPLAATS"><key>Woonplaatsidentificatie</key><key archief="true">Datum begin geldigheid woonplaats</key></entry>
		<entry class="WOZ-OBJECT"><key>WOZ-objectnummer</key><key archief="true">Datum begin geldigheid WOZ-object</key></entry>
		<entry class="WOZ-DEELOBJECT"><key>Nummer WOZ-deelobject</key><key archief="true">Datum begin geldigheid deelobject</key></entry>
		<entry class="VERBLIJFSTITEL"><key>Aanduiding verblijfstitel</key><key archief="true">Begindatum geldigheid verblijfstitel</key></entry>
		<entry class="WOZ-WAARDE"><key archief="true">Waardepeildatum</key></entry>
		<entry class="BENOEMD TERREIN"><key archief="true">Datum begin geldigheid benoemd terrein</key></entry>
		
		<!-- Referentie lijsten classes -->
		<entry class="SBI ACTIVITEIT"><key>SBI code</key></entry>
		<entry class="AARD RECHT VERKORT"><key>Aanduiding aard recht verkort</key></entry>
		<entry class="AARD VERKREGEN RECHT"><key>Aanduiding aard verkregen recht</key></entry>
		<entry class="ACADEMISCHE TITEL"><key>Academische titelcode</key></entry>
		<entry class="LAND"><key>Landcode ISO</key></entry>
		<entry class="NATIONALITEIT"><key>Nationaliteitcode</key></entry>
		<entry class="REISDOCUMENTSOORT"><key>Reisdocumentcode</key></entry>
	</xsl:variable>
	
	<!-- Context: /RSGB/Objecttypes/Class -->
	<!-- Geef een copy-of het Property element welke de primary key moet zijn -->
	<xsl:template name="get-class-primary-key-properties">
		<xsl:variable name="class-name" select="@name"/>
		<xsl:for-each select="Property">
			<xsl:if test="$class-primary-keys[@class = $class-name]/key = @name">
				<Property>
					<xsl:copy-of select="@*"/>
					<xsl:if test="$class-primary-keys[@class = $class-name]/key[@archief = 'true'] = @name">
						<xsl:attribute name="archief" select="'true'"/>
					</xsl:if>
				</Property>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>


	<!-- Context: /RSGB/Objecttyeps/Class/Property of /RSGB/Objecttypes/Property[@elementType= 'Objecttype/Referentielijsten - Property'] -->
	<!-- Geeft Class element met name attribuut met als childs de primary key Properties,
		en indien de class een superclass heeft als following-sibling een zelfde
		Class element van de superclass, recursief. Werkt ook voor referentielijsten.
    -->
	<xsl:template name="get-class-primary-key-properties-hierarchy">
		<xsl:param name="name"/>
		<xsl:param name="prefix" select="''"/>

		<xsl:variable name="class-object" select="$rsgb/Objecttypes/Class[@name = $name]"/>
		<xsl:choose>
			<!-- Is het een class? Zo niet, dan gaan we er vanuit dat het een referentielijst is -->
			<xsl:when test="$class-object">
		    <xsl:for-each select="$rsgb/Objecttypes/Class[@name = $name]">
			    <Class name="{@name}">
				    <xsl:variable name="properties">
					    <xsl:call-template name="get-class-primary-key-properties"/>
				    </xsl:variable>
				    <xsl:for-each select="$properties/Property">
					    <xsl:choose>
						    <xsl:when test="b3p:getElementType(.) = 'simple'">
							    <Property column-name="{concat($prefix,b3p:property-to-column(@name,$name))}">
								    <xsl:copy-of select="@*"/>
								    <xsl:value-of select="@name"/><xsl:text> - </xsl:text><xsl:value-of select="$name"/>
							    </Property>
						    </xsl:when>
						    <xsl:otherwise>
							    <xsl:message terminate="yes">Associaties naar een class die een associatie als primary key heeft worden niet ondersteund</xsl:message>
						    </xsl:otherwise>
					    </xsl:choose>
				    </xsl:for-each>
			    </Class>
			    <xsl:if test="@superclass">
				    <xsl:call-template name="get-class-primary-key-properties-hierarchy">
					    <xsl:with-param name="name" select="@superclass"/>
					    <xsl:with-param name="prefix" select="'sc_'"/>
				    </xsl:call-template>
			    </xsl:if>
		    </xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="ref-class" select="$rsgb/Objecttypes/Property[@elementType = 'Objecttype/Referentielijsten - Property' and @className = $name]"/>
				<xsl:if test="$ref-class">
					<xsl:variable name="stub-class">
						<Class name="{$name}">
							<xsl:for-each select="$ref-class">
								<Property name="{@name}" column-name="{concat($prefix,b3p:property-to-column(@name,$name))}" type="{@type}" typeName="{@typeName}"/>
							</xsl:for-each>
						</Class>
					</xsl:variable>
					<Class name="{$name}">
						<xsl:for-each select="$stub-class/Class">
							<xsl:call-template name="get-class-primary-key-properties"/>
						</xsl:for-each>
					</Class>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	
	</xsl:template>
</xsl:stylesheet>
