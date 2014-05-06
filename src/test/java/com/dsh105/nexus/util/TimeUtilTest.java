package com.dsh105.nexus.util;

import org.junit.Assert;
import org.junit.Test;

public class TimeUtilTest {

    @Test
    public void testParseSeconds() {
        Assert.assertEquals("Seconds time string is correctly parsed into milliseconds.", 2000, TimeUtil.parse("2s"));
    }

    @Test
    public void testParseMinutes() {
        Assert.assertEquals("Minutes time string is correctly parsed into milliseconds.", 2000 * 60, TimeUtil.parse("2m"));
    }

    @Test
    public void testParseHours() {
        Assert.assertEquals("Hours time string is correctly parsed into milliseconds.", 2000 * 60 * 60, TimeUtil.parse("2h"));
    }

    @Test
    public void testParseDays() {
        Assert.assertEquals("Days time string is correctly parsed into milliseconds.", 2000 * 60 * 60 * 24, TimeUtil.parse("2d"));
    }

    @Test
    public void testParseWeeks() {
        Assert.assertEquals("Weeks time string is correctly parsed into milliseconds.", 2000 * 60 * 60 * 24 * 7, TimeUtil.parse("2w"));
    }

    @Test
    public void testParseMultiple() {
        int result = 2000 * 60 * 60 * 24 * 7 + 2000 * 60 * 60 * 24 + 2000 * 60 * 60 + 2000 * 60 + 2000;
        Assert.assertEquals("Combination time string is correctly parsed into milliseconds.", result, TimeUtil.parse("2s2m2h2d2w"));
    }

    @Test
    public void testParseWhitespace() {
        int result = 2000 * 60 * 60 * 24 * 7 + 2000 * 60 * 60 * 24 + 2000 * 60 * 60 + 2000 * 60 + 2000;
        Assert.assertEquals("Whitespace ignored in time string.", result, TimeUtil.parse("2s 2m  2h 2d 2w"));
    }

    @Test
    public void testParseInvalid() {
        Assert.assertEquals("Invalid characters ignored in time string.", 0, TimeUtil.parse("f"));
    }

    @Test
    public void testParseDoubleDigits() {
        Assert.assertEquals("seconds time string with multiple digits is correctly parsed into milliseconds.", 1000 * 15, TimeUtil.parse("15s"));
    }
}
