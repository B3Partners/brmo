--
-- upgrade RSGB datamodel van 1.4.0 naar 1.4.1 (PostgreSQL)
--
-- brmo versie informatie
CREATE TABLE brmo_metadata
    (
        naam CHARACTER VARYING(255) NOT NULL,
        waarde CHARACTER VARYING(255),
        CONSTRAINT brmo_metadata_pk PRIMARY KEY (naam)
    );
COMMENT ON TABLE brmo_metadata IS 'BRMO metadata en versie gegevens';

insert into brmo_metadata (naam, waarde) values ('brmoversie','1.4.1');
