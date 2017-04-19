-- 
-- upgrade SQLserver RSGBBGT datamodel van 1.4.4 naar 1.4.5 
--

-- versienummer update
UPDATE brmo_metadata SET waarde='1.4.5' WHERE naam='brmoversie';

-- Klasse: Buurt
CREATE TABLE buurt (
        identif varchar(255) NOT NULL,
        dat_beg_geldh date,
        datum_einde_geldh date,
        relve_hoogteligging integer,
        bgt_status varchar(255),
        plus_status varchar(255),
        buurtcode varchar(255),
        naam varchar(255),
        geom2d geometry,
        wijk varchar(255),
        bijwerkdatum date,
        PRIMARY KEY (identif)
);

CREATE SPATIAL INDEX buurt_geom2d_idx ON buurt(geom2d) WITH ( BOUNDING_BOX = (12000,304000,280000,620000));

-- Klasse: Wijk
CREATE TABLE wijk (
        identif varchar(255) NOT NULL,
        dat_beg_geldh date,
        datum_einde_geldh date,
        relve_hoogteligging integer,
        bgt_status varchar(255),
        plus_status varchar(255),
        wijkcode varchar(255),
        naam varchar(255),
        geom2d geometry,
        stadsdeel varchar(255),
        bijwerkdatum date,
        PRIMARY KEY (identif)
);

CREATE SPATIAL INDEX wijk_geom2d_idx ON wijk(geom2d) WITH ( BOUNDING_BOX = (12000,304000,280000,620000));

-- Klasse: OpenbareRuimte
CREATE TABLE openbareruimte (
        identif varchar(255) NOT NULL,
        dat_beg_geldh date,
        datum_einde_geldh date,
        relve_hoogteligging integer,
        bgt_status varchar(255),
        plus_status varchar(255),
        naam varchar(255),
        naam_id_opr varchar(255),
        geom2d geometry,
        bijwerkdatum date,
        PRIMARY KEY (identif)
);

CREATE SPATIAL INDEX openbareruimte_geom2d_idx ON openbareruimte(geom2d) WITH ( BOUNDING_BOX = (12000,304000,280000,620000));
