<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:b3p="http://www.b3partners.nl/" xmlns:uml="http://www.omg.org/spec/UML/20110701" xmlns:xmi="http://www.omg.org/spec/XMI/20110701" xmlns:RGB="http://www.sparxsystems.com/profiles/RGB/1.0">

	<xsl:import href="identifiers.xsl"/>
	<xsl:import href="identifiers_oud.xsl"/>
	<xsl:import href="keys_types.xsl"/>
	
	<xsl:variable name="rsgb" select="/RSGB"/>
	
	<xsl:variable name="extra-scripts" as="element(script)*">
		<script filename="bag.sql" name="BAG herstel scripts" />
	</xsl:variable>	
	
	<xsl:variable name="geometryTypes" select="'GM_Surface GM_Curve GM_Point GM_MultiSurface LijnVlak PuntLijnVlak'"/>
	<xsl:variable name="dummyColumn" select="'dummy'"/>
	<!-- Start template -->
	<xsl:template match="/RSGB/Objecttypes">
		<xsl:apply-templates select="Class" />
		<xsl:call-template name="superclassReferences"/>
		<xsl:call-template name="superclasses"/>
		<xsl:call-template name="linkTables"/>	
		<xsl:call-template name="process-enumeraties"/>
		<xsl:call-template name="process-referentielijsten"/>
		<xsl:call-template name="backReferences"/>
		<xsl:text>- Alle foreign keys voor de associations
</xsl:text>
		<xsl:apply-templates select="Class" mode="foreign-keys"/>		
		<xsl:call-template name="removeDummyColumns"/>
		<xsl:call-template name="makeAutoNumbers"/>
		<xsl:call-template name="addExtraDateColumns"/>
		<xsl:call-template name="loadExtraScripts"/>
		<xsl:call-template name="makeClassList"/>
	</xsl:template>

	<!-- Maak DDL voor een enkele class -->
	<xsl:template match="Class">
		<xsl:variable name="className" select="@name"/>
		<xsl:variable name="classId" select="@id"/>
		<xsl:variable name="identifier" select="b3p:findIdentifier(b3p:dbsafe($className))"/>
		<xsl:text>create table </xsl:text><xsl:value-of select="b3p:dbsafe($className)"/><xsl:text> ( 
		</xsl:text>
		<xsl:if test="$identifier != ''">
			<xsl:variable name="temp" select="b3p:getIdentifierType($identifier, $className)"/>
			<xsl:variable name="idType">
				<xsl:choose>
					<xsl:when test="$temp != ''"><xsl:value-of select="$temp"/>  </xsl:when>
					<xsl:otherwise><xsl:value-of select="$fk-type"/> </xsl:otherwise>
				</xsl:choose>
			</xsl:variable> 
			<xsl:value-of select="$identifier"/> <xsl:text> </xsl:text><xsl:value-of select="$idType"/> <xsl:text> primary key /* Dit is een natural primary key */</xsl:text>
		</xsl:if>

		<xsl:variable name="propsWithoutGeom">			
			<xsl:for-each select="Property[@association = '']">
				<xsl:if test="not(fn:contains($geometryTypes,@typeName)) or not(@typeName)">
				<xsl:copy-of select="."/> 
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		
		<xsl:for-each select="$propsWithoutGeom/Property">
			<xsl:variable name="refDef" select="b3p:getReferentieDefs(.,not(position() = 1 and b3p:hasDoubleIdentifiers(b3p:dbsafe($className)) and $identifier = ''))"/>			
			<xsl:if test="$refDef != ''">
				<xsl:value-of select="$refDef"/>
			</xsl:if>
			<xsl:if test="$refDef = ''">
				<!--xsl:if test="not(fn:contains($geometryTypes,@typeName))"-->
					<xsl:variable name="columnName" select="b3p:dbsafe(@name,$className)"/>
					<xsl:if test="$columnName != $identifier">
						<xsl:if test="not(position() = 1 and b3p:hasDoubleIdentifiers(b3p:dbsafe($className)) and $identifier = '')">
											<xsl:text>,
	</xsl:text>
						</xsl:if>
						<xsl:choose>
							<xsl:when test="not(@typeName)">
								<xsl:value-of select="$columnName"/> <xsl:text> varchar(255) /* (heeft geen rsgb type) */ </xsl:text>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$columnName"/> <xsl:text> </xsl:text><xsl:value-of select="b3p:getColumnType(@type,@typeName)"/> /* <xsl:value-of select="@typeName"/><xsl:text> */</xsl:text>
						</xsl:otherwise>
						</xsl:choose>
					</xsl:if>
			</xsl:if>
		</xsl:for-each>
		<xsl:if test="b3p:hasNoColumns($classId) = true()">
			<xsl:value-of select="$dummyColumn"/><xsl:text> varchar(1) /* Dit is een dummy column om lege tabellen te voorkomen.  */</xsl:text>
		</xsl:if>
	<xsl:text>
);
</xsl:text>
	<xsl:variable name="primKey" select="b3p:createPrimaryKey($classId)"/>
	<xsl:value-of select="$primKey"/>
		<xsl:for-each select="Property[@association = '']">
			<xsl:if test="@typeName and fn:contains($geometryTypes,@typeName)">
				<xsl:value-of select="b3p:addGeometryColumn(b3p:dbsafe($className),b3p:dbsafe(@name,$className),@typeName,position())"/>
			</xsl:if>
		</xsl:for-each>
		<xsl:text>
		
