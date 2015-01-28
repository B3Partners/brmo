<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@include file="/WEB-INF/taglibs.jsp" %>
<stripes:layout-definition>
    <!DOCTYPE html>
    <html>
        <head>
            <title><stripes:layout-component name="title">BRMO Service</stripes:layout-component></title>
            <meta charset="utf-8">
            <meta http-equiv="X-UA-Compatible" content="IE=edge">
            <link rel="stylesheet" type="text/css" href="${contextPath}/styles/main.css" />
            <link rel="stylesheet" type="text/css" href="${contextPath}/extjs/resources/css/crisp/ext-theme-crisp-all.css">
            <script type="text/javascript" src="${contextPath}/extjs/ext-all.js"></script>
            <stripes:layout-component name="html_head"/>
        </head>
        <body class="x-body">
            <div class="header">
                <h1>B3Partners BRMO</h1>
                <jsp:include page="/WEB-INF/jsp/common/menu.jsp"/>
            </div>
            <div class="content">
                <stripes:layout-component name="contents"/>
            </div>
            <div class="footer"></div>
        </body>
    </html>
</stripes:layout-definition>