-- brmo versie informatie

CREATE TABLE brmo_metadata
    (
        naam VARCHAR(255) NOT NULL,
        waarde VARCHAR(255),
        PRIMARY KEY (naam)
    );

GO

EXEC sys.sp_addextendedproperty @name=N'comment', @value=N'BRMO metadata en versie gegevens' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'brmo_metadata'

GO
