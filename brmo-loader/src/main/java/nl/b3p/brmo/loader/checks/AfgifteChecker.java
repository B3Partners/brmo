/*
*  Copyright (C) 2019  B3Partners B.V.

* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.

* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.

* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package nl.b3p.brmo.loader.checks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import nl.b3p.brmo.loader.StagingProxy;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Meine Toonen
 */
public class AfgifteChecker {
  private static final Log log = LogFactory.getLog(AfgifteChecker.class);

  private List<Afgifte> afgiftes;
  private StagingProxy staging;

  public void init(String input, StagingProxy staging) throws IOException {
    try (FileInputStream fin = new FileInputStream(new File(input))) {
      init(fin, staging);
    }
  }

  public void init(InputStream input, StagingProxy staging) throws IOException {
    this.staging = staging;
    // parse csv/excel
    AfgiftelijstParser ap = new AfgiftelijstParser();
    afgiftes = ap.parse(input);
  }

  public void check() {
    afgiftes.forEach(this::check);
  }

  /**
   * afgifte controle.
   *
   * @param afgifte de te controleren afgifte
   */
  private void check(Afgifte afgifte) {
    try {
      LaadProces lp = staging.getLaadProcesByRestoredFilename(afgifte.getBestandsnaam());
      // kijk of afgifte bestaat in staging
      if (lp != null) {
        afgifte.setFoundInStaging(true);
        processFoundLaadprocess(afgifte, lp);
      }
    } catch (SQLException ex) {
      log.error("Error querying staging for laadproces for afgifte: " + afgifte.toString(), ex);
    }
  }

  /**
   * zoek de status van alle bijbehorende berichten op en leg die vast in de afgifte.
   *
   * @param afgifte de te controleren afgifte
   * @param lp het laadproces waar de berichten bij zoeken
   * @throws SQLException if any
   */
  private void processFoundLaadprocess(Afgifte afgifte, LaadProces lp) throws SQLException {
    List<Bericht> berichten = staging.getBerichtByLaadProces(lp);
    Map<Bericht.STATUS, Integer> counts = afgifte.getStatussen();
    berichten.stream()
        .peek(
            (bericht) -> {
              if (!counts.containsKey(bericht.getStatus())) {
                counts.put(bericht.getStatus(), 0);
              }
            })
        .forEachOrdered(
            (bericht) -> counts.put(bericht.getStatus(), counts.get(bericht.getStatus()) + 1));
  }

  public File getResults(String input, String f) throws IOException {
    return getResults(input, new File(f));
  }

  public File getResults(String input, File f) throws IOException {
    AfgiftelijstReport reporter = new AfgiftelijstReport();

    reporter.createReport(afgiftes, input, f);
    return f;
  }
}
