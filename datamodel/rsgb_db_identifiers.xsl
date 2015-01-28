<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:b3p="http://www.b3partners.nl/">

	<xsl:import href="datamodel.xsl"/>
	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
	<!-- Transformeert de identifiers in rsgb_converted.xml naar database-veilige namen
         voor tabellen en kolommen. De "database-safe" namen zijn case-insensitive.

         Per type database kunnen er andere regels zijn (zo heeft Oracle de vervelende
         restrictie van een max van 30 karakters en PostgreSQL niet), maar worden de
         gebruikte namen voor tabellen en kolommen voor alle types hetzelfde gehouden.
         Dit houdt voornamelijk in de max lengte beperken.
    -->

	<xsl:template match="/">
		<identifiers>
			<xsl:apply-templates select="RSGB/Objecttypes/Class"/>
			<xsl:call-template name="make-reference-lists"/>
			<xsl:call-template name="make-group-attributes"/>
			<xsl:call-template name="make-superclass-from-properties"/>
			<xsl:call-template name="make-simple-multi-properties"/>
		</identifiers>
	</xsl:template>
	
	<xsl:template match="Class">
		<class name="{@name}" table="{b3p:make-db-safe(@name)}">
			<xsl:for-each select="Property">
				<property name="{@name}" column="{b3p:make-db-safe(@name,../@name)}"/>
			</xsl:for-each>
		</class>
	</xsl:template>
	
	<xsl:template name="make-reference-lists">
	
		<xsl:for-each-group select="/RSGB/Objecttypes/Property[@elementType= 'Objecttype/Referentielijsten - Property']" group-by="@className">
			<xsl:variable name="class-name" select="current-grouping-key()"/>
			<class name="{@className}" table="{b3p:make-db-safe($class-name)}">
				<xsl:for-each select="/RSGB/Objecttypes/Property[@className = $class-name]">
					<property name="{@name}" column="{b3p:make-db-safe(@name,$class-name)}"/>
				</xsl:for-each>
			</class>
		</xsl:for-each-group>
	</xsl:template>
	
	<xsl:template name="make-group-attributes">
		<xsl:for-each-group select="/RSGB/Objecttypes/Property[@elementType = 'Objecttype/Groepattribuutsoort - Property']" group-by="@className">
			<xsl:variable name="class-name" select="current-grouping-key()"/>
			<class name="{$class-name}" table="{b3p:make-db-safe($class-name)}">
				<xsl:for-each select="//Property[@className=$class-name]">
					<property name="{@name}" column="{b3p:make-db-safe(@name)}"/>
				</xsl:for-each>
			</class>
		</xsl:for-each-group>
	</xsl:template>
	
	<xsl:template name="make-superclass-from-properties">
		<xsl:for-each select="/RSGB/Objecttypes/Property[@elementType = 'Objecttype - Superklasse']">
			<xsl:variable name="superclass" select="."/>
			<class name="{@className}" table="{b3p:make-db-safe(@className)}">
				<xsl:for-each select="/RSGB/Objecttypes/Property[@elementType='Objecttype/Relatieklasse - Property' and @classId = $superclass/@classId]" >
					<property name="{@name}" column="{b3p:make-db-safe(@name)}"/>
				</xsl:for-each>
			</class>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="make-simple-multi-properties">
		<xsl:for-each select="/RSGB/Objecttypes/Class/Property[@upperValueValue = 'n']/..">
			<xsl:variable name="className" select="@name"/>
			<xsl:for-each select="./Property">
				
				<xsl:if test="./@upperValueValue = 'n'">
					<class name="{concat($className,@name)}" table="{b3p:make-db-safe(b3p:make-db-safe(concat($className,'_',b3p:make-db-safe(@name, $className))))}">
						<property name="{@name}" column="{b3p:make-db-safe(@name)}"/>
					</class>
				</xsl:if>
			</xsl:for-each>			
		</xsl:for-each>
	</xsl:template>
	
	<!-- Afkortingen / vervangingen om onder 30 tekens te blijven, de limiet van
         een identifier in Oracle. @lang is een regular expression.

         Als de @lang regexp van een afko element matcht in de originele RSGB naam 
         wordt deze vervangen door @kort door middel van de b3p:dbsafe() function.
	-->
	<xsl:variable name="alle-afkortingen" as="element(afko)*"> 
		<afko lang="openb_ruimte_gem_openb_ruimte1" kort="openb_rmt_gem_openb_rmt1"/>
		<afko lang="reisdoc_ingeschr_nat_persoon2" kort="rsdoc_ingeschr_nt_persn2"/>
		<afko lang="reisdoc_ingeschr_nat_persoon3" kort="rsdoc_ingeschr_nt_persn3"/>
		<afko lang="benoemd_obj_kad_onrrnd_zaak1"  kort="benmd_obj_kd_onrrnd_zk1"/>
		<afko lang="kad_onrrnd_zk_kad_onrrnd_zk2" kort="kad_onr_zk_kd_onr_zk2"/>
		<afko lang="kad_onrrnd_zk_kad_onrrnd_zk3" kort="kad_onr_zk_kd_onr_zk3"/>
		<afko lang="_datum_" kort=""/>
		<afko lang="datum_" kort=""/>
		<afko lang="datum huwelijkssluiting/aangaan geregistreerd partnerschap" kort="datum_aangaan"/>
		<afko lang="land huwelijkssluiting/aangaan geregistreerd partnerschap" kort="land"/>
		<afko lang="plaats huwelijkssluiting/aangaan geregistreerd partnerschap" kort="plaats"/>
		<afko lang="identificatiecode overig adresseerbaar object aanduiding" kort="identificatiecode"/>
		<afko lang="huishouden_ingeschr_nat_persoon1" kort="hshdn_ingeschr_nt_prsn1"/>
		<afko lang="reisdocument_ingeschr_nat_persoon2" kort="rsdoc_ingeschr_nat_prsn2"/>
		<afko lang="reisdocument_ingeschr_nat_persoon3" kort="rsdoc_ingeschr_nat_prsn3"/>
		<afko lang="kad_onrrnd_zaak_kad_onrrnd_zaak2" kort="kad_onr_zk_kad_onr_zk2"/>
		<afko lang="kad_onrrnd_zaak_kad_onrrnd_zaak3" kort="kad_onr_zk_kad_onr_zk3"/>
		<afko lang="datum begin geldigheid addresserbaar object aanduiding" kort="dat_beg_geldh"/>
		<afko lang="datum einde geldigheid addresserbaar object aanduiding" kort="dat_eind_geldh"/>
		<afko lang="datum begin geldigheid" kort="dat_beg_geldh"/>
		<afko lang="identificatiecode overig adresseerbaar object aanduiding" kort="identificatiecode"/>
		<afko lang="huwelijk/geregistreerd partnerschap" kort="huw_ger_partn"/>
		<afko lang="identificatie adresseerbaar object aanduiding" kort="identificatie"/>
		<afko lang="nummer ander buitenlands niet-natuurlijk persoon" kort="nummer"/>
		<afko lang="natuurlijk" kort="nat"/>
		<afko lang="object" kort="obj"/>
		<afko lang="reisdocument" kort="rsdoc"/>
		<afko lang="burgerservicenummer" kort="bsn"/>
		<afko lang="zaak" kort="zk"/>
		<afko lang="buitenlands" kort="btnlnds"/>
		<afko lang="kadastrale" kort="kad"/>
		<afko lang="aantekening" kort="aantek"/>
		<afko lang="adresseerbaar" kort="addresseerb"/>
		<afko lang="aanduiding" kort="aand"/>
		<afko lang="addresserbaar" kort="addresseerb"/>
		<afko lang="geconstateerde" kort="geconst"/>
		<afko lang="openbare" kort="openb"/>
		<afko lang="privaatrechtelijk" kort="privaatr"/>
		<afko lang="verblijfplaats" kort="verblijfpl"/>
		<afko lang="nationaliteit" kort="nation"/>
		<afko lang="voorkomen" kort="voork"/>
		<afko lang="geldigheid" kort="geldh"/>
		<afko lang="verkrijging" kort="verkr"/>
		<afko lang="zakelijk" kort="zak"/>
		<afko lang="kadastraal" kort="kad"/>
		<afko lang="\(beeindiging\)" kort=""/>
		<afko lang="\(handels\)" kort=""/>
		<afko lang="\(statutaire\)" kort=""/>
		<afko lang="\(verblijfs\)" kort=""/>
		<afko lang="onbegroeid" kort="onbegr"/>
		<afko lang="maatschappelijke" kort="maatschapp"/>
		<afko lang="huisnummerrange" kort="huisnrrange"/>
		<afko lang="even en oneven" kort="on_even"/>
		<afko lang="overige" kort="ovrg"/>
		<afko lang="inwinningswijze" kort="inwwijze"/>
		<afko lang="geometrie" kort="geom"/>
		<afko lang="indicatie" kort="indic"/>
		<afko lang="identificatie" kort="identif"/>
		<afko lang="overige" kort="ovrg"/>
		<afko lang="coördinaten" kort="coordinaten"/>
		<afko lang="terreinvakonderdeel" kort="terreinvakonderd"/>
		<afko lang="kruinlijneometrie" kort="kruinlijngeom"/>
		<afko lang="/" kort=""/>
		<afko lang="geregistreerd" kort="ger"/>
		<afko lang="partnerschap" kort="prtnschp"/>
		<afko lang="huwelijk" kort="hwlk"/>
		<afko lang="ontbinding" kort="ontb"/>
		<afko lang="bevoegdheid" kort="bev"/>
		<afko lang="personen" kort="prsn"/>
		<afko lang="toetreding" kort="toetr"/>
		<afko lang="onroerende" kort="onrrnd"/>
		<afko lang="historie" kort="his"/>
		<!--afko lang="aangaan" kort="aang"/-->
		<afko lang="familierechtelijke" kort="fam_recht"/>
		<afko lang="betrekking" kort="betr"/>
		<afko lang="nederlanderschap" kort="nlschap"/>
		<afko lang="kiesrecht" kort="kiesr"/>
		<afko lang="verwachte" kort="verw"/>
		<afko lang="uitsluiting" kort="uitsl"/>
		<afko lang="einddatum" kort="eindd"/>
		<afko lang="europees" kort="euro"/>
		<afko lang="nederlandse" kort="nlse"/>
		<afko lang="relatie" kort="rel"/>
		<afko lang="omschrijving" kort="omschr"/>
		<afko lang="\(sen\)" kort=""/>
		<afko lang="snummer" kort="nummer"/>
		<afko lang="locatie" kort="loc"/>
		<afko lang="persoon" kort="prs"/>
		<afko lang="vestiging" kort="vestg"/>
		<afko lang="woonplaats" kort="wnplts"/>
		<afko lang="ruimte" kort="rmte"/>
		<!-- voor koppeltabellen-->
		<afko lang="appartementsrecht" kort="app_re"/>
		<afko lang="maakt deel uit van appartementencomplex dat staat op" kort="mkt_dl_app_cmp_op"/>
		<afko lang="begroeid" kort="begr"/>
		<afko lang="terreindeel" kort="terr_dl"/>
		<afko lang="bestaat" kort="bes"/>
		<!--afko lang="onstaan uit overgegaan in" kort="uit"/-->
		<afko lang="staat op of heeft ruimtelijke overlap met" kort="op_of_overlap"/>
		<afko lang="is hoofdperceel bij mandelige" kort="hfdprcl_mndlg"/>
		<afko lang="heeft als nevenadres" kort="heeft_nvnadrssn"/>
		<afko lang="is overgedragen naar" kort="overgdrgn"/>
		<afko lang="bestaande" kort="best"/>
		<afko lang="onderneming" kort="ondrnmng"/>
		<afko lang="maakt deel uit van" kort="deel_van"/>
		<afko lang="gemeentelijke" kort="gem"/>
		<afko lang="kent als bijgeschrevene" kort="bijgeschrevene"/>
		<afko lang="ingeschreven" kort="ingeschr"/>
		<afko lang="is ontstaan uit  overgegaan in" kort="ontstn_overggn"/>
		<afko lang="kad onrrnd zaak histor rel" kort="hist_rel"/>
		<afko lang="heeft nevenlocatie in of op" kort="nevenlocatie_in_op"/>
		<afko lang="bouwkundige bestemming actueel" kort="bouwk_best_act"/>
		<afko lang="ingeschr_nat_prs_btnlndse_nation" kort="ingeschr_nat_prs_btnlndse_nat"/>
	</xsl:variable>
	
	<!-- Zet een RSGB modelnaam om naar een naam die veilig is om in de database gebruikt te
         worden als tabel of kolomnaam.
    -->
	<xsl:function name="b3p:make-db-safe">
		<xsl:param name="identifier"/>
		<xsl:value-of select="b3p:make-db-safe($identifier,'')"/>
	</xsl:function>
	
	<!-- Zet een RSGB modelnaam om naar een naam die veilig is om in de database gebruikt te
         worden als kolomnaam voor een bepaalde tabel, en verwijder in RSGB soms gebruikelijke
         praktijk van het herhalen van de tabelnaam in de kolomnaam.
    -->
	<xsl:function name="b3p:make-db-safe">
		<xsl:param name="identifier"/>
		<xsl:param name="tablename"/>
		<!-- verwijder de tablenaam uit de kolomnaam -->
		<xsl:variable name="notablename">
			<xsl:choose>
				<xsl:when test="$tablename != ''">
					<xsl:value-of select="normalize-space(replace(lower-case($identifier), lower-case($tablename),''))"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$identifier"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<!--xsl:variable name="notablename" select="normalize-space(replace(lower-case($identifier), lower-case($tablename),''))"/-->
		<!-- vervang lange woorden door afkortingen -->
		<xsl:variable name="afgekort" select="b3p:replace-afkortingen(lower-case($notablename),$alle-afkortingen)"/>
		<!-- spatie naar underscore, trim spaces -->
		<xsl:variable name="replaced" select="replace(replace(normalize-space($afgekort),' ','_'),'-','_')"/>
		<xsl:variable name="safe" select="$replaced"/>
		<!-- debug: toon voor/na van lengte van identifier en identifier -->
		<!--xsl:value-of select="string-join((string(fn:string-length($identifier)),'->',string(fn:string-length($safe)),'van',$identifier,'naar',$safe),' ')"/-->
		<xsl:value-of select="$safe"/>
	</xsl:function>
	
	<!-- Search/replace functie van http://happygiraffe.net/blog/2009/07/23/search-replace-in-xslt-2/ -->
	<!-- Take some input and a list of suggestions, and do a recursive search and
       replace over the input until all have been applied. -->
	<xsl:function name="b3p:replace-afkortingen" as="xs:string">
		<xsl:param name="input" as="xs:string"/>
		<xsl:param name="afkortingen" as="element(afko)*"/>
		<xsl:variable name="afko" select="$afkortingen[1]"/>
		<xsl:sequence select="
			if (count($afkortingen) > 0) then
				b3p:replace-afkortingen(replace($input, $afko/@lang, $afko/@kort), $afkortingen[position() > 1])
			else
				$input"/>
	</xsl:function>
	
</xsl:stylesheet>
