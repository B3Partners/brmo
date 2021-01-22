<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cat="http://schemas.kvk.nl/schemas/hrip/catalogus/2015/02"
                version="1.0"
>
    <xsl:import href="nhr-object-ref-3.0.xsl"/>

    <xsl:template match="/">

        <root>
            <data>
                <xsl:if test="/cat:commercieleVestiging/cat:vestigingsnummer">
                    <xsl:variable name="key"><xsl:apply-templates select="." mode="object_ref"/></xsl:variable>

                    <xsl:for-each select="cat:nietCommercieleVestiging | cat:commercieleVestiging">
                        <vestg>
                            <sc_identif>
                                <xsl:value-of select="$key"/>
                            </sc_identif>

                            <xsl:for-each select="cat:activiteiten/cat:sbiActiviteit">
                                <!-- vullen als hoofd activiteit -->
                                <xsl:if test="./cat:isHoofdactiviteit/cat:code = 'J'">
                                    <fk_sa_sbi_activiteit_sbi_code>
                                        <xsl:value-of select="./cat:sbiCode/cat:code"/>
                                    </fk_sa_sbi_activiteit_sbi_code>
                                    <sa_indic_hoofdactiviteit>
                                        <xsl:value-of select="./cat:isHoofdactiviteit/cat:omschrijving"/>
                                    </sa_indic_hoofdactiviteit>
                                </xsl:if>
                            </xsl:for-each>
                        </vestg>
                    </xsl:for-each>

                </xsl:if>
            </data>
        </root>

    </xsl:template>

</xsl:stylesheet>