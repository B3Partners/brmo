--View die alleen de inhoud van de tabel kad_perceel toont waarbij de begrenzing_perceel niet NULL is
--Naam view: v_kad_perceel_metgeom
--Datum: 17/02/2016
--Auteur: Meine Toonen

CREATE VIEW
    V_KAD_PERCEEL_METGEOM
    (
        SC_KAD_IDENTIF,
        AAND_SOORT_GROOTTE,
        GROOTTE_PERCEEL,
        OMSCHR_DEELPERCEEL,
        FK_7KDP_SC_KAD_IDENTIF,
        KA_DEELPERCEELNUMMER,
        KA_KAD_GEMEENTECODE,
        KA_PERCEELNUMMER,
        KA_SECTIE,
        BEGRENZING_PERCEEL,
        PLAATSCOORDINATEN_PERCEEL
    ) AS
SELECT
    "SC_KAD_IDENTIF",
    "AAND_SOORT_GROOTTE",
    "GROOTTE_PERCEEL",
    "OMSCHR_DEELPERCEEL",
    "FK_7KDP_SC_KAD_IDENTIF",
    "KA_DEELPERCEELNUMMER",
    "KA_KAD_GEMEENTECODE",
    "KA_PERCEELNUMMER",
    "KA_SECTIE",
    "BEGRENZING_PERCEEL",
    "PLAATSCOORDINATEN_PERCEEL"
FROM
    kad_perceel p
WHERE
    p.begrenzing_perceel.sdo_srid != 0;
