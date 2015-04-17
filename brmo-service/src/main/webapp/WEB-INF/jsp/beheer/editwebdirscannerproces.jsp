<%@include file="/WEB-INF/taglibs.jsp" %>
<table>
    <tr>
        <td><stripes:label name="">Label</stripes:label></td>
        <td><stripes:text name="config['label']"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">Scan directory URL</stripes:label></td>
        <td><stripes:text name="config['scandirectory']" value="http://mirror.openstreetmap.nl/bag/mutatie/?order=d" style="width: 30em;"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">Archief directory (om opgehaalde .zip files te bewaren)</stripes:label></td>
        <td><stripes:text name="config['archiefdirectory']" /></td>
    </tr>
    <tr>
        <td><stripes:label name="">css path expressie</stripes:label></td>
        <td><stripes:text name="config['csspath']" value="td:nth-child(2) > a[href]" /></td>
    </tr>
    <tr>
        <td><stripes:label name="">Planning (cron expressie)</stripes:label></td>
        <td><stripes:text name="proces.cronExpressie"/></td>
    </tr>
</table>