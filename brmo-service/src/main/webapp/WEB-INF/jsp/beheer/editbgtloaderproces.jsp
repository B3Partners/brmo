<%@include file="/WEB-INF/taglibs.jsp" %>
<table>
    <tr>
        <td><stripes:label name="">Label</stripes:label></td>
        <td><stripes:text name="config['label']"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">WKT van het op te halen gebied</stripes:label></td>
        <td><stripes:text name="config['geo-filter']" style="width: 63em;"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">Objecttypes</stripes:label></td>
        <td><stripes:text name="config['feature-types']" style="width: 63em;"/></td>
    </tr>

    <tr>
        <td>Schema aanmaken</td>
        <td><label><stripes:checkbox name="config['create-schema']"/>Schema aanmaken</label>
        </td>
    </tr>
    <tr>
        <td>Historische Objecten</td>
        <td><label><stripes:checkbox name="config['include-history']"/>Historische Objecten
            opnemen</label>
        </td>
    </tr>
    <tr>
        <td>Bogen</td>
        <td><label><stripes:checkbox name="config['linearize-curves']"/>Bogen omzetten naar lijnsegmenten</label></td>
    </tr>
    <tr>
        <td><stripes:label name="">Planning <a href="http://cronmaker.com" target="_blank">(cron
            expressie)</a></stripes:label>
        </td>
        <td>
            <stripes:text name="proces.cronExpressie"/>
            <brmo:formatCron cronExpression="${actionBean.proces.cronExpressie}"/>
        </td>
    </tr>

</table>
<script>
    if (!${empty actionBean.proces.lastrun}) {
        // als proces 1 maal heeft gedraaid kan de configuratie niet meer aangepast worden (omdat de bgt loader zelf config bijhoudt in BGT schema)
        console("instellen readonly attribuut");
        document.getElementsByName("config['geo-filter']")[0].setAttribute("readonly", "readonly");
        document.getElementsByName("config['create-schema']")[0].setAttribute("readonly", "readonly");
        document.getElementsByName("config['feature-types']")[0].setAttribute("readonly", "readonly");
        document.getElementsByName("config['include-history']")[0].setAttribute("readonly", "readonly");
        document.getElementsByName("config['linearize-curves']")[0].setAttribute("readonly", "readonly");

    }
</script>


