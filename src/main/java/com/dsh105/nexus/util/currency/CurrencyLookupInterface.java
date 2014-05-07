package com.dsh105.nexus.util.currency;

import com.dsh105.nexus.exception.currency.CurrencyException;

public interface CurrencyLookupInterface {
    public double getExchangeRate(String from, String to) throws CurrencyException;
}
