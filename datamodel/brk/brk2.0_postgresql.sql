CREATE SCHEMA IF NOT EXISTS brk;
SET SCHEMA 'brk';
SET search_path = brk,public;

-- alle verschillende soorten stukken:
--  NL.IMKAD.Kadasterstuk
--  NL.IMKAD.TIAStuk
CREATE TABLE stuk
(
    -- Identificatie is een door het Kadaster toegekend landelijk uniek nummer aan een object binnen de kadastrale registratie.
    identificatie         VARCHAR(255) NOT NULL PRIMARY KEY,
    toelichtingbewaarder  VARCHAR(4000),
    -- Ambtelijk correctie gegeven identificatie
    portefeuillenummer    VARCHAR(16),
    -- De aanduiding van het deel binnen de reeks in het register, waarin het stuk is geregistreerd.
    -- Het Deel is een nummer maar vroeger zijn ook letters gebruik, daarom heeft het Deel het domein type Tekst.
    -- Er worden geen voorloopnullen gebruikt.
    deel                  VARCHAR(5),
    -- het volgnummer van het stuk binnen het deel van het register.
    -- In de landelijke stukkenregistratie is dit nummer niet uniek identificerend binnen een register, omdat tijdig
    -- ingediende verbeteringen hetzelfde volgnummer krijgen als het oorspronkelijke stuk.
    -- Er worden geen voorloopnullen gebruikt.
    nummer                VARCHAR(5),
    -- Verwijzing naar de oorspronkelijke (mogelijk tussentijds vervallen) Kadastervestiging waar het stuk oorspronkelijk is ingeschreven.
    -- https://developer.kadaster.nl/schemas/waardelijsten/Reekscode/
    reeks                 VARCHAR(19),
    -- De registercode is de aanduiding van het het register waarin het stuk is ingeschreven.
    -- Dit kan zijn het register hypotheken 3 voor hypotheken en beslagen en hypotheken 4 voor alle andere stukken.
    -- https://developer.kadaster.nl/schemas/waardelijsten/Registercode/
    registercode          VARCHAR(5),
    -- SoortRegister is een aanduiding van de hoofdcategorie van een ter inschrijving aangeboden stuk.
    -- Bijvoorbeeld Hypotheekakten, Transportakten.De mogelijke waarden zijn opgenomen in een waardelijst
    -- https://developer.kadaster.nl/schemas/waardelijsten/SoortRegister/
    soortregister         VARCHAR(16),
    -- TijdstipAanbieding is het tijdstip waarop een ter inschrijving aangeboden stuk is ontvangen met in achtneming van de
    -- openingstijden en -dagen van het Kadaster.
    -- Als tijdstip van inschrijving geldt het tijdstip van aanbieding van de voor de inschrijving vereiste stukken.
    tijdstipaanbieding    TIMESTAMP,
    -- Tijdstip ondertekening is het tijdstip waarop een ter inschrijving aangeboden stuk is ondertekend door de opsteller van het stuk.
    -- Alleen wanneer het TIA stuk een PubliekrechtelijkeBeperking betreft dan is dit tijdstip niet verplicht.
    tijdstipondertekening TIMESTAMP,
    -- Tekening ingeschreven geeft aan dat er is sprake van een appartementstekening (splitsingstekening van appartementen) als bijlage bij het stuk.
    tekeningingeschreven  BOOLEAN
);

CREATE TABLE stukdeel
(
    -- Identificatie is een door het Kadaster toegekend landelijk uniek nummer aan een object binnen de kadastrale registratie.
    identificatie               VARCHAR(255) NOT NULL PRIMARY KEY,
    -- Aanduiding aard stukdeel is een aanduiding voor de aard van een rechtsfeit.
    -- De mogelijke waarden zijn vermeld in een waardenlijst.De mogelijke waarden zijn vermeld in een waardenlijst
    -- https://developer.kadaster.nl/schemas/waardelijsten/AardStukdeel/
    aard                        VARCHAR(255),
    -- Bedrag transactiesom levering is het in een ter inschrijving aangeboden stuk vermelde bedrag, waarvoor 1 of meer
    -- onroerende zaken zijn verkregen.In een stuk kunnen verschillende transacties zijn vermeld, met verschillende transactiesom.
    -- Per stukdeel (transactie) is de transactiesom weergegeven.
    bedragtransactiesomlevering DECIMAL(20, 2),
    valutatransactiesomlevering VARCHAR(42),
    -- DatumKenbaarheid is de datum waarop een ter inschrijving aangboden publiekrechtelijke beperking besluit bekend is gemaakt of is geworden.
    datumkenbaarheidpb          DATE,
    -- referentie naar stuk
    deelvan                     VARCHAR(255) NOT NULL REFERENCES stuk (identificatie)
);


