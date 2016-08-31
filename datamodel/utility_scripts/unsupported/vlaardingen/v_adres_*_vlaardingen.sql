

 DROP VIEW v_adres_totaal;
  DROP VIEW v_adres_standplaats;
 DROP VIEW v_adres_ligplaats;
 DROP VIEW v_adres;

CREATE OR REPLACE VIEW v_adres AS 
 SELECT vbo.sc_identif AS fid,
    gem.naam AS gemeente,
    geor.naam_openb_rmte AS straat,
    addrobj.huinummer AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    string_agg(gog.gebruiksdoel_gebouwd_obj::text, ', '::text) AS gebruiksdoel,
    vbo.status,
    gobj.oppervlakte_obj || ' m2'::text AS oppervlakte,
    gobj.puntgeom AS the_geom
   FROM verblijfsobj vbo
     JOIN gebouwd_obj gobj ON gobj.sc_identif::text = vbo.sc_identif::text
     LEFT JOIN verblijfsobj_nummeraand vna ON vna.fk_nn_lh_vbo_sc_identif::text = vbo.sc_identif::text
     LEFT JOIN nummeraand na ON na.sc_identif::text = vna.fk_nn_rh_nra_sc_identif::text
     LEFT JOIN addresseerb_obj_aand addrobj ON addrobj.identif::text = na.sc_identif::text
     LEFT JOIN openb_rmte opr ON opr.identifcode::text = addrobj.fk_7opr_identifcode::text
     LEFT JOIN openb_rmte_gem_openb_rmte gmopr ON gmopr.fk_nn_lh_opr_identifcode::text = opr.identifcode::text
     LEFT JOIN gem_openb_rmte geor ON geor.identifcode::text = gmopr.fk_nn_rh_gor_identifcode::text
     LEFT JOIN gemeente gem ON geor.fk_7gem_code = gem.code
     JOIN gebouwd_obj_gebruiksdoel gog ON vbo.sc_identif::text = gog.fk_gbo_sc_identif::text
  WHERE na.status::text = 'Naamgeving uitgegeven'::text AND (vbo.status::text = 'Verblijfsobject in gebruik (niet ingemeten)'::text OR vbo.status::text = 'Verblijfsobject in gebruik'::text OR vbo.status::text = 'Verblijfsobject gevormd'::text OR vbo.status::text = 'Verblijfsobject buiten gebruik'::text)
  GROUP BY vbo.sc_identif, gem.naam, geor.naam_openb_rmte, addrobj.huinummer, addrobj.huisletter, addrobj.huinummertoevoeging, addrobj.postcode, vbo.status, gobj.oppervlakte_obj, gobj.puntgeom;

ALTER TABLE v_adres
  OWNER TO vlaardingen;


CREATE OR REPLACE VIEW v_adres_ligplaats AS 
 SELECT lpa.sc_identif AS fid,
    gem.naam AS gemeente,
    geor.naam_openb_rmte AS straat,
    addrobj.huinummer AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    lpa.status,
    benter.geom AS the_geom,
    st_centroid(benter.geom) AS centroide
   FROM ligplaats lpa
     JOIN benoemd_terrein benter ON benter.sc_identif::text = lpa.sc_identif::text
     LEFT JOIN ligplaats_nummeraand lna ON lna.fk_nn_lh_lpl_sc_identif::text = lpa.sc_identif::text
     LEFT JOIN nummeraand na ON na.sc_identif::text = lna.fk_nn_rh_nra_sc_identif::text
     LEFT JOIN addresseerb_obj_aand addrobj ON addrobj.identif::text = na.sc_identif::text
     LEFT JOIN openb_rmte opr ON opr.identifcode::text = addrobj.fk_7opr_identifcode::text
     LEFT JOIN openb_rmte_gem_openb_rmte gmopr ON gmopr.fk_nn_lh_opr_identifcode::text = opr.identifcode::text
     LEFT JOIN gem_openb_rmte geor ON geor.identifcode::text = gmopr.fk_nn_rh_gor_identifcode::text
     LEFT JOIN gemeente gem ON geor.fk_7gem_code = gem.code
  WHERE na.status::text = 'Naamgeving uitgegeven'::text AND lpa.status::text = 'Plaats aangewezen'::text;

