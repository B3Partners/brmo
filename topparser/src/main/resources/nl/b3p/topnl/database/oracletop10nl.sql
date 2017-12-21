CREATE TABLE top10nl.Hoogte
  (
    topnltype        VARCHAR2(255),
    id               INTEGER,
    identificatie    VARCHAR2(255),
    brontype         VARCHAR2(255),
    bronactualiteit  DATE,
    bronbeschrijving VARCHAR2(255),
    bronnauwkeurigheid DOUBLE PRECISION,
    objectBeginTijd DATE,
    objectEindTijd  DATE,
    visualisatieCode LONG,
    typeHoogte VARCHAR2(255),
    geometrie MDSYS.SDO_GEOMETRY,
    referentieVlak VARCHAR2(255),
    hoogte DOUBLE PRECISION,
    PRIMARY KEY (id)
  );
CREATE TABLE top10nl.FunctioneelGebied
  (
    topnltype        VARCHAR2(255),
    id               INTEGER,
    identificatie    VARCHAR2(255),
    brontype         VARCHAR2(255),
    bronactualiteit  DATE,
    bronbeschrijving VARCHAR2(255),
    bronnauwkeurigheid DOUBLE PRECISION,
    objectBeginTijd DATE,
    objectEindTijd  DATE,
    visualisatieCode LONG,
    typeFunctioneelGebied VARCHAR2(255),
    soortnaam             VARCHAR2(255),
    naamNL                VARCHAR2(255),
    naamFries             VARCHAR2(255),
    geometrie MDSYS.SDO_GEOMETRY,
    PRIMARY KEY (id)
  );
CREATE TABLE top10nl.PlanTopografie
  (
    topnltype        VARCHAR2(255),
    id               INTEGER,
    identificatie    VARCHAR2(255),
    brontype         VARCHAR2(255),
    bronactualiteit  DATE,
    bronbeschrijving VARCHAR2(255),
    bronnauwkeurigheid DOUBLE PRECISION,
    objectBeginTijd DATE,
    objectEindTijd  DATE,
    visualisatieCode LONG,
    typePlanTopografie VARCHAR2(255),
    soort              VARCHAR2(255),
    naam               VARCHAR2(255),
    geometrie MDSYS.SDO_GEOMETRY,
    PRIMARY KEY (id)
  );
CREATE TABLE top10nl.Gebouw
  (
    topnltype        VARCHAR2(255),
    id               INTEGER,
    identificatie    VARCHAR2(255),
    brontype         VARCHAR2(255),
    bronactualiteit  DATE,
    bronbeschrijving VARCHAR2(255),
    bronnauwkeurigheid DOUBLE PRECISION,
    objectBeginTijd DATE,
    objectEindTijd  DATE,
    visualisatieCode LONG,
    typeGebouw      VARCHAR2(255),
    status          VARCHAR2(255),
    fysiekVoorkomen VARCHAR2(255),
    hoogteklasse    VARCHAR2(255),
    hoogte DOUBLE PRECISION,
    soortnaam VARCHAR2(255),
    naam      VARCHAR2(255),
    naamFries VARCHAR2(255),
    geometrie MDSYS.SDO_GEOMETRY,
    PRIMARY KEY (id)
  );
CREATE TABLE top10nl.GeografischGebied
  (
    topnltype        VARCHAR2(255),
    id               INTEGER,
    identificatie    VARCHAR2(255),
    brontype         VARCHAR2(255),
    bronactualiteit  DATE,
    bronbeschrijving VARCHAR2(255),
    bronnauwkeurigheid DOUBLE PRECISION,
    objectBeginTijd DATE,
    objectEindTijd  DATE,
    visualisatieCode LONG,
    typeGeografischGebied VARCHAR2(255),
    naamNL                VARCHAR2(255),
    naamFries             VARCHAR2(255),
    geometrie MDSYS.SDO_GEOMETRY,
    PRIMARY KEY (id)
  );
CREATE TABLE top10nl.Inrichtingselement
  (
    topnltype        VARCHAR2(255),
    id               INTEGER,
    identificatie    VARCHAR2(255),
    brontype         VARCHAR2(255),
    bronactualiteit  DATE,
    bronbeschrijving VARCHAR2(255),
    bronnauwkeurigheid DOUBLE PRECISION,
    objectBeginTijd DATE,
    objectEindTijd  DATE,
    visualisatieCode LONG,
    typeInrichtingselement VARCHAR2(255),
    soortnaam              VARCHAR2(255),
    status                 VARCHAR2(255),
    hoogteniveau           NUMBER,
    geometrie MDSYS.SDO_GEOMETRY,
    PRIMARY KEY (id)
  );
