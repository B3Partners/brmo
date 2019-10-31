CREATE INDEX idx_gebrdoel
    ON public.gebouwd_obj_gebruiksdoel USING btree
    (fk_gbo_sc_identif ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX mb_kad_onrrnd_zak_archief_overgegaan_in_idx ON public.mb_kad_onrrnd_zk_archief USING btree (overgegaan_in);