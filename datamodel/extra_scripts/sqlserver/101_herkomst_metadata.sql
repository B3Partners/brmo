create table herkomst_metadata (
	tabel character varying(255) not null,
	kolom character varying(255) not null,
	waarde character varying(255) not null,
	herkomst_br character varying(255) not null,
	datum datetime not null,
	primary key clustered(tabel, kolom, waarde, herkomst_br, datum)
);
