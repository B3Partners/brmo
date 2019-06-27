-- 
-- upgrade PostgreSQL STAGING datamodel van 1.6.3 naar 1.6.4 
--

-- PR#568 upgrade GDS2 module
ALTER TABLE laadproces ADD COLUMN afgifteid varchar(255);
ALTER TABLE laadproces ADD COLUMN afgiftereferentie varchar(255);
ALTER TABLE laadproces ADD COLUMN artikelnummer varchar(255);
ALTER TABLE laadproces ADD COLUMN beschikbaar_tot timestamp;
ALTER TABLE laadproces ADD COLUMN bestandsreferentie varchar(255);
ALTER TABLE laadproces ADD COLUMN contractafgiftenummer numeric(19, 0);
ALTER TABLE laadproces ADD COLUMN contractnummer varchar(255);
ALTER TABLE laadproces ADD COLUMN klantafgiftenummer numeric(19, 0);

-- PR#688 controle module
alter table laadproces add column bestand_naam_hersteld character varying(255);

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.6.3_naar_1.6.4','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.4' WHERE naam='brmoversie';
