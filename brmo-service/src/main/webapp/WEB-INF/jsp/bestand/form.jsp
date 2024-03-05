<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="title">BRMO Bestand</stripes:layout-component>
    <stripes:layout-component name="contents">
        <h1>Bestand uploaden via browser</h1>
        Maximale grootte 10 MB, in XML formaat (of meerdere XML bestanden in ZIP bestand).
        <stripes:messages/>
        <stripes:errors/>
        <stripes:form beanclass="nl.b3p.brmo.service.stripes.BasisregistratieFileUploadActionBean" focus="">
            <table>
                <tr>
                    <td>Basisregistratie</td>
                    <td>
                        <stripes:select name="basisregistratie">
                            <stripes:option/>
                            <stripes:option value="brk2">BRK 2</stripes:option>
                            <stripes:option value="brk">BRK</stripes:option>
                            <stripes:option value="nhr">nHR</stripes:option>
                            <stripes:option value="gbav">GBAV</stripes:option>
                            <stripes:option value="woz">WOZ</stripes:option>
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <td>Bestand</td>
                    <td><stripes:file name="bestand"/></td>
                </tr>
            </table>
            <p><stripes:submit name="upload" value="Inladen" /></p>
        </stripes:form>
        <h1>Bestand inladen van bestandssysteem server</h1>
        Gebruik voor grote bestanden (zoals grote standen). ZIP-bestanden met meerdere XML bestanden erin worden ondersteund.
        <stripes:form beanclass="nl.b3p.brmo.service.stripes.BasisregistratieBigFileLoadActionBean" focus="">
            <table>
                <tr>
                    <td>Basisregistratie</td>
                    <td>
                        <stripes:select name="basisregistratie">
                            <stripes:option/>
                            <stripes:option value="brk2">BRK 2</stripes:option>
                            <stripes:option value="brk">BRK</stripes:option>
                            <stripes:option value="nhr">nHR</stripes:option>
                            <stripes:option value="gbav">GBAV</stripes:option>
                            <stripes:option value="woz">WOZ</stripes:option>
                        </stripes:select>
                    </td>
                </tr>
                <tr>
                    <td>Pad naar bestand (vanaf server)</td>
                    <td><stripes:text name="filename" size="40"/></td>
                </tr>
            </table>
            <p><stripes:submit name="load" value="Inladen" /></p>
        <h1>BAG 2.0 extract of mutaties inladen</h1>
            Om de BAG 2.0 stand of mutaties in te laden klik <stripes:link beanclass="nl.b3p.brmo.service.stripes.BAG2LoadActionBean">hier</stripes:link>.
        <h1>KVK Handelsregisterinschijvingen inladen</h1>
            Om KVK inschijvingen in te laden, klik <stripes:link beanclass="nl.b3p.brmo.service.stripes.NHRActionBean">hier</stripes:link>.
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>
