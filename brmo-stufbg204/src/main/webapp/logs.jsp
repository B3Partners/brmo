<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <script type="text/javascript">
            var currentdate = new Date();
            var datetime =  currentdate.getDate() + "-"
                            + (currentdate.getMonth()+1)  + "-"
                            + currentdate.getFullYear() + " om "
                            + currentdate.getHours() + ":"
                            + currentdate.getMinutes() + ":"
                            + currentdate.getSeconds();
        </script>
        <meta http-equiv="refresh" content="30" />
    <head>
    <body>
        <h1>BRMO BRMO StUF BG 2.04 Service logfile</h1>

        <p>(automatisch) ververst op: <script>document.write(datetime);</script></p>

        <p>Download de <a href="downloadlog.jsp">hele logfile</a>.</p>
        <p>Terug naar de <a href="index.html">Startpagina</a>.</p>

        <h2>Laatste 200 logregels</h2>
        <p>
        <pre><brmo:logtail count="200"><br /><%=line%></brmo:logtail></pre>
    </p>
    </body>
</html>
