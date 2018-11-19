-- 
-- upgrade PostgreSQL RSGB datamodel van 1.6.1 naar 1.6.2 
--

DROP MATERIALIZED VIEW mb_kad_onrrnd_zk_adres;
DROP MATERIALIZED VIEW mb_koz_rechth;
DROP MATERIALIZED VIEW mb_avg_koz_rechth;
DROP MATERIALIZED VIEW mb_kad_onrrnd_zk_archief;
DROP VIEW vb_avg_koz_rechth;
DROP VIEW vb_koz_rechth;
DROP VIEW vb_kad_onrrnd_zk_adres;
DROP VIEW vb_kad_onrrnd_zk_archief;

-- issue #565
alter table kad_perceel alter column aand_soort_grootte type character varying(2);
alter table kad_perceel_archief alter column aand_soort_grootte type character varying(2);

CREATE OR REPLACE VIEW
    vb_kad_onrrnd_zk_archief
    (
        objectid,
        koz_identif,
        begin_geldigheid,
        eind_geldigheid,
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
    ) AS
SELECT
    (row_number() OVER ())::INTEGER AS objectid,
    qry.identif as koz_identif,
    koza.dat_beg_geldh AS begin_geldigheid,
    koza.datum_einde_geldh AS eind_geldigheid,
    qry.type,
    (((COALESCE(qry.ka_sectie, ''::CHARACTER VARYING))::text || ' '::text) || (COALESCE
    (qry.ka_perceelnummer, ''::CHARACTER VARYING))::CHARACTER VARYING(6)) AS aanduiding,
    (((((((COALESCE(qry.ka_kad_gemeentecode, ''::CHARACTER VARYING))::text || ' '::text) ||
    (COALESCE(qry.ka_sectie, ''::CHARACTER VARYING))::text) || ' '::text) || (COALESCE
    (qry.ka_perceelnummer, ''::CHARACTER VARYING))::text) || ' '::text) || (COALESCE
    (qry.ka_appartementsindex, ''::CHARACTER VARYING))::CHARACTER VARYING(20)) AS aanduiding2,
    qry.ka_sectie                                             AS sectie,
    qry.ka_perceelnummer                                      AS perceelnummer,
    qry.ka_appartementsindex                                  AS appartementsindex,
    qry.ka_kad_gemeentecode                                   AS gemeentecode,
    qry.aand_soort_grootte,
    qry.grootte_perceel,
    qry.ka_deelperceelnummer AS deelperceelnummer,
    qry.omschr_deelperceel,
    koza.cu_aard_cultuur_onbebouwd AS aard_cultuur_onbebouwd,
    koza.ks_bedrag                 AS bedrag,
    koza.ks_koopjaar               AS koopjaar,
    koza.ks_meer_onroerendgoed     AS meer_onroerendgoed,
    koza.ks_valutasoort            AS valutasoort,
    koza.lo_loc__omschr            AS loc_omschr,
    kozhr.fk_sc_lh_koz_kad_identif AS overgegaan_in,
    qry.begrenzing_perceel::geometry(MULTIPOLYGON,28992)
FROM
    (
        SELECT
            pa.sc_kad_identif   AS identif,
            pa.sc_dat_beg_geldh AS dat_beg_geldh,
            'perceel'::CHARACTER VARYING(11)     AS type,
            pa.ka_sectie,
            pa.ka_perceelnummer,
            NULL::CHARACTER VARYING(4) AS ka_appartementsindex,
            pa.ka_kad_gemeentecode,
            pa.aand_soort_grootte,
            pa.grootte_perceel,
            pa.ka_deelperceelnummer,
            pa.omschr_deelperceel,
            pa.begrenzing_perceel
        FROM
            kad_perceel_archief pa
        UNION ALL
        SELECT
            ara.sc_kad_identif   AS identif,
            ara.sc_dat_beg_geldh AS dat_beg_geldh,
            'appartement'::CHARACTER VARYING(11)  AS type,
            ara.ka_sectie,
            ara.ka_perceelnummer,
            ara.ka_appartementsindex,
            ara.ka_kad_gemeentecode,
            NULL::CHARACTER VARYING(2)    AS aand_soort_grootte,
            NULL::NUMERIC(8,0)            AS grootte_perceel,
            NULL::CHARACTER VARYING(4)    AS ka_deelperceelnummer,
            NULL::CHARACTER VARYING(1120) AS omschr_deelperceel,
            NULL                          AS begrenzing_perceel
        FROM
            app_re_archief ara ) qry
