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

package com.dsh105.nexus.command.module.information;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.general.TimeDataLookupException;
import com.dsh105.nexus.util.StringUtil;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pircbotx.Colors;

@Command(command = "time",
        needsChannel = false,
        help = "Get a location's time",
        extendedHelp = {
                "{b}{p}{c} <location>{/b} - Allows you to get the time of a location"
        })
public class TimeCommand extends CommandModule {

    public static final String GOOGLE_COORDS_URL = "http://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=";
    public static final String TIME_URL = "http://www.earthtools.org/timezone-1.1/";

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length > 0) {
            String args = StringUtil.combineSplit(0, event.getArgs(), " ");
            try {
                HttpResponse<JsonNode> jsonResponse = Unirest.get(GOOGLE_COORDS_URL + args)
                        .header("accept", "application/json")
                        .asJson();
                JSONArray response = jsonResponse.getBody().getObject().getJSONArray("results");
                if (!jsonResponse.getBody().getObject().getString("status").equalsIgnoreCase("OK")) {
                    event.errorWithPing("Invalid request");
                } else {
                    double lat = response.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    double lng = response.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    String loc = response.getJSONObject(0).getString("formatted_address");
                    Document doc = Jsoup.connect(TIME_URL + lat + "/" + lng).get();
                    Element timeEl = doc.select("localtime").first();
                    String time = timeEl.text();
                    event.respond("Time in " + Colors.BOLD + loc + ": " + time);
                    return true;
                }
            } catch (Exception e) {
                throw new TimeDataLookupException("An error occurred in the lookup process", e);
            }
        } else {
            return false;
        }
        return true;
    }
}

