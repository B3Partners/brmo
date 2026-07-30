-- volgende scripts uitvoeren in het rsgb schema
ALTER SESSION SET NLS_LENGTH_SEMANTICS='CHAR';
SET DEFINE OFF;
WHENEVER SQLERROR EXIT sql.sqlcode;

CREATE MATERIALIZED VIEW mb_kadastraleonroerendezakenmetadres
AS
SELECT CAST(ROWNUM AS INTEGER)                                                 AS objectid,
       o.identificatie                                                         AS identificatie,
       TO_CHAR(o.begingeldigheid),
       o.begingeldigheid                                                       AS begingeldigheid_datum,
       -- koppeling met BAG
       CAST(NULL AS VARCHAR2(255 CHAR))                                        AS benoemdobj_identif,
       LOWER(o.type)                                                           AS type, -- 'perceel' of 'appartement' uit de basistabel
       NVL(o.sectie, '') || ' ' || NVL(TO_CHAR(o.perceelnummer), '')         AS aanduiding,
       NVL(o.akrkadastralegemeente, '') || ' ' || NVL(o.sectie, '') || ' ' ||
       NVL(TO_CHAR(o.perceelnummer), '') ||
       ' ' || NVL(TO_CHAR(o.appartementsrechtvolgnummer), '')                  AS aanduiding2,
       o.sectie                                                                AS sectie,
       o.perceelnummer                                                         AS perceelnummer,
       o.appartementsrechtvolgnummer,
       o.akrkadastralegemeente,
       p_direct.soortgrootte                                                   AS soortgrootte,
       p_direct.kadastralegrootte                                              AS kadastralegrootte,
       SDO_GEOM.SDO_AREA(NVL(p_direct.begrenzing_perceel, NVL(p_app1.begrenzing_perceel, p_app2.begrenzing_perceel)), 0.1) AS oppervlakte_geom,
       -- bestaat niet
       CAST(NULL AS VARCHAR2(4 CHAR))                                          AS deelperceelnummer,
       -- bestaat niet
       CAST(NULL AS VARCHAR2(1120 CHAR))                                       AS omschr_deelperceel,
       -- TODO verkoop datum uit stukdeel via recht
       CAST(NULL AS DATE)                                                      AS verkoop_datum,
       o.aard_cultuur_onbebouwd                                                AS aard_cultuur_onbebouwd,
       o.koopsom_bedrag,
       o.koopsom_koopjaar,
       o.koopsom_indicatiemeerobjecten,
       o.koopsom_valuta,
       -- TODO BRK adres?
       CAST(NULL AS VARCHAR2(255 CHAR))                                        AS loc_omschr,
       aantekeningen.aantekeningen                                             AS aantekeningen,
       maogb.identificatienummeraanduiding,
       maogb.nummeraanduidingstatus,
       maogb.gemeente,
       maogb.woonplaats,
       maogb.straatnaam,
       maogb.huisnummer,
       maogb.huisletter,
       maogb.huisnummertoevoeging,
       maogb.postcode,
       maogb.gebruiksdoelen,
       maogb.oppervlakte,
       SDO_CS.TRANSFORM(p_direct.plaatscoordinaten, 4326).SDO_POINT.X          AS lon,
       SDO_CS.TRANSFORM(p_direct.plaatscoordinaten, 4326).SDO_POINT.Y          AS lat,
       NVL(p_direct.begrenzing_perceel, NVL(p_app1.begrenzing_perceel, p_app2.begrenzing_perceel)) AS begrenzing_perceel

FROM BRMO_BRK.onroerendezaak o
LEFT JOIN BRMO_BRK.perceel p_direct 
       ON o.identificatie = p_direct.identificatie
LEFT JOIN BRMO_BRK.appartementsrecht a 
       ON o.identificatie = a.identificatie
LEFT JOIN BRMO_BRK.recht r 
       ON a.hoofdsplitsing = r.isbetrokkenbij
LEFT JOIN BRMO_BRK.perceel p_app1 
       ON r.rustop = p_app1.identificatie
LEFT JOIN BRMO_BRK.recht_isbelastmet ribm 
       ON r.identificatie = ribm.isbelastmet
LEFT JOIN BRMO_BRK.recht r2 
       ON ribm.zakelijkrecht = r2.identificatie
LEFT JOIN BRMO_BRK.perceel p_app2 
       ON r2.rustop = p_app2.identificatie
