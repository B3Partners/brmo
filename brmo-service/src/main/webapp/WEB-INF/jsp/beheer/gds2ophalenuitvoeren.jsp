<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">
        <c:choose>
            <c:when test="${actionBean.complete}">
                Klaar - GDS2 ophalen
            </c:when>
            <c:otherwise>
                [<fmt:formatNumber maxFractionDigits="1" value="${actionBean.progress}"/>%] Bezig met ophalen afgiftes...
            </c:otherwise>
        </c:choose>
    </stripes:layout-component>
    <stripes:layout-component name="html_head">
        <script type="text/javascript" src="${contextPath}/scripts/moment-with-locales.min.js"></script>
        <script type="text/javascript">
            moment.locale("nl");
        </script>
            
        <c:if test="${!actionBean.complete}">
              <meta http-equiv="refresh" content="0"/>
        </c:if>
    </stripes:layout-component>
    <stripes:layout-component name="contents">
        <h1>Afgiftes ophalen bij Kadaster afgifteservice (GDS2)</h1>
        <p>
            Gestart op: <fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${actionBean.start}"/>,
                <script type="text/javascript">
                    document.write(moment("<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${actionBean.start}"/>","YYYY-MM-DD HH:mm:ss").fromNow() + ".");
                </script>            
            <br>
            <c:if test="${!empty actionBean.update}">
                Update: <fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${actionBean.update}"/><br>
            </c:if>
            Totaal: ${actionBean.total}<br>
            Verwerkt: ${actionBean.processed}<br>
            Status: ${actionBean.status}
        </p>
        <p>
            <%--stripes:link href="/javasimon-console" target="_blank">Performance monitoring console</stripes:link--%>
        <p><b>
            <stripes:messages/>
            <stripes:errors/>
        </b></p>
        <p>
        <c:if test="${!actionBean.complete}">
            Voortgang: <fmt:formatNumber maxFractionDigits="1" value="${actionBean.progress}"/>%
        </c:if>
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