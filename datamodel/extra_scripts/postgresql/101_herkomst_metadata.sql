create table herkomst_metadata (
	tabel character varying(255),
	kolom character varying(255),
	waarde character varying(255),
	herkomst_br character varying(255),
	datum timestamp without time zone,
	primary key (tabel, kolom, waarde, herkomst_br, datum)
) with (
  	OIDS = FALSE
);

COMMENT ON TABLE herkomst_metadata IS 'BRMO bevat informatie over oorsprong van subject records';