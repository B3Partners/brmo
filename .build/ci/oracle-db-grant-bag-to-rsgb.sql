BEGIN
    FOR t IN (SELECT * FROM USER_VIEWS)
        LOOP
            EXECUTE IMMEDIATE 'GRANT SELECT ON ' || t.view_name || ' TO JENKINS_RSGB';
        END LOOP;
END;
/
