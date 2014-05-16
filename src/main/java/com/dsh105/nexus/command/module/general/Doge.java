package com.dsh105.nexus.command.module.general;


import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.currency.DogeCoinException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.pircbotx.Colors;


import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParsePosition;

@Command(command = "doge", needsChannel = false, help = "Dogecoin currency converter",
        extendedHelp = {"{b}{p}{c}{/b} <value> - Converts the entered value to either dogecoin or usd."})
public class Doge extends CommandModule {
    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length == 0) {
            return false;
        } else {
            try {
                HttpResponse<JsonNode> jsonResponse = Unirest.get("https://www.dogeapi.com/wow/v2/")
                        .field("a", "get_info")
                        .header("accept", "application/json")
                        .asJson();
                double amtFromDogeApi = jsonResponse.getBody().getObject().getJSONObject("data").getJSONObject("info").getDouble("doge_usd");
                double amtInUSDtoDoge = Double.parseDouble(event.getArgs()[0]);
                amtInUSDtoDoge = amtInUSDtoDoge / amtFromDogeApi;

                double amtInDogetoUSD = Double.parseDouble(event.getArgs()[0]);
                amtInDogetoUSD = amtInDogetoUSD * amtFromDogeApi;

                amtInDogetoUSD = (double) Math.round(amtInDogetoUSD * 100) / 100;
                amtInUSDtoDoge = (double) Math.round(amtInUSDtoDoge * 100) / 100;

                event.respond(Colors.BOLD + "USD → Doge: " + Colors.NORMAL + Colors.UNDERLINE + amtInUSDtoDoge);
                event.respond(Colors.BOLD + "Doge → USD: " + Colors.NORMAL + Colors.UNDERLINE + amtInDogetoUSD);


            } catch (UnirestException e) {
                throw new DogeCoinException("An error occurred in the conversion process", e);
            }

            return true;
        }
    }
}
