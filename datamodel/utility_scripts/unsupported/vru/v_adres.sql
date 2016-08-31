DROP VIEW v_adres;
 

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
vbo.sc_identif AS adresseerbaarobject, >> was addrobj.identif AS adresseerbaarobject
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