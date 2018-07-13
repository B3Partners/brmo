/*
Views for visualizing the BAG data.
versie 2
8-6-2018
*/
-- DROP VIEWS
--drop view basis.v_adres cascade;
--drop view basis.v_vbo_adres cascade;
--drop view basis.v_standplaats_adres cascade;
--drop view basis.v_ligplaats_adres cascade;
--drop view basis.v_pand cascade;
--drop view basis.v_benoemd_obj_adres cascade;

--drop materialized view basis.m_pand cascade;
--drop materialized view basis.m_benoemd_obj_adres cascade;
--drop materialized view basis.m_adres cascade;

--DROP INDEX m_adres_objectid cascade;
--DROP INDEX m_adres_identif cascade;
--DROP INDEX m_pand_objectid cascade;
--DROP INDEX m_pand_identif cascade;
--DROP INDEX m_pand_the_geom_idx cascade;
--DROP INDEX m_benoemd_obj_adres_objectid cascade;
--DROP INDEX m_benoemd_obj_adres_identif cascade;
--DROP INDEX m_benoemd_obj_adres_the_geom_idx cascade;

--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('basis', 'v_pand', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('basis', 'v_benoemd_obj_adres', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('basis', 'v_adres', 'objectid', 'assigned');

--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('basis', 'm_pand', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('basis', 'm_benoemd_obj_adres', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('basis', 'm_adres', 'objectid', 'assigned');

--REFRESH MATERIALIZED VIEW basis.m_pand;
--REFRESH MATERIALIZED VIEW basis.m_benoemd_obj_adres;
--REFRESH MATERIALIZED VIEW basis.m_adres;

set session authorization flamingo;

--drop view basis.v_adres;
CREATE OR REPLACE VIEW
    basis.v_adres
    (
        objectid,
        na_identif,
        begin_geldigheid,
        gemeente,
        woonplaats,
        straatnaam,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        geor_identif,
        wpl_identif,
        gem_code
    ) AS
SELECT
    (row_number() OVER ())::INTEGER                            AS objectid,
    na.sc_identif                                              AS na_identif,
    CASE
        WHEN position('-' IN addrobj.dat_beg_geldh) = 5
        THEN to_date(addrobj.dat_beg_geldh, 'YYYY-MM-DD'::text)
        ELSE to_date(addrobj.dat_beg_geldh, 'YYYYMMDDHH24MISSUS'::text)
    END AS begin_geldigheid,
    gem.naam                                                   AS gemeente,
    CASE
        WHEN (addrobj.fk_6wpl_identif IS NOT NULL)
        THEN
            (
                SELECT
                    wnplts.naam
                FROM
                    public.wnplts
                WHERE
                    ((wnplts.identif)::text = (addrobj.fk_6wpl_identif)::text))
        ELSE wp.naam
    END                  AS woonplaats,
    geor.naam_openb_rmte AS straatnaam,
    addrobj.huinummer    AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    geor.identifcode as geor_identif,
    wp.identif as wpl_identif,
    gem.code as gem_code
FROM
    (((((public.nummeraand na
LEFT JOIN
    public.addresseerb_obj_aand addrobj
ON
    (((
                addrobj.identif)::text = (na.sc_identif)::text)))
JOIN
    public.gem_openb_rmte geor
ON
    (((
                geor.identifcode)::text = (addrobj.fk_7opr_identifcode)::text)))
LEFT JOIN
    public.openb_rmte_wnplts orwp
ON
    (((
                geor.identifcode)::text = (orwp.fk_nn_lh_opr_identifcode)::text)))
LEFT JOIN
    public.wnplts wp
ON
    (((
                orwp.fk_nn_rh_wpl_identif)::text = (wp.identif)::text)))
LEFT JOIN
    public.gemeente gem
ON
    ((
            wp.fk_7gem_code = gem.code)));
            
COMMENT ON VIEW basis.v_adres
IS
    'commentaar view basis.v_adres:
volledig adres zonder locatie

beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* na_identif: natuurlijke id van nummeraanduiding,      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* gemeente: -,
* woonplaats: -,
* straatnaam: -,
* huisnummer: -,
* huisletter: -,
* huisnummer_toev: -,
* postcode: -,
* geor_identif: natuurlijk id van gemeentelijke openbare ruimte,
* wpl_identif: natuurlijk id van woonplaats,
* gem_code: gemeentecode

';
--drop materialized view basis.m_adres cascade;
CREATE MATERIALIZED VIEW basis.m_adres AS
SELECT
    *
FROM
    basis.v_adres WITH NO DATA;
CREATE UNIQUE INDEX m_adres_objectid ON basis.m_adres USING btree (objectid);
CREATE INDEX m_adres_identif ON basis.m_adres USING btree (na_identif);
            
--drop view basis.v_vbo_adres cascade;
CREATE OR REPLACE VIEW
    basis.v_vbo_adres
    (
        vbo_identif,
        begin_geldigheid,
        pand_identif,
        na_identif,
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
    vbo.sc_identif                                          AS vbo_identif,
    CASE
        WHEN position('-' IN gobj.dat_beg_geldh) = 5
        THEN to_date(gobj.dat_beg_geldh, 'YYYY-MM-DD'::text)
        ELSE to_date(gobj.dat_beg_geldh, 'YYYYMMDDHH24MISSUS'::text)
    END AS begin_geldigheid,
    fkpand.fk_nn_rh_pnd_identif                             AS pand_identif,
    bva.na_identif 																					as na_identif,
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
    (((((public.verblijfsobj vbo
JOIN
    public.gebouwd_obj gobj
ON
    (((
                gobj.sc_identif)::text = (vbo.sc_identif)::text)))
LEFT JOIN
    public.verblijfsobj_pand fkpand
ON
    (((
                fkpand.fk_nn_lh_vbo_sc_identif)::text = (vbo.sc_identif)::text)))
LEFT JOIN
    public.pand
ON
    (((
                fkpand.fk_nn_rh_pnd_identif)::text = (pand.identif)::text)))
LEFT JOIN
    public.verblijfsobj_nummeraand vna
ON
    (((
                vna.fk_nn_lh_vbo_sc_identif)::text = (vbo.sc_identif)::text)))
LEFT JOIN
    basis.v_adres bva
ON
    (((
                vna.fk_nn_rh_nra_sc_identif)::text = (bva.na_identif)::text)));
COMMENT ON VIEW basis.v_vbo_adres
IS
    'commentaar view basis.v_vbo_adres:
vbo met adres, puntlocatie en referentie naar pand

beschikbare kolommen:
* vbo_identif: natuurlijke id van vbo      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* pand_identif: natuurlijk id van pand dat aan dit vbo gekoppeld is,
* na_identif: natuurlijk id van nummeraanduiding,
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
                
                
--drop view basis.v_standplaats_adres cascade;
CREATE OR REPLACE VIEW
    basis.v_standplaats_adres
    (
        spl_identif,
        begin_geldigheid,
        na_identif,
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
    spl.sc_identif                                            AS spl_identif,
    CASE
        WHEN position('-' IN benter.dat_beg_geldh) = 5
        THEN to_date(benter.dat_beg_geldh, 'YYYY-MM-DD'::text)
        ELSE to_date(benter.dat_beg_geldh, 'YYYYMMDDHH24MISSUS'::text)
    END AS begin_geldigheid,
    bva.na_identif 					      as na_identif,
    bva.gemeente,
    bva.woonplaats,
    bva.straatnaam,
    bva.huisnummer,
    bva.huisletter,
    bva.huisnummer_toev,
    bva.postcode,
    spl.status,
    public.st_centroid(benter.geom) AS the_geom
FROM
    (((public.standplaats spl
JOIN
    public.benoemd_terrein benter
ON
    (((
                benter.sc_identif)::text = (spl.sc_identif)::text)))
LEFT JOIN
    public.standplaats_nummeraand sna
ON
    (((
                sna.fk_nn_lh_spl_sc_identif)::text = (spl.sc_identif)::text)))
LEFT JOIN
    basis.v_adres bva
ON
    (((
                sna.fk_nn_rh_nra_sc_identif)::text = (bva.na_identif)::text)));
COMMENT ON VIEW basis.v_standplaats_adres
IS
    'commentaar view basis.v_standplaats_adres:
standplaats met adres en puntlocatie

beschikbare kolommen:
* spl_identif: natuurlijke id van standplaats      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* na_identif: natuurlijk id van nummeraanduiding,
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
                
--drop view basis.v_ligplaats_adres cascade;
CREATE OR REPLACE VIEW
    basis.v_ligplaats_adres
    (
        lpl_identif,
        begin_geldigheid,
        na_identif,
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
    lpa.sc_identif                                            AS lpl_identif,
    CASE
        WHEN position('-' IN benter.dat_beg_geldh) = 5
        THEN to_date(benter.dat_beg_geldh, 'YYYY-MM-DD'::text)
        ELSE to_date(benter.dat_beg_geldh, 'YYYYMMDDHH24MISSUS'::text)
    END AS begin_geldigheid,
    bva.na_identif 				              as na_identif,
    bva.gemeente,
    bva.woonplaats,
    bva.straatnaam,
    bva.huisnummer,
    bva.huisletter,
    bva.huisnummer_toev,
    bva.postcode,
    lpa.status,
    public.st_centroid(benter.geom) AS the_geom
FROM
    (((public.ligplaats lpa
JOIN
    public.benoemd_terrein benter
ON
    (((
                benter.sc_identif)::text = (lpa.sc_identif)::text)))
LEFT JOIN
    public.ligplaats_nummeraand lna
ON
    (((
                lna.fk_nn_lh_lpl_sc_identif)::text = (lpa.sc_identif)::text)))
LEFT JOIN
    basis.v_adres bva
ON
    (((
                lna.fk_nn_rh_nra_sc_identif)::text = (bva.na_identif)::text)));
COMMENT ON VIEW basis.v_ligplaats_adres
IS
    'commentaar view basis.v_ligplaats_adres:
ligplaats met adres en puntlocatie

beschikbare kolommen:
* lpl_identif: natuurlijke id van ligplaats      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* na_identif: natuurlijk id van nummeraanduiding,
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
                
--drop view basis.v_pand cascade;
CREATE OR REPLACE VIEW
    basis.v_pand
    (
        objectid,
        pand_identif,
        begin_geldigheid,
        bouwjaar,
        status,
        the_geom
    ) AS
SELECT
    (row_number() OVER ())::INTEGER AS objectid,
    pand.identif as pand_identif,
    CASE
        WHEN position('-' IN pand.dat_beg_geldh) = 5
        THEN to_date(pand.dat_beg_geldh, 'YYYY-MM-DD'::text)
        ELSE to_date(pand.dat_beg_geldh, 'YYYYMMDDHH24MISSUS'::text)
    END AS begin_geldigheid,
    pand.oorspronkelijk_bouwjaar                            AS bouwjaar,
    pand.status,
    pand.geom_bovenaanzicht AS the_geom
FROM
    public.pand;
COMMENT ON VIEW basis.v_pand
IS
    'commentaar view basis.v_pand:
pand met datum veld voor begin geldigheid en objectid voor geoserver/arcgis
beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* pand_identif: natuurlijke id van pand      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* bouwjaar: -,
* status: -,
* the_geom: pandvlak
';
--drop materialized view basis.m_pand cascade;
CREATE MATERIALIZED VIEW basis.m_pand AS
SELECT
    *
FROM
    basis.v_pand WITH NO DATA;
CREATE UNIQUE INDEX m_pand_objectid ON basis.m_pand USING btree (objectid);
CREATE INDEX m_pand_identif ON basis.m_pand USING btree (pand_identif);
CREATE INDEX m_pand_the_geom_idx ON basis.m_pand USING gist (the_geom);

    
--drop view basis.v_benoemd_obj_adres cascade;
CREATE OR REPLACE VIEW
    basis.v_benoemd_obj_adres
    (
        objectid,
        benoemdobj_identif,
        na_identif,
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
    qry.benoemdobj_identif,
    qry.na_identif,
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
            vvla.vbo_identif as benoemdobj_identif,
            vvla.na_identif,
            vvla.begin_geldigheid,
            vvla.pand_identif,
            'VBO'::CHARACTER VARYING(50) AS soort,
            vvla.gemeente,
            vvla.woonplaats,
            vvla.straatnaam,
            vvla.huisnummer,
            vvla.huisletter,
            vvla.huisnummer_toev,
            vvla.postcode,
            vvla.status,
            vvla.the_geom
        FROM
            basis.v_vbo_adres vvla
        UNION ALL
        SELECT
            vlla.lpl_identif as benoemdobj_identif,
            vlla.na_identif,
            vlla.begin_geldigheid,
            NULL::CHARACTER VARYING(16)        AS pand_identif,
            'LIGPLAATS'::CHARACTER VARYING(50) AS soort,
            vlla.gemeente,
            vlla.woonplaats,
            vlla.straatnaam,
            vlla.huisnummer,
            vlla.huisletter,
            vlla.huisnummer_toev,
            vlla.postcode,
            vlla.status,
            vlla.the_geom
        FROM
            basis.v_ligplaats_adres vlla
        UNION ALL
        SELECT
            vsla.spl_identif as benoemdobj_identif,
            vsla.na_identif,
            vsla.begin_geldigheid,
            NULL::CHARACTER VARYING(16)          AS pand_identif,
            'STANDPLAATS'::CHARACTER VARYING(50) AS soort,
            vsla.gemeente,
            vsla.woonplaats,
            vsla.straatnaam,
            vsla.huisnummer,
            vsla.huisletter,
            vsla.huisnummer_toev,
            vsla.postcode,
            vsla.status,
            vsla.the_geom
        FROM
            basis.v_standplaats_adres vsla
    ) qry;
COMMENT ON VIEW basis.v_benoemd_obj_adres
IS
    'commentaar view basis.v_benoemd_obj_adres:
alle benoemde objecten (vbo, standplaats en ligplaats) met adres, puntlocatie, objectid voor geoserver/arcgis en bij vbo referentie naar pand
beschikbare kolommen:
* benoemdobj_identif: natuurlijke id van benoemd object      
* na_identif: natuurlijke id van nummeraanduiding      
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
--drop materialized view basis.m_benoemd_obj_adres cascade;
CREATE MATERIALIZED VIEW basis.m_benoemd_obj_adres AS
SELECT
    *
FROM
    basis.v_benoemd_obj_adres WITH NO DATA;
CREATE UNIQUE INDEX m_benoemd_obj_adres_objectid ON basis.m_benoemd_obj_adres USING btree (objectid);
CREATE INDEX m_benoemd_obj_adres_identif ON basis.m_benoemd_obj_adres USING btree (na_identif);
CREATE INDEX m_benoemd_obj_adres_the_geom_idx ON basis.m_benoemd_obj_adres USING gist (the_geom);

