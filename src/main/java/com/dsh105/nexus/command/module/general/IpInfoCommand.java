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

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.util.shorten.URLShortener;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.pircbotx.Colors;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Command(command = "ipinfo",
        aliases = {"ip"},
        needsChannel = false,
        help = "Looks up IP information.",
        extendedHelp = {
                "{b}{p}{c}{/b} <ip> - Gives approximate location, country & ISP information."
        })
public class IpInfoCommand extends CommandModule {

    public static final String API_URL = "http://www.telize.com/geoip/";

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        String[] args = event.getArgs();
        if (args.length != 1 || (args[0].length() < 7)) {
            return false;
        }

        String url = null;

        try {
            url = API_URL + URLEncoder.encode(args[0], "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            HttpResponse<JsonNode> resp = Unirest.get(url).asJson();

            if (resp.getCode() != 200) {
                event.errorWithPing("Unknown error occurred while executing command! Try again later...");
                return false;
            }

            StringBuilder builder = new StringBuilder();
            final JSONObject object = resp.getBody().getObject();
            String maps = "No maps available.";

            if (object.has("latitude")) {
                maps = "https://maps.google.com/maps?q=" + object.get("latitude") + "," + object.get("longitude");
            }

            String info = "Info for " + args[0] + ": ";
            builder.append(Colors.BOLD + info);

            if (object.has("country")) {
                builder.append("Country: ").append(Colors.BOLD + object.getString("country") + Colors.NORMAL).append(" ");
            }

            if (object.has("isp")) {
                builder.append("ISP: ").append(Colors.BOLD + object.getString("isp") + Colors.NORMAL).append(" ");
            }

            event.respondWithPing(builder.toString().trim() + " (" + URLShortener.shorten(maps) + ")");

        } catch (UnirestException e) {
            return false;
        }
        return true;
    }
}
