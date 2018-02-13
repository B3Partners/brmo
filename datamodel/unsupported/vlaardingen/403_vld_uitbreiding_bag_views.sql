-- extra bag views voor vlaardingen
-- 31-08-2016 (hersteld)
-----------------------------------
-- v_adres_met_buurt_en_wijk
-- v_adres_vld (aangepaste versie met gebruiksdoel, \
--   moet eigenlijk gelijkgetrokken worden met oorspronkelijke versie)
-- v_adres_totaal_vld (aangepaste versie met gebruiksdoel, 
--   moet gelijkgetrokken worden met oorspronkelijke versie)
-- v_bag_zoeker
-- v_ligplaats_alles
-- v_ligplaats_met_document
-- v_nummeraanduiding_met_document
-- v_openbareruimte_met_document
-- v_opzoeklijst_straat
-- v_pand_met_document
-- v_pand_met_document_inclusief_verblijfsobj
-- v_pand_status_gisviewer_vld
-- v_standplaats_alles
-- v_standplaats_met_document
-- v_verblijfsobj_pand
-- v_verblijfsobject_met_document
-- v_verblijfsobject_met_wijk_buurt
-- v_verblijfsobject_vbo_pand
-- v_woonplaats_met_document
-- v_brondocument_nummeraanduiding
-- v_brondocument_ligplaats
-- v_brondocument_nummeraanduiding
-- v_brondocument_openbareruimte
-- v_brondocument_pand
-- v_brondocument_standplaats
-- v_brondocument_vbo
-- v_brondocument_woonplaats
------------------------------------

------------------------------------
-- v_adres_met_buurt_en_wijk
------------------------------------

CREATE OR REPLACE VIEW
    v_adres_met_buurt_en_wijk
    (
        fid,
        gemeente,
        straat,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        gebruiksdoel,
        buurtnaam,
        buurtcode,
        wijknaam,
        wijkcode,
        the_geom
    ) AS
SELECT
    a.fid,
    a.gemeente,
    a.straat,
    a.huisnummer,
    a.huisletter,
    a.huisnummer_toev,
    a.postcode,
    a.gebruiksdoel,
    b.naam AS buurtnaam,
    b.code AS buurtcode,
    w.naam AS wijknaam,
    w.code AS wijkcode,
    a.the_geom
FROM
    ((v_adres_totaal_vld a
JOIN
    buurt b
ON
    (
        st_within(a.the_geom, b.geom)))
JOIN
    wijk w
ON
    (
        st_within(a.the_geom, w.geom)));

------------------------------------
-- v_adres (aangepaste versie met gebruiksdoel, 
-- moet eigenlijk gelijkgetrokken worden met oorspronkelijke versie)
------------------------------------
CREATE OR REPLACE VIEW
    v_adres
    (
        fid,
        gemeente,
        straat,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        gebruiksdoel,
        status,
        oppervlakte,
        the_geom
    ) AS
SELECT
    vbo.sc_identif       AS fid,
    gem.naam             AS gemeente,
    geor.naam_openb_rmte AS straat,
    addrobj.huinummer    AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    string_agg((gog.gebruiksdoel_gebouwd_obj)::text, ', '::text) AS gebruiksdoel,
    vbo.status,
    (gobj.oppervlakte_obj || ' m2'::text) AS oppervlakte,
    gobj.puntgeom                         AS the_geom
FROM
    (((((((((verblijfsobj vbo
JOIN
    gebouwd_obj gobj
ON
    (((
                gobj.sc_identif)::text = (vbo.sc_identif)::text)))
LEFT JOIN
    verblijfsobj_nummeraand vna
ON
    (((
                vna.fk_nn_lh_vbo_sc_identif)::text = (vbo.sc_identif)::text)))
LEFT JOIN
    nummeraand na
ON
    (((
                na.sc_identif)::text = (vna.fk_nn_rh_nra_sc_identif)::text)))
LEFT JOIN
    addresseerb_obj_aand addrobj
ON
    (((
                addrobj.identif)::text = (na.sc_identif)::text)))
LEFT JOIN
    openb_rmte opr
ON
    (((
                opr.identifcode)::text = (addrobj.fk_7opr_identifcode)::text)))
LEFT JOIN
    openb_rmte_gem_openb_rmte gmopr
ON
    (((
                gmopr.fk_nn_lh_opr_identifcode)::text = (opr.identifcode)::text)))
LEFT JOIN
    gem_openb_rmte geor
ON
    (((
                geor.identifcode)::text = (gmopr.fk_nn_rh_gor_identifcode)::text)))
LEFT JOIN
    gemeente gem
ON
    ((
            geor.fk_7gem_code = gem.code)))
JOIN
    gebouwd_obj_gebruiksdoel gog
ON
    (((
                vbo.sc_identif)::text = (gog.fk_gbo_sc_identif)::text)))
WHERE
    (((
                na.status)::text = 'Naamgeving uitgegeven'::text)
    AND (((((
                            vbo.status)::text = 'Verblijfsobject in gebruik (niet ingemeten)'::text
                    )
                OR  ((
                            vbo.status)::text = 'Verblijfsobject in gebruik'::text))
            OR  ((
                        vbo.status)::text = 'Verblijfsobject gevormd'::text))
        OR  ((
                    vbo.status)::text = 'Verblijfsobject buiten gebruik'::text)))
GROUP BY
    vbo.sc_identif,
    gem.naam,
    geor.naam_openb_rmte,
    addrobj.huinummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging,
    addrobj.postcode,
    vbo.status,
    gobj.oppervlakte_obj,
    gobj.puntgeom;


------------------------------------
-- v_adres_totaal (aangepaste versie met gebruiksdoel, 
-- moet gelijkgetrokken worden met oorspronkelijke versie)
------------------------------------
CREATE OR REPLACE VIEW
    v_adres_totaal
    (
        fid,
        straat,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        gemeente,
        gebruiksdoel,
        the_geom
    ) AS
SELECT
    v_adres.fid,
    v_adres.straat,
    v_adres.huisnummer,
    v_adres.huisletter,
    v_adres.huisnummer_toev,
    v_adres.postcode,
    v_adres.gemeente,
    v_adres.gebruiksdoel,
    v_adres.the_geom
FROM
    v_adres
UNION ALL
SELECT
    v_adres_ligplaats.fid,
    v_adres_ligplaats.straat,
    v_adres_ligplaats.huisnummer,
    v_adres_ligplaats.huisletter,
    v_adres_ligplaats.huisnummer_toev,
    v_adres_ligplaats.postcode,
    v_adres_ligplaats.gemeente,
    NULL::text                  AS gebruiksdoel,
    v_adres_ligplaats.centroide AS the_geom
