/*
Views for visualizing the BRK data.
versie 2
23-04-2018
*/

--drop view v2_volledig_subject cascade;
--drop view v2_avg_volledig_subject cascade;
--drop view v2_util_app_re_kad_perceel cascade;
--drop view v2_kad_onrrnd_zk_locatie_adres cascade;
--drop view v2_volledig_subject;
--drop view v2_util_zk_recht cascade;
--drop view v2_zr_rechth cascade;
--drop view v2_avg_zr_rechth cascade;
--drop view v2_koz_rechth cascade;
--drop view v2_avg_koz_rechth cascade;

--drop view v2_avg_volledig_subject cascade;
CREATE OR REPLACE VIEW
    v2_avg_volledig_subject
    (
        identif,
        soort,
        geslachtsnaam,
        voorvoegsel,
        voornamen,
        aand_naamgebruik,
        geslachtsaand,
        naam,
        woonadres,
        geboortedatum,
        geboorteplaats,
        overlijdensdatum,
        bsn,
        organisatie_naam,
        rechtsvorm,
        statutaire_zetel,
        rsin,
        kvk_nummer
    ) AS
SELECT
    s.identif,
    s.soort,
    NULL::text         AS geslachtsnaam,
    NULL::text         AS voorvoegsel,
    NULL::text         AS voornamen,
    NULL::text         AS aand_naamgebruik,
    NULL::text         AS geslachtsaand,
    s.organisatie_naam AS naam,
    NULL::text         AS woonadres,
    NULL::text         AS geboortedatum,
    NULL::text         AS geboorteplaats,
    NULL::text         AS overlijdensdatum,
    NULL::text         AS bsn,
    s.organisatie_naam,
    s.rechtsvorm,
    s.statutaire_zetel,
    s.rsin,
    s.kvk_nummer
FROM
    v2_volledig_subject s;
COMMENT ON VIEW v2_avg_volledig_subject
IS
    'commentaar view v2_avg_volledig_subject:
volledig subject (natuurlijk en niet natuurlijk) geschoond voor avg
beschikbare kolommen:
* identif: natuurlijke id van subject      
* soort: soort subject zoals natuurlijk, niet-natuurlijk enz.  
* geslachtsnaam: NULL (avg)       
* voorvoegsel: NULL (avg)      
* voornamen: NULL (avg)       
* aand_naamgebruik: NULL (avg)         
* geslachtsaand:NULL (avg)     
* naam: gelijk aan organisatie_naam
* woonadres: NULL (avg)        
* geboortedatum: NULL (avg)        
* geboorteplaats: NULL (avg)        
* overlijdensdatum: NULL (avg)        
* bsn: NULL (avg)         
* organisatie_naam: naam niet natuurlijk subject      
* rechtsvorm: -  
* statutaire_zetel: -      
* rsin: -        
* kvk_nummer: -
';
    
--drop view v2_util_app_re_kad_perceel cascade;
CREATE OR REPLACE VIEW
    v2_util_app_re_kad_perceel
    (
        app_re_identif,
        perceel_identif
    ) AS
WITH
    RECURSIVE related_app_re
    (
        app_re_identif,
        perceel_identif
    ) AS
    (
        SELECT
            b1.ref_id AS app_re_identif,
            b2.ref_id AS perceel_identif
        FROM
            (brondocument b1
        JOIN
            brondocument b2
        ON
            (((
                        b2.identificatie)::text = (b1.identificatie)::text)))
        WHERE
            (((((
                                b1.omschrijving)::text = 'betrokkenBij Ondersplitsing'::text)
                    OR  ((
                                b2.omschrijving)::text = 'betrokkenBij HoofdSplitsing'::text))
                AND ((
                            b1.omschrijving)::text = 'ontstaanUit Ondersplitsing'::text))
            OR  (((
                            b2.omschrijving)::text = 'betrokkenBij HoofdSplitsing'::text)
                AND ((
                            b1.omschrijving)::text = 'ontstaanUit HoofdSplitsing'::text)))
        GROUP BY
            b1.ref_id,
            b2.ref_id
        UNION
        SELECT
            vaa.app_re_identif,
            vap.perceel_identif
        FROM
            (
            (
                SELECT
                    b10.ref_id AS app_re_identif,
                    b20.ref_id AS parent_app_re_identif
                FROM
                    (brondocument b10
                JOIN
                    brondocument b20
                ON
                    (((
                                b20.identificatie)::text = (b10.identificatie)::text)))
                WHERE
                    (((((
                                        b10.omschrijving)::text = 'betrokkenBij Ondersplitsing'::
                                    text)
                            OR  ((
                                        b20.omschrijving)::text = 'betrokkenBij HoofdSplitsing'::
                                    text))
                        AND ((
                                    b10.omschrijving)::text = 'ontstaanUit Ondersplitsing'::text))
                    OR  (((
                                    b20.omschrijving)::text = 'betrokkenBij HoofdSplitsing'::text)
                        AND ((
                                    b10.omschrijving)::text = 'ontstaanUit HoofdSplitsing'::text)))
                GROUP BY
                    b10.ref_id,
                    b20.ref_id) vaa
        JOIN
            related_app_re vap
        ON
            (((
                        vaa.parent_app_re_identif)::text = (vap.app_re_identif)::text)))
        GROUP BY
            vaa.app_re_identif,
            vap.perceel_identif
    )
