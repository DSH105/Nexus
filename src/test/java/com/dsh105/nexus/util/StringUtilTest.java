package com.dsh105.nexus.util;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilTest {

    @Test
    public void testRemovePing() {
        Assert.assertNotEquals("Nicks are appropriately modified to prevent pings.", "lol768", StringUtil.removePing("lol768"));
    }

    @Test
    public void testIsInt() {
        Assert.assertTrue("Valid numeric string deemed integer.", StringUtil.isInt("1"));
        Assert.assertFalse("Invalid numeric string deemed not an integer.", StringUtil.isInt("g"));
        Assert.assertFalse("Invalid decimal string deemed not an integer.", StringUtil.isInt("1.4"));
    }

    @Test
    public void testIsDouble() {
        Assert.assertTrue("Valid decimal string deemed double.", StringUtil.isDouble("1.4"));
        Assert.assertTrue("Valid numeric string deemed double.", StringUtil.isDouble("1"));
        Assert.assertFalse("Invalid numeric string deemed not a double.", StringUtil.isDouble("g"));
    }

    @Test
    public void testStringCapitalisation() {
        Assert.assertEquals("Words are appropriately capitalised.", "Nexus Bot", StringUtil.capitalise("nexus bot"));
    }

    @Test
    public void testSeparate() {
        // TODO: Not sure what this function does, sorry.
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
        Assert.assertArrayEquals(new String[] {"arg", "1", "arg", "2"}, StringUtil.splitArgs(0, new String[] {"arg 1", "arg 2"}, " "));
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
