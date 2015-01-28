<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bep="http://www.kadaster.nl/schemas/bag-verstrekkingen/extract-producten-lvc/v20090901" xmlns:bag="http://www.kadaster.nl/schemas/imbag/lvc/v20090901" xmlns:bagtype="http://www.kadaster.nl/schemas/imbag/imbag-types/v20090901" xmlns:gml="http://www.opengis.net/gml" xmlns:nen5825="http://www.kadaster.nl/schemas/imbag/nen5825/v20090901">
	<xsl:template match="/">
		<root>
			<data>
				<!-- loop door bep:Nieuw elementen -->
				<xsl:for-each select="//bep:Mutatie-product/bep:Nieuw">
					<xsl:apply-templates select="."/>
				</xsl:for-each>
			</data>
		</root>
	</xsl:template>
	<!-- Template Pand
<bag:Pand>
	<bag:identificatie>aaaaaaaaaaaaaaaa</bag:identificatie>
	<bag:aanduidingRecordInactief>J</bag:aanduidingRecordInactief>
	<bag:aanduidingRecordCorrectie>0</bag:aanduidingRecordCorrectie>
	<bag:officieel>J</bag:officieel>
	<bag:pandGeometrie/>
	<bag:bouwjaar>2001</bag:bouwjaar>
	<bag:pandstatus>String</bag:pandstatus>
	<bag:tijdvakgeldigheid>
		<bagtype:begindatumTijdvakGeldigheid>0000000000000000</bagtype:begindatumTijdvakGeldigheid>
		<bagtype:einddatumTijdvakGeldigheid>0000000000000000</bagtype:einddatumTijdvakGeldigheid>
	</bag:tijdvakgeldigheid>
	<bag:inOnderzoek>J</bag:inOnderzoek>
	<bag:bron>
		<bagtype:documentdatum>00000000</bagtype:documentdatum>
		<bagtype:documentnummer>aaaaaaaaaaaaaaaaaaaa</bagtype:documentnummer>
	</bag:bron>
</bag:Pand> -->
	<xsl:template match="bag:Pand">
		<pand>
			<identif>
				<xsl:value-of select="bag:identificatie"/>
			</identif>
			<status>
				<xsl:value-of select="bag:pandstatus"/>
			</status>
			<dat_beg_geldh>
				<xsl:for-each select="bag:tijdvakgeldigheid/bagtype:begindatumTijdvakGeldigheid">
					<xsl:call-template name="date-numeric"/>
				</xsl:for-each>
			</dat_beg_geldh>
			<datum_einde_geldh>
				<xsl:for-each select="bag:tijdvakgeldigheid/bagtype:einddatumTijdvakGeldigheid">
					<xsl:call-template name="date-numeric"/>
				</xsl:for-each>
			</datum_einde_geldh>
			<indic_geconstateerd>
				<xsl:value-of select="bag:officieel"/>
			</indic_geconstateerd>
			<geom_bovenaanzicht>
				<xsl:copy-of select="bag:pandGeometrie/gml:Polygon"/>
			</geom_bovenaanzicht>
			<oorspronkelijk_bouwjaar>
				<xsl:value-of select="bag:bouwjaar"/>
			</oorspronkelijk_bouwjaar>
		</pand>
		<brondocument ignore-duplicates="yes">
			<tabel>
				<xsl:value-of select="'pand'"/>
			</tabel>
			<tabel_identificatie>
				<xsl:value-of select="bag:identificatie"/>
			</tabel_identificatie>
			<identificatie>
				<xsl:value-of select="bag:bron/bagtype:documentnummer"/>
			</identificatie>
			<datum>
				<xsl:for-each select="bag:bron/bagtype:documentdatum">
					<xsl:call-template name="date-numeric"/>
				</xsl:for-each>
			</datum>
		</brondocument>
	</xsl:template>
	<!-- template standplaats
