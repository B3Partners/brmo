-- 
-- upgrade Oracle RSGBBGT datamodel van 2.3.0 naar 2.3.1 
--

WHENEVER SQLERROR EXIT SQL.SQLCODE
BEGIN
    EXECUTE IMMEDIATE 'CREATE TABLE brmo_metadata(naam VARCHAR2(255 CHAR) NOT NULL, waarde CLOB, PRIMARY KEY (naam))';
EXCEPTION
WHEN OTHERS THEN
IF
    SQLCODE = -955 THEN
    NULL;
ELSE RAISE;
END IF;
END;
/
MERGE INTO brmo_metadata USING DUAL ON (naam = 'brmoversie') WHEN NOT MATCHED THEN INSERT (naam) VALUES('brmoversie');


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.3.0_naar_2.3.1','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.3.1' WHERE naam='brmoversie';
