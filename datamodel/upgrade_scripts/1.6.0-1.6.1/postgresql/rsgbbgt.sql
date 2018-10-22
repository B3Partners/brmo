-- 
-- upgrade PostgreSQL RSGBBGT datamodel van 1.6.0 naar 1.6.1 
--
-- herstel brmo versie metadata, zie: #543
INSERT INTO brmo_metadata (naam, waarde) SELECT 'brmoversie', '1.6.0'
            WHERE NOT EXISTS (SELECT naam FROM brmo_metadata WHERE naam = 'brmoversie');
COMMIT;

-- voeg tabel voor klasse Stadsdeel toe
-- #546
CREATE TABLE stadsdeel (
        identif character varying(255) NOT NULL,
        dat_beg_geldh date,
        tijdstip_registratie timestamp,
        relve_hoogteligging integer,
        bgt_status character varying(255),
        plus_status character varying(255),
        naam character varying(255),
        geom2d geometry(MULTIPOLYGON,28992),
        PRIMARY KEY (identif)
);

CREATE INDEX stadsdeel_geom2d_idx on stadsdeel USING GIST (geom2d);

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.6.0_naar_1.6.1','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.1' WHERE naam='brmoversie';