<bag:Standplaats>
	<bag:gerelateerdeAdressen>
		<bag:hoofdadres>
			<bag:identificatie>aaaaaaaaaaaaaaaa</bag:identificatie>
		</bag:hoofdadres>
		<bag:nevenadres>
			<bag:identificatie>aaaaaaaaaaaaaaaa</bag:identificatie>
		</bag:nevenadres>
	</bag:gerelateerdeAdressen>
	<bag:identificatie>aaaaaaaaaaaaaaaa</bag:identificatie>
	<bag:aanduidingRecordInactief>J</bag:aanduidingRecordInactief>
	<bag:aanduidingRecordCorrectie>0</bag:aanduidingRecordCorrectie>
	<bag:officieel>J</bag:officieel>
	<bag:standplaatsStatus>Plaats aangewezen</bag:standplaatsStatus>
	<bag:standplaatsGeometrie/>
	<bag:tijdvakgeldigheid>
		<bagtype:begindatumTijdvakGeldigheid>0000000000000000</bagtype:begindatumTijdvakGeldigheid>
		<bagtype:einddatumTijdvakGeldigheid>0000000000000000</bagtype:einddatumTijdvakGeldigheid>
	</bag:tijdvakgeldigheid>
	<bag:inOnderzoek>J</bag:inOnderzoek>
	<bag:bron>
		<bagtype:documentdatum>00000000</bagtype:documentdatum>
		<bagtype:documentnummer>aaaaaaaaaaaaaaaaaaaa</bagtype:documentnummer>
	</bag:bron>
</bag:Standplaats>
-->
	<xsl:template match="bag:Standplaats">
		<standplaats>
			<sc_identif>
				<xsl:value-of select="bag:identificatie"/>
			</sc_identif>
			<indic_geconst>
				<xsl:value-of select="bag:officieel"/>
			</indic_geconst>
			<status>
				<xsl:value-of select="bag:standplaatsStatus"/>
			</status>
			<fk_4nra_sc_identif>
				<xsl:value-of select="bag:gerelateerdeAdressen/bag:hoofdadres"/>
			</fk_4nra_sc_identif>
		</standplaats>
		<benoemd_obj>
			<sc_identif>
				<xsl:value-of select="bag:identificatie"/>
			</sc_identif>
			<clazz>
				<xsl:value-of select="'standplaats'"/>
			</clazz>
		</benoemd_obj>
		<xsl:variable name="bagid">
			<xsl:value-of select="bag:identificatie"/>
		</xsl:variable>
		<xsl:variable name="begindate">
			<xsl:for-each select="bag:tijdvakgeldigheid/bagtype:begindatumTijdvakGeldigheid">
				<xsl:call-template name="date-numeric"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:for-each select="bag:gerelateerdeAdressen/bag:nevenadres">
			<standplaats_nummeraand>
				<fk_nn_lh_spl_sc_identif>
					<xsl:value-of select="$bagid"/>
				</fk_nn_lh_spl_sc_identif>
				<fk_nn_lh_spl_sc_dat_beg_geldh>
					<xsl:value-of select="$begindate"/>
				</fk_nn_lh_spl_sc_dat_beg_geldh>
				<fk_nn_rh_nra_sc_identif>
					<xsl:value-of select="bag:identificatie"/>
				</fk_nn_rh_nra_sc_identif>
			</standplaats_nummeraand>
		</xsl:for-each>
		<benoemd_terrein>
			<dat_beg_geldh>
				<xsl:value-of select="$begindate"/>
			</dat_beg_geldh>
			<sc_identif>
				<xsl:value-of select="$bagid"/>
			</sc_identif>
			<clazz>
				<xsl:value-of select="'standplaats'"/>
			</clazz>
			<geom>
				<xsl:copy-of select="bag:standplaatsGeometrie/gml:Polygon"/>
			</geom>
			<datum_einde_geldh>
				<xsl:for-each select="bag:tijdvakgeldigheid/bagtype:einddatumTijdvakGeldigheid">
					<xsl:call-template name="date-numeric"/>
				</xsl:for-each>
			</datum_einde_geldh>
			<geom/>
		</benoemd_terrein>
		<brondocument ignore-duplicates="yes">
			<tabel>
				<xsl:value-of select="'standplaats'"/>
			</tabel>
			<tabel_identificatie>
				<xsl:value-of select="bag:identificatie"/>
			</tabel_identificatie>
			<identificatie>
				<xsl:value-of select="bag:bron/bagtype:documentnummer"/>
			</identificatie>
			<datum>
				<xsl:for-each select="bag:bron/bagtype:documentdatum">
					<xsl:call-template name="date-numeric"/>
				</xsl:for-each>
			</datum>
		</brondocument>
	</xsl:template>
	<!-- template ligplaats
