-- brmo versie informatie

CREATE TABLE brmo_metadata
    (
        naam CHARACTER VARYING(255) NOT NULL,
        waarde CHARACTER VARYING(255),
        CONSTRAINT brmo_metadata_pk PRIMARY KEY (naam)
    );
COMMENT ON TABLE brmo_metadata IS 'BRMO metadata en versie gegevens';
