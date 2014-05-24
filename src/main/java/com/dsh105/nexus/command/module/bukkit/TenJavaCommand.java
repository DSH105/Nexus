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

package com.dsh105.nexus.command.module.bukkit;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.bukkit.TenJavaDataLookupException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.ocpsoft.prettytime.PrettyTime;
import org.pircbotx.Colors;

import java.text.DecimalFormat;
import java.util.Date;

@Command(command = "tenjava",
        aliases = {"tj"},
        needsChannel = false,
        help = "Get tenjava points",
        extendedHelp = {
                "{b}{p}{c}{/b} - View tenjava donated points"
        })
public class TenJavaCommand extends CommandModule {

    public static final String TEN_JAVA_URL = "http://tenjava.com/assets/data.json";
    private final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    public boolean onCommand(CommandPerformEvent event) {

        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.get(TEN_JAVA_URL)
                    .header("accept", "application/json")
                    .asJson();
            int points = jsonResponse.getBody().getObject().getInt("points");
            long time = jsonResponse.getBody().getObject().getLong("last_update");
            PrettyTime pt = new PrettyTime();
            time = time * 1000;

            event.respond("Current points donated: " + Colors.BOLD + points + " ($" + df.format(points * 0.05) + " USD)" + Colors.NORMAL + ". Last updated: " + Colors.BOLD + pt.format(new Date(time)) + Colors.BOLD + " http://tenjava.com/points");


        } catch (Exception e) {
            throw new TenJavaDataLookupException("An error occurred in the lookup process", e);
        }

        return true;
    }
}

