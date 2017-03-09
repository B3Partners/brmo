-- 
-- upgrade SQLserver STAGING datamodel van 1.4.3 naar 1.4.4 
--

-- versienummer update
UPDATE brmo_metadata SET waarde='1.4.4' WHERE naam='brmoversie';
