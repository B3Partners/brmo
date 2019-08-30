/*
Views for visualizing the BRK data.
versie 2
30-8-2019
*/
--drop view vb_util_app_re_parent;
--drop view vb_util_app_re_parent_2;
--drop view vb_util_app_re_parent_3;
--drop view vb_util_app_re_splitsing;
--drop view vb_util_zk_recht;

--drop materialized view mb_kad_onrrnd_zk_archief;
--drop materialized view mb_avg_subject;
--drop materialized view mb_avg_zr_rechth;
--drop materialized view mb_kad_onrrnd_zk_adres;
--drop materialized view mb_koz_rechth;
--drop materialized view mb_avg_koz_rechth;
--drop materialized view mb_subject;
--drop materialized view mb_zr_rechth;
--drop materialized view mb_util_app_re_kad_perceel;
--drop materialized view mb_percelenkaart;

--drop index mb_avg_koz_rechth_begr_p_idx;
--drop index mb_avg_koz_rechth_identif;
--drop index mb_kad_onrrnd_zk_adres_identif;
--drop index mb_kad_onrrnd_zk_adr_bgrgpidx;
--drop index mb_kad_onrrnd_zk_a_bgrgpidx;
--drop index mb_kad_onrrnd_zk_a_identif;
--drop index mb_koz_rechth_begr_prcl_idx;
--drop index mb_koz_rechth_identif;
--drop index mb_zr_rechth_identif;
--drop index mb_avg_subject_identif;
--drop index mb_avg_zr_rechth_identif;
--drop index mb_subject_identif;
--drop index mb_util_app_re_kad_perceel_id;
--drop index mb_avg_koz_rechth_objectid;
--drop index mb_kad_onrrnd_zk_adres_objidx;
--drop index mb_kad_onrrnd_zk_a_objidx;
--drop index mb_koz_rechth_objectid;
--drop index mb_zr_rechth_objectid;
--drop index mb_avg_subject_objectid;
--drop index mb_avg_zr_rechth_objectid;
--drop index mb_subject_objectid;
--drop index mb_percelenkaart_objectid;
--drop index mb_percelenkaart_identif;
--drop index mb_percelenkaart_begrenzing_perceel_idx;


--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'mb_kad_onrrnd_zk_archief', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'mb_zr_rechth', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'mb_avg_subject', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'mb_avg_zr_rechth', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'mb_kad_onrrnd_zk_adres', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'mb_koz_rechth', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'mb_avg_koz_rechth', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'mb_subject', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'mb_zr_rechth', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'mb_kad_onrrnd_zk_archief', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'mb_percelenkaart', 'objectid', 'assigned');

--BEGIN
-- run 1
--REFRESH MATERIALIZED VIEW mb_subject;
--REFRESH MATERIALIZED VIEW mb_util_app_re_kad_perceel;
--REFRESH MATERIALIZED VIEW mb_kad_onrrnd_zk_archief;
--REFRESH MATERIALIZED VIEW mb_percelenkaart;
-- run 2
--REFRESH MATERIALIZED VIEW mb_avg_subject;
--REFRESH MATERIALIZED VIEW mb_zr_rechth;
-- run 3
--REFRESH MATERIALIZED VIEW mb_kad_onrrnd_zk_adres;
--REFRESH MATERIALIZED VIEW mb_avg_zr_rechth;
-- run 4
--REFRESH MATERIALIZED VIEW mb_koz_rechth;
--REFRESH MATERIALIZED VIEW mb_avg_koz_rechth;
--END

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
    ) AS
