----------------------------------------------------------------
-- fix voor issue https://github.com/B3Partners/brmo/issues/126
-- uit te voeren als RSGB schema eigenaar
----------------------------------------------------------------
ALTER VIEW
    v_adres
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
        oppervlakte,
        the_geom
    ) AS
SELECT
    vbo.sc_identif       AS fid,
    gem.naam             AS gemeente,
    wp.naam              AS woonplaats,
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
        na.sc_identif = vna.fk_nn_rh_nra_sc_identif )
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
