-- TODO: historische tabel?
-- zelfde als onderstaande tabellen, maar met `registratietijdstip_einde` (en onbekende registratietijdstip waardes ingevuld, zodat ORDER BY etc werkt)?

CREATE TABLE nhr_persoon (
  id text primary key

  -- Het objecttype van deze persoon.
  -- Ondersteund: NaamPersoon, NatuurlijkPersoon, Rechtspersoon, Samenwerkingsverband, RechtspersoonInOprichting, EenmanszaakMetMeerdereEigenaren, BuitenlandseVennootschap.
  type text

  -- MateriëleRegistratie
  datumaanvang text
  datumeinde text

  -- FormeleRegistratie
  registratietijdstip text

  -- De textuele rechtsvorm van deze Persoon.
  persoonrechtsvorm text

  -- De uitgebreide textuele rechtsvorm van deze Persoon, inclusief structuur, rechtsbevoegdheid, etc.
  uitgebreiderechtsvorm text

  -- De volledige naam van deze Persoon.
  volledigenaam text

  -- Telefoonnummer en telefoontoegangscode, beschikbaar op een NaamPersoon (curator, bewindvoerder, of rechtercommissaris)
  naampersoon_nummer text
  naampersoon_toegangscode text

  -- TODO: NaamPersoon.adres

  -- De identifier voor een NaamPersoon wordt gebaseerd op de eerste van de volgende kolommen, wanneer deze beschikbaar zijn:
  --   - BSN
  --   - TIN
  --   - achternaam + geboortedatum (+ optioneel geboorteland + geboorteplaats) (TODO: is dit stabiel genoeg?)

  -- Volgende waardes zijn alleen beschikbaar op NatuurlijkPersoon:

  -- Volledige achternaam van deze NatuurlijkPersoon, samengesteld volgens BRP.
  achternaam text

  -- Burgerservicenmummer van deze NatuurlijkPersoon. Alleen beschikbaar wanneer geauthoriseerd.
  bsn text

  -- Geboortedatum van de NatuurlijkPersoon.
  geboortedatum text  -- TODO: Welk datatype voor DatumIncompleet?

  -- Geboorteland van de NatuurlijkPersoon. Alleen beschikbaar wanneer geauthoriseerd.
  geboorteland text -- enumeratie (mogelijk automatisch tabel voor opbouwen?)

  -- Alleen beschikbaar wanneer geauthoriseerd.
  geboorteplaats text

  -- Alleen beschikbaar wanneer geauthoriseerd.
  geslachtsaanduiding text -- enumeratie

  overlijdensdatum text -- DatumIncompleet

  -- Tax Information Number van deze NatuurlijkPersoon. Alleen beschikbaar wanneer geauthoriseerd. Elke UBO heeft slechts één BSN of één TIN.
  tin text 

  voornamen text
  voorvoegsel text

  -- Voor elk NietNatuurlijkPersoon wordt het RSIN gebruikt als sleutelwaarde.
  -- De volgende informatie is beschikbaar op elke NietNatuurlijkPersoon.

  datumuitschrijving text
  rsin text
  statutendatumaanvang text
  statutendatumakte text

  -- Beschikbaar op Rechtspersoon, Samenwerkingsverband
  rechtsvorm text

  -- Beschikbaar op Rechtspersoon, BuitenlandseVennootschap
  geplaatstkapitaal text -- references kapitaal

  -- Rechtspersoon:
  aanvangStatutaireZetel text
  activiteitenGestaaktPer text

  bedragkostenoprichting text -- references kapitaal

  beleggingsmijmetveranderlijkkapitaal char(1) -- is een Indicatie (J/N/O)

  datumakteoprichting text
  datumaktestatutenwijziging text
  datumeersteinschrijvinghandelsregister text
  datumoprichting text

  gestortkapitaal text -- references kapitaal

  ingangstatuten text

  maatschappelijkkapitaal text -- references kapitaal

  nieuwgemelderechtsvorm text
  overigeprivaatrechtelijkrechtsvorm text
  publiekrechtelijkerechtsvorm text
  rechtsbevoegdheidvereniging text
  statutairezetel text
  stelselinrichting text
  structuur text

  -- Samenwerkingsverband
  aantalcommanditairevennoten text

  -- RechtspersoonInOprichting
  doelrechtsvorm text

  -- BuitenlandseVennootschap
  landvanoprichting text -- enumeratie Land
  landvanvestiging text -- enumeratie Land
);

CREATE TABLE nhr_kapitaal (
  -- TODO: registratie
  id text primary key

  soort text
  waarde_valuta text
  waarde_waarde text -- decimaal(18.8)
);

CREATE TABLE nhr_kapitaal_aandeel (
  id text

  kapitaal text references nhr_kapitaal(id)

  aandeel_soort text
  -- TODO: benaming misschien een beetje dubbelop
  aandeel_waarde_valuta text
  aandeel_waarde_waarde text
);
