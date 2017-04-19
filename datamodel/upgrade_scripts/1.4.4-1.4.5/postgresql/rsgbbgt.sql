-- 
-- upgrade PostgreSQL RSGBBGT datamodel van 1.4.4 naar 1.4.5 
--

-- versienummer update
UPDATE brmo_metadata SET waarde='1.4.5' WHERE naam='brmoversie';

-- Klasse: Buurt
CREATE TABLE buurt (
        identif character varying(255) NOT NULL,
        dat_beg_geldh date,
        datum_einde_geldh date,
        relve_hoogteligging integer,
        bgt_status character varying(255),
        plus_status character varying(255),
        buurtcode character varying(255),
        naam character varying(255),
        geom2d geometry(MULTIPOLYGON,28992),
        wijk character varying(255),
        bijwerkdatum date,
        PRIMARY KEY (identif)
);

CREATE INDEX buurt_geom2d_idx on buurt USING GIST (geom2d);

-- Klasse: Wijk
CREATE TABLE wijk (
        identif character varying(255) NOT NULL,
        dat_beg_geldh date,
        datum_einde_geldh date,
        relve_hoogteligging integer,
        bgt_status character varying(255),
        plus_status character varying(255),
        wijkcode character varying(255),
        naam character varying(255),
        geom2d geometry(MULTIPOLYGON,28992),
        stadsdeel character varying(255),
        bijwerkdatum date,
        PRIMARY KEY (identif)
);

CREATE INDEX wijk_geom2d_idx on wijk USING GIST (geom2d);

-- Klasse: OpenbareRuimte
CREATE TABLE openbareruimte (
        identif character varying(255) NOT NULL,
        dat_beg_geldh date,
        datum_einde_geldh date,
        relve_hoogteligging integer,
        bgt_status character varying(255),
        plus_status character varying(255),
        naam character varying(255),
        naam_id_opr character varying(255),
        geom2d geometry(MULTIPOLYGON,28992),
        bijwerkdatum date,
        PRIMARY KEY (identif)
);

CREATE INDEX openbareruimte_geom2d_idx on openbareruimte USING GIST (geom2d);
