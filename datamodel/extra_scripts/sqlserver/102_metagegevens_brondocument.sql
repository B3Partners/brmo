
-- Een brondocument wordt niet in de originele tabel opgenomen omdat dit een
-- 0..n relatie kan zijn en niet altijd een 0..1. 

-- In deze tabel wordt verwezen naar de tabel waarop het metagegeven brondocument
-- van toepassing is. Voor de identificatie van de rij van de tabel naar waar
-- wordt verwezen is een enkele kolom gebruikt: composite keys worden niet ondersteund.
-- Composite keys worden toch voornamelijk toegepast bij een combinate met datum
-- begin geldigheid.

create table brondocument (
  tabel varchar(30) not null,
  tabel_identificatie varchar(50) not null,
  identificatie varchar(50) not null,
  gemeente integer,
  omschrijving varchar(255),
  datum date,
  ref_id varchar(50),
  primary key clustered(tabel,tabel_identificatie,identificatie)
);
  
