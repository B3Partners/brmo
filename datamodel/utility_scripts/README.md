De scripts in de map utility_scripts worden niet opgenomen in het gegenereerde totaal script. Deze scripts zijn deels bedoeld voor aanpassingen aan het datamodel uit de tijd voor de aanmaak van specifieke upgrade scripts:
- 400_add_bsn.sql 
- 410_innp_add_rsin.sql

Deels zijn dit scripts waarmee hulptabellen gevuld worden of waarmee de hulptabel van een update wordt voorzien. Tot slot kan de database opgeschoond worden met de scripts.

De beheerder dient duidelijk voor ogen te hebben wat het doel is van ieder script. Het is uitdrukkelijk niet de bedoeling om deze scripts in het kader van een installatie allemaal te draaien.
