<%@include file="/WEB-INF/taglibs.jsp" %>

<c:set var="laadprocesSoorten" value="<%=nl.b3p.brmo.persistence.staging.LaadprocesTransformatieProces.LaadprocesSoorten.soorten()%>"/>

<table>
    <tr>
        <td><stripes:label name="">Label</stripes:label></td>
        <td><stripes:text name="config['label']"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">Laadproces soort</stripes:label></td>
        <td><stripes:select name="config['laadprocessoort']">
                <stripes:options-collection collection="${laadprocesSoorten}"/>
            </stripes:select>
        </td>
        </tr>
        <tr>
            <td>Als stand (versneld) transformeren</td>
            <td><label><stripes:checkbox name="config['versneldtransformeren']"/></label></td>
    </tr>
    <tr>
        <td><stripes:label name="">Planning <a href="http://cronmaker.com" target="_blank">(cron expressie)</a></stripes:label></td>
            <td>
            <stripes:text name="proces.cronExpressie"/>
            <brmo:formatCron cronExpression="${actionBean.proces.cronExpressie}" />
        </td>
    </tr>
</table>
