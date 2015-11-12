@echo off
rmdir /s/q wsdl\src\java\net 2>Nul
rmdir /s/q wsdl\src\java\nl 2>Nul
rmdir /s/q wsdl\src\java\org 2>Nul
call wsimport -verbose -Xnocompile -d wsdl\src\java -b bindings.xml wsdl/imgeo0300_ontvangAsynchroon_vert_LV.wsdl -wsdllocation imgeo0300_ontvangAsynchroon_vert_LV.wsdl