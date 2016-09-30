-- View: vru.v_adres_totaal

-- DROP VIEW vru.v_adres_totaal;
-- View: vru.v_adres

-- DROP VIEW vru.v_adres;

CREATE OR REPLACE VIEW v_adres AS

 SELECT vbo.sc_identif AS fid,

gem.naam AS gemeente,
wp.naam AS woonplaats,
geor.naam_openb_rmte AS straatnaam,
addrobj.huinummer AS huisnummer,
addrobj.huisletter,
addrobj.huinummertoevoeging AS huisnummer_toev,
addrobj.postcode,
vbo.status,
gobj.oppervlakte_obj || ' m2'::text AS oppervlakte,
gobj.puntgeom AS the_geom,
vbo.sc_identif AS adresseerbaarobject,
na.sc_identif AS nummeraanduiding
FROM verblijfsobj vbo
JOIN gebouwd_obj gobj ON gobj.sc_identif::text = vbo.sc_identif::text
LEFT JOIN verblijfsobj_nummeraand vna ON vna.fk_nn_lh_vbo_sc_identif::text = vbo.sc_identif::text
LEFT JOIN nummeraand na ON na.sc_identif::text = vbo.fk_11nra_sc_identif::text
LEFT JOIN addresseerb_obj_aand addrobj ON addrobj.identif::text = na.sc_identif::text
JOIN gem_openb_rmte geor ON geor.identifcode::text = addrobj.fk_7opr_identifcode::text
LEFT JOIN openb_rmte_wnplts orwp ON geor.identifcode::text = orwp.fk_nn_lh_opr_identifcode::text
LEFT JOIN wnplts wp ON orwp.fk_nn_rh_wpl_identif::text = wp.identif::text
LEFT JOIN gemeente gem ON wp.fk_7gem_code = gem.code
WHERE na.status::text = 'Naamgeving uitgegeven'::text AND (vbo.status::text = 'Verblijfsobject in gebruik (niet ingemeten)'::text OR vbo.status::text = 'Verblijfsobject in gebruik'::text);

 

ALTER TABLE v_adres

  OWNER TO rsgb;

-- View: vru.v_adres_ligplaats

-- DROP VIEW vru.v_adres_ligplaats;

CREATE OR REPLACE VIEW vru.v_adres_ligplaats AS 
 SELECT lpa.sc_identif AS fid,
    gem.naam AS gemeente,
    wp.naam AS woonplaats,
    geor.naam_openb_rmte AS straatnaam,
    addrobj.huinummer AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    lpa.status,
    benter.geom AS the_geom,
    st_centroid(benter.geom) AS centroide,
   lpa.sc_identif AS adresseerbaarobject,
    na.sc_identif AS nummeraanduiding
   FROM vru.ligplaats lpa
     JOIN vru.benoemd_terrein benter ON benter.sc_identif::text = lpa.sc_identif::text
     LEFT JOIN vru.ligplaats_nummeraand lna ON lna.fk_nn_lh_lpl_sc_identif::text = lpa.sc_identif::text
     LEFT JOIN vru.nummeraand na ON na.sc_identif::text = lna.fk_nn_rh_nra_sc_identif::text
     LEFT JOIN vru.addresseerb_obj_aand addrobj ON addrobj.identif::text = na.sc_identif::text
     JOIN vru.gem_openb_rmte geor ON geor.identifcode::text = addrobj.fk_7opr_identifcode::text
     LEFT JOIN vru.openb_rmte_wnplts orwp ON geor.identifcode::text = orwp.fk_nn_lh_opr_identifcode::text
     LEFT JOIN vru.wnplts wp ON orwp.fk_nn_rh_wpl_identif::text = wp.identif::text
     LEFT JOIN vru.gemeente gem ON wp.fk_7gem_code = gem.code
  WHERE na.status::text = 'Naamgeving uitgegeven'::text AND lpa.status::text = 'Plaats aangewezen'::text;


--



-- View: vru.v_adres_standplaats

-- DROP VIEW vru.v_adres_standplaats;

