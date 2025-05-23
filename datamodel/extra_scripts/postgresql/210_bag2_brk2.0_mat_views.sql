SET SCHEMA 'public';
SET search_path = public, brk;

-- materialized view die de BRK percelen en BAG adressen door middel van het adresseerbaarobjectidentificatie koppelt. 
CREATE MATERIALIZED VIEW mb_kadastraleonroerendezakenmetadres
            (
             objectid,
             identificatie,
             begingeldigheid,
             begingeldigheid_datum,
             adresseerbaarobject,
             type,
             aanduiding,
             aanduiding2,
             sectie,
             perceelnummer,
             appartementsrechtvolgnummer,
             akrkadastralegemeente,
             soortgrootte,
             kadastralegrootte,
             oppervlakte_geom,
             deelperceelnummer,
             omschr_deelperceel,
             verkoop_datum,
             aard_cultuur_onbebouwd,
             koopsom_bedrag,
             koopsom_koopjaar,
             koopsom_indicatiemeerobjecten,
             koopsom_valuta,
             loc_omschr,
             aantekeningen,
             identificatienummeraanduiding,
             nummeraanduidingstatus,
             gemeente,
             woonplaats,
             straatnaam,
             huisnummer,
             huisletter,
             huisnummertoevoeging,
             postcode,
             gebruiksdoelen,
             oppervlakte,
             lon,
             lat,
             begrenzing_perceel
                )
AS
SELECT row_number() OVER ()                                                                            AS objectid,
       o.identificatie,
       o.begingeldigheid                                                                               AS begingeldigheid,
       o.begingeldigheid                                                                               AS begingeldigheid_datum,
       a2.adresseerbaarobject                                                                          AS adresseerbaarobject,
       qry.type,
       COALESCE(o.sectie, '') || ' ' || COALESCE(o.perceelnummer::text, '')                            AS aanduiding,
       COALESCE(o.akrkadastralegemeente, '') || ' ' || COALESCE(o.sectie, '') || ' ' ||
       COALESCE(o.perceelnummer::text, '') || ' ' || COALESCE(o.appartementsrechtvolgnummer::text, '') AS aanduiding2,
       o.sectie,
       o.perceelnummer,
       o.appartementsrechtvolgnummer,
       o.akrkadastralegemeente,
       qry.soortgrootte,
       qry.kadastralegrootte,
       st_area(qry.begrenzing_perceel)                                                                 AS oppervlakte_geom,
       NULL                                                                                            AS deelperceelnummer,
       NULL                                                                                            AS omschr_deelperceel,
       NULL                                                                                            AS verkoop_datum,
       o.aard_cultuur_onbebouwd,
       o.koopsom_bedrag,
       o.koopsom_koopjaar,
       o.koopsom_indicatiemeerobjecten,
       o.koopsom_valuta,
       NULL                                                                                            AS loc_omschr,
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
       st_x(st_transform(qry.plaatscoordinaten, 4326))                                                 AS lon,
       st_y(st_transform(qry.plaatscoordinaten, 4326))                                                 AS lat,
       qry.begrenzing_perceel
--        ,st1.tijdstipaanbieding,
--        st2.tijdstipaanbieding as tijdstipaanbieding2
FROM (SELECT p.identificatie,
             'perceel' AS type,
             p.soortgrootte,
             p.kadastralegrootte,
             p.begrenzing_perceel,
             p.plaatscoordinaten
      FROM brk.perceel p

      UNION ALL

      SELECT a.identificatie,
             'appartement'                                         AS type,
             NULL                                                  AS soortgrootte,
             NULL                                                  AS kadastralegrootte,
             COALESCE(p.begrenzing_perceel, p2.begrenzing_perceel) AS begrenzing_perceel,
             COALESCE(p.plaatscoordinaten, p2.plaatscoordinaten)   AS plaatscoordinaten
      FROM brk.appartementsrecht a
               LEFT JOIN brk.recht r ON a.hoofdsplitsing = r.isbetrokkenbij
          -- wanneer het zakelijkrecht een eigendomsrecht is
               LEFT JOIN brk.perceel p ON r.rustop = p.identificatie
          -- [BRMO-342] wanneer het zakelijkrecht een recht is die het eigendomsrecht belast
               LEFT JOIN brk.recht_isbelastmet ribm ON r.identificatie = ribm.isbelastmet
               LEFT JOIN brk.recht r2 ON ribm.zakelijkrecht = r2.identificatie
               LEFT JOIN brk.perceel p2 ON r2.rustop = p2.identificatie) qry
         LEFT JOIN brk.onroerendezaak o ON qry.identificatie = o.identificatie
         LEFT JOIN brk.objectlocatie o2 ON o2.heeft = o.identificatie
         LEFT JOIN brk.adres a2 ON a2.identificatie = o2.betreft
         LEFT JOIN mb_adresseerbaar_object_geometrie_bag maogb ON maogb.identificatie = a2.adresseerbaarobject
