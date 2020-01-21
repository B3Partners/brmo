<%@include file="/WEB-INF/taglibs.jsp" %>
<table>
    <tr>
        <td><stripes:label name="">Label</stripes:label></td>
        <td><stripes:text name="config['label']"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">Planning <a href="http://cronmaker.com" target="_blank">(cron expressie)</a></stripes:label></td>
            <td>
            <stripes:text name="proces.cronExpressie"/>
            <brmo:formatCron cronExpression="${actionBean.proces.cronExpressie}" />
        </td>
    </tr>
</table>
