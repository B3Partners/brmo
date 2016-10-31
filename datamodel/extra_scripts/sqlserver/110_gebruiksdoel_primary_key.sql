ALTER TABLE
    gebouwd_obj_gebruiksdoel ALTER COLUMN gebruiksdoel_gebouwd_obj VARCHAR(80) NOT NULL;
    
GO
    
ALTER TABLE
    gebouwd_obj_gebruiksdoel ALTER COLUMN fk_gbo_sc_identif VARCHAR(16) NOT NULL;

GO

ALTER TABLE gebouwd_obj_gebruiksdoel
  ADD CONSTRAINT pk_geb_obj_gebr_doel PRIMARY KEY (gebruiksdoel_gebouwd_obj, fk_gbo_sc_identif);

GO
