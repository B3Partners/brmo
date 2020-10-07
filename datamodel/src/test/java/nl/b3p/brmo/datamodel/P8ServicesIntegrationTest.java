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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import static net.javacrumbs.jsonunit.JsonAssert.*;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * testcases om te kijken of we de P8 rest api niet stuk maken als er iets aan de views wordt geleuteld.
 * We testen alleen PostgreSQL.
 * <p>
 * deze test los draaien met: {@code mvn -Dit.test=P8ServicesIntegrationTest -Dtest.onlyITs=true verify -Pp8}
 * <p>
 * <strong>Door een bug in de P8 api wordt het tijdstip (GMT) van uitvragen toegevoegd aan de datum begin geldigheid,
 * daarom wordt op een aantal plaatsen de datum niet gechecked</strong>
 *
 * @author mark
 */
public class P8ServicesIntegrationTest extends P8TestFramework {

    private static final Log LOG = LogFactory.getLog(P8ServicesIntegrationTest.class);

    private static boolean didThisAllready = false;

    private HttpResponse response;

    @Override
    @AfterEach
    public void cleanup() throws SQLException {
        rsgb.close();
        dsRsgb.close();
    }

    @Override
    @BeforeEach
    public void setup() throws SQLException {
        this.setUpDB();

        if (!didThisAllready) {
            // LOG.debug("begin laden test data");
            // TODO testdata laden
            // data wordt nu niet nieuw geladen,
            // alleen de views nieuw aangemaakt en ververst, dat gebeurt met een bash script
            // zie .travis/reinstall-rsgb-views-pgsql.sh
            // TODO verversen van de views na laden van test data
            // LOG.debug("ververs test data in rsgb views");
            // ViewUtils.refreshKnownMViews(dsRsgb);
            didThisAllready = true;
        }
    }

