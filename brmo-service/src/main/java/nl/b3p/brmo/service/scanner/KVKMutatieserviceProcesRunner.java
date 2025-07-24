package nl.b3p.brmo.service.scanner;

import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.*;
import static nl.b3p.brmo.persistence.staging.KVKMutatieserviceProces.ABONNEMENT_ID;
import static nl.b3p.brmo.persistence.staging.KVKMutatieserviceProces.APIKEY;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.TransactionRequiredException;
import javax.persistence.Transient;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.KVKMutatieserviceProces;
import nl.b3p.brmo.persistence.staging.NHRInschrijving;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;

public class KVKMutatieserviceProcesRunner extends AbstractExecutableProces {
  private static final Log LOG = LogFactory.getLog(KVKMutatieserviceProcesRunner.class);
  private static final String API_URL = "https://api.kvk.nl/test/api/v1/abonnementen/%s?pagina=%d";
  private final KVKMutatieserviceProces config;
  private int pages;
  private final Set<String> kvkNummers = new HashSet<>();
  @Transient private ProgressUpdateListener listener;

  public static Log getLog() {
    return LOG;
  }

  public KVKMutatieserviceProcesRunner(KVKMutatieserviceProces config) {
    this.config = config;
  }

  @Override
  public void execute() throws BrmoException {
    this.execute(
        new ProgressUpdateListener() {
          @Override
          public void total(long total) {}

          @Override
          public void progress(long progress) {}

          @Override
          public void exception(Throwable t) {
            LOG.error(t);
          }

          @Override
          public void updateStatus(String status) {}

          @Override
          public void addLog(String log) {
            KVKMutatieserviceProcesRunner.LOG.info(log);
            config.updateSamenvattingEnLogfile(log);
            Stripersist.getEntityManager().merge(config);
          }
        });
  }

  @Override
  public void execute(ProgressUpdateListener listener) {
    this.listener = listener;
    listener.updateStatus("KVK Mutatieservice proces gestart op %tc.".formatted(new Date()));

    if (config.getStatus().equals(WAITING) || config.getStatus().equals(ERROR)) {
      if (config.getStatus().equals(ERROR)) {
        listener.addLog("Vorige run is met ERROR status afgerond, opnieuw proberen");
      }
      try {
        listener.updateStatus(PROCESSING.toString());
        config.setStatus(PROCESSING);
        Stripersist.getEntityManager().merge(config);
        listener.addLog("Gebruik %s als apikey.".formatted(config.getConfig().get(APIKEY)));
        listener.addLog(
            "Gebruik %s als abonnementId.".formatted(config.getConfig().get(ABONNEMENT_ID)));

        getPageFromAPI(
            config.getConfig().get(APIKEY).getValue(),
            config.getConfig().get(ABONNEMENT_ID).getValue(),
            1);

        config.setSamenvatting(
            "Er zijn %d pagina's KVK mutaties opgehaald en verwerkt.".formatted(pages));
        config.setStatus(WAITING);
        listener.updateStatus("KVK Mutatieservice proces voltooid op %tc.".formatted(new Date()));
      } catch (Exception e) {
        listener.exception(e);
        listener.updateStatus(ERROR.toString());
        config.setStatus(ERROR);
      } finally {
        config.setLastrun(new Date());
        Stripersist.getEntityManager().merge(config);
        Stripersist.getEntityManager().flush();
      }

    } else {
      // PROCESSING
      // Als het proces al bezig is of succesvol is afgerond, doen we niets
      listener.addLog("KVK Mutatieservice proces is al bezig, geen actie nodig.");
      LOG.info("KVK Mutatieservice proces is al bezig, geen actie nodig.");
    }
  }

