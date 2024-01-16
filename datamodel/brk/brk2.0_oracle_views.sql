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
               COALESCE(a.postcode, '') || ' ' ||
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
         LEFT JOIN adres a on p.woonlocatie = a.identificatie;

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
    * kvk_nummer: -';


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
    * kvk_nummer: -';



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
       o.identificatie                                                         AS koz_identif,
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
                  ON o.identificatie = aantekeningen.aantekeningkadastraalobject;

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
            BUILD DEFERRED
    REFRESH ON DEMAND
AS
SELECT CAST(ROWNUM AS INTEGER)                                                 AS objectid,
       o.identificatie                                                         AS koz_identif,
       TO_CHAR(o.begingeldigheid)                                              AS begin_geldigheid,
       o.begingeldigheid                                                       AS begin_geldigheid_datum,
       qry.type                                                                AS type,
       COALESCE(o.sectie, '') || ' ' || COALESCE(TO_CHAR(o.perceelnummer), '') AS aanduiding,
       COALESCE(o.akrkadastralegemeente, '') || ' ' || COALESCE(o.sectie, '') || ' ' ||
       COALESCE(TO_CHAR(o.perceelnummer), '')                                  AS aanduiding2,
       o.sectie                                                                AS sectie,
       o.perceelnummer                                                         AS perceelnummer,
       o.akrkadastralegemeente                                                 AS gemeentecode,
       qry.soortgrootte                                                        AS aand_soort_grootte,
       qry.kadastralegrootte                                                   AS grootte_perceel,
       SDO_GEOM.SDO_AREA(qry.begrenzing_perceel, 0.1)                          AS oppervlakte_geom,
       -- TODO verkoop datum uit stukdeel via recht
       CAST(NULL AS DATE)                                                      AS verkoop_datum,
       o.aard_cultuur_onbebouwd                                                AS aard_cultuur_onbebouwd,
       o.koopsom_bedrag                                                        AS bedrag,
       o.koopsom_koopjaar                                                      AS koopjaar,
       o.koopsom_indicatiemeerobjecten                                         AS meer_onroerendgoed,
       o.koopsom_valuta                                                        AS valutasoort,
       aantekeningen.aantekeningen                                             AS aantekeningen,
       SDO_CS.TRANSFORM((qry.plaatscoordinaten), 4326).SDO_POINT.X             AS lon,
       SDO_CS.TRANSFORM((qry.plaatscoordinaten), 4326).SDO_POINT.Y             AS lat,
       qry.begrenzing_perceel                                                  AS begrenzing_perceel
FROM (SELECT p.identificatie      AS identificatie,
             'perceel'            AS type,
             p.soortgrootte       AS soortgrootte,
             p.kadastralegrootte  AS kadastralegrootte,
             p.begrenzing_perceel AS begrenzing_perceel,
             p.plaatscoordinaten  AS plaatscoordinaten
      FROM perceel p) qry
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
                  ON o.identificatie = aantekeningen.aantekeningkadastraalobject;

