-- 
-- upgrade Oracle RSGB datamodel van 2.0.0 naar 2.0.1 
--

-- GH #766 update nationaliteiten tabel
UPDATE nation SET eindd_geldh = '20190212' WHERE code = 86;
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (88,'Burger van de Republiek Noord-Macedonië','20190212',null);


-- GH issue #746 maak mb_percelenkaart nieuw aan
DROP MATERIALIZED VIEW mb_percelenkaart;

CREATE MATERIALIZED VIEW mb_percelenkaart
    BUILD DEFERRED
    REFRESH
        ON DEMAND
AS
    SELECT
        CAST(ROWNUM AS INTEGER) AS objectid,
        qry.identif                     AS koz_identif,
        koz.dat_beg_geldh               AS begin_geldigheid,
        TO_DATE(koz.dat_beg_geldh,'YYYY-MM-DD') AS begin_geldigheid_datum,
        qry.type,
        ( coalesce(qry.ka_sectie,'')
          || ' '
          || coalesce(qry.ka_perceelnummer,'') ) AS aanduiding,
        ( coalesce(qry.ka_kad_gemeentecode,'')
          || ' '
          || coalesce(qry.ka_sectie,'')
          || ' '
          || coalesce(qry.ka_perceelnummer,'')
          || ' ' ) AS aanduiding2,
        qry.ka_sectie                   AS sectie,
        qry.ka_perceelnummer            AS perceelnummer,
        qry.ka_kad_gemeentecode         AS gemeentecode,
        qry.aand_soort_grootte,
        CAST(qry.grootte_perceel AS INTEGER) AS grootte_perceel,
        CASE
            WHEN qry.begrenzing_perceel.get_gtype() IS NOT NULL THEN sdo_geom.sdo_area(qry.begrenzing_perceel,0.1)
            ELSE NULL
        END AS oppervlakte_geom,
        b.datum                         AS verkoop_datum,
        koz.cu_aard_cultuur_onbebouwd   AS aard_cultuur_onbebouwd,
        CAST(koz.ks_bedrag AS INTEGER)  AS bedrag,
        koz.ks_koopjaar                 AS koopjaar,
        koz.ks_meer_onroerendgoed       AS meer_onroerendgoed,
        koz.ks_valutasoort              AS valutasoort,
        aant.aantekeningen              AS aantekeningen,
        CASE
            WHEN qry.begrenzing_perceel.get_gtype() IS NOT NULL THEN sdo_cs.transform(sdo_geom.sdo_centroid(qry.begrenzing_perceel
           ,0.1),4326).sdo_point.x
            ELSE NULL
        END AS lon,
        CASE
            WHEN qry.begrenzing_perceel.get_gtype() IS NOT NULL THEN sdo_cs.transform(sdo_geom.sdo_centroid(qry.begrenzing_perceel
           ,0.1),4326).sdo_point.y
        END AS lat,
        qry.begrenzing_perceel
    FROM
        (
            SELECT
                p.sc_kad_identif   AS identif,
                'perceel' AS type,
                p.ka_sectie,
                p.ka_perceelnummer,
                p.ka_kad_gemeentecode,
                p.aand_soort_grootte,
                p.grootte_perceel,
                p.begrenzing_perceel
            FROM
                kad_perceel p
        ) qry
        JOIN kad_onrrnd_zk koz ON koz.kad_identif = qry.identif
        LEFT JOIN (
            SELECT
                brondocument.ref_id,
                MAX(brondocument.datum) AS datum
            FROM
                brondocument
            WHERE
                brondocument.omschrijving = 'Akte van Koop en Verkoop'
            GROUP BY
                brondocument.ref_id
        ) b ON koz.kad_identif = b.ref_id
        LEFT JOIN (
            SELECT
                fk_4koz_kad_identif,
                LISTAGG('id: '
                          || coalesce(koza.kadaster_identif_aantek,'')
                          || ', '
                          || 'aard: '
                          || coalesce(koza.aard_aantek_kad_obj,'')
                          || ', '
                          || 'begin: '
                          || coalesce(koza.begindatum_aantek_kad_obj,'')
                          || ', '
                          || 'beschrijving: '
                          || coalesce(koza.beschrijving_aantek_kad_obj,'')
                          || ', '
                          || 'eind: '
                          || coalesce(koza.eindd_aantek_kad_obj,'')
                          || ', '
                          || 'koz-id: '
                          || coalesce(koza.fk_4koz_kad_identif,0)
                          || ', '
                          || 'subject-id: '
                          || coalesce(koza.fk_5pes_sc_identif,'')
                          || '; ',' & ' ON OVERFLOW TRUNCATE WITH COUNT) WITHIN GROUP(
                    ORDER BY
                        koza.fk_4koz_kad_identif
                ) AS aantekeningen
            FROM
                kad_onrrnd_zk_aantek koza
            GROUP BY
                fk_4koz_kad_identif
        ) aant ON koz.kad_identif = aant.fk_4koz_kad_identif;

