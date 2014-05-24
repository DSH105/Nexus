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
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

@Command(command = "expand",
        aliases = {"expandlink"},
        needsChannel = false,
        help = "See where a shortened URL goes too",
        extendedHelp = {
                "{b}{p}{c}{/b} <url> - See where a shortened URL goes too."
        })
public class ExpandLinkCommand extends CommandModule {

    public static final String API_URL = "http://api.longurl.org/v2/expand?format=json&url=";

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        String[] args = event.getArgs();
        if (args.length != 1) {
            return false;
        }

        String url;

        try {
            url = args[0];
            HttpResponse<JsonNode> response = Unirest.post(API_URL + url).asJson();

            String longUrl = response.getBody().getObject().getString("long-url");
            if (longUrl.equalsIgnoreCase(url)) {
                event.respondWithPing("{0} has no redirects!", url);
            } else {
                event.respondWithPing("{0} → {1}.", url, longUrl);
            }
        } catch (Exception e) {
            event.errorWithPing("An error occurred! Is it a valid URL?");
        }
        return true;
    }
}
