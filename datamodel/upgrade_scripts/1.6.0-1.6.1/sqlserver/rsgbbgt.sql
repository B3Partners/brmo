-- 
-- upgrade SQLserver RSGBBGT datamodel van 1.6.0 naar 1.6.1 
--

-- voeg tabel voor klasse Stadsdeel toe
CREATE TABLE stadsdeel (
        identif varchar(255) NOT NULL,
        dat_beg_geldh date,
        tijdstip_registratie datetime,
        relve_hoogteligging integer,
        bgt_status varchar(255),
        plus_status varchar(255),
        naam varchar(255),
        geom2d geometry,
        PRIMARY KEY (identif)
);

CREATE SPATIAL INDEX stadsdeel_geom2d_idx ON stadsdeel(geom2d) WITH ( BOUNDING_BOX = (12000,304000,280000,620000));

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.6.0_naar_1.6.1','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.1' WHERE naam='brmoversie';