SELECT
    (app_re.sc_kad_identif)::CHARACTER VARYING(50) AS app_re_identif,
    rar.perceel_identif
FROM
    (related_app_re rar
LEFT JOIN
    app_re
ON
    (((
                app_re.sc_kad_identif)::text = (rar.app_re_identif)::text)));
COMMENT ON VIEW v2_util_app_re_kad_perceel
IS
    'commentaar view v2_util_app_re_kad_perceel:
utility view, niet bedoeld voor direct gebruik, met lijst van appartementsrechten met bijbehorend grondperceel

beschikbare kolommen:
* app_re_identif: natuurlijk is van appartementsrecht,
* perceel_identif: natuurlijk id van grondperceel

';           

--drop view v2_kad_onrrnd_zk_locatie_adres cascade;
CREATE OR REPLACE VIEW
    v2_kad_onrrnd_zk_locatie_adres
    (
        objectid,
        identif,
        begin_geldigheid,
        benoemdobj_identif,
        type,
        sectie,
        perceelnummer,
        appartementsindex,
        gemeentecode,
        aand_soort_grootte,
        grootte_perceel,
        deelperceelnummer,
        omschr_deelperceel,
        verkoop_datum,
        aard_cultuur_onbebouwd,
        bedrag,
        koopjaar,
        meer_onroerendgoed,
        valutasoort,
        loc_omschr,
        gemeente,
        woonplaats,
        straatnaam,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        begrenzing_perceel
    ) AS
SELECT
    (row_number() OVER ())::INTEGER AS objectid,
    qry.identif,
    to_date(koz.dat_beg_geldh, 'YYYY-MM-DD'::text) AS begin_geldigheid,
    bok.fk_nn_lh_tgo_identif as benoemdobj_identif,
    qry.type,
    qry.ka_sectie,
    qry.ka_perceelnummer,
    qry.ka_appartementsindex,
    qry.ka_kad_gemeentecode,
    qry.aand_soort_grootte,
    qry.grootte_perceel,
    qry.ka_deelperceelnummer,
    qry.omschr_deelperceel,
    b.datum,
    koz.cu_aard_cultuur_onbebouwd,
    koz.ks_bedrag,
    koz.ks_koopjaar,
    koz.ks_meer_onroerendgoed,
    koz.ks_valutasoort,
    koz.lo_loc__omschr,
    bola.gemeente,
    bola.woonplaats,
    bola.straatnaam,
    bola.huisnummer,
    bola.huisletter,
    bola.huisnummer_toev,
    bola.postcode,
    qry.begrenzing_perceel
FROM
    (
        SELECT
            p.sc_kad_identif AS identif,
            'perceel'::text  AS type,
            p.ka_sectie,
            p.ka_perceelnummer,
            NULL::CHARACTER VARYING(4) AS ka_appartementsindex,
            p.ka_kad_gemeentecode,
            p.aand_soort_grootte,
            p.grootte_perceel,
            p.ka_deelperceelnummer,
            p.omschr_deelperceel,
            p.begrenzing_perceel
        FROM
            kad_perceel p
        UNION ALL
        SELECT
            ar.sc_kad_identif   AS identif,
            'appartement'::text AS type,
            ar.ka_sectie,
            ar.ka_perceelnummer,
            ar.ka_appartementsindex,
            ar.ka_kad_gemeentecode,
            NULL::CHARACTER VARYING(1)    AS aand_soort_grootte,
            NULL::NUMERIC(8,0)            AS grootte_perceel,
            NULL::CHARACTER VARYING(4)    AS ka_deelperceelnummer,
            NULL::CHARACTER VARYING(1120) AS omschr_deelperceel,
            kp.begrenzing_perceel
        FROM
            ((v2_util_app_re_kad_perceel v
        JOIN
            kad_perceel kp
        ON
            (((
                        v.perceel_identif)::NUMERIC = kp.sc_kad_identif)))
        JOIN
            app_re ar
        ON
            (((
                        v.app_re_identif)::NUMERIC = ar.sc_kad_identif)))) qry
