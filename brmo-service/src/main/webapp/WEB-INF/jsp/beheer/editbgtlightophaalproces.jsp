<%@include file="/WEB-INF/taglibs.jsp" %>
<table>
    <tr>
        <td><stripes:label name="">Label</stripes:label></td>
        <td><stripes:text name="config['label']"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">Opslaan in directory</stripes:label></td>
        <td><stripes:text name="config['archiefdirectory']" /></td>
    </tr>
    <tr>
        <td colspan="2">Geef een gebied</td>
    </tr>
    <tr>
        <td><stripes:label name="">WKT van het op te halen gebied</stripes:label></td>
        <td><stripes:text name="config['ophaalgebied']" /></td>
    </tr>
    <tr>
        <td colspan="2">of</td>
    </tr>
    <tr>
        <td><stripes:label name="">Grid identifiers om op te halen</stripes:label></td>
        <td><stripes:text name="config['gridids']" /></td>
    </tr>
    <tr>
        <td><stripes:label name="">basis URL</stripes:label></td>
        <td><stripes:text name="config['ophaalurl']" value="https://www.pdok.nl/download/service/extract.zip?extractset=gmllight&tiles=%7B%22layers%22%3A%5B%7B%22aggregateLevel%22%3A0%2C%22codes%22%3A%5BGRID_ID%5D%7D%5D%7D&excludedtypes=plaatsbepalingspunt&history=true&enddate=ENDDATE" style="width: 40em;"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">tileinfo URL</stripes:label></td>
        <td><stripes:text name="config['geojsonurl']" value="https://www.pdok.nl/download/service/tileinfo.json?dataset=bgt&format=citygml" style="width: 40em;"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">Planning <a href="http://cronmaker.com" target="_blank">(cron expressie)</a></stripes:label></td>
        <td><stripes:text name="proces.cronExpressie"/><brmo:formatCron cronExpression="${actionBean.proces.cronExpressie}" /></td>
    </tr>
</table>
