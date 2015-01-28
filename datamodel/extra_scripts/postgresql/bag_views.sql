/*
Views for visualizing the BAG data in RSGB format.
Based on Gouda views
*/


/*
    VERBLIJFSOBJ status
    VERBLIJFSOBJ_PAND pand id
    GEBOUWD_OBJ geometrie en oppervlak
    verblijfsobj_nummeraand koppeling
    NUMMERAAND hoofdadres
    addresseerb_obj_aand huisnummer
    gem_openb_rmte straat
    gemeente
    VERBLIJFSOBJGEBRUIKSDOEL doel

*/
CREATE VIEW
    V_VERBLIJFSOBJECT_ALLES
    (
        FID,
        PAND_ID,
        GEMEENTE_NAAM,
        openbareruimtenaam,
        STRAATNAAM,
        HUINUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        --GEBRUIKSDOEL,
        STATUS,
        OPPERVLAKTE,
        THE_GEOM
    ) AS
SELECT
    vbo.sc_IDENTIF            AS fid,
    fkpand.fk_nn_rh_pnd_identif    AS pand_id,
    gem.naam               AS gemeente_naam,
    geor.naam_openb_rmte AS openbareruimtenaam, -- straatnaam,
    geor.straatnaam as straatnaam,
    addrobj.huinummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    --doel.gebruiksdoel,
    vbo.status,
    gobj.oppervlakte_obj AS oppervlakte,
    gobj.puntgeom        AS the_geom
FROM
    VERBLIJFSOBJ vbo
JOIN
    VERBLIJFSOBJ_PAND fkpand
ON
    (
        fkpand.fk_nn_lh_vbo_sc_identif = vbo.sc_identif )
JOIN
    GEBOUWD_OBJ gobj
ON
    (
        gobj.sc_identif = vbo.sc_identif )
JOIN
    verblijfsobj_nummeraand vna
ON
    (
        vna.fk_nn_lh_vbo_sc_identif = vbo.sc_identif )
JOIN
    NUMMERAAND na
ON
    (
        na.sc_identif = vna.fk_nn_rh_nra_sc_identif )
JOIN
    addresseerb_obj_aand addrobj
ON
    (
        addrobj.identif = na.sc_identif )
JOIN
    gem_openb_rmte geor 
ON
    (
        geor.identifcode = addrobj.fk_7opr_identifcode )
JOIN
    gemeente gem
ON
    (
        geor.fk_7gem_code = gem.code )
/*JOIN
    VERBLIJFSOBJGEBRUIKSDOEL doel
ON
    (
        doel.verblijfsobj = vbo.IDENTIF
    AND doel.VBO_DATUM_BEGIN = vbo.DATUM_BEGIN_GELDH )*/
WHERE
    --vbo.DATUM_EIND_GELDH IS NULL
--vna.DATUM_EIND_GELDH IS NULL
 --na.DATUM_EIND_GELDH IS NULL
addrobj.dat_eind_geldh IS NULL
AND geor.DATUM_EINDE_GELDH IS NULL
AND gem.DATUM_EINDE_GELDH IS NULL
AND gobj.DATUM_EINDE_GELDH IS NULL;
------


-- V_VERBLIJFSOBJECT_GEVORMD
CREATE VIEW
    V_VERBLIJFSOBJECT_GEVORMD
    (
        FID,
        PAND_ID,
        GEMEENTE_NAAM,
        STRAATNAAM,
        HUINUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        --GEBRUIKSDOEL,
        STATUS,
        OPPERVLAKTE,
        THE_GEOM
    ) AS
SELECT
    FID,
    PAND_ID,
    GEMEENTE_NAAM,
    STRAATNAAm,
    HUINUMMER,
    HUISLETTER,
    HUISNUMMER_TOEV,
    POSTCODE,
    --GEBRUIKSDOEL,
    STATUS,
    OPPERVLAKTE,
    THE_GEOM
FROM
    V_VERBLIJFSOBJECT_ALLES
WHERE
    status = 'Verblijfsobject gevormd';


-- V_VERBLIJFSOBJECT

CREATE VIEW
    V_VERBLIJFSOBJECT
    (
        FID,
        PAND_ID,
        GEMEENTE_NAAM,
        STRAATNAAM,
        HUINUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        --GEBRUIKSDOEL,
        STATUS,
        OPPERVLAKTE,
        THE_GEOM
    ) AS
SELECT
     FID,
    PAND_ID,
    GEMEENTE_NAAM,
    STRAATNAAm,
    HUINUMMER,
    HUISLETTER,
    HUISNUMMER_TOEV,
    POSTCODE,
    --GEBRUIKSDOEL,
    STATUS,
    OPPERVLAKTE,
    THE_GEOM
FROM
    V_VERBLIJFSOBJECT_ALLES
