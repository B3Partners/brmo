<%@include file="/WEB-INF/taglibs.jsp" %>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">

    <stripes:layout-component name="html_head">
        <script type="text/javascript" src="${contextPath}/scripts/moment-with-locales.min.js"></script>
        <script type="text/javascript">
            moment.locale("nl");
        </script>
        <c:if test="${!actionBean.complete}">
            <meta http-equiv="refresh" content="0"/>
        </c:if>
    </stripes:layout-component>

    <stripes:layout-component name="title">
        <c:choose>
            <c:when test="${actionBean.complete}">
                Klaar - Afgiftenummer scan (${actionBean.proces.id}) uitvoeren
            </c:when>
            <c:otherwise>
                Afgiftenummer scan (${actionBean.proces.id}) uitvoeren...
            </c:otherwise>
        </c:choose>
    </stripes:layout-component>


    <stripes:layout-component name="contents">
        <h2>Afgiftenummer scan (${actionBean.proces.id}) uitvoeren</h2>

        <c:set  var="p" scope="page" value="${ actionBean.proces.config}" />
        <p>
            Afgiftenummer scan uitvoeren proces: ${p['label']} (proces id: ${actionBean.proces.id})<c:if test="${not empty p['contractnummer']}">
                voor GDS2 contractnummer: ${p['contractnummer']}</c:if>. Scan voor ontbrekende ${p['afgiftenummertype']}s.
        </p>

        <p>
            Gestart op: <fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${actionBean.start}"/>,
            <script type="text/javascript">
                document.write(moment("<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${actionBean.start}"/>", "YYYY-MM-DD HH:mm:ss").fromNow() + ".");
            </script>

            <c:if test="${!empty actionBean.update}">
                Update: <fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${actionBean.update}"/>
            </c:if>
                <br/>Status: ${actionBean.status}
                <br/>Toegevoegd: ${actionBean.total}
        </p>

        <p>
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
