SET SCHEMA 'brk';
SET search_path = brk,public;

CREATE MATERIALIZED VIEW mb_subject
            (
             objectid,
             subject_identif,
             soort,
             geslachtsnaam,
             voorvoegsel,
             voornamen,
             aand_naamgebruik,
             geslachtsaand,
             naam,
             woonadres,
             geboortedatum,
             geboorteplaats,
             overlijdensdatum,
             bsn,
             organisatie_naam,
             rechtsvorm,
             statutaire_zetel,
             rsin,
             kvk_nummer
                )
AS
SELECT row_number() OVER ()                               AS objectid,
       p.identificatie                                    AS subject_identif,
       p.soort                                            AS soort,
       np.geslachtsnaam                                   AS geslachtsnaam,
       np.voorvoegselsgeslachtsnaam                       AS voorvoegsel,
       np.voornamen                                       AS voornamen,
       np.aanduidingnaamgebruik                           AS aand_naamgebruik,
       np.geslacht                                        AS geslachtsaand,
       CASE
           WHEN (nnp.statutairenaam IS NOT NULL)
               THEN (nnp.statutairenaam)
           ELSE ((REPLACE(COALESCE(np.voornamen, '') || ' ' ||
                          COALESCE(np.voorvoegselsgeslachtsnaam, '') || ' ', '  ', ' ') ||
                  COALESCE(np.geslachtsnaam, '')))
           END                                            AS naam,
       REPLACE(COALESCE(a.openbareruimtenaam, '') || ' ' || COALESCE(a.huisnummer::text, '') ||
               COALESCE(a.huisletter, '') || COALESCE(a.huisnummertoevoeging, '') || ' ' ||
               COALESCE(a.woonplaatsnaam, ''), '  ', ' ') AS woonadres,
       np.geboortedatum                                   AS geboortedatum,
       np.geboorteplaats                                  AS geboorteplaats,
       np.datumoverlijden                                 AS overlijdensdatum,
       np.bsn                                             AS bsn,
       nnp.statutairenaam                                 AS organisatie_naam,
       nnp.rechtsvorm                                     AS rechtsvorm,
       nnp.statutairezetel                                AS statutaire_zetel,
       nnp.rsin                                           AS rsin,
       nnp.kvknummer                                      AS kvk_nummer
FROM persoon p
         LEFT JOIN natuurlijkpersoon np on p.identificatie = np.identificatie
         LEFT JOIN nietnatuurlijkpersoon nnp on p.identificatie = nnp.identificatie
         LEFT JOIN adres a on p.woonlocatie = a.identificatie
WITH NO DATA;

CREATE UNIQUE INDEX mb_subject_objectid ON mb_subject USING btree (objectid);
CREATE UNIQUE INDEX mb_subject_identif ON mb_subject USING btree (subject_identif);

COMMENT ON MATERIALIZED VIEW mb_subject IS
    'commentaar view mb_subject:
    samenvoeging alle soorten subjecten: natuurlijk en niet-natuurlijk.

    beschikbare kolommen:
    * objectid: uniek id bruikbaar voor geoserver/arcgis,
    * subject_identif: natuurlijke id van subject
    * soort: soort subject zoals natuurlijk, niet-natuurlijk enz.
    * geslachtsnaam: -
    * voorvoegsel: -
    * voornamen: -
    * aand_naamgebruik: -
    * geslachtsaand: -
    * naam: samengestelde naam bruikbaar voor natuurlijke en niet-natuurlijke subjecten
    * woonadres: woonlocatie meegeleverd adres buiten BAG koppeling om
    * geboortedatum: -
    * geboorteplaats: -
    * overlijdensdatum: -
    * bsn: -
    * organisatie_naam: statutairenaam NNP
    * rechtsvorm: -
    * statutaire_zetel: -
    * rsin: -
    * kvk_nummer: -
    ';


CREATE MATERIALIZED VIEW mb_avg_subject
            (
             objectid,
             subject_identif,
             soort,
             geslachtsnaam,
             voorvoegsel,
             voornamen,
             aand_naamgebruik,
             geslachtsaand,
             naam,
             woonadres,
             geboortedatum,
             geboorteplaats,
             overlijdensdatum,
             bsn,
             organisatie_naam,
             rechtsvorm,
             statutaire_zetel,
             rsin,
             kvk_nummer
                )
AS
SELECT s.objectid,
       s.subject_identif,
       s.soort,
       NULL::text         AS geslachtsnaam,
       NULL::text         AS voorvoegsel,
       NULL::text         AS voornamen,
       NULL::text         AS aand_naamgebruik,
       NULL::text         AS geslachtsaand,
       s.organisatie_naam AS naam,
       NULL::text         AS woonadres,
       NULL::text         AS geboortedatum,
       NULL::text         AS geboorteplaats,
       NULL::text         AS overlijdensdatum,
       NULL::text         AS bsn,
       s.organisatie_naam,
       s.rechtsvorm,
       s.statutaire_zetel,
       s.rsin,
       s.kvk_nummer
FROM mb_subject s
WITH NO DATA;

CREATE UNIQUE INDEX mb_avg_subject_objectid ON mb_avg_subject USING btree (objectid);
CREATE INDEX mb_avg_subject_identif ON mb_avg_subject USING btree (subject_identif);

COMMENT ON MATERIALIZED VIEW mb_avg_subject IS
    'commentaar view mb_avg_subject:
    volledig subject (natuurlijk en niet natuurlijk) geschoond voor avg
    beschikbare kolommen:
    * objectid: uniek id bruikbaar voor geoserver/arcgis,
    * subject_identif: natuurlijke id van subject
    * soort: soort subject zoals natuurlijk, niet-natuurlijk enz.
    * geslachtsnaam: NULL (avg)
    * voorvoegsel: NULL (avg)
    * voornamen: NULL (avg)
    * aand_naamgebruik: NULL (avg)
    * geslachtsaand:NULL (avg)
    * naam: gelijk aan organisatie_naam
    * woonadres: NULL (avg)
    * geboortedatum: NULL (avg)
    * geboorteplaats: NULL (avg)
    * overlijdensdatum: NULL (avg)
    * bsn: NULL (avg)
    * organisatie_naam: naam niet natuurlijk subject
    * rechtsvorm: -
    * statutaire_zetel: -
    * rsin: -
    * kvk_nummer: -
    ';


