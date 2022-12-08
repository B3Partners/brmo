-- 
-- upgrade PostgreSQL BRK datamodel van 2.3.3 naar 3.0.0
--

CREATE SCHEMA IF NOT EXISTS brk;

SET search_path = brk,public;


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.3.3_naar_3.0.0','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='3.0.0' WHERE naam='brmoversie';
