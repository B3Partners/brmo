<%@include file="/WEB-INF/taglibs.jsp" %>

<c:set var="statusValues" value="<%=nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.values()%>"/>

<table>
    <tr>
        <td><stripes:label name="">Label</stripes:label></td>
        <td><stripes:text name="config['label']"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">Geaddresseerde(n)</stripes:label></td>
        <td><stripes:text name="config['email']"  /></td>
    </tr>
    <tr>
        <td><stripes:label name="">Proces ID's voor rapportage</stripes:label></td>
        <td><stripes:text name="config['pIDS']"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">Proces status voor rapportage</stripes:label></td>
        <td><stripes:select name="config['forStatus']">
                <stripes:option value="">Alle</stripes:option>
                <stripes:options-collection collection="${statusValues}"/>
            </stripes:select></td>
    </tr>
    <tr>
        <td><stripes:label name="">Planning <a href="http://cronmaker.com" target="_blank">(cron expressie)</a></stripes:label></td>
        <td>
            <stripes:text name="proces.cronExpressie"/>
            <brmo:formatCron cronExpression="${actionBean.proces.cronExpressie}" />
        </td>
    </tr>
</table>