WITH NO DATA;

COMMENT ON MATERIALIZED VIEW public.mb_kadastraleonroerendezakenmetadres
    IS 'commentaar view mb_kad_onrrnd_zk_adres:
    alle kadastrale onroerende zaken (perceel en appartementsrecht) met opgezochte verkoop datum, objectid voor geoserver/arcgis en BAG adres
        beschikbare kolommen:
    * objectid: uniek id bruikbaar voor geoserver/arcgis,
    * identificatie: natuurlijke id van perceel of appartementsrecht
    * begingeldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt) als text veld,
    * begingeldigheid_datum: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt) als datum veld,
    * adresseerbaarobject: koppeling met BAG object,
    * type: perceel of appartement,
    * aanduiding: sectie perceelnummer,
    * aanduiding2:   sectie perceelnummer appartementsindex,
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

CREATE INDEX mb_kadastraleonroerendezakenmetadres_begrenzing_perceel_idx
    ON public.mb_kadastraleonroerendezakenmetadres USING gist
        (begrenzing_perceel)
    TABLESPACE pg_default;
CREATE INDEX mb_kadastraleonroerendezakenmetadres_identif
    ON public.mb_kadastraleonroerendezakenmetadres USING btree
        (identificatie COLLATE pg_catalog."default")
    TABLESPACE pg_default;
CREATE UNIQUE INDEX mb_kadastraleonroerendezakenmetadres_objectid
    ON public.mb_kadastraleonroerendezakenmetadres USING btree
        (objectid)
    TABLESPACE pg_default;

-- materialized view die de rechthebbenden, zakelijke rechten en de percelen met BAG adressen koppelt
CREATE MATERIALIZED VIEW public.mb_onroerendezakenmetrechthebbenden
            (
             objectid,
             identificatie,
             begingeldigheid,
             begingeldigheid_datum,
             type,
             aanduiding,
             aanduiding2,
             sectie,
             perceelnummer,
             appartementsrechtvolgnummer,
             akrkadastralegemeente,
             soortgrootte,
             kadastralegrootte,
             oppervlakte_geom,
             deelperceelnummer,
             omschr_deelperceel,
             verkoop_datum,
             aard_cultuur_onbebouwd,
             koopsom_bedrag,
             koopsom_koopjaar,
             koopsom_indicatiemeerobjecten,
             koopsom_valuta,
             loc_omschr,
             zakelijkrechtidentificatie,
             zakelijkrechtbegingeldigheid,
             mandeligheid_identif,
             tennamevan,
             aandeel,
             aard,
             isbetrokkenbij,
             soort,
             geslachtsnaam,
             voorvoegselsgeslachtsnaam,
             voornamen,
             daanduidingnaamgebruik,
             geslacht,
             naam,
             woonadres,
             geboortedatum,
             geboorteplaats,
             datumoverlijden,
             bsn,
             statutairenaam,
             rechtsvorm,
             statutairezetel,
             rsin,
             kvknummer,
             aantekeningen,
             gemeente,
             woonplaats,
             straatnaam,
             huisnummer,
             huisletter,
             huisnummertoevoeging,
             postcode,
             lon,
             lat,
             begrenzing_perceel,
             tijdstipaanbieding_stuk,
             tijdstipaanbieding_stuk2
                )
