-- utility script om kolombreedte aan te passen naar CHAR als de database
--   default op BYTE is ingesteld middels NLS_LENGTH_SEMANTICS optie.
--
-- zie: oa. https://github.com/B3Partners/brmo/issues/240
--   
-- Mogelijk zijn er meer tabellen/kolommen waar dit probleem naar voren komt, 
--   die kunnen dan hieronder worden toegevoegd
--
ALTER TABLE GEM_OPENB_RMTE MODIFY (STRAATNAAM VARCHAR2(24 CHAR));
