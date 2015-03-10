<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">BRMO Processen</stripes:layout-component>
    <stripes:layout-component name="html_head">
        <%-- TODO naar aparte stylesheet --%>
        <style>
            .longTxt{width: 100%;}
            .halfTxt{width: 50%;}
            form{padding-top: 1em;}
        </style>
        <script type="text/javascript" src="${contextPath}/scripts/processen.js"></script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <h2>Automatische processen</h2>

        <stripes:messages />
        <stripes:errors />

        <h3>BRK directory scanners</h3>

        <stripes:form partial="true" action="">
            <stripes:button name="toevoegen" value="Toevoegen" onclick="addBRKScanner();" id="brkScannerAdd"/>
        </stripes:form>

        <c:if test="${not empty actionBean.brkProcessen}">
            <script>nextBrk =<c:out value="${fn:length(actionBean.brkProcessen)}"/>;</script>
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
                        <stripes:label name="">Label
                            <stripes:text name="brkProcessen[${i.index}].config.label" value="${brk.config.label}" class="halfTxt"/>
                        </stripes:label>
                        <br />
                        <stripes:label name="">Scan directory
                            <stripes:text name="brkProcessen[${i.index}].scanDirectory" value="${brk.config.scanDirectory}" class="longTxt"/>
                        </stripes:label>
                        <br />
                        <stripes:label name="">Archief directory
                            <stripes:text name="brkProcessen[${i.index}].archiefDirectory" value="${brk.config.archiefDirectory}" class="longTxt"/>
                        </stripes:label>
                        <stripes:label name="">Planning (cron expressie)
                            <stripes:text name="brkProcessen[${i.index}].cron_expressie" value="${brk.cron_expressie}" class="halfTxt"/>
                        </stripes:label>
                    </fieldset>
                    <stripes:submit name="save" value="Opslaan" />
                    <stripes:submit name="startProces" value="Start" />
                    <stripes:submit name="stopProces" value="Stop" disabled="true" />
                    <stripes:submit name="verwijderProces" value="Verwijderen" />
                </stripes:form>
            </c:forEach>
        </c:if>

        <h3>BAG directory scanners</h3>

        <stripes:form partial="true" action="">
            <stripes:button name="toevoegen" value="Toevoegen" onclick="addBAGScanner();"  id="bagScannerAdd"/>
        </stripes:form>

        <c:if test="${not empty actionBean.bagProcessen}">
            <script>nextBag =<c:out value="${fn:length(actionBean.bagProcessen)}"/>;</script>
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
                        <stripes:label name="">Label
                            <stripes:text name="bagProcessen[${i.index}].config.label" value="${bag.config.label}" class="halfTxt"/>
                        </stripes:label>
                        <br />
                        <stripes:label name="">Scan directory
                            <stripes:text name="bagProcessen[${i.index}].scanDirectory" value="${bag.config.scanDirectory}" class="longTxt" />
                        </stripes:label>
                        <br />
                        <stripes:label name="">Archief directory
                            <stripes:text name="bagProcessen[${i.index}].archiefDirectory" value="${bag.config.archiefDirectory}" class="longTxt" />
                        </stripes:label>
                        <stripes:label name="">Planning (cron expressie)
                            <stripes:text name="bagProcessen[${i.index}].cron_expressie" value="${bag.cron_expressie}" class="halfTxt"/>
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
            <script>nextMail =<c:out value="${fn:length(actionBean.mailProcessen)}"/>;</script>
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
                        <stripes:label name="">Label
                            <stripes:text name="mailProcessen[${i.index}].config.label" value="${mail.config.label}" class="halfTxt"/>
                        </stripes:label>
                        <br />
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
                        <br/>
                        <stripes:label name="">Planning (cron expressie)
                            <stripes:text name="mailProcessen[${i.index}].cron_expressie" value="${mail.cron_expressie}" class="halfTxt"/>
                        </stripes:label>
                    </fieldset>
                    <stripes:submit name="save" value="Opslaan" />
                    <stripes:submit name="startProces" value="Start" />
                    <stripes:submit name="stopProces" value="Stop" disabled="true" />
                    <stripes:submit name="verwijderProces" value="Verwijderen" />
                </stripes:form>
            </c:forEach>
        </c:if>

        <h3>BRMO Bericht Transformatie processen</h3>
        <stripes:form partial="true" action="">
            <stripes:button name="toevoegen" value="Toevoegen" onclick="transformatieAdd();"  id="transformatieAdd"/>
        </stripes:form>

        <c:if test="${not empty actionBean.brmoProcessen}">
            <script>nextBRMO =<c:out value="${fn:length(actionBean.brmoProcessen)}"/>;</script>
            <c:forEach items="${actionBean.brmoProcessen}" varStatus="i" var="brmo" >
                <stripes:form beanclass="nl.b3p.brmo.service.stripes.AutoProcessenActionBean">
                    <fieldset>
                        <legend>
                            BRMO Transformatie taak: ${brmo.id} , status: ${brmo.status},
                            <stripes:link beanclass="nl.b3p.brmo.service.stripes.SamenvattingActionBean">
                                <stripes:param name="procesId" value="${brmo.id}" />
                                laatste run: <fmt:formatDate  pattern="${timeFormat}" value="${brmo.lastrun}"/>
                            </stripes:link>
                        </legend>
                        <stripes:hidden name="brmoProcessen[${i.index}].id" formatType="number"/>
                        <stripes:hidden name="brmoProcessen[${i.index}].config.transformAll" value="true" />
                        <%-- PID wordt gebruikt voor start, stop en verwijder proces --%>
                        <input type="hidden" name="PID" value="${brmo.id}"/>
                        <stripes:label name="">Label
                            <stripes:text name="brmoProcessen[${i.index}].config.label" value="${brmo.config.label}" class="halfTxt"/>
                        </stripes:label>
                        <br/>
                         <stripes:label name="">Planning (cron expressie)
                            <stripes:text name="brmoProcessen[${i.index}].cron_expressie" value="${brmo.cron_expressie}" class="halfTxt"/>
                        </stripes:label>
                    </fieldset>

                    <stripes:url var="url" beanclass="nl.b3p.brmo.service.stripes.TransformActionBean">
                        <stripes:param name="transformAll">true</stripes:param>
                    </stripes:url>
                    <stripes:submit name="save" value="Opslaan" />
                    <stripes:button name="execute" onclick="if(confirm('Alle berichten worden getransformeerd. Verder gaan?')) window.open('${url}');">Start</stripes:button>
                    <stripes:submit name="stopProces" value="Stop" disabled="true" />
                    <stripes:submit name="verwijderProces" value="Verwijderen" />
                </stripes:form>
            </c:forEach>


        </c:if>



        <h3>Ophalen GDS2 leveringen</h3>

        <stripes:form partial="true" action="">
            <stripes:button name="toevoegen" value="Toevoegen" onclick="addGDS2Ophalen();"  id="gds2OphalenAdd"/>
        </stripes:form>

        <c:if test="${not empty actionBean.gds2Processen}">
            <script>nextGDS =<c:out value="${fn:length(actionBean.gds2Processen)}"/>;</script>
            <c:forEach items="${actionBean.gds2Processen}" varStatus="i" var="gds" >
                <stripes:form beanclass="nl.b3p.brmo.service.stripes.AutoProcessenActionBean">
                    <fieldset>
                        <legend>
                            GDS2 levering taak: ${gds.id} , status: ${gds.status},
                            <stripes:link beanclass="nl.b3p.brmo.service.stripes.SamenvattingActionBean">
                                <stripes:param name="procesId" value="${gds.id}" />
                                laatste run: <fmt:formatDate  pattern="${timeFormat}" value="${gds.lastrun}"/>
                            </stripes:link>
                        </legend>
                        <stripes:hidden name="gds2Processen[${i.index}].id" formatType="number"/>
                        <%-- PID wordt gebruikt voor start, stop en verwijder proces --%>
                        <input type="hidden" name="PID" value="${gds.id}"/>
                        <stripes:label name="">Label
                            <stripes:text name="gds2Processen[${i.index}].config.label" value="${gds.config.label}" class="halfTxt"/>
                        </stripes:label>
                        <br />
                        <stripes:label name="">Afleveringsendpoint
                            <stripes:text name="gds2Processen[${i.index}].config.delivery_endpoint"  class="longTxt"/>
                        </stripes:label>
                        <stripes:label name="">Contractnummer
                            <stripes:text name="gds2Processen[${i.index}].config.gds2_contractnummer" size="20" />
                        </stripes:label>
                        <br/>
                        <stripes:label name="">Pad naar keystore:
                            <stripes:text name="gds2Processen[${i.index}].config.keystore_path" size="80"/>
                        </stripes:label>
                        <stripes:label name="">Wachtwoord
                            <stripes:password name="gds2Processen[${i.index}].config.keystore_password" size="20"/>
                        </stripes:label>
                        <stripes:label name="">Planning (cron expressie)
                            <stripes:text name="gds2Processen[${i.index}].cron_expressie" value="${gds2.cron_expressie}" class="halfTxt"/>
                        </stripes:label>
                    </fieldset>
                    <stripes:url var="url" beanclass="nl.b3p.brmo.service.stripes.GDS2OphalenUitvoerActionBean">
                        <stripes:param name="proces">${gds.id}</stripes:param>
                    </stripes:url>
                    <stripes:submit name="save" value="Opslaan" />
                    <stripes:button name="execute" onclick="if(confirm('Let op! Proces moet eerst zijn opgeslagen. Verder gaan?')) window.open('${url}');">Start</stripes:button>
                    <stripes:submit name="stopProces" value="Stop" disabled="true" />
                    <stripes:submit name="verwijderProces" value="Verwijderen" />
                </stripes:form>
            </c:forEach>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>