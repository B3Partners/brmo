/*
Views for visualizing the BAG data.
versie 2
22-6-2018
*/
-- DROP VIEWS
--drop view v_adres cascade;
--drop view v_vbo_adres cascade;
--drop view v_standplaats_adres cascade;
--drop view v_ligplaats_adres cascade;
--drop view v_pand cascade;
--drop view v_benoemd_obj_adres cascade;

--drop materialized view m_pand cascade;
--drop materialized view m_benoemd_obj_adres cascade;
--drop materialized view m_adres cascade;

--DROP INDEX m_adres_objectid cascade;
--DROP INDEX m_adres_identif cascade;
--DROP INDEX m_pand_objectid cascade;
--DROP INDEX m_pand_identif cascade;
--DROP INDEX m_pand_the_geom_idx cascade;
--DROP INDEX m_benoemd_obj_adres_objectid cascade;
--DROP INDEX m_benoemd_obj_adres_identif cascade;
--DROP INDEX m_ben_obj_adr_geom_idx cascade;

--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('basis', 'v_pand', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('basis', 'v_benoemd_obj_adres', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('basis', 'v_adres', 'objectid', 'assigned');

--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('basis', 'm_pand', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('basis', 'm_benoemd_obj_adres', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('basis', 'm_adres', 'objectid', 'assigned');

alter session set query_rewrite_integrity=stale_tolerated;

--drop view v_adres;
CREATE OR REPLACE VIEW
    v_adres
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
    CAST(ROWNUM AS INTEGER)                            AS objectid,
    na.sc_identif                                              AS na_identif,
    to_date(addrobj.dat_beg_geldh, 'YYYYMMDDHH24MISSUS') AS begin_geldigheid,
    CASE
        WHEN position('-' IN addrobj.dat_beg_geldh) = 5
        THEN to_date(addrobj.dat_beg_geldh, 'YYYY-MM-DD')
        ELSE to_date(addrobj.dat_beg_geldh, 'YYYYMMDDHH24MISSUS')
    END AS begin_geldigheid,
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
                    ((wnplts.identif) = (addrobj.fk_6wpl_identif)))
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
    (((((nummeraand na
LEFT JOIN
    addresseerb_obj_aand addrobj
ON
    (((
                addrobj.identif) = (na.sc_identif))))
JOIN
    gem_openb_rmte geor
ON
    (((
                geor.identifcode) = (addrobj.fk_7opr_identifcode))))
LEFT JOIN
    openb_rmte_wnplts orwp
ON
    (((
                geor.identifcode) = (orwp.fk_nn_lh_opr_identifcode))))
LEFT JOIN
    wnplts wp
ON
    (((
                orwp.fk_nn_rh_wpl_identif) = (wp.identif))))
LEFT JOIN
    gemeente gem
ON
    ((
            wp.fk_7gem_code = gem.code)));
            
--drop materialized view m_adres cascade;
CREATE MATERIALIZED VIEW m_adres 
BUILD DEFERRED
REFRESH ON DEMAND
AS
SELECT
    *
FROM
    v_adres;

CREATE UNIQUE INDEX m_adres_objectid ON m_adres (objectid asc);
CREATE INDEX m_adres_identif ON m_adres (na_identif asc);

COMMENT ON MATERIALIZED VIEW m_adres
IS
    'commentaar view m_adres:
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
            
--drop view v_vbo_adres cascade;
CREATE OR REPLACE VIEW
    v_vbo_adres
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
        THEN to_date(gobj.dat_beg_geldh, 'YYYY-MM-DD')
        ELSE to_date(gobj.dat_beg_geldh, 'YYYYMMDDHH24MISSUS')
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
    (((((verblijfsobj vbo
JOIN
    gebouwd_obj gobj
ON
    (((
                gobj.sc_identif) = (vbo.sc_identif))))
LEFT JOIN
    verblijfsobj_pand fkpand
ON
    (((
                fkpand.fk_nn_lh_vbo_sc_identif) = (vbo.sc_identif))))
LEFT JOIN
    pand
ON
    (((
                fkpand.fk_nn_rh_pnd_identif) = (pand.identif))))
LEFT JOIN
    verblijfsobj_nummeraand vna
ON
    (((
                vna.fk_nn_lh_vbo_sc_identif) = (vbo.sc_identif))))
LEFT JOIN
    v_adres bva
ON
    (((
                vna.fk_nn_rh_nra_sc_identif) = (bva.na_identif))));
                
                
--drop view v_standplaats_adres cascade;
CREATE OR REPLACE VIEW
    v_standplaats_adres
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
        THEN to_date(benter.dat_beg_geldh, 'YYYY-MM-DD')
        ELSE to_date(benter.dat_beg_geldh, 'YYYYMMDDHH24MISSUS')
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
    SDO_GEOM.SDO_CENTROID(benter.geom,2) AS the_geom
FROM
    (((standplaats spl
JOIN
    benoemd_terrein benter
ON
    (((
                benter.sc_identif) = (spl.sc_identif))))
LEFT JOIN
    standplaats_nummeraand sna
ON
    (((
                sna.fk_nn_lh_spl_sc_identif) = (spl.sc_identif))))
LEFT JOIN
    v_adres bva
ON
    (((
                sna.fk_nn_rh_nra_sc_identif) = (bva.na_identif))));
                
--drop view v_ligplaats_adres cascade;
CREATE OR REPLACE VIEW
    v_ligplaats_adres
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
        THEN to_date(benter.dat_beg_geldh, 'YYYY-MM-DD')
        ELSE to_date(benter.dat_beg_geldh, 'YYYYMMDDHH24MISSUS')
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
    SDO_GEOM.SDO_CENTROID(benter.geom,2) AS the_geom
FROM
    (((ligplaats lpa
JOIN
    benoemd_terrein benter
ON
    (((
                benter.sc_identif) = (lpa.sc_identif))))
LEFT JOIN
    ligplaats_nummeraand lna
ON
    (((
                lna.fk_nn_lh_lpl_sc_identif) = (lpa.sc_identif))))
LEFT JOIN
    v_adres bva
ON
    (((
                lna.fk_nn_rh_nra_sc_identif) = (bva.na_identif))));

                
--drop view v_pand cascade;
CREATE OR REPLACE VIEW
    v_pand
    (
        objectid,
        pand_identif,
        begin_geldigheid,
        bouwjaar,
        status,
        the_geom
    ) AS
SELECT
    CAST(ROWNUM AS INTEGER) AS objectid,
    pand.identif as pand_identif,
    CASE
        WHEN position('-' IN pand.dat_beg_geldh) = 5
        THEN to_date(pand.dat_beg_geldh, 'YYYY-MM-DD')
        ELSE to_date(pand.dat_beg_geldh, 'YYYYMMDDHH24MISSUS')
    END AS begin_geldigheid,
    pand.oorspronkelijk_bouwjaar                            AS bouwjaar,
    pand.status,
    pand.geom_bovenaanzicht AS the_geom
FROM
    pand;

--drop materialized view m_pand cascade;
CREATE MATERIALIZED VIEW m_pand 
BUILD DEFERRED
REFRESH ON DEMAND
AS
SELECT
    *
FROM
    v_pand;
create unique index m_pand_objectid on m_pand (objectid ASC);
create index m_pand_identif on m_pand(pand_identif ASC);
create index m_pand_the_geom_idx on m_pand(the_geom) indextype is mdsys.spatial_index;

delete from user_sdo_geom_metadata where TABLE_NAME ='m_pand';
insert into user_sdo_geom_metadata values ('m_pand', 'the_geom', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);


COMMENT ON MATERIALIZED VIEW m_pand
IS
    'commentaar view v_pand:
pand met datum veld voor begin geldigheid en objectid voor geoserver/arcgis
beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* pand_identif: natuurlijke id van pand      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* bouwjaar: -,
* status: -,
* the_geom: pandvlak
';
    
--drop view v_benoemd_obj_adres cascade;
CREATE OR REPLACE VIEW
    v_benoemd_obj_adres
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
    CAST(ROWNUM AS INTEGER) AS objectid,
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
            CAST('VBO' AS CHARACTER VARYING(50)) AS soort,
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
            v_vbo_adres vvla
        UNION ALL
        SELECT
            vlla.lpl_identif as benoemdobj_identif,
            vlla.na_identif,
            vlla.begin_geldigheid,
            CAST(NULL AS CHARACTER VARYING(16))        AS pand_identif,
            CAST('LIGPLAATS' AS CHARACTER VARYING(50)) AS soort,
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
            v_ligplaats_adres vlla
        UNION ALL
        SELECT
            vsla.spl_identif as benoemdobj_identif,
            vsla.na_identif,
            vsla.begin_geldigheid,
            CAST(NULL AS CHARACTER VARYING(16))        AS pand_identif,
            CAST('STANDPLAATS' AS CHARACTER VARYING(50)) AS soort,
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
            v_standplaats_adres vsla
    ) qry;
--drop materialized view m_benoemd_obj_adres cascade;
CREATE MATERIALIZED VIEW m_benoemd_obj_adres 
BUILD DEFERRED
REFRESH ON DEMAND
AS
SELECT
    *
FROM
    v_benoemd_obj_adres;
CREATE UNIQUE INDEX m_benoemd_obj_adres_objectid ON m_benoemd_obj_adres(objectid ASC);
CREATE INDEX m_benoemd_obj_adres_identif ON m_benoemd_obj_adres (na_identif ASC);
CREATE INDEX m_ben_obj_adr_geom_idx ON m_benoemd_obj_adres(the_geom) indextype is mdsys.spatial_index;

delete from user_sdo_geom_metadata where TABLE_NAME ='m_benoemd_obj_adres';
insert into user_sdo_geom_metadata values ('m_benoemd_obj_adres', 'the_geom', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

COMMENT ON MATERIALIZED VIEW m_benoemd_obj_adres
IS
    'commentaar view v_benoemd_obj_adres:
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

