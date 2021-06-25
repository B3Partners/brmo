<%@page contentType="text/html" pageEncoding="UTF-8" isErrorPage="true" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>BRMO Service: Fout</title>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <link rel="stylesheet" type="text/css" href="/brmo-service/styles/main.css">
    <link rel="stylesheet" type="text/css" href="/brmo-service/extjs/resources/css/crisp/ext-theme-crisp-all.css">
</head>
<body class="x-body">

<div class="header">
    <h1>B3Partners BRMO</h1>
    <ul>
        <li><a href="${pageContext.request.contextPath}/index.jsp">&#155; Home</a></li>
    </ul>
</div>

<div class="content">
    <h1>Fout</h1>
    <p>Er is een fout opgetreden. Neem contact op met de beheerder als deze zich voor blijft doen.</p>
    <p>Fout code: <c:out value="${requestScope['javax.servlet.error.status_code']}"/></p>
    <p><i><c:out value="${requestScope['javax.servlet.error.message']}"/></i></p>
</div>

<div class="footer"></div>
</body>
</html>
