/*
 * Copyright (C) 2022 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.nhr.loader;

import picocli.CommandLine;

public class NHRDatabaseOptions {
    @CommandLine.Option(names={"-c", "--connection"}, paramLabel = "<string>")
    private String connectionString = "jdbc:postgresql:brmo_staging?sslmode=disable";

    @CommandLine.Option(names={"-u", "--user"})
    private String user = "brmo";

    @CommandLine.Option(names={"-p", "--password"}, interactive = true, arity = "0..1")
    private String password = "brmo";

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
