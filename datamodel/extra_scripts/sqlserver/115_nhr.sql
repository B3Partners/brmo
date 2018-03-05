
alter table sbi_activiteit alter column omschr varchar(255);

create table vestg_activiteit(
    fk_vestg_nummer varchar(32),
    fk_sbi_activiteit_code varchar(6),
    indicatie_hoofdactiviteit decimal(1,0),
    primary key(fk_vestg_nummer, fk_sbi_activiteit_code),
    constraint fkfk_vestg_nummer foreign key (fk_vestg_nummer) references vestg(sc_identif),
    constraint fkfk_sbi_activiteit_code foreign key (fk_sbi_activiteit_code) references sbi_activiteit(sbi_code)
);

ALTER TABLE vestg_naam
  ALTER COLUMN naam varchar(500) NOT NULL;

GO

ALTER TABLE vestg_naam
  ALTER COLUMN fk_ves_sc_identif varchar(32) NOT NULL;

GO

ALTER TABLE vestg_naam
  ADD CONSTRAINT pk_vestg_naam PRIMARY KEY (naam, fk_ves_sc_identif);
