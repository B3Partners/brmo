-- 
-- upgrade Oracle RSGB datamodel van 2.3.2 naar 2.3.3 
--

WHENEVER SQLERROR EXIT SQL.SQLCODE

-- https://b3partners.atlassian.net/browse/BRMO-209
ALTER TABLE LOCAAND_OPENB_RMTE DROP CONSTRAINT FK_LOCAAND_OPENB_RMTE_SC_LH;

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.3.2_naar_2.3.3','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.3.3' WHERE naam='brmoversie';