CREATE MATERIALIZED VIEW mb_kad_onrrnd_zk_adres
            (
             objectid,
             koz_identif,
             begin_geldigheid,
             begin_geldigheid_datum,
             benoemdobj_identif,
             type,
             aanduiding,
             aanduiding2,
             sectie,
             perceelnummer,
             appartementsindex,
             gemeentecode,
             aand_soort_grootte,
             grootte_perceel,
             oppervlakte_geom,
             deelperceelnummer,
             omschr_deelperceel,
             verkoop_datum,
             aard_cultuur_onbebouwd,
             bedrag,
             koopjaar,
             meer_onroerendgoed,
             valutasoort,
             loc_omschr,
             aantekeningen,
             na_identif,
             na_status,
             gemeente,
             woonplaats,
             straatnaam,
             huisnummer,
             huisletter,
             huisnummer_toev,
             postcode,
             gebruiksdoelen,
             oppervlakte_obj,
             lon,
             lat,
             begrenzing_perceel)
AS
SELECT row_number() OVER ()                                                                            AS objectid,
       o.identificatie                                                                                 AS koz_identif,
       o.begingeldigheid::text                                                                         AS begin_geldigheid,
       o.begingeldigheid                                                                               AS begin_geldigheid_datum,
       -- koppeling met BAG
       NULL                                                                                            AS benoemdobj_identif,
       qry.type                                                                                        AS type,
       COALESCE(o.sectie, '') || ' ' || COALESCE(o.perceelnummer::text, '')                            AS aanduiding,
       COALESCE(o.akrkadastralegemeente, '') || ' ' || COALESCE(o.sectie, '') || ' ' ||
       COALESCE(o.perceelnummer::text, '') || ' ' || COALESCE(o.appartementsrechtvolgnummer::text, '') AS aanduiding2,
       o.sectie                                                                                        AS sectie,
       o.perceelnummer                                                                                 AS perceelnummer,
       o.appartementsrechtvolgnummer                                                                   AS appartementsindex,
       o.akrkadastralegemeente                                                                         AS gemeentecode,
       qry.soortgrootte                                                                                AS aand_soort_grootte,
       qry.kadastralegrootte                                                                           AS grootte_perceel,
       st_area(qry.begrenzing_perceel)                                                                 AS oppervlakte_geom,
       -- bestaat niet
       NULL                                                                                            AS deelperceelnummer,
       -- bestaat niet
       NULL                                                                                            AS omschr_deelperceel,
       -- TODO verkoop datum uit stukdeel via recht
       NULL                                                                                            AS verkoop_datum,
       o.aard_cultuur_onbebouwd                                                                        AS aard_cultuur_onbebouwd,
       o.koopsom_bedrag                                                                                AS bedrag,
       o.koopsom_koopjaar                                                                              AS koopjaar,
       o.koopsom_indicatiemeerobjecten                                                                 AS meer_onroerendgoed,
       o.koopsom_valuta                                                                                AS valutasoort,
       -- TODO BRK adres?
       NULL                                                                                            AS loc_omschr,
--       (
--        (
--            (select count(objloc.heeft) from objectlocatie objloc where o.identificatie = objloc.heeft)  )::text || ' meer adressen'
--                             )                                                                           AS loc_omschr,
       array_to_string(
               (SELECT array_agg(('id: ' || r.identificatie || ', ' ||
                                  'aard: ' || COALESCE(r.aard, '') || ', ' ||
                                  'begin: ' || COALESCE(r.begingeldigheid::text, '') || ', ' ||
                                  'beschrijving: ' || COALESCE(r.omschrijving, '') || ', ' ||
                                  'eind: ' || COALESCE(r.einddatum::text, '') || ', ' ||
                                  'koz-id: ' || COALESCE(r.aantekeningkadastraalobject, '') || ', ' ||
                                  'subject-id: ' || COALESCE(r.betrokkenpersoon, '') || '; '))
                FROM recht r
                WHERE r.aantekeningkadastraalobject = o.identificatie), ' & ')                         AS aantekeningen,
       -- koppeling met BAG
       NULL                                                                                            AS na_identif,
       -- koppeling met BAG
       NULL                                                                                            AS na_status,
       -- koppeling met BAG
       NULL                                                                                            AS gemeente,
       -- koppeling met BAG
       NULL                                                                                            AS woonplaats,
       -- koppeling met BAG
       NULL                                                                                            AS straatnaam,
       -- koppeling met BAG
       NULL                                                                                            AS huisnummer,
       -- koppeling met BAG
       NULL                                                                                            AS huisletter,
       -- koppeling met BAG
       NULL                                                                                            AS huisnummer_toev,
       -- koppeling met BAG
       NULL                                                                                            AS postcode,
       -- koppeling met BAG
       NULL                                                                                            AS gebruiksdoelen,
       -- koppeling met BAG
       NULL                                                                                            AS oppervlakte_obj,
       st_x(st_transform(qry.plaatscoordinaten, 4326))                                                 AS lon,
       st_y(st_transform(qry.plaatscoordinaten, 4326))                                                 AS lat,
       qry.begrenzing_perceel                                                                          AS begrenzing_perceel
FROM (SELECT p.identificatie
                 AS identificatie,
             'perceel'
                 AS type,
             p.soortgrootte,
             p.kadastralegrootte,
             p.begrenzing_perceel,
             p.plaatscoordinaten
      FROM perceel p
      UNION ALL
      SELECT a.identificatie
                 AS identificatie,
             'appartement'
                 AS type,
             NULL
                 AS soortgrootte,
             NULL
                 AS kadastralegrootte,
             NULL
                 AS begrenzing_perceel,
             NULL
                 AS plaatscoordinaten
      FROM appartementsrecht a) qry
         JOIN onroerendezaak o ON qry.identificatie = o.identificatie
WITH NO DATA;

CREATE UNIQUE INDEX mb_kad_onrrnd_zk_adres_objectid ON mb_kad_onrrnd_zk_adres USING btree (objectid);
CREATE INDEX mb_kad_onrrnd_zk_adres_identif ON mb_kad_onrrnd_zk_adres USING btree (koz_identif);
CREATE INDEX mb_kad_onrrnd_zk_adres_begrenzing_perceel_idx ON mb_kad_onrrnd_zk_adres USING gist (begrenzing_perceel);