</xsl:text>
	</xsl:template>

<!-- Template voor het maken van koppeltabellen voor de superclasses. FUNCTIONARIS, HUISHOUDENRELATIE, HUWERLIJK/GEREGISTREERD PARTNERSCHAP, KADASTRALE ONROERENDE ZAAK HISTORIE RELATIE,	
LCOATIEAANDUIDING ADRES, LCOATIEAANDUIDING OPENBARE RUIMTE, OUDE-KIND-RELATIE, WOZ-BELANG -->
	<xsl:template name="superclasses">
		<xsl:variable name="autonumberOffset" select="65"/>
		<xsl:for-each select="/RSGB/Objecttypes/Property[@classId = /RSGB/Objecttypes/Class/Property/@association and @elementType = 'Objecttype - Superklasse']">
			<xsl:variable name="cid" select="@classId"/>
			<xsl:variable name="table" select="b3p:dbsafe(@className)"/>
			<xsl:variable name="identifier" select="b3p:findIdentifier($table)"/>
			<xsl:variable name="idType" select="b3p:getIdentifierType($identifier)"/>
<xsl:text>
create table </xsl:text><xsl:value-of select="$table"/><xsl:text> ( 
	</xsl:text>
			<xsl:if test="$identifier != ''">
				<xsl:variable name="idType" select="b3p:getIdentifierType($identifier)"/>
				<xsl:value-of select="$identifier"/> <xsl:text> </xsl:text><xsl:value-of select="$idType"/> <xsl:text> primary key /* Dit is een natural primary key */</xsl:text>
			</xsl:if>
			<!-- Voeg de properties toe van deze superclass -->
			<xsl:for-each select="/RSGB/Objecttypes/Property[@elementType = 'Objecttype/Relatieklasse - Property' and @classId = $cid]">
				<xsl:variable name="refDef" select="b3p:getReferentieDefs(.,not(position() = 1 and b3p:hasDoubleIdentifiers($table) and $identifier = ''))"/>
		
				<xsl:choose>
					<xsl:when test="$refDef != ''">
						<xsl:text></xsl:text>
						<xsl:value-of select="$refDef"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="columnName" select="b3p:dbsafe(@name,@className)"/>
							<xsl:if test="$columnName != $identifier">
								<xsl:if test="not(position() = 1 and b3p:hasDoubleIdentifiers($table) and $identifier = '')">
											<xsl:text>,
	</xsl:text>
								</xsl:if>
									<xsl:value-of select="$columnName"/> <xsl:text> </xsl:text><xsl:value-of select="b3p:getColumnType(@type,@typeName)"/> /* <xsl:value-of select="@typeName"/><xsl:text> */</xsl:text>
								</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
				<xsl:text>
	);
				</xsl:text>
				<xsl:variable name="baseClass" select="$rsgb/Objecttypes/Class/Property[@association = $cid]"/>
				<xsl:variable name="referencing" select="$rsgb/Objecttypes/Class[@id = $baseClass/@type]/@name"/>
				<xsl:variable name="references" select="$baseClass/../@name"/>
				<xsl:variable name="referencingSafe" select="b3p:dbsafe($referencing)"/>
				<xsl:variable name="referencesSafe" select="b3p:dbsafe($references)"/>
				
				<xsl:variable name="volgnummerCol1">
					<xsl:choose>
						<xsl:when test="$references = $referencing"><xsl:value-of select="1" /></xsl:when>
					</xsl:choose>
				</xsl:variable>
								
				<xsl:variable name="volgnummerCol2">
					<xsl:choose>
						<xsl:when test="$references = $referencing"><xsl:value-of select="2" /></xsl:when>
					</xsl:choose>
				</xsl:variable>
				
				
				<xsl:variable name="column">
					<xsl:value-of select="$referencesSafe"/><xsl:value-of select="$volgnummerCol1"/> <xsl:text>_id</xsl:text>
				</xsl:variable>
			
				<xsl:variable name="column2">
					<xsl:value-of select="$referencingSafe"/><xsl:value-of select="$volgnummerCol2"/><xsl:text>_id</xsl:text>
				</xsl:variable>

				
				<xsl:choose>
					<xsl:when test="b3p:hasDoubleIdentifiersNonModel($referencesSafe) = 'true'">
						<xsl:value-of select="b3p:addColumn($table,$column,b3p:getIdentifierTypeDefault($references,$fk-type),'')"/> 
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="b3p:addColumn($table,$column,b3p:getIdentifierTypeDefault($references,$fk-type),('references',$referencesSafe,'on delete cascade'))"/> 
					</xsl:otherwise>
				</xsl:choose>
				
				
				<xsl:choose>
					<xsl:when test="b3p:hasDoubleIdentifiersNonModel($referencingSafe) = 'true'">
						<xsl:value-of select="b3p:addColumn($table,$column2,b3p:getIdentifierTypeDefault($referencing,$fk-type),'')"/> 
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="b3p:addColumn($table,$column2,b3p:getIdentifierTypeDefault($referencing,$fk-type),('references',$referencingSafe,'on delete cascade'))"/> 
					</xsl:otherwise>
				</xsl:choose>
				
				<xsl:variable name="primKey" select="b3p:createPrimaryKey($cid)"/>
				<xsl:value-of select="$primKey"/>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="Class" mode="foreign-keys">
		<xsl:variable name="table" select="b3p:dbsafe(@name)"/>
		<xsl:variable name="associations">
			<xsl:for-each select="Property[@association != '' and @upperValueValue != '*']"><xsl:copy-of select="."/></xsl:for-each>
		</xsl:variable>
		<xsl:for-each select="Property[@association != '' and @upperValueValue != '*']"> 
			<xsl:variable name="property" select="."/>
			<xsl:variable name="references" select="b3p:dbsafe(@typeName)"/>
			<xsl:variable name="colType" select="b3p:getIdentifierTypeDefault(@typeName, $fk-type)"/>
			<xsl:variable name="column">fk<xsl:value-of select="count($associations/Property[@id=$property/@id]/preceding-sibling::*)+1"/><xsl:text>_</xsl:text><xsl:value-of select="$references"/></xsl:variable>
			
			<xsl:choose>
				<xsl:when test="b3p:hasDoubleIdentifiersNonModel($references) = 'true'">
			<xsl:value-of select="b3p:addColumn($table,$column,$colType,'')"/>
				</xsl:when>
				<xsl:otherwise>
			<xsl:value-of select="b3p:addColumn($table,$column,$colType,('references',$references,'on delete cascade'))"/>
				</xsl:otherwise>
			</xsl:choose>
				
		</xsl:for-each>
		<xsl:text>
