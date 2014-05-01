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
import com.dsh105.nexus.hook.github.GitHub;
import com.dsh105.nexus.hook.github.GitHubEvent;
import com.dsh105.nexus.hook.github.GitHubRepo;
import com.dsh105.nexus.hook.github.GitHubUser;
import com.dsh105.nexus.util.StringUtil;
import org.pircbotx.Colors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

@Command(command = "repo", needsChannel = false, help = "Management of GitHub repositories.",
        extendedHelp = {
                "The repo command contains various commands to manage GitHub repositories.",
                "repo <name> - retrieves repository information for the given repo. Uses the sender's nick as the GitHub login",
                "repo <owner> <name> - retrieves repository information for the given repo and login.",
                "--------",
                "Following the above arguments, these commands can also be performed:",
                "repo <...> set <option> <args> - sets the value of the given event option for a repo.",
                "repo <..> get <option> - retrieves information on the given event option.",
                "Valid options are: irc"})
public class RepoCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length == 0) {
            return false;
        }
        int startCheck = 2;
        boolean subMatchesSecond = event.getArgs().length >= startCheck && Pattern.compile("(set|get)").matcher(event.getArgs()[startCheck - 1]).matches();
        boolean subMatchesThird = event.getArgs().length >= (startCheck + 1) && Pattern.compile("(set|get)").matcher(event.getArgs()[startCheck]).matches();

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
            repo = GitHub.getGitHub().getRepo(owner + "/" + repoName);
        } catch (GitHubRepoNotFoundException e) {
            try {
                String owner2 = repoName;
                repoName = owner;
                owner = owner2;
                repo = GitHub.getGitHub().getRepo(owner + "/" + repoName);
            } catch (GitHubRepoNotFoundException e2) {
                event.respond(Colors.RED + "The GitHub repository " + Colors.BOLD + " " + repoName + "/" + owner + Colors.NORMAL + Colors.RED + " could not be found! :(");
                return true;
            }
        }

        /*
         * Begin actual command stuff
         */

        int startIndex = subMatchesSecond ? 1 : (subMatchesThird ? 2 : -1);
        if (startIndex > 0) {
            if (event.getArgs().length <= startIndex) {
                return false;
            }
            if (event.getArgs()[startIndex].equalsIgnoreCase("set")) {
                if (!repo.getRepoOwner().getLogin().equalsIgnoreCase(event.getSender().getNick()) && !Nexus.getInstance().isAdmin(event.getSender())) {
                    event.respondWithPing("You are not permitted to set repository settings for {0}.", repo.getFullName());
                    return true;
                }
                if (event.getArgs().length >= startIndex + 1) {
                    if (event.getArgs()[startIndex + 1].equalsIgnoreCase("irc")) {
                        if (event.getArgs().length == startIndex + 2) {
                            String validEvents = "";
                            for (GitHubEvent e : GitHubEvent.values()) {
                                validEvents += (validEvents.isEmpty()) ? e.getJsonName() : ", " + e.getJsonName();
                            }
                            event.errorWithPing("Valid events are: {0}.", validEvents);
                            return true;
                        }
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
                            GitHub.getGitHub().setIrcNotifications(repo, events.toArray(new GitHubEvent[events.size()]));
                            String eventsStr = "";
                            for (GitHubEvent e : events) {
                                eventsStr += (eventsStr.isEmpty()) ? e.getJsonName() : ", " + e.getJsonName();
                            }
                            event.respondWithPing("IRC Notifications for GitHub repository ({0}) set to: " + eventsStr, repo.getFullName());
                            return true;
                        }
                    } else {
                        event.errorWithPing("Invalid repository setting entered: {0}.", event.getArgs()[startIndex + 1]);
                        return true;
                    }
                }
            } else if (event.getArgs()[startIndex].equalsIgnoreCase("get")) {
                if (event.getArgs()[startIndex + 1].equalsIgnoreCase("irc")) {
                    String eventsStr = "";
                    for (GitHubEvent e : GitHub.getGitHub().getIrcNotifications(repo)) {
                        eventsStr += (eventsStr.isEmpty()) ? e.getJsonName() : ", " + e.getJsonName();
                    }
                    event.respondWithPing("IRC notifications for GitHub repository ({0}) are{1}", repo.getFullName(), eventsStr.isEmpty() ? " empty" : ": " + eventsStr);
                    return true;
                }
            }
        }
        if (repo != null) {
            if (!(event.isInPrivateMessage() && repo.getRepoOwner().getLogin().equals(event.getSender().getNick()))) {
                if (repo.isPrivate()) {
                    event.respondWithPing("That repository is private. I will not post information here.");
                    return true;
                }
            }
            ArrayList<String> activeCollaborators = new ArrayList<>();
            ArrayList<String> contributors = new ArrayList<>();
            for (GitHubUser user : repo.getCollaborators()) {
                contributors.add(user.getLogin());
            }
            for (GitHubUser user : repo.getCollaborators()) {
                // Filter out anyone who hasn't actually contributed yet
                if (contributors.contains(user.getLogin())) {
                    activeCollaborators.add(event.removePing(user.getLogin()));
                }
            }
            event.respond(Colors.BOLD + "GitHub - " + Colors.BLUE + repo.getName() + Colors.NORMAL + " (" + Colors.BOLD + event.removePing(repo.getRepoOwner().getLogin()) + Colors.NORMAL + ") - " + StringUtil.combineSplit(0, repo.getLanguages(), ", ") + " (" + repo.getUrl() + ")");
            event.respond("By {0}", StringUtil.combineSplit(0, activeCollaborators.toArray(new String[activeCollaborators.size()]), ", "));
            event.respond("Forks: {0} | Issues: {1}", String.valueOf(repo.getForksCount()), String.valueOf(repo.getOpenIssuesCount()));
            event.respond("Created at {0}", repo.getDateCreated().split("T")[0] + "");
            event.respond("Last pushed to at {0}", repo.getDateLastPushedTo().split("T")[0] + "");
            event.respond("Last updated at {0}", repo.getDateLastUpdated().split("T")[0] + "");
            return true;
        }
        return false;
    }
}