COMMENT ON TABLE brmo_metadata IS 'BRMO metadata en versie gegevens';


COMMENT ON TABLE stuk IS 'Een stuk is een, door het Kadaster als authentiek erkend, document waaruit een wijziging in de BRK blijkt.';
COMMENT ON COLUMN stuk.identificatie IS 'Identificatie is een door het Kadaster toegekend landelijk uniek nummer aan een object binnen de kadastrale registratie.';
COMMENT ON COLUMN stuk.toelichtingbewaarder IS 'Toelichting bewaarder is een toelichtende tekst van de bewaarder bij een stuk. Een Toelichting Bewaarder wordt opgevoerd wanneer een toelichting bij gegevens in de registratie noodzakelijk is.';
COMMENT ON COLUMN stuk.portefeuillenummer IS 'Ambtelijk correctie gegeven identificatie';
COMMENT ON COLUMN stuk.deel IS 'De aanduiding van het deel binnen de reeks in het register, waarin het stuk is geregistreerd.';
COMMENT ON COLUMN stuk.nummer IS 'Het het volgnummer van het stuk binnen het deel van het register.';
COMMENT ON COLUMN stuk.reeks IS 'Verwijzing naar de oorspronkelijke (mogelijk tussentijds vervallen) Kadastervestiging waar het stuk oorspronkelijk is ingeschreven.';
COMMENT ON COLUMN stuk.registercode IS 'De registercode is de aanduiding van het het register waarin het stuk is ingeschreven.';
COMMENT ON COLUMN stuk.soortregister IS 'Een aanduiding van de hoofdcategorie van een ter inschrijving aangeboden stuk.';
COMMENT ON COLUMN stuk.tijdstipaanbieding IS 'TijdstipAanbieding is het tijdstip waarop een ter inschrijving aangeboden stuk is ontvangen met in achtneming van de openingstijden en -dagen van het Kadaster.';
COMMENT ON COLUMN stuk.tijdstipondertekening IS 'Tijdstip ondertekening is het tijdstip waarop een ter inschrijving aangeboden stuk is ondertekend door de opsteller van het stuk.';
COMMENT ON COLUMN stuk.tekeningingeschreven IS 'Tekening ingeschreven geeft aan dat er is sprake van een appartementstekening (splitsingstekening van appartementen) als bijlage bij het stuk.';


COMMENT ON TABLE stukdeel is 'Een stukdeel is een paragraaf in een akte waarmee een recht gevestigd wordt.';
COMMENT ON COLUMN stukdeel.identificatie IS 'Identificatie is een door het Kadaster toegekend landelijk uniek nummer aan een object binnen de kadastrale registratie.';
COMMENT ON COLUMN stukdeel.aard IS 'Aanduiding aard stukdeel is een aanduiding voor de aard van een rechtsfeit.';
COMMENT ON COLUMN stukdeel.bedragtransactiesomlevering IS 'Bedrag transactiesom levering is het in een ter inschrijving aangeboden stuk vermelde bedrag, waarvoor 1 of meer onroerende zaken zijn verkregen.';
COMMENT ON COLUMN stukdeel.valutatransactiesomlevering IS 'De aanduiding van de valutasoort.';
COMMENT ON COLUMN stukdeel.datumkenbaarheidpb IS 'DatumKenbaarheid is de datum waarop een ter inschrijving aangboden publiekrechtelijke beperking besluit bekend is gemaakt of is geworden.';
COMMENT ON COLUMN stukdeel.deelvan IS 'Referentie naar het stuk waarvan dit deel uitmaakt.';


