<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

<ul>
    <li><stripes:link href="/index.jsp">&#155; Home</stripes:link></li>
    <li><stripes:link beanclass="nl.b3p.brmo.service.stripes.BasisregistratieFileUploadActionBean">&#155; Bestand inladen</stripes:link></li>
    <li><stripes:link beanclass="nl.b3p.brmo.service.stripes.LaadProcesActionBean">&#155; Laadprocessen</stripes:link></li>
    <li><stripes:link beanclass="nl.b3p.brmo.service.stripes.BerichtenActionBean">&#155; Berichten</stripes:link></li>
    <li><stripes:link beanclass="nl.b3p.brmo.service.stripes.OphaalConfigActionBean">&#155; Automatische processen</stripes:link></li>
    <li><stripes:link href="/logs.jsp">&#155; Logfiles</stripes:link></li>
    <li><stripes:link href="/about.jsp">&#155; Versie Informatie</stripes:link></li>
    <li><stripes:link href="/logout.jsp">&#155; Uitloggen</stripes:link></li>
</ul>