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

package com.dsh105.nexus.command.module.finance;


import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.currency.DogeCoinException;
import com.dsh105.nexus.util.StringUtil;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.pircbotx.Colors;

@Command(command = "doge",
        needsChannel = false,
        help = "Dogecoin currency converter",
        extendedHelp = {
                "{b}{p}{c} <value>{/b} - Converts the entered value to either dogecoin or usd."
        })
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
                double amtInUsdtoDoge = StringUtil.toDouble(event.getArgs()[0]);
                amtInUsdtoDoge = amtInUsdtoDoge / amtFromDogeApi;

                double amtInDogetoUSD = StringUtil.toDouble(event.getArgs()[0]);
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