<bag:Ligplaats>
	<bag:gerelateerdeAdressen>
		<bag:hoofdadres>
			<bag:identificatie>aaaaaaaaaaaaaaaa</bag:identificatie>
		</bag:hoofdadres>
	</bag:gerelateerdeAdressen>
	<bag:identificatie>aaaaaaaaaaaaaaaa</bag:identificatie>
	<bag:aanduidingRecordInactief>J</bag:aanduidingRecordInactief>
	<bag:aanduidingRecordCorrectie>0</bag:aanduidingRecordCorrectie>
	<bag:officieel>J</bag:officieel>
	<bag:ligplaatsStatus>Plaats aangewezen</bag:ligplaatsStatus>
	<bag:ligplaatsGeometrie/>
	<bag:tijdvakgeldigheid>
		<bagtype:begindatumTijdvakGeldigheid>0000000000000000</bagtype:begindatumTijdvakGeldigheid>
		<bagtype:einddatumTijdvakGeldigheid>0000000000000000</bagtype:einddatumTijdvakGeldigheid>
	</bag:tijdvakgeldigheid>
	<bag:inOnderzoek>J</bag:inOnderzoek>
	<bag:bron>
		<bagtype:documentdatum>00000000</bagtype:documentdatum>
		<bagtype:documentnummer>aaaaaaaaaaaaaaaaaaaa</bagtype:documentnummer>
	</bag:bron>
</bag:Ligplaats>
-->
	<xsl:template match="bag:Ligplaats">
		<ligplaats>
			<sc_identif>
				<xsl:value-of select="bag:identificatie"/>
			</sc_identif>
			<indic_geconst>
				<xsl:value-of select="bag:officieel"/>
			</indic_geconst>
			<status>
				<xsl:value-of select="bag:ligplaatsStatus"/>
			</status>
			<fk_4nra_sc_identif>
				<xsl:value-of select="bag:gerelateerdeAdressen/bag:hoofdadres"/>
			</fk_4nra_sc_identif>
		</ligplaats>
		<benoemd_obj>
			<sc_identif>
				<xsl:value-of select="bag:identificatie"/>
			</sc_identif>
			<clazz>
				<xsl:value-of select="'ligplaats'"/>
			</clazz>
		</benoemd_obj>
		<xsl:variable name="bagid">
			<xsl:value-of select="bag:identificatie"/>
		</xsl:variable>
		<xsl:variable name="begindate">
			<xsl:for-each select="bag:tijdvakgeldigheid/bagtype:begindatumTijdvakGeldigheid">
				<xsl:call-template name="date-numeric"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:for-each select="bag:gerelateerdeAdressen/bag:nevenadres">
			<standplaats_nummeraand>
				<fk_nn_lh_lpl_sc_identif>
					<xsl:value-of select="$bagid"/>
				</fk_nn_lh_lpl_sc_identif>
				<fk_nn_lh_lpl_sc_dat_beg_geldh>
					<xsl:value-of select="$begindate"/>
				</fk_nn_lh_lpl_sc_dat_beg_geldh>
				<fk_nn_rh_nra_sc_identif>
					<xsl:value-of select="bag:identificatie"/>
				</fk_nn_rh_nra_sc_identif>
			</standplaats_nummeraand>
		</xsl:for-each>
		<benoemd_terrein>
			<dat_beg_geldh>
				<xsl:value-of select="$begindate"/>
			</dat_beg_geldh>
			<sc_identif>
				<xsl:value-of select="$bagid"/>
			</sc_identif>
			<clazz>
				<xsl:value-of select="'standplaats'"/>
			</clazz>
			<geom>
				<xsl:copy-of select="bag:standplaatsGeometrie/gml:Polygon"/>
			</geom>
			<datum_einde_geldh>
				<xsl:value-of select="bag:tijdvakgeldigheid/bagtype:einddatumTijdvakGeldigheid"/>
			</datum_einde_geldh>
			<geom/>
		</benoemd_terrein>
		<brondocument ignore-duplicates="yes">
			<tabel>
				<xsl:value-of select="'ligplaats'"/>
			</tabel>
			<tabel_identificatie>
				<xsl:value-of select="bag:identificatie"/>
			</tabel_identificatie>
			<identificatie>
				<xsl:value-of select="bag:bron/bagtype:documentnummer"/>
			</identificatie>
			<datum>
				<xsl:for-each select="bag:bron/bagtype:documentdatum">
					<xsl:call-template name="date-numeric"/>
				</xsl:for-each>
			</datum>
		</brondocument>
	</xsl:template>
	<!-- template nummeraanduiding
