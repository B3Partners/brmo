package nl.b3p.brmo.bgt.loader.cli;

import picocli.CommandLine.Option;

import static org.fusesource.jansi.internal.CLibrary.STDOUT_FILENO;
import static org.fusesource.jansi.internal.CLibrary.isatty;

public class CLIOptions {

    @Option(names="--progress", hidden = true)
    boolean progress;

    public boolean isConsoleProgressEnabled() {
        return progress || isatty(STDOUT_FILENO) == 1;
    }
}
