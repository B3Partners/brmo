<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">BRMO NHR laadproces</stripes:layout-component>
    <stripes:layout-component name="html_head">
        <script type="text/javascript" src="${contextPath}/scripts/plugins/PagingSelectionPersistence.js"></script>
        <script type="text/javascript" src="${contextPath}/scripts/common/GridSelection.js"></script>
        <script type="text/javascript" src="${contextPath}/scripts/nhr.js"></script> 
    </stripes:layout-component>
    <stripes:layout-component name="contents">
        <h1>Overzicht inschrijvingen</h1>
        <p><b>Shift-click:</b> Selecteer meerdere records,
        <b>Ctrl-click:</b> De-selecteer record.</p>
        <div id="comment-div"></div>
        <div id="nhr-grid" class="grid-container"></div>                
        <div id="button-retry"></div>
        <script type="text/javascript">
            var b3pberichten = Ext.create('B3P.brmo.NHRNummers', {
                gridurl: '<stripes:url beanclass="nl.b3p.brmo.service.stripes.NHRActionBean" event="getGridData"/>',
                runurl: '<stripes:url beanclass="nl.b3p.brmo.service.stripes.NHRActionBean" event="runNow"/>',
                logurl: '<stripes:url beanclass="nl.b3p.brmo.service.stripes.NHRActionBean" event="getLog"/>',
            });
        </script>

        <h1>Bestand uploaden via browser</h1>
        Maximale grootte 10 MB, als CSV (&eacute;&eacute;n KVK nummer per regel)
        <stripes:messages/>
        <stripes:errors/>
        <stripes:form beanclass="nl.b3p.brmo.service.stripes.NHRActionBean" focus="">
            <table>
                <tr>
                    <td>Bestand</td>
                    <td><stripes:file name="file"/></td>
                </tr>
            </table>
            <p><stripes:submit name="upload" value="Inladen" /></p>
        </stripes:form>

        <h1>Status</h1>
        <ul>
            <li>NHR ophalen actief: ${actionBean.statusActive}</li>
            <li>Certificaat verloopt: ${actionBean.statusCertificateExpiry} <c:choose>
                <c:when test="${actionBean.statusDaysUntilExpiry < 0}">(verlopen!)</c:when>
                <c:when test="${actionBean.statusDaysUntilExpiry < 30}">(${actionBean.statusDaysUntilExpiry} dagen)</c:when>
            </c:choose></li>
            <li>${actionBean.fetchCount} requests verstuurd (${actionBean.fetchErrorCount} mislukt), ${actionBean.secondsPerFetch} seconden per request</li>
            <c:if test="${actionBean.pendingCount > 0}"><li>${actionBean.pendingCount} requests te versturen, ${actionBean.estimatedTime}</li></c:if>
            <li>Endpoint: <code>${actionBean.statusEndpoint}</code> (<c:if test="${actionBean.statusEndpointPreprod}">pre</c:if>productie omgeving)</li>
            <li>Automatisch opnieuw ophalen: <c:choose><c:when test="${actionBean.statusRefetchDays == 0}">uit</c:when><c:otherwise>elke ${actionBean.statusRefetchDays} dag(en)</c:otherwise></c:choose></li>
            <c:if test="${actionBean.statusNotification != null && !actionBean.statusNotification.isEmpty()}"><li>Overige berichten: <pre>${actionBean.statusNotification}</pre></li></c:if>
        </ul>
    </stripes:layout-component>
</stripes:layout-render>
