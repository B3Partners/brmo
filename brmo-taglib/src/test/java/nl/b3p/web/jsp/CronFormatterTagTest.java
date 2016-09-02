/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.web.jsp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testcase voor {@link CronFormatterTag}.
 *
 * @author mprins
 */
public class CronFormatterTagTest {

    private CronFormatterTag tag;

    @Before
    public void setUp() {
        tag = new CronFormatterTag();

    }

    @Test
    public void testBuildOutputLocale() {
        tag.setLocale("nl");
        tag.setCronExpression("0 0 12 1/1 * ? *");
        assertEquals("Tag should output 'Om 12:00, elke dag, elk jaar'", "Om 12:00, elke dag, elk jaar", tag.buildOutput());
    }
    @Test
    public void testBuildOutputNoLocale() {
        tag.setCronExpression("0 0 12 1/1 * ? *");
        assertEquals("Tag should output 'Om 12:00, elke dag, elk jaar'", "Om 12:00, elke dag, elk jaar", tag.buildOutput());
    }
}