WHERE
    status = 'Verblijfsobject in gebruik (niet ingemeten)'
OR  status = 'Verblijfsobject in gebruik';


--V_PAND_IN_GEBRUIK

CREATE VIEW
    V_PAND_IN_GEBRUIK
    (
        FID,
        EIND_DATUM_GELDIG,
        BEGIN_DATUM_GELDIG,
        STATUS,
        BOUWJAAR,
        THE_GEOM
    ) AS
SELECT
    p.IDENTIF           AS fid,
    p.DATUM_EINDE_GELDH AS eind_datum_geldig,
    p.DAT_BEG_GELDH AS begin_datum_geldig,
    p.STATUS,
    p.OORSPRONKELIJK_BOUWJAAR AS bouwjaar,
    p.GEOM_BOVENAANZICHT      AS the_geom
FROM
    PAND p
WHERE
    status IN ('Sloopvergunning verleend',
               'Pand in gebruik (niet ingemeten)',
               'Pand in gebruik',
               'Bouw gestart')
AND datum_einde_geldh IS NULL;

--  V_PAND_GEBRUIK_NIET_INGEMETEN
CREATE VIEW
    V_PAND_GEBRUIK_NIET_INGEMETEN
    (
        FID,
        BEGIN_DATUM_GELDIG,
        STATUS,
        BOUWJAAR,
        THE_GEOM
    ) AS
SELECT
    p.IDENTIF           AS fid,
    p.DAT_BEG_GELDH AS begin_datum_geldig,
    p.STATUS,
    p.OORSPRONKELIJK_BOUWJAAR AS bouwjaar,
    p.GEOM_BOVENAANZICHT      AS the_geom
FROM
    PAND p
WHERE
    status = 'Pand in gebruik (niet ingemeten)'
AND datum_einde_geldh IS NULL;



-- v_standplaats
CREATE VIEW
    V_STANDPLAATS
    (
        sc_identif,
        STATUS,
        fk_4nra_sc_identif,
        DATUM_BEGIN_GELDH,
        GEOMETRIE
    ) AS
SELECT
    sp.sc_identif,
    sp.STATUS,
    sp.fk_4nra_sc_identif,
    bt.DAT_BEG_GELDH,
    bt.GEOM AS geometrie
FROM
    standplaats sp
LEFT JOIN
    benoemd_terrein bt
ON
    (
        sp.sc_identif = bt.sc_identif);

-- V_LIGPLAATS
CREATE VIEW
    V_LIGPLAATS
    (
        sc_identif,
        STATUS,
        fk_4nra_sc_identif,
        DAT_BEG_GELDH,
        GEOMETRIE
    ) AS
SELECT
    lp.sc_identif,
    lp.STATUS,
    lp.fk_4nra_sc_identif,
    bt.DAT_BEG_GELDH,
    bt.GEOM AS geometrie
FROM
    ligplaats lp
LEFT JOIN
    benoemd_terrein bt
ON
    (
        lp.sc_identif = bt.sc_identif)
;



-- v_adres
/*
volledige adressenlijst
standplaats en ligplaats via benoemd_terrein, waarbij centroide van polygon wordt genomen
plus veblijfsobjec via punt object van gebouwd_obj
*/
CREATE VIEW
    V_ADRES
    (
        FID,
        GEMEENTE,
        STRAAT,
        HUISNUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        STATUS,
        OPPERVLAKTE,
        THE_GEOM
    ) AS
SELECT
    vbo.sc_identif       AS fid,
    gem.naam             AS gemeente,
    geor.NAAM_OPENB_RMTE AS straat,
    addrobj.huinummer    AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    vbo.status,
    gobj.OPPERVLAKTE_OBJ || ' m2' AS oppervlakte,
    gobj.puntgeom                 AS the_geom
FROM
    VERBLIJFSOBJ vbo
JOIN
    GEBOUWD_OBJ gobj
ON
    (
        gobj.sc_identif = vbo.sc_identif )
LEFT JOIN
    verblijfsobj_nummeraand vna
ON
    (
        vna.fk_nn_lh_vbo_sc_identif = vbo.sc_identif )
LEFT JOIN
    NUMMERAAND na
ON
    (
        na.sc_identif = vna.fk_nn_rh_nra_sc_identif )
LEFT JOIN
    addresseerb_obj_aand addrobj
ON
    (
        addrobj.identif = na.sc_identif )
LEFT JOIN
    openb_rmte opr
ON
    (
        opr.identifcode = addrobj.fk_7opr_identifcode )
LEFT JOIN
    openb_rmte_gem_openb_rmte gmopr
ON
    (
        gmopr.fk_nn_lh_opr_identifcode = opr.identifcode )
LEFT JOIN
    gem_openb_rmte geor
