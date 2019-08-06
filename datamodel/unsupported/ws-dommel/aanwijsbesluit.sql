create materialized view wdd.belastingplichtige as
with belastingplichtige as
    (
        with eigenaar as 
                (select
                       kp.sc_kad_identif
                ,      kp.begrenzing_perceel
                ,      kp.ka_kad_gemeentecode||kp.ka_sectie||kp.ka_perceelnummer as kadsleutel
                ,      kp.ka_kad_gemeentecode
                ,      kp.ka_sectie
                ,      kp.ka_perceelnummer
                ,      kp.ka_deelperceelnummer
                ,      kp.grootte_perceel
                ,      case
                         when zr.kadaster_identif like 'NL.KAD.Tenaamstelling%' then '1'
                         when zr.kadaster_identif like 'NL.KAD.ZakelijkRecht%' then '2'
                         else '3'
                       end as criterium_type
                ,      case
                         when avr.omschr_aard_verkregenr_recht = 'Eigendom (recht van)' then '1'
                         when avr.omschr_aard_verkregenr_recht = 'Vruchtgebruik (recht van)' then '2'
                         when avr.omschr_aard_verkregenr_recht = 'Opstal (recht van)' then '3'
                         when avr.omschr_aard_verkregenr_recht = 'Erfpacht (recht van)' then '4'
                         when avr.omschr_aard_verkregenr_recht like 'Gebruik%' then '5'
                         else '6'
                       end as criterium_zakelijkrecht
                ,      cast( (ar_teller / ar_noemer) as float) as criterium_aandeel
                ,      case 
                         when nnp.naam is not null then '1'
                         when prs.geslachtsaand = 'M' then '2'
                         when prs.geslachtsaand = 'V' then '3'
                         else '4'
                       end as criterium_rechthebbende
                ,      prs.geboortedatum as criterium_geboortedatum
                ,      avr.omschr_aard_verkregenr_recht
                ,      prs.nm_geslachtsnaam
                ,      prs.nm_voornamen
                ,      prs.nm_voorvoegsel_geslachtsnaam
                ,      prs.geslachtsaand
                ,      prs.geboortedatum
                ,      prs.overlijdensdatum
                ,      prs.sc_identif
                ,      prs.adres as adres_belastingplichtige_moment_akte
                ,      nnp.naam as bedrijfsnaam
                ,      case 
                         when nnp.naam is not null then nnp.sc_identif
                         else prs.sc_identif
                       end as bpl_identif
                from kad_onrrnd_zk oz
                join kad_perceel kp on oz.kad_identif = kp.sc_kad_identif
                join zak_recht zr on oz.kad_identif = zr.fk_7koz_kad_identif
                join aard_verkregen_recht avr on zr.fk_3avr_aand = avr.aand
                left join (select nps.sc_identif
                                ,      nps.nm_geslachtsnaam
                                --,      nps.nm_voornamen
                                ,      (SELECT  
                                          string_agg(initial, '') as initials
                                        FROM
                                          (
                                            SELECT 
                                              row_number() OVER (ORDER BY sc_identif) as recnum,
                                              substring(regexp_split_to_table(t.nm_voornamen, '\s+') FROM 1 FOR 1)||'.' as initial 
                                            FROM public.nat_prs as t
                                            WHERE t.sc_identif = nps.sc_identif
                                          ) t_init
                                        GROUP BY recnum) as nm_voornamen
                                ,      nps.nm_voorvoegsel_geslachtsnaam
                                ,      nps.geslachtsaand
                                ,      ps.geboortedatum
                                ,      ps.overlijdensdatum
                                ,      ps.adres
                                from nat_prs nps
                                join (select ips.sc_identif
                                ,      ips.gb_geboortedatum as geboortedatum
                                ,      ips.ol_overlijdensdatum as overlijdensdatum
                                ,      ips.va_loc_beschrijving as adres
                                from ingeschr_nat_prs ips
                                UNION
                                select aps.sc_identif
                                ,      aps.geboortedatum
                                ,      aps.overlijdensdatum
                                ,      'Adres onbekend' as adres
                                from ander_nat_prs aps) ps on nps.sc_identif = ps.sc_identif) prs on zr.fk_8pes_sc_identif = prs.sc_identif
                left join niet_nat_prs nnp on zr.fk_8pes_sc_identif = nnp.sc_identif
--                where zr.kadaster_identif like 'NL.KAD.Tenaamstelling%'
                order by kp.ka_kad_gemeentecode,kp.ka_sectie,kp.ka_perceelnummer,criterium_type,criterium_zakelijkrecht,criterium_aandeel desc,criterium_rechthebbende,criterium_geboortedatum
                )
        SELECT ROW_NUMBER() OVER(PARTITION BY eigenaar.ka_kad_gemeentecode
                                                     ,eigenaar.ka_sectie
                                                     ,eigenaar.ka_perceelnummer
                                                     ,eigenaar.ka_deelperceelnummer
                                          ORDER BY eigenaar.ka_kad_gemeentecode
                                                  ,eigenaar.ka_sectie
                                                  ,eigenaar.ka_perceelnummer
                                                  ,eigenaar.ka_deelperceelnummer
                                                  ,eigenaar.criterium_type
                                                  ,eigenaar.criterium_zakelijkrecht
                                                  ,eigenaar.criterium_aandeel desc
                                                  ,eigenaar.criterium_rechthebbende
                                                  ,eigenaar.criterium_geboortedatum
                 ) rij
        ,      *         
        from eigenaar
        order by eigenaar.ka_kad_gemeentecode,eigenaar.ka_sectie,eigenaar.ka_perceelnummer,eigenaar.ka_deelperceelnummer
)
select sc_kad_identif
,      begrenzing_perceel
,      kadsleutel
,      bpl_identif
,      ka_kad_gemeentecode
,      ka_sectie
,      ka_perceelnummer
,      ka_deelperceelnummer
,      grootte_perceel
,      criterium_type
,      criterium_zakelijkrecht
,      criterium_aandeel
,      criterium_rechthebbende
,      criterium_geboortedatum
,      omschr_aard_verkregenr_recht
,      overlijdensdatum
,      case 
         when nm_geslachtsnaam is not null THEN CONCAT_WS(' ',nm_voornamen,nm_voorvoegsel_geslachtsnaam,nm_geslachtsnaam)
         else bedrijfsnaam 
       end as naam_belastingplichtige    
from belastingplichtige
where rij = 1;


CREATE INDEX sidx_wdd_belastingplichtige_shape
  ON wdd.belastingplichtige
  USING gist
(begrenzing_perceel);

create index idx_wdd_belastingplichtige_kadsleutel
on wdd.belastingplichtige
(kadsleutel);

create index idx_wdd_belastingplichtige_bpl_identif
on wdd.belastingplichtige
(bpl_identif);


refresh MATERIALIZED VIEW wdd_belastingplichtige;