JOIN
    kad_onrrnd_zk koz
ON
    (
        koz.kad_identif = qry.identif)
LEFT JOIN
    benoemd_obj_kad_onrrnd_zk bok
ON
    (
        bok.fk_nn_rh_koz_kad_identif = qry.identif)
LEFT JOIN
    v2_benoemd_obj_locatie_adres bola
ON
    bok.fk_nn_lh_tgo_identif = bola.identif
LEFT JOIN
    (
        SELECT
            brondocument.ref_id,
            MAX(brondocument.datum) AS datum
        FROM
            brondocument
        WHERE
            ((
                    brondocument.omschrijving)::text = 'Akte van Koop en Verkoop'::text)
        GROUP BY
            brondocument.ref_id) b
ON
    (
        koz.kad_identif::text = b.ref_id);    
COMMENT ON VIEW v2_kad_onrrnd_zk_locatie_adres
IS
    'commentaar view v2_kad_onrrnd_zk_locatie_adres:
alle kadastrale onroerende zaken (perceel en appartementsrecht) met opgezochte verkoop datum, objectid voor arcgis en BAG adres

beschikbare kolommen:
* objectid: uniek id bruikbaar voor arcgis,
* identif: natuurlijke id van perceel of appartementsrecht      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* benoemdobj_identif: koppeling met BAG object,
* type: perceel of appartement,
* sectie: -,
* perceelnummer: -,
* appartementsindex: -,
* gemeentecode: -,
* aand_soort_grootte: -,
* grootte_perceel: -,
* deelperceelnummer: -,
* omschr_deelperceel: -,
* verkoop_datum: laatste datum gevonden akten van verkoop,
* aard_cultuur_onbebouwd: -,
* bedrag: -,
* koopjaar: -,
* meer_onroerendgoed: -,
* valutasoort: -,
* loc_omschr: adres buiten BAG om meegegeven,
* gemeente: -,
* woonplaats: -,
* straatnaam: -,
* huisnummer: -,
* huisletter: -,
* huisnummer_toev: -,
* postcode: -,
* begrenzing_perceel: perceelvlak
';

        
--drop view v2_util_zk_recht cascade;
CREATE OR REPLACE VIEW
    v2_util_zk_recht
    (
        identif,
        aandeel,
        ar_teller,
        ar_noemer,
        subject_identif,
        koz_identif,
        indic_betrokken_in_splitsing,
        omschr_aard_verkregenr_recht,
        fk_3avr_aand
    ) AS
SELECT
    zr.kadaster_identif AS identif,
    ((COALESCE((zr.ar_teller)::text, ('0'::CHARACTER VARYING)::text) || ('/'::CHARACTER VARYING)::
    text) || COALESCE((zr.ar_noemer)::text, ('0'::CHARACTER VARYING)::text)) AS aandeel,
    zr.ar_teller,
    zr.ar_noemer,
    zr.fk_8pes_sc_identif  AS subject_identif,
    zr.fk_7koz_kad_identif AS koz_identif,
    zr.indic_betrokken_in_splitsing,
    avr.omschr_aard_verkregenr_recht,
    zr.fk_3avr_aand
FROM
    (zak_recht zr
JOIN
    aard_verkregen_recht avr
ON
    (((
                zr.fk_3avr_aand)::text = (avr.aand)::text)));
COMMENT ON VIEW v2_util_zk_recht
IS
    'commentaar view v2_util_zk_recht:
zakelijk recht met opgezocht aard recht en berekend aandeel

