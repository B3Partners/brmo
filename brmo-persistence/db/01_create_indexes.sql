create index idx_bericht_job_id on bericht(job_id);
create index idx_bericht_object_ref on bericht(object_ref);
create index idx_bericht_laadprocesid on bericht(laadprocesid);
create unique index idx_bericht_refiddatumnr on bericht(object_ref,datum,volgordenummer);