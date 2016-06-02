--
-- Maak een Geotools/Geoserver primary key metatabel aan, de metatabel 
-- kan vervolgens gebruikt worden voor het invulveld "Primary key metadata table" 
-- in de Geoserver bron configuratie. Uit te voeren als schema eigenaar.
--
-- zie: http://docs.geoserver.org/stable/en/user/data/database/primarykey.html
--
--
-- NB de lijst views is mogelijk niet compleet, afhankelijk van installatie
-- 
--
-- insert de primary key metadata voor de views
--V_KAD_PERCEEL_EIGENAAR
INSERT INTO gt_pk_metadata VALUES ('FLV_RSGB', 'V_KAD_PERCEEL_EIGENAAR', 'kadaster_identificatie', NULL, 'assigned', NULL);

--V_KAD_EIGENARENKAART
INSERT INTO gt_pk_metadata VALUES ('FLV_RSGB', 'V_KAD_EIGENARENKAART', 'kadaster_identificatie', NULL, 'assigned', NULL);

--materialized view: VM_KAD_EIGENARENKAART 
 INSERT INTO gt_pk_metadata VALUES ('FLV_RSGB', 'VM_KAD_EIGENARENKAART', 'kadaster_identificatie', NULL, 'assigned', NULL); 

--VM_BD_APP_RE_AND_KAD_PERCEEL
INSERT INTO gt_pk_metadata VALUES ('FLV_RSGB', 'VM_BD_APP_RE_AND_KAD_PERCEEL', 'kadaster_identificatie', NULL, 'assigned', NULL);
