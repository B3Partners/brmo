ALTER SESSION SET NLS_LENGTH_SEMANTICS='CHAR';
SET DEFINE OFF;
WHENEVER SQLERROR EXIT sql.sqlcode;

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
            BUILD DEFERRED
    REFRESH ON DEMAND
AS
SELECT CAST(ROWNUM AS INTEGER)                            AS objectid,
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
       REPLACE(COALESCE(a.openbareruimtenaam, '') || ' ' || COALESCE(TO_CHAR(a.huisnummer), '') ||
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
;

CREATE UNIQUE INDEX mb_subject_objectid ON mb_subject (objectid ASC);
CREATE INDEX mb_subject_identif ON mb_subject (subject_identif ASC);

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
            BUILD DEFERRED
    REFRESH ON DEMAND
AS
SELECT s.objectid,
       s.subject_identif                  AS subject_identif,
       s.soort,
       CAST(NULL AS CHARACTER VARYING(1)) AS geslachtsnaam,
       CAST(NULL AS CHARACTER VARYING(1)) AS voorvoegsel,
       CAST(NULL AS CHARACTER VARYING(1)) AS voornamen,
       CAST(NULL AS CHARACTER VARYING(1)) AS aand_naamgebruik,
       CAST(NULL AS CHARACTER VARYING(1)) AS geslachtsaand,
       s.organisatie_naam                 AS naam,
       CAST(NULL AS CHARACTER VARYING(1)) AS woonadres,
       CAST(NULL AS CHARACTER VARYING(1)) AS geboortedatum,
       CAST(NULL AS CHARACTER VARYING(1)) AS geboorteplaats,
       CAST(NULL AS CHARACTER VARYING(1)) AS overlijdensdatum,
       CAST(NULL AS CHARACTER VARYING(1)) AS bsn,
       s.organisatie_naam,
       s.rechtsvorm,
       s.statutaire_zetel,
       s.rsin,
       s.kvk_nummer
FROM mb_subject s;

CREATE UNIQUE INDEX mb_avg_subject_objectid ON mb_avg_subject (objectid ASC);
CREATE INDEX mb_avg_subject_identif ON mb_avg_subject (subject_identif ASC);

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
             begrenzing_perceel
                )
            BUILD DEFERRED
    REFRESH ON DEMAND
AS
SELECT CAST(ROWNUM AS INTEGER)                                                 AS objectid,
       qry.identificatie                                                       AS koz_identif,
       TO_CHAR(o.begingeldigheid)                                              AS begin_geldigheid,
       o.begingeldigheid                                                       AS begin_geldigheid_datum,
       -- koppeling met BAG
       CAST(NULL AS VARCHAR2(255 CHAR))                                        AS benoemdobj_identif,
       qry.type                                                                AS type,
       COALESCE(o.sectie, '') || ' ' || COALESCE(TO_CHAR(o.perceelnummer), '') AS aanduiding,
       COALESCE(o.akrkadastralegemeente, '') || ' ' || COALESCE(o.sectie, '') || ' ' ||
       COALESCE(TO_CHAR(o.perceelnummer), '') ||
       ' ' || COALESCE(TO_CHAR(o.appartementsrechtvolgnummer), '')             AS aanduiding2,
       o.sectie                                                                AS sectie,
       o.perceelnummer                                                         AS perceelnummer,
       o.appartementsrechtvolgnummer                                           AS appartementsindex,
       o.akrkadastralegemeente                                                 AS gemeentecode,
       qry.soortgrootte                                                        AS aand_soort_grootte,
       qry.kadastralegrootte                                                   AS grootte_perceel,
       SDO_GEOM.SDO_AREA(qry.begrenzing_perceel, 0.1)                          AS oppervlakte_geom,
       -- bestaat niet
       CAST(NULL AS VARCHAR2(4 CHAR))                                          AS deelperceelnummer,
       -- bestaat niet
       CAST(NULL AS VARCHAR2(1120 CHAR))                                       AS omschr_deelperceel,
       -- TODO verkoop datum uit stukdeel via recht
       CAST(NULL AS DATE)                                                      AS verkoop_datum,
       o.aard_cultuur_onbebouwd                                                AS aard_cultuur_onbebouwd,
       o.koopsom_bedrag                                                        AS bedrag,
       o.koopsom_koopjaar                                                      AS koopjaar,
       o.koopsom_indicatiemeerobjecten                                         AS meer_onroerendgoed,
       o.koopsom_valuta                                                        AS valutasoort,
       -- TODO BRK adres?
       CAST(NULL AS VARCHAR2(255 CHAR))                                        AS loc_omschr,
       aantekeningen.aantekeningen                                             AS aantekeningen,
       -- koppeling met BAG
       CAST(NULL AS VARCHAR2(4 CHAR))                                          AS na_identif,
       -- koppeling met BAG
       CAST(NULL AS VARCHAR2(80 CHAR))                                         AS na_status,
       -- koppeling met BAG
       CAST(NULL AS VARCHAR2(40 CHAR))                                         AS gemeente,
       -- koppeling met BAG
       CAST(NULL AS VARCHAR2(80 CHAR))                                         AS woonplaats,
       -- koppeling met BAG
       CAST(NULL AS VARCHAR2(80 CHAR))                                         AS straatnaam,
       -- koppeling met BAG
       CAST(NULL AS NUMBER(4))                                                 AS huisnummer,
       -- koppeling met BAG
       CAST(NULL AS VARCHAR2(1 CHAR))                                          AS huisletter,
       -- koppeling met BAG
       CAST(NULL AS VARCHAR2(4 CHAR))                                          AS huisnummer_toev,
       -- koppeling met BAG
       CAST(NULL AS VARCHAR2(6 CHAR))                                          AS postcode,
       -- koppeling met BAG
       CAST(NULL AS VARCHAR2(80 CHAR))                                         AS gebruiksdoelen,
       -- koppeling met BAG
       CAST(NULL AS NUMBER(4))                                                 AS oppervlakte_obj,
       SDO_CS.TRANSFORM((qry.plaatscoordinaten), 4326).SDO_POINT.X             AS lon,
       SDO_CS.TRANSFORM((qry.plaatscoordinaten), 4326).SDO_POINT.Y             AS lat,
       qry.begrenzing_perceel                                                  AS begrenzing_perceel
