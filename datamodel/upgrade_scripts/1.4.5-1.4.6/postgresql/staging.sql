-- 
-- upgrade PostgreSQL STAGING datamodel van 1.4.5 naar 1.4.6 
--

-- versienummer update
UPDATE brmo_metadata SET waarde='1.4.6' WHERE naam='brmoversie';