SELECT
    (row_number() OVER ())::INTEGER AS objectid,
    s.identif as subject_identif,
    s.clazz                         AS soort,
    np.nm_geslachtsnaam             AS geslachtsnaam,
    np.nm_voorvoegsel_geslachtsnaam AS voorvoegsel,
    np.nm_voornamen                 AS voornamen,
    np.aand_naamgebruik,
    CASE
        WHEN ((np.geslachtsaand)::text = '1'::text)
        THEN 'M'::CHARACTER VARYING(1)
        WHEN ((np.geslachtsaand)::text = '2'::text)
        THEN 'V'::CHARACTER VARYING(1)
        WHEN ((np.geslachtsaand)::text = '3'::text)
        THEN 'X'::CHARACTER VARYING(1)
        ELSE np.geslachtsaand
    END AS geslachtsaand,
    CASE
        WHEN (nnp.naam IS NOT NULL)
        THEN (nnp.naam)::CHARACTER VARYING(1000)
        ELSE ((((((COALESCE(np.nm_voornamen, ''::CHARACTER VARYING))::text || ' '::text) ||
            (COALESCE(np.nm_voorvoegsel_geslachtsnaam, ''::CHARACTER VARYING))::text) || ' '::text)
            || (COALESCE(np.nm_geslachtsnaam, ''::CHARACTER VARYING))::text))::CHARACTER VARYING
            (1000)
    END                     AS naam,
    inp.va_loc_beschrijving AS woonadres,
    CASE
        WHEN ((s.clazz)::text = 'INGESCHREVEN NATUURLIJK PERSOON'::text)
            AND LENGTH(inp.gb_geboortedatum::text)=8
        THEN (
                SUBSTRING(TO_CHAR(inp.gb_geboortedatum,'99999999'),2,4) || '-' || 
                SUBSTRING(TO_CHAR(inp.gb_geboortedatum,'99999999'),6,2) || '-' || 
                SUBSTRING(TO_CHAR(inp.gb_geboortedatum,'99999999'),8,2) 
              )::VARCHAR(10)
        WHEN ((s.clazz)::text = 'ANDER NATUURLIJK PERSOON'::text)
            AND LENGTH(anp.geboortedatum::text)=8
        THEN (
                SUBSTRING(TO_CHAR(anp.geboortedatum, '99999999'),2,4) || '-' || 
                SUBSTRING(TO_CHAR(anp.geboortedatum, '99999999'),6,2) || '-' || 
                SUBSTRING(TO_CHAR(anp.geboortedatum, '99999999'),8,2)  
              )::VARCHAR(10)
        WHEN ((s.clazz)::text = 'INGESCHREVEN NATUURLIJK PERSOON'::text)
           AND LENGTH(inp.gb_geboortedatum::text)=5
        THEN '0001-01-01'::VARCHAR(10)
        WHEN ((s.clazz)::text = 'ANDER NATUURLIJK PERSOON'::text)
            AND LENGTH(anp.geboortedatum::text)=5
        THEN '0001-01-01'::VARCHAR(10)
        ELSE NULL::VARCHAR(10)
    END                   AS geboortedatum,
    inp.gb_geboorteplaats AS geboorteplaats,
    CASE
        WHEN ((s.clazz)::text = 'INGESCHREVEN NATUURLIJK PERSOON'::text)
            AND LENGTH(inp.ol_overlijdensdatum::text)=8
        THEN (
                SUBSTRING(TO_CHAR(inp.ol_overlijdensdatum,'99999999'),2,4) || '-' || 
                SUBSTRING(TO_CHAR(inp.ol_overlijdensdatum,'99999999'),6,2) || '-' || 
                SUBSTRING(TO_CHAR(inp.ol_overlijdensdatum,'99999999'),8,2) 
              )::VARCHAR(10)
        WHEN ((s.clazz)::text = 'ANDER NATUURLIJK PERSOON'::text)
            AND LENGTH(anp.overlijdensdatum::text)=8
        THEN (
                SUBSTRING(TO_CHAR(anp.overlijdensdatum, '99999999'),2,4) || '-' || 
                SUBSTRING(TO_CHAR(anp.overlijdensdatum, '99999999'),6,2) || '-' || 
                SUBSTRING(TO_CHAR(anp.overlijdensdatum, '99999999'),8,2)  
              )::VARCHAR(10)
        WHEN ((s.clazz)::text = 'INGESCHREVEN NATUURLIJK PERSOON'::text)
           AND LENGTH(inp.ol_overlijdensdatum::text)=5
        THEN '0001-01-01'::VARCHAR(10)
        WHEN ((s.clazz)::text = 'ANDER NATUURLIJK PERSOON'::text)
            AND LENGTH(anp.overlijdensdatum::text)=5
        THEN '0001-01-01'::VARCHAR(10)
        ELSE NULL::VARCHAR(10)
    END                   AS overlijdensdatum,
    inp.bsn::INTEGER,
    nnp.naam AS organisatie_naam,
    innp.rechtsvorm,
    innp.statutaire_zetel,
    innp.rsin::INTEGER,
    s.kvk_nummer::INTEGER