COMMENT ON TABLE onroerendezaak IS 'Een onroerende zaak is de grond, de niet gewonnen delfstoffen, de met de grond verenigde beplantingen, alsmede de gebouwen en werken die duurzaam met de grond zijn verenigd, hetzij rechtstreeks, hetzij door vereniging met andere gebouwen of werken.';
COMMENT ON COLUMN onroerendezaak.identificatie IS 'Identificatie is een door het Kadaster toegekend landelijk uniek nummer aan een object binnen de kadastrale registratie.';
COMMENT ON COLUMN onroerendezaak.begingeldigheid IS 'BRMO: metadata tbv archivering, datum van bericht.';
COMMENT ON COLUMN onroerendezaak.eindegeldigheid IS 'BRMO: metadata tbv archivering, datum van opvolgend bericht.';
COMMENT ON COLUMN onroerendezaak.akrkadastralegemeentecode IS 'De kadastrale gemeente, deel van de kadastrale aanduiding van de onroerende zaak.';
COMMENT ON COLUMN onroerendezaak.akrkadastralegemeente IS 'De kadastrale gemeente, deel van de kadastrale aanduiding van de onroerende zaak.';
COMMENT ON COLUMN onroerendezaak.kadastralegemeentecode IS 'De kadastrale gemeente, deel van de kadastrale aanduiding van de onroerende zaak.';
COMMENT ON COLUMN onroerendezaak.kadastralegemeente IS 'De kadastrale gemeente, deel van de kadastrale aanduiding van de onroerende zaak.';
COMMENT ON COLUMN onroerendezaak.sectie IS 'Sectie is een onderverdeling van de kadastrale gemeente, bedoeld om het werk van de meetdienst en om de kadastrale kaarten overzichtelijk te houden.';
COMMENT ON COLUMN onroerendezaak.perceelnummer IS 'Het perceelnummer dat een geheel perceel of een complex uniek identificeert binnen de sectie.';
COMMENT ON COLUMN onroerendezaak.appartementsrechtvolgnummer IS 'Nummer dat het kadastraal object uniek identificeert als een appartementsrecht binnen het complex.';
COMMENT ON COLUMN onroerendezaak.landinrichtingsrente_bedrag IS 'Het bedrag waarmee de Onroerende zaak is belast in het kader van de landinrichtingswet.';
COMMENT ON COLUMN onroerendezaak.landinrichtingsrente_valuta IS 'De aanduiding van de valutasoort van het landinrichtingsrente bedrag.';
COMMENT ON COLUMN onroerendezaak.landinrichtingsrente_jaar IS 'Het laatste kalenderjaar waarin de rente in het kader van landinrichtingswet nog verschuldigd is.';
COMMENT ON COLUMN onroerendezaak.aard_cultuur_onbebouwd IS 'AardCultuurOnbebouwd is een aanduiding voor aard van het gebruik van een onbebouwde onroerende zaak.';
COMMENT ON COLUMN onroerendezaak.aard_cultuur_bebouwd IS 'AardCultuurBebouwd is een aanduiding voor aard van het gebruik van een bebouwde onroerende zaak.';
COMMENT ON COLUMN onroerendezaak.koopsom_bedrag IS 'Het in een ter inschrijving aangeboden stuk vermelde bedrag, waarvoor 1 of meer onroerende zaken zijn verkregen.';
COMMENT ON COLUMN onroerendezaak.koopsom_valuta IS 'De aanduiding van de valutasoort van de koopsom.';
COMMENT ON COLUMN onroerendezaak.koopsom_koopjaar IS 'Het jaar waarin het belangrijkste recht van het kadastraal object is verkregen.';
COMMENT ON COLUMN onroerendezaak.koopsom_indicatiemeerobjecten IS 'Geeft aan of de koopsom betrekking heeft op meer dan 1 kadastraal object.';
COMMENT ON COLUMN onroerendezaak.toelichtingbewaarder IS 'Toelichting bewaarder is een toelichtende tekst van de bewaarder bij het kadastraal object.';
COMMENT ON COLUMN onroerendezaak.tijdstipontstaanobject IS 'Tijdstip onstaan object is datum en (als beschikbaar) het tijdstip waarop een onroerende zaak is ontstaan.';
COMMENT ON COLUMN onroerendezaak.oudstdigitaalbekend IS 'OudstDigitaalBekend is de datum waarop het object voor het eerst vanuit een digitale Kadastrale registratie beschikbaar was.';


