-- 
-- upgrade PostgreSQL RSGB datamodel van 1.4.2 naar 1.4.3 
--

-- versienummer update
UPDATE brmo_metadata SET waarde='1.4.3' WHERE naam='brmoversie';
