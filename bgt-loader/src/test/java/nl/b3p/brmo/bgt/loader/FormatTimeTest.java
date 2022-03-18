/*
 * Copyright (C) 2022 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bgt.loader;

import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FormatTimeTest {
    @Test
    void seconds() {
        assertEquals("5s", Utils.formatDuration(Duration.ofSeconds(5)));
        assertEquals("12s", Utils.formatDuration(Duration.ofSeconds(12)));
    }

    @Test
    void minutes() {
        assertEquals("5m  0s", Utils.formatDuration(Duration.ofMinutes(5)));
        assertEquals("43m  0s", Utils.formatDuration(Duration.ofMinutes(43)));
        assertEquals("43m 12s", Utils.formatDuration(Duration.ofMinutes(43).plusSeconds(12)));
    }

    @Test
    void hours() {
        assertEquals("5h  0m  0s", Utils.formatDuration(Duration.ofHours(5)));
        assertEquals("5h 32m  0s", Utils.formatDuration(Duration.ofHours(5).plusMinutes(32)));
        assertEquals("5h 32m  4s", Utils.formatDuration(Duration.ofHours(5).plusMinutes(32).plusSeconds(4)));
    }

    @Test
    void days() {
        assertEquals("5d 0s", Utils.formatDuration(Duration.ofDays(5)));
        assertEquals("5d 12s", Utils.formatDuration(Duration.ofDays(5).plusSeconds(12)));
        assertEquals("5d 2m 12s", Utils.formatDuration(Duration.ofDays(5).plusMinutes(2).plusSeconds(12)));
        assertEquals("5d 3h  2m 12s", Utils.formatDuration(Duration.ofDays(5).plusHours(3).plusMinutes(2).plusSeconds(12)));
    }
}
