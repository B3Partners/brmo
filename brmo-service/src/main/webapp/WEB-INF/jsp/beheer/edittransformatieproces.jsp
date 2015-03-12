<%@include file="/WEB-INF/taglibs.jsp" %>
<table>
    <tr>
        <td><stripes:label name="">Label</stripes:label></td>
        <td><stripes:text name="config['label']"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">Planning (cron expressie)</stripes:label></td>
        <td><stripes:text name="proces.cronExpressie"/></td>
    </tr>
</table>
