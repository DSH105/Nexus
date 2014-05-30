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

package com.dsh105.nexus.command.module.github;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.command.CommandGroup;
import com.dsh105.nexus.exception.general.GenericUrlConnectionException;
import com.dsh105.nexus.hook.github.GitHubStatus;
import com.dsh105.nexus.util.JsonUtil;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.pircbotx.Colors;

import java.util.ArrayList;
import java.util.List;

@Command(command = "status",
        aliases = {"githubstatus", "gstatus", "ghstatus"},
        needsChannel = false,
        groups = CommandGroup.GITHUB,
        help = "View the Status of GitHub's services.",
        extendedHelp = {
                "{b}{p}{c}{/b} - View the Status of GitHub's services."
        })
public class GitHubStatusCommand extends CommandModule {

    private static String URL = "https://status.github.com/api/messages.json";
    private static String MESSAGE = "GitHub Status: %s" + Colors.NORMAL + Colors.BOLD + " (%s)";
    private List<GitHubStatus> statusMessages = new ArrayList<>();

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        try {
            load();
        } catch (UnirestException e) {
            throw new GenericUrlConnectionException("Error occurred while reading from " + URL, e);
        }

        if (statusMessages.isEmpty()) {
            event.respond("GitHub Status: {0}", "Nothing reported!");
            return true;
        }

        GitHubStatus status = statusMessages.get(0);
        event.respondWithPing(String.format(MESSAGE, getFormatting(status.getStatus()), status.getBody()));

        return true;
    }

    private void load() throws UnirestException {
        statusMessages.clear();

        for (GitHubStatus statusMessage : JsonUtil.read(Unirest.get(URL), GitHubStatus[].class)) {
            statusMessages.add(statusMessage);
        }
    }

    private String getFormatting(String status) {
        if (status.equalsIgnoreCase("good")) {
            return Colors.GREEN + Colors.BOLD + "All systems operational!";
        } else if (status.equalsIgnoreCase("minor")) {
            return Colors.OLIVE + Colors.BOLD + "Minor system outage";
        } else if (status.equalsIgnoreCase("major")) {
            return Colors.RED + Colors.BOLD + "Major system outage";
        } else {
            return status;
        }
    }
}
