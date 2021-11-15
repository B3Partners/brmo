-- 
-- upgrade SQLserver RSGBBGT datamodel van 2.1.0 naar 2.2.0-rc1
--

IF OBJECT_ID('brmo_metadata', 'U') IS NULL
CREATE TABLE brmo_metadata(naam VARCHAR(255) NOT NULL,waarde VARCHAR(255),PRIMARY KEY (naam));
GO
INSERT INTO brmo_metadata(naam) SELECT naam FROM brmo_metadata WHERE NOT('brmoversie' IN (SELECT naam FROM brmo_metadata));

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.1.0_naar_2.2.0-rc1','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.2.0-rc1' WHERE naam='brmoversie';
