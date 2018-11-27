-- 
-- upgrade Oracle RSGB datamodel van 1.6.1 naar 1.6.2 
--
WHENEVER SQLERROR EXIT SQL.SQLCODE

-- issue #565
ALTER TABLE KAD_PERCEEL MODIFY (AAND_SOORT_GROOTTE VARCHAR2(2));
ALTER TABLE KAD_PERCEEL_ARCHIEF MODIFY (AAND_SOORT_GROOTTE VARCHAR2(2));

CREATE MATERIALIZED VIEW mb_util_app_re_kad_perceel
    BUILD DEFERRED REFRESH ON DEMAND AS
SELECT
    u1.app_re_identif,
    kp.sc_kad_identif AS perceel_identif
FROM
    vb_util_app_re_parent u1
JOIN
    kad_perceel kp
ON
    u1.parent_identif = cast(kp.sc_kad_identif AS CHARACTER VARYING(50))
GROUP BY
    u1.app_re_identif,
    kp.sc_kad_identif;
    
COMMENT ON MATERIALIZED VIEW mb_util_app_re_kad_perceel
IS 'commentaar view mb_util_app_re_kad_perceel:
utility view, niet bedoeld voor direct gebruik, met lijst van appartementsrechten met bijbehorend grondperceel
beschikbare kolommen:
* app_re_identif: natuurlijk id van appartementsrecht,
* perceel_identif: natuurlijk id van grondperceel';
CREATE INDEX mb_util_app_re_kad_perceel_id ON mb_util_app_re_kad_perceel(app_re_identif);

DROP MATERIALIZED VIEW mb_kad_onrrnd_zk_adres;
CREATE MATERIALIZED VIEW mb_kad_onrrnd_zk_adres (
    objectid,
    koz_identif,
    begin_geldigheid,
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
    gemeente,
    woonplaats,
    straatnaam,
    huisnummer,
    huisletter,
    huisnummer_toev,
    postcode,
    lon,
    lat,
    begrenzing_perceel)
