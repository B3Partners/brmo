/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader.cli;

import picocli.CommandLine.*;

public class BAG2DatabaseOptions {
    @Option(names={"-c","--connection"}, paramLabel = "<string>")
    String connectionString = "jdbc:postgresql:bag?sslmode=disable&reWriteBatchedInserts=true";

    @Option(names={"-u","--user"})
    String user = "bag";

    @Option(names={"-p","--password"}, interactive = true, arity = "0..1")
    String password = "bag";

    @Option(names="--batch-size", paramLabel="number", hidden = true)
    Integer batchSize;

    @Option(names={"--no-pg-copy"}, negatable = true, hidden = true)
    boolean usePgCopy = true;

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

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isUsePgCopy() {
        return usePgCopy;
    }

    public void setUsePgCopy(boolean usePgCopy) {
        this.usePgCopy = usePgCopy;
    }
}
