-- maakt geometry update scripts voor de rsgb gemeente tabel tbv 111a_update_gemeente_geom.sql
--
-- 1. laad de gemeente kaart uit de bestuurlijke grenzen van pdok in een postgis database
-- ogr2ogr -f "PostgreSQL" "PG:dbname=dingetjes host=localhost user=mark_postgis password=mark_postgis" bestuurlijkegrenzen/Gemeentegrenzen.gml -append -nln public.gemeenten2018 -select "code,gemeentenaam" -nlt PROMOTE_TO_MULTI
--
-- 2. gebruik onderstaande om de update scripts te maken voor 111a_update_gemeente_geom.sql
SELECT

-- postgis blok
-- 'UPDATE gemeente SET geom = ST_GeomFromEWKT(''' || ST_AsEWKT(wkb_geometry) || ''') WHERE code = ' || trim(leading '0' FROM code) || ';
-- ' || 'INSERT INTO gemeente (code,naam,geom) SELECT '|| trim(leading '0' FROM code) ||','''|| gemeentenaam ||''',ST_GeomFromEWKT(''' || ST_AsEWKT(wkb_geometry) || ''') WHERE NOT EXISTS (SELECT 1 FROM gemeente WHERE code='||trim(leading '0' FROM code)||');'
--  as "--postgis gemeente 2018 geometrie update"

-- mssql blok
-- 'MERGE gemeente AS target USING (VALUES (geometry::STGeomFromText(''' || ST_AsText(wkb_geometry) || ''',28992))) AS source (geom) ON target.code = ' || trim(leading '0' FROM code) || '
--   WHEN MATCHED THEN UPDATE SET geom = source.geom
--   WHEN NOT MATCHED THEN INSERT (code,naam,geom) VALUES (' || trim(leading '0' FROM code) ||','''|| gemeentenaam ||''', source.geom);'
--as "--mssql gemeente 2018 geometrie update"

-- oracle blok
-- NB. Oracle heeft handwerk nodig! 
-- todo/handwerk `DECLARE wktA CLOB; wktB CLOB; wktC CLOB; wktD CLOB; wktE CLOB; wktF CLOB; BEGIN` aan het begin van het script plakken
-- todo/handwerk `END;` aan het eind van het export bestand plakken
-- Na het laden van de data kan het zinvol zijn om de ruimtelijke index te verversen; bijvoorbeeld ALTER INDEX GEMEENTE_GEOM1_IDX REBUILD;

-- oracle heeft een max lengte van 32767 voor een string dus de wkt moet opgeknipt worden in stukken en dan als blob verwerkt...
'
  wktA := ''' || substr(ST_AsText(wkb_geometry), 0,32766)     || ''';
  wktB := ''' || substr(ST_AsText(wkb_geometry), 32766,32766) || '@' || ''';
  wktC := ''' || substr(ST_AsText(wkb_geometry), 65532,32766) || '@' || ''';
  wktD := ''' || substr(ST_AsText(wkb_geometry), 98298,32766) || '@' || ''';
  wktE := ''' || substr(ST_AsText(wkb_geometry), 131064,32766)|| '@' || ''';
  wktF := ''' || substr(ST_AsText(wkb_geometry), 163830)      || '@' || ''';

  DBMS_LOB.APPEND(wktA,wktB);
  DBMS_LOB.APPEND(wktA,wktC);
  DBMS_LOB.APPEND(wktA,wktD);
  DBMS_LOB.APPEND(wktA,wktE);
  DBMS_LOB.APPEND(wktA,wktF);
  wktA := REPLACE(wktA,''@'','''');

  MERGE INTO gemeente USING dual ON (CODE=' || trim(leading '0' FROM code) || ')
    WHEN MATCHED THEN UPDATE SET GEOM=SDO_GEOMETRY(wktA,28992)
    WHEN NOT MATCHED THEN INSERT (CODE,NAAM,GEOM) VALUES (' || trim(leading '0' FROM code) || ','''|| gemeentenaam ||''', SDO_GEOMETRY(wktA,28992));'
as "--oracle gemeente 2018 geometrie update"

FROM gemeenten2018
--WHERE code IN ('0003','0584','0448','0289')
ORDER BY code;

