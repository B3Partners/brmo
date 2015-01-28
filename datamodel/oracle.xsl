<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:b3p="http://www.b3partners.nl/">

	<!-- Stylesheet voor het omzetten van door rsgbsst2.xsl gemaakte tussenvorm 
         van Enterprise Architect XML model naar een database.

		 Deze stylesheet maakt DDL SQL voor Oracle met Oracle Spatial.

	-->
	<xsl:import href="rsgb2ddl.xsl" />

	<xsl:variable name="dbtype" select="'oracle'"/>	
	<xsl:variable name="pk-type" select="'number'"/>
	<xsl:variable name="fk-type" select="'number'"/>
	
	<xsl:output method="text" encoding="UTF-8"/>
		
	
	<!-- Definieer functies die door rsgb2ddl.xsl worden aangeroepen voor Oracle
		 specifieke syntax.
	-->
	
	<xsl:function name="b3p:addColumn">
		<xsl:param name="table"/>
		<xsl:param name="column"/>
		<xsl:param name="type"/>
		<xsl:param name="extra"/>
		<xsl:text>alter table </xsl:text><xsl:value-of select="$table"/><xsl:text> add </xsl:text><xsl:value-of select="$column"/><xsl:text> </xsl:text><xsl:value-of select="$type"/><xsl:text> </xsl:text><xsl:value-of select="string-join($extra,' ')"/><xsl:text>;
</xsl:text>
	</xsl:function>
	<xsl:variable name="all-types" as="element(type)*">
		<type name="long" vervanging="number"/>
		<type name="boolean" vervanging="number"/>
		<type name="NEN3610ID" vervanging="number"/>
		<!-- onvolledige datum: https://new.kinggemeenten.nl/sites/default/files/document/gr_4679/Advies%20issue%20120%20Generiek%20domein%20datum-tijd%20v0%209.pdf 
			2010-01-31T12:30:00
			2010-01-31
			2010-01
			2010
			in bag wordt ander format gebruikt jjjjmmddhhmmssmm
			2011030811410622
			voorlopig varchar(19) voor volledige datumtijd zonder tijdzone, daar past bag ook in
		-->
		<!-- Fout in xml.. Aanname dat // een onvolledige datum is -->
		<type name="//" vervanging="varchar2(19)"/>
		<type name="OnvolledigeDatum" vervanging="varchar2(19)"/>
		<type name="Datum" vervanging="date"/>
		
		<type name="AN11 (NNNNN-NNNNN)" vervanging="varchar2(11)"/>
	</xsl:variable>
			
	<xsl:function name="b3p:autoNumberId">
		<xsl:param name="seqname"/>
		<xsl:param name="table"/>
		<xsl:param name="id"/>
			<xsl:text>
--/

create or replace trigger </xsl:text><xsl:value-of select="$table"/><xsl:text>_trg
BEFORE INSERT ON </xsl:text><xsl:value-of select="$table"/><xsl:text> FOR EACH ROW
BEGIN
  IF :new.</xsl:text><xsl:value-of select="$id"/><xsl:text> IS NOT NULL THEN
	RAISE_APPLICATION_ERROR(-20000, 'ID cannot be specified');
  ELSE
	SELECT </xsl:text><xsl:value-of select="$seqname"/><xsl:text>_seq.NEXTVAL
	INTO   :new.</xsl:text><xsl:value-of select="$id"/><xsl:text>
	FROM   dual;
  END IF;
END;

/
			</xsl:text>
	</xsl:function>
	
	<xsl:function name="b3p:createSequence">
		<xsl:param name="seqname"/>
		
		<xsl:text>create sequence </xsl:text><xsl:value-of select="$seqname"/><xsl:text>_seq;</xsl:text>
	</xsl:function>
	
	<xsl:function name="b3p:addGeometryColumn">
		<xsl:param name="table"/>	
		<xsl:param name="column"/>	
		<xsl:param name="type"/>
		<xsl:param name="pos"/>
		
insert into user_sdo_geom_metadata values('<xsl:value-of select="$table"/>', '<xsl:value-of select="$column"/>',    
	MDSYS.SDO_DIM_ARRAY(
        MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
        MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    90112
);

alter table <xsl:value-of select="$table"/> add( <xsl:value-of select="$column"/> sdo_geometry );

CREATE INDEX <xsl:value-of select="$table"/><xsl:value-of select="fn:substring($column,1,(25 - fn:string-length($table)))"/><xsl:value-of select="$pos"/>_idx ON <xsl:value-of select="$table"/> (<xsl:value-of select="$column"/>) INDEXTYPE IS MDSYS.SPATIAL_INDEX;


		<!-- evt create index <table>_idx00 on table(column) indextype is mdsys.spatial_index; -->
	</xsl:function>		
</xsl:stylesheet>
