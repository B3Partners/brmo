package nl.b3p.brmo.test.util.database;

/**
 * Een marker interface om integratie tests die mislukken vanwege manco's in de
 * HSQLDB driver aan te geven. Zie {@link PostgreSQLDriverBasedFailures} voor
 * gebruiksinstructie.
 *
 * @author mprins
 * @see PostgreSQLDriverBasedFailures
 * @note marker interface
 * @see org.junit.experimental.categories.Category
 * @deprecated sinds upgrade naar junit 5
 */
@Deprecated
public interface HSQLDBDriverBasedFailures {
}
