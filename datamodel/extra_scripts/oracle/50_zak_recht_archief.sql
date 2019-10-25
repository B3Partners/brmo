
CREATE TABLE zak_recht_archief
(
    kadaster_identif varchar2(255)NOT NULL,
    eindd_recht varchar2(255) ,
    indic_betrokken_in_splitsing varchar2(255) ,
    ingangsdatum_recht varchar2(19) NOT NULL,
    fk_7koz_kad_identif numeric(15,0),
    fk_8pes_sc_identif varchar2(255) ,
    ar_noemer numeric(8,0),
    ar_teller numeric(8,0),
    fk_2aard_recht_verkort_aand varchar2(4),
    fk_3avr_aand varchar2(6) ,
    CONSTRAINT zak_recht_archief_pk PRIMARY KEY (kadaster_identif,ingangsdatum_recht)
);
