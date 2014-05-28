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
import com.dsh105.nexus.hook.github.Octocat;
import com.dsh105.nexus.util.JsonUtil;
import com.dsh105.nexus.util.StringUtil;
import com.dsh105.nexus.util.shorten.URLShortener;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.pircbotx.Colors;

import java.util.HashMap;
import java.util.Random;

@Command(command = "octo",
        aliases = {"githuboctocat", "octocat"},
        needsChannel = false,
        groups = CommandGroup.GITHUB,
        help = "Give information on a random or specified Octocat.",
        extendedHelp = {
                "{b}{p}{c}{/b} - Display a random Octocat.",
                "{b}{p}{c} <id>{/b} - Display a specified Octocat."
        })
public class GitHubOctocatCommand extends CommandModule {

    private static String URL = "https://octodexapi.herokuapp.com/";
    private static String MESSAGE = "Octocat " + Colors.BOLD + "#{0}" + Colors.NORMAL + " - \"" + Colors.BOLD + Colors.BLUE + "{1}" + Colors.NORMAL + "\" - " + "(" + "{2}" + ")";
    // Integer = GitHub ID of Octocat
    private HashMap<Integer, Octocat> octocats = new HashMap<>();

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        try {
            load();
        } catch (UnirestException e) {
            throw new GenericUrlConnectionException("Error occurred while reading from " + URL, e);
        }

        int id = -1;

        if (event.getArgs().length == 0) {
            id = new Random().nextInt(octocats.size() + 1);
        } else if (event.getArgs().length == 1) {
            String octocatID = event.getArgs()[0];
            if (!StringUtil.isInt(octocatID)) {
                event.respondWithPing("{0} needs to be a number.", octocatID);
                return true;
            }

            id = Integer.parseInt(event.getArgs()[0]);
        }

        Octocat oc = octocats.get(id);
        if (oc == null) {
            event.respondWithPing("Invalid Octocat Specified!");
        } else if (id == -1) {
            event.respondWithPing("{0} is an invalid ID!", String.valueOf(id));
        } else {
            event.respondWithPing(MESSAGE, String.valueOf(oc.getNumber()), oc.getName(), URLShortener.shortenGit("https://octodex.github.com" + oc.getPage()));
        }
        return true;
    }

    private void load() throws UnirestException {
        octocats.clear();

        for (Octocat cat : JsonUtil.read(Unirest.get(URL), Octocat[].class)) {
            octocats.put(cat.getNumber(), cat);
        }
    }
}