CREATE UNIQUE INDEX mb_percelenkaart_objectid ON mb_percelenkaart ( objectid ASC );
CREATE INDEX mb_percelenkaart_identif ON mb_percelenkaart ( koz_identif ASC );
CREATE INDEX mb_percelenkaart_bgrgpidx ON mb_percelenkaart ( begrenzing_perceel ) INDEXTYPE IS mdsys.spatial_index;

COMMENT ON MATERIALIZED VIEW mb_percelenkaart
IS  'commentaar view mb_percelenkaart:
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
* begrenzing_perceel: perceelvlak';

-- GH issue #736 toevoegen ingangsdatum_recht aan de views
DROP MATERIALIZED VIEW mb_koz_rechth;
DROP MATERIALIZED VIEW mb_zr_rechth;
DROP MATERIALIZED VIEW mb_avg_koz_rechth;
DROP MATERIALIZED VIEW mb_avg_zr_rechth;
DROP VIEW vb_util_zk_recht;

CREATE OR REPLACE VIEW vb_util_zk_recht (
    zr_identif,
    ingangsdatum_recht,
    aandeel,
    ar_teller,
    ar_noemer,
    subject_identif,
    koz_identif,
    indic_betrokken_in_splitsing,
    omschr_aard_verkregenr_recht,
    fk_3avr_aand,
    aantekeningen
) AS
    SELECT
        zr.kadaster_identif      AS zr_identif,
        zr.ingangsdatum_recht,
        ( CAST( (coalesce(CAST(zr.ar_teller AS CHARACTER VARYING(7) ), ('0') )
                   || ('/')
                   || coalesce(CAST(zr.ar_noemer AS CHARACTER VARYING(7) ), ('0') ) ) AS CHARACTER VARYING(20) ) ) AS aandeel,
        zr.ar_teller,
        zr.ar_noemer,
        zr.fk_8pes_sc_identif    AS subject_identif,
        zr.fk_7koz_kad_identif   AS koz_identif,
        zr.indic_betrokken_in_splitsing,
        avr.omschr_aard_verkregenr_recht,
        zr.fk_3avr_aand,
        (
            SELECT
                LISTAGG( ('id: '
                            || coalesce(zra.kadaster_identif_aantek_recht,'')
                            || ', '
                            || 'aard: '
                            || coalesce(zra.aard_aantek_recht,'')
                            || ', '
                            || 'begin: '
                            || coalesce(zra.begindatum_aantek_recht,'')
                            || ', '
                            || 'beschrijving: '
                            || coalesce(zra.beschrijving_aantek_recht,'')
                            || ', '
                            || 'eind: '
                            || coalesce(zra.eindd_aantek_recht,'')
                            || ', '
                            || 'zkr-id: '
                            || coalesce(zra.fk_5zkr_kadaster_identif,'')
                            || ', '
                            || 'subject-id: '
                            || coalesce(zra.fk_6pes_sc_identif,'')
                            || '; '),'&& ' ON OVERFLOW TRUNCATE WITH COUNT) WITHIN GROUP(
                    ORDER BY
                        zra.fk_5zkr_kadaster_identif
                ) AS aantekeningen
            FROM
                zak_recht_aantek zra
            WHERE
                zra.fk_5zkr_kadaster_identif = zr.kadaster_identif
        ) AS aantekeningen
    FROM
        zak_recht zr
        JOIN aard_verkregen_recht avr ON zr.fk_3avr_aand = avr.aand;

