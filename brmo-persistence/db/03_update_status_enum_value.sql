-- update status van bestaande jobs neer geldige waarden van de enum
update automatisch_proces set status='WAITING' where status not in('WAITING', 'ERROR', 'ONBEKEND', 'PROCESSING');
