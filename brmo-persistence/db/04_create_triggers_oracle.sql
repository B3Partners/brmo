-- create triggers om (conditioneel) een id uit de sequence te halen
-- fix voor issue #86
CREATE OR REPLACE TRIGGER LAADPROCES_INSERT_TRIGGER
        BEFORE INSERT ON LAADPROCES
        FOR EACH ROW
BEGIN
    IF :new.ID IS NULL THEN
                SELECT LAADPROCES_ID_SEQ.nextval INTO :new.ID FROM DUAL;
    END IF;
END;



CREATE OR REPLACE TRIGGER BERICHT_INSERT_TRIGGER
        BEFORE INSERT ON BERICHT
        FOR EACH ROW
BEGIN
    IF :new.ID IS NULL THEN
                SELECT BERICHT_ID_SEQ.nextval INTO :new.ID FROM DUAL;
    END IF;
END;
