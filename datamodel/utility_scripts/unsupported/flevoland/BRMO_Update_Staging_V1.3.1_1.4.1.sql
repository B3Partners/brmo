--
-- upgrade STAGING datamodel van 1.4.0 naar 1.4.1 (Oracle)
-- staging.sql
CREATE INDEX idx_bericht_soort ON bericht(soort);
CREATE INDEX idx_bericht_status ON bericht(status);

-- brmo versie informatie
CREATE TABLE BRMO_METADATA
    (
        NAAM VARCHAR2(255 CHAR) NOT NULL,
        WAARDE VARCHAR2(255 CHAR),
        PRIMARY KEY (NAAM)
    );
    
COMMENT ON TABLE BRMO_METADATA IS 'BRMO metadata en versie gegevens';

INSERT INTO brmo_metadata (naam, waarde) VALUES ('brmoversie','1.4.1');

