<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">Updates</stripes:layout-component>
    <stripes:layout-component name="contents">
        <h1>Snelle updates</h1>
        
        Met snelle updates kunnen op basis van al verwerkte berichten met status
        RSGB_OK de niet-historische tabellen worden geupdate.
        <p>
        Kies een updateproces:
        
        <stripes:form beanclass="nl.b3p.brmo.service.stripes.UpdatesActionBean">
            
            <stripes:select name="updateProcessName">
                <stripes:options-collection collection="${actionBean.updateProcesses}" label="name" value="name"/>
            </stripes:select>
        
        <p>
            <stripes:submit name="update">Updaten</stripes:submit>
            
        </stripes:form>
        <p>
            Documentatie van de snelle updates is beschikbaar op <a href="https://github.com/B3Partners/brmo/wiki/Snelle-updates" target="_blank">de wiki</a>.
        </p>
    </stripes:layout-component>
</stripes:layout-render>