<%@include file="/WEB-INF/taglibs.jsp" %>

<c:set var="mviews" value="<%=nl.b3p.brmo.service.scanner.MaterializedViewRefreshUitvoeren.mviews()%>"/>

<table>
    <tr>
        <td><stripes:label name="">Label</stripes:label></td>
        <td><stripes:text name="config['label']"/></td>
    </tr>
    <tr>
        <c:choose>
            <c:when test="${empty mviews}">
                <td colspan="2">Geen materialized views gevonden</td>
            </c:when>
            <c:otherwise>
                <td><stripes:label name="">Te verversen materialized view</stripes:label></td>
                    <td>
                    <stripes:select name="config['mview']">
                        <stripes:options-collection collection="${mviews}"/>
                    </stripes:select>
                </td>
            </c:otherwise>
        </c:choose>
    </tr>
    <tr>
        <td><stripes:label name="">Planning <a href="http://cronmaker.com" target="_blank">(cron expressie)</a></stripes:label></td>
        <td><stripes:text name="proces.cronExpressie"/><brmo:formatCron cronExpression="${actionBean.proces.cronExpressie}" /></td>
    </tr>
</table>