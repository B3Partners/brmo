<%@include file="/WEB-INF/taglibs.jsp" %>

<c:set var="contractnummers" value="<%=nl.b3p.brmo.service.scanner.AfgifteNummerScanner.contractnummers()%>"/>

<table>
    <tr>
        <td><stripes:label name="">Label</stripes:label></td>
        <td><stripes:text name="config['label']"/></td>
    </tr>
    <tr>
        <c:choose>
            <c:when test="${empty contractnummers}">
                <td colspan="2">Geen GDS2 contractnummers gevonden</td>
            </c:when>
            <c:otherwise>
                <td><stripes:label name="">Te onderzoeken GDS2 contractnummer</stripes:label></td>
                <td>
                    <stripes:select name="config['contractnummer']">
                    <stripes:options-collection collection="${contractnummers}"/>
                    </stripes:select>
                </td>
            </c:otherwise>
        </c:choose>
    </tr>
    <tr>
        <td><stripes:label name="">Scan type</stripes:label></td>
        <td>
            <stripes:select name="config['afgiftenummertype']">
                <stripes:option value="klantafgiftenummer">Klantafgiftenummer</stripes:option>
                <stripes:option value="contractafgiftenummer">Contractafgiftenummer</stripes:option>
            </stripes:select>
        </td>
    </tr>
    <tr>
        <td><stripes:label name="">Planning <a href="http://cronmaker.com" target="_blank">(cron expressie)</a></stripes:label></td>
        <td><stripes:text name="proces.cronExpressie"/><brmo:formatCron cronExpression="${actionBean.proces.cronExpressie}" /></td>
    </tr>
</table>
