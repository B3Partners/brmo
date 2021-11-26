-- 
-- upgrade Oracle RSGBBGT datamodel van 2.1.0 naar 2.2.0
--

WHENEVER SQLERROR EXIT SQL.SQLCODE

DECLARE
BEGIN
    EXECUTE IMMEDIATE 'CREATE TABLE brmo_metadata(naam VARCHAR2(255 CHAR) NOT NULL,waarde VARCHAR2(255 CHAR),PRIMARY KEY (naam));';
    EXCEPTION WHEN OTHERS THEN
        IF SQLCODE = -955 THEN NULL; ELSE RAISE; END IF;
END;


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.1.0_naar_2.2.0','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.2.0' WHERE naam='brmoversie';
