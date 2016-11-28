<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:db="http://www.b3partners.nl/db-specific" xmlns:fn="http://www.w3.org/2005/xpath-functions">
	<xsl:output method="text" encoding="utf-8"/>
	<xsl:variable name="geometryTypes" select="'polygon multipolygon linestring multilinestring point multipoint geometry'"/>
	<xsl:template name="header">

    <xsl:text>--
-- BRMO RSGB script voor </xsl:text>
		<xsl:value-of select="$dbtype"/>
    <xsl:text>
-- Applicatie versie: </xsl:text>
        <xsl:value-of select="$versie"/>
    <xsl:text>
-- Gegenereerd op </xsl:text>
		<xsl:value-of select="current-dateTime()"/>
    <xsl:text>
--

</xsl:text>
        <xsl:if test="'oracle'=$dbtype">
<xsl:text>
-- voor ander tooling dan sqldeveloper define evt. uitzetten omdat er ampersand 
--   tekens in sommige gemeente/plaats/buurt namen voorkomen.
set define off;

</xsl:text>
        </xsl:if>
	</xsl:template>

	<xsl:template match="schema">
		<xsl:call-template name="header"/>
		<xsl:apply-templates select="table">
			<xsl:with-param name="archief" select="false()"/>
		</xsl:apply-templates>
		<xsl:apply-templates select="table" mode="foreign-keys"/>
		<xsl:text>
-- Archief tabellen

</xsl:text>
		<xsl:apply-templates select="table">
			<xsl:with-param name="archief" select="true()"/>
		</xsl:apply-templates>
		<xsl:apply-templates select="inserts"/>
		<xsl:apply-templates select="extra-scripts"/>
	</xsl:template>
	<xsl:template match="table">
		<xsl:param name="archief"/>
		<xsl:if test="not($archief) or column[@archief='true'] or column[@superclass-archief='true']">
			<xsl:variable name="table-name">
				<xsl:choose>
					<xsl:when test="$archief">
						<xsl:value-of select="fn:substring(@name,1,22)"/>_archief</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="@name"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:text>create table </xsl:text>
			<xsl:value-of select="$table-name"/>
			<xsl:text>(
</xsl:text>
			<xsl:for-each select="column[$archief or not(@superclass-archief='true')]">
				<xsl:if test="not (fn:contains($geometryTypes,@type))">
					<xsl:if test="position() > 1">
						<xsl:text>,
</xsl:text>
					</xsl:if>
					<xsl:text>&#9;</xsl:text>
					<xsl:value-of select="@name"/>
					<xsl:text> </xsl:text>
					<xsl:value-of select="db:type(@type)"/>
					<xsl:if test="@key='true' and ($archief or (not(@archief='true') and not(@superclass-archief='true')))">
						<xsl:text> not null</xsl:text>
					</xsl:if>
				</xsl:if>
			</xsl:for-each>
			<xsl:text>
);
</xsl:text>
			<xsl:if test="column[@key='true' and ($archief or (not(@archief='true') and not(@superclass-archief='true')))]">
				<xsl:text>alter table </xsl:text>
				<xsl:value-of select="$table-name"/> add constraint <xsl:if test="$archief">ar_<xsl:value-of select="fn:substring(@name,1,24)"/>
				</xsl:if>
				<xsl:if test="not($archief)">
					<xsl:value-of select="fn:substring(@name,1,27)"/>
				</xsl:if>_pk <xsl:value-of select="$dbpkdef"/>(<xsl:value-of select="fn:string-join(column[@key='true' and ($archief or (not(@archief='true') and not(@superclass-archief='true')))]/@name,',')"/>
				<xsl:text>);
</xsl:text>
			</xsl:if>
			<xsl:for-each select="column[fn:contains($geometryTypes,@type)]">
				<xsl:value-of select="db:addGeometryColumn($table-name, @name, @type, position())"/>
			</xsl:for-each>
			<xsl:text>
</xsl:text>
			<xsl:if test="$addcomment='true'">
				<xsl:if test="@desc">
					<xsl:text>comment on table </xsl:text>
					<xsl:value-of select="$table-name"/>
					<xsl:text> is </xsl:text>
					<xsl:value-of select="db:string-literal(@desc)"/>
					<xsl:text>;