</xsl:text>

		<xsl:variable name="class" select="."/>
		<xsl:variable name="subclasses" select="//Class[@superclassId = $class/@id]"/>
		<xsl:if test="count($subclasses) > 0">
			<xsl:value-of select="b3p:addColumn($table,'clazz','varchar(255)')"/>
			<xsl:text>comment on column </xsl:text><xsl:value-of select="$table"/>.clazz is 'Aanduiding subclasse; een van: <xsl:value-of select="string-join($subclasses/@name,', ')"/><xsl:text>';
</xsl:text>
		</xsl:if>
	</xsl:template>

	<!-- Maak de referentie (als die er nog niet is) naar de @superclass -->
	<xsl:template name="superclassReferences">
		<xsl:text>-- Maak foreign keys naar de superclasses
		</xsl:text>
		<xsl:for-each select="$rsgb/Objecttypes/Class[@superclass]">
			<xsl:text>
</xsl:text>
			<xsl:variable name="table" select="@name"/>
			<xsl:variable name="tableSafe" select="b3p:dbsafe(@name)"/>
			<xsl:choose>
				<xsl:when test="b3p:hasDoubleIdentifiers($tableSafe) = true() or b3p:hasSingleIdentifier($tableSafe) = true()">
					<xsl:variable name="idType" select="b3p:getIdentifierTypeDefault(@superclass, $fk-type)"/>
					<xsl:variable name="colName"><xsl:text>fk_</xsl:text><xsl:value-of  select="b3p:dbsafe(@superclass)"/></xsl:variable>
					<value-of>b3p:addColumn($tableSafe, $colName,$idType,('references',b3p:dbsafe(@superclass),'on delete cascade'))</value-of>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="b3p:hasDoubleIdentifiers(b3p:dbsafe(@superclass)) != 'true'">
						<xsl:text>alter table </xsl:text><xsl:value-of select="$tableSafe"/><xsl:text>  ADD CONSTRAINT fk</xsl:text><xsl:value-of select="position()"/> <xsl:text></xsl:text><xsl:value-of select="b3p:dbsafe(@superclass)"/><xsl:text> FOREIGN KEY (pk_</xsl:text><xsl:value-of select="b3p:dbsafe(@superclass)"/> <xsl:text>) REFERENCES </xsl:text><xsl:value-of select="b3p:dbsafe(@superclass)"/><xsl:text> ON DELETE CASCADE;</xsl:text>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
		
		<xsl:text>
		-- Einde superclassReferences
		</xsl:text>
	</xsl:template>
	
	<xsl:template name="linkTables">
		<xsl:variable name="properties">
			<xsl:for-each select="$rsgb/Objecttypes/Class/Property[@association != '' and @upperValueValue = '*']">
				<xsl:if test="@association != $rsgb/Objecttypes[@elementType != 'Objecttype - Superklasse']/@classId">
					<xsl:value-of select="."/>
				</xsl:if>
			 </xsl:for-each>
		</xsl:variable>
		<xsl:text>/** Link alle tabellen aan elkaar */</xsl:text>
		<xsl:for-each select="$rsgb/Objecttypes/Class/Property[@association != '' and @upperValueValue = '*']"> 
			<xsl:if test="@association != $rsgb/Objecttypes[@elementType != 'Objecttype - Superklasse']/@classId">
		
				<xsl:variable name="property" select="."/>
				<xsl:variable name="referenceeNon" select="$property/../@name"/>
				<xsl:variable name="referencedNon" select="$rsgb/Objecttypes/Class[@id = $property/@type]/@name"/>
				<xsl:variable name="referencee" select="b3p:dbsafe($referenceeNon)"/>
				<xsl:variable name="referenced" select="b3p:dbsafe($referencedNon)"/>
				
				<xsl:variable name="column1">
					<xsl:choose>
						<xsl:when test="$property/@name != '' "><xsl:value-of select="b3p:dbsafe($property/@name)"/> </xsl:when>
						<xsl:otherwise><xsl:value-of select="b3p:dbsafe($property/@associationName)"/> </xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:variable name="column2">
					<xsl:choose>
						<xsl:when test="$referenced != '' "><xsl:value-of select="$referenced"/> </xsl:when>
						<xsl:otherwise><xsl:value-of select="b3p:dbsafe($referenced/../@associationName)"/> </xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				
				<xsl:variable name="table"><xsl:value-of select="$referencee"/><xsl:text>_</xsl:text><xsl:value-of select="b3p:dbsafe($property/@associationName)"/> </xsl:variable>
				
				<xsl:variable name="col1Type" select="b3p:getIdentifierTypeDefault($referenceeNon,$fk-type)"/>
				<xsl:variable name="col2Type" select="b3p:getIdentifierTypeDefault($referencedNon,$fk-type)"/>
				
				<!-- Creeër de koppeltabel -->
				<xsl:text>
	create table </xsl:text> <xsl:value-of select="$table"/><xsl:text>(
		</xsl:text>fk<xsl:value-of select="(position()-1)*2"/><xsl:text>_</xsl:text><xsl:value-of select="$column1"/><xsl:text> </xsl:text><xsl:value-of select="$col1Type"/><xsl:text> </xsl:text><xsl:value-of select="string-join(('references',$referencee,'on delete cascade'),' ')"/><xsl:text>,
		</xsl:text>fk<xsl:value-of select="(position()-1)*2+1"/><xsl:text>_</xsl:text><xsl:value-of select="$column2"/><xsl:text> </xsl:text><xsl:value-of select="$col2Type"/><xsl:text> </xsl:text><xsl:value-of select="string-join(('references',$referenced,'on delete cascade'),' ')"/><xsl:text>
	);
	</xsl:text>
	
			</xsl:if>
		</xsl:for-each>
		
	</xsl:template>
	
	<xsl:template name="process-enumeraties">
		<xsl:variable name="type" select="b3p:getColumnType('','AN255')"/>
		<xsl:text>
		