FROM
    (((((subject s
LEFT JOIN
    nat_prs np
ON
    (((
                s.identif)::text = (np.sc_identif)::text)))
LEFT JOIN
    ingeschr_nat_prs inp
ON
    (((
                inp.sc_identif)::text = (np.sc_identif)::text)))
LEFT JOIN
    ander_nat_prs anp
ON
    (((
                anp.sc_identif)::text = (np.sc_identif)::text)))
LEFT JOIN
    niet_nat_prs nnp
ON
    (((
                nnp.sc_identif)::text = (s.identif)::text)))
LEFT JOIN
    ingeschr_niet_nat_prs innp
ON
    (((
                innp.sc_identif)::text = (nnp.sc_identif)::text)));

CREATE UNIQUE INDEX mb_subject_objectid ON mb_subject USING btree (objectid);
CREATE INDEX mb_subject_identif ON mb_subject USING btree (subject_identif);

COMMENT ON MATERIALIZED VIEW mb_subject
IS
    'commentaar view mb_subject:
samenvoeging alle soorten subjecten: natuurlijk en niet-natuurlijk.

beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* subject_identif: natuurlijke id van subject      
* soort: soort subject zoals natuurlijk, niet-natuurlijk enz.  
* geslachtsnaam: -       
* voorvoegsel: -     
* voornamen: -     
* aand_naamgebruik:        
- E (= Eigen geslachtsnaam)        
- N (=Geslachtsnaam echtgenoot/geregistreerd partner na eigen geslachtsnaam)        
- P (= Geslachtsnaam echtgenoot/geregistreerd partner)        
- V (= Geslachtsnaam evhtgenoot/geregistreerd partner voor eigen geslachtsnaam)        
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
* kvk_nummer: -
'
    ;

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
    ) AS
SELECT
    s.objectid,
    s.subject_identif as subject_identif,
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
    s.rsin::INTEGER,
    s.kvk_nummer::INTEGER
FROM
    mb_subject s WITH NO DATA;
    
CREATE UNIQUE INDEX mb_avg_subject_objectid ON mb_avg_subject USING btree (objectid);
CREATE INDEX mb_avg_subject_identif ON mb_avg_subject USING btree (subject_identif);
    
COMMENT ON MATERIALIZED VIEW mb_avg_subject
IS
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
'
    ;

--drop view vb_util_app_re_splitsing cascade;
CREATE OR REPLACE VIEW
    vb_util_app_re_splitsing AS
SELECT
    b1.ref_id AS child_identif,
    min(b2.ref_id) AS parent_identif
FROM
    brondocument b1
JOIN
    brondocument b2
ON
    b2.identificatie = b1.identificatie
WHERE
    (
        b2.omschrijving = 'betrokkenBij Ondersplitsing'
    OR  b2.omschrijving = 'betrokkenBij HoofdSplitsing')
AND (
        b1.omschrijving = 'ontstaanUit Ondersplitsing'
    OR  b1.omschrijving = 'ontstaanUit HoofdSplitsing')
GROUP BY
    b1.ref_id;
    
--drop view vb_util_app_re_parent_3 cascade;
CREATE OR REPLACE VIEW
    vb_util_app_re_parent_3 AS
SELECT
    re.sc_kad_identif::text AS app_re_identif,
    sp.parent_identif
FROM
    app_re re
LEFT JOIN
    vb_util_app_re_splitsing sp
ON
    re.sc_kad_identif::text = sp.child_identif
GROUP BY
    re.sc_kad_identif::text,
    sp.parent_identif;
    
--drop view vb_util_app_re_parent_2 cascade;
CREATE OR REPLACE VIEW
    vb_util_app_re_parent_2 AS
SELECT
    u1.app_re_identif,
    CASE
        WHEN sp.parent_identif IS NULL
        THEN u1.parent_identif
        ELSE sp.parent_identif
    END AS parent_identif
FROM
    vb_util_app_re_parent_3 u1
LEFT JOIN
    vb_util_app_re_splitsing sp
ON
    u1.parent_identif = sp.child_identif;
    
--drop view vb_util_app_re_parent cascade;
CREATE OR REPLACE VIEW
    vb_util_app_re_parent AS
SELECT
    u2.app_re_identif,
    CASE
        WHEN sp.parent_identif IS NULL
        THEN u2.parent_identif
        ELSE sp.parent_identif
    END AS parent_identif
FROM
    vb_util_app_re_parent_2 u2
LEFT JOIN
    vb_util_app_re_splitsing sp
ON
    u2.parent_identif = sp.child_identif;
    
CREATE MATERIALIZED VIEW mb_util_app_re_kad_perceel AS
SELECT
   u1.app_re_identif,
   kp.sc_kad_identif AS perceel_identif