CREATE UNIQUE INDEX mb_percelenkaart_objectid ON mb_percelenkaart (objectid ASC);
CREATE INDEX mb_percelenkaart_identif ON mb_percelenkaart (koz_identif ASC);
INSERT INTO user_sdo_geom_metadata
VALUES ('MB_PERCELENKAART', 'BEGRENZING_PERCEEL',
        MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
                            MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
CREATE INDEX mb_percelenkaart_bgrgpidx ON mb_percelenkaart (begrenzing_perceel) INDEXTYPE IS MDSYS.SPATIAL_INDEX;

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
    * begrenzing_perceel: perceelvlak';

-- BRMO-336: toevoegen van zakelijke rechten die het eigendomsrecht belasten
CREATE OR REPLACE VIEW vb_util_zk_recht_op_koz 
            (
             identificatie,
             rustop_zak_recht
            )
AS 
SELECT  qry.identificatie,
        qry.rustop_zak_recht
FROM    ( 
            SELECT ribm.isbelastmet                                     AS identificatie,
                   zakrecht.rustop                                      AS rustop_zak_recht
            FROM recht_isbelastmet ribm

            LEFT JOIN recht zakrecht ON ribm.zakelijkrecht = zakrecht.identificatie

            UNION ALL
            SELECT zakrecht.identificatie,
                   zakrecht.rustop                                      AS rustop_zak_recht
            FROM recht zakrecht) qry
WHERE SUBSTR(qry.identificatie, 1, INSTR(qry.identificatie, ':') - 1) = 'NL.IMKAD.ZakelijkRecht';


CREATE OR REPLACE VIEW vb_util_zk_recht
            (
             zr_identif,
             ingangsdatum_recht,
             aandeel,
             ar_teller,
             ar_noemer,
             subject_identif,
             mandeligheid_identif,
             koz_identif,
             indic_betrokken_in_splitsing,
             omschr_aard_verkregenr_recht,
             fk_3avr_aand,
             aantekeningen
                )
AS
SELECT zakrecht.identificatie                                            AS zr_identif,
       zakrecht.begingeldigheid                                          AS ingangsdatum_recht,
       COALESCE(TO_CHAR(tenaamstelling.aandeel_teller), '0') || '/' ||
       COALESCE(TO_CHAR(tenaamstelling.aandeel_noemer), '0')             AS aandeel,
       tenaamstelling.aandeel_teller                                     AS ar_teller,
       tenaamstelling.aandeel_noemer                                     AS ar_noemer,
       -- BRMO-339: samenvoegen van de tennamevan (tenaamstelling) en de heeftverenigingvaneigenaren, zodat de grondpercelen zichtbaar zijn
       -- BRMO-340: samenvoegen van de tennamevan (tenaamstelling) op de zakelijke rechten die bestemd zijn tot een mandeligheid
       COALESCE(tenaamstelling.tennamevan, '') || COALESCE(vve.heeftverenigingvaneigenaren, '') || 
       COALESCE(tenaamstelling2.tennamevan, '')                          AS subject_identif,
        -- BRMO-340: toevoegen van mandeligheidsidentificatie, zodat het duidelijk is dat het een mandelige zaak betreft.                                                               
       mandeligheid.identificatie                                        AS mandeligheid_identif,
       zakrecht.rustop                                                   AS koz_identif,
       CASE WHEN (zakrecht.isbetrokkenbij is not NULL) THEN 1 ELSE 0 END AS indic_betrokken_in_splitsing,
       zakrecht.aard                                                     AS omschr_aard_verkregen_recht,
       zakrecht.aard                                                     AS fk_3avr_aand,
       (SELECT LISTAGG(
                           'id: ' || COALESCE(aantekening.identificatie, '') || ', '
                           || 'aard: ' || COALESCE(aantekening.aard, '') || ', '
                           || 'begin: ' || COALESCE(TO_CHAR(aantekening.begingeldigheid), '') || ', '
                           || 'beschrijving: ' || COALESCE(aantekening.omschrijving, '') || ', '
                           || 'eind: ' || COALESCE(TO_CHAR(aantekening.einddatum), '') || ', '
                           || 'koz-id: ' || COALESCE(aantekening.aantekeningkadastraalobject, '') || ', '
                           || 'subject-id: ' || COALESCE(aantekening.betrokkenpersoon, '') || '; ', ' & ' ON OVERFLOW
                           TRUNCATE WITH COUNT)
                           WITHIN GROUP ( ORDER BY aantekening.aantekeningkadastraalobject ) AS aantekeningen
        FROM recht aantekening
        WHERE aantekening.aantekeningkadastraalobject = zakrecht.rustop) AS aantekeningen
FROM recht zakrecht
        -- tenaamstelling
        LEFT JOIN recht tenaamstelling ON zakrecht.identificatie = tenaamstelling.van
        -- vereniging van eigenaren
        LEFT JOIN recht vve ON zakrecht.isbetrokkenbij = vve.identificatie
        LEFT JOIN vb_util_zk_recht_op_koz vuzrok ON zakrecht.identificatie = vuzrok.identificatie
        -- mandeligheid
        LEFT JOIN recht mandeligheid ON zakrecht.isbestemdtot = mandeligheid.identificatie
        LEFT JOIN vb_util_zk_recht_op_koz vuzrok2 ON mandeligheid.heefthoofdzaak = vuzrok2.rustop_zak_recht
        LEFT JOIN recht tenaamstelling2 ON vuzrok2.identificatie = tenaamstelling2.van
WHERE SUBSTR(zakrecht.identificatie, 1, INSTR(zakrecht.identificatie, ':') - 1) = 'NL.IMKAD.ZakelijkRecht';

COMMENT ON TABLE vb_util_zk_recht IS
    'commentaar view vb_util_zk_recht:
    zakelijk recht met opgezocht aard recht en berekend aandeel
        beschikbare kolommen:
    * zr_identif: natuurlijke id van zakelijk recht
    * ingangsdatum_recht: -
    * aandeel: samenvoeging van teller en noemer (1/2),
    * ar_teller: teller van aandeel,
    * ar_noemer: noemer van aandeel,
    * subject_identif: natuurlijk id van subject (natuurlijk of niet natuurlijk) welke rechthebbende is,
    * mandeligheid_identif: identificatie van een mandeligheid, een gemeenschappelijk eigendom van een onroerende zaak,
    * koz_identif: natuurlijk id van kadastrale onroerende zaak (perceel of appratementsrecht) dat gekoppeld is,
    * indic_betrokken_in_splitsing: -,
    * omschr_aard_verkregen_recht: tekstuele omschrijving aard recht,
    * fk_3avr_aand: code aard recht,
    * aantekeningen: samenvoeging van alle aantekening op dit recht';


CREATE MATERIALIZED VIEW mb_zr_rechth
            (
             objectid,
             zr_identif,
             ingangsdatum_recht,
             subject_identif,
             mandeligheid_identif,
             koz_identif,
             aandeel,
             omschr_aard_verkregenr_recht,
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
            BUILD DEFERRED
    REFRESH ON DEMAND
AS
SELECT CAST(ROWNUM AS INTEGER) AS objectid,
       uzr.zr_identif          as zr_identif,
       uzr.ingangsdatum_recht,
       uzr.subject_identif,
       uzr.mandeligheid_identif,
       uzr.koz_identif,
       uzr.aandeel,
       uzr.omschr_aard_verkregenr_recht,
       uzr.indic_betrokken_in_splitsing,
       uzr.aantekeningen,
       vs.soort,
       vs.geslachtsnaam,
       vs.voorvoegsel,
       vs.voornamen,
       vs.aand_naamgebruik,
       vs.geslachtsaand,
       vs.naam,
       vs.woonadres,
       vs.geboortedatum,
       vs.geboorteplaats,
       vs.overlijdensdatum,
       vs.bsn,
       vs.organisatie_naam,
       vs.rechtsvorm,
       vs.statutaire_zetel,
       vs.rsin,
       vs.kvk_nummer
FROM vb_util_zk_recht uzr
         JOIN
     mb_subject vs
     ON
         uzr.subject_identif = vs.subject_identif;

CREATE UNIQUE INDEX mb_zr_rechth_objectid ON mb_zr_rechth (objectid ASC);
CREATE INDEX mb_zr_rechth_identif ON mb_zr_rechth (zr_identif ASC);

COMMENT ON MATERIALIZED VIEW mb_zr_rechth IS
    'commentaar view mb_zr_rechth:
    alle zakelijke rechten met rechthebbenden en referentie naar kadastraal onroerende zaak (perceel of appartementsrecht)
        beschikbare kolommen:
    * objectid: uniek id bruikbaar voor geoserver/arcgis,
    * zr_identif: natuurlijke id van zakelijk recht,
    * ingangsdatum_recht: -
    * subject_identif: natuurlijk id van subject (natuurlijk of niet natuurlijk) welke rechthebbende is,
    * mandeligheid_identif: identificatie van een mandeligheid, een gemeenschappelijk eigendom van een onroerende zaak,
    * koz_identif: natuurlijk id van kadastrale onroerende zaak (perceel of appratementsrecht) dat gekoppeld is,
    * aandeel: samenvoeging van teller en noemer (1/2),
    * omschr_aard_verkregen_recht: tekstuele omschrijving aard recht,
    * indic_betrokken_in_splitsing: -,
    * aantekeningen: samenvoeging van alle rechten voor dit recht,
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
    * kvk_nummer: -';


CREATE MATERIALIZED VIEW mb_avg_zr_rechth
            (
             objectid,
             zr_identif,
             ingangsdatum_recht,
             subject_identif,
             mandeligheid_identif,
             koz_identif,
             aandeel,
             omschr_aard_verkregenr_recht,
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
            BUILD DEFERRED
    REFRESH ON DEMAND
AS
SELECT CAST(ROWNUM AS INTEGER) AS objectid,
       uzr.zr_identif          AS zr_identif,
       uzr.ingangsdatum_recht,
       uzr.subject_identif,
       uzr.mandeligheid_identif,
       uzr.koz_identif,
       uzr.aandeel,
       uzr.omschr_aard_verkregenr_recht,
       uzr.indic_betrokken_in_splitsing,
       uzr.aantekeningen,
       vs.soort,
       vs.geslachtsnaam,
       vs.voorvoegsel,
       vs.voornamen,
       vs.aand_naamgebruik,
       vs.geslachtsaand,
       vs.naam,
       vs.woonadres,
       vs.geboortedatum,
       vs.geboorteplaats,
       vs.overlijdensdatum,
       vs.bsn,
       vs.organisatie_naam,
       vs.rechtsvorm,
       vs.statutaire_zetel,
       vs.rsin,
       vs.kvk_nummer
FROM vb_util_zk_recht uzr
         JOIN mb_avg_subject vs ON uzr.subject_identif = vs.subject_identif;

CREATE UNIQUE INDEX mb_avg_zr_rechth_objectid ON mb_avg_zr_rechth (objectid ASC);
CREATE INDEX mb_avg_zr_rechth_identif ON mb_avg_zr_rechth (zr_identif ASC);

COMMENT ON MATERIALIZED VIEW mb_avg_zr_rechth IS
    'commentaar view mb_avg_zr_rechth:
    alle zakelijke rechten met voor avg geschoonde rechthebbenden en referentie naar kadastraal onroerende zaak (perceel of appartementsrecht)
        beschikbare kolommen:
    * objectid: uniek id bruikbaar voor geoserver/arcgis,
    * zr_identif: natuurlijke id van zakelijk recht,
    * ingangsdatum_recht: -,
    * subject_identif: natuurlijk id van subject (natuurlijk of niet natuurlijk) welke rechthebbende is,
    * mandeligheid_identif: identificatie van een mandeligheid, een gemeenschappelijk eigendom van een onroerende zaak,
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
             mandeligheid_identif,
             aandeel,
             omschr_aard_verkregenr_recht,
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
            BUILD DEFERRED
    REFRESH ON DEMAND
AS
SELECT CAST(ROWNUM AS INTEGER) AS objectid,
       koz.koz_identif,
       koz.begin_geldigheid,
       koz.begin_geldigheid_datum,
       koz.type,
       koz.aanduiding,
       koz.aanduiding2,
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
       zrr.mandeligheid_identif,
       zrr.aandeel,
       zrr.omschr_aard_verkregenr_recht,
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
         RIGHT JOIN mb_kad_onrrnd_zk_adres koz ON zrr.koz_identif = koz.koz_identif;

CREATE UNIQUE INDEX mb_koz_rechth_objectid ON mb_koz_rechth (objectid ASC);
CREATE INDEX mb_koz_rechth_identif ON mb_koz_rechth (koz_identif ASC);
INSERT INTO user_sdo_geom_metadata
VALUES ('MB_KOZ_RECHTH', 'BEGRENZING_PERCEEL',
        MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
                            MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
CREATE INDEX mb_koz_rechth_begr_prcl_idx ON mb_koz_rechth (begrenzing_perceel) INDEXTYPE IS MDSYS.SPATIAL_INDEX;

COMMENT ON MATERIALIZED VIEW mb_koz_rechth IS
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
    * mandeligheid_identif: identificatie van een mandeligheid, een gemeenschappelijk eigendom van een onroerende zaak,
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
    * begrenzing_perceel: perceelvlak';


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
             mandeligheid_identif,
             aandeel,
             omschr_aard_verkregenr_recht,
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
            BUILD DEFERRED
    REFRESH ON DEMAND
AS
SELECT CAST(ROWNUM AS INTEGER) AS objectid,
       koz.koz_identif,
       koz.begin_geldigheid,
       koz.begin_geldigheid_datum,
       koz.type,
       koz.aanduiding,
       koz.aanduiding2,
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
       zrr.mandeligheid_identif,
       zrr.aandeel,
       zrr.omschr_aard_verkregenr_recht,
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
         RIGHT JOIN mb_kad_onrrnd_zk_adres koz ON zrr.koz_identif = koz.koz_identif;

CREATE UNIQUE INDEX mb_avg_koz_rechth_objectid ON mb_avg_koz_rechth (objectid ASC);
CREATE INDEX mb_avg_koz_rechth_identif ON mb_avg_koz_rechth (koz_identif ASC);
INSERT INTO user_sdo_geom_metadata
VALUES ('MB_AVG_KOZ_RECHTH', 'BEGRENZING_PERCEEL',
        MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
                            MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
CREATE INDEX mb_avg_koz_rechth_begr_p_idx ON mb_avg_koz_rechth (begrenzing_perceel) INDEXTYPE IS MDSYS.SPATIAL_INDEX;

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
    * mandeligheid_identif: identificatie van een mandeligheid, een gemeenschappelijk eigendom van een onroerende zaak,
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
    * begrenzing_perceel: perceelvlak';


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
            BUILD DEFERRED
    REFRESH ON DEMAND
AS
SELECT CAST(ROWNUM AS INTEGER)                                 AS objectid,
       qry.identificatie                                       as koz_identif,
       TO_CHAR(koza.begingeldigheid)                           AS begin_geldigheid,
       koza.begingeldigheid                                    AS begin_geldigheid_datum,
       TO_CHAR(koza.eindegeldigheid)                           AS eind_geldigheid,
       koza.eindegeldigheid                                    AS eind_geldigheid_datum,
       qry.type                                                AS type,
       COALESCE(koza.sectie, '') || ' ' ||
       COALESCE(TO_CHAR(koza.perceelnummer), '')               AS aanduiding,
       COALESCE(koza.akrkadastralegemeente, '') || ' ' || COALESCE(koza.sectie, '') || ' ' ||
       COALESCE(TO_CHAR(koza.perceelnummer), '') || ' ' ||
       COALESCE(TO_CHAR(koza.appartementsrechtvolgnummer), '') AS aanduiding2,
       koza.sectie                                             AS sectie,
       koza.perceelnummer                                      AS perceelnummer,
       koza.appartementsrechtvolgnummer                        AS appartementsindex,
       koza.akrkadastralegemeente                              AS gemeentecode,
       qry.soortgrootte                                        AS aand_soort_grootte,
       qry.kadastralegrootte                                   AS grootte_perceel,
       -- bestaat niet
       CAST(NULL AS VARCHAR2(4 CHAR))                          AS deelperceelnummer,
       -- bestaat niet
       CAST(NULL AS VARCHAR2(1120 CHAR))                       AS omschr_deelperceel,
       koza.aard_cultuur_onbebouwd                             AS aard_cultuur_onbebouwd,
       koza.koopsom_bedrag                                     AS bedrag,
       koza.koopsom_koopjaar                                   AS koopjaar,
       koza.koopsom_indicatiemeerobjecten                      AS meer_onroerendgoed,
       koza.koopsom_valuta                                     AS valutasoort,
       -- TODO BRK adres?
       CAST(NULL AS VARCHAR2(255 CHAR))                        AS loc_omschr,
       kozhr.onroerendezaak                                    AS overgegaan_in,
       qry.begrenzing_perceel                                  AS begrenzing_perceel
FROM (SELECT p_archief.identificatie,
             'perceel' AS type,
             p_archief.begingeldigheid,
             p_archief.soortgrootte,
             p_archief.kadastralegrootte,
             p_archief.begrenzing_perceel,
             p_archief.plaatscoordinaten
      FROM perceel_archief p_archief
      UNION ALL
      SELECT a_archief.identificatie,
             'appartement'               AS type,
             a_archief.begingeldigheid,
             CAST(NULL AS VARCHAR2(100)) AS soortgrootte,
             CAST(NULL AS NUMBER)        AS kadastralegrootte,
             CAST(NULL AS SDO_GEOMETRY)  AS begrenzing_perceel,
             CAST(NULL AS SDO_GEOMETRY)  AS plaatscoordinaten
      FROM appartementsrecht_archief a_archief) qry
         JOIN onroerendezaak_archief koza
              ON koza.identificatie = qry.identificatie AND qry.begingeldigheid = koza.begingeldigheid
         JOIN (SELECT ikoza.identificatie, MAX(ikoza.begingeldigheid) bdate
               FROM onroerendezaak_archief ikoza
               GROUP BY ikoza.identificatie) nqry
              ON nqry.identificatie = koza.identificatie AND nqry.bdate = koza.begingeldigheid
         LEFT JOIN onroerendezaakfiliatie kozhr ON kozhr.betreft = koza.identificatie;

CREATE UNIQUE INDEX mb_kad_onrrnd_zk_a_objidx ON mb_kad_onrrnd_zk_archief (objectiD ASC);
CREATE INDEX mb_kad_onrrnd_zk_a_identif ON mb_kad_onrrnd_zk_archief (koz_identif ASC);
INSERT INTO user_sdo_geom_metadata
VALUES ('MB_KAD_ONRRND_ZK_ARCHIEF', 'BEGRENZING_PERCEEL',
        MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
                            MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
CREATE INDEX mb_kad_onrrnd_zk_a_bgrgpidx ON mb_kad_onrrnd_zk_archief (begrenzing_perceel) INDEXTYPE IS MDSYS.SPATIAL_INDEX;
CREATE INDEX mb_kad_onrr_z_ar_overgeg_idx ON mb_kad_onrrnd_zk_archief (overgegaan_in);

COMMENT ON MATERIALIZED VIEW mb_kad_onrrnd_zk_archief IS
    'commentaar materialized view mb_kad_onrrnd_zk_archief:
    Nieuwste gearchiveerde versie van ieder kadastrale onroerende zaak (perceel en appartementsrecht) met objectid voor geoserver/arcgis en historische relatie
        beschikbare kolommen:
    * objectid: uniek id bruikbaar voor geoserver/arcgis,
    * koz_identif: natuurlijke id van perceel of appartementsrecht
    * begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
    * eind_geldigheid: datum wanneer dit object ongeldig geworden is,
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
    * begrenzing_perceel: perceelvlak';
