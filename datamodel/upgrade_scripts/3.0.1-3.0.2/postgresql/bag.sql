-- 
-- upgrade PostgreSQL BAG datamodel van 3.0.1 naar 3.0.2 
--

CREATE SCHEMA IF NOT EXISTS bag;

SET search_path = bag,public;


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_3.0.1_naar_3.0.2','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='3.0.2' WHERE naam='brmoversie';