COMMENT ON TABLE perceel IS 'Een perceel is een begrensd deel van het Nederlands grondgebied dat kadastraal geïdentificeerd is en met kadastrale grenzen begrensd is.';
COMMENT ON COLUMN perceel.identificatie IS 'Identificatie is een door het Kadaster toegekend landelijk uniek nummer aan een object binnen de kadastrale registratie.';
COMMENT ON COLUMN perceel.begrenzing_perceel IS 'Begrenzing perceel is het geheel van lijnketens waarmee de vlakomtrek van een perceel wordt gevormd.';
COMMENT ON COLUMN perceel.kadastralegrootte IS 'Oppervlak grootte, in vierkante meters.';
COMMENT ON COLUMN perceel.soortgrootte IS 'Geeft aan of de grootte van het perceel voorlopig, administratief of definitief is vastgesteld.';
COMMENT ON COLUMN perceel.perceelnummerrotatie IS 'Rotatie van het perceelnummer, in een hoek tussen de -90 +90 graden ten behoeve van afbeelding op de kaart.';
COMMENT ON COLUMN perceel.perceelnummer_deltax IS 'Verschuiving op de X as.';
COMMENT ON COLUMN perceel.perceelnummer_deltay IS 'Verschuiving op de Y as.';
COMMENT ON COLUMN perceel.plaatscoordinaten IS 'Betreft de plaatsing van het perceelnummer bij verbeelding op een kaart, om deze op een overzichtelijke plek in de perceel begrenzing te plaatsen.';
COMMENT ON COLUMN perceel.meettariefverschuldigd IS 'Meettarief verschuldigd is een indicatie voor het verschuldigd zijn van een meettarief bij overdracht van een perceel.';


COMMENT ON TABLE appartementsrecht IS 'Een Appartementsrecht is een aandeel in goederen die in een splitsing zijn betrokken, dat de bevoegdheid omvat tot het uitsluitend gebruik van bepaalde gedeelten van het gebouw die blijkens hun inrichting bestemd zijn of worden om als afzonderlijk geheel te worden gebruikt.';
COMMENT ON COLUMN appartementsrecht.identificatie IS 'Identificatie is een door het Kadaster toegekend landelijk uniek nummer aan een object binnen de kadastrale registratie.';
COMMENT ON COLUMN appartementsrecht.hoofdsplitsing IS 'Een Hoofdsplitsing is het gesplitste Zakelijk recht van 1 of meer Percelen.';


