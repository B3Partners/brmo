/*
Views for visualizing the BAG data.
versie 2
30-8-2019
*/
CREATE MATERIALIZED VIEW mb_adres (
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
)
    BUILD DEFERRED
    REFRESH
        ON DEMAND
AS
    SELECT
        CAST(ROWNUM AS INTEGER)       AS objectid,
        na.sc_identif                 AS na_identif,
        na.status,
        CAST(CASE
            WHEN( (instr(addrobj.dat_beg_geldh,'-') = 5)
                   AND(instr(addrobj.dat_beg_geldh,'-',1,2) = 8) ) THEN addrobj.dat_beg_geldh
            ELSE substr(addrobj.dat_beg_geldh,1,4)
                 || '-'
                 || substr(addrobj.dat_beg_geldh,5,2)
                 || '-'
                 || substr(addrobj.dat_beg_geldh,7,2)
        END AS VARCHAR2(10 CHAR) )    AS begin_geldigheid,
        CASE
            WHEN ( instr(addrobj.dat_beg_geldh,'-') = 5 ) THEN TO_DATE(addrobj.dat_beg_geldh,'YYYY-MM-DD')
            ELSE TO_DATE(addrobj.dat_beg_geldh,'YYYYMMDDHH24MISSUS')
        END AS begin_geldigheid_datum,
        gem.naam                      AS gemeente,
        CASE
            WHEN ( addrobj.fk_6wpl_identif IS NOT NULL ) THEN (
                SELECT
                    wnplts.naam
                FROM
                    wnplts
                WHERE
                    ( ( wnplts.identif ) = ( addrobj.fk_6wpl_identif ) )
            )
            ELSE wp.naam
        END AS woonplaats,
        geor.naam_openb_rmte          AS straatnaam,
        addrobj.huinummer             AS huisnummer,
        addrobj.huisletter,
        addrobj.huinummertoevoeging   AS huisnummer_toev,
        addrobj.postcode,
        geor.identifcode              AS geor_identif,
        wp.identif                    AS wpl_identif,
        gem.code                      AS gem_code
    FROM
        ( ( ( ( ( nummeraand na
        LEFT JOIN addresseerb_obj_aand addrobj ON ( ( ( addrobj.identif ) = ( na.sc_identif ) ) ) )
        JOIN gem_openb_rmte geor ON ( ( ( geor.identifcode ) = ( addrobj.fk_7opr_identifcode ) ) ) ) left
        JOIN openb_rmte_wnplts orwp ON ( ( ( geor.identifcode ) = ( orwp.fk_nn_lh_opr_identifcode ) ) ) ) left
        JOIN wnplts wp ON ( ( ( orwp.fk_nn_rh_wpl_identif ) = ( wp.identif ) ) ) ) left
        JOIN gemeente gem ON ( ( wp.fk_7gem_code = gem.code ) ) );

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

