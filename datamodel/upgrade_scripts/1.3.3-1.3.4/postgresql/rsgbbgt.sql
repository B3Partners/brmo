-- upgrade RSGBBGT datamodel van 1.3.3 naar 1.3.4 (PostgreSQL)

ALTER TABLE begroeid_terreindeel 
        ALTER COLUMN kruinlijn TYPE geometry(GEOMETRY,28992);
ALTER TABLE onbegroeid_terreindeel 
        ALTER COLUMN kruinlijn TYPE geometry(GEOMETRY,28992);
ALTER TABLE ondersteunend_wegdeel 
        ALTER COLUMN kruinlijn TYPE geometry(GEOMETRY,28992);
ALTER TABLE wegdeel 
        ALTER COLUMN kruinlijn TYPE geometry(GEOMETRY,28992);
