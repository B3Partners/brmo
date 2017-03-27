-- 
-- upgrade Oracle STAGING datamodel van 1.4.4 naar 1.4.5 
--

-- versienummer update
UPDATE brmo_metadata SET waarde='1.4.5' WHERE naam='brmoversie';
