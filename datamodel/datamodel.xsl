<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
 xmlns:b3p="http://www.b3partners.nl/" xmlns:fn="http://www.w3.org/2005/xpath-functions">
	
	<!-- Zet het enterprise architect model (dat getransformeerd is door rsgbsst2.xsl) om
         naar een databasemodel in XML formaat (tabellen, kolommen, keys, etc). 

         Er worden nog geen database-specifieke types gebruikt.
    -->

	<xsl:import href="identifiers.xsl"/>
	<xsl:import href="datamodel_types.xsl"/>	
	<xsl:import href="keys_types.xsl"/>

	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
	
	<xsl:variable name="rsgb" select="/RSGB"/>
	
	<xsl:variable name="default-sql-type" select="'int'"/>
		
	<xsl:template match="/RSGB/Objecttypes">
		<schema>
			<xsl:call-template name="create-reference-list-tables"/>
			<xsl:apply-templates select="Class" />
			<xsl:call-template name="process-simple-multi"/>
			<xsl:call-template name="objectype-superclass"/>
			<xsl:call-template name="multi-association"/>
			<xsl:call-template name="process-enumeraties"/>
			<xsl:call-template name="process-referentielijsten"/>

			<extra-scripts>
				<script>101_herkomst_metadata.sql</script>
				<script>102_metagegevens_brondocument.sql</script>
				<script>103_woz_waarde.sql</script>
				<script>104_brondocument_indices.sql</script>
				<script>105_appartements_rechten.sql</script>
				<script>106_bag_views.sql</script>
				<script>107_brk_views.sql</script>
				<script>108_insert_aard_recht_verkort.sql</script>
				<script>109_insert_aard_verkregen_recht.sql</script>
				<script>110_gebruiksdoel_primary_key.sql</script>
				<script>111_insert_gemeente.sql</script>
				<script>112_insert_buurt.sql</script>
				<script>113_insert_wijk.sql</script>
				<script>114_drop_constraints.sql</script>
				<script>115_nhr.sql</script>
				<script>116_brk_extra_indices.sql</script>
				<script>117_versienummer.sql</script>
				<script>118_drop_constraints_brp.sql</script>
				<script>119_reisdocumentsoort.sql</script>
				<script>120_increase_identifsize.sql</script>
				<script>121_insert_nation.sql</script>
			</extra-scripts>
		</schema>
	</xsl:template>
	
	<xsl:template match="Class">
		<xsl:variable name="class-name" select="@name"/>
		<xsl:variable name="table" select="b3p:class-to-table(@name)"/>
		
		<xsl:variable name="key-properties">
			<xsl:call-template name="get-class-primary-key-properties"/>
		</xsl:variable>
		
		<xsl:variable name="superclass-keys">
			<xsl:call-template name="get-class-primary-key-properties-hierarchy">
				<xsl:with-param name="name" select="@superclass"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="hierarchy-desc">
			<xsl:if test="$superclass-keys/Class">
				<xsl:text>. Subclass van: </xsl:text><xsl:value-of select="fn:string-join($superclass-keys/Class/@name,' -> ')"/>
			</xsl:if>
		</xsl:variable>
		<xsl:variable name="subclasses" select="$rsgb/Objecttypes/Class[@superclassId = current()/@id]"/>
		<xsl:variable name="subclass-desc">
			<xsl:if test="$subclasses">
			 <xsl:text>. Directe superclass van: </xsl:text><xsl:value-of select="fn:string-join($subclasses/@name,', ')"/>
			</xsl:if>
		</xsl:variable>

		<table name="{$table}" desc="{concat('RSGB class ',@name,$hierarchy-desc,$subclass-desc)}" fullname="{@name}">
			<xsl:comment> Primary key(s) </xsl:comment>
			<xsl:for-each select="$key-properties/Property">
				<xsl:choose>
					<xsl:when test="b3p:getElementType(.) = 'simple'">
						<column name="{b3p:property-to-column(@name,$class-name)}" fullname="{@name}" type="{b3p:get-column-type(@type,@typeName)}" key="true" desc="[PK] {@typeName}">
							<xsl:if test="@archief = 'true'">
								<xsl:attribute name="archief" select="'true'"/>
							</xsl:if>
						</column>
					</xsl:when>
					<xsl:when test="b3p:getElementType(.) = 'association'">
						<xsl:variable name="association-result">
							<xsl:call-template name="association">
								<xsl:with-param name="class-name" select="$class-name"/>
								<xsl:with-param name="position" select="position()"/>
							</xsl:call-template>
						</xsl:variable>
						<xsl:for-each select="$association-result/column">
							<column key="true" desc="{concat('[PK] ', @desc)}">
								<xsl:copy-of select="@*[local-name() != 'desc']"/>
							</column>
						</xsl:for-each>
						<xsl:copy-of select="$association-result/foreign-key"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:message terminate="yes">Type van primary key property &quot;<xsl:value-of select="@name"/> niet ondersteund</xsl:message>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>	
			<xsl:if test="@superclass">
				<xsl:call-template name="superclass-reference" />
			</xsl:if>
			<xsl:if test="$subclasses">
				<xsl:comment> Aanduiding subclass </xsl:comment>
				<column name="clazz" type="varchar(255)" desc="Aanduiding subclass"/>
			</xsl:if>		

			<xsl:variable name="non-key-properties">
				<xsl:for-each select="Property">
					<xsl:if test="not($key-properties/Property[@name = current()/@name])">
						<xsl:copy-of select="."/>
					</xsl:if>
				</xsl:for-each>
			</xsl:variable>
			<!--non-key-properties>
				<xsl:copy-of select="$non-key-properties"/>
			</non-key-properties-->
			
			<xsl:comment> Simple properties </xsl:comment>
			<xsl:for-each select="$non-key-properties/Property">
				<xsl:if test="b3p:getElementType(.) = 'simple' or b3p:getElementType(.) = 'Enumeratiesoort - Values'">
					<xsl:call-template name="simple-property">
						<xsl:with-param name="class-name" select="$class-name"/>
					</xsl:call-template>
				</xsl:if>
			</xsl:for-each>
			
			<xsl:comment> Association properties 1 - * </xsl:comment>
			<xsl:for-each select="$non-key-properties/Property">
				<xsl:if test="b3p:getElementType(.) = 'association'">
					<xsl:call-template name="association">
						<xsl:with-param name="class-name" select="$class-name"/>
						<xsl:with-param name="position" select="position()"/>
					</xsl:call-template>
				</xsl:if>
			</xsl:for-each>
			
			<xsl:comment> Association properties * - 1 </xsl:comment>
			<xsl:call-template name="refenced-as-property"/>					
							
			<xsl:comment> Groepsattributen </xsl:comment>
				<xsl:for-each select="$non-key-properties/Property">
				<xsl:if test="b3p:getElementType(.) = 'Objecttype/Groepattribuutsoort - Property'">
					<xsl:call-template name="group-attributes">
						<xsl:with-param name="class-name" select="$class-name"/>
					</xsl:call-template>
				</xsl:if>
			</xsl:for-each>
			
			<xsl:comment> Referentielijsten </xsl:comment>
			<xsl:for-each select="$non-key-properties/Property">
				<xsl:if test="b3p:getElementType(.) = 'Objecttype/Referentielijsten - Property'">
					<xsl:call-template name="reference-list-property">
						<xsl:with-param name="fk-name" select="concat('fk_',b3p:get-class-mnemonic($class-name),'_rl_',position())"/>
						<xsl:with-param name="association-name" select="concat('Referentielijst ',$class-name,'.',@name)"/>						
						<xsl:with-param name="column-uniquifier" select="position()"/>
					</xsl:call-template>
				</xsl:if>
			</xsl:for-each>
		</table>
	</xsl:template>

	<!-- Context: RSGB/Objecttypes/Class/Property -->
	<!-- Maakt XML DDL voor een simpele property (geen associatie) -->
	<xsl:template name="simple-property">
		<xsl:param name="key-properties"/>
		<xsl:param name="class-name"/>
				
		<xsl:choose>
			<xsl:when test="b3p:getElementType(.) = 'Enumeratiesoort - Values'">
				<column name="{b3p:property-to-column(@name,$class-name)}" fullname="{@name}" type="varchar(255)" desc="[Enumeratie]"/>
			</xsl:when>
			<xsl:when test="not(@typeName)">
				<column name="{b3p:property-to-column(@name,$class-name)}" fullname="{@name}" type="varchar(255)" desc="[geen RSGB type]"/>
			</xsl:when>
			<xsl:otherwise>
				<column name="{b3p:property-to-column(@name,$class-name)}" fullname="{@name}" type="{b3p:get-column-type(@type,@typeName)}" desc="{@typeName}"/>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>	
	
	<!-- Context: Class[@superclass] -->
	<!-- Maak de referentie naar de @superclass -->
	<xsl:template name="superclass-reference">
		<xsl:variable name="class-name" select="@name"/>
		<xsl:variable name="superclass" select="@superclass"/>
		
		<xsl:variable name="superclass-primary-keys">
			<xsl:call-template name="get-class-primary-key-properties-hierarchy">
				<xsl:with-param name="name" select="@superclass"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:for-each select="$superclass-primary-keys/Class[Property]">
			<xsl:variable name="foreign-key-position" select="position()"/>
			<xsl:for-each select="Property">
				<column superclass-archief="{@archief}" name="{concat('sc_',b3p:property-to-column(@name,../@name))}" fullname="{@name}" type="{b3p:get-column-type(@type,@typeName)}" key="true" desc="{concat('[PK] ',@typeName,', FK naar ',b3p:class-to-table($superclass),'.',@column-name)}"/>
			</xsl:for-each>
		</xsl:for-each>
		<xsl:variable name="fk">
			<xsl:for-each select="$superclass-primary-keys/Class/Property[not(@archief='true')]">
				<column>sc_<xsl:value-of select="b3p:property-to-column(@name,../@name)"/></column>
				<ref-column><xsl:value-of select="@column-name"/></ref-column>
			</xsl:for-each>
		</xsl:variable>
		<foreign-key name="{concat('fk_',b3p:get-class-mnemonic($class-name),'_sc')}"
			columns="{fn:string-join($fk/column,',')}" desc="Foreign key naar superclass" ref-table="{b3p:class-to-table(@superclass)}" ref-columns="{fn:string-join($fk/ref-column,',')}" />		
	</xsl:template>	
	
		
	<!-- Context: RSGB/Objecttypes/Class -->
	<!-- Maak de foreign key(s) voor deze class voor die Properties die deze class refereren als type, waarvoor een * - 1 geld -->
	<xsl:template name="refenced-as-property">
		<xsl:variable name="class" select="."/>
		<!-- Selecteer alle properties die naar de huidige class refereren en een upperValueValue van * hebben (om de * kant v/d relatie te definiëren) -->
		<!-- De informatie onder /RSGB/Objecttypes/Property moet 1 zijn -->
		<xsl:for-each select="$rsgb/Objecttypes/Class/Property[@type=$class/@id and @upperValueValue ='*' 
									and $rsgb/Objecttypes/Property[@upperValueValue = '1']/@id = @association]">	
			<xsl:call-template name="create-foreign-key">
				<xsl:with-param name="classname" select="../@name"/>
				<xsl:with-param name="fk-name" select="concat('fk_',b3p:get-class-mnemonic($class/@name),'_1n_',position())"/>
				<xsl:with-param name="column-uniquifier" select="position()"/>
			</xsl:call-template>
		</xsl:for-each>
	</xsl:template>
	
	<!-- Context: /RSGB/Objecttypes/Class/Property -->
	<!-- Template om bij een property waarvan het type een andere /RSGB/Objecttypes/Class is de relatie te leggen. -->
	<xsl:template name="association">
		<xsl:param name="class-name"/>
		<xsl:param name="position"/>
		
		<xsl:variable name="prop" select="."/>
		<xsl:for-each select="$rsgb/Objecttypes/Class[@id = $prop/@type]">
			
			<xsl:variable name="name" select="@name"/>	
			<!-- informatie over de referentie: cardinaliteit -->
			<xsl:variable name="reference-information" select="/RSGB/Objecttypes/Property[@elementType='Objecttype - Association' and @id=$prop/@association]"/>
					
			<!-- Bekijk op basis van de cardinaliteit wat er gedaan moet worden: als de refererende tabel een refererende tabel hoogstens 1 keer bij de gerefereerde tabel hoort, -->
			<!-- dan kan er een foreign key naar de gerefereerde tabel -->
			<xsl:if test="not($reference-information)">
				<xsl:if test="not($rsgb/Objecttypes/Property[@elementType='Objecttype - Superklasse' and @classId = $prop/@association])">
					<association-error>Geen association info en geen superklasse!</association-error>
				</xsl:if>
			</xsl:if>
			<xsl:if test="$reference-information and $prop/@upperValueValue ='1'">
				<xsl:call-template name="create-foreign-key">
					<xsl:with-param name="classname" select="$name"/>
					<xsl:with-param name="fk-name" select="concat('fk_',b3p:get-class-mnemonic($class-name),'_as_',$position)"/>
					<xsl:with-param name="column-uniquifier" select="$position"/>
					<xsl:with-param name="association-name" select="$prop/@associationName"/>
				</xsl:call-template>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	

	<!-- Context /RSGB/Objecttypes/Class/Property -->
	<!-- Template om de groepsattributen op te halen van dit property -->
	<xsl:template name="group-attributes">
		<xsl:param name="class-name"/>
		<xsl:variable name="prop" select="."/>
		<xsl:variable name="prefix" select="b3p:get-groupattribute-abbreviation($prop/@name)"/> 
		<xsl:for-each select="$rsgb/Objecttypes/Property[@classId = $prop/@type]">
			<xsl:choose>
				<xsl:when test="b3p:getElementType(.) = 'Objecttype/Referentielijsten - Property'">
					<xsl:call-template name="reference-list-property">
						<xsl:with-param name="fk-name" select="concat('fk_',b3p:get-class-mnemonic($class-name),'_',$prefix,'_',position())"/>
						<xsl:with-param name="association-name" select="concat('Groepsattribuut referentielijst ',@name)"/>
						<xsl:with-param name="column-uniquifier" select="concat($prefix,'_')"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="b3p:getElementType(.) = 'association'">
					<!--groepsattribuut-associatie>
						<xsl:copy-of select="."/>
					</groepsattribuut-associatie-->
					<!-- Er is geen Property met elementType 'Objecttype - Association' voor associaties van groepsattributen,
                         dus roep direct create-foreign-key aan -->
					<xsl:call-template name="create-foreign-key">
						<xsl:with-param name="classname" select="@typeName"/>
						<xsl:with-param name="fk-name" select="concat('fk_',b3p:get-class-mnemonic($class-name),'_',$prefix,'_as_',position())"/>
						<xsl:with-param name="column-uniquifier" select="concat($prefix,'_',position(),'_')"/>
						<xsl:with-param name="association-name" select="concat('Groepsattribuut ',@className,'.',@name)"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<column name="{concat($prefix,'_',b3p:property-to-column(@name,@className))}" fullname="{@name}" type="{b3p:get-column-type(@type,@typeName)}" desc="Groepsattribuut {@className}.{@name}"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	
	<!-- Context: /RSGB/Objecttypes/Class -->
	<!-- Maak de associatie voor een meer-op-meer relatie of een meer-op-een relatie -->
	<xsl:template name="multi-association">
		
		<xsl:for-each select="Class/Property[@upperValueValue = '*' 
								and @type = $rsgb/Objecttypes/Class/@id ]">
			<xsl:variable name="prop" select="."/>
			<xsl:variable name="referencee-class" select=".."/>
			<xsl:variable name="referencee-classname" select="../@name"/>
			
			<xsl:variable name="reference-information" select="$rsgb/Objecttypes/Property[@id = $prop/@association]"/>
			<xsl:variable name="referenced-class" select="$rsgb/Objecttypes/Class[@id=$prop/@type]"/>
			<xsl:variable name="referenced-classname" select="$referenced-class/@name"/>
			
			<xsl:if test="$reference-information">
				<xsl:choose>
					<xsl:when test="$reference-information/@upperValueValue = '1'">
						<xsl:comment> * - 1: <xsl:value-of select="$referencee-class/@name"/> - <xsl:value-of select="$referenced-class/@name"/> </xsl:comment>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="tablename" select="fn:substring(concat(b3p:class-to-table($referencee-classname),'_', b3p:class-to-table($referenced-classname)),1,30)"/>
						<!-- Als er in reference-information niks staat, is het een referentie naar zichzelf (en dus ook een * - * -->
							<!-- Referenties naar zichzelf. -->
						<xsl:comment> Een * op * relatie </xsl:comment>
						<table name="{$tablename}" desc="N - N relatie: {$referencee-classname} &quot;{$reference-information/@name}&quot; {$referenced-classname}" fullname="{@name}">
	
							<xsl:comment> Left hand side of association </xsl:comment>
							<xsl:variable name="foreign-key-lh">
								<xsl:call-template name="create-foreign-key">
									<xsl:with-param name="classname" select="$referencee-classname"/>
									<xsl:with-param name="column-uniquifier" select="'nn_lh_'"/>
									<xsl:with-param name="fk-name" select="concat('fk_',b3p:get-class-mnemonic($referencee-classname),'_',b3p:get-class-mnemonic($referenced-classname),'_nn_lh')"/> 
									<xsl:with-param name="include-archive-version" select="true()"/>
								</xsl:call-template>
							</xsl:variable>
							<xsl:for-each select="$foreign-key-lh/column">
								<column key="true">
									<xsl:copy-of select="@*"/>
								</column>
							</xsl:for-each>
							<xsl:copy-of select="$foreign-key-lh/*[local-name() != 'column']"/>
							
							<xsl:comment> Right hand side of association </xsl:comment>
							<xsl:variable name="foreign-key-rh">
								<xsl:call-template name="create-foreign-key">
									<xsl:with-param name="classname" select="$referenced-classname"/>
									<xsl:with-param name="column-uniquifier" select="'nn_rh_'"/>
									<xsl:with-param name="fk-name" select="concat('fk_',b3p:get-class-mnemonic($referencee-classname),'_',b3p:get-class-mnemonic($referenced-classname),'_nn_rh')"/> 
								</xsl:call-template>
							</xsl:variable>
							<xsl:for-each select="$foreign-key-rh/column">
								<column key="true">
									<xsl:copy-of select="@*"/>
								</column>
							</xsl:for-each>
							<xsl:copy-of select="$foreign-key-rh/*[local-name() != 'column']"/>
						</table>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
		</xsl:for-each>
		
	</xsl:template>
	
	<!-- Context: none -->
	<!-- Maak de tabellen die gedefinieerd staan onder /RSGB/Objecttypes/Property[@elementType = 'Objecttype - Superklasse'] -->
	<!-- Dit zijn de classes: FUNCTIONARIS, HUISHOUDENRELATIE, HUWERLIJK/GEREGISTREERD PARTNERSCHAP, KADASTRALE ONROERENDE ZAAK HISTORIE RELATIE,	
	LCOATIEAANDUIDING ADRES, LOCATIEAANDUIDING OPENBARE RUIMTE, OUDE-KIND-RELATIE, WOZ-BELANG -->
	<xsl:template name="objectype-superclass">
		<xsl:for-each select="$rsgb/Objecttypes/Property[@elementType = 'Objecttype - Superklasse']">
			<xsl:variable name="superclass" select="."/>
			<xsl:variable name="superclass-name" select="$superclass/@className"/>
			
			<table name="{b3p:class-to-table($superclass-name)}" desc="{concat('RSGB superclass ',$superclass-name)}" fullname="{$superclass-name}">
				<xsl:comment> De foreign keys naar de classes. </xsl:comment>
				<xsl:variable name="base-property" select="$rsgb/Objecttypes/Class/Property[@association = $superclass/@classId]"/>
				<xsl:variable name="referencing" select="$rsgb/Objecttypes/Class[@id = $base-property/@type]/@name"/>
				<xsl:variable name="referencing-name" select="$base-property/@name"/>
				<xsl:variable name="references" select="$base-property/../@name"/>
				
				<xsl:comment>Beschrijving: <xsl:value-of select="$referencing-name"/> </xsl:comment>
				<xsl:variable name="foreign-keys-results">
					<xsl:call-template name="create-foreign-key">
						<xsl:with-param name="classname" select="$referencing"/>
						<xsl:with-param name="fk-name" select="concat('fk_',b3p:class-to-table($superclass-name),'_sc_lh')"/>
						<xsl:with-param name="column-uniquifier" select="'sc_lh_'"/>
					</xsl:call-template>
				
					<xsl:call-template name="create-foreign-key">
						<xsl:with-param name="classname" select="$references"/>
						<xsl:with-param name="fk-name" select="concat('fk_',b3p:class-to-table($superclass-name),'_sc_rh')"/>
						<xsl:with-param name="column-uniquifier" select="'sc_rh_'"/>
					</xsl:call-template>
				</xsl:variable>
				<!-- Voeg key="true" toe aan de kolommen voor de foreign keys -->
				<xsl:for-each select="$foreign-keys-results/column">
					<column key="true">
						<xsl:copy-of select="@*"/>
					</column>
				</xsl:for-each>
				<xsl:copy-of select="$foreign-keys-results/*[local-name() != 'column']"/>
				
				<xsl:comment> De gewone kolommen </xsl:comment>					
				<xsl:for-each select="$rsgb/Objecttypes/Property[@elementType='Objecttype/Relatieklasse - Property' and @classId = $superclass/@classId]" >
					<xsl:choose>
						<xsl:when test="b3p:getElementType(.) = 'Objecttype/Groepattribuutsoort - Property'">
							<xsl:call-template name="group-attributes">
								<xsl:with-param name="class-name" select="$superclass-name"/>
							</xsl:call-template>							
						</xsl:when>
						<xsl:otherwise>
							<column name="{b3p:property-to-column(@name,@className)}" fullname="{@name}" type="{b3p:get-column-type(@type,@typeName)}" desc="{@typeName}"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>
			</table>
		</xsl:for-each>
	</xsl:template>
	
	<!-- Context: /RSGB/Objecttypes/Class/Property -->
	<!-- Maak de foreign key naar de referentielijst tabel -->	
	<xsl:template name="reference-list-property">
		<xsl:param name="fk-name"/>
		<xsl:param name="association-name"/>						
		<xsl:param name="column-uniquifier"/>
							
		<xsl:variable name="prop" select="."/>
		<xsl:variable name="reference-list-classname" select="$rsgb/Objecttypes/Property[@elementType= 'Objecttype/Referentielijsten - Property' and @classId = $prop/@type][1]/@className"/>
		
		<xsl:call-template name="create-foreign-key">
			<xsl:with-param name="classname" select="$reference-list-classname"/>
			<xsl:with-param name="fk-name" select="$fk-name"/>	
			<xsl:with-param name="column-uniquifier" select="$column-uniquifier"/>
			<xsl:with-param name="association-name" select="$association-name"/>
		</xsl:call-template>
	</xsl:template>
	
	<!-- Context: none -->
	<!-- Maak de foreign key(s) obv de classname en de fk-name -->
	<!-- param name: classname: de db unsafe classname om de foreign key naar te maken -->
	<!-- param name: fk-name de naam (max 5 chars.) om te gebruiken in de naam-->
	<xsl:template name="create-foreign-key">
		<xsl:param name="classname"/>
		<xsl:param name="fk-name"/>
		<xsl:param name="column-uniquifier" select="''"/>
		<xsl:param name="association-name" select="''"/>
		<xsl:param name="include-archive-version" select="false()"/>
		<xsl:variable name="association-name-desc">
			<xsl:if test="$association-name != ''">: &quot;<xsl:value-of select="$association-name"/>&quot;</xsl:if>
		</xsl:variable>
		<xsl:variable name="referenced-primary-keys">
			<xsl:call-template name="get-class-primary-key-properties-hierarchy">
				<xsl:with-param name="name" select="$classname"/>
			</xsl:call-template>
		</xsl:variable>
		<!--referenced-primary-keys>
			<xsl:copy-of select="$referenced-primary-keys"/>
		</referenced-primary-keys-->
		<!-- Haal alle primary keys op van de gerefereerde tabel op om de foreign key te maken -->
		<xsl:for-each select="$referenced-primary-keys/Class[Property]">
			<xsl:for-each select="Property[not(@archief='true')]">
				<xsl:variable name="fk-text"><xsl:if test="$classname != ../@name"> (is FK naar superclass <xsl:value-of select="../@name"/>)</xsl:if></xsl:variable>
				<column name="{concat('fk_',$column-uniquifier,b3p:get-class-mnemonic($classname),'_',@column-name)}" type="{b3p:get-column-type(@type,@typeName)}" desc="{concat('[FK] ',@typeName,', FK naar ',b3p:class-to-table($classname),'.',@column-name,$fk-text,$association-name-desc)}"/>
			</xsl:for-each>
		</xsl:for-each>
		<xsl:variable name="fk">
			<xsl:for-each select="$referenced-primary-keys/Class/Property[not(@archief='true')]">
				<column>fk_<xsl:value-of select="$column-uniquifier"/><xsl:value-of select="b3p:get-class-mnemonic($classname)"/>_<xsl:value-of select="@column-name"/></column>
				<ref-column><xsl:value-of select="@column-name"/></ref-column>
			</xsl:for-each>
		</xsl:variable>
		<foreign-key name="{$fk-name}"
			columns="{fn:string-join($fk/column,',')}" ref-table="{b3p:class-to-table($classname)}"
			ref-columns="{fn:string-join($fk/ref-column,',')}" />	
			
		<xsl:if test="$include-archive-version and $referenced-primary-keys/Class/Property[@archief='true']">
			<xsl:for-each select="$referenced-primary-keys/Class[Property]">
				<xsl:for-each select="Property[@archief='true']">
					<xsl:variable name="fk-text"><xsl:if test="$classname != ../@name"> (is FK naar superclass <xsl:value-of select="../@name"/>)</xsl:if></xsl:variable>
					<column archief="true" name="{concat('fk_',$column-uniquifier,b3p:get-class-mnemonic($classname),'_',@column-name)}" type="{b3p:get-column-type(@type,@typeName)}" desc="{concat('[FK] ',@typeName,', FK naar ',b3p:class-to-table($classname),'.',@column-name,$fk-text,$association-name-desc)}"/>
				</xsl:for-each>
			</xsl:for-each>
			<xsl:variable name="fk">
				<xsl:for-each select="$referenced-primary-keys/Class/Property">
					<column>fk_<xsl:value-of select="$column-uniquifier"/><xsl:value-of select="b3p:get-class-mnemonic($classname)"/>_<xsl:value-of select="@column-name"/></column>
					<ref-column><xsl:value-of select="@column-name"/></ref-column>
				</xsl:for-each>
			</xsl:variable>
			<xsl:comment> Foreign key met archief primary key kolom: </xsl:comment>
			<foreign-key archief="true" name="{$fk-name}"
				columns="{fn:string-join($fk/column,',')}" ref-table="{b3p:class-to-table($classname)}"
				ref-columns="{fn:string-join($fk/ref-column,',')}" />	
		</xsl:if>
	</xsl:template>
	
	<!-- Context: none -->
	<!-- Maak de tabellen voor kolommen die een-op-veel keer voorkomen, maar een simpel type zijn (bijvoorbeeld Gebouwd_object.Gebruiksdoel. Komt 'n' keer voor, maar is AN80 -->
	<xsl:template name="process-simple-multi">
		
		<xsl:for-each select="$rsgb/Objecttypes/Class/Property[@upperValueValue = 'n']/..">
			<xsl:variable name="class-name" select="@name"/>
			<xsl:for-each select="./Property">
					<xsl:if test="b3p:getElementType(.) = 'simple-multi'">
						<xsl:variable name="table" select="concat($class-name,@name)"/>
						<xsl:variable name="safename" select="b3p:class-to-table($table)"/>
						<table name="{$safename}" desc="{concat('RSGB class voor een-op-meer kolom ',$class-name,' ', @name)}" fullname="{$table}">
							<column name="{b3p:property-to-column(@name,$table)}" fullname="{@name}" type="{b3p:get-column-type(@type,@typeName)}" desc="{@typeName}"/>
							<xsl:call-template name="create-foreign-key">
								<xsl:with-param name="classname" select="$class-name"/>
								<xsl:with-param name="fk-name" select="concat('fk_',b3p:get-class-mnemonic($class-name),position())"/>
							</xsl:call-template>
						</table>
					</xsl:if>
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>

	<!-- Context: none -->
	<!-- Maak de tabellen voor de referentielijsten -->
	<xsl:template name="create-reference-list-tables">
		<xsl:for-each-group select="$rsgb/Objecttypes/Property[@elementType= 'Objecttype/Referentielijsten - Property']" group-by="@className">
			<xsl:variable name="class-name" select="current-grouping-key()"/>
			
			<xsl:variable name="key-property" select="$class-primary-keys[@class = $class-name]/key" />
			<table name="{b3p:class-to-table($class-name)}" desc="{concat('RSGB referentielijst ',$class-name)}" fullname="{$class-name}">
				<xsl:for-each select="$rsgb/Objecttypes/Property[@className = $class-name]">
					<xsl:variable name="isKey" select="@name = $key-property"/>
					<column name="{b3p:property-to-column(@name,$class-name)}" fullname="{@name}" type="{b3p:get-column-type(@type,@typeName)}" desc="Referentielijst property {$class-name}.{@name}" key="{$isKey}"/>
				</xsl:for-each>
			</table>
		</xsl:for-each-group>
	</xsl:template>
	
	<!-- Context: none. -->
	<!-- Maak xml DDL voor enumeraties. Maak daarnaast de inserts hiervoor. -->
	<xsl:template name="process-enumeraties">
		<xsl:variable name="class-name" select="'meta_enumeratie_waardes'"/>
		<xsl:variable name="type" select="b3p:getColumnType('','AN255')"/>
		
		<xsl:comment>Create the table for containing all the meta_enumerations values</xsl:comment>
		<table name="{$class-name}" desc="{concat('RSGB class ',$class-name)}" fullname="{$class-name}">
			<column name="naam" key="true" fullname="naam" type="varchar(255)" desc="naam van de enum"/>
			<column name="waarde" key="true" fullname="waarde" type="varchar(255)" desc="waarde van de enum"/>
		</table>
		
		<xsl:comment>Create the inserts for the enumeration values</xsl:comment>
		<inserts>
			<xsl:for-each-group select="Property[@elementType = 'Enumeratiesoort - Values']" group-by="@className">
				<xsl:for-each select="//Property[@className=current-grouping-key()]">
					<insert table="{$class-name}">
						<columns><column>naam</column><column>waarde</column></columns>
						<values><value><xsl:value-of select="current-grouping-key()"/></value><value><xsl:value-of select="@name"/></value></values>
					</insert>
				</xsl:for-each>				
			</xsl:for-each-group>
		</inserts>
		
		<xsl:variable name="class-name" select="'meta_enumeratie'"/>
		<xsl:comment>Create the table for containing all the meta_enumerations</xsl:comment>
		<table name="{$class-name}" desc="{concat('RSGB class ',$class-name)}" fullname="{$class-name}">
			<column name="tabel" fullname="tabel" type="{$type}" desc="[PK] tabel" key="true"/>
			<column name="kolom" fullname="kolom" type="{$type}" desc="[PK] kolom" key="true"/>
			<column name="enumeratie" fullname="enumeratie" type="{$type}" desc="enumeratie"/>
		</table>
		
		<xsl:comment>Create the inserts for the enumerations</xsl:comment>
		<inserts>
			<xsl:for-each select="/RSGB/Objecttypes/Class/Property[@association = '' and @type = /RSGB/Objecttypes/Property/@classId]">
				<xsl:variable name="property" select="."/>
				<xsl:variable name="property-md" select="/RSGB/Objecttypes/Property[@classId = $property/@type][1]"/>
				<xsl:if test="$property-md/@elementType = 'Enumeratiesoort - Values'">
					<insert table="{$class-name}">
						<columns><column>tabel</column><column>kolom</column><column>enumeratie</column></columns>
						<values><value><xsl:value-of select="b3p:class-to-table(../@name)"/></value><value><xsl:value-of select="b3p:property-to-column(@name,../@name)"/></value><value><xsl:value-of select="$property-md/@className"/></value></values>
					</insert>
				</xsl:if>
			</xsl:for-each>
		</inserts>
	</xsl:template>	
	
	<!-- Context: none. -->
	<!-- Maak xml DDL voor meta-referentielijsten Maak daarnaast de inserts hiervoor. -->
	<xsl:template name="process-referentielijsten">
		<xsl:variable name="class-name" select="'meta_referentielijsten'"/>
		<xsl:variable name="type" select="b3p:getColumnType('','AN255')"/>
		
		<xsl:comment>Create the table for containing all the referentielijsten values</xsl:comment>
		<table name="{$class-name}" desc="{concat('RSGB class ',$class-name)}" fullname="{$class-name}">
			<column name="tabel" fullname="tabel" type="{$type}" desc="[PK] tabel" key="true"/>
			<column name="kolom" fullname="kolom" type="{$type}" desc="[PK] kolom" key="true"/>
			<column name="referentielijst" fullname="referentielijst" type="{$type}" desc="referentielijst"/>
		</table>
		
		<xsl:comment>Create the inserts for the referentielijsten</xsl:comment>
		<inserts>
			<xsl:for-each select="/RSGB/Objecttypes/Class/Property[@association = '' and @type = /RSGB/Objecttypes/Property/@classId]">
				<xsl:variable name="property" select="."/>
				<xsl:variable name="property-md" select="/RSGB/Objecttypes/Property[@classId = $property/@type][1]"/>
				<xsl:if test="$property-md/@elementType = 'Objecttype/Referentielijsten - Property'">
					<insert table="{$class-name}">
						<columns><column>tabel</column><column>kolom</column><column>referentielijst</column></columns>
						<values><value><xsl:value-of select="b3p:class-to-table(../@name)"/></value><value><xsl:value-of select="b3p:property-to-column(@name,../@name)"/></value><value><xsl:value-of select="$property-md/@className"/></value></values>
					</insert>
				</xsl:if>
			</xsl:for-each>
		</inserts>
	</xsl:template>
			
	
	<!-- Functie om te bepalen of een property een type heeft dat gedefinieerd staat onder /RSGB/Objecttypes/Property. Als dit niet het geval is, is het een simpel property. -->
	<!-- @property: het /RSGB/Objecttypes/Class/Property om te checken -->
	<!-- returns the type of element: simple/'Objecttype/Groepattribuutsoort - Property'/'Enumeratiesoort - Values'/'Objecttype/Referentielijsten - Property'/'multi-association'/'simple-multi'"> -->
	<xsl:function name="b3p:getElementType">
		<xsl:param name="el"/>
		
		<!-- Does it have it's type defined elsewhere: is the type defined under /RSGB/Objecttypes/Property-->
		<xsl:variable name="has-type-defined-elsewhere" select="$el/@type = $rsgb/Objecttypes/Property/@classId"/>
		<!-- Is the type defined as another class: is the definition located under /RSGB/Objecttypes/Class -->
		<xsl:variable name="has-class-as-type" select="$el/@type = $rsgb/Objecttypes/Class/@id"/>
		<!-- Get the elementType of the first property -->
		<xsl:variable name="properties" select="$rsgb/Objecttypes/Property[@classId = $el/@type]"/>
		<xsl:variable name="elementType" select="$properties[1]/@elementType"/>		
		<xsl:variable name="type">
			<xsl:choose>
				<!-- Check for the non-simple types -->
				<xsl:when test="$has-type-defined-elsewhere and 
					$elementType = 'Objecttype/Groepattribuutsoort - Property'	or 
					$elementType = 'Enumeratiesoort - Values' or 
					$elementType = 'Objecttype/Referentielijsten - Property'">
					<xsl:value-of select="$elementType"/>
				</xsl:when>
				<xsl:when test="$has-class-as-type">
					<xsl:value-of select="'association'"/>
				</xsl:when>
				<xsl:when test="$el/@upperValueValue = 'n'">
					<xsl:value-of select="'simple-multi'"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="'simple'"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:value-of select="$type"/>
	</xsl:function>
</xsl:stylesheet>
