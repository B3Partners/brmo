/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package nl.b3p.brmo.bgt.loader.cli;

import picocli.CommandLine.Option;

public class DatabaseOptions {
    @Option(names={"-c","--connection"}, paramLabel = "<string>")
    String connectionString = "jdbc:postgresql:bgt?sslmode=disable&reWriteBatchedInserts=true";

    @Option(names={"-u","--user"})
    String user = "bgt";

    @Option(names={"-p","--password"}, interactive = true, arity = "0..1")
    String password = "bgt";

    @Option(names="--batch-size", paramLabel="number", hidden = true)
    Integer batchSize;

    @Option(names={"--no-pg-copy"}, negatable = true, hidden = true)
    boolean usePgCopy = true;

    public String getConnectionString() {
        return connectionString;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public boolean isUsePgCopy() {
        return usePgCopy;
    }
}