FROM
    v_adres_ligplaats
UNION ALL
SELECT
    v_adres_standplaats.fid,
    v_adres_standplaats.straat,
    v_adres_standplaats.huisnummer,
    v_adres_standplaats.huisletter,
    v_adres_standplaats.huisnummer_toev,
    v_adres_standplaats.postcode,
    v_adres_standplaats.gemeente,
    NULL::text                    AS gebruiksdoel,
    v_adres_standplaats.centroide AS the_geom
FROM
    v_adres_standplaats;

------------------------------------
-- v_ligplaats_alles
------------------------------------

CREATE OR REPLACE VIEW
    v_ligplaats_alles
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
        the_geom
    ) AS
SELECT
    lp.sc_identif        AS fid,
    gem.naam             AS gemeente,
    wp.naam              AS woonplaats,
    geor.naam_openb_rmte AS straatnaam,
    addrobj.huinummer    AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    lp.status,
    bt.geom AS the_geom
FROM
    (((((((ligplaats lp
JOIN
    benoemd_terrein bt
ON
    (((
                lp.sc_identif)::text = (bt.sc_identif)::text)))
JOIN
    nummeraand na
ON
    (((
                na.sc_identif)::text = (lp.fk_4nra_sc_identif)::text)))
JOIN
    addresseerb_obj_aand addrobj
ON
    (((
                addrobj.identif)::text = (na.sc_identif)::text)))
JOIN
    gem_openb_rmte geor
ON
    (((
                geor.identifcode)::text = (addrobj.fk_7opr_identifcode)::text)))
LEFT JOIN
    openb_rmte_wnplts orwp
ON
    (((
                geor.identifcode)::text = (orwp.fk_nn_lh_opr_identifcode)::text)))
LEFT JOIN
    wnplts wp
ON
    (((
                orwp.fk_nn_rh_wpl_identif)::text = (wp.identif)::text)))
LEFT JOIN
    gemeente gem
ON
    ((
            wp.fk_7gem_code = gem.code)))
WHERE
    ((((
                    addrobj.dat_eind_geldh IS NULL)
            AND (
                    geor.datum_einde_geldh IS NULL))
        AND (
                gem.datum_einde_geldh IS NULL))
    AND (
            bt.datum_einde_geldh IS NULL));

------------------------------------
-- v_bag_zoeker
------------------------------------

CREATE OR REPLACE VIEW
    v_bag_zoeker
    (
        fid,
        eind_datum_geldig,
        begin_datum_geldig,
        status,
        indic_geconstateerd,
        bouwjaar,
        verblijfsobject_id,
        gebruiksdoel,
        oppervlakte,
        the_geom
    ) AS
SELECT
    p.identif           AS fid,
    p.datum_einde_geldh AS eind_datum_geldig,
    p.dat_beg_geldh     AS begin_datum_geldig,
    p.status,
    p.indic_geconstateerd,
    p.oorspronkelijk_bouwjaar    AS bouwjaar,
    v.fk_nn_lh_vbo_sc_identif    AS verblijfsobject_id,
    gog.gebruiksdoel_gebouwd_obj AS gebruiksdoel,
    gbo.oppervlakte_obj          AS oppervlakte,
    p.geom_bovenaanzicht         AS the_geom
FROM
    (((pand p
JOIN
    verblijfsobj_pand v
ON
    (((
                v.fk_nn_rh_pnd_identif)::text = (p.identif)::text)))
JOIN
    gebouwd_obj_gebruiksdoel gog
ON
    (((
                v.fk_nn_lh_vbo_sc_identif)::text = (gog.fk_gbo_sc_identif)::text)))
JOIN
    gebouwd_obj gbo
ON
    (((
                v.fk_nn_lh_vbo_sc_identif)::text = (gbo.sc_identif)::text)));
        
------------------------------------
-- v_ligplaats_met_document
------------------------------------
        
CREATE OR REPLACE VIEW
    v_ligplaats_met_document
    (
        sc_identif,
        gemeente,
        straat,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        status,
        indic_geconst,
        fk_4nra_sc_identif,
        in_onderzoek_gemeentelijk,
        in_onderzoek_landelijk,
        documentnummer,
        documentdatum,
        begin_datum_geldig,
        geometrie
    ) AS
SELECT
    lp.sc_identif,
    a.gemeente,
    a.straat,
    a.huisnummer,
    a.huisletter,
    a.huisnummer_toev,
    a.postcode,
    lp.status,
    lp.indic_geconst,
    lp.fk_4nra_sc_identif,
    i.inonderzoekgemeentelijk AS in_onderzoek_gemeentelijk,
    i.inonderzoeklandelijk    AS in_onderzoek_landelijk,
    b.identificatie           AS documentnummer,
    b.datum                   AS documentdatum,
    (TO_CHAR((to_date((bt.dat_beg_geldh)::text, 'YYYYMMDDHH24MISSSSSSS'::text))::TIMESTAMP WITH
    TIME zone, 'YYYY-MM-DD'::text))::CHARACTER VARYING(19) AS begin_datum_geldig,
    bt.geom                                                AS geometrie
FROM
    ((((ligplaats lp
LEFT JOIN
    benoemd_terrein bt
ON
    (((
                lp.sc_identif)::text = (bt.sc_identif)::text)))
JOIN
    v_adres_met_buurt_en_wijk a
ON
    (((
                lp.sc_identif)::text = (a.fid)::text)))
JOIN
    v_brondocument_ligplaats b
ON
    ((((
                    b.tabel_identificatie)::text = (lp.sc_identif)::text)
        AND ((
                    b.tabel)::text = 'ligplaats'::text))))
JOIN
    inonderzoek i
ON
    ((((
                    i.tabel_identificatie)::text = (lp.sc_identif)::text)
        AND ((
                    i.tabel)::text = 'ligplaats'::text))));
                    
------------------------------------
-- v_nummeraanduiding_met_document
------------------------------------
                    
CREATE OR REPLACE VIEW
    v_nummeraanduiding_met_document
    (
        indentificatiecode,
        openbareruimtenaam,
        huisnummer,
        huisletter,
        huinummertoevoeging,
        postcode,
        indic_geconst,
        status,
        in_onderzoek_gemeentelijk,
        in_onderzoek_landelijk,
        verblijfsobj_code,
        begin_datum_geldig,
        documentnummer,
        documentdatum,
        buurtnaam,
        buurtcode,
        wijknaam,
        wijkcode,
        the_geom
    ) AS
