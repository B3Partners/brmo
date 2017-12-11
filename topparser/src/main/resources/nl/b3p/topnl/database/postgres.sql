set session authorization topnl;
create schema top250nl;
create schema top100nl;
create schema top50nl;
create schema top10nl;

CREATE TABLE top250nl.Hoogte(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeHoogte text,
        geometrie Geometry (Geometry,28992),
        referentieVlak text,
        hoogte DOUBLE PRECISION,
        PRIMARY KEY (id)
);
/*

create table top250nl.Hoogte (nltype text,id serial not null,
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

CREATE TABLE top250nl.FunctioneelGebied(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeFunctioneelGebied text,
        soortnaam text,
        naamNL text,
        naamFries text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top250nl.Gebouw(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeGebouw text,
        status text,
        fysiekVoorkomen text,
        hoogteklasse text,
        hoogte DOUBLE PRECISION,
        soortnaam text,
        naam text,
        naamFries text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top250nl.GeografischGebied(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeGeografischGebied text,
        naamNL text,
        naamFries text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top250nl.Inrichtingselement(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeInrichtingselement text,
        soortnaam text,
        status text,
        hoogteniveau bigint,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top250nl.Plaats(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeGebied text,
        aantalInwoners bigint,
        naamOfficieel text,
        naamNL text,
        naamFries text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top250nl.RegistratiefGebied(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeRegistratiefGebied text,
        naamOfficieel text,
        naamNL text,
        naamFries text,
        nummer text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top250nl.Relief(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeRelief text,
        hoogteklasse text,
        geometrie geometry (LineString, 28992),
        taludLageZijde geometry (LineString, 28992),
        taludHogeZijde geometry (LineString, 28992),
        hoogteniveau bigint,
        PRIMARY KEY (id)
);

CREATE TABLE top250nl.Spoorbaandeel(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeInfrastructuur text,
        typeSpoorbaan text,
        fysiekVoorkomen text,
        spoorbreedte text,
        aantalSporen text,
        vervoerfunctie text,
        elektrificatie BOOLEAN,
        status text,
        brugnaam text,
        tunnelnaam text,
        baanvaknaam text,
        hoogteniveau bigint,
        geometrie geometry (LineString, 28992),
        PRIMARY KEY (id)
);

CREATE TABLE top250nl.Terrein(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeLandgebruik text,
        naam text,
        geometrie geometry (Polygon, 28992),
        PRIMARY KEY (id)
);

CREATE TABLE top250nl.Waterdeel(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeWater text,
        breedteklasse text,
        fysiekVoorkomen text,
        voorkomen text,
        getijdeinvloed BOOLEAN,
        vaarwegklasse text,
        naamOfficieel text,
        naamNL text,
        naamFries text,
        isBAGnaam BOOLEAN,
        sluisnaam text,
        brugnaam text,
        hoogteniveau bigint,
        functie text,
        hoofdAfwatering BOOLEAN,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top250nl.Wegdeel(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeInfrastructuur text,
        typeWeg text,
        hoofdverkeersgebruik text,
        fysiekVoorkomen text,
        verhardingsbreedteklasse text,
        gescheidenRijbaan BOOLEAN,
        verhardingstype text,
        aantalRijstroken bigint,
        hoogteniveau bigint,
        status text,
        naam text,
        isBAGnaam BOOLEAN,
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
        PRIMARY KEY (id)
);

CREATE TABLE top250nl.PlanTopografie(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typePlanTopografie text,
        naam text,
        PRIMARY KEY (id)
);

CREATE TABLE top100nl.Hoogte(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeHoogte text,
        geometrie Geometry (Geometry,28992),
        referentieVlak text,
        hoogte DOUBLE PRECISION,
        PRIMARY KEY (id)
);

CREATE TABLE top100nl.FunctioneelGebied(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeFunctioneelGebied text,
        soortnaam text,
        naamNL text,
        naamFries text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top100nl.Gebouw(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeGebouw text,
        status text,
        fysiekVoorkomen text,
        hoogteklasse text,
        hoogte DOUBLE PRECISION,
        soortnaam text,
        naam text,
        naamFries text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top100nl.GeografischGebied(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeGeografischGebied text,
        naamNL text,
        naamFries text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top100nl.Inrichtingselement(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeInrichtingselement text,
        soortnaam text,
        status text,
        hoogteniveau bigint,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top100nl.Plaats(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeGebied text,
        aantalInwoners bigint,
        naamOfficieel text,
        naamNL text,
        naamFries text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top100nl.RegistratiefGebied(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeRegistratiefGebied text,
        naamOfficieel text,
        naamNL text,
        naamFries text,
        nummer text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top100nl.Relief(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeRelief text,
        hoogteklasse text,
        geometrie geometry (LineString, 28992),
        taludLageZijde geometry (LineString, 28992),
        taludHogeZijde geometry (LineString, 28992),
        hoogteniveau bigint,
        PRIMARY KEY (id)
);

CREATE TABLE top100nl.Spoorbaandeel(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeInfrastructuur text,
        typeSpoorbaan text,
        fysiekVoorkomen text,
        spoorbreedte text,
        aantalSporen text,
        vervoerfunctie text,
        elektrificatie BOOLEAN,
        status text,
        brugnaam text,
        tunnelnaam text,
        baanvaknaam text,
        hoogteniveau bigint,
        geometrie geometry (LineString, 28992),
        PRIMARY KEY (id)
);

CREATE TABLE top100nl.Terrein(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeLandgebruik text,
        naam text,
        geometrie geometry (Polygon, 28992),
        PRIMARY KEY (id)
);

CREATE TABLE top100nl.Waterdeel(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeWater text,
        breedteklasse text,
        fysiekVoorkomen text,
        voorkomen text,
        getijdeinvloed BOOLEAN,
        vaarwegklasse text,
        naamOfficieel text,
        naamNL text,
        naamFries text,
        isBAGnaam BOOLEAN,
        sluisnaam text,
        brugnaam text,
        hoogteniveau bigint,
        functie text,
        hoofdAfwatering BOOLEAN,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top100nl.Wegdeel(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeInfrastructuur text,
        typeWeg text,
        hoofdverkeersgebruik text,
        fysiekVoorkomen text,
        verhardingsbreedteklasse text,
        gescheidenRijbaan BOOLEAN,
        verhardingstype text,
        aantalRijstroken bigint,
        hoogteniveau bigint,
        status text,
        naam text,
        isBAGnaam BOOLEAN,
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
        PRIMARY KEY (id)
);

CREATE TABLE top100nl.PlanTopografie(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typePlanTopografie text,
        naam text,
        PRIMARY KEY (id)
);

CREATE TABLE top50nl.Hoogte(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeHoogte text,
        geometrie Geometry (Geometry,28992),
        referentieVlak text,
        hoogte DOUBLE PRECISION,
        PRIMARY KEY (id)
);

CREATE TABLE top50nl.FunctioneelGebied(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeFunctioneelGebied text,
        soortnaam text,
        naamNL text,
        naamFries text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top50nl.Gebouw(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeGebouw text,
        status text,
        fysiekVoorkomen text,
        hoogteklasse text,
        hoogte DOUBLE PRECISION,
        soortnaam text,
        naam text,
        naamFries text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top50nl.GeografischGebied(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeGeografischGebied text,
        naamNL text,
        naamFries text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top50nl.Inrichtingselement(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeInrichtingselement text,
        soortnaam text,
        status text,
        hoogteniveau bigint,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top50nl.Plaats(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeGebied text,
        aantalInwoners bigint,
        naamOfficieel text,
        naamNL text,
        naamFries text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top50nl.RegistratiefGebied(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeRegistratiefGebied text,
        naamOfficieel text,
        naamNL text,
        naamFries text,
        nummer text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top50nl.Relief(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeRelief text,
        hoogteklasse text,
        geometrie geometry (LineString, 28992),
        taludLageZijde geometry (LineString, 28992),
        taludHogeZijde geometry (LineString, 28992),
        hoogteniveau bigint,
        PRIMARY KEY (id)
);

CREATE TABLE top50nl.Spoorbaandeel(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeInfrastructuur text,
        typeSpoorbaan text,
        fysiekVoorkomen text,
        spoorbreedte text,
        aantalSporen text,
        vervoerfunctie text,
        elektrificatie BOOLEAN,
        status text,
        brugnaam text,
        tunnelnaam text,
        baanvaknaam text,
        hoogteniveau bigint,
        geometrie geometry (LineString, 28992),
        PRIMARY KEY (id)
);

CREATE TABLE top50nl.Terrein(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeLandgebruik text,
        naam text,
        geometrie geometry (Polygon, 28992),
        PRIMARY KEY (id)
);

CREATE TABLE top50nl.Waterdeel(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeWater text,
        breedteklasse text,
        fysiekVoorkomen text,
        voorkomen text,
        getijdeinvloed BOOLEAN,
        vaarwegklasse text,
        naamOfficieel text,
        naamNL text,
        naamFries text,
        isBAGnaam BOOLEAN,
        sluisnaam text,
        brugnaam text,
        hoogteniveau bigint,
        functie text,
        hoofdAfwatering BOOLEAN,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top50nl.Wegdeel(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeInfrastructuur text,
        typeWeg text,
        hoofdverkeersgebruik text,
        fysiekVoorkomen text,
        verhardingsbreedteklasse text,
        gescheidenRijbaan BOOLEAN,
        verhardingstype text,
        aantalRijstroken bigint,
        hoogteniveau bigint,
        status text,
        naam text,
        isBAGnaam BOOLEAN,
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
        PRIMARY KEY (id)
);

CREATE TABLE top50nl.PlanTopografie(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typePlanTopografie text,
        naam text,
        PRIMARY KEY (id)
);

CREATE TABLE top10nl.Hoogte(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeHoogte text,
        geometrie Geometry (Geometry,28992),
        referentieVlak text,
        hoogte DOUBLE PRECISION,
        PRIMARY KEY (id)
);

CREATE TABLE top10nl.FunctioneelGebied(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeFunctioneelGebied text,
        soortnaam text,
        naamNL text,
        naamFries text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top10nl.Gebouw(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeGebouw text,
        status text,
        fysiekVoorkomen text,
        hoogteklasse text,
        hoogte DOUBLE PRECISION,
        soortnaam text,
        naam text,
        naamFries text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top10nl.GeografischGebied(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeGeografischGebied text,
        naamNL text,
        naamFries text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top10nl.Inrichtingselement(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeInrichtingselement text,
        soortnaam text,
        status text,
        hoogteniveau bigint,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top10nl.Plaats(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeGebied text,
        aantalInwoners bigint,
        naamOfficieel text,
        naamNL text,
        naamFries text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top10nl.RegistratiefGebied(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeRegistratiefGebied text,
        naamOfficieel text,
        naamNL text,
        naamFries text,
        nummer text,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top10nl.Relief(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeRelief text,
        hoogteklasse text,
        geometrie geometry (LineString, 28992),
        taludLageZijde geometry (LineString, 28992),
        taludHogeZijde geometry (LineString, 28992),
        hoogteniveau bigint,
        PRIMARY KEY (id)
);

CREATE TABLE top10nl.Spoorbaandeel(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeInfrastructuur text,
        typeSpoorbaan text,
        fysiekVoorkomen text,
        spoorbreedte text,
        aantalSporen text,
        vervoerfunctie text,
        elektrificatie BOOLEAN,
        status text,
        brugnaam text,
        tunnelnaam text,
        baanvaknaam text,
        hoogteniveau bigint,
        geometrie geometry (LineString, 28992),
        puntGeometrie geometry (Point, 28992),
        PRIMARY KEY (id)
);

CREATE TABLE top10nl.Terrein(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeLandgebruik text,
        naam text,
        geometrie geometry (Polygon, 28992),
        PRIMARY KEY (id)
);

CREATE TABLE top10nl.Waterdeel(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeWater text,
        breedteklasse text,
        fysiekVoorkomen text,
        voorkomen text,
        getijdeinvloed BOOLEAN,
        vaarwegklasse text,
        naamOfficieel text,
        naamNL text,
        naamFries text,
        isBAGnaam BOOLEAN,
        sluisnaam text,
        brugnaam text,
        hoogteniveau bigint,
        functie text,
        hoofdAfwatering BOOLEAN,
        geometrie Geometry (Geometry,28992),
        PRIMARY KEY (id)
);

CREATE TABLE top10nl.Wegdeel(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typeInfrastructuur text,
        typeWeg text,
        hoofdverkeersgebruik text,
        fysiekVoorkomen text,
        verhardingsbreedteklasse text,
        gescheidenRijbaan BOOLEAN,
        verhardingstype text,
        aantalRijstroken bigint,
        hoogteniveau bigint,
        status text,
        naam text,
        isBAGnaam BOOLEAN,
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
        PRIMARY KEY (id)
);

CREATE TABLE top10nl.PlanTopografie(
        topnltype text,
        id serial NOT NULL,
        identificatie text,
        brontype text,
        bronactualiteit TIMESTAMP without TIME zone,
        bronbeschrijving text,
        bronnauwkeurigheid DOUBLE PRECISION,
        objectBeginTijd TIMESTAMP without TIME zone,
        objectEindTijd TIMESTAMP without TIME zone,
        visualisatieCode INTEGER,
        typePlanTopografie text,
        geometrie Geometry (Geometry, 28992),
        naam text,
        PRIMARY KEY (id)
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
