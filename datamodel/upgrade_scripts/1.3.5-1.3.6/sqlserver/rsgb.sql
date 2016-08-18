-- upgrade RSGB datamodel van 1.3.5 naar 1.3.6 (MS SQLserver)

-- vergroten van het veld 'omschrijving' in de tabel brondocument van 40 naar 255 characters
ALTER TABLE
    brondocument
ALTER COLUMN
    omschrijving VARCHAR(255);