SELECT
    na.sc_identif        AS indentificatiecode,
    geor.naam_openb_rmte AS openbareruimtenaam,
    addrobj.huinummer    AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging,
    addrobj.postcode,
    na.indic_geconst,
    na.status,
    i.inonderzoekgemeentelijk AS in_onderzoek_gemeentelijk,
    i.inonderzoeklandelijk    AS in_onderzoek_landelijk,
    vbo.sc_identif            AS verblijfsobj_code,
    (TO_CHAR((to_date((vna.fk_nn_lh_vbo_sc_dat_beg_geldh)::text, 'YYYYMMDDHH24MISSSSSSS'::text))::
    TIMESTAMP WITH TIME zone, 'YYYY-MM-DD'::text))::CHARACTER VARYING(19) AS begin_datum_geldig,
    br.identificatie                                                      AS documentnummer,
    br.datum                                                              AS documentdatum,
    b.naam                                                                AS buurtnaam,
    b.code                                                                AS buurtcode,
    w.naam                                                                AS wijknaam,
    w.code                                                                AS wijkcode,
    gobj.puntgeom                                                         AS the_geom
FROM
    ((((((((((((verblijfsobj vbo
JOIN
    gebouwd_obj gobj
ON
    (((
                gobj.sc_identif)::text = (vbo.sc_identif)::text)))
JOIN
    verblijfsobj_nummeraand vna
ON
    (((
                vna.fk_nn_lh_vbo_sc_identif)::text = (vbo.sc_identif)::text)))
JOIN
    nummeraand na
ON
    (((
                na.sc_identif)::text = (vna.fk_nn_rh_nra_sc_identif)::text)))
JOIN
    addresseerb_obj_aand addrobj
ON
    (((
                addrobj.identif)::text = (na.sc_identif)::text)))
JOIN
    openb_rmte opr
ON
    (((
                opr.identifcode)::text = (addrobj.fk_7opr_identifcode)::text)))
JOIN
    openb_rmte_gem_openb_rmte gmopr
ON
    (((
                gmopr.fk_nn_lh_opr_identifcode)::text = (opr.identifcode)::text)))
JOIN
    gem_openb_rmte geor
ON
    (((
                geor.identifcode)::text = (gmopr.fk_nn_rh_gor_identifcode)::text)))
JOIN
    gemeente gem
ON
    ((
            geor.fk_7gem_code = gem.code)))
JOIN
    inonderzoek i
ON
    ((((
                    i.tabel_identificatie)::text = (na.sc_identif)::text)
        AND ((
                    i.tabel)::text = 'nummeraanduiding'::text))))
JOIN
    v_brondocument_nummeraanduiding br
ON
    ((((
                    br.tabel_identificatie)::text = (na.sc_identif)::text)
        AND ((
                    br.tabel)::text = 'nummeraanduiding'::text))))
JOIN
    buurt b
ON
    (
        st_within(gobj.puntgeom, b.geom)))
JOIN
    wijk w
ON
    (
        st_within(gobj.puntgeom, w.geom)))
WHERE
    ((((((
                            addrobj.dat_eind_geldh IS NULL)
                    AND (
                            geor.datum_einde_geldh IS NULL))
                AND (
                        gem.datum_einde_geldh IS NULL))
            AND (
                    gobj.datum_einde_geldh IS NULL))
        AND ((((
                            vbo.status)::text = 'Verblijfsobject in gebruik (niet ingemeten)'::text
                    )
                OR  ((
                            vbo.status)::text = 'Verblijfsobject in gebruik'::text))
            OR  ((
                        vbo.status)::text = 'Verblijfsobject gevormd'::text)))
    AND ((
                na.status)::text = 'Naamgeving uitgegeven'::text));
                
                
------------------------------------
-- v_openbareruimte_met_document
------------------------------------
                
CREATE OR REPLACE VIEW
    v_openbareruimte_met_document
    (
        naam_openb_rmte,
        type_openb_rmte,
        identifcode,
        indic_geconst_openb_rmte,
        in_onderzoek_gemeentelijk,
        in_onderzoek_landelijk,
        woonplaatsnaam,
        status_openb_rmte,
        dat_beg_geldh,
        documentnummer,
        documentdatum
    ) AS
SELECT
    gor.naam_openb_rmte,
    gor.type_openb_rmte,
    gor.identifcode,
    gor.indic_geconst_openb_rmte,
    i.inonderzoekgemeentelijk AS in_onderzoek_gemeentelijk,
    i.inonderzoeklandelijk    AS in_onderzoek_landelijk,
    w.naam                    AS woonplaatsnaam,
    gor.status_openb_rmte,
    gor.dat_beg_geldh,
    b.identificatie AS documentnummer,
    b.datum         AS documentdatum
FROM
    ((((gem_openb_rmte gor
JOIN
    gemeente g
ON
    ((
            g.code = gor.fk_7gem_code)))
JOIN
    wnplts w
ON
    ((
            w.fk_7gem_code = g.code)))
JOIN
    v_brondocument_openbareruimte b
ON
    ((((
                    b.tabel_identificatie)::text = (gor.identifcode)::text)
        AND ((
                    b.tabel)::text = 'openbareruimte'::text))))
JOIN
    inonderzoek i
ON
    ((((
                    i.tabel_identificatie)::text = (gor.identifcode)::text)
        AND ((
                    i.tabel)::text = 'openbare ruimte'::text))));
                    
------------------------------------
-- v_opzoeklijst_straat
------------------------------------
                    
CREATE OR REPLACE VIEW
    v_opzoeklijst_straat
    (
        naam_openb_rmte
    ) AS
SELECT DISTINCT
    gem_openb_rmte.naam_openb_rmte
FROM
    gem_openb_rmte
WHERE
    ((
            gem_openb_rmte.type_openb_rmte)::text = 'Weg'::text)
ORDER BY
    gem_openb_rmte.naam_openb_rmte;
    
------------------------------------
-- v_pand_met_document
------------------------------------
    
CREATE OR REPLACE VIEW
    v_pand_met_document
    (
        fid,
        eind_datum_geldig,
        begin_datum_geldig,
        status,
        indic_geconstateerd,
        in_onderzoek_gemeentelijk,
        in_onderzoek_landelijk,
        documentnummer,
        documentdatum,
        metverblijfsobjecten,
        bouwjaar,
        the_geom,
        oppervlakte
    ) AS
