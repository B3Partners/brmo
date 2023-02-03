/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.web.jsp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testcase voor {@link CronFormatterTag}.
 *
 * @author mprins
 */
public class CronFormatterTagTest {

  private CronFormatterTag tag;

  @BeforeEach
  public void setUp() {
    tag = new CronFormatterTag();
  }

  @Test
  public void testBuildOutputLocale() {
    tag.setLocale("nl");
    tag.setCronExpression("0 0 12 1/1 * ? *");
    assertEquals("om 12:00 elke dag", tag.buildOutput(), "Tag should output 'om 12:00 elke dag'");
  }

  @Test
  public void testBuildOutputUKLocale() {
    tag.setLocale("uk");
    tag.setCronExpression("0 0 12 1/1 * ? *");
    assertEquals("at 12:00 every day", tag.buildOutput(), "Tag should output 'at 12:00 every day'");
  }

  @Test
  public void testBuildOutputNoLocale() {
    tag.setCronExpression("0 0 12 1/1 * ? *");
    assertEquals("om 12:00 elke dag", tag.buildOutput(), "Tag should output 'om 12:00 elke dag'");
  }
}