FROM vb_util_app_re_parent u1
JOIN kad_perceel kp ON u1.parent_identif = kp.sc_kad_identif::text
GROUP BY u1.app_re_identif, kp.sc_kad_identif WITH NO DATA;

CREATE INDEX mb_util_app_re_kad_perceel_identif ON mb_util_app_re_kad_perceel USING btree (app_re_identif);

COMMENT ON MATERIALIZED VIEW mb_util_app_re_kad_perceel
IS 'commentaar view mb_util_app_re_kad_perceel:
utility view, niet bedoeld voor direct gebruik, met lijst van appartementsrechten met bijbehorend grondperceel
beschikbare kolommen:
* app_re_identif: natuurlijk id van appartementsrecht,
* perceel_identif: natuurlijk id van grondperceel';
    
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
    ) AS
SELECT
    (row_number() OVER ())::INTEGER AS objectid,
    qry.identif as koz_identif,
    koz.dat_beg_geldh AS begin_geldigheid,
    to_date(koz.dat_beg_geldh, 'YYYY-MM-DD'::text) AS begin_geldigheid_datum,
    bok.fk_nn_lh_tgo_identif                       AS benoemdobj_identif,
    qry.type,
    COALESCE(qry.ka_sectie, '') || ' ' || COALESCE(qry.ka_perceelnummer, '') AS aanduiding,
    COALESCE(qry.ka_kad_gemeentecode, '') || ' ' || COALESCE(qry.ka_sectie, '') || ' ' || COALESCE(qry.ka_perceelnummer, '') || ' ' || COALESCE(qry.ka_appartementsindex, '') AS aanduiding2,
    qry.ka_sectie,
    qry.ka_perceelnummer,
    qry.ka_appartementsindex,
    qry.ka_kad_gemeentecode,
    qry.aand_soort_grootte,
    qry.grootte_perceel::INTEGER,
    st_area(begrenzing_perceel) as oppervlakte_geom,
    qry.ka_deelperceelnummer,
    qry.omschr_deelperceel,
    b.datum,
    koz.cu_aard_cultuur_onbebouwd,
    koz.ks_bedrag::INTEGER,
    koz.ks_koopjaar,
    koz.ks_meer_onroerendgoed,
    koz.ks_valutasoort,
    koz.lo_loc__omschr,
    array_to_string(
        (SELECT array_agg(('id: '::text || COALESCE(koza.kadaster_identif_aantek, ''::character varying)::text || ', '::text || 
        'aard: '::text || COALESCE(koza.aard_aantek_kad_obj, ''::character varying)::text || ', '::text || 
       'begin: '::text || COALESCE(koza.begindatum_aantek_kad_obj, ''::character varying)::text || ', '::text || 
       'beschrijving: '::text || COALESCE(koza.beschrijving_aantek_kad_obj, ''::character varying)::text || ', '::text || 
       'eind: '::text || COALESCE(koza.eindd_aantek_kad_obj, ''::character varying)::text || ', '::text || 
       'koz-id: '::text || COALESCE(koza.fk_4koz_kad_identif, 0::NUMERIC(15,0))::NUMERIC(15,0) || ', '::text || 
       'subject-id: '::text || COALESCE(koza.fk_5pes_sc_identif, ''::character varying)::text || '; '::text))
       FROM kad_onrrnd_zk_aantek koza
       WHERE koza.fk_4koz_kad_identif = koz.kad_identif), ' & ') as aantekeningen,   
    bola.na_identif,
    bola.na_status,
    bola.gemeente,
    bola.woonplaats,
    bola.straatnaam,
    bola.huisnummer::INTEGER,
    bola.huisletter,
    bola.huisnummer_toev,
    bola.postcode,
    bola.gebruiksdoelen,
    bola.oppervlakte_obj,
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
            ((mb_util_app_re_kad_perceel v
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
    mb_benoemd_obj_adres bola
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
        koz.kad_identif::text = b.ref_id) WITH NO DATA;

CREATE UNIQUE INDEX mb_kad_onrrnd_zk_adres_objectid ON mb_kad_onrrnd_zk_adres USING btree (objectid);
CREATE INDEX mb_kad_onrrnd_zk_adres_identif ON mb_kad_onrrnd_zk_adres USING btree (koz_identif);
CREATE INDEX mb_kad_onrrnd_zk_adres_begrenzing_perceel_idx ON mb_kad_onrrnd_zk_adres USING gist (begrenzing_perceel);


COMMENT ON MATERIALIZED VIEW mb_kad_onrrnd_zk_adres
IS
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
'
    ;

CREATE MATERIALIZED VIEW mb_percelenkaart AS
 SELECT row_number() OVER ()::integer AS objectid,
    qry.identif AS koz_identif,
    koz.dat_beg_geldh AS begin_geldigheid,
    to_date(koz.dat_beg_geldh, 'YYYY-MM-DD'::text) AS begin_geldigheid_datum,
    qry.type,
    (COALESCE(qry.ka_sectie, ''::character varying)::text || ' '::text) || COALESCE(qry.ka_perceelnummer, ''::character varying)::text AS aanduiding,
    (((((COALESCE(qry.ka_kad_gemeentecode, ''::character varying)::text || ' '::text) || COALESCE(qry.ka_sectie, ''::character varying)::text) || ' '::text) || COALESCE(qry.ka_perceelnummer, ''::character varying)::text) || ' '::text)  AS aanduiding2,
    qry.ka_sectie AS sectie,
    qry.ka_perceelnummer AS perceelnummer,
    qry.ka_kad_gemeentecode AS gemeentecode,
    qry.aand_soort_grootte,
    qry.grootte_perceel::INTEGER,
    st_area(qry.begrenzing_perceel) AS oppervlakte_geom,
    b.datum AS verkoop_datum,
    koz.cu_aard_cultuur_onbebouwd AS aard_cultuur_onbebouwd,
    koz.ks_bedrag::INTEGER AS bedrag,
    koz.ks_koopjaar AS koopjaar,
    koz.ks_meer_onroerendgoed AS meer_onroerendgoed,
    koz.ks_valutasoort AS valutasoort,
    array_to_string(
        (SELECT array_agg(('id: '::text || COALESCE(koza.kadaster_identif_aantek, ''::character varying)::text || ', '::text || 
        'aard: '::text || COALESCE(koza.aard_aantek_kad_obj, ''::character varying)::text || ', '::text || 
       'begin: '::text || COALESCE(koza.begindatum_aantek_kad_obj, ''::character varying)::text || ', '::text || 
       'beschrijving: '::text || COALESCE(koza.beschrijving_aantek_kad_obj, ''::character varying)::text || ', '::text || 
       'eind: '::text || COALESCE(koza.eindd_aantek_kad_obj, ''::character varying)::text || ', '::text || 
       'koz-id: '::text || COALESCE(koza.fk_4koz_kad_identif, 0::NUMERIC(15,0))::NUMERIC(15,0) || ', '::text || 
       'subject-id: '::text || COALESCE(koza.fk_5pes_sc_identif, ''::character varying)::text || '; '::text))
       FROM kad_onrrnd_zk_aantek koza
       WHERE koza.fk_4koz_kad_identif = koz.kad_identif), ' & ') as aantekeningen,   
    st_x(st_transform(st_setsrid(st_centroid(qry.begrenzing_perceel), 28992), 4326)) AS lon,
    st_y(st_transform(st_setsrid(st_centroid(qry.begrenzing_perceel), 28992), 4326)) AS lat,
    qry.begrenzing_perceel
    FROM ( SELECT p.sc_kad_identif AS identif,
            'perceel'::character varying(11) AS type,
            p.ka_sectie,
            p.ka_perceelnummer,
            p.ka_kad_gemeentecode,
            p.aand_soort_grootte,
            p.grootte_perceel,
            p.begrenzing_perceel
           FROM kad_perceel p
		) qry
     JOIN kad_onrrnd_zk koz ON koz.kad_identif = qry.identif
     LEFT JOIN ( SELECT brondocument.ref_id,
            max(brondocument.datum) AS datum
           FROM brondocument
          WHERE brondocument.omschrijving::text = 'Akte van Koop en Verkoop'::text
          GROUP BY brondocument.ref_id) b ON koz.kad_identif::text = b.ref_id::text
 WITH NO DATA;

CREATE UNIQUE INDEX mb_percelenkaart_objectid ON mb_percelenkaart USING btree (objectid);
CREATE INDEX mb_percelenkaart_identif ON mb_percelenkaart USING btree (koz_identif);
CREATE INDEX mb_percelenkaart_begrenzing_perceel_idx ON mb_percelenkaart USING gist (begrenzing_perceel);

COMMENT ON MATERIALIZED VIEW mb_percelenkaart
IS
    'commentaar view vb_percelenkaart:
alle kadastrale onroerende zaken (perceel en appartementsrecht) met opgezochte verkoop datum, objectid voor geoserver/arcgis

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
'
    ;
    
--drop view vb_util_zk_recht cascade;
CREATE OR REPLACE VIEW
    vb_util_zk_recht
    (
        zr_identif,
        aandeel,
        ar_teller,
        ar_noemer,
        subject_identif,
        koz_identif,
        indic_betrokken_in_splitsing,
        omschr_aard_verkregen_recht,
        fk_3avr_aand,
        aantekeningen
    ) AS
SELECT
    zr.kadaster_identif AS zr_identif,
    ((COALESCE((zr.ar_teller)::text, ('0'::CHARACTER VARYING)::text) || ('/'::CHARACTER VARYING)::
    text) || COALESCE((zr.ar_noemer)::text, ('0'::CHARACTER VARYING)::text))::CHARACTER VARYING(20) AS aandeel,
    zr.ar_teller::INTEGER,
    zr.ar_noemer::INTEGER,
    zr.fk_8pes_sc_identif  AS subject_identif,
    zr.fk_7koz_kad_identif AS koz_identif,
    zr.indic_betrokken_in_splitsing,
    avr.omschr_aard_verkregenr_recht,
    zr.fk_3avr_aand,
    array_to_string(
        (SELECT array_agg(('id: '::text || COALESCE(zra.kadaster_identif_aantek_recht, ''::character varying)::text || ', '::text || 
        'aard: '::text || COALESCE(zra.aard_aantek_recht, ''::character varying)::text || ', '::text || 
       'begin: '::text || COALESCE(zra.begindatum_aantek_recht, ''::character varying)::text || ', '::text || 
       'beschrijving: '::text || COALESCE(zra.beschrijving_aantek_recht, ''::character varying)::text || ', '::text || 
       'eind: '::text || COALESCE(zra.eindd_aantek_recht, ''::character varying)::text || ', '::text || 
       'zkr-id: '::text || COALESCE(zra.fk_5zkr_kadaster_identif, ''::character varying)::text || ', '::text || 
       'subject-id: '::text || COALESCE(zra.fk_6pes_sc_identif, ''::character varying)::text || '; '::text))
       FROM zak_recht_aantek zra
       WHERE zra.fk_5zkr_kadaster_identif = zr.kadaster_identif), '&& ') as aantekeningen    
FROM
    (zak_recht zr
JOIN
    aard_verkregen_recht avr
ON
    (((
                zr.fk_3avr_aand)::text = (avr.aand)::text)));

COMMENT ON VIEW vb_util_zk_recht
IS
    'commentaar view vb_util_zk_recht:
zakelijk recht met opgezocht aard recht en berekend aandeel

beschikbare kolommen:
* zr_identif: natuurlijke id van zakelijk recht     
* aandeel: samenvoeging van teller en noemer (1/2),
* ar_teller: teller van aandeel,
* ar_noemer: noemer van aandeel,
* subject_identif: natuurlijk id van subject (natuurlijk of niet natuurlijk) welke rechthebbende is,
* koz_identif: natuurlijk id van kadastrale onroerende zaak (perceel of appratementsrecht) dat gekoppeld is,
* indic_betrokken_in_splitsing: -,
* omschr_aard_verkregen_recht: tekstuele omschrijving aard recht,
* fk_3avr_aand: code aard recht,
* aantekeningen: samenvoeging van alle aantekening op dit recht
'
    ;
    
CREATE MATERIALIZED VIEW mb_zr_rechth 
    (
        objectid,
        zr_identif,
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
    ) AS
SELECT
    (row_number() OVER ())::INTEGER AS objectid,
    uzr.zr_identif as zr_identif,
    uzr.subject_identif,
    uzr.koz_identif,
    uzr.aandeel,
    uzr.omschr_aard_verkregen_recht,
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
    vs.bsn::INTEGER,
    vs.organisatie_naam,
    vs.rechtsvorm,
    vs.statutaire_zetel,
    vs.rsin::INTEGER,
    vs.kvk_nummer::INTEGER
FROM
    (vb_util_zk_recht uzr
JOIN
    mb_subject vs
ON
    (((
                uzr.subject_identif)::text = (vs.subject_identif)::text))) WITH NO DATA;

CREATE UNIQUE INDEX mb_zr_rechth_objectid ON mb_zr_rechth USING btree (objectid);
CREATE INDEX mb_zr_rechth_identif ON mb_zr_rechth USING btree (zr_identif);

COMMENT ON MATERIALIZED VIEW mb_zr_rechth
IS
    'commentaar view mb_zr_rechth:
alle zakelijke rechten met rechthebbenden en referentie naar kadastraal onroerende zaak (perceel of appartementsrecht)

beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* zr_identif: natuurlijke id van zakelijk recht 
* subject_identif: natuurlijk id van subject (natuurlijk of niet natuurlijk) welke rechthebbende is,
* koz_identif: natuurlijk id van kadastrale onroerende zaak (perceel of appratementsrecht) dat gekoppeld is,
* aandeel: samenvoeging van teller en noemer (1/2),
* omschr_aard_verkregen_recht: tekstuele omschrijving aard recht,
* indic_betrokken_in_splitsing: -,
* aantekeningen: samenvoeging van alle rechten voor dit recht,
* soort: soort subject zoals natuurlijk, niet-natuurlijk enz.  
* geslachtsnaam: -       
* voorvoegsel: -     
* voornamen: -     
* aand_naamgebruik:        
- E (= Eigen geslachtsnaam)        
- N (=Geslachtsnaam echtgenoot/geregistreerd partner na eigen geslachtsnaam)        
- P (= Geslachtsnaam echtgenoot/geregistreerd partner)        
- V (= Geslachtsnaam evhtgenoot/geregistreerd partner voor eigen geslachtsnaam)        
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
* kvk_nummer: -
'
    ;

CREATE MATERIALIZED VIEW mb_avg_zr_rechth 
    (
        objectid,
        zr_identif,
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
    ) AS
SELECT
    (row_number() OVER ())::INTEGER AS objectid,
    uzr.zr_identif as zr_identif,
    uzr.subject_identif,
    uzr.koz_identif,
    uzr.aandeel,
    uzr.omschr_aard_verkregen_recht,
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
    vs.rsin::INTEGER,
    vs.kvk_nummer::INTEGER
FROM
    (vb_util_zk_recht uzr
JOIN
    mb_avg_subject vs
ON
    (((
                uzr.subject_identif)::text = (vs.subject_identif)::text))) WITH NO DATA;

CREATE UNIQUE INDEX mb_avg_zr_rechth_objectid ON mb_avg_zr_rechth USING btree (objectid);
CREATE INDEX mb_avg_zr_rechth_identif ON mb_avg_zr_rechth USING btree (zr_identif);

COMMENT ON MATERIALIZED VIEW mb_avg_zr_rechth
IS
    'commentaar view mb_avg_zr_rechth:
alle zakelijke rechten met voor avg geschoonde rechthebbenden en referentie naar kadastraal onroerende zaak (perceel of appartementsrecht)

beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* zr_identif: natuurlijke id van zakelijk recht     
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
* kvk_nummer: -

'
    ;

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
    ) AS
