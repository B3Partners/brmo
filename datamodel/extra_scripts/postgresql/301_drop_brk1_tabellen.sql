-- verwijder alle BRK1 1.0 tabellen en eventueel views die daarvan gebruik maken
-- in versie 5.0.0 is de BRK 1 ondersteuning verwijderd
-- de BRK 1.0 tabellen en views zijn niet meer nodig en kunnen verwijderd worden
-- verwijderen van BRK 1 gerelateerde views
set search_path = public;

drop materialized view if exists mb_kad_onrrnd_zk_archief cascade;
drop materialized view if exists mb_avg_zr_rechth cascade;
drop materialized view if exists mb_zr_rechth cascade;
drop view if exists vb_util_zk_recht cascade;
drop materialized view if exists mb_percelenkaart cascade;
drop materialized view if exists mb_util_app_re_kad_perceel cascade;
drop view if exists vb_util_app_re_parent cascade;
drop view if exists vb_util_app_re_parent_2 cascade;
drop view if exists vb_util_app_re_parent_3 cascade;
drop view if exists vb_util_app_re_splitsing cascade;
drop materialized view if exists mb_avg_subject cascade;
drop materialized view if exists mb_subject cascade;