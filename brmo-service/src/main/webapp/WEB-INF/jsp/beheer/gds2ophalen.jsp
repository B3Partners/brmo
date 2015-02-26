<%@include file="/WEB-INF/taglibs.jsp" %>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">Ophalen GDS2 leveringen</stripes:layout-component>
    <stripes:layout-component name="contents">

        <h2>Ophalen GDS2 leveringen</h2>

        <stripes:messages />
        <stripes:errors />

        <h3>Afgiftes ophalen bij Kadaster afgifteservice (GDS2)</h3>
        
        <p>Configureer een proces dat automatisch leveringen ophaalt bij het Kadaster.</p>
        
        <c:set var="l" value="${actionBean.processen}"/>
        <c:if test="${empty l}">
            <i>Nog geen processen gedefinieerd.</i>
        </c:if>
        <c:if test="${!empty l}">
            <table border="1" style="border-collapse: collapse" cellspacing="0">
                <tr><th>ID</th><th>Label</th><th>Laatste run</th><th>Status</th><th>Samenvatting</th></tr>
                <c:forEach var="p" items="${l}">
                    <stripes:url var="url" beanclass="nl.b3p.brmo.service.stripes.GDS2OphaalConfigActionBean" event="edit">
                        <stripes:param name="proces">${p.id}</stripes:param>
                    </stripes:url>                    
                    <c:set var="selected" value="${p.id == form.id}"/>
                    <c:set var="bgcol" value="${selected ? '#cccccc' : 'white'}"/>
                    <tr style="background-color: ${bgcol}; cursor: pointer;" onmouseover="this.style.background='#cccccc';" onmouseout="this.style.background='${bgcol}';"
                            onclick="javascript: window.location.href='${url}';">                        
                        <td>${p.id}</td>
                        <td><c:out value="${p.config['label']}"/></td>
                        <td style="white-space: nowrap"><fmt:formatDate pattern="dd-MM-yyyy HH:mm:ss" value="${p.lastrun}"/></td>
                        <td><c:out value="${p.status}"/></td>
                        <td><c:out value="${p.samenvatting}"/></td>
                    </tr>
                </c:forEach>
            </table>
        </c:if>
        <p/>
        <stripes:form beanclass="nl.b3p.brmo.service.stripes.GDS2OphaalConfigActionBean">
            <stripes:hidden name="proces"/>
            ${actionBean.context.eventName}<br/>
            <c:set var="_new" value="${actionBean.context.eventName == 'add'}"/>
            <c:set var="edit" value="${actionBean.context.eventName == 'view' && !empty actionBean.proces}"/>
            
<c:if test="${edit || _new}">
    <stripes:submit name="save">Opslaan</stripes:submit>
    <c:if test="${!_new}">
        <stripes:submit name="delete" onclick="return confirm('Weet u zeker dat u dit proces wilt verwijderen?')">Verwijderen</stripes:submit>
    </c:if>
    <stripes:submit name="cancel">Annuleren</stripes:submit>
</c:if>
<c:if test="${!edit && !_new}">
    <stripes:submit name="add">Toevoegen</stripes:submit>
</c:if>            
            <c:if test="${edit || _new}">
                <table>
                    <tr><td>Label:</td><td><stripes:text name="config['label']"/></td></tr>
                    <tr><td>Afleveringsendpoint:</td><td><stripes:text name="config['delivery_endpoint']" size="80"/></td></tr>
                    <%--tr><td>GDS2 endpoint (leeg is standaard):</td><td><stripes:text name="config['gds2_endpoint']" size="80"/></td></tr--%>
                    <%--tr><td>GDS2 HTTP BASIC username:</td><td><stripes:text name="config['gds2_username']"/> (optioneel)</td></tr--%>
                    <%--tr><td>GDS2 HTTP BASIC wachtwoord:</td><td><stripes:text name="config['gds2_password']"/> (optioneel)</td></tr--%>
                    <%--tr><td valign="top">GDS2 public key:</td><td><stripes:textarea rows="4" cols="80" name="config['gds2_pubkey']"/></td></tr--%>
                    <%--tr><td valign="top">GDS2 private key:</td><td><stripes:textarea rows="4" cols="80" name="config['gds2_pubkey']"/></td></tr--%>
                    <tr><td>Pad naar keystore:</td><td><stripes:text name="config['keystore_path']" size="80"/></td></tr>
                    <tr><td>Wachtwoord keystore:</td><td><stripes:password name="config['keystore_password']" size="20"/></td></tr>
                </table>
                
                <stripes:url var="url" beanclass="nl.b3p.brmo.service.stripes.GDS2OphalenUitvoerActionBean">
                    <stripes:param name="proces">${actionBean.proces.id}</stripes:param>
                </stripes:url>
                    <stripes:button name="execute" onclick="if(confirm('Let op! Proces moet eerst zijn opgeslagen. Verder gaan?')) window.open('${url}');">Uitvoeren</stripes:button>
            </c:if>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>