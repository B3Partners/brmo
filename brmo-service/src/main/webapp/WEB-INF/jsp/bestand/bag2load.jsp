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

    <c:choose>
      <c:when test="${actionBean.loading}">
        <h1>BAG 2.0 inladen bezig...</h1>
      </c:when>
      <c:otherwise>
        <h1>BAG 2.0 ingeladen</h1>
        <c:choose>
          <c:when test="${actionBean.loadResult}">
            De BAG 2.0 bestanden zijn succesvol ingeladen.
          </c:when>
          <c:otherwise>
            Fout bij laden van de BAG 2.0 bestanden.
          </c:otherwise>
        </c:choose>
      </c:otherwise>
    </c:choose>
    <p>
      Controleer de <stripes:link href="/logs.jsp">logs</stripes:link> voor details.
    </p>
  </stripes:layout-component>
</stripes:layout-render>