generated scripts rsgb datamodel
================================

In deze map staan de postgresql en oracle scripts voor het aanmaken van het RSGB schema. Deze scripts worden gegenereerd op basis van het originele RSGB 2.2 UML. Hierna worden een aantal acties ondernomen: herstellen fouten in RSGB 2.2, weghalen van een aantal contraints om BR's onafhankelijk te kunnen verversen en vullen van hulptabellen. Behalve vanwege bugs zal dit script niet aangepast worden zolang RSGB 2.2 de basis is van de BRMO. De versie wordt gedefinieerd door de timestamp van generatie.

Klantspecifieke aanpassingen worden zoveel mogelijk in een apart schema gedaan. Indien dergelijke aanpassingen toch nodig zijn, dan zullen hier aparte scripts voor worden aangemaakt; deze komen niet in dit gegenereerde script, maar zullen handmatig moeten worden toegevoegd. Deze klantspecifieke scripts bevinden zich meestal in de utility_scripts map.
