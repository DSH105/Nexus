package com.dsh105.nexus.currency;

import com.dsh105.nexus.exception.currency.CurrencyException;
import com.dsh105.nexus.mock.MockLookup;
import com.dsh105.nexus.util.currency.CurrencyConverter;
import org.junit.Assert;
import org.junit.Test;

public class CurrencyTest {

    @Test
    public void testConverterSimple() {
        CurrencyConverter converter = new CurrencyConverter(new MockLookup(1));
        try {
            Assert.assertEquals("Converter uses Lookup system's exchange rate correctly.", converter.convertAmount(2d, "XYZ", "XYZ"), 2d, 0d);
        } catch (CurrencyException e) {
            Assert.fail("Currency exception.");
        }
    }

    @Test
    public void testConverterRounding() throws CurrencyException {
        Assert.assertEquals("Rounding works correctly.", 2.51d, CurrencyConverter.roundToPennies(2.51235d), 0.005d);
    }

}
