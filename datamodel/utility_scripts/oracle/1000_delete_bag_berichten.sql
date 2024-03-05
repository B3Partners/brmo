-- verwijder alle BAG 1 data uit de staging database
DELETE FROM bericht WHERE soort = 'bag';
DELETE FROM laadproces WHERE soort = 'bag';