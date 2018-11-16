-- 
-- upgrade PostgreSQL RSGB datamodel van 1.6.1 naar 1.6.2 
--

CREATE MATERIALIZED VIEW mb_util_app_re_kad_perceel AS
SELECT
   u1.app_re_identif,
   kp.sc_kad_identif AS perceel_identif
FROM vb_util_app_re_parent u1
JOIN kad_perceel kp ON u1.parent_identif = kp.sc_kad_identif::text
GROUP BY u1.app_re_identif, kp.sc_kad_identif
WITH NO DATA;

COMMENT ON VIEW mb_util_app_re_kad_perceel
IS 'commentaar view mb_util_app_re_kad_perceel:
utility view, niet bedoeld voor direct gebruik, met lijst van appartementsrechten met bijbehorend grondperceel
beschikbare kolommen:
* app_re_identif: natuurlijk id van appartementsrecht,
* perceel_identif: natuurlijk id van grondperceel';
CREATE INDEX mb_util_app_re_kad_perceel_identif ON mb_util_app_re_kad_perceel USING btree (app_re_identif);


DROP MATERIALIZED VIEW mb_kad_onrrnd_zk_adres;
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
            NULL::character varying(1) AS aand_soort_grootte,
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


DROP MATERIALIZED VIEW mb_avg_koz_rechth;
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

DROP MATERIALIZED VIEW mb_koz_rechth;
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