SELECT
    p.identif           AS fid,
    p.datum_einde_geldh AS eind_datum_geldig,
    (TO_CHAR((to_date((p.dat_beg_geldh)::text, 'YYYYMMDDHH24MISSSSSSS'::text))::TIMESTAMP WITH TIME
    zone, 'YYYY-MM-DD'::text))::CHARACTER VARYING(19) AS begin_datum_geldig,
    p.status,
    p.indic_geconstateerd,
    i.inonderzoekgemeentelijk AS in_onderzoek_gemeentelijk,
    i.inonderzoeklandelijk    AS in_onderzoek_landelijk,
    b.identificatie           AS documentnummer,
    b.datum                   AS documentdatum,
    CASE
        WHEN (EXISTS
                (
                    SELECT
                        verblijfsobj_pand.fk_nn_lh_vbo_sc_identif
                    FROM
                        verblijfsobj_pand
                    WHERE
                        ((verblijfsobj_pand.fk_nn_rh_pnd_identif)::text = (p.identif)::text)))
        THEN 'ja'::text
        ELSE 'nee'::text
    END                       AS metverblijfsobjecten,
    p.oorspronkelijk_bouwjaar AS bouwjaar,
    p.geom_bovenaanzicht      AS the_geom,
    p.oppervlakte
FROM
    ((pand p
JOIN
    inonderzoek i
ON
    ((((
                    i.tabel_identificatie)::text = (p.identif)::text)
        AND ((
                    i.tabel)::text = 'Pand'::text))))
JOIN
    V_brondocument_pand b
ON
    ((((
                    b.tabel_identificatie)::text = (p.identif)::text)
        AND ((
                    b.tabel)::text = 'pand'::text))))
WHERE
    (((
                p.status)::text = ANY (ARRAY[('Sloopvergunning verleend'::CHARACTER VARYING)::text,
            ('Pand in gebruik (niet ingemeten)'::CHARACTER VARYING)::text,
            ('Bouwvergunning verleend'::CHARACTER VARYING)::text, ('Pand buiten gebruik'::CHARACTER
            VARYING)::text, ('Pand in gebruik'::CHARACTER VARYING)::text, ('Bouw gestart'::
            CHARACTER VARYING)::text]))
    AND (
            p.datum_einde_geldh IS NULL));

------------------------------------
-- v_pand_met_document_inclusief_verblijfsobj
------------------------------------
           
CREATE OR REPLACE VIEW
    v_pand_met_document_inclusief_verblijfsobj
    (
        fid,
        eind_datum_geldig,
        begin_datum_geldig,
        status,
        indic_geconstateerd,
        in_onderzoek_gemeentelijk,
        in_onderzoek_landelijk,
        documentnummer,
        documentdatum,
        metverblijfsobjecten,
        bouwjaar,
        the_geom,
        verblijfsobj_id
    ) AS
SELECT
    p.identif           AS fid,
    p.datum_einde_geldh AS eind_datum_geldig,
    (TO_CHAR((to_date((p.dat_beg_geldh)::text, 'YYYYMMDDHH24MISSSSSSS'::text))::TIMESTAMP WITH TIME
    zone, 'YYYY-MM-DD'::text))::CHARACTER VARYING(19) AS begin_datum_geldig,
    p.status,
    p.indic_geconstateerd,
    i.inonderzoekgemeentelijk AS in_onderzoek_gemeentelijk,
    i.inonderzoeklandelijk    AS in_onderzoek_landelijk,
    b.identificatie           AS documentnummer,
    b.datum                   AS documentdatum,
    CASE
        WHEN (EXISTS
                (
                    SELECT
                        verblijfsobj_pand.fk_nn_lh_vbo_sc_identif
                    FROM
                        verblijfsobj_pand
                    WHERE
                        ((verblijfsobj_pand.fk_nn_rh_pnd_identif)::text = (p.identif)::text)))
        THEN 'ja'::text
        ELSE 'nee'::text
    END                       AS metverblijfsobjecten,
    p.oorspronkelijk_bouwjaar AS bouwjaar,
    p.geom_bovenaanzicht      AS the_geom,
    v.fk_nn_lh_vbo_sc_identif AS verblijfsobj_id
FROM
    (((pand p
JOIN
    v_brondocument_pand b
ON
    ((((
                    b.tabel_identificatie)::text = (p.identif)::text)
        AND ((
                    b.tabel)::text = 'pand'::text))))
JOIN
    verblijfsobj_pand v
ON
    (((
                v.fk_nn_rh_pnd_identif)::text = (p.identif)::text)))
JOIN
    inonderzoek i
ON
    ((((
                    i.tabel_identificatie)::text = (p.identif)::text)
        AND ((
                    i.tabel)::text = 'Pand'::text))))
WHERE
    (((
                p.status)::text = ANY (ARRAY[('Sloopvergunning verleend'::CHARACTER VARYING)::text,
            ('Pand in gebruik (niet ingemeten)'::CHARACTER VARYING)::text,
            ('Bouwvergunning verleend'::CHARACTER VARYING)::text, ('Pand in gebruik'::CHARACTER
            VARYING)::text, ('Pand buiten gebruik'::CHARACTER VARYING)::text, ('Bouw gestart'::
            CHARACTER VARYING)::text]))
    AND (
            p.datum_einde_geldh IS NULL));
			
------------------------------------
-- v_pand_status_gisviewer_vld
------------------------------------			
-- View: v_pand_status_gisviewer_vld

-- DROP VIEW v_pand_status_gisviewer_vld;

CREATE OR REPLACE VIEW v_pand_status_gisviewer_vld AS 
 SELECT p.identif AS fid,
    p.datum_einde_geldh AS eind_datum_geldig,
    p.dat_beg_geldh AS begin_datum_geldig,
    p.status,
    p.oorspronkelijk_bouwjaar AS bouwjaar,
    p.geom_bovenaanzicht AS the_geom
   FROM pand p
  WHERE (p.status::text = ANY (ARRAY['Bouwvergunning verleend'::character varying::text, 'Sloopvergunning verleend'::character varying::text, 'Pand in gebruik (niet ingemeten)'::character varying::text, 'Pand in gebruik'::character varying::text, 'Bouw gestart'::character varying::text])) AND p.datum_einde_geldh IS NULL;

ALTER TABLE v_pand_status_gisviewer_vld
  OWNER TO vlaardingen;
			
  
------------------------------------
-- v_standplaats_alles
------------------------------------
CREATE OR REPLACE VIEW
    v_standplaats_alles
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
        the_geom
    ) AS
SELECT
    sp.sc_identif        AS fid,
    gem.naam             AS gemeente,
    wp.naam              AS woonplaats,
    geor.naam_openb_rmte AS straatnaam,
    addrobj.huinummer    AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    sp.status,
    bt.geom AS the_geom