ON
    (
        geor.identifcode = gmopr.fk_nn_rh_gor_identifcode )
LEFT JOIN
    gemeente gem
ON
    (
        geor.fk_7gem_code = gem.code )
WHERE
    na.status = 'Naamgeving uitgegeven'
AND (
        vbo.status = 'Verblijfsobject in gebruik (niet ingemeten)'
    OR  vbo.status = 'Verblijfsobject in gebruik');
    
    
    
CREATE VIEW
    V_ADRES_LIGPLAATS
    (
        FID,
        GEMEENTE,
        STRAAT,
        HUISNUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        STATUS,
        THE_GEOM,
        CENTROIDE
    ) AS
SELECT
    lpa.sc_identif       AS fid,
    gem.naam             AS gemeente,
    geor.NAAM_OPENB_RMTE AS straat,
    addrobj.huinummer    AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    lpa.status,
    benter.GEOM AS the_geom,
    st_centroid(benter.GEOM)
FROM
    LIGPLAATS lpa
JOIN
    benoemd_terrein benter
ON
    (
        benter.sc_identif = lpa.sc_identif )
LEFT JOIN
    LIGPLAATS_NUMMERAAND lna
ON
    (
        lna.FK_NN_LH_LPL_SC_IDENTIF = lpa.sc_identif )
LEFT JOIN
    NUMMERAAND na
ON
    (
        na.sc_identif = lna.FK_NN_RH_NRA_SC_IDENTIF )
LEFT JOIN
    addresseerb_obj_aand addrobj
ON
    (
        addrobj.identif = na.sc_identif )
LEFT JOIN
    openb_rmte opr
ON
    (
        opr.identifcode = addrobj.fk_7opr_identifcode )
LEFT JOIN
    openb_rmte_gem_openb_rmte gmopr
ON
    (
        gmopr.fk_nn_lh_opr_identifcode = opr.identifcode )
LEFT JOIN
    gem_openb_rmte geor
ON
    (
        geor.identifcode = gmopr.fk_nn_rh_gor_identifcode )
LEFT JOIN
    gemeente gem
ON
    (
        geor.fk_7gem_code = gem.code )
WHERE
    na.status = 'Naamgeving uitgegeven'
AND lpa.status = 'Plaats aangewezen';



CREATE VIEW
    V_ADRES_STANDPLAATS
    (
        FID,
        GEMEENTE,
        STRAAT,
        HUISNUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        STATUS,
        THE_GEOM,
        CENTROIDE
    ) AS
SELECT
    spl.sc_identif       AS fid,
    gem.naam             AS gemeente,
    geor.NAAM_OPENB_RMTE AS straat,
    addrobj.huinummer    AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    spl.status,
    benter.GEOM AS the_geom,
    st_centroid(benter.GEOM)
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
        sna.FK_NN_LH_SPL_SC_IDENTIF = spl.sc_identif )
LEFT JOIN
    NUMMERAAND na
ON
    (
        na.sc_identif = sna.FK_NN_RH_NRA_SC_IDENTIF )
LEFT JOIN
    addresseerb_obj_aand addrobj
ON
    (
        addrobj.identif = na.sc_identif )
LEFT JOIN
    openb_rmte opr
ON
    (
        opr.identifcode = addrobj.fk_7opr_identifcode )
LEFT JOIN
    openb_rmte_gem_openb_rmte gmopr
ON
    (
        gmopr.fk_nn_lh_opr_identifcode = opr.identifcode )
LEFT JOIN
    gem_openb_rmte geor
ON
    (
        geor.identifcode = gmopr.fk_nn_rh_gor_identifcode )
LEFT JOIN
    gemeente gem
ON
    (
        geor.fk_7gem_code = gem.code )
WHERE
    na.status = 'Naamgeving uitgegeven'
AND spl.status = 'Plaats aangewezen';



CREATE VIEW
    V_ADRES_TOTAAL
    (
        FID,
        STRAAT,
        HUISNUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        GEMEENTE,
        THE_GEOM
    ) AS
    (
        SELECT
            FID ,
            STRAAT,
            HUISNUMMER,
            HUISLETTER,
            HUISNUMMER_TOEV,
            POSTCODE,
            GEMEENTE,
            the_geom
        FROM
            V_ADRES
        UNION ALL
        SELECT
            FID ,
            STRAAT,
            HUISNUMMER,
            HUISLETTER,
            HUISNUMMER_TOEV,
            POSTCODE,
            GEMEENTE,
            centroide AS the_geom
        FROM
            V_ADRES_ligplaats
        UNION ALL
        SELECT
            FID ,
            STRAAT,
            HUISNUMMER,
            HUISLETTER,
            HUISNUMMER_TOEV,
            POSTCODE,
            GEMEENTE,
            centroide AS the_geom
        FROM
            V_ADRES_standplaats
    );