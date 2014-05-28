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

package com.dsh105.nexus.command.module.minecraft;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.general.GenericUrlConnectionException;
import com.dsh105.nexus.util.JsonUtil;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.pircbotx.Colors;


@Command(command = "mcuser",
        aliases = {"minecraftuser"},
        needsChannel = false,
        help = "View information about a specific Minecraft username.",
        extendedHelp = {
                "{b}{p}{c}{/b} <usernane>- View information about the Minecraft username."
        })
public class MinecraftUserCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length == 1) {
            String username = event.getArgs()[0];
            if (!username.matches("^([A-Za-z]|[0-9]|_)+$")) {
                event.respondWithPing(Colors.RED + "Minecraft usernames can only contain letters, numbers and underscores!");
                return true;
            }

            String uuid = "N/A";
            boolean isValid;
            boolean hasPaid;
            try {
                UserProfile[] profiles = getProfiles(username);
                if (profiles.length > 0) {
                    uuid = profiles[0].id;
                }
                isValid = getPageContents("https://account.minecraft.net/buy/frame/checkName/" + username).equalsIgnoreCase("TAKEN");
                hasPaid = getPageContents("https://minecraft.net/haspaid.jsp?user=" + username).equalsIgnoreCase("TRUE");
            } catch (UnirestException e) {
                throw new GenericUrlConnectionException("Failed to fetch data on Minecraft username (" + username + ").", e);
            }

            String valid = isValid ? Colors.GREEN + "valid" + Colors.NORMAL + " (exists)" : Colors.RED + "not valid" + Colors.NORMAL + " (does not exist)";
            String paid = hasPaid ? "a " + Colors.GREEN + "paid" + Colors.NORMAL : Colors.RED + "not a paid" + Colors.NORMAL;
            event.respond("Minecraft account {0} ({1}) is " + valid + " and is " + paid + " account", username, uuid);
            return true;
        }
        return false;
    }

    private String getPageContents(String urlString) throws UnirestException {
        return Unirest.get(urlString).asString().getBody();
    }

    private UserProfile[] getProfiles(String username) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.post("https://api.mojang.com/profiles/page/1").header("content-type", "application/json").body("[{\"name\":\"" + username + "\", \"agent\":\"Minecraft\"}]").asJson();
        return JsonUtil.read(response.getRawBody(), "profiles", UserProfile[].class);
    }

    private class UserProfile {
        private String id;
        private String name;
    }
}
