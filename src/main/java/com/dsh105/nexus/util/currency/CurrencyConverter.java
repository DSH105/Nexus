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

package com.dsh105.nexus.util.currency;

import com.dsh105.nexus.exception.currency.CurrencyException;

import java.math.BigDecimal;

public class CurrencyConverter {

    private CurrencyLookupInterface lookup;

    public CurrencyConverter(CurrencyLookupInterface lookupInterface) {
        this.lookup = lookupInterface;
    }

    public static double roundToPennies(double amount) {
        BigDecimal bd = new BigDecimal(amount);
        BigDecimal rounded = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        return rounded.doubleValue();
    }

    public double convertAmount(double amount, String from, String to) throws CurrencyException {
        return this.lookup.getExchangeRate(from, to) * amount;
    }

}
