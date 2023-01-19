SET SCHEMA 'brk';
SET search_path = brk,public;

-- TODO evt nog toevoegen:
--      p.beschikkingsbevoegdheid
--      p.indicatieniettoonbarediakriet
--      p.postlocatie
--      np.indicatieoverleden
--      np.indicatieafschermingpersoonsgegevens
--      np.adellijketitelofpredicaat
--      np.landwaarnaarvertrokken
--      np.geboorteland
--      np.indicatiegeheim
--      np.partnergeslachtsnaam
--      np.partnervoornamen
--      np.partnervoorvoegselsgeslachtsnaam
--      a.postcode
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
SELECT (row_number() OVER ())::INTEGER                    AS objectid,
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
SELECT (row_number() OVER ())::INTEGER                                                                 AS objectid,
       o.identificatie                                                                                 AS koz_identif,
       o.begingeldigheid::text                                                                         AS begin_geldigheid,
       o.begingeldigheid                                                                               AS begin_geldigheid_datum,
       -- TODO koppling BAG
       null                                                                                            AS benoemdobj_identif,
       qry.type                                                                                        AS type,
       COALESCE(o.sectie, '') || ' ' || COALESCE(o.perceelnummer::text, '')                            AS aanduiding,
       COALESCE(o.akrkadastralegemeente, '') || ' ' || COALESCE(o.sectie, '') || ' ' ||
       COALESCE(o.perceelnummer::text, '') || ' ' || COALESCE(o.appartementsrechtvolgnummer::text, '') AS aanduiding2,
       o.sectie                                                                                        AS sectie,
       o.perceelnummer                                                                                 AS perceelnummer,
       o.appartementsrechtvolgnummer                                                                   AS appartementsindex,
       o.akrkadastralegemeentecode                                                                     AS gemeentecode,
       qry.soortgrootte                                                                                AS aand_soort_grootte,
       qry.kadastralegrootte                                                                           AS grootte_perceel,
       st_area(qry.begrenzing_perceel)                                                            AS oppervlakte_geom,
       NULL                                                                                            AS deelperceelnummer,
       NULL                                                                                            AS omschr_deelperceel,
       -- TODO
       null                                                                                            AS verkoop_datum,
       o.aard_cultuur_onbebouwd                                                                        AS aard_cultuur_onbebouwd,
       o.koopsom_bedrag                                                                                AS bedrag,
       o.koopsom_koopjaar                                                                              AS koopjaar,
       o.koopsom_indicatiemeerobjecten                                                                 AS meer_onroerendgoed,
       'EURO'                                                                                          AS valutasoort,
       -- TODO BRK adres?
       null                                                                                            AS loc_omschr,
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
                WHERE r.aantekeningkadastraalobject = o.identificatie), ' & ')                         as aantekeningen,
       -- TODO koppling BAG
       null                                                                                            AS na_identif,
       -- TODO koppling BAG
       null                                                                                            AS na_status,
       -- TODO koppling BAG
       null                                                                                            AS gemeente,
       -- TODO koppling BAG
       null                                                                                            AS woonplaats,
       -- TODO koppling BAG
       null                                                                                            AS straatnaam,
       -- TODO koppling BAG
       null                                                                                            AS huisnummer,
       -- TODO koppling BAG
       null                                                                                            AS huisletter,
       -- TODO koppling BAG
       null                                                                                            AS huisnummer_toev,
       -- TODO koppling BAG
       null                                                                                            AS postcode,
       -- TODO koppling BAG
       null                                                                                            AS gebruiksdoelen,
       -- TODO koppling BAG
       null                                                                                            AS oppervlakte_obj,
       st_x(st_transform(qry.plaatscoordinaten, 4326))                                                 as lon,
       st_y(st_transform(qry.plaatscoordinaten, 4326))                                                 as lat,
       qry.begrenzing_perceel
                                                                                                       AS begrenzing_perceel
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
      FROM appartementsrecht a
         --             ((mb_util_app_re_kad_perceel v
--         JOIN
--             kad_perceel kp
--         ON
--             (((
--                         v.perceel_identif)::NUMERIC = kp.sc_kad_identif)))
--         JOIN
--             app_re ar
--         ON
--             (((
--                         v.app_re_identif)::NUMERIC = ar.sc_kad_identif)))
     ) qry
         JOIN onroerendezaak o ON qry.identificatie = o.identificatie
;

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