create table meta_enumeratie_waardes ( 
	naam </xsl:text><xsl:value-of select="$type"/><xsl:text>,
	waarde </xsl:text><xsl:value-of select="$type"/> <xsl:text>
);
</xsl:text>
		<xsl:text>
-- Alle inserts voor de enumeraties
		</xsl:text>
			<xsl:for-each-group select="Property[@elementType = 'Enumeratiesoort - Values']" group-by="@className">
					<xsl:for-each select="//Property[@className=current-grouping-key()]">
	<xsl:text>
insert into meta_enumeratie_waardes (naam, waarde) values ('</xsl:text><xsl:value-of select="current-grouping-key()"/>','<xsl:value-of select="b3p:escapeInsertValue(@name)"/><xsl:text>');</xsl:text>
			</xsl:for-each>
				
		</xsl:for-each-group>
		
		<xsl:text>

create table meta_enumeraties(
	tabel </xsl:text><xsl:value-of select="$type"/><xsl:text>,
	kolom </xsl:text><xsl:value-of select="$type"/> <xsl:text>,
	enumeratie </xsl:text><xsl:value-of select="$type"/> <xsl:text>,
	primary key(tabel, kolom)
);
</xsl:text>
		<xsl:for-each select="/RSGB/Objecttypes/Class/Property[@association = '' and @type = /RSGB/Objecttypes/Property/@classId]">
			<xsl:variable name="property" select="."/>
			<xsl:variable name="property-md" select="/RSGB/Objecttypes/Property[@classId = $property/@type][1]"/>
			<xsl:if test="$property-md/@elementType = 'Enumeratiesoort - Values'">
insert into meta_enumeraties (tabel, kolom, enumeratie) values ('<xsl:value-of select="b3p:dbsafe(../@name)"/>', '<xsl:value-of select="b3p:dbsafe(@name)"/>', '<xsl:value-of select="$property-md/@className"/><xsl:text>');</xsl:text>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="process-referentielijsten">
		<xsl:variable name="type" select="b3p:getColumnType('','AN255')"/>	
		<xsl:text>

create table meta_referentielijsten(
	tabel </xsl:text><xsl:value-of select="$type"/><xsl:text>,
	kolom </xsl:text><xsl:value-of select="$type"/> <xsl:text>,
	referentielijst </xsl:text><xsl:value-of select="$type"/> <xsl:text>,
	primary key(tabel, kolom)
);
</xsl:text>	
		<xsl:for-each select="/RSGB/Objecttypes/Class/Property[@association = '' and @type = /RSGB/Objecttypes/Property/@classId]">
			<xsl:variable name="property" select="."/>
			<xsl:variable name="property-md" select="/RSGB/Objecttypes/Property[@classId = $property/@type][1]"/>
			<xsl:if test="$property-md/@elementType = 'Objecttype/Referentielijsten - Property'">
insert into meta_referentielijsten (tabel, kolom, referentielijst) values ('<xsl:value-of select="b3p:dbsafe(../@name)"/>', '<xsl:value-of select="b3p:dbsafe(@name)"/>', '<xsl:value-of select="$property-md/@className"/><xsl:text>');</xsl:text>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
		

	<xsl:template name="backReferences">
		<xsl:text> 
		-- Haal alle refererende kolommen van andere classes op die * keer verwijzen naar deze class en neem ze op 
