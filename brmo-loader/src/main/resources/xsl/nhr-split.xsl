<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:cat="http://schemas.kvk.nl/schemas/hrip/catalogus/2013/01"
	xmlns:gpd="http://schemas.kvk.nl/schemas/hrip/generiekproduct/2013/01" 	
	xmlns:gml="http://www.opengis.net/gml"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:brmo="http://www.b3partners.nl/brmo/bericht"
	>

	<xsl:import href="nhr-object-ref.xsl"/>
	
	<!-- Dit stylesheet splitst een "GeneriekProduct" NHR Dataservice respons in meerdere berichten over
		authentieke gegevens van de basisregistratie.
	-->
    <xsl:template match="/">
        <brmo:berichten>
			<!-- Bij het parsen wordt bij berichten de volgorde van de bericht elementen aangehouden in 
				de volgordenummer kolom -->
			<xsl:apply-templates select="//gpd:inhoud"/>
        </brmo:berichten>
    </xsl:template>	
    
    <xsl:template match="cat:maatschappelijkeActiviteit">
		<xsl:variable name="datum">
			<brmo:datum><xsl:value-of select="../@peilmoment"/></brmo:datum>
		</xsl:variable>
		
		<xsl:for-each select="cat:heeftAlsEigenaar/*[cat:bsn or cat:rsin]">
			<!-- TODO: afsplitsen door/voor Persoon -->
			<brmo:bericht>
				<brmo:object_ref><xsl:apply-templates select="." mode="object_ref"/></brmo:object_ref>
				<xsl:copy-of select="$datum"/>
				<brmo:br_xml>
					<xsl:copy>
						<xsl:attribute name="peilmoment"><xsl:value-of select="$datum"/></xsl:attribute>
						<xsl:copy-of select="node()"/>
						<cat:isEigenaarVan>
							<xsl:for-each select="../..">
								<xsl:copy>
									<xsl:copy-of select="cat:kvkNummer"/>
								</xsl:copy>
							</xsl:for-each>
						</cat:isEigenaarVan>
					</xsl:copy>
				</brmo:br_xml>
			</brmo:bericht>
		</xsl:for-each>		
		
		<brmo:bericht>
			<brmo:object_ref><xsl:apply-templates select="." mode="object_ref"/></brmo:object_ref>
			<xsl:copy-of select="$datum"/>
			<brmo:br_xml>
				<xsl:copy>
					<xsl:attribute name="peilmoment"><xsl:value-of select="$datum"/></xsl:attribute>
					<xsl:copy-of select="node()"/>
				</xsl:copy>
			</brmo:br_xml>
		</brmo:bericht>
		<!-- Completere vestiging in wordtGeleidVanuit dan wordtUitgeoefendIn -->
		<xsl:variable name="leidendeVestiging" select="cat:wordtGeleidVanuit/cat:vestigingsnummer"/>
		<xsl:for-each select="cat:wordtGeleidVanuit/*">
			<brmo:bericht>
				<brmo:object_ref><xsl:apply-templates select="." mode="object_ref"/></brmo:object_ref>
				<xsl:copy-of select="$datum"/>
				<brmo:br_xml>
					<!-- Voeg element wordtUitgeoefendDoor toe. Deze is niet aanwezig bij opvragen van 
					maatschappelijkeActiviteit, maar in RSGB een foreign key dus deze informatie bij
					splitsen wel overbrengen via toevoegen van dit element -->
					<xsl:copy>
						<xsl:attribute name="peilmoment"><xsl:value-of select="$datum"/></xsl:attribute>
						<xsl:copy-of select="node()"/>
						<cat:wordtUitgeoefendDoor>
							<!-- Voor een commercieleVestiging komt hier een onderneming, met alleen het kvkNummer -->
							<xsl:if test="local-name(.) = 'commercieleVestiging'">
								<xsl:variable name="vestigingsnr" select="cat:vestigingsnummer"/>
								<xsl:for-each select="//cat:onderneming[cat:wordtUitgeoefendIn/cat:commercieleVestiging/cat:vestigingsnummer = $vestigingsnr]">
									<xsl:copy>
										<xsl:copy-of select="cat:kvkNummer"/>
									</xsl:copy>
								</xsl:for-each>
							</xsl:if>
							
							<!-- Voor een nietCommercieleVestiging komt hier een maatschappelijkeActiviteit -->
							<xsl:if test="local-name(.) = 'nietCommercieleVestiging'">
								<hier_komt_de_ma/>
							</xsl:if>
						</cat:wordtUitgeoefendDoor>
					</xsl:copy>
				</brmo:br_xml>
			</brmo:bericht>
		</xsl:for-each>
		<!-- Incompletere commerciele vestigingen waaruit de maatschappelijkeActiviteit niet wordt geleid, maar toch worden
			meegeleverd (testcases nodig!) -->
		<xsl:for-each select="//cat:commercieleVestiging[cat:vestigingsnummer != $leidendeVestiging]">
			<!-- child van onderneming, manifesteertZichAls en maatschappelijkeActiviteit -->
			<brmo:message fatal="true">
				Onbekende case: commercieleVestiging van maatschappelijkeActiviteit waaruit deze niet wordt geleid, aub testcase aanmelden bij ontwikkelaars!
			</brmo:message>
		</xsl:for-each>
		<!-- Incompletere niet commerciele vestigingen waaruit de maatschappelijkeActiviteit niet wordt geleid, maar toch worden
			meegeleverd (testcases nodig) -->
		<xsl:for-each select="//cat:nietCommercieleVestiging[cat:vestigingsnummer != $leidendeVestiging]">
			<!-- child van wordtUitgeoefendIn en maatschappelijkeActiviteit -->
			<brmo:message fatal="true">
				Onbekende case: nietCommercieleVestiging van maatschappelijkeActiviteit waaruit deze niet wordt geleid, aub testcase aanmelden bij ontwikkelaars!
			</brmo:message>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
