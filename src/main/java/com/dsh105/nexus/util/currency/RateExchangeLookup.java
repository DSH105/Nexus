package com.dsh105.nexus.util.currency;

import com.dsh105.nexus.exception.currency.CurrencyException;
import com.dsh105.nexus.exception.currency.CurrencyLookupException;
import com.dsh105.nexus.exception.currency.InvalidCurrencyException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class RateExchangeLookup implements CurrencyLookupInterface {

    public static final String API_URL = "http://rate-exchange.appspot.com/currency";

    @Override
    public double getExchangeRate(String from, String to) throws CurrencyException {
        try {
            HttpResponse<JsonNode> resp = Unirest.get(API_URL).field("to", to).field("from", from).asJson();
            if (resp.getBody().getObject().has("err")) {
                throw new InvalidCurrencyException();
            } else {
                return resp.getBody().getObject().getDouble("rate");
            }
        } catch (UnirestException e) {
            throw new CurrencyLookupException();
        }
    }
}