beschikbare kolommen:
* identif: natuurlijke id van zakelijk recht     
* aandeel: samenvoeging van teller en noemer (1/2),
* ar_teller: teller van aandeel,
* ar_noemer: noemer van aandeel,
* subject_identif: natuurlijk id van subject (natuurlijk of niet natuurlijk) welke rechthebbende is,
* koz_identif: natuurlijk id van kadastrale onroerende zaak (perceel of appratementsrecht) dat gekoppeld is,
* indic_betrokken_in_splitsing: -,
* omschr_aard_verkregenr_recht: tekstuele omschrijving aard recht,
* fk_3avr_aand: code aard recht
';
                
--drop view v2_zr_rechth cascade;
CREATE OR REPLACE VIEW
    v2_zr_rechth
    (
        identif,
        subject_identif,
        koz_identif,
        aandeel,
        omschr_aard_verkregenr_recht,
        indic_betrokken_in_splitsing,
        soort,
        geslachtsnaam,
        voorvoegsel,
        voornamen,
        aand_naamgebruik,
        geslachtsaand,
        naam,
        woonadres,
        geboortedatum,
        geboorteplaats,
        overlijdensdatum,
        bsn,
        organisatie_naam,
        rechtsvorm,
        statutaire_zetel,
        rsin,
        kvk_nummer
    ) AS
SELECT
    uzr.identif,
    uzr.subject_identif,
    uzr.koz_identif,
    uzr.aandeel,
    uzr.omschr_aard_verkregenr_recht,
    uzr.indic_betrokken_in_splitsing,
    vs.soort,
    vs.geslachtsnaam,
    vs.voorvoegsel,
    vs.voornamen,
    vs.aand_naamgebruik,
    vs.geslachtsaand,
    vs.naam,
    vs.woonadres,
    vs.geboortedatum,
    vs.geboorteplaats,
    vs.overlijdensdatum,
    vs.bsn,
    vs.organisatie_naam,
    vs.rechtsvorm,
    vs.statutaire_zetel,
    vs.rsin,
    vs.kvk_nummer
FROM
    (v2_util_zk_recht uzr
JOIN
    v2_volledig_subject vs
ON
    (((
                uzr.subject_identif)::text = (vs.identif)::text)));
COMMENT ON VIEW v2_zr_rechth
IS
    'commentaar view v2_zr_rechth:
alle zakelijke rechten met rechthebbenden en referentie naar kadastraal onroerende zaak (perceel of appartementsrecht)

beschikbare kolommen:
* identif: natuurlijke id van zakelijk recht 
* subject_identif: natuurlijk id van subject (natuurlijk of niet natuurlijk) welke rechthebbende is,
* koz_identif: natuurlijk id van kadastrale onroerende zaak (perceel of appratementsrecht) dat gekoppeld is,
* aandeel: samenvoeging van teller en noemer (1/2),
* omschr_aard_verkregenr_recht: tekstuele omschrijving aard recht,
* indic_betrokken_in_splitsing: -,
* soort: soort subject zoals natuurlijk, niet-natuurlijk enz.  
* geslachtsnaam: -       
* voorvoegsel: -     
* voornamen: -     
* aand_naamgebruik:
        - E (= Eigen geslachtsnaam)
        - N (=Geslachtsnaam echtgenoot/geregistreerd partner na eigen geslachtsnaam)
        - P (= Geslachtsnaam echtgenoot/geregistreerd partner)
        - V (= Geslachtsnaam evhtgenoot/geregistreerd partner voor eigen geslachtsnaam)        
* geslachtsaand: M/V   
* naam: samengestelde naam bruikbaar voor natuurlijke en niet-natuurlijke subjecten
* woonadres: meegeleverd adres buiten BAG koppeling om      
* geboortedatum: -       
* geboorteplaats: -       
* overlijdensdatum: -       
* bsn: -       
* organisatie_naam: naam niet natuurlijk subject      
* rechtsvorm: -  
* statutaire_zetel: -      
* rsin: -        
* kvk_nummer: -
';
                
--drop view v2_avg_zr_rechth cascade;
CREATE OR REPLACE VIEW
    v2_avg_zr_rechth
    (
        identif,
        subject_identif,
        koz_identif,
        aandeel,
        omschr_aard_verkregenr_recht,
        indic_betrokken_in_splitsing,
        soort,
        geslachtsnaam,
        voorvoegsel,
        voornamen,
        aand_naamgebruik,
        geslachtsaand,
        naam,
        woonadres,
        geboortedatum,
        geboorteplaats,
        overlijdensdatum,
        bsn,
        organisatie_naam,
        rechtsvorm,
        statutaire_zetel,
        rsin,
        kvk_nummer
    ) AS