FROM
    (((((((standplaats sp
JOIN
    benoemd_terrein bt
ON
    (((
                sp.sc_identif)::text = (bt.sc_identif)::text)))
JOIN
    nummeraand na
ON
    (((
                na.sc_identif)::text = (sp.fk_4nra_sc_identif)::text)))
JOIN
    addresseerb_obj_aand addrobj
ON
    (((
                addrobj.identif)::text = (na.sc_identif)::text)))
JOIN
    gem_openb_rmte geor
ON
    (((
                geor.identifcode)::text = (addrobj.fk_7opr_identifcode)::text)))
LEFT JOIN
    openb_rmte_wnplts orwp
ON
    (((
                geor.identifcode)::text = (orwp.fk_nn_lh_opr_identifcode)::text)))
LEFT JOIN
    wnplts wp
ON
    (((
                orwp.fk_nn_rh_wpl_identif)::text = (wp.identif)::text)))
LEFT JOIN
    gemeente gem
ON
    ((
            wp.fk_7gem_code = gem.code)))
WHERE
    ((((
                    addrobj.dat_eind_geldh IS NULL)
            AND (
                    geor.datum_einde_geldh IS NULL))
        AND (
                gem.datum_einde_geldh IS NULL))
    AND (
            bt.datum_einde_geldh IS NULL));  
            
------------------------------------
-- v_standplaats_met_document
------------------------------------
             
CREATE OR REPLACE VIEW
    v_standplaats_met_document
    (
        sc_identif,
        gemeente,
        straat,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        status,
        indic_geconst,
        in_onderzoek_gemeentelijk,
        in_onderzoek_landelijk,
        fk_4nra_sc_identif,
        documentnummer,
        documentdatum,
        begin_datum_geldig,
        geometrie
    ) AS
SELECT
    sp.sc_identif,
    a.gemeente,
    a.straat,
    a.huisnummer,
    a.huisletter,
    a.huisnummer_toev,
    a.postcode,
    sp.status,
    sp.indic_geconst,
    i.inonderzoekgemeentelijk AS in_onderzoek_gemeentelijk,
    i.inonderzoeklandelijk    AS in_onderzoek_landelijk,
    sp.fk_4nra_sc_identif,
    b.identificatie AS documentnummer,
    b.datum         AS documentdatum,
    (TO_CHAR((to_date((bt.dat_beg_geldh)::text, 'YYYYMMDDHH24MISSSSSSS'::text))::TIMESTAMP WITH
    TIME zone, 'YYYY-MM-DD'::text))::CHARACTER VARYING(19) AS begin_datum_geldig,
    bt.geom                                                AS geometrie
FROM
    ((((standplaats sp
LEFT JOIN
    benoemd_terrein bt
ON
    (((
                sp.sc_identif)::text = (bt.sc_identif)::text)))
JOIN
    v_adres_met_buurt_en_wijk a
ON
    (((
                sp.sc_identif)::text = (a.fid)::text)))
JOIN
    inonderzoek i
ON
    ((((
                    i.tabel_identificatie)::text = (sp.sc_identif)::text)
        AND ((
                    i.tabel)::text = 'standplaats'::text))))
JOIN
    v_brondocument_standplaats b
ON
    ((((
                    b.tabel_identificatie)::text = (sp.sc_identif)::text)
        AND ((
                    b.tabel)::text = 'standplaats'::text))));
                    
------------------------------------
-- v_verblijfsobj_pand
------------------------------------
                    
CREATE OR REPLACE VIEW
    v_verblijfsobj_pand
    (
        fk_nn_lh_vbo_sc_identif,
        fk_nn_lh_vbo_sc_dat_beg_geldh,
        fk_nn_rh_pnd_identif,
        fid,
        eind_datum_geldig,
        begin_datum_geldig,
        status,
        metverblijfsobjecten,
        indic_geconstateerd,
        in_onderzoek_gemeentelijk,
        in_onderzoek_landelijk,
        documentnummer,
        documentdatum,
        bouwjaar,
        the_geom
    ) AS
SELECT
    vop.fk_nn_lh_vbo_sc_identif,
    vop.fk_nn_lh_vbo_sc_dat_beg_geldh,
    vop.fk_nn_rh_pnd_identif,
    p.fid,
    p.eind_datum_geldig,
    p.begin_datum_geldig,
    p.status,
    p.metverblijfsobjecten,
    p.indic_geconstateerd,
    i.inonderzoekgemeentelijk AS in_onderzoek_gemeentelijk,
    i.inonderzoeklandelijk    AS in_onderzoek_landelijk,
    p.documentnummer,
    p.documentdatum,
    p.bouwjaar,
    p.the_geom
FROM
    ((verblijfsobj_pand vop
LEFT JOIN
    v_pand_met_document p
ON
    (((
                vop.fk_nn_rh_pnd_identif)::text = (p.fid)::text)))
JOIN
    inonderzoek i
ON
    ((((
                    i.tabel_identificatie)::text = (vop.fk_nn_rh_pnd_identif)::text)
        AND ((
                    i.tabel)::text = 'Pand'::text))));
------------------------------------
-- v_verblijfsobject_met_document
------------------------------------
                    
CREATE OR REPLACE VIEW
    v_verblijfsobject_met_document
    (
        fid,
        pand_id,
        gemeente_naam,
        openbareruimtenaam,
        straatnaam,
        huinummer,
        huisletter,
        huisnummer_toev,
        postcode,
        status,
        oppervlakte,
        indicatie_geconstateerd,
        in_onderzoek_gemeentelijk,
        in_onderzoek_landelijk,
        gebruiksdoel,
        documentnummer,
        documentdatum,
        the_geom
    ) AS
SELECT
    v.fid,
    v.pand_id,
    v.gemeente_naam,
    v.openbareruimtenaam,
    v.straatnaam,
    v.huinummer,
    v.huisletter,
    v.huisnummer_toev,
    v.postcode,
    v.status,
    v.oppervlakte,
    v.indicatie_geconstateerd,
    i.inonderzoekgemeentelijk                                    AS in_onderzoek_gemeentelijk,
    i.inonderzoeklandelijk                                       AS in_onderzoek_landelijk,
    string_agg((gog.gebruiksdoel_gebouwd_obj)::text, ', '::text) AS gebruiksdoel,
    b.identificatie                                              AS documentnummer,
    b.datum                                                      AS documentdatum,
    v.the_geom
