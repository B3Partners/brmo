<%@include file="/WEB-INF/taglibs.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<stripes:layout-render name="/WEB-INF/jsp/layout/default.jsp">
    <stripes:layout-component name="contents">
        <h2>Inloggen</h2>

        <p style="color: red; font-weight: bold">Ongeldige logingegevens.</p>

        <form method="post" action="j_security_check">

            <table>
                <tr><td>Gebruikersnaam:</td><td><input type="text" name="j_username" /></td></tr>
                <tr><td>Wachtwoord:</td><td><input type="password" name="j_password"/></td></tr>
            </table>

            <p>
            <input type="submit" name="submit" value="Login"/>
        </form>
        <script type="text/javascript">
            window.onload = function() {
                document.forms[0].j_username.focus();
            };
        </script>
    </stripes:layout-component>
</stripes:layout-render>