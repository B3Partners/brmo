SELECT 'INSERT INTO buurt (code, naam) VALUES (' || to_number(bu_code, '9999999999') ||', '''|| bu_naam ||''');'
as "-- cbs buurten 2021" 
FROM cbs_buurten2021 WHERE water = 'NEE' ORDER BY bu_code;
