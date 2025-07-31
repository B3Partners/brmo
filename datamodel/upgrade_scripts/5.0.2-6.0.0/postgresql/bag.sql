-- 
-- upgrade PostgreSQL BAG datamodel van 5.0.2 naar 6.0.0
--

CREATE SCHEMA IF NOT EXISTS bag;

SET search_path = bag,public;


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_5.0.2_naar_6.0.0','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='6.0.0' WHERE naam='brmoversie';
