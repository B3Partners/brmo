<%@include file="/WEB-INF/taglibs.jsp"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <title>About BRMO</title>
        <link href="${contextPath}/styles/main.css" rel="stylesheet">
        <link href="${contextPath}/extjs/resources/css/crisp/ext-theme-crisp-all.css" rel="stylesheet">
    </head>
    <body class="x-body">
        <div class="header">
            <h1>B3Partners BRMO</h1>
            <jsp:include page="/WEB-INF/jsp/common/menu.jsp"/>
        </div>
        <div class="content">
            <h1>BRMO versie informatie</h1>
            <c:set var="version" value="${project.version}"/>
            <table>
                <tr>
                    <td><b>Versie:</b></td>
                    <td>
                        <c:choose>
                            <c:when test="${fn:contains(version,'SNAPSHOT')}">
                                ${project.version}-${builddetails.commit.id.abbrev}
                            </c:when>
                            <c:otherwise>
                                ${project.version}
                            </c:otherwise>
                        </c:choose>
                        <span id="actuele-versie"><!-- gevuld via jsonp naar GH api --></span>
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
                    <td colspan="2"><h3>Git details</h3></td>
                </tr>
                <tr>
                    <td><b>Git branch:</b></td>
                    <td>${builddetails.branch}</td>
                </tr>
                <tr>
                    <td><b>Git remote url:</b></td>
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
            <h2>Runtime informatie</h2>
            <table>
                <tr>
                    <td><b>OS info:</b></td>
                    <td>
                        <jsp:expression>System.getProperty("os.name")</jsp:expression>
                        <jsp:expression>System.getProperty("os.version")</jsp:expression>
                        <jsp:expression>System.getProperty("os.arch")</jsp:expression>
                    </td>
                </tr>
                <tr>
                    <td><b>Java versie:</b></td>
                    <td>
                        <jsp:expression>System.getProperty("java.vendor")</jsp:expression>
                        <jsp:expression>System.getProperty("java.version")</jsp:expression>
                    </td>
                </tr>
                <tr>
                    <td><b>Servlet container info:</b></td>
                    <td><jsp:expression>getServletContext().getServerInfo()</jsp:expression></td>
                </tr>
            </table>
        </div>
        <div class="footer"></div>
        <script>
            // gebruik jsonp om de laatste release op te halen en te tonen
            function v(json){
                var versie=json.data.name;
                var datum = new Date( json.data.published_at).toDateString();
                document.getElementById('actuele-versie').innerHTML = '(laatste release: '+versie+', dd. '+datum+')';
            }

            var scriptTag = document.createElement("script");
            scriptTag.src = "https://api.github.com/repos/b3partners/brmo/releases/latest?callback=v";
            document.getElementsByTagName('head')[0].appendChild(scriptTag);
        </script>
    </body>
</html>
