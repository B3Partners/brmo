/*
Views for visualizing the BAG data.
*/
-- DROP VIEWS
-- DROP VIEW v_adres_totaal;
-- DROP VIEW v_adres_standplaats;
-- DROP VIEW v_adres_ligplaats;
-- DROP VIEW v_adres;
-- DROP VIEW v_ligplaats;
-- DROP VIEW v_standplaats;
-- DROP VIEW v_ligplaats_alles;
-- DROP VIEW v_standplaats_alles;
-- DROP VIEW v_pand_gebruik_niet_ingemeten;
-- DROP VIEW v_pand_in_gebruik;
-- DROP VIEW v_verblijfsobject;
-- DROP VIEW v_verblijfsobject_gevormd;
-- DROP VIEW v_verblijfsobject_alles;

-------------------------------------------------
-- v_verblijfsobject_alles
-------------------------------------------------
GO

CREATE VIEW
    v_verblijfsobject_alles
    (
        objectid,
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
        the_geom
    ) AS
SELECT
    CAST(ROW_NUMBER() over(ORDER BY vbo.sc_identif) AS INT) AS ObjectID,
    vbo.sc_identif              AS fid,
    fkpand.fk_nn_rh_pnd_identif AS pand_id,
    gem.naam                    AS gemeente,
    CASE
         WHEN addrobj.fk_6wpl_identif IS NOT NULL
         -- opzoeken want in andere woonplaats
         THEN  (select naam from wnplts where identif = fk_6wpl_identif)
         ELSE wp.naam           
    END                         AS woonplaats,
    geor.naam_openb_rmte        AS straatnaam,
    addrobj.huinummer           AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    vbo.status,
    gobj.oppervlakte_obj AS oppervlakte,
    gobj.puntgeom        AS the_geom
FROM
    ((((((((verblijfsobj vbo
JOIN
    verblijfsobj_pand fkpand
ON
    ((fkpand.fk_nn_lh_vbo_sc_identif = vbo.sc_identif)))
JOIN
    gebouwd_obj gobj
ON
    ((gobj.sc_identif = vbo.sc_identif)))
JOIN
    nummeraand na
ON
    ((na.sc_identif = vbo.fk_11nra_sc_identif)))
JOIN
    addresseerb_obj_aand addrobj
ON
    ((addrobj.identif = na.sc_identif)))
JOIN
    gem_openb_rmte geor
ON
    ((geor.identifcode = addrobj.fk_7opr_identifcode)))
LEFT JOIN
    openb_rmte_wnplts orwp
ON
    ((geor.identifcode = orwp.fk_nn_lh_opr_identifcode)))
LEFT JOIN
    wnplts wp
ON
    ((orwp.fk_nn_rh_wpl_identif = wp.identif)))
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
-------------------------------------------------
-- v_verblijfsobject_gevormd
-------------------------------------------------
GO

CREATE VIEW
    v_verblijfsobject_gevormd
    (
        objectid,
        fid,
        pand_id,
        gemeente,
        woonplaats,
        straatnaam,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        --gebruiksdoel,
        status,
        oppervlakte,
        the_geom
    ) AS
SELECT
    objectid,
    fid,
    pand_id,
    gemeente,
    woonplaats,
    straatnaam,
    huisnummer,
    huisletter,
    huisnummer_toev,
    postcode,
    --gebruiksdoel,
    status,
    oppervlakte,
    the_geom
FROM
    v_verblijfsobject_alles
WHERE
    status = 'Verblijfsobject gevormd';
-------------------------------------------------
-- v_verblijfsobject
-------------------------------------------------
GO

CREATE VIEW
    v_verblijfsobject
    (
        objectid,
        fid,
        pand_id,
        gemeente,
        woonplaats,
        straatnaam,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        --gebruiksdoel,
        status,
        oppervlakte,
        the_geom
    ) AS
SELECT
    objectid,
    fid,
    pand_id,
    gemeente,
    woonplaats,
    straatnaam,
    huisnummer,
    huisletter,
    huisnummer_toev,
    postcode,
    --gebruiksdoel,
    status,
    oppervlakte,
    the_geom
FROM
    v_verblijfsobject_alles
WHERE
    status = 'Verblijfsobject in gebruik (niet ingemeten)'
OR  status = 'Verblijfsobject in gebruik';
-------------------------------------------------
-- v_pand_in_gebruik
-------------------------------------------------
GO

CREATE VIEW
    v_pand_in_gebruik
    (
        objectid,
        fid,
        eind_datum_geldig,
        begin_datum_geldig,
        status,
        bouwjaar,
        the_geom
    ) AS
SELECT
    CAST(ROW_NUMBER() over(ORDER BY p.identif) AS INT) AS ObjectID,
    p.identif           AS fid,
    p.datum_einde_geldh AS eind_datum_geldig,
    p.dat_beg_geldh     AS begin_datum_geldig,
    p.status,
    p.oorspronkelijk_bouwjaar AS bouwjaar,
    p.geom_bovenaanzicht      AS the_geom
FROM
    pand p
WHERE
    status IN ('Sloopvergunning verleend',
               'Pand in gebruik (niet ingemeten)',
               'Pand in gebruik',
               'Bouw gestart')
AND datum_einde_geldh IS NULL;
-------------------------------------------------
-- v_pand_gebruik_niet_ingemeten
-------------------------------------------------
GO

CREATE VIEW
    v_pand_gebruik_niet_ingemeten
    (
        objectid,
        fid,
        begin_datum_geldig,
        status,
        bouwjaar,
        the_geom
    ) AS
SELECT
    CAST(ROW_NUMBER() over(ORDER BY p.identif) AS INT) AS ObjectID,
    p.identif       AS fid,
    p.dat_beg_geldh AS begin_datum_geldig,
    p.status,
    p.oorspronkelijk_bouwjaar AS bouwjaar,
    p.geom_bovenaanzicht      AS the_geom
FROM
    pand p
WHERE
    status = 'Pand in gebruik (niet ingemeten)'
AND datum_einde_geldh IS NULL;
-------------------------------------------------
-- v_standplaats
-------------------------------------------------
GO

CREATE VIEW
    v_standplaats
    (
        objectid,
        sc_identif,
        status,
        fk_4nra_sc_identif,
        datum_begin_geldh,
        geometrie
    ) AS
SELECT
    CAST(ROW_NUMBER() over(ORDER BY sp.sc_identif) AS INT) AS ObjectID,
    sp.sc_identif,
    sp.status,
    sp.fk_4nra_sc_identif,
    bt.dat_beg_geldh,
    bt.geom AS geometrie
FROM
    standplaats sp
LEFT JOIN
    benoemd_terrein bt
ON
    (
        sp.sc_identif = bt.sc_identif);
-------------------------------------------------
-- v_ligplaats
-------------------------------------------------
GO

CREATE VIEW
    v_ligplaats
    (
        objectid,
        sc_identif,
        status,
        fk_4nra_sc_identif,
        dat_beg_geldh,
        geometrie
    ) AS
SELECT
    CAST(ROW_NUMBER() over(ORDER BY lp.sc_identif) AS INT) AS ObjectID,
    lp.sc_identif,
    lp.status,
    lp.fk_4nra_sc_identif,
    bt.dat_beg_geldh,
    bt.geom AS geometrie
FROM
    ligplaats lp
LEFT JOIN
    benoemd_terrein bt
ON
    (
        lp.sc_identif = bt.sc_identif) ;
-------------------------------------------------
-- v_ligplaats_alles
-------------------------------------------------
/*
ligplaats met hoofdadres
*/
GO

CREATE VIEW
    v_ligplaats_alles
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
        the_geom
    ) AS
