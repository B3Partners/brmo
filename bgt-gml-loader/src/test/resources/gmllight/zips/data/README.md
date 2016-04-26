# Testcase update zips

Bestanden kun je halen op: https://www.pdok.nl/nl/producten/pdok-downloads/download-basisregistratie-grootschalige-topografie.




## download commando's
eentje met veel spoor (IJmuiden):

  - curl -o code38451_aggrlevel0-20160429.zip "https://www.pdok.nl/download/service/extract.zip?extractset=gmllight&tiles=%7B%22layers%22%3A%5B%7B%22aggregateLevel%22%3A0%2C%22codes%22%3A%5B38451%5D%7D%5D%7D&excludedtypes=plaatsbepalingspunt&history=true&enddate=29-4-2016"

  - curl -o code51627_aggrlevel0-20160429.zip "https://www.pdok.nl/download/service/extract.zip?extractset=gmllight&tiles=%7B%22layers%22%3A%5B%7B%22aggregateLevel%22%3A0%2C%22codes%22%3A%5B51627%5D%7D%5D%7D&excludedtypes=plaatsbepalingspunt&history=true&enddate=29-4-2016"
  - curl -o code51625_aggrlevel0-20160429.zip "https://www.pdok.nl/download/service/extract.zip?extractset=gmllight&tiles=%7B%22layers%22%3A%5B%7B%22aggregateLevel%22%3A0%2C%22codes%22%3A%5B51625%5D%7D%5D%7D&excludedtypes=plaatsbepalingspunt&history=true&enddate=29-4-2016"

Gebruik het ant script `build.xml` om een subset te maken van `code51627_aggrlevel0-20160429.zip` -> `extract-gmllight.zip` mogelijk moet er dan een aantal testverwachtingen worden bijgwerkt mbt. aantal features
