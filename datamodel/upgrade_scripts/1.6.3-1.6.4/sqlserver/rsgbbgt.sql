-- 
-- upgrade SQLserver RSGBBGT datamodel van 1.6.3 naar 1.6.4 
--

-- Klasse: Waterschap
CREATE TABLE waterschap (
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

CREATE SPATIAL INDEX waterschap_geom2d_idx ON waterschap(geom2d) WITH ( BOUNDING_BOX = (12000,304000,280000,620000));


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.6.3_naar_1.6.4','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.4' WHERE naam='brmoversie';
