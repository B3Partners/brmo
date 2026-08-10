-- 
-- upgrade PostgreSQL BRK datamodel van 6.0.2 naar 6.0.3 
--

CREATE SCHEMA IF NOT EXISTS brk;

SET search_path = brk,public;

SET SCHEMA 'brk';


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_6.0.2_naar_6.0.3','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='6.0.3' WHERE naam='brmoversie';