  private void getPageFromAPI(String apikey, String abonnementId, int pagina) {
    // execute a http request to the KVK Mutatieservice API
    // similar to curl
    // https://api.kvk.nl/test/api/v1/abonnementen/1365acf9-0b63-3ed4-9e68-a8c37425080b?pagina=2 -H
    // "apikey: l7xx1f2691f2520d487b902f4e0b57a0b197"
    // using the apikey header and abonnementId as a url part and pagina as a query parameter and
    // the template in API_URL
    String url = String.format(API_URL, abonnementId, pagina);
    LOG.debug("Requesting KVK Mutatieservice API: " + url);
    listener.progress(pagina);
    listener.addLog("Verwerken van pagina %d van KVK mutaties.".formatted(pagina));

    try {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request =
          HttpRequest.newBuilder().uri(URI.create(url)).header(APIKEY, apikey).build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        parseApiResponse(response.body());
      } else {
        LOG.error(
            "Ophalen van mutatiedata is mislukt met statuscode %s en melding: %s"
                .formatted(response.statusCode(), response.body()));
      }
    } catch (IOException | InterruptedException | JSONException e) {
      LOG.error("Fout tijdens benaderen KVK Mutatieservice API data", e);
      listener.addLog("Fout tijdens benaderen KVK Mutatieservice API data: " + e.getMessage());
      listener.exception(e);
    }
  }

  private void parseApiResponse(String responseBody)
      throws JSONException, IllegalArgumentException, TransactionRequiredException {
    // test json response:
    //          {
    //                  "pagina": 1,
    //                  "aantal": 100,
    //                  "totaal": 14344,
    //                  "totaalPaginas": 144,
    //                  "signalen": [
    //              {
    //                  "id": "ed6b886b-39e2-46d9-8175-85a48f227c40",
    //                      "timestamp": "2025-07-17T13:16:01.86692Z",
    //                      "kvknummer": "90003403",
    //                      "signaalType": "SignaalGewijzigdeInschrijving"
    //              },
    //              {
    //                  "id": "a124dca0-de17-4d10-af8a-9e0ad79da1ed",
    //                      "timestamp": "2025-07-17T13:16:01.885015Z",
    //                      "kvknummer": "90004604",
    //                      "signaalType": "SignaalGewijzigdeInschrijving"
    //              }
    //              ]}
    LOG.debug("Parsing API response: " + responseBody);
    JSONObject jsonObject = new JSONObject(responseBody);
    pages = jsonObject.getInt("totaalPaginas");
    int pagina = jsonObject.getInt("pagina");
    listener.total(pages);
    listener.addLog(
        "Aantal signalen op pagina %d: %d".formatted(pagina, jsonObject.getInt("aantal")));

    final JSONArray signalen = jsonObject.getJSONArray("signalen");
    for (int i = 0; i < signalen.length(); i++) {
      JSONObject signaal = signalen.getJSONObject(i);
      String kvkNummer = signaal.getString("kvknummer");
      if (kvkNummers.add(kvkNummer)) {
        listener.addLog("KVK nummer %s toegevoegd van pagina %d.".formatted(kvkNummer, pagina));
      }
    }
    if (pagina < pages) {
      // If there are more pages, fetch the next page
      getPageFromAPI(
          config.getConfig().get(APIKEY).getValue(),
          config.getConfig().get(ABONNEMENT_ID).getValue(),
          pagina + 1);
    } else {
      listener.addLog("Alle %d pagina's KVK mutaties zijn opgehaald.".formatted(pages));
      storeKvkNummers();
    }
  }

  private void storeKvkNummers() throws IllegalArgumentException, TransactionRequiredException {
    for (String kvkNummer : kvkNummers) {
      NHRInschrijving inschrijving =
          Stripersist.getEntityManager().find(NHRInschrijving.class, kvkNummer);
      if (inschrijving == null) {
        inschrijving = new NHRInschrijving(kvkNummer);
      } else {
        inschrijving.setDatum(new Date());
        inschrijving.setVolgendProberen(new Date());
        inschrijving.setProbeerAantal(0);
      }
      Stripersist.getEntityManager().merge(inschrijving);
    }
    listener.addLog("KVK nummers opgeslagen of bijgewerkt: " + kvkNummers.size());
  }
}