</xsl:text>
		<!-- Haal alle refererende kolommen van andere classes op die * keer verwijzen naar deze class en neem ze op -->
		<xsl:for-each select="$rsgb/Objecttypes/Class[@id = $rsgb/Objecttypes/Class/Property[@upperValueValue = '*']/@type ]">
			
			<xsl:variable name="cid" select="@id"/>
			<xsl:variable name="name" select="@name"/><!-- pand-->
			<xsl:variable name="safeName" select="b3p:dbsafe($name)"/>
			<xsl:variable name="references" select="$rsgb/Objecttypes/Class/Property[@type = $cid and @upperValueValue = '*']"/> <!-- verblijffsobject referernde column-->
			
			<xsl:for-each select="$references">
				<xsl:variable name="refName" select="../@name"/> <!--verblijfsobject-->
				
				<xsl:variable name="refId" select="../@id"/> 
				<!-- Checks if the relation is a N-N relation. -->
				<xsl:variable name="isNN">
					<xsl:value-of select="count(//Property[@elementType='Objecttype - Association' and @id=$references/@association and @upperValueValue ='*'] )> 0"/>
				</xsl:variable> 
				<xsl:choose>
					<xsl:when test="$isNN = true()">
						<xsl:variable name="tableNameUnsafe">
							<xsl:value-of select="b3p:dbsafe($refName)"/><xsl:text>_</xsl:text><xsl:value-of select="$safeName"/><xsl:value-of select="position()"/>
						</xsl:variable>
						<xsl:variable name="tableName"><xsl:value-of select="b3p:dbsafe($tableNameUnsafe)"/></xsl:variable>
						
						<xsl:variable name="ref1"><xsl:value-of select="$refName"/></xsl:variable>
						<xsl:variable name="ref2"><xsl:value-of select="$name"/></xsl:variable>
						<xsl:variable name="refCol1"><xsl:text>fk1_</xsl:text><xsl:value-of select="$refName"/></xsl:variable>
						<xsl:variable name="refCol2"><xsl:text>fk2_</xsl:text><xsl:value-of select="$name"/></xsl:variable>
						
						<!-- Maak koppeltabel -->
						<xsl:text>create table </xsl:text><xsl:value-of select="$tableName"/><xsl:text>(
							prim_key </xsl:text> <xsl:value-of select="$pk-type"/><xsl:text> primary key,
							</xsl:text>
 <xsl:value-of select="$dummyColumn"/><xsl:text> </xsl:text> <xsl:value-of select="$fk-type"/><xsl:text>
);
</xsl:text>
			<xsl:value-of select="b3p:createSequence($tableName)"/>
			<xsl:choose>
				<xsl:when test="b3p:hasDoubleIdentifiersNonModel(b3p:dbsafe($ref1)) ='true'">
<xsl:value-of select="b3p:addColumn($tableName,b3p:dbsafe($refCol1),b3p:getIdentifierTypeDefault($ref1,$fk-type),'')"/> 
				</xsl:when>
				<xsl:otherwise>
<xsl:value-of select="b3p:addColumn($tableName,b3p:dbsafe($refCol1),b3p:getIdentifierTypeDefault($ref1,$fk-type),('references',b3p:dbsafe($ref1),'on delete cascade'))"/> 
				</xsl:otherwise>
			</xsl:choose>	
			
			<xsl:choose>
				<xsl:when test="b3p:hasDoubleIdentifiersNonModel(b3p:dbsafe($ref2)) ='true'">
<xsl:value-of select="b3p:addColumn($tableName,b3p:dbsafe($refCol2),b3p:getIdentifierTypeDefault($ref2,$fk-type),'')"/> 
				</xsl:when>
				<xsl:otherwise>
<xsl:value-of select="b3p:addColumn($tableName,b3p:dbsafe($refCol2),b3p:getIdentifierTypeDefault($ref2,$fk-type),('references',b3p:dbsafe($ref2),'on delete cascade'))"/> 
				</xsl:otherwise>
			</xsl:choose>
<xsl:text>
alter table </xsl:text><xsl:value-of select="$tableName"/><xsl:text> DROP COLUMN </xsl:text><xsl:value-of select="$dummyColumn"/><xsl:text>;
</xsl:text>

					</xsl:when>
					<xsl:otherwise>
						<!-- It is a many to zero/one -->
						<xsl:variable name="refSafe" select="b3p:dbsafe($refName)"/>
						<xsl:variable name="colName"><xsl:text>fk_</xsl:text><xsl:value-of select="position()"/><xsl:value-of select="$refSafe"/> </xsl:variable>
						<xsl:variable name="colType" select="b3p:getIdentifierTypeDefault($refName,$fk-type)"/>
						<xsl:choose>
							<xsl:when test="b3p:hasDoubleIdentifiersNonModel($refSafe) ='true'">
								<xsl:value-of select="b3p:addColumn($safeName,$colName,$colType,'')"/>	
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="b3p:addColumn($safeName,$colName,$colType,('references',$refSafe,'on delete cascade'))"/>	
							</xsl:otherwise>
						</xsl:choose>	
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>	
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="makeAutoNumbers">
		<xsl:text> 
		-- Maak alle autonumbers in een aparte file
