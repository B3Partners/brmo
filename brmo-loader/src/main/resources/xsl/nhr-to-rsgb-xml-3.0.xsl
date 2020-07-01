<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cat="http://schemas.kvk.nl/schemas/hrip/catalogus/2015/02"
                xmlns:fn="http://www.w3.org/2005/xpath-functions">

    <xsl:import href="nhr-object-ref-3.0.xsl"/>

    <!-- parameters van het bericht -->
    <xsl:param name="objectRef" />
    <xsl:param name="datum" />
    <xsl:param name="volgordeNummer" />
    <xsl:param name="soort" />
    <xsl:variable name="hoofdvestiging" select="/cat:maatschappelijkeActiviteit/cat:wordtGeleidVanuit//cat:vestigingsnummer"/>
    <xsl:variable name="peilmoment" select="/*/@peilmoment"/>
    <xsl:variable name="peilmoment-dateTime"><xsl:value-of select="substring($peilmoment,1,4)"/>-<xsl:value-of select="substring($peilmoment,5,2)"/>-<xsl:value-of select="substring($peilmoment,7,2)"/>T<xsl:value-of select="substring($peilmoment,9,2)"/>:<xsl:value-of select="substring($peilmoment,11,2)"/>:<xsl:value-of select="substring($peilmoment,13,2)"/></xsl:variable>
    <xsl:variable name="peilmomentDate"><xsl:value-of select="substring($peilmoment,1,4)"/>-<xsl:value-of select="substring($peilmoment,5,2)"/>-<xsl:value-of select="substring($peilmoment,7,2)"/></xsl:variable>

    <xsl:template match="/">
        <root>
            <xsl:comment>
                <xsl:text>objectRef: </xsl:text>
                <xsl:value-of select="$objectRef"/>
                <xsl:text>, datum: </xsl:text>
                <xsl:value-of select="$datum"/>
                <xsl:text>, volgordeNummer: </xsl:text>
                <xsl:value-of select="$volgordeNummer"/>
                <xsl:text>, soort: </xsl:text>
                <xsl:value-of select="$soort"/>
            </xsl:comment>
            <xsl:comment>
                <!-- <hoofdvestiging><xsl:value-of select="$hoofdvestiging"/></hoofdvestiging> -->
                <xsl:text>hoofdvestiging: </xsl:text><xsl:value-of select="$hoofdvestiging" />
            </xsl:comment>
            <data>
                <xsl:apply-templates select="*"/>
            </data>
        </root>
    </xsl:template>

    <xsl:template name="registratie-datum">
        <xsl:param name="begin" select="'datum_aanvang'"/>
        <xsl:param name="einde" select="'datum_einde_geldig'"/>
        <xsl:element name="{$begin}">
            <xsl:value-of select="cat:registratie/cat:datumAanvang"/>
        </xsl:element>
        <xsl:element name="{$einde}">
            <xsl:value-of select="cat:registratie/cat:datumEinde"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="natPersoon">
        <xsl:variable name="key"><xsl:apply-templates select="." mode="object_ref"/></xsl:variable>
        <!-- als er een bsn is dan INGESCHREVEN NATUURLIJK PERSOON -->
        <xsl:variable name="class">
            <xsl:choose>
                <xsl:when test="cat:bsn"><xsl:text>INGESCHREVEN NATUURLIJK PERSOON</xsl:text></xsl:when>
                <xsl:otherwise><xsl:text>NATUURLIJK PERSOON</xsl:text></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <comfort search-table="subject" search-column="identif" search-value="{$key}" snapshot-date="{$peilmomentDate}">

            <subject>
                <identif><xsl:value-of select="$key"/></identif>
                <clazz><xsl:value-of select="$class"/></clazz>
                <typering><xsl:value-of select="substring($class,1,35)"/></typering>
                <naam><xsl:value-of select="cat:volledigeNaam"/></naam>

                <xsl:call-template name="subject"/>
            </subject>

            <prs>
                <sc_identif><xsl:value-of select="$key"/></sc_identif>
                <clazz><xsl:value-of select="$class"/></clazz>
            </prs>

            <nat_prs>
                <sc_identif><xsl:value-of select="$key"/></sc_identif>
                <clazz><xsl:value-of select="$class"/></clazz>
                <aand_naamgebruik>
                    <xsl:choose>
                        <xsl:when test="cat:aanduidingNaamgebruik = 'EigenGeslachtsnaam'">E</xsl:when>
                        <xsl:when test="cat:aanduidingNaamgebruik = 'GeslachtsnaamPartner'">P</xsl:when>
                        <xsl:when test="cat:aanduidingNaamgebruik = 'GeslachtsnaamPartnerNaEigenGeslachtsnaam'">N</xsl:when>
                        <xsl:when test="cat:aanduidingNaamgebruik = 'GeslachtsnaamPartnerVoorEigenGeslachtsnaam'">V</xsl:when>
                    </xsl:choose>
                </aand_naamgebruik>
                <geslachtsaand>
                    <xsl:choose>
                        <xsl:when test="cat:geslachtsaanduiding = 'Man'">M</xsl:when>
                        <xsl:when test="cat:geslachtsaanduiding = 'Vrouw'">V</xsl:when>
                        <xsl:otherwise>O</xsl:otherwise>
                    </xsl:choose>
                </geslachtsaand>
                <nm_adellijke_titel_predikaat><xsl:value-of select="cat:adellijkeTitel"/></nm_adellijke_titel_predikaat>
                <nm_geslachtsnaam><xsl:value-of select="cat:geslachtsnaam"/></nm_geslachtsnaam>
                <nm_voornamen><xsl:value-of select="cat:voornamen"/></nm_voornamen>
                <nm_voorvoegsel_geslachtsnaam><xsl:value-of select="cat:voorvoegselGeslachtsnaam"/></nm_voorvoegsel_geslachtsnaam>
            </nat_prs>

            <ingeschr_nat_prs>
                <sc_identif><xsl:value-of select="$key"/></sc_identif>
                <clazz><xsl:value-of select="$class"/></clazz>
                <bsn><xsl:value-of select="cat:bsn"/></bsn>
                <fk_28nra_sc_identif><xsl:value-of select="cat:woonLocatie/cat:adres/cat:bagId/cat:identificatieAdresseerbaarObject"/></fk_28nra_sc_identif>
            </ingeschr_nat_prs>
        </comfort>
    </xsl:template>

    <xsl:template name="heeft">
        <xsl:for-each select="./*">
            <!--persoon-->
            <xsl:for-each select="cat:door">
                <xsl:variable name="key"><xsl:apply-templates select="*[1]" mode="object_ref" /></xsl:variable>
                <xsl:comment>
                    <xsl:text>node name: </xsl:text><xsl:value-of select="name(*[1])"/><xsl:text> heeft </xsl:text>
                    <xsl:choose>
                        <xsl:when test="*[1]//cat:bsn">wel</xsl:when>
                        <xsl:when test="not(*[1]//cat:bsn)">geen</xsl:when>
                        <xsl:otherwise>onbekend</xsl:otherwise>
                    </xsl:choose>
                    <xsl:text> BSN</xsl:text>
                </xsl:comment>
                <xsl:if test="$key =''">
                    <xsl:comment>dataprobleem - geen natuurlijke sleutel gevonden voor persoon en functionaris</xsl:comment>
                </xsl:if>
                <xsl:if test="$key !=''">
                    <!-- zonder key kunnen we geen persoon vastleggen -->
                    <xsl:comment>heeft node: "<xsl:value-of select="local-name(.)" />" met "<xsl:value-of select="local-name(*[1])" />" en persoon: <xsl:value-of select="name(*[1])" />; <xsl:value-of select="$key" /></xsl:comment>
                    <xsl:choose>
                        <xsl:when test="local-name(*[1]) = 'natuurlijkPersoon'">
                            <xsl:for-each select="cat:natuurlijkPersoon">
                                <xsl:call-template name="natPersoon" />
                            </xsl:for-each>
                        </xsl:when>

                        <xsl:when test="local-name(*[1]) = 'rechtspersoon'">
                            <xsl:apply-templates select="cat:rechtspersoon"/>