<bag:Nummeraanduiding>
	<bag:identificatie>aaaaaaaaaaaaaaaa</bag:identificatie>
	<bag:aanduidingRecordInactief>J</bag:aanduidingRecordInactief>
	<bag:aanduidingRecordCorrectie>0</bag:aanduidingRecordCorrectie>
	<bag:huisnummer>1</bag:huisnummer>
	<bag:officieel>J</bag:officieel>
	<bag:huisletter>,</bag:huisletter>
	<bag:huisnummertoevoeging>,</bag:huisnummertoevoeging>
	<bag:postcode>1000AA</bag:postcode>
	<bag:tijdvakgeldigheid>
		<bagtype:begindatumTijdvakGeldigheid>0000000000000000</bagtype:begindatumTijdvakGeldigheid>
		<bagtype:einddatumTijdvakGeldigheid>0000000000000000</bagtype:einddatumTijdvakGeldigheid>
	</bag:tijdvakgeldigheid>
	<bag:inOnderzoek>J</bag:inOnderzoek>
	<bag:typeAdresseerbaarObject>Verblijfsobject</bag:typeAdresseerbaarObject>
	<bag:bron>
		<bagtype:documentdatum>00000000</bagtype:documentdatum>
		<bagtype:documentnummer>aaaaaaaaaaaaaaaaaaaa</bagtype:documentnummer>
	</bag:bron>
	<bag:nummeraanduidingStatus>Naamgeving uitgegeven</bag:nummeraanduidingStatus>
	<bag:gerelateerdeOpenbareRuimte>
		<bag:identificatie>aaaaaaaaaaaaaaaa</bag:identificatie>
	</bag:gerelateerdeOpenbareRuimte>
	<bag:gerelateerdeWoonplaats>
		<bag:identificatie>0000</bag:identificatie>
	</bag:gerelateerdeWoonplaats>
</bag:Nummeraanduiding>
-->
	<xsl:template match="bag:Nummeraanduiding">
		<nummeraand>
			<sc_identif>
				<xsl:value-of select="bag:identificatie"/>
			</sc_identif>
			<indic_geconst>
				<xsl:value-of select="bag:officieel"/>
			</indic_geconst>
			<indic_hoofdadres>
				<xsl:value-of select="'?'"/>
			</indic_hoofdadres>
			<status>
				<xsl:value-of select="bag:nummeraanduidingStatus"/>
			</status>
		</nummeraand>
		<addresseerb_obj_aand>
			<dat_beg_geldh>
				<xsl:for-each select="bag:tijdvakgeldigheid/bagtype:einddatumTijdvakGeldigheid">
					<xsl:call-template name="date-numeric"/>
				</xsl:for-each>
			</dat_beg_geldh>
			<identif>
				<xsl:value-of select="bag:identificatie"/>
			</identif>
			<clazz>
				<xsl:value-of select="'nummeraand'"/>
			</clazz>
			<dat_eind_geldh>
				<xsl:for-each select="bag:tijdvakgeldigheid/bagtype:einddatumTijdvakGeldigheid">
					<xsl:call-template name="date-numeric"/>
				</xsl:for-each>
			</dat_eind_geldh>
			<huisletter>
				<xsl:value-of select="bag:huisletter"/>
			</huisletter>
			<huinummer>
				<xsl:value-of select="bag:huisletter"/>
			</huinummer>
			<huinummertoevoeging>
				<xsl:value-of select="bag:huisnummertoevoeging"/>
			</huinummertoevoeging>
			<postcode>
				<xsl:value-of select="bag:postcode"/>
			</postcode>
			<fk_6wpl_identif>
				<xsl:value-of select="bag:gerelateerdeWoonplaats/bag:identificatie"/>
			</fk_6wpl_identif>
			<fk_7opr_identifcode>
				<xsl:value-of select="bag:gerelateerdeOpenbareRuimte/bag:identificatie"/>
			</fk_7opr_identifcode>
		</addresseerb_obj_aand>
		<brondocument ignore-duplicates="yes">
			<tabel>
				<xsl:value-of select="'nummeraand'"/>
			</tabel>
			<tabel_identificatie>
				<xsl:value-of select="bag:identificatie"/>
			</tabel_identificatie>
			<identificatie>
				<xsl:value-of select="bag:bron/bagtype:documentnummer"/>
			</identificatie>
			<datum>
				<xsl:for-each select="bag:bron/bagtype:documentdatum">
					<xsl:call-template name="date-numeric"/>
				</xsl:for-each>
			</datum>
		</brondocument>
	</xsl:template>
	<!-- template woonplaats