    @Test
    public void testPercelen() throws IOException {
        // https://imkad-b3.p8.nl/web/kadastralepercelen.json?offset=0&limit=3&_format=json
        HttpUriRequest request = RequestBuilder.get(params.getProperty("p8.baseurl") + "kadastralepercelen.json")
                .addParameter("offset", "0")
                .addParameter("limit", "3")
                .addParameter("_format", "json")
                .build();
        response = client.execute(request);
        String body = EntityUtils.toString(response.getEntity());
        LOG.debug("antwoord: " + body);
        assertThat("Response status is OK.", response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        assertNotNull(body, "Response body mag niet null zijn.");
        assertJsonEquals(
                "{\"kadastrale_percelen\":[{\"kadastrale_code\":\"VDG00B1708\",\"gemeente_code\":\"VDG00\",\"sectie\":\"B\",\"perceelnummer\":1708,\"oppervlakte\":65,\"straat\":\"Oosterstraat\",\"huisnummer\":\"33\",\"postcode\":\"3134NM\",\"woonplaats\":\"Vlaardingen\",\"percnr17\":\"VDG00B1708\"},{\"kadastrale_code\":\"VDG00B1708\",\"gemeente_code\":\"VDG00\",\"sectie\":\"B\",\"perceelnummer\":1708,\"oppervlakte\":65,\"straat\":\"Oosterstraat\",\"huisnummer\":\"35\",\"postcode\":\"3134NM\",\"woonplaats\":\"Vlaardingen\",\"percnr17\":\"VDG00B1708\"},{\"kadastrale_code\":\"VDG00B1709\",\"gemeente_code\":\"VDG00\",\"sectie\":\"B\",\"perceelnummer\":1709,\"oppervlakte\":65,\"straat\":\"Oosterstraat\",\"huisnummer\":\"29\",\"postcode\":\"3134NM\",\"woonplaats\":\"Vlaardingen\",\"percnr17\":\"VDG00B1709\"}],\"offset\":\"0\",\"limit\":\"3\",\"total_item_count\":1000}",
                body,
                when(IGNORING_ARRAY_ORDER)
        );
    }


    @Test
    public void testKadastraalPerceel() throws IOException {
        // https://imkad-b3.p8.nl/web/kadastraalperceel.json?kadperceelcode=VDG00B1708&geoinfo=true&_format=json
        HttpUriRequest request = RequestBuilder.get(params.getProperty("p8.baseurl") + "kadastraalperceel.json")
                .addParameter("kadperceelcode", "VDG00B1708")
                .addParameter("geoinfo", "true")
                .addParameter("_format", "json")
                .build();
        response = client.execute(request);
        String body = EntityUtils.toString(response.getEntity());
        LOG.debug("antwoord: " + body);
        assertThat("Response status is OK.", response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        assertNotNull(body, "Response body mag niet null zijn.");
        assertJsonEquals(
                "{\"kadastrale_code\":\"VDG00B1708\",\"gemeente_code\":\"VDG00\",\"sectie\":\"B\",\"perceelnummer\":1708,\"oppervlakte\":65,\"adres\":\"OOSTERSTR 33, 3134NM VLAARDINGEN  (1 meer adressen)\",\"postcode\":\"3134NM\",\"woonplaats\":\"Vlaardingen\",\"gemeente\":\"Vlaardingen\",\"geom\":\"MULTIPOLYGON (((83442.009 435842.213, 83445.535 435831.176, 83451.303 435833.022, 83447.831 435844.075, 83442.009 435842.213)))\",\"rechten\":[{\"subject\":{\"naam\":\"5a063d4c 251bf\",\"type\":\"perceel\",\"persoonid\":\"NL.KAD.Persoon.157125580\",\"woonplaats\":\"Vlaardingen\"},\"aandeel\":\"1\\/1\",\"recht_soort\":\"INGESCHREVEN NATUURLIJK PERSOON\",\"datum_ingang\":\"2012-12-31T09:02:46+00:00\"},{\"subject\":{\"naam\":\"Gemeente Vlaardingen (Kad Gem Vlaardingen Sectie B)\",\"type\":\"perceel\",\"persoonid\":\"NL.KAD.Persoon.159287767\",\"woonplaats\":\"Vlaardingen\"},\"aandeel\":\"1\\/1\",\"recht_soort\":\"INGESCHREVEN NIET-NATUURLIJK PERSOON\",\"datum_ingang\":\"2012-12-31T09:02:46+00:00\"},{\"subject\":{\"naam\":\"5a063d4c 251bf\",\"type\":\"perceel\",\"persoonid\":\"NL.KAD.Persoon.157125580\",\"woonplaats\":\"Vlaardingen\"},\"aandeel\":\"1\\/1\",\"recht_soort\":\"INGESCHREVEN NATUURLIJK PERSOON\",\"datum_ingang\":\"2012-12-31T09:02:46+00:00\"},{\"subject\":{\"naam\":\"Gemeente Vlaardingen (Kad Gem Vlaardingen Sectie B)\",\"type\":\"perceel\",\"persoonid\":\"NL.KAD.Persoon.159287767\",\"woonplaats\":\"Vlaardingen\"},\"aandeel\":\"1\\/1\",\"recht_soort\":\"INGESCHREVEN NIET-NATUURLIJK PERSOON\",\"datum_ingang\":\"2012-12-31T09:02:46+00:00\"}],\"adressen\":[{\"woonplaats\":\"Vlaardingen\",\"straat\":\"Oosterstraat\",\"postcode\":\"3134NM\",\"huisnummer\":\"33\",\"postadres\":false},{\"woonplaats\":\"Vlaardingen\",\"straat\":\"Oosterstraat\",\"postcode\":\"3134NM\",\"huisnummer\":\"35\",\"postadres\":false}]}",
                body,
                //when(IGNORING_ARRAY_ORDER),
                whenIgnoringPaths("rechten[*].datum_ingang")
        );
    }

    @Test
    public void testKadastralepercelenadressen() throws IOException {
        //https://imkad-b3.p8.nl/web/kadastralepercelenadressen.json?offset=0&limit=3&_format=json
        HttpUriRequest request = RequestBuilder.get(params.getProperty("p8.baseurl") + "kadastralepercelenadressen.json")
                .addParameter("offset", "0")
                .addParameter("limit", "3")
                .addParameter("_format", "json")
                .build();
        response = client.execute(request);
        String body = EntityUtils.toString(response.getEntity());
        LOG.debug("antwoord: " + body);
        assertThat("Response status is OK.", response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        assertNotNull(body, "Response body mag niet null zijn.");
        assertJsonEquals(
                "{\"kadastrale_percelen\":[{\"kadastrale_code\":\"VDG00B1708\",\"gemeente_code\":\"VDG00\",\"sectie\":\"B\",\"perceelnummer\":1708,\"oppervlakte\":65,\"straat\":\"Oosterstraat\",\"huisnummer\":\"33\",\"postcode\":\"3134NM\",\"woonplaats\":\"Vlaardingen\",\"percnr17\":\"VDG00B1708\"},{\"kadastrale_code\":\"VDG00B1708\",\"gemeente_code\":\"VDG00\",\"sectie\":\"B\",\"perceelnummer\":1708,\"oppervlakte\":65,\"straat\":\"Oosterstraat\",\"huisnummer\":\"35\",\"postcode\":\"3134NM\",\"woonplaats\":\"Vlaardingen\",\"percnr17\":\"VDG00B1708\"},{\"kadastrale_code\":\"VDG00B1709\",\"gemeente_code\":\"VDG00\",\"sectie\":\"B\",\"perceelnummer\":1709,\"oppervlakte\":65,\"straat\":\"Oosterstraat\",\"huisnummer\":\"29\",\"postcode\":\"3134NM\",\"woonplaats\":\"Vlaardingen\",\"percnr17\":\"VDG00B1709\"}],\"offset\":\"0\",\"limit\":\"3\",\"total_item_count\":1000}",
                body,
                when(IGNORING_ARRAY_ORDER)
        );
    }

    @Test
    public void testKadastralesubjectpercelen() throws IOException {
        // https://imkad-b3.p8.nl/web/kadastralesubjectpercelen.json?offset=0&limit=3&_format=json&subjectid=NL.KAD.Persoon.157450463
        HttpUriRequest request = RequestBuilder.get(params.getProperty("p8.baseurl") + "kadastralesubjectpercelen.json")
                .addParameter("offset", "0")
                .addParameter("limit", "3")
                .addParameter("subjectid", "NL.KAD.Persoon.157450463")
                .addParameter("_format", "json")
                .build();
        response = client.execute(request);
        String body = EntityUtils.toString(response.getEntity());
        LOG.debug("antwoord: " + body);
        assertThat("Response status is OK.", response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        assertNotNull(body, "Response body mag niet null zijn.");
        assertJsonEquals(
                "{\"kadastrale_percelen\":[{\"kadastrale_code\":\"VDG00B1712\",\"gemeente_code\":\"VDG00\",\"sectie\":\"B\",\"perceelnummer\":1712,\"oppervlakte\":68,\"percnr17\":\"VDG00B1712\",\"aandeel\":\"1\\/2\",\"rechtsoort\":\"INGESCHREVEN NATUURLIJK PERSOON\",\"datum_ingang\":\"2012-12-31T09:06:24+00:00\"}],\"offset\":\"0\",\"limit\":\"3\",\"total_item_count\":1}",
                body,
                // when(IGNORING_ARRAY_ORDER)
                // TODO bug in P8; verzint een tijdstip aan datums
                whenIgnoringPaths("kadastrale_percelen[0].datum_ingang")

        );
    }

    @Test
    public void testSubject() throws IOException {
        // https://imkad-b3.p8.nl/web/subject.json?subjectid=NL.KAD.Persoon.157450463&_format=json
        HttpUriRequest request = RequestBuilder.get(params.getProperty("p8.baseurl") + "subject.json")
                .addParameter("subjectid", "NL.KAD.Persoon.157450463")
                .addParameter("_format", "json")
                .build();
        response = client.execute(request);
        String body = EntityUtils.toString(response.getEntity());
        LOG.debug("antwoord: " + body);
        assertThat("Response status is OK.", response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        assertNotNull(body, "Response body mag niet null zijn.");
        assertJsonEquals(
                "{\"subjectid\":\"NL.KAD.Persoon.157450463\",\"type\":\"KadastraalNatuurlijkSubject\",\"adres\":\"Oosterstraat 17, 3134NM VLAARDINGEN\",\"natuurlijk_subject\":{\"voornaam\":\"d941\",\"achternaam\":\"abda6a2\",\"geslacht\":\"M\",\"geboorte_datum\":\"1964-01-09T09:07:45+00:00\",\"geboorte_plaats\":\"4379c2\"}}",
                body,
                // TODO bug in P8; verzint een tijdstip aan datums
                whenIgnoringPaths("natuurlijk_subject.geboorte_datum")
        );
    }

    @Test
    public void testSubjecten() throws IOException {
        // https://imkad-b3.p8.nl/web/subjecten.json?offset=0&limit=3&_format=json
        HttpUriRequest request = RequestBuilder.get(params.getProperty("p8.baseurl") + "subjecten.json")
                .addParameter("offset", "0")
                .addParameter("limit", "3")
                .addParameter("_format", "json")
                .build();
        response = client.execute(request);
        String body = EntityUtils.toString(response.getEntity());
        LOG.debug("antwoord: " + body);
        assertThat("Response status is OK.", response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));
        assertNotNull(body, "Response body mag niet null zijn.");
        assertJsonEquals(
                "{\"subjecten\":[{\"subjectid\":\"NL.KAD.Persoon.158819809\",\"voornamen\":\"7efe9\",\"achternaam\":\"ec3e8\",\"geslacht\":\"V\",\"adres\":\"\"},{\"subjectid\":\"NL.KAD.Persoon.157125580\",\"voornamen\":\"5a063d4c\",\"achternaam\":\"251bf\",\"geslacht\":\"M\",\"geboorte_datum\":{},\"adres\":\"Oosterstraat 35, 3134NM VLAARDINGEN\"},{\"subjectid\":\"NL.KAD.Persoon.158911578\",\"voornamen\":\"73427d 7345\",\"achternaam\":\"11fba2\",\"geslacht\":\"M\",\"geboorte_datum\":{},\"adres\":\"van der Waalsstraat 86, 3132TN VLAARDINGEN\"}],\"offset\":\"0\",\"limit\":\"3\",\"total_item_count\":1000}",
                body,
                when(IGNORING_ARRAY_ORDER)
        );
    }
}