COMMENT ON TABLE adres IS 'Een AdresLocatie is een PostbusLocatie of een ObjectLocatie';
COMMENT ON COLUMN adres.identificatie IS 'Identificatie is een door het Kadaster toegekend landelijk uniek nummer aan een object binnen de kadastrale registratie.';
COMMENT ON COLUMN adres.huisnummer IS 'Een door of namens het gemeentebestuur ten aanzien van een adresseerbaar object toegekende nummering.';
COMMENT ON COLUMN adres.huisletter IS 'Een door of namens het gemeentebestuur ten aanzien van een adresseerbaar object toegekende toevoeging aan een huisnummer in de vorm van een alfanumeriek teken.';
COMMENT ON COLUMN adres.huisnummertoevoeging IS 'Een door of namens het gemeentebestuur ten aanzien van een adresseerbaar object toegekende nadere toevoeging aan een huisnummer of een combinatie van huisnummer en huisletter.';
COMMENT ON COLUMN adres.postbusnummer IS 'Het postbusnummer van een postbuslocatie is het nummer van een postbus.';
COMMENT ON COLUMN adres.postcode IS 'De postcode van het gebouw waarin de postbus zich bevindt of de door PostNL vastgestelde code behorende bij een bepaalde combinatie van een straatnaam en een huisnummer.';
COMMENT ON COLUMN adres.openbareruimtenaam IS 'De naam die aan een openbare ruimte is toegekend in een daartoe strekkend formeel gemeentelijk besluit.';
COMMENT ON COLUMN adres.woonplaatsnaam IS 'De woonplaats waarin de postbus zich bevindt of de benaming van een door het gemeentebestuur aangewezen woonplaats.';
COMMENT ON COLUMN adres.nummeraanduiding IS 'Het nummer van de door het bevoegde gemeentelijke orgaan als zodanig toegekende aanduiding van een verblijfsobject, een standplaats of een ligplaats.';
COMMENT ON COLUMN adres.adresseerbaarobject IS 'Een adresseerbaar object is een object waaraan adressen ingevolge de basisregistratie adressen kunnen worden toegekend : een verblijfsobject , standplaats of ligplaats.';
COMMENT ON COLUMN adres.verblijfsobject IS 'Het nummer van de door het bevoegde gemeentelijke orgaan als zodanig toegekende aanduiding van een verblijfsobject.';
COMMENT ON COLUMN adres.standplaats IS 'Het nummer van de door het bevoegde gemeentelijke orgaan als zodanig toegekende aanduiding van een standplaats.';
COMMENT ON COLUMN adres.ligplaats IS 'Het nummer van de door het bevoegde gemeentelijke orgaan als zodanig toegekende aanduiding van een ligplaats.';
COMMENT ON COLUMN adres.hoofdadres IS 'Referentie naar de betreffende objectlocatie.';
COMMENT ON COLUMN adres.nevenadres IS 'Referentie naar de betreffende objectlocatie.';
COMMENT ON COLUMN adres.buitenlandadres IS 'Het adres is een combinatie van de straat en huisnummer.';
COMMENT ON COLUMN adres.buitenlandwoonplaats IS 'Woonplaats is de combinatie van een eventuele postcode en woonplaats.';
COMMENT ON COLUMN adres.buitenlandregio IS 'Regio is de naam van een of meer geografische gebieden als groepering ten behoeve van het adresseren.';
COMMENT ON COLUMN adres.land IS 'Land is een topografische - en internationaal rechtserkende eenheid.';


COMMENT ON TABLE objectlocatie IS 'Een Locatie kadastraal object is een aanduiding voor de locatie van het Kadastraal object in Nederland.';
COMMENT ON COLUMN objectlocatie.heeft IS 'Referentie naar de betreffende onroerende zaak.';
COMMENT ON COLUMN objectlocatie.betreft IS 'Referentie naar het betreffende adres.';
COMMENT ON COLUMN objectlocatie.koppelingswijze IS 'Koppelingswijze is de beschrijving van de manier waarop de koppeling tussen het Kadastraalobject (Perceel of Appartementsrecht) met ObjectlocatieBinnenland bepaald is.';


COMMENT ON TABLE persoon IS 'Een persoon is een natuurlijk persoon of een niet-natuurlijk persoon.';
COMMENT ON COLUMN persoon.identificatie IS 'Identificatie is een door het Kadaster toegekend landelijk uniek nummer aan een object binnen de kadastrale registratie.';
COMMENT ON COLUMN persoon.beschikkingsbevoegdheid IS 'Beschikkingsbevoegdheid is een aanduiding van de beschikkingsbevoegdheid van een persoon.';
COMMENT ON COLUMN persoon.indicatieniettoonbarediakriet IS 'Indicatie niet toonbare diakriet geeft aan of de naam van een persoon diakrieten bevat die niet getoond kunnen worden.';
COMMENT ON COLUMN persoon.postlocatie IS 'Postadres, referentie naar adres';
COMMENT ON COLUMN persoon.woonlocatie IS 'Woonadres, referentie naar adres';
COMMENT ON COLUMN persoon.soort IS 'tbv. datamodel: om opzoeken van de bijhorende persoonsgegevens in tabel natuurlijkpersoon|nietnatuurlijkpersoon te vergemakkelijken.';