CREATE TABLE top10nl.Plaats
  (
    topnltype        VARCHAR2(255),
    id               INTEGER,
    identificatie    VARCHAR2(255),
    brontype         VARCHAR2(255),
    bronactualiteit  DATE,
    bronbeschrijving VARCHAR2(255),
    bronnauwkeurigheid DOUBLE PRECISION,
    objectBeginTijd DATE,
    objectEindTijd  DATE,
    visualisatieCode LONG,
    typeGebied     VARCHAR2(255),
    aantalInwoners NUMBER,
    naamOfficieel  VARCHAR2(255),
    naamNL         VARCHAR2(255),
    naamFries      VARCHAR2(255),
    geometrie MDSYS.SDO_GEOMETRY,
    PRIMARY KEY (id)
  );
CREATE TABLE top10nl.RegistratiefGebied
  (
    topnltype        VARCHAR2(255),
    id               INTEGER,
    identificatie    VARCHAR2(255),
    brontype         VARCHAR2(255),
    bronactualiteit  DATE,
    bronbeschrijving VARCHAR2(255),
    bronnauwkeurigheid DOUBLE PRECISION,
    objectBeginTijd DATE,
    objectEindTijd  DATE,
    visualisatieCode LONG,
    typeRegistratiefGebied VARCHAR2(255),
    naamOfficieel          VARCHAR2(255),
    naamNL                 VARCHAR2(255),
    naamFries              VARCHAR2(255),
    nummer                 VARCHAR2(255),
    geometrie MDSYS.SDO_GEOMETRY,
    PRIMARY KEY (id)
  );
CREATE TABLE top10nl.Relief
  (
    topnltype        VARCHAR2(255),
    id               INTEGER,
    identificatie    VARCHAR2(255),
    brontype         VARCHAR2(255),
    bronactualiteit  DATE,
    bronbeschrijving VARCHAR2(255),
    bronnauwkeurigheid DOUBLE PRECISION,
    objectBeginTijd DATE,
    objectEindTijd  DATE,
    visualisatieCode LONG,
    typeRelief   VARCHAR2(255),
    hoogteklasse VARCHAR2(255),
    hoogteniveau NUMBER,
    geometrie MDSYS.SDO_GEOMETRY,
    taludLageZijde MDSYS.SDO_GEOMETRY,
    taludHogeZijde MDSYS.SDO_GEOMETRY,
    PRIMARY KEY (id)
  );
CREATE TABLE top10nl.Spoorbaandeel
  (
    topnltype        VARCHAR2(255),
    id               INTEGER,
    identificatie    VARCHAR2(255),
    brontype         VARCHAR2(255),
    bronactualiteit  DATE,
    bronbeschrijving VARCHAR2(255),
    bronnauwkeurigheid DOUBLE PRECISION,
    objectBeginTijd DATE,
    objectEindTijd  DATE,
    visualisatieCode LONG,
    typeInfrastructuur VARCHAR2(255),
    typeSpoorbaan      VARCHAR2(255),
    fysiekVoorkomen    VARCHAR2(255),
    spoorbreedte       VARCHAR2(255),
    aantalSporen       VARCHAR2(255),
    vervoerfunctie     VARCHAR2(255),
    elektrificatie     CHAR(1),
    status             VARCHAR2(255),
    brugnaam           VARCHAR2(255),
    tunnelnaam         VARCHAR2(255),
    baanvaknaam        VARCHAR2(255),
    hoogteniveau       NUMBER,
    geometrie MDSYS.SDO_GEOMETRY,
    puntGeometrie MDSYS.SDO_GEOMETRY,
    PRIMARY KEY (id)
  );
CREATE TABLE top10nl.Terrein
  (
    topnltype        VARCHAR2(255),
    id               INTEGER,
    identificatie    VARCHAR2(255),
    brontype         VARCHAR2(255),
    bronactualiteit  DATE,
    bronbeschrijving VARCHAR2(255),
    bronnauwkeurigheid DOUBLE PRECISION,
    objectBeginTijd DATE,
    objectEindTijd  DATE,
    visualisatieCode LONG,
    typeLandgebruik VARCHAR2(255),
    naam            VARCHAR2(255),
    geometrie MDSYS.SDO_GEOMETRY,
    PRIMARY KEY (id)
  );