-- Een onroerende zaak is de grond, de niet gewonnen delfstoffen, de met de grond verenigde beplantingen,
-- alsmede de gebouwen en werken die duurzaam met de grond zijn verenigd, hetzij rechtstreeks,
-- hetzij door vereniging met andere gebouwen of werken.
CREATE TABLE onroerendezaak
(
    -- De Kadaster identificatie is een door het Kadaster toegekend landelijk uniek nummer aan dit object binnen de kadastrale registratie.
    -- NL.IMKAD.KadastraalObject
    identificatie                 VARCHAR(255) NOT NULL PRIMARY KEY,
    -- metadata tbv archivering
    begingeldigheid               DATE         NOT NULL,
    eindegeldigheid               DATE,
    -- Kadastrale aanduiding is de unieke aanduiding van een onroerende zaak, die door het kadaster wordt vastgesteld.
    -- Kadastrale aanduiding is de unieke aanduiding van een onroerende zaak, die door het kadaster wordt vastgesteld.
    -- Percelen worden kadastraal aangeduid door vermelding van achtereenvolgens de kadastrale gemeente en sectie,
    -- waarin de percelen en gedeelten van percelen zijn gelegen waarvan het grondgebied tot die zaak behoort,
    -- alsmede de nummers van die percelen. Voor een onroerende zaak die zich krachtens een opstalrecht op,
    -- in of boven de grond van een ander bevindt, geldt dezelfde kadastrale aanduiding als van de onroerende zaak die met dat
    -- opstalrecht is bezwaard. Dit is van overeenkomstige toepassing op een onroerende zaak die zich op,
    -- in of boven de grond van een ander bevindt krachtens een recht als bedoeld in het voor 1 januari 1992 geldende artikel 5,
    -- derde lid, onder b, laatste zinsnede, van de Belemmeringenwet Privaatrecht.
    -- Appartementsrechten worden kadastraal aangeduid door de vermelding van achtereenvolgens de kadastrale gemeente en sectie,
    -- waarin de in de splitsing betrokken percelen zijn gelegen, de complexaanduiding en de appartementsindex
    -- (Een oplopend volgnummer van "0001" tot "9999").
    -- De complexaanduiding bestaat uit het voor de in de splitsing betrokken percelen vastgestelde complexnummer, gevolgd door de hoofdletter A.
    -- Onze Minister stelt regelen vast omtrent de vaststelling van het complexnummer.
    -- Er zijn 1111 kadastrale gemeentenamen. Elke kadastrale gemeentenaam is uniek.
    -- https://developer.kadaster.nl/schemas/waardelijsten/KadastraleGemeente/
    akrkadastralegemeentecode     DECIMAL(4, 0),
    akrkadastralegemeente         VARCHAR(5),
    -- De kadastrale gemeente, deel van de kadastrale aanduiding van de onroerende zaak.
    kadastralegemeentecode        DECIMAL(4, 0),
    kadastralegemeente            VARCHAR(30),
    -- Sectie is een onderverdeling van de kadastrale gemeente, bedoeld om het werk van de meetdienst en om de kadastrale kaarten overzichtelijk te houden.
    -- Per kadastrale gemeente zijn er max. 26x26 secties .
    -- Elke sectie heeft een of twee letters (bijv. "A", 'B', ....'AA', 'AB', ....'ZZ' .
    -- Alleen de sectieletter "J" werd niet gebruikt om verwarring (handgeschreven) te voorkomen met "I"
    sectie                        VARCHAR(2),
    -- Het perceelnummer dat een geheel perceel of een complex uniek identificeert binnen de sectie.
    -- Per kadastrale gemeente en per sectie heeft een perceel een perceelnummer oplopend door de jaren heen van "00001" tot max "99999"
    perceelnummer                 DECIMAL(5, 0),
    -- Nummer dat het kadastraal object uniek identificeert als een appartementsrecht binnen het complex.
    -- Appartementsrechten worden kadastraal aangeduid door de vermelding van achtereenvolgens:
    -- * de kadastrale gemeente en
    -- * sectie, waarin de in de appartementsrechtssplitsing betrokken percelen zijn gelegen,
    -- * het nummer gevolgd door
    -- * het volgnummer van het appartementsrecht (Een oplopend volgnummer van "0001" tot "9999").
    appartementsrechtvolgnummer   DECIMAL(4, 0),
    -- Landinrichtingsrente is het bedrag waarmee de Onroerende zaak is belast in het kader van de landinrichtingswet.
    -- Opgebouwd uit de onderdelen Aanduiding Landinrichtingsrente, Bedrag Landinrichtingsrente, en Eindjaar Landinrichtingsrente.
    --
    -- Als twee percelen met elk  landinrichtingsrente-gegevens met verschillende eindjaren verenigd worden,
    -- worden beide landinrichtingsrente-gegevensgroepen bij het nieuwe perceel opgenomen.
    --
    -- Het bedrag waarmee de Onroerende zaak is belast in het kader van de landinrichtingswet.
    -- 	  Let op: Het bedrag is in AKR in euro-centen opgenomen!
    landinrichtingsrente_bedrag   DECIMAL(20, 2),
    landinrichtingsrente_valuta   VARCHAR(42),
    -- 	Het laatste kalenderjaar waarin de rente in het kader van landinrichtingswet nog verschuldigd is.
    landinrichtingsrente_jaar     INTEGER,
    -- In principe moet elke onbebouwde onroerende zaak minimaal 1 (één) beschrijving hebben van de cultuur onbebouwd (bijv. grasland).
    -- Er kunnen meerdere culturen onbebouwd bij een onbebouwde onroerende zaak voorkomen.
    -- https://developer.kadaster.nl/schemas/waardelijsten/CultuurcodeOnbebouwd
    aard_cultuur_onbebouwd        VARCHAR(65),
    -- In principe moet elke bebouwde onroerende zaak minimaal 1 (één) beschrijving hebben van de cultuur bebouwd (bijv. wonen).
    -- Er kunnen meerdere culturen bebouwd bij een onbebouwde onroerende zaak voorkomen.
    -- https://developer.kadaster.nl/schemas/waardelijsten/CultuurcodeBebouwd
    aard_cultuur_bebouwd          VARCHAR(65),
    -- De in akten vermelde koopsommen die volgens de gehanteerde verwerkingsinstructie moeten worden geregistreerd.
    -- koopsom  bestaat uit:
    -- Het in een ter inschrijving aangeboden stuk vermelde bedrag, waarvoor 1 of meer onroerende zaken zijn verkregen.
    -- Koopsom is altijd een positief bedrag. Dit is een bedrag (omgerekend naar) euro's.
    koopsom_bedrag                DECIMAL(20, 2),
    koopsom_valuta                VARCHAR(42),
    -- Het jaar waarin het belangrijkste recht van het kadastraal object is verkregen.
    koopsom_koopjaar              INTEGER,
    -- Geeft aan of de koopsom betrekking heeft op meer dan 1 kadastraal object.
    koopsom_indicatiemeerobjecten BOOLEAN,
    -- Toelichting bewaarder is een toelichtende tekst van de bewaarder bij het kadastraal object.
    -- Een Toelichting Bewaarder wordt opgevoerd wanneer een toelichting bij gegevens in de registratie noodzakelijk is.
    -- Slechts in specifieke gevallen zal de bewaarder een toelichtende tekst bij een onroerende zaak willen toevoegen. In totaal zullen er dit enige duizenden zijn.
    toelichtingbewaarder          VARCHAR(4000),
    --  Tijdstip onstaan object is datum en (als beschikbaar) het tijdstip waarop een onroerende zaak is ontstaan.
    --  Percelen en Appartementsrechten worden bij het Kadaster sinds 1832 geregistreerd.
    --  Het tijdstipOntstaanObject is niet sindsdien digitaal beschikbaar in de registratie, maar het is wel door middel van onderzoek te achterhalen.
    --  Het kan dus zijn dat tijdstipOntstaanObject op een bepaald (later) moment toch bekend wordt.
    --  Bij recentelijk ontstane onroerende zaken is dit gegeven wel altijd bekend.
    --  Voor percelen die ontstaan zijn na yyyy is altijd een tijdstip bekend, en voor appartementsrechten sinds yyyy.
    -- bestaat uit datum + (optioneel) tijdstip
    tijdstipontstaanobject        TIMESTAMP,
    -- OudstDigitaalBekend is de datum waarop het object voor het eerst vanuit een digitale Kadastrale registratie beschikbaar was is.
    -- Dit is een vaststaand gegeven en zal niet veranderen als gevolg van het digitaliseren van analoge registraties.
    -- Dit gegeven is te zien als het technisch tijdstip ontstaan van de eerste versie van een object.
    oudstdigitaalbekend           TIMESTAMP
);

CREATE TABLE onroerendezaak_archief
(
    identificatie                 VARCHAR(255) NOT NULL,
    begingeldigheid               DATE         NOT NULL,
    eindegeldigheid               DATE         NOT NULL,
    akrkadastralegemeentecode     DECIMAL(4, 0),
    akrkadastralegemeente         VARCHAR(5),
    kadastralegemeentecode        DECIMAL(4, 0),
    kadastralegemeente            VARCHAR(30),
    sectie                        VARCHAR(2),
    perceelnummer                 DECIMAL(5, 0),
    appartementsrechtvolgnummer   DECIMAL(4, 0),
    landinrichtingsrente_bedrag   DECIMAL(20, 2),
    landinrichtingsrente_valuta   VARCHAR(42),
    landinrichtingsrente_jaar     INTEGER,
    aard_cultuur_onbebouwd        VARCHAR(65),
    aard_cultuur_bebouwd          VARCHAR(65),
    koopsom_bedrag                DECIMAL(20, 2),
    koopsom_valuta                VARCHAR(42),
    koopsom_koopjaar              INTEGER,
    koopsom_indicatiemeerobjecten BOOLEAN,
    toelichtingbewaarder          VARCHAR(4000),
    tijdstipontstaanobject        TIMESTAMP,
    oudstdigitaalbekend           TIMESTAMP,
    ontstaanuit                   VARCHAR,
    PRIMARY KEY (identificatie, begingeldigheid)
);

CREATE TABLE adres
(
    identificatie        VARCHAR(255) PRIMARY KEY NOT NULL,
    -- adres binnenland
    huisnummer           NUMERIC(5, 0),
    huisletter           VARCHAR(1),
    huisnummertoevoeging VARCHAR(4),
    postbusnummer        NUMERIC(6, 0),
    postcode             VARCHAR(6),
    openbareruimtenaam   VARCHAR(80),
    woonplaatsnaam       VARCHAR(80),
    -- BAG NUM id
    nummeraanduiding     VARCHAR(16),
    -- BAG ADR id: en verblijfsobject , standplaats of ligplaats
    adresseerbaarobject  VARCHAR(16),
    --BAG VBO id
    verblijfsobject      VARCHAR(16),
    -- BAG STA id
    standplaats          VARCHAR(16),
    --BAG LIG id
    ligplaats            VARCHAR(16),
    hoofdadres           VARCHAR(16),
    nevenadres           VARCHAR(16),
    -- adres buitenland
    buitenlandadres      VARCHAR(200),
    buitenlandwoonplaats VARCHAR(200),
    buitenlandregio      VARCHAR(150),
    -- https://developer.kadaster.nl/schemas/waardelijsten/BRPLand/
    land                 VARCHAR(40)
);

CREATE TABLE objectlocatie
(
    heeft           VARCHAR REFERENCES onroerendezaak (identificatie),
    betreft         VARCHAR REFERENCES adres (identificatie),
    -- https://developer.kadaster.nl/schemas/waardelijsten/Koppelingswijze/
    koppelingswijze VARCHAR(29),
    PRIMARY KEY (heeft, betreft)
);

CREATE TABLE objectlocatie_archief
(
    heeft           VARCHAR(255) NOT NULL ,
    betreft         VARCHAR(255) NOT NULL,
    koppelingswijze VARCHAR(29),
    begingeldigheid  DATE         NOT NULL,
    PRIMARY KEY (heeft, betreft, begingeldigheid)
);

-- Een persoon is een natuurlijk persoon of een niet-natuurlijk persoon.
-- NL.IMKAD.Persoon
CREATE TABLE persoon
(
    identificatie                 VARCHAR(255) NOT NULL PRIMARY KEY,
    -- Beschikkingsbevoegdheid is een aanduiding van de beschikkingsbevoegdheid van een persoon.
    -- https://developer.kadaster.nl/schemas/waardelijsten/Beschikkingsbevoegdheid/
    beschikkingsbevoegdheid       VARCHAR(33),
    -- Indicatie niet toonbare diakriet geeft aan of de naam van een persoon diakrieten bevat die niet getoond kunnen worden.
    -- Deze aanduiding was in het verleden nodig omdat de systemen niet alle diakrieten konden vastleggen en weergeven.
    -- Dit kan spelen bij personen die voor 2019 zijn geregistreerd in de BRK.
    indicatieniettoonbarediakriet BOOLEAN,
    -- post adres referentie
    postlocatie                   VARCHAR REFERENCES adres (identificatie),
    -- object locatie referentie
    woonlocatie                   VARCHAR REFERENCES adres (identificatie),
    -- tbv. DATAMODEL: om opzoeken van de bijhorende persoonsgegevens makkelijk te maken
    soort                         VARCHAR(21) CHECK ( soort IN ('natuurlijkpersoon', 'nietnatuurlijkpersoon') )
);

CREATE TABLE natuurlijkpersoon
(
    identificatie                        VARCHAR(255) NOT NULL PRIMARY KEY,
    -- Indicatie Overleden is een indicatie of de persoon al dan niet overleden is.
    -- Deze indicatie is enkel van belang als de gegevens uit de BRP niet beschikbaar zijn
    -- De datum van overlijden is soms niet bekend, maar wel dat iemand overleden is.
    indicatieoverleden                   BOOLEAN,
    -- Indicatie Afscherming Persoonsgegevens is een indicatie om aan te geven dat de gegevens van een persoon zijn afgeschermd.Op grond van artikel 37a Kadasterbesluit heeft het Kadaster de mogelijkheid op verzoek van een persoon gedurende een periode van vijf jaar geen gegevens over deze persoon te verstrekken. Alleen personen die binnen het stelsel Bewaken en Beveiligen vallen komen hiervoor in aanmerking.
    -- Gegevens worden wel verstrekt aan notarissen, deurwaarders en bestuursorganen indien zij deze gegevens nodig hebben voor de uitvoering van hun wettelijke taak.
    indicatieafschermingpersoonsgegevens BOOLEAN,
    -- Het BSN is het burgerservicenummer van de persoon.
    bsn                                  VARCHAR,
    -- adellijkeTitelOfPredikaat is de adellijke titel of het predikaat dat behoort tot de naam van de persoon (bij adellijke titel geslachtsnaam, bij predikaat voornaam).
    -- https://developer.kadaster.nl/schemas/waardelijsten/AdellijkeTitelOfPredicaat/
    adellijketitelofpredicaat            VARCHAR(10),
    -- aanduidingNaamgebruik is een aanduiding voor de wijze van aanschrijving van de ingeschrevene.
    -- https://developer.kadaster.nl/schemas/waardelijsten/BRPAanduidingNaamgebruik/
    -- https://developer.kadaster.nl/schemas/waardelijsten/AanduidingNaamgebruik/
    aanduidingnaamgebruik                VARCHAR(72),
    -- landWaarnaarVertrokken is het land dat de ingeschrevene opgeeft bij vertrek naar het buitenland. Als we van BRP een mutatiemelding krijgen met enkel een land dat wordt dat geregistreerd als landWaarnaarVertrokken. Worden ook andere adresgegevens ontvangen, minimaal woonplaats, dan wordt dit als buitenlandsadres geregistreerd.
    landwaarnaarvertrokken               VARCHAR(40),
    geslachtsnaam                        VARCHAR(200),
    voornamen                            VARCHAR(200),
    voorvoegselsgeslachtsnaam            VARCHAR(10),
    -- https://developer.kadaster.nl/schemas/waardelijsten/AanduidingGeslacht/ gebruikt 1/2/3
    -- https://developer.kadaster.nl/schemas/waardelijsten/BRPAanduidingGeslacht/ (gebruikt M/V/O)
    geslacht                             VARCHAR(8),
    -- De geboortedatum is de datum waarop de persoon is geboren: OnvolledigeDatum
    geboortedatum                        VARCHAR(10),
    -- De geboorteplaats is de plaats of een plaatsbepaling, die aangeeft waar de persoon is geboren
    geboorteplaats                       VARCHAR(80),
    -- Het geboorteland is de naam, die het land aangeeft waar de persoon is geboren.
    -- https://developer.kadaster.nl/schemas/waardelijsten/BRPLand/
    geboorteland                         VARCHAR(40),
    -- indicatieGeheim is een aanduiding die aangeeft dat gegevens van een persoon wel of niet verstrekt mogen worden.
    indicatiegeheim                      BOOLEAN,
    -- Datum overlijden is de datum waarop de persoon overleden is: OnvolledigeDatum
    datumoverlijden                      VARCHAR(10),
    partnergeslachtsnaam                 VARCHAR(200),
    partnervoornamen                     VARCHAR(200),
    partnervoorvoegselsgeslachtsnaam     VARCHAR(10)
);
-- Een rechtspersoon is een rechtssubject die zelfstandig drager is van rechten en met een natuurlijk persoon gelijk staat,
-- tenzij uit de wet het tegendeel voortvloeit.
-- De BRK bevat de rechtspersonen die rechthebbenden zijn m.b.t. een registergoed.
CREATE TABLE nietnatuurlijkpersoon
(
    identificatie   VARCHAR(255) NOT NULL PRIMARY KEY,
    statutairenaam  VARCHAR(200),
    -- Rechtsvorm is de rechtsvorm van de Rechtspersoon.
    -- https://developer.kadaster.nl/schemas/waardelijsten/Rechtsvorm/
    rechtsvorm      VARCHAR(52),
    --Statutaire zetel is de plaats waar een Rechtspersoon volgens de statuten gevestigd is.
    statutairezetel VARCHAR(40),
    -- Het RSIN is het Rechtspersonen Samenwerkingsverbanden Informatie Nummer is een uniek nummer wat iedere rechtspersoon krijgt.
    -- Een RSIN is een uniek nummen dat wordt toegekend aan een rechtspersoon of samenwerkingsverband.
    -- Een eenmanszaak is geen rechtspersoon en heeft dus ook geen RSIN.
    rsin            VARCHAR(9),
    -- Het KvKNummer is een uniek identificerend administratienummer van een rechtspersoon zijnde een niet-natuurlijk persoon zoals
    -- door de Kamer van Koophandel
    -- Elke onderneming of maatschappelijke activiteit krijgt in het Handelsregister één KvK-nummer.
    -- Dit KvK-nummer bestaat altijd uit 8 cijfers.
    kvknummer       VARCHAR(8)
);

-- NL.IMKAD.PubliekrechtelijkeBeperking
-- omvat 1 of meer onroerendezaakbeperkingen
CREATE TABLE publiekrechtelijkebeperking
(
    identificatie    VARCHAR(255) PRIMARY KEY NOT NULL,
    -- De grondslag is een verwijzing naar de wet waar de publiekrechtelijke beperking op gebaseerd is.
    -- https://developer.kadaster.nl/schemas/waardelijsten/GrondslagBRK-PB/index.html
    grondslag        VARCHAR(255),
    datuminwerking   DATE,
    datumbeeindiging DATE,
    -- stukdeel ref
    isgebaseerdop    VARCHAR REFERENCES stukdeel (identificatie),
    -- nnp ref
    bevoegdgezag     VARCHAR REFERENCES nietnatuurlijkpersoon (identificatie)
);

CREATE TABLE onroerendezaakbeperking
(
    inonderzoek BOOLEAN,
    beperkt     VARCHAR REFERENCES onroerendezaak (identificatie),
    leidttot    VARCHAR REFERENCES publiekrechtelijkebeperking (identificatie),
    PRIMARY KEY (beperkt, leidttot)
);

CREATE TABLE onroerendezaakbeperking_archief
(
    inonderzoek     BOOLEAN,
    beperkt         VARCHAR(255) NOT NULL,
    leidttot        VARCHAR REFERENCES publiekrechtelijkebeperking (identificatie),
    begingeldigheid DATE         NOT NULL,
    PRIMARY KEY (beperkt, leidttot, begingeldigheid)
);

-- Een Onroerende zaak filiatie geeft de relatie aan tussen een nieuwe en een oude Onroerende zaak.
-- Het betreft hier de relatie tussen 2 percelen, 2 Appartementsrechten of 2 leidingnetwerken.
CREATE TABLE onroerendezaakfiliatie
(
    -- Een aanduiding voor de aard van de filiatie.
    -- Onroerende zaak filiatie wordt gebruikt om aan te geven hoe een onroerende zaak (historisch) tot stand gekomen is.
    -- Het geeft aan waarom het ene kadastrale object gerelateerd is aan het andere.
    -- https://developer.kadaster.nl/schemas/waardelijsten/AardFiliatie/
    aard            VARCHAR(65) NOT NULL,
    onroerendezaak  VARCHAR REFERENCES onroerendezaak (identificatie) ON DELETE CASCADE,
    -- betreft OZ relatie; referentie naar OZ/Perceel/AppRe
    betreft         VARCHAR(255),
    -- metadata tbv archivering
    begingeldigheid DATE        NOT NULL,
    PRIMARY KEY (aard, onroerendezaak, betreft)
);

CREATE TABLE onroerendezaakfiliatie_archief
(
    aard            VARCHAR(65)  NOT NULL,
    onroerendezaak  VARCHAR(255),
    betreft         VARCHAR(255) NOT NULL,
    begingeldigheid DATE         NOT NULL,
    PRIMARY KEY (aard, onroerendezaak, betreft, begingeldigheid)
);

-- In de BRK is een kadastraal perceel een specialisatie van een onroerende zaak.
-- Percelen worden cartografisch gerepresenteerd door een tweedimensionale vlakbegrenzing.
-- Tussen alle kadastrale percelen in Nederland geldt een topologische relatie (opdelende vlakstructuur), d.w.z. dat naburige perceelsvlakken naadloos moeten aansluiten en elkaar niet mogen overlappen.
CREATE TABLE perceel
(
    identificatie          VARCHAR(255)           NOT NULL PRIMARY KEY,
    -- Een perceel is een begrensd deel van het Nederlands grondgebied dat kadastraal geïdentificeerd is en met kadastrale grenzen begrensd is.
    -- Het gehele Nederlandse grondgebied is aaneengesloten kadastraal geïdentificeerd.
    -- Perceel is authentiek volgens de BRK voorzover het de attribuutsoorten kadastraleGrootte en KadatraleAanduiding betreft.
    begrenzing_perceel     GEOMETRY(MULTIPOLYGON, 28992) NOT NULL,
    -- De grootte van een perceel zoals vermeld in de kadastrale registratie. Het Kadaster bepaalt niet een excacte maar een indicatieve grootte.
    -- Grootte is alleen authentiek als soort groote de waarde "vastgesteld" heeft.
    -- De oppervlakgrootte wordt vastgelegd in vierkante meter.
    kadastralegrootte      DECIMAL(9, 1),
    -- De soortGrootte geeft aan of de grootte van het perceel voorlopig, administratief of definitief is vastgesteld.
    -- Een grens is voorlopig zolang de [Aanwijzing kadastrale grens](http://tax.kadaster.nl/id/begrip/Aanwijzing_kadastrale_grens) nog niet heeft plaatsgevonden.
    -- Met voorlopige kadastrale grenzen (VKG) worden gehele kadastrale percelen gevormd, voordat de definitieve grenzen in het terrein aan te wijzen zijn.
    -- Bij de splitsing ontstaan gehele percelen met:* voorlopige kadastrale grenzen;* een voorlopige kadastrale oppervlakte;* definitieve perceelnummers.definitieve grenzen in het terrein aangewezen zijn of nog niet aan te wijzen zijn.
    -- Bij de splitsing ontstaan gehele percelen met:* voorlopige kadastrale grenzen;* een voorlopige kadastrale oppervlakte;* definitieve perceelnummers.
    -- Een administratieve grens is een grens die door het Kadaster is ingetekend om een, in een akte geleverd gedeelte van een perceel, af te beelden op de kadastrale kaart.
    -- Een administratieve grens wordt definitief als de grens, na aanwijzing door belanghebbenden, door het Kadaster is gemeten.
    -- Op de kadastrale kaart is de kleur van een definitiefe grens zwart, van een voorlopige grens bruin en een administratief voorlopige grens blauw.
    -- https://developer.kadaster.nl/schemas/waardelijsten/SoortGrootte/
    soortgrootte           VARCHAR(100),
    -- Rotatie van het perceelnummer, in een hoek tussen de -90 +90 graden ten behoeve van afbeelding op de kaart.
    -- Perceelnummers worden bijvoorbeeld gekanteld om in een smal perceel te passen.
    perceelnummerrotatie   DECIMAL(3, 1),
    -- Verschuiving op de X as.
    perceelnummer_deltax   DECIMAL(20, 10),
    -- Verschuiving op de Y as.
    perceelnummer_deltay   DECIMAL(20, 10),
    --  Betreft de plaatsing van het perceelnummer bij verbeelding op een kaart, om deze op een overzichtelijke plek in de perceel begrenzing te plaatsen.
    plaatscoordinaten      GEOMETRY(POINT, 28992) NOT NULL,
    -- Meettarief verschuldigd is een indicatie voor het verschuldigd zijn van een meettarief bij overdracht van een perceel.
    -- Een meettarief is verschuldigd (indicator is true) als het een administratief gevormd perceel met voorlopige grenzen,
    -- of een perceel met voorlopige grenzen betreft dat nog nooit is overgedragen.
    meettariefverschuldigd BOOLEAN
    -- alleen archief
    -- begingeldigheid        DATE                          NOT NULL
);

CREATE TABLE perceel_archief
(
    identificatie          VARCHAR(255)                  NOT NULL,
    begingeldigheid        DATE                          NOT NULL,
    begrenzing_perceel     GEOMETRY(MULTIPOLYGON, 28992) NOT NULL,
    kadastralegrootte      DECIMAL(9, 1),
    soortgrootte           VARCHAR(100),
    perceelnummerrotatie   DECIMAL(3, 1),
    perceelnummer_deltax   DECIMAL(20, 10),
    perceelnummer_deltay   DECIMAL(20, 10),
    plaatscoordinaten      GEOMETRY(POINT, 28992)        NOT NULL,
    meettariefverschuldigd BOOLEAN,
    PRIMARY KEY (identificatie, begingeldigheid)
);

-- bevat alle "rechten"
--   NL.IMKAD.ZakelijkRecht
--   NL.IMKAD.Erfpachtcanon
--   NL.IMKAD.Mandeligheid
--   NL.IMKAD.AppartementsrechtSplitsing
--      Hoofdsplitsing
--      Ondersplitsing
--      SpiegelsplitsingAfkoopErfpacht
--      SpiegelsplitsingOndersplitsing
--   NL.IMKAD.Tenaamstelling
--   NL.IMKAD.GezamenlijkAandeel
--   NL.IMKAD.Aantekening
CREATE TABLE recht
(
    identificatie                          VARCHAR(255) NOT NULL PRIMARY KEY,
    -- Aard (zakelijk recht) is een aanduiding voor de aard van het recht. De waarden zijn opgenomen in een waardelijst.
    -- https://developer.kadaster.nl/schemas/waardelijsten/AardZakelijkRecht/
    -- of
    -- https://developer.kadaster.nl/schemas/waardelijsten/AardAantekening/
    aard                                   VARCHAR(255),
    toelichtingbewaarder                   VARCHAR(4000),
    -- Meerdere ‘is gebaseerd op’ bij een zakelijk recht is een valide situatie.
    -- Dit komt voor als het zakelijk recht eerst ontstaat onder opschortende voorwaarden (ontstaat dan nog niet in de BRK)
    -- en er later een stuk komt waarin deze opschortende voorwaarden in vervulling gaan.
    -- Bij het 2e stuk ontstaat dan het zakelijk recht in de BRK, maar beide stukken worden dan als ‘is gebaseerd op’ geregistreerd.
    -- Daarnaast lijken er gevallen te zijn met meerdere gebaseerd op stukdelen die te maken hebben met fouten uit de
    -- conversie van AKR naar Koers en/of met verkeerd herstel. Dit zal worden gecorrigeerd
    -- Onder andere in de volgende gevallen kan een tenaamstelling gebaseerd zijn op meerdere stukdelen:
    -- * In sommige stukdelen worden de ‘gebaseerd op’ relaties meegenomen van de oude tenaamstellingen naar de nieuwe
    -- tenaamstellingen, omdat de oude titels nog relevant zijn.
    -- Bv. Verklaring van erfrecht*
    -- Bij vermengen van tenaamstellingen kunnen meerdere ‘gebaseerd op’ relaties ontstaan.
    -- Bv. 1/2 verkregen obv stuk1 en 1/2 verkregen obv stuk2*
    -- Bij verenigen van percelen kunnen meerdere ‘gebaseerd op’ relaties ontstaan.
    -- Bv. perceel1 verkregen obv stuk1 en perceel2 verkregen obv stuk2* Bij beëindigen van een beperkt recht
    -- krijgen de tenaamstellingen van het onderliggend recht het te verwerken stuk als extra ‘is gebaseerd op’,
    -- omdat dit ook een vorm van verkrijging is. Bv. Afstand beperkt recht.
    --
    -- 0..2 stukdeel referentie
    isgebaseerdop                          VARCHAR(255) REFERENCES stukdeel (identificatie),
    isgebaseerdop2                         VARCHAR(255) REFERENCES stukdeel (identificatie),
    -- zakelijke recht referentie
    -- relatie Recht:Erfpachtcanon/Recht:betreft/Recht-ref:ZakelijkRechtRef
    betreft                                VARCHAR(255) REFERENCES recht (identificatie),
    -- OZ referentie
    rustop                                 VARCHAR REFERENCES onroerendezaak (identificatie),
    -- een splitsing ref
    isontstaanuit                          VARCHAR REFERENCES recht (identificatie),
    -- een splitsing ref
    isbetrokkenbij                         VARCHAR REFERENCES recht (identificatie),
    -- Mandeligheid ref
    isbestemdtot                           VARCHAR REFERENCES recht (identificatie),
    -- Erfpachtcanon.Soort is een nadere aanduiding van de erfpachtcanon. De waarden zijn opgenomen in een waardelijst
    -- We onderkennen de volgende soorten erfpachtcanon* Eeuwigdurend afgekocht* Afgekocht tot* Variabel bedrag* Jaarlijks bedrag
    -- https://developer.kadaster.nl/schemas/waardelijsten/SoortErfpachtcanon/
    soort                                  VARCHAR(22),
    -- Jaarlijksbedrag is het bedrag dat jaarlijks als erpachtcanon moet worden betaald.
    jaarlijksbedrag                        DECIMAL(20, 2),
    jaarlijksbedrag_valuta                 VARCHAR(42),
    -- Betreft meer onroerende zaken is een indicatie waarmee wordt aangegeven dat het jaarlijks bedrag meerdere onroerende zaken betreft.
    jaarlijksbedragbetreftmeerdere_oz      BOOLEAN,
    -- erfpacht
    einddatumafkoop                        DATE,
    -- Met deze indicatie wordt aangegeven dat de erfpacht oorspronkelijk gevestigd is bij een perceel dat later is verenigd met een ander perceel.
    indicatieoudeonroerendezaakbetrokken   BOOLEAN,
    -- Een Hoofdzaak is een onroerende zaak die een (aandeel in) een mandeligheid omvat.Wanneer een Hoofzaak uit een mandeligheid overgedragen wordt dan wordt ook de afhankelijke Mandelige zaak overgedragen.
    -- Mandeligheid komt vaak voor bij gemeenschappelijke achterpaden, parkeerplaatsen en dergelijke.
    heefthoofdzaak                         VARCHAR REFERENCES onroerendezaak (identificatie),
    -- NNP verwijzing
    heeftverenigingvaneigenaren            VARCHAR REFERENCES nietnatuurlijkpersoon (identificatie),
    -- TODO misschien een kolom met soort splitsing toevoegen? maar let op dat er al een soort kolom is voor erfpachtcanon
    -- TODO Hoofdsplitsing
    --		Een Hoofdsplitsing is het gesplitste Zakelijk recht van 1 of meer Percelen.De eigendom, het recht van erfpacht en/of het recht van
    --		opstal van 1 of enkele percelen (de zogenaamde grondpercelen) is gesplitst.
    --		De bij de hoofdsplitsing ontstane eigendom van de appartemenstrechten (de zogenaamde hoofd appartementsrechten) is tenaamgesteld
    --		van de gerechtigden (van het gesplitste zakelijke recht). Het gesplitste zakelijk recht van de grondpercelen is niet tenaamgesteld.
    -- TODO Ondersplitsing
    --		Een ondersplitsing is het ondergesplitste zakelijk recht van een appartementsrecht.
    --		Het eigendom, het recht van erfpacht en/of het recht van opstal van een appartementsrecht is ondergesplitst.
    --		De bij de ondersplitsing ontstane eigendom van de nieuwe appartemenstrechten is tenaam van de gerechtigden (van het gesplitste zakelijke recht) gesteld.
    --		Het ondergesplitste zakelijk recht is niet tenaamgesteld.
    --		Zowel hoofd appartementsrechten als eerder ondergesplitste appartementsrechten kunnen bij een (nieuwe) ondersplitsing betrokken zijn.
    -- TODO SpiegelsplitsingAfkoopErfpacht
    -- 		Een SpiegelsplitsingAfkoopErfpacht is het in appartementsrechten gesplitste recht van eigendom van 1 of meer percelen,
    -- 		waarvan het recht van erfpacht al eerder in appartementsrechten is gesplitst.
    -- 		Een SplitsingAfkoopErfpacht is een Appartementsrechtsplitsing en vormt het geheel van de bloot-eigendom van 1 of meer percelen,
    -- 		dat is gesplitst in appartementsrechten.In grote steden is in het verleden beleid geweest dat de grond niet in eigendom was
    -- 		te verkrijgen maar dat men alleen erfpacht kon verwerven.
    -- 		In veel gevallen is bij splitsing in appartementsrechten de erfpacht gesplitst.
    -- 		De bloot-eigendom van het grondperceel in niet in de splitsing betrokken en is in de BRK tenaam van de gemeente gesteld.
    -- 		In Rotterdam is het beleid enkel jaren geleden gewijzigd. Daar is het mogelijk geworden dat de erfpachter op zijn/haar wens de erfpacht
    -- 		mag afkopen (ook de bloot-eigendom verkrijgen) zodat hij/zij een volledig recht van eigendom heeft.
    -- 		In het geval 1 of meer eigenaren van een appartementsrecht de erfpacht willen afkopen wordt de bloot-eigendom van
    -- 		het grondperceel eveneens gesplitst in appartementsrechten. De bloot-eigendom wordt gesplitst in even zoveel appartementsrechten
    -- 		als er aanwezig zijn bij de splitsing van de erfpacht. Splitsing van de bloot-eigendom vindt plaats onder dezelfde complexaanduiding
    -- 		als de splitsing erfpacht, er is geen nieuwe tekening nodig. De spiegelsplitsingafkooperfpacht kent een 'eigen' Vereniging van eigenaren.
    -- TODO SpiegelsplitsingOndersplitsing
    -- 		SpiegelsplitsingOndersplitsing is een spiegelsplitsing afkoop erfpacht van een ondersplitsing.
    -- Aandeel in Recht is het aandeel waarvoor een persoon deelneemt in het Recht of GezamenlijkAandeel.
    -- Het aantal delen. De teller is altijd lager dan de noemer.
    aandeel_teller                         DECIMAL(32, 0),
    -- De noemer van het deel
    aandeel_noemer                         DECIMAL(32, 0),
    -- Burgerlijke staat tijdens verkrijging is een aanduiding voor de leefvorm van een persoon, zoals deze volgens het brondocument ten tijde van de verkrijging van het recht bestond.De waarden zijn opgenomen in een [waardelijst]
    -- Leefvorm van een persoon heeft betrekking op huwelijk c.q. geregistreerd partnerschap.
    -- https://developer.kadaster.nl/schemas/waardelijsten/BurgerlijkeStaat/
    burgerlijkestaattentijdevanverkrijging VARCHAR(43),
    -- Verkregen namens samenwerkingsverband is de aard van het samenwerkingsverband (zoals Maatschap, VOF of CV) namens welke
    -- een natuurlijk persoon deze tenaamstelling heeft verkregen.
    -- https://developer.kadaster.nl/schemas/waardelijsten/Samenwerkingsverband/
    verkregennamenssamenwerkingsverband    VARCHAR(26),
    -- relatie Recht:Tenaamstelling/Recht:van/Recht-ref:ZakelijkRechtRef
    van                                    VARCHAR REFERENCES recht (identificatie),
    -- NP verwijzing
    betrokkenpartner                       VARCHAR REFERENCES natuurlijkpersoon (identificatie),
    -- GezamenlijkAandeelRef
    geldtvoor                              VARCHAR REFERENCES recht (identificatie),
    -- NNP verwijzing
    betrokkensamenwerkingsverband          VARCHAR REFERENCES nietnatuurlijkpersoon (identificatie),
    -- NNP verwijzing
    betrokkengorzenenaanwassen             VARCHAR REFERENCES nietnatuurlijkpersoon (identificatie),
    -- NNP of NP verwijzing
    tennamevan                             VARCHAR REFERENCES persoon (identificatie),
    -- Omschrijving is de nadere beschrijving van de aantekening.
    omschrijving                           VARCHAR(4000),
    -- EinddatumRecht is de datum waarop een recht eindigt.
    -- De einddatum van een recht is in de BRK opgenomen als aantekening om de volgende redenen.
    -- Einddatum van een tenaamstelling is niet meer dan een aantekening (een verwijzing naar een stuk waarin iets is vermeld over een (mogelijke) einddatum).
    -- Het Kadaster acteert niet als dit tijdstip is aangebroken, dwz een tenaamstelling vervalt niet automatisch door tijdsverloop.
    -- De tenaamstelling in BRK vervalt enkel nadat er een nieuwe akte van levering is ingeschreven.
    -- Ook kan in een later stuk een andere einddatum zijn vermeld.
    -- Alle stukken betreffende een einddatum worden als aantekening bij de tenaamstelling vermeld.
    einddatumrecht                         DATE,
    -- Einddatum is de datum waarop de geldigheid van de aantekening eindigt.
    einddatum                              DATE,
    -- BetreftGedeelteVanPerceel is een aanduiding of de aantekening het gehele perceel (nee of niet gevuld) betreft of slechts een gedeelte (ja).
    betreftgedeeltevanperceel              BOOLEAN,
    aantekeningkadastraalobject            VARCHAR REFERENCES onroerendezaak (identificatie),
    -- NNP of NP verwijzing
    betrokkenpersoon                       VARCHAR REFERENCES persoon (identificatie),
    -- metadata tbv archivering
    begingeldigheid                        DATE         NOT NULL
    -- eindgeldigheid                        DATE
);

CREATE TABLE recht_archief
(
    identificatie                          VARCHAR(255) NOT NULL,
    aard                                   VARCHAR(255),
    toelichtingbewaarder                   VARCHAR(4000),
    -- stukdeel referentie
    isgebaseerdop                          VARCHAR(255) REFERENCES stukdeel (identificatie),
    isgebaseerdop2                         VARCHAR(255) REFERENCES stukdeel (identificatie),
    -- zakelijke recht referentie
    -- relatie Recht:Erfpachtcanon/Recht:betreft/Recht-ref:ZakelijkRechtRef
    -- REFERENCES recht (identificatie)
    betreft                                VARCHAR(255),
    -- OZ referentie
    -- REFERENCES onroerendezaak (identificatie)
    rustop                                 VARCHAR(255),
    -- een splitsing ref
    -- REFERENCES recht (identificatie)
    isontstaanuit                          VARCHAR(255),
    -- een splitsing ref
    -- REFERENCES recht (identificatie)
    isbetrokkenbij                         VARCHAR(255),
    -- Mandeligheid ref
    -- REFERENCES recht (identificatie)
    isbestemdtot                           VARCHAR(255),
    soort                                  VARCHAR(22),
    jaarlijksbedrag                        DECIMAL(20, 2),
    jaarlijksbedrag_valuta                 VARCHAR(42),
    jaarlijksbedragbetreftmeerdere_oz      BOOLEAN,
    einddatumafkoop                        DATE,
    indicatieoudeonroerendezaakbetrokken   BOOLEAN,
    -- REFERENCES onroerendezaak (identificatie)
    heefthoofdzaak                         VARCHAR(255),
    -- NNP verwijzing
    -- REFERENCES nietnatuurlijkpersoon (identificatie)
    heeftverenigingvaneigenaren            VARCHAR(255),
    aandeel_teller                         DECIMAL(32, 0),
    aandeel_noemer                         DECIMAL(32, 0),
    burgerlijkestaattentijdevanverkrijging VARCHAR(43),
    verkregennamenssamenwerkingsverband    VARCHAR(26),
    -- relatie Recht:Tenaamstelling/Recht:van/Recht-ref:ZakelijkRechtRef
    -- REFERENCES recht (identificatie)
    van                                    VARCHAR(255),
    -- NP verwijzing
    -- REFERENCES natuurlijkpersoon (identificatie)
    betrokkenpartner                       VARCHAR(255),
    -- GezamenlijkAandeelRef
    -- REFERENCES recht (identificatie)
    geldtvoor                              VARCHAR(255),
    -- NNP verwijzing
    -- REFERENCES nietnatuurlijkpersoon (identificatie)
    betrokkensamenwerkingsverband          VARCHAR(255),
    -- NNP verwijzing
    -- REFERENCES nietnatuurlijkpersoon (identificatie)
    betrokkengorzenenaanwassen             VARCHAR(255),
    -- NNP of NP verwijzing
    -- REFERENCES persoon (identificatie)
    tennamevan                             VARCHAR(255),
    omschrijving                           VARCHAR(4000),
    einddatumrecht                         DATE,
    einddatum                              DATE,
    betreftgedeeltevanperceel              BOOLEAN,
    -- REFERENCES onroerendezaak (identificatie)
    aantekeningkadastraalobject            VARCHAR(255),
    -- NNP of NP verwijzing
    -- REFERENCES persoon (identificatie)
    betrokkenpersoon                       VARCHAR(255),
    begingeldigheid                        DATE         NOT NULL,
    eindegeldigheid                        DATE         NOT NULL,
    PRIMARY KEY (identificatie, begingeldigheid)
);

-- koppeltabellen voor 1:n (n>1) recht:recht relaties
CREATE TABLE recht_aantekeningrecht
(
    aantekening    VARCHAR REFERENCES recht (identificatie),
    tenaamstelling VARCHAR REFERENCES recht (identificatie),
    PRIMARY KEY (aantekening, tenaamstelling)
);
CREATE TABLE recht_isbelastmet
(
    zakelijkrecht VARCHAR REFERENCES recht (identificatie),
    isbelastmet   VARCHAR REFERENCES recht (identificatie),
    PRIMARY KEY (zakelijkrecht, isbelastmet)
);
CREATE TABLE recht_isbeperkttot
(
    zakelijkrecht  VARCHAR REFERENCES recht (identificatie),
    -- tenaamstelling VARCHAR REFERENCES recht (identificatie); maar tenaamstelling onbreekt soms in bericht
    tenaamstelling VARCHAR(255) NOT NULL,
    PRIMARY KEY (zakelijkrecht, tenaamstelling)
);

CREATE TABLE recht_aantekeningrecht_archief
(
    --  REFERENCES recht_archief (identificatie)
    aantekening     VARCHAR(255) NOT NULL,
    tenaamstelling  VARCHAR(255) NOT NULL,
    -- REFERENCES recht_archief (identificatie)
    -- metadata tbv archivering
    begingeldigheid DATE         NOT NULL,
    PRIMARY KEY (aantekening, tenaamstelling, begingeldigheid)
);
CREATE TABLE recht_isbelastmet_archief
(
    -- REFERENCES recht_archief (identificatie)
    zakelijkrecht   VARCHAR(255) NOT NULL,
    -- REFERENCES recht_archief (identificatie)
    isbelastmet     VARCHAR(255) NOT NULL,
    -- metadata tbv archivering
    begingeldigheid DATE         NOT NULL,
    PRIMARY KEY (zakelijkrecht, isbelastmet, begingeldigheid)
);
CREATE TABLE recht_isbeperkttot_archief
(
    -- REFERENCES recht_archief (identificatie)
    zakelijkrecht   VARCHAR(255) NOT NULL,
    -- tenaamstelling VARCHAR REFERENCES recht (identificatie); maar tenaamstelling onbreekt soms in bericht
    tenaamstelling  VARCHAR(255) NOT NULL,
    -- metadata tbv archivering
    begingeldigheid DATE         NOT NULL,
    PRIMARY KEY (zakelijkrecht, tenaamstelling, begingeldigheid)
);

CREATE TABLE appartementsrecht
(
    identificatie  VARCHAR(255) NOT NULL PRIMARY KEY,
    -- Een Hoofdsplitsing is het gesplitste Zakelijk recht van 1 of meer Percelen.
    -- De eigendom, het recht van erfpacht en/of het recht van opstal van 1 of enkele percelen (de zogenaamde grondpercelen) is gesplitst.
    -- De bij de hoofdsplitsing ontstane eigendom van de appartemenstrechten (de zogenaamde hoofd appartementsrechten) is
    -- tenaamgesteld van de gerechtigden (van het gesplitste zakelijke recht).
    -- Het gesplitste zakelijk recht van de grondpercelen is niet tenaamgesteld.
    -- geen  `REFERENCES recht (identificatie)` omdat niet in alle berichten de de Hoofdsplitsing is opgenomen en er dan een constraint violation optreedt
    hoofdsplitsing VARCHAR(255) NOT NULL
    -- metadata tbv archivering
    -- begingeldigheid DATE         NOT NULL
);

CREATE TABLE appartementsrecht_archief
(
    identificatie   VARCHAR(255) NOT NULL,
    begingeldigheid DATE         NOT NULL,
    -- REFERENCES recht (identificatie)
    hoofdsplitsing  VARCHAR(255) NOT NULL,
    PRIMARY KEY (identificatie, begingeldigheid)
);


CREATE TABLE brmo_metadata
(
    naam   VARCHAR(255) NOT NULL PRIMARY KEY,
    waarde VARCHAR(255)
);

-- brmo versienummer
INSERT INTO brmo_metadata (naam, waarde) VALUES ('brmoversie', '${project.version}');

CREATE INDEX perceel_begrenzing_perceel ON perceel USING GIST (begrenzing_perceel);
CREATE INDEX perceel_plaatscoordinaten ON perceel USING GIST (plaatscoordinaten);
CREATE INDEX perceel_archief_begrenzing_perceel ON perceel_archief USING GIST (begrenzing_perceel);
CREATE INDEX perceel_archief_plaatscoordinaten ON perceel_archief USING GIST (plaatscoordinaten);
