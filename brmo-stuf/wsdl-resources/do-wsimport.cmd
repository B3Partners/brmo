@echo off
FOR /D %%p IN ("src\java\*.*") DO rmdir "%%p" /s /q
call wsimport -verbose -Xnocompile -d src\java -b wsdl/stuf-imgeo0300-vertikaal/stuf-imgeo0300-vertikaal-bindings.xml wsdl/stuf-imgeo0300-vertikaal/imgeo0300_ontvangAsynchroon_vert_LV.wsdl -wsdllocation imgeo0300_ontvangAsynchroon_vert_LV.wsdl