<bag:Woonplaats>
	<bag:identificatie>0000</bag:identificatie>
	<bag:aanduidingRecordInactief>J</bag:aanduidingRecordInactief>
	<bag:aanduidingRecordCorrectie>0</bag:aanduidingRecordCorrectie>
	<bag:woonplaatsNaam>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa</bag:woonplaatsNaam>
	<bag:woonplaatsGeometrie/>
	<bag:officieel>J</bag:officieel>
	<bag:tijdvakgeldigheid>
		<bagtype:begindatumTijdvakGeldigheid>0000000000000000</bagtype:begindatumTijdvakGeldigheid>
		<bagtype:einddatumTijdvakGeldigheid>0000000000000000</bagtype:einddatumTijdvakGeldigheid>
	</bag:tijdvakgeldigheid>
	<bag:inOnderzoek>J</bag:inOnderzoek>
	<bag:bron>
		<bagtype:documentdatum>00000000</bagtype:documentdatum>
		<bagtype:documentnummer>aaaaaaaaaaaaaaaaaaaa</bagtype:documentnummer>
	</bag:bron>
	<bag:woonplaatsStatus>Woonplaats aangewezen</bag:woonplaatsStatus>
</bag:Woonplaats>
-->
	<xsl:template match="bag:Woonplaats">
		<wnplts>
			<dat_beg_geldh>
				<xsl:for-each select="bag:tijdvakgeldigheid/bagtype:begindatumTijdvakGeldigheid">
					<xsl:call-template name="date-numeric"/>
				</xsl:for-each>
			</dat_beg_geldh>
			<identif>
				<xsl:value-of select="bag:identificatie"/>
			</identif>
			<datum_einde_geldh>
				<xsl:for-each select="bag:tijdvakgeldigheid/bagtype:einddatumTijdvakGeldigheid">
					<xsl:call-template name="date-numeric"/>
				</xsl:for-each>
			</datum_einde_geldh>
			<indic_geconst>
				<xsl:value-of select="bag:officieel"/>
			</indic_geconst>
			<naam>
				<xsl:value-of select="bag:woonplaatsNaam"/>
			</naam>
			<naam_nen>
				<xsl:value-of select="bag:woonplaatsNaam"/>
			</naam_nen>
			<geom>
				<xsl:copy-of select="bag:standplaatsGeometrie/gml:Polygon"/>
			</geom>
			<status>
				<xsl:value-of select="bag:woonplaatsStatus"/>
			</status>
			<fk_7gem_code>
				<xsl:value-of select="'via omrekentabel'"/>
			</fk_7gem_code>
			<geom/>
		</wnplts>
		<brondocument ignore-duplicates="yes">
			<tabel>
				<xsl:value-of select="'wnplts'"/>
			</tabel>
			<tabel_identificatie>
				<xsl:value-of select="bag:identificatie"/>
			</tabel_identificatie>
			<identificatie>
				<xsl:value-of select="bag:bron/bagtype:documentnummer"/>
			</identificatie>
			<datum>
				<xsl:for-each select="bag:bron/bagtype:documentdatum">
					<xsl:call-template name="date-numeric"/>
				</xsl:for-each>
			</datum>
		</brondocument>
	</xsl:template>
	<!-- template verblijfsobject
