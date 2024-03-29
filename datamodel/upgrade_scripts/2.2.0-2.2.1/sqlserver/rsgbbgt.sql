-- 
-- upgrade SQLserver RSGBBGT datamodel van 2.2.0 naar 2.2.1 
--

IF OBJECT_ID('brmo_metadata', 'U') IS NULL
CREATE TABLE brmo_metadata(naam VARCHAR(255) NOT NULL,waarde NTEXT,PRIMARY KEY (naam));
GO
ALTER TABLE brmo_metadata ALTER COLUMN waarde NTEXT;

INSERT INTO brmo_metadata(naam) SELECT naam FROM brmo_metadata WHERE NOT('brmoversie' IN (SELECT naam FROM brmo_metadata));


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.2.0_naar_2.2.1','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.2.1' WHERE naam='brmoversie';
