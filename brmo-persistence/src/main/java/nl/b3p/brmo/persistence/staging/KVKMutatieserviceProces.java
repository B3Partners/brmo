package nl.b3p.brmo.persistence.staging;

import javax.persistence.Entity;

/**
 * KVKMutatieServiceProces is an entity representing a process for accessing the KVK Mutatie
 * Service.
 *
 * @author mprins
 */
@Entity
public class KVKMutatieserviceProces extends AutomatischProces {
  public static final String APIURL = "apiurl";
  public static final String APIKEY = "apikey";
  public static final String ABONNEMENT_ID = "abonnementId";
  public static final String TOT = "tot";
  public static final String VANAF = "vanaf";
}
