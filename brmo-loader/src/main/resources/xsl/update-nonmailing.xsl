<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cat="http://schemas.kvk.nl/schemas/hrip/catalogus/2015/02"
                version="1.0"
>

    <xsl:template match="/">

        <root>
            <data>

                <xsl:for-each select="cat:maatschappelijkeActiviteit">

                    <maatschapp_activiteit column-dat-beg-geldh="datum_aanvang"
                                           column-datum-einde-geldh="datum_einde_geldig">
                        <kvk_nummer>
                            <xsl:value-of select="cat:kvkNummer"/>
                        </kvk_nummer>
                        <!-- update/vul nonMailing attribuut-->
                        <nonmailing>
                            <xsl:value-of select="cat:nonMailing/cat:omschrijving"/>
                        </nonmailing>
                    </maatschapp_activiteit>

                </xsl:for-each>
            </data>
        </root>

    </xsl:template>

</xsl:stylesheet>