BUILD DEFERRED REFRESH ON DEMAND AS
SELECT
        CAST(ROWNUM AS INTEGER) AS objectid,
        qry.identif                AS koz_identif,
        koz.dat_beg_geldh          AS begin_geldigheid,
        bok.fk_nn_lh_tgo_identif   AS benoemdobj_identif,
        qry.type,
        coalesce(qry.ka_sectie,'')
        || ' '
        || coalesce(qry.ka_perceelnummer,'') AS aanduiding,
        coalesce(qry.ka_kad_gemeentecode,'')
        || ' '
        || coalesce(qry.ka_sectie,'')
        || ' '
        || coalesce(qry.ka_perceelnummer,'')
        || ' '
        || coalesce(qry.ka_appartementsindex,'') AS aanduiding2,
        qry.ka_sectie,
        qry.ka_perceelnummer,
        qry.ka_appartementsindex,
        qry.ka_kad_gemeentecode,
        qry.aand_soort_grootte,
        qry.grootte_perceel,
        qry.oppervlakte_geom,
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
        qry.lon,
        qry.lat,
        qry.begrenzing_perceel
    FROM
        (
            SELECT
                p.sc_kad_identif       AS identif,
                'perceel' AS type,
                p.ka_sectie,
                p.ka_perceelnummer,
                CAST(NULL AS CHARACTER VARYING(4) ) AS ka_appartementsindex,
                p.ka_kad_gemeentecode,
                p.aand_soort_grootte,
                p.grootte_perceel,
                p.ka_deelperceelnummer,
                p.omschr_deelperceel,
                p.begrenzing_perceel   AS begrenzing_perceel,
                CASE
                    WHEN p.begrenzing_perceel.get_gtype() IS NOT NULL THEN sdo_geom.sdo_area(p.begrenzing_perceel,0.1)
                    ELSE NULL
                END AS oppervlakte_geom,
                CASE
                    WHEN p.begrenzing_perceel.get_gtype() IS NOT NULL THEN sdo_cs.transform(sdo_geom.sdo_centroid(p.begrenzing_perceel
                   ,0.1),4326).sdo_point.x
                    ELSE NULL
                END AS lon,
                CASE
                    WHEN p.begrenzing_perceel.get_gtype() IS NOT NULL THEN sdo_cs.transform(sdo_geom.sdo_centroid(p.begrenzing_perceel
                   ,0.1),4326).sdo_point.y
                END AS lat
            FROM
                kad_perceel p
            UNION ALL
            SELECT
                ar.sc_kad_identif       AS identif,
                'appartement' AS type,
                ar.ka_sectie,
                ar.ka_perceelnummer,
                ar.ka_appartementsindex,
                ar.ka_kad_gemeentecode,
                CAST(NULL AS CHARACTER VARYING(2) ) AS aand_soort_grootte,
                CAST(NULL AS NUMERIC(8,0) ) AS grootte_perceel,
                CAST(NULL AS CHARACTER VARYING(4) ) AS ka_deelperceelnummer,
                CAST(NULL AS CHARACTER VARYING(1120) ) AS omschr_deelperceel,
                kp.begrenzing_perceel   AS begrenzing_perceel,
                CASE
                    WHEN kp.begrenzing_perceel.get_gtype() IS NOT NULL THEN sdo_geom.sdo_area(kp.begrenzing_perceel,0.1)
                    ELSE NULL
                END AS oppervlakte_geom,
                CASE
                    WHEN kp.begrenzing_perceel.get_gtype() IS NOT NULL THEN sdo_cs.transform(sdo_geom.sdo_centroid(kp.begrenzing_perceel
                   ,0.1),4326).sdo_point.x
                    ELSE NULL
                END AS lon,
                CASE
                    WHEN kp.begrenzing_perceel.get_gtype() IS NOT NULL THEN sdo_cs.transform(sdo_geom.sdo_centroid(kp.begrenzing_perceel
                   ,0.1),4326).sdo_point.y
                END AS lat
            FROM
                mb_util_app_re_kad_perceel v
                JOIN kad_perceel kp ON CAST(v.perceel_identif AS NUMERIC) = kp.sc_kad_identif
                JOIN app_re ar ON CAST(v.app_re_identif AS NUMERIC) = ar.sc_kad_identif
        ) qry
        JOIN kad_onrrnd_zk koz ON ( koz.kad_identif = qry.identif )
        LEFT JOIN benoemd_obj_kad_onrrnd_zk bok ON ( bok.fk_nn_rh_koz_kad_identif = qry.identif )
        LEFT JOIN vb_benoemd_obj_adres bola ON ( bok.fk_nn_lh_tgo_identif = bola.benoemdobj_identif )
        LEFT JOIN (
            SELECT
                bd.ref_id,
                MAX(bd.datum) AS datum
            FROM
                brondocument bd
            WHERE
                ( ( bd.omschrijving ) = 'Akte van Koop en Verkoop' )
            GROUP BY
                bd.ref_id
        ) b ON ( koz.kad_identif = b.ref_id );
    
CREATE UNIQUE INDEX MB_KAD_ONRRND_ZK_ADRES_OBJIDX ON MB_KAD_ONRRND_ZK_ADRES(OBJECTID ASC);
CREATE INDEX MB_KAD_ONRRND_ZK_ADRES_IDENTIF ON MB_KAD_ONRRND_ZK_ADRES(KOZ_IDENTIF ASC);
CREATE INDEX MB_KAD_ONRRND_ZK_ADR_BGRGPIDX ON MB_KAD_ONRRND_ZK_ADRES (BEGRENZING_PERCEEL) INDEXTYPE IS MDSYS.SPATIAL_INDEX;

COMMENT ON MATERIALIZED VIEW mb_kad_onrrnd_zk_adres
IS 'commentaar view mb_kad_onrrnd_zk_adres:
alle kadastrale onroerende zaken (perceel en appartementsrecht) met opgezochte verkoop datum, objectid voor geoserver/arcgis en BAG adres
beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* koz_identif: natuurlijke id van perceel of appartementsrecht      
* begin_geldigheid: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
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
* gemeente: -,
* woonplaats: -,
* straatnaam: -,
* huisnummer: -,
* huisletter: -,
* huisnummer_toev: -,
* postcode: -,
* lon: coordinaat als WSG84,
* lon: coordinaat als WSG84,
* begrenzing_perceel: perceelvlak';