SELECT
    (row_number() OVER ())::INTEGER AS objectid,
    koz.koz_identif,
    koz.begin_geldigheid,
    to_date(koz.begin_geldigheid, 'YYYY-MM-DD'::text) AS begin_geldigheid_datum,
    koz.type,
    COALESCE(koz.sectie, '') || ' ' || COALESCE(koz.perceelnummer, '') AS aanduiding,
    COALESCE(koz.gemeentecode, '') || ' ' || COALESCE(koz.sectie, '') || ' ' || COALESCE(koz.perceelnummer, '') || ' ' || COALESCE(koz.appartementsindex, '') AS aanduiding2,
    koz.sectie,
    koz.perceelnummer,
    koz.appartementsindex,
    koz.gemeentecode,
    koz.aand_soort_grootte,
    koz.grootte_perceel::INTEGER,
    koz.oppervlakte_geom as oppervlakte_geom,
    koz.deelperceelnummer,
    koz.omschr_deelperceel,
    koz.verkoop_datum,
    koz.aard_cultuur_onbebouwd,
    koz.bedrag::INTEGER,
    koz.koopjaar,
    koz.meer_onroerendgoed,
    koz.valutasoort,
    koz.loc_omschr,
    zrr.zr_identif,
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
    zrr.bsn::INTEGER,
    zrr.organisatie_naam,
    zrr.rechtsvorm,
    zrr.statutaire_zetel,
    zrr.rsin::INTEGER,
    zrr.kvk_nummer::INTEGER,
    zrr.aantekeningen,
    koz.gemeente,
    koz.woonplaats,
    koz.straatnaam,
    koz.huisnummer::INTEGER,
    koz.huisletter,
    koz.huisnummer_toev,
    koz.postcode,
    koz.lon,
    koz.lat,
    koz.begrenzing_perceel
