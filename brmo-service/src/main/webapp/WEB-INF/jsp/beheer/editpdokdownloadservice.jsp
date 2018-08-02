<%@include file="/WEB-INF/taglibs.jsp" %>
<table>
    <tr><td>PDOK service URL:</td><td><stripes:text name="config['pdok_service_url']" size="80"/><br>(laat leeg voor default)</td></tr>
    <tr><td>Download directory:</td><td><stripes:text name="config['downloaddir']" size="80"/></td></tr>
    <tr><td>Dataset:</td><td><input value="bgtv3" disabled/></td></tr>
    <tr><td>Format:</td><td><input value="citygml" disabled></td></tr>

    <tr><td>BGT excludedtypes:</td><td><stripes:text name="config['param_bgtExludedTypes']" size="80"/><br>Default: [plaatsbepalingspunt]</td></tr>
    <tr><td>BGT geographischFilter:</td><td><stripes:text name="config['param_bgtGeographischFilter']" size="80"/></td></tr>

    <tr><td>Start deltaId:</td><td><stripes:text name="config['start_deltaId']" size="40"/><br>(laat leeg om te beginnen met laatste volledige stand)<br>
            <a href="https://test.downloads.pdok.nl/api/v2/deltas" target="_blank">Bekijk volledige lijst</a>
        </td></tr>

    <tr><td>SSL verificatie:</td><td><label><stripes:checkbox name="config['ssl_validation']"/> Controleer geldigheid SSL certificaat</label> (niet aanvinken voor test)</td></tr>

    <td>Actie:</td>
    <td><stripes:select name="config['mode']" value="check_downloads">
            <stripes:option value="check_downloads">Aanmaken laadprocessen</stripes:option>
            <stripes:option value="download">Downloaden bestanden</stripes:option>
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
</table>