FROM
    (((v_verblijfsobject_alles v
JOIN
    v_brondocument_vbo b
ON
    ((((
                    b.tabel_identificatie)::text = (v.fid)::text)
        AND ((
                    b.tabel)::text = 'verblijfsobject'::text))))
JOIN
    inonderzoek i
ON
    ((((
                    i.tabel_identificatie)::text = (v.fid)::text)
        AND ((
                    i.tabel)::text = 'verblijfsobject'::text))))
JOIN
    gebouwd_obj_gebruiksdoel gog
ON
    (((
                v.fid)::text = (gog.fk_gbo_sc_identif)::text)))
GROUP BY
    v.fid,
    v.pand_id,
    v.gemeente_naam,
    v.openbareruimtenaam,
    v.straatnaam,
    v.huinummer,
    v.huisletter,
    v.huisnummer_toev,
    v.postcode,
    v.status,
    v.oppervlakte,
    v.indicatie_geconstateerd,
    i.inonderzoekgemeentelijk,
    i.inonderzoeklandelijk,
    b.identificatie,
    b.datum,
    v.the_geom;
    
------------------------------------
-- v_verblijfsobject_met_wijk_buurt
------------------------------------
    
CREATE OR REPLACE VIEW
    v_verblijfsobject_met_wijk_buurt
    (
        fid,
        pand_id,
        gemeente_naam,
        openbareruimtenaam,
        huinummer,
        huisnum_alfa,
        postcode,
        status,
        oppervlakte,
        buurtnaam,
        buurtcode,
        wijknaam,
        wijkcode,
        the_geom
    ) AS
SELECT
    v.fid,
    v.pand_id,
    v.gemeente_naam,
    v.openbareruimtenaam,
    v.huinummer,
    (((v.huisletter)::text || ' '::text) || (v.huisnummer_toev)::text) AS huisnum_alfa,
    v.postcode,
    v.status,
    v.oppervlakte,
    b.naam AS buurtnaam,
    b.code AS buurtcode,
    w.naam AS wijknaam,
    w.code AS wijkcode,
    v.the_geom
FROM
    ((v_verblijfsobject_alles v
JOIN
    buurt b
ON
    (
        st_within(v.the_geom, b.geom)))
JOIN
    wijk w
ON
    (
        st_within(v.the_geom, w.geom)))
WHERE
    (((
                v.status)::text = 'Verblijfsobject in gebruik (niet ingemeten)'::text)
    OR  ((
                v.status)::text = 'Verblijfsobject in gebruik'::text));
                
------------------------------------
-- v_verblijfsobject_vbo_pand
------------------------------------
                
CREATE OR REPLACE VIEW
    v_verblijfsobject_vbo_pand
    (
        fid,
        gemeente_naam,
        openbareruimtenaam,
        straatnaam,
        huinummer,
        huisletter,
        huisnummer_toev,
        postcode,
        status,
        documentnummer,
        documentdatum,
        indicatie_geconstateerd,
        in_onderzoek_gemeentelijk,
        in_onderzoek_landelijk,
        oppervlakte,
        gebruiksdoel,
        the_geom
    ) AS
SELECT
    vbo.sc_identif       AS fid,
    gem.naam             AS gemeente_naam,
    geor.naam_openb_rmte AS openbareruimtenaam,
    geor.straatnaam,
    addrobj.huinummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    vbo.status,
    b.identificatie                                              AS documentnummer,
    b.datum                                                      AS documentdatum,
    vbo.indic_geconstateerd                                      AS indicatie_geconstateerd,
    i.inonderzoekgemeentelijk                                    AS in_onderzoek_gemeentelijk,
    i.inonderzoeklandelijk                                       AS in_onderzoek_landelijk,
    gobj.oppervlakte_obj                                         AS oppervlakte,
    string_agg((gog.gebruiksdoel_gebouwd_obj)::text, ', '::text) AS gebruiksdoel,
    gobj.puntgeom                                                AS the_geom
FROM
    (((((((((verblijfsobj vbo
JOIN
    gebouwd_obj gobj
ON
    (((
                gobj.sc_identif)::text = (vbo.sc_identif)::text)))
JOIN
    verblijfsobj_nummeraand vna
ON
    (((
                vna.fk_nn_lh_vbo_sc_identif)::text = (vbo.sc_identif)::text)))
JOIN
    nummeraand na
ON
    (((
                na.sc_identif)::text = (vna.fk_nn_rh_nra_sc_identif)::text)))
JOIN
    addresseerb_obj_aand addrobj
ON
    (((
                addrobj.identif)::text = (na.sc_identif)::text)))
JOIN
    gem_openb_rmte geor
ON
    (((
                geor.identifcode)::text = (addrobj.fk_7opr_identifcode)::text)))
JOIN
    gemeente gem
ON
    ((
            geor.fk_7gem_code = gem.code)))
JOIN
    v_brondocument_vbo b
ON
    ((((
                    b.tabel_identificatie)::text = (vbo.sc_identif)::text)
        AND ((
                    b.tabel)::text = 'verblijfsobject'::text))))
JOIN
    inonderzoek i
ON
    ((((
                    i.tabel_identificatie)::text = (vbo.sc_identif)::text)
        AND ((
                    i.tabel)::text = 'verblijfsobject'::text))))
JOIN
    gebouwd_obj_gebruiksdoel gog
ON
    (((
                vbo.sc_identif)::text = (gog.fk_gbo_sc_identif)::text)))
WHERE
    ((((
                    addrobj.dat_eind_geldh IS NULL)
            AND (
                    geor.datum_einde_geldh IS NULL))
        AND (
                gem.datum_einde_geldh IS NULL))
    AND (
            gobj.datum_einde_geldh IS NULL))
GROUP BY
    vbo.sc_identif,
    gem.naam,
    geor.naam_openb_rmte,
    geor.straatnaam,
    addrobj.huinummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging,
    addrobj.postcode,
    vbo.status,
    b.identificatie,
    b.datum,
    i.inonderzoekgemeentelijk,
    i.inonderzoeklandelijk,
    gobj.oppervlakte_obj,
    gobj.puntgeom;

------------------------------------
-- v_woonplaats_met_document
------------------------------------
CREATE OR REPLACE VIEW
    v_woonplaats_met_document
    (
        gemeentecode,
        gemeentenaam,
        woonplaatsnaam,
        woonplaatsidentificatie,
        identificatiecode,
        indicatiegeconstateerd,
        status,
        in_onderzoek_gemeentelijk,
        in_onderzoek_landelijk,
        begin_datum_geldig,
        documentnummer,
        documentdatum,
        geom
    ) AS
