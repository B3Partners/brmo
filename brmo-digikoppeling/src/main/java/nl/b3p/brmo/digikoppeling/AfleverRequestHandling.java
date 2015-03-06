/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.digikoppeling;

import nl.b3p.brmo.digipoort.koppelvlakservices._1_2.AfleverRequest;
import nl.b3p.brmo.digipoort.koppelvlakservices._1_2.AfleverResponse;

/**
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public interface AfleverRequestHandling {

    public enum STATUSCODE {

        ERROR, SUCCESS
    }

    /**
     * Verwerkt het verzoek.
     *
     * @param verzoek
     * @param antwoord
     */
    public void handle(AfleverRequest verzoek, AfleverResponse antwoord);
}
