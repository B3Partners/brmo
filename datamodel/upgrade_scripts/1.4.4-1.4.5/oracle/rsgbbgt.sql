-- 
-- upgrade Oracle RSGBBGT datamodel van 1.4.4 naar 1.4.5 
--

-- versienummer update
UPDATE brmo_metadata SET waarde='1.4.5' WHERE naam='brmoversie';

-- 
-- LET OP: onderstaande gaat er voor de insert van de metadata (in GT_PK_METADATA 
--         en GEOMETRY_COLUMNS tabellen) van uit dat het schema de naam BRMO_RSGBBGT heeft.
--         In geval van een andere naam dient dat te worden aangepast!

-- Klasse: Buurt
CREATE TABLE buurt (
        identif VARCHAR2(255) NOT NULL,
        dat_beg_geldh date,
        datum_einde_geldh date,
        relve_hoogteligging NUMBER(10),
        bgt_status VARCHAR2(255),
        plus_status VARCHAR2(255),
        BUURTCODE VARCHAR2(255),
        NAAM VARCHAR2(255),
        GEOM2D MDSYS.SDO_GEOMETRY,
        WIJK VARCHAR2(255),
        bijwerkdatum date,
        PRIMARY KEY (identif)
);

INSERT INTO GT_PK_METADATA VALUES ('BRMO_RSGBBGT', 'BUURT', 'IDENTIF', NULL, 'assigned', NULL);
INSERT INTO USER_SDO_GEOM_METADATA VALUES('BUURT', 'GEOM2D',
        MDSYS.SDO_DIM_ARRAY(
            MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
            MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)
        ), 28992);            
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE)
            VALUES ('BRMO_RSGBBGT','BUURT', 'GEOM2D', 2, 28992,'MULTIPOLYGON');
CREATE INDEX BUURT_GEOM2D_IDX ON BUURT (GEOM2D)
        INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ('SDO_INDX_DIMS=2 LAYER_GTYPE=MULTIPOLYGON');

-- Klasse: Wijk
CREATE TABLE wijk (
        identif VARCHAR2(255) NOT NULL,
        dat_beg_geldh date,
        datum_einde_geldh date,
        relve_hoogteligging NUMBER(10),
        bgt_status VARCHAR2(255),
        plus_status VARCHAR2(255),
        WIJKCODE VARCHAR2(255),
        NAAM VARCHAR2(255),
        GEOM2D MDSYS.SDO_GEOMETRY,
        STADSDEEL VARCHAR2(255),
        bijwerkdatum date,
        PRIMARY KEY (identif)
);

INSERT INTO GT_PK_METADATA VALUES ('BRMO_RSGBBGT', 'WIJK', 'IDENTIF', NULL, 'assigned', NULL);
INSERT INTO USER_SDO_GEOM_METADATA VALUES('WIJK', 'GEOM2D',
        MDSYS.SDO_DIM_ARRAY(
            MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
            MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)
        ), 28992);            
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE)
            VALUES ('BRMO_RSGBBGT','WIJK', 'GEOM2D', 2, 28992,'MULTIPOLYGON');
CREATE INDEX WIJK_GEOM2D_IDX ON WIJK (GEOM2D)
        INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ('SDO_INDX_DIMS=2 LAYER_GTYPE=MULTIPOLYGON');

-- Klasse: OpenbareRuimte
CREATE TABLE openbareruimte (
        identif VARCHAR2(255) NOT NULL,
        dat_beg_geldh date,
        datum_einde_geldh date,
        relve_hoogteligging NUMBER(10),
        bgt_status VARCHAR2(255),
        plus_status VARCHAR2(255),
        NAAM VARCHAR2(255),
        NAAM_ID_OPR VARCHAR2(255),
        GEOM2D MDSYS.SDO_GEOMETRY,
        bijwerkdatum date,
        PRIMARY KEY (identif)
);

INSERT INTO GT_PK_METADATA VALUES ('BRMO_RSGBBGT', 'OPENBARERUIMTE', 'IDENTIF', NULL, 'assigned', NULL);
INSERT INTO USER_SDO_GEOM_METADATA VALUES('OPENBARERUIMTE', 'GEOM2D',
        MDSYS.SDO_DIM_ARRAY(
            MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
            MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)
        ), 28992);            
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE)
            VALUES ('BRMO_RSGBBGT','OPENBARERUIMTE', 'GEOM2D', 2, 28992,'MULTIPOLYGON');
CREATE INDEX OPENBARERUIMTE_GEOM2D_IDX ON OPENBARERUIMTE (GEOM2D)
        INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ('SDO_INDX_DIMS=2 LAYER_GTYPE=MULTIPOLYGON');
