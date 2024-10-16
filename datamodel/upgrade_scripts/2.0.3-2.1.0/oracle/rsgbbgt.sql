-- 
-- upgrade Oracle RSGBBGT datamodel van 2.0.3 naar 2.1.0
--
WHENEVER SQLERROR EXIT SQL.SQLCODE

-- drop oude RSGBBGT tabellen en metadata
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'BAK';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'BAK';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'BAK';
DROP TABLE BAK CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'BEGROEID_TERREINDEEL';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'BEGROEID_TERREINDEEL';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'BEGROEID_TERREINDEEL';
DROP TABLE BEGROEID_TERREINDEEL CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'BORD';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'BORD';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'BORD';
DROP TABLE BORD CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'FUNCTIONEELGEBIED';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'FUNCTIONEELGEBIED';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'FUNCTIONEELGEBIED';
DROP TABLE FUNCTIONEELGEBIED CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'GEBOUW_INSTALLATIE';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'GEBOUW_INSTALLATIE';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'GEBOUW_INSTALLATIE';
DROP TABLE GEBOUW_INSTALLATIE CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'INSTALLATIE';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'INSTALLATIE';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'INSTALLATIE';
DROP TABLE INSTALLATIE CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'KAST';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'KAST';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'KAST';
DROP TABLE KAST CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'MAST';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'MAST';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'MAST';
DROP TABLE MAST CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'PAAL';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'PAAL';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'PAAL';
DROP TABLE PAAL CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'PUT';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'PUT';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'PUT';
DROP TABLE PUT CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'SENSOR';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'SENSOR';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'SENSOR';
DROP TABLE SENSOR CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'OPENBARERUIMTELABEL';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'OPENBARERUIMTELABEL';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'OPENBARERUIMTELABEL';
DROP TABLE OPENBARERUIMTELABEL CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'OVERIGE_SCHEIDING';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'OVERIGE_SCHEIDING';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'OVERIGE_SCHEIDING';
DROP TABLE OVERIGE_SCHEIDING CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'SCHEIDING';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'SCHEIDING';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'SCHEIDING';
DROP TABLE SCHEIDING CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'OVERIG_BOUWWERK';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'OVERIG_BOUWWERK';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'OVERIG_BOUWWERK';
DROP TABLE OVERIG_BOUWWERK CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'ONGECLASSIFICEERDOBJECT';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'ONGECLASSIFICEERDOBJECT';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'ONGECLASSIFICEERDOBJECT';
DROP TABLE ONGECLASSIFICEERDOBJECT CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'WATERDEEL';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'WATERDEEL';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'WATERDEEL';
DROP TABLE WATERDEEL CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'KUNSTWERKDEEL';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'KUNSTWERKDEEL';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'KUNSTWERKDEEL';
DROP TABLE KUNSTWERKDEEL CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'ONBEGROEID_TERREINDEEL';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'ONBEGROEID_TERREINDEEL';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'ONBEGROEID_TERREINDEEL';
DROP TABLE ONBEGROEID_TERREINDEEL CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'OVERBRUGGINGSDEEL';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'OVERBRUGGINGSDEEL';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'OVERBRUGGINGSDEEL';
DROP TABLE OVERBRUGGINGSDEEL CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'STRAATMEUBILAIR';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'STRAATMEUBILAIR';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'STRAATMEUBILAIR';
DROP TABLE STRAATMEUBILAIR CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'ONDERSTEUNEND_WATERDEEL';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'ONDERSTEUNEND_WATERDEEL';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'ONDERSTEUNEND_WATERDEEL';
DROP TABLE ONDERSTEUNEND_WATERDEEL CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'ONDERSTEUNEND_WEGDEEL';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'ONDERSTEUNEND_WEGDEEL';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'ONDERSTEUNEND_WEGDEEL';
DROP TABLE ONDERSTEUNEND_WEGDEEL CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'SPOOR';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'SPOOR';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'SPOOR';
DROP TABLE SPOOR CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'WEGINRICHTINGSELEMENT';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'WEGINRICHTINGSELEMENT';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'WEGINRICHTINGSELEMENT';
DROP TABLE WEGINRICHTINGSELEMENT CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'VEGETATIEOBJECT';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'VEGETATIEOBJECT';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'VEGETATIEOBJECT';
DROP TABLE VEGETATIEOBJECT CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'TUNNELDEEL';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'TUNNELDEEL';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'TUNNELDEEL';
DROP TABLE TUNNELDEEL CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'WATERINRICHTINGSELEMENT';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'WATERINRICHTINGSELEMENT';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'WATERINRICHTINGSELEMENT';
DROP TABLE WATERINRICHTINGSELEMENT CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'WEGDEEL';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'WEGDEEL';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'WEGDEEL';
DROP TABLE WEGDEEL CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'PAND';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'PAND';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'PAND';
DROP TABLE PAND CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'BUURT';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'BUURT';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'BUURT';
DROP TABLE BUURT CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'STADSDEEL';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'STADSDEEL';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'STADSDEEL';
DROP TABLE STADSDEEL CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'WIJK';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'WIJK';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'WIJK';
DROP TABLE WIJK CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'OPENBARERUIMTE';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'OPENBARERUIMTE';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'OPENBARERUIMTE';
DROP TABLE OPENBARERUIMTE CASCADE CONSTRAINTS;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'WATERSCHAP';
DELETE FROM GT_PK_METADATA WHERE TABLE_NAME = 'WATERSCHAP';
DELETE FROM GEOMETRY_COLUMNS WHERE F_TABLE_NAME = 'WATERSCHAP';
DROP TABLE WATERSCHAP CASCADE CONSTRAINTS;

DROP TABLE GEOMETRY_COLUMNS CASCADE CONSTRAINTS;
DROP TABLE GT_PK_METADATA CASCADE CONSTRAINTS;

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.0.3_naar_2.1.0','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.1.0' WHERE naam='brmoversie';
