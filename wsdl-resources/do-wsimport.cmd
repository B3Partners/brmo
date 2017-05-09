@echo off
FOR /D %%p IN ("src\java\*.*") DO rmdir "%%p" /s /q
call wsimport -verbose -Xnocompile -d src\java -b wsdl/stuf-bg204/bg0204/stuf-bg204-bindings.xml wsdl/stuf-bg204/bg0204/bg0204.wsdl -wsdllocation bg0204.wsdl


