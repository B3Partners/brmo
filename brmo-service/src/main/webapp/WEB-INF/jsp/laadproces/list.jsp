<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">BRMO Laadprocessen</stripes:layout-component>
    <stripes:layout-component name="html_head">
        <script type="text/javascript" src="${contextPath}/scripts/plugins/PagingSelectionPersistence.js"></script>
        <script type="text/javascript" src="${contextPath}/scripts/common/GridSelection.js"></script>
        <script type="text/javascript" src="${contextPath}/scripts/laadprocessen.js"></script>
    </stripes:layout-component>
    <stripes:layout-component name="contents">
        <h1>Overzicht Laadprocessen</h1>
        <p><b>Shift-click:</b> Selecteer meerdere records,
        <b>Ctrl-click:</b> De-selecteer record.</p>
        <div id="comment-div"></div>
        <div id="laadproces-grid" class="grid-container"></div>
        <div id="button-transform"></div>
        <div id="button-delete"></div>
        <script type="text/javascript">
            Ext.create('B3P.brmo.LaadProces', {
                gridurl: '<stripes:url beanclass="nl.b3p.brmo.service.stripes.LaadProcesActionBean" event="getGridData"/>',
                deleteurl: '<stripes:url beanclass="nl.b3p.brmo.service.stripes.LaadProcesActionBean" event="delete"/>',
                transformurl: '<stripes:url beanclass="nl.b3p.brmo.service.stripes.TransformActionBean" event="transformSelectedLaadprocessen"/>'
            });
        </script>
    </stripes:layout-component>
</stripes:layout-render>