COMMENT ON TABLE vb_util_zk_recht
IS 'commentaar view vb_util_zk_recht:
zakelijk recht met opgezocht aard recht en berekend aandeel
beschikbare kolommen:
* zr_identif: natuurlijke id van zakelijk recht
* ingangsdatum_recht: -
* aandeel: samenvoeging van teller en noemer (1/2),
* ar_teller: teller van aandeel,
* ar_noemer: noemer van aandeel,
* subject_identif: natuurlijk id van subject (natuurlijk of niet natuurlijk) welke rechthebbende is,
* koz_identif: natuurlijk id van kadastrale onroerende zaak (perceel of appratementsrecht) dat gekoppeld is,
* indic_betrokken_in_splitsing: -,
* omschr_aard_verkregen_recht: tekstuele omschrijving aard recht,
* fk_3avr_aand: code aard recht,
* aantekeningen: samenvoeging van alle aantekening op dit recht';

CREATE MATERIALIZED VIEW mb_zr_rechth
    (
        objectid,
        zr_identif,
        ingangsdatum_recht,
        subject_identif,
        koz_identif,
        aandeel,
        omschr_aard_verkregenr_recht,
        indic_betrokken_in_splitsing,
        aantekeningen,
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
    )
BUILD DEFERRED
REFRESH ON DEMAND
AS
SELECT
    CAST(ROWNUM AS INTEGER) AS objectid,
    uzr.zr_identif as zr_identif,
    uzr.ingangsdatum_recht,
    uzr.subject_identif,
    uzr.koz_identif,
    uzr.aandeel,
    uzr.omschr_aard_verkregenr_recht,
    uzr.indic_betrokken_in_splitsing,
    uzr.aantekeningen,
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
    mb_subject vs
ON
    uzr.subject_identif = vs.subject_identif;

CREATE UNIQUE INDEX MB_ZR_RECHTH_OBJECTID ON MB_ZR_RECHTH(OBJECTID ASC);
CREATE INDEX MB_ZR_RECHTH_IDENTIF ON MB_ZR_RECHTH(ZR_IDENTIF ASC);

COMMENT ON MATERIALIZED VIEW mb_zr_rechth
IS 'commentaar view mb_zr_rechth:
alle zakelijke rechten met rechthebbenden en referentie naar kadastraal onroerende zaak (perceel of appartementsrecht)
beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* zr_identif: natuurlijke id van zakelijk recht,
* ingangsdatum_recht: -
* subject_identif: natuurlijk id van subject (natuurlijk of niet natuurlijk) welke rechthebbende is,
* koz_identif: natuurlijk id van kadastrale onroerende zaak (perceel of appratementsrecht) dat gekoppeld is,
* aandeel: samenvoeging van teller en noemer (1/2),
* omschr_aard_verkregen_recht: tekstuele omschrijving aard recht,
* indic_betrokken_in_splitsing: -,
* aantekeningen: samenvoeging van alle rechten voor dit recht,
* soort: soort subject zoals natuurlijk, niet-natuurlijk enz.
* geslachtsnaam: -
* voorvoegsel: -
* voornamen: -
* aand_naamgebruik:
- E (= Eigen geslachtsnaam)
- N (=Geslachtsnaam echtgenoot/geregistreerd partner na eigen geslachtsnaam)
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
* kvk_nummer: -';

