/*
 * Copyright (C) 2019 B3Partners B.V.
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
package nl.b3p.brmo.datamodel;

import nl.b3p.brmo.test.util.database.ViewUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * testcases om te kijken of we de P8 rest api niet stuk maken als er iets aan de views wordt geleuteld.
 * In deze integratie test alleen de niet-data specifieke endpoints testen.
 * <p>
 * deze test los draaien met: {@code mvn -Dit.test=P8RestAPIIntegrationTest verify -Pp8}
 *
 * @author mark
 */
public class P8RestAPIIntegrationTest extends P8TestFramework {
    private HttpResponse response;

    @Override
    @After
    public void cleanup() {
    }

    @Override
    @Before
    public void setup() {
    }

    @Test
    public void testVersion() throws IOException {
        // https://imkad-b3.p8.nl/web/version.json?_format=json
        HttpUriRequest request = RequestBuilder.get(params.getProperty("p8.baseurl") + "version.json")
                .addParameter("_format", "json")
                .build();
        response = client.execute(request);
        String body = EntityUtils.toString(response.getEntity());

        assertThat("Response status is OK.", response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        assertNotNull("Response body mag niet null zijn.", body);
        assertJsonEquals("\"imkad_api_1.8.20\"", body);
    }

    @Test
    public void testEndpointstatus() throws IOException {
        // https://imkad-b3.p8.nl/web/endpointstatus.json?_format=json
        HttpUriRequest request = RequestBuilder.get(params.getProperty("p8.baseurl") + "endpointstatus.json")
                .addParameter("_format", "json")
                .build();
        response = client.execute(request);
        String body = EntityUtils.toString(response.getEntity());

        assertThat("Response status is OK.", response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        assertNotNull("Response body mag niet null zijn.", body);
        assertJsonEquals("{\"errors\":[]}", body);
    }


}
