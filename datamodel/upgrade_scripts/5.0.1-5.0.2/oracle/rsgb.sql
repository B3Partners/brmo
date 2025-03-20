-- 
-- upgrade Oracle RSGB datamodel van 5.0.1 naar 5.0.2 
--

WHENEVER SQLERROR EXIT SQL.SQLCODE

alter table brmo_metadata add (waarde_clob clob);
update brmo_metadata set waarde_clob = waarde;
alter table brmo_metadata drop column waarde;
alter table brmo_metadata rename column waarde_clob to waarde;

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_5.0.1_naar_5.0.2','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='5.0.2' WHERE naam='brmoversie';
