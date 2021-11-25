<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%--
  ~ Copyright (C) 2021 B3Partners B.V.
  ~
  ~ SPDX-License-Identifier: MIT
  ~
  --%>

<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">BAG 2.0 inladen</stripes:layout-component>
    <stripes:layout-component name="html_head">
        <script type="text/javascript" src="${contextPath}/scripts/moment-with-locales.min.js"></script>
        <script type="text/javascript">
            moment.locale("nl");
        </script>
    </stripes:layout-component>
    <stripes:layout-component name="contents">
        <h1>BAG 2.0 inladen</h1>
        <p>
            Geef het pad of de URL naar de BAG 2.0 stand of mutaties op in onderstaan tekstveld. Dit kan een publiek te
            downloaden URL zijn (zonder authenticatie) zoals de standaard ingevulde stand van heel Nederland.
        </p>
        <p>
            Het is ook mogelijk om URLs op te geven van de <a href="https://extracten.bag.kadaster.nl/lvbag/extracten/" target="_blank">publieke server</a>
            met BAG 2.0 extracten, zolang deze nog beschikbaar is.
        </p>
        <p>
            Het is momenteel (nog) niet mogelijk om BAG 2.0 bestanden direct te laden vanaf de "BAG Bestanden" dienst
            van het Kadaster. Deze moeten eerst gedownload worden en dan kunnen deze geladen worden vanaf het
            bestandssysteem van de server. <b>Let op! Handmatig downloaden is dus alleen nodig als je gemeente stand(en)
            wil inladen, voor heel Nederland is de stand publiek beschikbaar en is de URL al standaard ingevuld!</b>
        </p>
        <p>
            Mutaties kunnen wel vanaf de "BAG Bestanden" dienst of een publieke mirror daarvan worden geladen. Maak
            daarvoor na het inladen van de stand een automatisch proces. Het is ook mogelijk om handmatig mutaties in te
            laden door de bestandsnamen hieronder op te geven.
        </p>
        <p>
            Om meerdere gemeentes in te laden moeten de stand- en mutatiebestanden van alle gemeentes tegelijkertijd
            worden verwerkt. Dit is nodig om efficient panden die op de gemeentegrenzen liggen en dus in meerdere
            gemeentestanden voorkomen niet dubbel in te laden. In het tekstvak kunnen daarom meerdere regels met bestanden
            of URLs worden opgenomen. Mutatiebestanden kunnen in willekeurige volgorde worden opgegeven. Meerdere
            dagmutatiebestanden kunnen ook tegelijk worden opgegeven.
        </p>
        <stripes:messages/>
        <stripes:errors/>
        <stripes:form beanclass="nl.b3p.brmo.service.stripes.BAG2LoadActionBean" focus="">
            <h2>Databaseverbinding</h2>
            <p>
            De BAG gegevens worden ingeladen naar de JNDI databaseconnectie <code>jdbc/brmo/rsgbbag</code>. Deze moet voor Tomcat
            in <code>conf/server.xml</code> gedefinieerd worden.
            </p>
            <p>
                <h4>Controles en status</h4>
                <ul>
                    <li>
                        <b>JNDI resource:</b>
                        <c:choose>
                            <c:when test="${actionBean.rsgbbag != null}">
                                <span style="color: green; font-weight: bold">GOED: </span> JNDI resource gevonden
                            </c:when>
                            <c:otherwise>
                                <span style="color: red; font-weight: bold">FOUT: </span> JNDI resource niet gevonden! Voeg deze toe aan <code>conf/server.xml</code> van Tomcat en herstart Tomcat.
                                <p style="margin-left: 30px"><b>Details:</b> <code><c:out value="${actionBean.namingException}"/></code></p>
                            </c:otherwise>
                        </c:choose>
                    </li>
                    <li>
                        <b>Verbinding maken:</b>
                        <c:choose>
                            <c:when test="${actionBean.rsgbbag == null}">-</c:when>
                            <c:otherwise>
                                <c:choose>
                                    <c:when test="${actionBean.connectionOk}">
                                        <span style="color: green; font-weight: bold">GOED: </span> verbinding gemaakt
                                        <p style="margin-left: 30px"><b>Details:</b>
                                        <table style="margin-left: 30px">
                                            <tr><td>Connectie-string:</td><td><b><c:out value="${actionBean.connectionString}"/></b></td></tr>
                                            <tr><td>Database:</td><td><b><c:out value="${actionBean.databaseName}"/></b></td></tr>
                                            <tr><td>Gebruikersnaam:</td><td><b><c:out value="${actionBean.databaseUserName}"/></b></td></tr>
                                        </table>
                                        </p>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color: red; font-weight: bold">FOUT: </span> verbinding kan niet worden gemaakt.
                                        <p style="margin-left: 30px"><b>Details:</b>  <code><c:out value="${actionBean.connectionException}"/></code></p>
                                    </c:otherwise>
                                </c:choose>
                            </c:otherwise>
                        </c:choose>
                    </li>
                    <c:choose>
                        <c:when test="${actionBean.databaseDialect == 'postgis'}">
                            <li>
                                In PostgreSQL wordt de BAG2 altijd in het <code>bag</code> schema geladen. De definitie van
                                de <code>rsgbbag</code> JNDI resource kan exact hetzelfde zijn als die voor <code>rsgb</code>,
                                maar een andere database is ook mogelijk. Om views te maken die bestaande RSGB tabellen koppelen
                                met de BAG moet dezelfde database gebruikt worden.
                            </li>
                        </c:when>
                        <c:when test="${actionBean.databaseDialect == 'oracle'}">
                            <li>
                                <b>Oracle schema:</b>
                                <c:choose>
                                    <c:when test="${!fn:contains(fn:toLowerCase(actionBean.databaseUserName), 'bag')}">
                                        <span style="color: red; font-weight: bold">FOUT: </span> gebruikersnaam <code><b><c:out value="${actionBean.databaseUserName}"/></b></code> bevat niet "<code>bag</code>", wordt wel een andere gebruiker (schema) gebruikt dan voor de <code>rsgb</code> connectie?
                                        <c:set var="oracleConnectionOk" value="false"/>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="color: green; font-weight: bold">GOED: </span> gebruikersnaam bevat "<code>bag</code>", er wordt een andere gebruiker (schema) gebruikt dan voor de <code>rsgb</code> connectie.
                                        <c:set var="oracleConnectionOk" value="true"/>
                                    </c:otherwise>
                                </c:choose>
                            </li>
                        </c:when>
                    </c:choose>
                    <c:if test="${actionBean.connectionOk}">
                        <li>
                            <b>Status huidige stand:</b>
                            <c:choose>
                                <c:when test="${empty actionBean.currentTechnischeDatum}">Nog niet ingeladen</c:when>
                                <c:otherwise>
                                    <script>
                                        const standLoadTechnischeDatum = moment("<fmt:formatDate pattern="yyyy-MM-dd" value="${actionBean.standLoadTechnischeDatum}"/>", "YYYY-MM-DD HH:mm:ss");
                                        const currentTechnischeDatum = moment("<fmt:formatDate pattern="yyyy-MM-dd" value="${actionBean.currentTechnischeDatum}"/>", "YYYY-MM-DD");
                                    </script>
                                    <p style="margin-left: 30px">
                                        Stand ingeladen op <b><fmt:formatDate pattern="dd-MM-yyyy HH:mm:ss" value="${actionBean.standLoadTime}"/></b> met technische datum <b><fmt:formatDate pattern="dd-MM-yyyy" value="${actionBean.standLoadTechnischeDatum}"/>
                                        <script type="text/javascript">
                                            document.write(`(${'${standLoadTechnischeDatum.fromNow()}'}).`);
                                        </script></b>
                                        Alhoewel de BAG database met mutaties actueel kan worden gehouden, is het advies is om minstens &eacute&eacuten keer per jaar de BAG stand opnieuw in te laden.
                                    </p>
                                    <p style="margin-left: 30px">
                                        Huidige technische datum: <b><fmt:formatDate pattern="dd-MM-yyyy" value="${actionBean.currentTechnischeDatum}"/>
                                        <script type="text/javascript">
                                            document.write(`(${'${currentTechnischeDatum.fromNow()}'}).`);
                                        </script></b>
                                        Configureer een automatisch proces om mutaties toe te passen als de datum ouder is dan &eacute;&eacuten dag.
                                    </p>
                                </c:otherwise>
                            </c:choose>
                        </li>
                    </c:if>
                </ul>
            </p>
            <h2>Lijst met URLs of bestanden vanaf het bestandssysteem van de server</h2></td></tr>
            <stripes:textarea name="files" cols="120" rows="6"/>
            <c:set var="loadDisabled" value="${!actionBean.connectionOk or (actionBean.databaseDialect == 'oracle' and !oracleConnectionOk)}"/>
            <stripes:submit name="load" value="Inladen" disabled="${loadDisabled}" />
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>