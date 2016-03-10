-- create triggers om (conditioneel) een id uit de sequence te halen

CREATE OR REPLACE TRIGGER JOB_INSERT_TRIGGER
        BEFORE INSERT ON JOB
        FOR EACH ROW
BEGIN
    IF :new.JID IS NULL THEN
                SELECT JOB_JID_SEQ.nextval INTO :new.JID FROM DUAL;
    END IF;
END;
