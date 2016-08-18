-- upgrade RSGB datamodel van 1.3.5 naar 1.3.6 (Oracle)

-- vergroten van het veld 'omschrijving' in de tabel brondocument van 40 naar 255 characters
--oracle
ALTER TABLE
    BRONDOCUMENT
MODIFY
    OMSCHRIJVING VARCHAR2(255);

-- hierna invalid views compileren