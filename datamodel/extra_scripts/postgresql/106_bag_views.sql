/*
Views for visualizing the BAG data.
versie 2
28-8-2018
*/
-- DROP VIEWS
--drop view vb_adres cascade;
--drop view vb_vbo_adres cascade;
--drop view vb_standplaats_adres cascade;
--drop view vb_ligplaats_adres cascade;
--drop view vb_pand cascade;
--drop view vb_benoemd_obj_adres cascade;
--drop view vb_ben_obj_nevenadres cascade;


--drop materialized view mb_pand cascade;
--drop materialized view mb_benoemd_obj_adres cascade;
--drop materialized view mb_adres cascade;
--drop materialized view mb_ben_obj_nevenadres cascade;


--DROP INDEX m_adres_objectid cascade;
--DROP INDEX m_adres_identif cascade;
--DROP INDEX m_pand_objectid cascade;
--DROP INDEX m_pand_identif cascade;
--DROP INDEX m_pand_the_geom_idx cascade;
--DROP INDEX m_benoemd_obj_adres_objectid cascade;
--DROP INDEX m_benoemd_obj_adres_identif cascade;
--DROP INDEX m_benoemd_obj_adres_the_geom_idx cascade;
--DROP INDEX m_ben_obj_nevenadres_identif cascade;

--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'vb_pand', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'vb_benoemd_obj_adres', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'vb_adres', 'objectid', 'assigned');

--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'mb_pand', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'mb_benoemd_obj_adres', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'mb_adres', 'objectid', 'assigned');

--REFRESH MATERIALIZED VIEW mb_pand;
--REFRESH MATERIALIZED VIEW mb_benoemd_obj_adres;
--REFRESH MATERIALIZED VIEW mb_adres;
--REFRESH MATERIALIZED VIEW mb_ben_obj_nevenadres;

--set session authorization flamingo;

--drop view vb_adres;
CREATE OR REPLACE VIEW
    vb_adres
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
    (CASE
        WHEN position('-' IN addrobj.dat_beg_geldh) = 5
        THEN addrobj.dat_beg_geldh
        ELSE 
            substring(addrobj.dat_beg_geldh,1,4) || '-' ||
          	substring(addrobj.dat_beg_geldh,5,2) || '-' || 
          	substring(addrobj.dat_beg_geldh,7,2)       
    END)::CHARACTER VARYING(10) AS begin_geldigheid,
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
            
COMMENT ON VIEW vb_adres
IS
    'commentaar view vb_adres:
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
--drop materialized view mb_adres cascade;
CREATE MATERIALIZED VIEW mb_adres AS
SELECT
    *
FROM
    vb_adres WITH NO DATA;
CREATE UNIQUE INDEX m_adres_objectid ON mb_adres USING btree (objectid);
CREATE INDEX m_adres_identif ON mb_adres USING btree (na_identif);
            
