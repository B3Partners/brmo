--- koppel nevenvestigingen met hoofdvestiging
create or replace view v_kvk_hoofd_nevenvestiging as 
select      s.naam as hoofdvestigingnaam,
            coalesce(v.fk_15ond_kvk_nummer, v.fk_17mac_kvk_nummer)                                 as kvknummer,
            listagg(distinct s2.naam, ' | ' on overflow truncate) within group ( order by s2.naam) as nevenvestigingen,
            count(v2.sc_identif)                                                                   as aantal_nevenvestigingen
from vestg v
join subject s on v.sc_identif = s.identif
join vestg v2 on coalesce(v.fk_15ond_kvk_nummer, v.fk_17mac_kvk_nummer) = coalesce(v2.fk_15ond_kvk_nummer, v2.fk_17mac_kvk_nummer)
join subject s2 on v2.sc_identif = s2.identif
where v.hoofdvestiging = 'Ja' and v2.hoofdvestiging = 'Nee'
group by s.naam, coalesce(v.fk_15ond_kvk_nummer, v.fk_17mac_kvk_nummer);

-- koppel subject en vestg tabel met het adresseerbaarobject
create materialized view mb_kvk_adres as 
select  
        row_number() OVER ()::integer AS objectid,
        v.sc_identif,
        -- fk_15ond_kvk_nummer = kvknummer voor onderneming
        -- fk_17mac_kvk_nummer = kvknummer voor maatschappelijke activiteit (niet commercieel)
        to_char(coalesce(v.fk_15ond_kvk_nummer,v.fk_17mac_kvk_nummer))  as kvknummer,
        s.naam,
        v.hoofdvestiging,
        vhn.hoofdvestigingnaam,
        -- de sbi codes zijn gebruikt om de bedrijfsactiviteit te generaliseren
        --https://www.kvk.nl/over-het-handelsregister/overzicht-standaard-bedrijfsindeling-sbi-codes-voor-activiteiten/
        case
            when v.fk_sa_sbi_activiteit_sbi_code between '01%' and '04%' then 'Landbouw, bosbouw en visserij'
            when v.fk_sa_sbi_activiteit_sbi_code between '06%' and '09%' then 'Winning van delftstoffen'
            when v.fk_sa_sbi_activiteit_sbi_code between '10%' and '34%' then 'Industrie'
            when v.fk_sa_sbi_activiteit_sbi_code between '35%' and '36%' then 'Productie en distributie van en handel in elektriciteit, aardgas, stoom en gekoelde lucht '
            when v.fk_sa_sbi_activiteit_sbi_code between '36%' and '41%' then 'Winning en distributie van water; afval- en afvalwaterbeheer en sanering'
            when v.fk_sa_sbi_activiteit_sbi_code between '41%' and '45%' then 'Bouwnijverheid'
            when v.fk_sa_sbi_activiteit_sbi_code between '45%' and '49%' then 'Groot- en detailhandel'
            when v.fk_sa_sbi_activiteit_sbi_code between '49%' and '55%' then 'Vervoer en opslag'
            when v.fk_sa_sbi_activiteit_sbi_code between '55%' and '58%' then 'Logies-, maaltijd- en drankverstrekking'
            when v.fk_sa_sbi_activiteit_sbi_code between '58%' and '61%' then 'Activiteiten van uitgeverijen, omroepactiviteiten, en activiteiten op het gebied van productie en distributie van inhoud'
            when v.fk_sa_sbi_activiteit_sbi_code between '61%' and '64%' then 'Telecommunicatie, computerprogrammering en consultancy, informatica-infrastructuur en overige activiteiten'
            when v.fk_sa_sbi_activiteit_sbi_code between '64%' and '68%' then 'Financiële dienstverlening en verzekeringen'
            when v.fk_sa_sbi_activiteit_sbi_code between '68%' and '69%' then 'Exploitatie van en handel in onroerend goed'
            when v.fk_sa_sbi_activiteit_sbi_code between '69%' and '77%' then 'Wetenschappelijke en technische activiteiten en specialistische zakelijke dienstverlening'
            when v.fk_sa_sbi_activiteit_sbi_code between '77%' and '84%' then 'Verhuur van roerende goederen en overige zakelijke dienstverlening'
            when v.fk_sa_sbi_activiteit_sbi_code between '84%' and '85%' then 'Openbaar bestuur, overheidsdiensten en verplichte sociale verzekeringen'
            when v.fk_sa_sbi_activiteit_sbi_code between '85%' and '86%' then 'Onderwijs'
            when v.fk_sa_sbi_activiteit_sbi_code between '86%' and '90%' then 'Gezondheids- en welzijnszorg'
            when v.fk_sa_sbi_activiteit_sbi_code between '90%' and '94%' then 'Kunst, cultuur, sport en recreatie'
            when v.fk_sa_sbi_activiteit_sbi_code between '94%' and '97%' then 'Overige dienstverlening'
            when v.fk_sa_sbi_activiteit_sbi_code between '97%' and '99%' then 'Huishoudens als werkgever; niet-gedifferentieerde productie van goederen en diensten door huishoudens voor eigen gebruik'
            when v.fk_sa_sbi_activiteit_sbi_code like '99%' then 'Extraterritoriale organisaties en instanties'
            else 'Geen specifiek activiteit'
        end as activiteit,
        v.fk_sa_sbi_activiteit_sbi_code as sbi_code,
        sa.omschr,
        v.activiteit_omschr 	   as omschr_detail,
        v.typering,
        -- datum bedrijf
        v.datum_aanvang,
        v.datum_beeindiging,
        -- werknemer(s)
        v.fulltime_werkzame_mannen + v.parttime_werkzame_mannen as aantal_werknemers,
        v.fulltime_werkzame_mannen as aantal_fulltime_werknemers,
        v.parttime_werkzame_mannen as parttime_werknemers,
        -- adresgegevens
        coalesce(s.adres_binnenland, s.adres_buitenland) as adres,
        -- correspondentieadres
        replace(coalesce(coa.straatnaam, '') || ' ' || coalesce(to_char(coa.huisnummer), '') || coalesce(coa.huisletter, '') || coalesce(coa.huisnummertoevoeging, '') || 
        ' ' || coalesce(coa.postcode, '') || ' ' || coalesce(coa.woonplaats, ''), ' ', ' ') as correspondentieadres,
        -- contactgegevens
        s.emailadres,
        s.fax_nummer,
        s.telefoonnummer,
        s.website_url,
        -- adresseerbaarobjectidentificaties
        v.fk_20aoa_identif 		   as adresseerbaarobjectid,
        s.fk_15aoa_identif 		   as correspondentie_aoi,
        a.maaktdeeluitvan,
        a.geometrie_centroide      as geometrie
