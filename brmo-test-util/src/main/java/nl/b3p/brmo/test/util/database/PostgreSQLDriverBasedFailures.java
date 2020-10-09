/*
 * Copyright (C) 2017 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * @deprecated sinds upgrade naar junit 5
 */
@Deprecated
public interface PostgreSQLDriverBasedFailures {

}
