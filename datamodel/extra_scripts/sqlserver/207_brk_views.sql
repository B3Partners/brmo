/*
Views voor visualisatie van BRK data.
versie 2
30-8-2018
*/

CREATE VIEW vb_subject (
        objectid,
        subject_identif,
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
    CAST(row_number() OVER (ORDER BY s.identif)AS INT) AS ObjectID,
    s.identif                                          AS subject_identif,
    s.clazz                                            AS soort,
    np.nm_geslachtsnaam                                AS geslachtsnaam,
    np.nm_voorvoegsel_geslachtsnaam                    AS voorvoegsel,
    np.nm_voornamen                                    AS voornamen,
    np.aand_naamgebruik,
    CASE
        WHEN ((np.geslachtsaand) = '1')
        THEN 'M'
        WHEN ((np.geslachtsaand) = '2')
        THEN 'V'
        ELSE np.geslachtsaand
    END AS geslachtsaand,
    CASE
        WHEN (nnp.naam IS NOT NULL)
        THEN (nnp.naam)
        ELSE ((((((COALESCE(np.nm_voornamen, '')) + ' ') + (COALESCE
            (np.nm_voorvoegsel_geslachtsnaam, ''))) + ' ') + (COALESCE (np.nm_geslachtsnaam, ''))))
    END                     AS naam,
    inp.va_loc_beschrijving AS woonadres,
    CASE
        WHEN ((s.clazz) = 'INGESCHREVEN NATUURLIJK PERSOON') AND LEN(inp.gb_geboortedatum)=8
        THEN (
                substring(CONVERT(VARCHAR, inp.gb_geboortedatum),2,4) + '-' +
                substring(CONVERT(VARCHAR, inp.gb_geboortedatum),6,2) + '-' +
                substring(CONVERT(VARCHAR, inp.gb_geboortedatum),8,2)
        )

        WHEN ((s.clazz) = 'ANDER NATUURLIJK PERSOON') AND LEN(anp.geboortedatum)=8
        THEN (
                substring(CONVERT(VARCHAR, anp.geboortedatum),2,4) + '-' +
                substring(CONVERT(VARCHAR, anp.geboortedatum),6,2) + '-' +
                substring(CONVERT(VARCHAR, anp.geboortedatum),8,2)
        )
        WHEN ((s.clazz) = 'INGESCHREVEN NATUURLIJK PERSOON') AND LEN(inp.gb_geboortedatum)=5
        THEN '0001-01-01'
        WHEN ((s.clazz) = 'ANDER NATUURLIJK PESOON') AND LEN(anp.geboortedatum)=5
        THEN '0001-01-01'
        ELSE NULL
    END                   AS geboortedatum,
    inp.gb_geboorteplaats AS geboorteplaats,
    CASE
        WHEN ((s.clazz) = 'INGESCHREVEN NATUURLIJK PERSOON') AND LEN(inp.ol_overlijdensdatum)=8
        THEN (
                substring(CONVERT(VARCHAR, inp.ol_overlijdensdatum),2,4) + '-' +
                substring(CONVERT(VARCHAR, inp.ol_overlijdensdatum),6,2) + '-' +
                substring(CONVERT(VARCHAR, inp.ol_overlijdensdatum),8,2)
        )

        WHEN ((s.clazz) = 'ANDER NATUURLIJK PERSOON') AND LEN(anp.overlijdensdatum)=8
        THEN (
                substring(CONVERT(VARCHAR, anp.overlijdensdatum),2,4) + '-' +
                substring(CONVERT(VARCHAR, anp.overlijdensdatum),6,2) + '-' +
                substring(CONVERT(VARCHAR, anp.overlijdensdatum),8,2)
        )
        WHEN ((s.clazz) = 'INGESCHREVEN NATUURLIJK PERSOON') AND LEN(inp.ol_overlijdensdatum)=5
        THEN '0001-01-01'
        WHEN ((s.clazz) = 'ANDER NATUURLIJK PESOON') AND LEN(anp.overlijdensdatum)=5
        THEN '0001-01-01'
        ELSE NULL
    END AS overlijdensdatum,
    inp.bsn,
    nnp.naam AS organisatie_naam,
    innp.rechtsvorm,
    innp.statutaire_zetel,
    innp.rsin,
    s.kvk_nummer
FROM
    subject s
LEFT JOIN
    nat_prs np
ON
    s.identif = np.sc_identif
LEFT JOIN
    ingeschr_nat_prs inp
ON
    inp.sc_identif = np.sc_identif
LEFT JOIN
    ander_nat_prs anp
ON
    anp.sc_identif = np.sc_identif
LEFT JOIN
    niet_nat_prs nnp
ON
    nnp.sc_identif = s.identif
LEFT JOIN
    ingeschr_niet_nat_prs innp
ON
    innp.sc_identif = nnp.sc_identif;

GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'samenvoeging alle soorten subjecten: natuurlijk en niet-natuurlijk.

beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* subject_identif: natuurlijke id van subject
* soort: soort subject zoals natuurlijk, niet-natuurlijk enz.
* geslachtsnaam: -
* voorvoegsel: -
* voornamen: -
* aand_naamgebruik:
- E (= Eigen geslachtsnaam)
- N (= Geslachtsnaam echtgenoot/geregistreerd partner na eigen geslachtsnaam)
- P (= Geslachtsnaam echtgenoot/geregistreerd partner)
- V (= Geslachtsnaam evhtgenoot/geregistreerd partner voor eigen geslachtsnaam)
* geslachtsaand: M/V/X
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
* kvk_nummer: -',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_subject';

GO

CREATE VIEW vb_avg_subject (
        objectid,
        subject_identif,
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
    s.objectid,
    s.subject_identif  AS subject_identif,
    s.soort,
    NULL               AS geslachtsnaam,
    NULL               AS voorvoegsel,
    NULL               AS voornamen,
    NULL               AS aand_naamgebruik,
    NULL               AS geslachtsaand,
    s.organisatie_naam AS naam,
    NULL               AS woonadres,
    NULL               AS geboortedatum,
    NULL               AS geboorteplaats,
    NULL               AS overlijdensdatum,
    NULL               AS bsn,
    s.organisatie_naam,
    s.rechtsvorm,
    s.statutaire_zetel,
    s.rsin,
    s.kvk_nummer
FROM
    vb_subject s;

GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'volledig subject (natuurlijk en niet natuurlijk) geschoond voor avg
beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* subject_identif: natuurlijke id van subject      
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
* kvk_nummer: -',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_avg_subject';

GO

CREATE VIEW vb_util_app_re_splitsing AS
SELECT
    b1.ref_id AS child_identif,
    min(b2.ref_id) AS parent_identif
FROM
    brondocument b1
JOIN
    brondocument b2
ON
    b2.identificatie = b1.identificatie
WHERE
    (
        b2.omschrijving = 'betrokkenBij Ondersplitsing'
    OR  b2.omschrijving = 'betrokkenBij HoofdSplitsing')
AND (
        b1.omschrijving = 'ontstaanUit Ondersplitsing'
    OR  b1.omschrijving = 'ontstaanUit HoofdSplitsing')
GROUP BY
    b1.ref_id;
    
GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'utility view, niet bedoeld voor direct gebruik',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_util_app_re_splitsing';

GO

CREATE VIEW vb_util_app_re_parent_3 AS
SELECT
    re.sc_kad_identif      AS app_re_identif,
    min(sp.parent_identif) AS parent_identif
FROM
    app_re re
LEFT JOIN
    vb_util_app_re_splitsing sp
ON
    re.sc_kad_identif = sp.child_identif
GROUP BY
    re.sc_kad_identif;
    
GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'utility view, niet bedoeld voor direct gebruik',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_util_app_re_parent_3';

GO

CREATE VIEW vb_util_app_re_parent_2 AS
SELECT
    u1.app_re_identif,
    CASE
        WHEN sp.parent_identif IS NULL
        THEN u1.parent_identif
        ELSE sp.parent_identif
    END AS parent_identif
FROM
    vb_util_app_re_parent_3 u1
LEFT JOIN
    vb_util_app_re_splitsing sp
ON
    u1.parent_identif = sp.child_identif;
    
GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'utility view, niet bedoeld voor direct gebruik',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_util_app_re_parent_2';

GO

CREATE VIEW vb_util_app_re_parent AS
SELECT
    u2.app_re_identif,
    CASE
        WHEN sp.parent_identif IS NULL
        THEN u2.parent_identif
        ELSE sp.parent_identif
    END AS parent_identif
FROM
    vb_util_app_re_parent_2 u2
LEFT JOIN
    vb_util_app_re_splitsing sp
ON
    u2.parent_identif = sp.child_identif;
    
GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'utility view, niet bedoeld voor direct gebruik',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_util_app_re_parent';

GO

CREATE VIEW vb_util_app_re_kad_perceel AS
SELECT
    u1.app_re_identif,
    kp.sc_kad_identif AS perceel_identif
FROM
    vb_util_app_re_parent u1
JOIN
    kad_perceel kp
ON
    u1.parent_identif = kp.sc_kad_identif
GROUP BY
    u1.app_re_identif,
    kp.sc_kad_identif;

GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'utility view, niet bedoeld voor direct gebruik, met lijst van appartementsrechten met bijbehorend grondperceel

beschikbare kolommen:
* app_re_identif: natuurlijk is van appartementsrecht,
* perceel_identif: natuurlijk id van grondperceel',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_util_app_re_kad_perceel';

GO

CREATE VIEW vb_kad_onrrnd_zk_adres (
        objectid,
        koz_identif,
        begin_geldigheid,
        begin_geldigheid_datum,
        benoemdobj_identif,
        type,
        aanduiding,
        aanduiding2,
        sectie,
        perceelnummer,
        appartementsindex,
        gemeentecode,
        aand_soort_grootte,
        grootte_perceel,
        oppervlakte_geom,
        deelperceelnummer,
        omschr_deelperceel,
        verkoop_datum,
        aard_cultuur_onbebouwd,
        bedrag,
        koopjaar,
        meer_onroerendgoed,
        valutasoort,
        loc_omschr,
        aantekeningen,
        na_identif,
        na_status,
        gemeente,
        woonplaats,
        straatnaam,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        gebruiksdoelen,
        oppervlakte_obj,
        lon,
        lat,
        begrenzing_perceel
    ) AS
SELECT
    CAST(row_number() OVER (ORDER BY qry.identif) AS INT)                       AS ObjectID,
    qry.identif                                                                 AS koz_identif,
    koz.dat_beg_geldh                                                           AS begin_geldigheid,
    TRY_CONVERT(DATETIME, koz.dat_beg_geldh)                                    AS begin_geldigheid_datum,
    bok.fk_nn_lh_tgo_identif                                                    AS benoemdobj_identif,
    qry.type,
    COALESCE(qry.ka_sectie, '') + ' ' + COALESCE(qry.ka_perceelnummer, '')                                                                                              AS aanduiding,
    COALESCE(qry.ka_kad_gemeentecode, '') + ' ' + COALESCE(qry.ka_sectie, '') + ' ' + COALESCE(qry.ka_perceelnummer, '') + ' ' + COALESCE(qry.ka_appartementsindex, '') AS aanduiding2,
    qry.ka_sectie,
    qry.ka_perceelnummer,
    qry.ka_appartementsindex,
    qry.ka_kad_gemeentecode,
    qry.aand_soort_grootte,
    qry.grootte_perceel,
    (qry.begrenzing_perceel.STArea())                                           AS oppervlakte_geom,
    qry.ka_deelperceelnummer,
    qry.omschr_deelperceel,
    b.datum,
    koz.cu_aard_cultuur_onbebouwd,
    koz.ks_bedrag,
    koz.ks_koopjaar,
    koz.ks_meer_onroerendgoed,
    koz.ks_valutasoort,
    koz.lo_loc__omschr,
    aant.aantekeningen                                                          AS aantekeningen,
    bola.na_identif,
    bola.na_status,
    bola.gemeente,
    bola.woonplaats,
    bola.straatnaam,
    bola.huisnummer,
    bola.huisletter,
    bola.huisnummer_toev,
    bola.postcode,
    bola.gebruiksdoelen,
    bola.oppervlakte_obj,
    -- mssqlserver heeft geen STtransform functie, dus projectie naar EPSG:4326 onmogelijk,
    -- derhalve NULL
    NULL AS lon,
    NULL AS lat,
    qry.begrenzing_perceel
FROM
    (
        SELECT
            p.sc_kad_identif    AS identif,
            'perceel'           AS type,
            p.ka_sectie,
            p.ka_perceelnummer,
            NULL                AS ka_appartementsindex,
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
            'appartement'       AS type,
            ar.ka_sectie,
            ar.ka_perceelnummer,
            ar.ka_appartementsindex,
            ar.ka_kad_gemeentecode,
            NULL                AS aand_soort_grootte,
            NULL                AS grootte_perceel,
            NULL                AS ka_deelperceelnummer,
            NULL                AS omschr_deelperceel,
            kp.begrenzing_perceel
        FROM
            vb_util_app_re_kad_perceel v
        JOIN
            kad_perceel kp
        ON  v.perceel_identif = kp.sc_kad_identif
        JOIN
            app_re ar
        ON v.app_re_identif = ar.sc_kad_identif) qry
JOIN
        kad_onrrnd_zk koz
ON
        koz.kad_identif = qry.identif
LEFT JOIN
        benoemd_obj_kad_onrrnd_zk bok
ON
        bok.fk_nn_rh_koz_kad_identif = qry.identif
LEFT JOIN
    vb_benoemd_obj_adres bola
ON
    bok.fk_nn_lh_tgo_identif = bola.benoemdobj_identif
LEFT JOIN (
        SELECT
            brondocument.ref_id,
            MAX(brondocument.datum) AS datum
        FROM
            brondocument
        WHERE
            brondocument.omschrijving = 'Akte van Koop en Verkoop'
        GROUP BY brondocument.ref_id) b
ON
        koz.kad_identif = b.ref_id
LEFT JOIN (
        SELECT
            koza.fk_4koz_kad_identif,
            STRING_AGG(
                    CONCAT_WS(' ',
                            'id:', COALESCE(koza.kadaster_identif_aantek, ''),
                            ', aard:', COALESCE(koza.aard_aantek_kad_obj, ''),
                            ', begin:', COALESCE(koza.begindatum_aantek_kad_obj, ''),
                            ', beschrijving:', COALESCE(koza.beschrijving_aantek_kad_obj, ''),
                            ', eind:', COALESCE(koza.eindd_aantek_kad_obj, ''),
                            ', koz-id:', COALESCE(CAST(koza.fk_4koz_kad_identif AS NUMERIC), 0),
                            ', subject-id:', COALESCE(koza.fk_5pes_sc_identif, ''),
                            ';'),
                        ' & ') WITHIN GROUP ( ORDER BY koza.fk_4koz_kad_identif ) AS aantekeningen
        FROM kad_onrrnd_zk_aantek koza
        GROUP BY fk_4koz_kad_identif) aant
    ON koz.kad_identif = aant.fk_4koz_kad_identif;


GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'alle kadastrale onroerende zaken (perceel en appartementsrecht) met opgezochte verkoop datum, objectid voor geoserver/arcgis en BAG adres

beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* koz_identif: natuurlijke id van perceel of appartementsrecht
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* begin_geldigheid_datum: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* benoemdobj_identif: koppeling met BAG object,
* type: perceel of appartement,
* aanduiding: sectie perceelnummer,
* aanduiding2: kadgem sectie perceelnummer appartementsindex,
* sectie: -,
* perceelnummer: -,
* appartementsindex: -,
* gemeentecode: -,
* aand_soort_grootte: -,
* grootte_perceel: -,
* oppervlakte_geom: oppervlakte berekend uit geometrie, hoort gelijk te zijn aan grootte_perceel,
* deelperceelnummer: -,
* omschr_deelperceel: -,
* verkoop_datum: laatste datum gevonden akten van verkoop,
* aard_cultuur_onbebouwd: -,
* bedrag: -,
* koopjaar: -,
* meer_onroerendgoed: -,
* valutasoort: -,
* loc_omschr: adres buiten BAG om meegegeven,
* aantekeningen: -,
* na_identif: identificatie van nummeraanduiding
* na_status: status van nummeraanduiding
* gemeente: -,
* woonplaats: -,
* straatnaam: -,
* huisnummer: -,
* huisletter: -,
* huisnummer_toev: -,
* postcode: -,
* gebruiksdoelen: alle gebruiksdoelen gescheiden door komma
* oppervlakte_obj: oppervlak van gebouwd object
* lon: coordinaat als WSG84,
* lon: coordinaat als WSG84,
* begrenzing_perceel: perceelvlak',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_kad_onrrnd_zk_adres';

GO

CREATE VIEW vb_percelenkaart AS
SELECT
    CAST(ROW_NUMBER() OVER(ORDER BY qry.identif) AS INT) AS ObjectID,
    qry.identif                                          AS koz_identif,
    koz.dat_beg_geldh                                    AS begin_geldigheid,
    TRY_CONVERT(DATETIME, koz.dat_beg_geldh)             AS begin_geldigheid_datum,
    qry.type,
    COALESCE(qry.ka_sectie, '') + ' ' + COALESCE(qry.ka_perceelnummer, '')                                                     AS aanduiding,
    COALESCE(qry.ka_kad_gemeentecode, '') + ' ' + COALESCE(qry.ka_sectie, '') + ' ' + COALESCE(qry.ka_perceelnummer, '') + ' ' AS aanduiding2,
    qry.ka_sectie                                                                                                              AS sectie,
    qry.ka_perceelnummer                                                                                                       AS perceelnummer,
    qry.ka_kad_gemeentecode                                                                                                    AS gemeentecode,
    qry.aand_soort_grootte,
    qry.grootte_perceel,
    qry.begrenzing_perceel.STArea() AS oppervlakte_geom,
    b.datum                         AS verkoop_datum,
    koz.cu_aard_cultuur_onbebouwd   AS aard_cultuur_onbebouwd,
    koz.ks_bedrag                   AS bedrag,
    koz.ks_koopjaar                 AS koopjaar,
    koz.ks_meer_onroerendgoed       AS meer_onroerendgoed,
    koz.ks_valutasoort              AS valutasoort,
    aant.aantekeningen              AS aantekeningen,
    -- mssqlserver heeft geen STtransform functie, dus projectie naar EPSG:4326 onmogelijk, derhalve NULL
    qry.begrenzing_perceel
FROM (
        SELECT
            p.sc_kad_identif AS identif,
            'perceel'        AS type,
            p.ka_sectie,
            p.ka_perceelnummer,
            p.ka_kad_gemeentecode,
            p.aand_soort_grootte,
            p.grootte_perceel,
            p.begrenzing_perceel
        FROM
            kad_perceel p) qry
JOIN
    kad_onrrnd_zk koz
    ON koz.kad_identif = qry.identif
LEFT JOIN
    (
        SELECT
            brondocument.ref_id,
            MAX(brondocument.datum) AS datum
        FROM
            brondocument
        WHERE
            brondocument.omschrijving = 'Akte van Koop en Verkoop'
        GROUP BY
            brondocument.ref_id) b
    ON koz.kad_identif = b.ref_id
LEFT JOIN
    (
        SELECT
            koza.fk_4koz_kad_identif,
            STRING_AGG(
                    CONCAT_WS(' ',
                            'id:', COALESCE(koza.kadaster_identif_aantek, ''),
                            ', aard:', COALESCE(koza.aard_aantek_kad_obj, ''),
                            ', begin:', COALESCE(koza.begindatum_aantek_kad_obj, ''),
                            ', beschrijving:', COALESCE(koza.beschrijving_aantek_kad_obj, ''),
                            ', eind:', COALESCE(koza.eindd_aantek_kad_obj, ''),
                            ', koz-id:', COALESCE(CAST(koza.fk_4koz_kad_identif AS NUMERIC), 0),
                            ', subject-id:', COALESCE(koza.fk_5pes_sc_identif, ''),
                            ';'),
                        ' & ') WITHIN GROUP ( ORDER BY koza.fk_4koz_kad_identif ) AS aantekeningen
        FROM kad_onrrnd_zk_aantek koza
        GROUP BY fk_4koz_kad_identif) aant
    ON koz.kad_identif = aant.fk_4koz_kad_identif;

GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'
commentaar view vb_percelenkaart:
alle kadastrale onroerende zaken (perceel en appartementsrecht) met opgezochte verkoop datum, objectid voor geoserver/arcgis

beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* koz_identif: natuurlijke id van perceel of appartementsrecht
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* begin_geldigheid_datum: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* type: perceel of appartement,
* aanduiding: sectie perceelnummer,
* aanduiding2: kadgem sectie perceelnummer appartementsindex,
* sectie: -,
* perceelnummer: -,
* gemeentecode: -,
* aand_soort_grootte: -,
* grootte_perceel: -,
* oppervlakte_geom: oppervlakte berekend uit geometrie, hoort gelijk te zijn aan grootte_perceel,
* verkoop_datum: laatste datum gevonden akten van verkoop,
* aard_cultuur_onbebouwd: -,
* bedrag: -,
* koopjaar: -,
* meer_onroerendgoed: -,
* valutasoort: -,
* aantekeningen: -,
* lon: coordinaat als WSG84,
* lon: coordinaat als WSG84,
* begrenzing_perceel: perceelvlak',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_percelenkaart';

GO

CREATE VIEW
    vb_util_zk_recht
    (
        zr_identif,
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
    zr.kadaster_identif AS zr_identif,
    ( (COALESCE(CAST(zr.ar_teller AS VARCHAR), ('0')) + ('/')) + COALESCE(CAST(zr.ar_noemer AS VARCHAR), ('0')) ) AS aandeel,
    zr.ar_teller,
    zr.ar_noemer,
    zr.fk_8pes_sc_identif  AS subject_identif,
    zr.fk_7koz_kad_identif AS koz_identif,
    zr.indic_betrokken_in_splitsing,
    avr.omschr_aard_verkregenr_recht,
    zr.fk_3avr_aand
FROM
    zak_recht zr
JOIN
    aard_verkregen_recht avr
ON
    zr.fk_3avr_aand = avr.aand;

GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'zakelijk recht met opgezocht aard recht en berekend aandeel

beschikbare kolommen:
* zr_identif: natuurlijke id van zakelijk recht     
* aandeel: samenvoeging van teller en noemer (1/2),
* ar_teller: teller van aandeel,
* ar_noemer: noemer van aandeel,
* subject_identif: natuurlijk id van subject (natuurlijk of niet natuurlijk) welke rechthebbende is,
* koz_identif: natuurlijk id van kadastrale onroerende zaak (perceel of appratementsrecht) dat gekoppeld is,
* indic_betrokken_in_splitsing: -,
* omschr_aard_verkregenr_recht: tekstuele omschrijving aard recht,
* fk_3avr_aand: code aard recht',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_util_zk_recht';

GO


CREATE VIEW
    vb_zr_rechth
    (
        objectid,
        zr_identif,
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
    CAST(row_number() OVER (ORDER BY uzr.zr_identif)AS INT) AS ObjectID,
    uzr.zr_identif                                          AS zr_identif,
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
    vb_util_zk_recht uzr
JOIN
    vb_subject vs
ON
    uzr.subject_identif = vs.subject_identif;

GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'alle zakelijke rechten met rechthebbenden en referentie naar kadastraal onroerende zaak (perceel of appartementsrecht)

beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* zr_identif: natuurlijke id van zakelijk recht 
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
* kvk_nummer: -',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_zr_rechth';

GO


CREATE VIEW
    vb_avg_zr_rechth
    (
        objectid,
        zr_identif,
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
    CAST(row_number() OVER (ORDER BY uzr.zr_identif)AS INT) AS ObjectID,
    uzr.zr_identif                                          AS zr_identif,
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
    vb_util_zk_recht uzr
JOIN
    vb_avg_subject vs
ON
    uzr.subject_identif = vs.subject_identif;

GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'alle zakelijke rechten met voor avg geschoonde rechthebbenden en referentie naar kadastraal onroerende zaak (perceel of appartementsrecht)

beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* zr_identif: natuurlijke id van zakelijk recht     
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
* kvk_nummer: -',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_avg_zr_rechth';

GO


CREATE VIEW
    vb_koz_rechth
    (
        objectid,
        koz_identif,
        begin_geldigheid,
        type,
        aanduiding,
        aanduiding2,
        sectie,
        perceelnummer,
        appartementsindex,
        gemeentecode,
        aand_soort_grootte,
        grootte_perceel,
        oppervlakte_geom,
        deelperceelnummer,
        omschr_deelperceel,
        verkoop_datum,
        aard_cultuur_onbebouwd,
        bedrag,
        koopjaar,
        meer_onroerendgoed,
        valutasoort,
        loc_omschr,
        zr_identif,
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
        lon,
        lat,
        begrenzing_perceel
    ) AS
SELECT
    CAST(row_number() OVER (ORDER BY koz.koz_identif)AS INT) AS ObjectID,
    koz.koz_identif,
    koz.begin_geldigheid,
    koz.type,
    COALESCE(koz.sectie, '') + ' ' + COALESCE(koz.perceelnummer, '') AS aanduiding,
    COALESCE(koz.gemeentecode, '') + ' ' + COALESCE(koz.sectie, '') + ' ' + COALESCE
    (koz.perceelnummer, '') + ' ' + COALESCE(koz.appartementsindex, '') AS aanduiding2,
    koz.sectie,
    koz.perceelnummer,
    koz.appartementsindex,
    koz.gemeentecode,
    koz.aand_soort_grootte,
    koz.grootte_perceel,
    koz.oppervlakte_geom AS oppervlakte_geom,
    koz.deelperceelnummer,
    koz.omschr_deelperceel,
    koz.verkoop_datum,
    koz.aard_cultuur_onbebouwd,
    koz.bedrag,
    koz.koopjaar,
    koz.meer_onroerendgoed,
    koz.valutasoort,
    koz.loc_omschr,
    zrr.zr_identif,
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
    koz.lon,
    koz.lat,
    koz.begrenzing_perceel
FROM
    vb_zr_rechth zrr
RIGHT JOIN
    vb_kad_onrrnd_zk_adres koz
ON
    zrr.koz_identif = koz.koz_identif;

GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'kadastrale percelen een appartementsrechten met rechten en rechthebbenden en objectid voor geoserver/arcgis
beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* koz_identif: natuurlijke id van perceel of appartementsrecht      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* type: perceel of appartement,
* aanduiding: sectie perceelnummer,
* aanduiding2: kadgem sectie perceelnummer appartementsindex,
* sectie: -,
* perceelnummer: -,
* appartementsindex: -,
* gemeentecode: -,
* aand_soort_grootte: -,
* grootte_perceel: -,
* oppervlakte_geom: oppervlakte berekend uit geometrie, hoort gelijk te zijn aan grootte_perceel,
* deelperceelnummer: -,
* omschr_deelperceel: -,
* verkoop_datum: laatste datum gevonden akten van verkoop,
* aard_cultuur_onbebouwd: -,
* bedrag: -,
* koopjaar: -,
* meer_onroerendgoed: -,
* valutasoort: -,
* loc_omschr: adres buiten BAG om meegegeven,
* zr_identif: natuurlijk id van zakelijk recht,
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
* lon: coordinaat als WSG84,
* lon: coordinaat als WSG84,
* begrenzing_perceel: perceelvlak',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_koz_rechth';

GO


CREATE VIEW
    vb_avg_koz_rechth
    (
        objectid,
        koz_identif,
        begin_geldigheid,
        type,
        aanduiding,
        aanduiding2,
        sectie,
        perceelnummer,
        appartementsindex,
        gemeentecode,
        aand_soort_grootte,
        grootte_perceel,
        oppervlakte_geom,
        deelperceelnummer,
        omschr_deelperceel,
        verkoop_datum,
        aard_cultuur_onbebouwd,
        bedrag,
        koopjaar,
        meer_onroerendgoed,
        valutasoort,
        loc_omschr,
        zr_identif,
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
        lon,
        lat,
        begrenzing_perceel
    ) AS
SELECT
    CAST(row_number() OVER (ORDER BY koz.koz_identif)AS INT) AS ObjectID,
    koz.koz_identif                                          AS koz_identif,
    koz.begin_geldigheid,
    koz.type,
    COALESCE(koz.sectie, '') + ' ' + COALESCE(koz.perceelnummer, '') AS aanduiding,
    COALESCE(koz.gemeentecode, '') + ' ' + COALESCE(koz.sectie, '') + ' ' + COALESCE
    (koz.perceelnummer, '') + ' ' + COALESCE(koz.appartementsindex, '') AS aanduiding2,
    koz.sectie,
    koz.perceelnummer,
    koz.appartementsindex,
    koz.gemeentecode,
    koz.aand_soort_grootte,
    koz.grootte_perceel,
    koz.oppervlakte_geom,
    koz.deelperceelnummer,
    koz.omschr_deelperceel,
    koz.verkoop_datum,
    koz.aard_cultuur_onbebouwd,
    koz.bedrag,
    koz.koopjaar,
    koz.meer_onroerendgoed,
    koz.valutasoort,
    koz.loc_omschr,
    zrr.zr_identif,
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
    koz.lon,
    koz.lat,
    koz.begrenzing_perceel
FROM
    vb_avg_zr_rechth zrr
RIGHT JOIN
    vb_kad_onrrnd_zk_adres koz
ON
    zrr.koz_identif = koz.koz_identif;

GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'kadastrale percelen een appartementsrechten met rechten en rechthebbenden geschoond voor avg en objectid voor geoserver/arcgis
beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* koz_identif: natuurlijke id van perceel of appartementsrecht      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* type: perceel of appartement,
* aanduiding: sectie perceelnummer,
* aanduiding2: kadgem sectie perceelnummer appartementsindex,
* sectie: -,
* perceelnummer: -,
* appartementsindex: -,
* gemeentecode: -,
* aand_soort_grootte: -,
* grootte_perceel: -,
* oppervlakte_geom: oppervlakte berekend uit geometrie, hoort gelijk te zijn aan grootte_perceel,
* deelperceelnummer: -,
* omschr_deelperceel: -,
* verkoop_datum: laatste datum gevonden akten van verkoop,
* aard_cultuur_onbebouwd: -,
* bedrag: -,
* koopjaar: -,
* meer_onroerendgoed: -,
* valutasoort: -,
* loc_omschr: adres buiten BAG om meegegeven,
* zr_identif: natuurlijk id van zakelijk recht,
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
* lon: coordinaat als WSG84,
* lat: coordinaat als WSG84,
* begrenzing_perceel: perceelvlak',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_avg_koz_rechth';

GO


CREATE VIEW
    vb_kad_onrrnd_zk_archief
    (
        objectid,
        koz_identif,
        begin_geldigheid,
        eind_geldigheid,
        type,
        aanduiding,
        aanduiding2,
        sectie,
        perceelnummer,
        appartementsindex,
        gemeentecode,
        aand_soort_grootte,
        grootte_perceel,
        deelperceelnummer,
        omschr_deelperceel,
        aard_cultuur_onbebouwd,
        bedrag,
        koopjaar,
        meer_onroerendgoed,
        valutasoort,
        loc_omschr,
        overgegaan_in,
        begrenzing_perceel
    ) AS
SELECT
    CAST(row_number() OVER (ORDER BY qry.identif)AS INT) AS ObjectID,
    qry.identif                                          AS koz_identif,
    koza.dat_beg_geldh                                   AS begin_geldigheid,
    koza.datum_einde_geldh                               AS eind_geldigheid,
    qry.type,
    COALESCE(qry.ka_sectie, '') + ' ' + COALESCE (qry.ka_perceelnummer, '') AS aanduiding,
    COALESCE(qry.ka_kad_gemeentecode, '') + ' ' + COALESCE (qry.ka_sectie, '') + ' ' + COALESCE
    (qry.ka_perceelnummer, '') + ' ' + COALESCE (qry.ka_appartementsindex, '') AS aanduiding2,
    qry.ka_sectie                                                              AS sectie,
    qry.ka_perceelnummer                                                       AS perceelnummer,
    qry.ka_appartementsindex                                                   AS appartementsindex,
    qry.ka_kad_gemeentecode AS gemeentecode,
    qry.aand_soort_grootte,
    qry.grootte_perceel,
    qry.ka_deelperceelnummer AS deelperceelnummer,
    qry.omschr_deelperceel,
    koza.cu_aard_cultuur_onbebouwd AS aard_cultuur_onbebouwd,
    koza.ks_bedrag                 AS bedrag,
    koza.ks_koopjaar               AS koopjaar,
    koza.ks_meer_onroerendgoed     AS meer_onroerendgoed,
    koza.ks_valutasoort            AS valutasoort,
    koza.lo_loc__omschr            AS loc_omschr ,
    kozhr.fk_sc_lh_koz_kad_identif AS overgegaan_in,
    qry.begrenzing_perceel
FROM
    (
        SELECT
            pa.sc_kad_identif   AS identif,
            pa.sc_dat_beg_geldh AS dat_beg_geldh,
            'perceel'           AS type,
            pa.ka_sectie,
            pa.ka_perceelnummer,
            NULL AS ka_appartementsindex,
            pa.ka_kad_gemeentecode,
            pa.aand_soort_grootte,
            pa.grootte_perceel,
            pa.ka_deelperceelnummer,
            pa.omschr_deelperceel,
            pa.begrenzing_perceel
        FROM
            kad_perceel_archief pa
        UNION ALL
        SELECT
            ara.sc_kad_identif   AS identif,
            ara.sc_dat_beg_geldh AS dat_beg_geldh,
            'appartement'        AS type,
            ara.ka_sectie,
            ara.ka_perceelnummer,
            ara.ka_appartementsindex,
            ara.ka_kad_gemeentecode,
            NULL AS aand_soort_grootte,
            NULL AS grootte_perceel,
            NULL AS ka_deelperceelnummer,
            NULL AS omschr_deelperceel,
            NULL AS begrenzing_perceel
        FROM
            app_re_archief ara ) qry
JOIN
    kad_onrrnd_zk_archief koza
ON
    koza.kad_identif = qry.identif
AND qry.dat_beg_geldh = koza.dat_beg_geldh
JOIN
    (
        SELECT
            ikoza.kad_identif,
            MAX(ikoza.dat_beg_geldh) bdate
        FROM
            kad_onrrnd_zk_archief ikoza
        GROUP BY
            ikoza.kad_identif 
     ) nqry
ON
    nqry.kad_identif = koza.kad_identif
AND nqry.bdate = koza.dat_beg_geldh
LEFT JOIN
    kad_onrrnd_zk_his_rel kozhr
ON
    kozhr.fk_sc_rh_koz_kad_identif = koza.kad_identif
-- ORDER BY bdate DESC
-- want: [Code: 1033, SQL State: S1000]  The ORDER BY clause is invalid in views, inline functions, derived tables, subqueries, and common table expressions, unless TOP, OFFSET or FOR XML is also specified.
-- niet duidelijk wat dit nog toevoegt, immers bdate is max(dat_beg_geldh)
;

GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'Nieuwste gearchiveerde versie van ieder kadastrale onroerende zaak (perceel en appartementsrecht) met objectid voor geoserver/arcgis en historische relatie

beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* koz_identif: natuurlijke id van perceel of appartementsrecht      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
* eind_geldigheid: datum wanneer dit object ongeldig geworden is,
* benoemdobj_identif: koppeling met BAG object,
* type: perceel of appartement,
* sectie: -,
* aanduiding: sectie perceelnummer,
* aanduiding2: kadgem sectie perceelnummer appartementsindex,
* perceelnummer: -,
* appartementsindex: -,
* gemeentecode: -,
* aand_soort_grootte: -,
* grootte_perceel: -,
* deelperceelnummer: -,
* omschr_deelperceel: -,
* aard_cultuur_onbebouwd: -,
* bedrag: -,
* koopjaar: -,
* meer_onroerendgoed: -,
* valutasoort: -,
* loc_omschr: adres buiten BAG om meegegeven,
* overgegaan_in: natuurlijk id van kadastrale onroerende zaak waar dit object in is overgegaan,
* begrenzing_perceel: perceelvlak',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_kad_onrrnd_zk_archief';

GO