</xsl:text>
		<!-- Haal alle refererende kolommen van andere classes op die * keer verwijzen naar deze class en neem ze op -->
		<xsl:for-each select="$rsgb/Objecttypes/Class[@id = $rsgb/Objecttypes/Class/Property[@upperValueValue = '*']/@type ]">
			
			<xsl:variable name="cid" select="@id"/>
			<xsl:variable name="name" select="@name"/><!-- pand-->
			<xsl:variable name="safeName" select="b3p:dbsafe($name)"/>
			<xsl:variable name="references" select="$rsgb/Objecttypes/Class/Property[@type = $cid and @upperValueValue = '*']"/> <!-- verblijffsobject referernde column-->
			
			<xsl:for-each select="$references">
				<xsl:variable name="refName" select="../@name"/> <!--verblijfsobject-->
				
				<xsl:variable name="refId" select="../@id"/> 
				<!-- Checks if the relation is a N-N relation. -->
				<xsl:variable name="isNN">
					<xsl:value-of select="count(//Property[@elementType='Objecttype - Association' and @id=$references/@association and @upperValueValue ='*'] )> 0"/>
				</xsl:variable> 
				<xsl:if test="$isNN = true()">
						<xsl:variable name="tableNameUnsafe">
							<xsl:value-of select="b3p:dbsafe($refName)"/><xsl:text>_</xsl:text><xsl:value-of select="$safeName"/><xsl:value-of select="position()"/>
						</xsl:variable>
						<xsl:variable name="tableName"><xsl:value-of select="b3p:dbsafe($tableNameUnsafe)"/></xsl:variable>
												
						<xsl:value-of select="b3p:autoNumberId($tableName, $tableName, 'prim_key' )"/>

				</xsl:if>
			</xsl:for-each>	
		</xsl:for-each>
	</xsl:template>
	
	
	<xsl:template name="removeDummyColumns">
			<xsl:text> -- Verwijder alle dummy kolommen
</xsl:text>
		<xsl:for-each select="$rsgb/Objecttypes/Class">
			<xsl:if test="b3p:hasNoColumns(@id) = true()">
				<xsl:text>
alter table </xsl:text><xsl:value-of select="b3p:dbsafe(@name)"/><xsl:text> DROP COLUMN </xsl:text><xsl:value-of select="$dummyColumn"/><xsl:text>;</xsl:text>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	

	<xsl:template name="makeClassList">
	
		<xsl:element name="xsl:variable"><xsl:attribute name="name" select="'tableIdentifiers'"/><xsl:attribute name="as" select="'element(entry)'"/> 
		
			<xsl:for-each select="/RSGB/Objecttypes/Class">
				<xsl:text>
				 </xsl:text><xsl:value-of select="@name"/><xsl:text>	->	</xsl:text><xsl:value-of select="b3p:dbsafe(@name)"/>
			</xsl:for-each>
			
			
			<xsl:for-each select="/RSGB/Objecttypes/Property[@classId = /RSGB/Objecttypes/Class/Property/@association and @elementType = 'Objecttype - Superklasse']">
				<xsl:text>
				 </xsl:text><xsl:value-of select="@className"/><xsl:text>	->	</xsl:text><xsl:value-of select="b3p:dbsafe(@className)"/>
			</xsl:for-each>
		
		</xsl:element>
		
	
	</xsl:template>
		
	<xsl:template name="loadExtraScripts">
	<xsl:text> -- Laad alle extra scripts in om de RSGB compleet te maken
	</xsl:text>
	<xsl:variable name="basePath">
		<!--xsl:text>file:///</xsl:text-->
		<xsl:value-of select="'extra_scripts/'"/>
		<xsl:value-of select="$dbtype"/>
		<xsl:text>/</xsl:text>
	</xsl:variable>
		<xsl:for-each select="$extra-scripts">
			<xsl:text>
			-- Laad het volgende script in: </xsl:text><xsl:value-of select="@filename"/>
			<xsl:text>
			--</xsl:text><xsl:value-of select="@name"/>
			<xsl:text>
			</xsl:text>
			<xsl:variable name="scriptPath">
				<xsl:value-of select="$basePath"/>
				<xsl:value-of select="@filename"/>
			</xsl:variable>
			<xsl:value-of select="unparsed-text($scriptPath)"/>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template name="addExtraDateColumns">
		<xsl:text>/* Voeg extra datum kolommen toe voor alle classes */
		</xsl:text>
		<xsl:variable name="temporalType" select="b3p:getColumnType('dummy' , 'Datum' )"/>
		<xsl:for-each select="//Class/Property[@typeName = 'OnvolledigeDatum']">
			<xsl:variable name="tableUnsafe">
				<xsl:choose>
					<xsl:when test="../@name">
						<xsl:value-of select="../@name"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="@className"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:variable name="colUnsafe" select="@name"/>
			<xsl:variable name="tableSafe" select="b3p:dbsafe($tableUnsafe)"/>
			<xsl:variable name="colSafe" select="b3p:dbsafe($colUnsafe)"/>
			<xsl:variable name="colLower">
				<xsl:value-of select="b3p:dbsafe($colSafe,concat('_' , $tableSafe))"/><xsl:text>_low</xsl:text>
			</xsl:variable>
			<xsl:variable name="colUpper">
				<xsl:value-of select="b3p:dbsafe($colSafe,concat('_' , $tableSafe))"/><xsl:text>_up</xsl:text>
			</xsl:variable>
			<xsl:value-of select="b3p:addColumn($tableSafe,$colLower, $temporalType )"/><xsl:text>
