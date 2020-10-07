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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

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
    @AfterEach
    public void cleanup() {
    }

    @Override
    @BeforeEach
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
        Assertions.assertNotNull(body, "Response body mag niet null zijn.");
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
        Assertions.assertNotNull(body, "Response body mag niet null zijn.");
        assertJsonEquals("{\"errors\":[]}", body);
    }


}