ALTER TABLE v_adres_ligplaats
  OWNER TO vlaardingen;



CREATE OR REPLACE VIEW v_adres_standplaats AS 
 SELECT spl.sc_identif AS fid,
    gem.naam AS gemeente,
    geor.naam_openb_rmte AS straat,
    addrobj.huinummer AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    spl.status,
    benter.geom AS the_geom,
    st_centroid(benter.geom) AS centroide
   FROM standplaats spl
     JOIN benoemd_terrein benter ON benter.sc_identif::text = spl.sc_identif::text
     LEFT JOIN standplaats_nummeraand sna ON sna.fk_nn_lh_spl_sc_identif::text = spl.sc_identif::text
     LEFT JOIN nummeraand na ON na.sc_identif::text = sna.fk_nn_rh_nra_sc_identif::text
     LEFT JOIN addresseerb_obj_aand addrobj ON addrobj.identif::text = na.sc_identif::text
     LEFT JOIN openb_rmte opr ON opr.identifcode::text = addrobj.fk_7opr_identifcode::text
     LEFT JOIN openb_rmte_gem_openb_rmte gmopr ON gmopr.fk_nn_lh_opr_identifcode::text = opr.identifcode::text
     LEFT JOIN gem_openb_rmte geor ON geor.identifcode::text = gmopr.fk_nn_rh_gor_identifcode::text
     LEFT JOIN gemeente gem ON geor.fk_7gem_code = gem.code
  WHERE na.status::text = 'Naamgeving uitgegeven'::text AND spl.status::text = 'Plaats aangewezen'::text;

ALTER TABLE v_adres_standplaats
  OWNER TO vlaardingen;


CREATE OR REPLACE VIEW v_adres_totaal AS 
 SELECT v_adres.fid,
    v_adres.straat,
    v_adres.huisnummer,
    v_adres.huisletter,
    v_adres.huisnummer_toev,
    v_adres.postcode,
    v_adres.gemeente,
    v_adres.gebruiksdoel,
    v_adres.the_geom
   FROM v_adres
UNION ALL
 SELECT v_adres_ligplaats.fid,
    v_adres_ligplaats.straat,
    v_adres_ligplaats.huisnummer,
    v_adres_ligplaats.huisletter,
    v_adres_ligplaats.huisnummer_toev,
    v_adres_ligplaats.postcode,
    v_adres_ligplaats.gemeente,
    NULL::text AS gebruiksdoel,
    v_adres_ligplaats.centroide AS the_geom
   FROM v_adres_ligplaats
UNION ALL
 SELECT v_adres_standplaats.fid,
    v_adres_standplaats.straat,
    v_adres_standplaats.huisnummer,
    v_adres_standplaats.huisletter,
    v_adres_standplaats.huisnummer_toev,
    v_adres_standplaats.postcode,
    v_adres_standplaats.gemeente,
    NULL::text AS gebruiksdoel,
    v_adres_standplaats.centroide AS the_geom
   FROM v_adres_standplaats;

ALTER TABLE v_adres_totaal
  OWNER TO vlaardingen;



CREATE OR REPLACE VIEW v_adres_met_buurt_en_wijk AS 
 SELECT a.fid,
    a.gemeente,
    a.straat,
    a.huisnummer,
    a.huisletter,
    a.huisnummer_toev,
    a.postcode,
    a.gebruiksdoel,
    b.naam AS buurtnaam,
    b.code AS buurtcode,
    w.naam AS wijknaam,
    w.code AS wijkcode,
    a.the_geom
   FROM v_adres_totaal a
     JOIN buurt b ON st_within(a.the_geom, b.geom)
     JOIN wijk w ON st_within(a.the_geom, w.geom);

ALTER TABLE v_adres_met_buurt_en_wijk
  OWNER TO vlaardingen;