CREATE TABLE top10nl.Waterdeel
  (
    topnltype        VARCHAR2(255),
    id               INTEGER,
    identificatie    VARCHAR2(255),
    brontype         VARCHAR2(255),
    bronactualiteit  DATE,
    bronbeschrijving VARCHAR2(255),
    bronnauwkeurigheid DOUBLE PRECISION,
    objectBeginTijd DATE,
    objectEindTijd  DATE,
    visualisatieCode LONG,
    typeWater       VARCHAR2(255),
    breedteklasse   VARCHAR2(255),
    fysiekVoorkomen VARCHAR2(255),
    voorkomen       VARCHAR2(255),
    getijdeinvloed  CHAR(1),
    vaarwegklasse   VARCHAR2(255),
    naamOfficieel   VARCHAR2(255),
    naamNL          VARCHAR2(255),
    naamFries       VARCHAR2(255),
    isBAGnaam       CHAR(1),
    sluisnaam       VARCHAR2(255),
    brugnaam        VARCHAR2(255),
    hoogteniveau    NUMBER,
    functie         VARCHAR2(255),
    hoofdAfwatering CHAR(1),
    geometrie MDSYS.SDO_GEOMETRY,
    PRIMARY KEY (id)
  );
CREATE TABLE top10nl.Wegdeel
  (
    topnltype        VARCHAR2(255),
    id               INTEGER,
    identificatie    VARCHAR2(255),
    brontype         VARCHAR2(255),
    bronactualiteit  DATE,
    bronbeschrijving VARCHAR2(255),
    bronnauwkeurigheid DOUBLE PRECISION,
    objectBeginTijd DATE,
    objectEindTijd  DATE,
    visualisatieCode LONG,
    typeInfrastructuur       VARCHAR2(255),
    typeWeg                  VARCHAR2(255),
    hoofdverkeersgebruik     VARCHAR2(255),
    fysiekVoorkomen          VARCHAR2(255),
    verhardingsbreedteklasse VARCHAR2(255),
    gescheidenRijbaan        CHAR(1),
    verhardingstype          VARCHAR2(255),
    aantalRijstroken         NUMBER,
    hoogteniveau             NUMBER,
    status                   VARCHAR2(255),
    naam                     VARCHAR2(255),
    isBAGnaam                CHAR(1),
    aWegnummer               VARCHAR2(255),
    nWegnummer               VARCHAR2(255),
    eWegnummer               VARCHAR2(255),
    sWegnummer               VARCHAR2(255),
    afritnummer              VARCHAR2(255),
    afritnaam                VARCHAR2(255),
    knooppuntnaam            VARCHAR2(255),
    brugnaam                 VARCHAR2(255),
    tunnelnaam               VARCHAR2(255),
    geometrie MDSYS.SDO_GEOMETRY,
    hartGeometrie MDSYS.SDO_GEOMETRY,
    PRIMARY KEY (id)
  );
INSERT
INTO user_sdo_geom_metadata VALUES
  (
    'Hoogte',
    'geometrie',
    MDSYS.SDO_DIM_ARRAY( MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1), MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );
CREATE INDEX Hoogte_geom_idx ON Hoogte
  (
    geometrie
  )
  INDEXTYPE IS MDSYS.SPATIAL_INDEX;
INSERT
INTO user_sdo_geom_metadata VALUES
  (
    'PlanTopografie',
    'geometrie',
    MDSYS.SDO_DIM_ARRAY( MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1), MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );
CREATE INDEX PlanTopografie_geom_idx ON PlanTopografie
  (
    geometrie
  )
  INDEXTYPE IS MDSYS.SPATIAL_INDEX;
INSERT
INTO user_sdo_geom_metadata VALUES
  (
    'FunctioneelGebied',
    'geometrie',
    MDSYS.SDO_DIM_ARRAY( MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1), MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );
CREATE INDEX FunctioneelGebied_geom_idx ON FunctioneelGebied
  (
    geometrie
  )
  INDEXTYPE IS MDSYS.SPATIAL_INDEX;
INSERT
INTO user_sdo_geom_metadata VALUES
  (
    'Gebouw',
    'geometrie',
    MDSYS.SDO_DIM_ARRAY( MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1), MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );
