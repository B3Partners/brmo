-- 
-- SQL voor aanmaken materialized views van alle views met een ObjectID kolom tbv. arcgis. 
-- bruikbaar vanaf versie 1.4.0
--
-- Views kunnen vervolgens in SDE worden geregistreerd, zie: http://resources.arcgis.com/en/help/main/10.1/002n/002n00000029000000.htm
-- of er kunnen layerfiles worden gemaakt als er geen sde beschikbaar is.
--
-- de refresh van de M-views wordt ingesteld om 07:30 iedere dag
-- de DBA kan ook kiezen voor een andere bijwerk procedure mbv. een scheduler; daarbij kan dan de ruimtelijke index eerst worden gedropped zodat het opbouwen sneller gaat.
--
-- gebruik onderstaande select om (een aangepaste versie van) de sql te genereren 
--  (voer dan eventueel eerst 401_insert_spatial_view_metadata.sql uit om views in USER_SDO_GEOM_METADATA te registreren)
--
/* 
SELECT 
    CHR(10) ||'--drop materialized view ' || REPLACE(table_name,'V_', 'VM_')||';'|| CHR(10) ||
    'create materialized view ' || REPLACE(table_name,'V_', 'VM_') ||' refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from ' || table_name || ';'  || CHR(10) ||
    'delete from user_sdo_geom_metadata where TABLE_NAME ='''||REPLACE(table_name,'V_', 'VM_')||''';'|| CHR(10) ||
    'insert into user_sdo_geom_metadata values ('''||REPLACE(table_name,'V_', 'VM_')||''', '''||m.column_name||''', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT(''X'', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT(''Y'', 304000, 620000, .1)), '||m.srid||');'|| CHR(10) ||
    'create unique index '||SUBSTR(REPLACE(table_name,'V_', 'VM_'),0,22)||'_OID_IDX on '||REPLACE(table_name,'V_', 'VM_')||' (OBJECTID ASC);'|| CHR(10) ||
    'create index '||SUBSTR(REPLACE(table_name,'V_', 'VM_'),0,24)||'_G_IDX on '||REPLACE(table_name,'V_', 'VM_')|| '('||m.column_name ||') indextype is mdsys.spatial_index;-- (''LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT'');'
AS create_vm_en_index
FROM
    USER_SDO_GEOM_METADATA m
WHERE
    m.table_name IN (
        SELECT cols.table_name FROM USER_TAB_COLUMNS cols
        WHERE
            cols.table_name IN (SELECT uv.view_name FROM USER_VIEWS uv)
        AND cols.column_name = 'OBJECTID')
ORDER BY m.table_name;
*/

