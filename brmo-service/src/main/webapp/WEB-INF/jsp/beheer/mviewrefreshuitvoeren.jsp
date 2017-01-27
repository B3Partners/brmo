<%@include file="/WEB-INF/taglibs.jsp" %>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">Materialized view verversen</stripes:layout-component>
    <stripes:layout-component name="contents">

        <h2>Materialized view verversen</h2>

        <c:set  var="p" scope="page" value="${actionBean.proces.config}" />
        <p>Materialized view ververs proces: ${p['label']} (id: ${actionBean.proces.id}) view: ${p['mview']}<br/>
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