SELECT
    CAST(ROW_NUMBER() over(ORDER BY lp.sc_identif) AS INT) AS ObjectID,
    lp.sc_identif        AS fid,
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
    lp.status,
    bt.geom AS the_geom
FROM
    (((((((ligplaats lp
JOIN
    benoemd_terrein bt
ON
    ((lp.sc_identif = bt.sc_identif)))
JOIN
    nummeraand na
ON
    ((na.sc_identif = lp.fk_4nra_sc_identif)))
JOIN
    addresseerb_obj_aand addrobj
ON
    ((addrobj.identif = na.sc_identif)))
JOIN
    gem_openb_rmte geor
ON
    ((geor.identifcode = addrobj.fk_7opr_identifcode)))
LEFT JOIN
    openb_rmte_wnplts orwp
ON
    ((geor.identifcode = orwp.fk_nn_lh_opr_identifcode)))
LEFT JOIN
    wnplts wp
ON
    ((orwp.fk_nn_rh_wpl_identif = wp.identif)))
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
-------------------------------------------------
-- v_standplaats_alles
-------------------------------------------------
/*
standplaats met hoofdadres
*/
GO

CREATE VIEW
    v_standplaats_alles
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
        the_geom
    ) AS
SELECT
    CAST(ROW_NUMBER() over(ORDER BY sp.sc_identif) AS INT) AS ObjectID,
    sp.sc_identif        AS fid,
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
    sp.status,
    bt.geom AS the_geom
FROM
    (((((((standplaats sp
JOIN
    benoemd_terrein bt
ON
    ((sp.sc_identif = bt.sc_identif)))
JOIN
    nummeraand na
ON
    ((na.sc_identif = sp.fk_4nra_sc_identif)))
JOIN
    addresseerb_obj_aand addrobj
ON
    ((addrobj.identif = na.sc_identif)))
JOIN
    gem_openb_rmte geor
ON
    ((geor.identifcode = addrobj.fk_7opr_identifcode)))
LEFT JOIN
    openb_rmte_wnplts orwp
ON
    ((geor.identifcode = orwp.fk_nn_lh_opr_identifcode)))
LEFT JOIN
    wnplts wp
ON
    ((orwp.fk_nn_rh_wpl_identif = wp.identif)))
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
-------------------------------------------------
-- v_adres
-------------------------------------------------
/*
volledige adressenlijst
standplaats en ligplaats via benoemd_terrein, 
waarbij centroide van polygon wordt genomen
plus verblijfsobject via punt object van gebouwd_obj
*/
GO

CREATE VIEW
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
        the_geom
    ) AS