</xsl:text>
				</xsl:if>
				<xsl:for-each select="column[(@desc or @fullname) and ($archief or (not(@archief='true') and not(@superclass-archief='true')))]">
					<xsl:variable name="comment">
						<xsl:choose>
							<xsl:when test="@desc and @fullname">
								<xsl:value-of select="@desc"/>
								<xsl:text> - </xsl:text>
								<xsl:value-of select="@fullname"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="@desc"/>
								<xsl:value-of select="@fullname"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<xsl:text>comment on column </xsl:text>
					<xsl:value-of select="$table-name"/>.<xsl:value-of select="@name"/>
					<xsl:text> is </xsl:text>
					<xsl:value-of select="db:string-literal($comment)"/>
					<xsl:text>;
</xsl:text>
				</xsl:for-each>
			</xsl:if>
			<xsl:text>
</xsl:text>
		</xsl:if>
	</xsl:template>
	<xsl:template match="table" mode="foreign-keys">
		<xsl:if test="foreign-key[not(@archief='true')]">
			<xsl:text>
-- Foreign keys voor tabel </xsl:text>
			<xsl:value-of select="@name"/>
			<xsl:text>
</xsl:text>
			<xsl:for-each select="foreign-key[not(@archief='true')]">
				<!--xsl:text>alter table </xsl:text><xsl:value-of select="../@name"/> drop constraint <xsl:value-of select="@name"/><xsl:text>;
</xsl:text-->
				<xsl:text>alter table </xsl:text>
				<xsl:value-of select="../@name"/> add constraint <xsl:value-of select="@name"/>
				<xsl:text> foreign key (</xsl:text>
				<xsl:value-of select="@columns"/>) references <xsl:value-of select="@ref-table"/> (<xsl:value-of select="@ref-columns"/>
				<xsl:text>) on delete</xsl:text>
				<xsl:choose>
					<xsl:when test="$dbtype='sqlserver'"><xsl:text> no action</xsl:text></xsl:when>
					<xsl:otherwise><xsl:text> cascade</xsl:text></xsl:otherwise>
				</xsl:choose>
				<xsl:text>;
</xsl:text>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>
	<xsl:template match="inserts">
		<xsl:for-each select="insert">
			<xsl:text>insert into </xsl:text>
			<xsl:value-of select="@table"/>
			<xsl:text> (</xsl:text>
			<xsl:call-template name="make-comma-separated">
				<xsl:with-param name="node-list" select="columns/column"/>
			</xsl:call-template>
			<xsl:text>) values (</xsl:text>
			<xsl:call-template name="make-comma-separated">
				<xsl:with-param name="node-list" select="values/value"/>
				<xsl:with-param name="escape" select="true()"/>
			</xsl:call-template>
			<xsl:text>);
</xsl:text>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="extra-scripts">
		<xsl:text>
-- Handmatige scripts

</xsl:text>
		<xsl:for-each select="script">
			<xsl:text>-- Script: </xsl:text>
			<xsl:value-of select="."/>
			<xsl:text>

</xsl:text>
			<xsl:value-of select="unparsed-text(concat('extra_scripts/',$dbtype,'/',.))"/>
		</xsl:for-each>
		
		<xsl:call-template name="insert-versienummer"/>
		
	</xsl:template>
	
	<!-- Context: None -->
	<!-- Maak van een node-list een comma separated string. Optioneel wordt de string geëscaped -->
	<!-- Param node-list: de node-list om gecommasepareert te worden (verplicht)-->
	<!-- Param escape (boolean) moeten de waardes geëscaped worden?(optioneel, default naar false)-->
	<xsl:template name="make-comma-separated">
		<xsl:param name="node-list"/>
		<xsl:param name="escape" select="false()"/>
		<xsl:value-of>
			<xsl:for-each select="$node-list">
				<xsl:choose>
					<xsl:when test="$escape = true()">
						<xsl:value-of select="db:string-literal(.)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="."/>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:if test="position() != last()">
					<xsl:text>,</xsl:text>
				</xsl:if>
			</xsl:for-each>
		</xsl:value-of>
	</xsl:template>

	<xsl:template name="insert-versienummer">
		<xsl:text>
-- brmo rsgb versienummer

INSERT INTO brmo_metadata (naam, waarde) VALUES ('brmoversie','</xsl:text><xsl:value-of select="$versie"/><xsl:text>');
</xsl:text>
	</xsl:template>
</xsl:stylesheet>
