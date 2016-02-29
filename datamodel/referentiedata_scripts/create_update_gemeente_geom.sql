-- maakt geometry update scripts voor de rsgb gemeente tabel tbv 111b_update_gemeente_geom.sql
-- laad de gemeente kaart uit de bestuurlijke grenzen van pdok in een postgis database, gebruik onderstaande om de update scripts te maken
SELECT
  'UPDATE gemeente SET naam = ''' || gemeentenaam ||''', geom = ST_GeomFromEWKT(''' || ST_AsEWKT(geom) || ''') WHERE code = ' || trim(leading '0' FROM code) || ';' as postgis_update
  -- 'UPDATE gemeente SET geom = SDO_GEOMETRY(''' || ST_AsText(geom) || ''',28992) WHERE code = ' || trim(leading '0' FROM code) || ';' as oracle_update
  -- 'UPDATE gemeente SET geom = STGeomFromText(''' || ST_AsText(geom) || ''',28992) WHERE code = ' || trim(leading '0' FROM code) || ';' as sqlserver_update
FROM "PDOK-gemeenten_2016"
 WHERE code = '0034'
-- LIMIT 1
;
