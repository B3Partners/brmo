-- drop (materialized) views uit schema
DELETE FROM user_sdo_geom_metadata WHERE table_name LIKE 'MB_%' OR table_name LIKE 'VB_%';
BEGIN
    FOR cur_rec IN (
            SELECT object_name, object_type FROM user_objects
                WHERE
                    object_type IN ('VIEW','MATERIALIZED VIEW')
                AND (object_name LIKE 'MB_%' OR object_name LIKE 'VB_%')
            )
    LOOP
        BEGIN
            EXECUTE IMMEDIATE   'DROP '
                                  || cur_rec.object_type
                                  || ' "'
                                  || cur_rec.object_name
                                  || '"';
        EXCEPTION
            WHEN OTHERS
            THEN
                DBMS_OUTPUT.put_line ('FAILED: DROP '
                                      || cur_rec.object_type
                                      || ' "'
                                      || cur_rec.object_name
                                      || '"'
                                    );
        END;
    END LOOP;
END;
/
COMMIT;
PURGE RECYCLEBIN;
COMMIT;
