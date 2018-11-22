-- 
-- upgrade SQLserver STAGING datamodel van 1.6.3 naar 1.6.4 
--

-- PR#568 upgrade GDS2 module
ALTER TABLE laadproces ADD afgifteid varchar(255) null;
ALTER TABLE laadproces ADD afgiftereferentie varchar(255) null;
ALTER TABLE laadproces ADD artikelnummer varchar(255) null;
ALTER TABLE laadproces ADD beschikbaar_tot datetime null;
ALTER TABLE laadproces ADD bestandsreferentie varchar(255) null;
ALTER TABLE laadproces ADD contractafgiftenummer numeric(19,2) null;
ALTER TABLE laadproces ADD contractnummer varchar(255) null;
ALTER TABLE laadproces ADD klantafgiftenummer numeric(19,2) null;


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.6.3_naar_1.6.4','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.4' WHERE naam='brmoversie';
