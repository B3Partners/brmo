package nl.b3p.brmo.bgt.loader.cli;

import picocli.CommandLine.Option;

import static org.fusesource.jansi.internal.CLibrary.STDOUT_FILENO;
import static org.fusesource.jansi.internal.CLibrary.isatty;

public class CLIOptions {

    @Option(names="--progress", hidden = true)
    boolean progress;

    public boolean isConsoleProgressEnabled() {
        try {
            return progress || isatty(STDOUT_FILENO) == 1;
        } catch(Exception e) {
            // Native libary may fail to load on aarch64
            return false;
        }
    }
}