SELECT
    uzr.identif,
    uzr.subject_identif,
    uzr.koz_identif,
    uzr.aandeel,
    uzr.omschr_aard_verkregenr_recht,
    uzr.indic_betrokken_in_splitsing,
    vs.soort,
    vs.geslachtsnaam,
    vs.voorvoegsel,
    vs.voornamen,
    vs.aand_naamgebruik,
    vs.geslachtsaand,
    vs.naam,
    vs.woonadres,
    vs.geboortedatum,
    vs.geboorteplaats,
    vs.overlijdensdatum,
    vs.bsn,
    vs.organisatie_naam,
    vs.rechtsvorm,
    vs.statutaire_zetel,
    vs.rsin,
    vs.kvk_nummer
FROM
    (v2_util_zk_recht uzr
JOIN
    v2_avg_volledig_subject vs
ON
    (((
                uzr.subject_identif)::text = (vs.identif)::text)));
COMMENT ON VIEW v2_avg_zr_rechth
IS
    'commentaar view v2_avg_zr_rechth:
alle zakelijke rechten met voor avg geschoonde rechthebbenden en referentie naar kadastraal onroerende zaak (perceel of appartementsrecht)

beschikbare kolommen:
* identif: natuurlijke id van zakelijk recht     
* subject_identif: natuurlijk id van subject (natuurlijk of niet natuurlijk) welke rechthebbende is,
* koz_identif: natuurlijk id van kadastrale onroerende zaak (perceel of appratementsrecht) dat gekoppeld is,
* aandeel: samenvoeging van teller en noemer (1/2),
* omschr_aard_verkregenr_recht: tekstuele omschrijving aard recht,
* indic_betrokken_in_splitsing: -,
* soort: soort subject zoals natuurlijk, niet-natuurlijk enz.  
* geslachtsnaam: NULL (avg)       
* voorvoegsel: NULL (avg)      
* voornamen: NULL (avg)       
* aand_naamgebruik: NULL (avg)         
* geslachtsaand:NULL (avg)     
* naam: gelijk aan organisatie_naam
* woonadres: NULL (avg)        
* geboortedatum: NULL (avg)        
* geboorteplaats: NULL (avg)        
* overlijdensdatum: NULL (avg)        
* bsn: NULL (avg)         
* organisatie_naam: naam niet natuurlijk subject      
* rechtsvorm: -  
* statutaire_zetel: -      
* rsin: -        
* kvk_nummer: -

';
                
--drop view v2_koz_rechth cascade;
CREATE OR REPLACE VIEW
    v2_koz_rechth
    (
        objectid,
        identif,
        begin_geldigheid,
        type,
        sectie,
        perceelnummer,
        appartementsindex,
        gemeentecode,
        aand_soort_grootte,
        grootte_perceel,
        deelperceelnummer,
        omschr_deelperceel,
        verkoop_datum,
        aard_cultuur_onbebouwd,
        bedrag,
        koopjaar,
        meer_onroerendgoed,
        valutasoort,
        loc_omschr,
        subject_identif,
        aandeel,
        omschr_aard_verkregenr_recht,
        indic_betrokken_in_splitsing,
        soort,
        geslachtsnaam,
        voorvoegsel,
        voornamen,
        aand_naamgebruik,
        geslachtsaand,
        naam,
        woonadres,
        geboortedatum,
        geboorteplaats,
        overlijdensdatum,
        bsn,
        organisatie_naam,
        rechtsvorm,
        statutaire_zetel,
        rsin,
        kvk_nummer,
        gemeente,
        woonplaats,
        straatnaam,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        begrenzing_perceel
    ) AS
