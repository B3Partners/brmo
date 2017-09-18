-- 
-- upgrade Oracle RSGB datamodel van 1.4.5 naar 1.5.0 
--

-- pas view v_bd_app_re_all_kad_perceel aan om te filteren tegen actuele appartementsrechten issue#331
CREATE OR REPLACE VIEW v_bd_app_re_all_kad_perceel AS
select to_char(re.sc_kad_identif) as app_re_identif, rar.perceel_identif from (
select * from v_bd_app_re_kad_perceel
union
select * from v_bd_app_re_2_kad_perceel
union
select * from v_bd_app_re_3_kad_perceel
) rar
left join app_re re 
on to_char(re.sc_kad_identif) = rar.app_re_identif;


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.4.5_naar_1.5.0','vorige versie was '||waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.5.0' WHERE naam='brmoversie';
