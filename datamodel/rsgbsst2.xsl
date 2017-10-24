<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:uml="http://www.omg.org/spec/UML/20110701" xmlns:xmi="http://www.omg.org/spec/XMI/20110701" xmlns:thecustomprofile="http://www.sparxsystems.com/profiles/thecustomprofile/1.0" xmlns:RGB="http://www.sparxsystems.com/profiles/RGB/1.0">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/xmi:XMI">
		<xsl:element name="RSGB">
			<xsl:element name="Metadata_definities">
				<xsl:for-each select="RGB:Objecttype">
					<xsl:variable name="bc" select="@base_Class"/>
					<xsl:element name="Metadata">
						<xsl:attribute name="type"><xsl:value-of select="'Objecttype'"/></xsl:attribute>
						<xsl:attribute name="id"><xsl:value-of select="$bc"/></xsl:attribute>
						<xsl:for-each select="//packagedElement[@xmi:id=$bc]">
							<xsl:attribute name="bcName"><xsl:value-of select="@name"/></xsl:attribute>
						</xsl:for-each>
						<xsl:attribute name="Toelichting"><xsl:value-of select="@Toelichting_objecttype"/></xsl:attribute>
						<xsl:attribute name="Datum_opname"><xsl:value-of select="@Datum_opname_objecttype"/></xsl:attribute>
						<xsl:attribute name="Kwaliteitsbegrip"><xsl:value-of select="@Kwaliteitsbegrip_objecttype"/></xsl:attribute>
						<xsl:attribute name="Populatie"><xsl:value-of select="@ Populatie_objecttype"/></xsl:attribute>
						<xsl:attribute name="Herkomst"><xsl:value-of select="@Herkomst_objecttype"/></xsl:attribute>
						<xsl:attribute name="Code"><xsl:value-of select="@ Code_objecttype"/></xsl:attribute>
						<xsl:attribute name="Unieke_aanduiding"><xsl:value-of select="@Unieke_aanduiding_objecttype"/></xsl:attribute>
						<xsl:attribute name="Herkomst_definitie"><xsl:value-of select="@Herkomst_definitie_objecttype"/></xsl:attribute>
					</xsl:element>
				</xsl:for-each>
				<xsl:for-each select="RGB:Groepattribuutsoort">
					<xsl:variable name="bc" select="@base_Attribute"/>
					<xsl:element name="Metadata">
						<xsl:attribute name="type"><xsl:value-of select="'Groepattribuutsoort'"/></xsl:attribute>
						<xsl:attribute name="id"><xsl:value-of select="$bc"/></xsl:attribute>
						<xsl:for-each select="//ownedAttribute[@xmi:id=$bc]">
							<xsl:attribute name="bcName"><xsl:value-of select="@name"/></xsl:attribute>
						</xsl:for-each>
						<xsl:attribute name="Aanduiding_gebeurtenis"><xsl:value-of select="@Aanduiding_gebeurtenis"/></xsl:attribute>
						<xsl:attribute name="Aanduiding__strijdigheid_nietigheid"><xsl:value-of select="@Aanduiding__strijdigheid_nietigheid"/></xsl:attribute>
						<xsl:attribute name="Aanduiding_brondocument"><xsl:value-of select="@Aanduiding_brondocument"/></xsl:attribute>
						<xsl:attribute name="Code"><xsl:value-of select="@Code_attribuutsoort"/></xsl:attribute>
						<xsl:attribute name="Datum_opname"><xsl:value-of select="@Datum_opname_attribuutsoort"/></xsl:attribute>
						<xsl:attribute name="Herkomst"><xsl:value-of select="@Herkomst_attribuutsoort"/></xsl:attribute>
						<xsl:attribute name="Herkomst_definitie"><xsl:value-of select="@Herkomst_definitie_attribuutsoort"/></xsl:attribute>
						<xsl:attribute name="Indicatie_authentiek"><xsl:value-of select="@Indicatie_authentiek"/></xsl:attribute>
						<xsl:attribute name="Indicatie_formele_historie"><xsl:value-of select="@Indicatie_formele_historie"/></xsl:attribute>
						<xsl:attribute name="Indicatie_in_onderzoek"><xsl:value-of select="@Indicatie_in_onderzoek"/></xsl:attribute>
						<xsl:attribute name="Indicatie_materiële_historie"><xsl:value-of select="@Indicatie_materiële_historie"/></xsl:attribute>
						<xsl:attribute name="Kwaliteitsbegrip"><xsl:value-of select="@Kwaliteitsbegrip"/></xsl:attribute>
						<xsl:attribute name="Naam_terugrelatie"><xsl:value-of select="@Naam_terugrelatie"/></xsl:attribute>
						<xsl:attribute name="Populatie"><xsl:value-of select="@Populatie"/></xsl:attribute>
						<xsl:attribute name="Regels"><xsl:value-of select="@Regels_attribuutsoort"/></xsl:attribute>
						<xsl:attribute name="Toelichting"><xsl:value-of select="@Toelichting"/></xsl:attribute>
						<xsl:attribute name="Unieke_aanduiding"><xsl:value-of select="@Unieke_aanduiding"/></xsl:attribute>
						<xsl:attribute name="Waardenverzameling"><xsl:value-of select="@Waardenverzameling"/></xsl:attribute>
					</xsl:element>
				</xsl:for-each>
				<xsl:for-each select="RGB:Attribuutsoort">
					<xsl:variable name="bc" select="@base_Attribute"/>
					<xsl:element name="Metadata">
						<xsl:attribute name="type"><xsl:value-of select="'Attribuutsoort'"/></xsl:attribute>
						<xsl:attribute name="id"><xsl:value-of select="$bc"/></xsl:attribute>
						<xsl:for-each select="//ownedAttribute[@xmi:id=$bc]">
							<xsl:attribute name="bcName"><xsl:value-of select="@name"/></xsl:attribute>
						</xsl:for-each>
						<xsl:attribute name="Aanduiding__strijdigheid_nietigheid"><xsl:value-of select="@Aanduiding__strijdigheid_nietigheid"/></xsl:attribute>
						<xsl:attribute name="Aanduiding_brondocument"><xsl:value-of select="@Aanduiding_brondocument"/></xsl:attribute>
						<xsl:attribute name="Code"><xsl:value-of select="@Code_attribuutsoort"/></xsl:attribute>
						<xsl:attribute name="Datum_opname"><xsl:value-of select="@Datum_opname_attribuutsoort"/></xsl:attribute>
						<xsl:attribute name="Herkomst"><xsl:value-of select="@Herkomst_attribuutsoort"/></xsl:attribute>
						<xsl:attribute name="Herkomst_definitie"><xsl:value-of select="@Herkomst_definitie"/></xsl:attribute>
						<xsl:attribute name="Indicatie_authentiek"><xsl:value-of select="@Indicatie_authentiek"/></xsl:attribute>
						<xsl:attribute name="Indicatie_formele_historie"><xsl:value-of select="@Indicatie_formele_historie"/></xsl:attribute>
						<xsl:attribute name="Indicatie_in_onderzoek"><xsl:value-of select="@Indicatie_in_onderzoek"/></xsl:attribute>
						<xsl:attribute name="Indicatie_materiële_historie"><xsl:value-of select="@Indicatie_materiële_historie"/></xsl:attribute>
						<xsl:attribute name="Kwaliteitsbegrip"><xsl:value-of select="@Kwaliteitsbegrip"/></xsl:attribute>
						<xsl:attribute name="Naam_terugrelatie"><xsl:value-of select="@Naam_terugrelatie"/></xsl:attribute>
						<xsl:attribute name="Populatie"><xsl:value-of select="@Populatie"/></xsl:attribute>
						<xsl:attribute name="Regels"><xsl:value-of select="@Regels"/></xsl:attribute>
						<xsl:attribute name="Toelichting"><xsl:value-of select="@Toelichting_attribuutsoort"/></xsl:attribute>
						<xsl:attribute name="Unieke_aanduiding"><xsl:value-of select="@Unieke_aanduiding"/></xsl:attribute>
						<xsl:attribute name="Waardenverzameling"><xsl:value-of select="@Waardenverzameling"/></xsl:attribute>
					</xsl:element>
				</xsl:for-each>
				<xsl:for-each select="RGB:Relatiesoort">
					<xsl:variable name="bc" select="@base_Association"/>
					<xsl:element name="Metadata">
						<xsl:attribute name="type"><xsl:value-of select="'Relatiesoort'"/></xsl:attribute>
						<xsl:attribute name="id"><xsl:value-of select="$bc"/></xsl:attribute>
						<xsl:for-each select="//packagedElement[@xmi:id=$bc]">
							<xsl:attribute name="bcName"><xsl:value-of select="@name"/></xsl:attribute>
						</xsl:for-each>
						<xsl:attribute name="Aanduiding__strijdigheid_nietigheid"><xsl:value-of select="@Aanduiding__strijdigheid_nietigheid"/></xsl:attribute>
						<xsl:attribute name="Aanduiding_brondocument"><xsl:value-of select="@Aanduiding_brondocument"/></xsl:attribute>
						<xsl:attribute name="Code"><xsl:value-of select="@Code_relatiesoort"/></xsl:attribute>
						<xsl:attribute name="Datum_opname"><xsl:value-of select="@Datum_opname_relatiesoort"/></xsl:attribute>
						<xsl:attribute name="Herkomst"><xsl:value-of select="@Herkomst_relatiesoort"/></xsl:attribute>
						<xsl:attribute name="Herkomst_definitie"><xsl:value-of select="@Herkomst_definitie_relatiesoort"/></xsl:attribute>
						<xsl:attribute name="Indicatie_authentiek"><xsl:value-of select="@Indicatie_authentiek"/></xsl:attribute>
						<xsl:attribute name="Indicatie_formele_historie"><xsl:value-of select="@Indicatie_formele_historie"/></xsl:attribute>
						<xsl:attribute name="Indicatie_in_onderzoek"><xsl:value-of select="@Indicatie_in_onderzoek"/></xsl:attribute>
						<xsl:attribute name="Indicatie_materiële_historie"><xsl:value-of select="@Indicatie_materiële_historie"/></xsl:attribute>
						<xsl:attribute name="Kwaliteitsbegrip"><xsl:value-of select="@Kwaliteitsbegrip"/></xsl:attribute>
						<xsl:attribute name="Naam_terugrelatie"><xsl:value-of select="@Naam_terugrelatie"/></xsl:attribute>
						<xsl:attribute name="Populatie"><xsl:value-of select="@Populatie"/></xsl:attribute>
						<xsl:attribute name="Regels"><xsl:value-of select="@Regels_relatiesoort"/></xsl:attribute>
						<xsl:attribute name="Toelichting"><xsl:value-of select="@Toelichting_relatiesoort"/></xsl:attribute>
						<xsl:attribute name="Unieke_aanduiding"><xsl:value-of select="@Unieke_aanduiding"/></xsl:attribute>
						<xsl:attribute name="Waardenverzameling"><xsl:value-of select="@Waardenverzameling"/></xsl:attribute>
					</xsl:element>
				</xsl:for-each>
				<!-- geen nuttige info
			<xsl:for-each select="RGB:Generalisatie">
				<xsl:variable name="bc" select="@base_Generalization"/>
				<xsl:element name="Metadata">
					<xsl:attribute name="type"><xsl:value-of select="'Generalisatie'"/></xsl:attribute>
					<xsl:attribute name="id"><xsl:value-of select="$bc"/></xsl:attribute>
					<xsl:for-each select="//packagedElement[@xmi:id=$bc]">
						<xsl:attribute name="bcName"><xsl:value-of select="@name"/></xsl:attribute>
					</xsl:for-each>
				</xsl:element>
			</xsl:for-each>
			-->
			</xsl:element>
			<xsl:for-each select="uml:Model/packagedElement">
				<xsl:element name="Objecttypes">
					<xsl:for-each select="packagedElement[@name='Model']">
						<xsl:for-each select="packagedElement[@name='Objecttype']">
							<xsl:for-each select="packagedElement[@xmi:type='uml:Class']">
								<xsl:element name="Class">
									<xsl:attribute name="id"><xsl:value-of select="@xmi:id"/></xsl:attribute>
									<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
									<xsl:attribute name="isAbstract"><xsl:value-of select="@isAbstract"/></xsl:attribute>
									<xsl:for-each select="generalization">
										<xsl:variable name="general" select="@general"/>
										<xsl:attribute name="superclassId"><xsl:value-of select="@general"/></xsl:attribute>
										<xsl:attribute name="superclass"><xsl:value-of select="../../packagedElement[@xmi:id=$general]/@name"/></xsl:attribute>
									</xsl:for-each>							
									<xsl:for-each select="ownedAttribute[@xmi:type='uml:Property']">
										<xsl:element name="Property">
											<xsl:attribute name="elementType"><xsl:value-of select="'Objecttype - Property'"/></xsl:attribute>
											<xsl:attribute name="id"><xsl:value-of select="@xmi:id"/></xsl:attribute>
											<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
											<xsl:for-each select="lowerValue">
												<xsl:attribute name="lowerValueType"><xsl:value-of select="@xmi:type"/></xsl:attribute>
												<xsl:attribute name="lowerValueValue"><xsl:value-of select="@value"/></xsl:attribute>
											</xsl:for-each>
											<xsl:for-each select="upperValue">
												<xsl:attribute name="upperValueType"><xsl:value-of select="@xmi:type"/></xsl:attribute>
												<xsl:attribute name="upperValueValue"><xsl:value-of select="@value"/></xsl:attribute>
											</xsl:for-each>
											<xsl:for-each select="type">
												<xsl:variable name="typeID" select="@xmi:idref"/>
												<xsl:attribute name="type"><xsl:value-of select="$typeID"/></xsl:attribute>
												<xsl:for-each select="//packagedElement[@xmi:id=$typeID]">
													<xsl:attribute name="typeName"><xsl:value-of select="@name"/></xsl:attribute>
												</xsl:for-each>
											</xsl:for-each>
											<xsl:attribute name="visibility"><xsl:value-of select="@visibility"/></xsl:attribute>
											<xsl:attribute name="association"><xsl:value-of select="@association"/></xsl:attribute>
											<xsl:variable name="assoc" select="@association"/>
											<xsl:for-each select="//packagedElement[@xmi:id=$assoc]">
												<xsl:attribute name="associationName"><xsl:value-of select="@name"/></xsl:attribute>
											</xsl:for-each>
										</xsl:element>
									</xsl:for-each>
								</xsl:element>
							</xsl:for-each>
							<!-- Zoek de terugrelaties op. Onder andere nodig voor n-n relaties-->
						<xsl:for-each select="packagedElement[@xmi:type='uml:Association']">
							<xsl:element name="Property">
								<xsl:attribute name="elementType"><xsl:value-of select="'Objecttype - Association'"/></xsl:attribute>
								<xsl:attribute name="id"><xsl:value-of select="@xmi:id"/></xsl:attribute>
								<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
								<xsl:for-each select="ownedEnd">
									<xsl:for-each select="lowerValue">
										<xsl:attribute name="lowerValueType"><xsl:value-of select="@xmi:type"/></xsl:attribute>
										<xsl:attribute name="lowerValueValue"><xsl:value-of select="@value"/></xsl:attribute>
									</xsl:for-each>
									<xsl:for-each select="upperValue">
										<xsl:attribute name="upperValueType"><xsl:value-of select="@type"/></xsl:attribute>
										<xsl:attribute name="upperValueValue"><xsl:value-of select="@value"/></xsl:attribute>
									</xsl:for-each>
									<xsl:for-each select="type">
										<xsl:variable name="typeID" select="@xmi:idref"/>
										<xsl:attribute name="type"><xsl:value-of select="$typeID"/></xsl:attribute>
										<xsl:for-each select="//packagedElement[@xmi:id=$typeID]">
											<xsl:attribute name="typeName"><xsl:value-of select="@name"/></xsl:attribute>
										</xsl:for-each>
									</xsl:for-each>
								</xsl:for-each>
							</xsl:element>
						</xsl:for-each>
						</xsl:for-each>
						<xsl:for-each select="packagedElement[@name='Relatieklasse']">
							<xsl:for-each select="packagedElement[@xmi:type='uml:AssociationClass']">
								<xsl:for-each select="ownedEnd[@xmi:type='uml:Property']">
									<xsl:element name="Property">
										<xsl:attribute name="elementType"><xsl:value-of select="'Objecttype - Superklasse'"/></xsl:attribute>
										<xsl:attribute name="classId"><xsl:value-of select="../@xmi:id"/></xsl:attribute>
										<xsl:attribute name="className"><xsl:value-of select="../@name"/></xsl:attribute>
										<xsl:attribute name="isAbstract"><xsl:value-of select="../@isAbstract"/></xsl:attribute>
										<xsl:attribute name="id"><xsl:value-of select="@xmi:id"/></xsl:attribute>
										<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
										<xsl:for-each select="lowerValue">
											<xsl:attribute name="lowerValueType"><xsl:value-of select="@xmi:type"/></xsl:attribute>
											<xsl:attribute name="lowerValueValue"><xsl:value-of select="@value"/></xsl:attribute>
										</xsl:for-each>
										<xsl:for-each select="upperValue">
											<xsl:attribute name="upperValueType"><xsl:value-of select="@xmi:type"/></xsl:attribute>
											<xsl:attribute name="upperValueValue"><xsl:value-of select="@value"/></xsl:attribute>
										</xsl:for-each>
										<xsl:for-each select="type">
											<xsl:variable name="typeID" select="@xmi:idref"/>
											<xsl:attribute name="type"><xsl:value-of select="$typeID"/></xsl:attribute>
											<xsl:for-each select="//packagedElement[@xmi:id=$typeID]">
												<xsl:attribute name="typeName"><xsl:value-of select="@name"/></xsl:attribute>
											</xsl:for-each>
										</xsl:for-each>
										<xsl:attribute name="visibility"><xsl:value-of select="@visibility"/></xsl:attribute>
										<xsl:attribute name="association"><xsl:value-of select="@association"/></xsl:attribute>
										<xsl:variable name="assoc" select="@association"/>
										<xsl:for-each select="//packagedElement[@xmi:id=$assoc]">
											<xsl:attribute name="associationName"><xsl:value-of select="@name"/></xsl:attribute>
										</xsl:for-each>
									</xsl:element>
								</xsl:for-each>
								<xsl:for-each select="ownedAttribute[@xmi:type='uml:Property']">
									<xsl:element name="Property">
										<xsl:attribute name="elementType"><xsl:value-of select="'Objecttype/Relatieklasse - Property'"/></xsl:attribute>
										<xsl:attribute name="classId"><xsl:value-of select="../@xmi:id"/></xsl:attribute>
										<xsl:attribute name="className"><xsl:value-of select="../@name"/></xsl:attribute>
										<xsl:attribute name="isAbstract"><xsl:value-of select="../@isAbstract"/></xsl:attribute>
										<xsl:attribute name="id"><xsl:value-of select="@xmi:id"/></xsl:attribute>
										<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
										<xsl:for-each select="lowerValue">
											<xsl:attribute name="lowerValueType"><xsl:value-of select="@xmi:type"/></xsl:attribute>
											<xsl:attribute name="lowerValueValue"><xsl:value-of select="@value"/></xsl:attribute>
										</xsl:for-each>
										<xsl:for-each select="upperValue">
											<xsl:attribute name="upperValueType"><xsl:value-of select="@xmi:type"/></xsl:attribute>
											<xsl:attribute name="upperValueValue"><xsl:value-of select="@value"/></xsl:attribute>
										</xsl:for-each>
										<xsl:for-each select="type">
											<xsl:variable name="typeID" select="@xmi:idref"/>
											<xsl:attribute name="type"><xsl:value-of select="$typeID"/></xsl:attribute>
											<xsl:for-each select="//packagedElement[@xmi:id=$typeID]">
												<xsl:attribute name="typeName"><xsl:value-of select="@name"/></xsl:attribute>
											</xsl:for-each>
										</xsl:for-each>
										<xsl:attribute name="visibility"><xsl:value-of select="@visibility"/></xsl:attribute>
										<xsl:attribute name="association"><xsl:value-of select="@association"/></xsl:attribute>
										<xsl:variable name="assoc" select="@association"/>
										<xsl:for-each select="//packagedElement[@xmi:id=$assoc]">
											<xsl:attribute name="associationName"><xsl:value-of select="@name"/></xsl:attribute>
										</xsl:for-each>
									</xsl:element>
								</xsl:for-each>
							</xsl:for-each>
						</xsl:for-each>
						<xsl:for-each select="packagedElement[@name='Groepattribuutsoort']">
							<xsl:for-each select="packagedElement[@xmi:type='uml:Class']">
								<xsl:for-each select="ownedAttribute[@xmi:type='uml:Property']">
									<xsl:element name="Property">
										<xsl:attribute name="elementType"><xsl:value-of select="'Objecttype/Groepattribuutsoort - Property'"/></xsl:attribute>
										<xsl:attribute name="classId"><xsl:value-of select="../@xmi:id"/></xsl:attribute>
										<xsl:attribute name="className"><xsl:value-of select="../@name"/></xsl:attribute>
										<xsl:attribute name="id"><xsl:value-of select="@xmi:id"/></xsl:attribute>
										<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
										<xsl:for-each select="lowerValue">
											<xsl:attribute name="lowerValueType"><xsl:value-of select="@xmi:type"/></xsl:attribute>
											<xsl:attribute name="lowerValueValue"><xsl:value-of select="@value"/></xsl:attribute>
										</xsl:for-each>
										<xsl:for-each select="upperValue">
											<xsl:attribute name="upperValueType"><xsl:value-of select="@xmi:type"/></xsl:attribute>
											<xsl:attribute name="upperValueValue"><xsl:value-of select="@value"/></xsl:attribute>
										</xsl:for-each>
										<xsl:for-each select="type">
											<xsl:variable name="typeID" select="@xmi:idref"/>
											<xsl:attribute name="type"><xsl:value-of select="$typeID"/></xsl:attribute>
											<xsl:for-each select="//packagedElement[@xmi:id=$typeID]">
												<xsl:attribute name="typeName"><xsl:value-of select="@name"/></xsl:attribute>
											</xsl:for-each>
										</xsl:for-each>
									</xsl:element>
								</xsl:for-each>
							</xsl:for-each>
						</xsl:for-each>
						<xsl:for-each select="packagedElement[@name='Referentielijsten']">
							<xsl:for-each select="packagedElement[@xmi:type='uml:Class']">
								<xsl:for-each select="ownedAttribute[@xmi:type='uml:Property']">
									<xsl:element name="Property">
										<xsl:attribute name="elementType"><xsl:value-of select="'Objecttype/Referentielijsten - Property'"/></xsl:attribute>
										<xsl:attribute name="classId"><xsl:value-of select="../@xmi:id"/></xsl:attribute>
										<xsl:attribute name="className"><xsl:value-of select="../@name"/></xsl:attribute>
										<xsl:attribute name="id"><xsl:value-of select="@xmi:id"/></xsl:attribute>
										<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
										<xsl:for-each select="lowerValue">
											<xsl:attribute name="lowerValueType"><xsl:value-of select="@xmi:type"/></xsl:attribute>
											<xsl:attribute name="lowerValueValue"><xsl:value-of select="@value"/></xsl:attribute>
										</xsl:for-each>
										<xsl:for-each select="upperValue">
											<xsl:attribute name="upperValueType"><xsl:value-of select="@xmi:type"/></xsl:attribute>
											<xsl:attribute name="upperValueValue"><xsl:value-of select="@value"/></xsl:attribute>
										</xsl:for-each>
										<xsl:for-each select="type">
											<xsl:variable name="typeID" select="@xmi:idref"/>
											<xsl:attribute name="type"><xsl:value-of select="$typeID"/></xsl:attribute>
											<xsl:for-each select="//packagedElement[@xmi:id=$typeID]">
												<xsl:attribute name="typeName"><xsl:value-of select="@name"/></xsl:attribute>
											</xsl:for-each>
										</xsl:for-each>
									</xsl:element>
								</xsl:for-each>
							</xsl:for-each>
						</xsl:for-each>
						<xsl:for-each select="packagedElement[@name='Enumeratiesoort']">
							<xsl:for-each select="packagedElement[@xmi:type='uml:Enumeration']">
								<xsl:for-each select="ownedLiteral[@xmi:type='uml:EnumerationLiteral']">
									<xsl:element name="Property">
										<xsl:attribute name="elementType"><xsl:value-of select="'Enumeratiesoort - Values'"/></xsl:attribute>
										<xsl:attribute name="classId"><xsl:value-of select="../@xmi:id"/></xsl:attribute>
										<xsl:attribute name="className"><xsl:value-of select="../@name"/></xsl:attribute>
										<xsl:attribute name="id"><xsl:value-of select="@xmi:id"/></xsl:attribute>
										<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
										<xsl:for-each select="type">
											<xsl:variable name="typeID" select="@xmi:idref"/>
											<xsl:attribute name="type"><xsl:value-of select="$typeID"/></xsl:attribute>
											<xsl:for-each select="//packagedElement[@xmi:id=$typeID]">
												<xsl:attribute name="typeName"><xsl:value-of select="@name"/></xsl:attribute>
											</xsl:for-each>
										</xsl:for-each>
									</xsl:element>
								</xsl:for-each>
							</xsl:for-each>
						</xsl:for-each>
						<xsl:for-each select="packagedElement[@name='Metagegevens']">
							<xsl:for-each select="packagedElement[@xmi:type='uml:Class']">
								<xsl:for-each select="ownedAttribute[@xmi:type='uml:Property']">
									<xsl:element name="Property">
										<xsl:attribute name="elementType"><xsl:value-of select="'Objecttype/Metagegevens - Property'"/></xsl:attribute>
										<xsl:attribute name="classId"><xsl:value-of select="../@xmi:id"/></xsl:attribute>
										<xsl:attribute name="className"><xsl:value-of select="../@name"/></xsl:attribute>
										<xsl:attribute name="id"><xsl:value-of select="@xmi:id"/></xsl:attribute>
										<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
										<xsl:for-each select="lowerValue">
											<xsl:attribute name="lowerValueType"><xsl:value-of select="@xmi:type"/></xsl:attribute>
											<xsl:attribute name="lowerValueValue"><xsl:value-of select="@value"/></xsl:attribute>
										</xsl:for-each>
										<xsl:for-each select="upperValue">
											<xsl:attribute name="upperValueType"><xsl:value-of select="@xmi:type"/></xsl:attribute>
											<xsl:attribute name="upperValueValue"><xsl:value-of select="@value"/></xsl:attribute>
										</xsl:for-each>
										<xsl:for-each select="type">
											<xsl:variable name="typeID" select="@xmi:idref"/>
											<xsl:attribute name="type"><xsl:value-of select="$typeID"/></xsl:attribute>
											<xsl:for-each select="//packagedElement[@xmi:id=$typeID]">
												<xsl:attribute name="typeName"><xsl:value-of select="@name"/></xsl:attribute>
											</xsl:for-each>
										</xsl:for-each>
									</xsl:element>
								</xsl:for-each>
							</xsl:for-each>
						</xsl:for-each>
					</xsl:for-each>
				</xsl:element>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>
