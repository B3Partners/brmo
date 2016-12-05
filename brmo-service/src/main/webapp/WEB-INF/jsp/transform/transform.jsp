<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">
        <c:choose>
            <c:when test="${actionBean.complete}">
                Klaar - Berichten transformeren
            </c:when>
            <c:when test="${actionBean.total le 0}">
                [Voortgang onbekend] Bezig met transformeren...
            </c:when>
            <c:otherwise>
                [<fmt:formatNumber maxFractionDigits="1" value="${actionBean.progress}"/>%] Bezig met transformeren...
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
        <h1>Berichten transformeren</h1>
        <p>
            Gestart op: <fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${actionBean.start}"/>,
                <script type="text/javascript">
                    document.write(moment("<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${actionBean.start}"/>","YYYY-MM-DD HH:mm:ss").fromNow() + ".");
                </script>            
            <br>
            <c:if test="${!empty actionBean.update}">
                Update: <fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${actionBean.update}"/><br>
            </c:if>
                Totaal: 
            <c:choose>
                    <c:when test="${actionBean.total le 0}">
                        Onbekend
                    </c:when>
                    <c:otherwise>
                        ${actionBean.total}
                    </c:otherwise>
            </c:choose>

            <c:if test="${actionBean.standBerichtenVerwerkingsLimiet > 0 }">
                (Het maximum aantal <b>stand</b> berichten dat in één run verwerkt kan worden is: ${actionBean.standBerichtenVerwerkingsLimiet}.
                <c:if test="${actionBean.total eq actionBean.standBerichtenVerwerkingsLimiet }">
                    <br/>De stand transformatie dient mogelijk nog eens gestart te worden voor een volgende batch.
                </c:if>)
            </c:if>
            <br>Verwerkt: ${actionBean.processed}
        </p>
        <p>
            <stripes:link href="/javasimon-console" target="_blank">Performance monitoring console</stripes:link>
        <p><b>
            <stripes:messages/>
            <stripes:errors/>
        </b></p>
        <p>
        <c:choose>
            <c:when test="${actionBean.complete}">
                Klaar - Berichten transformeren
            </c:when>
            <c:when test="${actionBean.total le 0}">
                [Voortgang onbekend] Bezig met transformeren...
            </c:when>
            <c:otherwise>
                [<fmt:formatNumber maxFractionDigits="1" value="${actionBean.progress}"/>%] Bezig met transformeren...
            </c:otherwise>
        </c:choose>
        </p>
        <c:if test="${!empty actionBean.exceptionStacktrace}">
            <b>Proces afgebroken vanwege een exception:</b>
            <pre><c:out value="${actionBean.exceptionStacktrace}"/></pre>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>