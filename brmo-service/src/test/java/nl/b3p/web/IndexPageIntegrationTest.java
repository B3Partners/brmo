/*
 * Copyright (C) 2016 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.web;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import static org.hamcrest.CoreMatchers.equalTo;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 * Integration test for the index page.
 *
 * @author mprins
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IndexPageIntegrationTest extends WebTestUtil {

    /**
     * onze test response.
     */
    private HttpResponse response;

    /**
     * Test of de database bestaat en de default admin gebruiker is aangemaakt.
     * Test of de .travis.yml correct is uitgevoerd.
     *
     * @throws SQLException als er iets misgaat met SQL parsen
     */
    @Test
    public void testDefaultUserPresent() throws SQLException {
        // staat in /brmo-persistence/db/02_insert_default_user.sql
        String default_gebruikersnaam = "brmo";
        String default_hash = "6310227872580fec7d1262ab7ab3b4b3902a9f61";

        try {
            Class.forName(DBPROPS.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException ex) {
            fail("Laden van database driver (" + DBPROPS.getProperty("jdbc.driverClassName") + ") is mislukt.");
        }
        Connection connection = DriverManager.getConnection(DBPROPS.getProperty("staging.url"),
                DBPROPS.getProperty("staging.username"),
                DBPROPS.getProperty("staging.password")
        );
        ResultSet rs = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
        ).executeQuery("SELECT gebruikersnaam, wachtwoord FROM gebruiker_");

        String actual_gebruikersnaam = "";
        String actual_hash = "";
        while (rs.next()) {
            // gebruik getBytes ipv. getString vanwege optredende fout
            // "java.lang.NoSuchMethodError: org.postgresql.core.BaseConnection.getEncoding()Lorg/postgresql/core/Encoding;"
            // waarschijnlijk veroorzaakt door de postgis dependency...
            //            message="org.postgresql.core.BaseConnection.getEncoding()Lorg/postgresql/core/Encoding;" type="java.lang.NoSuchMethodError">java.lang.NoSuchMethodError: org.postgresql.core.BaseConnection.getEncoding()Lorg/postgresql/core/Encoding;
            //	at org.postgresql.jdbc2.AbstractJdbc2ResultSet.getString(AbstractJdbc2ResultSet.java:1889)
            //	at nl.b3p.web.IndexPageIntegrationTest.testDefaultUserPresent(IndexPageIntegrationTest.java:99)

            //actual_gebruikersnaam = rs.getObject(1, String.class);
            //actual_hash = rs.getString(2);
            actual_gebruikersnaam = new String(rs.getBytes(1));
            actual_hash = new String(rs.getBytes(2));
        }
        assertThat("Er is maar 1 'user' record (eerste en laatste zijn dezelfde record).", rs.isLast(), equalTo(rs.isFirst()));

        rs.close();
        connection.close();

        assertEquals("De record moet de verwachte naam hebben.", default_gebruikersnaam, actual_gebruikersnaam);
        assertEquals("De record moet de verwachte password hash hebben.", default_hash, actual_hash);
    }

    /**
     * Test of de brmo-service applicatie is gestart en de index pagina
     * opgehaald kan worden.
     *
     * @throws IOException als die optreedt tijdens ophalen pagina
     */
    @Test
    public void testIndex() throws IOException {
        response = client.execute(new HttpGet(BASE_TEST_URL + "/brmo-service/"));

        final String body = EntityUtils.toString(response.getEntity());
        assertThat("Response status is OK.", response.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_OK));
        assertNotNull("Response body mag niet null zijn.", body);
    }

    /**
     * Test login/index/about/logout sequentie.
     *
     * @throws IOException mag niet optreden
     * @throws URISyntaxException mag niet optreden
     */
    @Test
    public void testLoginLogout() throws IOException, URISyntaxException {
        // login
        response = client.execute(new HttpGet(BASE_TEST_URL));
        EntityUtils.consume(response.getEntity());

        HttpUriRequest login = RequestBuilder.post()
                .setUri(new URI(BASE_TEST_URL + "j_security_check"))
                .addParameter("j_username", "brmo")
                .addParameter("j_password", "brmo")
                .build();
        response = client.execute(login);
        EntityUtils.consume(response.getEntity());
        assertThat("Response status is OK.", response.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_OK));

        // index
        response = client.execute(new HttpGet(BASE_TEST_URL + "index.jsp"));
        String body = EntityUtils.toString(response.getEntity());
        assertNotNull("Response body mag niet null zijn.", body);
        assertTrue("Response moet 'BRMO Service' header hebben.", body.contains("<h1>BRMO Service</h1>"));

        // about
        response = client.execute(new HttpGet(BASE_TEST_URL + "about.jsp"));
        body = EntityUtils.toString(response.getEntity());
        assertNotNull("Response body mag niet null zijn.", body);
        assertTrue("Response moet 'BRMO versie' header hebben.", body.contains("<h1>BRMO versie informatie</h1>"));

        // logout
        response = client.execute(new HttpGet(BASE_TEST_URL + "logout.jsp"));
        body = EntityUtils.toString(response.getEntity());
        assertThat("Response status is OK.", response.getStatusLine().getStatusCode(),
                equalTo(HttpStatus.SC_OK));
        assertNotNull("Response body mag niet null zijn.", body);
        assertTrue("Response moet 'Opnieuw inloggen' tekst hebben.", body.contains("Opnieuw inloggen"));
    }

    @Override
    public void setUp() throws Exception {
        // void implementatie
    }
}
