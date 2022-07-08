CREATE SCHEMA IF NOT EXISTS brk;

SET search_path = brk,public;

-- Een persoon is een natuurlijk persoon of een niet-natuurlijk persoon.
-- NL.IMKAD.Persoon
create table persoon
(
    identificatie                               varchar(255) not null primary key,
    -- Beschikkingsbevoegdheid is een aanduiding van de beschikkingsbevoegdheid van een persoon.
    -- https://developer.kadaster.nl/schemas/waardelijsten/Beschikkingsbevoegdheid/
    beschikkingsbevoegdheid                     varchar(33),
    -- Indicatie niet toonbare diakriet geeft aan of de naam van een persoon diakrieten bevat die niet getoond kunnen worden.
    -- Deze aanduiding was in het verleden nodig omdat de systemen niet alle diakrieten konden vastleggen en weergeven.
    -- Dit kan spelen bij personen die voor 2019 zijn geregistreerd in de BRK.
    indicatieNietToonbareDiakriet               boolean,
    postlocatie /* post adres referentie */     varchar,
    woonlocatie /* object locatie referentie */ varchar,
    -- tbv. DATAMODEL: om opzoeken van de bijhorende persoonsgegevens makkelijk te maken
    soort varchar(21) check ( soort in ('natuurlijkpersoon', 'nietnatuurlijkpersoon') )
);

create table natuurlijkpersoon
(
    identificatie                        varchar(255) not null primary key,
    -- Indicatie Overleden is een indicatie of de persoon al dan niet overleden is.
    -- Deze indicatie is enkel van belang als de gegevens uit de BRP niet beschikbaar zijn
    -- De datum van overlijden is soms niet bekend, maar wel dat iemand overleden is.
    indicatieoverleden                   boolean,
    -- Indicatie Afscherming Persoonsgegevens is een indicatie om aan te geven dat de gegevens van een persoon zijn afgeschermd.Op grond van artikel 37a Kadasterbesluit heeft het Kadaster de mogelijkheid op verzoek van een persoon gedurende een periode van vijf jaar geen gegevens over deze persoon te verstrekken. Alleen personen die binnen het stelsel Bewaken en Beveiligen vallen komen hiervoor in aanmerking.
    -- Gegevens worden wel verstrekt aan notarissen, deurwaarders en bestuursorganen indien zij deze gegevens nodig hebben voor de uitvoering van hun wettelijke taak.
    indicatieafschermingpersoonsgegevens boolean,
    -- De extern gekoppelde gegevens van NatuurlijkePersonen zijn gemodelleerd volgens het model van de BasisRegistratiePersonen.
    -- TODO kan mogelijk weg?
    betreft                              varchar,
    -- Het BSN is het burgerservicenummer van de persoon.
    bsn                                  varchar,
    -- adellijkeTitelOfPredikaat is de adellijke titel of het predikaat dat behoort tot de naam van de persoon (bij adellijke titel geslachtsnaam, bij predikaat voornaam).
    -- https://developer.kadaster.nl/schemas/waardelijsten/AdellijkeTitelOfPredicaat/
    adellijketitelofpredicaat            varchar(10),
    -- aanduidingNaamgebruik is een aanduiding voor de wijze van aanschrijving van de ingeschrevene.
    -- https://developer.kadaster.nl/schemas/waardelijsten/BRPAanduidingNaamgebruik/
    -- https://developer.kadaster.nl/schemas/waardelijsten/AanduidingNaamgebruik/
    aanduidingnaamgebruik                varchar(72),
    -- landWaarnaarVertrokken is het land dat de ingeschrevene opgeeft bij vertrek naar het buitenland. Als we van BRP een mutatiemelding krijgen met enkel een land dat wordt dat geregistreerd als landWaarnaarVertrokken. Worden ook andere adresgegevens ontvangen, minimaal woonplaats, dan wordt dit als buitenlandsadres geregistreerd.
    landwaarnaarvertrokken               varchar(40),
    geslachtsnaam                        varchar(200),
    voornamen                            varchar(200),
    voorvoegselsgeslachtsnaam            varchar(10),
    -- https://developer.kadaster.nl/schemas/waardelijsten/AanduidingGeslacht/ gebruikt 1/2/3
    -- https://developer.kadaster.nl/schemas/waardelijsten/BRPAanduidingGeslacht/ (gebruikt M/V/O)
    geslacht                             varchar(8),
    -- De geboortedatum is de datum waarop de persoon is geboren: OnvolledigeDatum
    geboortedatum                        varchar(4),
    -- De geboorteplaats is de plaats of een plaatsbepaling, die aangeeft waar de persoon is geboren
    geboorteplaats                       varchar(80),
    -- Het geboorteland is de naam, die het land aangeeft waar de persoon is geboren.
    -- https://developer.kadaster.nl/schemas/waardelijsten/BRPLand/
    geboorteland                         varchar(40),
    -- indicatieGeheim is een aanduiding die aangeeft dat gegevens van een persoon wel of niet verstrekt mogen worden.
    indicatiegeheim                      boolean,
    -- Datum overlijden is de datum waarop de persoon overleden is: OnvolledigeDatum
    datumoverlijden                      varchar(4),
    partnergeslachtsnaam                 varchar(200),
    partnervoornamen                     varchar(200),
    partnervoorvoegselsgeslachtsnaam     varchar(10)
);
-- Een rechtspersoon is een rechtssubject die zelfstandig drager is van rechten en met een natuurlijk persoon gelijk staat,
-- tenzij uit de wet het tegendeel voortvloeit.
-- De BRK bevat de rechtspersonen die rechthebbenden zijn m.b.t. een registergoed.
create table nietnatuurlijkpersoon
(
    identificatie   varchar(255) not null primary key,
    statutairenaam  varchar(200),
    -- Rechtsvorm is de rechtsvorm van de Rechtspersoon.
    -- https://developer.kadaster.nl/schemas/waardelijsten/Rechtsvorm/
    rechtsvorm      varchar(52),
    --Statutaire zetel is de plaats waar een Rechtspersoon volgens de statuten gevestigd is.
    statutairezetel varchar(40),
    -- TODO kan mogelijk weg?
    -- De extern gekoppelde gegevens van NietNatuurlijkePersonen zijn gemodelleerd volgens het model van het HandelsRegister
    betreft         varchar,
    -- Het RSIN is het Rechtspersonen Samenwerkingsverbanden Informatie Nummer is een uniek nummer wat iedere rechtspersoon krijgt.
    -- Een RSIN is een uniek nummen dat wordt toegekend aan een rechtspersoon of samenwerkingsverband.
    -- Een eenmanszaak is geen rechtspersoon en heeft dus ook geen RSIN.
    rsin            varchar(9),
    -- Het KvKNummer is een uniek identificerend administratienummer van een rechtspersoon zijnde een niet-natuurlijk persoon zoals
    -- door de Kamer van Koophandel
    -- Elke onderneming of maatschappelijke activiteit krijgt in het Handelsregister één KvK-nummer.
    -- Dit KvK-nummer bestaat altijd uit 8 cijfers.
    kvknummer       varchar(8)

);

