<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:snp="http://www.kadaster.nl/schemas/brk-levering/snapshot/v20120901" xmlns:ko="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-kadastraalobject/v20120701" xmlns:typ="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-typen/v20120201" xmlns:nen="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-NEN3610-2011/v20120201" xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:recht="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-recht/v20120201" xmlns:rechtref="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-recht-ref/v20120201" xmlns:pers="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-persoon/v20120201" xmlns:nhr="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-nhr-rechtspersoon/v20120201" xmlns:gba="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-gba-persoon/v20120901" xmlns:GbaPersoonRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-gba-persoon-ref/v20120201" xmlns:Stuk="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-stuk/v20120201" xmlns:bagadres="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-bag-adres/v20120201" xmlns:fn="http://www.w3.org/2005/xpath-functions">
	<xsl:template match="/">
		<root>
			<data>
				<xsl:for-each select="/snp:KadastraalObjectSnapshot/nhr:Rechtspersoon">
                    
                    <ingeschr_niet_nat_prs>
                        <sc_identif>
                            <xsl:call-template name="nen_identificatie">
                                <xsl:with-param name="id" select="pers:identificatie"/>
                            </xsl:call-template>
                        </sc_identif>
                        <rechtsvorm><xsl:value-of select="nhr:rechtsvorm/typ:waarde"/></rechtsvorm>
                        <statutaire_zetel><xsl:value-of select="nhr:statutaireZetel"/></statutaire_zetel>
                        <xsl:for-each select="nhr:RSIN">
                            <rsin><xsl:value-of select="."/></rsin>
                        </xsl:for-each>                
                    </ingeschr_niet_nat_prs>
				</xsl:for-each>
			</data>
		</root>
	</xsl:template>
	<xsl:template name="nen_identificatie">
		<xsl:param name="id"/>
		<xsl:value-of select="$id/nen:namespace"/>.<xsl:value-of select="$id/nen:lokaalId"/>
	</xsl:template>
</xsl:stylesheet>
