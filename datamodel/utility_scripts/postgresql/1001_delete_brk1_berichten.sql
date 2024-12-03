-- verwijder alle BAG 1 data uit de staging database
delete from bericht where soort = 'brk';
delete from laadproces where soort = 'brk';