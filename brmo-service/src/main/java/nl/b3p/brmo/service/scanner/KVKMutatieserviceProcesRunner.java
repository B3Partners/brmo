package nl.b3p.brmo.service.scanner;

import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.*;
import static nl.b3p.brmo.persistence.staging.KVKMutatieserviceProces.*;
import static nl.b3p.gds2.GDS2Util.getDatumTijd;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.TransactionRequiredException;
import javax.persistence.Transient;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.ClobElement;
import nl.b3p.brmo.persistence.staging.KVKMutatieserviceProces;
import nl.b3p.brmo.persistence.staging.NHRInschrijving;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;

public class KVKMutatieserviceProcesRunner extends AbstractExecutableProces {
  private static final Log LOG = LogFactory.getLog(KVKMutatieserviceProcesRunner.class);

  private final KVKMutatieserviceProces config;
  private int pages;
  private String ophalenVanaf = "";
  private String ophalenTot = "";
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
        LOG.debug(
            "Gebruik %s als apikey en %s als abonnementId."
                .formatted(config.getConfig().get(APIKEY), config.getConfig().get(ABONNEMENT_ID)));

        // datum tijd parsen/instellen
        GregorianCalendar vanaf;
        GregorianCalendar tot =
            getDatumTijd(ClobElement.nullSafeGet(this.config.getConfig().get(TOT)));
        String sVanaf = ClobElement.nullSafeGet(this.config.getConfig().get(VANAF));
        if (tot != null && ("-1".equals(sVanaf) || "-2".equals(sVanaf) || "-3".equals(sVanaf))) {
          vanaf =
              getDatumTijd(
                  ClobElement.nullSafeGet(this.config.getConfig().get(TOT)),
                  Integer.parseInt(sVanaf));
        } else {
          vanaf = getDatumTijd(sVanaf);
        }

        ophalenVanaf =
            vanaf == null ? "" : vanaf.toZonedDateTime().format(DateTimeFormatter.ISO_INSTANT);
        ophalenTot = tot == null ? "" : tot.toZonedDateTime().format(DateTimeFormatter.ISO_INSTANT);
        getPageFromAPI(/*start met p.1*/ 1);

        config.setSamenvatting(
            "Er zijn %d pagina's van de KVK Mutatieservice opgehaald en verwerkt."
                .formatted(pages));
        config.setStatus(WAITING);
        listener.updateStatus("KVK Mutatieservice proces voltooid op %tc.".formatted(new Date()));
      } catch (Exception e) {
        listener.exception(e);
        listener.updateStatus(ERROR.toString());
        listener.addLog(e.getMessage());
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

  private void getPageFromAPI(int pagina)
      throws JSONException,
          IllegalArgumentException,
          TransactionRequiredException,
          IOException,
          BrmoException {

    final String API_URL_PARAMS = "%s/%s?pagina=%d&vanaf=%s&tot=%s";
    String url =
        String.format(
            API_URL_PARAMS,
            config.getConfig().get(APIURL).getValue(),
            config.getConfig().get(ABONNEMENT_ID).getValue(),
            pagina,
            this.ophalenVanaf,
            this.ophalenTot);
    LOG.debug("Requesting KVK Mutatieservice API: " + url);
    listener.progress(pagina);
    listener.updateStatus(
        "Ophalen van pagina %d van %d van KVK mutaties.".formatted(pagina, pages));
    LOG.info("Ophalen van pagina %d van %d van KVK mutaties.".formatted(pagina, pages));

    try {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(url))
              .header(APIKEY, config.getConfig().get(APIKEY).getValue())
              .header("Accept", "application/json")
              .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        parseApiResponse(response.body());
      } else {
        // de API heeft een foutmelding teruggegeven, maar dat kan HTML zijn, dus die moet escaped
        // worden
        // zie ook KVK TopDesk melding M2507 3520
        String errorMsg =
            "Ophalen van mutatiedata is mislukt met statuscode %s en melding: %s"
                .formatted(response.statusCode(), StringEscapeUtils.escapeHtml4(response.body()));
        listener.updateStatus(ERROR.toString());
        config.setStatus(ERROR);
        throw new BrmoException(errorMsg);
      }
    } catch (InterruptedException | JSONException e) {
      LOG.error("Fout tijdens benaderen KVK Mutatieservice API data", e);
      listener.addLog("Fout tijdens benaderen KVK Mutatieservice API data: " + e.getMessage());
      listener.exception(e);
    }
  }

  private void parseApiResponse(String responseBody)
      throws JSONException,
          IllegalArgumentException,
          TransactionRequiredException,
          BrmoException,
          IOException {
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
    LOG.debug("Aantal signalen op pagina %d: %d".formatted(pagina, jsonObject.getInt("aantal")));

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
      getPageFromAPI(pagina + 1);
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
    listener.addLog(
        "Er zijn %s unieke KVK nummers met mutatie opgeslagen of bijgewerkt."
            .formatted(kvkNummers.size()));
  }
}