AS
SELECT row_number() OVER ()             AS objectid,
       koz.identificatie,
       koz.begingeldigheid,
       koz.begingeldigheid_datum,
       koz.type,
       rtrim(koz.aanduiding)            AS aanduidng,
       rtrim(koz.aanduiding2)           AS aanduiding2,
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
       zrr.zr_identif                   AS zakelijkrechtidentificatie,
       zrr.ingangsdatum_recht           AS zakelijkrechtbegingeldigheid,
       zrr.mandeligheid_identif,
       zrr.subject_identif              AS tennamevan,
       zrr.aandeel,
       zrr.omschr_aard_verkregen_recht  AS aard,
       zrr.indic_betrokken_in_splitsing AS isbetrokkenbij,
       zrr.soort,
       zrr.geslachtsnaam,
       zrr.voorvoegsel                  AS voorvoegselsgeslachtsnaam,
       zrr.voornamen,
       zrr.aand_naamgebruik             AS daanduidingnaamgebruik,
       zrr.geslachtsaand                AS geslacht,
       zrr.naam,
       zrr.woonadres,
       zrr.geboortedatum,
       zrr.geboorteplaats,
       zrr.overlijdensdatum             AS datumoverlijden,
       zrr.bsn,
       zrr.organisatie_naam             AS statutairenaam,
       zrr.rechtsvorm,
       zrr.statutaire_zetel             AS statutairezetel,
       zrr.rsin,
       zrr.kvk_nummer                   AS kvknummer,
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
       st1.tijdstipaanbieding as tijdstipaanbieding_stuk,
       st2.tijdstipaanbieding as tijdstipaanbieding_stuk2
FROM brk.mb_zr_rechth zrr
         RIGHT JOIN mb_kadastraleonroerendezakenmetadres koz ON zrr.koz_identif = koz.identificatie
         JOIN brk.recht r on  zrr.zr_identif = r.van
         LEFT JOIN brk.stukdeel sd1 ON sd1.identificatie = r.isgebaseerdop
         LEFT JOIN brk.stukdeel sd2 ON sd2.identificatie = r.isgebaseerdop2
         LEFT JOIN brk.stuk st1 ON sd1.deelvan = st1.identificatie
         LEFT JOIN brk.stuk st2 ON sd2.deelvan = st2.identificatie
WITH NO DATA;
-- View indexes:
CREATE INDEX mb_onroerendezakenmetrechthebbenden_begrenzing_perceel_idx ON public.mb_onroerendezakenmetrechthebbenden USING gist (begrenzing_perceel);
CREATE INDEX mb_onroerendezakenmetrechthebbenden_identif ON public.mb_onroerendezakenmetrechthebbenden USING btree (identificatie);
CREATE UNIQUE INDEX mb_onroerendezakenmetrechthebbenden_objectid ON public.mb_onroerendezakenmetrechthebbenden USING btree (objectid);


COMMENT ON MATERIALIZED VIEW public.mb_onroerendezakenmetrechthebbenden IS 'commentaar view mb_onroerendezakenmetrechthebbenden:
    kadastrale percelen een appartementsrechten met rechten en rechthebbenden en objectid voor geoserver/arcgis
        beschikbare kolommen:
    * objectid: uniek id bruikbaar voor geoserver/arcgis,
    * identificatie: natuurlijke id van perceel of appartementsrecht
    * begingeldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
    * begingeldigheid_datum: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
    * type: perceel of appartement,
    * aanduiding: sectie perceelnummer,
    * aanduiding2: sectie perceelnummer appartementsindex,
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
    * mandeligheid_identif: identificatie van een mandeligheid, een gemeenschappelijk eigendom van een onroerende zaak,
    * tennamevan: natuurlijk id van rechthebbende,
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


-- materialized view zonder gegevens van natuurlijke personen
CREATE MATERIALIZED VIEW public.mb_avg_onroerendezakenmetrechthebbenden
            (
             objectid,
             identificatie,
             begingeldigheid,
             begingeldigheid_datum,
             type,
             aanduiding,
             aanduiding2,
             sectie,
             perceelnummer,
             appartementsrechtvolgnummer,
             akrkadastralegemeente,
             soortgrootte,
             kadastralegrootte,
             oppervlakte_geom,
             deelperceelnummer,
             omschr_deelperceel,
             verkoop_datum,
             aard_cultuur_onbebouwd,
             koopsom_bedrag,
             koopsom_koopjaar,
             koopsom_indicatiemeerobjecten,
             koopsom_valuta,
             loc_omschr,
             zakelijkrechtidentificatie,
             zakelijkrechtbegingeldigheid,
             mandeligheid_identif,
             tennamevan,
             aandeel,
             aard,
             isbetrokkenbij,
             soort,
             geslachtsnaam,
             voorvoegselsgeslachtsnaam,
             voornamen,
             daanduidingnaamgebruik,
             geslacht,
             naam,
             woonadres,
             geboortedatum,
             geboorteplaats,
             datumoverlijden,
             bsn,
             statutairenaam,
             rechtsvorm,
             statutairezetel,
             rsin,
             kvknummer,
             aantekeningen,
             gemeente,
             woonplaats,
             straatnaam,
             huisnummer,
             huisletter,
             huisnummertoevoeging,
             postcode,
             lon,
             lat,
             begrenzing_perceel,
             tijdstipaanbieding_stuk,
             tijdstipaanbieding_stuk2
                )
