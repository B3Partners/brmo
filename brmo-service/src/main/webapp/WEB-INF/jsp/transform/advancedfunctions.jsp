<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">Geavanceerde functies</stripes:layout-component>
    <stripes:layout-component name="contents">
        <h1>Geavanceerde functies</h1>
        
        Geavanceerde functies ten bate van beheer van BRMO
        <p>
        Kies een functie:
        
        <stripes:form beanclass="nl.b3p.brmo.service.stripes.AdvancedFunctionsActionBean">
            
            <stripes:select name="advancedFunctionProcessName">
                <stripes:options-collection collection="${actionBean.advancedFunctionProcesses}" label="name" value="name"/>
            </stripes:select>
        
        <p>
            <stripes:submit name="perform">Uitvoeren</stripes:submit>
            
        </stripes:form>
        <p>
            Documentatie van de geavanceerde functies is beschikbaar op <a href="https://github.com/B3Partners/brmo/wiki/Geavanceerde-functies" target="_blank">de wiki</a>.
        </p>
    </stripes:layout-component>
</stripes:layout-render>