COMMENT ON MATERIALIZED VIEW mb_kad_onrrnd_zk_adres IS
    'commentaar view mb_kad_onrrnd_zk_adres:
    alle kadastrale onroerende zaken (perceel en appartementsrecht) met opgezochte verkoop datum, objectid voor geoserver/arcgis en BAG adres

    beschikbare kolommen:
    * objectid: uniek id bruikbaar voor geoserver/arcgis,
    * koz_identif: natuurlijke id van perceel of appartementsrecht
    * begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
    * begin_geldigheid_datum: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
    * benoemdobj_identif: koppeling met BAG object,
    * type: perceel of appartement,
    * aanduiding: sectie perceelnummer,
    * aanduiding2: kadgem sectie perceelnummer appartementsindex,
    * sectie: -,
    * perceelnummer: -,
    * appartementsindex: -,
    * gemeentecode: -,
    * aand_soort_grootte: -,
    * grootte_perceel: -,
    * oppervlakte_geom: oppervlakte berekend uit geometrie, hoort gelijk te zijn aan grootte_perceel,
    * deelperceelnummer: -,
    * omschr_deelperceel: -,
    * verkoop_datum: laatste datum gevonden akten van verkoop,
    * aard_cultuur_onbebouwd: -,
    * bedrag: -,
    * koopjaar: -,
    * meer_onroerendgoed: -,
    * valutasoort: -,
    * loc_omschr: adres buiten BAG om meegegeven,
    * aantekeningen: -,
    * na_identif: identificatie van nummeraanduiding
    * na_status: status van nummeraanduiding
    * gemeente: -,
    * woonplaats: -,
    * straatnaam: -,
    * huisnummer: -,
    * huisletter: -,
    * huisnummer_toev: -,
    * postcode: -,
    * gebruiksdoelen: alle gebruiksdoelen gescheiden door komma
    * oppervlakte_obj: oppervlak van gebouwd object
    * lon: coordinaat als WSG84,
    * lon: coordinaat als WSG84,
    * begrenzing_perceel: perceelvlak
    ';



CREATE MATERIALIZED VIEW mb_percelenkaart
            (
             objectid,
             koz_identif,
             begin_geldigheid,
             begin_geldigheid_datum,
             type,
             aanduiding,
             aanduiding2,
             sectie,
             perceelnummer,
             gemeentecode,
             aand_soort_grootte,
             grootte_perceel,
             oppervlakte_geom,
             verkoop_datum,
             aard_cultuur_onbebouwd,
             bedrag,
             koopjaar,
             meer_onroerendgoed,
             valutasoort,
             aantekeningen,
             lon,
             lat,
             begrenzing_perceel
                )
AS
SELECT row_number() OVER ()                                                    AS objectid,
       o.identificatie                                                         AS koz_identif,
       o.begingeldigheid::text                                                 AS begin_geldigheid,
       o.begingeldigheid                                                       AS begin_geldigheid_datum,
       qry.type                                                                AS type,
       COALESCE(o.sectie, '') || ' ' || COALESCE(o.perceelnummer::text, '')    AS aanduiding,
       COALESCE(o.akrkadastralegemeente, '') || ' ' || COALESCE(o.sectie, '') || ' ' ||
       COALESCE(o.perceelnummer::text, '')                                     AS aanduiding2,
       o.sectie                                                                AS sectie,
       o.perceelnummer                                                         AS perceelnummer,
       o.akrkadastralegemeente                                                 AS gemeentecode,
       qry.soortgrootte                                                        AS aand_soort_grootte,
       qry.kadastralegrootte                                                   AS grootte_perceel,
       st_area(qry.begrenzing_perceel)                                         AS oppervlakte_geom,
       -- TODO verkoop datum uit stukdeel via recht
       NULL                                                                    AS verkoop_datum,
       o.aard_cultuur_onbebouwd                                                AS aard_cultuur_onbebouwd,
       o.koopsom_bedrag                                                        AS bedrag,
       o.koopsom_koopjaar                                                      AS koopjaar,
       o.koopsom_indicatiemeerobjecten                                         AS meer_onroerendgoed,
       o.koopsom_valuta                                                        AS valutasoort,
       array_to_string(
               (SELECT array_agg(('id: ' || r.identificatie || ', ' ||
                                  'aard: ' || COALESCE(r.aard, '') || ', ' ||
                                  'begin: ' || COALESCE(r.begingeldigheid::text, '') || ', ' ||
                                  'beschrijving: ' || COALESCE(r.omschrijving, '') || ', ' ||
                                  'eind: ' || COALESCE(r.einddatum::text, '') || ', ' ||
                                  'koz-id: ' || COALESCE(r.aantekeningkadastraalobject, '') || ', ' ||
                                  'subject-id: ' || COALESCE(r.betrokkenpersoon, '') || '; '))
                FROM recht r
                WHERE r.aantekeningkadastraalobject = o.identificatie), ' & ') AS aantekeningen,

       st_x(st_transform(qry.plaatscoordinaten, 4326))                         AS lon,
       st_y(st_transform(qry.plaatscoordinaten, 4326))                         AS lat,
       qry.begrenzing_perceel                                                  AS begrenzing_perceel
FROM (SELECT p.identificatie,
             'perceel' AS type,
             p.soortgrootte,
             p.kadastralegrootte,
             p.plaatscoordinaten,
             p.begrenzing_perceel
      FROM perceel p) qry
         LEFT JOIN onroerendezaak o on qry.identificatie = o.identificatie
WITH NO DATA;

CREATE UNIQUE INDEX mb_percelenkaart_objectid ON mb_percelenkaart USING btree (objectid);
CREATE INDEX mb_percelenkaart_identif ON mb_percelenkaart USING btree (koz_identif);
CREATE INDEX mb_percelenkaart_begrenzing_perceel_idx ON mb_percelenkaart USING gist (begrenzing_perceel);

COMMENT ON MATERIALIZED VIEW mb_percelenkaart IS
    'commentaar view mb_percelenkaart:
    alle kadastrale onroerende zaken (perceel en appartementsrecht) met opgezochte verkoop datum, objectid voor geoserver/arcgis

    beschikbare kolommen:
    * objectid: uniek id bruikbaar voor geoserver/arcgis,
    * koz_identif: natuurlijke id van perceel of appartementsrecht
    * begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
    * begin_geldigheid_datum: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
    * type: perceel of appartement,
    * aanduiding: sectie perceelnummer,
    * aanduiding2: kadgem sectie perceelnummer,
    * sectie: -,
    * perceelnummer: -,
    * gemeentecode: -,
    * aand_soort_grootte: -,
    * grootte_perceel: -,
    * oppervlakte_geom: oppervlakte berekend uit geometrie, hoort gelijk te zijn aan grootte_perceel,
    * verkoop_datum: laatste datum gevonden akten van verkoop,
    * aard_cultuur_onbebouwd: -,
    * bedrag: -,
    * koopjaar: -,
    * meer_onroerendgoed: -,
    * valutasoort: -,
    * aantekeningen: -,
    * lon: coordinaat als WSG84,
    * lon: coordinaat als WSG84,
    * begrenzing_perceel: perceelvlak
    ';



