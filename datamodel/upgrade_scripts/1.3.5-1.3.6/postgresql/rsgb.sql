-- upgrade RSGB datamodel van 1.3.5 naar 1.3.6 (PostgreSQL)

-- vergroten van het veld 'omschrijving' in de tabel brondocument van 40 naar 255 characters
-- drop de van van brondocument afhankelijke views

-- DROP VIEW v_bd_kad_perceel_met_app;
-- DROP VIEW v_bd_kad_perceel_with_app_re;
-- DROP VIEW v_bd_app_re_all_kad_perceel;
-- DROP VIEW kad_perceel_app_rechten;

-- pas tabel aan
ALTER TABLE brondocument ALTER COLUMN omschrijving TYPE character varying(255);
-- bouw views weer op (uit 107_brk_views.sql)

-- en uit 105_appartements_rechten.sql
-- en...
