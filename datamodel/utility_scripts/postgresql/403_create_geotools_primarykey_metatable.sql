--
-- Maak een Geotools/Geoserver primary key metatabel aan, de metatabel 
-- kan vervolgens gebruikt worden voor het invulveld "Primary key metadata table" 
-- in de Geoserver bron configuratie. Uit te voeren als schema eigenaar.
--
-- zie: http://docs.geoserver.org/stable/en/user/data/database/primarykey.html
--
-- NB. let op de schema naam 'brmo_rsgb' in dit bestand; die dient vervangen te worden door de juiste naam.
-- NB. de lijst views is mogelijk niet compleet, afhankelijk van installatie
-- 
CREATE TABLE gt_pk_metadata (
    table_schema VARCHAR(32) NOT NULL,
    table_name VARCHAR(32) NOT NULL,
    pk_column VARCHAR(32) NOT NULL,
    pk_column_idx INTEGER,
    pk_policy VARCHAR(32),
    pk_sequence VARCHAR(64),
    unique (table_schema, table_name, pk_column),
    check (pk_policy in ('sequence', 'assigned', 'autoincrement'))
);

CREATE UNIQUE INDEX gt_pk_metadata_idx 
  ON gt_pk_metadata (table_schema, table_name, pk_column);

--
-- insert de primary key metadata voor de views
--
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_adres', 'fid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_adres_ligplaats', 'fid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_adres_standplaats', 'fid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_adres_totaal', 'fid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_bd_app_re_bij_perceel', 'sc_kad_identif', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_bd_kad_perceel_met_app', 'sc_kad_identif', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_kad_perceel_eenvoudig', 'sc_kad_identif', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_kad_perceel_in_eigendom', 'sc_kad_identif', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_ligplaats', 'sc_kad_identif', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_map_kad_perceel', 'sc_kad_identif', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_pand_gebruik_niet_ingemeten', 'fid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_pand_in_gebruik', 'fid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_standplaats', 'sc_identif', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_verblijfsobject', 'fid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_verblijfsobject_alles', 'fid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_verblijfsobject_gevormd', 'fid', NULL, 'assigned', NULL);

-- in onderstaande tabellen is objectid een gegenereerd veld
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_kad_perceel_zr_adressen', 'ObjectID', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_bd_app_re_and_kad_perceel', 'ObjectID', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_kad_eigenarenkaart', 'objectid', NULL, 'assigned', NULL);
