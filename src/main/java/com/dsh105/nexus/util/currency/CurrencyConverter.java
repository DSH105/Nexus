package com.dsh105.nexus.util.currency;

import com.dsh105.nexus.exception.currency.CurrencyException;

import java.math.BigDecimal;

public class CurrencyConverter {

    private CurrencyLookupInterface lookup;

    public CurrencyConverter(CurrencyLookupInterface lookupInterface) {
        this.lookup = lookupInterface;
    }

    public double convertAmount(double amount, String from, String to) throws CurrencyException {
        return this.lookup.getExchangeRate(from, to) * amount;
    }

    public static double roundToPennies(double amount) {
        BigDecimal bd = new BigDecimal(amount);
        BigDecimal rounded = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        return rounded.doubleValue();
    }

}
