--
-- maakt een tweetal metadata tabellen (gt_pk_metadata en geometry_columns) 
-- aan voor geotools/geoserver en vult deze.
--

-- primary key metadata tabel
CREATE TABLE gt_pk_metadata (
    table_schema  VARCHAR2(32) NOT NULL,
    table_name    VARCHAR2(32) NOT NULL,
    pk_column     VARCHAR2(32) NOT NULL,
    pk_column_idx NUMBER(38),
    pk_policy     VARCHAR2(32),
    pk_sequence   VARCHAR2(64),
    CONSTRAINT chk_pk_policy CHECK (pk_policy IN ('sequence', 'assigned', 'autoincrement'))
  );
CREATE UNIQUE INDEX gt_pk_metadata_table_idx01 ON gt_pk_metadata (table_schema, table_name, pk_column);
COMMENT ON TABLE gt_pk_metadata IS 'Primary key metadata tabel ten behoeve van Geoserver/Geotools';

-- geometrie metadata tabel
CREATE TABLE geometry_columns (
    F_TABLE_SCHEMA    VARCHAR(30) NOT NULL,
    F_TABLE_NAME      VARCHAR(30) NOT NULL,
    F_GEOMETRY_COLUMN VARCHAR(30) NOT NULL,
    COORD_DIMENSION   INTEGER,
    SRID              INTEGER NOT NULL,
    TYPE              VARCHAR(30) NOT NULL,
    UNIQUE(F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN),
    CHECK(TYPE IN ('POINT', 'LINE', 'POLYGON', 'COLLECTION', 'MULTIPOINT', 'MULTILINE', 'MULTIPOLYGON', 'GEOMETRY'))
  );
COMMENT ON TABLE GEOMETRY_COLUMNS IS 'Geometry metadata tabel ten behoeve van Geoserver/Geotools';

-- tabellen wissen
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_SCHEMA='BRMO_RSGB' AND F_TABLE_NAME LIKE 'V_P8_%' OR F_TABLE_NAME LIKE 'VM_P8_%';
DELETE FROM GT_PK_METADATA WHERE TABLE_SCHEMA='BRMO_RSGB' AND TABLE_NAME LIKE 'V_P8_%' OR TABLE_NAME LIKE 'VM_P8_%';



-- V_P8_KAD_PERCEEL_OVER_IN heeft geen PK kolom, maar gebruikt ROWNUM en geen geometrie
INSERT INTO gt_pk_metadata VALUES ('BRMO_RSGB', 'V_P8_KAD_PERCEEL_OVER_IN', 'OID', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('BRMO_RSGB', 'VM_P8_KAD_PERCEEL_OVER_IN', 'OID', NULL, 'assigned', NULL);


-- V_P8_KADASTRAAL_PERCEEL heeft unieke KADPERCEELCODE kolom
INSERT INTO gt_pk_metadata VALUES ('BRMO_RSGB', 'V_P8_KADASTRAAL_PERCEEL', 'KADPERCEELCODE', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('BRMO_RSGB', 'VM_P8_KADASTRAAL_PERCEEL', 'KADPERCEELCODE', NULL, 'assigned', NULL);
INSERT INTO geometry_columns (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) VALUES ('BRMO_RSGB', 'V_P8_KADASTRAAL_PERCEEL', 'GEOM', 2, 28992, 'MULTIPOLYGON');
INSERT INTO geometry_columns (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) VALUES ('BRMO_RSGB', 'VM_P8_KADASTRAAL_PERCEEL', 'GEOM', 2, 28992, 'MULTIPOLYGON');
    

-- V_P8_KADASTRAAL_PERCEEL_AANT heeft geen PK kolom, maar gebruikt ROWNUM en geen geometrie
INSERT INTO gt_pk_metadata VALUES ('BRMO_RSGB', 'V_P8_KADASTRAAL_PERCEEL_AANT', 'OID', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('BRMO_RSGB', 'VM_P8_KADASTRAAL_PERCEEL_AANT', 'OID', NULL, 'assigned', NULL);


-- V_P8_KADASTRAAL_PERCEEL_RECHT heeft geen PK kolom, maar gebruikt ROWNUM en geen geometrie
INSERT INTO gt_pk_metadata VALUES ('BRMO_RSGB', 'V_P8_KADASTRAAL_PERCEEL_RECHT', 'OID', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('BRMO_RSGB', 'VM_P8_KADASTRAAL_PERCEEL_RECHT', 'OID', NULL, 'assigned', NULL);


-- V_P8_SUBJECT heeft geen PK kolom en geen geometrie, waarschijnlijk is SUBJECTID te gebruiken als PK
INSERT INTO gt_pk_metadata VALUES ('BRMO_RSGB', 'V_P8_SUBJECT', 'SUBJECTID', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('BRMO_RSGB', 'VM_P8_SUBJECT', 'SUBJECTID', NULL, 'assigned', NULL);


-- V_P8_SUBJECT_PERCELEN heeft geen PK kolom, maar gebruikt ROWNUM
INSERT INTO gt_pk_metadata VALUES ('BRMO_RSGB', 'V_P8_SUBJECT_PERCELEN', 'OID', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('BRMO_RSGB', 'VM_P8_SUBJECT_PERCELEN', 'OID', NULL, 'assigned', NULL);
INSERT INTO geometry_columns (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) VALUES ('BRMO_RSGB', 'V_P8_SUBJECT_PERCELEN', 'GEOM', 2, 28992, 'MULTIPOLYGON');
INSERT INTO geometry_columns (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) VALUES ('BRMO_RSGB', 'VM_P8_SUBJECT_PERCELEN', 'GEOM', 2, 28992, 'MULTIPOLYGON');

-- V_P8_KADASTRAAL_ADRES heeft geen PK kolom, maar gebruikt ROWNUM
INSERT INTO gt_pk_metadata VALUES ('BRMO_RSGB', 'V_P8_KADASTRAAL_ADRES', 'OID', NULL, 'assigned', NULL);
INSERT INTO gt_pk_metadata VALUES ('BRMO_RSGB', 'VM_P8_KADASTRAAL_ADRES', 'OID', NULL, 'assigned', NULL);
INSERT INTO geometry_columns (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) VALUES ('BRMO_RSGB', 'V_P8_KADASTRAAL_ADRES', 'GEOM', 2, 28992, 'MULTIPOLYGON');
INSERT INTO geometry_columns (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) VALUES ('BRMO_RSGB', 'VM_P8_KADASTRAAL_ADRES', 'GEOM', 2, 28992, 'MULTIPOLYGON');