AS
SELECT row_number() OVER ()             AS objectid,
       koz.identificatie,
       koz.begingeldigheid,
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
       zrr.zr_identif                   AS zakelijkrechtidentificatie,
       zrr.ingangsdatum_recht           AS zakelijkrechtbegingeldigheid,
       zrr.mandeligheid_identif,
       zrr.subject_identif              AS tennamevan,
       zrr.aandeel,
       zrr.omschr_aard_verkregen_recht  AS aard,
       zrr.indic_betrokken_in_splitsing AS isbetrokkenbij,
       zrr.soort,
       zrr.geslachtsnaam,
       zrr.voorvoegsel                  AS voorvoegselsgeslachtsnaam,
       zrr.voornamen,
       zrr.aand_naamgebruik             AS daanduidingnaamgebruik,
       zrr.geslachtsaand                AS geslacht,
       zrr.naam,
       zrr.woonadres,
       zrr.geboortedatum,
       zrr.geboorteplaats,
       zrr.overlijdensdatum             AS datumoverlijden,
       zrr.bsn,
       zrr.organisatie_naam             AS statutairenaam,
       zrr.rechtsvorm,
       zrr.statutaire_zetel             AS statutairezetel,
       zrr.rsin,
       zrr.kvk_nummer                   AS kvknummer,
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
       st1.tijdstipaanbieding as tijdstipaanbieding_stuk,
       st2.tijdstipaanbieding as tijdstipaanbieding_stuk2
FROM brk.mb_avg_zr_rechth zrr
         RIGHT JOIN mb_kadastraleonroerendezakenmetadres koz ON zrr.koz_identif = koz.identificatie
         JOIN brk.recht r on  zrr.zr_identif = r.van
         LEFT JOIN brk.stukdeel sd1 ON sd1.identificatie = r.isgebaseerdop
         LEFT JOIN brk.stukdeel sd2 ON sd2.identificatie = r.isgebaseerdop2
         LEFT JOIN brk.stuk st1 ON sd1.deelvan = st1.identificatie
         LEFT JOIN brk.stuk st2 ON sd2.deelvan = st2.identificatie
WITH NO DATA;
-- View indexes:
CREATE INDEX mb_avg_onroerendezakenmetrechthebbenden_begrenzing_perceel_idx ON public.mb_avg_onroerendezakenmetrechthebbenden USING gist (begrenzing_perceel);
CREATE INDEX mb_avg_onroerendezakenmetrechthebbenden_identif ON public.mb_avg_onroerendezakenmetrechthebbenden USING btree (identificatie);
CREATE UNIQUE INDEX mb_avg_onroerendezakenmetrechthebbenden_objectid ON public.mb_avg_onroerendezakenmetrechthebbenden USING btree (objectid);


COMMENT ON MATERIALIZED VIEW public.mb_avg_onroerendezakenmetrechthebbenden IS 'commentaar view mb_avg_onroerendezakenmetrechthebbenden:
    kadastrale percelen een appartementsrechten met rechten en rechthebbenden en objectid voor geoserver/arcgis
        beschikbare kolommen:
    * objectid: uniek id bruikbaar voor geoserver/arcgis,
    * identificatie: natuurlijke id van perceel of appartementsrecht
    * begingeldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
    * begingeldigheid_datum: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
    * type: perceel of appartement,
    * aanduiding: sectie perceelnummer,
    * aanduiding2: sectie perceelnummer appartementsindex,
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
    * mandeligheid_identif: identificatie van een mandeligheid, een gemeenschappelijk eigendom van een onroerende zaak,
    * tennamevan: natuurlijk id van rechthebbende,
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
