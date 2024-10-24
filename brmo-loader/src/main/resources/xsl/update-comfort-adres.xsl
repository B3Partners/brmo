<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:snp="http://www.kadaster.nl/schemas/brk-levering/snapshot/v20120901"
                xmlns:ko="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-kadastraalobject/v20120701"
                xmlns:typ="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-typen/v20120201"
                xmlns:nen="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-NEN3610-2011/v20120201"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:recht="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-recht/v20120201"
                xmlns:rechtref="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-recht-ref/v20120201"
                xmlns:pers="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-persoon/v20120201"
                xmlns:nhr="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-nhr-rechtspersoon/v20120201"
                xmlns:gba="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-gba-persoon/v20120901"
                xmlns:GbaPersoonRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-gba-persoon-ref/v20120201"
                xmlns:Stuk="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-stuk/v20120201"
                xmlns:bagadres="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-bag-adres/v20120201"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                version="1.0">
    <!-- parameters van het bericht -->
    <xsl:variable name="toestandsdatum" select="/snp:KadastraalObjectSnapshot/snp:toestandsdatum"/>

    <xsl:template match="/">
        <root>
            <data>
                <xsl:for-each select="/snp:KadastraalObjectSnapshot/pers:*">
                    <xsl:apply-templates select="."/>
                </xsl:for-each>
                <xsl:for-each select="/snp:KadastraalObjectSnapshot/nhr:*">
                    <xsl:apply-templates select="."/>
                </xsl:for-each>
                <xsl:for-each select="/snp:KadastraalObjectSnapshot/gba:*">
                    <xsl:apply-templates select="."/>
                </xsl:for-each>
            </data>
        </root>
    </xsl:template>

    <!--Persoon-->
    <xsl:template match="/snp:KadastraalObjectSnapshot/pers:KADNatuurlijkPersoon">

        <!-- comfort data -->
        <xsl:variable name="comfort-search-value">
            <xsl:call-template name="nen_identificatie">
                <xsl:with-param name="id" select="pers:identificatie"/>
            </xsl:call-template>
        </xsl:variable>
        <comfort search-table="subject" search-column="identif" search-value="{$comfort-search-value}" snapshot-date="{$toestandsdatum}">

            <xsl:call-template name="persoon">
                <xsl:with-param name="persoon" select="."/>
                <xsl:with-param name="clazz" select="'ANDER NATUURLIJK PERSOON'"/>
            </xsl:call-template>
            <nat_prs>
                <sc_identif>
                    <xsl:call-template name="nen_identificatie">
                        <xsl:with-param name="id" select="pers:identificatie"/>
                    </xsl:call-template>
                </sc_identif>
                <clazz>ANDER NATUURLIJK PERSOON</clazz>
                <geslachtsaand><xsl:value-of select="pers:geslacht/typ:code"/></geslachtsaand>
                <nm_voornamen><xsl:value-of select="pers:voornamen"/></nm_voornamen>
                <nm_voorvoegsel_geslachtsnaam><xsl:value-of select="pers:voorvoegselsGeslachtsnaam"/></nm_voorvoegsel_geslachtsnaam>
                <nm_geslachtsnaam><xsl:value-of select="pers:geslachtsnaam"/></nm_geslachtsnaam>
            </nat_prs>
            <ander_nat_prs>
                <sc_identif>
                    <xsl:call-template name="nen_identificatie">
                        <xsl:with-param name="id" select="pers:identificatie"/>
                    </xsl:call-template>
                </sc_identif>
                <geboortedatum>
                    <xsl:for-each select="pers:geboortedatum"><xsl:call-template name="numeric-date"/></xsl:for-each>
                </geboortedatum>
                <overlijdensdatum>
                    <xsl:for-each select="pers:datumOverlijden"><xsl:call-template name="numeric-date"/></xsl:for-each>
                </overlijdensdatum>
            </ander_nat_prs>
        </comfort>
    </xsl:template>

    <xsl:template match="/snp:KadastraalObjectSnapshot/gba:Ingezetene">
        <!-- comfort data -->
        <xsl:variable name="comfort-search-value">
            <xsl:call-template name="nen_identificatie">
                <xsl:with-param name="id" select="pers:identificatie"/>
            </xsl:call-template>
        </xsl:variable>
        <comfort search-table="subject" search-column="identif" search-value="{$comfort-search-value}" snapshot-date="{$toestandsdatum}">

            <xsl:call-template name="persoon">
                <xsl:with-param name="persoon" select="."/>
                <xsl:with-param name="clazz" select="'INGESCHREVEN NATUURLIJK PERSOON'"/>
            </xsl:call-template>
            <nat_prs>
                <clazz>INGESCHREVEN NATUURLIJK PERSOON</clazz>
                <xsl:call-template name="geregistreerd_persoon-nat_persoon">
                    <xsl:with-param name="persoon" select="."/>
                </xsl:call-template>
            </nat_prs>
            <ingeschr_nat_prs>
                <xsl:call-template name="geregistreerd_persoon-ingeschr_nat_persoon">
                    <xsl:with-param name="persoon" select="."/>
                </xsl:call-template>
            </ingeschr_nat_prs>
        </comfort>
    </xsl:template>

    <xsl:template match="/snp:KadastraalObjectSnapshot/gba:NietIngezetene">
        <!-- comfort data -->
        <xsl:variable name="comfort-search-value">
            <xsl:call-template name="nen_identificatie">
                <xsl:with-param name="id" select="pers:identificatie"/>
            </xsl:call-template>
        </xsl:variable>
        <comfort search-table="subject" search-column="identif" search-value="{$comfort-search-value}" snapshot-date="{$toestandsdatum}">

            <xsl:call-template name="persoon">
                <xsl:with-param name="persoon" select="."/>
                <xsl:with-param name="clazz" select="'INGESCHREVEN NATUURLIJK PERSOON'"/>
            </xsl:call-template>
            <nat_prs>
                <clazz>INGESCHREVEN NATUURLIJK PERSOON</clazz>
                <xsl:call-template name="geregistreerd_persoon-nat_persoon">
                    <xsl:with-param name="persoon" select="."/>
                </xsl:call-template>
            </nat_prs>
            <ingeschr_nat_prs>
                <xsl:call-template name="geregistreerd_persoon-ingeschr_nat_persoon">
                    <xsl:with-param name="persoon" select="."/>
                </xsl:call-template>
                <!--fk_17lnd_code_iso> XXX conversie naar 2-letterige ISO code
                    <xsl:value-of select="gba:landWaarnaarVertrokken/typ:waarde"/>
                </fk_17lnd_code_iso-->
            </ingeschr_nat_prs>
            <niet_ingezetene>
                <sc_identif>
                    <!-- in levering allemaal het zelfde, dus tijdelijk de lokaal id;-->
                    <!--xsl:value-of select="gba:BSN"/-->
                    <xsl:call-template name="nen_identificatie">
                        <xsl:with-param name="id" select="pers:identificatie"/>
                    </xsl:call-template>
                </sc_identif>
            </niet_ingezetene>

        </comfort>
    </xsl:template>

    <xsl:template match="/snp:KadastraalObjectSnapshot/pers:KADNietNatuurlijkPersoon">
        <!-- comfort data -->
        <xsl:variable name="comfort-search-value">
            <xsl:call-template name="nen_identificatie">
                <xsl:with-param name="id" select="pers:identificatie"/>
            </xsl:call-template>
        </xsl:variable>
        <comfort search-table="subject" search-column="identif" search-value="{$comfort-search-value}" snapshot-date="{$toestandsdatum}">

            <xsl:call-template name="persoon">
                <xsl:with-param name="persoon" select="."/>
                <xsl:with-param name="clazz" select="'INGESCHREVEN NIET-NATUURLIJK PERSOON'"/>
            </xsl:call-template>

            <niet_nat_prs>
                <xsl:call-template name="rechtspersoon-niet_nat_persoon">
                    <xsl:with-param name="persoon" select="."/>
                    <xsl:with-param name="clazz" select="'INGESCHREVEN NIET-NATUURLIJK PERSOON'"/>
                </xsl:call-template>
            </niet_nat_prs>
            <ingeschr_niet_nat_prs>
                <sc_identif>
                    <xsl:call-template name="nen_identificatie">
                        <xsl:with-param name="id" select="pers:identificatie"/>
                    </xsl:call-template>
                </sc_identif>
                <rechtsvorm><xsl:value-of select="pers:rechtsvorm/typ:waarde"/></rechtsvorm>
                <statutaire_zetel><xsl:value-of select="pers:statutaireZetel"/></statutaire_zetel>
            </ingeschr_niet_nat_prs>

        </comfort>
    </xsl:template>

    <xsl:template match="/snp:KadastraalObjectSnapshot/nhr:Rechtspersoon">
        <!-- comfort data -->
        <xsl:variable name="comfort-search-value">
            <xsl:call-template name="nen_identificatie">
                <xsl:with-param name="id" select="pers:identificatie"/>
            </xsl:call-template>
        </xsl:variable>
        <comfort search-table="subject" search-column="identif" search-value="{$comfort-search-value}" snapshot-date="{$toestandsdatum}">

            <xsl:call-template name="persoon">
                <xsl:with-param name="persoon" select="."/>
                <xsl:with-param name="clazz" select="'INGESCHREVEN NIET-NATUURLIJK PERSOON'"/>
            </xsl:call-template>

            <niet_nat_prs>
                <sc_identif>
                    <xsl:call-template name="nen_identificatie">
                        <xsl:with-param name="id" select="pers:identificatie"/>
                    </xsl:call-template>
                </sc_identif>
                <clazz>INGESCHREVEN NIET-NATUURLIJK PERSOON</clazz>
                <naam>
                    <xsl:value-of select="nhr:statutaireNaam"/>
                </naam>
            </niet_nat_prs>
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

        </comfort>
    </xsl:template>

    <!-- templates voor persoon -->
    <xsl:template name="persoon">
        <xsl:param name="persoon"/>
        <xsl:param name="clazz"/>

        <subject>
            <identif>
                <xsl:call-template name="nen_identificatie">
                    <xsl:with-param name="id" select="$persoon/pers:identificatie"/>
                </xsl:call-template>
            </identif>
            <xsl:if test="$clazz">
                <clazz><xsl:value-of select="$clazz"/></clazz>
            </xsl:if>
            <xsl:for-each select="nhr:KVKnummer">
                <kvk_nummer><xsl:value-of select="."/></kvk_nummer>
            </xsl:for-each>
            <!--257 char, vullen met woonadres -->
            <adres_binnenland>
                <xsl:if test="pers:woonlocatie/adres:KADBinnenlandsAdres" xmlns:adres="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-adres/v20120201">
                    <xsl:for-each select="pers:woonlocatie">
                        <xsl:variable name="woonadres">
                            <xsl:call-template name="describe-locatie" />
                        </xsl:variable>
                        <xsl:value-of select="substring($woonadres,1,257)"/>
                    </xsl:for-each>
                </xsl:if>
            </adres_binnenland>
            <!--
            <adres_buitenland>149 char</adres_buitenland>
            <vb_adres_buitenland_1>35 char</vb_adres_buitenland_1>
            <vb_adres_buitenland_2>35 char</vb_adres_buitenland_2>
            <vb_adres_buitenland_3>35 char</vb_adres_buitenland_3>
            -->
            <pa_postadres_postcode>
                <xsl:value-of select="pers:postlocatie/adres:PostbusAdres/adres:postcode" xmlns:adres="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-adres/v20120201"/>
            </pa_postadres_postcode>
            <!-- P voor postbus, antwoordnummers komen niet voor in ImkadAdres want
                "Een locatie kan een binnenlandse locatie, een buitenlandse locatie of een postbusadres zijn." -->
            <pa_postadrestype>
                <xsl:if test="pers:postlocatie/adres:PostbusAdres/adres:postbusnummer" xmlns:adres="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-adres/v20120201">
                    <xsl:text>P</xsl:text>
                </xsl:if>
            </pa_postadrestype>
            <pa_postbus__of_antwoordnummer>
                <xsl:value-of select="pers:postlocatie/adres:PostbusAdres/adres:postbusnummer" xmlns:adres="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-adres/v20120201"/>
            </pa_postbus__of_antwoordnummer>

        </subject>

        <prs>
            <sc_identif>
                <xsl:call-template name="nen_identificatie">
                    <xsl:with-param name="id" select="$persoon/pers:identificatie"/>
                </xsl:call-template>
            </sc_identif>
            <xsl:if test="$clazz">
                <clazz><xsl:value-of select="$clazz"/></clazz>
            </xsl:if>
        </prs>
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
        <!--fk_gb_lnd_code_iso> XXX conversie naar 2-letterige ISO code
            <xsl:value-of select="$persoon/gba:geboorte/gba:geboorteland/typ:code"/>
        </fk_gb_lnd_code_iso-->
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

    <xsl:template name="geregistreerd_persoon-nat_persoon">
        <xsl:param name="persoon"/>
        <sc_identif>
            <xsl:call-template name="nen_identificatie">
                <xsl:with-param name="id" select="$persoon/pers:identificatie"/>
            </xsl:call-template>
        </sc_identif>
        <nm_geslachtsnaam>
            <xsl:value-of select="$persoon/gba:naam/gba:geslachtsnaam"/>
        </nm_geslachtsnaam>
        <nm_voornamen>
            <xsl:value-of select="$persoon/gba:naam/gba:voornamen"/>
        </nm_voornamen>
        <nm_voorvoegsel_geslachtsnaam>
            <xsl:value-of select="$persoon/gba:naam/gba:voorvoegselsgeslachtsnaam"/>
        </nm_voorvoegsel_geslachtsnaam>
        <geslachtsaand>
            <xsl:value-of select="$persoon/gba:geslacht/gba:geslachtsaanduiding/typ:code"/>
        </geslachtsaand>
        <aand_naamgebruik>
            <xsl:value-of select="$persoon/gba:aanduidingNaamgebruik/typ:code"/>
        </aand_naamgebruik>
    </xsl:template>

    <xsl:template name="rechtspersoon-niet_nat_persoon">
        <xsl:param name="persoon"/>
        <xsl:param name="clazz"/>
        <sc_identif>
            <xsl:call-template name="nen_identificatie">
                <xsl:with-param name="id" select="$persoon/pers:identificatie"/>
            </xsl:call-template>
        </sc_identif>
        <xsl:if test="$clazz">
            <clazz><xsl:value-of select="$clazz"/></clazz>
        </xsl:if>
        <naam>
            <xsl:value-of select="$persoon/pers:naam"/>
        </naam>
    </xsl:template>

    <xsl:template name="nen_identificatie">
        <xsl:param name="id"/>
        <xsl:value-of select="$id/nen:namespace"/>.<xsl:value-of select="$id/nen:lokaalId"/>
    </xsl:template>

    <!-- Levert een string op die een locatie beschrijft:
      - PostbusAdres
      - KADBinnenlandsAdres
      - Ligplaats/Standplaats/Verblijfsobject
      - BuitenlandsAdres
      - KADBuitenlandsAdres
    -->
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


    <!-- jjjj-mm-dd -> jjjjmmdd -->
    <xsl:template name="numeric-date">
        <xsl:value-of select="concat(substring(.,1,4),substring(.,6,2),substring(.,9,2))"/>
    </xsl:template>

</xsl:stylesheet>