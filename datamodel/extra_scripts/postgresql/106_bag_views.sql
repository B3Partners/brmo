/*
Views for visualizing the BAG data.
versie 2
23-04-2018
*/
-- DROP VIEWS
--drop view v2_volledig_adres cascade;
--drop view v2_vbo_locatie_adres cascade;
--drop view v2_standplaats_locatie_adres cascade;
--drop view v2_ligplaats_locatie_adres cascade;
--drop view v2_pand cascade;
--drop view v2_benoemd_obj_locatie_adres cascade;

--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'v2_pand', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'v2_benoemd_obj_locatie_adres', 'objectid', 'assigned');

--drop view v2_volledig_adres;
CREATE OR REPLACE VIEW
    v2_volledig_adres
    (
        identif,
        begin_geldigheid,
        gemeente,
        woonplaats,
        straatnaam,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode
    ) AS
SELECT
    na.sc_identif                                              AS identif,
    to_date(addrobj.dat_beg_geldh, 'YYYYMMDDHH24MISSUS'::text) AS begin_geldigheid,
    gem.naam                                                   AS gemeente,
    CASE
        WHEN (addrobj.fk_6wpl_identif IS NOT NULL)
        THEN
            (
                SELECT
                    wnplts.naam
                FROM
                    wnplts
                WHERE
                    ((wnplts.identif)::text = (addrobj.fk_6wpl_identif)::text))
        ELSE wp.naam
    END                  AS woonplaats,
    geor.naam_openb_rmte AS straatnaam,
    addrobj.huinummer    AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode
