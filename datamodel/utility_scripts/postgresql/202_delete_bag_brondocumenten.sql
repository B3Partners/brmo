-- brondocument tabel kan niet in zijn geheel gewist worden
-- omdat meerdere basisregistraties daar samenkomen
delete from brondocument where tabel in ('verblijfsobject','woonplaats','standplaats','pand','ligplaats','openbareruimte','nummeraanduiding');

