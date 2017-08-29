/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.test.util.database;

/**
 * Een marker interface om integratie tests te markeren. Gebruik met
 * {@code @Category(PostgreSQLDriverBasedFailures.class)} als annotatie op een
 * test klasse en {@code import org.junit.experimental.categories.Category;}.
 * Vervolgens de maven surefire en/of failsafe plugin configureren met een
 * {@code <excludedGroups>} element, bijvoorbeeld:
 * <pre>
 * &lt;plugin&gt;
 *   &lt;groupId&gt;org.apache.maven.plugins&lt;/groupId&gt;
 *   &lt;artifactId&gt;maven-surefire-plugin&lt;/artifactId&gt;
 *   &lt;dependencies&gt;
 *     &lt;dependency&gt;
 *       &lt;groupId&gt;org.apache.maven.surefire&lt;/groupId&gt;
 *       &lt;artifactId&gt;surefire-junit47&lt;/artifactId&gt;
 *     &lt;/dependency&gt;
 *   &lt;/dependencies&gt;
 *   &lt;configuration&gt;
 *       &lt;includes&gt;
 *         &lt;include&gt;*.class
 *       &lt;/include&gt;
 *     &lt;/includes&gt;
 *     &lt;excludedGroups&gt;nl.b3p.brmo.test.util.database.PostgreSQLDriverBasedFailures&lt;/excludedGroups&gt;
 *   &lt;/configuration&gt;
 * &lt;/plugin&gt;
 * </pre>
 *
 * zie:
 * <a href="http://www.javaworld.com/article/2074569/core-java/unit-and-integration-tests-with-maven-and-junit-categories.html">Unit
 * and Integration Tests With Maven and JUnit Categories</a>.
 *
 * @author mprins
 * @note marker interface
 * @see org.junit.experimental.categories.Category
 */
public interface PostgreSQLDriverBasedFailures {

}