</xsl:text>
			<xsl:value-of select="b3p:addColumn($tableSafe,$colUpper, $temporalType )"/>
		</xsl:for-each>
		
		<xsl:text>/* Voeg extra datum kolommen toe voor alle superclasses */
		</xsl:text>
		<xsl:variable name="temporalType" select="b3p:getColumnType('dummy' , 'Datum' )"/>
		<xsl:for-each select="$rsgb/Objecttypes/Property[(@elementType = 'Objecttype/Relatieklasse - Property' or @elementType = 'Objecttype/Relatieklasse - Property')  and @typeName = 'OnvolledigeDatum']">
			<xsl:variable name="tableUnsafe" select="@className"/>
			<xsl:variable name="colUnsafe" select="@name"/>
			<xsl:variable name="tableSafe" select="b3p:dbsafe($tableUnsafe)"/>
			<xsl:variable name="colSafe" select="b3p:dbsafe($colUnsafe)"/>
			<xsl:variable name="colLower">
				<xsl:value-of select="b3p:dbsafe($colSafe,concat('_' , $tableSafe))"/><xsl:text>_low</xsl:text>
			</xsl:variable>
			<xsl:variable name="colUpper">
				<xsl:value-of select="b3p:dbsafe($colSafe,concat('_' , $tableSafe))"/><xsl:text>_up</xsl:text>
			</xsl:variable>
			<xsl:value-of select="b3p:addColumn($tableSafe,$colLower, $temporalType )"/><xsl:text>
