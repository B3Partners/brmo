<%@include file="/WEB-INF/taglibs.jsp" %>
<table>
    <tr>
        <td><stripes:label name="">label</stripes:label></td>
        <td><stripes:text name="config['label']"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">scan directory URL</stripes:label></td>
        <td><stripes:text name="config['scandirectory']" value="http://mirror.openstreetmap.nl/bag/mutatie/?order=d" style="width: 30em;"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">archief directory (om opgehaalde .zip files te bewaren)</stripes:label></td>
    <td><stripes:text name="config['archiefdirectory']" /></td>
    </tr>
    <tr>
        <td><stripes:label name="">css path expressie</stripes:label></td>
        <td><stripes:text name="config['csspath']" value="td:nth-child(2) > a[href]" /></td>
    </tr>
    <tr>
        <td><stripes:label name="">planning <a href="http://cronmaker.com" target="_blank">(cron expressie)</a></stripes:label></td>
        <td><stripes:text name="proces.cronExpressie"/><brmo:formatCron cronExpression="${actionBean.proces.cronExpressie}"/></td>
    </tr>
</table>
