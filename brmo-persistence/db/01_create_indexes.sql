create index idx_bericht_job_id on bericht(job_id);
create index idx_bericht_object_ref on bericht(object_ref);
create index idx_bericht_laadprocesid on bericht(laadprocesid);
create index idx_bericht_soort on bericht (soort);
create index idx_bericht_status on bericht (status);
create unique index idx_bericht_refiddatumnr on bericht(object_ref,datum,volgordenummer);
