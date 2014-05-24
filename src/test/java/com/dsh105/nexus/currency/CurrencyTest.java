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