---
-- eigendom
-- select
--     -- zakelijk recht
--     r.identificatie  as zak_recht_id,
--     -- altijd 'Eigendom (recht van)'
--     -- r.aard,
--     r.rustop,
--     -- tenaamsteling
--     r2.identificatie as tenaamstelling,
--     r2.isgebaseerdop,
--     r2.tennamevan,
--     r2.aandeel_teller,
--     r2.aandeel_noemer,
--     -- stukdeel
--     s.aard,
--     s.bedragtransactiesomlevering,
--     -- stuk
--     s2.tijdstipaanbieding,
--     s2.tijdstipondertekening
-- from recht r
--          left join recht r2 on
--     r.identificatie = r2.van
--          left join stukdeel s on
--     s.identificatie = r2.isgebaseerdop
--          left join stuk s2 on
--     s2.identificatie = s.deelvan
-- where r.aard = 'Eigendom (recht van)';

CREATE OR REPLACE VIEW
    vb_util_zk_recht
            (
             zr_identif,
             ingangsdatum_recht,
             aandeel,
             ar_teller,
             ar_noemer,
             subject_identif,
             koz_identif,
             indic_betrokken_in_splitsing,
             omschr_aard_verkregen_recht,
             fk_3avr_aand,
             aantekeningen
                )
AS
SELECT zakrecht.identificatie                                               AS zr_identif,
       zakrecht.begingeldigheid AS ingangsdatum_recht,
       COALESCE(tenaamstelling.aandeel_teller::text, '0') || '/'::text || COALESCE(tenaamstelling.aandeel_noemer::text, '0') AS aandeel,
       tenaamstelling.aandeel_teller                                        AS ar_teller,
       tenaamstelling.aandeel_noemer                                        AS ar_noemer,
       tenaamstelling.tennamevan                                            AS subject_identif,
       zakrecht.rustop                                                      AS koz_identif,
       (zakrecht.isbetrokkenbij is not NULL)::bool                          AS indic_betrokken_in_splitsing,
       zakrecht.aard                                                        AS omschr_aard_verkregen_recht,
       zakrecht.aard                                                        AS fk_3avr_aand,
       array_to_string(
               (SELECT array_agg(('id: ' || aantekening.identificatie || ', ' ||
                                  'aard: ' || COALESCE(aantekening.aard, '') || ', ' ||
                                  'begin: ' || COALESCE(aantekening.begingeldigheid::text, '') || ', ' ||
                                  'beschrijving: ' || COALESCE(aantekening.omschrijving, '') || ', ' ||
                                  'eind: ' || COALESCE(aantekening.einddatum::text, '') || ', ' ||
                                  'koz-id: ' || COALESCE(aantekening.aantekeningkadastraalobject, '') || ', ' ||
                                  'subject-id: ' || COALESCE(aantekening.betrokkenpersoon, '') || '; '))
                FROM recht aantekening
                WHERE aantekening.aantekeningkadastraalobject = zakrecht.rustop),
               ' & ')                                                       AS aantekeningen
FROM recht zakrecht
         JOIN recht tenaamstelling ON zakrecht.identificatie = tenaamstelling.van
WHERE zakrecht.identificatie like 'NL.IMKAD.ZakelijkRecht:%';

COMMENT ON VIEW vb_util_zk_recht IS
    'commentaar view vb_util_zk_recht:
    zakelijk recht met opgezocht aard recht en berekend aandeel

    beschikbare kolommen:
    * zr_identif: natuurlijke id van zakelijk recht
    * ingangsdatum_recht: -
    * aandeel: samenvoeging van teller en noemer (1/2),
    * ar_teller: teller van aandeel,
    * ar_noemer: noemer van aandeel,
    * subject_identif: natuurlijk id van subject (natuurlijk of niet natuurlijk) welke rechthebbende is,
    * koz_identif: natuurlijk id van kadastrale onroerende zaak (perceel of appratementsrecht) dat gekoppeld is,
    * indic_betrokken_in_splitsing: -,
    * omschr_aard_verkregen_recht: tekstuele omschrijving aard recht,
    * fk_3avr_aand: code aard recht,
    * aantekeningen: samenvoeging van alle aantekening op dit recht
    ';



CREATE MATERIALIZED VIEW mb_zr_rechth
            (
             objectid,
             zr_identif,
             ingangsdatum_recht,
             subject_identif,
             koz_identif,
             aandeel,
             omschr_aard_verkregen_recht,
             indic_betrokken_in_splitsing,
             aantekeningen,
             soort,
             geslachtsnaam,
             voorvoegsel,
             voornamen,
             aand_naamgebruik,
             geslachtsaand,
             naam,
             woonadres,
             geboortedatum,
             geboorteplaats,
             overlijdensdatum,
             bsn,
             organisatie_naam,
             rechtsvorm,
             statutaire_zetel,
             rsin,
             kvk_nummer
                )
AS
SELECT row_number() OVER () AS objectid,
       uzr.zr_identif,
       uzr.ingangsdatum_recht,
       uzr.subject_identif,
       uzr.koz_identif,
       uzr.aandeel,
       uzr.omschr_aard_verkregen_recht,
       uzr.indic_betrokken_in_splitsing,
       uzr.aantekeningen,
       persoon.soort,
       persoon.geslachtsnaam,
       persoon.voorvoegsel,
       persoon.voornamen,
       persoon.aand_naamgebruik,
       persoon.geslachtsaand,
       persoon.naam,
       persoon.woonadres,
       persoon.geboortedatum,
       persoon.geboorteplaats,
       persoon.overlijdensdatum,
       persoon.bsn,
       persoon.organisatie_naam,
       persoon.rechtsvorm,
       persoon.statutaire_zetel,
       persoon.rsin,
       persoon.kvk_nummer
FROM vb_util_zk_recht uzr
         JOIN mb_subject persoon ON uzr.subject_identif = persoon.subject_identif
WITH NO DATA;

