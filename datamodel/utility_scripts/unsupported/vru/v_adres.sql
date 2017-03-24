SET SESSION AUTHORIZATION RSGB;
-- v_adres
CREATE OR REPLACE VIEW
    v_adres
    (
        objectid,
        fid,
        gemeente,
        woonplaats,
        straatnaam,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        status,
        oppervlakte,
        the_geom,
        adresseerbaarobject,
        nummeraanduiding
    ) AS
SELECT
    (row_number() OVER ())::integer AS ObjectID,
    vbo.sc_identif       AS fid,
    gem.naam             AS gemeente,
    CASE
        WHEN addrobj.fk_6wpl_identif IS NOT NULL
        -- opzoeken want in andere woonplaats
        THEN  (select naam from wnplts where identif = fk_6wpl_identif)
        ELSE wp.naam           
    END                  AS woonplaats,
    geor.naam_openb_rmte AS straatnaam,
    addrobj.huinummer    AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    vbo.status,
    gobj.oppervlakte_obj || ' m2' AS oppervlakte,
    gobj.puntgeom                 AS the_geom,
    vbo.sc_identif AS adresseerbaarobject,
    na.sc_identif AS nummeraanduiding
FROM
    verblijfsobj vbo
JOIN
    gebouwd_obj gobj
ON
    (
        gobj.sc_identif = vbo.sc_identif )
LEFT JOIN
    verblijfsobj_nummeraand vna
ON
    (
        vna.fk_nn_lh_vbo_sc_identif = vbo.sc_identif )
LEFT JOIN
    nummeraand na
ON
    (
        na.sc_identif = vbo.fk_11nra_sc_identif)
LEFT JOIN
    addresseerb_obj_aand addrobj
ON
    (
        addrobj.identif = na.sc_identif )
JOIN
    gem_openb_rmte geor
ON
    (
        geor.identifcode = addrobj.fk_7opr_identifcode )
LEFT JOIN
    openb_rmte_wnplts orwp
ON
    (
        geor.identifcode = orwp.fk_nn_lh_opr_identifcode)
LEFT JOIN
    wnplts wp
ON
    (
        orwp.fk_nn_rh_wpl_identif = wp.identif)
LEFT JOIN
    gemeente gem
ON
    (
        wp.fk_7gem_code = gem.code )
WHERE
    na.status = 'Naamgeving uitgegeven'
AND (
        vbo.status = 'Verblijfsobject in gebruik (niet ingemeten)'
    OR  vbo.status = 'Verblijfsobject in gebruik');


-------------------------------------------------
-- v_adres_ligplaats
-------------------------------------------------

CREATE OR REPLACE VIEW
    v_adres_ligplaats
    (
        fid,
        gemeente,
        woonplaats,
        straatnaam,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        status,
        the_geom,
        centroide,
        adresseerbaarobject,
        nummeraanduiding
    ) AS
SELECT
    lpa.sc_identif       AS fid,
    gem.naam             AS gemeente,
    CASE
        WHEN addrobj.fk_6wpl_identif IS NOT NULL
        -- opzoeken want in andere woonplaats
        THEN  (select naam from wnplts where identif = fk_6wpl_identif)
        ELSE wp.naam           
    END                  AS woonplaats,
    geor.naam_openb_rmte AS straatnaam,
    addrobj.huinummer    AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    lpa.status,
    benter.geom AS the_geom,
    st_centroid(benter.geom) AS centroide,
    lpa.sc_identif AS adresseerbaarobject,
    na.sc_identif AS nummeraanduiding
FROM
    ligplaats lpa
JOIN
    benoemd_terrein benter
ON
    (
        benter.sc_identif = lpa.sc_identif )
LEFT JOIN
    ligplaats_nummeraand lna
ON
    (
        lna.fk_nn_lh_lpl_sc_identif = lpa.sc_identif )
LEFT JOIN
    nummeraand na
ON
    (
        na.sc_identif = lpa.fk_4nra_sc_identif )
LEFT JOIN
    addresseerb_obj_aand addrobj
ON
    (
        addrobj.identif = na.sc_identif )
JOIN
    gem_openb_rmte geor
ON
    (
        geor.identifcode = addrobj.fk_7opr_identifcode )
LEFT JOIN
    openb_rmte_wnplts orwp
ON
    (
        geor.identifcode = orwp.fk_nn_lh_opr_identifcode)
LEFT JOIN
    wnplts wp
ON
    (
        orwp.fk_nn_rh_wpl_identif = wp.identif)
LEFT JOIN
    gemeente gem
ON
    (
        wp.fk_7gem_code = gem.code )
WHERE
    na.status = 'Naamgeving uitgegeven'
AND lpa.status = 'Plaats aangewezen';

-------------------------------------------------
-- v_adres_standplaats
-------------------------------------------------
CREATE OR REPLACE VIEW
    v_adres_standplaats
    (
        fid,
        gemeente,
        woonplaats,
        straatnaam,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        status,
        the_geom,
        centroide,
        adresseerbaarobject,
        nummeraanduiding
    ) AS
SELECT
    spl.sc_identif       AS fid,
    gem.naam             AS gemeente,
    CASE
        WHEN addrobj.fk_6wpl_identif IS NOT NULL
        -- opzoeken want in andere woonplaats
        THEN  (select naam from wnplts where identif = fk_6wpl_identif)
        ELSE wp.naam           
    END                  AS woonplaats,
    geor.naam_openb_rmte AS straatnaam,
    addrobj.huinummer    AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    spl.status,
    benter.geom AS the_geom,
    st_centroid(benter.geom),
    spl.sc_identif AS adresseerbaarobject,
    na.sc_identif AS nummeraanduiding
