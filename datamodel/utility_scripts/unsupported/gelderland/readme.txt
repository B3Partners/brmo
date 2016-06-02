Kad_gemeente.zip bevat script voor vullen van, jawel, kad_gemeente.  Ten behoeve van onder andere pm_kadaster_perceel_eigenaar en pm_p8_kadaster_perceel om gemeente namen bij kadaster coderingen te tonen.

Algemeen:

Views zijn voor postgres 9.1, ergo geen materialized views. Er wordt een tmp tabel aangemaakt, die later hernoemd wordt.

Aantal indexen zijn overbodig/onzinnig… Zou nog een keer kritisch naar gekeken moeten worden.

Hier en daar wat ‘vervuiling’ door uit gecommentarieerde code.

Overigen:
pv_info_i_koz_adres_sk == pv_info_i_koz_adres, maar verschil zit in het gebruik van de pv ipv pm views. Stamt nog uit de tijd dat het verversen nog niet automatisch verliep.

pv_info_i_koz_zak_recht_sk == pv_info_i_koz_zak_recht + kolom zre.kadaster_identif  (zoals lang lang geleden besproken is) om koppeling met pv_info_i_koz_zak_recht_aant te maken.

PM_perceel_kadaster_archief maakt een paar ondersteunende _archief views aan. Oude SQL, nog geen mooie header …