JOIN
    kad_onrrnd_zk_archief koza
ON
    koza.kad_identif = qry.identif
AND qry.dat_beg_geldh = koza.dat_beg_geldh
JOIN
    (
        SELECT
            ikoza.kad_identif,
            MAX(ikoza.dat_beg_geldh) bdate
        FROM
            kad_onrrnd_zk_archief ikoza
        GROUP BY
            ikoza.kad_identif ) nqry
ON
    nqry.kad_identif = koza.kad_identif
AND nqry.bdate = koza.dat_beg_geldh
LEFT JOIN
    kad_onrrnd_zk_his_rel kozhr
ON
    (
        kozhr.fk_sc_rh_koz_kad_identif = koza.kad_identif)
ORDER BY
    bdate DESC ;
COMMENT ON VIEW vb_kad_onrrnd_zk_archief
IS
    'commentaar view vb_kad_onrrnd_zk_archief:
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
* begrenzing_perceel: perceelvlak
';


CREATE OR REPLACE VIEW
    vb_kad_onrrnd_zk_adres
    (
        objectid,
        koz_identif,
        begin_geldigheid,
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
    ) AS
SELECT
    (row_number() OVER ())::INTEGER AS objectid,
    qry.identif as koz_identif,
    koz.dat_beg_geldh AS begin_geldigheid,
    bok.fk_nn_lh_tgo_identif                       AS benoemdobj_identif,
    qry.type,
    COALESCE(qry.ka_sectie, '') || ' ' || COALESCE(qry.ka_perceelnummer, '') AS aanduiding,
    COALESCE(qry.ka_kad_gemeentecode, '') || ' ' || COALESCE(qry.ka_sectie, '') || ' ' || COALESCE(qry.ka_perceelnummer, '') || ' ' || COALESCE(qry.ka_appartementsindex, '') AS aanduiding2,
    qry.ka_sectie,
    qry.ka_perceelnummer,
    qry.ka_appartementsindex,
    qry.ka_kad_gemeentecode,
    qry.aand_soort_grootte,
    qry.grootte_perceel,
    st_area(begrenzing_perceel) as oppervlakte_geom,
    qry.ka_deelperceelnummer,
    qry.omschr_deelperceel,
    b.datum,
    koz.cu_aard_cultuur_onbebouwd,
    koz.ks_bedrag,
    koz.ks_koopjaar,
    koz.ks_meer_onroerendgoed,
    koz.ks_valutasoort,
    koz.lo_loc__omschr,
    bola.gemeente,
    bola.woonplaats,
    bola.straatnaam,
    bola.huisnummer,
    bola.huisletter,
    bola.huisnummer_toev,
    bola.postcode,
    st_x(st_transform(st_setsrid(st_centroid(qry.begrenzing_perceel), 28992), 4326) ) as lon,
    st_y(st_transform(st_setsrid(st_centroid(qry.begrenzing_perceel), 28992), 4326) ) as lat,
    qry.begrenzing_perceel