CREATE UNIQUE INDEX mb_zr_rechth_objectid ON mb_zr_rechth USING btree (objectid);
CREATE INDEX mb_zr_rechth_identif ON mb_zr_rechth USING btree (zr_identif);

COMMENT ON MATERIALIZED VIEW mb_zr_rechth IS
    'commentaar view mb_zr_rechth:
    alle zakelijke rechten met rechthebbenden en referentie naar kadastraal onroerende zaak (perceel of appartementsrecht)

    beschikbare kolommen:
    * objectid: uniek id bruikbaar voor geoserver/arcgis,
    * zr_identif: natuurlijke id van zakelijk recht,
    * ingangsdatum_recht: -
    * subject_identif: natuurlijk id van subject (natuurlijk of niet natuurlijk) welke rechthebbende is,
    * koz_identif: natuurlijk id van kadastrale onroerende zaak (perceel of appratementsrecht) dat gekoppeld is,
    * aandeel: samenvoeging van teller en noemer (1/2),
    * omschr_aard_verkregen_recht: tekstuele omschrijving aard recht,
    * indic_betrokken_in_splitsing: -,
    * aantekeningen: samenvoeging van alle aantekeningen voor dit recht,
    * soort: soort subject zoals natuurlijk, niet-natuurlijk enz.
    * geslachtsnaam: -
    * voorvoegsel: -
    * voornamen: -
    * aand_naamgebruik: -
    * geslachtsaand: M/V/X
    * naam: samengestelde naam bruikbaar voor natuurlijke en niet-natuurlijke subjecten
    * woonadres: meegeleverd adres buiten BAG koppeling om
    * geboortedatum: -
    * geboorteplaats: -
    * overlijdensdatum: -
    * bsn: -
    * organisatie_naam: naam niet natuurlijk subject
    * rechtsvorm: -
    * statutaire_zetel: -
    * rsin: -
    * kvk_nummer: -';


CREATE MATERIALIZED VIEW mb_avg_zr_rechth
            (
             objectid,
             zr_identif,
             ingangsdatum_recht,
             subject_identif,
             koz_identif,
             aandeel,
             omschr_aard_verkregen_recht,
             indic_betrokken_in_splitsing,
             aantekeningen,
             soort,
             geslachtsnaam,
             voorvoegsel,
             voornamen,
             aand_naamgebruik,
             geslachtsaand,
             naam,
             woonadres,
             geboortedatum,
             geboorteplaats,
             overlijdensdatum,
             bsn,
             organisatie_naam,
             rechtsvorm,
             statutaire_zetel,
             rsin,
             kvk_nummer
                )
AS
SELECT row_number() OVER () AS objectid,
       uzr.zr_identif       as zr_identif,
       uzr.ingangsdatum_recht,
       uzr.subject_identif,
       uzr.koz_identif,
       uzr.aandeel,
       uzr.omschr_aard_verkregen_recht,
       uzr.indic_betrokken_in_splitsing,
       uzr.aantekeningen,
       avgpersoon.soort,
       avgpersoon.geslachtsnaam,
       avgpersoon.voorvoegsel,
       avgpersoon.voornamen,
       avgpersoon.aand_naamgebruik,
       avgpersoon.geslachtsaand,
       avgpersoon.naam,
       avgpersoon.woonadres,
       avgpersoon.geboortedatum,
       avgpersoon.geboorteplaats,
       avgpersoon.overlijdensdatum,
       avgpersoon.bsn,
       avgpersoon.organisatie_naam,
       avgpersoon.rechtsvorm,
       avgpersoon.statutaire_zetel,
       avgpersoon.rsin,
       avgpersoon.kvk_nummer
FROM vb_util_zk_recht uzr
         JOIN mb_avg_subject avgpersoon ON uzr.subject_identif = avgpersoon.subject_identif
WITH NO DATA;

CREATE UNIQUE INDEX mb_avg_zr_rechth_objectid ON mb_avg_zr_rechth USING btree (objectid);
CREATE INDEX mb_avg_zr_rechth_identif ON mb_avg_zr_rechth USING btree (zr_identif);

COMMENT ON MATERIALIZED VIEW mb_avg_zr_rechth IS
    'commentaar view mb_avg_zr_rechth:
    alle zakelijke rechten met voor avg geschoonde rechthebbenden en referentie naar kadastraal onroerende zaak (perceel of appartementsrecht)

    beschikbare kolommen:
    * objectid: uniek id bruikbaar voor geoserver/arcgis,
    * zr_identif: natuurlijke id van zakelijk recht,
    * ingangsdatum_recht: -,
    * subject_identif: natuurlijk id van subject (natuurlijk of niet natuurlijk) welke rechthebbende is,
    * koz_identif: natuurlijk id van kadastrale onroerende zaak (perceel of appratementsrecht) dat gekoppeld is,
    * aandeel: samenvoeging van teller en noemer (1/2),
    * omschr_aard_verkregen_recht: tekstuele omschrijving aard recht,
    * indic_betrokken_in_splitsing: -,
    * aantekeningen: samenvoeging van alle aantekeningen van dit recht
    * soort: soort subject zoals natuurlijk, niet-natuurlijk enz.
    * geslachtsnaam: NULL (avg)
    * voorvoegsel: NULL (avg)
    * voornamen: NULL (avg)
    * aand_naamgebruik: NULL (avg)
    * geslachtsaand:NULL (avg)
    * naam: gelijk aan organisatie_naam
    * woonadres: NULL (avg)
    * geboortedatum: NULL (avg)
    * geboorteplaats: NULL (avg)
    * overlijdensdatum: NULL (avg)
    * bsn: NULL (avg)
    * organisatie_naam: naam niet natuurlijk subject
    * rechtsvorm: -
    * statutaire_zetel: -
    * rsin: -
    * kvk_nummer: -';



