<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : update-brondocument.xsl
    Created on : August 18, 2016, 9:30 AM
    Author     : mark
    Description:
        update brondocument velden omschrijving, datum, ref_id
-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:snp="http://www.kadaster.nl/schemas/brk-levering/snapshot/v20120901"
                xmlns:ko="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-kadastraalobject/v20120701"
                xmlns:typ="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-typen/v20120201"
                xmlns:nen="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-NEN3610-2011/v20120201"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:recht="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-recht/v20120201"
                xmlns:rechtref="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-recht-ref/v20120201"
                xmlns:pers="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-persoon/v20120201"
                xmlns:nhr="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-nhr-rechtspersoon/v20120201"
                xmlns:gba="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-gba-persoon/v20120901"
                xmlns:GbaPersoonRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-gba-persoon-ref/v20120201"
                xmlns:Stuk="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-stuk/v20120201"
                xmlns:bagadres="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-bag-adres/v20120201"
                xmlns:fn="http://www.w3.org/2005/xpath-functions">

    <xsl:variable name="kad_oz_id" select="/snp:KadastraalObjectSnapshot/ko:Perceel/ko:identificatie/nen:lokaalId | /snp:KadastraalObjectSnapshot/ko:Appartementsrecht/ko:identificatie/nen:lokaalId"/>

    <xsl:template match="/">
        <root>
            <data>

                <xsl:apply-templates select="/snp:KadastraalObjectSnapshot/Stuk:*"/>

                <xsl:for-each select="/snp:KadastraalObjectSnapshot/recht:Tenaamstelling">
                    <xsl:apply-templates select="."/>
                </xsl:for-each>
   
                <xsl:for-each select="/snp:KadastraalObjectSnapshot/recht:ZakelijkRecht">
                    <xsl:variable name="parent-id">
                        <xsl:call-template name="nen_identificatie">
                            <xsl:with-param name="id" select="recht:identificatie"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:for-each select="recht:ontstaanUit//recht:isGebaseerdOp | recht:betrokkenBij//recht:isGebaseerdOp">
                        <xsl:call-template name="is_gebaseerd_op_brondocument">
                            <xsl:with-param name="tabel">ZAK_RECHT</xsl:with-param>
                            <xsl:with-param name="tabel_identificatie" select="$parent-id"/>
                            <xsl:with-param name="omschrijving" select="concat(local-name(../..), ' ', local-name(..))"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </xsl:for-each>

                <xsl:for-each select="/snp:KadastraalObjectSnapshot/recht:Aantekening">
                    <xsl:apply-templates select="."/>
                </xsl:for-each>
            </data>
        </root>
    </xsl:template>

    <xsl:template match="/snp:KadastraalObjectSnapshot/recht:Tenaamstelling">
        <xsl:variable name="parent-id">
            <xsl:call-template name="nen_identificatie">
                <xsl:with-param name="id" select="recht:identificatie"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:for-each select="recht:isGebaseerdOp">
            <xsl:call-template name="is_gebaseerd_op_brondocument">
                <xsl:with-param name="tabel">ZAK_RECHT</xsl:with-param>
                <xsl:with-param name="tabel_identificatie" select="$parent-id"/>
                <xsl:with-param name="omschrijving" select="concat(local-name(), ' ', local-name(..))"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="/snp:KadastraalObjectSnapshot/recht:Aantekening">
        <xsl:variable name="parent-id">
            <xsl:call-template name="nen_identificatie">
                <xsl:with-param name="id" select="recht:identificatie"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:if test="recht:betreftAantekeningKadastraalObject">
            <xsl:for-each select="recht:isGebaseerdOp">
                <xsl:call-template name="is_gebaseerd_op_brondocument">
                    <xsl:with-param name="tabel">KAD_ONRRND_ZAAK_AANTEK</xsl:with-param>
                    <xsl:with-param name="tabel_identificatie" select="$parent-id"/>
                    <xsl:with-param name="omschrijving" select="concat(local-name(), ' ', local-name(..))"/>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>
        <xsl:if test="recht:betreftAantekeningRecht" >
            <xsl:for-each select="recht:isGebaseerdOp">
                <xsl:call-template name="is_gebaseerd_op_brondocument">
                    <xsl:with-param name="tabel">KAD_ONRRND_ZAAK_AANTEK</xsl:with-param>
                    <xsl:with-param name="tabel_identificatie" select="$parent-id"/>
                    <xsl:with-param name="omschrijving" select="concat(local-name(), ' ', local-name(..))"/>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <xsl:template match="Stuk:*">
        <xsl:variable name="parent-id">
            <xsl:call-template name="nen_identificatie">
                <xsl:with-param name="id" select="Stuk:identificatie"/>
            </xsl:call-template>
        </xsl:variable>
    
        <brondocument ignore-duplicates="yes">
            <identificatie>
                <xsl:value-of select="$parent-id"/>
            </identificatie>
            <xsl:choose>
                <xsl:when test="Stuk:deelEnNummer">
                    <xsl:for-each select="Stuk:deelEnNummer">
                        <omschrijving>
                            <xsl:text>deel: </xsl:text>
                            <xsl:value-of select="Stuk:deel"/>
                            <xsl:text>, nummer: </xsl:text>
                            <xsl:value-of select="Stuk:nummer"/>
                            <xsl:text>, registercode: </xsl:text>
                            <xsl:value-of select="Stuk:registercode/typ:waarde"/>
                            <xsl:text>, soortregister: </xsl:text>
                            <xsl:value-of select="Stuk:soortRegister/typ:waarde"/>
                        </omschrijving>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <omschrijving>
                        <xsl:if test="Stuk:AKRPortefeuilleNr">
                            <xsl:text>AKR Portefeuille Nr: </xsl:text>
                            <xsl:value-of select="Stuk:AKRPortefeuilleNr"/>
                        </xsl:if>
                    </omschrijving>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:choose>
                <xsl:when test="../ko:Perceel">
                    <tabel>KAD_PERCEEL</tabel>
                    <tabel_identificatie>
                        <xsl:value-of select="../ko:Perceel/ko:identificatie/nen:lokaalId"/>
                    </tabel_identificatie>
                    <ref_id>
                        <xsl:value-of select="../ko:Perceel/ko:identificatie/nen:lokaalId"/>
                    </ref_id>
                </xsl:when>
                <xsl:when test="../ko:Appartementsrecht">
                    <tabel>APP_RE</tabel>
                    <tabel_identificatie>
                        <xsl:value-of select="../ko:Appartementsrecht/ko:identificatie/nen:lokaalId"/>
                    </tabel_identificatie>
                    <ref_id>
                        <xsl:value-of select="../ko:Appartementsrecht/ko:identificatie/nen:lokaalId"/>
                    </ref_id>
                </xsl:when>
            </xsl:choose>
        </brondocument>

        <xsl:for-each select="Stuk:omvat/Stuk:Stukdeel">
            <brondocument ignore-duplicates="yes">
                <identificatie>
                    <xsl:call-template name="nen_identificatie">
                        <xsl:with-param name="id" select="Stuk:identificatie"/>
                    </xsl:call-template>
                </identificatie>
                <tabel>BRONDOCUMENT</tabel>
                <tabel_identificatie>
                    <xsl:value-of select="$parent-id"/>
                </tabel_identificatie>
                <xsl:for-each select="Stuk:aardStukdeel">
                    <omschrijving>
                        <xsl:value-of select="typ:waarde"/>
                    </omschrijving>
                </xsl:for-each>
                <xsl:for-each select="../../Stuk:tijdstipAanbieding">
                    <datum>
                        <xsl:value-of select="."/>
                    </datum>
                </xsl:for-each>
                <ref_id>
                    <xsl:value-of select="$kad_oz_id"/>
                </ref_id>
            </brondocument>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="is_gebaseerd_op_brondocument">
        <xsl:param name="tabel"/>
        <xsl:param name="tabel_identificatie"/>
        <xsl:param name="omschrijving"/>
        <xsl:param name="ref_id" select="$kad_oz_id"/>

        <xsl:for-each select="StukRef:StukdeelRef" xmlns:StukRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-stuk-ref/v20120201">
            <xsl:variable name="id" select="substring(@xlink:href,2)"/>
            <brondocument ignore-duplicates="yes">
                <identificatie>
                    <xsl:call-template name="nen_identificatie">
                        <xsl:with-param name="id" select="//Stuk:Stukdeel[@id = $id]/Stuk:identificatie"/>
                    </xsl:call-template>
                </identificatie>
                <tabel>
                    <xsl:value-of select="$tabel"/>
                </tabel>
                <tabel_identificatie>
                    <xsl:value-of select="$tabel_identificatie"/>
                </tabel_identificatie>
                <xsl:if test="$omschrijving">
                    <omschrijving>
                        <xsl:value-of select="$omschrijving"/>
                    </omschrijving>
                </xsl:if>
                <datum>
                    <xsl:value-of select="//Stuk:Stukdeel[@id = $id]/../../Stuk:tijdstipAanbieding"/>
                </datum>
                <xsl:if test="$ref_id">
                    <ref_id>
                        <xsl:value-of select="$ref_id"/>
                    </ref_id>
                </xsl:if>
            </brondocument>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="nen_identificatie">
        <xsl:param name="id"/>
        <xsl:value-of select="$id/nen:namespace"/>.<xsl:value-of select="$id/nen:lokaalId"/>
    </xsl:template>

</xsl:stylesheet>
