create table  top50nl.Hoogte (
    topnltype varchar2(255),
    id Integer,
    identificatie varchar2(255),
    brontype varchar2(255),
    bronactualiteit Date,
    bronbeschrijving varchar2(255),
    bronnauwkeurigheid Double precision,
    objectBeginTijd Date,
    objectEindTijd Date,
    visualisatieCode Long,

    typeHoogte varchar2(255),
    geometrie MDSYS.SDO_GEOMETRY,
    referentieVlak varchar2(255),
    hoogte Double precision,
  primary key (id)
    
    );

create table top50nl.PlanTopografie(

    topnltype varchar2(255),
    id Integer,
    identificatie varchar2(255),
    brontype varchar2(255),
    bronactualiteit Date,
    bronbeschrijving varchar2(255),
    bronnauwkeurigheid Double precision,
    objectBeginTijd Date,
    objectEindTijd Date,
    visualisatieCode Long,

    
  typePlanTopografie varchar2(255),
  soort varchar2(255),
    
  geometrie MDSYS.SDO_GEOMETRY,
  primary key (id)
  );


create table top50nl.FunctioneelGebied(

    topnltype varchar2(255),
    id Integer,
    identificatie varchar2(255),
    brontype varchar2(255),
    bronactualiteit Date,
    bronbeschrijving varchar2(255),
    bronnauwkeurigheid Double precision,
    objectBeginTijd Date,
    objectEindTijd Date,
    visualisatieCode Long,

    
  typeFunctioneelGebied varchar2(255),
  soortnaam varchar2(255),
  naamNL varchar2(255),
  naamFries varchar2(255),
    
  geometrie MDSYS.SDO_GEOMETRY,
  primary key (id)
  );


create table top50nl.Gebouw(

    topnltype varchar2(255),
    id Integer,
    identificatie varchar2(255),
    brontype varchar2(255),
    bronactualiteit Date,
    bronbeschrijving varchar2(255),
    bronnauwkeurigheid Double precision,
    objectBeginTijd Date,
    objectEindTijd Date,
    visualisatieCode Long,

  typeGebouw varchar2(255),
  status varchar2(255),
  fysiekVoorkomen varchar2(255),
  hoogteklasse varchar2(255),
  hoogte Double precision,
  soortnaam varchar2(255),
  naam varchar2(255),
  naamFries varchar2(255),
    
    
  geometrie MDSYS.SDO_GEOMETRY,
  primary key (id)
  );



create table top50nl.GeografischGebied(

    topnltype varchar2(255),
    id Integer,
    identificatie varchar2(255),
    brontype varchar2(255),
    bronactualiteit Date,
    bronbeschrijving varchar2(255),
    bronnauwkeurigheid Double precision,
    objectBeginTijd Date,
    objectEindTijd Date,
    visualisatieCode Long,

    
  typeGeografischGebied varchar2(255),
  naamNL varchar2(255),
  naamFries varchar2(255),
    
  geometrie MDSYS.SDO_GEOMETRY,
  primary key (id)
  );


create table top50nl.Inrichtingselement(

    topnltype varchar2(255),
    id Integer,
    identificatie varchar2(255),
    brontype varchar2(255),
    bronactualiteit Date,
    bronbeschrijving varchar2(255),
    bronnauwkeurigheid Double precision,
    objectBeginTijd Date,
    objectEindTijd Date,
    visualisatieCode Long,

    
  typeInrichtingselement varchar2(255),
  soortnaam varchar2(255),
  status varchar2(255),
  hoogteniveau number,
    
  geometrie MDSYS.SDO_GEOMETRY,
  primary key (id)
  );



create table top50nl.Plaats(

    topnltype varchar2(255),
    id Integer,
    identificatie varchar2(255),
    brontype varchar2(255),
    bronactualiteit Date,
    bronbeschrijving varchar2(255),
    bronnauwkeurigheid Double precision,
    objectBeginTijd Date,
    objectEindTijd Date,
    visualisatieCode Long,

    
  typeGebied varchar2(255),
  aantalInwoners number,
  naamOfficieel varchar2(255),
  naamNL varchar2(255),
  naamFries varchar2(255),
    
  geometrie MDSYS.SDO_GEOMETRY,
  primary key (id)
  );


