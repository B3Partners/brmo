-- maakt geometry update scripts voor de rsgb gemeente tabel tbv 111b_update_gemeente_geom.sql
-- laad de gemeente kaart uit de bestuurlijke grenzen van pdok in een postgis database, gebruik 
-- onderstaande om de update scripts te maken voor 111<?>_update_gemeente_geom.sql
SELECT
-- 'UPDATE gemeente SET naam = '|| quote_literal(gemeentenaam) ||', geom = ST_GeomFromEWKT(''' || ST_AsEWKT(geom) || ''') WHERE code = ' || trim(leading '0' FROM code) || ';
-- ' || 'INSERT INTO gemeente (code,naam,geom) SELECT '|| trim(leading '0' FROM code) ||','|| quote_literal(gemeentenaam) ||',ST_GeomFromEWKT(''' || ST_AsEWKT(geom) || ''') WHERE NOT EXISTS (SELECT 1 FROM gemeente WHERE code='||trim(leading '0' FROM code)||');'
--  as postgis_update

-- 'MERGE gemeente AS target USING (VALUES ('|| quote_literal(gemeentenaam) ||',geometry::STGeomFromText(''' || ST_AsText(geom) || ''',28992))) as source (naam,geom) on target.code = ' || trim(leading '0' FROM code) || '
--   WHEN MATCHED THEN UPDATE SET naam = source.naam, geom = source.geom
--   WHEN NOT MATCHED THEN INSERT (code,naam,geom) VALUES (' || trim(leading '0' FROM code) || ', source.naam, source.geom);' as sqlserver_update


-- Oracle heeft handwerk nodig!
-- todo `DECLARE wktA CLOB; wktB CLOB; wktC CLOB; wktD CLOB; wktE CLOB; BEGIN` aan het begin van het script plakken
-- todo `END;` aan het eind van het export bestand plakken
-- oracle heeft een max lengte van 32767 voor een string dus de wkt moet opgeknipt worden in stukken

-- geom: ''' || ST_AsText(geom) || '''
'
  wktA := ''' || substr(ST_AsText(geom), 0,32766)     || ''';
  wktB := ''' || substr(ST_AsText(geom), 32766,32766) || '@' || ''';
  wktC := ''' || substr(ST_AsText(geom), 65532,32766) || '@' || ''';
  wktD := ''' || substr(ST_AsText(geom), 98298,32766) || '@' || ''';
  wktE := ''' || substr(ST_AsText(geom), 131064)      || '@' || ''';
  
  DBMS_LOB.APPEND(wktA,wktB);
  DBMS_LOB.APPEND(wktA,wktC);
  DBMS_LOB.APPEND(wktA,wktD);
  DBMS_LOB.APPEND(wktA,wktE);
  wktA := REPLACE(wktA,''@'','''');
  
  MERGE INTO gemeente USING dual ON (CODE=' || trim(leading '0' FROM code) || ') 
    WHEN MATCHED THEN UPDATE SET NAAM='|| quote_literal(gemeentenaam) ||', GEOM=SDO_GEOMETRY(wktA,28992) 
    WHEN NOT MATCHED THEN INSERT (CODE,NAAM,GEOM) VALUES (' || trim(leading '0' FROM code) || ','||quote_literal(gemeentenaam) ||', SDO_GEOMETRY(wktA,28992));'

as oracle__update

FROM "PDOK-gemeenten_2016"
-- WHERE code IN ('0518','0523','0820', '0632', '0344')
;


