create index brondocument_tabel_idx on brondocument(tabel);
create index brondocument_tabel_identif_idx on brondocument(tabel_identificatie);
create index brondocument_identificatie_idx on brondocument(identificatie);
CREATE INDEX BRONDOCUMENT_OMSCHRIJVING_IDX ON BRONDOCUMENT(OMSCHRIJVING);

CREATE INDEX brondocument_ref_id  ON brondocument (ref_id);

