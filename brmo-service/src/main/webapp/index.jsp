<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="contents">
        <h1>BRMO Service</h1>
        <p>
            Deze service werkt met de RSGB database van B3Partners en kan
            Basisregistratie berichten laden in deze RSGB database. Deze service
            kan een eenmalige stand inlezen of mutaties van de BAG, BRK, NHR en BGT;
            andere registraties kunnen worden toegevoegd.
        </p>
        <p>
            Deze service werkt op twee manieren:
        <ul>
            <li>Gekoppeld met een servicebus, waarbij berichten door de 
            servicebus automatisch aan de BRMO service worden aangeboden;</li>
            <li>Handmatig kan een bestand worden geupload via deze website.</li>
        </ul>
            In beide gevallen wordt het bestand als laadproces in een lijst 
            getoond. Elk bestand wordt bij het laden opgesplitst in individuele
            berichten (zowel een stand- als een mutatielevering kan meerdere
            berichten bevatten).
        </p>
        <p>
            De individuele berichten worden in een aparte lijst getoond. Hierin
            zijn referentienummer en volgnummer zichtbaar. Alle berichten kunnen
            in &eacute;&eacute;n keer of individueel geladen worden in de RSGB database. 
            Alleen berichten met de status STAGING_OK worden geladen. Bij het
            laden van meerdere berichten wordt de juiste volgorde gebruikt.
        </p>
    </stripes:layout-component>
</stripes:layout-render>