SELECT
    koz.objectid,
    koz.identif,
    koz.begin_geldigheid,
    koz.type,
    koz.sectie,
    koz.perceelnummer,
    koz.appartementsindex,
    koz.gemeentecode,
    koz.aand_soort_grootte,
    koz.grootte_perceel,
    koz.deelperceelnummer,
    koz.omschr_deelperceel,
    koz.verkoop_datum,
    koz.aard_cultuur_onbebouwd,
    koz.bedrag,
    koz.koopjaar,
    koz.meer_onroerendgoed,
    koz.valutasoort,
    koz.loc_omschr,
    zrr.subject_identif,
    zrr.aandeel,
    zrr.omschr_aard_verkregenr_recht,
    zrr.indic_betrokken_in_splitsing,
    zrr.soort,
    zrr.geslachtsnaam,
    zrr.voorvoegsel,
    zrr.voornamen,
    zrr.aand_naamgebruik,
    zrr.geslachtsaand,
    zrr.naam,
    zrr.woonadres,
    zrr.geboortedatum,
    zrr.geboorteplaats,
    zrr.overlijdensdatum,
    zrr.bsn,
    zrr.organisatie_naam,
    zrr.rechtsvorm,
    zrr.statutaire_zetel,
    zrr.rsin,
    zrr.kvk_nummer,
    koz.gemeente,
    koz.woonplaats,
    koz.straatnaam,
    koz.huisnummer,
    koz.huisletter,
    koz.huisnummer_toev,
    koz.postcode,
    koz.begrenzing_perceel
FROM
    (v2_zr_rechth zrr
right JOIN
    v2_kad_onrrnd_zk_locatie_adres koz
ON
    ((
            zrr.koz_identif = koz.identif)));
COMMENT ON VIEW v2_koz_rechth
IS
    'commentaar view v2_koz_rechth:
kadastrale percelen een appartementsrechten met rechten en rechthebbenden en objectid voor arcgis
beschikbare kolommen:
* objectid: uniek id bruikbaar voor arcgis,
* identif: natuurlijke id van perceel of appartementsrecht      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* type: perceel of appartement,
* sectie: -,
* perceelnummer: -,
* appartementsindex: -,
* gemeentecode: -,
* aand_soort_grootte: -,
* grootte_perceel: -,
* deelperceelnummer: -,
* omschr_deelperceel: -,
* verkoop_datum: laatste datum gevonden akten van verkoop,
* aard_cultuur_onbebouwd: -,
* bedrag: -,
* koopjaar: -,
* meer_onroerendgoed: -,
* valutasoort: -,
* loc_omschr: adres buiten BAG om meegegeven,
* subject_identif: natuurlijk id van rechthebbende,
* aandeel: samenvoeging van teller en noemer (1/2),
* omschr_aard_verkregenr_recht: tekstuele omschrijving aard recht,
* indic_betrokken_in_splitsing: -,
* soort: soort subject zoals natuurlijk, niet-natuurlijk enz.  
* geslachtsnaam: -       
* voorvoegsel: -     
* voornamen: -     
* aand_naamgebruik:
        - E (= Eigen geslachtsnaam)
        - N (=Geslachtsnaam echtgenoot/geregistreerd partner na eigen geslachtsnaam)
        - P (= Geslachtsnaam echtgenoot/geregistreerd partner)
        - V (= Geslachtsnaam evhtgenoot/geregistreerd partner voor eigen geslachtsnaam)        
* geslachtsaand: M/V   
* naam: samengestelde naam bruikbaar voor natuurlijke en niet-natuurlijke subjecten
* woonadres: meegeleverd adres buiten BAG koppeling om      
* geboortedatum: -       
* geboorteplaats: -       
* overlijdensdatum: -       
* bsn: -       
* organisatie_naam: naam niet natuurlijk subject      
* rechtsvorm: -  
* statutaire_zetel: -      
* rsin: -        
* kvk_nummer: -
* gemeente: -,
* woonplaats: -,
* straatnaam: -,
* huisnummer: -,
* huisletter: -,
* huisnummer_toev: -,
* postcode: -,
* begrenzing_perceel: perceelvlak
';
            
--drop view v2_avg_koz_rechth cascade;
CREATE OR REPLACE VIEW
    v2_avg_koz_rechth
    (
        objectid,
        identif,
        begin_geldigheid,
        type,
        sectie,
        perceelnummer,
        appartementsindex,
        gemeentecode,
        aand_soort_grootte,
        grootte_perceel,
        deelperceelnummer,
        omschr_deelperceel,
        verkoop_datum,
        aard_cultuur_onbebouwd,
        bedrag,
        koopjaar,
        meer_onroerendgoed,
        valutasoort,
        loc_omschr,
        subject_identif,
        aandeel,
        omschr_aard_verkregenr_recht,
        indic_betrokken_in_splitsing,
        soort,
        geslachtsnaam,
        voorvoegsel,
        voornamen,
        aand_naamgebruik,
        geslachtsaand,
        naam,
        woonadres,
        geboortedatum,
        geboorteplaats,
        overlijdensdatum,
        bsn,
        organisatie_naam,
        rechtsvorm,
        statutaire_zetel,
        rsin,
        kvk_nummer,
        gemeente,
        woonplaats,
        straatnaam,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        begrenzing_perceel
    ) AS
