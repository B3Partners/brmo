/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.bgt.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mockwebserver3.MockWebServer;
import nl.b3p.brmo.bgt.loader.cli.BGTLoaderMain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import picocli.CommandLine;

public class CommandLineTestBase extends DBTestBase {
  protected BGTLoaderMain main;
  protected CommandLine cmd;
  protected MockWebServer mockWebServer;

  @BeforeEach
  void init(TestInfo info) throws IOException {
    main = new BGTLoaderMain();
    main.configureLogging(true);
    cmd = new CommandLine(main);
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    System.out.println("\nExecute test: " + info.getDisplayName());
  }

  @AfterEach
  void closeWebServer() {
    mockWebServer.close();
  }

  protected String[] getArgs(String command, String... args) {
    List<String> allArgs = new ArrayList<>();
    allArgs.addAll(Arrays.asList(command.split(" ")));
    allArgs.add("--connection=" + dbOptions.getConnectionString());
    allArgs.add("--user=" + dbOptions.getUser());
    allArgs.add("--password=" + dbOptions.getPassword());
    allArgs.addAll(Arrays.asList(args));
    System.out.println("Command line: " + String.join(" ", allArgs));
    return allArgs.toArray(new String[] {});
  }

  void run(String command, String... args) {
    int exitCode = cmd.execute(getArgs(command, args));
    assertEquals(0, exitCode);
  }
}
