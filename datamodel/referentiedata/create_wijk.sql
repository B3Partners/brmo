SELECT 'INSERT INTO wijk (code, naam) VALUES (' || to_number(wk_code, '99999999') ||', '''|| wk_naam ||''');'
as "-- cbs wijken 2021" 
FROM cbs_wijken2021 WHERE water = 'NEE' ORDER BY wk_code;