FROM
    (((((nummeraand na
LEFT JOIN
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
            wp.fk_7gem_code = gem.code)));
            
COMMENT ON VIEW v2_volledig_adres
IS
    'commentaar view v2_volledig_adres:
volledig adres zonder locatie

beschikbare kolommen:
* identif: natuurlijke id van adres      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* gemeente: -,
* woonplaats: -,
* straatnaam: -,
* huisnummer: -,
* huisletter: -,
* huisnummer_toev: -,
* postcode: -
';
            
--drop view v2_vbo_locatie_adres cascade;
CREATE OR REPLACE VIEW
    v2_vbo_locatie_adres
    (
        identif,
        begin_geldigheid,
        pand_identif,
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
    vbo.sc_identif                                          AS identif,
    to_date(gobj.dat_beg_geldh, 'YYYYMMDDHH24MISSUS'::text) AS begin_geldigheid,
    fkpand.fk_nn_rh_pnd_identif                             AS pand_identif,
    bva.gemeente,
    bva.woonplaats,
    bva.straatnaam,
    bva.huisnummer,
    bva.huisletter,
    bva.huisnummer_toev,
    bva.postcode,
    vbo.status,
    gobj.puntgeom AS the_geom
FROM
    (((((verblijfsobj vbo
JOIN
    gebouwd_obj gobj
ON
    (((
                gobj.sc_identif)::text = (vbo.sc_identif)::text)))
LEFT JOIN
    verblijfsobj_pand fkpand
ON
    (((
                fkpand.fk_nn_lh_vbo_sc_identif)::text = (vbo.sc_identif)::text)))
LEFT JOIN
    pand
ON
    (((
                fkpand.fk_nn_rh_pnd_identif)::text = (pand.identif)::text)))
LEFT JOIN
    verblijfsobj_nummeraand vna
ON
    (((
                vna.fk_nn_lh_vbo_sc_identif)::text = (vbo.sc_identif)::text)))
LEFT JOIN
    v2_volledig_adres bva
ON
    (((
                vna.fk_nn_rh_nra_sc_identif)::text = (bva.identif)::text)));
COMMENT ON VIEW v2_vbo_locatie_adres
IS
    'commentaar view v2_vbo_locatie_adres:
vbo met adres, puntlocatie en referentie naar pand

beschikbare kolommen:
* identif: natuurlijke id van vbo      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* pand_identif: natuurlijk id van pand dat aan dit vbo gekoppeld is,
* gemeente: -,
* woonplaats: -,
* straatnaam: -,
* huisnummer: -,
* huisletter: -,
* huisnummer_toev: -,
* postcode: -,
* status: -,
* the_geom: puntlocatie

';
                
                
--drop view v2_standplaats_locatie_adres cascade;
CREATE OR REPLACE VIEW
    v2_standplaats_locatie_adres
    (
        identif,
        begin_geldigheid,
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
    spl.sc_identif                                            AS identif,
    to_date(benter.dat_beg_geldh, 'YYYYMMDDHH24MISSUS'::text) AS begin_geldigheid,
    bva.gemeente,
    bva.woonplaats,
    bva.straatnaam,
    bva.huisnummer,
    bva.huisletter,
    bva.huisnummer_toev,
    bva.postcode,
    spl.status,
    st_centroid(benter.geom) AS the_geom
FROM
    (((standplaats spl
JOIN
    benoemd_terrein benter
ON
    (((
                benter.sc_identif)::text = (spl.sc_identif)::text)))
LEFT JOIN
    standplaats_nummeraand sna
ON
    (((
                sna.fk_nn_lh_spl_sc_identif)::text = (spl.sc_identif)::text)))
LEFT JOIN
    v2_volledig_adres bva
ON
    (((
                sna.fk_nn_rh_nra_sc_identif)::text = (bva.identif)::text)));
COMMENT ON VIEW v2_standplaats_locatie_adres
IS
    'commentaar view v2_standplaats_locatie_adres:
standplaats met adres en puntlocatie

beschikbare kolommen:
* identif: natuurlijke id van standplaats      
* gemeente: -,
* woonplaats: -,
* straatnaam: -,
* huisnummer: -,
* huisletter: -,
* huisnummer_toev: -,
* postcode: -,
* status: -,
* the_geom: puntlocatie
';
                
--drop view v2_ligplaats_locatie_adres cascade;
CREATE OR REPLACE VIEW
    v2_ligplaats_locatie_adres
    (
        identif,
        begin_geldigheid,
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
    lpa.sc_identif                                            AS identif,
    to_date(benter.dat_beg_geldh, 'YYYYMMDDHH24MISSUS'::text) AS begin_geldigheid,
    bva.gemeente,
    bva.woonplaats,
    bva.straatnaam,
    bva.huisnummer,
    bva.huisletter,
    bva.huisnummer_toev,
    bva.postcode,
    lpa.status,
    st_centroid(benter.geom) AS the_geom
FROM
    (((ligplaats lpa
JOIN
    benoemd_terrein benter
ON
    (((
                benter.sc_identif)::text = (lpa.sc_identif)::text)))
LEFT JOIN
    ligplaats_nummeraand lna
ON
    (((
                lna.fk_nn_lh_lpl_sc_identif)::text = (lpa.sc_identif)::text)))
LEFT JOIN
    v2_volledig_adres bva
ON
    (((
                lna.fk_nn_rh_nra_sc_identif)::text = (bva.identif)::text)));
COMMENT ON VIEW v2_ligplaats_locatie_adres
IS
    'commentaar view v2_ligplaats_locatie_adres:
ligplaats met adres en puntlocatie

beschikbare kolommen:
* identif: natuurlijke id van ligplaats      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* gemeente: -,
* woonplaats: -,
* straatnaam: -,
* huisnummer: -,
* huisletter: -,
* huisnummer_toev: -,
* postcode: -,
* status: -,
* the_geom: puntlocatie
';
                
--drop view v2_pand cascade;
CREATE OR REPLACE VIEW
    v2_pand
    (
        objectid,
        identif,
        begin_geldigheid,
        bouwjaar,
        status,
        the_geom
    ) AS
SELECT
    (row_number() OVER ())::INTEGER AS objectid,
    pand.identif,
    to_date(pand.dat_beg_geldh, 'YYYYMMDDHH24MISSUS'::text) AS begin_geldigheid,
    pand.oorspronkelijk_bouwjaar                            AS bouwjaar,
    pand.status,
    pand.geom_bovenaanzicht AS the_geom
FROM
    pand;
COMMENT ON VIEW v2_pand
IS
    'commentaar view v2_pand:
pand met datum veld voor begin geldigheid en objectid voor arcgis
beschikbare kolommen:
* objectid: uniek id bruikbaar voor arcgis,
* identif: natuurlijke id van pand      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* bouwjaar: -,
* status: -,
* the_geom: pandvlak
';
    
--drop view v2_benoemd_obj_locatie_adres cascade;
CREATE OR REPLACE VIEW
    v2_benoemd_obj_locatie_adres
    (
        objectid,
        identif,
        begin_geldigheid,
        pand_identif,
        soort,
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
    (row_number() OVER ())::INTEGER AS objectid,
    qry.identif,
    qry.begin_geldigheid,
    qry.pand_identif,
    qry.soort,
    qry.gemeente,
    qry.woonplaats,
    qry.straatnaam,
    qry.huisnummer,
    qry.huisletter,
    qry.huisnummer_toev,
    qry.postcode,
    qry.status,
    qry.the_geom
FROM
    (
        SELECT
            v2_vbo_locatie_adres.identif,
            v2_vbo_locatie_adres.begin_geldigheid,
            v2_vbo_locatie_adres.pand_identif,
            'VBO'::CHARACTER VARYING(50) AS soort,
            v2_vbo_locatie_adres.gemeente,
            v2_vbo_locatie_adres.woonplaats,
            v2_vbo_locatie_adres.straatnaam,
            v2_vbo_locatie_adres.huisnummer,
            v2_vbo_locatie_adres.huisletter,
            v2_vbo_locatie_adres.huisnummer_toev,
            v2_vbo_locatie_adres.postcode,
            v2_vbo_locatie_adres.status,
            v2_vbo_locatie_adres.the_geom
        FROM
            v2_vbo_locatie_adres
        UNION ALL
        SELECT
            v2_ligplaats_locatie_adres.identif,
            v2_ligplaats_locatie_adres.begin_geldigheid,
            NULL::CHARACTER VARYING(16)        AS pand_identif,
            'LIGPLAATS'::CHARACTER VARYING(50) AS soort,
            v2_ligplaats_locatie_adres.gemeente,
            v2_ligplaats_locatie_adres.woonplaats,
            v2_ligplaats_locatie_adres.straatnaam,
            v2_ligplaats_locatie_adres.huisnummer,
            v2_ligplaats_locatie_adres.huisletter,
            v2_ligplaats_locatie_adres.huisnummer_toev,
            v2_ligplaats_locatie_adres.postcode,
            v2_ligplaats_locatie_adres.status,
            v2_ligplaats_locatie_adres.the_geom
        FROM
            v2_ligplaats_locatie_adres
        UNION ALL
        SELECT
            v2_standplaats_locatie_adres.identif,
            v2_standplaats_locatie_adres.begin_geldigheid,
            NULL::CHARACTER VARYING(16)          AS pand_identif,
            'STANDPLAATS'::CHARACTER VARYING(50) AS soort,
            v2_standplaats_locatie_adres.gemeente,
            v2_standplaats_locatie_adres.woonplaats,
            v2_standplaats_locatie_adres.straatnaam,
            v2_standplaats_locatie_adres.huisnummer,
            v2_standplaats_locatie_adres.huisletter,
            v2_standplaats_locatie_adres.huisnummer_toev,
            v2_standplaats_locatie_adres.postcode,
            v2_standplaats_locatie_adres.status,
            v2_standplaats_locatie_adres.the_geom
        FROM
            v2_standplaats_locatie_adres) qry;
COMMENT ON VIEW v2_benoemd_obj_locatie_adres
IS
    'commentaar view v2_benoemd_obj_locatie_adres:
alle benoemde objecten (vbo, standplaats en ligplaats) met adres, puntlocatie, objectid voor arcgis en bij vbo referentie naar pand
beschikbare kolommen:
* identif: natuurlijke id van benoemd object      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* pand_identif: natuurlijk id van pand dat aan dit object gekoppeld is (alleen vbo),
* gemeente: -,
* woonplaats: -,
* straatnaam: -,
* huisnummer: -,
* huisletter: -,
* huisnummer_toev: -,
* postcode: -,
* status: -,
* the_geom: puntlocatie
';
