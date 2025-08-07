-- 
-- upgrade PostgreSQL BAG datamodel van 6.0.0 naar 6.0.1 
--

CREATE SCHEMA IF NOT EXISTS bag;

SET search_path = bag,public;


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_6.0.0_naar_6.0.1','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='6.0.1' WHERE naam='brmoversie';