FROM
    (
        SELECT
            p.sc_kad_identif AS identif,
            'perceel'::CHARACTER VARYING(11)  AS type,
            p.ka_sectie,
            p.ka_perceelnummer,
            NULL::CHARACTER VARYING(4) AS ka_appartementsindex,
            p.ka_kad_gemeentecode,
            p.aand_soort_grootte,
            p.grootte_perceel,
            p.ka_deelperceelnummer,
            p.omschr_deelperceel,
            p.begrenzing_perceel
        FROM
            kad_perceel p
        UNION ALL
        SELECT
            ar.sc_kad_identif   AS identif,
            'appartement'::CHARACTER VARYING(11) AS type,
            ar.ka_sectie,
            ar.ka_perceelnummer,
            ar.ka_appartementsindex,
            ar.ka_kad_gemeentecode,
            NULL::CHARACTER VARYING(2)    AS aand_soort_grootte,
            NULL::NUMERIC(8,0)            AS grootte_perceel,
            NULL::CHARACTER VARYING(4)    AS ka_deelperceelnummer,
            NULL::CHARACTER VARYING(1120) AS omschr_deelperceel,
            kp.begrenzing_perceel
        FROM
            ((vb_util_app_re_kad_perceel v
        JOIN
            kad_perceel kp
        ON
            (((
                        v.perceel_identif)::NUMERIC = kp.sc_kad_identif)))
        JOIN
            app_re ar
        ON
            (((
                        v.app_re_identif)::NUMERIC = ar.sc_kad_identif)))) qry
JOIN
    kad_onrrnd_zk koz
ON
    (
        koz.kad_identif = qry.identif)
LEFT JOIN
    benoemd_obj_kad_onrrnd_zk bok
ON
    (
        bok.fk_nn_rh_koz_kad_identif = qry.identif)
LEFT JOIN
    vb_benoemd_obj_adres bola
ON
    bok.fk_nn_lh_tgo_identif = bola.benoemdobj_identif
LEFT JOIN
    (
        SELECT
            brondocument.ref_id,
            MAX(brondocument.datum) AS datum
        FROM
            brondocument
        WHERE
            ((
                    brondocument.omschrijving)::text = 'Akte van Koop en Verkoop'::text)
        GROUP BY
            brondocument.ref_id) b
ON
    (
        koz.kad_identif::text = b.ref_id);
COMMENT ON VIEW vb_kad_onrrnd_zk_adres
IS
    'commentaar view vb_kad_onrrnd_zk_adres:
alle kadastrale onroerende zaken (perceel en appartementsrecht) met opgezochte verkoop datum, objectid voor geoserver/arcgis en BAG adres

beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* koz_identif: natuurlijke id van perceel of appartementsrecht
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
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

CREATE OR REPLACE VIEW
    vb_koz_rechth
    (
        objectid,
        koz_identif,
        begin_geldigheid,
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
        subject_identif,
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
    ) AS
SELECT
    (row_number() OVER ())::INTEGER AS objectid,
    koz.koz_identif,
    koz.begin_geldigheid,
    koz.type,
    COALESCE(koz.sectie, '') || ' ' || COALESCE(koz.perceelnummer, '') AS aanduiding,
    COALESCE(koz.gemeentecode, '') || ' ' || COALESCE(koz.sectie, '') || ' ' || COALESCE(koz.perceelnummer, '') || ' ' || COALESCE(koz.appartementsindex, '') AS aanduiding2,
    koz.sectie,
    koz.perceelnummer,
    koz.appartementsindex,
    koz.gemeentecode,
    koz.aand_soort_grootte,
    koz.grootte_perceel,
    koz.oppervlakte_geom as oppervlakte_geom,
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
    zrr.subject_identif,
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
FROM
    (vb_zr_rechth zrr
RIGHT JOIN
    vb_kad_onrrnd_zk_adres koz
ON
    ((
            zrr.koz_identif = koz.koz_identif)));
COMMENT ON VIEW vb_koz_rechth
IS
    'commentaar view vb_koz_rechth:
