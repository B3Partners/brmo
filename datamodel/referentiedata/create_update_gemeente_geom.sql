-- laadt de CBS shape van https://www.cbs.nl/nl-nl/dossier/nederland-regionaal/geografische-data/wijk-en-buurtkaart-2020
-- update gemeenten2020 set gm_code = substring(gm_code,3);
-- update gemeenten2020 set geom = ST_Multi(ST_SimplifyPreserveTopology(geom, 0.01));
-- update gemeenten2020 set geom = ST_SnapToGrid(geom, 0.01);

SELECT

-- postgis blok
-- 'UPDATE gemeente SET geom = ST_GeomFromEWKT(''' || ST_AsEWKT(geom) || ''') WHERE code = ' || trim(leading '0' FROM code) || ';
-- ' || 'INSERT INTO gemeente (code,naam,geom) SELECT '|| trim(leading '0' FROM gm_code) ||','''|| gm_naam ||''',ST_GeomFromEWKT(''' || ST_AsEWKT(geom) || ''') WHERE NOT EXISTS (SELECT 1 FROM gemeente WHERE code='||trim(leading '0' FROM gm_code)||');'
--  as "--postgis gemeente 2020 geometrie update"

-- mssql blok
-- 'MERGE gemeente AS target USING (VALUES (geometry::STGeomFromText(''' || ST_AsText(geom) || ''',28992))) AS source (geom) ON target.code = ' || trim(leading '0' FROM gm_code) || '
--   WHEN MATCHED THEN UPDATE SET geom = source.geom
--   WHEN NOT MATCHED THEN INSERT (code,naam,geom) VALUES (' || trim(leading '0' FROM gm_code) ||','''|| gm_naam ||''', source.geom);'
--as "--mssql gemeente 2020 geometrie update"

-- oracle blok
-- NB. Oracle heeft handwerk nodig! 
-- todo/handwerk `DECLARE wktA CLOB; wktB CLOB; wktC CLOB; wktD CLOB; wktE CLOB; wktF CLOB; wktG CLOB; wktH CLOB; wktK CLOB; wktL CLOB; wktM CLOB; wktN CLOB; wktO CLOB; wktP CLOB; wktQ CLOB; wktR CLOB; wktS CLOB; BEGIN` aan het begin van het script plakken
-- todo/handwerk `END;` aan het eind van het export bestand plakken
-- Na het laden van de data kan het zinvol zijn om de ruimtelijke index te verversen; bijvoorbeeld ALTER INDEX GEMEENTE_GEOM1_IDX REBUILD;

-- oracle heeft een max lengte van 32767 voor een string dus de wkt moet opgeknipt worden in stukken en dan als blob verwerkt...
'
  wktA := ''' || substr(ST_AsText(geom), 0,32766)      || ''';
  wktB := ''' || substr(ST_AsText(geom), 32766,32766)  || '@' || ''';
  wktC := ''' || substr(ST_AsText(geom), 65532,32766)  || '@' || ''';
  wktD := ''' || substr(ST_AsText(geom), 98298,32766)  || '@' || ''';
  wktE := ''' || substr(ST_AsText(geom), 131064,32766) || '@' || ''';
  wktF := ''' || substr(ST_AsText(geom), 163830,32766) || '@' || ''';
  wktG := ''' || substr(ST_AsText(geom), 196596,32766) || '@' || ''';
  wktH := ''' || substr(ST_AsText(geom), 229362,32766) || '@' || ''';
  wktK := ''' || substr(ST_AsText(geom), 262128,32766) || '@' || ''';
  wktL := ''' || substr(ST_AsText(geom), 294894,32766) || '@' || ''';
  wktM := ''' || substr(ST_AsText(geom), 327660,32766) || '@' || ''';
  wktN := ''' || substr(ST_AsText(geom), 360426,32766) || '@' || ''';
  wktO := ''' || substr(ST_AsText(geom), 393192,32766) || '@' || ''';
  wktP := ''' || substr(ST_AsText(geom), 425958,32766) || '@' || ''';
  wktQ := ''' || substr(ST_AsText(geom), 458724,32766) || '@' || ''';
  wktR := ''' || substr(ST_AsText(geom), 491490,32766) || '@' || ''';
  wktS := ''' || substr(ST_AsText(geom), 524256)       || '@' || ''';

  DBMS_LOB.APPEND(wktA,wktB);
  DBMS_LOB.APPEND(wktA,wktC);
  DBMS_LOB.APPEND(wktA,wktD);
  DBMS_LOB.APPEND(wktA,wktE);
  DBMS_LOB.APPEND(wktA,wktF);
  DBMS_LOB.APPEND(wktA,wktG);
  DBMS_LOB.APPEND(wktA,wktH);
  DBMS_LOB.APPEND(wktA,wktK);
  DBMS_LOB.APPEND(wktA,wktL);
  DBMS_LOB.APPEND(wktA,wktM);
  DBMS_LOB.APPEND(wktA,wktN);
  DBMS_LOB.APPEND(wktA,wktO);
  DBMS_LOB.APPEND(wktA,wktP);
  DBMS_LOB.APPEND(wktA,wktQ);
  DBMS_LOB.APPEND(wktA,wktR);
  DBMS_LOB.APPEND(wktA,wktS);
  wktA := REPLACE(wktA,''@'','''');

  MERGE INTO gemeente USING dual ON (CODE=' || trim(leading '0' FROM gm_code) || ')
    WHEN MATCHED THEN UPDATE SET GEOM=SDO_GEOMETRY(wktA,28992)
    WHEN NOT MATCHED THEN INSERT (CODE,NAAM,GEOM) VALUES (' || trim(leading '0' FROM gm_code) || ','''|| gm_naam ||''', SDO_GEOMETRY(wktA,28992));'
as "--oracle gemeente 2020 geometrie update"

FROM gemeenten2020
WHERE water='NEE'
--AND gm_code IN ('0003','0584','0448','0289')
ORDER BY gm_code;
