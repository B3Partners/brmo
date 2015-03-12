<%@include file="/WEB-INF/taglibs.jsp" %>
<table>
    <tr><td>Label:</td><td><stripes:text name="config['label']"/></td></tr>
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
<%--tr><td>Pad naar keystore:</td><td><stripes:text name="config['keystore_path']" size="80"/></td></tr--%>
<%--tr><td>Wachtwoord keystore:</td><td><stripes:password name="config['keystore_password']" size="20"/></td></tr--%>
<tr>
    <td><stripes:label name="">Planning (cron expressie)</stripes:label></td>
    <td><stripes:text name="proces.cronExpressie"/></td>
</tr>
</table>