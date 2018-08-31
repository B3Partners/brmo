/*
Views for visualizing the BAG data.
versie 2
30-8-2018
*/

-- DROP VIEW vb_ben_obj_nevenadres;
-- DROP VIEW vb_benoemd_obj_adres;
-- DROP VIEW vb_pand;
-- DROP VIEW vb_ligplaats_adres;
-- DROP VIEW vb_standplaats_adres;
-- DROP VIEW vb_vbo_adres;
-- DROP VIEW vb_adres;

GO

CREATE VIEW
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
    CAST(row_number() OVER (ORDER BY na.sc_identif)AS INT) AS ObjectID,
    na.sc_identif                                          AS na_identif,
    CASE
        WHEN CHARINDEX('-',addrobj.dat_beg_geldh) = 5
        THEN addrobj.dat_beg_geldh
        ELSE substring(addrobj.dat_beg_geldh,1,4) + '-'
            + substring(addrobj.dat_beg_geldh,5,2) + '-'
            + substring(addrobj.dat_beg_geldh,7,2)
    END                                                    AS begin_geldigheid,
    gem.naam                                               AS gemeente,
    CASE
        WHEN (addrobj.fk_6wpl_identif IS NOT NULL)
        THEN
            (
                SELECT
                    wnplts.naam
                FROM
                    wnplts
                WHERE
                    (wnplts.identif = (addrobj.fk_6wpl_identif)))
        ELSE wp.naam
    END                  AS woonplaats,
    geor.naam_openb_rmte AS straatnaam,
    addrobj.huinummer    AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    geor.identifcode AS geor_identif,
    wp.identif       AS wpl_identif,
    gem.code         AS gem_code
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
GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'volledig adres zonder locatie

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
* gem_code: gemeentecode',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_adres';
GO


CREATE VIEW
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
    vbo.sc_identif              AS vbo_identif,
    CASE
        WHEN CHARINDEX('-',gobj.dat_beg_geldh) = 5
        THEN gobj.dat_beg_geldh
        ELSE substring(gobj.dat_beg_geldh,1,4) + '-'
            + substring(gobj.dat_beg_geldh,5,2) + '-'
            + substring(gobj.dat_beg_geldh,7,2)
    END                         AS begin_geldigheid,
    fkpand.fk_nn_rh_pnd_identif AS pand_identif,
    bva.na_identif              AS na_identif,
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
                vna.fk_nn_lh_vbo_sc_identif)= (vbo.sc_identif))))
LEFT JOIN
    vb_adres bva
ON
    (((
                vna.fk_nn_rh_nra_sc_identif) = (bva.na_identif))));
GO 

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'vbo met adres, puntlocatie en referentie naar pand

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
* the_geom: puntlocatie',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_vbo_adres';

GO


CREATE VIEW
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
    spl.sc_identif       AS spl_identif,
    CASE
        WHEN CHARINDEX('-',benter.dat_beg_geldh) = 5
        THEN benter.dat_beg_geldh
        ELSE substring(benter.dat_beg_geldh,1,4) + '-'
            + substring(benter.dat_beg_geldh,5,2) + '-'
            + substring(benter.dat_beg_geldh,7,2)
    END                  AS begin_geldigheid,
    bva.na_identif       AS na_identif,
    bva.gemeente,
    bva.woonplaats,
    bva.straatnaam,
    bva.huisnummer,
    bva.huisletter,
    bva.huisnummer_toev,
    bva.postcode,
    spl.status,
    (benter.geom.STCentroid()) AS the_geom
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
    vb_adres bva
ON
    (((
                sna.fk_nn_rh_nra_sc_identif) = (bva.na_identif))));
GO 

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'standplaats met adres en puntlocatie

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
* the_geom: puntlocatie',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_standplaats_adres';

GO

CREATE VIEW
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
    lpa.sc_identif         AS lpl_identif,
    CASE
        WHEN CHARINDEX('-',benter.dat_beg_geldh) = 5
        THEN benter.dat_beg_geldh
        ELSE substring(benter.dat_beg_geldh,1,4) + '-'
            + substring(benter.dat_beg_geldh,5,2) + '-'
            + substring(benter.dat_beg_geldh,7,2)
    END                    AS begin_geldigheid,
    bva.na_identif         AS na_identif,
    bva.gemeente,
    bva.woonplaats,
    bva.straatnaam,
    bva.huisnummer,
    bva.huisletter,
    bva.huisnummer_toev,
    bva.postcode,
    lpa.status,
    (benter.geom.STCentroid()) AS the_geom
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
    vb_adres bva
ON
    (((
                lna.fk_nn_rh_nra_sc_identif) = (bva.na_identif))));

GO 

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'ligplaats met adres en puntlocatie

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
* the_geom: puntlocatie',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_ligplaats_adres';

GO

CREATE VIEW
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
    CAST(row_number() OVER (ORDER BY pand.identif)AS INT) AS ObjectID,
    pand.identif                    AS pand_identif,
    CASE
        WHEN CHARINDEX('-',pand.dat_beg_geldh) = 5
        THEN pand.dat_beg_geldh
        ELSE substring(pand.dat_beg_geldh,1,4) + '-'
            + substring(pand.dat_beg_geldh,5,2) + '-'
            + substring(pand.dat_beg_geldh,7,2)
    END                             AS begin_geldigheid,
    pand.oorspronkelijk_bouwjaar    AS bouwjaar,
    pand.status,
    pand.geom_bovenaanzicht AS the_geom
