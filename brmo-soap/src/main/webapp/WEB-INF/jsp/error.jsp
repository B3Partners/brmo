<%@page contentType="text/html" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html>
<head>
    <title>BRMO SOAP Services: Fout</title>
    <meta charset="utf-8">
</head>
<body class="x-body">

<div class="header">
    <h1>B3Partners BRMO</h1>
    <ul>
        <li><a href="${pageContext.request.contextPath}/index.html">Home</a></li>
    </ul>
</div>

<div class="content">
    <h1>Fout</h1>
    <p>Er is een fout opgetreden. Neem contact op met de beheerder als deze zich voor blijft doen.</p>
    <p>Fout code: ${requestScope['javax.servlet.error.status_code']}</p>
    <p><i>${requestScope['javax.servlet.error.message']}</i></p>
</div>

<div class="footer"></div>
</body>
</html>
