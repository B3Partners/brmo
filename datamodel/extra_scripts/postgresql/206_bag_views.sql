/*
Views for visualizing the BAG data.
versie 2
30-8-2019
*/

--DROP VIEWS
--drop view vb_vbo_adres cascade;
--drop view vb_standplaats_adres cascade;
--drop view vb_ligplaats_adres cascade;

--drop materialized view mb_pand cascade;
--drop materialized view mb_benoemd_obj_adres cascade;
--drop materialized view mb_adres cascade;
--drop materialized view mb_ben_obj_nevenadres cascade;

--drop index mb_benoemd_obj_adres_identif cascade; 
--drop index mb_ben_obj_adr_geom_idx cascade; 
--drop index mb_ben_obj_nevenadres_identif cascade; 
--drop index mb_pand_identif cascade; 
--drop index mb_pand_the_geom_idx cascade; 
--drop index mb_adres_identif cascade; 
--drop index mb_ben_obj_adres_objectid cascade; 
--drop index mb_pand_objectid cascade; 
--drop index mb_adres_objectid cascade; 


--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'vb_pand', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'vb_benoemd_obj_adres', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'vb_adres', 'objectid', 'assigned');

--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'mb_pand', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'mb_benoemd_obj_adres', 'objectid', 'assigned');
--INSERT INTO gt_pk_metadata (table_schema, table_name, pk_column, pk_policy) VALUES ('public', 'mb_adres', 'objectid', 'assigned');

--BEGIN
-- run 1
--REFRESH MATERIALIZED VIEW mb_adres;
--REFRESH MATERIALIZED VIEW mb_pand;
-- run 2
--REFRESH MATERIALIZED VIEW mb_benoemd_obj_adres;
--REFRESH MATERIALIZED VIEW mb_ben_obj_nevenadres;
--END

--set session authorization flamingo;

CREATE MATERIALIZED VIEW mb_adres
    (
        objectid,
        na_identif,
        na_status,
        begin_geldigheid,
        begin_geldigheid_datum,
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
    na.status                                             		 AS na_status,
    (CASE
        WHEN position('-' IN addrobj.dat_beg_geldh) = 5
        THEN addrobj.dat_beg_geldh
        ELSE 
            substring(addrobj.dat_beg_geldh,1,4) || '-' ||
          	substring(addrobj.dat_beg_geldh,5,2) || '-' || 
          	substring(addrobj.dat_beg_geldh,7,2)       
    END)::CHARACTER VARYING(10) AS begin_geldigheid,
    CASE
        WHEN position('-' IN addrobj.dat_beg_geldh) = 5
        THEN to_date(addrobj.dat_beg_geldh, 'YYYY-MM-DD'::text)
        ELSE to_date(addrobj.dat_beg_geldh, 'YYYYMMDDHH24MISSUS'::text)
    END AS begin_geldigheid_datum,
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
    addrobj.huinummer::INTEGER    AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    geor.identifcode as geor_identif,
    wp.identif as wpl_identif,
    gem.code::INTEGER  as gem_code
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
            wp.fk_7gem_code = gem.code))) WITH NO DATA;
            
create unique index mb_adres_objectid on mb_adres (objectid asc);
create index mb_adres_identif on mb_adres (na_identif asc);
            
            
COMMENT ON MATERIALIZED VIEW mb_adres
IS 'commentaar view vb_adres:
volledig adres zonder locatie
beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* na_identif: natuurlijke id van nummeraanduiding,   
* na_status: status van de nummeraanduiding,   
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
            