<!--                            <xsl:variable name="class">INGESCHREVEN NIET-NATUURLIJK PERSOON</xsl:variable>-->

<!--                            <subject>-->
<!--                                <identif><xsl:value-of select="$key" /></identif>-->
<!--                                <clazz><xsl:value-of select="$class" /></clazz>-->
<!--                                <typering><xsl:value-of select="substring($class,1,35)" /></typering>-->
<!--                                <naam>-->
<!--                                    <xsl:choose>-->
<!--                                        <xsl:when test="cat:rechtspersoon/cat:volledigeNaam"><xsl:value-of select="cat:rechtspersoon/cat:volledigeNaam" /></xsl:when>-->
<!--                                        <xsl:otherwise><xsl:value-of select="cat:naam" /></xsl:otherwise>-->
<!--                                    </xsl:choose>-->
<!--                                </naam>-->
<!--                                <xsl:for-each select="cat:manifesteertZichAls/cat:onderneming/cat:kvkNummer">-->
<!--                                    <kvk_nummer><xsl:value-of select="."/></kvk_nummer>-->
<!--                                </xsl:for-each>-->

<!--                                <xsl:call-template name="subject" />-->
<!--                            </subject>-->

<!--                            <prs>-->
<!--                                <sc_identif><xsl:value-of select="$key" /></sc_identif>-->
<!--                                <clazz><xsl:value-of select="$class" /></clazz>-->
<!--                            </prs>-->

<!--                            <niet_nat_prs>-->
<!--                                <sc_identif><xsl:value-of select="$key" /></sc_identif>-->
<!--                                <clazz><xsl:value-of select="$class" /></clazz>-->
<!--                                <naam>-->
<!--                                    <xsl:choose>-->
<!--                                        <xsl:when test="cat:rechtspersoon/cat:volledigeNaam"><xsl:value-of select="cat:rechtspersoon/cat:volledigeNaam" /></xsl:when>-->
<!--                                        <xsl:otherwise><xsl:value-of select="cat:naam" /></xsl:otherwise>-->
<!--                                    </xsl:choose>-->
<!--                                </naam>-->
<!--                                <verkorte_naam><xsl:value-of select="substring(cat:naam,1,45)" /></verkorte_naam>-->
<!--                                <xsl:call-template name="registratie-datum">-->
<!--                                    <xsl:with-param name="einde" select="'datum_beeindiging'" />-->
<!--                                </xsl:call-template>-->
<!--                            </niet_nat_prs>-->

