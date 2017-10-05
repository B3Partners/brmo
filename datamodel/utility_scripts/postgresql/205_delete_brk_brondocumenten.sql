-- brondocument tabel kan niet in zijn geheel gewist worden
-- omdat meerdere basisregistraties daar samenkomen
delete from brondocument where tabel in ('zak_recht','app_re','kad_perceel','kad_onrrnd_zaak_aantek');
