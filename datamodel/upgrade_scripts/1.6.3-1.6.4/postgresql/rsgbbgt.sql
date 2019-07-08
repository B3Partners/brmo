-- 
-- upgrade PostgreSQL RSGBBGT datamodel van 1.6.3 naar 1.6.4 
--
-- Klasse: Waterschap
CREATE TABLE waterschap (
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

CREATE INDEX waterschap_geom2d_idx on waterschap USING GIST (geom2d);

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.6.3_naar_1.6.4','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.4' WHERE naam='brmoversie';
