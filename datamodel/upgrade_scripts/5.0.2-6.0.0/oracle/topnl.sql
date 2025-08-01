--
-- Verwijder Oracle TopNL datamodel voor upgrade van 5.0.2 naar 6.0.0
--
-- NB uit te voeren onder sys of een andere gebruiker met voldoende rechten in alle schemas, bijvoorbeeld de superuser
ALTER SESSION SET "_ORACLE_SCRIPT"=true;

-- Verwijder de TopNL schemas
DROP USER IF EXISTS TOPNL CASCADE;
DROP USER IF EXISTS TOP10NL CASCADE;
DROP USER IF EXISTS TOP50NL CASCADE;
DROP USER IF EXISTS TOP100NL CASCADE;
DROP USER IF EXISTS TOP250NL CASCADE;
