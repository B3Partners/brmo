-- 
-- upgrade Oracle RSGB datamodel van 2.0.0 naar 2.0.1 
--
-- issue #746 maak mb_percelenkaart nieuw aan
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

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.0.0_naar_2.0.1','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.0.1' WHERE naam='brmoversie';