FROM
    standplaats spl
JOIN
    benoemd_terrein benter
ON
    (
        benter.sc_identif = spl.sc_identif )
LEFT JOIN
    standplaats_nummeraand sna
ON
    (
        sna.fk_nn_lh_spl_sc_identif = spl.sc_identif )
LEFT JOIN
    nummeraand na
ON
    (
        na.sc_identif = spl.fk_4nra_sc_identif )
LEFT JOIN
    addresseerb_obj_aand addrobj
ON
    (
        addrobj.identif = na.sc_identif )
JOIN
    gem_openb_rmte geor
ON
    (
        geor.identifcode = addrobj.fk_7opr_identifcode )
LEFT JOIN
    openb_rmte_wnplts orwp
ON
    (
        geor.identifcode = orwp.fk_nn_lh_opr_identifcode)
LEFT JOIN
    wnplts wp
ON
    (
        orwp.fk_nn_rh_wpl_identif = wp.identif)
LEFT JOIN
    gemeente gem
ON
    (
        wp.fk_7gem_code = gem.code )
WHERE
    na.status = 'Naamgeving uitgegeven'
AND spl.status = 'Plaats aangewezen';


-------------------------------------------------
-- v_adres_totaal
-------------------------------------------------

 DROP MATERIALIZED VIEW v_adres_totaal;

CREATE MATERIALIZED VIEW v_adres_totaal AS 
 SELECT row_number() OVER ()::integer AS objectid,
    qry.fid,
    qry.straatnaam,
    qry.huisnummer,
    qry.huisletter,
    qry.huisnummer_toev,
    qry.postcode,
    qry.gemeente,
    qry.woonplaats,
    qry.the_geom as geopunt
   FROM ( SELECT v_adres.fid,
            v_adres.straatnaam,
            v_adres.huisnummer,
            v_adres.huisletter,
            v_adres.huisnummer_toev,
            v_adres.postcode,
            v_adres.gemeente,
            v_adres.woonplaats,
            v_adres.the_geom
           FROM v_adres
        UNION ALL
         SELECT v_adres_ligplaats.fid,
            v_adres_ligplaats.straatnaam,
            v_adres_ligplaats.huisnummer,
            v_adres_ligplaats.huisletter,
            v_adres_ligplaats.huisnummer_toev,
            v_adres_ligplaats.postcode,
            v_adres_ligplaats.gemeente,
            v_adres_ligplaats.woonplaats,
            v_adres_ligplaats.centroide AS the_geom
           FROM v_adres_ligplaats
        UNION ALL
         SELECT v_adres_standplaats.fid,
            v_adres_standplaats.straatnaam,
            v_adres_standplaats.huisnummer,
            v_adres_standplaats.huisletter,
            v_adres_standplaats.huisnummer_toev,
            v_adres_standplaats.postcode,
            v_adres_standplaats.gemeente,
            v_adres_standplaats.woonplaats,
            v_adres_standplaats.centroide AS the_geom
           FROM v_adres_standplaats) qry
WITH DATA;

ALTER TABLE v_adres_totaal
  OWNER TO rsgb;


-- DROP VIEW v_moi_bag_denormalisatie;

CREATE OR REPLACE VIEW v_moi_bag_denormalisatie AS 
 SELECT vbo.fid AS nummer_id,
    vbo.pand_id,
    vbo.gemeente AS gem_naam,
    vbo.woonplaats,
    vbo.straatnaam,
    vbo.huisnummer,
    vbo.huisletter,
    vbo.huisnummer_toev AS toevoeging,
    vbo.postcode,
    p.oorspronkelijk_bouwjaar AS bouwjaar,
    gbd.gebruiksdoel_gebouwd_obj AS functie_bag
   FROM v_verblijfsobject vbo
     JOIN verblijfsobj_pand vp ON vp.fk_nn_lh_vbo_sc_identif::text = vbo.fid::text
     JOIN pand p ON vp.fk_nn_rh_pnd_identif::text = p.identif::text
     LEFT JOIN gebouwd_obj_gebruiksdoel gbd ON gbd.fk_gbo_sc_identif::text = vbo.fid::text;

ALTER TABLE v_moi_bag_denormalisatie
  OWNER TO rsgb;



 DROP VIEW v_pand_in_gebruik;

CREATE OR REPLACE VIEW v_pand_in_gebruik AS 
 SELECT row_number() OVER ()::integer AS objectid,
    p.identif AS fid,
    p.datum_einde_geldh AS eind_datum_geldig,
    p.dat_beg_geldh AS begin_datum_geldig,
    p.status,
    p.oorspronkelijk_bouwjaar AS bouwjaar,
    p.geom_bovenaanzicht AS geovlak
   FROM pand p
  WHERE (p.status::text = ANY (ARRAY['Sloopvergunning verleend'::character varying, 'Pand in gebruik (niet ingemeten)'::character varying, 'Pand in gebruik'::character varying, 'Bouw gestart'::character varying]::text[])) AND p.datum_einde_geldh IS NULL;

ALTER TABLE v_pand_in_gebruik
  OWNER TO rsgb;

  -- Materialized View: v_adres_totaal