CREATE MATERIALIZED VIEW mb_koz_rechth
            (
             objectid,
             koz_identif,
             begin_geldigheid,
             begin_geldigheid_datum,
             type,
             aanduiding,
             aanduiding2,
             sectie,
             perceelnummer,
             appartementsindex,
             gemeentecode,
             aand_soort_grootte,
             grootte_perceel,
             oppervlakte_geom,
             deelperceelnummer,
             omschr_deelperceel,
             verkoop_datum,
             aard_cultuur_onbebouwd,
             bedrag,
             koopjaar,
             meer_onroerendgoed,
             valutasoort,
             loc_omschr,
             zr_identif,
             ingangsdatum_recht,
             subject_identif,
             aandeel,
             omschr_aard_verkregen_recht,
             indic_betrokken_in_splitsing,
             soort,
             geslachtsnaam,
             voorvoegsel,
             voornamen,
             aand_naamgebruik,
             geslachtsaand,
             naam,
             woonadres,
             geboortedatum,
             geboorteplaats,
             overlijdensdatum,
             bsn,
             organisatie_naam,
             rechtsvorm,
             statutaire_zetel,
             rsin,
             kvk_nummer,
             aantekeningen,
             gemeente,
             woonplaats,
             straatnaam,
             huisnummer,
             huisletter,
             huisnummer_toev,
             postcode,
             lon,
             lat,
             begrenzing_perceel
                )
AS
SELECT row_number() OVER ()                                                     AS objectid,
       koz.koz_identif,
       koz.begin_geldigheid,
       koz.begin_geldigheid_datum,
       koz.type,
       COALESCE(koz.sectie, '') || ' ' || COALESCE(koz.perceelnummer::text, '') AS aanduiding,
       COALESCE(koz.gemeentecode, '') || ' ' || COALESCE(koz.sectie, '') || ' ' ||
       COALESCE(koz.perceelnummer::text, '') ||
       ' ' || COALESCE(koz.appartementsindex::text, '')                         AS aanduiding2,
       koz.sectie,
       koz.perceelnummer,
       koz.appartementsindex,
       koz.gemeentecode,
       koz.aand_soort_grootte,
       koz.grootte_perceel,
       koz.oppervlakte_geom,
       koz.deelperceelnummer,
       koz.omschr_deelperceel,
       koz.verkoop_datum,
       koz.aard_cultuur_onbebouwd,
       koz.bedrag,
       koz.koopjaar,
       koz.meer_onroerendgoed,
       koz.valutasoort,
       koz.loc_omschr,
       zrr.zr_identif,
       zrr.ingangsdatum_recht,
       zrr.subject_identif,
       zrr.aandeel,
       zrr.omschr_aard_verkregen_recht,
       zrr.indic_betrokken_in_splitsing,
       zrr.soort,
       zrr.geslachtsnaam,
       zrr.voorvoegsel,
       zrr.voornamen,
       zrr.aand_naamgebruik,
       zrr.geslachtsaand,
       zrr.naam,
       zrr.woonadres,
       zrr.geboortedatum,
       zrr.geboorteplaats,
       zrr.overlijdensdatum,
       zrr.bsn,
       zrr.organisatie_naam,
       zrr.rechtsvorm,
       zrr.statutaire_zetel,
       zrr.rsin,
       zrr.kvk_nummer,
       zrr.aantekeningen,
       koz.gemeente,
       koz.woonplaats,
       koz.straatnaam,
       koz.huisnummer,
       koz.huisletter,
       koz.huisnummer_toev,
       koz.postcode,
       koz.lon,
       koz.lat,
       koz.begrenzing_perceel
FROM mb_zr_rechth zrr
         RIGHT JOIN mb_kad_onrrnd_zk_adres koz ON zrr.koz_identif = koz.koz_identif
WITH NO DATA;

CREATE UNIQUE INDEX mb_koz_rechth_objectid ON mb_koz_rechth USING btree (objectid);
CREATE INDEX mb_koz_rechth_identif ON mb_koz_rechth USING btree (koz_identif);
CREATE INDEX mb_koz_rechth_begrenzing_perceel_idx ON mb_koz_rechth USING gist (begrenzing_perceel);

COMMENT ON MATERIALIZED VIEW mb_koz_rechth
    IS
        'commentaar view mb_koz_rechth:
        kadastrale percelen een appartementsrechten met rechten en rechthebbenden en objectid voor geoserver/arcgis
        beschikbare kolommen:
        * objectid: uniek id bruikbaar voor geoserver/arcgis,
        * koz_identif: natuurlijke id van perceel of appartementsrecht
        * begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
        * begin_geldigheid_datum: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
        * type: perceel of appartement,
        * aanduiding: sectie perceelnummer,
        * aanduiding2: kadgem sectie perceelnummer appartementsindex,
        * sectie: -,
        * perceelnummer: -,
        * appartementsindex: -,
        * gemeentecode: -,
        * aand_soort_grootte: -,
        * grootte_perceel: -,
        * oppervlakte_geom: oppervlakte berekend uit geometrie, hoort gelijk te zijn aan grootte_perceel,
        * deelperceelnummer: -,
        * omschr_deelperceel: -,
        * verkoop_datum: laatste datum gevonden akten van verkoop,
        * aard_cultuur_onbebouwd: -,
        * bedrag: -,
        * koopjaar: -,
        * meer_onroerendgoed: -,
        * valutasoort: -,
        * loc_omschr: adres buiten BAG om meegegeven,
        * zr_identif: natuurlijk id van zakelijk recht,
        * ingangsdatum_recht: - ,
        * subject_identif: natuurlijk id van rechthebbende,
        * aandeel: samenvoeging van teller en noemer (1/2),
        * omschr_aard_verkregen_recht: tekstuele omschrijving aard recht,
        * indic_betrokken_in_splitsing: -,
        * soort: soort subject zoals natuurlijk, niet-natuurlijk enz.
        * geslachtsnaam: -
        * voorvoegsel: -
        * voornamen: -
        * aand_naamgebruik: -
        * geslachtsaand: -
        * naam: samengestelde naam bruikbaar voor natuurlijke en niet-natuurlijke subjecten
        * woonadres: meegeleverd adres buiten BAG koppeling om
        * geboortedatum: -
        * geboorteplaats: -
        * overlijdensdatum: -
        * bsn: -
        * organisatie_naam: naam niet natuurlijk subject
        * rechtsvorm: -
        * statutaire_zetel: -
        * rsin: -
        * kvk_nummer: -
        * aantekeningen: samenvoeging van alle aantekeningen van dit recht,
        * gemeente: -,
        * woonplaats: -,
        * straatnaam: -,
        * huisnummer: -,
        * huisletter: -,
        * huisnummer_toev: -,
        * postcode: -,
        * lon: coordinaat als WSG84,
        * lon: coordinaat als WSG84,
        * begrenzing_perceel: perceelvlak
        ';



