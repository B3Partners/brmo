<%@include file="/WEB-INF/taglibs.jsp" %>
<table>
    <tr>
        <td><stripes:label name="">Label</stripes:label></td>
        <td><stripes:text name="config['label']"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">Scan directory url</stripes:label></td>
        <td><stripes:text name="config['scandirectory']" /></td>
    </tr>
    <tr>
        <td><stripes:label name="">Archief directory</stripes:label></td>
        <td><stripes:text name="config['archiefdirectory']"  /></td>
    </tr>
    <tr>
        <td><stripes:label name="">css path expressie</stripes:label></td>
        <td><stripes:text name="config['csspath']"  /></td>
    </tr>
    <tr>
        <td><stripes:label name="">Planning (cron expressie)</stripes:label></td>
        <td><stripes:text name="proces.cronExpressie"/></td>
    </tr>
</table>