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

package com.dsh105.nexus.command.module.general;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.general.GenericUrlConnectionException;
import com.dsh105.nexus.util.StringUtil;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

@Command(command = "rand",
        aliases = {"random", "randomnumber"},
        needsChannel = false,
        help = "Generate a random number from Random.org",
        extendedHelp = {
                "{b}{p}{c} <max>{/b} - Chooses a number from 1 to <max>.",
                "{b}{p}{c} <min> <max>{/b} - Chooses a number from <min> to <max>",
                "{b}{p}{c} dice{/b} - Rolls the dice! (Chooses a number from 1 to 6)",
                "{b}{p}{c} coin{/b} - Flip a coin"
        })
public class RandomNumberCommand extends CommandModule {

    public static final String API_URL = "https://api.random.org/json-rpc/1/invoke";

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        String[] args = event.getArgs();
        if (args.length == 0 || args.length > 2) {
            return false;
        }

        getNumber(0, 5);

        if (StringUtil.isInt(args[0])) {
            int min;
            int max;

            if (args.length == 2) {
                max = Math.max(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                min = Math.min(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            } else {
                max = Math.max(Integer.parseInt(args[0]), 1);
                min = Math.min(Integer.parseInt(args[0]), 1);
            }

            event.respondWithPing("Random number between {0} and {1}: {2}", String.valueOf(min), String.valueOf(max), String.valueOf(getNumber(min, max)));
        } else {
            if (args[0].equalsIgnoreCase("dice")) {
                event.respondWithPing("The dice rolled a {0}!", String.valueOf(getNumber(1, 6)));
            } else if (args[0].equalsIgnoreCase("coin")) {
                boolean heads = (getNumber(1, 2) == 1);
                event.respondWithPing("The coin landed on {0}!", (heads ? "heads" : "tails"));
            } else {
                return false;
            }
        }

        return true;
    }

    public int getNumber(int min, int max) {
        max = Math.max(max, min);
        min = Math.min(min, max);
        try {
            HttpResponse<JsonNode> response = Unirest.post(API_URL)
                    .header("content-type", "application/json")
                    .body("{\"jsonrpc\":\"2.0\",\"method\":\"generateIntegers\",\"params\":{\"apiKey\":\"" + Nexus.getInstance().getConfig().getRandomOrgApiKey() + "\",\"n\":1,\"min\":" + min + ",\"max\":" + max + ",\"replacement\":true,\"base\":10},\"id\":19021}").asJson();
            return (int) response.getBody().getObject().getJSONObject("result").getJSONObject("random").getJSONArray("data").get(0);
        } catch (UnirestException e) {
            e.printStackTrace();
            throw new GenericUrlConnectionException("Failed to generate random number! ", e);
        }
    }
}