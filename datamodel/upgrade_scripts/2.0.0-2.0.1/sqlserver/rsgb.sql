-- 
-- upgrade SQLserver RSGB datamodel van 2.0.0 naar 2.0.1 
--

-- GH #766 update nationaliteiten tabel
UPDATE nation SET eindd_geldh = '20190212' WHERE code = 86;
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (88,'Burger van de Republiek Noord-Macedonië','20190212',null);


-- GH issue #736 toevoegen ingangsdatum_recht aan de views
DROP VIEW vb_koz_rechth;
DROP VIEW vb_zr_rechth;
DROP VIEW vb_avg_koz_rechth;
DROP VIEW vb_avg_zr_rechth;
DROP VIEW vb_util_zk_recht;

CREATE VIEW vb_util_zk_recht (
        zr_identif,
        ingangsdatum_recht,
        aandeel,
        ar_teller,
        ar_noemer,
        subject_identif,
        koz_identif,
        indic_betrokken_in_splitsing,
        omschr_aard_verkregen_recht,
        fk_3avr_aand,
        aantekeningen
    ) AS
SELECT
    zr.kadaster_identif AS zr_identif,
    zr.ingangsdatum_recht,
    ( (COALESCE(CAST(zr.ar_teller AS VARCHAR), ('0')) + ('/')) + COALESCE(CAST(zr.ar_noemer AS VARCHAR), ('0')) ) AS aandeel,
    zr.ar_teller,
    zr.ar_noemer,
    zr.fk_8pes_sc_identif  AS subject_identif,
    zr.fk_7koz_kad_identif AS koz_identif,
    zr.indic_betrokken_in_splitsing,
    avr.omschr_aard_verkregenr_recht,
    zr.fk_3avr_aand,
    (SELECT STRING_AGG( CAST( CONCAT_WS(' ',
                            'id:', COALESCE(zra.kadaster_identif_aantek_recht, ''),
                            ', aard:', COALESCE(zra.aard_aantek_recht, ''),
                            ', begin:', COALESCE(zra.begindatum_aantek_recht, ''),
                            ', beschrijving:', COALESCE(zra.beschrijving_aantek_recht, ''),
                            ', eind:', COALESCE(zra.eindd_aantek_recht, ''),
                            ', zkr-id:', COALESCE(zra.fk_5zkr_kadaster_identif, ''),
                            ', subject-id:', COALESCE(zra.fk_6pes_sc_identif, ''),
                            ';')
                        AS VARCHAR(MAX) ), ' & ') WITHIN GROUP ( ORDER BY zra.fk_5zkr_kadaster_identif ) AS aantekeningen
                        FROM zak_recht_aantek zra
                        WHERE zra.fk_5zkr_kadaster_identif = zr.kadaster_identif
    ) AS aantekeningen
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
* aantekeningen: samenvoeging van alle aantekening op dit recht',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_util_zk_recht';

GO


CREATE VIEW vb_zr_rechth (
        objectid,
        zr_identif,
        ingangsdatum_recht,
        subject_identif,
        koz_identif,
        aandeel,
        omschr_aard_verkregen_recht,
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
    ) AS
SELECT
    CAST(row_number() OVER (ORDER BY uzr.zr_identif)AS INT) AS ObjectID,
    uzr.zr_identif                                          AS zr_identif,
    uzr.ingangsdatum_recht,
    uzr.subject_identif,
    uzr.koz_identif,
    uzr.aandeel,
    uzr.omschr_aard_verkregen_recht,
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
    vb_subject vs
ON
    uzr.subject_identif = vs.subject_identif;

GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'alle zakelijke rechten met rechthebbenden en referentie naar kadastraal onroerende zaak (perceel of appartementsrecht)

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
@level1type = N'View', @level1name = N'vb_zr_rechth';

GO


CREATE VIEW vb_avg_zr_rechth (
        objectid,
        zr_identif,
        ingangsdatum_recht,
        subject_identif,
        koz_identif,
        aandeel,
        omschr_aard_verkregen_recht,
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
    ) AS
SELECT
    CAST(row_number() OVER (ORDER BY uzr.zr_identif)AS INT) AS ObjectID,
    uzr.zr_identif                                          AS zr_identif,
    uzr.ingangsdatum_recht,
    uzr.subject_identif,
    uzr.koz_identif,
    uzr.aandeel,
    uzr.omschr_aard_verkregen_recht,
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
    vb_avg_subject vs
ON
    uzr.subject_identif = vs.subject_identif;

GO

EXEC sp_addextendedproperty
@name = N'comment',
@value = N'alle zakelijke rechten met voor avg geschoonde rechthebbenden en referentie naar kadastraal onroerende zaak (perceel of appartementsrecht)

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
* kvk_nummer: -',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_avg_zr_rechth';

GO


CREATE VIEW vb_koz_rechth (
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
        omschr_aard_verkregen_recht,
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
    ) AS
SELECT
    CAST(row_number() OVER (ORDER BY koz.koz_identif)AS INT)            AS ObjectID,
    koz.koz_identif,
    koz.begin_geldigheid,
    TRY_CONVERT(DATETIME, koz.begin_geldigheid_datum)                   AS begin_geldigheid_datum,
    koz.type,
    COALESCE(koz.sectie, '') + ' ' + COALESCE(koz.perceelnummer, '')    AS aanduiding,
    COALESCE(koz.gemeentecode, '') + ' ' + COALESCE(koz.sectie, '') + ' ' + COALESCE  (koz.perceelnummer, '') + ' ' + COALESCE(koz.appartementsindex, '') AS aanduiding2,
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
    zrr.ingangsdatum_recht,
    zrr.subject_identif,
    zrr.aandeel,
    zrr.omschr_aard_verkregen_recht,
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
* ingangsdatum_recht: - ,
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
- N (= Geslachtsnaam echtgenoot/geregistreerd partner na eigen geslachtsnaam)
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

CREATE VIEW vb_avg_koz_rechth (
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
        omschr_aard_verkregen_recht,
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
    ) AS
SELECT
    CAST(row_number() OVER (ORDER BY koz.koz_identif)AS INT)            AS ObjectID,
    koz.koz_identif                                                     AS koz_identif,
    koz.begin_geldigheid,
    TRY_CONVERT(DATETIME, koz.begin_geldigheid)                         AS begin_geldigheid_datum,
    koz.type,
    COALESCE(koz.sectie, '') + ' ' + COALESCE(koz.perceelnummer, '')    AS aanduiding,
    COALESCE(koz.gemeentecode, '') + ' ' + COALESCE(koz.sectie, '') + ' ' + COALESCE(koz.perceelnummer, '') + ' ' + COALESCE(koz.appartementsindex, '') AS aanduiding2,
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
    zrr.omschr_aard_verkregen_recht,
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
* begrenzing_perceel: perceelvlak',
@level0type = N'Schema', @level0name = N'dbo',
@level1type = N'View', @level1name = N'vb_avg_koz_rechth';

GO



-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.0.0_naar_2.0.1','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.0.1' WHERE naam='brmoversie';
