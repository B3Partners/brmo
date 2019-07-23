<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">BRMO Controle</stripes:layout-component>
    <stripes:layout-component name="html_head">

    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <h2>Controle van berichten</h2>
        <stripes:messages />
        <stripes:errors />

        <stripes:form beanclass="nl.b3p.brmo.service.stripes.ControleActionBean" focus="">
            Afgiftelijst: <stripes:file name="file"/>
            <p><stripes:submit name="check" value="Controleer" /></p>
        </stripes:form>
     
    </stripes:layout-component>
</stripes:layout-render>
