<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">BRMO Processen</stripes:layout-component>
    <stripes:layout-component name="html_head">
        <stripes:layout-component name="html_head">
            <%-- HEAD sectie --%>
            <style>
                .dirPath{width: 100%;}
            </style>

        </stripes:layout-component>
    </stripes:layout-component>

    <stripes:layout-component name="contents">
        <h2>Directory scanners</h2>

        <stripes:messages />
        <stripes:errors />
        
        <h3>BRK scanners</h3>

        <stripes:form partial="true" action="">
            <p><stripes:button name="toevoegen" value="Toevoegen" disabled="true" onclick="alert('TODO formulier invoegen');"/></p>
        </stripes:form>
        <c:when test="${not empty actionBean.brkProcessen}">
            <c:forEach items="${actionBean.brkProcessen}" varStatus="i" var="brk" >
                <stripes:form beanclass="nl.b3p.brmo.service.stripes.AutoProcessenActionBean" focus="">
                    <fieldset>
                        <legend>Scanner ID: ${brk.id} , status: ${brk.status}, laatste run: ${brk.lastrun}</legend>
                        <stripes:hidden name="brkProcessen[${i.index}].id" />
                        <%-- pID wordt gebruikt voor start, stop en verwijder proces --%>
                        <stripes:hidden name="pId" value="${brk.id}"/>
                        <stripes:label name="">Scan directory
                            <stripes:text name="brkProcessen[${i.index}].scanDirectory" value="${brk.scanDirectory}" class="dirPath"/>
                        </stripes:label>
                        <br />
                        <stripes:label name="">Archief directory
                            <stripes:text name="brkProcessen[${i.index}].archiefDirectory" value="${brk.archiefDirectory}" class="dirPath"/>
                        </stripes:label>
                    </fieldset>
                    <stripes:submit name="save" value="Opslaan" />
                    <stripes:submit name="startProces" value="Start" />
                    <stripes:submit name="stopProces" value="Stop" disabled="true" />
                    <stripes:submit name="verwijderProces" value="Verwijderen" disabled="true" />
                </stripes:form>
            </c:forEach>
        </c:when>

        <h3>BAG scanners</h3>

        <stripes:form partial="true" action="">
            <p><stripes:button name="toevoegen" value="Toevoegen" disabled="true" onclick="alert('TODO formulier invoegen');"/></p>
        </stripes:form>
        <c:when test="${not empty actionBean.bagProcessen}">
            <c:forEach items="${actionBean.bagProcessen}" varStatus="j" var="bag" >
                <p>weer eentje ${j}</p>
            </c:forEach>
        </c:when>


        <h3>Notificaties</h3>

        <stripes:form partial="true" action="">
            <p><stripes:button name="toevoegen" value="Toevoegen" disabled="true" onclick="alert('TODO formulier invoegen');"/></p>
        </stripes:form>
        <c:when test="${not empty actionBean.mailProcessen}">
            content
        </c:when>



    </stripes:layout-component>
</stripes:layout-render>