CREATE OR REPLACE VIEW vru.v_adres_standplaats AS 
 SELECT spl.sc_identif AS fid,
    gem.naam AS gemeente,
    wp.naam AS woonplaats,
    geor.naam_openb_rmte AS straatnaam,
    addrobj.huinummer AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    spl.status,
    benter.geom AS the_geom,
    st_centroid(benter.geom) AS centroide,
   spl.sc_identif  as adresseerbaarobject,
    na.sc_identif as nummeraanduiding
   FROM vru.standplaats spl
     JOIN vru.benoemd_terrein benter ON benter.sc_identif::text = spl.sc_identif::text
     LEFT JOIN vru.standplaats_nummeraand sna ON sna.fk_nn_lh_spl_sc_identif::text = spl.sc_identif::text
     LEFT JOIN vru.nummeraand na ON na.sc_identif::text = sna.fk_nn_rh_nra_sc_identif::text
     LEFT JOIN vru.addresseerb_obj_aand addrobj ON addrobj.identif::text = na.sc_identif::text
     JOIN vru.gem_openb_rmte geor ON geor.identifcode::text = addrobj.fk_7opr_identifcode::text
     LEFT JOIN vru.openb_rmte_wnplts orwp ON geor.identifcode::text = orwp.fk_nn_lh_opr_identifcode::text
     LEFT JOIN vru.wnplts wp ON orwp.fk_nn_rh_wpl_identif::text = wp.identif::text
     LEFT JOIN vru.gemeente gem ON wp.fk_7gem_code = gem.code
  WHERE na.status::text = 'Naamgeving uitgegeven'::text AND spl.status::text = 'Plaats aangewezen'::text;




  ---

CREATE OR REPLACE VIEW vru.v_adres_totaal AS 
 SELECT v_adres.fid as gid,
    v_adres.straatnaam as openbareruimtenaam,
    v_adres.huisnummer,
    v_adres.huisletter,
    v_adres.huisnummer_toev as huisnummertoevoeging,
    v_adres.postcode,
    v_adres.gemeente as gemeentenaam,
    v_adres.woonplaats as woonplaatsnaam,
    v_adres.the_geom as geopunt,
    false as nevenadres,
    'VBO' as typeadresseerbaarobject,
    adresseerbaarobject,
    nummeraanduiding
   FROM vru.v_adres
UNION ALL
 SELECT v_adres_ligplaats.fid as gid,
    v_adres_ligplaats.straatnaam as openbareruimtenaam,
    v_adres_ligplaats.huisnummer,
    v_adres_ligplaats.huisletter,
    v_adres_ligplaats.huisnummer_toev as huisnummertoevoeging,
    v_adres_ligplaats.postcode,
    v_adres_ligplaats.gemeente as gemeentenaam,
    v_adres_ligplaats.woonplaats,
    v_adres_ligplaats.centroide as geopunt,
    false as nevenadres,
    'LIG' as typeadresseerbaarobject,
    adresseerbaarobject,
    nummeraanduiding
   FROM vru.v_adres_ligplaats
UNION ALL
 SELECT v_adres_standplaats.fid as gid,
    v_adres_standplaats.straatnaam as openbareruimtenaam,
    v_adres_standplaats.huisnummer,
    v_adres_standplaats.huisletter,
    v_adres_standplaats.huisnummer_toev as huisnummertoevoeging,
    v_adres_standplaats.postcode,
    v_adres_standplaats.gemeente as gemeentenaam,
    v_adres_standplaats.woonplaats,
    v_adres_standplaats.centroide as geopunt,
    false as nevenadres,
    'STA' as typeadresseerbaarobject,
    adresseerbaarobject,
    nummeraanduiding
   FROM vru.v_adres_standplaats;



CREATE OR REPLACE VIEW vru.v_pand_in_gebruik AS 
 SELECT p.identif AS identificatie,
    p.datum_einde_geldh AS eind_datum_geldig,
    p.dat_beg_geldh AS begin_datum_geldig,
    p.status,
    p.oorspronkelijk_bouwjaar AS bouwjaar,
    p.geom_bovenaanzicht AS geovlak
   FROM vru.pand p
  WHERE (p.status::text = ANY (ARRAY['Sloopvergunning verleend'::character varying, 'Pand in gebruik (niet ingemeten)'::character varying, 'Pand in gebruik'::character varying, 'Bouw gestart'::character varying]::text[])) AND p.datum_einde_geldh IS NULL;

