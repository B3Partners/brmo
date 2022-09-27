<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/xpath-functions">
	<!-- 
		Repareert het databasemodel voor fouten in het RSGB model en
		verwijdert enige attributen.
    -->
	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
	<!--Identity template, 
        provides default behavior that copies all content into the output -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_073F4BB6_B410_4595_B6E2_A9CDAEC9FD52" name="Overig gebouwd object identificatie"-->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_073F4BB6_B410_4595_B6E2_A9CDAEC9FD52']">
		<xsl:comment>
			<xsl:text>repair.xsl: Overig gebouwd object identificatie  -  verwijderd omdat pk van tabel wordt gebruikt hiervoor</xsl:text>
		</xsl:comment>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_0A77018B_A4B0_4ab2_A817_8DEEC1A2FEB9" name="Standplaatsidentificatie" -->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_0A77018B_A4B0_4ab2_A817_8DEEC1A2FEB9']">
		<xsl:comment>
			<xsl:text>repair.xsl: Standplaatsidentificatie  -  verwijderd omdat pk van tabel wordt gebruikt hiervoor</xsl:text>
		</xsl:comment>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_36805A09_9486_463a_8F72_25044E2C3CAB" name="Overig  terrein identificatie"-->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_36805A09_9486_463a_8F72_25044E2C3CAB']">
		<xsl:comment>
			<xsl:text>repair.xsl: Overig  terrein identificatie  -  verwijderd omdat pk van tabel wordt gebruikt hiervoor</xsl:text>
		</xsl:comment>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_4DF562B2_713D_4fdd_A381_D2523F72C99B" name="Nummer ander buitenlands niet-natuurlijk persoon" -->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_4DF562B2_713D_4fdd_A381_D2523F72C99B']">
		<xsl:comment>
			<xsl:text>repair.xsl: Nummer ander buitenlands niet-natuurlijk persoon  -  verwijderd omdat pk van tabel wordt gebruikt hiervoor</xsl:text>
		</xsl:comment>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_8C394BCE_D0B2_4d3a_95F2_654E80A702E5" name="Ligplaatsidentificatie"-->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_8C394BCE_D0B2_4d3a_95F2_654E80A702E5']">
		<xsl:comment>
			<xsl:text>repair.xsl: Ligplaatsidentificatie  -  verwijderd omdat pk van tabel wordt gebruikt hiervoor</xsl:text>
		</xsl:comment>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_A037EC08_F127_4278_8979_49778229089C" name="Identificatiecode overig adresseerbaar object aanduiding"-->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_A037EC08_F127_4278_8979_49778229089C']">
		<xsl:comment>
			<xsl:text>repair.xsl:  Identificatiecode overig adresseerbaar object aanduiding -  verwijderd omdat pk van tabel wordt gebruikt hiervoor</xsl:text>
		</xsl:comment>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_B7C10692_7174_427f_B65D_225D49FF3CAD" name="Identificatiecode nummeraanduiding"-->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_B7C10692_7174_427f_B65D_225D49FF3CAD']">
		<xsl:comment>
			<xsl:text>repair.xsl: Identificatiecode nummeraanduiding  -  verwijderd omdat pk van tabel wordt gebruikt hiervoor</xsl:text>
		</xsl:comment>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_D7CD3FFA_2CDA_45ac_B5CF_4618178036C5" name="Nummer ander natuurlijk persoon"-->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_D7CD3FFA_2CDA_45ac_B5CF_4618178036C5']">
		<xsl:comment>
			<xsl:text>repair.xsl: Nummer ander natuurlijk persoon  -  verwijderd omdat pk van tabel wordt gebruikt hiervoor</xsl:text>
		</xsl:comment>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_EC83BE4D_783E_40c3_9F26_65E1A83096E3" name="Verblijfsobjectidentificatie"-->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_EC83BE4D_783E_40c3_9F26_65E1A83096E3']">
		<xsl:comment>
			<xsl:text>repair.xsl:  Verblijfsobjectidentificatie -  verwijderd omdat pk van tabel wordt gebruikt hiervoor</xsl:text>
		</xsl:comment>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_FF4FFB65_1D5D_4e01_B60C_42CAF1912B98" name="Vestigingsnummer"-->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_FF4FFB65_1D5D_4e01_B60C_42CAF1912B98']">
		<xsl:comment>
			<xsl:text>repair.xsl: Vestigingsnummer  -  verwijderd omdat pk van tabel wordt gebruikt hiervoor</xsl:text>
		</xsl:comment>
	</xsl:template>
	<!-- Property elementType="Objecttype - Property" id="EAID_dst1F44A8_B55B_40e6_A5CB_CC8555D9635D" name="buurt" -->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_dst1F44A8_B55B_40e6_A5CB_CC8555D9635D']">
		<xsl:comment>
			<xsl:text>repair.xsl: buurt  -  verwijderd</xsl:text>
		</xsl:comment>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_dst5BF648_C231_4744_ADA9_63521891E583" name="buurt"  -->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_dst5BF648_C231_4744_ADA9_63521891E583']">
		<xsl:comment>
			<xsl:text>repair.xsl: buurt  -  verwijderd</xsl:text>
		</xsl:comment>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_dst2B5B2D_C729_4156_B828_8DF93BB553AE" name="wijk"  -->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_dst2B5B2D_C729_4156_B828_8DF93BB553AE']">
		<xsl:comment>
			<xsl:text>repair.xsl: wijk  -  verwijderd</xsl:text>
		</xsl:comment>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_dst17F437_70A0_4c93_B04F_628EE7EDDB2D" name="gemeente" -->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_dst17F437_70A0_4c93_B04F_628EE7EDDB2D']">
		<xsl:comment>
			<xsl:text>repair.xsl: gemeente  -  verwijderd</xsl:text>
		</xsl:comment>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_dst56AD9F_136A_485a_93B1_7137FCC39416"-->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_dst56AD9F_136A_485a_93B1_7137FCC39416']">
		<xsl:comment>
			<xsl:text>repair.xsl: EAID_dst56AD9F_136A_485a_93B1_7137FCC39416 N-N relatie verplaatst van BENOEMD OBJECT naar BENOEMD TERREIN, omdat hier archief pk wel in zit</xsl:text>
		</xsl:comment>
	</xsl:template>
	<xsl:template match="Class[@id='EAID_73F1C4A1_481E_4052_A372_1A0895ED1295']">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
			<xsl:comment>
				<xsl:text>repair.xsl: EAID_dst56AD9F_136A_485a_93B1_7137FCC39416 N-N relatie verplaatst van BENOEMD OBJECT naar BENOEMD TERREIN, omdat hier archief pk wel in zit</xsl:text>
			</xsl:comment>
			<Property elementType="Objecttype - Property" id="EAID_dst56AD9F_136A_485a_93B1_7137FCC39416" name="" lowerValueType="uml:LiteralInteger" lowerValueValue="0" upperValueType="uml:LiteralUnlimitedNatural" upperValueValue="*" type="EAID_73F1C4A1_481E_4052_A372_1A0895ED1295" typeName="BENOEMD TERREIN" visibility="private" association="EAID_9A56AD9F_136A_485a_93B1_7137FCC39416" associationName="is ontstaan uit / overgegaan in"/>
		</xsl:copy>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_EEFE0F7E_DBED_4be9_B092_CFB8142FA6BE" name="Kadaster identificatie aantekening" type="EAJava_AN15" typeName="AN255" -->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_EEFE0F7E_DBED_4be9_B092_CFB8142FA6BE']">
		<xsl:comment>
			<xsl:text>repair.xsl: Kadaster identificatie aantekening -  kolom vergroot van 15 naar 255</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="typeName" select="'AN255'"/>
			<xsl:apply-templates select="@*[name()!='typeName']|node()"/>
		</xsl:copy>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_BB239C52_91CA_40b6_A253_819CCD5FE245" name="Naam aanschrijving" type="EAJava_Naam_aanschrijving_NATUURLIJK_PERSOON" typeName="Naam aanschrijving NATUURLIJK PERSOON"  -->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_BB239C52_91CA_40b6_A253_819CCD5FE245']">
		<xsl:comment>
			<xsl:text>repair.xsl: Naam aanschrijving  - type aangepast</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="type" select="'EAID_02601AC0_9F85_47e3_82AF_7E8D860559F7'"/>
			<xsl:apply-templates select="@*[name()!='type']|node()"/>
		</xsl:copy>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_103F3EF8_972C_403e_A83F_B4B9BF7DA6DB" name="Identificatie" type="EAJava_AN17" typeName="AN32" -->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_103F3EF8_972C_403e_A83F_B4B9BF7DA6DB']">
		<xsl:comment>
			<xsl:text>repair.xsl: Identificatie -  kolom vergroot van 17 naar 32</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="typeName" select="'AN32'"/>
			<xsl:apply-templates select="@*[name()!='typeName']|node()"/>
		</xsl:copy>
	</xsl:template>
	<!--Property elementType="Objecttype - Association" id="EAID_9A56AD9F_136A_485a_93B1_7137FCC39416" name="is ontstaan uit / overgegaan in" type="EAID_3E9E08B7_574E_43dc_8D93_251BFA8F8D75" typeName="BENOEMD OBJECT"/-->
	<xsl:template match="*[@elementType='Objecttype - Association' and @id='EAID_9A56AD9F_136A_485a_93B1_7137FCC39416']">
		<xsl:comment>
			<xsl:text>repair.xsl: s ontstaan uit / overgegaan in -  omgezet van BENOEMD OBJECT naar BENOEMD TERREIN</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="typeName" select="'BENOEMD TERREIN'"/>
			<xsl:apply-templates select="@*[name()!='typeName']|node()"/>
		</xsl:copy>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_1DEA625F_DC14_439e_A5A7_32AB34C5EE39" name="Verblijf buitenland" type="EAJava_Verblijf_buitenland" typeName="Verblijf buitenland"  -->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_1DEA625F_DC14_439e_A5A7_32AB34C5EE39']">
		<xsl:comment>
			<xsl:text>repair.xsl: Verblijf buitenland  - type aangepast</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="type" select="'EAID_E3567219_5744_4b70_901D_BAEBD8FF4947'"/>
			<xsl:apply-templates select="@*[name()!='type']|node()"/>
		</xsl:copy>
	</xsl:template>
	<!--Property elementType="Objecttype/Groepattribuutsoort - Property" id="EAID_AA4BCDEE_1E76_4853_AFB1_06F7571CDA95" name="Locatie beschrijving" type="EAJava_AN35" typeName="AN255" -->
	<xsl:template match="*[@elementType='Objecttype/Groepattribuutsoort - Property' and @id='EAID_AA4BCDEE_1E76_4853_AFB1_06F7571CDA95']">
		<xsl:comment>
			<xsl:text>repair.xsl: Locatie beschrijving -  kolom vergroot van 35 naar 255</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="typeName" select="'AN255'"/>
			<xsl:apply-templates select="@*[name()!='typeName']|node()"/>
		</xsl:copy>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_FD278EEF_C833_4de3_A369_14F5310E9764" name="Wijkcode" type="EAJava_N2" typeName="N2"/-->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_FD278EEF_C833_4de3_A369_14F5310E9764']">
		<xsl:comment>
			<xsl:text>repair.xsl: Wijkcode -  kolom vergroot van 2 naar 6</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="typeName" select="'N6'"/>
			<xsl:apply-templates select="@*[name()!='typeName']|node()"/>
		</xsl:copy>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_7DEE95C6_32D0_4f59_8144_62AA14014261" name="Wijknaam" type="EAJava_AN40" typeName="AN40"/-->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_7DEE95C6_32D0_4f59_8144_62AA14014261']">
		<xsl:comment>
			<xsl:text>repair.xsl: Wijknaam -  kolom vergroot van 40 naar 80</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="typeName" select="'AN80'"/>
			<xsl:apply-templates select="@*[name()!='typeName']|node()"/>
		</xsl:copy>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_F0191663_C96D_45aa_8F58_78CF4C280F29" name="Buurtcode" type="EAJava_N2" typeName="N2"/-->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_F0191663_C96D_45aa_8F58_78CF4C280F29']">
		<xsl:comment>
			<xsl:text>repair.xsl: Buurtcode -  kolom vergroot van 2 naar 8</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="typeName" select="'N8'"/>
			<xsl:apply-templates select="@*[name()!='typeName']|node()"/>
		</xsl:copy>
	</xsl:template>
	<!--Property elementType="Objecttype - Property" id="EAID_824CBA3D_D469_49c8_949C_FFB30EF57C86" name="Buurtnaam" type="EAJava_AN40" typeName="AN40"/-->
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_824CBA3D_D469_49c8_949C_FFB30EF57C86']">
		<xsl:comment>
			<xsl:text>repair.xsl: Buurtnaam -  kolom vergroot van 40 naar 80</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="typeName" select="'AN80'"/>
			<xsl:apply-templates select="@*[name()!='typeName']|node()"/>
		</xsl:copy>
	</xsl:template>
	<!--Property elementType="Objecttype - Association" id="EAID_0D466716_6612_4da4_80E0_09D64C1AA317" name="heeft als nevenadressen" upperValueValue="1" -->
	<xsl:template match="*[@elementType='Objecttype - Association' and @id='EAID_0D466716_6612_4da4_80E0_09D64C1AA317']">
		<xsl:comment>
			<xsl:text>repair.xsl: heeft als nevenadressen -  many</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="upperValueValue" select="'*'"/>
			<xsl:apply-templates select="@*[name()!='upperValueValue']|node()"/>
		</xsl:copy>
	</xsl:template>
	<!--Property elementType="Objecttype - Association" id="EAID_7536DECC_F165_4e1f_8CDC_01C980CE79F8" name="heeft als nevenadres(sen)" upperValueValue="1" -->
	<xsl:template match="*[@elementType='Objecttype - Association' and @id='EAID_7536DECC_F165_4e1f_8CDC_01C980CE79F8']">
		<xsl:comment>
			<xsl:text>repair.xsl: heeft als nevenadressen -  many</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="upperValueValue" select="'*'"/>
			<xsl:apply-templates select="@*[name()!='upperValueValue']|node()"/>
		</xsl:copy>
	</xsl:template>
	<!--Property elementType="Objecttype - Association" id="EAID_9FC0B053_CBDE_4618_9502_C659CADEAA93" name="heeft als nevenadressen" upperValueValue="1"  -->
	<xsl:template match="*[@elementType='Objecttype - Association' and @id='EAID_9FC0B053_CBDE_4618_9502_C659CADEAA93']">
		<xsl:comment>
			<xsl:text>repair.xsl: heeft als nevenadressen -  many</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="upperValueValue" select="'*'"/>
			<xsl:apply-templates select="@*[name()!='upperValueValue']|node()"/>
		</xsl:copy>
	</xsl:template>
	<!-- Property elementType="Objecttype/Groepattribuutsoort - Property" id="EAID_584B0B4E_6AEA_469a_95F6_840132CEDA08" name="Locatie- omschrijving" type="EAJava_AN40" typeName="AN40" -->
	<xsl:template match="*[@elementType='Objecttype/Groepattribuutsoort - Property' and @id='EAID_584B0B4E_6AEA_469a_95F6_840132CEDA08']">
		<xsl:comment>
			<xsl:text>repair.xsl: Locatie- omschrijving -  kolom vergroot van 40 naar 255</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="typeName" select="'AN255'"/>
			<xsl:apply-templates select="@*[name()!='typeName']|node()"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_531227ED_1F06_47ad_8585_15EA78553E5C']">
		<xsl:comment>
			<xsl:text>repair.xsl: Aanduiding soort grootte - type aangepast van boolean naar string(2), zie: https://github.com/B3Partners/brmo/issues/565</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="type" select="'EAJava_AN2'"/>
			<xsl:attribute name="typeName" select="'AN2'"/>
			<xsl:apply-templates select="@*[name()!='typeName']|node()"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_31CC2AB2_A6D1_4a6e_B452_EEC92B1E2EEB']">
		<xsl:comment>
			<xsl:text>repair.xsl: Adres buitenland -  kolom vergroot van 149 naar 500</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="typeName" select="'AN500'"/>
			<xsl:apply-templates select="@*[name()!='typeName']|node()"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_53F98DF1_FDCA_471d_9729_A082B4D943C9']">
		<xsl:comment>
			<xsl:text>repair.xsl: Nummer WOZ-deelobject - kolom vergroot van 6 naar 12</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="typeName" select="'N12'"/>
			<xsl:apply-templates select="@*[name()!='typeName']|node()"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_5CDB5EA8_D0DE_45df_B5DE_B49C6E48859D']">
		<xsl:comment>
			<xsl:text>repair.xsl: datum begin geldigheid WOZ-deelobject - omgezet naar onvolledige datum</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="type" select="'xmi:idref=EAJava_OnvolledigeDatum'"/>
			<xsl:attribute name="typeName" select="'OnvolledigeDatum'"/>
			<xsl:apply-templates select="@*[name()!='typeName']|node()"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_D3873A5F_CA86_4689_B2F0_A8309A2572E3']">
		<xsl:comment>
			<xsl:text>repair.xsl: datum einde geldigheid WOZ-deelobject - omgezet naar onvolledige datum</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="type" select="'xmi:idref=EAJava_OnvolledigeDatum'"/>
			<xsl:attribute name="typeName" select="'OnvolledigeDatum'"/>
			<xsl:apply-templates select="@*[name()!='typeName']|node()"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="*[@elementType='Objecttype - Property' and @id='EAID_A79988B8_DFD6_49c8_9996_A22579B02F5C']">
		<xsl:comment>
			<xsl:text>repair.xsl: Ingeschreven niet-natuurlijk persoon: vergroot ruimte publiekrechtelijke rechtsvorm</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="type" select="'EAJava_AN60'"/>
			<xsl:attribute name="typeName" select="'AN60'"/>
			<xsl:apply-templates select="@*[name()!='typeName']|node()"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="*[@elementType='Objecttype/Relatieklasse - Property' and @id='EAID_3E00C0F2_1C7E_46d1_A04C_A43820E37FC1']">
		<xsl:comment>
			<xsl:text>repair.xsl: Functionaris: soort bevoegdheid: kolom vergroot naar 50</xsl:text>
		</xsl:comment>
		<xsl:copy>
			<xsl:attribute name="type" select="'EANone_AN50'"/>
			<xsl:attribute name="typeName" select="'AN50'"/>
			<xsl:apply-templates select="@*[name()!='typeName']|node()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