COMMENT ON TABLE natuurlijkpersoon IS 'Een natuurlijk persoon is een mens als subject en drager van rechten en plichten.';
COMMENT ON COLUMN natuurlijkpersoon.identificatie IS 'Identificatie is een door het Kadaster toegekend landelijk uniek nummer aan een object binnen de kadastrale registratie.';
COMMENT ON COLUMN natuurlijkpersoon.indicatieoverleden IS 'Natuurlijk persoon is al dan niet overleden en is niet gekoppeld aan de BRP.';
COMMENT ON COLUMN natuurlijkpersoon.indicatieafschermingpersoonsgegevens IS 'Indicatie Afscherming Persoonsgegevens is een indicatie om aan te geven dat de gegevens van een persoon zijn afgeschermd.';
COMMENT ON COLUMN natuurlijkpersoon.bsn IS 'Het BSN is het burgerservicenummer van de persoon.';
COMMENT ON COLUMN natuurlijkpersoon.adellijketitelofpredicaat IS 'De adellijke titel of het predikaat dat behoort tot de naam van de persoon (bij adellijke titel geslachtsnaam, bij predikaat voornaam).';
COMMENT ON COLUMN natuurlijkpersoon.aanduidingnaamgebruik IS 'Een aanduiding voor de wijze van aanschrijving van de ingeschrevene.';
COMMENT ON COLUMN natuurlijkpersoon.landwaarnaarvertrokken IS 'Het land dat de ingeschrevene opgeeft bij vertrek naar het buitenland.';
COMMENT ON COLUMN natuurlijkpersoon.geslachtsnaam IS 'De geslachtsnaam is de (geslachts)naam waarvan de eventueel aanwezige voorvoegsels en adellijke titel zijn afgesplitst.';
COMMENT ON COLUMN natuurlijkpersoon.voornamen IS 'De voornamen zijn de verzameling namen die, gescheiden door spaties, aan de geslachtsnaam voorafgaat. Indien aanwezig, wordt het predikaat afgesplitst.';
COMMENT ON COLUMN natuurlijkpersoon.voorvoegselsgeslachtsnaam IS 'Voorvoegselsgeslachtsnaam zijn dat deel van de geslachtsnaam dat voorkomt in Tabel 36 (BRP), Voorvoegseltabel en, gescheiden door een spatie, vooraf gaat aan de rest van de geslachtsnaam.';
COMMENT ON COLUMN natuurlijkpersoon.geslacht IS 'De geslachtsaanduiding is een gegeven over het geslacht van de persoon (man, vrouw, of onbekend).';
COMMENT ON COLUMN natuurlijkpersoon.geboortedatum IS 'De geboortedatum is de datum waarop de persoon is geboren.';
COMMENT ON COLUMN natuurlijkpersoon.geboorteplaats IS 'De geboorteplaats is de plaats of een plaatsbepaling, die aangeeft waar de persoon is geboren';
COMMENT ON COLUMN natuurlijkpersoon.geboorteland IS 'Het geboorteland is de naam, die het land aangeeft waar de persoon is geboren.';
COMMENT ON COLUMN natuurlijkpersoon.indicatiegeheim IS 'Een aanduiding die aangeeft dat gegevens van een persoon wel of niet verstrekt mogen worden.';
COMMENT ON COLUMN natuurlijkpersoon.datumoverlijden IS 'Datum overlijden is de datum waarop de persoon overleden is.';
COMMENT ON COLUMN natuurlijkpersoon.partnergeslachtsnaam IS 'De geslachtsnaam van de partner is de (geslachts)naam waarvan de eventueel aanwezige voorvoegsels en adellijke titel zijn afgesplitst.';
COMMENT ON COLUMN natuurlijkpersoon.partnervoornamen IS 'De voornamen van de partner zijn de verzameling namen die, gescheiden door spaties, aan de geslachtsnaam voorafgaat. Indien aanwezig, wordt het predikaat afgesplitst.';
COMMENT ON COLUMN natuurlijkpersoon.partnervoorvoegselsgeslachtsnaam IS 'Dat deel van de geslachtsnaam van de partner dat voorkomt in Tabel 36 (BRP), Voorvoegseltabel en, gescheiden door een spatie, vooraf gaat aan de rest van de geslachtsnaam.';