<bag:Verblijfsobject>
	<bag:gerelateerdeAdressen>
		<bag:hoofdadres>
			<bag:identificatie>aaaaaaaaaaaaaaaa</bag:identificatie>
		</bag:hoofdadres>
	</bag:gerelateerdeAdressen>
	<bag:identificatie>aaaaaaaaaaaaaaaa</bag:identificatie>
	<bag:aanduidingRecordInactief>J</bag:aanduidingRecordInactief>
	<bag:aanduidingRecordCorrectie>0</bag:aanduidingRecordCorrectie>
	<bag:officieel>J</bag:officieel>
	<bag:verblijfsobjectGeometrie/>
	<bag:gebruiksdoelVerblijfsobject>woonfunctie</bag:gebruiksdoelVerblijfsobject>
	<bag:oppervlakteVerblijfsobject>1</bag:oppervlakteVerblijfsobject>
	<bag:verblijfsobjectStatus>Verblijfsobject gevormd</bag:verblijfsobjectStatus>
	<bag:tijdvakgeldigheid>
		<bagtype:begindatumTijdvakGeldigheid>0000000000000000</bagtype:begindatumTijdvakGeldigheid>
		<bagtype:einddatumTijdvakGeldigheid>0000000000000000</bagtype:einddatumTijdvakGeldigheid>
	</bag:tijdvakgeldigheid>
	<bag:inOnderzoek>J</bag:inOnderzoek>
	<bag:bron>
		<bagtype:documentdatum>00000000</bagtype:documentdatum>
		<bagtype:documentnummer>aaaaaaaaaaaaaaaaaaaa</bagtype:documentnummer>
	</bag:bron>
	<bag:gerelateerdPand>
		<bag:identificatie>aaaaaaaaaaaaaaaa</bag:identificatie>
	</bag:gerelateerdPand>
