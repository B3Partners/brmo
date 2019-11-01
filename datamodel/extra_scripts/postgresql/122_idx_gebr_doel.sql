CREATE INDEX idx_gebrdoel
    ON public.gebouwd_obj_gebruiksdoel USING btree
    (fk_gbo_sc_identif ASC NULLS LAST)
    TABLESPACE pg_default;
