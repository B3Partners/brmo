-- 
-- upgrade Oracle STAGING datamodel van 1.6.3 naar 1.6.4 
--

-- PR#568 upgrade GDS2 module
ALTER TABLE laadproces ADD(afgifteid varchar2(255 char));
ALTER TABLE laadproces ADD(afgiftereferentie varchar2(255 char));
ALTER TABLE laadproces ADD(artikelnummer varchar2(255 char));
ALTER TABLE laadproces ADD(beschikbaar_tot timestamp);
ALTER TABLE laadproces ADD(bestandsreferentie varchar2(255 char));
ALTER TABLE laadproces ADD(contractafgiftenummer number(19,0));
ALTER TABLE laadproces ADD(contractnummer varchar2(255 char));
ALTER TABLE laadproces ADD(klantafgiftenummer number(19,0));

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.6.3_naar_1.6.4','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.4' WHERE naam='brmoversie';