kadastrale percelen een appartementsrechten met rechten en rechthebbenden en objectid voor geoserver/arcgis
beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* koz_identif: natuurlijke id van perceel of appartementsrecht
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
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
* subject_identif: natuurlijk id van rechthebbende,
* aandeel: samenvoeging van teller en noemer (1/2),
* omschr_aard_verkregenr_recht: tekstuele omschrijving aard recht,
* indic_betrokken_in_splitsing: -,
* soort: soort subject zoals natuurlijk, niet-natuurlijk enz.
* geslachtsnaam: -
* voorvoegsel: -
* voornamen: -
* aand_naamgebruik:
- E (= Eigen geslachtsnaam)
- N (=Geslachtsnaam echtgenoot/geregistreerd partner na eigen geslachtsnaam)
- P (= Geslachtsnaam echtgenoot/geregistreerd partner)
- V (= Geslachtsnaam evhtgenoot/geregistreerd partner voor eigen geslachtsnaam)
* geslachtsaand: M/V
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


CREATE OR REPLACE VIEW
    vb_avg_koz_rechth
    (
        objectid,
        koz_identif,
        begin_geldigheid,
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
        subject_identif,
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
    ) AS
SELECT
    (row_number() OVER ())::INTEGER AS objectid,
    koz.koz_identif as koz_identif,
    koz.begin_geldigheid,
    koz.type,
    COALESCE(koz.sectie, '') || ' ' || COALESCE(koz.perceelnummer, '') AS aanduiding,
    COALESCE(koz.gemeentecode, '') || ' ' || COALESCE(koz.sectie, '') || ' ' || COALESCE(koz.perceelnummer, '') || ' ' || COALESCE(koz.appartementsindex, '') AS aanduiding2,
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
    zrr.subject_identif,
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
FROM
    (vb_avg_zr_rechth zrr
RIGHT JOIN
    vb_kad_onrrnd_zk_adres koz
ON
    ((
            zrr.koz_identif = koz.koz_identif)));
COMMENT ON VIEW vb_avg_koz_rechth
IS
    'commentaar view vb_avg_koz_rechth:
kadastrale percelen een appartementsrechten met rechten en rechthebbenden geschoond voor avg en objectid voor geoserver/arcgis
beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* koz_identif: natuurlijke id van perceel of appartementsrecht
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
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
* subject_identif: natuurlijk id van rechthebbende,
* aandeel: samenvoeging van teller en noemer (1/2),
* omschr_aard_verkregenr_recht: tekstuele omschrijving aard recht,
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


CREATE MATERIALIZED VIEW mb_kad_onrrnd_zk_archief AS
SELECT
    *
FROM
    vb_kad_onrrnd_zk_archief WITH NO DATA;
CREATE UNIQUE INDEX mb_kad_onrrnd_zk_archief_objectid ON mb_kad_onrrnd_zk_archief USING btree (objectid);
CREATE INDEX mb_kad_onrrnd_zk_archief_identif ON mb_kad_onrrnd_zk_archief USING btree (koz_identif);
CREATE INDEX mb_kad_onrrnd_zk_archief_begrenzing_perceel_idx ON mb_kad_onrrnd_zk_archief USING gist (begrenzing_perceel);



CREATE MATERIALIZED VIEW mb_util_app_re_kad_perceel AS
SELECT
   u1.app_re_identif,
   kp.sc_kad_identif AS perceel_identif
FROM vb_util_app_re_parent u1
JOIN kad_perceel kp ON u1.parent_identif = kp.sc_kad_identif::text
GROUP BY u1.app_re_identif, kp.sc_kad_identif
WITH NO DATA;

COMMENT ON MATERIALIZED VIEW mb_util_app_re_kad_perceel
IS 'commentaar view mb_util_app_re_kad_perceel:
utility view, niet bedoeld voor direct gebruik, met lijst van appartementsrechten met bijbehorend grondperceel
beschikbare kolommen:
* app_re_identif: natuurlijk id van appartementsrecht,
* perceel_identif: natuurlijk id van grondperceel';
CREATE INDEX mb_util_app_re_kad_perceel_identif ON mb_util_app_re_kad_perceel USING btree (app_re_identif);


