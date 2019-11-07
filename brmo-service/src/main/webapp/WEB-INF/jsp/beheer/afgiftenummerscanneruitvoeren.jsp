<%@include file="/WEB-INF/taglibs.jsp" %>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">Afgiftenummer scan uitvoeren</stripes:layout-component>
    <stripes:layout-component name="contents">

        <h2>Afgiftenummer scan uitvoeren</h2>

        <c:set  var="p" scope="page" value="${actionBean.proces.config}" />
        <p>
            Afgiftenummer scan uitvoeren proces: ${p['label']} (proces id: ${actionBean.proces.id}), 
            voor GDS2 contractnummer: ${p['contractnummer']} met scan voor ontbrekende ${p['afgiftenummertype']}s.<br/>
            <stripes:messages />
            <stripes:errors />
        </p>
        <c:if test="${!empty actionBean.exceptionStacktrace}">
            <b>Proces afgebroken vanwege een exception:</b>
            <pre><c:out value="${actionBean.exceptionStacktrace}"/></pre>
        </c:if>
        <p/>
        Log:<br/>
        <pre><c:out value="${actionBean.log}"/></pre>
    </stripes:layout-component>
</stripes:layout-render>
