# Notities

Digilevering draait op Digikoppeling, een ebMS implementatie. Er worden gebeurtenis
berichten door Digilevering verstrekt aan het afnemer (klant) systeem.
Welke gebeurtenissen er precies aan de afnemer worden verstrekt is
afhankelijk van de CPA (Collaboration Protocol Agreement).

Vooralsnog zijn de volgende berichten van belang voor de implementatie:

  - Digilevering BT-4: controlebericht Ping
  - Digilevering BT-5: antwoordbericht Pong
  - Digilevering BT-2: Gebeurtenis afnemer

De berichten gebruiken ebMS standaard uit OSB 2.0 waarvan alleen "WS-2: Ontvang Gebeurtenis" nodig is.

OCVService-SR:1:0


## compliance
Compliance test kan worden uitgevoerd tegen: https://www.ebms.cv.osb.overheid.nl/

## Mule adapter
  
  - https://sourceforge.net/p/muleebmsadapter/code/ci/master/tree/ en https://github.com/MartinMulder/ebmsadapter

## documentatie

  - [Koppelvlak specificatie digilevering](pdf/121801_Koppelvlakspecificatie.pdf) (van https://www.logius.nl/ondersteuning/digilevering/)
  - [Koppelvlakbeschrijving Digipoort Digikoppeling WUS 2.0 (overheden) v1.2](pdf/Koppelvlakbeschrijving%20Digikoppeling%20ebMS_1.2.pdf) (via https://www.logius.nl/ondersteuning/gegevensuitwisseling/koppelvlak-ebms-overheden/)
  - [Servicebeschrijving ebMS 2.0 Afleveren v1.2](pdf/Servicebeschrijving%20DigiPoort%20ebMS%202%200%20Afleveren_v1.2.pdf) (via https://www.logius.nl/ondersteuning/gegevensuitwisseling/koppelvlak-ebms-overheden/)
  - [Handleiding CPA creatievoorziening](pdf/Handleiding_CPA_creatievoorziening_v1.3_.pdf) (via https://www.cpa.serviceregister.overheid.nl/Handleiding_CPA_creatievoorziening_v1.3_.pdf)
  - [Best Practices ebMS Digikoppeling 2.0](pdf/Fase2_Digikoppeling_2.0_Best_Practices_ebMS_v1.6.pdf)


## links

  - https://www.logius.nl/ondersteuning/digilevering/
  - https://www.logius.nl/ondersteuning/gegevensuitwisseling/koppelvlak-ebms-overheden/
  - https://www.logius.nl/ondersteuning/gegevensuitwisseling/koppelvlak-wus-overheden/
  - CPA creatie website: https://www.cpa.serviceregister.overheid.nl/
  - CPA "afnemer" aanvragen op de "Digikoppeling Compliancevoorziening ebMS" website: https://www.ebms.cv.osb.overheid.nl/cv-1p1/
  - https://digistandaarden.pleio.nl/
  