CREATE MATERIALIZED VIEW mb_kad_onrrnd_zk_adres AS
 SELECT row_number() OVER ()::integer AS objectid,
    qry.identif AS koz_identif,
    koz.dat_beg_geldh AS begin_geldigheid,
    bok.fk_nn_lh_tgo_identif AS benoemdobj_identif,
    qry.type,
    (COALESCE(qry.ka_sectie, ''::character varying)::text || ' '::text) || COALESCE(qry.ka_perceelnummer, ''::character varying)::text AS aanduiding,
    (((((COALESCE(qry.ka_kad_gemeentecode, ''::character varying)::text || ' '::text) || COALESCE(qry.ka_sectie, ''::character varying)::text) || ' '::text) || COALESCE(qry.ka_perceelnummer, ''::character varying)::text) || ' '::text) || COALESCE(qry.ka_appartementsindex, ''::character varying)::text AS aanduiding2,
    qry.ka_sectie AS sectie,
    qry.ka_perceelnummer AS perceelnummer,
    qry.ka_appartementsindex AS appartementsindex,
    qry.ka_kad_gemeentecode AS gemeentecode,
    qry.aand_soort_grootte,
    qry.grootte_perceel,
    st_area(qry.begrenzing_perceel) AS oppervlakte_geom,
    qry.ka_deelperceelnummer AS deelperceelnummer,
    qry.omschr_deelperceel,
    b.datum AS verkoop_datum,
    koz.cu_aard_cultuur_onbebouwd AS aard_cultuur_onbebouwd,
    koz.ks_bedrag AS bedrag,
    koz.ks_koopjaar AS koopjaar,
    koz.ks_meer_onroerendgoed AS meer_onroerendgoed,
    koz.ks_valutasoort AS valutasoort,
    koz.lo_loc__omschr AS loc_omschr,
    bola.gemeente,
    bola.woonplaats,
    bola.straatnaam,
    bola.huisnummer,
    bola.huisletter,
    bola.huisnummer_toev,
    bola.postcode,
    st_x(st_transform(st_setsrid(st_centroid(qry.begrenzing_perceel), 28992), 4326)) AS lon,
    st_y(st_transform(st_setsrid(st_centroid(qry.begrenzing_perceel), 28992), 4326)) AS lat,
    qry.begrenzing_perceel
   FROM ( SELECT p.sc_kad_identif AS identif,
            'perceel'::character varying(11) AS type,
            p.ka_sectie,
            p.ka_perceelnummer,
            NULL::character varying(4) AS ka_appartementsindex,
            p.ka_kad_gemeentecode,
            p.aand_soort_grootte,
            p.grootte_perceel,
            p.ka_deelperceelnummer,
            p.omschr_deelperceel,
            p.begrenzing_perceel
           FROM kad_perceel p
        UNION ALL
         SELECT ar.sc_kad_identif AS identif,
            'appartement'::character varying(11) AS type,
            ar.ka_sectie,
            ar.ka_perceelnummer,
            ar.ka_appartementsindex,
            ar.ka_kad_gemeentecode,
            NULL::character varying(2) AS aand_soort_grootte,
            NULL::numeric(8,0) AS grootte_perceel,
            NULL::character varying(4) AS ka_deelperceelnummer,
            NULL::character varying(1120) AS omschr_deelperceel,
            kp.begrenzing_perceel
           FROM mb_util_app_re_kad_perceel v
             JOIN kad_perceel kp ON v.perceel_identif = kp.sc_kad_identif
             JOIN app_re ar ON v.app_re_identif::numeric = ar.sc_kad_identif) qry
     JOIN kad_onrrnd_zk koz ON koz.kad_identif = qry.identif
     LEFT JOIN benoemd_obj_kad_onrrnd_zk bok ON bok.fk_nn_rh_koz_kad_identif = qry.identif
     LEFT JOIN mb_benoemd_obj_adres bola ON bok.fk_nn_lh_tgo_identif::text = bola.benoemdobj_identif::text
     LEFT JOIN ( SELECT brondocument.ref_id,
            max(brondocument.datum) AS datum
           FROM brondocument
          WHERE brondocument.omschrijving::text = 'Akte van Koop en Verkoop'::text
          GROUP BY brondocument.ref_id) b ON koz.kad_identif::text = b.ref_id::text
 WITH NO DATA;