-- alle verschillende soorten stukken:
--  NL.IMKAD.Kadasterstuk
--  NL.IMKAD.TIAStuk
create table stuk
(
    -- Identificatie is een door het Kadaster toegekend landelijk uniek nummer aan een object binnen de kadastrale registratie.
    identificatie         varchar(255) not null primary key,
    toelichtingbewaarder  varchar(4000),
    -- Ambtelijk correctie gegeven identificatie
    portefeuillenummer    varchar(16),
    -- De aanduiding van het deel binnen de reeks in het register, waarin het stuk is geregistreerd.
    -- Het Deel is een nummer maar vroeger zijn ook letters gebruik, daarom heeft het Deel het domein type Tekst.
    -- Er worden geen voorloopnullen gebruikt.
    deel                  varchar(5),
    -- het volgnummer van het stuk binnen het deel van het register.
    -- In de landelijke stukkenregistratie is dit nummer niet uniek identificerend binnen een register, omdat tijdig
    -- ingediende verbeteringen hetzelfde volgnummer krijgen als het oorspronkelijke stuk.
    -- Er worden geen voorloopnullen gebruikt.
    nummer                varchar(5),
    -- Verwijzing naar de oorspronkelijke (mogelijk tussentijds vervallen) Kadastervestiging waar het stuk oorspronkelijk is ingeschreven.
    -- https://developer.kadaster.nl/schemas/waardelijsten/Reekscode/
    reeks                 varchar,
    -- De registercode is de aanduiding van het het register waarin het stuk is ingeschreven.
    -- Dit kan zijn het register hypotheken 3 voor hypotheken en beslagen en hypotheken 4 voor alle andere stukken.
    -- https://developer.kadaster.nl/schemas/waardelijsten/Registercode/
    registercode          varchar(5),
    -- SoortRegister is een aanduiding van de hoofdcategorie van een ter inschrijving aangeboden stuk.
    -- Bijvoorbeeld Hypotheekakten, Transportakten.De mogelijke waarden zijn opgenomen in een waardelijst
    -- https://developer.kadaster.nl/schemas/waardelijsten/SoortRegister/
    soortregister         varchar(16),
    -- TijdstipAanbieding is het tijdstip waarop een ter inschrijving aangeboden stuk is ontvangen met in achtneming van de openingstijden en -dagen van het Kadaster.
    -- Als tijdstip van inschrijving geldt het tijdstip van aanbieding van de voor de inschrijving vereiste stukken.
    tijdstipaanbieding    timestamp,
    -- Tijdstip ondertekening is het tijdstip waarop een ter inschrijving aangeboden stuk is ondertekend door de opsteller van het stuk.
    -- Alleen wanneer het TIA stuk een PubliekrechtelijkeBeperking betreft dan is dit tijdstip niet verplicht.
    tijdstipondertekening timestamp,
    -- Tekening ingeschreven geeft aan dat er is sprake van een appartementstekening (splitsingstekening van appartementen) als bijlage bij het stuk.
    tekeningingeschreven  boolean
);