from vestg v 
-- Een vestiging kan meerdere activiteiten bevatten, deze staat in de vestg_activiteit tabel. 
left join sbi_activiteit sa on v.fk_sa_sbi_activiteit_sbi_code = sa.sbi_code
-- soms staat de naam niet in de vestg_naam tabel. Daarom is het noodzakelijk om de naam van 
-- de subject tabel ook te raadplegen
left join subject s on v.sc_identif = s.identif
-- koppeling met geometrie
left join mb_adresseerbaar_object_geometrie_bag a on v.fk_20aoa_identif = a.identificatie
-- koppeling met correspondentie adresgegevens en geometrie
left join mb_adresseerbaar_object_geometrie_bag coa on s.fk_15aoa_identif = coa.identificatie
-- voeg hoofdvestiging naam toe
left join v_kvk_hoofd_nevenvestiging vhn on coalesce(v.fk_15ond_kvk_nummer,v.fk_17mac_kvk_nummer) = vhn.kvknummer;

delete
from user_sdo_geom_metadata
where table_name = 'MB_KVK_ADRES';
insert into user_sdo_geom_metadata
values ('mb_kvk_adres', 'geometrie',
        MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
                            MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

CREATE INDEX mb_kvk_adres_geometrie_idx ON mb_kvk_adres (geometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX;
CREATE UNIQUE INDEX mb_kvk_adres_objectid ON mb_kvk_adres (objectid);

-- koppel pandgeometrieën aan de nHr-gegevens
create materialized view mb_kvk_pand as 
select  
        cast(rownum as integer)    as objectid,
        kvk.sc_identif,
		kvk.kvknummer,
		kvk.naam,
		kvk.hoofdvestiging,
		kvk.hoofdvestigingnaam,
		kvk.activiteit,
		kvk.sbi_code,
		kvk.omschr,
		kvk.omschr_detail,
		kvk.typering,
		kvk.datum_aanvang,
		kvk.datum_beeindiging,
		kvk.aantal_werknemers,
		kvk.aantal_fulltime_werknemers,
		kvk.parttime_werknemers,
		kvk.adres,
		kvk.correspondentieadres,
		kvk.emailadres,
		kvk.fax_nummer,
		kvk.telefoonnummer,
		kvk.website_url,
		kvk.adresseerbaarobjectid,
		kvk.correspondentie_aoi,
		kvk.maaktdeeluitvan,
		-- pandgeometrie
		vp.geometrie
from mb_kvk_adres kvk
-- koppel pand geometrie
join brmo_bag.v_pand_actueel vp on INSTR(',' || kvk.maaktdeeluitvan || ',', ',' || vp.identificatie || ',') > 0

delete
from user_sdo_geom_metadata
where table_name = 'MB_KVK_PAND';
insert into user_sdo_geom_metadata
values ('mb_kvk_pand', 'geometrie',
        MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
                            MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

CREATE INDEX mb_kvk_pand_geometrie_idx ON mb_kvk_pand (geometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX;
CREATE UNIQUE INDEX mb_kvk_pand_objectid ON mb_kvk_pand (objectid);

-- koppel BRK-gegevens en perceelgrenzen aan de nHr-gegevens
create materialized view mb_kvk_perceel as 
select  
		cast(rownum as integer)    as objectid,
		kvk.sc_identif,
		kvk.kvknummer,
		kvk.naam,
		kvk.hoofdvestiging,
		kvk.hoofdvestigingnaam,
		kvk.activiteit,
		kvk.sbi_code,
		kvk.omschr,
		kvk.omschr_detail,
		kvk.typering,
		kvk.datum_aanvang,
		kvk.datum_beeindiging,
		kvk.aantal_werknemers,
		kvk.aantal_fulltime_werknemers,
		kvk.parttime_werknemers,
		kvk.adres,
		kvk.correspondentieadres,
		kvk.emailadres,
		kvk.fax_nummer,
		kvk.telefoonnummer,
		kvk.website_url,
		kvk.adresseerbaarobjectid,
		kvk.correspondentie_aoi,
		-- BRK gegevens
		p.identificatie as perceelsidentificatie, 
		zrr.soort,
		zrr.kvk_nummer as kvk_eigenaar, 
		p.begrenzing_perceel as geometrie
from mb_kvk_adres kvk
-- koppel BRK gegevens
join brmo_brk.perceel p on SDO_RELATE(kvk.geometrie, p.begrenzing_perceel, 'mask=INSIDE querytype=window')='TRUE'
join brmo_brk.mb_zr_rechth zrr on p.identificatie = zrr.koz_identif;

delete
from user_sdo_geom_metadata
where table_name = 'MB_KVK_PERCEEL';
insert into user_sdo_geom_metadata
values ('mb_kvk_perceel', 'geometrie',
        MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
                            MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

CREATE INDEX mb_kvk_perceel_geometrie_idx ON mb_kvk_perceel (geometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX;
CREATE UNIQUE INDEX mb_kvk_perceel_objectid ON mb_kvk_perceel (objectid);
CREATE INDEX mb_kvk_perceel_perceelsidentificatie ON mb_kvk_perceel (perceelsidentificatie);
