-- brmo versie informatie

CREATE TABLE BRMO_METADATA
    (
        NAAM VARCHAR2(255 CHAR) NOT NULL,
        WAARDE CLOB,
        PRIMARY KEY (NAAM)
    );
COMMENT ON TABLE BRMO_METADATA IS 'BRMO systeem metadata en versie gegevens';
