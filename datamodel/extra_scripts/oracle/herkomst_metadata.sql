
create table herkomst_metadata (
    id number,
    tabel varchar2(255),
    kolom varchar2(255),
    waarde varchar2(255),
    herkomst_br varchar2(255),
    datum timestamp,
    primary key(tabel, kolom, waarde, herkomst_br, datum)
);