FROM (SELECT p.identificatie      AS identificatie,
             'perceel'            AS type,
             p.soortgrootte       AS soortgrootte,
             p.kadastralegrootte  AS kadastralegrootte,
             p.begrenzing_perceel AS begrenzing_perceel,
             p.plaatscoordinaten  AS plaatscoordinaten
      FROM perceel p
      UNION ALL
      SELECT a.identificatie             AS identificatie,
             'appartement'               AS type,
             CAST(NULL AS VARCHAR2(100)) AS soortgrootte,
             CAST(NULL AS NUMBER)        AS kadastralegrootte,
             CAST(NULL AS SDO_GEOMETRY)  AS begrenzing_perceel,
             CAST(NULL AS SDO_GEOMETRY)  AS plaatscoordinaten
      FROM appartementsrecht a) qry
         JOIN onroerendezaak o ON qry.identificatie = o.identificatie
         LEFT JOIN(SELECT r.aantekeningkadastraalobject,
                          LISTAGG(
                                      'id: ' || COALESCE(r.identificatie, '') || ', '
                                      || 'aard: ' || COALESCE(r.aard, '') || ', '
                                      || 'begin: ' || COALESCE(TO_CHAR(r.begingeldigheid), '') || ', '
                                      || 'beschrijving: ' || COALESCE(r.omschrijving, '') || ', '
                                      || 'eind: ' || COALESCE(TO_CHAR(r.einddatum), '') || ', '
                                      || 'koz-id: ' || COALESCE(r.aantekeningkadastraalobject, '') || ', '
                                      || 'subject-id: ' || COALESCE(r.betrokkenpersoon, '') || '; ', ' & ' ON OVERFLOW
                                      TRUNCATE WITH COUNT)
                                      WITHIN GROUP ( ORDER BY r.aantekeningkadastraalobject ) AS aantekeningen
                   FROM recht r
                   GROUP BY r.aantekeningkadastraalobject) aantekeningen
                  ON o.identificatie = aantekeningen.aantekeningkadastraalobject
;

CREATE UNIQUE INDEX mb_kad_onrrnd_zk_adres_objidx ON mb_kad_onrrnd_zk_adres (objectid ASC);
CREATE INDEX mb_kad_onrrnd_zk_adres_identif ON mb_kad_onrrnd_zk_adres (koz_identif ASC);
INSERT INTO user_sdo_geom_metadata
VALUES ('MB_KAD_ONRRND_ZK_ADRES', 'BEGRENZING_PERCEEL',
        MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
                            MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
CREATE INDEX mb_kad_onrrnd_zk_adr_bgrgpidx ON mb_kad_onrrnd_zk_adres (begrenzing_perceel) INDEXTYPE IS MDSYS.SPATIAL_INDEX;

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
    * begrenzing_perceel: perceelvlak';