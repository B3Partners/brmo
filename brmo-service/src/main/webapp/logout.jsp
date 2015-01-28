<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="contents">
        <% request.getSession().invalidate();%>

        <h1>Uitgelogd</h1>

        <b>U bent uitgelogd. <stripes:link href="/index.jsp">Opnieuw inloggen</stripes:link></b>
    </stripes:layout-component>
</stripes:layout-render>