package nl.b3p.brmo.imgeo.cli;

import picocli.CommandLine.Option;

public class DatabaseOptions {
    @Option(names={"-c","--connection"}, paramLabel = "<string>")
    String connectionString = "jdbc:postgresql:bgt?sslmode=disable&reWriteBatchedInserts=true";

    @Option(names={"-u","--user"})
    String user = "bgt";

    @Option(names={"-p","--password"}, interactive = true, arity = "0..1")
    String password = "bgt";

    @Option(names="--batch-size", paramLabel="number")
    Integer batchSize;
}
