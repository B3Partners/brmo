<%@include file="/WEB-INF/taglibs.jsp" %>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">Mail proces uitvoeren</stripes:layout-component>
    <stripes:layout-component name="contents">

        <h2>Mail proces uitvoeren</h2>

        <c:set  var="p" scope="page" value="${actionBean.proces.config}" />
        <p>Mail proces: ${p['label']} (id: ${actionBean.proces.id}) aan ${p['email']}:<br/>

            <stripes:messages />
            <stripes:errors />
        </p>
    </stripes:layout-component>
</stripes:layout-render>

