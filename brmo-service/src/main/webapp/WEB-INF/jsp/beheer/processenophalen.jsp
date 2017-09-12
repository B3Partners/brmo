<%@include file="/WEB-INF/taglibs.jsp" %>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">Inzien Automatische processen</stripes:layout-component>
    <stripes:layout-component name="contents">

        <% pageContext.setAttribute("newLine", "\n");%>

        <h2>Automatische processen</h2>

        <stripes:messages />
        <stripes:errors />

        <c:set var="l" value="${actionBean.processen}"/>
        <c:if test="${empty l}">
            <em>Nog geen processen gedefinieerd.</em>
        </c:if>

        <c:if test="${!empty l}">
            <table border="1" style="border-collapse: collapse" cellspacing="0">
                <thead>
                    <tr><th>Procestype (id)</th><th>Label</th><th>Laatste run</th><th>Planning (cron)</th><th>Status</th><th>Samenvatting</th><th>Log</th></tr>
                </thead>
                <tbody>
                    <c:forEach var="p" items="${l}">
                        <stripes:url var="url" beanclass="nl.b3p.brmo.service.stripes.OphaalConfigActionBean" event="edit">
                            <stripes:param name="proces">${p.id}</stripes:param>
                        </stripes:url>
                        <c:set var="selected" value="${p.id == form.id}"/>
                        <c:set var="bgcol" value="${selected ? '#cccccc' : 'white'}"/>


                        <tr style="background-color: ${bgcol}; cursor: pointer;"
                            onmouseover="this.style.background = '#cccccc';"
                            onmouseout="this.style.background = '${bgcol}';"
                            onclick="javascript: window.location.href = '${url}';"
                            tabindex="0">
                            <td><%= pageContext.getAttribute("p").getClass().getSimpleName() %> (${p.id})</td>
                            <td><c:out value="${p.config['label']}"/></td>
                            <td style="white-space: nowrap"><fmt:formatDate pattern="dd-MM-yyyy HH:mm:ss" value="${p.lastrun}"/></td>
                            <td style="white-space: nowrap"><c:out value="${p.cronExpressie}"/></td>
                            <td><c:out value="${p.status}"/></td>
                            <td><c:out value="${fn:replace(p.samenvatting, newLine, '<br />')}" escapeXml="false"/></td>
                            <td>
                                <stripes:link beanclass="nl.b3p.brmo.service.stripes.SamenvattingActionBean">
                                    <stripes:param name="procesId" value="${p.id}"/>log file
                                </stripes:link>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:if>

        <p />

        <stripes:form beanclass="nl.b3p.brmo.service.stripes.OphaalConfigActionBean">

            <c:set var="_new" value="${actionBean.context.eventName == 'add'}"/>
            <c:set var="edit" value="${actionBean.context.eventName == 'view' && !empty actionBean.proces}"/>

            <c:if test="${_new}">
                <stripes:hidden name="type"/>
            </c:if>
            <c:if test="${edit}">
                <stripes:hidden name="proces"/>
            </c:if>

            <c:if test="${edit || _new}">
                <stripes:submit name="save">Opslaan</stripes:submit>
                <c:if test="${!_new}">
                    <stripes:submit name="delete" onclick="return confirm('Weet u zeker dat u dit proces wilt verwijderen?')">Verwijderen</stripes:submit>
                </c:if>
                <stripes:submit name="cancel">Annuleren</stripes:submit>
            </c:if>

            <c:if test="${!edit && !_new}">
                <stripes:select name="type">
                    <stripes:options-enumeration enum="nl.b3p.brmo.service.scanner.ProcesExecutable.ProcessingImple" />
                </stripes:select>
                <stripes:submit name="add">Toevoegen</stripes:submit>
            </c:if>

            <c:if test="${edit || _new}">

                <c:choose>
                    <c:when test="${actionBean.type eq 'MailRapportageProces'}">
                        <jsp:include page="editmailproces.jsp" />
                        <stripes:url var="url" beanclass="nl.b3p.brmo.service.stripes.MailProcesUitvoerActionBean">
                            <stripes:param name="proces">${actionBean.proces.id}</stripes:param>
                        </stripes:url>
                    </c:when>

                    <c:when test="${actionBean.type eq 'BAGScannerProces'}">
                        <jsp:include page="editdirscannerproces.jsp" />
                        <stripes:url var="url" beanclass="nl.b3p.brmo.service.stripes.DirectoryScannerUitvoerActionBean">
                            <stripes:param name="proces">${actionBean.proces.id}</stripes:param>
                        </stripes:url>
                    </c:when>

                    <c:when test="${actionBean.type eq 'BRKScannerProces'}">
                        <jsp:include page="editbrkdirscannerproces.jsp" />
                        <stripes:url var="url" beanclass="nl.b3p.brmo.service.stripes.DirectoryScannerUitvoerActionBean">
                            <stripes:param name="proces">${actionBean.proces.id}</stripes:param>
                        </stripes:url>
                    </c:when>

                    <c:when test="${actionBean.type eq 'WebMirrorBAGScannerProces'}">
                        <jsp:include page="editwebdirscannerproces.jsp" />
                        <stripes:url var="url" beanclass="nl.b3p.brmo.service.stripes.DirectoryScannerUitvoerActionBean">
                            <stripes:param name="proces">${actionBean.proces.id}</stripes:param>
                        </stripes:url>
                    </c:when>

                    <c:when test="${actionBean.type eq 'GDS2OphaalProces'}">
                        <jsp:include page="editgds2proces.jsp" />
                        <stripes:url var="url" beanclass="nl.b3p.brmo.service.stripes.GDS2OphalenUitvoerActionBean">
                            <stripes:param name="proces">${actionBean.proces.id}</stripes:param>
                        </stripes:url>
                    </c:when>

                    <c:when test="${actionBean.type eq 'BerichtTransformatieProces'}">
                        <jsp:include page="edittransformatieproces.jsp" />
                        <stripes:url var="url" beanclass="nl.b3p.brmo.service.stripes.TransformActionBean">
                            <stripes:param name="transformAll" />
                        </stripes:url>
                    </c:when>

                    <c:when test="${actionBean.type eq 'BerichtDoorstuurProces'}">
                        <jsp:include page="editberichtdoorsturenproces.jsp" />
                        <stripes:url var="url" beanclass="nl.b3p.brmo.service.stripes.BerichtDoorsturenUitvoerActionBean">
                            <stripes:param name="proces">${actionBean.proces.id}</stripes:param>
                        </stripes:url>
                    </c:when>

                    <c:when test="${actionBean.type eq 'BGTLightScannerProces'}">
                        <jsp:include page="editbgtlightscannerproces.jsp" />
                        <stripes:url var="url" beanclass="nl.b3p.brmo.service.stripes.DirectoryScannerUitvoerActionBean">
                            <stripes:param name="proces">${actionBean.proces.id}</stripes:param>
                        </stripes:url>
                    </c:when>

                    <c:when test="${actionBean.type eq 'TopNLScannerProces'}">
                        <jsp:include page="editbgtlightscannerproces.jsp" />
                        <stripes:url var="url" beanclass="nl.b3p.brmo.service.stripes.DirectoryScannerUitvoerActionBean">
                            <stripes:param name="proces">${actionBean.proces.id}</stripes:param>
                        </stripes:url>
                    </c:when>

                    <c:when test="${actionBean.type eq 'BGTLightOphaalProces'}">
                        <jsp:include page="editbgtlightophaalproces.jsp" />
                        <stripes:url var="url" beanclass="nl.b3p.brmo.service.stripes.BGTOphalenUitvoerActionBean">
                            <stripes:param name="proces">${actionBean.proces.id}</stripes:param>
                            <stripes:param name="title">BGT Light GML</stripes:param>
                        </stripes:url>
                    </c:when>

                            <c:when test="${actionBean.type eq 'LaadprocesTransformatieProces'}">
                                <jsp:include page="editlaadprocestransformatieproces.jsp" />
                                <stripes:url var="url" beanclass="nl.b3p.brmo.service.stripes.LaadprocesTransformatieUitvoerActionBean">
                                    <stripes:param name="proces">${actionBean.proces.id}</stripes:param>
                                </stripes:url>
                            </c:when>

                            <c:when test="${actionBean.type eq 'MaterializedViewRefresh'}">
                                <jsp:include page="editmviewrefreshproces.jsp" />
                                <stripes:url var="url" beanclass="nl.b3p.brmo.service.stripes.MViewRefreshActionBean">
                                    <stripes:param name="proces">${actionBean.proces.id}</stripes:param>
                                </stripes:url>
                            </c:when>

                            <c:when test="${actionBean.type eq 'BerichtstatusRapportProces'}">
                                <jsp:include page="berichtstatusrapportedit.jsp" />
                                <stripes:url var="url" beanclass="nl.b3p.brmo.service.stripes.BerichtstatusRapportActionBean">
                                    <stripes:param name="proces">${actionBean.proces.id}</stripes:param>
                                </stripes:url>
                            </c:when>

                            <c:otherwise>
                                <p>Onbekende input</p>
                            </c:otherwise>
                </c:choose>

                <stripes:submit name="execute" onclick="if(confirm('Let op! Het proces moet eerst zijn opgeslagen. \nCancel; eerst opslaan, OK; Toch verder gaan?')) window.open('${url}');">Uitvoeren</stripes:submit>

            </c:if>
        </stripes:form>
        <p>
            Documentatie van de automatische processen is beschikbaar op <a href="https://github.com/B3Partners/brmo/wiki/Automatische-processen" target="_blank">de wiki</a>.
        </p>
    </stripes:layout-component>
</stripes:layout-render>