create table top50nl.RegistratiefGebied(

    topnltype varchar2(255),
    id Integer,
    identificatie varchar2(255),
    brontype varchar2(255),
    bronactualiteit Date,
    bronbeschrijving varchar2(255),
    bronnauwkeurigheid Double precision,
    objectBeginTijd Date,
    objectEindTijd Date,
    visualisatieCode Long,

    
  typeRegistratiefGebied varchar2(255),
  naamOfficieel varchar2(255),
  naamNL varchar2(255),
  naamFries varchar2(255),
  nummer varchar2(255),
  geometrie MDSYS.SDO_GEOMETRY,
  primary key (id)
  );


create table top50nl.Relief(

    topnltype varchar2(255),
    id Integer,
    identificatie varchar2(255),
    brontype varchar2(255),
    bronactualiteit Date,
    bronbeschrijving varchar2(255),
    bronnauwkeurigheid Double precision,
    objectBeginTijd Date,
    objectEindTijd Date,
    visualisatieCode Long,

  typeRelief varchar2(255),
  hoogteklasse varchar2(255),

  hoogteniveau number,
  geometrie  MDSYS.SDO_GEOMETRY,
  primary key (id)
  );

create table top50nl.Spoorbaandeel (

    topnltype varchar2(255),
    id Integer,
    identificatie varchar2(255),
    brontype varchar2(255),
    bronactualiteit Date,
    bronbeschrijving varchar2(255),
    bronnauwkeurigheid Double precision,
    objectBeginTijd Date,
    objectEindTijd Date,
    visualisatieCode Long,


  typeInfrastructuur varchar2(255),
  typeSpoorbaan varchar2(255),
  fysiekVoorkomen varchar2(255),
  spoorbreedte varchar2(255),
  aantalSporen varchar2(255),
  vervoerfunctie varchar2(255),
  elektrificatie char(1),
  status varchar2(255),
  brugnaam varchar2(255),
  tunnelnaam varchar2(255),
  baanvaknaam varchar2(255),
  hoogteniveau number,
    
  geometrie  MDSYS.SDO_GEOMETRY,
  primary key (id)
  );

create table top50nl.Terrein(

    topnltype varchar2(255),
    id Integer,
    identificatie varchar2(255),
    brontype varchar2(255),
    bronactualiteit Date,
    bronbeschrijving varchar2(255),
    bronnauwkeurigheid Double precision,
    objectBeginTijd Date,
    objectEindTijd Date,
    visualisatieCode Long,

    
  typeLandgebruik varchar2(255),
  naam varchar2(255),
    
  geometrie MDSYS.SDO_GEOMETRY,
  primary key (id)
  );


create table top50nl.Waterdeel (

    topnltype varchar2(255),
    id Integer,
    identificatie varchar2(255),
    brontype varchar2(255),
    bronactualiteit Date,
    bronbeschrijving varchar2(255),
    bronnauwkeurigheid Double precision,
    objectBeginTijd Date,
    objectEindTijd Date,
    visualisatieCode Long,


  typeWater varchar2(255),
  breedteklasse varchar2(255),
  fysiekVoorkomen varchar2(255),
  voorkomen varchar2(255),
  getijdeinvloed char(1),
  vaarwegklasse varchar2(255),
  naamOfficieel varchar2(255),
  naamNL varchar2(255),
  naamFries varchar2(255),
  isBAGnaam char(1),
  sluisnaam varchar2(255),
  brugnaam varchar2(255),
  hoogteniveau number,
  functie varchar2(255),
  hoofdAfwatering char(1),
    
  geometrie MDSYS.SDO_GEOMETRY,
  primary key (id)
  );


