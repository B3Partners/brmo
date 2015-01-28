<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">BRMO Berichten</stripes:layout-component>
    <stripes:layout-component name="html_head">
        <script type="text/javascript" src="${contextPath}/scripts/plugins/PagingSelectionPersistence.js"></script>
        <script type="text/javascript" src="${contextPath}/scripts/common/GridSelection.js"></script>
        <script type="text/javascript" src="${contextPath}/scripts/berichten.js"></script> 
    </stripes:layout-component>
    <stripes:layout-component name="contents">
        <h1>Overzicht Berichten</h1>
        <p><b>Shift-click:</b> Selecteer meerdere records,
        <b>Ctrl-click:</b> De-selecteer record.</p>
        <div id="comment-div"></div>
        <div id="berichten-grid" class="grid-container"></div>                
        <div id="button-run"></div>
        <div id="button-run-all"></div>
        <script type="text/javascript">
            var b3pberichten = Ext.create('B3P.brmo.Berichten', {
                gridurl: '<stripes:url beanclass="nl.b3p.brmo.service.stripes.BerichtenActionBean" event="getGridData"/>',
                runurl: '<stripes:url beanclass="nl.b3p.brmo.service.stripes.TransformActionBean" event="transformSelected"/>',
                runallurl: '<stripes:url beanclass="nl.b3p.brmo.service.stripes.TransformActionBean" event="transformAll"/>',
                gridsaveurl: '<stripes:url beanclass="nl.b3p.brmo.service.stripes.BerichtenActionBean" event="saveRecord"/>',
                logurl : '<stripes:url beanclass="nl.b3p.brmo.service.stripes.BerichtenActionBean" event="log"/>'
            });
        </script>
    </stripes:layout-component>
</stripes:layout-render>