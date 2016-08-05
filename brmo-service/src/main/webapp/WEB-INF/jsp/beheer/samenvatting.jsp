<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">BRMO Proces logfile</stripes:layout-component>
    <stripes:layout-component name="html_head">

    </stripes:layout-component>

    <c:set value="${actionBean.proces}" var="p" />

    <stripes:layout-component name="contents">

        <h2>Samenvatting van taak ${p.id}</h2>
        <stripes:messages />
        <stripes:errors />

        <p>
            Laatste run op: <fmt:formatDate  pattern="${timeFormat}" value="${p.lastrun}"/>, met status: ${p.status}.
        </p>
        <h3>Samenvatting</h3>
        <p>
            ${fn:replace(p.samenvatting, actionBean.newLine, "<br />")}
        </p>

        <stripes:form partial="true" action="">
            <stripes:url beanclass="nl.b3p.brmo.service.stripes.SamenvattingActionBean" var="_url" event="eraseLog">
                <stripes:param name="proces" value="${p}" />
            </stripes:url>
            <stripes:button name="eraseLog" value="Log wissen" onclick="if(confirm('Let op! Dit is onherroepelijk. Verder gaan?')) window.location='${_url}';"/>
        </stripes:form>

        <h3>Logfile</h3>
        <p>
            ${fn:replace(p.logfile, actionBean.newLine, "<br />")}
        </p>
    </stripes:layout-component>
</stripes:layout-render>
