/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader;

import nl.b3p.brmo.bgt.loader.cli.BGTLoaderMain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandLineTestBase extends DBTestBase {
    BGTLoaderMain main;
    CommandLine cmd;

    @BeforeEach
    void init(TestInfo info) {
        main = new BGTLoaderMain();
        main.configureLogging();
        cmd = new CommandLine(main);
        System.out.println("\nExecute test: " + info.getDisplayName());
    }

    void run(String command, String... args) {
        List<String> allArgs = new ArrayList<>();
        allArgs.addAll(Arrays.asList(command.split(" ")));
        allArgs.add("--connection=" + connectionString);
        allArgs.add("--user=" + user);
        allArgs.add("--password=" + password);
        allArgs.addAll(Arrays.asList(args));
        System.out.println("Command line: " + String.join(" ", allArgs));
        int exitCode = cmd.execute(allArgs.toArray(new String[]{}));
        assertEquals(0, exitCode);
    }
}