SELECT
    koz.objectid,
    koz.identif,
    koz.begin_geldigheid,
    koz.type,
    koz.sectie,
    koz.perceelnummer,
    koz.appartementsindex,
    koz.gemeentecode,
    koz.aand_soort_grootte,
    koz.grootte_perceel,
    koz.deelperceelnummer,
    koz.omschr_deelperceel,
    koz.verkoop_datum,
    koz.aard_cultuur_onbebouwd,
    koz.bedrag,
    koz.koopjaar,
    koz.meer_onroerendgoed,
    koz.valutasoort,
    koz.loc_omschr,
    zrr.subject_identif,
    zrr.aandeel,
    zrr.omschr_aard_verkregenr_recht,
    zrr.indic_betrokken_in_splitsing,
    zrr.soort,
    zrr.geslachtsnaam,
    zrr.voorvoegsel,
    zrr.voornamen,
    zrr.aand_naamgebruik,
    zrr.geslachtsaand,
    zrr.naam,
    zrr.woonadres,
    zrr.geboortedatum,
    zrr.geboorteplaats,
    zrr.overlijdensdatum,
    zrr.bsn,
    zrr.organisatie_naam,
    zrr.rechtsvorm,
    zrr.statutaire_zetel,
    zrr.rsin,
    zrr.kvk_nummer,
    koz.gemeente,
    koz.woonplaats,
    koz.straatnaam,
    koz.huisnummer,
    koz.huisletter,
    koz.huisnummer_toev,
    koz.postcode,
    koz.begrenzing_perceel
FROM
    (v2_avg_zr_rechth zrr
right JOIN
    v2_kad_onrrnd_zk_locatie_adres koz
ON
    ((
            zrr.koz_identif = koz.identif)));
COMMENT ON VIEW v2_avg_koz_rechth
IS
    'commentaar view v2_avg_koz_rechth:
kadastrale percelen een appartementsrechten met rechten en rechthebbenden geschoond voor avg en objectid voor arcgis
beschikbare kolommen:
* objectid: uniek id bruikbaar voor arcgis,
* identif: natuurlijke id van perceel of appartementsrecht      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* type: perceel of appartement,
* sectie: -,
* perceelnummer: -,
* appartementsindex: -,
* gemeentecode: -,
* aand_soort_grootte: -,
* grootte_perceel: -,
* deelperceelnummer: -,
* omschr_deelperceel: -,
* verkoop_datum: laatste datum gevonden akten van verkoop,
* aard_cultuur_onbebouwd: -,
* bedrag: -,
* koopjaar: -,
* meer_onroerendgoed: -,
* valutasoort: -,
* loc_omschr: adres buiten BAG om meegegeven,
* subject_identif: natuurlijk id van rechthebbende,
* aandeel: samenvoeging van teller en noemer (1/2),
* omschr_aard_verkregenr_recht: tekstuele omschrijving aard recht,
* indic_betrokken_in_splitsing: -,
* soort: soort subject zoals natuurlijk, niet-natuurlijk enz.  
* geslachtsnaam: NULL (avg)       
* voorvoegsel: NULL (avg)      
* voornamen: NULL (avg)       
* aand_naamgebruik: NULL (avg)         
* geslachtsaand:NULL (avg)     
* naam: gelijk aan organisatie_naam
* woonadres: NULL (avg)        
* geboortedatum: NULL (avg)        
* geboorteplaats: NULL (avg)        
* overlijdensdatum: NULL (avg)        
* bsn: NULL (avg)         
* organisatie_naam: naam niet natuurlijk subject      
* rechtsvorm: -  
* statutaire_zetel: -      
* rsin: -        
* kvk_nummer: -
* gemeente: -,
* woonplaats: -,
* straatnaam: -,
* huisnummer: -,
* huisletter: -,
* huisnummer_toev: -,
* postcode: -,
* begrenzing_perceel: perceelvlak
';
 