CREATE OR REPLACE VIEW vb_vbo_adres (
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
        vbo.sc_identif                AS vbo_identif,
        CAST(CASE
            WHEN( (instr(gobj.dat_beg_geldh,'-') = 5)
                   AND(instr(gobj.dat_beg_geldh,'-',1,2) = 8) ) THEN gobj.dat_beg_geldh
            ELSE substr(gobj.dat_beg_geldh,1,4)
                 || '-'
                 || substr(gobj.dat_beg_geldh,5,2)
                 || '-'
                 || substr(gobj.dat_beg_geldh,7,2)
        END AS VARCHAR2(10 CHAR) ) AS begin_geldigheid,
        CASE
            WHEN ( instr(gobj.dat_beg_geldh,'-') = 5 ) THEN TO_DATE(gobj.dat_beg_geldh,'YYYY-MM-DD')
            ELSE TO_DATE(gobj.dat_beg_geldh,'YYYYMMDDHH24MISSUS')
        END AS begin_geldigheid_datum,
        fkpand.fk_nn_rh_pnd_identif   AS pand_identif,
        bva.na_identif                AS na_identif,
        bva.na_status                 AS na_status,
        bva.gemeente,
        bva.woonplaats,
        bva.straatnaam,
        bva.huisnummer,
        bva.huisletter,
        bva.huisnummer_toev,
        bva.postcode,
        vbo.status,
        (
            SELECT
                LISTAGG(gog.gebruiksdoel_gebouwd_obj,',') WITHIN GROUP(
                    ORDER BY
                        gog.gebruiksdoel_gebouwd_obj
                ) AS gebruiksdoelen
            FROM
                gebouwd_obj_gebruiksdoel gog
            WHERE
                gog.fk_gbo_sc_identif = vbo.sc_identif
        ) AS gebruiksdoelen,
        gobj.oppervlakte_obj,
        gobj.puntgeom                 AS the_geom
    FROM
        verblijfsobj vbo
        JOIN gebouwd_obj gobj ON gobj.sc_identif = vbo.sc_identif
        LEFT JOIN verblijfsobj_pand fkpand ON fkpand.fk_nn_lh_vbo_sc_identif = vbo.sc_identif
        LEFT JOIN mb_adres bva ON vbo.fk_11nra_sc_identif = bva.na_identif;

CREATE OR REPLACE VIEW vb_standplaats_adres (
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
        spl.sc_identif   AS spl_identif,
        CAST(CASE
            WHEN( (instr(benter.dat_beg_geldh,'-') = 5)
                   AND(instr(benter.dat_beg_geldh,'-',1,2) = 8) ) THEN benter.dat_beg_geldh
            ELSE substr(benter.dat_beg_geldh,1,4)
                 || '-'
                 || substr(benter.dat_beg_geldh,5,2)
                 || '-'
                 || substr(benter.dat_beg_geldh,7,2)
        END AS CHARACTER VARYING(10) ) AS begin_geldigheid,
        CASE
            WHEN instr(benter.dat_beg_geldh,'-') = 5 THEN TO_DATE(benter.dat_beg_geldh,'YYYY-MM-DD')
            ELSE TO_DATE(benter.dat_beg_geldh,'YYYYMMDDHH24MISSUS')
        END AS begin_geldigheid_datum,
        bva.na_identif   AS na_identif,
        bva.na_status    AS na_status,
        bva.gemeente,
        bva.woonplaats,
        bva.straatnaam,
        bva.huisnummer,
        bva.huisletter,
        bva.huisnummer_toev,
        bva.postcode,
        spl.status,
        sdo_geom.sdo_centroid(benter.geom,2) AS the_geom
    FROM
        ( ( standplaats spl
        JOIN benoemd_terrein benter ON ( ( ( benter.sc_identif ) = ( spl.sc_identif ) ) ) ) left
        JOIN mb_adres bva ON ( ( ( spl.fk_4nra_sc_identif ) = ( bva.na_identif ) ) ) );

CREATE OR REPLACE VIEW vb_ligplaats_adres (
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
        lpl.sc_identif   AS lpl_identif,
        CAST(CASE
            WHEN( (instr(benter.dat_beg_geldh,'-') = 5)
                   AND(instr(benter.dat_beg_geldh,'-',1,2) = 8) ) THEN benter.dat_beg_geldh
            ELSE substr(benter.dat_beg_geldh,1,4)
                 || '-'
                 || substr(benter.dat_beg_geldh,5,2)
                 || '-'
                 || substr(benter.dat_beg_geldh,7,2)
        END AS CHARACTER VARYING(10) ) AS begin_geldigheid,
        CASE
            WHEN instr(benter.dat_beg_geldh,'-') = 5 THEN TO_DATE(benter.dat_beg_geldh,'YYYY-MM-DD')
            ELSE TO_DATE(benter.dat_beg_geldh,'YYYYMMDDHH24MISSUS')
        END AS begin_geldigheid_datum,
        bva.na_identif   AS na_identif,
        bva.na_status    AS na_status,
        bva.gemeente,
        bva.woonplaats,
        bva.straatnaam,
        bva.huisnummer,
        bva.huisletter,
        bva.huisnummer_toev,
        bva.postcode,
        lpl.status,
        sdo_geom.sdo_centroid(benter.geom,2) AS the_geom
    FROM
        ( ( ligplaats lpl
        JOIN benoemd_terrein benter ON ( ( ( benter.sc_identif ) = ( lpl.sc_identif ) ) ) ) left
        JOIN mb_adres bva ON ( ( ( lpl.fk_4nra_sc_identif ) = ( bva.na_identif ) ) ) );


CREATE MATERIALIZED VIEW mb_pand (
    objectid,
    pand_identif,
    begin_geldigheid,
    begin_geldigheid_datum,
    bouwjaar,
    status,
    the_geom
)
    BUILD DEFERRED
    REFRESH
        ON DEMAND
AS
    SELECT
        CAST(ROWNUM AS INTEGER) AS objectid,
        pand.identif                   AS pand_identif,
        CAST(CASE
            WHEN( (instr(pand.dat_beg_geldh,'-') = 5)
                   AND(instr(pand.dat_beg_geldh,'-',1,2) = 8) ) THEN pand.dat_beg_geldh
            ELSE substr(pand.dat_beg_geldh,1,4)
                 || '-'
                 || substr(pand.dat_beg_geldh,5,2)
                 || '-'
                 || substr(pand.dat_beg_geldh,7,2)
        END AS CHARACTER VARYING(10) ) AS begin_geldigheid,
        CASE
            WHEN instr(pand.dat_beg_geldh,'-') = 5 THEN TO_DATE(pand.dat_beg_geldh,'YYYY-MM-DD')
            ELSE TO_DATE(pand.dat_beg_geldh,'YYYYMMDDHH24MISSUS')
        END AS begin_geldigheid_datum,
        pand.oorspronkelijk_bouwjaar   AS bouwjaar,
        pand.status,
        pand.geom_bovenaanzicht        AS the_geom
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

CREATE MATERIALIZED VIEW mb_benoemd_obj_adres (
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
)
    BUILD DEFERRED
    REFRESH
        ON DEMAND
AS
    SELECT
        CAST(ROWNUM AS INTEGER) AS objectid,
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
        qry.huisnummer,
        qry.huisletter,
        qry.huisnummer_toev,
        qry.postcode,
        qry.status,
        qry.gebruiksdoelen,
        CAST(qry.oppervlakte_obj AS INTEGER),
        qry.the_geom
    FROM
        (
            SELECT
                vvla.vbo_identif   AS benoemdobj_identif,
                vvla.na_identif,
                vvla.na_status,
                vvla.begin_geldigheid,
                vvla.begin_geldigheid_datum,
                vvla.pand_identif,
                CAST('VBO' AS CHARACTER VARYING(50) ) AS soort,
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
                vlla.lpl_identif   AS benoemdobj_identif,
                vlla.na_identif,
                vlla.na_status,
                vlla.begin_geldigheid,
                vlla.begin_geldigheid_datum,
                CAST(NULL AS CHARACTER VARYING(16) ) AS pand_identif,
                CAST('LIGPLAATS' AS CHARACTER VARYING(50) ) AS soort,
                vlla.gemeente,
                vlla.woonplaats,
                vlla.straatnaam,
                vlla.huisnummer,
                vlla.huisletter,
                vlla.huisnummer_toev,
                vlla.postcode,
                vlla.status,
                CAST(NULL AS CHARACTER VARYING(500) ) AS gebruiksdoelen,
                CAST(NULL AS INTEGER) AS oppervlakte_obj,
                vlla.the_geom
            FROM
                vb_ligplaats_adres vlla
            UNION ALL
            SELECT
                vsla.spl_identif   AS benoemdobj_identif,
                vsla.na_identif,
                vsla.na_status,
                vsla.begin_geldigheid,
                vsla.begin_geldigheid_datum,
                CAST(NULL AS CHARACTER VARYING(16) ) AS pand_identif,
                CAST('STANDPLAATS' AS CHARACTER VARYING(50) ) AS soort,
                vsla.gemeente,
                vsla.woonplaats,
                vsla.straatnaam,
                vsla.huisnummer,
                vsla.huisletter,
                vsla.huisnummer_toev,
                vsla.postcode,
                vsla.status,
                CAST(NULL AS CHARACTER VARYING(500) ) AS gebruiksdoelen,
                CAST(NULL AS INTEGER) AS oppervlakte_obj,
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

CREATE MATERIALIZED VIEW mb_ben_obj_nevenadres (
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
)
    BUILD DEFERRED
    REFRESH
        ON DEMAND
AS
    SELECT
        qry.benoemdobj_identif,
        qry.na_identif,
        qry.na_status,
        qry.begin_geldigheid,
        qry.begin_geldigheid_datum,
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
                vna.fk_nn_lh_vbo_sc_identif   AS benoemdobj_identif,
                vba.na_identif,
                vba.na_status,
                CAST(CASE
                    WHEN( (instr(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,'-') = 5)
                           AND(instr(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,'-',1,2) = 8) ) THEN vna.fk_nn_lh_vbo_sc_dat_beg_geldh
                    ELSE substr(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,1,4)
                         || '-'
                         || substr(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,5,2)
                         || '-'
                         || substr(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,7,2)
                END AS CHARACTER VARYING(10) ) AS begin_geldigheid,
                CASE
                    WHEN instr(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,'-') = 5 THEN TO_DATE(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,'YYYY-MM-DD'
                    )
                    ELSE TO_DATE(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,'YYYYMMDDHH24MISSUS')
                END AS begin_geldigheid_datum,
                CAST('VBO' AS CHARACTER VARYING(50) ) AS soort,
                vba.gemeente,
                vba.woonplaats,
                vba.straatnaam,
                vba.huisnummer,
                vba.huisletter,
                vba.huisnummer_toev,
                vba.postcode
            FROM
                mb_adres vba
                JOIN verblijfsobj_nummeraand vna ON ( vna.fk_nn_rh_nra_sc_identif = vba.na_identif )
                JOIN verblijfsobj vbo ON ( vna.fk_nn_lh_vbo_sc_identif = vbo.sc_identif )
            WHERE
                vbo.fk_11nra_sc_identif <> vna.fk_nn_rh_nra_sc_identif
            UNION ALL
            SELECT
                lpa.fk_nn_lh_lpl_sc_identif   AS benoemdobj_identif,
                vba.na_identif,
                vba.na_status,
                CAST(CASE
                    WHEN( (instr(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,'-') = 5)
                           AND(instr(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,'-',1,2) = 8) ) THEN lpa.fk_nn_lh_lpl_sc_dat_beg_geldh
                    ELSE substr(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,1,4)
                         || '-'
                         || substr(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,5,2)
                         || '-'
                         || substr(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,7,2)
                END AS CHARACTER VARYING(10) ) AS begin_geldigheid,
                CASE
                    WHEN instr(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,'-') = 5 THEN TO_DATE(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,'YYYY-MM-DD'
                    )
                    ELSE TO_DATE(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,'YYYYMMDDHH24MISSUS')
                END AS begin_geldigheid_datum,
                CAST('LIGPLAATS' AS CHARACTER VARYING(50) ) AS soort,
                vba.gemeente,
                vba.woonplaats,
                vba.straatnaam,
                vba.huisnummer,
                vba.huisletter,
                vba.huisnummer_toev,
                vba.postcode
            FROM
                mb_adres vba
                JOIN ligplaats_nummeraand lpa ON ( lpa.fk_nn_rh_nra_sc_identif = vba.na_identif )
                JOIN ligplaats lpl ON ( lpa.fk_nn_lh_lpl_sc_identif = lpl.sc_identif )
            WHERE
                lpl.fk_4nra_sc_identif <> lpa.fk_nn_rh_nra_sc_identif
            UNION ALL
            SELECT
                spa.fk_nn_lh_spl_sc_identif   AS benoemdobj_identif,
                vba.na_identif,
                vba.na_status,
                CAST(CASE
                    WHEN( (instr(spa.fk_nn_lh_spl_sc_dat_beg_geldh,'-') = 5)
                           AND(instr(spa.fk_nn_lh_spl_sc_dat_beg_geldh,'-',1,2) = 8) ) THEN spa.fk_nn_lh_spl_sc_dat_beg_geldh
                    ELSE substr(spa.fk_nn_lh_spl_sc_dat_beg_geldh,1,4)
                         || '-'
                         || substr(spa.fk_nn_lh_spl_sc_dat_beg_geldh,5,2)
                         || '-'
                         || substr(spa.fk_nn_lh_spl_sc_dat_beg_geldh,7,2)
                END AS CHARACTER VARYING(10) ) AS begin_geldigheid,
                CASE
                    WHEN instr(spa.fk_nn_lh_spl_sc_dat_beg_geldh,'-') = 5 THEN TO_DATE(spa.fk_nn_lh_spl_sc_dat_beg_geldh,'YYYY-MM-DD'
                    )
                    ELSE TO_DATE(spa.fk_nn_lh_spl_sc_dat_beg_geldh,'YYYYMMDDHH24MISSUS')
                END AS begin_geldigheid_datum,
                CAST('STANDPLAATS' AS CHARACTER VARYING(50) ) AS soort,
                vba.gemeente,
                vba.woonplaats,
                vba.straatnaam,
                vba.huisnummer,
                vba.huisletter,
                vba.huisnummer_toev,
                vba.postcode
            FROM
                mb_adres vba
                JOIN standplaats_nummeraand spa ON ( spa.fk_nn_rh_nra_sc_identif = vba.na_identif )
                JOIN standplaats spl ON ( spa.fk_nn_lh_spl_sc_identif = spl.sc_identif )
            WHERE
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