COMMENT ON TABLE nietnatuurlijkpersoon IS 'Een niet-natuurlijk persoon is een rechtspersoon of een samenwerkingsverband zonder rechtspersoonlijkheid.';
COMMENT ON COLUMN nietnatuurlijkpersoon.identificatie IS 'Identificatie is een door het Kadaster toegekend landelijk uniek nummer aan een object binnen de kadastrale registratie.';
COMMENT ON COLUMN nietnatuurlijkpersoon.statutairenaam IS 'Statutaire naam is de naam van de Niet-Natuurlijk Persoon zoals die statutair is vastgelegd.';
COMMENT ON COLUMN nietnatuurlijkpersoon.rechtsvorm IS 'Rechtsvorm is de rechtsvorm van de Rechtspersoon.';
COMMENT ON COLUMN nietnatuurlijkpersoon.statutairezetel IS 'Statutaire zetel is de plaats waar een Rechtspersoon volgens de statuten gevestigd is.';
COMMENT ON COLUMN nietnatuurlijkpersoon.rsin IS 'Het RSIN is het Rechtspersonen Samenwerkingsverbanden Informatie Nummer is een uniek nummer wat iedere rechtspersoon krijgt.';
COMMENT ON COLUMN nietnatuurlijkpersoon.kvknummer IS 'Het KvKNummer is een uniek identificerend administratienummer van een rechtspersoon zijnde een niet-natuurlijk persoon zoals toegewezen door de Kamer van Koophandel.';


COMMENT ON TABLE publiekrechtelijkebeperking IS 'PubliekrechtelijkeBeperking';
COMMENT ON COLUMN publiekrechtelijkebeperking.identificatie IS 'Identificatie is een door het Kadaster toegekend landelijk uniek nummer aan een object binnen de kadastrale registratie.';
COMMENT ON COLUMN publiekrechtelijkebeperking.grondslag IS 'De grondslag is een verwijzing naar de wet waar de publiekrechtelijke beperking op gebaseerd is.';
COMMENT ON COLUMN publiekrechtelijkebeperking.datuminwerking IS 'datumInWerking wordt overgenomen in het LTO van de PB';
COMMENT ON COLUMN publiekrechtelijkebeperking.datumbeeindiging IS 'datumBeeindiging wordt overgenomen in het LTV van de PB';
COMMENT ON COLUMN publiekrechtelijkebeperking.isgebaseerdop IS 'Het stukdeel onderliggende de beperking.';
COMMENT ON COLUMN publiekrechtelijkebeperking.bevoegdgezag IS 'De niet-natuurlijk persoon die beheerder is.';


COMMENT ON TABLE onroerendezaakbeperking IS 'OnroerendeZaakBeperking';
COMMENT ON COLUMN onroerendezaakbeperking.inonderzoek IS 'Één of meer objecten uit het werkingsgebied van de publiekrechtelijke beperking die overlappen met de onroerende zaak, zijn blijkens hun registratie, inmiddels niet meer actueel. Het bevoegd gezag dient het werkingsgebied opnieuw te beoordelen.';
COMMENT ON COLUMN onroerendezaakbeperking.beperkt IS 'De onroerende zaak waarop de beperking is gevestigd.';
COMMENT ON COLUMN onroerendezaakbeperking.leidttot IS 'De publiekrechtelijkebeperking.';


