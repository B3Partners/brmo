-- 
-- upgrade Oracle BAG datamodel van 2.3.0 naar 2.3.1 
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

WHENEVER SQLERROR EXIT SQL.SQLCODE
BEGIN
EXECUTE IMMEDIATE 'alter table ligplaats modify (objectid drop identity)';
EXECUTE IMMEDIATE 'alter table nummeraanduiding modify (objectid drop identity)';
EXECUTE IMMEDIATE 'alter table openbareruimte modify (objectid drop identity)';
EXECUTE IMMEDIATE 'alter table pand modify (objectid drop identity)';
EXECUTE IMMEDIATE 'alter table standplaats modify (objectid drop identity)';
EXECUTE IMMEDIATE 'alter table verblijfsobject modify (objectid drop identity)';
EXECUTE IMMEDIATE 'alter table woonplaats modify (objectid drop identity)';
EXECUTE IMMEDIATE 'alter table ligplaats modify objectid integer default objectid_seq.nextval';
EXECUTE IMMEDIATE 'alter table nummeraanduiding modify objectid integer default objectid_seq.nextval';
EXECUTE IMMEDIATE 'alter table openbareruimte modify objectid integer default objectid_seq.nextval';
EXECUTE IMMEDIATE 'alter table pand modify objectid integer default objectid_seq.nextval';
EXECUTE IMMEDIATE 'alter table standplaats modify objectid integer default objectid_seq.nextval';
EXECUTE IMMEDIATE 'alter table verblijfsobject modify objectid integer default objectid_seq.nextval';
EXECUTE IMMEDIATE 'alter table woonplaats modify objectid integer default objectid_seq.nextval';
EXECUTE IMMEDIATE 'create sequence objectid_seq';
EXECUTE IMMEDIATE 'update ligplaats set objectid = objectid_seq.nextval';
EXECUTE IMMEDIATE 'update nummeraanduiding set objectid = objectid_seq.nextval';
EXECUTE IMMEDIATE 'update openbareruimte set objectid = objectid_seq.nextval';
EXECUTE IMMEDIATE 'update pand set objectid = objectid_seq.nextval';
EXECUTE IMMEDIATE 'update standplaats set objectid = objectid_seq.nextval';
EXECUTE IMMEDIATE 'update verblijfsobject set objectid = objectid_seq.nextval';
EXECUTE IMMEDIATE 'update woonplaats set objectid = objectid_seq.nextval';
EXCEPTION
WHEN OTHERS THEN
IF
    SQLCODE = -942 THEN
    NULL;
ELSE RAISE;
END IF;
END;
/

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.3.0_naar_2.3.1','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.3.1' WHERE naam='brmoversie';