CREATE UNIQUE INDEX mb_kad_onrrnd_zk_adres_objectid ON mb_kad_onrrnd_zk_adres USING btree (objectid);
CREATE INDEX mb_kad_onrrnd_zk_adres_identif ON mb_kad_onrrnd_zk_adres USING btree (koz_identif);
CREATE INDEX mb_kad_onrrnd_zk_adres_begrenzing_perceel_idx ON mb_kad_onrrnd_zk_adres USING gist (begrenzing_perceel);


CREATE MATERIALIZED VIEW mb_avg_koz_rechth AS
 SELECT row_number() OVER ()::integer AS objectid,
    koz.koz_identif,
    koz.begin_geldigheid,
    koz.type,
    (COALESCE(koz.sectie, ''::character varying)::text || ' '::text) || COALESCE(koz.perceelnummer, ''::character varying)::text AS aanduiding,
    (((((COALESCE(koz.gemeentecode, ''::character varying)::text || ' '::text) || COALESCE(koz.sectie, ''::character varying)::text) || ' '::text) || COALESCE(koz.perceelnummer, ''::character varying)::text) || ' '::text) || COALESCE(koz.appartementsindex, ''::character varying)::text AS aanduiding2,
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
    zrr.subject_identif,
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
   FROM vb_avg_zr_rechth zrr
     RIGHT JOIN mb_kad_onrrnd_zk_adres koz ON zrr.koz_identif = koz.koz_identif
WITH NO DATA;

CREATE UNIQUE INDEX mb_avg_koz_rechth_objectid ON mb_avg_koz_rechth USING btree (objectid);
CREATE INDEX mb_avg_koz_rechth_identif ON mb_avg_koz_rechth USING btree (koz_identif);
CREATE INDEX mb_avg_koz_rechth_begrenzing_perceel_idx ON mb_avg_koz_rechth USING gist (begrenzing_perceel);


CREATE MATERIALIZED VIEW mb_koz_rechth AS
 SELECT
    row_number() OVER()::integer AS objectid,
    koz.koz_identif,
    koz.begin_geldigheid,
    koz.type,
    (COALESCE(koz.sectie, ''::character varying)::text || ' '::text) || COALESCE(koz.perceelnummer, ''::character varying)::text AS aanduiding,
    (((((COALESCE(koz.gemeentecode, ''::character varying)::text || ' '::text) || COALESCE(koz.sectie, ''::character varying)::text) || ' '::text) || COALESCE(koz.perceelnummer, ''::character varying)::text) || ' '::text) || COALESCE(koz.appartementsindex, ''::character varying)::text AS aanduiding2,
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
    zrr.subject_identif,
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
   FROM vb_zr_rechth zrr
     RIGHT JOIN mb_kad_onrrnd_zk_adres koz ON zrr.koz_identif = koz.koz_identif
WITH NO DATA;

CREATE UNIQUE INDEX mb_koz_rechth_objectid ON mb_koz_rechth USING btree (objectid);
CREATE INDEX mb_koz_rechth_identif ON mb_koz_rechth USING btree (koz_identif);
CREATE INDEX mb_koz_rechth_begrenzing_perceel_idx ON mb_koz_rechth USING gist (begrenzing_perceel);


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.6.1_naar_1.6.2','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.2' WHERE naam='brmoversie';
