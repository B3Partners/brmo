-- verwijder alle BRK 1 data uit de staging database
DELETE FROM bericht WHERE soort = 'brk';
DELETE FROM laadproces WHERE soort = 'brk';