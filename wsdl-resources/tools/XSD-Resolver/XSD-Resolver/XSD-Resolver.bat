@echo off
rem set saxon_path=D:\Data\Projecten\KING\Versterking-StUF\StUF\StUFmaster\tools\XSD-Resolver\saxon\saxon9he.jar
set saxon_path=saxon\saxon9he.jar

@echo on

del /Q generated\*.*
del /Q consolidated\*.xsd
del /Q finalized\*.xsd

cls

java -Xmx1024m -jar %saxon_path% -o:generated/temp.xml -s:input/configuration.xml -xsl:xslt/resolveXSDs.xslt versie=