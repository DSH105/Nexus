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

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.GitHubRepoNotFoundException;
import com.dsh105.nexus.hook.github.GitHubEvent;
import com.dsh105.nexus.hook.github.GitHubRepo;
import com.dsh105.nexus.hook.github.GitHubUser;
import com.dsh105.nexus.util.StringUtil;
import org.pircbotx.Colors;

import java.util.ArrayList;
import java.util.regex.Pattern;

@Command(command = "repo", subCommands = {"<repo_name>", "<author> <repo_name>"}, help = "View GitHub repository information.")
public class RepoCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        int startCheck = 2;
        boolean subMatchesSecond = event.getArgs().length >= startCheck && Pattern.compile("(set)").matcher(event.getArgs()[startCheck - 1]).matches();
        boolean subMatchesThird = event.getArgs().length >= (startCheck + 1) && Pattern.compile("(set)").matcher(event.getArgs()[startCheck]).matches();

        String repoName = subMatchesSecond ? event.getArgs()[0] : ((subMatchesThird || event.getArgs().length == 2) ? event.getArgs()[1] : event.getArgs()[0]);
        String owner = subMatchesSecond ? event.getSender().getNick() : ((subMatchesThird || event.getArgs().length == 2) ? event.getArgs()[0] : event.getSender().getNick());
        if (repoName.contains("/") && repoName.split("/").length == 2) {
            String[] parts = repoName.split("/");
            owner = parts[0];
            repoName = parts[1];
        } else if (owner == null) {
            owner = event.getSender().getNick();
        }

        GitHubRepo repo;
        try {
            repo = Nexus.getInstance().getGithub().getRepo(owner + "/" + repoName);
        } catch (GitHubRepoNotFoundException e) {
            try {
                String owner2 = repoName;
                repoName = owner;
                owner = owner2;
                repo = Nexus.getInstance().getGithub().getRepo(owner + "/" + repoName);
            } catch (GitHubRepoNotFoundException e2) {
                event.respond(Colors.RED + "The GitHub repository " + Colors.BOLD + " " + repoName + "/" + owner + Colors.NORMAL + Colors.RED + " could not be found! :(");
                return true;
            }
        }

        repoName = repo.getName();
        owner = repo.getRepoOwner().getLogin();

        int startIndex = subMatchesSecond ? 1 : (subMatchesThird ? 2 : -1);
        if (startIndex > 0) {
            if (event.getArgs().length <= startIndex) {
                // TODO: Usage
                event.respond("Do you even GitHub? (TODO)");
                return true;
            }
            if (event.getArgs().length >= startIndex + 1) {
                if (event.getArgs()[startIndex + 1].equalsIgnoreCase("irc")) {
                    ArrayList<String> invalidEvents = new ArrayList<>();
                    ArrayList<GitHubEvent> events = new ArrayList<>();
                    for (int i = startIndex + 2; i < event.getArgs().length; i++) {
                        GitHubEvent e = GitHubEvent.getByJsonName(event.getArgs()[i]);
                        if (e != null) {
                            events.add(e);
                        } else {
                            invalidEvents.add(event.getArgs()[i]);
                        }
                    }
                    if (!invalidEvents.isEmpty()) {
                        String validEvents = "";
                        for (GitHubEvent e : GitHubEvent.values()) {
                            validEvents += (validEvents.isEmpty()) ? e.getJsonName() : ", " + e.getJsonName();
                        }
                        event.respond(Colors.RED + "You entered " + Colors.BOLD + "invalid" + Colors.NORMAL + Colors.RED + " event names: " + StringUtil.combineSplit(0, invalidEvents.toArray(new String[invalidEvents.size()]), ", ") + ". " + Colors.BOLD + "Valid" + Colors.NORMAL + Colors.RED + " event names are: " + validEvents);
                        if (events.isEmpty()) {
                            return true;
                        }
                    }

                    if (!events.isEmpty()) {
                        Nexus.getInstance().getGithub().setIrcNotifications(repo, events.toArray(new GitHubEvent[events.size()]));
                        String eventsStr = "";
                        for (GitHubEvent e : events) {
                            eventsStr += (eventsStr.isEmpty()) ? e.getJsonName() : ", " + e.getJsonName();
                        }
                        event.respond("IRC Notifications for GitHub repository (" + Colors.BOLD + owner + "/" + repoName + Colors.NORMAL + ") set to: " + eventsStr);
                        return true;
                    }
                } else {
                    event.error("Invalid repository setting entered: {0}.", event.getArgs()[startIndex + 1]);
                    return true;
                }
            }
        } else {
            if (repo != null) {
                ArrayList<String> collaborators = new ArrayList<>();
                for (GitHubUser user : repo.getCollaborators()) {
                    if (!user.getLogin().equals(repo.getRepoOwner().getLogin())) {
                        collaborators.add(user.getLogin());
                    }
                }
                event.respond(Colors.BOLD + Colors.BLUE + repo.getName() + Colors.NORMAL + " - " + repo.getLanguage() + Colors.NORMAL + " (" + repo.getUrl() + ")");
                event.respond(Colors.UNDERLINE + "Owner" + Colors.NORMAL + ": " + repo.getRepoOwner());
                event.respond(Colors.UNDERLINE + "Collaborators" + Colors.NORMAL + ": " + StringUtil.combineSplit(0, collaborators.toArray(new String[collaborators.size()]), ", "));
                event.respond(Colors.UNDERLINE + "Homepage" + Colors.NORMAL + ": " + (repo.getHomepage() == null ? "Not configured!" : repo.getHomepage()));
                event.respond(Colors.UNDERLINE + "Description" + Colors.NORMAL + ": " + (repo.getDescription() == null ? "Not configured!" : repo.getDescription()));
                event.respond(Colors.UNDERLINE + "Forks" + Colors.NORMAL + ": " + repo.getForksCount());
                event.respond(Colors.UNDERLINE + "Open Issues" + Colors.NORMAL + ": " + repo.getOpenIssuesCount());
                event.respond(Colors.UNDERLINE + "Created at" + Colors.NORMAL + ": " + repo.getDateCreated().split("T")[0] + "");
                event.respond(Colors.UNDERLINE + "Last pushed to at" + Colors.NORMAL + ": " + repo.getDateLastPushedTo().split("T")[0] + "");
                event.respond(Colors.UNDERLINE + "Last updated at" + Colors.NORMAL + ": " + repo.getDateLastUpdated().split("T")[0] + "");
            }
            return true;
        }
        return false;
    }
}