LEFT JOIN (
    SELECT r_aant.aantekeningkadastraalobject,
           LISTAGG(
               'id: ' || NVL(r_aant.identificatie, '') || ', '
               || 'aard: ' || NVL(r_aant.aard, '') || ', '
               || 'begin: ' || NVL(TO_CHAR(r_aant.begingeldigheid), '') || ', '
               || 'beschrijving: ' || NVL(r_aant.omschrijving, '') || ', '
               || 'eind: ' || NVL(TO_CHAR(r_aant.einddatum), '') || ', '
               || 'koz-id: ' || NVL(r_aant.aantekeningkadastraalobject, '') || ', '
               || 'subject-id: ' || NVL(r_aant.betrokkenpersoon, '') || '; ', ' & ' ON OVERFLOW TRUNCATE WITH COUNT)
           WITHIN GROUP ( ORDER BY r_aant.aantekeningkadastraalobject ) AS aantekeningen
    FROM BRMO_BRK.recht r_aant
    GROUP BY r_aant.aantekeningkadastraalobject
) aantekeningen ON o.identificatie = aantekeningen.aantekeningkadastraalobject

LEFT JOIN BRMO_BRK.objectlocatie o2 ON o2.heeft = o.identificatie
LEFT JOIN BRMO_BRK.adres a2 ON a2.identificatie = o2.betreft
LEFT JOIN mb_adresseerbaar_object_geometrie_bag maogb ON maogb.identificatie = a2.adresseerbaarobject;

COMMENT ON MATERIALIZED VIEW mb_kadastraleonroerendezakenmetadres IS
    'commentaar view mb_kad_onrrnd_zk_adres:
    alle kadastrale onroerende zaken (perceel en appartementsrecht) met opgezochte verkoop datum, objectid voor geoserver/arcgis en BAG adres
        beschikbare kolommen:
    * objectid: uniek id bruikbaar voor geoserver/arcgis,
    * identificatie: natuurlijke id van perceel of appartementsrecht
    * begingeldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt) als text veld,
    * begingeldigheid_datum: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt) als datum veld,
    * adresseerbaarobject: koppeling met BAG object,
    * type: perceel of appartement,
    * aanduiding: sectie perceelnummer,
    * aanduiding2: kadgem sectie perceelnummer appartementsindex,
    * sectie: -,
    * perceelnummer: -,
    * appartementsrechtvolgnummer: -,
    * akrkadastralegemeente: -,
    * soortgrootte: -,
    * kadastralegrootte: -,
    * oppervlakte_geom: oppervlakte berekend uit geometrie, hoort gelijk te zijn aan grootte_perceel,
    * deelperceelnummer: -,
    * omschr_deelperceel: -,
    * verkoop_datum: laatste datum gevonden akten van verkoop,
    * aard_cultuur_onbebouwd: -,
    * koopsom_bedrag: -,
    * koopsom_koopjaar: -,
    * koopsom_indicatiemeerobjecten: -,
    * koopsom_valuta: -,
    * loc_omschr: adres buiten BAG om meegegeven,
    * aantekeningen: -,
    * identificatienummeraanduiding: identificatie van nummeraanduiding (uit BAG)
    * na_status: status van nummeraanduiding (uit BAG)
    * gemeente: - (uit BAG),
    * woonplaats: - (uit BAG),
    * straatnaam: - (uit BAG),
    * huisnummer: - (uit BAG),
    * huisletter: - (uit BAG),
    * huisnummer_toev: - (uit BAG),
    * postcode: - (uit BAG),
    * gebruiksdoelen: alle gebruiksdoelen gescheiden door komma (uit BAG)
    * oppervlakte_obj: oppervlak van gebouwd object (uit BAG)
    * lon: coordinaat als WSG84,
    * lon: coordinaat als WSG84,
    * begrenzing_perceel: perceelvlak';

