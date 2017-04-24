# BGT-GML Light Loader

Via PDOK zijn BGT extracten in 4 verschillende formaten te downloaden vanaf https://www.pdok.nl/nl/producten/pdok-downloads/download-basisregistratie-grootschalige-topografie
Deze lader ondersteund het GML Light formaat zonder plaatsbepalingspunten.

## data ophalen

Heel NL in GML Light formaat ophalen met einddatum 5-4-2016: `wget -O extract-gmllight-5-4-2016.zip "https://www.pdok.nl/download/service/extract.zip?extractset=gmllight&tiles=%7B%22layers%22%3A%5B%7B%22aggregateLevel%22%3A0%2C%22codes%22%3A%5B%5D%7D%2C%7B%22aggregateLevel%22%3A1%2C%22codes%22%3A%5B%5D%7D%2C%7B%22aggregateLevel%22%3A2%2C%22codes%22%3A%5B%5D%7D%2C%7B%22aggregateLevel%22%3A3%2C%22codes%22%3A%5B%5D%7D%2C%7B%22aggregateLevel%22%3A4%2C%22codes%22%3A%5B%5D%7D%2C%7B%22aggregateLevel%22%3A5%2C%22codes%22%3A%5B9%2C12%2C13%2C24%2C26%2C18%2C15%2C14%2C11%2C36%2C37%2C48%2C27%2C49%2C51%2C57%2C56%2C50%2C39%2C45%5D%7D%5D%7D&excludedtypes=plaatsbepalingspunt&history=true&enddate=5-4-2016"`

Beter is om een aantal kleinere bestanden te downloaden, plaats deze bij elkaar in een directory, in het configuratie bestand komt deze terug in de `scandirectory` optie.
