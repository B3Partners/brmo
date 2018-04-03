-- ogr2ogr -f "PostgreSQL" "PG:dbname=dingetjes host=localhost user=mark_postgis password=mark_postgis" bestuurlijkegrenzen/Gemeentegrenzen.gml -append -nln public.gemeenten2018 -select "code" -nlt PROMOTE_TO_MULTI


-- maakt geometry update scripts voor de rsgb gemeente tabel tbv 111b_update_gemeente_geom.sql
-- laad de gemeente kaart uit de bestuurlijke grenzen van pdok in een postgis database, gebruik
-- onderstaande om de update scripts te maken voor 111<?>_update_gemeente_geom.sql
SELECT

-- 'UPDATE gemeente SET geom = ST_GeomFromEWKT(''' || ST_AsEWKT(wkb_geometry) || ''') WHERE code = ' || trim(leading '0' FROM code) || ';
-- ' || 'INSERT INTO gemeente (code,geom) SELECT '|| trim(leading '0' FROM code) ||',ST_GeomFromEWKT(''' || ST_AsEWKT(wkb_geometry) || ''') WHERE NOT EXISTS (SELECT 1 FROM gemeente WHERE code='||trim(leading '0' FROM code)||');'
--  as postgis_update

 'MERGE gemeente AS target USING (VALUES (geometry::STGeomFromText(''' || ST_AsText(wkb_geometry) || ''',28992) as source (geom) on target.code = ' || trim(leading '0' FROM code) || '
   WHEN MATCHED THEN UPDATE SET geom = source.geom
   WHEN NOT MATCHED THEN INSERT (code,geom) VALUES (' || trim(leading '0' FROM code) || ', source.geom);'
as sqlserver_update


-- Oracle heeft handwerk nodig!
-- todo `DECLARE wktA CLOB; wktB CLOB; wktC CLOB; wktD CLOB; wktE CLOB; BEGIN` aan het begin van het script plakken
-- todo `END;` aan het eind van het export bestand plakken
-- oracle heeft een max lengte van 32767 voor een string dus de wkt moet opgeknipt worden in stukken...

--'
--  wktA := ''' || substr(ST_AsText(wkb_geometry), 0,32766)     || ''';
--  wktB := ''' || substr(ST_AsText(wkb_geometry), 32766,32766) || '@' || ''';
--  wktC := ''' || substr(ST_AsText(wkb_geometry), 65532,32766) || '@' || ''';
--  wktD := ''' || substr(ST_AsText(wkb_geometry), 98298,32766) || '@' || ''';
--  wktE := ''' || substr(ST_AsText(wkb_geometry), 131064)      || '@' || ''';
--
--  DBMS_LOB.APPEND(wktA,wktB);
--  DBMS_LOB.APPEND(wktA,wktC);
--  DBMS_LOB.APPEND(wktA,wktD);
--  DBMS_LOB.APPEND(wktA,wktE);
--  wktA := REPLACE(wktA,''@'','''');
--
--  MERGE INTO gemeente USING dual ON (CODE=' || trim(leading '0' FROM code) || ')
--    WHEN MATCHED THEN UPDATE SET GEOM=SDO_GEOMETRY(wktA,28992)
--    WHEN NOT MATCHED THEN INSERT (CODE,GEOM) VALUES (' || trim(leading '0' FROM code) || ', SDO_GEOMETRY(wktA,28992));'
--as oracle__update


FROM gemeenten2018
WHERE code IN ('0584','0448','0289')
;

