--
-- Maak en vul de meta tabel GEOMETRY_COLUMNS ten behoeve van Geoserver/Geotools
-- 
-- NB let op de schema naam 'RSGBTEST' in dit bestand; die dient vervangen te worden
-- NB de lijst views is mogelijk niet compleet, afhankelijk van installatie
--

CREATE TABLE
    GEOMETRY_COLUMNS
    (
        F_TABLE_SCHEMA VARCHAR(30) NOT NULL,
        F_TABLE_NAME VARCHAR(30) NOT NULL,
        F_GEOMETRY_COLUMN VARCHAR(30) NOT NULL,
        COORD_DIMENSION INTEGER,
        SRID INTEGER NOT NULL,
        TYPE VARCHAR(30) NOT NULL,
        UNIQUE(F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN),
        CHECK(TYPE IN ('POINT',
                       'LINE',
                       'POLYGON',
                       'COLLECTION',
                       'MULTIPOINT',
                       'MULTILINE',
                       'MULTIPOLYGON',
                       'GEOMETRY'))
    );

COMMENT ON TABLE GEOMETRY_COLUMNS IS 'Geometry metadata tabel ten behoeve van Geoserver/Geotools';


-- V_ADRES
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_ADRES', 'THE_GEOM', 2, 28992, 'POINT');

-- V_ADRES_LIGPLAATS
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_ADRES_LIGPLAATS', 'THE_GEOM', 2, 28992, 'MULTIPOLYGON');
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_ADRES_LIGPLAATS', 'CENTROIDE', 2, 28992, 'POINT');
    
-- V_ADRES_STANDPLAATS
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_ADRES_STANDPLAATS', 'THE_GEOM', 2, 28992, 'MULTIPOLYGON');
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_ADRES_STANDPLAATS', 'CENTROIDE', 2, 28992, 'POINT');

-- V_ADRES_TOTAAL
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_ADRES_TOTAAL', 'THE_GEOM', 2, 28992, 'POINT');

-- V_BD_APP_RE_BIJ_PERCEEL
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_BD_APP_RE_BIJ_PERCEEL', 'BEGRENZING_PERCEEL', 2, 28992, 'MULTIPOLYGON');

-- V_BD_KAD_PERCEEL_MET_APP
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_BD_KAD_PERCEEL_MET_APP', 'BEGRENZING_PERCEEL', 2, 28992, 'MULTIPOLYGON');
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_BD_KAD_PERCEEL_MET_APP', 'PLAATSCOORDINATEN_PERCEEL', 2, 28992, 'POINT');

-- V_KAD_PERCEEL_EENVOUDIG
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_KAD_PERCEEL_EENVOUDIG', 'BEGRENZING_PERCEEL', 2, 28992, 'MULTIPOLYGON');

-- V_KAD_PERCEEL_IN_EIGENDOM
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_KAD_PERCEEL_IN_EIGENDOM', 'BEGRENZING_PERCEEL', 2, 28992, 'MULTIPOLYGON');

-- V_KAD_PERCEEL_ZR_ADRESSEN
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_KAD_PERCEEL_ZR_ADRESSEN', 'BEGRENZING_PERCEEL', 2, 28992, 'MULTIPOLYGON');

-- V_LIGPLAATS
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_LIGPLAATS', 'GEOMETRIE', 2, 28992, 'MULTIPOLYGON');

-- V_MAP_KAD_PERCEEL
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_MAP_KAD_PERCEEL', 'BEGRENZING_PERCEEL', 2, 28992, 'MULTIPOLYGON');

-- V_PAND_GEBRUIK_NIET_INGEMETEN
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_PAND_GEBRUIK_NIET_INGEMETEN', 'THE_GEOM', 2, 28992, 'MULTIPOLYGON');

-- V_PAND_IN_GEBRUIK
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_PAND_IN_GEBRUIK', 'THE_GEOM', 2, 28992, 'MULTIPOLYGON');

-- V_STANDPLAATS
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_STANDPLAATS', 'GEOMETRIE', 2, 28992, 'MULTIPOLYGON');

-- V_VERBLIJFSOBJECT
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_VERBLIJFSOBJECT', 'THE_GEOM', 2, 28992, 'POINT');

-- V_VERBLIJFSOBJECT_ALLES
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_VERBLIJFSOBJECT_ALLES', 'THE_GEOM', 2, 28992, 'POINT');

-- V_VERBLIJFSOBJECT_GEVORMD
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_VERBLIJFSOBJECT_GEVORMD', 'THE_GEOM', 2, 28992, 'POINT');

-- VM_KAD_EIGENARENKAART
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'VM_KAD_EIGENARENKAART', 'BEGRENZING_PERCEEL', 2, 28992, 'MULTIPOLYGON');
    
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_BD_KAD_PERCEEL_MET_APP_VLAK', 'BEGRENZING_PERCEEL', 2, 28992, 'MULTIPOLYGON');

-- adres vlakken    
INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_ADRES_PANDVLAK', 'THE_GEOM', 2, 28992, 'MULTIPOLYGON');

INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
    VALUES ('RSGBTEST', 'V_ADRES_TOTAAL_VLAK', 'THE_GEOM', 2, 28992, 'MULTIPOLYGON');