CREATE OR REPLACE VIEW
    vb_vbo_adres
    (
        vbo_identif,
        begin_geldigheid,
        begin_geldigheid_datum,
        pand_identif,
        na_identif,
        na_status,
        gemeente,
        woonplaats,
        straatnaam,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        status,
        gebruiksdoelen,
        oppervlakte_obj,
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
    CASE
        WHEN position('-' IN gobj.dat_beg_geldh) = 5
				THEN to_date(gobj.dat_beg_geldh, 'YYYY-MM-DD'::text)
				ELSE to_date(gobj.dat_beg_geldh, 'YYYYMMDDHH24MISSUS'::text)
		END AS begin_geldigheid_datum,
    fkpand.fk_nn_rh_pnd_identif                             AS pand_identif,
    bva.na_identif 																					as na_identif,
    bva.na_status 																					as na_status,
    bva.gemeente,
    bva.woonplaats,
    bva.straatnaam,
    bva.huisnummer::INTEGER,
    bva.huisletter,
    bva.huisnummer_toev,
    bva.postcode,
    vbo.status,
    array_to_string(
    	(SELECT array_agg(gog.gebruiksdoel_gebouwd_obj) 
    	FROM gebouwd_obj_gebruiksdoel gog 
    	WHERE gog.fk_gbo_sc_identif = vbo.sc_identif), ',') as gebruiksdoelen,
    gobj.oppervlakte_obj,
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
    mb_adres bva
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
* na_status: status van de nummeraanduiding,   
* gemeente: -,
* woonplaats: -,
* straatnaam: -,
* huisnummer: -,
* huisletter: -,
* huisnummer_toev: -,
* postcode: -,
* status: -,
* gebruiksdoelen: alle gebruiksdoel gescheiden door komma,
* the_geom: puntlocatie

';
                
                
CREATE OR REPLACE VIEW
    vb_standplaats_adres
    (
        spl_identif,
        begin_geldigheid,
        begin_geldigheid_datum,
        na_identif,
        na_status,
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
    CASE
        WHEN position('-' IN benter.dat_beg_geldh) = 5
				THEN to_date(benter.dat_beg_geldh, 'YYYY-MM-DD'::text)
				ELSE to_date(benter.dat_beg_geldh, 'YYYYMMDDHH24MISSUS'::text)
		END AS begin_geldigheid_datum,
    bva.na_identif 					      as na_identif,
    bva.na_status 					      as na_status,
    bva.gemeente,
    bva.woonplaats,
    bva.straatnaam,
    bva.huisnummer::INTEGER,
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
    mb_adres bva
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
* na_status: status van de nummeraanduiding,   
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
                
CREATE OR REPLACE VIEW
    vb_ligplaats_adres
    (
        lpl_identif,
        begin_geldigheid,
        begin_geldigheid_datum,
        na_identif,
        na_status,
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
    CASE
        WHEN position('-' IN benter.dat_beg_geldh) = 5
				THEN to_date(benter.dat_beg_geldh, 'YYYY-MM-DD'::text)
				ELSE to_date(benter.dat_beg_geldh, 'YYYYMMDDHH24MISSUS'::text)
		END AS begin_geldigheid_datum,
    bva.na_identif 				              as na_identif,
    bva.na_status 				              as na_status,
    bva.gemeente,
    bva.woonplaats,
    bva.straatnaam,
    bva.huisnummer::INTEGER,
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
    mb_adres bva
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
* na_status: status van de nummeraanduiding,   
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
                
CREATE MATERIALIZED VIEW mb_pand
    (
        objectid,
        pand_identif,
        begin_geldigheid,
        begin_geldigheid_datum,
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
    CASE
        WHEN position('-' IN pand.dat_beg_geldh) = 5
				THEN to_date(pand.dat_beg_geldh, 'YYYY-MM-DD'::text)
				ELSE to_date(pand.dat_beg_geldh, 'YYYYMMDDHH24MISSUS'::text)
		END AS begin_geldigheid_datum,
	    pand.oorspronkelijk_bouwjaar::INTEGER                            AS bouwjaar,
    pand.status,
    pand.geom_bovenaanzicht AS the_geom
FROM
    pand WITH NO DATA;

CREATE UNIQUE INDEX mb_pand_objectid ON mb_pand USING btree (objectid);
CREATE INDEX mb_pand_identif ON mb_pand USING btree (pand_identif);
CREATE INDEX mb_pand_the_geom_idx ON mb_pand USING gist (the_geom);
 
    
COMMENT ON MATERIALIZED VIEW mb_pand
IS
    'commentaar view vb_pand:
pand met datum veld voor begin geldigheid en objectid voor geoserver/arcgis
beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* pand_identif: natuurlijke id van pand      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* begin_geldigheid_datum: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* bouwjaar: -,
* status: -,
* the_geom: pandvlak
';

    
CREATE MATERIALIZED VIEW mb_benoemd_obj_adres
    (
        objectid,
        benoemdobj_identif,
        na_identif,
        na_status,
        begin_geldigheid,
        begin_geldigheid_datum,
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
        gebruiksdoelen,
        oppervlakte_obj,
        the_geom
    ) AS
SELECT
    (row_number() OVER ())::INTEGER AS objectid,
    qry.benoemdobj_identif,
    qry.na_identif,
    qry.na_status,
    qry.begin_geldigheid,
    qry.begin_geldigheid_datum,
    qry.pand_identif,
    qry.soort,
    qry.gemeente,
    qry.woonplaats,
    qry.straatnaam,
    qry.huisnummer::INTEGER,
    qry.huisletter,
    qry.huisnummer_toev,
    qry.postcode,
    qry.status,
    qry.gebruiksdoelen,
    qry.oppervlakte_obj::INTEGER,
    qry.the_geom::geometry(POINT,28992)
FROM
    (
        SELECT
            vvla.vbo_identif as benoemdobj_identif,
            vvla.na_identif,
            vvla.na_status,
            vvla.begin_geldigheid,
        		vvla.begin_geldigheid_datum,
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
            vvla.gebruiksdoelen,
            vvla.oppervlakte_obj,
            vvla.the_geom
        FROM
            vb_vbo_adres vvla
        UNION ALL
        SELECT
            vlla.lpl_identif as benoemdobj_identif,
            vlla.na_identif,
            vlla.na_status,
            vlla.begin_geldigheid,
        		vlla.begin_geldigheid_datum,
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
            NULL::CHARACTER VARYING(500)        AS gebruiksdoelen,
            NULL::INTEGER        					AS oppervlakte_obj,
            vlla.the_geom
        FROM
            vb_ligplaats_adres vlla
        UNION ALL
        SELECT
            vsla.spl_identif as benoemdobj_identif,
            vsla.na_identif,
            vsla.na_status,
            vsla.begin_geldigheid,
        		vsla.begin_geldigheid_datum,
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
            NULL::CHARACTER VARYING(500)        AS gebruiksdoelen,
            NULL::INTEGER        					AS oppervlakte_obj,
            vsla.the_geom
        FROM
            vb_standplaats_adres vsla
    ) qry WITH NO DATA;

CREATE UNIQUE INDEX mb_benoemd_obj_adres_objectid ON mb_benoemd_obj_adres USING btree (objectid);
CREATE INDEX mb_benoemd_obj_adres_identif ON mb_benoemd_obj_adres USING btree (na_identif);
CREATE INDEX mb_ben_obj_adr_geom_idx ON mb_benoemd_obj_adres USING gist (the_geom);

COMMENT ON MATERIALIZED VIEW mb_benoemd_obj_adres
IS
    'commentaar view vb_benoemd_obj_adres:
alle benoemde objecten (vbo, standplaats en ligplaats) met adres, puntlocatie, objectid voor geoserver/arcgis en bij vbo referentie naar pand
beschikbare kolommen:
* benoemdobj_identif: natuurlijke id van benoemd object      
* na_identif: natuurlijke id van nummeraanduiding      
* na_status: status van de nummeraanduiding,   
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* begin_geldigheid_datum: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* pand_identif: natuurlijk id van pand dat aan dit object gekoppeld is (alleen vbo),
* gemeente: -,
* woonplaats: -,
* straatnaam: -,
* huisnummer: -,
* huisletter: -,
* huisnummer_toev: -,
* postcode: -,
* status: -,
* gebruiksdoelen: alle gebruiksdoel gescheiden door komma,
* oppervlakte_obj: oppervlak van het gebouwd object
* the_geom: puntlocatie
';

CREATE MATERIALIZED VIEW mb_ben_obj_nevenadres
    (
        benoemdobj_identif,
        na_identif,
        na_status,
        begin_geldigheid,
        begin_geldigheid_datum,
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
    qry.na_status,
    qry.begin_geldigheid,
    qry.begin_geldigheid_datum,
    qry.soort,
    qry.gemeente,
    qry.woonplaats,
    qry.straatnaam,
    qry.huisnummer::INTEGER,
    qry.huisletter,
    qry.huisnummer_toev,
    qry.postcode
from
    (
						select
						    vna.fk_nn_lh_vbo_sc_identif as benoemdobj_identif,
						    vba.na_identif,
						    vba.na_status,
						    (CASE
						        WHEN position('-' IN vna.fk_nn_lh_vbo_sc_dat_beg_geldh) = 5
						        THEN vna.fk_nn_lh_vbo_sc_dat_beg_geldh
						        ELSE 
						            substring(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,1,4) || '-' ||
						          	substring(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,5,2) || '-' || 
						          	substring(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,7,2)       
						    END)::CHARACTER VARYING(10) AS begin_geldigheid,
						    CASE
						        WHEN position('-' IN vna.fk_nn_lh_vbo_sc_dat_beg_geldh) = 5
        						THEN to_date(vna.fk_nn_lh_vbo_sc_dat_beg_geldh, 'YYYY-MM-DD'::text)
        						ELSE to_date(vna.fk_nn_lh_vbo_sc_dat_beg_geldh, 'YYYYMMDDHH24MISSUS'::text)
    						END AS begin_geldigheid_datum,
						    'VBO'::CHARACTER VARYING(50) AS soort,
						    vba.gemeente,
						    vba.woonplaats,
						    vba.straatnaam,
						    vba.huisnummer,
						    vba.huisletter,
						    vba.huisnummer_toev,
						    vba.postcode
						from
						    mb_adres vba
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
						    vba.na_status,
						    (CASE
						        WHEN position('-' IN lpa.fk_nn_lh_lpl_sc_dat_beg_geldh) = 5
						        THEN lpa.fk_nn_lh_lpl_sc_dat_beg_geldh
						        ELSE 
						            substring(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,1,4) || '-' ||
						          	substring(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,5,2) || '-' || 
						          	substring(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,7,2)       
						    END)::CHARACTER VARYING(10) AS begin_geldigheid,
						    CASE
						        WHEN position('-' IN lpa.fk_nn_lh_lpl_sc_dat_beg_geldh) = 5
        						THEN to_date(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh, 'YYYY-MM-DD'::text)
        						ELSE to_date(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh, 'YYYYMMDDHH24MISSUS'::text)
    						END AS begin_geldigheid_datum,
						    'ligplaats'::CHARACTER VARYING(50) AS soort,
						    vba.gemeente,
						    vba.woonplaats,
						    vba.straatnaam,
						    vba.huisnummer,
						    vba.huisletter,
						    vba.huisnummer_toev,
						    vba.postcode
						from
						    mb_adres vba
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
						    vba.na_status,
						    (CASE
						        WHEN position('-' IN spa.fk_nn_lh_spl_sc_dat_beg_geldh) = 5
						        THEN spa.fk_nn_lh_spl_sc_dat_beg_geldh
						        ELSE 
						            substring(spa.fk_nn_lh_spl_sc_dat_beg_geldh,1,4) || '-' ||
						          	substring(spa.fk_nn_lh_spl_sc_dat_beg_geldh,5,2) || '-' || 
						          	substring(spa.fk_nn_lh_spl_sc_dat_beg_geldh,7,2)       
						    END)::CHARACTER VARYING(10) AS begin_geldigheid,
						    CASE
						        WHEN position('-' IN spa.fk_nn_lh_spl_sc_dat_beg_geldh) = 5
        						THEN to_date(spa.fk_nn_lh_spl_sc_dat_beg_geldh, 'YYYY-MM-DD'::text)
        						ELSE to_date(spa.fk_nn_lh_spl_sc_dat_beg_geldh, 'YYYYMMDDHH24MISSUS'::text)
    						END AS begin_geldigheid_datum,
						    'standplaats'::CHARACTER VARYING(50) AS soort,
						    vba.gemeente,
						    vba.woonplaats,
						    vba.straatnaam,
						    vba.huisnummer,
						    vba.huisletter,
						    vba.huisnummer_toev,
						    vba.postcode
						from
						    mb_adres vba
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
    ) qry WITH NO DATA;

CREATE INDEX mb_ben_obj_nevenadres_identif ON mb_ben_obj_nevenadres USING btree (na_identif);

comment on materialized view mb_ben_obj_nevenadres
is
    'commentaar view mb_ben_obj_nevenadres:
alle nevenadressen van een benoemde object (vbo, standplaats en ligplaats)
beschikbare kolommen:
* benoemdobj_identif: natuurlijke id van benoemd object      
* na_identif: natuurlijke id van nummeraanduiding      
* na_status: status van de nummeraanduiding,   
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* begin_geldigheid_datum: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* soort: vbo, ligplaats of standplaats
* gemeente: nevenadres,
* woonplaats: nevenadres,
* straatnaam: nevenadres,
* huisnummer: nevenadres,
* huisletter: nevenadres,
* huisnummer_toev: nevenadres,
* postcode: nevenadres
';


