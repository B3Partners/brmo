-- 
-- upgrade SQLserver RSGB datamodel van 1.6.0 naar 1.6.1 
--
DROP VIEW vb_kad_onrrnd_zk_archief;
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
    kozhr.fk_sc_rh_koz_kad_identif = koza.kad_identif;

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

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.6.0_naar_1.6.1','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.1' WHERE naam='brmoversie';
