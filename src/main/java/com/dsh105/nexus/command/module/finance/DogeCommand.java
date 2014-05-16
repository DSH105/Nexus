package com.dsh105.nexus.command.module.finance;


import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.currency.DogeCoinException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.pircbotx.Colors;

@Command(command = "doge", needsChannel = false, help = "Dogecoin currency converter",
        extendedHelp = {"{b}{p}{c}{/b} <value> - Converts the entered value to either dogecoin or usd."})
public class DogeCommand extends CommandModule {

    public static final String DOGE_API_BASE_URL = "https://www.dogeapi.com/wow/v2/";

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length == 0) {
            return false;
        } else {
            try {
                HttpResponse<JsonNode> jsonResponse = Unirest.get(DOGE_API_BASE_URL)
                        .field("a", "get_info")
                        .header("accept", "application/json")
                        .asJson();
                double amtFromDogeApi = jsonResponse.getBody().getObject().getJSONObject("data").getJSONObject("info").getDouble("doge_usd");
                double amtInUsdtoDoge = Double.parseDouble(event.getArgs()[0]);
                amtInUsdtoDoge = amtInUsdtoDoge / amtFromDogeApi;

                double amtInDogetoUSD = Double.parseDouble(event.getArgs()[0]);
                amtInDogetoUSD = amtInDogetoUSD * amtFromDogeApi;

                amtInDogetoUSD = (double) Math.round(amtInDogetoUSD * 100) / 100;
                amtInUsdtoDoge = (double) Math.round(amtInUsdtoDoge * 100) / 100;

                event.respond(Colors.BOLD + "USD → Doge: " + Colors.NORMAL + Colors.UNDERLINE + "Ð" + amtInUsdtoDoge);
                event.respond(Colors.BOLD + "Doge → USD: " + Colors.NORMAL + Colors.UNDERLINE + "$" + amtInDogetoUSD);


            } catch (UnirestException e) {
                throw new DogeCoinException("An error occurred in the conversion process", e);
            }

            return true;
        }
    }
}