create table stukdeel
(
    -- Identificatie is een door het Kadaster toegekend landelijk uniek nummer aan een object binnen de kadastrale registratie.
    identificatie               character varying(255) not null primary key,
    -- Aanduiding aard stukdeel is een aanduiding voor de aard van een rechtsfeit.
    -- De mogelijke waarden zijn vermeld in een waardenlijst.De mogelijke waarden zijn vermeld in een waardenlijst
    -- https://developer.kadaster.nl/schemas/waardelijsten/AardStukdeel/
    aard                        varchar(255),
    -- Bedrag transactiesom levering is het in een ter inschrijving aangeboden stuk vermelde bedrag, waarvoor 1 of meer
    -- onroerende zaken zijn verkregen.In een stuk kunnen verschillende transacties zijn vermeld, met verschillende transactiesom.
    -- Per stukdeel (transactie) is de transactiesom weergegeven.
    bedragtransactiesomlevering decimal(9, 0),
    -- DatumKenbaarheid is de datum waarop een ter inschrijving aangboden publiekrechtelijke beperking besluit bekend is gemaakt of is geworden.
    datumKenbaarheidPB          date,
    -- referentie naar stuk
    deelvan                     varchar(255)           not null references stuk (identificatie)
);


-- Een onroerende zaak is de grond, de niet gewonnen delfstoffen, de met de grond verenigde beplantingen,
-- alsmede de gebouwen en werken die duurzaam met de grond zijn verenigd, hetzij rechtstreeks,
-- hetzij door vereniging met andere gebouwen of werken.
create table onroerendezaak
(
    -- Kadastrale aanduiding is de unieke aanduiding van een onroerende zaak, die door het kadaster wordt vastgesteld.
    kadastraleaanduiding     decimal(15, 0) not null primary key,
    -- Landinrichtingsrente is het bedrag waarmee de Onroerende zaak is belast in het kader van de landinrichtingswet.
    -- Opgebouwd uit de onderdelen Aanduiding Landinrichtingsrente, Bedrag Landinrichtingsrente, en Eindjaar Landinrichtingsrente.
    --
    -- Als twee percelen met elk  landinrichtingsrente-gegevens met verschillende eindjaren verenigd worden, worden beide landinrichtingsrente-gegevensgroepen bij het nieuwe perceel opgenomen.
    -- 0 to *
    -- landinrichtingsrente, bestaat uit
    -- 	Het bedrag waarmee de Onroerende zaak is belast in het kader van de landinrichtingswet.
    -- 	Let op: Het bedrag is in AKR in euro-centen opgenomen!
    lr_bedrag                decimal(9, 0),
    -- 	Het laatste kalenderjaar waarin de rente in het kader van landinrichtingswet nog verschuldigd is.
    lt_jaar                  integer,
    -- In principe moet elke onbebouwde onroerende zaak minimaal 1 (één) beschrijving hebben van de cultuur onbebouwd (bijv. grasland).
    -- Er kunnen meerdere culturen onbebouwd bij een onbebouwde onroerende zaak voorkomen.
    -- https://developer.kadaster.nl/schemas/waardelijsten/CultuurcodeOnbebouwd
    aard_cultuur_onbebouwd   varchar(65),
    -- In principe moet elke bebouwde onroerende zaak minimaal 1 (één) beschrijving hebben van de cultuur bebouwd (bijv. wonen).
    -- Er kunnen meerdere culturen bebouwd bij een onbebouwde onroerende zaak voorkomen.
    -- https://developer.kadaster.nl/schemas/waardelijsten/CultuurcodeBebouwd
    aard_cultuur_bebouwd     varchar(65),
    -- De in akten vermelde koopsommen die volgens de gehanteerde verwerkingsinstructie moeten worden geregistreerd.
    -- koopsom  bestaat uit:
    -- Het in een ter inschrijving aangeboden stuk vermelde bedrag, waarvoor 1 of meer onroerende zaken zijn verkregen.
    -- Koopsom is altijd een positief bedrag. Dit is een bedrag (omgerekend naar) euro's.
    ks_bedrag                decimal(9, 0),
    -- Het jaar waarin het belangrijkste recht van het kadastraal object is verkregen.
    ks_koopjaar              integer,
    -- Geeft aan of de koopsom betrekking heeft op meer dan 1 kadastraal object.
    ks_indicatiemeerobjecten boolean,
    -- Toelichting bewaarder is een toelichtende tekst van de bewaarder bij het kadastraal object.
    -- Een Toelichting Bewaarder wordt opgevoerd wanneer een toelichting bij gegevens in de registratie noodzakelijk is.
    -- Slechts in specifieke gevallen zal de bewaarder een toelichtende tekst bij een onroerende zaak willen toevoegen. In totaal zullen er dit enige duizenden zijn.
    toelichtingbewaarder     varchar(4000),
    --  Tijdstip onstaan object is datum en (als beschikbaar) het tijdstip waarop een onroerende zaak is ontstaan.
    --  Percelen en Appartementsrechten worden bij het Kadaster sinds 1832 geregistreerd.
    --  Het tijdstipOntstaanObject is niet sindsdien digitaal beschikbaar in de registratie, maar het is wel door middel van onderzoek te achterhalen.
    --  Het kan dus zijn dat tijdstipOntstaanObject op een bepaald (later) moment toch bekend wordt.
    --  Bij recentelijk ontstane onroerende zaken is dit gegeven wel altijd bekend.
    --  Voor percelen die ontstaan zijn na yyyy is altijd een tijdstip bekend, en voor appartementsrechten sinds yyyy.
    -- bestaat uit datum + (optioneel) tijdstip
    tijdstipontstaanobject   timestamp,
    -- OudstDigitaalBekend is de datum waarop het object voor het eerst vanuit een digitale Kadastrale registratie beschikbaar was is.
    -- Dit is een vaststaand gegeven en zal niet veranderen als gevolg van het digitaliseren van analoge registraties.
    -- Dit gegeven is te zien als het technisch tijdstip ontstaan van de eerste versie van een object.
    oudstdigitaalbekend      timestamp,
    -- ontstaan uit OZ filiatie relatie
    ontstaanuit              decimal(15, 0)

);



