/*
 * Copyright (C) 2022 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.nhr.model;

import nl.kvk.schemas.schemas.hrip.catalogus._2015._02.PersoonType;
import nl.kvk.schemas.schemas.hrip.catalogus._2015._02.NaamPersoonType;
import nl.kvk.schemas.schemas.hrip.catalogus._2015._02.NatuurlijkPersoonType;
import nl.kvk.schemas.schemas.hrip.catalogus._2015._02.NietNatuurlijkPersoonType;
import nl.kvk.schemas.schemas.hrip.catalogus._2015._02.RechtspersoonType;
import nl.kvk.schemas.schemas.hrip.catalogus._2015._02.SamenwerkingsverbandType;
import nl.kvk.schemas.schemas.hrip.catalogus._2015._02.RechtspersoonInOprichtingType;
import nl.kvk.schemas.schemas.hrip.catalogus._2015._02.EenmanszaakMetMeerdereEigenarenType;
import nl.kvk.schemas.schemas.hrip.catalogus._2015._02.BuitenlandseVennootschapType;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class Persoon {
    public String getId() {
        if (bsn != null) {
            return "nhr.bsn." + bsn;
        } else if (tin != null) {
            return "nhr.tin." + tin;
        } else if (rsin != null) {
            return "nhr.rsin." + rsin;
        } else {
            // TODO: stabiel genoeg?
            return "nhr.persoon." + achternaam + geboortedatum + geboorteland + geboorteplaats;
        }
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
 
    // dynamic-dispatch style behavior.
    public static Persoon fromAny(PersoonType persoon) {
        if (persoon instanceof BuitenlandseVennootschapType) {
            return Persoon.from((BuitenlandseVennootschapType) persoon);
        } else if (persoon instanceof EenmanszaakMetMeerdereEigenarenType) {
            return Persoon.from((EenmanszaakMetMeerdereEigenarenType) persoon);
        } else if (persoon instanceof RechtspersoonInOprichtingType) {
            return Persoon.from((RechtspersoonInOprichtingType) persoon);
        } else if (persoon instanceof SamenwerkingsverbandType) {
            return Persoon.from((SamenwerkingsverbandType) persoon);
        } else if (persoon instanceof RechtspersoonType) {
            return Persoon.from((RechtspersoonType) persoon);
        } else if (persoon instanceof NietNatuurlijkPersoonType) {
            return Persoon.from((NietNatuurlijkPersoonType) persoon);
        } else if (persoon instanceof NatuurlijkPersoonType) {
            return Persoon.from((NatuurlijkPersoonType) persoon);
        } else if (persoon instanceof NaamPersoonType) {
            return Persoon.from((NaamPersoonType) persoon);
        } else {
            return Persoon.from(persoon);
        }

    }
    public static Persoon from(PersoonType persoon) {
        Persoon result = new Persoon();
        result.persoonrechtsvorm = persoon.getPersoonRechtsvorm();
        result.volledigenaam = persoon.getVolledigeNaam();
        result.uitgebreiderechtsvorm = persoon.getUitgebreideRechtsvorm();

        result.datumaanvang = persoon.getRegistratie().getDatumAanvang();
        result.datumeinde = persoon.getRegistratie().getDatumEinde();
        result.registratietijdstip = persoon.getRegistratie().getRegistratieTijdstip();
        return result;
    }

    public static Persoon from(NaamPersoonType naamPersoon) {
        Persoon persoon = Persoon.from((PersoonType) naamPersoon);
        persoon.type = "NaamPersoon";
        persoon.naam = naamPersoon.getNaam();
        if (naamPersoon.getTelefoonnummer() != null) {
          persoon.naampersoon_nummer = naamPersoon.getTelefoonnummer().getNummer();
          persoon.naampersoon_toegangscode = naamPersoon.getTelefoonnummer().getToegangscode();
        }
        return persoon;
    }

    public static Persoon from(NatuurlijkPersoonType natuurlijkPersoon) {
        Persoon persoon = Persoon.from((PersoonType) natuurlijkPersoon);
        persoon.type = "NatuurlijkPersoon";

        // persoon.achternaam = natuurlijkPersoon.getAchternaam();
        // TODO: extraElementen
        persoon.bsn = natuurlijkPersoon.getBsn();
        persoon.geboortedatum = natuurlijkPersoon.getGeboortedatum();
        persoon.geboorteland = natuurlijkPersoon.getGeboorteland().getCode();
        persoon.geboorteplaats = natuurlijkPersoon.getGeboorteplaats();
        persoon.geslachtsaanduiding = natuurlijkPersoon.getGeslachtsaanduiding().getCode();
        // persoon.nationaliteit = natuurlijkPersoon.getNationaliteit();
        // TODO: doesn't exist?
        persoon.overlijdensdatum =  natuurlijkPersoon.getOverlijdensdatum();
        // TODO: persoon.tin = natuurlijkPersoon.getTin();
        persoon.voornamen = natuurlijkPersoon.getVoornamen();
        // TODO: persoon.voorvoegsel = natuurlijkPersoon.getVoorvoegsel();
        // TODO: extraElementen
        return persoon;
    }
    public static Persoon from(NietNatuurlijkPersoonType nietNatuurlijkPersoon) {
        Persoon persoon = Persoon.from((PersoonType) nietNatuurlijkPersoon);
        persoon.type = "NietNatuurlijkPersoon";

        persoon.datumuitschrijving = nietNatuurlijkPersoon.getDatumUitschrijving();
        persoon.rsin = nietNatuurlijkPersoon.getRsin();

        // datumAanvangStatuten datumAkteStatuten are part of extraElementen.
        // TODO: persoon.statutendatumaanvang = nietNatuurlijkPersoon.getStatutenDatumAanvang();
        // TODO: persoon.statutendatumakte = nietNatuurlijkPersoon.getStatutenDatumAkte();
        return persoon;
    }

    public static Persoon from(RechtspersoonType rechtspersoon) {
        Persoon persoon = Persoon.from((NietNatuurlijkPersoonType) rechtspersoon);
        persoon.type = "Rechtspersoon";

        persoon.aanvangstatutairezetel = rechtspersoon.getAanvangStatutaireZetel();
        persoon.activiteitengestaaktper = rechtspersoon.getActiviteitenGestaaktPer();
        if (rechtspersoon.getBeleggingsMijMetVeranderlijkKapitaal() != null) {
            persoon.beleggingsmijmetveranderlijkkapitaal = rechtspersoon.getBeleggingsMijMetVeranderlijkKapitaal().getCode().charAt(0);
        }

        persoon.datumakteoprichting = rechtspersoon.getDatumAkteOprichting();
        persoon.datumaktestatutenwijziging = rechtspersoon.getDatumAkteStatutenwijziging();
        persoon.datumeersteinschrijvinghandelsregister = rechtspersoon.getDatumEersteInschrijvingHandelsregister();
        persoon.datumoprichting = rechtspersoon.getDatumOprichting();
        persoon.ingangstatuten = rechtspersoon.getIngangStatuten();
        persoon.nieuwgemelderechtsvorm = rechtspersoon.getNieuwGemeldeRechtsvorm();
        if (rechtspersoon.getOverigePrivaatrechtelijkeRechtsvorm() != null) {
            persoon.overigeprivaatrechtelijkrechtsvorm = rechtspersoon.getOverigePrivaatrechtelijkeRechtsvorm().getCode();
        }
        if (rechtspersoon.getPubliekrechtelijkeRechtsvorm() != null) {
            persoon.publiekrechtelijkerechtsvorm = rechtspersoon.getPubliekrechtelijkeRechtsvorm().getCode();
        }
        if (rechtspersoon.getRechtsbevoegdheidVereniging() != null) {
            persoon.rechtsbevoegdheidvereniging = rechtspersoon.getRechtsbevoegdheidVereniging().getCode();
        }
        if (rechtspersoon.getRechtsvorm() != null) {
            persoon.rechtsvorm = rechtspersoon.getRechtsvorm().getCode();
        }
        persoon.statutairezetel = rechtspersoon.getStatutaireZetel();
        if (rechtspersoon.getStelselInrichting() != null) {
            persoon.stelselinrichting = rechtspersoon.getStelselInrichting().getCode();
        }

        if (rechtspersoon.getStructuur() != null) {
            persoon.structuur = rechtspersoon.getStructuur().getCode();
        }
        return persoon;
    }

    public static Persoon from(SamenwerkingsverbandType samenwerkingsverband) {
        Persoon persoon = Persoon.from((NietNatuurlijkPersoonType) samenwerkingsverband);
        persoon.type = "Samenwerkingsverband";

        persoon.aantalcommanditairevennoten = samenwerkingsverband.getAantalCommanditaireVennoten().toString();
        persoon.rechtsvorm = samenwerkingsverband.getRechtsvorm().getCode();

        return persoon;
    }

    public static Persoon from(RechtspersoonInOprichtingType rechtspersoonInOprichting) {
        Persoon persoon = Persoon.from((NietNatuurlijkPersoonType) rechtspersoonInOprichting);
        persoon.type = "RechtspersoonInOprichting";

        persoon.doelrechtsvorm = rechtspersoonInOprichting.getDoelRechtsvorm().getCode();

        return persoon;
    }

    public static Persoon from(EenmanszaakMetMeerdereEigenarenType eenmanszaakMetMeerdereEigenaren) {
        Persoon persoon = Persoon.from((NietNatuurlijkPersoonType) eenmanszaakMetMeerdereEigenaren);
        persoon.type = "EenmanszaakMetMeerdereEigenaren";
        return persoon;
    }

    public static Persoon from(BuitenlandseVennootschapType buitenlandseVennootschap) {
        Persoon persoon = Persoon.from((NietNatuurlijkPersoonType) buitenlandseVennootschap);
        persoon.type = "BuitenlandseVennootschap";
        // TODO: geplaatstkapitaal
        persoon.landvanoprichting = buitenlandseVennootschap.getLandVanOprichting().getCode();
        persoon.landvanvestiging = buitenlandseVennootschap.getLandVanVestiging().getCode();
        return persoon;
    }

    public String type;
    public String datumaanvang;
    public String datumeinde;
    public String registratietijdstip;
    public String persoonrechtsvorm;
    public String uitgebreiderechtsvorm;
    public String volledigenaam;
    public String naam;
    public String naampersoon_nummer;
    public String naampersoon_toegangscode;
    public String achternaam;
    public String bsn;
    public String geboortedatum;
    public String geboorteland;
    public String geboorteplaats;
    public String geslachtsaanduiding;
    public String overlijdensdatum;
    public String tin;
    public String voornamen;
    public String voorvoegsel;
    public String datumuitschrijving;
    public String rsin;
    public String statutendatumaanvang;
    public String statutendatumakte;
    public String rechtsvorm;
    public String geplaatstkapitaal;
    public String aanvangstatutairezetel;
    public String activiteitengestaaktper;
    public String bedragkostenoprichting;
    public char beleggingsmijmetveranderlijkkapitaal;
    public String datumakteoprichting;
    public String datumaktestatutenwijziging;
    public String datumeersteinschrijvinghandelsregister;
    public String datumoprichting;
    public String gestortkapitaal;
    public String ingangstatuten;
    public String maatschappelijkkapitaal;
    public String nieuwgemelderechtsvorm;
    public String overigeprivaatrechtelijkrechtsvorm;
    public String publiekrechtelijkerechtsvorm;
    public String rechtsbevoegdheidvereniging;
    public String statutairezetel;
    public String stelselinrichting;
    public String structuur;
    public String aantalcommanditairevennoten;
    public String doelrechtsvorm;
    public String landvanoprichting;
    public String landvanvestiging;
}
