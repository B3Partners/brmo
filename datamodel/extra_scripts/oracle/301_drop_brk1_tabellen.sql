-- verwijder alle BRK1 1.0 tabellen en eventueel views die daarvan gebruik maken
-- in versie 5.0.0 is de BRK 1 ondersteuning verwijderd
-- de BRK 1.0 tabellen en views zijn niet meer nodig en kunnen verwijderd worden
BEGIN
    FOR i IN (SELECT column_value
              FROM table (SYS.DBMS_DEBUG_VC2COLL('MB_KAD_ONRRND_ZK_ARCHIEF',
                                                 'MB_AVG_ZR_RECHTH',
                                                 'MB_ZR_RECHTH',
                                                 'MB_PERCELENKAART',
                                                 'MB_UTIL_APP_RE_KAD_PERCEEL',
                                                 'MB_AVG_SUBJECT',
                                                 'MB_SUBJECT'))
        )
        LOOP
            BEGIN
                EXECUTE IMMEDIATE 'DELETE FROM user_sdo_geom_metadata WHERE table_name =''' || i.column_value || '''';
                EXECUTE IMMEDIATE 'DROP MATERIALIZED VIEW ' || i.column_value;
            EXCEPTION
                WHEN OTHERS THEN
                    IF SQLCODE != -12003 THEN
                        RAISE;
                    END IF;
            END;
        END LOOP;
END;
/

BEGIN
    FOR i IN (SELECT column_value
              FROM table (SYS.DBMS_DEBUG_VC2COLL('VB_UTIL_ZK_RECHT',
                                                 'VB_UTIL_APP_RE_PARENT',
                                                 'VB_UTIL_APP_RE_PARENT_2',
                                                 'VB_UTIL_APP_RE_PARENT_3',
                                                 'VB_UTIL_APP_RE_SPLITSING'))
        )
        LOOP
            BEGIN
                EXECUTE IMMEDIATE 'DELETE FROM user_sdo_geom_metadata WHERE table_name =''' || i.column_value || '''';
                EXECUTE IMMEDIATE 'DROP VIEW ' || i.column_value || ' CASCADE CONSTRAINTS';
            EXCEPTION
                WHEN OTHERS THEN
                    IF SQLCODE != -942 THEN
                        RAISE;
                    END IF;
            END;
        END LOOP;
END;
/

BEGIN
    FOR i IN (SELECT column_value
              FROM table (SYS.DBMS_DEBUG_VC2COLL('KAD_PERCEEL_ARCHIEF',
                                                 'KAD_ONRRND_ZK_KAD_ONRR_ARCHIEF',
                                                 'APP_RE_KAD_PERCEEL_ARCHIEF',
                                                 'APP_RE_ARCHIEF',
                                                 'ZAK_RECHT_ARCHIEF',
                                                 'ZAK_RECHT_AANTEK',
                                                 'KAD_ONRRND_ZK_KAD_ONRRND_ZK',
                                                 'KAD_ONRRND_ZK_AANTEK',
                                                 'APP_RE_KAD_PERCEEL',
                                                 'KAD_ONRRND_ZK_HIS_REL',
                                                 'KAD_ONRRND_ZK_ARCHIEF',
                                                 'KAD_ONRRND_ZK_AANTEK_ARCHIEF',
                                                 'BENOEMD_OBJ_KAD_ONRRND_ZK',
                                                 'ZAK_RECHT',
                                                 'APP_RE',
                                                 'KAD_PERCEEL',
                                                 'KAD_ONRRND_ZK',
                                                 'KAD_GEMEENTE',
                                                 'AARD_RECHT_VERKORT',
                                                 'AARD_VERKREGEN_RECHT'))
        )
        LOOP
            BEGIN
                EXECUTE IMMEDIATE 'DELETE FROM user_sdo_geom_metadata WHERE table_name =''' || i.column_value || '''';
                EXECUTE IMMEDIATE 'DROP TABLE ' || i.column_value || ' CASCADE CONSTRAINTS PURGE';
            EXCEPTION
                WHEN OTHERS THEN
                    IF SQLCODE != -942 THEN
                        RAISE;
                    END IF;
            END;
        END LOOP;
END;
/