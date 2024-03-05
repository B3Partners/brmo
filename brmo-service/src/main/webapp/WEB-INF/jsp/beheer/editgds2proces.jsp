<%@include file="/WEB-INF/taglibs.jsp" %>
<table>
    <tr><td>Label:</td><td><stripes:text name="config['label']" size="80"/></td></tr>
    <tr><td>Afleveringsendpoint:</td><td><stripes:text name="config['delivery_endpoint']" size="80"/></td></tr>
    <%--tr><td>GDS2 endpoint (leeg is standaard):</td><td><stripes:text name="config['gds2_endpoint']" size="80"/></td></tr--%>
    <%--tr><td>GDS2 HTTP BASIC username:</td><td><stripes:text name="config['gds2_username']"/> (optioneel)</td></tr--%>
    <%--tr><td>GDS2 HTTP BASIC wachtwoord:</td><td><stripes:text name="config['gds2_password']"/> (optioneel)</td></tr--%>
    <tr>
        <td valign="top">GDS2 public key:</td>
        <td>
            Certificaat in PEM formaat, dient te beginnen met <tt>-----BEGIN CERTIFICATE-----</tt>
            <stripes:textarea rows="4" cols="80" name="config['gds2_pubkey']"/>
</td>
</tr>
<tr>
    <td valign="top">GDS2 private key:</td>
    <td>
        Niet-versleutelde private key in PEM formaat, dient te beginnen met <tt>-----BEGIN PRIVATE KEY-----</tt>
        <stripes:textarea rows="4" cols="80" name="config['gds2_privkey']"/>
</td>
</tr>
<tr><td>Contractnummer:</td><td><stripes:text name="config['gds2_contractnummer']" size="10"/></td></tr>
<tr><td>Artikelnummer:</td><td><stripes:text name="config['gds2_artikelnummer']" size="10"/></td></tr>
<%-- hidden field onderaan de pagina zodat deze niet wordt weggegooid bij opslaan --%>
<tr><td>Hoogste klantafgiftenummer:</td><td><c:out value="${actionBean.config['hoogste_afgiftenummer']}"/></td></tr>
<tr>
    <td>Bericht soort:</td>
    <td><stripes:select name="config['gds2_br_soort']" value="brk2">
            <stripes:option value="brk2">BRK 2</stripes:option>
            <stripes:option value="brk">BRK</stripes:option>
        </stripes:select>
    </td>
</tr>
<tr><td>Al gerapporteerde afgiftes:</td><td><label><stripes:checkbox name="config['gds2_al_gerapporteerde_afgiftes']"/>Ook ophalen</label></td></tr>
<tr><td>Niet gerapporteerde afgiftes:</td><td><label><stripes:checkbox name="config['gds2_niet_gerapporteerde_afgiftes_niet_ophalen']"/>Nog niet ophalen (tbv test, alleen eerste 2000 afgiftes)</label></td></tr>
<%--tr><td>Pad naar keystore:</td><td><stripes:text name="config['keystore_path']" size="80"/></td></tr--%>
<%--tr><td>Wachtwoord keystore:</td><td><stripes:password name="config['keystore_password']" size="20"/></td></tr--%>
<tr>
    <td><stripes:label name="">Planning <a href="http://cronmaker.com" target="_blank">(cron expressie)</a></stripes:label></td>
    <td>
        <stripes:text name="proces.cronExpressie"/>
        <brmo:formatCron cronExpression="${actionBean.proces.cronExpressie}" />
    </td>
</tr>
<tr>
    <td>datum vanaf (dd-MM-yyyy formaat of -1 / -2  / -3 mits "datum tot" gevuld)</td>
    <td><stripes:text name="config['vanafdatum']"/></td>
</tr>
<tr>
    <td>datum tot-en-met (dd-MM-yyyy formaat of "nu")</td><td><stripes:text name="config['totdatum']"/></td>
</tr>
<tr>
    <td>klantafgiftenummer vanaf</td><td><stripes:text name="config['klantafgiftenummer_vanaf']"/></td>
</tr>
<tr>
    <td>klantafgiftenummer tot-en-met</td><td><stripes:text name="config['klantafgiftenummer_totenmet']"/></td>
</tr>
</table>
<stripes:hidden name="config['hoogste_afgiftenummer']" />
