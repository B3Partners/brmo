-- 
-- upgrade SQLserver RSGB datamodel van 1.6.3 naar 1.6.4 
--

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
CREATE TABLE zak_recht_archief
(
   kadaster_identif character varying(255) NOT NULL,
   eindd_recht character varying(255),
   indic_betrokken_in_splitsing character varying(255),
   ingangsdatum_recht character varying(19) NOT NULL,
   fk_7koz_kad_identif numeric(15,0),
   fk_8pes_sc_identif character varying(255),
   ar_noemer numeric(8,0),
   ar_teller numeric(8,0),
   fk_2aard_recht_verkort_aand character varying(4),
   fk_3avr_aand character varying(6),
   CONSTRAINT zak_recht_archief_pk PRIMARY KEY (kadaster_identif,ingangsdatum_recht)
);
 