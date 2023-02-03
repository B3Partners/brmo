/*
 * Copyright (C) 2018 B3Partners B.V.
 */
package nl.b3p.web.jsp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testcase voor {@link TailTag}.
 *
 * @author mprins
 */
public class TailTagTest {
  private TailTag tag;

  @BeforeEach
  public void setUp() {
    tag = new TailTag();
  }

  @Test
  public void testdefaultId() {
    tag.setCount(10);
    tag.setFile("src/test/resources/log4j.xml");
    assertEquals(TailTag.DEFAULT_ID, tag.getId());
  }

  //    /**
  //     * @throws Exception if any
  //     */
  //    @Test
  //    public void testAbsolutePath() throws Exception {
  //        tag.setCount(10);
  //        tag.setFile("/home/mprins/workspace/brmo/brmo-taglib/src/test/resources/log4j.xml");
  //        tag.setId("S");
  //        tag.doStartTag();
  //        String body = tag.getBodyContent().getString();
  //        System.out.println(body);
  //    }

  //    @Test
  //    public void testRelativePath() {
  //        tag.setCount(10);
  //        tag.setFile("test/resources/log4j.xml");
  //        String body = tag.getBodyContent().getString();
  //        System.out.println(body);
  //    }
}
