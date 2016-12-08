<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:snp="http://www.kadaster.nl/schemas/brk-levering/snapshot/v20120901" xmlns:ko="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-kadastraalobject/v20120701" xmlns:typ="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-typen/v20120201" xmlns:nen="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-NEN3610-2011/v20120201" xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:recht="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-recht/v20120201" xmlns:rechtref="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-recht-ref/v20120201" xmlns:pers="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-persoon/v20120201" xmlns:nhr="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-nhr-rechtspersoon/v20120201" xmlns:gba="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-gba-persoon/v20120901" xmlns:GbaPersoonRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-gba-persoon-ref/v20120201" xmlns:Stuk="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-stuk/v20120201" xmlns:bagadres="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-bag-adres/v20120201" xmlns:fn="http://www.w3.org/2005/xpath-functions">
	<xsl:template match="/">
		<root>
			<data>
				<xsl:for-each select="/snp:KadastraalObjectSnapshot/gba:NietIngezetene | /snp:KadastraalObjectSnapshot/gba:Ingezetene">
					<ingeschr_nat_prs>
						<xsl:call-template name="geregistreerd_persoon-ingeschr_nat_persoon">
							<xsl:with-param name="persoon" select="."/>
						</xsl:call-template>
					</ingeschr_nat_prs>
				</xsl:for-each>
			</data>
		</root>
	</xsl:template>
	<xsl:template name="geregistreerd_persoon-ingeschr_nat_persoon">
		<xsl:param name="persoon"/>
		<sc_identif>
			<xsl:call-template name="nen_identificatie">
				<xsl:with-param name="id" select="$persoon/pers:identificatie"/>
			</xsl:call-template>
		</sc_identif>
		<bsn>
			<xsl:value-of select="$persoon/gba:BSN"/>
		</bsn>
                <gb_geboorteplaats>
                    <xsl:value-of select="$persoon/gba:geboorte/gba:geboorteplaats"/>
                </gb_geboorteplaats>
                <gb_geboortedatum>
                                <xsl:for-each select="$persoon/gba:geboorte/gba:geboortedatum"><xsl:call-template name="numeric-date"/></xsl:for-each>
                </gb_geboortedatum>
                <ol_overlijdensdatum>
                    <xsl:for-each select="$persoon/gba:overlijden/gba:datumOverlijden"><xsl:call-template name="numeric-date"/></xsl:for-each>
                </ol_overlijdensdatum>
                <xsl:for-each select="$persoon/pers:woonlocatie">
                                <va_loc_beschrijving>
                                        <xsl:call-template name="describe-locatie"/>
                                </va_loc_beschrijving>
                </xsl:for-each>
        </xsl:template>        
	<xsl:template name="nen_identificatie">
		<xsl:param name="id"/>
		<xsl:value-of select="$id/nen:namespace"/>.<xsl:value-of select="$id/nen:lokaalId"/>
	</xsl:template>
	<!-- jjjj-mm-dd -> jjjjmmdd -->
	<xsl:template name="numeric-date">
		<xsl:value-of select="concat(substring(.,1,4),substring(.,6,2),substring(.,9,2))"/>
	</xsl:template>
	<xsl:template name="describe-locatie" xmlns:adres="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-adres/v20120201">
		<xsl:for-each select="adres:PostbusAdres">
			Postbus <xsl:value-of select="adres:postbusnummer"/>, <xsl:value-of select="adres:postcode"/><xsl:text> </xsl:text><xsl:value-of select="adres:woonplaatsNaam"/>
		</xsl:for-each>
		<xsl:for-each select="adres:KADBinnenlandsAdres">
			<xsl:value-of select="adres:openbareRuimteNaam"/><xsl:text> </xsl:text><xsl:value-of select="adres:huisNummer"/><xsl:for-each select="adres:huisNummerToevoeging"><xsl:text> </xsl:text><xsl:value-of select="."/></xsl:for-each><xsl:for-each select="adres:huisLetter"><xsl:text> </xsl:text><xsl:value-of select="."/></xsl:for-each>, <xsl:value-of select="adres:postcode"/><xsl:text> </xsl:text><xsl:value-of select="adres:woonplaatsNaam"/>
		</xsl:for-each>
		<xsl:for-each select="bagadres:Ligplaats | bagadres:Standplaats | bagadres:Verlijfsobject">
			BAG ID: <xsl:value-of select="bagadres:BAGIdentificatie"/>
		</xsl:for-each>
		<xsl:for-each select="gba:BuitenlandsAdres">
			<xsl:value-of select="gba:adres"/>, <xsl:value-of select="gba:woonplaats"/><xsl:for-each select="gba:regio">, <xsl:value-of select="."/></xsl:for-each>, <xsl:value-of select="gba:land/typ:waarde"/>
		</xsl:for-each>
		<xsl:for-each select="adres:KADBuitenlandsAdres">
			<xsl:value-of select="adres:adres"/>, <xsl:value-of select="adres:woonplaats"/><xsl:for-each select="adres:regio">, <xsl:value-of select="adres:regio"/></xsl:for-each>, <xsl:value-of select="adres:land"/>
		</xsl:for-each>
	</xsl:template>        
</xsl:stylesheet>