--drop view vb_vbo_adres cascade;
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
    (CASE
        WHEN position('-' IN gobj.dat_beg_geldh) = 5
        THEN gobj.dat_beg_geldh
        ELSE 
            substring(gobj.dat_beg_geldh,1,4) || '-' ||
          	substring(gobj.dat_beg_geldh,5,2) || '-' || 
          	substring(gobj.dat_beg_geldh,7,2)       
    END)::CHARACTER VARYING(10) AS begin_geldigheid,
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
    (((verblijfsobj vbo
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
    vb_adres bva
ON
    (((
                vbo.fk_11nra_sc_identif)::text = (bva.na_identif)::text)));
                
COMMENT ON VIEW vb_vbo_adres
IS
    'commentaar view vb_vbo_adres:
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
                
                
--drop view vb_standplaats_adres cascade;
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
    (CASE
        WHEN position('-' IN benter.dat_beg_geldh) = 5
        THEN benter.dat_beg_geldh
        ELSE 
            substring(benter.dat_beg_geldh,1,4) || '-' ||
          	substring(benter.dat_beg_geldh,5,2) || '-' || 
          	substring(benter.dat_beg_geldh,7,2)       
    END)::CHARACTER VARYING(10) AS begin_geldigheid,
    bva.na_identif 					      as na_identif,
    bva.gemeente,
    bva.woonplaats,
    bva.straatnaam,
    bva.huisnummer,
    bva.huisletter,
    bva.huisnummer_toev,
    bva.postcode,
    spl.status,
    st_centroid(benter.geom)::geometry(POINT,28992) AS the_geom
FROM
    ((standplaats spl
JOIN
    benoemd_terrein benter
ON
    (((
                benter.sc_identif)::text = (spl.sc_identif)::text)))
LEFT JOIN
    vb_adres bva
ON
    (((
                spl.fk_4nra_sc_identif)::text = (bva.na_identif)::text)));
COMMENT ON VIEW vb_standplaats_adres
IS
    'commentaar view vb_standplaats_adres:
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
                
--drop view vb_ligplaats_adres cascade;
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
    (CASE
        WHEN position('-' IN benter.dat_beg_geldh) = 5
        THEN benter.dat_beg_geldh
        ELSE 
            substring(benter.dat_beg_geldh,1,4) || '-' ||
          	substring(benter.dat_beg_geldh,5,2) || '-' || 
          	substring(benter.dat_beg_geldh,7,2)       
    END)::CHARACTER VARYING(10) AS begin_geldigheid,
    bva.na_identif 				              as na_identif,
    bva.gemeente,
    bva.woonplaats,
    bva.straatnaam,
    bva.huisnummer,
    bva.huisletter,
    bva.huisnummer_toev,
    bva.postcode,
    lpl.status,
    st_centroid(benter.geom)::geometry(POINT,28992) AS the_geom
FROM
    ((ligplaats lpl
JOIN
    benoemd_terrein benter
ON
    (((
                benter.sc_identif)::text = (lpl.sc_identif)::text)))
LEFT JOIN
    vb_adres bva
ON
    (((
                lpl.fk_4nra_sc_identif)::text = (bva.na_identif)::text)));
COMMENT ON VIEW vb_ligplaats_adres
IS
    'commentaar view vb_ligplaats_adres:
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
                
--drop view vb_pand cascade;
CREATE OR REPLACE VIEW
    vb_pand
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
    (CASE
        WHEN position('-' IN pand.dat_beg_geldh) = 5
        THEN pand.dat_beg_geldh
        ELSE 
            substring(pand.dat_beg_geldh,1,4) || '-' ||
          	substring(pand.dat_beg_geldh,5,2) || '-' || 
          	substring(pand.dat_beg_geldh,7,2)       
    END)::CHARACTER VARYING(10) AS begin_geldigheid,
    pand.oorspronkelijk_bouwjaar                            AS bouwjaar,
    pand.status,
    pand.geom_bovenaanzicht AS the_geom
FROM
    pand;
COMMENT ON VIEW vb_pand
IS
    'commentaar view vb_pand:
pand met datum veld voor begin geldigheid en objectid voor geoserver/arcgis
beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* pand_identif: natuurlijke id van pand      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* bouwjaar: -,
* status: -,
* the_geom: pandvlak
';
--drop materialized view mb_pand cascade;
CREATE MATERIALIZED VIEW mb_pand AS
SELECT
    *
FROM
    vb_pand WITH NO DATA;
CREATE UNIQUE INDEX m_pand_objectid ON mb_pand USING btree (objectid);
CREATE INDEX m_pand_identif ON mb_pand USING btree (pand_identif);
CREATE INDEX m_pand_the_geom_idx ON mb_pand USING gist (the_geom);

    
--drop view vb_benoemd_obj_adres cascade;
CREATE OR REPLACE VIEW
    vb_benoemd_obj_adres
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
    qry.the_geom::geometry(POINT,28992)
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
            vb_vbo_adres vvla
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
            vb_ligplaats_adres vlla
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
            vb_standplaats_adres vsla
    ) qry;
COMMENT ON VIEW vb_benoemd_obj_adres
IS
    'commentaar view vb_benoemd_obj_adres:
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
--drop materialized view mb_benoemd_obj_adres cascade;
CREATE MATERIALIZED VIEW mb_benoemd_obj_adres AS
SELECT
    *
FROM
    vb_benoemd_obj_adres WITH NO DATA;
CREATE UNIQUE INDEX m_benoemd_obj_adres_objectid ON mb_benoemd_obj_adres USING btree (objectid);
CREATE INDEX m_benoemd_obj_adres_identif ON mb_benoemd_obj_adres USING btree (na_identif);
CREATE INDEX m_benoemd_obj_adres_the_geom_idx ON mb_benoemd_obj_adres USING gist (the_geom);

--drop view vb_ben_obj_nevenadres cascade;
create or replace view
    vb_ben_obj_nevenadres
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
    ) as