-- NL.IMKAD.PubliekrechtelijkeBeperking
-- omvat 1 of meer onroerendezaakbeperkingen
create table publiekrechtelijkebeperking
(
    identificatie                         varchar(255) primary key not null,
    -- De grondslag is een verwijzing naar de wet waar de publiekrechtelijke beperking op gebaseerd is.
    -- https://developer.kadaster.nl/schemas/waardelijsten/GrondslagBRK-PB/index.html
    grondslag                             varchar(255),
    datuminwerking                        date,
    datumbeeindiging                      date,
    isgebaseerdop /* stukdeel ref */      varchar references stukdeel (identificatie),
    bevoegdgezag /*TODO NNP referentie */ varchar
);

create table onroerendezaakbeperking
(
    identificatie                                varchar(255) primary key not null,
    inonderzoek                                  boolean,
    beperkt                                      decimal(15, 0) references onroerendezaak (kadastraleaanduiding),
    leidttot /*publiekrechtelijkebeperking ref*/ varchar references publiekrechtelijkebeperking (identificatie)
);

-- Een Onroerende zaak filiatie geeft de relatie aan tussen een nieuwe en een oude Onroerende zaak.
-- Het betreft hier de relatie tussen 2 percelen, 2 Appartementsrechten of 2 leidingnetwerken.
create table onroerendezaakfiliatie
(
    -- Een aanduiding voor de aard van de filiatie.
    -- Onroerende zaak filiatie wordt gebruikt om aan te geven hoe een onroerende zaak (historisch) tot stand gekomen is.
    -- Het geeft aan waarom het ene kadastrale object gerelateerd is aan het andere.
    -- https://developer.kadaster.nl/schemas/waardelijsten/AardFiliatie/
    aard    varchar(65),
    -- betreft OZ relatie; referentie naar OZ/Perceel/AppRe
    betreft decimal(15, 0) references onroerendezaak (kadastraleaanduiding) on delete cascade
);

