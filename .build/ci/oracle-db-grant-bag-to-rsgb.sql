BEGIN
    FOR t IN (SELECT * FROM USER_TABLES)
    LOOP
        EXECUTE IMMEDIATE 'GRANT SELECT ON ' || t.table_name || ' TO JENKINS_RSGB';
    END LOOP;
END;
/

BEGIN
    FOR t IN (SELECT * FROM USER_SEQUENCES)
    LOOP
        EXECUTE IMMEDIATE 'GRANT SELECT ON ' || t.sequence_name || ' TO JENKINS_RSGB';
    END LOOP;
END;
/