create table top50nl.Wegdeel (

    topnltype varchar2(255),
    id Integer,
    identificatie varchar2(255),
    brontype varchar2(255),
    bronactualiteit Date,
    bronbeschrijving varchar2(255),
    bronnauwkeurigheid Double precision,
    objectBeginTijd Date,
    objectEindTijd Date,
    visualisatieCode Long,


  typeInfrastructuur varchar2(255),
  typeWeg varchar2(255),
  hoofdverkeersgebruik varchar2(255),
  fysiekVoorkomen varchar2(255),
  verhardingsbreedteklasse varchar2(255),
  gescheidenRijbaan char(1),
  verhardingstype varchar2(255),
  aantalRijstroken number,
  hoogteniveau number,
  status varchar2(255),
  naam varchar2(255),
  isBAGnaam char(1),
  aWegnummer varchar2(255),
  nWegnummer varchar2(255),
  eWegnummer varchar2(255),
  sWegnummer varchar2(255),
  afritnummer varchar2(255),
  afritnaam varchar2(255),
  knooppuntnaam varchar2(255),
  brugnaam varchar2(255),
  tunnelnaam varchar2(255),

  geometrie MDSYS.SDO_GEOMETRY,
  hartGeometrie MDSYS.SDO_GEOMETRY,
  primary key (id)
  );





