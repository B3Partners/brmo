<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">Exporteren</stripes:layout-component>
    <stripes:layout-component name="contents">
        <h1>Exporteren</h1>
        
        Exporten van informatie uit de BRMO
        <p>
        Kies een exportproces:
        
        <stripes:form beanclass="nl.b3p.brmo.service.stripes.ExportActionBean">
            
            <stripes:select name="exportProcessName">
                <stripes:options-collection collection="${actionBean.exportProcesses}" label="name" value="name"/>
            </stripes:select>
        
        <p>
            <stripes:submit name="export">Exporteren</stripes:submit>
            
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>