FROM
    (mb_zr_rechth zrr
RIGHT JOIN
    mb_kad_onrrnd_zk_adres koz
ON
    ((
            zrr.koz_identif = koz.koz_identif))) WITH NO DATA;

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
* subject_identif: natuurlijk id van rechthebbende,
* aandeel: samenvoeging van teller en noemer (1/2),
* omschr_aard_verkregen_recht: tekstuele omschrijving aard recht,
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
'
    ;

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
    ) AS
SELECT
    (row_number() OVER ())::INTEGER AS objectid,
    koz.koz_identif as koz_identif,
    koz.begin_geldigheid,
    to_date(koz.begin_geldigheid, 'YYYY-MM-DD'::text) AS begin_geldigheid_datum,
    koz.type,
    COALESCE(koz.sectie, '') || ' ' || COALESCE(koz.perceelnummer, '') AS aanduiding,
    COALESCE(koz.gemeentecode, '') || ' ' || COALESCE(koz.sectie, '') || ' ' || COALESCE(koz.perceelnummer, '') || ' ' || COALESCE(koz.appartementsindex, '') AS aanduiding2,
    koz.sectie,
    koz.perceelnummer,
    koz.appartementsindex,
    koz.gemeentecode,
    koz.aand_soort_grootte,
    koz.grootte_perceel::INTEGER,
    koz.oppervlakte_geom,
    koz.deelperceelnummer,
    koz.omschr_deelperceel,
    koz.verkoop_datum,
    koz.aard_cultuur_onbebouwd,
    koz.bedrag::INTEGER,
    koz.koopjaar,
    koz.meer_onroerendgoed,
    koz.valutasoort,
    koz.loc_omschr,
    zrr.zr_identif,
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
    zrr.rsin::INTEGER,
    zrr.kvk_nummer::INTEGER,
    zrr.aantekeningen,
    koz.gemeente,
    koz.woonplaats,
    koz.straatnaam,
    koz.huisnummer::INTEGER,
    koz.huisletter,
    koz.huisnummer_toev,
    koz.postcode,
    koz.lon,
    koz.lat,
    koz.begrenzing_perceel
