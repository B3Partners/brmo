CREATE INDEX zak_recht_fk_kad_identif_idx ON zak_recht (fk_7koz_kad_identif);
CREATE INDEX kad_onrrnd_zk_aantek_fk4_idx ON kad_onrrnd_zk_aantek (fk_4koz_kad_identif);
CREATE INDEX kad_perceel_id_idx ON kad_perceel (ka_kad_gemeentecode, ka_sectie, ka_perceelnummer);
CREATE INDEX zak_recht_aantek_fk5_zk_re_idx ON zak_recht_aantek (fk_5zkr_kadaster_identif);
