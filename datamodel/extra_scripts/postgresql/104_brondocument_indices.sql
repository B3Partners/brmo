create index brondocument_tabel_idx on brondocument(tabel);
create index brondocument_tabel_identif_idx on brondocument(tabel_identificatie);
create index brondocument_identificatie_idx on brondocument(identificatie);
CREATE INDEX brondocument_omschrijving_idx ON brondocument(omschrijving);

CREATE INDEX brondocument_ref_id  ON brondocument (ref_id);

