
CREATE OR REPLACE VIEW v_adres_totaal_vlaardingen AS 
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

ALTER TABLE v_adres_totaal_vlaardingen
  OWNER TO vlaardingen;
