/*
 * This file is part of Nexus.
 *
 * Nexus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nexus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nexus.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    public void testParseMonths() {
        Assert.assertEquals("Months time string is correctly parsed into milliseconds.", (long) (2000 * 60 * 60 * 24 * 30.42), TimeUtil.parse("2mo"));
    }

    @Test
    public void testParseYears() {
        Assert.assertEquals("Years time string is correctly parsed into milliseconds.", (long) (2000 * 60 * 60 * 24 * 364.25), TimeUtil.parse("2y"));
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