COMMENT ON TABLE onroerendezaakfiliatie IS 'Een Onroerende zaak filiatie geeft de relatie aan tussen een nieuwe en een oude Onroerende zaak. Het betreft hier de relatie tussen 2 percelen, 2 Appartementsrechten of 2 leidingnetwerken.';
COMMENT ON COLUMN onroerendezaakfiliatie.aard IS 'Aard is een aanduiding voor de aard van de filiatie. Het geeft aan waarom het ene kadastrale object gerelateerd is aan het andere.';
COMMENT ON COLUMN onroerendezaakfiliatie.onroerendezaak IS 'De ontstane onroerende zaak';
COMMENT ON COLUMN onroerendezaakfiliatie.betreft IS 'De betreffende (bron) onroerende zaak.';
COMMENT ON COLUMN onroerendezaakfiliatie.begingeldigheid IS 'BRMO: metadata tbv archivering, datum van bericht.';


COMMENT ON TABLE recht IS 'Alle rechten uit het BRK model: Zakelijkrecht, Erfpacht, Mandeligheid, AppartementsrechtSplitsing en andere splitsingen, Tenaamstelling, GezamenlijkAandeel, Aantekening.';
COMMENT ON COLUMN recht.identificatie IS 'Identificatie is een door het Kadaster toegekend landelijk uniek nummer aan een object binnen de kadastrale registratie.';
COMMENT ON COLUMN recht.aard IS 'Een aanduiding voor de aard van het recht of de soort van de aantekening.';
COMMENT ON COLUMN recht.toelichtingbewaarder IS 'Toelichting bewaarder is een toelichtende tekst van de bewaarder bij het Zakelijk Recht.';
COMMENT ON COLUMN recht.isgebaseerdop IS 'Relatie naar het aan het recht onderliggende stukdeel.';
COMMENT ON COLUMN recht.isgebaseerdop2 IS 'Relatie naar het aan het recht onderliggende stukdeel.';
COMMENT ON COLUMN recht.betreft IS 'Betreft zakelijk recht.';
COMMENT ON COLUMN recht.rustop IS 'Relatie van zakelijk recht naar onroerende zaak.';
COMMENT ON COLUMN recht.isontstaanuit IS 'Relatie van zakelijk recht naar (appartementsrecht) splitsing.';
COMMENT ON COLUMN recht.isbetrokkenbij IS 'Relatie van zakelijk recht naar (appartementsrecht) splitsing.';
COMMENT ON COLUMN recht.isbestemdtot IS 'Relatie van zakelijk recht naar mandeligheid.';
COMMENT ON COLUMN recht.soort IS 'Soort is een nadere aanduiding van de erfpachtcanon.';
COMMENT ON COLUMN recht.jaarlijksbedrag IS 'Jaarlijksbedrag is het bedrag dat jaarlijks als erpachtcanon moet worden betaald.';
COMMENT ON COLUMN recht.jaarlijksbedrag_valuta IS 'De aanduiding van de valutasoort van het jaarlijksbedrag.';
COMMENT ON COLUMN recht.jaarlijksbedragbetreftmeerdere_oz IS 'Betreft meer onroerende zaken is een indicatie waarmee wordt aangegeven dat het jaarlijks bedrag van de erfpachtcanon meerdere onroerende zaken betreft.';
COMMENT ON COLUMN recht.einddatumafkoop IS 'Erfpachtcanon afkoop, dit is niet hetzelfde als de historievelden in Voorkomen.';
COMMENT ON COLUMN recht.indicatieoudeonroerendezaakbetrokken IS 'Met deze indicatie wordt aangegeven dat de erfpacht oorspronkelijk gevestigd is bij een perceel dat later is verenigd met een ander perceel.';
COMMENT ON COLUMN recht.heefthoofdzaak IS 'Een Hoofdzaak is een onroerende zaak die een (aandeel in) een mandeligheid omvat.';
COMMENT ON COLUMN recht.heeftverenigingvaneigenaren IS '-';
COMMENT ON COLUMN recht.aandeel_teller IS 'Het aantal delen. Aandeel in Recht is het aandeel waarvoor een persoon deelneemt in het Recht.';
COMMENT ON COLUMN recht.aandeel_noemer IS 'De noemer van het deel. Aandeel in Recht is het aandeel waarvoor een persoon deelneemt in het Recht.';
COMMENT ON COLUMN recht.burgerlijkestaattentijdevanverkrijging IS 'Burgerlijke staat tijdens verkrijging is een aanduiding voor de leefvorm van een persoon, zoals deze volgens het brondocument ten tijde van de verkrijging van het recht bestond.';
COMMENT ON COLUMN recht.verkregennamenssamenwerkingsverband IS 'Verkregen namens samenwerkingsverband is de aard van het samenwerkingsverband (zoals Maatschap, VOF of CV) namens welke een natuurlijk persoon deze tenaamstelling heeft verkregen.';
COMMENT ON COLUMN recht.van IS 'Relatie van tenaamstelling naar zakelijk recht';
COMMENT ON COLUMN recht.betrokkenpartner IS 'Relatie van tenaamstelling naar natuurlijk persoon';
COMMENT ON COLUMN recht.geldtvoor IS 'Relatie van tenaamstelling naar gezamenlijk aandeel';
COMMENT ON COLUMN recht.betrokkensamenwerkingsverband IS 'Relatie van tenaamstelling naar niet-natuurlijk persoon';
COMMENT ON COLUMN recht.betrokkengorzenenaanwassen IS 'Relatie van tenaamstelling naar niet-natuurlijk persoon';
COMMENT ON COLUMN recht.tennamevan IS 'Relatie van tenaamstelling naar persoon';
COMMENT ON COLUMN recht.omschrijving IS 'Omschrijving is de nadere beschrijving van de aantekening.';
COMMENT ON COLUMN recht.einddatumrecht IS 'De datum waarop een recht eindigt. Einddatum van een tenaamstelling is niet meer dan een aantekening (een verwijzing naar een stuk waarin iets is vermeld over een (mogelijke) einddatum).';
COMMENT ON COLUMN recht.einddatum IS 'Einddatum is de datum waarop de geldigheid van de aantekening eindigt.';
COMMENT ON COLUMN recht.betreftgedeeltevanperceel IS 'BetreftGedeelteVanPerceel is een aanduiding of de aantekening het gehele perceel (nee of niet gevuld) betreft of slechts een gedeelte (ja).';
COMMENT ON COLUMN recht.aantekeningkadastraalobject IS 'Relatie van aantekening naar onroerende zaak.';
COMMENT ON COLUMN recht.betrokkenpersoon IS 'Relatie van aantekening naar persoon';
COMMENT ON COLUMN recht.begingeldigheid IS 'BRMO: metadata tbv archivering, datum van bericht.';


COMMENT ON TABLE recht_aantekeningrecht IS 'Koppeltabel voor aantekeningen bij een tenaamstelling van een recht.';
COMMENT ON COLUMN recht_aantekeningrecht.aantekening IS 'De aantekening';
COMMENT ON COLUMN recht_aantekeningrecht.tenaamstelling IS 'De aangetekende tenaamstelling';


COMMENT ON TABLE recht_isbelastmet IS 'Koppeltabel voor de relatie van zakelijk recht naar zakelijk recht.';
COMMENT ON COLUMN recht_isbelastmet.zakelijkrecht IS 'Het zakelijk recht.';
COMMENT ON COLUMN recht_isbelastmet.isbelastmet IS 'Het belastende zakelijk recht';


COMMENT ON TABLE recht_isbeperkttot IS 'Koppeltabel voor de relatie van zakelijk recht naar tenaamstelling.';
COMMENT ON COLUMN recht_isbeperkttot.zakelijkrecht IS 'Het zakelijk recht';
COMMENT ON COLUMN recht_isbeperkttot.tenaamstelling IS 'De beperkte tenaamstelling';