CREATE MATERIALIZED VIEW mb_avg_zr_rechth (
    objectid,
    zr_identif,
    ingangsdatum_recht,
    subject_identif,
    koz_identif,
    aandeel,
    omschr_aard_verkregenr_recht,
    indic_betrokken_in_splitsing,
    aantekeningen,
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
)
    BUILD DEFERRED
    REFRESH
        ON DEMAND
AS
    SELECT
        CAST(ROWNUM AS INTEGER) AS objectid,
        uzr.zr_identif   AS zr_identif,
        uzr.ingangsdatum_recht,
        uzr.subject_identif,
        uzr.koz_identif,
        uzr.aandeel,
        uzr.omschr_aard_verkregenr_recht,
        uzr.indic_betrokken_in_splitsing,
        uzr.aantekeningen,
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
        JOIN mb_avg_subject vs ON uzr.subject_identif = vs.subject_identif;

CREATE UNIQUE INDEX mb_avg_zr_rechth_objectid ON mb_avg_zr_rechth(objectid ASC);
CREATE INDEX mb_avg_zr_rechth_identif ON mb_avg_zr_rechth(zr_identif ASC);

COMMENT ON MATERIALIZED VIEW mb_avg_zr_rechth
IS 'commentaar view mb_avg_zr_rechth:
alle zakelijke rechten met voor avg geschoonde rechthebbenden en referentie naar kadastraal onroerende zaak (perceel of appartementsrecht)
beschikbare kolommen:
* objectid: uniek id bruikbaar voor geoserver/arcgis,
* zr_identif: natuurlijke id van zakelijk recht,
* ingangsdatum_recht: -,
* subject_identif: natuurlijk id van subject (natuurlijk of niet natuurlijk) welke rechthebbende is,
* koz_identif: natuurlijk id van kadastrale onroerende zaak (perceel of appratementsrecht) dat gekoppeld is,
* aandeel: samenvoeging van teller en noemer (1/2),
* omschr_aard_verkregen_recht: tekstuele omschrijving aard recht,
* indic_betrokken_in_splitsing: -,
* aantekeningen: samenvoeging van alle aantekeningen van dit recht
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
* kvk_nummer: -';

CREATE MATERIALIZED VIEW mb_koz_rechth (
        objectid,
        koz_identif,
        begin_geldigheid,
		begin_geldigheid_datum,
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
        ingangsdatum_recht,
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
        aantekeningen,
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
    to_date(koz.begin_geldigheid, 'YYYY-MM-DD') AS begin_geldigheid_datum,
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
    zrr.ingangsdatum_recht,
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
    zrr.aantekeningen,
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
    mb_zr_rechth zrr
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
* begin_geldigheid_datum: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
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
* ingangsdatum_recht: - ,
* subject_identif: natuurlijk id van rechthebbende,
* aandeel: samenvoeging van teller en noemer (1/2),
* omschr_aard_verkregen_recht: tekstuele omschrijving aard recht,
* indic_betrokken_in_splitsing: -,
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
* kvk_nummer: -
* aantekeningen: samenvoeging van alle aantekeningen van dit recht,
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

CREATE MATERIALIZED VIEW mb_avg_koz_rechth (
    objectid,
    koz_identif,
    begin_geldigheid,
	begin_geldigheid_datum,
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
    ingangsdatum_recht,
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
    aantekeningen,
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
    to_date(koz.begin_geldigheid, 'YYYY-MM-DD') AS begin_geldigheid_datum,
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
    zrr.ingangsdatum_recht,
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
    zrr.aantekeningen,
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
    mb_avg_zr_rechth zrr
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
* begin_geldigheid_datum: datum wanneer dit object geldig geworden is (ontstaat of bijgewerkt),
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
* ingangsdatum_recht: - ,
* subject_identif: natuurlijk id van rechthebbende,
* aandeel: samenvoeging van teller en noemer (1/2),
* omschr_aard_verkregen_recht: tekstuele omschrijving aard recht,
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
* aantekeningen: samenvoeging van alle aantekeningen van dit recht,
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



-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.0.0_naar_2.0.1','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.0.1' WHERE naam='brmoversie';
