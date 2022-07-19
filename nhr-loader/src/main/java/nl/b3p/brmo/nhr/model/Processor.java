/*
 * Copyright (C) 2022 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.nhr.model;

import nl.kvk.schemas.schemas.hrip.dataservice._2015._02.InschrijvingResponseType;
import nl.kvk.schemas.schemas.hrip.catalogus._2015._02.MaatschappelijkeActiviteitType;
import nl.kvk.schemas.schemas.hrip.catalogus._2015._02.PersoonType;
import nl.kvk.schemas.schemas.hrip.catalogus._2015._02.PersoonRelatieRegistratieType;

import java.util.Map;
import java.util.HashMap;

public class Processor {
    private Map<String, Persoon> persoonStore = new HashMap<>();

    private String registerPersoon(Persoon persoon) {
        String id = persoon.getId();
        Persoon existingPersoon = persoonStore.getOrDefault(id, null);
        if (existingPersoon != null && !existingPersoon.equals(persoon)) {
            throw new Error("Found two different Persoon objects with the same ID but different contents (" + id + ")");
        }

        if (existingPersoon == null) {
            persoonStore.put(id, persoon);
        }

        return id;
    }

    public String process(PersoonType persoon) {
        if (persoon == null) return null;

        Persoon pers = Persoon.fromAny(persoon);
        return registerPersoon(pers);
    }

    public String process(PersoonRelatieRegistratieType typ) {
        if (typ == null) return null;
        if (typ.getNaamPersoon() != null) {
            return process(typ.getNaamPersoon());
        }
        if (typ.getNatuurlijkPersoon() != null) {
            return process(typ.getNatuurlijkPersoon());
        }
        if (typ.getBuitenlandseVennootschap() != null) {
            return process(typ.getBuitenlandseVennootschap());
        }
        if (typ.getEenmanszaakMetMeerdereEigenaren() != null) {
            return process(typ.getEenmanszaakMetMeerdereEigenaren());
        }
        if (typ.getRechtspersoon() != null) {
            return process(typ.getRechtspersoon());
        }
        if (typ.getRechtspersoonInOprichting() != null) {
            return process(typ.getRechtspersoonInOprichting());
        }
        if (typ.getSamenwerkingsverband() != null) {
            return process(typ.getSamenwerkingsverband());
        }

        // TODO: process(typ.getAfgeslotenMoeder());
        return null;
    }

    public void process(MaatschappelijkeActiviteitType maatschappelijkeActiviteit) {
        process(maatschappelijkeActiviteit.getHeeftAlsEigenaar());
        process(maatschappelijkeActiviteit.getHadAlsEigenaar());
    }

    public void process(InschrijvingResponseType.Product product) {
        process(product.getMaatschappelijkeActiviteit());
    }

    public void process(InschrijvingResponseType response) {
        process(response.getProduct());
    }

    public void dumpState() {
        System.out.println("Persoon:");
        for (String id : persoonStore.keySet()) {
            System.out.printf("%s\t%s\n", id, persoonStore.get(id).toString());
        }
    }
}
