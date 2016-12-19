-- 
-- upgrade STAGING datamodel van 1.4.1 naar 1.4.2 
--

-- versienummer update
UPDATE brmo_metadata SET waarde='1.4.2' WHERE naam='brmoversie';