FROM
    (mb_avg_zr_rechth zrr
RIGHT JOIN
    mb_kad_onrrnd_zk_adres koz
ON
    ((
            zrr.koz_identif = koz.koz_identif))) WITH NO DATA;

CREATE UNIQUE INDEX mb_avg_koz_rechth_objectid ON mb_avg_koz_rechth USING btree (objectid);
CREATE INDEX mb_avg_koz_rechth_identif ON mb_avg_koz_rechth USING btree (koz_identif);
CREATE INDEX mb_avg_koz_rechth_begrenzing_perceel_idx ON mb_avg_koz_rechth USING gist (begrenzing_perceel);

COMMENT ON MATERIALIZED VIEW mb_avg_koz_rechth
IS
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
'
    ;

CREATE MATERIALIZED VIEW MB_KAD_ONRRND_ZK_ARCHIEF 
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
    ) AS
SELECT
    (row_number() OVER ())::INTEGER AS objectid,
    qry.identif as koz_identif,
    koza.dat_beg_geldh AS begin_geldigheid,
    to_date(koza.dat_beg_geldh, 'YYYY-MM-DD'::text) AS begin_geldigheid_datum,
    koza.datum_einde_geldh AS eind_geldigheid,
    to_date(koza.datum_einde_geldh, 'YYYY-MM-DD'::text) AS eind_geldigheid_datum,
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
    qry.grootte_perceel::INTEGER,
    qry.ka_deelperceelnummer AS deelperceelnummer,
    qry.omschr_deelperceel,
    koza.cu_aard_cultuur_onbebouwd AS aard_cultuur_onbebouwd,
    koza.ks_bedrag::INTEGER                 AS bedrag,
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
        kozhr.fk_sc_rh_koz_kad_identif = koza.kad_identif) WITH NO DATA;
        
CREATE UNIQUE INDEX mb_kad_onrrnd_zk_archief_objectid ON mb_kad_onrrnd_zk_archief USING btree (objectid);
CREATE INDEX mb_kad_onrrnd_zk_archief_identif ON mb_kad_onrrnd_zk_archief USING btree (koz_identif);
CREATE INDEX mb_kad_onrrnd_zk_archief_begrenzing_perceel_idx ON mb_kad_onrrnd_zk_archief USING gist (begrenzing_perceel);
        
        
COMMENT ON MATERIALIZED VIEW mb_kad_onrrnd_zk_archief
IS
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
'
    ;

