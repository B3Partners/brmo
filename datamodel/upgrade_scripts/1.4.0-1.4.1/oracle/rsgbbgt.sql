--
-- upgrade RSGBBGT datamodel van 1.4.0 naar 1.4.1 (Oracle)
--
-- brmo versie informatie
CREATE TABLE BRMO_METADATA
    (
        NAAM VARCHAR2(255 CHAR) NOT NULL,
        WAARDE VARCHAR2(255 CHAR),
        PRIMARY KEY (NAAM)
    );
COMMENT ON TABLE BRMO_METADATA IS 'BRMO metadata en versie gegevens';

INSERT INTO brmo_metadata (naam, waarde) VALUES ('brmoversie','1.4.1');