</xsl:text>
			<xsl:value-of select="b3p:addColumn($tableSafe,$colUpper, $temporalType )"/>
		</xsl:for-each>
		
	</xsl:template>
	
	<xsl:function name="b3p:createPrimaryKey">
		<xsl:param name="tableId"/>
		<xsl:variable name="tableEl" select="$rsgb/Objecttypes/Class[@id = $tableId]"/>
		<xsl:variable name="superclassEl" select="$rsgb/Objecttypes/Property[@classId = $tableId and @elementType='Objecttype - Superklasse']"/>
		<xsl:variable name="table" select="b3p:dbsafe($tableEl/@name)"/>
		<xsl:variable name="addCol">
			<xsl:choose>
				<xsl:when test="$superclassEl and b3p:hasDoubleIdentifiers(b3p:dbsafe($superclassEl/@name))">
					<xsl:variable name="superElName" select="b3p:dbsafe($superclassEl/@className)"/>
					<xsl:variable name="ids" select="$tableDoubleIdentifiers[@table = $superElName]"/>
					<xsl:text>alter table </xsl:text><xsl:value-of select="$superElName"/> <xsl:text> add constraint pk_</xsl:text> <xsl:value-of select="$superElName"/><xsl:text> primary key (</xsl:text> <xsl:value-of select="$ids/@identifier1"/><xsl:text>,</xsl:text><xsl:value-of select="$ids/@identifier2"/><xsl:text>);
					</xsl:text>
				</xsl:when>
				<xsl:when test="b3p:hasDoubleIdentifiers($table) = true()">
					<xsl:variable name="ids" select="$tableDoubleIdentifiers[@table = $table]"/>
					<xsl:text>alter table </xsl:text><xsl:value-of select="$table"/> <xsl:text> add constraint pk_</xsl:text> <xsl:value-of select="$table"/><xsl:text> primary key (</xsl:text> <xsl:value-of select="$ids/@identifier1"/><xsl:text>,</xsl:text><xsl:value-of select="$ids/@identifier2"/><xsl:text>);
					</xsl:text>
				</xsl:when>
				<xsl:when test="b3p:hasSingleIdentifier($table) = true()">
					<!-- Identifier zit al in de tabeldefinitie -->
				</xsl:when>
				<xsl:otherwise>
					<xsl:variable name="references" select="$rsgb/Objecttypes/Class[@id=$tableEl/@superclassId]"/>
					<xsl:variable name="refId" select="b3p:findIdentifier(b3p:dbsafe($references/@name))"/>
					<xsl:variable name="idType" select="b3p:getIdentifierTypeDefault($references/@name, $fk-type)"/>
					<xsl:variable name="colName"><xsl:text>pk_</xsl:text><xsl:value-of  select="b3p:dbsafe($references/@name)"/></xsl:variable>
					<xsl:value-of select="b3p:addColumn(b3p:dbsafe($table), $colName,$idType,('primary key ',''))"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:value-of select="$addCol"/>
	</xsl:function>
	
	<xsl:function name="b3p:addColumn">
		<xsl:param name="table"/>
		<xsl:param name="column"/>
		<xsl:param name="type"/>
		<xsl:value-of select="b3p:addColumn($table,$column,$type,())"/>
	</xsl:function>

	<!-- Geeft het SQL kolomtype voor een datatype uit het RSGB model -->
	<xsl:function name="b3p:getColumnType">
		<xsl:param name="typeEAID"/>	
		<xsl:param name="typeName"/>
		
		<xsl:variable name="elementType" select="b3p:getElementType($typeEAID)"/>
		<xsl:choose>
			<!-- Ga er van uit dat een kolom type voor een enumaration altijd string van max 255 lengte is -->
			<xsl:when test="$elementType = 'Enumeratiesoort - Values'">varchar(255) /* enumeratie */</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="b3p:replaceDataType($typeName)"/> 
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	
	<xsl:function name="b3p:getElementType">
		<xsl:param name="typeEAID"/>
		
		<xsl:value-of select="$rsgb/Objecttypes/Property[@classId = $typeEAID][1]/@elementType"/>
	</xsl:function>	
	
	<xsl:function name="b3p:replaceDataType">
		<xsl:param name="type"/>

		<xsl:choose>
			<xsl:when test="$type = 'AN'">
				<xsl:text>varchar(255)</xsl:text>
			</xsl:when>
			<xsl:when test="fn:matches($type, '^AN?[0-9]+$')">
				<xsl:variable name="length" select="fn:replace($type,'AN?','')"/>
				<xsl:value-of select="string-join(('varchar(',$length,')'),'')"/>
			</xsl:when>
			<xsl:when test="fn:matches($type, '^N[0-9]+$')">
				<xsl:variable name="length" select="fn:replace($type,'N','')"/>
				<xsl:value-of select="string-join(('decimal(',$length,',0)'),'')"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="replacement" select="$all-types[@name=$type]/@vervanging"/>
				<xsl:if test="$replacement"><xsl:value-of select="$replacement"/></xsl:if>
				<xsl:if test="not($replacement)"><xsl:value-of select="$replacement"/>int</xsl:if>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:function>	

	<xsl:function name="b3p:isReferentie">
		<xsl:param name="typeEAID"/>
		<xsl:value-of select="count($rsgb/Objecttypes/Property[@classId = $typeEAID])"/>
	</xsl:function>	

	<xsl:function name="b3p:escapeInsertValue">
		<xsl:param name="value"/>	
		<xsl:variable name="noApostrophe" select="replace($value, '''' ,'''''' )"/>
		<xsl:value-of select="$noApostrophe"/>
	</xsl:function>

	<xsl:function name="b3p:hasNoColumns">
		<xsl:param name="cid"/>
		<xsl:value-of select="count($rsgb/Objecttypes/Property[@classId = /RSGB/Objecttypes/Class[@id = $cid]/Property/@type]) =0 and count($rsgb/Objecttypes/Class[@id = $cid]/Property[@upperValueValue != '*']) = 0"/> 
	</xsl:function>
	
	<xsl:function name="b3p:getReferentieDefs">
		<xsl:param name="el"/>
		<xsl:param name="firstComma"/>
		<xsl:variable name="is-special-property" select="$el/@type = $rsgb/Objecttypes/Property/@classId"/>
		<xsl:value-of>
			<xsl:if test="$is-special-property">
				<xsl:variable name="multiValue" select="$el/@upperValueValue = 'n'"/>
				<xsl:variable name="properties" select="$rsgb/Objecttypes/Property[@classId = $el/@type]"/>
				<xsl:variable name="elementType" select="$properties[1]/@elementType"/>		
				<xsl:choose>
					<xsl:when test="$elementType = 'Enumeratiesoort - Values' or $elementType = 'Objecttype/Referentielijsten - Property'">
						<xsl:if test="$firstComma">
							<xsl:text>,
		</xsl:text>
						</xsl:if>
						<xsl:value-of select="b3p:dbsafe($el/@name)"/><xsl:text> </xsl:text>
						<xsl:choose>
							<xsl:when test="$multiValue">
								<xsl:value-of select="b3p:getColumnType('','AN2000')"/> <xsl:text> /* Dit is een comma separated column: er kunnen meerdere waardes in deze kolom staan */</xsl:text>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="b3p:getColumnType('','AN255')"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:when test="$elementType = 'Objecttype/Groepattribuutsoort - Property'">
						<xsl:for-each select="$properties">
							<xsl:variable name="typeRef" select="./@type"/>
							<xsl:if test="$firstComma or position() != 1">
								<xsl:text>,
	</xsl:text>
							</xsl:if>
							<xsl:value-of select="b3p:replaceAfkortingen($el/@name,$groepattribuut-prefixes)"/><xsl:text>_</xsl:text><xsl:value-of select="b3p:dbsafe(@name)"/>
							<xsl:choose>
								<xsl:when test="b3p:isReferentie($typeRef) != 0">
									<xsl:text> </xsl:text><xsl:value-of select="b3p:getColumnType(@type,@typeName)"/>
									<xsl:text> /* Is een verwijzing naar referentielijst ></xsl:text><xsl:value-of select="$rsgb/Objecttypes/Property[@classId = $typeRef][1]/@className"/> <xsl:text> */</xsl:text>
								</xsl:when>
								<xsl:otherwise>
									<xsl:text> </xsl:text><xsl:value-of select="b3p:getColumnType(@type,@typeName)"/> <xsl:text>/* </xsl:text> <xsl:value-of select="@typeName"/><xsl:text> */</xsl:text>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
			/* onbekend elementType: <xsl:value-of select="$elementType"/> voor property <xsl:value-of select="$el/@name"/> */
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
		</xsl:value-of>
	</xsl:function>	
	
	<xsl:function name="b3p:hasDoubleIdentifiers">
		<xsl:param name="table"/>
		<xsl:value-of  select="count($tableDoubleIdentifiers[@table = $table]) > 0"/>
	</xsl:function>
	
	<xsl:function name="b3p:hasDoubleIdentifiersNonModel">
		<xsl:param name="table"/>
		<xsl:value-of  select="count($tableDoubleIdentifiers[@table = $table and @non-model = true()]) > 0"/>
	</xsl:function>
	
	<xsl:function name="b3p:hasSingleIdentifier">
		<xsl:param name="table"/>
		<xsl:value-of  select="count($tableIdentifiers[@table = $table]) > 0"/>
	</xsl:function>
</xsl:stylesheet>
