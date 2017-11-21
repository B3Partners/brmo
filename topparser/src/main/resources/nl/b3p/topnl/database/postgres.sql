set session authorization brmo;
create schema top250nl;
create schema top100nl;
create schema top50nl;
create schema top10nl;

create table top250nl.Hoogte (
    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    typeHoogte text,
    geometrie Geometry (Geometry,28992),
    referentieVlak text,
    hoogte double precision,
  primary key (id)
    
    );

/*
create table top250nl.Hoogte (
    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    type text,
    naam text,
  primary key (id)
    
    );
*/
create table top250nl.FunctioneelGebied(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeFunctioneelGebied text,
  soortnaam text,
  naamNL text,
  naamFries text,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top250nl.Gebouw(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

  typeGebouw text,
  status text,
  fysiekVoorkomen text,
  hoogteklasse text,
  hoogte double precision,
  soortnaam text,
  naam text,
  naamFries text,
    
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );



create table top250nl.GeografischGebied(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeGeografischGebied text,
  naamNL text,
  naamFries text,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top250nl.Inrichtingselement(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeInrichtingselement text,
  soortnaam text,
  status text,
  hoogteniveau bigint,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );



create table top250nl.Plaats(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeGebied text,
  aantalInwoners bigint,
  naamOfficieel text,
  naamNL text,
  naamFries text,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top250nl.RegistratiefGebied(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeRegistratiefGebied text,
  naamOfficieel text,
  naamNL text,
  naamFries text,
  nummer text,
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top250nl.Relief(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

  typeRelief text,
  hoogteklasse text,
  geometrie geometry (LineString, 28992),
  taludLageZijde geometry (LineString, 28992),
  taludHogeZijde geometry (LineString, 28992),
  hoogteniveau bigint,
  primary key (id)
  );

create table top250nl.Spoorbaandeel (

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,


  typeInfrastructuur text,
  typeSpoorbaan text,
  fysiekVoorkomen text,
  spoorbreedte text,
  aantalSporen text,
  vervoerfunctie text,
  elektrificatie Boolean,
  status text,
  brugnaam text,
  tunnelnaam text,
  baanvaknaam text,
  hoogteniveau bigint,
    
  geometrie geometry (LineString, 28992),
  primary key (id)
  );

create table top250nl.Terrein(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeLandgebruik text,
  naam text,
    
  geometrie geometry (Polygon, 28992),
  primary key (id)
  );


create table top250nl.Waterdeel (

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,


  typeWater text,
  breedteklasse text,
  fysiekVoorkomen text,
  voorkomen text,
  getijdeinvloed Boolean,
  vaarwegklasse text,
  naamOfficieel text,
  naamNL text,
  naamFries text,
  isBAGnaam Boolean,
  sluisnaam text,
  brugnaam text,
  hoogteniveau bigint,
  functie text,
  hoofdAfwatering Boolean,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top250nl.Wegdeel (

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,


  typeInfrastructuur text,
  typeWeg text,
  hoofdverkeersgebruik text,
  fysiekVoorkomen text,
  verhardingsbreedteklasse text,
  gescheidenRijbaan Boolean,
  verhardingstype text,
  aantalRijstroken bigint,
  hoogteniveau bigint,
  status text,
  naam text,
  isBAGnaam Boolean,
  aWegnummer text,
  nWegnummer text,
  eWegnummer text,
  sWegnummer text,
  afritnummer text,
  afritnaam text,
  knooppuntnaam text,
  brugnaam text,
  tunnelnaam text,

  geometrie Geometry (Geometry,28992),
  hartgeometrie Geometry (Geometry,28992),
  primary key (id)
  );

create table top250nl.PlanTopografie (
    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    typePlanTopografie text,
    naam text,
  primary key (id)
    
    );


create table top100nl.Hoogte (
    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    typeHoogte text,
    geometrie Geometry (Geometry,28992),
    referentieVlak text,
    hoogte double precision,
  primary key (id)
    
    );

create table top100nl.FunctioneelGebied(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeFunctioneelGebied text,
  soortnaam text,
  naamNL text,
  naamFries text,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top100nl.Gebouw(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

  typeGebouw text,
  status text,
  fysiekVoorkomen text,
  hoogteklasse text,
  hoogte double precision,
  soortnaam text,
  naam text,
  naamFries text,
    
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );



create table top100nl.GeografischGebied(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeGeografischGebied text,
  naamNL text,
  naamFries text,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top100nl.Inrichtingselement(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeInrichtingselement text,
  soortnaam text,
  status text,
  hoogteniveau bigint,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );



create table top100nl.Plaats(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeGebied text,
  aantalInwoners bigint,
  naamOfficieel text,
  naamNL text,
  naamFries text,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top100nl.RegistratiefGebied(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeRegistratiefGebied text,
  naamOfficieel text,
  naamNL text,
  naamFries text,
  nummer text,
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top100nl.Relief(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

  typeRelief text,
  hoogteklasse text,
  geometrie geometry (LineString, 28992),
  taludLageZijde geometry (LineString, 28992),
  taludHogeZijde geometry (LineString, 28992),
  hoogteniveau bigint,
  primary key (id)
  );

create table top100nl.Spoorbaandeel (

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,


  typeInfrastructuur text,
  typeSpoorbaan text,
  fysiekVoorkomen text,
  spoorbreedte text,
  aantalSporen text,
  vervoerfunctie text,
  elektrificatie Boolean,
  status text,
  brugnaam text,
  tunnelnaam text,
  baanvaknaam text,
  hoogteniveau bigint,
    
  geometrie geometry (LineString, 28992),
  primary key (id)
  );

create table top100nl.Terrein(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeLandgebruik text,
  naam text,
    
  geometrie geometry (Polygon, 28992),
  primary key (id)
  );


create table top100nl.Waterdeel (

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,


  typeWater text,
  breedteklasse text,
  fysiekVoorkomen text,
  voorkomen text,
  getijdeinvloed Boolean,
  vaarwegklasse text,
  naamOfficieel text,
  naamNL text,
  naamFries text,
  isBAGnaam Boolean,
  sluisnaam text,
  brugnaam text,
  hoogteniveau bigint,
  functie text,
  hoofdAfwatering Boolean,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top100nl.Wegdeel (

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,


  typeInfrastructuur text,
  typeWeg text,
  hoofdverkeersgebruik text,
  fysiekVoorkomen text,
  verhardingsbreedteklasse text,
  gescheidenRijbaan Boolean,
  verhardingstype text,
  aantalRijstroken bigint,
  hoogteniveau bigint,
  status text,
  naam text,
  isBAGnaam Boolean,
  aWegnummer text,
  nWegnummer text,
  eWegnummer text,
  sWegnummer text,
  afritnummer text,
  afritnaam text,
  knooppuntnaam text,
  brugnaam text,
  tunnelnaam text,

  geometrie Geometry (Geometry,28992),
  hartgeometrie Geometry (Geometry,28992),
  primary key (id)
  );

create table top100nl.PlanTopografie (
    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    typePlanTopografie text,
    naam text,
  primary key (id)
    
    );


create table top50nl.Hoogte (
    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    typeHoogte text,
    geometrie Geometry (Geometry,28992),
    referentieVlak text,
    hoogte double precision,
  primary key (id)
    
    );

create table top50nl.FunctioneelGebied(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeFunctioneelGebied text,
  soortnaam text,
  naamNL text,
  naamFries text,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top50nl.Gebouw(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

  typeGebouw text,
  status text,
  fysiekVoorkomen text,
  hoogteklasse text,
  hoogte double precision,
  soortnaam text,
  naam text,
  naamFries text,
    
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );



create table top50nl.GeografischGebied(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeGeografischGebied text,
  naamNL text,
  naamFries text,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top50nl.Inrichtingselement(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeInrichtingselement text,
  soortnaam text,
  status text,
  hoogteniveau bigint,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );



create table top50nl.Plaats(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeGebied text,
  aantalInwoners bigint,
  naamOfficieel text,
  naamNL text,
  naamFries text,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top50nl.RegistratiefGebied(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeRegistratiefGebied text,
  naamOfficieel text,
  naamNL text,
  naamFries text,
  nummer text,
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top50nl.Relief(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

  typeRelief text,
  hoogteklasse text,
  geometrie geometry (LineString, 28992),
  taludLageZijde geometry (LineString, 28992),
  taludHogeZijde geometry (LineString, 28992),
  hoogteniveau bigint,
  primary key (id)
  );

create table top50nl.Spoorbaandeel (

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,


  typeInfrastructuur text,
  typeSpoorbaan text,
  fysiekVoorkomen text,
  spoorbreedte text,
  aantalSporen text,
  vervoerfunctie text,
  elektrificatie Boolean,
  status text,
  brugnaam text,
  tunnelnaam text,
  baanvaknaam text,
  hoogteniveau bigint,
    
  geometrie geometry (LineString, 28992),
  primary key (id)
  );

create table top50nl.Terrein(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeLandgebruik text,
  naam text,
    
  geometrie geometry (Polygon, 28992),
  primary key (id)
  );


create table top50nl.Waterdeel (

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,


  typeWater text,
  breedteklasse text,
  fysiekVoorkomen text,
  voorkomen text,
  getijdeinvloed Boolean,
  vaarwegklasse text,
  naamOfficieel text,
  naamNL text,
  naamFries text,
  isBAGnaam Boolean,
  sluisnaam text,
  brugnaam text,
  hoogteniveau bigint,
  functie text,
  hoofdAfwatering Boolean,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top50nl.Wegdeel (

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,


  typeInfrastructuur text,
  typeWeg text,
  hoofdverkeersgebruik text,
  fysiekVoorkomen text,
  verhardingsbreedteklasse text,
  gescheidenRijbaan Boolean,
  verhardingstype text,
  aantalRijstroken bigint,
  hoogteniveau bigint,
  status text,
  naam text,
  isBAGnaam Boolean,
  aWegnummer text,
  nWegnummer text,
  eWegnummer text,
  sWegnummer text,
  afritnummer text,
  afritnaam text,
  knooppuntnaam text,
  brugnaam text,
  tunnelnaam text,

  geometrie Geometry (Geometry,28992),
  hartgeometrie Geometry (Geometry,28992),
  primary key (id)
  );

create table top50nl.PlanTopografie (
    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    typePlanTopografie text,
    naam text,
  primary key (id)
    
    );



create table top10nl.Hoogte (
    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    typeHoogte text,
    geometrie Geometry (Geometry,28992),
    referentieVlak text,
    hoogte double precision,
  primary key (id)
    
    );

create table top10nl.FunctioneelGebied(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeFunctioneelGebied text,
  soortnaam text,
  naamNL text,
  naamFries text,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top10nl.Gebouw(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

  typeGebouw text,
  status text,
  fysiekVoorkomen text,
  hoogteklasse text,
  hoogte double precision,
  soortnaam text,
  naam text,
  naamFries text,
    
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );



create table top10nl.GeografischGebied(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeGeografischGebied text,
  naamNL text,
  naamFries text,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top10nl.Inrichtingselement(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeInrichtingselement text,
  soortnaam text,
  status text,
  hoogteniveau bigint,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );



create table top10nl.Plaats(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeGebied text,
  aantalInwoners bigint,
  naamOfficieel text,
  naamNL text,
  naamFries text,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top10nl.RegistratiefGebied(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeRegistratiefGebied text,
  naamOfficieel text,
  naamNL text,
  naamFries text,
  nummer text,
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top10nl.Relief(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

  typeRelief text,
  hoogteklasse text,
  geometrie geometry (LineString, 28992),
  taludLageZijde geometry (LineString, 28992),
  taludHogeZijde geometry (LineString, 28992),
  hoogteniveau bigint,
  primary key (id)
  );

create table top10nl.Spoorbaandeel (

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,


  typeInfrastructuur text,
  typeSpoorbaan text,
  fysiekVoorkomen text,
  spoorbreedte text,
  aantalSporen text,
  vervoerfunctie text,
  elektrificatie Boolean,
  status text,
  brugnaam text,
  tunnelnaam text,
  baanvaknaam text,
  hoogteniveau bigint,
    
  geometrie geometry (LineString, 28992),
  puntGeometrie geometry (Point, 28992),
  primary key (id)
  );

create table top10nl.Terrein(

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    
  typeLandgebruik text,
  naam text,
    
  geometrie geometry (Polygon, 28992),
  primary key (id)
  );


create table top10nl.Waterdeel (

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,


  typeWater text,
  breedteklasse text,
  fysiekVoorkomen text,
  voorkomen text,
  getijdeinvloed Boolean,
  vaarwegklasse text,
  naamOfficieel text,
  naamNL text,
  naamFries text,
  isBAGnaam Boolean,
  sluisnaam text,
  brugnaam text,
  hoogteniveau bigint,
  functie text,
  hoofdAfwatering Boolean,
    
  geometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top10nl.Wegdeel (

    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,


  typeInfrastructuur text,
  typeWeg text,
  hoofdverkeersgebruik text,
  fysiekVoorkomen text,
  verhardingsbreedteklasse text,
  gescheidenRijbaan Boolean,
  verhardingstype text,
  aantalRijstroken bigint,
  hoogteniveau bigint,
  status text,
  naam text,
  isBAGnaam Boolean,
  aWegnummer text,
  nWegnummer text,
  eWegnummer text,
  sWegnummer text,
  afritnummer text,
  afritnaam text,
  knooppuntnaam text,
  brugnaam text,
  tunnelnaam text,

  geometrie Geometry (Geometry,28992),
  hartgeometrie Geometry (Geometry,28992),
  primary key (id)
  );


create table top10nl.PlanTopografie (
    topnltype text,
    id serial not null,
    identificatie text,
    brontype text,
    bronactualiteit timestamp without time zone,
    bronbeschrijving text,
    bronnauwkeurigheid double precision,
    objectBeginTijd timestamp without time zone,
    objectEindTijd timestamp without time zone,
    visualisatieCode integer,

    typePlanTopografie text,
    naam text,
  primary key (id)
    
    );



CREATE INDEX top250nl_hoogte_geometrie_idx ON top250nl.hoogte USING GIST ( geometrie);
CREATE INDEX top250nl_functioneelgebied_geometrie_idx ON top250nl.functioneelgebied USING GIST ( geometrie);
CREATE INDEX top250nl_gebouw_geometrie_idx ON top250nl.gebouw USING GIST ( geometrie);
CREATE INDEX top250nl_geografischgebied_geometrie_idx ON top250nl.geografischgebied USING GIST ( geometrie);
CREATE INDEX top250nl_inrichtingselement_geometrie_idx ON top250nl.inrichtingselement USING GIST ( geometrie);
CREATE INDEX top250nl_plaats_geometrie_idx ON top250nl.plaats USING GIST ( geometrie);
CREATE INDEX top250nl_registratiefgebied_geometrie_idx ON top250nl.registratiefgebied USING GIST ( geometrie);
CREATE INDEX top250nl_relief_geometrie_idx ON top250nl.relief USING GIST ( geometrie);
CREATE INDEX top250nl_relief_taludlagezijde_idx ON top250nl.relief USING GIST ( taludlagezijde);
CREATE INDEX top250nl_relief_taludhogezijde_idx ON top250nl.relief USING GIST ( taludhogezijde);
CREATE INDEX top250nl_spoorbaandeel_geometrie_idx ON top250nl.spoorbaandeel USING GIST ( geometrie);
CREATE INDEX top250nl_terrein_geometrie_idx ON top250nl.terrein USING GIST ( geometrie);
CREATE INDEX top250nl_waterdeel_geometrie_idx ON top250nl.waterdeel USING GIST ( geometrie);
CREATE INDEX top250nl_wegdeel_geometrie_idx ON top250nl.wegdeel USING GIST ( geometrie);
CREATE INDEX top250nl_wegdeel_hartgeometrie_idx ON top250nl.wegdeel USING GIST ( hartgeometrie);
CREATE INDEX top100nl_hoogte_geometrie_idx ON top100nl.hoogte USING GIST ( geometrie);
CREATE INDEX top100nl_functioneelgebied_geometrie_idx ON top100nl.functioneelgebied USING GIST ( geometrie);
CREATE INDEX top100nl_gebouw_geometrie_idx ON top100nl.gebouw USING GIST ( geometrie);
CREATE INDEX top100nl_geografischgebied_geometrie_idx ON top100nl.geografischgebied USING GIST ( geometrie);
CREATE INDEX top100nl_inrichtingselement_geometrie_idx ON top100nl.inrichtingselement USING GIST ( geometrie);
CREATE INDEX top100nl_plaats_geometrie_idx ON top100nl.plaats USING GIST ( geometrie);
CREATE INDEX top100nl_registratiefgebied_geometrie_idx ON top100nl.registratiefgebied USING GIST ( geometrie);
CREATE INDEX top100nl_relief_geometrie_idx ON top100nl.relief USING GIST ( geometrie);
CREATE INDEX top100nl_relief_taludlagezijde_idx ON top100nl.relief USING GIST ( taludlagezijde);
CREATE INDEX top100nl_relief_taludhogezijde_idx ON top100nl.relief USING GIST ( taludhogezijde);
CREATE INDEX top100nl_spoorbaandeel_geometrie_idx ON top100nl.spoorbaandeel USING GIST ( geometrie);
CREATE INDEX top100nl_terrein_geometrie_idx ON top100nl.terrein USING GIST ( geometrie);
CREATE INDEX top100nl_waterdeel_geometrie_idx ON top100nl.waterdeel USING GIST ( geometrie);
CREATE INDEX top100nl_wegdeel_geometrie_idx ON top100nl.wegdeel USING GIST ( geometrie);
CREATE INDEX top100nl_wegdeel_hartgeometrie_idx ON top100nl.wegdeel USING GIST ( hartgeometrie);
CREATE INDEX top50nl_hoogte_geometrie_idx ON top50nl.hoogte USING GIST ( geometrie);
CREATE INDEX top50nl_functioneelgebied_geometrie_idx ON top50nl.functioneelgebied USING GIST ( geometrie);
CREATE INDEX top50nl_gebouw_geometrie_idx ON top50nl.gebouw USING GIST ( geometrie);
CREATE INDEX top50nl_geografischgebied_geometrie_idx ON top50nl.geografischgebied USING GIST ( geometrie);
CREATE INDEX top50nl_inrichtingselement_geometrie_idx ON top50nl.inrichtingselement USING GIST ( geometrie);
CREATE INDEX top50nl_plaats_geometrie_idx ON top50nl.plaats USING GIST ( geometrie);
CREATE INDEX top50nl_registratiefgebied_geometrie_idx ON top50nl.registratiefgebied USING GIST ( geometrie);
CREATE INDEX top50nl_relief_geometrie_idx ON top50nl.relief USING GIST ( geometrie);
CREATE INDEX top50nl_relief_taludlagezijde_idx ON top50nl.relief USING GIST ( taludlagezijde);
CREATE INDEX top50nl_relief_taludhogezijde_idx ON top50nl.relief USING GIST ( taludhogezijde);
CREATE INDEX top50nl_spoorbaandeel_geometrie_idx ON top50nl.spoorbaandeel USING GIST ( geometrie);
CREATE INDEX top50nl_terrein_geometrie_idx ON top50nl.terrein USING GIST ( geometrie);
CREATE INDEX top50nl_waterdeel_geometrie_idx ON top50nl.waterdeel USING GIST ( geometrie);
CREATE INDEX top50nl_wegdeel_geometrie_idx ON top50nl.wegdeel USING GIST ( geometrie);
CREATE INDEX top50nl_wegdeel_hartgeometrie_idx ON top50nl.wegdeel USING GIST ( hartgeometrie);
CREATE INDEX top10nl_hoogte_geometrie_idx ON top10nl.hoogte USING GIST ( geometrie);
CREATE INDEX top10nl_functioneelgebied_geometrie_idx ON top10nl.functioneelgebied USING GIST ( geometrie);
CREATE INDEX top10nl_gebouw_geometrie_idx ON top10nl.gebouw USING GIST ( geometrie);
CREATE INDEX top10nl_geografischgebied_geometrie_idx ON top10nl.geografischgebied USING GIST ( geometrie);
CREATE INDEX top10nl_inrichtingselement_geometrie_idx ON top10nl.inrichtingselement USING GIST ( geometrie);
CREATE INDEX top10nl_plaats_geometrie_idx ON top10nl.plaats USING GIST ( geometrie);
CREATE INDEX top10nl_registratiefgebied_geometrie_idx ON top10nl.registratiefgebied USING GIST ( geometrie);
CREATE INDEX top10nl_relief_geometrie_idx ON top10nl.relief USING GIST ( geometrie);
CREATE INDEX top10nl_relief_taludlagezijde_idx ON top10nl.relief USING GIST ( taludlagezijde);
CREATE INDEX top10nl_relief_taludhogezijde_idx ON top10nl.relief USING GIST ( taludhogezijde);
CREATE INDEX top10nl_spoorbaandeel_geometrie_idx ON top10nl.spoorbaandeel USING GIST ( geometrie);
CREATE INDEX top10nl_terrein_geometrie_idx ON top10nl.terrein USING GIST ( geometrie);
CREATE INDEX top10nl_waterdeel_geometrie_idx ON top10nl.waterdeel USING GIST ( geometrie);
CREATE INDEX top10nl_wegdeel_geometrie_idx ON top10nl.wegdeel USING GIST ( geometrie);
CREATE INDEX top10nl_wegdeel_hartgeometrie_idx ON top10nl.wegdeel USING GIST ( hartgeometrie);
CREATE INDEX top10nl_plantopografie_geometrie_idx ON top10nl.plantopografie USING GIST ( geometrie);
