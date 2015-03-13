<%@include file="/WEB-INF/taglibs.jsp"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <title>About</title>
        <link href="${contextPath}/styles/main.css" rel="stylesheet">
        <link href="${contextPath}/extjs/resources/css/crisp/ext-theme-crisp-all.css" rel="stylesheet">
    </head>
    <body class="x-body">
        <div class="header">
            <h1>B3Partners BRMO</h1>
            <ul>
                <li><a href="/brmo-service/index.jsp">&#155; Home</a></li>
                <li><a href="/brmo-service/BasisregistratieFileUpload.action">&#155; Bestand inladen</a></li>
                <li><a href="/brmo-service/LaadProces.action">&#155; Laadprocessen</a></li>
                <li><a href="/brmo-service/Berichten.action">&#155; Berichten</a></li>
                <li><a href="/brmo-service/OphaalConfig.action">&#155; Automatische processen</a></li>
                <li><a href="/brmo-service/about.jsp">&#155; Versie Informatie</a></li>
                <li><a href="/brmo-service/logout.jsp">&#155; Uitloggen</a></li>
            </ul>
        </div>
        <div class="content">
            <h1>BRMO versie informatie</h1>
            <c:set var="version" value="${project.version}"/>
            <table>

                <tr>
                    <td><b>Version:</b></td>
                    <td>
                        <c:choose>
                            <c:when test="${fn:contains(version,'SNAPSHOT')}">
                                ${project.version}-${builddetails.commit.id.abbrev}
                            </c:when>
                            <c:otherwise>
                                ${project.version}
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
                <tr>
                    <td><b>Build time:</b></td>
                    <td>${builddetails.build.time}</td>
                </tr>
                <tr>
                    <td><b>Build by:</b></td>
                    <td>${builddetails.build.user.name}</td>
                </tr>
                <tr>
                    <td colspan="2">
                <center><b>Git details</b></center>
                </td>
                </tr>
                <tr>
                    <td><b>Git branch:</b></td>
                    <td>${builddetails.branch}</td>
                </tr>
                <tr>
                    <td><b>Git remote url</b></td>
                    <td>${builddetails.remote.origin.url}</td>
                </tr>
                <tr>
                    <td><b>Git commit abbrev id:</b></td>
                    <td>${builddetails.commit.id.abbrev}</td>
                </tr>
                <tr>
                    <td><b>Git commit full id:</b></td>
                    <td>${builddetails.commit.id}</td>
                </tr>
                <tr>
                    <td><b>Git commit time:</b></td>
                    <td>${builddetails.commit.time}</td>
                </tr>
            </table>
        </div>
        <div class="footer"></div>
    </body>
</html>
