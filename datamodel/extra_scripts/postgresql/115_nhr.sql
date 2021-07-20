
alter table functionaris alter column functionaristypering type character varying(255);

alter table sbi_activiteit alter column omschr type character varying(255);

create table vestg_activiteit(
    fk_vestg_nummer varchar(32) references vestg(sc_identif),
    fk_sbi_activiteit_code varchar(6) references sbi_activiteit(sbi_code),
    indicatie_hoofdactiviteit numeric(1,0),
    primary key(fk_vestg_nummer, fk_sbi_activiteit_code)
);

ALTER TABLE vestg_naam
  ADD PRIMARY KEY (naam, fk_ves_sc_identif);

-- voeg kolom nonmailing toe aan tabel maatschapp_activiteit
ALTER TABLE maatschapp_activiteit ADD nonmailing varchar(3) NULL;
COMMENT ON COLUMN maatschapp_activiteit.nonmailing IS 'nonMailing attribuut (niet-RSGB)';