SELECT
    CAST(ROW_NUMBER() over(ORDER BY vbo.sc_identif) AS INT) AS ObjectID,
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
    gobj.oppervlakte_obj AS oppervlakte_m2,
    gobj.puntgeom        AS the_geom
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
GO

CREATE VIEW
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
        centroide
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
    benter.geom.STCentroid()
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
GO

CREATE VIEW
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
        centroide
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
    benter.geom.STCentroid()
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
GO

CREATE VIEW
    v_adres_totaal
    (
        objectid,
        fid,
        straatnaam,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        gemeente,
        woonplaats,
        the_geom
    ) AS
  SELECT
    CAST(ROW_NUMBER() over(ORDER BY qry.fid) AS INT) AS ObjectID,
    qry.*
    FROM
    (
        SELECT
            fid ,
            straatnaam,
            huisnummer,
            huisletter,
            huisnummer_toev,
            postcode,
            gemeente,
            woonplaats,
            the_geom
        FROM
            v_adres
        UNION ALL
        SELECT
            fid ,
            straatnaam,
            huisnummer,
            huisletter,
            huisnummer_toev,
            postcode,
            gemeente,
            woonplaats,
            centroide AS the_geom
        FROM
            v_adres_ligplaats
        UNION ALL
        SELECT
            fid ,
            straatnaam,
            huisnummer,
            huisletter,
            huisnummer_toev,
            postcode,
            gemeente,
            woonplaats,
            centroide AS the_geom
        FROM
            v_adres_standplaats
    ) qry;

GO

-------------------------------------------------
-- v_adres_pandvlak: adressen met (maaiveld) pandvlak
-------------------------------------------------
CREATE VIEW
    v_adres_pandvlak
    (
        objectid,
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
        the_geom
    ) AS
SELECT
    CAST(ROW_NUMBER() over(ORDER BY vbo.sc_identif) AS INT) AS ObjectID,
    vbo.sc_identif       AS fid,
    fkpand.fk_nn_rh_pnd_identif AS pand_id,
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
    pand.geom_bovenaanzicht AS the_geom
FROM (
    verblijfsobj vbo
JOIN
    verblijfsobj_pand fkpand
ON
    (fkpand.fk_nn_lh_vbo_sc_identif = vbo.sc_identif)
JOIN 
    pand 
ON 
    (fkpand.fk_nn_rh_pnd_identif = pand.identif) 
)    
LEFT JOIN
    verblijfsobj_nummeraand vna
ON
    (vna.fk_nn_lh_vbo_sc_identif = vbo.sc_identif)

LEFT JOIN
    nummeraand na
ON
    (na.sc_identif = vbo.fk_11nra_sc_identif)

LEFT JOIN
    addresseerb_obj_aand addrobj
ON
    (addrobj.identif = na.sc_identif)
JOIN
    gem_openb_rmte geor
ON
    ( geor.identifcode = addrobj.fk_7opr_identifcode )
    
LEFT JOIN
    openb_rmte_wnplts orwp
ON
    ( geor.identifcode = orwp.fk_nn_lh_opr_identifcode)

LEFT JOIN
    wnplts wp
ON
    ( orwp.fk_nn_rh_wpl_identif = wp.identif)

LEFT JOIN
    gemeente gem
ON
    ( wp.fk_7gem_code = gem.code )
WHERE
    na.status = 'Naamgeving uitgegeven'
AND ( vbo.status = 'Verblijfsobject in gebruik (niet ingemeten)'
    OR  vbo.status = 'Verblijfsobject in gebruik');

GO

-------------------------------------------------
-- v_adres_totaal_vlak: adressen met maaiveld vlak van pand 
--   of openbare ruimte in geval stand of ligplaats
-------------------------------------------------
CREATE VIEW v_adres_totaal_vlak
    (
        objectid,
        fid,
        straatnaam,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        gemeente,
        woonplaats,
        the_geom
    ) AS
SELECT 
    CAST(ROW_NUMBER() over(ORDER BY qry.fid) AS INT) AS ObjectID,
    qry.*
    FROM (
        SELECT
            fid,
            straatnaam,
            huisnummer,
            huisletter,
            huisnummer_toev,
            postcode,
            gemeente,
            woonplaats,
            the_geom
        FROM
            v_adres_pandvlak
        UNION ALL
        SELECT
            fid ,
            straatnaam,
            huisnummer,
            huisletter,
            huisnummer_toev,
            postcode,
            gemeente,
            woonplaats,
            the_geom
        FROM
            v_adres_ligplaats
        UNION ALL
        SELECT
            fid ,
            straatnaam,
            huisnummer,
            huisletter,
            huisnummer_toev,
            postcode,
            gemeente,
            woonplaats,
            the_geom
        FROM
            v_adres_standplaats
    ) qry;

GO
