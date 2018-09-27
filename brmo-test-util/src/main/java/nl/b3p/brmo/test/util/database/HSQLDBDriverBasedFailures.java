package nl.b3p.brmo.test.util.database;

/**
 * Een marker interface om integratie tests die mislukken vanwege manco's in de
 * HSQLDB driver aan te geven. Zie {@link JTDSDriverBasedFailures} voor
 * gebruiksinstructie.
 *
 * @author mprins
 * @see JTDSDriverBasedFailures
 * @note marker interface
 * @see org.junit.experimental.categories.Category
 */
public interface HSQLDBDriverBasedFailures {
}
