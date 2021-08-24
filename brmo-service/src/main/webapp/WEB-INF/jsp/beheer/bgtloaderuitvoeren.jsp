<%@include file="/WEB-INF/taglibs.jsp" %>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">
        <c:choose>
            <c:when test="${actionBean.complete}">
                Klaar - BGT laden
            </c:when>
            <c:otherwise>
                Bezig met BGT laden...
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

        <h2>BGT laden</h2>

        <p>
            Gestart op: <fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${actionBean.start}"/>,
            <script type="text/javascript">
                document.write(moment("<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${actionBean.start}"/>", "YYYY-MM-DD HH:mm:ss").fromNow() + ".");
            </script>
            <br>
            <c:if test="${!empty actionBean.update}">
                Update: <fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${actionBean.update}"/><br>
            </c:if>
            Status: ${actionBean.status}
        </p>

        <c:set var="p" scope="page" value="${actionBean.proces.config}"/>
        <p>BGT laden proces: ${p['label']} (id: ${actionBean.proces.id})<br/>
            <stripes:messages/>
            <stripes:errors/>
        </p>

        <c:if test="${!empty actionBean.exceptionStacktrace}">
            <b>Proces afgebroken vanwege een exception:</b>
            <pre><c:out value="${actionBean.exceptionStacktrace}"/></pre>
            <p>Controleer de <stripes:link href="logs.jsp">logfile</stripes:link></p>
        </c:if>

        <p/>
        Samenvatting:<br/>
        <pre><c:out value="${actionBean.log}"/></pre>
        <p>Details in de <stripes:link href="logs.jsp">logfile</stripes:link></p>
    </stripes:layout-component>
</stripes:layout-render>