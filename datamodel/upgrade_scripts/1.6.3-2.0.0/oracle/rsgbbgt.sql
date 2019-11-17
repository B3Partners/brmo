-- 
-- upgrade Oracle RSGBBGT datamodel van 1.6.3 naar 2.0.0
--

-- Klasse: Waterschap
CREATE TABLE waterschap (
        identif VARCHAR2(255) NOT NULL,
        dat_beg_geldh date,
        tijdstip_registratie timestamp,
        relve_hoogteligging NUMBER(10),
        bgt_status VARCHAR2(255),
        plus_status VARCHAR2(255),
        NAAM VARCHAR2(255),
        GEOM2D MDSYS.SDO_GEOMETRY,
        PRIMARY KEY (identif)
);

INSERT INTO GT_PK_METADATA VALUES ('BRMO_RSGBBGT', 'WATERSCHAP', 'IDENTIF', NULL, 'assigned', NULL);
INSERT INTO USER_SDO_GEOM_METADATA VALUES('WATERSCHAP', 'GEOM2D',
        MDSYS.SDO_DIM_ARRAY(
            MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
            MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)
        ), 28992);
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE)
            VALUES ('BRMO_RSGBBGT','WATERSCHAP', 'GEOM2D', 2, 28992,'MULTIPOLYGON');
CREATE INDEX WATERSCHAP_GEOM2D_IDX ON WATERSCHAP (GEOM2D)
        INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ('SDO_INDX_DIMS=2 LAYER_GTYPE=MULTIPOLYGON');



-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.6.3_naar_2.0.0','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.0.0' WHERE naam='brmoversie';
