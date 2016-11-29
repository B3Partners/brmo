
create table hoogte(
      id serial primary key,
      identificatie text,
      topnltype text,
      brontype text,
      bronactualiteit timestamp,
      bronbeschrijving text,
      bronnauwkeurigheid double precision,
      objectBeginTijd timestamp,
      objectEindTijd timestamp,
      visualisatieCode integer,
      typeHoogte text,
      referentieVlak text,
      hoogte double precision
);