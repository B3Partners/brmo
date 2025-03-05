-- 
-- upgrade Oracle BRK datamodel van 5.0.1 naar 5.0.2 
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

DECLARE
    v_constraint_name VARCHAR2(255);
BEGIN
    SELECT c.constraint_name
    INTO v_constraint_name
    FROM all_cons_columns col
             JOIN all_constraints c
                  ON col.constraint_name = c.constraint_name
    WHERE col.table_name = 'APPARTEMENTSRECHT_ARCHIEF'
      AND col.column_name = 'HOOFDSPLITSING'
      AND c.constraint_type = 'R';
    EXECUTE IMMEDIATE 'ALTER TABLE APPARTEMENTSRECHT_ARCHIEF DROP CONSTRAINT ' || v_constraint_name;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        DBMS_OUTPUT.PUT_LINE('Geen constraint gevonden voor appartementsrecht_archief.hoofdsplitsing');
END;
/

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_5.0.1_naar_5.0.2','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='5.0.2' WHERE naam='brmoversie';
