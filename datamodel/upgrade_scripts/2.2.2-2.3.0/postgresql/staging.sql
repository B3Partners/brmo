-- 
-- upgrade PostgreSQL STAGING datamodel van 2.2.2 naar 2.3.0 
--

    CREATE TABLE nhr_laadproces (
        datum TIMESTAMP(6) WITHOUT TIME ZONE,
        laatst_geprobeerd TIMESTAMP(6) WITHOUT TIME ZONE,
        volgend_proberen TIMESTAMP(6) WITHOUT TIME ZONE,
        probeer_aantal INTEGER,
        kvk_nummer text not null,
        exception text,
        PRIMARY KEY (kvk_nummer)
    );

    create index idx_nhr_laadproces_volgend_proberen on nhr_laadproces(volgend_proberen);


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.2.2_naar_2.3.0','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.3.0' WHERE naam='brmoversie';