CREATE MATERIALIZED VIEW mb_avg_koz_rechth
            (
             objectid,
             koz_identif,
             begin_geldigheid,
             begin_geldigheid_datum,
             type,
             aanduiding,
             aanduiding2,
             sectie,
             perceelnummer,
             appartementsindex,
             gemeentecode,
             aand_soort_grootte,
             grootte_perceel,
             oppervlakte_geom,
             deelperceelnummer,
             omschr_deelperceel,
             verkoop_datum,
             aard_cultuur_onbebouwd,
             bedrag,
             koopjaar,
             meer_onroerendgoed,
             valutasoort,
             loc_omschr,
             zr_identif,
             ingangsdatum_recht,
             subject_identif,
             aandeel,
             omschr_aard_verkregen_recht,
             indic_betrokken_in_splitsing,
             soort,
             geslachtsnaam,
             voorvoegsel,
             voornamen,
             aand_naamgebruik,
             geslachtsaand,
             naam,
             woonadres,
             geboortedatum,
             geboorteplaats,
             overlijdensdatum,
             bsn,
             organisatie_naam,
             rechtsvorm,
             statutaire_zetel,
             rsin,
             kvk_nummer,
             aantekeningen,
             gemeente,
             woonplaats,
             straatnaam,
             huisnummer,
             huisletter,
             huisnummer_toev,
             postcode,
             lon,
             lat,
             begrenzing_perceel
                )
AS
SELECT row_number() OVER ()                                                     AS objectid,
       koz.koz_identif,
       koz.begin_geldigheid,
       koz.begin_geldigheid_datum,
       koz.type,
       COALESCE(koz.sectie, '') || ' ' || COALESCE(koz.perceelnummer::text, '') AS aanduiding,
       COALESCE(koz.gemeentecode, '') || ' ' || COALESCE(koz.sectie, '') || ' ' ||
       COALESCE(koz.perceelnummer::text, '') ||
       ' ' || COALESCE(koz.appartementsindex::text, '')                         AS aanduiding2,
       koz.sectie,
       koz.perceelnummer,
       koz.appartementsindex,
       koz.gemeentecode,
       koz.aand_soort_grootte,
       koz.grootte_perceel,
       koz.oppervlakte_geom,
       koz.deelperceelnummer,
       koz.omschr_deelperceel,
       koz.verkoop_datum,
       koz.aard_cultuur_onbebouwd,
       koz.bedrag,
       koz.koopjaar,
       koz.meer_onroerendgoed,
       koz.valutasoort,
       koz.loc_omschr,
       zrr.zr_identif,
       zrr.ingangsdatum_recht,
       zrr.subject_identif,
       zrr.aandeel,
       zrr.omschr_aard_verkregen_recht,
       zrr.indic_betrokken_in_splitsing,
       zrr.soort,
       zrr.geslachtsnaam,
       zrr.voorvoegsel,
       zrr.voornamen,
       zrr.aand_naamgebruik,
       zrr.geslachtsaand,
       zrr.naam,
       zrr.woonadres,
       zrr.geboortedatum,
       zrr.geboorteplaats,
       zrr.overlijdensdatum,
       zrr.bsn,
       zrr.organisatie_naam,
       zrr.rechtsvorm,
       zrr.statutaire_zetel,
       zrr.rsin,
       zrr.kvk_nummer,
       zrr.aantekeningen,
       koz.gemeente,
       koz.woonplaats,
       koz.straatnaam,
       koz.huisnummer,
       koz.huisletter,
       koz.huisnummer_toev,
       koz.postcode,
       koz.lon,
       koz.lat,
       koz.begrenzing_perceel
FROM mb_avg_zr_rechth zrr
         RIGHT JOIN mb_kad_onrrnd_zk_adres koz ON zrr.koz_identif = koz.koz_identif
WITH NO DATA;

CREATE UNIQUE INDEX mb_avg_koz_rechth_objectid ON mb_avg_koz_rechth USING btree (objectid);
CREATE INDEX mb_avg_koz_rechth_identif ON mb_avg_koz_rechth USING btree (koz_identif);
CREATE INDEX mb_avg_koz_rechth_begrenzing_perceel_idx ON mb_avg_koz_rechth USING gist (begrenzing_perceel);

COMMENT ON MATERIALIZED VIEW mb_avg_koz_rechth IS
    'commentaar view mb_avg_koz_rechth:
    kadastrale percelen een appartementsrechten met rechten en rechthebbenden geschoond voor avg en objectid voor geoserver/arcgis
    beschikbare kolommen:
    * objectid: uniek id bruikbaar voor geoserver/arcgis,
    * koz_identif: natuurlijke id van perceel of appartementsrecht
    * begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
    * begin_geldigheid_datum: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
    * type: perceel of appartement,
    * aanduiding: sectie perceelnummer,
    * aanduiding2: kadgem sectie perceelnummer appartementsindex,
    * sectie: -,
    * perceelnummer: -,
    * appartementsindex: -,
    * gemeentecode: -,
    * aand_soort_grootte: -,
    * grootte_perceel: -,
    * oppervlakte_geom: oppervlakte berekend uit geometrie, hoort gelijk te zijn aan grootte_perceel,
    * deelperceelnummer: -,
    * omschr_deelperceel: -,
    * verkoop_datum: laatste datum gevonden akten van verkoop,
    * aard_cultuur_onbebouwd: -,
    * bedrag: -,
    * koopjaar: -,
    * meer_onroerendgoed: -,
    * valutasoort: -,
    * loc_omschr: adres buiten BAG om meegegeven,
    * zr_identif: natuurlijk id van zakelijk recht,
    * ingangsdatum_recht: - ,
    * subject_identif: natuurlijk id van rechthebbende,
    * aandeel: samenvoeging van teller en noemer (1/2),
    * omschr_aard_verkregen_recht: tekstuele omschrijving aard recht,
    * indic_betrokken_in_splitsing: -,
    * soort: soort subject zoals natuurlijk, niet-natuurlijk enz.
    * geslachtsnaam: NULL (avg)
    * voorvoegsel: NULL (avg)
    * voornamen: NULL (avg)
    * aand_naamgebruik: NULL (avg)
    * geslachtsaand:NULL (avg)
    * naam: gelijk aan organisatie_naam
    * woonadres: NULL (avg)
    * geboortedatum: NULL (avg)
    * geboorteplaats: NULL (avg)
    * overlijdensdatum: NULL (avg)
    * bsn: NULL (avg)
    * organisatie_naam: naam niet natuurlijk subject
    * rechtsvorm: -
    * statutaire_zetel: -
    * rsin: -
    * kvk_nummer: -
    * aantekeningen: samenvoeging van alle aantekeningen van dit recht,
    * gemeente: -,
    * woonplaats: -,
    * straatnaam: -,
    * huisnummer: -,
    * huisletter: -,
    * huisnummer_toev: -,
    * postcode: -,
    * lon: coordinaat als WSG84,
    * lat: coordinaat als WSG84,
    * begrenzing_perceel: perceelvlak
    ';



