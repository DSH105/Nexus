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

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.general.GenericUrlConnectionException;
import com.dsh105.nexus.util.StringUtil;
import com.dsh105.nexus.util.shorten.URLShortener;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

@Command(command = "githubkey", aliases = {"ghk", "ghkey"}, needsChannel = false, help = "Authenticate with GitHub through Nexus to allow the use of various GitHub commands requiring an API key.",
        extendedHelp = {"{b}{p}{c}{/b} - Provides instructions on how to set this up."})
public class GitHubKeyCommand extends CommandModule {

    private static String AUTHORISE = "https://github.com/login/oauth/authorize?client_id={client_id}&scope={scope}&state={state}";
    private static String ACCESS_TOKEN = "https://github.com/login/oauth/access_token?client_id={client_id}&client_secret={client_secret}&code={code}";

    @Override
    public boolean onCommand(final CommandPerformEvent event) {
        if (event.getArgs().length == 0) {
            event.respond("Please follow the following instructions:", true);
            event.respond("- Visit " + URLShortener.shorten(AUTHORISE
                    .replace("{client_id}", Nexus.getInstance().getGitHubConfig().getGitHubOauthAppClientId())
                    .replace("{scope}", Nexus.getInstance().getGitHubConfig().getGitHubOauthAppScope())
                    .replace("{state}", Nexus.getInstance().getGitHubConfig().getGitHubOauthAppState())), true);
            event.respond("- Allow Nexus access.", true);
            event.respond("- Copy the URL you are redirected to (the code information in this is important, so don't change anything!).", true);
            event.respond("- Perform {0}, where <code> is the URL you copied above.", true, Nexus.getInstance().getConfig().getCommandPrefix() + this.getCommand() + " <code>");
            if (!event.isInPrivateMessage()) {
                event.respondWithPing("Please check your private messages for instructions on how to configure your GitHub API key.");
            }
            return true;
        } else if (event.getArgs().length == 1) {
            // request confirmed - check if valid
            String codeUrl = event.getArgs()[0];
            try {
                HashMap<String, String> params = getParams(codeUrl);
                String code = params.get("code");
                String state = params.get("state");
                if (code != null && state != null && state.equals(Nexus.getInstance().getGitHubConfig().getGitHubOauthAppState())) {
                    HttpResponse<JsonNode> response = Unirest.get(ACCESS_TOKEN
                                .replace("{client_id}", Nexus.getInstance().getGitHubConfig().getGitHubOauthAppClientId())
                                .replace("{client_secret}", Nexus.getInstance().getGitHubConfig().getGitHubOauthAppClientSecret())
                                .replace("{code}", code))
                            .header("accept", "application/json")
                            .asJson();

                    try {
                        final String accessToken = response.getBody().getObject().getString("access_token");
                        Nexus.getInstance().sendMessage(Nexus.getInstance().getUser("NickServ"), "info " + event.getSender().getNick());
                        final String nick = event.getSender().getNick();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                System.out.println("Attempting to retrieve account name...");
                                String account = Nexus.getInstance().getNicksConfig().getAccountNameFor(nick);
                                if (account != null && !account.isEmpty()) {
                                    Nexus.getInstance().getGitHubConfig().set("github-key-" + account, accessToken);
                                    Nexus.getInstance().getGitHubConfig().save();
                                    event.respondWithPing("You may now use the Nexus commands requiring API key information (e.g. IRC notification settings).");
                                }
                            }
                        }, 5000);
                        return true;
                    } catch (JSONException e) {
                        event.errorWithPing("Access denied. Reason: \"{0}\"", response.getBody().getObject().getString("error_description"));
                        return true;
                    }
                } else {
                    event.errorWithPing("This code isn't right! Please make sure you copy the entire URL into the command.");
                    return true;
                }
            } catch (UnirestException e) {
                throw new GenericUrlConnectionException("Failed to connect.", e);
            } catch (MalformedURLException e) {
                event.errorWithPing("Invalid URL code provided. Please make sure you copy the entire URL into the command.");
                return true;
            }
        }
        return false;
    }

    private HashMap<String, String> getParams(String url) throws MalformedURLException {
        HashMap<String, String> map = new HashMap<>();
        String query = new URL(url).getQuery();
        String[] parts = query.split("&");
        for (int i = 0; i < parts.length; i++) {
            String[] param = parts[i].split("=");
            map.put(param[0], param[1]);
        }
        return map;
    }
}