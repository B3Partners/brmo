<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
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
		<!-- parameters van het bericht -->
		<xsl:param name="objectRef" select="'NL.KAD.OnroerendeZaak:onbekend'"/>
		<xsl:param name="datum" select="'datum-onbekend'"/>
		<xsl:param name="volgordeNummer" select="'volgordeNummer-onbekend'"/>
		<xsl:param name="soort" select="'soort-onbekend'"/>
    <xsl:variable name="kad_oz_id" select="/snp:KadastraalObjectSnapshot/ko:Perceel/ko:identificatie/nen:lokaalId | /snp:KadastraalObjectSnapshot/ko:Appartementsrecht/ko:identificatie/nen:lokaalId"/>
    <xsl:variable name="toestandsdatum" select="/snp:KadastraalObjectSnapshot/snp:toestandsdatum"/>
    <xsl:variable name="persoonId"/>
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
            <data>
								<xsl:if test="not(/snp:KadastraalObjectSnapshot)">
									<!-- 	
										als bericht geen KadastraalObjectSnapshot bevat dan
										moet dit object verwijderd worden. Alleen kad_onrrnd_zk
										verwijderen en vertrouwen op delete cascade.
									-->
									<delete>
										<kad_onrrnd_zk>
											<kad_identif><xsl:value-of select="substring($objectRef, 23)"/></kad_identif>
										</kad_onrrnd_zk>
									</delete>
								</xsl:if>
                <xsl:for-each select="/snp:KadastraalObjectSnapshot/pers:*">
                    <xsl:apply-templates select="."/>
                </xsl:for-each>
                <xsl:for-each select="/snp:KadastraalObjectSnapshot/nhr:*">
                    <xsl:apply-templates select="."/>
                </xsl:for-each>

                <xsl:for-each select="/snp:KadastraalObjectSnapshot/gba:*">
                    <xsl:apply-templates select="."/>
                </xsl:for-each>

                <xsl:apply-templates select="/snp:KadastraalObjectSnapshot/ko:Perceel"/>
                <xsl:apply-templates select="/snp:KadastraalObjectSnapshot/ko:Appartementsrecht"/>

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
					<zak_recht>
						<kadaster_identif><xsl:value-of select="$parent-id"/></kadaster_identif>
						<xsl:call-template name="zakelijk_recht">
							<xsl:with-param name="zr" select="."/>
						</xsl:call-template>
						<!-- "fk_8pes_sc_identif" ("heeft als gerechtigde") kan maar één keer gevuld worden; het kan zijn dat er èn een ontstaanUit
                             èn een betrokkenBij record is. Alleen de "betrokkenBij" VVE wordt in deze kolom ingevuld. Wel wordt er een brondocument
                             voor het ontstaanUit element gemaakt  -->
						<xsl:for-each select="*[local-name() = 'betrokkenBij']//recht:verenigingVanEigenaren">
							<xsl:for-each select="PersoonRef:KADNietNatuurlijkPersoonRef | NhrRechtspersoonRef:RechtspersoonRef"
										  xmlns:NhrRechtspersoonRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-nhr-rechtspersoon-ref/v20120201"
										  xmlns:PersoonRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-persoon-ref/v20120201">
								<xsl:variable name="id" select="substring(@xlink:href,2)"/>
								<fk_8pes_sc_identif>
									<xsl:call-template name="nen_identificatie">
										<xsl:with-param name="id" select="//*[@id = $id]/pers:identificatie"/>
									</xsl:call-template>
								</fk_8pes_sc_identif>
							</xsl:for-each>
						</xsl:for-each>
					</zak_recht>
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
    <xsl:template match="/snp:KadastraalObjectSnapshot/ko:Perceel">
        <xsl:call-template name="kadastraal_onroerende_zaak">
            <xsl:with-param name="oz" select="."/>
        </xsl:call-template>
        <kad_perceel>
			<sc_dat_beg_geldh alleen-archief="true"><xsl:value-of select="$toestandsdatum"/></sc_dat_beg_geldh>
			<sc_kad_identif><xsl:value-of select="ko:identificatie/nen:lokaalId"/></sc_kad_identif>
            <ka_kad_gemeentecode>
                <xsl:value-of select="ko:kadastraleAanduiding/ko:AKRKadastraleGemeenteCode/typ:waarde"/>
            </ka_kad_gemeentecode>
            <ka_sectie>
                <xsl:value-of select="ko:kadastraleAanduiding/ko:sectie"/>
            </ka_sectie>
            <ka_perceelnummer>
                <xsl:value-of select="ko:kadastraleAanduiding/ko:perceelnummer"/>
            </ka_perceelnummer>
            <begrenzing_perceel>
                <xsl:copy-of select="ko:begrenzingPerceel/gml:Surface"/>
            </begrenzing_perceel>
            <grootte_perceel>
                <xsl:value-of select="ko:kadastraleGrootte/ko:waarde"/>
            </grootte_perceel>
            <aand_soort_grootte>
                <xsl:value-of select="ko:soortGrootte/typ:code"/>
            </aand_soort_grootte>
            <omschr_deelperceel>
                <xsl:value-of select="ko:omschrijvingDeelpercelen"/>
            </omschr_deelperceel>
            <plaatscoordinaten_perceel>
                <xsl:copy-of select="ko:plaatscoordinaten/gml:Point"/>
            </plaatscoordinaten_perceel>
        </kad_perceel>
    </xsl:template>
    <xsl:template match="/snp:KadastraalObjectSnapshot/ko:Appartementsrecht">
        <xsl:call-template name="kadastraal_onroerende_zaak">
            <xsl:with-param name="oz" select="."/>
        </xsl:call-template>
        <app_re column-dat-beg-geldh="sc_dat_beg_geldh">
			<sc_dat_beg_geldh alleen-archief="true"><xsl:value-of select="$toestandsdatum"/></sc_dat_beg_geldh>
			<sc_kad_identif><xsl:value-of select="ko:identificatie/nen:lokaalId"/></sc_kad_identif>
			<xsl:for-each select="ko:kadastraleAanduiding">
				<ka_appartementsindex><xsl:value-of select="ko:appartementsrechtVolgnummer"/></ka_appartementsindex>
				<ka_kad_gemeentecode><xsl:value-of select="ko:AKRKadastraleGemeenteCode/typ:waarde"/></ka_kad_gemeentecode>
				<ka_perceelnummer><xsl:value-of select="ko:perceelnummer"/></ka_perceelnummer>
				<ka_sectie><xsl:value-of select="ko:sectie"/></ka_sectie>
			</xsl:for-each>
			<!-- FK1_NIET_NAT_PERSOON wordt niet gevuld (FK naar VVE), zou evt via ZakelijkRecht
		      dat rustOp het appartementsrecht via ontstaanUit//verenigingVanEigenaren kunnen,
			  maar is te complex en is een _PersoonRef, dus niet gegarandeerd een NIET_NAT_PERSOON FK. -->
			<xsl:variable name="app_re_id" select="@id"/>
			<xsl:variable name="vve_id" select="../recht:ZakelijkRecht[substring(recht:rustOp/KadastraalObjectRef:AppartementsrechtRef/@xlink:href,2) = $app_re_id]//recht:verenigingVanEigenaren[1]/PersoonRef:KADNietNatuurlijkPersoonRef/@xlink:href" xmlns:KadastraalObjectRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-kadastraalobject-ref/v20120201" xmlns:PersoonRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-persoon-ref/v20120201"/>
			<xsl:if test="$vve_id">
				<fk_2nnp_sc_identif>
                    <xsl:choose>
                        <xsl:when test="../pers:KADNietNatuurlijkPersoon[@id = substring($vve_id,2)]/pers:identificatie">
                            <xsl:call-template name="nen_identificatie">
                                <xsl:with-param name="id" select="../pers:KADNietNatuurlijkPersoon[@id = substring($vve_id,2)]/pers:identificatie"/>
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="nen_identificatie">
                                <xsl:with-param name="id" select="../nhr:Rechtspersoon[@id = substring($vve_id,2)]/pers:identificatie"/>
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
				</fk_2nnp_sc_identif>
			</xsl:if>
        </app_re>
    </xsl:template>
    <xsl:template name="kadastraal_onroerende_zaak">
        <xsl:param name="oz"/>
        <kad_onrrnd_zk column-dat-beg-geldh="dat_beg_geldh" column-datum-einde-geldh="datum_einde_geldh">
			<dat_beg_geldh><xsl:value-of select="$toestandsdatum"/></dat_beg_geldh>
			<datum_einde_geldh></datum_einde_geldh>
            <kad_identif>
				<xsl:value-of select="$oz/ko:identificatie/nen:lokaalId"/>
            </kad_identif>
            <clazz>
				<xsl:choose>
					<xsl:when test="local-name(.) = 'Perceel'">KADASTRAAL PERCEEL</xsl:when>
					<xsl:otherwise>APPARTEMENTSRECHT</xsl:otherwise>
				</xsl:choose>
            </clazz>
            <ks_bedrag>
                <xsl:value-of select="$oz/ko:koopsom/ko:bedrag/typ:som"/>
            </ks_bedrag>
            <ks_valutasoort>
                <xsl:value-of select="$oz/ko:koopsom/ko:bedrag/typ:valuta/typ:waarde"/>
            </ks_valutasoort>
            <xsl:if test="$oz/ko:koopsom/ko:koopjaar">
                <ks_koopjaar>
                    <xsl:value-of select="$oz/ko:koopsom/ko:koopjaar"/>
                </ks_koopjaar>
            </xsl:if>
            <ks_meer_onroerendgoed>
				<xsl:if test="$oz/ko:koopsom/ko:indicatieMeerObjecten">J</xsl:if>
				<xsl:if test="not($oz/ko:koopsom/ko:indicatieMeerObjecten)">N</xsl:if>
            </ks_meer_onroerendgoed>
            <!-- TODO: van eerste tenaamstelling recht het stukdeel ophalen; van dat stuk de datum nemen
            <ks_transactiedatum></ks_transactiedatum>
             -->
            <cu_aard_bebouwing>
                <xsl:value-of select="$oz/ko:heeftLocatie/ko:LocatieKadastraalObject/ko:cultuurBebouwd/ko:code"/>
            </cu_aard_bebouwing>
            <cu_aard_cultuur_onbebouwd>
                <xsl:value-of select="$oz/ko:aardCultuurOnbebouwd/typ:waarde"/>
            </cu_aard_cultuur_onbebouwd>
            <!-- extra adres, nuttig wanneer geen BAG koppeling -->

            <!-- construeer extra adres wanneer geen BAG koppeling;
                het is mogelijk dat er zowel een bagadres als 1 of meer gba en/of imkad adressen in een bericht voorkomen; in
                dat geval vullen we 'lo_loc__omschr' met het eerste niet-bagadres dat we tegenkomen en wordt daar aangeplakt
                dat er "x meer adressen" zijn, het bagadres wordt dan ook verwerkt natuurlijk.
            -->
            <lo_loc__omschr>
                <xsl:variable name="countadressen" select="count($oz/ko:heeftLocatie/ko:LocatieKadastraalObject/ko:adres[not(bagadres:*)])" />
                <!-- <xsl:for-each select="./ko:heeftLocatie/ko:LocatieKadastraalObject/ko:adres/adres:KADBuitenlandsAdres/..|
                                    ./ko:heeftLocatie/ko:LocatieKadastraalObject/ko:adres/adres:KADBinnenlandsAdres/..|
                                    ./ko:heeftLocatie/ko:LocatieKadastraalObject/ko:adres/adres:PostbusAdres/..|
                                    ./ko:heeftLocatie/ko:LocatieKadastraalObject/ko:adres/gba:BuitenlandsAdres/.."
                > -->
                <xsl:for-each select="$oz/ko:heeftLocatie/ko:LocatieKadastraalObject/ko:adres[not(bagadres:*)]">
                    <xsl:variable name="count" select="position()"/>
                    <xsl:choose>
                        <xsl:when test="($count = $countadressen) and ($countadressen > 1)">
                            <xsl:text>  (</xsl:text>
                            <xsl:value-of select="$count - 1"/>
                            <xsl:text> meer adressen)</xsl:text>
                        </xsl:when>
                        <xsl:when test="$count = 1">
                            <xsl:call-template name="describe-locatie"/>
                        </xsl:when>
                    </xsl:choose>
                </xsl:for-each>
            </lo_loc__omschr>
        </kad_onrrnd_zk>

        <xsl:for-each select="$oz/ko:heeftLocatie/ko:LocatieKadastraalObject/ko:adres">
			<xsl:for-each select="bagadres:Verblijfsobject | bagadres:Ligplaats | bagadres:Standplaats">
				<!-- N - N relatie: BENOEMD OBJECT &quot;staat op of heeft ruimtelijke overlap met&quot; KADASTRALE ONROERENDE ZAAK -->
				<benoemd_obj_kad_onrrnd_zk ignore-duplicates="yes">
					<fk_nn_lh_tgo_identif><xsl:value-of select="bagadres:BAGIdentificatie"/></fk_nn_lh_tgo_identif>
					<fk_nn_rh_koz_kad_identif><xsl:value-of select="$oz/ko:identificatie/nen:lokaalId"/></fk_nn_rh_koz_kad_identif>
				</benoemd_obj_kad_onrrnd_zk>
			</xsl:for-each>
        </xsl:for-each>

        <xsl:for-each select="$oz/ko:ontstaanUitOZ/ko:OnroerendeZaakFiliatie">
			<xsl:variable name="overgegaan-in-href" select="ko:onroerendeZaak/*/@xlink:href"/>

			<xsl:variable name="vorige-zelfde-overgegaan-in-href" select="preceding-sibling::ko:OnroerendeZaakFiliatie[ko:onroerendeZaak/*/@xlink:href = $overgegaan-in-href]"/>
			<xsl:if test="$overgegaan-in-href and not($vorige-zelfde-overgegaan-in-href)">
				<kad_onrrnd_zk_his_rel>
					<fk_sc_lh_koz_kad_identif><xsl:value-of select="$oz/ko:identificatie/nen:lokaalId"/></fk_sc_lh_koz_kad_identif>
					<fk_sc_rh_koz_kad_identif><xsl:value-of select="substring-after($overgegaan-in-href,'NL.KAD.OnroerendeZaak.')"/></fk_sc_rh_koz_kad_identif>
					<aard><xsl:value-of select="ko:aard/typ:waarde"/></aard>
					<overgangsgrootte><xsl:value-of select="ko:overgangsgrootte"/></overgangsgrootte>
				</kad_onrrnd_zk_his_rel>
			</xsl:if>
        </xsl:for-each>
        <!--xsl:for-each select="$oz/ko:overgegaanInOZ/ko:OnroerendeZaakFiliatie/ko:onroerendeZaak/*">
			<KAD_ONR_ZK_KAD_ONR_ZK3>
				<FK1_KAD_ONRRND_ZAAK><xsl:value-of select="$oz/ko:identificatie/nen:lokaalId"/></FK1_KAD_ONRRND_ZAAK>
				<FK2_KAD_ONRRND_ZAAK><xsl:value-of select="substring-after(@xlink:href,'NL.KAD.OnroerendeZaak.')"/></FK2_KAD_ONRRND_ZAAK>
			</KAD_ONR_ZK_KAD_ONR_ZK3>
        </xsl:for-each-->
    </xsl:template>

    <xsl:template name="zakelijk_recht">
        <xsl:param name="zr"/>
        <fk_7koz_kad_identif>
			<xsl:value-of select="$kad_oz_id"/>
		</fk_7koz_kad_identif>
        <fk_3avr_aand>
            <xsl:value-of select="$zr/recht:aard/typ:code"/>
        </fk_3avr_aand>
        <indic_betrokken_in_splitsing>
            <xsl:choose>
                <xsl:when test="$zr/recht/betrokkenBij">Ja</xsl:when>
                <xsl:otherwise>Nee</xsl:otherwise>
            </xsl:choose>
        </indic_betrokken_in_splitsing>
    </xsl:template>

    <xsl:template match="/snp:KadastraalObjectSnapshot/recht:Tenaamstelling">
        <xsl:variable name="zakRechtId" select="substring(recht:van/rechtref:ZakelijkRechtRef/@*[local-name() = 'href'],2)"/>

        <xsl:variable name="parent-id">
			<xsl:call-template name="nen_identificatie">
				<xsl:with-param name="id" select="recht:identificatie"/>
			</xsl:call-template>
        </xsl:variable>
        <zak_recht>
			<kadaster_identif><xsl:value-of select="$parent-id"/></kadaster_identif>
			<xsl:choose>
				<xsl:when test="recht:aandeel">
					<ar_teller>
						<xsl:value-of select="recht:aandeel/recht:teller"/>
					</ar_teller>
					<ar_noemer>
						<xsl:value-of select="recht:aandeel/recht:noemer"/>
					</ar_noemer>
				</xsl:when>
				<xsl:when test="recht:geldtVoor/recht:GezamenlijkAandeel/recht:aandeel">
					<ar_teller>
						<xsl:value-of select="recht:geldtVoor/recht:GezamenlijkAandeel/recht:aandeel/recht:teller"/>
					</ar_teller>
					<ar_noemer>
						<xsl:value-of select="recht:geldtVoor/recht:GezamenlijkAandeel/recht:aandeel/recht:noemer"/>
					</ar_noemer>
				</xsl:when>
			</xsl:choose>
            <xsl:call-template name="zakelijk_recht">
                <xsl:with-param name="zr" select="/snp:KadastraalObjectSnapshot/recht:ZakelijkRecht[@id = $zakRechtId]"/>
            </xsl:call-template>

			<xsl:for-each select="recht:vanPersoon">
				<xsl:for-each select="PersoonRef:KADNatuurlijkPersoonRef | GbaPersoonRef:IngezeteneRef | GbaPersoonRef:NietIngezeteneRef | PersoonRef:KADNietNatuurlijkPersoonRef | NhrRechtspersoonRef:RechtspersoonRef" xmlns:NhrRechtspersoonRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-nhr-rechtspersoon-ref/v20120201"  xmlns:PersoonRef="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-persoon-ref/v20120201">
					<xsl:variable name="id" select="substring(@xlink:href,2)"/>
					<fk_8pes_sc_identif>
						<xsl:call-template name="nen_identificatie">
							<xsl:with-param name="id" select="//*[@id = $id]/pers:identificatie"/>
						</xsl:call-template>
					</fk_8pes_sc_identif>
				</xsl:for-each>
			</xsl:for-each>

        </zak_recht>

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

		<!--check of het een aantekening betreffende een onroerende zaak is of recht (of beide..)-->
		<xsl:if test="recht:betreftAantekeningKadastraalObject">
			<kad_onrrnd_zk_aantek column-dat-beg-geldh="begindatum_aantek_kad_obj">
				<fk_4koz_kad_identif>
					<xsl:value-of select="$kad_oz_id"/>
				</fk_4koz_kad_identif>
				<!-- voor archief moet datum begin geldigheid zijn ingevuld... daarom maar toestandsdatum -->
				<begindatum_aantek_kad_obj><xsl:value-of select="$toestandsdatum"/>
					<!-- "De attribuutsoort komt niet voor in de BRK. De waarde wordt
						  afgeleid van de datum ondertekening van het stuk waarin het
						  feit genoemd is waarop de aantekening is gebaseerd." -->
					<!--xsl:for-each select="recht:isGebaseerdOp">
						TODO
					</xsl:for-each-->
				</begindatum_aantek_kad_obj>
				<kadaster_identif_aantek><xsl:value-of select="$parent-id"/></kadaster_identif_aantek>
				<aard_aantek_kad_obj>
					<xsl:value-of select="recht:aard/typ:waarde"/>
				</aard_aantek_kad_obj>
				<eindd_aantek_kad_obj>
					<xsl:value-of select="recht:einddatum"/>
				</eindd_aantek_kad_obj>
				<beschrijving_aantek_kad_obj>
					<xsl:value-of select="recht:omschrijving"/>
				</beschrijving_aantek_kad_obj>
			</kad_onrrnd_zk_aantek>

			<xsl:for-each select="recht:isGebaseerdOp">
				<xsl:call-template name="is_gebaseerd_op_brondocument">
					<xsl:with-param name="tabel">KAD_ONRRND_ZAAK_AANTEK</xsl:with-param>
					<xsl:with-param name="tabel_identificatie" select="$parent-id"/>
                    <xsl:with-param name="omschrijving" select="concat(local-name(), ' ', local-name(..))"/>
				</xsl:call-template>
			</xsl:for-each>
		</xsl:if>
		<xsl:if test="recht:betreftAantekeningRecht" >
			<zak_recht_aantek>
				<kadaster_identif_aantek_recht><xsl:value-of select="$parent-id"/></kadaster_identif_aantek_recht>
				<fk_5zkr_kadaster_identif>
					<xsl:variable name="tenaamId" select="substring(recht:betreftAantekeningRecht/recht:AantekeningRecht/recht:heeftBetrekkingOp/rechtref:TenaamstellingRef/@*[local-name() = 'href'],2)"/>
					<xsl:call-template name="nen_identificatie">
						<xsl:with-param name="id" select="/snp:KadastraalObjectSnapshot/recht:Tenaamstelling[@id = $tenaamId]/recht:identificatie"/>
					</xsl:call-template>
				</fk_5zkr_kadaster_identif>
				<aard_aantek_recht>
					<xsl:value-of select="recht:aard/typ:waarde"/>
				</aard_aantek_recht>
				<eindd_aantek_recht>
					<xsl:value-of select="recht:einddatum"/>
				</eindd_aantek_recht>
				<beschrijving_aantek_recht>
					<xsl:value-of select="recht:omschrijving"/>
				</beschrijving_aantek_recht>
			</zak_recht_aantek>

			<xsl:for-each select="recht:isGebaseerdOp">
				<xsl:call-template name="is_gebaseerd_op_brondocument">
					<xsl:with-param name="tabel">KAD_ONRRND_ZAAK_AANTEK</xsl:with-param>
					<xsl:with-param name="tabel_identificatie" select="$parent-id"/>
                    <xsl:with-param name="omschrijving" select="concat(local-name(), ' ', local-name(..))"/>
				</xsl:call-template>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>

    <!--Persoon-->
    <xsl:template match="/snp:KadastraalObjectSnapshot/pers:KADNatuurlijkPersoon">

		<!-- comfort data -->
		<xsl:variable name="comfort-search-value">
			<xsl:call-template name="nen_identificatie">
				<xsl:with-param name="id" select="pers:identificatie"/>
			</xsl:call-template>
		</xsl:variable>
		<comfort search-table="subject" search-column="identif" search-value="{$comfort-search-value}" snapshot-date="{$toestandsdatum}">

        <xsl:call-template name="persoon">
            <xsl:with-param name="persoon" select="."/>
            <xsl:with-param name="clazz" select="'ANDER NATUURLIJK PERSOON'"/>
        </xsl:call-template>
        <nat_prs>
            <sc_identif>
                <xsl:call-template name="nen_identificatie">
					<xsl:with-param name="id" select="pers:identificatie"/>
				</xsl:call-template>
            </sc_identif>
            <clazz>ANDER NATUURLIJK PERSOON</clazz>
            <geslachtsaand><xsl:value-of select="pers:geslacht/typ:code"/></geslachtsaand>
            <nm_voornamen><xsl:value-of select="pers:voornamen"/></nm_voornamen>
            <nm_voorvoegsel_geslachtsnaam><xsl:value-of select="pers:voorvoegselsGeslachtsnaam"/></nm_voorvoegsel_geslachtsnaam>
            <nm_geslachtsnaam><xsl:value-of select="pers:geslachtsnaam"/></nm_geslachtsnaam>
        </nat_prs>
        <ander_nat_prs>
            <sc_identif>
                <xsl:call-template name="nen_identificatie">
					<xsl:with-param name="id" select="pers:identificatie"/>
				</xsl:call-template>
            </sc_identif>
            <geboortedatum>
				<xsl:for-each select="pers:geboortedatum"><xsl:call-template name="numeric-date"/></xsl:for-each>
			</geboortedatum>
            <overlijdensdatum>
				<xsl:for-each select="pers:datumOverlijden"><xsl:call-template name="numeric-date"/></xsl:for-each>
			</overlijdensdatum>
        </ander_nat_prs>
		</comfort>
    </xsl:template>

    <xsl:template match="/snp:KadastraalObjectSnapshot/gba:Ingezetene">
		<!-- comfort data -->
		<xsl:variable name="comfort-search-value">
			<xsl:call-template name="nen_identificatie">
				<xsl:with-param name="id" select="pers:identificatie"/>
			</xsl:call-template>
		</xsl:variable>
		<comfort search-table="subject" search-column="identif" search-value="{$comfort-search-value}" snapshot-date="{$toestandsdatum}">

        <xsl:call-template name="persoon">
            <xsl:with-param name="persoon" select="."/>
            <xsl:with-param name="clazz" select="'INGESCHREVEN NATUURLIJK PERSOON'"/>
        </xsl:call-template>
        <nat_prs>
            <clazz>INGESCHREVEN NATUURLIJK PERSOON</clazz>
            <xsl:call-template name="geregistreerd_persoon-nat_persoon">
                <xsl:with-param name="persoon" select="."/>
            </xsl:call-template>
        </nat_prs>
        <ingeschr_nat_prs>
            <xsl:call-template name="geregistreerd_persoon-ingeschr_nat_persoon">
                <xsl:with-param name="persoon" select="."/>
            </xsl:call-template>
        </ingeschr_nat_prs>
		</comfort>
    </xsl:template>

    <xsl:template match="/snp:KadastraalObjectSnapshot/gba:NietIngezetene">
		<!-- comfort data -->
		<xsl:variable name="comfort-search-value">
			<xsl:call-template name="nen_identificatie">
				<xsl:with-param name="id" select="pers:identificatie"/>
			</xsl:call-template>
		</xsl:variable>
		<comfort search-table="subject" search-column="identif" search-value="{$comfort-search-value}" snapshot-date="{$toestandsdatum}">

		<!-- TODO: class -->
        <xsl:call-template name="persoon">
            <xsl:with-param name="persoon" select="."/>
        </xsl:call-template>
        <nat_prs>
            <xsl:call-template name="geregistreerd_persoon-nat_persoon">
                <xsl:with-param name="persoon" select="."/>
            </xsl:call-template>
        </nat_prs>
        <ingeschr_nat_prs>
            <xsl:call-template name="geregistreerd_persoon-ingeschr_nat_persoon">
                <xsl:with-param name="persoon" select="."/>
            </xsl:call-template>
            <!--fk_17lnd_code_iso> XXX conversie naar 2-letterige ISO code
                <xsl:value-of select="gba:landWaarnaarVertrokken/typ:waarde"/>
            </fk_17lnd_code_iso-->
        </ingeschr_nat_prs>
        <niet_ingezetene>
            <sc_identif>
				<!-- in levering allemaal het zelfde, dus tijdelijk de lokaal id;-->
				<!--xsl:value-of select="gba:BSN"/-->
                <xsl:call-template name="nen_identificatie">
					<xsl:with-param name="id" select="pers:identificatie"/>
				</xsl:call-template>
            </sc_identif>
        </niet_ingezetene>

		</comfort>
    </xsl:template>

    <xsl:template match="/snp:KadastraalObjectSnapshot/pers:KADNietNatuurlijkPersoon">
		<!-- comfort data -->
		<xsl:variable name="comfort-search-value">
			<xsl:call-template name="nen_identificatie">
				<xsl:with-param name="id" select="pers:identificatie"/>
			</xsl:call-template>
		</xsl:variable>
		<comfort search-table="subject" search-column="identif" search-value="{$comfort-search-value}" snapshot-date="{$toestandsdatum}">

			<xsl:call-template name="persoon">
				<xsl:with-param name="persoon" select="."/>
				<xsl:with-param name="clazz" select="'INGESCHREVEN NIET-NATUURLIJK PERSOON'"/>
			</xsl:call-template>

			<niet_nat_prs>
				<xsl:call-template name="rechtspersoon-niet_nat_persoon">
					<xsl:with-param name="persoon" select="."/>
					<xsl:with-param name="clazz" select="'INGESCHREVEN NIET-NATUURLIJK PERSOON'"/>
				</xsl:call-template>
			</niet_nat_prs>
			<ingeschr_niet_nat_prs>
				<sc_identif>
					<xsl:call-template name="nen_identificatie">
						<xsl:with-param name="id" select="pers:identificatie"/>
					</xsl:call-template>
				</sc_identif>
				<rechtsvorm><xsl:value-of select="pers:rechtsvorm/typ:waarde"/></rechtsvorm>
				<statutaire_zetel><xsl:value-of select="pers:statutaireZetel"/></statutaire_zetel>
			</ingeschr_niet_nat_prs>

		</comfort>
    </xsl:template>

    <xsl:template match="/snp:KadastraalObjectSnapshot/nhr:Rechtspersoon">
		<!-- comfort data -->
		<xsl:variable name="comfort-search-value">
			<xsl:call-template name="nen_identificatie">
				<xsl:with-param name="id" select="pers:identificatie"/>
			</xsl:call-template>
		</xsl:variable>
		<comfort search-table="subject" search-column="identif" search-value="{$comfort-search-value}" snapshot-date="{$toestandsdatum}">

			<xsl:call-template name="persoon">
				<xsl:with-param name="persoon" select="."/>
				<xsl:with-param name="clazz" select="'INGESCHREVEN NIET-NATUURLIJK PERSOON'"/>
			</xsl:call-template>

			<niet_nat_prs>
				<sc_identif>
					<xsl:call-template name="nen_identificatie">
						<xsl:with-param name="id" select="pers:identificatie"/>
					</xsl:call-template>
				</sc_identif>
				<clazz>INGESCHREVEN NIET-NATUURLIJK PERSOON</clazz>
				<naam>
					<xsl:value-of select="nhr:statutaireNaam"/>
				</naam>
			</niet_nat_prs>
			<ingeschr_niet_nat_prs>
				<sc_identif>
					<xsl:call-template name="nen_identificatie">
						<xsl:with-param name="id" select="pers:identificatie"/>
					</xsl:call-template>
				</sc_identif>
				<rechtsvorm><xsl:value-of select="nhr:rechtsvorm/typ:waarde"/></rechtsvorm>
				<statutaire_zetel><xsl:value-of select="nhr:statutaireZetel"/></statutaire_zetel>
                <xsl:for-each select="nhr:RSIN">
                    <rsin><xsl:value-of select="."/></rsin>
                </xsl:for-each>
			</ingeschr_niet_nat_prs>

		</comfort>
    </xsl:template>

    <!-- templates voor persoon -->
    <xsl:template name="persoon">
        <xsl:param name="persoon"/>
        <xsl:param name="clazz"/>

		<subject>
			<identif>
                <xsl:call-template name="nen_identificatie">
					<xsl:with-param name="id" select="$persoon/pers:identificatie"/>
				</xsl:call-template>
			</identif>
			<xsl:if test="$clazz">
				<clazz><xsl:value-of select="$clazz"/></clazz>
			</xsl:if>
            <xsl:for-each select="nhr:KVKnummer">
                <kvk_nummer><xsl:value-of select="."/></kvk_nummer>
            </xsl:for-each>
        </subject>

        <prs>
            <sc_identif>
                <xsl:call-template name="nen_identificatie">
					<xsl:with-param name="id" select="$persoon/pers:identificatie"/>
				</xsl:call-template>
            </sc_identif>
			<xsl:if test="$clazz">
				<clazz><xsl:value-of select="$clazz"/></clazz>
			</xsl:if>
        </prs>
    </xsl:template>

    <xsl:template name="geregistreerd_persoon-ingeschr_nat_persoon">
        <xsl:param name="persoon"/>
        <sc_identif>
			<xsl:call-template name="nen_identificatie">
				<xsl:with-param name="id" select="$persoon/pers:identificatie"/>
			</xsl:call-template>
		</sc_identif>
				<bsn>
					<xsl:value-of select="$persoon/gba:BSN"/>
				</bsn>
        <gb_geboorteplaats>
            <xsl:value-of select="$persoon/gba:geboorte/gba:geboorteplaats"/>
        </gb_geboorteplaats>
        <!--fk_gb_lnd_code_iso> XXX conversie naar 2-letterige ISO code
            <xsl:value-of select="$persoon/gba:geboorte/gba:geboorteland/typ:code"/>
        </fk_gb_lnd_code_iso-->
        <gb_geboortedatum>
			<xsl:for-each select="$persoon/gba:geboorte/gba:geboortedatum"><xsl:call-template name="numeric-date"/></xsl:for-each>
        </gb_geboortedatum>
        <ol_overlijdensdatum>
            <xsl:for-each select="$persoon/gba:overlijden/gba:datumOverlijden"><xsl:call-template name="numeric-date"/></xsl:for-each>
        </ol_overlijdensdatum>
        <xsl:for-each select="$persoon/pers:woonlocatie">
			<va_loc_beschrijving>
				<xsl:call-template name="describe-locatie"/>
			</va_loc_beschrijving>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="geregistreerd_persoon-nat_persoon">
        <xsl:param name="persoon"/>
        <sc_identif>
			<xsl:call-template name="nen_identificatie">
				<xsl:with-param name="id" select="$persoon/pers:identificatie"/>
			</xsl:call-template>
        </sc_identif>
        <nm_geslachtsnaam>
            <xsl:value-of select="$persoon/gba:naam/gba:geslachtsnaam"/>
        </nm_geslachtsnaam>
        <nm_voornamen>
            <xsl:value-of select="$persoon/gba:naam/gba:voornamen"/>
        </nm_voornamen>
        <nm_voorvoegsel_geslachtsnaam>
            <xsl:value-of select="$persoon/gba:naam/gba:voorvoegselsgeslachtsnaam"/>
        </nm_voorvoegsel_geslachtsnaam>
        <geslachtsaand>
            <xsl:value-of select="$persoon/gba:geslacht/gba:geslachtsaanduiding/typ:code"/>
        </geslachtsaand>
        <aand_naamgebruik>
            <xsl:value-of select="$persoon/gba:aanduidingNaamgebruik/typ:code"/>
        </aand_naamgebruik>
    </xsl:template>

    <xsl:template name="rechtspersoon-niet_nat_persoon">
        <xsl:param name="persoon"/>
        <xsl:param name="clazz"/>
        <sc_identif>
			<xsl:call-template name="nen_identificatie">
				<xsl:with-param name="id" select="$persoon/pers:identificatie"/>
			</xsl:call-template>
        </sc_identif>
		<xsl:if test="$clazz">
			<clazz><xsl:value-of select="$clazz"/></clazz>
		</xsl:if>
        <naam>
            <xsl:value-of select="$persoon/pers:naam"/>
        </naam>
    </xsl:template>

    <xsl:template match="Stuk:*">
		<xsl:variable name="parent-id">
			<xsl:call-template name="nen_identificatie">
				<xsl:with-param name="id" select="Stuk:identificatie"/>
			</xsl:call-template>
		</xsl:variable>
		<brondocument ignore-duplicates="yes">
			<identificatie><xsl:value-of select="$parent-id"/></identificatie>
			<xsl:for-each select="Stuk:tijdstipAanbieding">
				<!-- Alleen voor TerInschrijvingAangebodenStuk -->
				<datum>
					<xsl:value-of select="."/>
				</datum>
			</xsl:for-each>
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
				<!--
				<xsl:otherwise>
					<tabel>KAD_ONRRND_ZAAK</tabel>
					<tabel_identificatie>
						<xsl:value-of select="../ko:Perceel/ko:identificatie/nen:lokaalId | ../ko:Appartementsrecht/ko:identificatie/nen:lokaalId"/>
					</tabel_identificatie>
				</xsl:otherwise>
				-->
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
				<tabel_identificatie><xsl:value-of select="$parent-id"/></tabel_identificatie>
                <xsl:for-each select="Stuk:aardStukdeel">
                    <omschrijving><xsl:value-of select="typ:waarde"/></omschrijving>
                </xsl:for-each>
                <xsl:for-each select="../../Stuk:tijdstipAanbieding">
                    <datum><xsl:value-of select="."/></datum>
                </xsl:for-each>
                <ref_id><xsl:value-of select="$kad_oz_id"/></ref_id>
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
				<tabel><xsl:value-of select="$tabel"/></tabel>
				<tabel_identificatie><xsl:value-of select="$tabel_identificatie"/></tabel_identificatie>
				<xsl:if test="$omschrijving">
					<omschrijving><xsl:value-of select="$omschrijving"/></omschrijving>
                </xsl:if>
				<datum><xsl:value-of select="//Stuk:Stukdeel[@id = $id]/../../Stuk:tijdstipAanbieding"/></datum>
				<xsl:if test="$ref_id">
					<ref_id><xsl:value-of select="$ref_id"/></ref_id>
               </xsl:if>
			</brondocument>
		</xsl:for-each>
    </xsl:template>

	<xsl:template name="nen_identificatie">
		<xsl:param name="id"/>
		<xsl:value-of select="$id/nen:namespace"/>.<xsl:value-of select="$id/nen:lokaalId"/>
	</xsl:template>

	<!-- Levert een string op die een locatie beschrijft:
      - PostbusAdres
      - KADBinnenlandsAdres
      - Ligplaats/Standplaats/Verblijfsobject
      - BuitenlandsAdres
      - KADBuitenlandsAdres
    -->
	<xsl:template name="describe-locatie" xmlns:adres="http://www.kadaster.nl/schemas/brk-levering/snapshot/imkad-adres/v20120201">
		<xsl:for-each select="adres:PostbusAdres">
			Postbus <xsl:value-of select="adres:postbusnummer"/>, <xsl:value-of select="adres:postcode"/><xsl:text> </xsl:text><xsl:value-of select="adres:woonplaatsNaam"/>
		</xsl:for-each>
		<xsl:for-each select="adres:KADBinnenlandsAdres">
			<xsl:value-of select="adres:openbareRuimteNaam"/><xsl:text> </xsl:text><xsl:value-of select="adres:huisNummer"/><xsl:for-each select="adres:huisNummerToevoeging"><xsl:text> </xsl:text><xsl:value-of select="."/></xsl:for-each><xsl:for-each select="adres:huisLetter"><xsl:text> </xsl:text><xsl:value-of select="."/></xsl:for-each>, <xsl:value-of select="adres:postcode"/><xsl:text> </xsl:text><xsl:value-of select="adres:woonplaatsNaam"/>
		</xsl:for-each>
		<xsl:for-each select="bagadres:Ligplaats | bagadres:Standplaats | bagadres:Verlijfsobject">
			BAG ID: <xsl:value-of select="bagadres:BAGIdentificatie"/>
		</xsl:for-each>
		<xsl:for-each select="gba:BuitenlandsAdres">
			<xsl:value-of select="gba:adres"/>, <xsl:value-of select="gba:woonplaats"/><xsl:for-each select="gba:regio">, <xsl:value-of select="."/></xsl:for-each>, <xsl:value-of select="gba:land/typ:waarde"/>
		</xsl:for-each>
		<xsl:for-each select="adres:KADBuitenlandsAdres">
			<xsl:value-of select="adres:adres"/>, <xsl:value-of select="adres:woonplaats"/><xsl:for-each select="adres:regio">, <xsl:value-of select="adres:regio"/></xsl:for-each>, <xsl:value-of select="adres:land"/>
		</xsl:for-each>
	</xsl:template>


	<!-- jjjj-mm-dd -> jjjjmmdd -->
	<xsl:template name="numeric-date">
		<xsl:value-of select="concat(substring(.,1,4),substring(.,6,2),substring(.,9,2))"/>
	</xsl:template>
</xsl:stylesheet>