</bag:Verblijfsobject>
-->
	<xsl:template match="bag:Verblijfsobject">
		<verblijfsobj>
			<sc_identif>
				<xsl:value-of select="bag:identificatie"/>
			</sc_identif>
			<indic_geconstateerd>
				<xsl:value-of select="bag:officieel"/>
			</indic_geconstateerd>
			<status>
				<xsl:value-of select="bag:verblijfsobjectStatus"/>
			</status>
			<fk_11nra_sc_identif>
				<xsl:value-of select="bag:gerelateerdeAdressen/bag:hoofdadres"/>
			</fk_11nra_sc_identif>
		</verblijfsobj>
		<gebouwd_obj>
			<dat_beg_geldh>
				<xsl:for-each select="bag:tijdvakgeldigheid/bagtype:begindatumTijdvakGeldigheid">
					<xsl:call-template name="date-numeric"/>
				</xsl:for-each>
			</dat_beg_geldh>
			<sc_identif>
				<xsl:value-of select="bag:identificatie"/>
			</sc_identif>
			<clazz>
				<xsl:value-of select="'verblijfsobject'"/>
			</clazz>
			<datum_einde_geldh>
				<xsl:for-each select="bag:tijdvakgeldigheid/bagtype:einddatumTijdvakGeldigheid">
					<xsl:call-template name="date-numeric"/>
				</xsl:for-each>
			</datum_einde_geldh>
			<oppervlakte_obj>
				<xsl:value-of select="bag:oppervlakteVerblijfsobject"/>
			</oppervlakte_obj>
			<puntgeom>
				<xsl:copy-of select="bag:verblijfsobjectGeometrie/gml:Point"/>
			</puntgeom>
		</gebouwd_obj>
                <gebouwd_obj_gebruiksdoel>
                    <gebruiksdoel_gebouwd_obj>
                        <xsl:value-of select="bag:gebruiksdoelVerblijfsobject"/>
                    </gebruiksdoel_gebouwd_obj>
                    <fk_gbo_sc_identif>
                        <xsl:value-of select="bag:identificatie"/>
                    </fk_gbo_sc_identif>
                </gebouwd_obj_gebruiksdoel>
		<benoemd_obj>
			<sc_identif>
				<xsl:value-of select="bag:identificatie"/>
			</sc_identif>
			<clazz>
				<xsl:value-of select="'verblijfsobject'"/>
			</clazz>
		</benoemd_obj>
		<xsl:variable name="bagid">
			<xsl:value-of select="bag:identificatie"/>
		</xsl:variable>
		<xsl:variable name="begindate">
			<xsl:for-each select="bag:tijdvakgeldigheid/bagtype:begindatumTijdvakGeldigheid">
				<xsl:call-template name="date-numeric"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:for-each select="bag:gerelateerdeAdressen/bag:nevenadres">
			<verblijfsobj_nummeraand>
				<fk_nn_lh_vbo_sc_identif>
					<xsl:value-of select="$bagid"/>
				</fk_nn_lh_vbo_sc_identif>
				<fk_nn_lh_vbo_sc_dat_beg_geldh>
					<xsl:value-of select="$begindate"/>
				</fk_nn_lh_vbo_sc_dat_beg_geldh>
				<fk_nn_rh_nra_sc_identif>
					<xsl:value-of select="bag:identificatie"/>
				</fk_nn_rh_nra_sc_identif>
			</verblijfsobj_nummeraand>
		</xsl:for-each>
		<xsl:for-each select="bag:gerelateerdPand/bag:identificatie">
			<verblijfsobj_pand>
				<fk_nn_lh_vbo_sc_identif>
					<xsl:value-of select="$bagid"/>
				</fk_nn_lh_vbo_sc_identif>
				<fk_nn_lh_vbo_sc_dat_beg_geldh>
					<xsl:value-of select="$begindate"/>
				</fk_nn_lh_vbo_sc_dat_beg_geldh>
				<fk_nn_rh_pnd_identif>
					<xsl:value-of select="."/>
				</fk_nn_rh_pnd_identif>
			</verblijfsobj_pand>
		</xsl:for-each>
		<brondocument ignore-duplicates="yes">
			<tabel>
				<xsl:value-of select="'verblijfsobj'"/>
			</tabel>
			<tabel_identificatie>
				<xsl:value-of select="bag:identificatie"/>
			</tabel_identificatie>
			<identificatie>
				<xsl:value-of select="bag:bron/bagtype:documentnummer"/>
			</identificatie>
			<datum>
				<xsl:for-each select="bag:bron/bagtype:documentdatum">
					<xsl:call-template name="date-numeric"/>
				</xsl:for-each>
			</datum>
		</brondocument>
	</xsl:template>
	<!-- template openbare ruimte
<bag:OpenbareRuimte>
	<bag:identificatie>aaaaaaaaaaaaaaaa</bag:identificatie>
	<bag:aanduidingRecordInactief>J</bag:aanduidingRecordInactief>
	<bag:aanduidingRecordCorrectie>0</bag:aanduidingRecordCorrectie>
	<bag:openbareRuimteNaam>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa</bag:openbareRuimteNaam>
	<bag:officieel>J</bag:officieel>
	<bag:tijdvakgeldigheid>
		<bagtype:begindatumTijdvakGeldigheid>0000000000000000</bagtype:begindatumTijdvakGeldigheid>
		<bagtype:einddatumTijdvakGeldigheid>0000000000000000</bagtype:einddatumTijdvakGeldigheid>
	</bag:tijdvakgeldigheid>
	<bag:inOnderzoek>J</bag:inOnderzoek>
	<bag:openbareRuimteType>Weg</bag:openbareRuimteType>
	<bag:bron>
		<bagtype:documentdatum>00000000</bagtype:documentdatum>
		<bagtype:documentnummer>aaaaaaaaaaaaaaaaaaaa</bagtype:documentnummer>
	</bag:bron>
	<bag:openbareruimteStatus>Naamgeving uitgegeven</bag:openbareruimteStatus>
	<bag:gerelateerdeWoonplaats>
		<bag:identificatie>0000</bag:identificatie>
	</bag:gerelateerdeWoonplaats>
	<nen5825:VerkorteOpenbareruimteNaam>aaaaaaaaaaaaaaaaaaaaaaaa</nen5825:VerkorteOpenbareruimteNaam>
