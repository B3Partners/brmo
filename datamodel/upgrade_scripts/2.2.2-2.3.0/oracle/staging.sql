-- 
-- upgrade Oracle STAGING datamodel van 2.2.2 naar 2.3.0 
--

WHENEVER SQLERROR EXIT SQL.SQLCODE

    create table nhr_laadproces (
        datum timestamp,
        laatst_geprobeerd timestamp,
        volgend_proberen timestamp,
        probeer_aantal number(19, 0),
        kvk_nummer varchar2(255 char) not null,
        exception clob,
        primary key (kvk_nummer)
    );

create index idx_nhr_laadproces_volgend_proberen on nhr_laadproces(volgend_proberen);

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.2.2_naar_2.3.0','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.3.0' WHERE naam='brmoversie';
