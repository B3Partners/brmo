-- verwijder alle BAG 1 data uit de staging database
delete from bericht where soort = 'bag';
delete from laadproces where soort = 'bag';