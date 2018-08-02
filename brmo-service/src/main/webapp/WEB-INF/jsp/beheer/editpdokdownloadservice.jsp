<%@include file="/WEB-INF/taglibs.jsp" %>
<script>
    function editpdok_actiechange(e) {
        var isDownload = Ext.get("selectmode").getValue() === "download";

        Ext.select("tr.check_download").setVisibility(!isDownload);
        Ext.select("tr.download").setVisibility(isDownload);
    }
    Ext.onReady(editpdok_actiechange);
</script>
<table>
    <tr><td valign="top">PDOK service URL:</td><td><input value="https://test.downloads.pdok.nl/" disabled size="30"><br></td></tr>
    <tr><td>Download directory:</td><td><stripes:text name="config['downloaddir']" size="80"/></td></tr>
    <tr><td>Dataset:</td><td><input value="bgtv3" disabled/></td></tr>
    <tr><td>Format:</td><td><input value="citygml" disabled></td></tr>

    <td>Actie:</td>
    <td><stripes:select id="selectmode" name="config['mode']" value="check_downloads" onchange="editpdok_actiechange(event);">
            <stripes:option value="check_downloads">Aanmaken laadprocessen voor downloads</stripes:option>
            <stripes:option value="download">Downloaden bestanden</stripes:option>
        </stripes:select>
    </td>

    <tr class="download" valign="top"><td>BGT excludedtypes:</td><td><stripes:text name="config['param_bgtExludedTypes']" size="80"/><br>Default: [plaatsbepalingspunt]</td></tr>
    <tr class="download" valign="top">
        <td>
            BGT geographischFilter:</td><td><stripes:text name="config['param_bgtGeographischFilter']" size="80"/><br>
            1. Selecteer gebied bij <a href="https://downloads.pdok.nl/embed.html" target="_blank">PDOK</a><br>
            2. Kopieer de link "Download kaartbladselectie" naar het klembord<br>
            3. Plak de waarde op <a href="https://www.urldecoder.org/" target="_blank">URL decoder</a><br>
            4. Vul de waarde achter <tt>"tiles="</tt> hier in, bijvoorbeeld: <tt>{"layers":[{"aggregateLevel":2,"codes":[2385]}]}</tt><br>
    </tr>

    <tr class="check_download">
        <td valign="top">Start deltaId:</td><td><stripes:text name="config['start_deltaId']" size="40"/><br>(laat leeg om te beginnen met laatste volledige stand)<br>
            <a href="https://test.downloads.pdok.nl/api/v2/deltas" target="_blank">Bekijk volledige lijst</a>
        </td>
    </tr>

    <tr><td>SSL verificatie:</td><td><label><stripes:checkbox name="config['ssl_validation']"/> Controleer geldigheid SSL certificaat</label> (niet aanvinken voor test)</td></tr>
</tr>
<tr>
    <td><stripes:label name="">Planning <a href="http://cronmaker.com" target="_blank">(cron expressie)</a></stripes:label></td>
    <td>
        <stripes:text name="proces.cronExpressie"/>
        <brmo:formatCron cronExpression="${actionBean.proces.cronExpressie}" />
    </td>
</tr>
</table>
