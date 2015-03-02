<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">BRMO Processen</stripes:layout-component>
    <stripes:layout-component name="html_head">
        <%-- TODO naar aparte stylesheet --%>
        <style>
            .longTxt{width: 100%;}
        </style>
        <script type="text/javascript" src="${contextPath}/scripts/processen.js"></script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <h2>Automatische processen</h2>

        <stripes:messages />
        <stripes:errors />

        <h3>BRK scanners</h3>

        <stripes:form partial="true" action="">
            <stripes:button name="toevoegen" value="Toevoegen" onclick="addBRKScanner();" id="brkScannerAdd"/>
        </stripes:form>

        <c:if test="${not empty actionBean.brkProcessen}">
            <script>nextBrk=<c:out value="${fn:length(actionBean.brkProcessen)}"/>;</script>
            <c:forEach items="${actionBean.brkProcessen}" varStatus="i" var="brk" >
                <stripes:form beanclass="nl.b3p.brmo.service.stripes.AutoProcessenActionBean">
                    <fieldset>
                        <legend>
                            Scanner taak: ${brk.id} , status: ${brk.status},
                            <stripes:link beanclass="nl.b3p.brmo.service.stripes.SamenvattingActionBean">
                                <stripes:param name="procesId" value="${brk.id}" />
                                laatste run: <fmt:formatDate  pattern="${timeFormat}" value="${brk.lastrun}"/>
                            </stripes:link>
                        </legend>
                        <stripes:hidden name="brkProcessen[${i.index}].id" formatType="number"/>
                        <%-- PID wordt gebruikt voor start, stop en verwijder proces --%>
                        <input type="hidden" name="PID" value="${brk.id}"/>
                        <stripes:label name="">Scan directory
                            <stripes:text name="brkProcessen[${i.index}].scanDirectory" value="${brk.config.scanDirectory}" class="longTxt"/>
                        </stripes:label>
                        <br />
                        <stripes:label name="">Archief directory
                            <stripes:text name="brkProcessen[${i.index}].archiefDirectory" value="${brk.config.archiefDirectory}" class="longTxt"/>
                        </stripes:label>
                    </fieldset>
                    <stripes:submit name="save" value="Opslaan" />
                    <stripes:submit name="startProces" value="Start" />
                    <stripes:submit name="stopProces" value="Stop" disabled="true" />
                    <stripes:submit name="verwijderProces" value="Verwijderen" />
                </stripes:form>
            </c:forEach>
        </c:if>

        <h3>BAG scanners</h3>

        <stripes:form partial="true" action="">
            <stripes:button name="toevoegen" value="Toevoegen" onclick="addBAGScanner();"  id="bagScannerAdd"/>
        </stripes:form>

        <c:if test="${not empty actionBean.bagProcessen}">
            <script>nextBag=<c:out value="${fn:length(actionBean.bagProcessen)}"/>;</script>
            <c:forEach items="${actionBean.bagProcessen}" varStatus="i" var="bag" >
                <stripes:form beanclass="nl.b3p.brmo.service.stripes.AutoProcessenActionBean">
                    <fieldset>
                        <legend>
                            Scanner taak: ${bag.id} , status:${bag.status},
                            <stripes:link beanclass="nl.b3p.brmo.service.stripes.SamenvattingActionBean">
                                <stripes:param name="procesId" value="${bag.id}" />
                                laatste run: <fmt:formatDate  pattern="${timeFormat}" value="${bag.lastrun}"/>
                            </stripes:link>
                        </legend>
                        <stripes:hidden name="bagProcessen[${i.index}].id" formatType="number"/>
                        <%-- PID wordt gebruikt voor start, stop en verwijder proces --%>
                        <input type="hidden" name="PID" value="${bag.id}"/>
                        <stripes:label name="">Scan directory
                            <stripes:text name="bagProcessen[${i.index}].scanDirectory" value="${bag.config.scanDirectory}" class="longTxt" />
                        </stripes:label>
                        <br />
                        <stripes:label name="">Archief directory
                            <stripes:text name="bagProcessen[${i.index}].archiefDirectory" value="${bag.config.archiefDirectory}" class="longTxt" />
                        </stripes:label>
                    </fieldset>
                    <stripes:submit name="save" value="Opslaan" />
                    <stripes:submit name="startProces" value="Start" />
                    <stripes:submit name="stopProces" value="Stop" disabled="true" />
                    <stripes:submit name="verwijderProces" value="Verwijderen" />
                </stripes:form>
            </c:forEach>
        </c:if>

        <h3>Email Notificaties</h3>

        <stripes:form partial="true" action="">
            <stripes:button name="toevoegen" value="Toevoegen" onclick="addMailRapportage();"  id="mailRapportAdd"/>
        </stripes:form>

        <c:if test="${not empty actionBean.mailProcessen}">
            <c:set var="statusValues" value="<%=ProcessingStatus.values()%>"/>
            <script>nextMail=<c:out value="${fn:length(actionBean.mailProcessen)}"/>;</script>
            <c:forEach items="${actionBean.mailProcessen}" varStatus="i" var="mail" >
                <stripes:form beanclass="nl.b3p.brmo.service.stripes.AutoProcessenActionBean">
                    <fieldset>
                        <legend>
                            Rapportage taak: ${mail.id} , status: ${mail.status},
                            <stripes:link beanclass="nl.b3p.brmo.service.stripes.SamenvattingActionBean">
                                <stripes:param name="procesId" value="${mail.id}" />
                                laatste run: <fmt:formatDate  pattern="${timeFormat}" value="${mail.lastrun}"/>
                            </stripes:link>
                        </legend>
                        <stripes:hidden name="mailProcessen[${i.index}].id" formatType="number"/>
                        <%-- PID wordt gebruikt voor start, stop en verwijder proces --%>
                        <input type="hidden" name="PID" value="${mail.id}"/>
                        <stripes:label name="">Geaddresseerde(n)
                            <stripes:text name="mailProcessen[${i.index}].mailAdressen" value="${mail.config.email}" class="longTxt"/>
                        </stripes:label>
                        <stripes:label name="">Proces ID's voor rapportage
                            <stripes:text name="mailProcessen[${i.index}].config.pIDS" value="${mail.config.pIDS}" class="longTxt"/>
                        </stripes:label>
                        <stripes:label name="">Proces status voor rapportage
                            <stripes:select name="mailProcessen[${i.index}].config.forStatus">
                                <stripes:option value="">Alle</stripes:option>
                                <stripes:options-collection collection="${statusValues}"/>
                            </stripes:select>
                        </stripes:label>
                    </fieldset>
                    <stripes:submit name="save" value="Opslaan" />
                    <stripes:submit name="startProces" value="Start" />
                    <stripes:submit name="stopProces" value="Stop" disabled="true" />
                    <stripes:submit name="verwijderProces" value="Verwijderen" />
                </stripes:form>
            </c:forEach>
        </c:if>

    </stripes:layout-component>
</stripes:layout-render>