CREATE INDEX Gebouw_geom_idx ON Gebouw
  (
    geometrie
  )
  INDEXTYPE IS MDSYS.SPATIAL_INDEX;
INSERT
INTO user_sdo_geom_metadata VALUES
  (
    'GeografischGebied',
    'geometrie',
    MDSYS.SDO_DIM_ARRAY( MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1), MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );
CREATE INDEX GeografischGebied_geom_idx ON GeografischGebied
  (
    geometrie
  )
  INDEXTYPE IS MDSYS.SPATIAL_INDEX;
INSERT
INTO user_sdo_geom_metadata VALUES
  (
    'Inrichtingselement',
    'geometrie',
    MDSYS.SDO_DIM_ARRAY( MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1), MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );
CREATE INDEX Inrichtingselement_geom_idx ON Inrichtingselement
  (
    geometrie
  )
  INDEXTYPE IS MDSYS.SPATIAL_INDEX;
INSERT
INTO user_sdo_geom_metadata VALUES
  (
    'Plaats',
    'geometrie',
    MDSYS.SDO_DIM_ARRAY( MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1), MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );
CREATE INDEX Plaats_geom_idx ON Plaats
  (
    geometrie
  )
  INDEXTYPE IS MDSYS.SPATIAL_INDEX;
INSERT
INTO user_sdo_geom_metadata VALUES
  (
    'RegistratiefGebied',
    'geometrie',
    MDSYS.SDO_DIM_ARRAY( MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1), MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );
CREATE INDEX RegistratiefGebied_geom_idx ON RegistratiefGebied
  (
    geometrie
  )
  INDEXTYPE IS MDSYS.SPATIAL_INDEX;
INSERT
INTO user_sdo_geom_metadata VALUES
  (
    'Relief',
    'geometrie',
    MDSYS.SDO_DIM_ARRAY( MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1), MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );
CREATE INDEX Relief_geom_idx ON Relief
  (
    geometrie
  )
  INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=LINE');
INSERT
INTO user_sdo_geom_metadata VALUES
  (
    'Relief',
    'taludLageZijde',
    MDSYS.SDO_DIM_ARRAY( MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1), MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );
CREATE INDEX Relief_geom2_idx ON Relief
  (
    taludLageZijde
  )
  INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=LINE');
INSERT
INTO user_sdo_geom_metadata VALUES
  (
    'Relief',
    'taludHogeZijde',
    MDSYS.SDO_DIM_ARRAY( MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1), MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );
CREATE INDEX Relief_geom3_idx ON Relief
  (
    taludHogeZijde
  )
  INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=LINE');
INSERT
INTO user_sdo_geom_metadata VALUES
  (
    'Spoorbaandeel',
    'geometrie',
    MDSYS.SDO_DIM_ARRAY( MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1), MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );
CREATE INDEX Spoorbaandeel_geom_idx ON Spoorbaandeel
  (
    geometrie
  )
  INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=LINE');
INSERT
INTO user_sdo_geom_metadata VALUES
  (
    'Terrein',
    'geometrie',
    MDSYS.SDO_DIM_ARRAY( MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1), MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );
CREATE INDEX Terrein_geom_idx ON Terrein
  (
    geometrie
  )
  INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=POLYGON');
INSERT
INTO user_sdo_geom_metadata VALUES
  (
    'Waterdeel',
    'geometrie',
    MDSYS.SDO_DIM_ARRAY( MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1), MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );
CREATE INDEX Waterdeel_geom_idx ON Waterdeel
  (
    geometrie
  )
  INDEXTYPE IS MDSYS.SPATIAL_INDEX;
INSERT
INTO user_sdo_geom_metadata VALUES
  (
    'Wegdeel',
    'geometrie',
    MDSYS.SDO_DIM_ARRAY( MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1), MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );
CREATE INDEX Wegdeel_geom_idx ON Wegdeel
  (
    geometrie
  )
  INDEXTYPE IS MDSYS.SPATIAL_INDEX;
INSERT
INTO user_sdo_geom_metadata VALUES
  (
    'Wegdeel',
    'hartGeometrie',
    MDSYS.SDO_DIM_ARRAY( MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1), MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );
CREATE INDEX Wegdeel_geom2_idx ON Wegdeel
  (
    hartGeometrie
  )
  INDEXTYPE IS MDSYS.SPATIAL_INDEX;
