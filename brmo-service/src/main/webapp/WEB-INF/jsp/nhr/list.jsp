<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">BRMO NHR laadproces</stripes:layout-component>
    <stripes:layout-component name="html_head">
        <script type="text/javascript" src="${contextPath}/scripts/plugins/PagingSelectionPersistence.js"></script>
        <script type="text/javascript" src="${contextPath}/scripts/common/GridSelection.js"></script>
        <script type="text/javascript" src="${contextPath}/scripts/nhr.js"></script> 
    </stripes:layout-component>
    <stripes:layout-component name="contents">
        <h1>Overzicht Berichten</h1>
        <p><b>Shift-click:</b> Selecteer meerdere records,
        <b>Ctrl-click:</b> De-selecteer record.</p>
        <div id="comment-div"></div>
        <div id="nhr-grid" class="grid-container"></div>                
        <div id="button-retry"></div>
        <script type="text/javascript">
            var b3pberichten = Ext.create('B3P.brmo.NHRNummers', {
                gridurl: '<stripes:url beanclass="nl.b3p.brmo.service.stripes.NHRActionBean" event="getGridData"/>',
                runurl: '<stripes:url beanclass="nl.b3p.brmo.service.stripes.NHRActionBean" event="runNow"/>',
                logurl: '<stripes:url beanclass="nl.b3p.brmo.service.stripes.NHRActionBean" event="getLog"/>',
            });
        </script>

        <h1>Bestand uploaden via browser</h1>
        Maximale grootte 10 MB, als CSV (&eacute;&eacute;n KVK nummer per regel)
        <stripes:messages/>
        <stripes:errors/>
        <stripes:form beanclass="nl.b3p.brmo.service.stripes.NHRActionBean" focus="">
            <table>
                <tr>
                    <td>Bestand</td>
                    <td><stripes:file name="file"/></td>
                </tr>
            </table>
            <p><stripes:submit name="upload" value="Inladen" /></p>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>