CREATE MATERIALIZED VIEW mb_kad_onrrnd_zk_archief
            (
             objectid,
             koz_identif,
             begin_geldigheid,
             begin_geldigheid_datum,
             eind_geldigheid,
             eind_geldigheid_datum,
             type,
             aanduiding,
             aanduiding2,
             sectie,
             perceelnummer,
             appartementsindex,
             gemeentecode,
             aand_soort_grootte,
             grootte_perceel,
             deelperceelnummer,
             omschr_deelperceel,
             aard_cultuur_onbebouwd,
             bedrag,
             koopjaar,
             meer_onroerendgoed,
             valutasoort,
             loc_omschr,
             overgegaan_in,
             begrenzing_perceel
                )
AS
SELECT row_number() OVER ()                                 AS objectid,
       qry.identificatie                                    as koz_identif,
       koza.begingeldigheid::text                           AS begin_geldigheid,
       koza.begingeldigheid                                 AS begin_geldigheid_datum,
       koza.eindegeldigheid::text                           AS eind_geldigheid,
       koza.eindegeldigheid                                 AS eind_geldigheid_datum,
       qry.type,
       COALESCE(koza.sectie, '') || ' ' ||
       COALESCE(koza.perceelnummer::text, '')               AS aanduiding,
       COALESCE(koza.akrkadastralegemeente, '') || ' ' || COALESCE(koza.sectie, '') || ' ' ||
       COALESCE(koza.perceelnummer::text, '') || ' ' ||
       COALESCE(koza.appartementsrechtvolgnummer::text, '') AS aanduiding2,
       koza.sectie                                          AS sectie,
       koza.perceelnummer                                   AS perceelnummer,
       koza.appartementsrechtvolgnummer                     AS appartementsindex,
       koza.akrkadastralegemeente                           AS gemeentecode,
       qry.soortgrootte,
       qry.kadastralegrootte,
       NULL                                                 AS deelperceelnummer,
       NULL                                                 AS omschr_deelperceel,
       koza.aard_cultuur_onbebouwd                          AS aard_cultuur_onbebouwd,
       koza.koopsom_bedrag                                  AS bedrag,
       koza.koopsom_koopjaar                                AS koopjaar,
       koza.koopsom_indicatiemeerobjecten                   AS meer_onroerendgoed,
       koza.koopsom_valuta                                  AS valutasoort,
       -- TODO BRK adres?
       NULL                                                 AS loc_omschr,
       kozhr.onroerendezaak                                 AS overgegaan_in,
       qry.begrenzing_perceel
FROM (SELECT p_archief.identificatie,
             'perceel'
                 AS type,
             p_archief.begingeldigheid,
             p_archief.soortgrootte,
             p_archief.kadastralegrootte,
             p_archief.begrenzing_perceel,
             p_archief.plaatscoordinaten
      FROM perceel_archief p_archief
      UNION ALL
      SELECT a_archief.identificatie,
             'appartement'
                 AS type,
             a_archief.begingeldigheid,
             NULL
                 AS soortgrootte,
             NULL
                 AS kadastralegrootte,
             NULL
                 AS begrenzing_perceel,
             NULL
                 AS plaatscoordinaten
      FROM appartementsrecht_archief a_archief) qry
         JOIN onroerendezaak_archief koza
              ON koza.identificatie = qry.identificatie AND qry.begingeldigheid = koza.begingeldigheid
         JOIN (SELECT ikoza.identificatie, MAX(ikoza.begingeldigheid) bdate
               FROM onroerendezaak_archief ikoza
               GROUP BY ikoza.identificatie) nqry
              ON nqry.identificatie = koza.identificatie AND nqry.bdate = koza.begingeldigheid
         LEFT JOIN onroerendezaakfiliatie_archief kozhr ON kozhr.betreft = koza.identificatie
WITH NO DATA;

CREATE UNIQUE INDEX mb_kad_onrrnd_zk_archief_objectid ON mb_kad_onrrnd_zk_archief USING btree (objectid);
CREATE INDEX mb_kad_onrrnd_zk_archief_identif ON mb_kad_onrrnd_zk_archief USING btree (koz_identif);
CREATE INDEX mb_kad_onrrnd_zk_archief_begrenzing_perceel_idx ON mb_kad_onrrnd_zk_archief USING gist (begrenzing_perceel);
CREATE INDEX mb_kad_onrrnd_zk_archief_overgegaan_in_idx ON mb_kad_onrrnd_zk_archief USING btree (overgegaan_in);

COMMENT ON MATERIALIZED VIEW mb_kad_onrrnd_zk_archief IS
    'commentaar view mb_kad_onrrnd_zk_archief:
    Nieuwste gearchiveerde versie van ieder kadastrale onroerende zaak (perceel en appartementsrecht) met objectid voor geoserver/arcgis en historische relatie

    beschikbare kolommen:
    * objectid: uniek id bruikbaar voor geoserver/arcgis,
    * koz_identif: natuurlijke id van perceel of appartementsrecht
    * begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
    * begin_geldigheid_datum: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
    * eind_geldigheid: datum wanneer dit object ongeldig geworden is,
    * eind_geldigheid_datum: datum wanneer dit object ongeldig geworden is,
    * benoemdobj_identif: koppeling met BAG object,
    * type: perceel of appartement,
    * sectie: -,
    * aanduiding: sectie perceelnummer,
    * aanduiding2: kadgem sectie perceelnummer appartementsindex,
    * perceelnummer: -,
    * appartementsindex: -,
    * gemeentecode: -,
    * aand_soort_grootte: -,
    * grootte_perceel: -,
    * deelperceelnummer: -,
    * omschr_deelperceel: -,
    * aard_cultuur_onbebouwd: -,
    * bedrag: -,
    * koopjaar: -,
    * meer_onroerendgoed: -,
    * valutasoort: -,
    * loc_omschr: adres buiten BAG om meegegeven,
    * overgegaan_in: natuurlijk id van kadastrale onroerende zaak waar dit object in is overgegaan,
    * begrenzing_perceel: perceelvlak
    ';