<!--                            <ingeschr_niet_nat_prs>-->
<!--                                <sc_identif><xsl:value-of select="$key" /></sc_identif>-->
<!--                                <typering><xsl:value-of select="substring($class,1,35)" /></typering>-->
<!--                                <fk_8aoa_identif>-->
<!--                                    <xsl:choose>-->
<!--                                        <xsl:when test="cat:rechtspersoon/cat:bezoekLocatie/cat:adres/cat:binnenlandsAdres/cat:bagId/cat:identificatieAdresseerbaarObject">-->
<!--                                            <xsl:value-of select="cat:rechtspersoon/cat:bezoekLocatie/cat:adres/cat:binnenlandsAdres/cat:bagId/cat:identificatieAdresseerbaarObject" />-->
<!--                                        </xsl:when>-->
<!--                                        <xsl:when test="cat:rechtspersoon/cat:bezoekLocatiePersoon/cat:adres/cat:binnenlandsAdres/cat:bagId/cat:identificatieAdresseerbaarObject">-->
<!--                                            <xsl:value-of select="cat:rechtspersoon/cat:bezoekLocatiePersoon/cat:adres/cat:binnenlandsAdres/cat:bagId/cat:identificatieAdresseerbaarObject" />-->
<!--                                        </xsl:when>-->
<!--                                        <xsl:otherwise>-->
<!--                                            <xsl:value-of select="cat:bezoekLocatie/cat:adres/cat:binnenlandsAdres/cat:bagId/cat:identificatieAdresseerbaarObject" />-->
<!--                                        </xsl:otherwise>-->
<!--                                    </xsl:choose>-->
<!--                                </fk_8aoa_identif>-->
<!--                                <rechtsvorm>-->
<!--                                    <xsl:if test="cat:rechtspersoon/cat:persoonRechtsvorm"><xsl:value-of select="cat:rechtspersoon/cat:persoonRechtsvorm"/></xsl:if>-->
<!--                                    <xsl:if test="cat:persoonRechtsvorm"><xsl:value-of select="cat:persoonRechtsvorm"/></xsl:if>-->
<!--                                </rechtsvorm>-->
<!--                                <publiekrechtelijke_rechtsvorm><xsl:value-of select="cat:publiekrechtelijkeRechtsvorm/cat:omschrijving"/></publiekrechtelijke_rechtsvorm>-->
<!--                                <rsin><xsl:value-of select="cat:rechtspersoon/cat:rsin"/></rsin>-->
<!--                            </ingeschr_niet_nat_prs>-->

                        </xsl:when>

                        <xsl:when test="local-name(*[1]) = 'buitenlandseVennootschap'">
                            <xsl:apply-templates select="cat:buitenlandseVennootschap"/>
                        </xsl:when>

                        <xsl:when test="local-name(*[1]) = 'samenwerkingsverband'">
                            <xsl:apply-templates select="cat:samenwerkingsverband"/>
                        </xsl:when>

                        <xsl:otherwise>
                            <xsl:comment>
                                <xsl:text>namespace probleem voor node met naam: </xsl:text><xsl:value-of select="name(*[1])"/>
                                <xsl:text> - local-name: </xsl:text><xsl:value-of select="local-name(*[1])"/>
                                <xsl:text> - geen subject records aangemaakt</xsl:text>
                            </xsl:comment>
                        </xsl:otherwise>
                    </xsl:choose>

                    <!--functionaris-->
                    <functionaris>
                        <!--character varying(255) NOT NULL, - [FK] AN32, FK naar prs.sc_identif (is FK naar superclass SUBJECT)-->
                        <fk_sc_lh_pes_sc_identif><xsl:value-of select="$key" /></fk_sc_lh_pes_sc_identif>
                        <!--character varying(255) NOT NULL, - [FK] AN32, FK naar prs.sc_identif (is FK naar superclass SUBJECT)-->
                        <fk_sc_rh_pes_sc_identif><xsl:value-of select="$objectRef" /></fk_sc_rh_pes_sc_identif>

                        <!-- numeric(18,0), - N18 - Beperking bevoegdheid  in euros-->
                        <beperking_bev_in_euros><xsl:value-of select="../cat:bevoegdheid/cat:beperkingInEuros/cat:waarde"/></beperking_bev_in_euros>
                        <!-- numeric(18,0), - Groepsattribuut Beperkte volmacht FUNCTIONARIS.Beperking in geld - Beperking in geld-->
                        <!--<bv_beperking_in_geld></bv_beperking_in_geld>-->
                        <!--character vary ing(2000), - Groepsattribuut Beperkte volmacht FUNCTIONARIS.Omschrijving overige beperkingen - Omschrijving overige beperkingen-->
                        <!--<bv_omschr_ovrg_beperkingen></bv_omschr_ovrg_beperkingen>-->
                        <bv_ovrg_volmacht><xsl:value-of select="../cat:volmacht/cat:beperkteVolmacht/cat:heeftOverigeVolmacht/cat:omschrijving"/></bv_ovrg_volmacht>

                        <!-- character varying(35), - Groepsattribuut Beperkte volmacht FUNCTIONARIS.Soort handeling - Soort handeling-->
                        <bv_soort_handeling><xsl:value-of select="local-name(../cat:volmacht/cat:beperkteVolmacht/*[1])"/></bv_soort_handeling>
                        <bev_met_andere_prsn><xsl:value-of select="../cat:bevoegdheid/cat:isBevoegdMetAnderePersonen/cat:omschrijving" /></bev_met_andere_prsn>
                        <!-- TODO er zijn alleen registratie datums in de berichten -->
                        <!--datum_toetr character varying(19), - OnvolledigeDatum - Datum toetreding-->
                        <!--<datum_toetr></datum_toetr>-->
                        <!--datum_uittreding character varying(19), - OnvolledigeDatum - Datum uittreding-->
                        <!--<datum_uittreding></datum_uittreding>-->

                        <functie><xsl:value-of select="../cat:functie/cat:omschrijving" /></functie>
                        <functionaristypering><xsl:value-of select="../cat:functietitel/cat:titel" /></functionaristypering>
                        <indic_statutair_volmacht><xsl:value-of select="../cat:volmacht/cat:isStatutair/cat:omschrijving"/></indic_statutair_volmacht>
                        <!-- character varying(3), - AN3 - Overige beperking bevoegdheid, alleen optioneel gevuld indien Aansprakelijke-->
                        <xsl:if test="../cat:functie/cat:omschrijving = 'Aansprakelijke'">
                            <ovrg_beperking_bev><xsl:value-of select="../cat:bevoegdheid/cat:overigeBeperking/cat:omschrijving"/></ovrg_beperking_bev>
                        </xsl:if>
                        <xsl:if test="../cat:functie/cat:omschrijving = 'Vennoot'">
                            <ovrg_beperking_bev><xsl:value-of select="../cat:bevoegdheid/cat:overigeBeperking/cat:omschrijving"/></ovrg_beperking_bev>
                        </xsl:if>


                        <soort_bev><xsl:value-of select="../cat:bevoegdheid/cat:soort/cat:omschrijving" /></soort_bev>
                        <!-- character varying(1), - AN1 - Volledig Beperkt volmacht, waarde "B" of "V" indien Gevolmachtigde-->
                        <xsl:if test="../cat:functie/cat:omschrijving = 'Gevolmachtigde'">
                            <xsl:if test="../cat:volmacht/cat:beperkteVolmacht">
                                <volledig_beperkt_volmacht><xsl:value-of select="'B'"/></volledig_beperkt_volmacht>
                            </xsl:if>
                            <xsl:if test="not(../cat:volmacht/cat:beperkteVolmacht)">
                                <volledig_beperkt_volmacht><xsl:value-of select="'V'"/></volledig_beperkt_volmacht>
                            </xsl:if>
                        </xsl:if>
                    </functionaris>
                </xsl:if>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="cat:maatschappelijkeActiviteit">
        <xsl:for-each select="cat:manifesteertZichAls/cat:onderneming">
            <xsl:comment>Maatschappelijke activiteit manifesteert zich als onderneming, met niet-hoofdvestigingen</xsl:comment>
            <xsl:apply-templates select="."/>
        </xsl:for-each>

        <maatschapp_activiteit column-dat-beg-geldh="datum_aanvang" column-datum-einde-geldh="datum_einde_geldig">
            <kvk_nummer><xsl:value-of select="cat:kvkNummer"/></kvk_nummer>

            <xsl:call-template name="registratie-datum"/>

            <xsl:for-each select="cat:manifesteertZichAls/cat:onderneming/cat:kvkNummer">
                <fk_3ond_kvk_nummer><xsl:value-of select="."/></fk_3ond_kvk_nummer>
            </xsl:for-each>
            <fk_4pes_sc_identif>
                <xsl:apply-templates select="cat:heeftAlsEigenaar/cat:rechtspersoon" mode="object_ref"/>
            </fk_4pes_sc_identif>
        </maatschapp_activiteit>

        <!-- TODO: wordtUitgeoefendIn nietCommercieleVestiging -->

        <xsl:variable name="key"><xsl:apply-templates select="." mode="object_ref"/></xsl:variable>
        <xsl:variable name="class">INGESCHREVEN NIET-NATUURLIJK PERSOON</xsl:variable>

        <subject>
            <identif><xsl:value-of select="$key"/></identif>
            <clazz><xsl:value-of select="$class"/></clazz>
            <typering><xsl:value-of select="substring($class,1,35)"/></typering>
            <naam><xsl:value-of select="cat:naam"/></naam>

            <xsl:if test="cat:manifesteertZichAls/cat:onderneming/cat:kvkNummer">
                <xsl:for-each select="cat:manifesteertZichAls/cat:onderneming/cat:kvkNummer">
                    <kvk_nummer><xsl:value-of select="."/></kvk_nummer>
                </xsl:for-each>
            </xsl:if>
            <xsl:if test="not(cat:manifesteertZichAls/cat:onderneming/cat:kvkNummer)">
                <!-- bijv. een vereniging -->
                <kvk_nummer><xsl:value-of select="cat:kvkNummer"/></kvk_nummer>
            </xsl:if>
            <xsl:call-template name="subject"/>
        </subject>

        <prs>
            <sc_identif><xsl:value-of select="$key"/></sc_identif>
            <clazz><xsl:value-of select="$class"/></clazz>
        </prs>

        <niet_nat_prs>
            <sc_identif><xsl:value-of select="$key"/></sc_identif>
            <clazz><xsl:value-of select="$class"/></clazz>
            <naam><xsl:value-of select="cat:naam"/></naam>
            <verkorte_naam><xsl:value-of select="substring(cat:naam,1,45)"/></verkorte_naam>
            <xsl:call-template name="registratie-datum">
                <xsl:with-param name="einde" select="'datum_beeindiging'"/>
            </xsl:call-template>
        </niet_nat_prs>

        <ingeschr_niet_nat_prs>
            <sc_identif><xsl:value-of select="$key"/></sc_identif>
            <typering><xsl:value-of select="substring($class,1,35)"/></typering>

            <!-- Afgesplitst -->
            <!-- Is dit correct, of moeten deze velden leeg geladen worden en heeftAlsEigenaar als apart object worden
                vastgelegd en heeftAlsEignaar relatie via andere koppeling worden vastgelegd? -->
            <!--xsl:for-each select="cat:heeftAlsEigenaar/*">
                <rsin><xsl:value-of select="cat:rsin"/></rsin>
                <rechtsvorm><xsl:value-of select="cat:persoonRechtsvorm"/></rechtsvorm>
                <publiekrechtelijke_rechtsvorm><xsl:value-of select="cat:publiekrechtelijkeRechtsvorm/cat:omschrijving"/></publiekrechtelijke_rechtsvorm>
                <statutaire_zetel><xsl:value-of select="cat:statutaireZetel"/></statutaire_zetel>

                <!- - TODO: rechtstoestand - ->
            </xsl:for-each-->
            <fk_8aoa_identif><xsl:value-of select="cat:bezoekLocatie/cat:adres/cat:binnenlandsAdres/cat:bagId/cat:identificatieAdresseerbaarObject"/></fk_8aoa_identif>
            <!-- TODO heeftAlsEigenaar/rechtspersoon/heeft -->
        </ingeschr_niet_nat_prs>

        <!-- Afgesplitst -->
        <!--xsl:for-each select="cat:wordtGeleidVanuit">
            <xsl:comment>Hoofdvestiging (maatschappelijkeActiviteit wordtGeleidVanuit)</xsl:comment>
            <xsl:apply-templates select="."/>
        </xsl:for-each-->
    </xsl:template>

    <xsl:template match="cat:onderneming">
        <xsl:if test="not(cat:kvkNummer)">
            <xsl:message terminate="yes">
                Onderneming zonder kvk nummer!
            </xsl:message>
        </xsl:if>

        <ondrnmng column-dat-beg-geldh="datum_aanvang" column-datum-einde-geldh="datum_einde">
            <kvk_nummer><xsl:value-of select="cat:kvkNummer"/></kvk_nummer>
            <xsl:call-template name="registratie-datum">
                <xsl:with-param name="einde" select="'datum_einde'"/>
            </xsl:call-template>

            <!-- Overgedragen aan onderneming zonder kvknummer negeren -->
            <xsl:for-each select="cat:isOvergedragenAan/cat:onderneming/cat:kvkNummer">
                <!-- TODO: constraint droppen of deze onderneming eerst inserten? Testcase nodig om te bepalen of volledige onderneming meegeleverd wordt -->
                <!-- Niet meer in RSGB 3.0. Voorlopig negeren -->
                <!--fk_1ond_kvk_nummer><xsl:value-of select="."/></fk_1ond_kvk_nummer-->
            </xsl:for-each>
        </ondrnmng>

        <!-- Geen hoofdvestiging transformeren hier, omdat deze bij de levering als completer bericht los is geleverd -->
        <xsl:apply-templates select="cat:wordtUitgeoefendIn[cat:vestigingsnummer != $hoofdvestiging]"/>
    </xsl:template>

    <xsl:template match="cat:nietCommercieleVestiging[cat:vestigingsnummer] | cat:commercieleVestiging[cat:vestigingsnummer]">
        <xsl:variable name="key"><xsl:apply-templates select="." mode="object_ref"/></xsl:variable>

        <xsl:variable name="naam">
            <xsl:choose>
                <xsl:when test="cat:eersteHandelsnaam"> <!-- commercieleVestiging -->
                    <!-- NHR max lengte 625 naar RSGB 500 -->
                    <xsl:value-of select="substring(cat:eersteHandelsnaam,1,500)"/>
                </xsl:when>
                <xsl:when test="cat:naam"> <!-- nietCommercieleVestiging -->
                    <!-- max lengte NHR en RSGB beide 500 -->
                    <xsl:value-of select="cat:naam"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="substring(cat:handeltOnder[position()=1]/cat:handelsnaam/cat:naam,1,45)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <subject>
            <identif><xsl:value-of select="$key"/></identif>
            <clazz>VESTIGING</clazz>
            <typering>VESTIGING</typering>
            <naam><xsl:value-of select="$naam"/></naam>

            <xsl:call-template name="subject"/>
        </subject>

        <xsl:for-each select="cat:activiteiten/cat:hoofdSbiActiviteit | cat:activiteiten/cat:sbiActiviteit">
            <comfort search-table="sbi_activiteit" search-column="sbi_code" search-value="{cat:sbiCode/cat:code}" snapshot-date="{$peilmoment-dateTime}">
                <sbi_activiteit>
                    <sbi_code><xsl:value-of select="cat:sbiCode/cat:code"/></sbi_code>
                    <omschr><xsl:value-of select="cat:sbiCode/cat:omschrijving"/></omschr>
                </sbi_activiteit>
            </comfort>
        </xsl:for-each>

        <vestg>
            <sc_identif><xsl:value-of select="$key"/></sc_identif>
            <xsl:call-template name="registratie-datum">
                <xsl:with-param name="einde" select="'datum_beeindiging'"/>
            </xsl:call-template>

            <xsl:for-each select="cat:wordtUitgeoefendDoor/cat:onderneming">
                <!-- [FK] N8, FK naar ondrnmng.kvk_nummer: "betreft uitoefening van activiteiten door" -->
                <fk_15ond_kvk_nummer><xsl:value-of select="cat:kvkNummer"/></fk_15ond_kvk_nummer>
            </xsl:for-each>

            <xsl:for-each select="cat:wordtUitgeoefendDoor/cat:nietCommercieleVestiging">
                <!-- [FK] N8, FK naar maatschapp_activiteit.kvk_nummer: "betreft uitoefening van activiteiten door" -->
                <fk_17mac_kvk_nummer><xsl:value-of select="cat:kvkNummer"/></fk_17mac_kvk_nummer>
            </xsl:for-each>

            <xsl:comment>
                <xsl:text>(hoofd) vestigings nummer: </xsl:text><xsl:value-of select="cat:wordtGeleidVanuit/cat:vestigingsnummer"/>
                <xsl:text>(neven) vestigings nummer: </xsl:text><xsl:value-of select="cat:vestigingsnummer"/>
            </xsl:comment>
            <xsl:choose>
                <xsl:when test="not(cat:wordtGeleidVanuit/cat:vestigingsnummer)">
                    <!-- [FK] N8, FK naar maatschapp_activiteit.kvk_nummer: "is hoofdvestiging van" -->
                    <fk_19mac_kvk_nummer>
                        <!--
                        <xsl:value-of select="cat:wordtUitgeoefendDoor/cat:onderneming/cat:kvkNummer"/>
                        of
                        <xsl:value-of select="cat:wordtUitgeoefendDoor/cat:nietCommercieleVestiging/cat:kvkNummer"/>
                        -->
                        <xsl:value-of select="cat:wordtUitgeoefendDoor/*/cat:kvkNummer"/>
                    </fk_19mac_kvk_nummer>
                    <hoofdvestiging>
                        <xsl:text>Ja</xsl:text>
                    </hoofdvestiging>
                </xsl:when>
                <xsl:otherwise>
                    <hoofdvestiging><xsl:text>Nee</xsl:text></hoofdvestiging>
                </xsl:otherwise>
            </xsl:choose>

            <typering>
                <xsl:choose>
                    <xsl:when test="local-name(.) = 'nietCommercieleVestiging'">Niet-commerciele vestiging</xsl:when>
                    <xsl:otherwise>Commerciele vestiging</xsl:otherwise>
                </xsl:choose>
            </typering>

            <datum_voortzetting><xsl:value-of select="cat:datumVoortzetting"/></datum_voortzetting>
            <xsl:for-each select="cat:verkorteNaam"> <!-- nietCommercieleVestiging -->
                <!-- max lengte NHR en RSGB beide 45 -->
                <verkorte_naam><xsl:value-of select="."/></verkorte_naam>
            </xsl:for-each>
            <xsl:choose>
                <xsl:when test="cat:voltijdWerkzamePersonen and cat:deeltijdWerkzamePersonen">
                    <fulltime_werkzame_mannen><xsl:value-of select="cat:voltijdWerkzamePersonen"/></fulltime_werkzame_mannen>
                    <parttime_werkzame_mannen><xsl:value-of select="cat:deeltijdWerkzamePersonen"/></parttime_werkzame_mannen>
                </xsl:when>
                <xsl:when test="cat:totaalWerkzamePersonen">
                    <fulltime_werkzame_mannen><xsl:value-of select="cat:totaalWerkzamePersonen"/></fulltime_werkzame_mannen>
                </xsl:when>
            </xsl:choose>

            <toevoeging_adres><xsl:value-of select="cat:bezoekLocatie/cat:toevoegingAdres"/></toevoeging_adres>

            <activiteit_omschr><xsl:value-of select="cat:activiteiten/cat:omschrijving"/></activiteit_omschr>

            <fk_16tgo_identif><xsl:value-of select="cat:bezoekLocatie/cat:adres/cat:binnenlandsAdres/cat:bagId/cat:identificatieNummeraanduiding"/></fk_16tgo_identif>
            <fk_20aoa_identif><xsl:value-of select="cat:bezoekLocatie/cat:adres/cat:binnenlandsAdres/cat:bagId/cat:identificatieAdresseerbaarObject"/></fk_20aoa_identif>
            <!-- vullen als hoofd activiteit -->
            <xsl:if test="cat:activiteiten/cat:sbiActiviteit/cat:isHoofdactiviteit/cat:code = 'J'">
                <fk_sa_sbi_activiteit_sbi_code><xsl:value-of select="cat:activiteiten/cat:sbiActiviteit/cat:sbiCode/cat:code"/></fk_sa_sbi_activiteit_sbi_code>
                <sa_indic_hoofdactiviteit><xsl:value-of select="cat:activiteiten/cat:sbiActiviteit/cat:isHoofdactiviteit/cat:omschrijving"/></sa_indic_hoofdactiviteit>
            </xsl:if>
        </vestg>

        <!--xsl:comment> Gaat uit van fixed issue #139, #140</xsl:comment-->
        <xsl:for-each select="cat:activiteiten/cat:hoofdSbiActiviteit | cat:activiteiten/cat:sbiActiviteit">
            <!--xsl:if test="position() = 1">
                <vestg_activiteit delete-rows="true">
                    <fk_vestg_nummer><xsl:value-of select="$key"/></fk_vestg_nummer>
                </vestg_activiteit>
            </xsl:if-->
            <vestg_activiteit>
                <fk_vestg_nummer><xsl:value-of select="$key"/></fk_vestg_nummer>
                <fk_sbi_activiteit_code><xsl:value-of select="cat:sbiCode/cat:code"/></fk_sbi_activiteit_code>
                <indicatie_hoofdactiviteit>
                    <xsl:if test="cat:isHoofdactiviteit/cat:code = 'J'">1</xsl:if>
                    <xsl:if test="cat:isHoofdactiviteit/cat:code != 'J'">0</xsl:if>
                </indicatie_hoofdactiviteit>
            </vestg_activiteit>
        </xsl:for-each>

        <xsl:for-each select="cat:handeltOnder/cat:handelsnaam/cat:naam">
            <!--xsl:if test="position() = 1">
                <xsl:comment> Gaat uit van fixed issue #139</xsl:comment>
                <vestg_naam delete-rows="true">
                    <fk_ves_sc_identif><xsl:value-of select="$key"/></fk_ves_sc_identif>
                </vestg_naam>
            </xsl:if-->
            <vestg_naam>
                <naam><xsl:value-of select="."/></naam> <!-- NHR max 625 lang, BRMO 500 -->
                <fk_ves_sc_identif><xsl:value-of select="$key"/></fk_ves_sc_identif>
            </vestg_naam>
        </xsl:for-each>

    </xsl:template>

    <!-- Werkt voor elementen met cat:bezoekLocatie -->
    <xsl:template name="subject">
        <xsl:comment>gegevens uit template subject</xsl:comment>
        <!-- Lengte mismatch: NHR 500, RSGB 257 -->
        <adres_binnenland>
            <xsl:if test="cat:bezoekLocatie/cat:adres/cat:binnenlandsAdres">
                <xsl:value-of select="normalize-space(cat:bezoekLocatie/cat:volledigAdres)"/>
            </xsl:if>
            <xsl:if test="cat:bezoekLocatiePersoon/cat:adres/cat:binnenlandsAdres">
                <xsl:value-of select="normalize-space(cat:bezoekLocatiePersoon/cat:volledigAdres)"/>
            </xsl:if>
            <xsl:if test="cat:rechtspersoon/cat:bezoekLocatiePersoon/cat:adres/cat:binnenlandsAdres">
                <xsl:value-of select="normalize-space(cat:rechtspersoon/cat:bezoekLocatiePersoon/cat:volledigAdres)"/>
            </xsl:if>
        </adres_binnenland>
        <adres_buitenland>
            <xsl:if test="cat:bezoekLocatie/cat:adres/cat:buitenlandsAdres">
                <xsl:value-of select="normalize-space(cat:bezoekLocatie/cat:volledigAdres)"/>
            </xsl:if>
            <xsl:if test="cat:bezoekLocatiePersoon/cat:adres/cat:buitenlandsAdres">
                <xsl:value-of select="normalize-space(cat:bezoekLocatiePersoon/cat:volledigAdres)"/>
            </xsl:if>
            <xsl:if test="cat:rechtspersoon/cat:bezoekLocatiePersoon/cat:adres/cat:buitenlandsAdres">
                <xsl:value-of select="normalize-space(cat:rechtspersoon/cat:bezoekLocatiePersoon/cat:volledigAdres)"/>
            </xsl:if>
            <xsl:if test="cat:buitenlandseVennootschap/cat:bezoekLocatiePersoon/cat:adres/cat:buitenlandsAdres">
                <xsl:value-of select="normalize-space(cat:buitenlandseVennootschap/cat:bezoekLocatiePersoon/cat:adres/cat:buitenlandsAdres)"/>
            </xsl:if>
        </adres_buitenland>

        <!-- <fk_13wpl_identif /> [FK] AN4, FK naar wnplts.identif: "heeft als correspondentieadres" -->
        <!-- <fk_14aoa_identif /> [FK] AN16, FK naar addresseerb_obj_aand.identif: "heeft als factuuradres" -->
        <fk_15aoa_identif><xsl:value-of select="cat:postLocatie/cat:adres/cat:binnenlandsAdres/cat:bagId/cat:identificatieAdresseerbaarObject"/></fk_15aoa_identif>

        <xsl:for-each select="cat:postLocatie/cat:adres/cat:binnenlandsAdres[cat:postbusnummer]">
            <pa_postadres_postcode><xsl:value-of select="cat:postcode/cat:cijfercombinatie"/><xsl:value-of select="cat:postcode/cat:lettercombinatie"/></pa_postadres_postcode>
            <pa_postadrestype>P</pa_postadrestype>
            <pa_postbus__of_antwoordnummer><xsl:value-of select="cat:postbusnummer"/></pa_postbus__of_antwoordnummer>
        </xsl:for-each>
        <!-- <fk_pa_4_wpl_identif /> [FK] AN4, FK naar wnplts.identif: "Groepsattribuut Postadres SUBJECT.woonplaats" -->

        <emailadres><xsl:value-of select="cat:communicatiegegevens/cat:emailAdres"/></emailadres>
        <xsl:for-each select="cat:communicatiegegevens/cat:communicatienummer[cat:soort/cat:omschrijving = 'Fax']/cat:nummer">
            <xsl:if test="position() = 1">
                <fax_nummer><xsl:value-of select="."/></fax_nummer>
            </xsl:if>
        </xsl:for-each>
        <xsl:for-each select="cat:communicatiegegevens/cat:communicatienummer[cat:soort/cat:omschrijving = 'Telefoon']/cat:nummer">
            <xsl:if test="position() = 1">
                <telefoonnummer><xsl:value-of select="."/></telefoonnummer>
            </xsl:if>
        </xsl:for-each>

        <website_url><xsl:value-of select="cat:communicatiegegevens/cat:domeinNaam"/></website_url>
    </xsl:template>

    <xsl:template match="cat:rechtspersoon | cat:samenwerkingsverband">
        <xsl:variable name="key"><xsl:apply-templates select="." mode="object_ref"/></xsl:variable>
        <xsl:variable name="class">INGESCHREVEN NIET-NATUURLIJK PERSOON</xsl:variable>
        <xsl:comment>template voor cat:rechtspersoon | cat:samenwerkingsverband</xsl:comment>
        <subject>
            <identif><xsl:value-of select="$key"/></identif>
            <clazz><xsl:value-of select="$class"/></clazz>
            <typering><xsl:value-of select="substring($class,1,35)"/></typering>
            <naam><xsl:value-of select="cat:volledigeNaam"/></naam>
            <xsl:for-each select="cat:manifesteertZichAls/cat:onderneming/cat:kvkNummer">
                <kvk_nummer><xsl:value-of select="." /></kvk_nummer>
            </xsl:for-each>

            <xsl:call-template name="subject"/>
        </subject>

        <prs>
            <sc_identif><xsl:value-of select="$key"/></sc_identif>
            <clazz><xsl:value-of select="$class"/></clazz>
        </prs>

        <niet_nat_prs>
            <sc_identif><xsl:value-of select="$key"/></sc_identif>
            <clazz><xsl:value-of select="$class"/></clazz>
            <naam><xsl:value-of select="cat:volledigeNaam"/></naam>
            <xsl:for-each select="cat:verkorteNaam">
                <verkorte_naam><xsl:value-of select="."/></verkorte_naam>
            </xsl:for-each>
            <xsl:call-template name="registratie-datum">
                <xsl:with-param name="einde" select="'datum_beeindiging'"/>
            </xsl:call-template>
        </niet_nat_prs>

        <ingeschr_niet_nat_prs>
            <sc_identif><xsl:value-of select="$key"/></sc_identif>
            <typering><xsl:value-of select="substring($class,1,35)"/></typering>

            <fk_8aoa_identif><xsl:value-of select="cat:bezoekLocatie/cat:adres/cat:binnenlandsAdres/cat:bagId/cat:identificatieAdresseerbaarObject"/></fk_8aoa_identif>

            <rsin><xsl:value-of select="cat:rsin"/></rsin>
            <rechtsvorm><xsl:value-of select="cat:persoonRechtsvorm"/></rechtsvorm>
            <publiekrechtelijke_rechtsvorm><xsl:value-of select="cat:publiekrechtelijkeRechtsvorm/cat:omschrijving"/></publiekrechtelijke_rechtsvorm>
            <statutaire_zetel><xsl:value-of select="cat:statutaireZetel"/></statutaire_zetel>

            <!-- TODO heeft (comfortdata) -->
        </ingeschr_niet_nat_prs>

        <xsl:for-each select="cat:heeft">
            <xsl:call-template name="heeft" />
        </xsl:for-each>

        <xsl:for-each select="cat:activiteiten/cat:hoofdSbiActiviteit | cat:activiteiten/cat:sbiActiviteit">
            <comfort search-table="sbi_activiteit" search-column="sbi_code" search-value="{cat:sbiCode/cat:code}" snapshot-date="{$peilmoment-dateTime}">
                <sbi_activiteit>
                    <sbi_code><xsl:value-of select="cat:sbiCode/cat:code"/></sbi_code>
                    <omschr><xsl:value-of select="cat:sbiCode/cat:omschrijving"/></omschr>
                </sbi_activiteit>
            </comfort>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="cat:natuurlijkPersoon">
        <xsl:call-template name="natPersoon"/>
    </xsl:template>

    <xsl:template match="cat:buitenlandseVennootschap">
        <xsl:variable name="key"><xsl:apply-templates select="." mode="object_ref"/></xsl:variable>
        <xsl:variable name="class">ANDER BUITENLANDS NIET-NATUURLIJK PERSOON</xsl:variable>
        <subject>
            <identif><xsl:value-of select="$key"/></identif>
            <clazz><xsl:value-of select="$class"/></clazz>
            <typering><xsl:value-of select="substring($class,1,35)"/></typering>
            <naam><xsl:value-of select="cat:volledigeNaam"/></naam>

            <xsl:call-template name="subject"/>
        </subject>
        <prs>
            <sc_identif><xsl:value-of select="$key"/></sc_identif>
            <clazz><xsl:value-of select="$class"/></clazz>
        </prs>
        <niet_nat_prs>
            <sc_identif><xsl:value-of select="$key"/></sc_identif>
            <clazz><xsl:value-of select="$class"/></clazz>
            <naam><xsl:value-of select="cat:volledigeNaam"/></naam>
            <xsl:for-each select="cat:verkorteNaam">
                <!-- max lengte NHR en RSGB beide 45 -->
                <verkorte_naam><xsl:value-of select="."/></verkorte_naam>
            </xsl:for-each>
            <xsl:call-template name="registratie-datum">
                <xsl:with-param name="einde" select="'datum_beeindiging'"/>
            </xsl:call-template>
        </niet_nat_prs>
        <ander_btnlnds_niet_nat_prs>
            <sc_identif><xsl:value-of select="$key"/></sc_identif>
        </ander_btnlnds_niet_nat_prs>
    </xsl:template>

    <xsl:template match="*">
        <xsl:comment><xsl:text>Catch-all template voor onbekend element: </xsl:text><xsl:value-of select="local-name()"/></xsl:comment>
    </xsl:template>
</xsl:stylesheet>