SELECT
    g.code          AS gemeentecode,
    g.naam          AS gemeentenaam,
    w.naam          AS woonplaatsnaam,
    w.identif       AS woonplaatsidentificatie,
    w.identif       AS identificatiecode,
    w.indic_geconst AS indicatiegeconstateerd,
    w.status,
    i.inonderzoekgemeentelijk AS in_onderzoek_gemeentelijk,
    i.inonderzoeklandelijk    AS in_onderzoek_landelijk,
    (TO_CHAR((to_date((w.dat_beg_geldh)::text, 'YYYYMMDDHH24MISSSSSSS'::text))::TIMESTAMP WITH TIME
    zone, 'YYYY-MM-DD'::text))::CHARACTER VARYING(19) AS begin_datum_geldig,
    b.identificatie                                   AS documentnummer,
    b.datum                                           AS documentdatum,
    w.geom
FROM
    (((wnplts w
JOIN
    gemeente g
ON
    ((
            w.fk_7gem_code = g.code)))
JOIN
    inonderzoek i
ON
    ((((
                    i.tabel_identificatie)::text = (w.identif)::text)
        AND ((
                    i.tabel)::text = 'Woonplaats'::text))))
JOIN
    v_brondocument_woonplaats b
ON
    ((((
                    b.tabel_identificatie)::text = (w.identif)::text)
        AND ((
                    b.tabel)::text = 'woonplaats'::text))));

------------------------------------
-- v_adres_met buurt_en_wijk_vld
------------------------------------
CREATE OR REPLACE VIEW
    v_adres_met_buurt_en_wijk_vld
    (
        fid,
        gemeente,
        straat,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        gebruiksdoel,
        buurtnaam,
        buurtcode,
        wijknaam,
        wijkcode,
        the_geom
    ) AS
SELECT
    a.fid,
    a.gemeente,
    a.straat,
    a.huisnummer,
    a.huisletter,
    a.huisnummer_toev,
    a.postcode,
    a.gebruiksdoel,
    b.naam AS buurtnaam,
    b.code AS buurtcode,
    w.naam AS wijknaam,
    w.code AS wijkcode,
    a.the_geom
FROM
    ((v_adres_totaal_vld a
JOIN
    buurt b
ON
    (
        st_within(a.the_geom, b.geom)))
JOIN
    wijk w
ON
    (
        st_within(a.the_geom, w.geom)));                    

------------------------------------
--  v_adres_totaal_vld
------------------------------------  

 (
        fid,
        straat,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        gemeente,
        gebruiksdoel,
        the_geom
    ) AS
SELECT
    v_adres_vld.fid,
    v_adres_vld.straat,
    v_adres_vld.huisnummer,
    v_adres_vld.huisletter,
    v_adres_vld.huisnummer_toev,
    v_adres_vld.postcode,
    v_adres_vld.gemeente,
    v_adres_vld.gebruiksdoel,
    v_adres_vld.the_geom
FROM
    v_adres_vld
UNION ALL
SELECT
    v_adres_ligplaats.fid,
    v_adres_ligplaats.straat,
    v_adres_ligplaats.huisnummer,
    v_adres_ligplaats.huisletter,
    v_adres_ligplaats.huisnummer_toev,
    v_adres_ligplaats.postcode,
    v_adres_ligplaats.gemeente,
    NULL::text                  AS gebruiksdoel,
    v_adres_ligplaats.centroide AS the_geom
FROM
    v_adres_ligplaats
UNION ALL
SELECT
    v_adres_standplaats.fid,
    v_adres_standplaats.straat,
    v_adres_standplaats.huisnummer,
    v_adres_standplaats.huisletter,
    v_adres_standplaats.huisnummer_toev,
    v_adres_standplaats.postcode,
    v_adres_standplaats.gemeente,
    NULL::text                    AS gebruiksdoel,
    v_adres_standplaats.centroide AS the_geom
FROM
    v_adres_standplaats;
    
------------------------------------
-- v_adres_vld
------------------------------------    
CREATE OR REPLACE VIEW
    v_adres_vld
    (
        fid,
        gemeente,
        straat,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        gebruiksdoel,
        status,
        oppervlakte,
        the_geom
    ) AS
SELECT
    vbo.sc_identif       AS fid,
    gem.naam             AS gemeente,
    geor.naam_openb_rmte AS straat,
    addrobj.huinummer    AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    string_agg((gog.gebruiksdoel_gebouwd_obj)::text, ', '::text) AS gebruiksdoel,
    vbo.status,
    (gobj.oppervlakte_obj || ' m2'::text) AS oppervlakte,
    gobj.puntgeom                         AS the_geom
FROM
    (((((((((verblijfsobj vbo
JOIN
    gebouwd_obj gobj
ON
    (((
                gobj.sc_identif)::text = (vbo.sc_identif)::text)))
LEFT JOIN
    verblijfsobj_nummeraand vna
ON
    (((
                vna.fk_nn_lh_vbo_sc_identif)::text = (vbo.sc_identif)::text)))
LEFT JOIN
    nummeraand na
ON
    (((
                na.sc_identif)::text = (vna.fk_nn_rh_nra_sc_identif)::text)))
LEFT JOIN
    addresseerb_obj_aand addrobj
ON
    (((
                addrobj.identif)::text = (na.sc_identif)::text)))
LEFT JOIN
    openb_rmte opr
ON
    (((
                opr.identifcode)::text = (addrobj.fk_7opr_identifcode)::text)))
LEFT JOIN
    openb_rmte_gem_openb_rmte gmopr
ON
    (((
                gmopr.fk_nn_lh_opr_identifcode)::text = (opr.identifcode)::text)))
LEFT JOIN
    gem_openb_rmte geor
ON
    (((
                geor.identifcode)::text = (gmopr.fk_nn_rh_gor_identifcode)::text)))
LEFT JOIN
    gemeente gem
ON
    ((
            geor.fk_7gem_code = gem.code)))
JOIN
    gebouwd_obj_gebruiksdoel gog
ON
    (((
                vbo.sc_identif)::text = (gog.fk_gbo_sc_identif)::text)))
WHERE
    (((
                na.status)::text = 'Naamgeving uitgegeven'::text)
    AND (((((
                            vbo.status)::text = 'Verblijfsobject in gebruik (niet ingemeten)'::text
                    )
                OR  ((
                            vbo.status)::text = 'Verblijfsobject in gebruik'::text))
            OR  ((
                        vbo.status)::text = 'Verblijfsobject gevormd'::text))
        OR  ((
                    vbo.status)::text = 'Verblijfsobject buiten gebruik'::text)))
GROUP BY
    vbo.sc_identif,
    gem.naam,
    geor.naam_openb_rmte,
    addrobj.huinummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging,
    addrobj.postcode,
    vbo.status,
    gobj.oppervlakte_obj,
    gobj.puntgeom;
    
