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

package com.dsh105.nexus.command.module;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.github.GitHubUserNotFoundException;
import com.dsh105.nexus.hook.github.GitHub;
import com.dsh105.nexus.hook.github.GitHubUser;
import com.dsh105.nexus.util.StringUtil;
import com.dsh105.nexus.util.shorten.URLShortener;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.pircbotx.Colors;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Command(command = "ghuser", aliases = {"ghu"}, needsChannel = false, help = "Retrieves information on a GitHub user",
        extendedHelp = {"{b}{p}{c}{/b} <user_name> - Provides information on a GitHub user."})
public class GhUserCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length != 1) {
            return false;
        }
        String userLogin = event.getArgs()[0];
        GitHubUser user;
        try {
            user = GitHub.getGitHub().getUser(userLogin, StringUtil.getIdent(event.getSender()));
        } catch (GitHubUserNotFoundException e) {
            event.respondWithPing("GitHub user ({0}) could not be found.", userLogin);
            return true;
        }

        String nameInfo = user.getLogin() + Colors.NORMAL + (user.getName().isEmpty() ? "" : " (" + Colors.BOLD + user.getName() + Colors.NORMAL + ")");
        event.respond(Colors.BOLD + "GitHub User" + Colors.NORMAL + " - " + Colors.BOLD + Colors.BLUE + nameInfo + " - (" + URLShortener.shorten(user.getUrl()) + ")");
        event.respond("Company: {0} | Followers: {1} | Following: {2}", user.getCompany().isEmpty() ? "None!" : user.getCompany(), String.valueOf(user.getFollowers()), String.valueOf(user.getFollowing()));
        event.respond("Repos: {0} | Gists: {1} | Avatar: {2}", String.valueOf(user.getPublicRepos()), String.valueOf(user.getPublicGists()), URLShortener.shorten(user.getAvatarUrl()));
        return true;
    }
}
