package com.dsh105.nexus.command;


import com.dsh105.nexus.command.module.general.tempCommand;
import org.junit.Assert;
import org.junit.Test;


public class TestTemperatureCommand {
    @Test
    public void testFarenheitToCelsius() {
        Assert.assertEquals(69d, tempCommand.fToC(156.2), 0.01d);
    }

    @Test
    public void testCelsiusToFarenheit() {
        Assert.assertEquals(420d, tempCommand.cTof(215.556), 0.01d);
    }
}
