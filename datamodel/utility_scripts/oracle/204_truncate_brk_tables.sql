--Pas op, zorg dat constraints tussen BRK en andere
--BR gedropt zijn, anders gaat de gehele database leeg.

-- kadastrale objecten: verwijderd in versie 5.0.0
-- TRUNCATE TABLE app_re_kad_perceel CASCADE;
-- TRUNCATE TABLE app_re_kad_perceel_archief CASCADE;
-- TRUNCATE TABLE zak_recht_aantek CASCADE;
-- TRUNCATE TABLE zak_recht CASCADE;
-- TRUNCATE TABLE app_re_archief CASCADE;
-- TRUNCATE TABLE app_re CASCADE;
-- TRUNCATE TABLE kad_perceel_archief CASCADE;
-- TRUNCATE TABLE kad_perceel CASCADE;
-- TRUNCATE TABLE kad_onrrnd_zk_kad_onrr_archief CASCADE;
-- TRUNCATE TABLE kad_onrrnd_zk_kad_onrrnd_zk CASCADE;
-- TRUNCATE TABLE kad_onrrnd_zk_aantek_archief CASCADE;
-- TRUNCATE TABLE kad_onrrnd_zk_archief CASCADE;
-- TRUNCATE TABLE kad_onrrnd_zk_aantek CASCADE;
-- TRUNCATE TABLE zak_recht_aantek CASCADE;
-- TRUNCATE TABLE kad_onrrnd_zk_his_rel CASCADE;
-- TRUNCATE TABLE kad_onrrnd_zk CASCADE;
-- TRUNCATE TABLE kad_onrrnd_zk_archief CASCADE;

--
-- personen/subjecten ed. die mogelijk uit kadaster komen,
-- mogelijk uit andere BR met personen (NHR, BRP, WOZ)
-- controleer eerst met:
--        SELECT DISTINCT herkomst_br FROM herkomst_metadata CASCADE;
-- als daar meer dan 1 type BR uit komt kunnen tabellen niet met
-- truncate worden geleegd omdat dan er meer dan alleen BRK informatie verloren gaat
--
TRUNCATE TABLE herkomst_metadata CASCADE;
TRUNCATE TABLE ander_btnlnds_niet_nat_prs CASCADE;
TRUNCATE TABLE ingeschr_niet_nat_prs CASCADE;
TRUNCATE TABLE niet_nat_prs CASCADE;
TRUNCATE TABLE ander_nat_prs CASCADE;
TRUNCATE TABLE niet_ingezetene CASCADE;
TRUNCATE TABLE ingeschr_nat_prs CASCADE;
TRUNCATE TABLE nat_prs CASCADE;
TRUNCATE TABLE prs CASCADE;
TRUNCATE TABLE subject CASCADE;
