<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">BRMO Processen</stripes:layout-component>
    <stripes:layout-component name="html_head">
        <stripes:layout-component name="html_head">
            <%-- HEAD sectie --%>
            <style>
                .longTxt{width: 100%;}
            </style>

        </stripes:layout-component>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <h2>Automatische processen</h2>

        <stripes:messages />
        <stripes:errors />
        <h3>BRK scanners</h3>

        <script language="javascript">
            function addBRKScanner() {
                alert('TODO BRKScanner formulier invoegen');
            }
        </script>

        <stripes:form partial="true" action="">
            <p><stripes:button name="toevoegen" value="Toevoegen" onclick="addBRKScanner();"/></p>
        </stripes:form>

        <c:if test="${not empty actionBean.brkProcessen}">
            <c:forEach items="${actionBean.brkProcessen}" varStatus="i" var="brk" >
                <stripes:form beanclass="nl.b3p.brmo.service.stripes.AutoProcessenActionBean">
                    <fieldset>
                        <legend>Scanner ID: ${brk.id} , status: ${brk.status}, laatste run: ${brk.lastrun}</legend>
                        <stripes:hidden name="brkProcessen[${i.index}].id" formatType="number"/>
                        <%-- pID wordt gebruikt voor start, stop en verwijder proces --%>
                        <stripes:hidden name="pId" value="${brk.id}" formatType="number"/>
                        <stripes:label name="">Scan directory
                            <stripes:text name="brkProcessen[${i.index}].scanDirectory" value="${brk.scanDirectory}" class="longTxt"/>
                        </stripes:label>
                        <br />
                        <stripes:label name="">Archief directory
                            <stripes:text name="brkProcessen[${i.index}].archiefDirectory" value="${brk.archiefDirectory}" class="longTxt"/>
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

        <script language="javascript">
            function addBAGScanner() {
                alert('TODO BAGScanner formulier invoegen');
            }
        </script>

        <stripes:form partial="true" action="">
            <p><stripes:button name="toevoegen" value="Toevoegen" onclick="addBAGScanner();"/></p>
        </stripes:form>

        <c:if test="${not empty actionBean.bagProcessen}">
            <c:forEach items="${actionBean.bagProcessen}" varStatus="j" var="bag" >
                <stripes:form beanclass="nl.b3p.brmo.service.stripes.AutoProcessenActionBean">
                    <fieldset>
                        <legend>Scanner ID: ${bag.id} , status: ${bag.status}, laatste run: ${bag.lastrun}</legend>
                        <stripes:hidden name="bagProcessen[${i.index}].id" formatType="number"/>
                        <%-- pID wordt gebruikt voor start, stop en verwijder proces --%>
                        <stripes:hidden name="pId" value="${bag.id}" formatType="number"/>
                        <stripes:label name="">Scan directory
                            <stripes:text name="bagProcessen[${i.index}].scanDirectory" value="${bag.scanDirectory}" class="longTxt" />
                        </stripes:label>
                        <br />
                        <stripes:label name="">Archief directory
                            <stripes:text name="bagProcessen[${i.index}].archiefDirectory" value="${bag.archiefDirectory}" class="longTxt" />
                        </stripes:label>
                    </fieldset>
                    <stripes:submit name="save" value="Opslaan" />
                    <stripes:submit name="startProces" value="Start" />
                    <stripes:submit name="stopProces" value="Stop" disabled="true" />
                    <stripes:submit name="verwijderProces" value="Verwijderen" />
                </stripes:form>
            </c:forEach>
        </c:if>


        <h3>Notificaties</h3>

        <script language="javascript">
            function addMailRapportage() {
                alert('TODO MailRapportage formulier invoegen');
            }
        </script>

        <stripes:form partial="true" action="">
            <p><stripes:button name="toevoegen" value="Toevoegen" onclick="addMailRapportage();"/></p>
        </stripes:form>

        <c:if test="${not empty actionBean.mailProcessen}">
            <c:forEach items="${actionBean.mailProcessen}" varStatus="i" var="mail" >
                <stripes:form beanclass="nl.b3p.brmo.service.stripes.AutoProcessenActionBean">
                    <fieldset>
                        <legend>Scanner ID: ${mail.id} , status: ${mail.status}, laatste run: ${mail.lastrun}</legend>
                        <stripes:hidden name="mailProcessen[${i.index}].id" formatType="number"/>
                        <%-- pID wordt gebruikt voor start, stop en verwijder proces --%>
                        <stripes:hidden name="pId" value="${mail.id}" formatType="number"/>
                        <stripes:label name="">Geaddresseerde(n)
                            <stripes:text name="mailProcessen[${i.index}].mailAdressen" value="${mail.config.email}" class="longTxt"/>
                        </stripes:label>
                        <stripes:label name="">Proces ID's voor rapportage
                            <stripes:text name="mailProcessen[${i.index}].config.pIDS" value="${mail.config.pIDS}" class="longTxt"/>
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