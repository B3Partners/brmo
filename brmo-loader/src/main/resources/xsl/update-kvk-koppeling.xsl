<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cat="http://schemas.kvk.nl/schemas/hrip/catalogus/2015/02"
                version="1.0"
>
    <xsl:import href="nhr-to-rsgb-xml-3.0.xsl" />
    <xsl:template match="/">
        <root>
            <data>
                <xsl:for-each select="cat:maatschappelijkeActiviteit">
                    <xsl:for-each select="cat:heeftAlsEigenaar/*[not(local-name()='relatieRegistratie')]">
                        <xsl:call-template name="persoon" />
                    </xsl:for-each>

                    <maatschapp_activiteit column-dat-beg-geldh="datum_aanvang"
                                           column-datum-einde-geldh="datum_einde_geldig">
                        <kvk_nummer>
                            <xsl:value-of select="cat:kvkNummer"/>
                        </kvk_nummer>

                        <fk_4pes_sc_identif>
                            <xsl:apply-templates select="cat:heeftAlsEigenaar/*[not(local-name()='relatieRegistratie')]" mode="object_ref"/>
                        </fk_4pes_sc_identif>
                    </maatschapp_activiteit>
                </xsl:for-each>
            </data>
        </root>
    </xsl:template>
</xsl:stylesheet>
