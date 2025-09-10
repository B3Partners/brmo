<%@include file="/WEB-INF/taglibs.jsp" %>
<table>
    <tr>
        <td><stripes:label name="">Label</stripes:label></td>
        <td><stripes:text name="config['label']" style="width: 22em"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">API URL</stripes:label></td>
        <%-- KVKMutatieserviceProces#APIURL --%>
        <td><stripes:select  name="config['apiurl']" value="https://api.kvk.nl/api/v1/abonnementen/" style="width: 22em">
            <stripes:option value="https://developers.kvk.nl/test/api/v1/abonnementen/">Test KVK Mutatieservice</stripes:option>
            <stripes:option value="https://api.kvk.nl/api/v1/abonnementen/">Productie KVK Mutatieservice</stripes:option>
        </stripes:select></td>
    </tr>
    <tr>
        <td><stripes:label name="">KVK API key</stripes:label></td>
        <%-- KVKMutatieserviceProces#APIKEY --%>
        <td><stripes:text name="config['apikey']" style="width: 22em"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">Abonnement ID</stripes:label></td>
        <%-- KVKMutatieserviceProces#ABONNEMENT_ID --%>
        <td><stripes:text name="config['abonnementId']" style="width: 22em"/></td>
    </tr>
    <tr>
        <td><stripes:label name="">Planning <a href="http://cronmaker.com" target="_blank">(cron
            expressie)</a></stripes:label></td>
        <td>
            <stripes:text name="proces.cronExpressie"/>
            <brmo:formatCron cronExpression="${actionBean.proces.cronExpressie}"/>
        </td>
    </tr>
    <tr>
        <td>datum vanaf (dd-MM-yyyy formaat of -1 / -2  / -3 mits "datum tot" gevuld)</td>
        <%-- KVKMutatieserviceProces#VANAF --%>
        <td><stripes:text name="config['vanaf']"/></td>
    </tr>
    <tr>
        <td>datum tot (dd-MM-yyyy formaat of "nu")</td>
        <%-- KVKMutatieserviceProces#TOT --%>
        <td><stripes:text name="config['tot']"/></td>
    </tr>
</table>