DROP MATERIALIZED VIEW mb_avg_koz_rechth;
CREATE MATERIALIZED VIEW mb_avg_koz_rechth (
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
)
BUILD DEFERRED REFRESH ON DEMAND AS
SELECT
    CAST(ROWNUM AS INTEGER) AS objectid,
    koz.koz_identif as koz_identif,
    koz.begin_geldigheid,
    koz.type,
    COALESCE(koz.sectie, '') || ' ' || COALESCE(koz.perceelnummer, '') AS aanduiding,
    COALESCE(koz.gemeentecode, '') || ' ' || COALESCE(koz.sectie, '') || ' ' || COALESCE(koz.perceelnummer, '') || ' ' || COALESCE(koz.appartementsindex, '') AS aanduiding2,
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
    mb_kad_onrrnd_zk_adres koz
ON  zrr.koz_identif = koz.koz_identif;

CREATE UNIQUE INDEX MB_AVG_KOZ_RECHTH_OBJECTID ON MB_AVG_KOZ_RECHTH(OBJECTID ASC);
CREATE INDEX MB_AVG_KOZ_RECHTH_IDENTIF ON MB_AVG_KOZ_RECHTH(KOZ_IDENTIF ASC);
CREATE INDEX MB_AVG_KOZ_RECHTH_BEGR_P_IDX ON MB_AVG_KOZ_RECHTH (BEGRENZING_PERCEEL) INDEXTYPE IS MDSYS.SPATIAL_INDEX;

COMMENT ON MATERIALIZED VIEW mb_avg_koz_rechth
IS 'commentaar view mb_avg_koz_rechth:
kadastrale percelen een appartementsrechten met rechten en rechthebbenden geschoond voor avg en objectid voor geoserver/arcgis
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
* begrenzing_perceel: perceelvlak';

DROP MATERIALIZED VIEW mb_koz_rechth;
CREATE MATERIALIZED VIEW mb_koz_rechth
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
    ) 
BUILD DEFERRED REFRESH ON DEMAND AS
SELECT
    CAST(ROWNUM AS INTEGER) AS objectid,
    koz.koz_identif,
    koz.begin_geldigheid,
    koz.type,
    COALESCE(koz.sectie, '') || ' ' || COALESCE(koz.perceelnummer, '') AS aanduiding,
    COALESCE(koz.gemeentecode, '') || ' ' || COALESCE(koz.sectie, '') || ' ' || COALESCE(koz.perceelnummer, '') || ' ' || COALESCE(koz.appartementsindex, '') AS aanduiding2,
    koz.sectie,
    koz.perceelnummer,
    koz.appartementsindex,
    koz.gemeentecode,
    koz.aand_soort_grootte,
    koz.grootte_perceel,
    koz.oppervlakte_geom as oppervlakte_geom,
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
    mb_kad_onrrnd_zk_adres koz
ON
    zrr.koz_identif = koz.koz_identif;
    
CREATE UNIQUE INDEX MB_KOZ_RECHTH_OBJECTID ON MB_KOZ_RECHTH(OBJECTID ASC);
CREATE INDEX MB_KOZ_RECHTH_IDENTIF ON MB_KOZ_RECHTH(KOZ_IDENTIF ASC);
CREATE INDEX MB_KOZ_RECHTH_BEGR_PRCL_IDX ON MB_KOZ_RECHTH(BEGRENZING_PERCEEL)  INDEXTYPE IS MDSYS.SPATIAL_INDEX;

COMMENT ON MATERIALIZED VIEW mb_koz_rechth
IS 'commentaar view mb_koz_rechth:
kadastrale percelen een appartementsrechten met rechten en rechthebbenden en objectid voor geoserver/arcgis
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
* begrenzing_perceel: perceelvlak';

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.6.1_naar_1.6.2','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.2' WHERE naam='brmoversie';
