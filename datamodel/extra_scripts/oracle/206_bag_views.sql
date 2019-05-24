/*
Views for visualizing the BAG data.
versie 2
28-8-2018
*/
-- DROP VIEWS
--drop view vb_vbo_adres;
--drop view vb_standplaats_adres;
--drop view vb_ligplaats_adres;

--drop materialized view mb_pand;
--drop materialized view mb_benoemd_obj_adres;
--drop materialized view mb_adres;
--drop materialized view mb_ben_obj_nevenadres;

--DROP INDEX MB_BENOEMD_OBJ_ADRES_IDENTIF; 
--DROP INDEX MB_BEN_OBJ_ADR_GEOM_IDX; 
--DROP INDEX MB_BEN_OBJ_NEVENADRES_IDENTIF; 
--DROP INDEX MB_PAND_IDENTIF; 
--DROP INDEX MB_PAND_THE_GEOM_IDX; 
--DROP INDEX mb_adres_identif; 
--DROP INDEX MB_BEN_OBJ_ADRES_OBJECTID; 
--DROP INDEX MB_PAND_OBJECTID; 
--DROP INDEX mb_adres_objectid; 

--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('RSGB', 'vb_pand', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('RSGB', 'vb_benoemd_obj_adres', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('RSGB', 'vb_adres', 'objectid', 'assigned');

--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('RSGB', 'mb_pand', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('RSGB', 'mb_benoemd_obj_adres', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('RSGB', 'mb_adres', 'objectid', 'assigned');

--BEGIN
-- run 1
--DBMS_SNAPSHOT.REFRESH( 'mb_adres','c');
--DBMS_SNAPSHOT.REFRESH( 'mb_pand','c');
-- run 2
--DBMS_SNAPSHOT.REFRESH( 'mb_benoemd_obj_adres','c');
--DBMS_SNAPSHOT.REFRESH( 'mb_ben_obj_nevenadres','c');
--END

alter session set query_rewrite_integrity=stale_tolerated;

--drop materialized view mb_adres;
CREATE MATERIALIZED VIEW mb_adres
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
    ) 
BUILD DEFERRED
REFRESH ON DEMAND
AS
SELECT
    CAST(ROWNUM AS INTEGER)                            AS objectid,
    na.sc_identif                                              AS na_identif,
    CAST(CASE
        WHEN
            ((INSTR(addrobj.dat_beg_geldh,'-')  = 5) AND (INSTR(addrobj.dat_beg_geldh,'-',1,2)  = 8))
        THEN
            addrobj.dat_beg_geldh
        ELSE
            SUBSTR(addrobj.dat_beg_geldh,1,4) || '-' ||
            SUBSTR(addrobj.dat_beg_geldh,5,2) || '-' ||
            SUBSTR(addrobj.dat_beg_geldh,7,2)
    END AS CHARACTER VARYING(10))  AS begin_geldigheid,
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

CREATE UNIQUE INDEX mb_adres_objectid ON mb_adres (objectid asc);
CREATE INDEX mb_adres_identif ON mb_adres (na_identif asc);

COMMENT ON MATERIALIZED VIEW mb_adres
IS 'commentaar view mb_adres:
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
* gem_code: gemeentecode';

--drop view vb_vbo_adres;
CREATE OR REPLACE VIEW
    vb_vbo_adres
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
    CAST(CASE
        WHEN
            ((INSTR(gobj.dat_beg_geldh,'-')  = 5) AND (INSTR(gobj.dat_beg_geldh,'-',1,2)  = 8))
        THEN
            gobj.dat_beg_geldh
        ELSE
            SUBSTR(gobj.dat_beg_geldh,1,4) || '-' ||
            SUBSTR(gobj.dat_beg_geldh,5,2) || '-' ||
            SUBSTR(gobj.dat_beg_geldh,7,2)
    END AS CHARACTER VARYING(10)) AS begin_geldigheid,
    fkpand.fk_nn_rh_pnd_identif                             AS pand_identif,
    bva.na_identif                                                                                  as na_identif,
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
    (((verblijfsobj vbo
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
    mb_adres bva
ON
    (((
                vbo.fk_11nra_sc_identif) = (bva.na_identif))));


--drop view vb_standplaats_adres;
CREATE OR REPLACE VIEW
    vb_standplaats_adres
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
    CAST(CASE
        WHEN
            ((INSTR(benter.dat_beg_geldh,'-')  = 5) AND (INSTR(benter.dat_beg_geldh,'-',1,2)  = 8))
        THEN
            benter.dat_beg_geldh
        ELSE
            SUBSTR(benter.dat_beg_geldh,1,4) || '-' ||
            SUBSTR(benter.dat_beg_geldh,5,2) || '-' ||
            SUBSTR(benter.dat_beg_geldh,7,2)
    END AS CHARACTER VARYING(10)) AS begin_geldigheid,
    bva.na_identif                        as na_identif,
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
    ((standplaats spl
JOIN
    benoemd_terrein benter
ON
    (((
                benter.sc_identif) = (spl.sc_identif))))
LEFT JOIN
    mb_adres bva
ON
    (((
                spl.fk_4nra_sc_identif) = (bva.na_identif))));

--drop view vb_ligplaats_adres;
CREATE OR REPLACE VIEW
    vb_ligplaats_adres
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
    lpl.sc_identif                                            AS lpl_identif,
    CAST(CASE
        WHEN
            ((INSTR(benter.dat_beg_geldh,'-')  = 5) AND (INSTR(benter.dat_beg_geldh,'-',1,2)  = 8))
        THEN
            benter.dat_beg_geldh
        ELSE
            SUBSTR(benter.dat_beg_geldh,1,4) || '-' ||
            SUBSTR(benter.dat_beg_geldh,5,2) || '-' ||
            SUBSTR(benter.dat_beg_geldh,7,2)
    END AS CHARACTER VARYING(10)) AS begin_geldigheid,
    bva.na_identif                            as na_identif,
    bva.gemeente,
    bva.woonplaats,
    bva.straatnaam,
    bva.huisnummer,
    bva.huisletter,
    bva.huisnummer_toev,
    bva.postcode,
    lpl.status,
    SDO_GEOM.SDO_CENTROID(benter.geom,2) AS the_geom
FROM
    ((ligplaats lpl
JOIN
    benoemd_terrein benter
ON
    (((
                benter.sc_identif) = (lpl.sc_identif))))
LEFT JOIN
    mb_adres bva
ON
    (((
                lpl.fk_4nra_sc_identif) = (bva.na_identif))));


--drop materialized view mb_pand;
CREATE MATERIALIZED VIEW mb_pand
    (
        objectid,
        pand_identif,
        begin_geldigheid,
        bouwjaar,
        status,
        the_geom
    ) 
BUILD DEFERRED
REFRESH ON DEMAND
AS
SELECT
    CAST(ROWNUM AS INTEGER) AS objectid,
    pand.identif as pand_identif,
    CAST(CASE
        WHEN
            ((INSTR(pand.dat_beg_geldh,'-')  = 5) AND (INSTR(pand.dat_beg_geldh,'-',1,2)  = 8))
        THEN
            pand.dat_beg_geldh
        ELSE
                SUBSTR(pand.dat_beg_geldh,1,4) || '-' ||
            SUBSTR(pand.dat_beg_geldh,5,2) || '-' ||
            SUBSTR(pand.dat_beg_geldh,7,2)
    END AS CHARACTER VARYING(10)) AS begin_geldigheid,
    pand.oorspronkelijk_bouwjaar                            AS bouwjaar,
    pand.status,
    pand.geom_bovenaanzicht AS the_geom
FROM
    pand;

CREATE UNIQUE INDEX MB_PAND_OBJECTID ON MB_PAND (OBJECTID ASC);
CREATE INDEX MB_PAND_IDENTIF ON MB_PAND(PAND_IDENTIF ASC);
INSERT INTO USER_SDO_GEOM_METADATA VALUES ('MB_PAND', 'THE_GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
CREATE INDEX MB_PAND_THE_GEOM_IDX ON MB_PAND(THE_GEOM) INDEXTYPE IS MDSYS.SPATIAL_INDEX;

COMMENT ON MATERIALIZED VIEW mb_pand
IS    'commentaar view mb_pand:
pand met datum veld voor begin geldigheid en objectid voor geoserver/arcgis
beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* pand_identif: natuurlijke id van pand
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* bouwjaar: -,
* status: -,
* the_geom: pandvlak';

--drop materialized view mb_benoemd_obj_adres;
CREATE MATERIALIZED VIEW mb_benoemd_obj_adres
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
    ) 
BUILD DEFERRED
REFRESH ON DEMAND
AS
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
            vb_vbo_adres vvla
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
            vb_ligplaats_adres vlla
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
            vb_standplaats_adres vsla
    ) qry;

CREATE UNIQUE INDEX MB_BEN_OBJ_ADRES_OBJECTID ON MB_BENOEMD_OBJ_ADRES(OBJECTID ASC);
CREATE INDEX MB_BENOEMD_OBJ_ADRES_IDENTIF ON MB_BENOEMD_OBJ_ADRES (NA_IDENTIF ASC);
INSERT INTO USER_SDO_GEOM_METADATA VALUES ('MB_BENOEMD_OBJ_ADRES', 'THE_GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
CREATE INDEX MB_BEN_OBJ_ADR_GEOM_IDX ON MB_BENOEMD_OBJ_ADRES(THE_GEOM) INDEXTYPE IS MDSYS.SPATIAL_INDEX;

COMMENT ON MATERIALIZED VIEW mb_benoemd_obj_adres
IS 'commentaar view mb_benoemd_obj_adres:
alle benoemde objecten (vbo, standplaats en ligplaats) met adres, puntlocatie, objectid voor geoserver/arcgis en bij vbo referentie naar pand
beschikbare kolommen:
* benoemdobj_identif: natuurlijke id van benoemd object
* na_identif: natuurlijke id van nummeraanduiding
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* pand_identif: natuurlijk id van pand dat aan dit object gekoppeld is (alleen vbo),
* soort: vbo, ligplaats of standplaats
* gemeente: hoofdadres,
* woonplaats: hoofdadres,
* straatnaam: hoofdadres,
* huisnummer: hoofdadres,
* huisletter: hoofdadres,
* huisnummer_toev: hoofdadres,
* postcode: hoofdadres,
* status: -,
* the_geom: puntlocatie';

--drop materialized view mb_ben_obj_nevenadres;
CREATE MATERIALIZED VIEW mb_ben_obj_nevenadres
    (
        benoemdobj_identif,
        na_identif,
        begin_geldigheid,
        soort,
        gemeente,
        woonplaats,
        straatnaam,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode
    ) 
BUILD DEFERRED
REFRESH ON DEMAND
AS
SELECT
    qry.benoemdobj_identif,
    qry.na_identif,
    qry.begin_geldigheid,
    qry.soort,
    qry.gemeente,
    qry.woonplaats,
    qry.straatnaam,
    qry.huisnummer,
    qry.huisletter,
    qry.huisnummer_toev,
    qry.postcode
FROM
    (
                        SELECT
                            vna.FK_NN_LH_VBO_SC_IDENTIF AS benoemdobj_identif,
                            vba.na_identif,
                            CAST(CASE
                                WHEN
                                  ((INSTR(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,'-')  = 5) AND
                                    (INSTR(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,'-',1,2)  = 8))
                                THEN
                                    vna.fk_nn_lh_vbo_sc_dat_beg_geldh
                                ELSE
                                    SUBSTR(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,1,4) || '-' ||
                                    SUBSTR(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,5,2) || '-' ||
                                    SUBSTR(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,7,2)
                            END AS CHARACTER VARYING(10)) AS begin_geldigheid,
                            CAST('VBO' AS CHARACTER VARYING(50)) AS soort,
                            vba.gemeente,
                            vba.woonplaats,
                            vba.straatnaam,
                            vba.huisnummer,
                            vba.huisletter,
                            vba.huisnummer_toev,
                            vba.postcode
                        FROM
                            mb_adres vba
                        JOIN
                            verblijfsobj_nummeraand vna
                        ON
                            (vna.fk_nn_rh_nra_sc_identif = vba.na_identif)
                        join
                            verblijfsobj vbo
                        on
                            (vna.fk_nn_lh_vbo_sc_identif = vbo.sc_identif)
                        where
                vbo.fk_11nra_sc_identif <> vna.fk_nn_rh_nra_sc_identif
            UNION ALL
                        SELECT
                            lpa.fk_nn_lh_lpl_sc_identif AS benoemdobj_identif,
                            vba.na_identif,
                            CAST(CASE
                                WHEN
                                  ((INSTR(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,'-')  = 5) AND
                                    (INSTR(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,'-',1,2)  = 8))
                                THEN
                                    lpa.fk_nn_lh_lpl_sc_dat_beg_geldh
                                ELSE
                                    SUBSTR(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,1,4) || '-' ||
                                    SUBSTR(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,5,2) || '-' ||
                                    SUBSTR(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,7,2)
                            END AS CHARACTER VARYING(10)) AS begin_geldigheid,
                            CAST('LIGPLAATS' AS CHARACTER VARYING(50)) AS soort,
                            vba.gemeente,
                            vba.woonplaats,
                            vba.straatnaam,
                            vba.huisnummer,
                            vba.huisletter,
                            vba.huisnummer_toev,
                            vba.postcode
                        FROM
                            mb_adres vba
                        JOIN
                            ligplaats_nummeraand lpa
                        ON
                            (lpa.fk_nn_rh_nra_sc_identif = vba.na_identif)
                        join
                            ligplaats lpl
                        on
                            (lpa.fk_nn_lh_lpl_sc_identif = lpl.sc_identif)
                        where
                lpl.fk_4nra_sc_identif <> lpa.fk_nn_rh_nra_sc_identif
        UNION ALL
                        SELECT
                            spa.fk_nn_lh_spl_sc_identif AS benoemdobj_identif,
                            vba.na_identif,
                            CAST(CASE
                                WHEN
                                  ((INSTR(spa.fk_nn_lh_spl_sc_dat_beg_geldh,'-')  = 5) AND
                                    (INSTR(spa.fk_nn_lh_spl_sc_dat_beg_geldh,'-',1,2)  = 8))
                                THEN
                                    spa.fk_nn_lh_spl_sc_dat_beg_geldh
                                ELSE
                                    SUBSTR(spa.fk_nn_lh_spl_sc_dat_beg_geldh,1,4) || '-' ||
                                    SUBSTR(spa.fk_nn_lh_spl_sc_dat_beg_geldh,5,2) || '-' ||
                                    SUBSTR(spa.fk_nn_lh_spl_sc_dat_beg_geldh,7,2)
                            END AS CHARACTER VARYING(10)) AS begin_geldigheid,
                            CAST('STANDPLAATS' AS CHARACTER VARYING(50)) AS soort,
                            vba.gemeente,
                            vba.woonplaats,
                            vba.straatnaam,
                            vba.huisnummer,
                            vba.huisletter,
                            vba.huisnummer_toev,
                            vba.postcode
                        FROM
                            mb_adres vba
                        JOIN
                            standplaats_nummeraand spa
                        ON
                            (spa.fk_nn_rh_nra_sc_identif = vba.na_identif)
                        join
                            standplaats spl
                        on
                            (spa.fk_nn_lh_spl_sc_identif = spl.sc_identif)
                        where
                spl.fk_4nra_sc_identif <> spa.fk_nn_rh_nra_sc_identif
    ) qry;

CREATE INDEX MB_BEN_OBJ_NEVENADRES_IDENTIF ON MB_BEN_OBJ_NEVENADRES (NA_IDENTIF ASC);

COMMENT ON MATERIALIZED VIEW mb_ben_obj_nevenadres
IS    'commentaar view mb_ben_obj_nevenadres:
alle nevenadressen van een benoemde object (vbo, standplaats en ligplaats),
alle hoofdadressen worden weggefilterd.
beschikbare kolommen:
* benoemdobj_identif: natuurlijke id van benoemd object
* na_identif: natuurlijke id van nummeraanduiding
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* soort: vbo, ligplaats of standplaats
* gemeente: nevenadres,
* woonplaats: nevenadres,
* straatnaam: nevenadres,
* huisnummer: nevenadres,
* huisletter: nevenadres,
* huisnummer_toev: nevenadres,
* postcode: nevenadres';
