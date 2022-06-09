-- 
-- upgrade PostgreSQL BAG datamodel van 2.3.0 naar 2.3.1 
--

CREATE SCHEMA IF NOT EXISTS bag;

SET search_path = bag,public;

create sequence objectid_seq;
alter table ligplaats alter column objectid set default nextval('objectid_seq');
alter table nummeraanduiding alter column objectid set default nextval('objectid_seq');
alter table openbareruimte alter column objectid set default nextval('objectid_seq');
alter table pand alter column objectid set default nextval('objectid_seq');
alter table standplaats alter column objectid set default nextval('objectid_seq');
alter table verblijfsobject alter column objectid set default nextval('objectid_seq');
alter table woonplaats alter column objectid set default nextval('objectid_seq');
drop sequence ligplaats_objectid_seq;
drop sequence nummeraanduiding_objectid_seq;
drop sequence openbareruimte_objectid_seq;
drop sequence pand_objectid_seq;
drop sequence standplaats_objectid_seq;
drop sequence verblijfsobject_objectid_seq;
drop sequence woonplaats_objectid_seq;
update ligplaats set objectid = nextval('objectid_seq');
update nummeraanduiding set objectid = nextval('objectid_seq');
update openbareruimte set objectid = nextval('objectid_seq');
update pand set objectid = nextval('objectid_seq');
update standplaats set objectid = nextval('objectid_seq');
update verblijfsobject set objectid = nextval('objectid_seq');
update woonplaats set objectid = nextval('objectid_seq');

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.3.0_naar_2.3.1','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.3.1' WHERE naam='brmoversie';
