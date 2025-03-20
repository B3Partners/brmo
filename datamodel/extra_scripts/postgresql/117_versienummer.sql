-- brmo versie informatie

CREATE TABLE brmo_metadata
    (
        naam CHARACTER VARYING(255) NOT NULL,
        waarde TEXT,
        CONSTRAINT brmo_metadata_pk PRIMARY KEY (naam)
    );
COMMENT ON TABLE brmo_metadata IS 'BRMO systeem metadata en versie gegevens';