CREATE SEQUENCE Hoogte_seq START WITH 1;
CREATE SEQUENCE PlanTopografie_seq START WITH 1;
CREATE SEQUENCE FunctioneelGebied_seq START WITH 1;
CREATE SEQUENCE Gebouw_seq START WITH 1;
CREATE SEQUENCE GeografischGebied_seq START WITH 1;
CREATE SEQUENCE Inrichtingselement_seq START WITH 1;
CREATE SEQUENCE Plaats_seq START WITH 1;
CREATE SEQUENCE RegistratiefGebied_seq START WITH 1;
CREATE SEQUENCE Relief_seq START WITH 1;
CREATE SEQUENCE Spoorbaandeel_seq START WITH 1;
CREATE SEQUENCE Terrein_seq START WITH 1;
CREATE SEQUENCE Waterdeel_seq START WITH 1;
CREATE SEQUENCE Wegdeel_seq START WITH 1;
  --/
CREATE OR REPLACE TRIGGER Hoogte_trig BEFORE
  INSERT ON top10nl.Hoogte FOR EACH ROW BEGIN
  SELECT Hoogte_seq.NEXTVAL INTO :new.id FROM dual;
END;
/
--/
CREATE OR REPLACE TRIGGER PlanTopografie_trig BEFORE
  INSERT ON top10nl.PlanTopografie FOR EACH ROW BEGIN
  SELECT PlanTopografie_seq.NEXTVAL INTO :new.id FROM dual;
END;
/
--/
CREATE OR REPLACE TRIGGER FunctioneelGebied_trig BEFORE
  INSERT ON top10nl.FunctioneelGebied FOR EACH ROW BEGIN
  SELECT FunctioneelGebied_seq.NEXTVAL INTO :new.id FROM dual;
END;
/
--/
CREATE OR REPLACE TRIGGER Gebouw_trig BEFORE
  INSERT ON top10nl.Gebouw FOR EACH ROW BEGIN
  SELECT Gebouw_seq.NEXTVAL INTO :new.id FROM dual;
END;
/
--/
CREATE OR REPLACE TRIGGER GeografischGebied_trig BEFORE
  INSERT ON top10nl.GeografischGebied FOR EACH ROW BEGIN
  SELECT GeografischGebied_seq.NEXTVAL INTO :new.id FROM dual;
END;
/
--/
CREATE OR REPLACE TRIGGER Inrichtingselement_trig BEFORE
  INSERT ON top10nl.Inrichtingselement FOR EACH ROW BEGIN
  SELECT Inrichtingselement_seq.NEXTVAL INTO :new.id FROM dual;
END;
/
--/
CREATE OR REPLACE TRIGGER Plaats_trig BEFORE
  INSERT ON top10nl.Plaats FOR EACH ROW BEGIN
  SELECT Plaats_seq.NEXTVAL INTO :new.id FROM dual;
END;
/
--/
CREATE OR REPLACE TRIGGER RegistratiefGebied_trig BEFORE
  INSERT ON top10nl.RegistratiefGebied FOR EACH ROW BEGIN
  SELECT RegistratiefGebied_seq.NEXTVAL INTO :new.id FROM dual;
END;
/
--/
CREATE OR REPLACE TRIGGER Relief_trig BEFORE
  INSERT ON top10nl.Relief FOR EACH ROW BEGIN
  SELECT Relief_seq.NEXTVAL INTO :new.id FROM dual;
END;
/
--/
CREATE OR REPLACE TRIGGER Spoorbaandeel_trig BEFORE
  INSERT ON top10nl.Spoorbaandeel FOR EACH ROW BEGIN
  SELECT Spoorbaandeel_seq.NEXTVAL INTO :new.id FROM dual;
END;
/
--/
CREATE OR REPLACE TRIGGER Terrein_trig BEFORE
  INSERT ON top10nl.Terrein FOR EACH ROW BEGIN
  SELECT Terrein_seq.NEXTVAL INTO :new.id FROM dual;
END;
/
--/
CREATE OR REPLACE TRIGGER Waterdeel_trig BEFORE
  INSERT ON top10nl.Waterdeel FOR EACH ROW BEGIN
  SELECT Waterdeel_seq.NEXTVAL INTO :new.id FROM dual;
END;
/
--/
CREATE OR REPLACE TRIGGER Wegdeel_trig BEFORE
  INSERT ON top10nl.Wegdeel FOR EACH ROW BEGIN
  SELECT Wegdeel_seq.NEXTVAL INTO :new.id FROM dual;
END;
/