select
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
from
    (
						select
						    vna.fk_nn_lh_vbo_sc_identif as benoemdobj_identif,
						    vba.na_identif,
						    (CASE
						        WHEN position('-' IN vna.fk_nn_lh_vbo_sc_dat_beg_geldh) = 5
						        THEN vna.fk_nn_lh_vbo_sc_dat_beg_geldh
						        ELSE 
						            substring(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,1,4) || '-' ||
						          	substring(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,5,2) || '-' || 
						          	substring(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,7,2)       
						    END)::CHARACTER VARYING(10) AS begin_geldigheid,
						    'VBO'::CHARACTER VARYING(50) AS soort,
						    vba.gemeente,
						    vba.woonplaats,
						    vba.straatnaam,
						    vba.huisnummer,
						    vba.huisletter,
						    vba.huisnummer_toev,
						    vba.postcode
						from
						    vb_adres vba
						join
						    verblijfsobj_nummeraand vna
						on
						    (vna.fk_nn_rh_nra_sc_identif = vba.na_identif)
						join
						    verblijfsobj vbo
						on
						    (vna.fk_nn_lh_vbo_sc_identif = vbo.sc_identif)
						where 
                vbo.fk_11nra_sc_identif <> vna.fk_nn_rh_nra_sc_identif
            union all
						select
						    lpa.fk_nn_lh_lpl_sc_identif as benoemdobj_identif,
						    vba.na_identif,
						    (CASE
						        WHEN position('-' IN lpa.fk_nn_lh_lpl_sc_dat_beg_geldh) = 5
						        THEN lpa.fk_nn_lh_lpl_sc_dat_beg_geldh
						        ELSE 
						            substring(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,1,4) || '-' ||
						          	substring(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,5,2) || '-' || 
						          	substring(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,7,2)       
						    END)::CHARACTER VARYING(10) AS begin_geldigheid,
						    'ligplaats'::CHARACTER VARYING(50) AS soort,
						    vba.gemeente,
						    vba.woonplaats,
						    vba.straatnaam,
						    vba.huisnummer,
						    vba.huisletter,
						    vba.huisnummer_toev,
						    vba.postcode
						from
						    vb_adres vba
						join
						    ligplaats_nummeraand lpa
						on
						    (lpa.fk_nn_rh_nra_sc_identif = vba.na_identif)
						join
						    ligplaats lpl
						on
						    (lpa.fk_nn_lh_lpl_sc_identif = lpl.sc_identif)
						where 
                lpl.fk_4nra_sc_identif <> lpa.fk_nn_rh_nra_sc_identif
        union all
						select
						    spa.fk_nn_lh_spl_sc_identif as benoemdobj_identif,
						    vba.na_identif,
						    (CASE
						        WHEN position('-' IN spa.fk_nn_lh_spl_sc_dat_beg_geldh) = 5
						        THEN spa.fk_nn_lh_spl_sc_dat_beg_geldh
						        ELSE 
						            substring(spa.fk_nn_lh_spl_sc_dat_beg_geldh,1,4) || '-' ||
						          	substring(spa.fk_nn_lh_spl_sc_dat_beg_geldh,5,2) || '-' || 
						          	substring(spa.fk_nn_lh_spl_sc_dat_beg_geldh,7,2)       
						    END)::CHARACTER VARYING(10) AS begin_geldigheid,
						    'standplaats'::CHARACTER VARYING(50) AS soort,
						    vba.gemeente,
						    vba.woonplaats,
						    vba.straatnaam,
						    vba.huisnummer,
						    vba.huisletter,
						    vba.huisnummer_toev,
						    vba.postcode
						from
						    vb_adres vba
						join
						    standplaats_nummeraand spa
						on
						    (spa.fk_nn_rh_nra_sc_identif = vba.na_identif)
						join
						    standplaats spl
						on
						    (spa.fk_nn_lh_spl_sc_identif = spl.sc_identif)
						where 
                spl.fk_4nra_sc_identif <> spa.fk_nn_rh_nra_sc_identif
    ) qry;


comment on view vb_ben_obj_nevenadres
is
    'commentaar view mb_ben_obj_nevenadres:
alle nevenadressen van een benoemde object (vbo, standplaats en ligplaats)
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
* postcode: nevenadres
';

--drop materialized view mb_ben_obj_nevenadres cascade;
create materialized view mb_ben_obj_nevenadres as
SELECT
    *
FROM
    vb_ben_obj_nevenadres WITH NO DATA;
CREATE INDEX m_ben_obj_nevenadres_identif ON mb_ben_obj_nevenadres USING btree (na_identif);

