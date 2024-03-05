-- verwijder alle bag 1.0 tabellen en eventueel views die daarvan gebruik maken
-- in versie 4.0.0 is de BAG 1 ondersteuning verwijderd
-- de BAG 1.0 tabellen en views zijn niet meer nodig en kunnen verwijderd worden
BEGIN
    FOR i IN (SELECT column_value
              FROM table (SYS.DBMS_DEBUG_VC2COLL('MB_KAD_ONRRND_ZK_ADRES',
                                                 'MB_KOZ_RECHTH',
                                                 'MB_AVG_KOZ_RECHTH',
                                                 'MB_BEN_OBJ_NEVENADRES',
                                                 'MB_BENOEMD_OBJ_ADRES',
                                                 'MB_PAND',
                                                 'MB_ADRES'))
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
              FROM table (SYS.DBMS_DEBUG_VC2COLL('VB_LIGPLAATS_ADRES',
                                                 'VB_STANDPLAATS_ADRES',
                                                 'VB_VBO_ADRES'))
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
              FROM table (SYS.DBMS_DEBUG_VC2COLL('WNPLTS_ARCHIEF',
                                                 'WNPLTS',
                                                 'VERBLIJFSOBJ_PAND_ARCHIEF',
                                                 'VERBLIJFSOBJ_PAND',
                                                 'VERBLIJFSOBJ_NUMMERAAND',
                                                 'VERBLIJFSOBJ_NUMMERAAN_ARCHIEF',
                                                 'VERBLIJFSOBJ_ARCHIEF',
                                                 'VERBLIJFSOBJ',
                                                 'STANDPLAATS_NUMMERAAND_ARCHIEF',
                                                 'STANDPLAATS_NUMMERAAND',
                                                 'STANDPLAATS_ARCHIEF',
                                                 'STANDPLAATS',
                                                 'PAND_ARCHIEF',
                                                 'PAND',
                                                 'OVRG_ADDRESSEERB_OBJ_AAND',
                                                 'OVRG_ADDRESSEERB_OBJ_A_ARCHIEF',
                                                 'OVERIG_TERREIN_GEBRUIKSDOEL',
                                                 'OVERIG_TERREIN_ARCHIEF',
                                                 'OVERIG_TERREIN',
                                                 'OVERIG_GEBOUWD_OBJ_ARCHIEF',
                                                 'OVERIG_GEBOUWD_OBJ',
                                                 'OVERIG_BOUWWERK_ARCHIEF',
                                                 'OVERIG_BOUWWERK',
                                                 'OPENB_RMTE_WNPLTS',
                                                 'OPENB_RMTE_GEM_OPENB_RMTE',
                                                 'OPENB_RMTE',
                                                 'NUMMERAAND_ARCHIEF',
                                                 'NUMMERAAND',
                                                 'LIGPLAATS_NUMMERAAND_ARCHIEF',
                                                 'LIGPLAATS_NUMMERAAND',
                                                 'LIGPLAATS_ARCHIEF',
                                                 'LIGPLAATS',
                                                 'GEM_OPENB_RMTE_ARCHIEF',
                                                 'GEM_OPENB_RMTE',
                                                 'GEBOUWD_OBJ_GEBRUIKSDOEL',
                                                 'GEBOUWD_OBJ_ARCHIEF',
                                                 'GEBOUWD_OBJ',
                                                 'BENOEMD_TERREIN_BENOEMD_TERREI',
                                                 'BENOEMD_TERREIN_BENOEM_ARCHIEF',
                                                 'BENOEMD_TERREIN_ARCHIEF',
                                                 'BENOEMD_TERREIN',
                                                 'BENOEMD_OBJ',
                                                 'ADDRESSEERB_OBJ_AAND_ARCHIEF',
                                                 'ADDRESSEERB_OBJ_AAND'))
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

