<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">
        <c:choose>
            <c:when test="${actionBean.complete}">
                Klaar - BRMO Bestand inladen
            </c:when>
            <c:otherwise>
                [<fmt:formatNumber maxFractionDigits="1" value="${actionBean.progress}"/>%] Bezig met BRMO bestand inladen...
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
        <h1>Bestand inladen</h1>
        <p>
            Bestand: <c:out value="${actionBean.filename}"/><br>
            Gestart op: <fmt:formatDate pattern="HH:mm:ss" value="${actionBean.start}"/>,
                <script type="text/javascript">
                    document.write(moment("<fmt:formatDate pattern="HH:mm:ss" value="${actionBean.start}"/>","HH:mm:ss").fromNow() + ".");
                </script>            
            <br>
            <c:if test="${!empty actionBean.update}">
                Update: <fmt:formatDate pattern="HH:mm:ss" value="${actionBean.update}"/><br>
            </c:if>
            <c:if test="${!empty actionBean.progressDisplay}">
                Gelezen: ${actionBean.progressDisplay}
            </c:if>
        </p>
        <p style="font-weight: bold">
            <stripes:messages/>
            <stripes:errors/>
        </p>
        <p>
        <c:if test="${!actionBean.complete}">
            Voortgang: <fmt:formatNumber maxFractionDigits="1" value="${actionBean.progress}"/>%
        </c:if>
        </p>
    </stripes:layout-component>
</stripes:layout-render>