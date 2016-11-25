--
-- upgrade STAGING datamodel van 1.4.0 naar 1.4.1 (PostgreSQL)
--
create index idx_bericht_soort on bericht (soort);
create index idx_bericht_status on bericht (status);
