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

public class StringUtilTest {

    @Test
    public void testRemovePing() {
        Assert.assertNotEquals("Nicks are appropriately modified to prevent pings.", "lol768", StringUtil.removePing("lol768"));
    }

    @Test
    public void testValidIntegerConversion() {
        Assert.assertEquals("Valid numeric string deemed integer.", 6, StringUtil.toInteger("6"));
        Assert.assertEquals("Mixed alphanumerical string converted to integer.", 41336, StringUtil.toInteger("4^%|1g33|6^$#"));
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidIntegerConversion() {
        int number = StringUtil.toInteger("a");
    }

    @Test
    public void testDoubleConversion() {
        Assert.assertEquals(6.5, StringUtil.toDouble("6.5"), 0);
        Assert.assertEquals(4.354, StringUtil.toDouble("aa4.354f|$..|"), 0);
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidDoubleConversion() {
        int number = StringUtil.toInteger("a");
    }

    @Test
    public void testStringCapitalisation() {
        Assert.assertEquals("Words are appropriately capitalised.", "Nexus Bot", StringUtil.capitalise("nexus bot"));
    }

    @Test
    public void testCombineSplit() {
        final String[] stringArray = {"The", "quick", "brown", "fox"};
        Assert.assertEquals("String is appropriately imploded.", "The quick brown fox", StringUtil.combineSplit(0, stringArray, " "));
    }

    @Test
    public void testCombineSplitWithIndex() {
        final String[] stringArray = {"the", "Slow", "red", "cat"};
        Assert.assertEquals("String is appropriately imploded.", "Slow red cat", StringUtil.combineSplit(1, stringArray, " "));
    }

    @Test
    public void testSplitArgs() {
        Assert.assertArrayEquals(new String[]{"arg", "1", "arg", "2"}, StringUtil.splitArgs(0, new String[]{"arg 1", "arg 2"}, " "));
    }

    @Test
    public void testValidSentenceList() {
        final String[] animals = {"Cat", "fox", "zebra", "dog"};
        Assert.assertEquals("Cat, fox, zebra and dog", StringUtil.buildSentenceList(animals));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptySentenceList() {
        final String[] animals = {};
        StringUtil.buildSentenceList(animals);
    }

}
