/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.test.util.database;

/**
 * Een marker interface om integratie tests die mislukken vanwege manco's in de
 * Oracle driver aan te geven. Zie {@link JTDSDriverBasedFailures} voor
 * gebruiksinstructie.
 *
 * @author mprins
 * @see JTDSDriverBasedFailures
 * @note marker interface
 * @see org.junit.experimental.categories.Category
 */
public interface OracleDriverBasedFailures {

}
