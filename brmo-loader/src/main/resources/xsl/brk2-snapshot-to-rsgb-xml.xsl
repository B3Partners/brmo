<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:Stand="http://www.kadaster.nl/schemas/brk-levering/product-stand/v20211119"
                xmlns:Snapshot="http://www.kadaster.nl/schemas/brk-levering/snapshot/v20211119"
                xmlns:Adres="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-adres/v20211119"
                xmlns:Adres-ref="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-adres-ref/v20210702"
                xmlns:KadastraalObject="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-kadastraalobject/v20210702"
                xmlns:OnroerendeZaak="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-onroerendezaak/v20211119"
                xmlns:OnroerendeZaak-ref="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-onroerendezaak-ref/v20210702"
                xmlns:Persoon="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-persoon/v20210903"
                xmlns:Persoon-ref="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-persoon-ref/v20210702"
                xmlns:PubliekrechtelijkeBeperking="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-publiekrechtelijkebeperking/v20210702"
                xmlns:PubliekrechtelijkeBeperking-ref="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-publiekrechtelijkebeperking-ref/v20210702"
                xmlns:Recht="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-recht/v20210903"
                xmlns:Recht-ref="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-recht-ref/v20210702"
                xmlns:Stuk="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-stuk/v20211119"
                xmlns:Stuk-ref="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-stuk-ref/v20210702"
                xmlns:Typen="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-typen/v20210702"
                xmlns:KIMBAGAdres="http://www.kadaster.nl/schemas/brk-levering/snapshot/kimbag-adres/v20210903"
                xmlns:KIMBRPPersoon="http://www.kadaster.nl/schemas/brk-levering/snapshot/kimbrp-persoon/v20210903"
                xmlns:KIMBRPAdres="http://www.kadaster.nl/schemas/brk-levering/snapshot/kimbrp-adres/v20210702"
                xmlns:KIMNHRRechtspersoon="http://www.kadaster.nl/schemas/brk-levering/snapshot/kimnhr-rechtspersoon/v20210702"
                version="1.0">

    <!--        <xsl:strip-space elements="*"/>-->
    <!--     method="xml" omit-xml-declaration="no" -->
    <!--    <xsl:output encoding="UTF-8" version="1.0" standalone="yes" indent="yes"/>-->

    <!-- parameters van het bericht -->
    <xsl:param name="objectRef" select="'NL.KAD.OnroerendeZaak:onbekend'"/>
    <xsl:param name="datum" select="'datum-onbekend'"/>
    <xsl:param name="volgordeNummer" select="'volgordeNummer-onbekend'"/>
    <xsl:param name="soort" select="'soort-onbekend'"/>
    <xsl:variable name="kad_oz_id"
                  select="Snapshot:KadastraalObjectSnapshot/OnroerendeZaak:Perceel/KadastraalObject:identificatie |
                         Snapshot:KadastraalObjectSnapshot/OnroerendeZaak:Appartementsrecht/KadastraalObject:identificatie"
    />
    <xsl:variable name="toestandsdatum" select="Snapshot:KadastraalObjectSnapshot/Snapshot:toestandsdatum"/>

    <xsl:template match="/">
        <root>
            <xsl:comment>
                <xsl:text>objectRef: </xsl:text>
                <xsl:value-of select="$objectRef"/>
                <xsl:text>, datum: </xsl:text>
                <xsl:value-of select="$datum"/>
                <xsl:text>, volgordeNummer: </xsl:text>
                <xsl:value-of select="$volgordeNummer"/>
                <xsl:text>, soort: </xsl:text>
                <xsl:value-of select="$soort"/>
            </xsl:comment>
            <data>
                <xsl:if test="not(/Snapshot:KadastraalObjectSnapshot)">
                    <!--    
                        als bericht geen KadastraalObjectSnapshot bevat dan moet dit object verwijderd worden.
                        Alleen onroerendezaak verwijderen en vertrouwen op delete-cascade.
                    -->
                    <delete>
                        <onroerendezaak>
                            <identificatie>
                                <xsl:value-of select="$objectRef"/>
                            </identificatie>
                        </onroerendezaak>
                    </delete>
                </xsl:if>

                <!--
                    TODO bepalen of we ook de KIMBAGAdres moeten verwerken
                         bevat waarschijnlijk een BAG referentie naar een adres
                -->
                <xsl:for-each select="/Snapshot:KadastraalObjectSnapshot/Adres:* |
                         /Snapshot:KadastraalObjectSnapshot/KIMBAGAdres:Verblijfsobject |
                         /Snapshot:KadastraalObjectSnapshot/KIMBAGAdres:Standplaats |
                         /Snapshot:KadastraalObjectSnapshot/KIMBAGAdres:Ligplaats">
                    <xsl:apply-templates select="."/>
                </xsl:for-each>

                <xsl:for-each select="/Snapshot:KadastraalObjectSnapshot/Persoon:*">
                    <xsl:apply-templates select="."/>
                </xsl:for-each>

                <xsl:for-each
                        select="/Snapshot:KadastraalObjectSnapshot/Stuk:TerInschrijvingAangebodenStuk | /Snapshot:KadastraalObjectSnapshot/Stuk:Kadasterstuk">
                    <xsl:apply-templates select="."/>
                </xsl:for-each>

                <xsl:apply-templates select="/Snapshot:KadastraalObjectSnapshot/OnroerendeZaak:Perceel"/>

                <xsl:for-each select="/Snapshot:KadastraalObjectSnapshot/Recht:Hoofdsplitsing">
                    <!-- eerst de hoofdsplitsing vanwege verwijzing naar appartementsrecht en later andere rechten -->
                    <xsl:call-template name="recht">
                        <xsl:with-param name="recht" select="."/>
                    </xsl:call-template>
                </xsl:for-each>

                <xsl:apply-templates select="/Snapshot:KadastraalObjectSnapshot/OnroerendeZaak:Appartementsrecht"/>

                <!-- alle verschillende rechten in de juiste? volgorde voor insert -->
                <xsl:for-each select="/Snapshot:KadastraalObjectSnapshot/Recht:Ondersplitsing">
                    <xsl:call-template name="recht">
                        <xsl:with-param name="recht" select="."/>
                    </xsl:call-template>
                </xsl:for-each>
                <xsl:for-each select="/Snapshot:KadastraalObjectSnapshot/Recht:SpiegelsplitsingAfkoopErfpacht">
                    <xsl:call-template name="recht">
                        <xsl:with-param name="recht" select="."/>
                    </xsl:call-template>
                </xsl:for-each>
                <xsl:for-each select="/Snapshot:KadastraalObjectSnapshot/Recht:SpiegelsplitsingOndersplitsing">
                    <xsl:call-template name="recht">
                        <xsl:with-param name="recht" select="."/>
                    </xsl:call-template>
                </xsl:for-each>
                <xsl:for-each select="/Snapshot:KadastraalObjectSnapshot/Recht:Erfpachtcanon">
                    <xsl:call-template name="recht">
                        <xsl:with-param name="recht" select="."/>
                    </xsl:call-template>
                </xsl:for-each>
                <xsl:for-each select="/Snapshot:KadastraalObjectSnapshot/Recht:GezamenlijkAandeel">
                    <xsl:call-template name="recht">
                        <xsl:with-param name="recht" select="."/>
                    </xsl:call-template>
                </xsl:for-each>
                <xsl:for-each select="/Snapshot:KadastraalObjectSnapshot/Recht:Tenaamstelling">
                    <xsl:call-template name="recht">
                        <xsl:with-param name="recht" select="."/>
                    </xsl:call-template>
                </xsl:for-each>
                <xsl:for-each select="/Snapshot:KadastraalObjectSnapshot/Recht:Mandeligheid">
                    <xsl:call-template name="recht">
                        <xsl:with-param name="recht" select="."/>
                    </xsl:call-template>
                </xsl:for-each>
                <xsl:for-each select="/Snapshot:KadastraalObjectSnapshot/Recht:Aantekening">
                    <xsl:call-template name="recht">
                        <xsl:with-param name="recht" select="."/>
                    </xsl:call-template>
                </xsl:for-each>
                <xsl:for-each select="/Snapshot:KadastraalObjectSnapshot/Recht:ZakelijkRecht">
                    <!-- recht met laagste identificatie eerst - met aanname dat lagere nummers geen verwijzing hebben naar hogere nummers, maar wel andersom -->
                    <xsl:sort select="Recht:identificatie" data-type="number" order="descending"/>
                    <xsl:call-template name="recht">
                        <xsl:with-param name="recht" select="."/>
                    </xsl:call-template>
                </xsl:for-each>

                <xsl:for-each
                        select="/Snapshot:KadastraalObjectSnapshot/PubliekrechtelijkeBeperking:PubliekrechtelijkeBeperking">
                    <xsl:apply-templates select="."/>
                </xsl:for-each>

                <!-- Koppel tabellen -->
                <xsl:for-each
                        select="/Snapshot:KadastraalObjectSnapshot/KadastraalObject:LocatieKadastraalObject">
                    <xsl:apply-templates select="."/>
                </xsl:for-each>

            </data>
        </root>
    </xsl:template>

    <xsl:template match="/Snapshot:KadastraalObjectSnapshot/PubliekrechtelijkeBeperking:PubliekrechtelijkeBeperking">
        <xsl:variable name="beperkingId">
            <xsl:call-template name="domein_identificatie">
                <xsl:with-param name="id" select="PubliekrechtelijkeBeperking:identificatie"/>
            </xsl:call-template>
        </xsl:variable>

        <!-- vult publiekrechtelijkebeperking en gerelateerde onroerendezaakbeperking -->
        <publiekrechtelijkebeperking>
            <identificatie>
                <xsl:value-of select="$beperkingId"/>
            </identificatie>
            <grondslag>
                <xsl:value-of select="PubliekrechtelijkeBeperking:grondslag/Typen:waarde"/>
            </grondslag>
            <datuminwerking>
                <xsl:value-of select="PubliekrechtelijkeBeperking:datumInWerking"/>
            </datuminwerking>
            <datumbeeindiging>
                <xsl:value-of select="PubliekrechtelijkeBeperking:datumBeeindiging"/>
            </datumbeeindiging>
            <isgebaseerdop>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="PubliekrechtelijkeBeperking:isGebaseerdOp/Stuk-ref:StukdeelRef"/>
                </xsl:call-template>
            </isgebaseerdop>
            <bevoegdgezag>
                <!-- NNP referentie
                        zoek de bevoegdgezag
                        ../PubliekrechtelijkeBeperking:BevoegdGezag/PubliekrechtelijkeBeperking:is/Persoon-ref:NietNatuurlijkPersoonRef
                        welke verwijst naar deze publiekrechtelijkebeperking met
                        ../PubliekrechtelijkeBeperking:BevoegdGezag/PubliekrechtelijkeBeperking:beheert/PubliekrechtelijkeBeperking-ref:PubliekrechtelijkeBeperkingRef
                -->
                <xsl:variable name="gezagLinkId">
                    <xsl:value-of select="@id"/>
                </xsl:variable>
                <xsl:variable name="bevoegdGezag"
                              select="../PubliekrechtelijkeBeperking:BevoegdGezag[PubliekrechtelijkeBeperking:beheert/PubliekrechtelijkeBeperking-ref:PubliekrechtelijkeBeperkingRef[substring(@xlink:href,2) = $gezagLinkId]]"/>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id"
                                    select="$bevoegdGezag/PubliekrechtelijkeBeperking:is/Persoon-ref:NietNatuurlijkPersoonRef"/>
                </xsl:call-template>
            </bevoegdgezag>
        </publiekrechtelijkebeperking>

        <xsl:for-each select="PubliekrechtelijkeBeperking:leidtTot">
            <onroerendezaakbeperking>
                <inonderzoek>
                    <xsl:value-of
                            select="PubliekrechtelijkeBeperking:OnroerendeZaakBeperking/PubliekrechtelijkeBeperking:inOnderzoek"/>
                </inonderzoek>
                <beperkt>
                    <xsl:call-template name="domein_identificatie">
                        <xsl:with-param name="id"
                                        select="PubliekrechtelijkeBeperking:OnroerendeZaakBeperking/PubliekrechtelijkeBeperking:beperkt/OnroerendeZaak-ref:PerceelRef |
                                                PubliekrechtelijkeBeperking:OnroerendeZaakBeperking/PubliekrechtelijkeBeperking:beperkt/OnroerendeZaak-ref:AppartementsrechtRef"/>
                    </xsl:call-template>
                </beperkt>
                <leidttot>
                    <xsl:value-of select="$beperkingId"/>
                </leidttot>
            </onroerendezaakbeperking>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="/Snapshot:KadastraalObjectSnapshot/Adres:* |
                         /Snapshot:KadastraalObjectSnapshot/KIMBAGAdres:Verblijfsobject |
                         /Snapshot:KadastraalObjectSnapshot/KIMBAGAdres:Standplaats |
                         /Snapshot:KadastraalObjectSnapshot/KIMBAGAdres:Ligplaats">
        <adres ignore-duplicates="yes">
            <identificatie>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="Adres:identificatie|KIMBAGAdres:identificatie"/>
                </xsl:call-template>
            </identificatie>
            <huisnummer>
                <xsl:value-of select="Adres:adresgegevens/KIMBAGAdres:Nummeraanduiding/KIMBAGAdres:huisnummer"/>
            </huisnummer>
            <huisletter>
                <xsl:value-of select="Adres:adresgegevens/KIMBAGAdres:Nummeraanduiding/KIMBAGAdres:huisletter"/>
            </huisletter>
            <huisnummertoevoeging>
                <xsl:value-of
                        select="Adres:adresgegevens/KIMBAGAdres:Nummeraanduiding/KIMBAGAdres:huisnummertoevoeging"/>
            </huisnummertoevoeging>
            <postbusnummer>
                <xsl:value-of select="Adres:postbusnummer"/>
            </postbusnummer>
            <postcode>
                <xsl:value-of
                        select="Adres:postcode|Adres:adresgegevens/KIMBAGAdres:Nummeraanduiding/KIMBAGAdres:postcode"/>
            </postcode>
            <openbareruimtenaam>
                <xsl:value-of
                        select="Adres:adresgegevens/KIMBAGAdres:Nummeraanduiding/KIMBAGAdres:gerelateerdeOpenbareRuimte/KIMBAGAdres:OpenbareRuimte/KIMBAGAdres:openbareRuimteNaam"/>
            </openbareruimtenaam>
            <woonplaatsnaam>
                <xsl:value-of
                        select="Adres:woonplaatsNaam|
                        Adres:adresgegevens/KIMBAGAdres:Nummeraanduiding/KIMBAGAdres:gerelateerdeOpenbareRuimte/KIMBAGAdres:OpenbareRuimte/KIMBAGAdres:gerelateerdeWoonplaats/KIMBAGAdres:Woonplaats/KIMBAGAdres:woonplaatsNaam"/>
            </woonplaatsnaam>
            <openbareruimte>
                <!-- TODO: BAG OPR id Controle -->
                <xsl:value-of
                        select="Adres:adresgegevens/KIMBAGAdres:Nummeraanduiding/KIMBAGAdres:gerelateerdeOpenbareRuimte/KIMBAGAdres:OpenbareRuimte/KIMBAGAdres:identificatie"/>
            </openbareruimte>
            <verblijfsobject>
                <xsl:if test="local-name() = 'Verblijfsobject'">
                    <xsl:value-of select="KIMBAGAdres:identificatie"/>
                </xsl:if>
            </verblijfsobject>
            <adresseerbaarobject>
                <!-- TODO: BAG ADR id -->
            </adresseerbaarobject>
            <nummeraanduiding>
                <xsl:value-of select="Adres:adresgegevens/KIMBAGAdres:Nummeraanduiding/KIMBAGAdres:identificatie"/>
            </nummeraanduiding>
            <standplaats>
                <xsl:if test="local-name() = 'Standplaats'">
                    <xsl:value-of select="KIMBAGAdres:identificatie"/>
                </xsl:if>
            </standplaats>
            <ligplaats>
                <xsl:if test="local-name() = 'Ligplaats'">
                    <xsl:value-of select="KIMBAGAdres:identificatie"/>
                </xsl:if>
            </ligplaats>
            <nevenadres>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="KIMBAGAdres:nevenadres/Adres-ref:ObjectlocatieBinnenlandRef"/>
                </xsl:call-template>
            </nevenadres>
            <hoofdadres>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="KIMBAGAdres:hoofdadres/Adres-ref:ObjectlocatieBinnenlandRef"/>
                </xsl:call-template>
            </hoofdadres>
            <!-- adres buitenland -->
            <buitenlandadres>
                <xsl:value-of select="Adres:adresgegevens/KIMBRPAdres:AdresBuitenland/KIMBRPAdres:adres"/>
            </buitenlandadres>
            <buitenlandwoonplaats>
                <xsl:value-of select="Adres:adresgegevens/KIMBRPAdres:AdresBuitenland/KIMBRPAdres:woonplaats"/>
            </buitenlandwoonplaats>
            <buitenlandregio>
                <xsl:value-of select="Adres:adresgegevens/KIMBRPAdres:AdresBuitenland/KIMBRPAdres:regio"/>
            </buitenlandregio>
            <land>
                <xsl:value-of select="Adres:adresgegevens/KIMBRPAdres:AdresBuitenland/KIMBRPAdres:land/Typen:waarde"/>
            </land>
        </adres>
    </xsl:template>


    <xsl:template match="/Snapshot:KadastraalObjectSnapshot/KadastraalObject:LocatieKadastraalObject">
        <objectlocatie>
            <heeft>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="KadastraalObject:heeft/OnroerendeZaak-ref:PerceelRef |
                                                  KadastraalObject:heeft/OnroerendeZaak-ref:AppartementsrechtRef"/>
                </xsl:call-template>
            </heeft>
            <betreft>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="KadastraalObject:betreft/Adres-ref:ObjectlocatieBinnenlandRef"/>
                </xsl:call-template>
            </betreft>
            <koppelingswijze>
                <xsl:value-of select="KadastraalObject:koppelingswijze/Typen:waarde"/>
            </koppelingswijze>
        </objectlocatie>
    </xsl:template>


    <xsl:template match="/Snapshot:KadastraalObjectSnapshot/OnroerendeZaak:Perceel">
        <xsl:call-template name="kadastraal_onroerende_zaak">
            <xsl:with-param name="oz" select="."/>
        </xsl:call-template>

        <perceel>
            <begingeldigheid alleen-archief="true">
                <xsl:value-of select="$toestandsdatum"/>
            </begingeldigheid>
            <identificatie>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="KadastraalObject:identificatie"/>
                </xsl:call-template>
            </identificatie>
            <begrenzing_perceel>
                <!--     TODO geometrie parsen mislukt...
                <xsl:copy-of select="OnroerendeZaak:begrenzingPerceel/gml:Surface"/>
                -->
            </begrenzing_perceel>
            <kadastralegrootte>
                <xsl:value-of select="OnroerendeZaak:kadastraleGrootte/OnroerendeZaak:waarde"/>
            </kadastralegrootte>
            <soortgrootte>
                <xsl:value-of select="OnroerendeZaak:kadastraleGrootte/OnroerendeZaak:soortGrootte/Typen:waarde"/>
            </soortgrootte>
            <perceelnummerrotatie>
                <xsl:value-of select="OnroerendeZaak:perceelnummerRotatie"/>
            </perceelnummerrotatie>
            <perceelnummer_deltax>
                <xsl:value-of select="OnroerendeZaak:perceelnummerVerschuiving/OnroerendeZaak:deltaX"/>
            </perceelnummer_deltax>
            <perceelnummer_deltay>
                <xsl:value-of select="OnroerendeZaak:perceelnummerVerschuiving/OnroerendeZaak:deltaY"/>
            </perceelnummer_deltay>
            <plaatscoordinaten>
                <xsl:copy-of select="OnroerendeZaak:plaatscoordinaten/gml:Point"/>
            </plaatscoordinaten>
            <meettariefverschuldigd>
                <xsl:value-of select="OnroerendeZaak:meettariefVerschuldigd"/>
            </meettariefverschuldigd>
        </perceel>
    </xsl:template>

    <xsl:template match="/Snapshot:KadastraalObjectSnapshot/OnroerendeZaak:Appartementsrecht">
        <xsl:call-template name="kadastraal_onroerende_zaak">
            <xsl:with-param name="oz" select="."/>
        </xsl:call-template>

        <appartementsrecht>
            <begingeldigheid alleen-archief="true">
                <xsl:value-of select="$toestandsdatum"/>
            </begingeldigheid>
            <identificatie>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="KadastraalObject:identificatie"/>
                </xsl:call-template>
            </identificatie>
            <hoofdsplitsing>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="OnroerendeZaak:hoofdsplitsing/Recht-ref:HoofdsplitsingRef"/>
                </xsl:call-template>
            </hoofdsplitsing>
        </appartementsrecht>
    </xsl:template>

    <xsl:template match="/Snapshot:KadastraalObjectSnapshot/Persoon:NatuurlijkPersoon">
        <!-- comfort data -->
        <xsl:variable name="comfort-search-value">
            <xsl:call-template name="domein_identificatie">
                <xsl:with-param name="id" select="Persoon:identificatie"/>
            </xsl:call-template>
        </xsl:variable>

        <comfort search-table="natuurlijkpersoon" search-column="identificatie" search-value="{$comfort-search-value}"
                 snapshot-date="{$toestandsdatum}">

            <xsl:call-template name="persoon">
                <xsl:with-param name="persoon" select="."/>
                <xsl:with-param name="clazz" select="'natuurlijkpersoon'"/>
            </xsl:call-template>

            <natuurlijkpersoon>
                <identificatie>
                    <xsl:value-of select="$comfort-search-value"/>
                </identificatie>
                <indicatieoverleden>
                    <xsl:value-of select="Persoon:indicatieOverleden"/>
                </indicatieoverleden>
                <indicatieafschermingpersoonsgegevens>
                    <xsl:value-of select="Persoon:indicatieAfschermingPersoonsgegevens"/>
                </indicatieafschermingpersoonsgegevens>
                <bsn>
                    <xsl:value-of select="Persoon:betreft/KIMBRPPersoon:GeregistreerdPersoon/KIMBRPPersoon:bsn"/>
                </bsn>
                <adellijketitelofpredicaat>
                    <xsl:value-of
                            select="Persoon:betreft/KIMBRPPersoon:GeregistreerdPersoon/KIMBRPPersoon:adellijkeTitelOfPredicaat/Typen:waarde"/>
                </adellijketitelofpredicaat>
                <aanduidingnaamgebruik>
                    <xsl:value-of
                            select="Persoon:betreft/KIMBRPPersoon:GeregistreerdPersoon/KIMBRPPersoon:aanduidingNaamgebruik/Typen:waarde"/>
                </aanduidingnaamgebruik>
                <landwaarnaarvertrokken>
                    <xsl:value-of
                            select="Persoon:betreft/KIMBRPPersoon:GeregistreerdPersoon/KIMBRPPersoon:landWaarnaarVertrokken/Typen:waarde"/>
                </landwaarnaarvertrokken>
                <geslachtsnaam>
                    <xsl:value-of
                            select="Persoon:betreft/KIMBRPPersoon:GeregistreerdPersoon/KIMBRPPersoon:naam/KIMBRPPersoon:Naam/KIMBRPPersoon:geslachtsnaam"/>
                </geslachtsnaam>
                <voornamen>
                    <xsl:value-of
                            select="Persoon:betreft/KIMBRPPersoon:GeregistreerdPersoon/KIMBRPPersoon:naam/KIMBRPPersoon:Naam/KIMBRPPersoon:voornamen"/>
                </voornamen>
                <voorvoegselsgeslachtsnaam>
                    <xsl:value-of
                            select="Persoon:betreft/KIMBRPPersoon:GeregistreerdPersoon/KIMBRPPersoon:naam/KIMBRPPersoon:Naam/KIMBRPPersoon:voorvoegselsgeslachtsnaam"/>
                </voorvoegselsgeslachtsnaam>
                <geslacht>
                    <xsl:value-of
                            select="Persoon:betreft/KIMBRPPersoon:GeregistreerdPersoon/KIMBRPPersoon:geslacht/KIMBRPPersoon:Geslacht/KIMBRPPersoon:geslachtsaanduiding/Typen:waarde"/>
                </geslacht>
                <geboortedatum>
                    <xsl:value-of
                            select="Persoon:betreft/KIMBRPPersoon:GeregistreerdPersoon/KIMBRPPersoon:geboorte/KIMBRPPersoon:Geboorte/KIMBRPPersoon:geboortedatum/Typen:datum"/>
                </geboortedatum>
                <geboorteplaats>
                    <xsl:value-of
                            select="Persoon:betreft/KIMBRPPersoon:GeregistreerdPersoon/KIMBRPPersoon:geboorte/KIMBRPPersoon:Geboorte/KIMBRPPersoon:geboorteplaats"/>
                </geboorteplaats>
                <geboorteland>
                    <xsl:value-of
                            select="Persoon:betreft/KIMBRPPersoon:GeregistreerdPersoon/KIMBRPPersoon:geboorte/KIMBRPPersoon:Geboorte/KIMBRPPersoon:geboorteland/Typen:waarde"/>
                </geboorteland>
                <indicatiegeheim>
                    <xsl:value-of
                            select="Persoon:betreft/KIMBRPPersoon:GeregistreerdPersoon/KIMBRPPersoon:indicatieGeheim"/>
                </indicatiegeheim>
                <datumoverlijden>
                    <xsl:value-of
                            select="Persoon:betreft/KIMBRPPersoon:GeregistreerdPersoon/KIMBRPPersoon:overlijden/KIMBRPPersoon:Overlijden/KIMBRPPersoon:datumOverlijden/Typen:datum"/>
                </datumoverlijden>
                <partnergeslachtsnaam>
                    <xsl:value-of
                            select="Persoon:betreft/KIMBRPPersoon:GeregistreerdPersoon/KIMBRPPersoon:heeftPartnerschap/KIMBRPPersoon:Partnerschap/KIMBRPPersoon:naamPartner/KIMBRPPersoon:Naam/KIMBRPPersoon:geslachtsnaam"/>
                </partnergeslachtsnaam>
                <partnervoornamen>
                    <xsl:value-of
                            select="Persoon:betreft/KIMBRPPersoon:GeregistreerdPersoon/KIMBRPPersoon:heeftPartnerschap/KIMBRPPersoon:Partnerschap/KIMBRPPersoon:naamPartner/KIMBRPPersoon:Naam/KIMBRPPersoon:voornamen"/>
                </partnervoornamen>
                <partnervoorvoegselsgeslachtsnaam>
                    <xsl:value-of
                            select="Persoon:betreft/KIMBRPPersoon:GeregistreerdPersoon/KIMBRPPersoon:heeftPartnerschap/KIMBRPPersoon:Partnerschap/KIMBRPPersoon:naamPartner/KIMBRPPersoon:Naam/KIMBRPPersoon:voorvoegselsgeslachtsnaam"/>
                </partnervoorvoegselsgeslachtsnaam>
            </natuurlijkpersoon>
        </comfort>
    </xsl:template>

    <xsl:template match="/Snapshot:KadastraalObjectSnapshot/Persoon:NietNatuurlijkPersoon">
        <xsl:variable name="comfort-search-value">
            <xsl:call-template name="domein_identificatie">
                <xsl:with-param name="id" select="Persoon:identificatie"/>
            </xsl:call-template>
        </xsl:variable>


        <comfort search-table="nietnatuurlijkpersoon" search-column="identificatie"
                 search-value="{$comfort-search-value}"
                 snapshot-date="{$toestandsdatum}">

            <xsl:call-template name="persoon">
                <xsl:with-param name="persoon" select="."/>
                <xsl:with-param name="clazz" select="'nietnatuurlijkpersoon'"/>
            </xsl:call-template>

            <nietnatuurlijkpersoon>
                <identificatie>
                    <xsl:value-of select="$comfort-search-value"/>
                </identificatie>
                <statutairenaam>
                    <xsl:value-of select="Persoon:statutaireNaam"/>
                </statutairenaam>
                <rechtsvorm>
                    <xsl:value-of select="Persoon:rechtsvorm/Typen:waarde"/>
                </rechtsvorm>
                <statutairezetel>
                    <xsl:value-of select="Persoon:statutaireZetel"/>
                </statutairezetel>
                <rsin>
                    <xsl:value-of select="Persoon:betreft/KIMNHRRechtspersoon:Rechtspersoon/KIMNHRRechtspersoon:rsin"/>
                </rsin>
                <kvknummer>
                    <xsl:value-of
                            select="Persoon:betreft/KIMNHRRechtspersoon:Rechtspersoon/KIMNHRRechtspersoon:kvkNummer"/>
                </kvknummer>
            </nietnatuurlijkpersoon>
        </comfort>
    </xsl:template>

    <!--
        Stukken en bijbehorende stukdelen
    -->
    <xsl:template match="Stuk:TerInschrijvingAangebodenStuk | Stuk:Kadasterstuk">
        <xsl:variable name="stukId">
            <xsl:call-template name="domein_identificatie">
                <xsl:with-param name="id" select="Stuk:identificatie"/>
            </xsl:call-template>
        </xsl:variable>

        <stuk>
            <identificatie>
                <xsl:value-of select="$stukId"/>
            </identificatie>
            <toelichtingbewaarder>
                <xsl:value-of select="Stuk:toelichtingBewaarder"/>
            </toelichtingbewaarder>
            <portefeuillenummer>
                <xsl:value-of select="Stuk:portefeuillenummer"/>
            </portefeuillenummer>
            <deel>
                <xsl:value-of select="Stuk:deelEnNummer/Stuk:deel"/>
            </deel>
            <nummer>
                <xsl:value-of select="Stuk:deelEnNummer/Stuk:nummer"/>
            </nummer>
            <reeks>
                <xsl:value-of select="Stuk:deelEnNummer/Stuk:reeks/Typen:waarde"/>
            </reeks>
            <registercode>
                <xsl:value-of select="Stuk:deelEnNummer/Stuk:registercode/Typen:waarde"/>
            </registercode>
            <soortregister>
                <xsl:value-of select="Stuk:deelEnNummer/Stuk:soortRegister/Typen:waarde"/>
            </soortregister>
            <tijdstipaanbieding>
                <xsl:if test="Stuk:tijdstipAanbieding and not(Stuk:tijdstipAanbieding/@xsi:nil)">
                    <xsl:value-of select="Stuk:tijdstipAanbieding/Typen:datum"/>
                    <xsl:if test="not(Stuk:tijdstipAanbieding/Typen:tijd/@xsi:nil='true')">
                        <xsl:text>T</xsl:text>
                        <xsl:value-of select="Stuk:tijdstipAanbieding/Typen:tijd"/>
                    </xsl:if>
                </xsl:if>
            </tijdstipaanbieding>
            <tijdstipondertekening>
                <xsl:if test="Stuk:tijdstipOndertekening and not(Stuk:tijdstipOndertekening/@xsi:nil)">
                    <xsl:value-of select="Stuk:tijdstipOndertekening/Typen:datum"/>
                    <xsl:if test="not(Stuk:tijdstipOndertekening/Typen:tijd/@xsi:nil='true')">
                        <xsl:text>T</xsl:text>
                        <xsl:value-of select="Stuk:tijdstipOndertekening/Typen:tijd"/>
                    </xsl:if>
                </xsl:if>
            </tijdstipondertekening>
            <tekeningingeschreven>
                <xsl:value-of select="Stuk:tekeningIngeschreven"/>
            </tekeningingeschreven>
        </stuk>

        <xsl:for-each select="Stuk:omvat/Stuk-ref:StukdeelRef">
            <xsl:variable name="stukdeelRefID">
                <xsl:value-of select="substring(@xlink:href,2)"/>
            </xsl:variable>

            <xsl:for-each select="//Stuk:Stukdeel[@id=$stukdeelRefID]">
                <stukdeel>
                    <identificatie>
                        <xsl:call-template name="domein_identificatie">
                            <xsl:with-param name="id" select="Stuk:identificatie"/>
                        </xsl:call-template>
                    </identificatie>
                    <aard>
                        <xsl:value-of select="Stuk:aard/Typen:waarde"/>
                    </aard>
                    <bedragtransactiesomlevering>
                        <xsl:value-of select="Stuk:bedragTransactiesomLevering/Typen:som"/>
                    </bedragtransactiesomlevering>
                    <datumkenbaarheidpb>
                        <xsl:value-of select="Stuk:datumKenbaarheidPB"/>
                    </datumkenbaarheidpb>
                    <deelvan>
                        <xsl:value-of select="$stukId"/>
                    </deelvan>
                </stukdeel>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>


    <!--
    vult tabellen "onroerendezaak" en "onroerendezaakfiliatie"
    -->
    <xsl:template name="kadastraal_onroerende_zaak">
        <xsl:param name="oz"/>

        <xsl:variable name="ozId">
            <xsl:call-template name="domein_identificatie">
                <xsl:with-param name="id" select="$oz/KadastraalObject:identificatie"/>
            </xsl:call-template>
        </xsl:variable>

        <onroerendezaak column-dat-beg-geldh="begingeldigheid" column-datum-einde-geldh="eindegeldigheid">
            <begingeldigheid>
                <xsl:value-of select="$toestandsdatum"/>
            </begingeldigheid>
            <eindegeldigheid></eindegeldigheid>
            <identificatie>
                <xsl:value-of select="$ozId"/>
            </identificatie>
            <akrkadastralegemeentecode>
                <xsl:value-of
                        select="$oz/OnroerendeZaak:kadastraleAanduiding/OnroerendeZaak:akrKadastraleGemeenteCode/Typen:code"/>
            </akrkadastralegemeentecode>
            <akrkadastralegemeente>
                <xsl:value-of
                        select="$oz/OnroerendeZaak:kadastraleAanduiding/OnroerendeZaak:akrKadastraleGemeenteCode/Typen:waarde"/>
            </akrkadastralegemeente>
            <kadastralegemeentecode>
                <xsl:value-of
                        select="$oz/OnroerendeZaak:kadastraleAanduiding/OnroerendeZaak:kadastraleGemeente/Typen:code"/>
            </kadastralegemeentecode>
            <kadastralegemeente>
                <xsl:value-of
                        select="$oz/OnroerendeZaak:kadastraleAanduiding/OnroerendeZaak:kadastraleGemeente/Typen:waarde"/>
            </kadastralegemeente>
            <sectie>
                <xsl:value-of select="$oz/OnroerendeZaak:kadastraleAanduiding/OnroerendeZaak:sectie"/>
            </sectie>
            <perceelnummer>
                <xsl:value-of select="$oz/OnroerendeZaak:kadastraleAanduiding/OnroerendeZaak:perceelnummer"/>
            </perceelnummer>
            <appartementsrechtvolgnummer>
                <xsl:value-of
                        select="$oz/OnroerendeZaak:kadastraleAanduiding/OnroerendeZaak:appartementsrechtVolgnummer"/>
            </appartementsrechtvolgnummer>
            <landinrichtingsrente_bedrag>
                <xsl:value-of
                        select="$oz/OnroerendeZaak:landinrichtingsrente/OnroerendeZaak:TypeLandinrichtingsrente/OnroerendeZaak:bedrag/Typen:som"/>
            </landinrichtingsrente_bedrag>
            <landinrichtingsrente_jaar>
                <xsl:value-of
                        select="$oz/OnroerendeZaak:landinrichtingsrente/OnroerendeZaak:TypeLandinrichtingsrente/OnroerendeZaak:eindjaar"/>
            </landinrichtingsrente_jaar>
            <aard_cultuur_onbebouwd>
                <!-- TODO Er kunnen meerdere culturen onbebouwd bij een onbebouwde onroerende zaak voorkomen. -->
                <xsl:value-of select="$oz/OnroerendeZaak:aardCultuurOnbebouwd/Typen:waarde"/>
            </aard_cultuur_onbebouwd>
            <aard_cultuur_bebouwd>
                <!-- TODO  Er kunnen meerdere culturen bebouwd bij een onbebouwde onroerende zaak voorkomen. -->
                <xsl:value-of
                        select="$oz/OnroerendeZaak:aardCultuurBebouwd/Typen:waarde"/>
            </aard_cultuur_bebouwd>
            <koopsom_bedrag>
                <xsl:value-of
                        select="$oz/OnroerendeZaak:koopsom/OnroerendeZaak:TypeKoopsom/OnroerendeZaak:bedrag/Typen:som"/>
            </koopsom_bedrag>
            <koopsom_koopjaar>
                <xsl:value-of select="$oz/OnroerendeZaak:koopsom/OnroerendeZaak:TypeKoopsom/OnroerendeZaak:koopjaar"/>
            </koopsom_koopjaar>
            <koopsom_indicatiemeerobjecten>
                <xsl:value-of
                        select="$oz/OnroerendeZaak:koopsom/OnroerendeZaak:TypeKoopsom/OnroerendeZaak:indicatieMeerObjecten"/>
            </koopsom_indicatiemeerobjecten>
            <toelichtingbewaarder>
                <xsl:value-of select="$oz/OnroerendeZaak:toelichtingBewaarder"/>
            </toelichtingbewaarder>
            <tijdstipontstaanobject>
                <xsl:if test="not($oz/OnroerendeZaak:tijdstipOntstaanObject/@xsi:nil)">
                    <xsl:value-of select="$oz/OnroerendeZaak:tijdstipOntstaanObject/Typen:datum"/>
                    <xsl:if test="not($oz/OnroerendeZaak:tijdstipOntstaanObject/Typen:tijd/@xsi:nil='true')">
                        <xsl:text>T</xsl:text>
                        <xsl:value-of select="$oz/OnroerendeZaak:tijdstipOntstaanObject/Typen:tijd"/>
                    </xsl:if>
                </xsl:if>
            </tijdstipontstaanobject>
            <oudstdigitaalbekend>
                <xsl:if test="not($oz/OnroerendeZaak:oudstDigitaalBekend/@xsi:nil)">
                    <xsl:value-of select="$oz/OnroerendeZaak:oudstDigitaalBekend/Typen:datum"/>
                    <xsl:if test="not($oz/OnroerendeZaak:oudstDigitaalBekend/Typen:tijd/@xsi:nil='true')">
                        <xsl:text>T</xsl:text>
                        <xsl:value-of select="$oz/OnroerendeZaak:oudstDigitaalBekend/Typen:tijd"/>
                    </xsl:if>
                </xsl:if>
            </oudstdigitaalbekend>
            <ontstaanuit><!-- TODO deze ws kan weg omdat de relatie andersom is --></ontstaanuit>
        </onroerendezaak>

        <xsl:for-each select="OnroerendeZaak:ontstaanUitOZ/OnroerendeZaak:OnroerendeZaakFiliatie">
            <onroerendezaakfiliatie column-dat-beg-geldh="begingeldigheid">
                <aard>
                    <xsl:value-of select="OnroerendeZaak:aard/Typen:waarde"/>
                </aard>
                <betreft>
                    <xsl:value-of select="$ozId"/>
                </betreft>
                <begingeldigheid>
                    <xsl:value-of select="$toestandsdatum"/>
                </begingeldigheid>
            </onroerendezaakfiliatie>
        </xsl:for-each>

    </xsl:template>

    <!--
        vult recht.
    -->
    <xsl:template name="recht">
        <xsl:param name="recht"/>
        <!--
        TODO:
             - relatie Recht:Erfpachtcanon/Recht:betreft/Recht-ref:ZakelijkRechtRef
             - relatie Recht:Tenaamstelling/Recht:van/Recht-ref:ZakelijkRechtRef
             - relatie Recht:Tenaamstelling/Recht:tenNameVan/Persoon-ref:NietNatuurlijkPersoonRef
             - relatie Recht:Tenaamstelling/Recht:tenNameVan/Persoon-ref:NatuurlijkPersoonRef
             - meerdere stukdeel referenties Recht:isGebaseerdOp/Stuk-ref:StukdeelRef waarbij er een set stukdelen is
             - zakelijke rechten die naar elkaar verwijzen staan mogelijk niet in de juiste volgorde in een bericht
               waardoor de insert mislukt met een constraint violation (mn "isbelastmet" voor NL.IMKAD.KadastraalObject:53850184110001)
        -->
        <recht column-dat-beg-geldh="begingeldigheid" column-datum-einde-geldh="einddatumrecht">
            <identificatie>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="$recht/Recht:identificatie"/>
                </xsl:call-template>
            </identificatie>
            <aard>
                <xsl:value-of select="$recht/Recht:aard/Typen:waarde"/>
            </aard>
            <toelichtingbewaarder>
                <xsl:value-of select="$recht/Recht:toelichtingBewaarder"/>
            </toelichtingbewaarder>
            <isbelastmet>
                <!-- TODO: dit kunnen er meer dan 1 zijn -->
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="$recht/Recht:isBelastMet/Recht-ref:ZakelijkRechtRef"/>
                </xsl:call-template>
            </isbelastmet>
            <isgebaseerdop>
                <xsl:call-template name="domein_identificatie">
                    <!-- TODO Er kunnen meer dan 1 stukdelen zijn (xsd zegt max 2)
                              bijv. NL.IMKAD.KadastraalObject.53730012470000             -->
                    <xsl:with-param name="id" select="$recht/Recht:isGebaseerdOp/Stuk-ref:StukdeelRef"/>
                </xsl:call-template>
            </isgebaseerdop>
            <rustop>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="$recht/Recht:rustOp/OnroerendeZaak-ref:PerceelRef |
                                                      $recht/Recht:rustOp/OnroerendeZaak-ref:AppartementsrechtRef"/>
                </xsl:call-template>
            </rustop>
            <isontstaanuit>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="$recht/Recht:isOntstaanUit/Recht-ref:HoofdsplitsingRef |
                                                      $recht/Recht:isOntstaanUit/Recht-ref:OndersplitsingRef |
                                                      $recht/Recht:isOntstaanUit/Recht-ref:SpiegelsplitsingAfkoopErfpachtRef |
                                                      $recht/Recht:isOntstaanUit/Recht-ref:SpiegelsplitsingOndersplitsingRef"/>
                </xsl:call-template>
            </isontstaanuit>
            <isbetrokkenbij>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="$recht/Recht:isOntstaanUit/Recht-ref:HoofdsplitsingRef |
                                                      $recht/Recht:isOntstaanUit/Recht-ref:OndersplitsingRef |
                                                      $recht/Recht:isOntstaanUit/Recht-ref:SpiegelsplitsingAfkoopErfpachtRef |
                                                      $recht/Recht:isOntstaanUit/Recht-ref:SpiegelsplitsingOndersplitsingRef"/>
                </xsl:call-template>
            </isbetrokkenbij>
            <isbestemdtot>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="$recht/Recht:isBestemdTot/Recht-ref:MandeligheidRef"/>
                </xsl:call-template>
            </isbestemdtot>
            <isbeperkttot>
                <!-- TODO: dit kunnen er meer dan 1 zijn -->
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="$recht/Recht:isBeperktTot/Recht-ref:TenaamstellingRef"/>
                </xsl:call-template>
            </isbeperkttot>
            <soort>
                <xsl:value-of select="$recht/Recht:soort/Typen:waarde"/>
            </soort>
            <jaarlijksbedrag>
                <xsl:value-of select="$recht/Recht:jaarlijksBedrag/Typen:som"/>
            </jaarlijksbedrag>
            <jaarlijksbedragbetreftmeerdere_oz>
                <xsl:value-of select="$recht/Recht:betreftMeerOnroerendeZaken"/>
            </jaarlijksbedragbetreftmeerdere_oz>
            <einddatumafkoop>
                <xsl:value-of select="$recht/Recht:einddatumAfkoop"/>
            </einddatumafkoop>
            <indicatieoudeonroerendezaakbetrokken>
                <xsl:value-of select="$recht/Recht:indicatieOudeOnroerendeZaakBetrokken"/>
            </indicatieoudeonroerendezaakbetrokken>
            <heefthoofdzaak>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="$recht/Recht:heeftHoofdzaak/OnroerendeZaak-ref:PerceelRef |
                                                      $recht/Recht:heeftHoofdzaak/OnroerendeZaak-ref:AppartementsrechtRef"/>
                </xsl:call-template>
            </heefthoofdzaak>
            <heeftverenigingvaneigenaren>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id"
                                    select="$recht/Recht:heeftVerenigingVanEigenaren/Persoon-ref:NietNatuurlijkPersoonRef"/>
                </xsl:call-template>
            </heeftverenigingvaneigenaren>
            <aandeel_teller>
                <xsl:value-of select="$recht/Recht:aandeel/Recht:teller"/>
            </aandeel_teller>
            <aandeel_noemer>
                <xsl:value-of select="$recht/Recht:aandeel/Recht:noemer"/>
            </aandeel_noemer>
            <burgerlijkestaattentijdevanverkrijging>
                <xsl:value-of select="$recht/Recht:burgerlijkeStaatTenTijdeVanVerkrijging/Typen:waarde"/>
            </burgerlijkestaattentijdevanverkrijging>
            <verkregennamenssamenwerkingsverband>
                <xsl:value-of select="$recht/Recht:verkregenNamensSamenwerkingsverband/Typen:waarde"/>
            </verkregennamenssamenwerkingsverband>
            <betrokkenpartner>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="$recht/Recht:betrokkenPartner/Persoon-ref:NatuurlijkPersoonRef"/>
                </xsl:call-template>
            </betrokkenpartner>
            <geldtvoor>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id"
                                    select="$recht/Recht:geldtVoor/Recht-ref:GezamenlijkAandeelRef"/>
                </xsl:call-template>
            </geldtvoor>
            <betrokkensamenwerkingsverband>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id"
                                    select="$recht/Recht:betrokkenSamenwerkingsverband/Persoon-ref:NietNatuurlijkPersoonRef"/>
                </xsl:call-template>
            </betrokkensamenwerkingsverband>
            <betrokkengorzenenaanwassen>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id"
                                    select="$recht/Recht:betrokkenGorzenEnAanwassen/Persoon-ref:NietNatuurlijkPersoonRef"/>
                </xsl:call-template>
            </betrokkengorzenenaanwassen>
            <tennamevan>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id"
                                    select="$recht/Recht:tenNameVan/Persoon-ref:NatuurlijkPersoonRef |
                                            $recht/Recht:tenNameVan/Persoon-ref:NietNatuurlijkPersoonRef"/>
                </xsl:call-template>
            </tennamevan>
            <omschrijving>
                <xsl:value-of select="$recht/Recht:omschrijving"/>
            </omschrijving>
            <einddatumrecht alleen-archief="true">
                <!-- TODO check alleen-archief="true"       -->
                <xsl:value-of select="$recht/Recht:einddatumRecht"/>
            </einddatumrecht>
            <einddatum>
                <!-- Einddatum is de datum waarop de geldigheid van de aantekening eindigt -->
                <xsl:value-of select="$recht/Recht:einddatum"/>
            </einddatum>
            <betreftgedeeltevanperceel>
                <xsl:value-of select="$recht/Recht:betreftGedeelteVanPerceel"/>
            </betreftgedeeltevanperceel>
            <aantekeningrecht>
                <!-- TODO : dit kunnen er meer dan 1 ref naar tenaamstellingen zijn -->
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="$recht/Recht:aantekeningRecht/Recht-ref:TenaamstellingRef"/>
                </xsl:call-template>
            </aantekeningrecht>
            <aantekeningkadastraalobject>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id"
                                    select="$recht/Recht:aantekeningKadastraalObject/OnroerendeZaak-ref:PerceelRef |
                                            $recht/Recht:aantekeningKadastraalObject/OnroerendeZaak-ref:AppartementsrechtRef"/>
                </xsl:call-template>
            </aantekeningkadastraalobject>
            <betrokkenpersoon>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id"
                                    select="$recht/Recht:betrokkenPersoon/Persoon-ref:NatuurlijkPersoonRef |
                                            $recht/Recht:betrokkenPersoon/Persoon-ref:NietNatuurlijkPersoonRef"/>
                </xsl:call-template>
            </betrokkenpersoon>
            <begingeldigheid>
                <xsl:value-of select="$toestandsdatum"/>
            </begingeldigheid>
        </recht>
    </xsl:template>

    <!--
        vult de tabel persoon (superklasse).
    -->
    <xsl:template name="persoon">
        <xsl:param name="persoon"/>
        <xsl:param name="clazz"/>

        <persoon>
            <identificatie>
                <xsl:call-template name="domein_identificatie">
                    <xsl:with-param name="id" select="$persoon/Persoon:identificatie"/>
                </xsl:call-template>
            </identificatie>
            <beschikkingsbevoegdheid>
                <xsl:value-of select="$persoon/Persoon:beschikkingsbevoegdheid/Typen:waarde"/>
            </beschikkingsbevoegdheid>
            <indicatieniettoonbarediakriet>
                <xsl:value-of select="$persoon/Persoon:indicatieNietToonbareDiakriet"/>
            </indicatieniettoonbarediakriet>
            <postlocatie>
                <xsl:if test="$persoon/Persoon:postlocatie/Adres-ref:ObjectlocatieBinnenlandRef">
                    <xsl:call-template name="domein_identificatie">
                        <xsl:with-param name="id"
                                        select="$persoon/Persoon:postlocatie/Adres-ref:ObjectlocatieBinnenlandRef |
                                                $persoon/Persoon:postlocatie/Adres-ref:ObjectlocatieBuitenlandRef |
                                                $persoon/Persoon:postlocatie/Adres-ref:PostbusLocatieRef"/>
                    </xsl:call-template>
                </xsl:if>
            </postlocatie>
            <woonlocatie>
                <xsl:if test="$persoon/Persoon:woonlocatie/Adres-ref:ObjectlocatieBinnenlandRef">
                    <xsl:call-template name="domein_identificatie">
                        <xsl:with-param name="id"
                                        select="$persoon/Persoon:woonlocatie/Adres-ref:ObjectlocatieBinnenlandRef |
                                                $persoon/Persoon:woonlocatie/Adres-ref:ObjectlocatieBuitenlandRef"/>
                    </xsl:call-template>
                </xsl:if>
            </woonlocatie>
            <soort>
                <xsl:value-of select="$clazz"/>
            </soort>
        </persoon>
    </xsl:template>

    <!-- maak een domein identificatie aan -->
    <xsl:template name="domein_identificatie">
        <xsl:param name="id"/>
        <xsl:variable name="nameSpace" select="$id/@domein"/>

        <xsl:if test="$id">
            <xsl:value-of select="$nameSpace"/>
            <xsl:if test="'' != $nameSpace">
                <!-- BAG id's hebben geen domein/NEN3610 namespace als bijv. NL.IMBAG.Openbareruimte -->
                <xsl:text>:</xsl:text>
            </xsl:if>
            <xsl:value-of select="$id"/>
        </xsl:if>
    </xsl:template>

    <!-- converteer datum: jjjj-mm-dd -> jjjjmmdd -->
    <xsl:template name="numeric-date">
        <xsl:value-of select="concat(substring(.,1,4),substring(.,6,2),substring(.,9,2))"/>
    </xsl:template>
</xsl:stylesheet>
