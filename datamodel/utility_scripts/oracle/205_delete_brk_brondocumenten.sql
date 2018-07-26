-- brondocument tabel kan niet in zijn geheel gewist worden
-- omdat meerdere basisregistraties daar samenkomen
delete from BRONDOCUMENT where tabel in ('ZAK_RECHT','APP_RE','KAD_PERCEEL','KAD_ONRRND_ZAAK_AANTEK');
