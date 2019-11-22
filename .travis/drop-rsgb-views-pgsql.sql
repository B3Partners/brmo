-- drop (materialized) views uit schema
DO
$m$
DECLARE
    _m varchar;
DECLARE
    _v varchar;
BEGIN
    FOR _m IN ( SELECT oid::regclass::text FROM pg_class WHERE relkind = 'm' AND oid::regclass::text LIKE 'mb_%' )
    LOOP
        RAISE NOTICE 'DROP M-VIEW %',_m;
        EXECUTE 'DROP MATERIALIZED VIEW IF EXISTS ' || _m || ' CASCADE';
    END LOOP;

    FOR _v IN ( SELECT oid::regclass::text FROM pg_class WHERE relkind = 'v' AND oid::regclass::text LIKE 'vb_%' )
    LOOP
        RAISE NOTICE 'DROP VIEW %',_v;
        EXECUTE 'DROP VIEW IF EXISTS ' || _v || ' CASCADE';
    END LOOP;
END
$m$
