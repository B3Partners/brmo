-- 
-- upgrade PostgreSQL BRK datamodel van 4.0.0 naar 4.0.1 
--

CREATE SCHEMA IF NOT EXISTS brk;

SET search_path = brk,public;

SET SCHEMA 'brk';


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_4.0.0_naar_4.0.1','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='4.0.1' WHERE naam='brmoversie';