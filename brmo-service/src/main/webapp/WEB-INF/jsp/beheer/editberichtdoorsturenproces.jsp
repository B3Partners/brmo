<%@include file="/WEB-INF/taglibs.jsp" %>
<table>
    <tr>
        <td>GDS2 ophaalproces:</td>
        <td>
            <c:set var="selectedProces" value="${actionBean.config['gds2_ophaalproces_id'].value}"/>
            <stripes:select name="config['gds2_ophaalproces_id']">
                <stripes:option value=""></stripes:option>
                <c:forEach var="proces" items="${actionBean.processen}">
                    <!-- proces: ${proces}, id: ${proces.id} -->
                    <c:set var="className"><%= pageContext.getAttribute("proces").getClass().getSimpleName() %></c:set>
                    <c:if test="${className == 'GDS2OphaalProces'}">
                        <stripes:option value="${proces.id}">
                            <c:out value="${proces.config.label}"/> (${proces.id})
                        </stripes:option>
                    </c:if>
                </c:forEach>
            </stripes:select>
        </td>
    </tr>
    <tr>
        <td><stripes:label name="">Planning <a href="http://cronmaker.com" target="_blank">(cron expressie)</a></stripes:label></td>
        <td>
            <stripes:text name="proces.cronExpressie"/>
            <brmo:formatCron cronExpression="${actionBean.proces.cronExpressie}" />
        </td>
    </tr>
    <tr>
        <td><stripes:label name="">Commit page size (leeg voor default)</stripes:label></td>
        <td><stripes:text name="config['commitPageSize']"/></td>
    </tr>
</table>
