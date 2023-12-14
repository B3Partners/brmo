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
-- insert de primary key metadata voor de views, NB OBJECTID is een gegenereerd veld (rijnummer)
--
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_adres_ligplaats', 'fid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_adres_standplaats', 'fid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_bd_kad_perceel_met_app', 'sc_kad_identif', NULL, 'assigned', NULL);

INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_adres', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_ligplaats_alles', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_standplaats_alles', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_adres_totaal', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_bd_app_re_bij_perceel', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_kad_perceel_eenvoudig', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_kad_perceel_in_eigendom', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_ligplaats', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_map_kad_perceel', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_pand_gebruik_niet_ingemeten', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_pand_in_gebruik', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_standplaats', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_verblijfsobject', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_verblijfsobject_alles', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_verblijfsobject_gevormd', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_kad_perceel_zr_adressen', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_bd_app_re_and_kad_perceel', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_kad_eigenarenkaart', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_bd_kad_perceel_met_app_vlak', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_adres_pandvlak', 'objectid', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('brmo_rsgb', 'v_adres_totaal_vlak', 'objectid', NULL, 'assigned', NULL);

-- brk 2.0 materialized views
INSERT INTO gt_pk_metadata VALUES ('brk', 'mb_percelenkaart', 'objectid', null, 'assigned', null);
INSERT INTO gt_pk_metadata VALUES ('brk', 'mb_subject', 'objectid', null, 'assigned', null);
INSERT INTO gt_pk_metadata VALUES ('brk', 'mb_avg_subject', 'objectid', null, 'assigned', null);
INSERT INTO gt_pk_metadata VALUES ('brk', 'mb_kad_onrrnd_zk_adres', 'objectid', null, 'assigned', null);
INSERT INTO gt_pk_metadata VALUES ('brk', 'mb_kad_onrrnd_zk_archief', 'objectid', null, 'assigned', null);
INSERT INTO gt_pk_metadata VALUES ('brk', 'mb_zr_rechth', 'objectid', null, 'assigned', null);
INSERT INTO gt_pk_metadata VALUES ('brk', 'mb_avg_zr_rechth', 'objectid', null, 'assigned', null);
INSERT INTO gt_pk_metadata VALUES ('brk', 'mb_koz_rechth', 'objectid', null, 'assigned', null);
INSERT INTO gt_pk_metadata VALUES ('brk', 'mb_avg_koz_rechth', 'objectid', null, 'assigned', null);

-- brk 2.0 x bag 2.0 materialized views
insert into gt_pk_metadata values ('public', 'mb_adresseerbaar_object_geometrie_bag', 'objectid', null, 'assigned', null);
insert into gt_pk_metadata values ('public', 'mb_kadastraleonroerendezakenmetadres', 'objectid', null, 'assigned', null);
insert into gt_pk_metadata values ('public', 'mb_onroerendezakenmetrechthebbenden', 'objectid', null, 'assigned', null);
insert into gt_pk_metadata values ('public', 'mb_avg_onroerendezakenmetrechthebbenden', 'objectid', null, 'assigned', null);