--drop materialized view VM_ADRES;
create materialized view VM_ADRES refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_ADRES;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_ADRES';
insert into user_sdo_geom_metadata values ('VM_ADRES', 'THE_GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_ADRES_OID_IDX on VM_ADRES (OBJECTID ASC);
create index VM_ADRES_G_IDX on VM_ADRES(THE_GEOM) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_ADRES_PANDVLAK;
create materialized view VM_ADRES_PANDVLAK refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_ADRES_PANDVLAK;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_ADRES_PANDVLAK';
insert into user_sdo_geom_metadata values ('VM_ADRES_PANDVLAK', 'THE_GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_ADRES_PANDVLAK_OID_IDX on VM_ADRES_PANDVLAK (OBJECTID ASC);
create index VM_ADRES_PANDVLAK_G_IDX on VM_ADRES_PANDVLAK(THE_GEOM) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_ADRES_TOTAAL;
create materialized view VM_ADRES_TOTAAL refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_ADRES_TOTAAL;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_ADRES_TOTAAL';
insert into user_sdo_geom_metadata values ('VM_ADRES_TOTAAL', 'THE_GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_ADRES_TOTAAL_OID_IDX on VM_ADRES_TOTAAL (OBJECTID ASC);
create index VM_ADRES_TOTAAL_G_IDX on VM_ADRES_TOTAAL(THE_GEOM) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_ADRES_TOTAAL_VLAK;
create materialized view VM_ADRES_TOTAAL_VLAK refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_ADRES_TOTAAL_VLAK;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_ADRES_TOTAAL_VLAK';
insert into user_sdo_geom_metadata values ('VM_ADRES_TOTAAL_VLAK', 'THE_GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_ADRES_TOTAAL_VLAK_OID_IDX on VM_ADRES_TOTAAL_VLAK (OBJECTID ASC);
create index VM_ADRES_TOTAAL_VLAK_G_IDX on VM_ADRES_TOTAAL_VLAK(THE_GEOM) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_BD_APP_RE_AND_KAD_PERCEEL;
create materialized view VM_BD_APP_RE_AND_KAD_PERCEEL refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_BD_APP_RE_AND_KAD_PERCEEL;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_BD_APP_RE_AND_KAD_PERCEEL';
insert into user_sdo_geom_metadata values ('VM_BD_APP_RE_AND_KAD_PERCEEL', 'BEGRENZING_PERCEEL', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_BD_APP_RE_AND_KAD_P_OID_IDX on VM_BD_APP_RE_AND_KAD_PERCEEL (OBJECTID ASC);
create index VM_BD_APP_RE_AND_KAD_PER_G_IDX on VM_BD_APP_RE_AND_KAD_PERCEEL(BEGRENZING_PERCEEL) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_BD_APP_RE_BIJ_PERCEEL;
create materialized view VM_BD_APP_RE_BIJ_PERCEEL refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_BD_APP_RE_BIJ_PERCEEL;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_BD_APP_RE_BIJ_PERCEEL';
insert into user_sdo_geom_metadata values ('VM_BD_APP_RE_BIJ_PERCEEL', 'BEGRENZING_PERCEEL', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_BD_APP_RE_BIJ_PERCE_OID_IDX on VM_BD_APP_RE_BIJ_PERCEEL (OBJECTID ASC);
create index VM_BD_APP_RE_BIJ_PERCEEL_G_IDX on VM_BD_APP_RE_BIJ_PERCEEL(BEGRENZING_PERCEEL) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_BD_KAD_PERCEEL_MET_APP_VLAK;
create materialized view VM_BD_KAD_PERCEEL_MET_APP_VLAK refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_BD_KAD_PERCEEL_MET_APP_VLAK;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_BD_KAD_PERCEEL_MET_APP_VLAK';
insert into user_sdo_geom_metadata values ('VM_BD_KAD_PERCEEL_MET_APP_VLAK', 'BEGRENZING_PERCEEL', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_BD_KAD_PERCEEL_MET__OID_IDX on VM_BD_KAD_PERCEEL_MET_APP_VLAK (OBJECTID ASC);
create index VM_BD_KAD_PERCEEL_MET_AP_G_IDX on VM_BD_KAD_PERCEEL_MET_APP_VLAK(BEGRENZING_PERCEEL) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_KAD_PERCEEL_EENVOUDIG;
create materialized view VM_KAD_PERCEEL_EENVOUDIG refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_KAD_PERCEEL_EENVOUDIG;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_KAD_PERCEEL_EENVOUDIG';
insert into user_sdo_geom_metadata values ('VM_KAD_PERCEEL_EENVOUDIG', 'BEGRENZING_PERCEEL', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_KAD_PERCEEL_EENVOUD_OID_IDX on VM_KAD_PERCEEL_EENVOUDIG (OBJECTID ASC);
create index VM_KAD_PERCEEL_EENVOUDIG_G_IDX on VM_KAD_PERCEEL_EENVOUDIG(BEGRENZING_PERCEEL) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_KAD_PERCEEL_IN_EIGENDOM;
create materialized view VM_KAD_PERCEEL_IN_EIGENDOM refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_KAD_PERCEEL_IN_EIGENDOM;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_KAD_PERCEEL_IN_EIGENDOM';
insert into user_sdo_geom_metadata values ('VM_KAD_PERCEEL_IN_EIGENDOM', 'BEGRENZING_PERCEEL', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_KAD_PERCEEL_IN_EIGE_OID_IDX on VM_KAD_PERCEEL_IN_EIGENDOM (OBJECTID ASC);
create index VM_KAD_PERCEEL_IN_EIGEND_G_IDX on VM_KAD_PERCEEL_IN_EIGENDOM(BEGRENZING_PERCEEL) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_KAD_PERCEEL_ZR_ADRESSEN;
create materialized view VM_KAD_PERCEEL_ZR_ADRESSEN refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_KAD_PERCEEL_ZR_ADRESSEN;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_KAD_PERCEEL_ZR_ADRESSEN';
insert into user_sdo_geom_metadata values ('VM_KAD_PERCEEL_ZR_ADRESSEN', 'BEGRENZING_PERCEEL', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_KAD_PERCEEL_ZR_ADRE_OID_IDX on VM_KAD_PERCEEL_ZR_ADRESSEN (OBJECTID ASC);
create index VM_KAD_PERCEEL_ZR_ADRESS_G_IDX on VM_KAD_PERCEEL_ZR_ADRESSEN(BEGRENZING_PERCEEL) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_LIGPLAATS;
create materialized view VM_LIGPLAATS refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_LIGPLAATS;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_LIGPLAATS';
insert into user_sdo_geom_metadata values ('VM_LIGPLAATS', 'GEOMETRIE', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_LIGPLAATS_OID_IDX on VM_LIGPLAATS (OBJECTID ASC);
create index VM_LIGPLAATS_G_IDX on VM_LIGPLAATS(GEOMETRIE) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_LIGPLAATS_ALLES;
create materialized view VM_LIGPLAATS_ALLES refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_LIGPLAATS_ALLES;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_LIGPLAATS_ALLES';
insert into user_sdo_geom_metadata values ('VM_LIGPLAATS_ALLES', 'THE_GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_LIGPLAATS_ALLES_OID_IDX on VM_LIGPLAATS_ALLES (OBJECTID ASC);
create index VM_LIGPLAATS_ALLES_G_IDX on VM_LIGPLAATS_ALLES(THE_GEOM) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_MAP_KAD_PERCEEL;
create materialized view VM_MAP_KAD_PERCEEL refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_MAP_KAD_PERCEEL;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_MAP_KAD_PERCEEL';
insert into user_sdo_geom_metadata values ('VM_MAP_KAD_PERCEEL', 'BEGRENZING_PERCEEL', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_MAP_KAD_PERCEEL_OID_IDX on VM_MAP_KAD_PERCEEL (OBJECTID ASC);
create index VM_MAP_KAD_PERCEEL_G_IDX on VM_MAP_KAD_PERCEEL(BEGRENZING_PERCEEL) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_PAND_GEBRUIK_NIET_INGEMETEN;
create materialized view VM_PAND_GEBRUIK_NIET_INGEMETEN refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_PAND_GEBRUIK_NIET_INGEMETEN;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_PAND_GEBRUIK_NIET_INGEMETEN';
insert into user_sdo_geom_metadata values ('VM_PAND_GEBRUIK_NIET_INGEMETEN', 'THE_GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_PAND_GEBRUIK_NIET_I_OID_IDX on VM_PAND_GEBRUIK_NIET_INGEMETEN (OBJECTID ASC);
create index VM_PAND_GEBRUIK_NIET_ING_G_IDX on VM_PAND_GEBRUIK_NIET_INGEMETEN(THE_GEOM) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_PAND_IN_GEBRUIK;
create materialized view VM_PAND_IN_GEBRUIK refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_PAND_IN_GEBRUIK;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_PAND_IN_GEBRUIK';
insert into user_sdo_geom_metadata values ('VM_PAND_IN_GEBRUIK', 'THE_GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_PAND_IN_GEBRUIK_OID_IDX on VM_PAND_IN_GEBRUIK (OBJECTID ASC);
create index VM_PAND_IN_GEBRUIK_G_IDX on VM_PAND_IN_GEBRUIK(THE_GEOM) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_STANDPLAATS;
create materialized view VM_STANDPLAATS refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_STANDPLAATS;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_STANDPLAATS';
insert into user_sdo_geom_metadata values ('VM_STANDPLAATS', 'GEOMETRIE', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_STANDPLAATS_OID_IDX on VM_STANDPLAATS (OBJECTID ASC);
create index VM_STANDPLAATS_G_IDX on VM_STANDPLAATS(GEOMETRIE) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_STANDPLAATS_ALLES;
create materialized view VM_STANDPLAATS_ALLES refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_STANDPLAATS_ALLES;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_STANDPLAATS_ALLES';
insert into user_sdo_geom_metadata values ('VM_STANDPLAATS_ALLES', 'THE_GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_STANDPLAATS_ALLES_OID_IDX on VM_STANDPLAATS_ALLES (OBJECTID ASC);
create index VM_STANDPLAATS_ALLES_G_IDX on VM_STANDPLAATS_ALLES(THE_GEOM) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_VERBLIJFSOBJECT;
create materialized view VM_VERBLIJFSOBJECT refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_VERBLIJFSOBJECT;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_VERBLIJFSOBJECT';
insert into user_sdo_geom_metadata values ('VM_VERBLIJFSOBJECT', 'THE_GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_VERBLIJFSOBJECT_OID_IDX on VM_VERBLIJFSOBJECT (OBJECTID ASC);
create index VM_VERBLIJFSOBJECT_G_IDX on VM_VERBLIJFSOBJECT(THE_GEOM) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_VERBLIJFSOBJECT_ALLES;
create materialized view VM_VERBLIJFSOBJECT_ALLES refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_VERBLIJFSOBJECT_ALLES;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_VERBLIJFSOBJECT_ALLES';
insert into user_sdo_geom_metadata values ('VM_VERBLIJFSOBJECT_ALLES', 'THE_GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_VERBLIJFSOBJECT_ALL_OID_IDX on VM_VERBLIJFSOBJECT_ALLES (OBJECTID ASC);
create index VM_VERBLIJFSOBJECT_ALLES_G_IDX on VM_VERBLIJFSOBJECT_ALLES(THE_GEOM) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');

--drop materialized view VM_VERBLIJFSOBJECT_GEVORMD;
create materialized view VM_VERBLIJFSOBJECT_GEVORMD refresh on demand start with trunc(sysdate) + (7.5/24) next trunc(sysdate) + 1+(7.5/24) as select * from V_VERBLIJFSOBJECT_GEVORMD;
delete from user_sdo_geom_metadata where TABLE_NAME ='VM_VERBLIJFSOBJECT_GEVORMD';
insert into user_sdo_geom_metadata values ('VM_VERBLIJFSOBJECT_GEVORMD', 'THE_GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
create unique index VM_VERBLIJFSOBJECT_GEV_OID_IDX on VM_VERBLIJFSOBJECT_GEVORMD (OBJECTID ASC);
create index VM_VERBLIJFSOBJECT_GEVOR_G_IDX on VM_VERBLIJFSOBJECT_GEVORMD(THE_GEOM) indextype is mdsys.spatial_index;-- ('LAYER_GTYPE=MULTIPOLYGON"|LINE|COLLECTION|POINT');