delete
from user_sdo_geom_metadata
where table_name = 'MB_KADASTRALEONROERENDEZAKENMETADRES';
insert into user_sdo_geom_metadata
values ('MB_KADASTRALEONROERENDEZAKENMETADRES', 'begrenzing_perceel',
        MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
                            MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

CREATE INDEX mb_kadastraleonroerendezakenmetadres_begrenzing_perceel_idx ON mb_kadastraleonroerendezakenmetadres (begrenzing_perceel) INDEXTYPE IS MDSYS.SPATIAL_INDEX;
CREATE INDEX mb_kadastraleonroerendezakenmetadres_identif ON mb_kadastraleonroerendezakenmetadres (identificatie);
CREATE UNIQUE INDEX mb_kadastraleonroerendezakenmetadres_objectid ON mb_kadastraleonroerendezakenmetadres (objectid);

CREATE MATERIALIZED VIEW mb_onroerendezakenmetrechthebbenden
AS
SELECT CAST(ROWNUM AS INTEGER)            AS objectid,
       koz.identificatie,
       TO_CHAR(koz.begingeldigheid_datum) AS begingeldigheid,
       koz.begingeldigheid_datum,
       koz.type,
       koz.aanduiding,
       koz.aanduiding2,
       koz.sectie,
       koz.perceelnummer,
       koz.appartementsrechtvolgnummer,
       koz.akrkadastralegemeente,
       koz.soortgrootte,
       koz.kadastralegrootte,
       koz.oppervlakte_geom,
       koz.deelperceelnummer,
       koz.omschr_deelperceel,
       koz.verkoop_datum,
       koz.aard_cultuur_onbebouwd,
       koz.koopsom_bedrag,
       koz.koopsom_koopjaar,
       koz.koopsom_indicatiemeerobjecten,
       koz.koopsom_valuta,
       koz.loc_omschr,
       zrr.zr_identif                     AS zakelijkrechtidentificatie,
       zrr.ingangsdatum_recht             AS zakelijkrechtbegingeldigheid,
       zrr.subject_identif                AS tennamevan,
       zrr.mandeligheid_identif,
       zrr.aandeel,
       zrr.omschr_aard_verkregenr_recht   AS aard,
       zrr.indic_betrokken_in_splitsing   AS isbetrokkenbij,
       zrr.soort,
       zrr.geslachtsnaam,
       zrr.voorvoegsel,
       zrr.voornamen,
       zrr.aand_naamgebruik               AS aanduidingnaamgebruik,
       zrr.geslachtsaand                  AS geslacht,
       zrr.naam,
       zrr.woonadres,
       zrr.geboortedatum,
       zrr.geboorteplaats,
       zrr.overlijdensdatum,
       zrr.bsn,
       zrr.organisatie_naam               AS statutairenaam,
       zrr.rechtsvorm,
       zrr.statutaire_zetel,
       zrr.rsin,
       zrr.kvk_nummer                     AS kvknummer,
       zrr.aantekeningen,
       koz.gemeente,
       koz.woonplaats,
       koz.straatnaam,
       koz.huisnummer,
       koz.huisletter,
       koz.huisnummertoevoeging,
       koz.postcode,
       koz.lon,
       koz.lat,
       koz.begrenzing_perceel,
       -- BRMO-401: tijdstipaanbieding nu uit mb_zr_rechth te halen.
       zrr.tijdstipaanbieding            AS tijdstipaanbieding_stuk,
       zrr.tijdstipaanbieding2           AS tijdstipaanbieding_stuk2
FROM BRMO_BRK.mb_zr_rechth zrr
         RIGHT JOIN mb_kadastraleonroerendezakenmetadres koz ON (zrr.koz_identif = koz.identificatie)
 GROUP BY koz.identificatie, koz.begingeldigheid_datum, koz.type, koz.aanduiding, koz.aanduiding2, koz.sectie, koz.perceelnummer, koz.appartementsrechtvolgnummer, koz.akrkadastralegemeente, koz.soortgrootte, koz.kadastralegrootte, koz.oppervlakte_geom, koz.deelperceelnummer, koz.omschr_deelperceel, koz.verkoop_datum, koz.aard_cultuur_onbebouwd, koz.koopsom_bedrag, koz.koopsom_koopjaar, koz.koopsom_indicatiemeerobjecten, koz.koopsom_valuta, koz.loc_omschr, zrr.zr_identif, zrr.ingangsdatum_recht, zrr.mandeligheid_identif, zrr.subject_identif, zrr.aandeel, zrr.omschr_aard_verkregenr_recht, zrr.indic_betrokken_in_splitsing, zrr.soort, zrr.geslachtsnaam, zrr.voorvoegsel, zrr.voornamen, zrr.aand_naamgebruik, zrr.geslachtsaand, zrr.naam, zrr.woonadres, zrr.geboortedatum, zrr.geboorteplaats, zrr.overlijdensdatum, zrr.bsn, zrr.organisatie_naam, zrr.rechtsvorm, zrr.statutaire_zetel, zrr.rsin, zrr.kvk_nummer, zrr.aantekeningen, koz.gemeente, koz.woonplaats, koz.straatnaam, koz.huisnummer, koz.huisletter, koz.huisnummertoevoeging, koz.postcode, koz.lon, koz.lat, koz.begrenzing_perceel, zrr.tijdstipaanbieding, zrr.tijdstipaanbieding2;

COMMENT ON MATERIALIZED VIEW mb_onroerendezakenmetrechthebbenden
    IS 'commentaar view mb_onroerendezakenmetrechthebbenden:
    kadastrale percelen een appartementsrechten met rechten en rechthebbenden en objectid voor geoserver/arcgis
        beschikbare kolommen:
    * objectid: uniek id bruikbaar voor geoserver/arcgis,
    * identificatie: natuurlijke id van perceel of appartementsrecht
    * begingeldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
    * begingeldigheid_datum: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
    * type: perceel of appartement,
    * aanduiding: sectie perceelnummer,
    * aanduiding2: kadgem sectie perceelnummer appartementsindex,
    * sectie: -,
    * perceelnummer: -,
    * appartementsrechtvolgnummer: -,
    * akrkadastralegemeente: -,
    * soortgrootte: -,
    * kadastralegrootte: -,
    * oppervlakte_geom: oppervlakte berekend uit geometrie, hoort gelijk te zijn aan grootte_perceel,
    * deelperceelnummer: -,
    * omschr_deelperceel: -,
    * verkoop_datum: laatste datum gevonden akten van verkoop,
    * aard_cultuur_onbebouwd: -,
    * koopsom_bedrag: -,
    * koopsom_koopjaar: -,
    * koopsom_indicatiemeerobjecten: -,
    * koopsom_valuta: -,
    * loc_omschr: adres buiten BAG om meegegeven,
    * zakelijkrechtidentificatie: natuurlijk id van zakelijk recht,
    * zakelijkrechtbegingeldigheid: - ,
    * tennamevan: natuurlijk id van rechthebbende,
    * mandeligheid_identif: identificatie van een mandeligheid, een gemeenschappelijk eigendom van een onroerende zaak,
    * aandeel: samenvoeging van teller en noemer (1/2),
    * aard: tekstuele omschrijving aard recht,
    * isbetrokkenbij: -,
    * soort: soort subject zoals natuurlijk, niet-natuurlijk enz.
    * geslachtsnaam: -
    * voorvoegselsgeslachtsnaam: -
    * voornamen: -
    * aanduidingnaamgebruik: -
    * geslacht: -
    * naam: samengestelde naam bruikbaar voor natuurlijke en niet-natuurlijke subjecten
    * woonadres: meegeleverd adres buiten BAG koppeling om
    * geboortedatum: -
    * geboorteplaats: -
    * datumoverlijden: -
    * bsn: -
    * statutairenaam: naam niet natuurlijk subject
    * rechtsvorm: -
    * statutairezetel: -
    * rsin: -
    * kvknummer: -
    * aantekeningen: samenvoeging van alle aantekeningen van dit recht,
    * gemeente: -,
    * woonplaats: -,
    * straatnaam: -,
    * huisnummer: -,
    * huisletter: -,
    * huisnummertoevoeging: -,
    * postcode: -,
    * lon: coordinaat als WSG84,
    * lon: coordinaat als WSG84,
    * begrenzing_perceel: perceelvlak,
    * tijdstipaanbieding_stuk: tijdstip van aanbieding van stuk,
    * tijdstipaanbieding_stuk2: tijdstip van aanbieding van 2e stuk';

delete
from user_sdo_geom_metadata
where table_name = 'MB_ONROERENDEZAKENMETRECHTHEBBENDEN';
insert into user_sdo_geom_metadata
values ('mb_onroerendezakenmetrechthebbenden', 'begrenzing_perceel',
        MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
                            MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

CREATE INDEX mb_onroerendezakenmetrechthebbenden_begrenzing_perceel_idx ON mb_onroerendezakenmetrechthebbenden (begrenzing_perceel) INDEXTYPE IS MDSYS.SPATIAL_INDEX;
CREATE INDEX mb_onroerendezakenmetrechthebbenden_identif ON mb_onroerendezakenmetrechthebbenden (identificatie);
CREATE UNIQUE INDEX mb_onroerendezakenmetrechthebbenden_objectid ON mb_onroerendezakenmetrechthebbenden (objectid);

CREATE MATERIALIZED VIEW mb_avg_onroerendezakenmetrechthebbenden
AS
SELECT CAST(ROWNUM AS INTEGER)            AS objectid,
       koz.identificatie,
       TO_CHAR(koz.begingeldigheid_datum) AS begingeldigheid,
       koz.begingeldigheid_datum,
       koz.type,
       koz.aanduiding,
       koz.aanduiding2,
       koz.sectie,
       koz.perceelnummer,
       koz.appartementsrechtvolgnummer,
       koz.akrkadastralegemeente,
       koz.soortgrootte,
       koz.kadastralegrootte,
       koz.oppervlakte_geom,
       koz.deelperceelnummer,
       koz.omschr_deelperceel,
       koz.verkoop_datum,
       koz.aard_cultuur_onbebouwd,
       koz.koopsom_bedrag,
       koz.koopsom_koopjaar,
       koz.koopsom_indicatiemeerobjecten,
       koz.koopsom_valuta,
       koz.loc_omschr,
       zrr.zr_identif                     AS zakelijkrechtidentificatie,
       zrr.ingangsdatum_recht             AS zakelijkrechtbegingeldigheid,
       zrr.subject_identif                AS tennamevan,
       zrr.mandeligheid_identif,
       zrr.aandeel,
       zrr.omschr_aard_verkregenr_recht   AS aard,
       zrr.indic_betrokken_in_splitsing   AS isbetrokkenbij,
       zrr.soort,
       zrr.geslachtsnaam,
       zrr.voorvoegsel,
       zrr.voornamen,
       zrr.aand_naamgebruik               AS aanduidingnaamgebruik,
       zrr.geslachtsaand                  AS geslacht,
       zrr.naam,
       zrr.woonadres,
       zrr.geboortedatum,
       zrr.geboorteplaats,
       zrr.overlijdensdatum,
       zrr.bsn,
       zrr.organisatie_naam               AS statutairenaam,
       zrr.rechtsvorm,
       zrr.statutaire_zetel,
       zrr.rsin,
       zrr.kvk_nummer                     AS kvknummer,
       zrr.aantekeningen,
       koz.gemeente,
       koz.woonplaats,
       koz.straatnaam,
       koz.huisnummer,
       koz.huisletter,
       koz.huisnummertoevoeging,
       koz.postcode,
       koz.lon,
       koz.lat,
       koz.begrenzing_perceel,
       -- BRMO-401: tijdstipaanbieding nu uit mb_zr_rechth te halen.
       zrr.tijdstipaanbieding            AS tijdstipaanbieding_stuk,
       zrr.tijdstipaanbieding2           AS tijdstipaanbieding_stuk2
FROM BRMO_BRK.mb_avg_zr_rechth zrr
         RIGHT JOIN mb_kadastraleonroerendezakenmetadres koz ON (zrr.koz_identif = koz.identificatie)
 GROUP BY koz.identificatie, koz.begingeldigheid_datum, koz.type, koz.aanduiding, koz.aanduiding2, koz.sectie, koz.perceelnummer, koz.appartementsrechtvolgnummer, koz.akrkadastralegemeente, koz.soortgrootte, koz.kadastralegrootte, koz.oppervlakte_geom, koz.deelperceelnummer, koz.omschr_deelperceel, koz.verkoop_datum, koz.aard_cultuur_onbebouwd, koz.koopsom_bedrag, koz.koopsom_koopjaar, koz.koopsom_indicatiemeerobjecten, koz.koopsom_valuta, koz.loc_omschr, zrr.zr_identif, zrr.ingangsdatum_recht, zrr.mandeligheid_identif, zrr.subject_identif, zrr.aandeel, zrr.omschr_aard_verkregenr_recht, zrr.indic_betrokken_in_splitsing, zrr.soort, zrr.geslachtsnaam, zrr.voorvoegsel, zrr.voornamen, zrr.aand_naamgebruik, zrr.geslachtsaand, zrr.naam, zrr.woonadres, zrr.geboortedatum, zrr.geboorteplaats, zrr.overlijdensdatum, zrr.bsn, zrr.organisatie_naam, zrr.rechtsvorm, zrr.statutaire_zetel, zrr.rsin, zrr.kvk_nummer, zrr.aantekeningen, koz.gemeente, koz.woonplaats, koz.straatnaam, koz.huisnummer, koz.huisletter, koz.huisnummertoevoeging, koz.postcode, koz.lon, koz.lat, koz.begrenzing_perceel, zrr.tijdstipaanbieding, zrr.tijdstipaanbieding2;

COMMENT ON MATERIALIZED VIEW mb_avg_onroerendezakenmetrechthebbenden
    IS 'commentaar view mb_avg_onroerendezakenmetrechthebbenden:
    kadastrale percelen een appartementsrechten met rechten en rechthebbenden en objectid voor geoserver/arcgis
        beschikbare kolommen:
    * objectid: uniek id bruikbaar voor geoserver/arcgis,
    * identificatie: natuurlijke id van perceel of appartementsrecht
    * begingeldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
    * begingeldigheid_datum: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
    * type: perceel of appartement,
    * aanduiding: sectie perceelnummer,
    * aanduiding2: kadgem sectie perceelnummer appartementsindex,
    * sectie: -,
    * perceelnummer: -,
    * appartementsrechtvolgnummer: -,
    * akrkadastralegemeente: -,
    * soortgrootte: -,
    * kadastralegrootte: -,
    * oppervlakte_geom: oppervlakte berekend uit geometrie, hoort gelijk te zijn aan grootte_perceel,
    * deelperceelnummer: -,
    * omschr_deelperceel: -,
    * verkoop_datum: laatste datum gevonden akten van verkoop,
    * aard_cultuur_onbebouwd: -,
    * koopsom_bedrag: -,
    * koopsom_koopjaar: -,
    * koopsom_indicatiemeerobjecten: -,
    * koopsom_valuta: -,
    * loc_omschr: adres buiten BAG om meegegeven,
    * zakelijkrechtidentificatie: natuurlijk id van zakelijk recht,
    * zakelijkrechtbegingeldigheid: - ,
    * tennamevan: natuurlijk id van rechthebbende,
    * mandeligheid_identif: identificatie van een mandeligheid, een gemeenschappelijk eigendom van een onroerende zaak,
    * aandeel: samenvoeging van teller en noemer (1/2),
    * aard: tekstuele omschrijving aard recht,
    * isbetrokkenbij: -,
    * soort: soort subject zoals natuurlijk, niet-natuurlijk enz.
    * geslachtsnaam: -
    * voorvoegselsgeslachtsnaam: -
    * voornamen: -
    * aanduidingnaamgebruik: -
    * geslacht: -
    * naam: samengestelde naam bruikbaar voor natuurlijke en niet-natuurlijke subjecten
    * woonadres: meegeleverd adres buiten BAG koppeling om
    * geboortedatum: -
    * geboorteplaats: -
    * datumoverlijden: -
    * bsn: -
    * statutairenaam: naam niet natuurlijk subject
    * rechtsvorm: -
    * statutairezetel: -
    * rsin: -
    * kvknummer: -
    * aantekeningen: samenvoeging van alle aantekeningen van dit recht,
    * gemeente: -,
    * woonplaats: -,
    * straatnaam: -,
    * huisnummer: -,
    * huisletter: -,
    * huisnummertoevoeging: -,
    * postcode: -,
    * lon: coordinaat als WSG84,
    * lon: coordinaat als WSG84,
    * begrenzing_perceel: perceelvlak,
    * tijdstipaanbieding_stuk: tijdstip van aanbieding van stuk,
    * tijdstipaanbieding_stuk2: tijdstip van aanbieding van 2e stuk';

delete
from user_sdo_geom_metadata
where table_name = 'MB_AVG_ONROERENDEZAKENMETRECHTHEBBENDEN';
insert into user_sdo_geom_metadata
values ('mb_avg_onroerendezakenmetrechthebbenden', 'begrenzing_perceel',
        MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
                            MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

CREATE INDEX mb_avg_onroerendezakenmetrechthebbenden_begrenzing_perceel_idx ON mb_avg_onroerendezakenmetrechthebbenden (begrenzing_perceel) INDEXTYPE IS MDSYS.SPATIAL_INDEX;
CREATE INDEX mb_avg_onroerendezakenmetrechthebbenden_identif ON mb_avg_onroerendezakenmetrechthebbenden (identificatie);
CREATE UNIQUE INDEX mb_avg_onroerendezakenmetrechthebbenden_objectid ON mb_avg_onroerendezakenmetrechthebbenden (objectid);