------------------------------------
-- v_verblijfsobject_alles_vld
------------------------------------    
 CREATE OR RPELACE VIEW
    v_verblijfsobject_alles_vld
    (
        fid,
        pand_id,
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
        indicatie_geconstateerd
    ) AS
SELECT
    vbo.sc_identif              AS fid,
    fkpand.fk_nn_rh_pnd_identif AS pand_id,
    gem.naam                    AS gemeente,
    wp.naam                     AS woonplaats,
    geor.naam_openb_rmte        AS straatnaam,
    addrobj.huinummer           AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    vbo.status,
    gobj.oppervlakte_obj    AS oppervlakte,
    gobj.puntgeom           AS the_geom,
    vbo.indic_geconstateerd AS indicatie_geconstateerd
FROM
    ((((((((verblijfsobj vbo
JOIN
    verblijfsobj_pand fkpand
ON
    (((
                fkpand.fk_nn_lh_vbo_sc_identif)::text = (vbo.sc_identif)::text)))
JOIN
    gebouwd_obj gobj
ON
    (((
                gobj.sc_identif)::text = (vbo.sc_identif)::text)))
JOIN
    nummeraand na
ON
    (((
                na.sc_identif)::text = (vbo.fk_11nra_sc_identif)::text)))
JOIN
    addresseerb_obj_aand addrobj
ON
    (((
                addrobj.identif)::text = (na.sc_identif)::text)))
JOIN
    gem_openb_rmte geor
ON
    (((
                geor.identifcode)::text = (addrobj.fk_7opr_identifcode)::text)))
LEFT JOIN
    openb_rmte_wnplts orwp
ON
    (((
                geor.identifcode)::text = (orwp.fk_nn_lh_opr_identifcode)::text)))
LEFT JOIN
    wnplts wp
ON
    (((
                orwp.fk_nn_rh_wpl_identif)::text = (wp.identif)::text)))
LEFT JOIN
    gemeente gem
ON
    ((
            wp.fk_7gem_code = gem.code)))
WHERE
    ((((
                    addrobj.dat_eind_geldh IS NULL)
            AND (
                    geor.datum_einde_geldh IS NULL))
        AND (
                gem.datum_einde_geldh IS NULL))
    AND (
            gobj.datum_einde_geldh IS NULL));
    
------------------------------------
-- v_brondocument_ligplaats
------------------------------------   
CREATE OR REPLACE VIEW v_brondocument_ligplaats AS 
 SELECT DISTINCT ON (brondocument.tabel_identificatie) brondocument.tabel,
    brondocument.tabel_identificatie,
    brondocument.identificatie,
    brondocument.gemeente,
    brondocument.omschrijving,
    brondocument.datum,
    brondocument.ref_id
   FROM brondocument
  WHERE brondocument.tabel::text = 'ligplaats'::text
  ORDER BY brondocument.tabel_identificatie, brondocument.datum DESC;
------------------------------------
-- v_brondocument_nummeraanduiding
------------------------------------ 
CREATE OR REPLACE VIEW v_brondocument_nummeraanduiding AS 
 SELECT DISTINCT ON (brondocument.tabel_identificatie) brondocument.tabel,
    brondocument.tabel_identificatie,
    brondocument.identificatie,
    brondocument.gemeente,
    brondocument.omschrijving,
    brondocument.datum,
    brondocument.ref_id
   FROM brondocument
  WHERE brondocument.tabel::text = 'nummeraanduiding'::text
  ORDER BY brondocument.tabel_identificatie, brondocument.datum DESC;
------------------------------------
-- v_brondocument_openbareruimte
------------------------------------
CREATE OR REPLACE VIEW v_brondocument_openbareruimte AS 
 SELECT DISTINCT ON (brondocument.tabel_identificatie) brondocument.tabel,
    brondocument.tabel_identificatie,
    brondocument.identificatie,
    brondocument.gemeente,
    brondocument.omschrijving,
    brondocument.datum,
    brondocument.ref_id
   FROM brondocument
  WHERE brondocument.tabel::text = 'openbareruimte'::text
  ORDER BY brondocument.tabel_identificatie, brondocument.datum DESC;
------------------------------------
-- v_brondocument_pand
------------------------------------
CREATE OR REPLACE VIEW v_brondocument_pand AS 
 SELECT DISTINCT ON (brondocument.tabel_identificatie) brondocument.tabel,
    brondocument.tabel_identificatie,
    brondocument.identificatie,
    brondocument.gemeente,
    brondocument.omschrijving,
    brondocument.datum,
    brondocument.ref_id
   FROM brondocument
  WHERE brondocument.tabel::text = 'pand'::text
  ORDER BY brondocument.tabel_identificatie, brondocument.datum DESC;
------------------------------------
-- v_brondocument_standplaats
------------------------------------
CREATE OR REPLACE VIEW v_brondocument_standplaats AS 
 SELECT DISTINCT ON (brondocument.tabel_identificatie) brondocument.tabel,
    brondocument.tabel_identificatie,
    brondocument.identificatie,
    brondocument.gemeente,
    brondocument.omschrijving,
    brondocument.datum,
    brondocument.ref_id
   FROM brondocument
  WHERE brondocument.tabel::text = 'standplaats'::text
  ORDER BY brondocument.tabel_identificatie, brondocument.datum DESC;
------------------------------------
-- v_brondocument_vbo
------------------------------------
CREATE OR REPLACE VIEW v_brondocument_vbo AS 
 SELECT DISTINCT ON (brondocument.tabel_identificatie) brondocument.tabel,
    brondocument.tabel_identificatie,
    brondocument.identificatie,
    brondocument.gemeente,
    brondocument.omschrijving,
    brondocument.datum,
    brondocument.ref_id
   FROM brondocument
  WHERE brondocument.tabel::text = 'verblijfsobject'::text
  ORDER BY brondocument.tabel_identificatie, brondocument.datum DESC;
------------------------------------
-- v_brondocument_woonplaats
------------------------------------
CREATE OR REPLACE VIEW v_brondocument_woonplaats AS 
 SELECT DISTINCT ON (brondocument.tabel_identificatie) brondocument.tabel,
    brondocument.tabel_identificatie,
    brondocument.identificatie,
    brondocument.gemeente,
    brondocument.omschrijving,
    brondocument.datum,
    brondocument.ref_id
   FROM brondocument
  WHERE brondocument.tabel::text = 'woonplaats'::text
ORDER BY brondocument.tabel_identificatie, brondocument.datum DESC;
