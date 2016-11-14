-- 117 rsgb versie informatie

create table brmo_metadata (
  naam varchar2(255),
  waarde varchar2(255),
  primary key(sleutel)
);

comment on table brmo_metadata is 'brmo metadata en versie gegevens';
comment on column brmo_metadata.naam is 'sleutel';
comment on column brmo_metadata.waarde is 'waarde voor de betreffende sleutel';

INSERT INTO brmo_metadata (naam, waarde) VALUES ('versie', '@project.version@');
