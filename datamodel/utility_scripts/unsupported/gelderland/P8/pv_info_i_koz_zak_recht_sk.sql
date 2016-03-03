CREATE OR REPLACE VIEW pv_info_i_koz_zak_recht_sk AS 
 SELECT koz.kad_identif AS koz_identif, zre.kadaster_identif, zre.eindd_recht, zre.indic_betrokken_in_splitsing, zre.ingangsdatum_recht, pso.sc_identif AS pso_identif, 
        CASE
            WHEN npe.sc_identif IS NOT NULL THEN 'Natuurlijk persoon'::text
            ELSE 'Niet natuurlijk persoon'::text
        END AS soort_eigenaar, npe.nm_geslachtsnaam AS geslachtsnaam, npe.nm_voorvoegsel_geslachtsnaam AS voorvoegsel, npe.nm_voornamen AS voornamen, npe.geslachtsaand AS geslacht, inp.va_loc_beschrijving AS woonadres, inp.gb_geboortedatum AS geboortedatum, inp.gb_geboorteplaats AS geboorteplaats, inp.ol_overlijdensdatum AS overlijdensdatum, nnp.nnp_naam AS naam_niet_natuurlijk_persoon, nnp.rechtsvorm, nnp.statutaire_zetel, nnp.kvk_nummer, zre.ar_noemer AS aandeel_noemer, zre.ar_teller AS aandeel_teller, zre.arv_omschr, zre.fk_3avr_aand
   FROM pv_kad_onroerende_zaak koz
   JOIN pv_zakelijk_recht zre ON zre.fk_7koz_kad_identif = koz.kad_identif
   LEFT JOIN pv_persoon pso ON pso.sc_identif::text = zre.fk_8pes_sc_identif::text
   LEFT JOIN pv_natuurlijk_persoon npe ON npe.sc_identif::text = pso.sc_identif::text
   LEFT JOIN pv_ingeschr_natuurlijk_persoon inp ON inp.sc_identif::text = pso.sc_identif::text
   LEFT JOIN pv_niet_natuurlijk_persoon nnp ON nnp.sc_identif::text = pso.sc_identif::text
  WHERE 1 = 1 AND (npe.nm_geslachtsnaam IS NOT NULL OR nnp.nnp_naam IS NOT NULL);