insert into user_sdo_geom_metadata values('Hoogte', 'geometrie', MDSYS.SDO_DIM_ARRAY(
	MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
	MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
	28992
);
CREATE INDEX Hoogte_geom_idx ON Hoogte (geometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX;



insert into user_sdo_geom_metadata values('FunctioneelGebied', 'geometrie', MDSYS.SDO_DIM_ARRAY(
	MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
	MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
	28992
);
CREATE INDEX FunctioneelGebied_geom_idx ON FunctioneelGebied (geometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX;



insert into user_sdo_geom_metadata values('Gebouw', 'geometrie', MDSYS.SDO_DIM_ARRAY(
	MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
	MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
	28992
);
CREATE INDEX Gebouw_geom_idx ON Gebouw (geometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX;



insert into user_sdo_geom_metadata values('GeografischGebied', 'geometrie', MDSYS.SDO_DIM_ARRAY(
	MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
	MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
	28992
);
CREATE INDEX GeografischGebied_geom_idx ON GeografischGebied (geometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX;



insert into user_sdo_geom_metadata values('Inrichtingselement', 'geometrie', MDSYS.SDO_DIM_ARRAY(
	MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
	MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
	28992
);
CREATE INDEX Inrichtingselement_geom_idx ON Inrichtingselement (geometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX;



insert into user_sdo_geom_metadata values('Plaats', 'geometrie', MDSYS.SDO_DIM_ARRAY(
	MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
	MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
	28992
);
CREATE INDEX Plaats_geom_idx ON Plaats (geometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX;



insert into user_sdo_geom_metadata values('RegistratiefGebied', 'geometrie', MDSYS.SDO_DIM_ARRAY(
	MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
	MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
	28992
);
CREATE INDEX RegistratiefGebied_geom_idx ON RegistratiefGebied (geometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX;



insert into user_sdo_geom_metadata values('Relief', 'geometrie', MDSYS.SDO_DIM_ARRAY(
	MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
	MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
	28992
);
CREATE INDEX Relief_geom_idx ON Relief (geometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=LINE');



insert into user_sdo_geom_metadata values('Spoorbaandeel', 'geometrie', MDSYS.SDO_DIM_ARRAY(
	MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
	MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
	28992
);
CREATE INDEX Spoorbaandeel_geom_idx ON Spoorbaandeel (geometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=LINE');



insert into user_sdo_geom_metadata values('Terrein', 'geometrie', MDSYS.SDO_DIM_ARRAY(
	MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
	MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
	28992
);
CREATE INDEX Terrein_geom_idx ON Terrein (geometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=POLYGON');;



insert into user_sdo_geom_metadata values('Waterdeel', 'geometrie', MDSYS.SDO_DIM_ARRAY(
	MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
	MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
	28992
);
CREATE INDEX Waterdeel_geom_idx ON Waterdeel (geometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX;



insert into user_sdo_geom_metadata values('Wegdeel', 'geometrie', MDSYS.SDO_DIM_ARRAY(
	MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
	MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
	28992
);
CREATE INDEX Wegdeel_geom_idx ON Wegdeel (geometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX;



insert into user_sdo_geom_metadata values('Wegdeel', 'hartGeometrie', MDSYS.SDO_DIM_ARRAY(
  MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
  MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
  28992
);
CREATE INDEX Wegdeel_geom2_idx ON Wegdeel (hartGeometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX;


insert into user_sdo_geom_metadata values('PlanTopografie', 'geometrie', MDSYS.SDO_DIM_ARRAY(
  MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
  MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
  28992
);
CREATE INDEX PlanTopografie_geom_idx ON PlanTopografie (geometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX;


CREATE SEQUENCE Hoogte_seq START WITH 1;
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
CREATE SEQUENCE PlanTopografie_seq START WITH 1;

--/
CREATE OR REPLACE TRIGGER PlanTopografie_trig 
BEFORE INSERT ON top50nl.PlanTopografie 
FOR EACH ROW

BEGIN
  SELECT PlanTopografie_seq.NEXTVAL
  INTO   :new.id
  FROM   dual;
END;
/
--/
CREATE OR REPLACE TRIGGER Hoogte_trig 
BEFORE INSERT ON top50nl.Hoogte 
FOR EACH ROW

BEGIN
  SELECT Hoogte_seq.NEXTVAL
  INTO   :new.id
  FROM   dual;
END;
/
--/
CREATE OR REPLACE TRIGGER FunctioneelGebied_trig 
BEFORE INSERT ON top50nl.FunctioneelGebied 
FOR EACH ROW

BEGIN
  SELECT FunctioneelGebied_seq.NEXTVAL
  INTO   :new.id
  FROM   dual;
END;
/
--/
CREATE OR REPLACE TRIGGER Gebouw_trig 
BEFORE INSERT ON top50nl.Gebouw 
FOR EACH ROW

BEGIN
  SELECT Gebouw_seq.NEXTVAL
  INTO   :new.id
  FROM   dual;
END;
/
--/
CREATE OR REPLACE TRIGGER GeografischGebied_trig 
BEFORE INSERT ON top50nl.GeografischGebied 
FOR EACH ROW

BEGIN
  SELECT GeografischGebied_seq.NEXTVAL
  INTO   :new.id
  FROM   dual;
END;
/
--/
CREATE OR REPLACE TRIGGER Inrichtingselement_trig 
BEFORE INSERT ON top50nl.Inrichtingselement 
FOR EACH ROW

BEGIN
  SELECT Inrichtingselement_seq.NEXTVAL
  INTO   :new.id
  FROM   dual;
END;
/
--/
CREATE OR REPLACE TRIGGER Plaats_trig 
BEFORE INSERT ON top50nl.Plaats 
FOR EACH ROW

BEGIN
  SELECT Plaats_seq.NEXTVAL
  INTO   :new.id
  FROM   dual;
END;
/
--/
CREATE OR REPLACE TRIGGER RegistratiefGebied_trig 
BEFORE INSERT ON top50nl.RegistratiefGebied 
FOR EACH ROW

BEGIN
  SELECT RegistratiefGebied_seq.NEXTVAL
  INTO   :new.id
  FROM   dual;
END;
/
--/
CREATE OR REPLACE TRIGGER Relief_trig 
BEFORE INSERT ON top50nl.Relief 
FOR EACH ROW

BEGIN
  SELECT Relief_seq.NEXTVAL
  INTO   :new.id
  FROM   dual;
END;
/
--/
CREATE OR REPLACE TRIGGER Spoorbaandeel_trig 
BEFORE INSERT ON top50nl.Spoorbaandeel 
FOR EACH ROW

BEGIN
  SELECT Spoorbaandeel_seq.NEXTVAL
  INTO   :new.id
  FROM   dual;
END;
/
--/
CREATE OR REPLACE TRIGGER Terrein_trig 
BEFORE INSERT ON top50nl.Terrein 
FOR EACH ROW

BEGIN
  SELECT Terrein_seq.NEXTVAL
  INTO   :new.id
  FROM   dual;
END;
/
--/
CREATE OR REPLACE TRIGGER Waterdeel_trig 
BEFORE INSERT ON top50nl.Waterdeel 
FOR EACH ROW

BEGIN
  SELECT Waterdeel_seq.NEXTVAL
  INTO   :new.id
  FROM   dual;
END;
/
--/
CREATE OR REPLACE TRIGGER Wegdeel_trig 
BEFORE INSERT ON top50nl.Wegdeel 
FOR EACH ROW

BEGIN
  SELECT Wegdeel_seq.NEXTVAL
  INTO   :new.id
  FROM   dual;
END;
/