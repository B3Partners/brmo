<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="html_head">
        <script type="text/javascript" src="${contextPath}/scripts/moment-with-locales.min.js"></script>
        <script type="text/javascript">
            moment.locale("nl");
        </script>
        <meta http-equiv="refresh" content="30" />
    </stripes:layout-component>
    <stripes:layout-component name="contents">
        <h1>BRMO Service logfile</h1>

        <p>ververst op: <script>document.write(moment(Date.now()).format('YYYY-MM-DD hh:mm:ss'));</script></p>

        <p>Download <stripes:link href="/downloadlog.jsp">hele logfile</stripes:link>.</p>

        <h3>Laatste 200 logregels</h3>
        <p>
            <pre><brmo:logtail count="200"><br /><%=line%></brmo:logtail></pre>
        </p>

    </stripes:layout-component>
</stripes:layout-render>