-- In de BRK is een kadastraal perceel een specialisatie van een onroerende zaak.
-- Percelen worden cartografisch gerepresenteerd door een tweedimensionale vlakbegrenzing.
-- Tussen alle kadastrale percelen in Nederland geldt een topologische relatie (opdelende vlakstructuur), d.w.z. dat naburige perceelsvlakken naadloos moeten aansluiten en elkaar niet mogen overlappen.
create table perceel
(
    kadastraleaanduiding   decimal(15, 0)                not null primary key,
    -- Een perceel is een begrensd deel van het Nederlands grondgebied dat kadastraal geïdentificeerd is en met kadastrale grenzen begrensd is.
    -- Het gehele Nederlandse grondgebied is aaneengesloten kadastraal geïdentificeerd.
    -- Perceel is authentiek volgens de BRK voorzover het de attribuutsoorten kadastraleGrootte en KadatraleAanduiding betreft.
    begrenzing_perceel     geometry(MULTIPOLYGON, 28992) not null,
    -- De grootte van een perceel zoals vermeld in de kadastrale registratie. Het Kadaster bepaalt niet een excacte maar een indicatieve grootte.
    -- Grootte is alleen authentiek als soort groote de waarde "vastgesteld" heeft.
    -- De oppervlakgrootte wordt vastgelegd in vierkante meter.
    kadastralegrootte      decimal(9, 1),
    -- De soortGrootte geeft aan of de grootte van het perceel voorlopig, administratief of definitief is vastgesteld.
    -- Een grens is voorlopig zolang de [Aanwijzing kadastrale grens](http://tax.kadaster.nl/id/begrip/Aanwijzing_kadastrale_grens) nog niet heeft plaatsgevonden.
    -- Met voorlopige kadastrale grenzen (VKG) worden gehele kadastrale percelen gevormd, voordat de definitieve grenzen in het terrein aan te wijzen zijn.
    -- Bij de splitsing ontstaan gehele percelen met:* voorlopige kadastrale grenzen;* een voorlopige kadastrale oppervlakte;* definitieve perceelnummers.definitieve grenzen in het terrein aangewezen zijn of nog niet aan te wijzen zijn.
    -- Bij de splitsing ontstaan gehele percelen met:* voorlopige kadastrale grenzen;* een voorlopige kadastrale oppervlakte;* definitieve perceelnummers.
    -- Een administratieve grens is een grens die door het Kadaster is ingetekend om een, in een akte geleverd gedeelte van een perceel, af te beelden op de kadastrale kaart.
    -- Een administratieve grens wordt definitief als de grens, na aanwijzing door belanghebbenden, door het Kadaster is gemeten.
    -- Op de kadastrale kaart is de kleur van een definitiefe grens zwart, van een voorlopige grens bruin en een administratief voorlopige grens blauw.
    -- https://developer.kadaster.nl/schemas/waardelijsten/SoortGrootte/
    soortGrootte           varchar(100),
    -- Rotatie van het perceelnummer, in een hoek tussen de -90 +90 graden ten behoeve van afbeelding op de kaart.
    -- Perceelnummers worden bijvoorbeeld gekanteld om in een smal perceel te passen.
    perceelnummerrotatie   decimal(3, 1),
    -- Verschuiving op de X as.
    perceelnummer_deltax   decimal(10, 10),
    -- Verschuiving op de Y as.
    perceelnummer_deltay   decimal(10, 10),
    --  Betreft de plaatsing van het perceelnummer bij verbeelding op een kaart, om deze op een overzichtelijke plek in de perceel begrenzing te plaatsen.
    plaatscoordinaten      geometry(POINT, 28992)        not null,
    -- Meettarief verschuldigd is een indicatie voor het verschuldigd zijn van een meettarief bij overdracht van een perceel.
    -- Een meettarief is verschuldigd (indicator is true) als het een administratief gevormd perceel met voorlopige grenzen, of een perceel met voorlopige grenzen betreft dat nog nooit is overgedragen.
    meettariefVerschuldigd boolean
);

-- bevat alle "rechten"
--   NL.IMKAD.ZakelijkRecht
--   NL.IMKAD.Erfpachtcanon
--   NL.IMKAD.Mandeligheid
--   NL.IMKAD.AppartementsrechtSplitsing
--   TODO sub typen splitsing?:
--      Hoofdsplitsing
--      Ondersplitsing
--      SpiegelsplitsingAfkoopErfpacht
--      SpiegelsplitsingOndersplitsing
--   NL.IMKAD.Tenaamstelling
--   NL.IMKAD.GezamenlijkAandeel
--   NL.IMKAD.Aantekening
-- TODO sommige rechten hebben meer dan 1 stukdeel ref... daarvoor zouden we een koppeltabel nodig hebben
create table recht
(
    identificatie                                           varchar(255) not null primary key,
    -- Aard (zakelijk recht) is een aanduiding voor de aard van het recht. De waarden zijn opgenomen in een waardelijst.
    -- https://developer.kadaster.nl/schemas/waardelijsten/AardZakelijkRecht/
    -- of
    -- https://developer.kadaster.nl/schemas/waardelijsten/AardAantekening/
    aard                                                    varchar(255),
    toelichtingbewaarder                                    varchar(4000),
    --
    isbelastmet /*zak. recht referentie*/                   varchar references recht (identificatie),
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
    isgebaseerdop /*stukdeel referentie*/varchar(255) references stukdeel (identificatie),
    rustop /* OZ referentie*/                               decimal(15, 0) references onroerendezaak (kadastraleaanduiding),
    isontstaanuit /* een splitsing ref */                   varchar references recht (identificatie),
    isbetrokkenbij /* een splitsing ref */                  varchar references recht (identificatie),
    isbestemdtot /* Mandeligheid ref */                     varchar references recht (identificatie),
    isbeperkttot /* tenaamstelling ref */                   varchar references recht (identificatie),
    -- Erfpachtcanon.Soort is een nadere aanduiding van de erfpachtcanon. De waarden zijn opgenomen in een waardelijst
    -- We onderkennen de volgende soorten erfpachtcanon* Eeuwigdurend afgekocht* Afgekocht tot* Variabel bedrag* Jaarlijks bedrag
    -- https://developer.kadaster.nl/schemas/waardelijsten/SoortErfpachtcanon/
    soort                                                   varchar(22),
    -- Jaarlijksbedrag is het bedrag dat jaarlijks als erpachtcanon moet worden betaald.
    jaarlijksbedrag                                         decimal(9, 0),
    -- Betreft meer onroerende zaken is een indicatie waarmee wordt aangegeven dat het jaarlijks bedrag meerdere onroerende zaken betreft.
    jaarlijksbedragbetreftmeerdere_oz                       boolean,
    -- erfpacht
    einddatumafkoop                                         date,
    -- Met deze indicatie wordt aangegeven dat de erfpacht oorspronkelijk gevestigd is bij een perceel dat later is verenigd met een ander perceel.
    indicatieoudeonroerendezaakbetrokken                    boolean,
    -- Een Hoofdzaak is een onroerende zaak die een (aandeel in) een mandeligheid omvat.Wanneer een Hoofzaak uit een mandeligheid overgedragen wordt dan wordt ook de afhankelijke Mandelige zaak overgedragen.
    -- Mandeligheid komt vaak voor bij gemeenschappelijke achterpaden, parkeerplaatsen en dergelijke.
    heefthoofdzaak                                          decimal(15, 0) references onroerendezaak (kadastraleaanduiding),
    heeftverenigingvaneigenaren /*  NNP verwijzing */   varchar references nietnatuurlijkpersoon(identificatie),
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
    aandeel_teller                                          decimal(32, 0),
    -- De noemer van het deel
    aandeel_noemer                                          decimal(32, 0),
    -- Burgerlijke staat tijdens verkrijging is een aanduiding voor de leefvorm van een persoon, zoals deze volgens het brondocument ten tijde van de verkrijging van het recht bestond.De waarden zijn opgenomen in een [waardelijst]
    -- Leefvorm van een persoon heeft betrekking op huwelijk c.q. geregistreerd partnerschap.
    -- https://developer.kadaster.nl/schemas/waardelijsten/BurgerlijkeStaat/
    burgerlijkestaattentijdevanverkrijging                  varchar(43),
    -- Verkregen namens samenwerkingsverband is de aard van het samenwerkingsverband (zoals Maatschap, VOF of CV) namens welke
    -- een natuurlijk persoon deze tenaamstelling heeft verkregen.
    -- https://developer.kadaster.nl/schemas/waardelijsten/Samenwerkingsverband/
    verkregennamenssamenwerkingsverband                     varchar(26),
    betrokkenpartner /* NP verwijzing */               varchar references natuurlijkpersoon(identificatie),
    geldtvoor /* GezamenlijkAandeelRef*/                    varchar references recht (identificatie),
    betrokkensamenwerkingsverband /*  NNP verwijzing */ varchar references nietnatuurlijkpersoon(identificatie),
    betrokkengorzenenaanwassen /*  NNP verwijzing */    varchar references nietnatuurlijkpersoon(identificatie),
    tennamevan /*  NNP of NP verwijzing */              varchar references persoon(identificatie),
    -- Omschrijving is de nadere beschrijving van de aantekening.
    omschrijving                                            varchar(4000),
    -- EinddatumRecht is de datum waarop een recht eindigt.
    -- De einddatum van een recht is in de BRK opgenomen als aantekening om de volgende redenen.
    -- Einddatum van een tenaamstelling is niet meer dan een aantekening (een verwijzing naar een stuk waarin iets is vermeld over een (mogelijke) einddatum).
    -- Het Kadaster acteert niet als dit tijdstip is aangebroken, dwz een tenaamstelling vervalt niet automatisch door tijdsverloop.
    -- De tenaamstelling in BRK vervalt enkel nadat er een nieuwe akte van levering is ingeschreven.
    -- Ook kan in een later stuk een andere einddatum zijn vermeld.
    -- Alle stukken betreffende een einddatum worden als aantekening bij de tenaamstelling vermeld.
    einddatumRecht                                          date,
    -- Einddatum is de datum waarop de geldigheid van de aantekening eindigt.
    einddatum                                               date,
    -- BetreftGedeelteVanPerceel is een aanduiding of de aantekening het gehele perceel (nee of niet gevuld) betreft of slechts een gedeelte (ja).
    betreftgedeeltevanperceel                               boolean,
    -- AantekeningRecht is een aantekening bij een tenaamstelling van een recht.
    aantekeningrecht /* tenaamstelling ref*/            varchar references recht(identificatie),
    --
    aantekeningkadastraalobject                             decimal(15, 0) references onroerendezaak (kadastraleaanduiding),
    betrokkenpersoon /* NNP of NP verwijzing */        varchar references persoon(identificatie)
);

create table appartementsrecht
(
    kadastraleaanduiding decimal(15, 0)         not null primary key,
    -- Een Hoofdsplitsing is het gesplitste Zakelijk recht van 1 of meer Percelen.
    -- De eigendom, het recht van erfpacht en/of het recht van opstal van 1 of enkele percelen (de zogenaamde grondpercelen) is gesplitst.
    -- De bij de hoofdsplitsing ontstane eigendom van de appartemenstrechten (de zogenaamde hoofd appartementsrechten) is
    -- tenaamgesteld van de gerechtigden (van het gesplitste zakelijke recht).
    -- Het gesplitste zakelijk recht van de grondpercelen is niet tenaamgesteld.
    hoofdsplitsing       character varying(255) not null references recht (identificatie)
);