FROM
    pand;

GO 

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'pand met datum veld voor begin geldigheid en objectid voor geoserver/arcgis
beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* pand_identif: natuurlijke id van pand      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* bouwjaar: -,
* status: -,
* the_geom: pandvlak',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_pand';

GO

CREATE VIEW
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
    CAST(row_number() OVER (ORDER BY qry.benoemdobj_identif)AS INT) AS ObjectID,
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
            vvla.vbo_identif AS benoemdobj_identif,
            vvla.na_identif,
            vvla.begin_geldigheid,
            vvla.pand_identif,
            'VBO' AS soort,
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
            vlla.lpl_identif AS benoemdobj_identif,
            vlla.na_identif,
            vlla.begin_geldigheid,
            NULL        AS pand_identif,
            'LIGPLAATS' AS soort,
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
            vsla.spl_identif AS benoemdobj_identif,
            vsla.na_identif,
            vsla.begin_geldigheid,
            NULL          AS pand_identif,
            'STANDPLAATS' AS soort,
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
            vb_standplaats_adres vsla ) qry;


GO 

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'alle benoemde objecten (vbo, standplaats en ligplaats) met adres, puntlocatie, objectid voor geoserver/arcgis en bij vbo referentie naar pand
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
* the_geom: puntlocatie',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_benoemd_obj_adres';

GO

CREATE VIEW
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
    ) AS
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
            vna.fk_nn_lh_vbo_sc_identif AS benoemdobj_identif,
            vba.na_identif,
            (
                CASE
                    WHEN CHARINDEX('-',vna.fk_nn_lh_vbo_sc_dat_beg_geldh) = 5
                    THEN vna.fk_nn_lh_vbo_sc_dat_beg_geldh
                    ELSE substring(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,1,4) + '-'
                         + substring(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,5,2) + '-'
                         + substring(vna.fk_nn_lh_vbo_sc_dat_beg_geldh,7,2)
                END) AS begin_geldigheid,
            'VBO'    AS soort,
            vba.gemeente,
            vba.woonplaats,
            vba.straatnaam,
            vba.huisnummer,
            vba.huisletter,
            vba.huisnummer_toev,
            vba.postcode
        FROM
            vb_adres vba
        JOIN
            verblijfsobj_nummeraand vna
        ON
            vna.fk_nn_rh_nra_sc_identif = vba.na_identif
        JOIN
            verblijfsobj vbo
        ON
            vna.fk_nn_lh_vbo_sc_identif = vbo.sc_identif
        WHERE
            vbo.fk_11nra_sc_identif <> vna.fk_nn_rh_nra_sc_identif
        UNION ALL
        SELECT
            lpa.fk_nn_lh_lpl_sc_identif AS benoemdobj_identif,
            vba.na_identif,
            (
                CASE
                    WHEN CHARINDEX('-',lpa.fk_nn_lh_lpl_sc_dat_beg_geldh) = 5
                    THEN lpa.fk_nn_lh_lpl_sc_dat_beg_geldh
                    ELSE substring(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,1,4) + '-'
                         + substring(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,5,2) + '-'
                         + substring(lpa.fk_nn_lh_lpl_sc_dat_beg_geldh,7,2)
                END)    AS begin_geldigheid,
            'ligplaats' AS soort,
            vba.gemeente,
            vba.woonplaats,
            vba.straatnaam,
            vba.huisnummer,
            vba.huisletter,
            vba.huisnummer_toev,
            vba.postcode
        FROM
            vb_adres vba
        JOIN
            ligplaats_nummeraand lpa
        ON
            lpa.fk_nn_rh_nra_sc_identif = vba.na_identif
        JOIN
            ligplaats lpl
        ON
            lpa.fk_nn_lh_lpl_sc_identif = lpl.sc_identif
        WHERE
            lpl.fk_4nra_sc_identif <> lpa.fk_nn_rh_nra_sc_identif
        UNION ALL
        SELECT
            spa.fk_nn_lh_spl_sc_identif AS benoemdobj_identif,
            vba.na_identif,
            (
                CASE
                    WHEN CHARINDEX('-',spa.fk_nn_lh_spl_sc_dat_beg_geldh) = 5
                    THEN spa.fk_nn_lh_spl_sc_dat_beg_geldh
                    ELSE substring(spa.fk_nn_lh_spl_sc_dat_beg_geldh,1,4) + '-'
                        + substring(spa.fk_nn_lh_spl_sc_dat_beg_geldh,5,2) + '-'
                        + substring(spa.fk_nn_lh_spl_sc_dat_beg_geldh,7,2)
                END)      AS begin_geldigheid,
            'standplaats' AS soort,
            vba.gemeente,
            vba.woonplaats,
            vba.straatnaam,
            vba.huisnummer,
            vba.huisletter,
            vba.huisnummer_toev,
            vba.postcode
        FROM
            vb_adres vba
        JOIN
            standplaats_nummeraand spa
        ON
            spa.fk_nn_rh_nra_sc_identif = vba.na_identif
        JOIN
            standplaats spl
        ON
            spa.fk_nn_lh_spl_sc_identif = spl.sc_identif
        WHERE
            spl.fk_4nra_sc_identif <> spa.fk_nn_rh_nra_sc_identif ) qry;

GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'alle nevenadressen van een benoemde object (vbo, standplaats en ligplaats)
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
* postcode: nevenadres',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_ben_obj_nevenadres';

GO
