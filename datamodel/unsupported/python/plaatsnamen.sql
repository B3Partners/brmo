-- gebruik deze query om het plaatsnamen bestand te maken,
--     pas eventueel het pad naar het (absolute) bestand aan
COPY (
    SELECT
        fk_7gem_code AS "GEMCODE",
        naam         AS "NAAM",
        identif      AS "PLAATSCODE"
    FROM
        wnplts
    WHERE
        fk_7gem_code IS NOT NULL
)
-- check pad voor bestand, dit is op de database server
TO '/tmp/plaatsnamen.csv'
WITH (FORMAT CSV, HEADER, QUOTE '"', FORCE_QUOTE('NAAM', 'PLAATSCODE'), DELIMITER ',', ENCODING 'UTF-8');