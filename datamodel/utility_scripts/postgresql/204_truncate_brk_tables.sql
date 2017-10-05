--Pas op, zorg dat constraints tussen BRK en andere
--BR gedropt zijn, anders gaat de gehele database leeg.

-- kadastrale objecten
truncate app_re cascade;
truncate app_re_archief cascade;
truncate app_re_kad_perceel cascade;
truncate app_re_kad_perceel_archief cascade;
truncate kad_onrrnd_zk cascade;
truncate kad_onrrnd_zk_aantek cascade;
truncate kad_onrrnd_zk_aantek_archief cascade;
truncate kad_onrrnd_zk_archief cascade;
truncate kad_onrrnd_zk_his_rel cascade;
truncate kad_onrrnd_zk_kad_onrr_archief cascade;
truncate kad_onrrnd_zk_kad_onrrnd_zk cascade;
truncate kad_perceel cascade;
truncate kad_perceel_archief cascade;
truncate zak_recht cascade;
truncate zak_recht_aantek cascade;
--
-- personen/subjecten ed. die mogelijk uit kadaster komen, mogelijk 
--    uit andere BR met personen (NHR, BRP)
-- controleer eerst met: 
--        select distinct herkomst_br from herkomst_metadata;
-- als daar meer dan 1 type BR uit komt kunne tabellen niet met 
-- truncate worden geleegd omdat dan er meer dan BRK informatie verloren gaat
--
--truncate ander_btnlnds_niet_nat_prs cascade;
--truncate ander_nat_prs cascade;
--truncate herkomst_metadata cascade;
--truncate ingeschr_nat_prs cascade;
--truncate ingeschr_niet_nat_prs cascade;
--truncate nat_prs cascade;
--truncate niet_ingezetene cascade;
--truncate niet_nat_prs cascade;
--truncate prs cascade;
--truncate subject cascade;
