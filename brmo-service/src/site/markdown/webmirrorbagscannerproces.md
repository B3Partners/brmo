# WebMirrorBAGScannerProces

Dit is een automatisch proces dat BAG mutaties van de [mirror site](http://mirror.openstreetmap.nl/bag/mutatie/)
kan verwerken.

## Configuratie
Bij het aanmaken van een nieuw proces in de BRMO web interface zijn de belangrijkste velden voor-ingevuld,
dat zijn de scan directory url en de css path expressie. Verder kunnen (optioneel) een label en een
[cron expressie](./planning-configuratie.html) worden gegeven.

### Opslaan downloads
Optioneel kan een archief directory worden aangegeven om de `.zip` bestanden die gedownloaded zijn
lokaal op te slaan. Als het veld leeg is worden de bestanden niet opgeslagen (wel worden de mutatie
berichten in de staging database gezet).
