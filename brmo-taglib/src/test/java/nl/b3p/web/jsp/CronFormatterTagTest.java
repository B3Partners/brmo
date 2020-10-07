/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.web.jsp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals("Om 12:00, elke dag, elk jaar", tag.buildOutput(), "Tag should output 'Om 12:00, elke dag, elk jaar'");
    }
    @Test
    public void testBuildOutputNoLocale() {
        tag.setCronExpression("0 0 12 1/1 * ? *");
        assertEquals("Om 12:00, elke dag, elk jaar", tag.buildOutput(), "Tag should output 'Om 12:00, elke dag, elk jaar'");
    }
}
