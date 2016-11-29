<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gmi="http://www.isotc211.org/2005/gmi" xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:gsr="http://www.isotc211.org/2005/gsr" xmlns:gss="http://www.isotc211.org/2005/gss" xmlns:gts="http://www.isotc211.org/2005/gts" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xs="http://www.w3.org/2001/XMLSchema">
    <xsl:output method="html" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" doctype-system="http://www.w3.org/TR/html4/loose.dtd" encoding="ISO-8859-1" indent="yes"/>
    <xsl:template match="/">
        <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <title>
                    <xsl:value-of select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
                </title>
                <style>
                    body {
                                    
                    font-family: "Helvetica Neue", "Lucida Grande", "Lucida Sans", Arial, sans-serif;
                    background-color:  white;
                    }

                    h1 {
                    color: black;
                    margin-left: 40px;
                    } 

                    th {
                    color: black;
                    font-size: 14px;
                    } 

                    .mdname {
                    background-color: darkgray;
                    } 

                    .mdrow {
                    background-color: lightgray;
                    } 

                </style>
            </head>
            <body>
                <h1>
                    <xsl:value-of select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
                </h1>
                <table>
                    <tr colspan="3">
                        <th>Algemeen</th>
                    </tr>
                    <tr class="mdrow">
                        <td class="mdname">Naam van de dataset</td>
                        <td class="mdvalue">
                            <xsl:for-each select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString">
                                <xsl:value-of select="."/>
                                <xsl:text> </xsl:text>
                            </xsl:for-each>
                        </td>
                        <td class="mdexplain">De herkenbare naam van de dataset waarmee de gebruiker de dataset voldoende kan herkennen. Bijvoorbeeld: Strooiroutes; oplaadpunten elektrisch vervoer; Verkeersintensiteiten op lokale wegen; Gemeentelijk vastgoed.</td>
                    </tr>
                    <tr class="mdrow">
                        <td class="mdname">Samenvatting</td>
                        <td class="mdvalue">
                            <xsl:for-each select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString">
                                <xsl:value-of select="."/>
                                <xsl:text> </xsl:text>
                            </xsl:for-each>
                        </td>
                        <td class="mdexplain">Een korte beschrijving van de inhoud van de dataset. Denk aan de  aard, omvang, tijdsperiode, scope, dekking, belangrijkste variabelen en andere relevante  eigenschappen van de data.</td>
                    </tr>
                    <tr class="mdrow">
                        <td class="mdname">Datahoudende organisatie</td>
                        <td class="mdvalue">
                            <xsl:for-each select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString">
                                <xsl:value-of select="."/>
                                <xsl:text> </xsl:text>
                            </xsl:for-each>
                        </td>
                        <td class="mdexplain">De  naam van de (overheids)organisatie die verantwoordelijk is voor de dataset. Voluit geschreven. Bijvoorbeeld: Gemeente Haarlem; Rijkswaterstaat (RWS).</td>
                    </tr>
                    <tr class="mdrow">
                        <td class="mdname">Metadata wijziginsdatum</td>
                        <td class="mdvalue">
                            <xsl:for-each select="gmd:MD_Metadata/gmd:dateStamp/gco:Date">
                                <xsl:value-of select="."/>
                                <xsl:text> </xsl:text>
                            </xsl:for-each>
                        </td>
                        <td class="mdexplain">De datum waarop dit metadatabestand voor het laatst is gewijzigd.</td>
                    </tr>
                    <tr class="mdrow">
                        <td class="mdname">Metadata unieke identifier</td>
                        <td class="mdvalue">
                            <xsl:for-each select="gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString">
                                <xsl:value-of select="."/>
                                <xsl:text> </xsl:text>
                            </xsl:for-each>
                        </td>
                        <td class="mdexplain">Unieke code die bij deze metadata hoort. Deze code wordt automatisch gegenereerd; graag alleen wijzigen indien het expliciet bekend is dat de code een andere waarde moet hebben.</td>
                    </tr>
                    <tr colspan="3">
                        <th>Kenmerken</th>
                    </tr>
                    <tr class="mdrow">
                        <td class="mdname">Doel van de vervaardiging</td>
                        <td class="mdvalue">
                            <xsl:for-each select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:purpose/gco:CharacterString">
                                <xsl:value-of select="."/>
                                <xsl:text> </xsl:text>
                            </xsl:for-each>
                        </td>
                        <td class="mdexplain">Het doel waarvoor de data oorspronkelijk is gemaakt, voor welk doel de data door de datahoudende organisatie wordt gebruikt. </td>
                    </tr>
                    <tr class="mdrow">
                        <td class="mdname">Hoe zijn de data verzameld?</td>
                        <td class="mdvalue">
                            <xsl:for-each select="gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString">
                                <xsl:value-of select="."/>
                                <xsl:text> </xsl:text>
                            </xsl:for-each>
                        </td>
                        <td class="mdexplain">De wijze waarop de data zijn verzameld, de herkomst van de data, het productieproces. </td>
                    </tr>
                    <tr class="mdrow">
                        <td class="mdname">Volledigheid van de dataset</td>
                        <td class="mdvalue">
                            <xsl:for-each select="gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_CompletenessOmission/gmd:result/gmd:DQ_QuantitativeResult/gmd:value/gco:Record">
                                <xsl:value-of select="."/>
                                <xsl:text> </xsl:text>
                            </xsl:for-each>
                        </td>
                        <td class="mdexplain">De volledigheid van de dataset. Bijvoorbeeld: "De dataset bevat alle bomen in de openbare ruimte van de gemeente Utrecht behalve die van Leidsche Rijn omdat deze 'wijk in ontwikkeling' nog door de projectorganisatie beheerd wordt". </td>
                    </tr>
                    <tr class="mdrow">
                        <td class="mdname">Trefwoorden</td>
                        <td class="mdvalue">
                            <xsl:for-each select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString">
                                <xsl:value-of select="."/>
                                <xsl:text> </xsl:text>
                            </xsl:for-each>
                        </td>
                        <td class="mdexplain">De meest relevante trefwoorden die op de dataset van toepassing zijn. Waarmee de dataset beter gevonden kan worden. Houd het aantal trefwoorden beperkt. Twijfel = nee.</td>
                    </tr>
                    <tr colspan="3">
                        <th>Actualiteit</th>
                    </tr>
                    <tr class="mdrow">
                        <td class="mdname">Creatiedatum van de dataset</td>
                        <td class="mdvalue">
                            <xsl:for-each select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date">
                                <xsl:value-of select="."/>
                                <xsl:text> </xsl:text>
                            </xsl:for-each>
                        </td>
                        <td class="mdexplain">De datum waarop de originele dataset, de brondata voor het eerst is gemaakt. Geeft informatie over de historie en gecombineerd met de updatefrequentie over de tijdspanne van de dataset.</td>
                    </tr>
                    <tr class="mdrow">
                        <td class="mdname">Updatefrequentie van de dataset</td>
                        <td class="mdvalue">
                            <xsl:for-each select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceMaintenance/gmd:MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode">
                                <xsl:value-of select="."/>
                                <xsl:text> </xsl:text>
                            </xsl:for-each>
                        </td>
                        <td class="mdexplain">Frequentie waarmee de data herzien wordt.</td>
                    </tr>
                    <xsl:for-each select="gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine">
                        <tr colspan="3">
                            <th>Naar de data [<xsl:value-of select="position()"/>]</th>
                        </tr>
                        <tr class="mdrow">
                            <td class="mdname">URL dataset</td>
                            <td class="mdvalue">
                                <xsl:for-each select="gmd:CI_OnlineResource/gmd:linkage/gmd:URL">
                                    <xsl:value-of select="."/>
                                    <xsl:text> </xsl:text>
                                </xsl:for-each>
                            </td>
                            <td class="mdexplain">De link naar de service of naar het te downloaden bestand. </td>
                        </tr>
                        <tr class="mdrow">
                            <td class="mdname">Bestands- of kaartlaagnaam</td>
                            <td class="mdvalue">
                                <xsl:for-each select="gmd:CI_OnlineResource/gmd:name/gco:CharacterString">
                                    <xsl:value-of select="."/>
                                    <xsl:text> </xsl:text>
                                </xsl:for-each>
                            </td>
                            <td class="mdexplain">De bestandsnaam inclusief de extensie of de kaartlaagnaam voor gebruik bij WMS en WFS.</td>
                        </tr>
                        <tr class="mdrow">
                            <td class="mdname">Type service</td>
                            <td class="mdvalue">
                                <xsl:for-each select="gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString">
                                    <xsl:value-of select="."/>
                                    <xsl:text> </xsl:text>
                                </xsl:for-each>
                            </td>
                            <td class="mdexplain">Het type service waarmee de data wordt aangeroepen of gedownload kan worden. De meest bekende services voor geodata bijvoorbeeld zijn WMS (Web Map Service) en WFS (Web Feature Service). Hiermee kan via een weblink (URL), een directe en actuele koppeling met de brondata tot stand gebracht worden.</td>
                        </tr>
                    </xsl:for-each>
                    <tr colspan="3">
                        <th>Openheid</th>
                    </tr>
                    <tr class="mdrow">
                        <td class="mdname">Gebruiksvoorwaarden (licentie)</td>
                        <td class="mdvalue">
                            <xsl:for-each select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString">
                                <xsl:value-of select="."/>
                                <xsl:text> </xsl:text>
                            </xsl:for-each>
                        </td>
                        <td class="mdexplain">Wie data hergebruikt, moet weten welke voorwaarden er gelden. De Nederlandse overheid wil overheidsinformatie zoveel mogelijk gratis en zonder voorwaarden beschikbaar stellen. Bij voorkeur via de Creative Commons Zero (CC0) Publiek Domein Verklaring. Met deze verklaring wordt afstand gedaan van alle rechten.</td>
                    </tr>
                    <tr class="mdrow">
                        <td class="mdname">Aanvullende informatie</td>
                        <td class="mdvalue">
                            <xsl:for-each select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/gco:CharacterString">
                                <xsl:value-of select="."/>
                                <xsl:text> </xsl:text>
                            </xsl:for-each>
                        </td>
                        <td class="mdexplain">Vul hier naar behoefte in, alles wat wel relevant is om te noemen ten aanzien van de dataset maar wat niet past in een van de andere velden.</td>
                    </tr>
                    <tr colspan="3">
                        <th>Contactgegevens</th>
                    </tr>
                    <tr class="mdrow">
                        <td class="mdname">Voor- en achternaam contactpersoon</td>
                        <td class="mdvalue">
                            <xsl:for-each select="gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString">
                                <xsl:value-of select="."/>
                                <xsl:text> </xsl:text>
                            </xsl:for-each>
                        </td>
                        <td class="mdexplain">Het gaat hier om de eerste persoon met wie contact opgenomen kan worden bij vragen over de dataset. Deze persoon is in staat om alle vragen over de dataset te adresseren. Bij zichzelf of bij collega's.</td>
                    </tr>
                    <tr class="mdrow">
                        <td class="mdname">Organisatie contactpersoon</td>
                        <td class="mdvalue">
                            <xsl:for-each select="gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString">
                                <xsl:value-of select="."/>
                            </xsl:for-each>
                        </td>
                        <td class="mdexplain">De organisatie waar de contactpersoon werkzaam is.</td>
                    </tr>
                    <tr class="mdrow">
                        <td class="mdname">Functie contactpersoon</td>
                        <td class="mdvalue">
                            <xsl:for-each select="gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:positionName/gco:CharacterString">
                                <xsl:value-of select="."/>
                                <xsl:text> </xsl:text>
                            </xsl:for-each>
                        </td>
                        <td class="mdexplain">De functie van de contactpersoon binnen zijn of haar organisatie.
                            De organisatie waar de contactpersoon werkzaam is.
                        </td>
                    </tr>
                    <tr class="mdrow">
                        <td class="mdname">E-mailadres contactpersoon</td>
                        <td class="mdvalue">
                            <xsl:for-each select="gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString">
                                <xsl:value-of select="."/>
                                <xsl:text> </xsl:text>
                            </xsl:for-each>
                        </td>
                        <td class="mdexplain">Het e-mailadres van de contactpersoon.</td>
                    </tr>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
