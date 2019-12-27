-- 
-- upgrade PostgreSQL STAGING datamodel van 2.0.0 naar 2.0.1 
--




-- #761 vul de bestand_naam_hersteld uit afgifte gegevens
UPDATE
    laadproces
SET
    bestand_naam_hersteld = SUBSTRING(bestand_naam, 1, POSITION('.zip' IN bestand_naam)+ 4)
WHERE
    soort = 'brk'
    AND opmerking LIKE 'GDS2 download van%'
    AND bestand_naam_hersteld IS NULL;

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.0.0_naar_2.0.1','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.0.1' WHERE naam='brmoversie';
