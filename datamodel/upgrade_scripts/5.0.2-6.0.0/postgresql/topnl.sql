--
-- Verwijder PostgreSQL TopNL datamodel voor upgrade van 5.0.2 naar 6.0.0
--
-- NB uit te voeren door gebruiker met voldoende rechten in alle schemas, bijvoorbeeld de superuser

-- Verwijder de TopNL database en role
DROP DATABASE IF EXISTS topnl;
DROP ROLE IF EXISTS topnl;