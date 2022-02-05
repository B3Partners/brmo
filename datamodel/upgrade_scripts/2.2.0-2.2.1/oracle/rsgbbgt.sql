-- 
-- upgrade Oracle RSGBBGT datamodel van 2.2.0 naar 2.2.1 
--

WHENEVER SQLERROR EXIT SQL.SQLCODE


DECLARE
BEGIN
  EXECUTE IMMEDIATE 'CREATE TABLE brmo_metadata(naam VARCHAR2(255 CHAR) NOT NULL,waarde CLOB,PRIMARY KEY (naam));';
  EXCEPTION WHEN OTHERS THEN
    IF SQLCODE = -955 THEN NULL; ELSE RAISE; END IF;
END;
MERGE INTO brmo_metadata USING DUAL ON (naam = 'brmoversie') WHEN NOT MATCHED THEN INSERT (naam) VALUES('brmoversie');

-- Update existing brmo_metadata table to use clob type for waarde
ALTER TABLE brmo_metadata ADD (temp clob);
UPDATE BRMO_METADATA SET temp = waarde;
ALTER TABLE brmo_metadata DROP COLUMN waarde;
ALTER TABLE brmo_metadata RENAME COLUMN temp to waarde;

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.2.0_naar_2.2.1','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.2.1' WHERE naam='brmoversie';
