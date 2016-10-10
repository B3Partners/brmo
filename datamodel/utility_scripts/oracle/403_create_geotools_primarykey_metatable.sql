--
-- Maak een Geotools/Geoserver primary key metatabel aan, de metatabel 
-- kan vervolgens gebruikt worden voor het invulveld "Primary key metadata table" 
-- in de Geoserver bron configuratie. Uit te voeren als schema eigenaar.
--
-- zie: http://docs.geoserver.org/stable/en/user/data/database/primarykey.html
--
-- NB let op de schema naam 'RSGBTEST' in dit bestand; die dient vervangen te worden
-- NB de lijst views is mogelijk niet compleet, afhankelijk van installatie
-- 
CREATE TABLE gt_pk_metadata (
  table_schema VARCHAR2(32) NOT NULL,
  table_name VARCHAR2(32) NOT NULL,
  pk_column VARCHAR2(32) NOT NULL,
  pk_column_idx NUMBER(38),
  pk_policy VARCHAR2(32),
  pk_sequence VARCHAR2(64),
  CONSTRAINT  chk_pk_policy CHECK (pk_policy IN ('sequence', 'assigned', 'autoincrement')));

CREATE UNIQUE INDEX gt_pk_metadata_table_idx01 ON gt_pk_metadata (table_schema, table_name, pk_column);
COMMENT ON TABLE gt_pk_metadata IS 'Primary key metadata tabel ten behoeve van Geoserver/Geotools';

--
-- insert de primary key metadata voor de views
--
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'V_ADRES', 'FID', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'V_ADRES_LIGPLAATS', 'FID', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'V_ADRES_STANDPLAATS', 'FID', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'V_ADRES_TOTAAL', 'FID', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'V_BD_APP_RE_BIJ_PERCEEL', 'SC_KAD_IDENTIF', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'V_BD_KAD_PERCEEL_MET_APP', 'SC_KAD_IDENTIF', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'V_KAD_PERCEEL_EENVOUDIG', 'SC_KAD_IDENTIF', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'V_KAD_PERCEEL_IN_EIGENDOM', 'SC_KAD_IDENTIF', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'V_LIGPLAATS', 'SC_KAD_IDENTIF', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'V_MAP_KAD_PERCEEL', 'SC_KAD_IDENTIF', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'V_PAND_GEBRUIK_NIET_INGEMETEN', 'FID', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'V_PAND_IN_GEBRUIK', 'FID', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'V_STANDPLAATS', 'SC_IDENTIF', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'V_VERBLIJFSOBJECT', 'FID', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'V_VERBLIJFSOBJECT_ALLES', 'FID', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'V_VERBLIJFSOBJECT_GEVORMD', 'FID', NULL, 'assigned', NULL);

-- in onderstaande tabellen is OBJECTID een gegenereerd veld
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'V_KAD_PERCEEL_ZR_ADRESSEN', 'OBJECTID', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'V_BD_APP_RE_AND_KAD_PERCEEL', 'OBJECTID', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('RSGBTEST', 'VM_KAD_EIGENARENKAART', 'OBJECTID', NULL, 'assigned', NULL);
