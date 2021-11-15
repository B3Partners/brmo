<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%--
  ~ Copyright (C) 2021 B3Partners B.V.
  ~
  ~ SPDX-License-Identifier: MIT
  ~
  --%>

<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
  <stripes:layout-component name="title">BAG 2.0 inladen voortgang</stripes:layout-component>
  <stripes:layout-component name="html_head">
    <script type="text/javascript" src="${contextPath}/scripts/moment-with-locales.min.js"></script>
    <script type="text/javascript">
      moment.locale("nl");
    </script>

    <c:if test="${actionBean.loading}">
      <meta http-equiv="refresh" content="0"/>
    </c:if>
  </stripes:layout-component>
  <stripes:layout-component name="contents">

    <script>
        const start = moment("<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${actionBean.loadStart}"/>", "YYYY-MM-DD HH:mm:ss");
        const update = moment("<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${actionBean.updateTime}"/>", "YYYY-MM-DD HH:mm:ss");
    </script>
    <c:choose>
      <c:when test="${actionBean.loading}">
        <h1>BAG 2.0 inladen bezig...</h1>

        Gestart op: <fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${actionBean.loadStart}"/>,
        <script type="text/javascript">
          document.write(start.fromNow() + ".");
        </script>
        <br>
        <c:if test="${!empty actionBean.updateTime}">
          Update: <fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${actionBean.updateTime}"/><br>
        </c:if>

      </c:when>
      <c:otherwise>
        <h1>BAG 2.0 ingeladen</h1>
        <c:choose>
          <c:when test="${actionBean.loadResult}">
            De BAG 2.0 bestanden zijn succesvol ingeladen. Gestart op <fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${actionBean.loadStart}"/>
            en be&euml;indigd op <fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${actionBean.updateTime}"/>
            <script>
              document.write(" (" + moment.duration(update.diff(start)).humanize() + ").")
            </script>
          </c:when>
          <c:otherwise>
            Fout bij laden van de BAG 2.0 bestanden:
            <p>
              Foutmelding: <code>${actionBean.loadException}</code>
            </p>
          </c:otherwise>
        </c:choose>
      </c:otherwise>
    </c:choose>
    <p>
      Controleer de <stripes:link href="/logs.jsp" target="_blank">logs</stripes:link> voor details.
    </p>
  </stripes:layout-component>
</stripes:layout-render>