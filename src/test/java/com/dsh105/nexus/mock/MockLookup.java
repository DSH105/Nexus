package com.dsh105.nexus.mock;

import com.dsh105.nexus.exception.currency.CurrencyException;
import com.dsh105.nexus.util.currency.CurrencyLookupInterface;

public class MockLookup implements CurrencyLookupInterface {

    private int multipicationRate;

    public MockLookup(int multipicationRate) {
        this.multipicationRate = multipicationRate;
    }

    @Override
    public double getExchangeRate(String from, String to) throws CurrencyException {
        return multipicationRate;
    }
}