</bag:OpenbareRuimte>
-->
	<xsl:template match="bag:OpenbareRuimte">
		<gem_openb_rmte>
			<dat_beg_geldh>
				<xsl:for-each select="bag:tijdvakgeldigheid/bagtype:begindatumTijdvakGeldighei">
					<xsl:call-template name="date-numeric"/>
				</xsl:for-each>
			</dat_beg_geldh>
			<identifcode>
				<xsl:value-of select="bag:identificatie"/>
			</identifcode>
			<datum_einde_geldh>
				<xsl:for-each select="bag:tijdvakgeldigheid/bagtype:einddatumTijdvakGeldigheid">
					<xsl:call-template name="date-numeric"/>
				</xsl:for-each>
			</datum_einde_geldh>
			<indic_geconst_openb_rmte>
				<xsl:value-of select="bag:officieel"/>
			</indic_geconst_openb_rmte>
			<naam_openb_rmte>
				<xsl:value-of select="bag:openbareRuimteNaam"/>
			</naam_openb_rmte>
			<status_openb_rmte>
				<xsl:value-of select="bag:openbareruimteStatus"/>
			</status_openb_rmte>
			<straatnaam>
				<xsl:value-of select="nen5825:VerkorteOpenbareruimteNaam"/>
			</straatnaam>
			<type_openb_rmte>
				<xsl:value-of select="bag:openbareRuimteType"/>
			</type_openb_rmte>
			<fk_7gem_code>
				<xsl:value-of select="bag:gerelateerdeWoonplaats/bag:identificatie"/>
			</fk_7gem_code>
		</gem_openb_rmte>
		<openb_rmte>
			<identifcode>
				<xsl:value-of select="bag:identificatie"/>
			</identifcode>
		</openb_rmte>
		<openb_rmte_wnplts>
			<fk_nn_lh_opr_identifcode>
				<xsl:value-of select="bag:identificatie"/>
			</fk_nn_lh_opr_identifcode>
			<fk_nn_rh_wpl_identif>
				<xsl:value-of select="bag:gerelateerdeWoonplaats/bag:identificatie"/>
			</fk_nn_rh_wpl_identif>
		</openb_rmte_wnplts>
		<openb_rmte_gem_openb_rmte>
			<fk_nn_lh_opr_identifcode>
				<xsl:value-of select="bag:identificatie"/>
			</fk_nn_lh_opr_identifcode>
			<fk_nn_rh_gor_identifcode>
				<xsl:value-of select="bag:identificatie"/>
			</fk_nn_rh_gor_identifcode>
		</openb_rmte_gem_openb_rmte>
		<brondocument ignore-duplicates="yes">
			<tabel>
				<xsl:value-of select="'gem_openb_rmte'"/>
			</tabel>
			<tabel_identificatie>
				<xsl:value-of select="bag:identificatie"/>
			</tabel_identificatie>
			<identificatie>
				<xsl:value-of select="bag:bron/bagtype:documentnummer"/>
			</identificatie>
			<datum>
				<xsl:for-each select="bag:bron/bagtype:documentdatum">
					<xsl:call-template name="date-numeric"/>
				</xsl:for-each>
			</datum>
		</brondocument>
	</xsl:template>
	<!-- jjjj-mm-dd -> jjjjmmdd -->
	<xsl:template name="numeric-date">
		<xsl:value-of select="concat(substring(.,1,4),substring(.,6,2),substring(.,9,2))"/>
	</xsl:template>
	<!-- jjjjmmdd -> jjjj-mm-dd -->
	<xsl:template name="date-numeric">
		<xsl:choose>
			<xsl:when test="string-length(.) &gt; 9">
				<xsl:value-of select="concat(substring(.,1,4),'-',substring(.,5,2),'-',substring(.,7,2))"/>
			</xsl:when>
			<xsl:when test="string-length(.) &gt; 7">
				<xsl:value-of select="concat(substring(.,1,4),'-',substring(.,5,2),'-01')"/>
			</xsl:when>
			<xsl:when test="string-length(.) &gt; 5">
				<xsl:value-of select="concat(substring(.,1,4),'-01-01')"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
