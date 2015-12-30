ALTER TABLE woz_waarde ALTER COLUMN fk_1woz_nummer DECIMAL(12,0) NOT NULL;
alter table woz_waarde add constraint woz_waarde_pk primary key (fk_1woz_nummer);

alter table woz_waarde_archief drop constraint ar_woz_waarde_pk;
ALTER TABLE woz_waarde_archief ALTER COLUMN fk_1woz_nummer DECIMAL(12,0) NOT NULL;
alter table woz_waarde_archief add constraint woz_waarde_archief_pk primary key(waardepeildatum,fk_1woz_nummer);
