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
import com.dsh105.nexus.exception.github.GitHubRepoNotFoundException;
import com.dsh105.nexus.hook.github.*;
import com.dsh105.nexus.util.shorten.URLShortener;
import com.dsh105.nexus.util.StringUtil;
import org.pircbotx.Colors;

import java.util.ArrayList;
import java.util.regex.Pattern;

@Command(command = "repo", needsChannel = false, help = "Management of GitHub repositories.",
        extendedHelp = {
                "The repo command contains various commands to manage GitHub repositories.",
                "{b}{p}{c} <name>{/b} - retrieves repository information for the given repo. Uses the sender's nick as the GitHub login",
                "{b}{p}{c} [owner] <name>{/b} - retrieves repository information for the given repo and login.",
                "--------",
                "Following the above arguments, these commands can also be performed:",
                "{b}{p}{c} <...> save{/b} - save a repo and owner combination for later ease of command use.",
                "{b}{p}{c} <...> set <option> <args>{/b} - sets the value of the given event option for a repo.",
                "{b}{p}{c} <..> get <option>{/b} - retrieves information on the given event option.",
                "Valid options are: irc"})
public class RepoCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length == 0) {
            return false;
        }
        int startCheck = 2;
        boolean subMatchesSecond = event.getArgs().length >= startCheck && Pattern.compile("(set|get|save|issue)").matcher(event.getArgs()[startCheck - 1]).matches();
        boolean subMatchesThird = event.getArgs().length >= (startCheck + 1) && Pattern.compile("(set|get|save|issue)").matcher(event.getArgs()[startCheck]).matches();

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
                String storedCase = Nexus.getInstance().getConfig().get("github-repo-" + owner.toLowerCase(), ""); // temporarily use the owner so that the error message outputs correctly
                if (!storedCase.isEmpty()) {
                    repoName = owner;
                    owner = storedCase;
                    try {
                        repo = GitHub.getGitHub().getRepo(owner + "/" + repoName);
                    } catch (GitHubRepoNotFoundException e3) {
                        event.errorWithPing("The GitHub repository {0} could not be found! :(", repoName + "/" + owner);
                        return true;
                    }
                } else {
                    event.errorWithPing("The GitHub repository {0} could not be found! :(", repoName + "/" + owner);
                    return true;
                }
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
                if (event.getArgs().length >= startIndex + 2) {
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
                            event.respondWithPing("You entered " + Colors.BOLD + "invalid" + Colors.NORMAL + Colors.RED + " event names: " + StringUtil.combineSplit(0, invalidEvents.toArray(new String[invalidEvents.size()]), ", ") + ". " + Colors.BOLD + "Valid" + Colors.NORMAL + Colors.RED + " event names are: " + validEvents);
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
                if (event.getArgs().length >= startIndex + 2) {
                    if (event.getArgs()[startIndex + 1].equalsIgnoreCase("irc")) {
                        String eventsStr = "";
                        for (GitHubEvent e : GitHub.getGitHub().getIrcNotifications(repo)) {
                            eventsStr += (eventsStr.isEmpty()) ? e.getJsonName() : ", " + e.getJsonName();
                        }
                        event.respondWithPing("IRC notifications for GitHub repository ({0}) are{1}", repo.getFullName(), eventsStr.isEmpty() ? " empty" : ": " + eventsStr);
                        return true;
                    } else {
                        event.errorWithPing("Invalid repository setting entered: {0}.", event.getArgs()[startIndex + 1]);
                        return true;
                    }
                }
            } else if (event.getArgs()[startIndex].equalsIgnoreCase("save")){
                if (event.getArgs().length >= startIndex + 1) {
                    if (Nexus.getInstance().isAdmin(event.getSender())) {
                        Nexus.getInstance().getConfig().set("github-repo-" + repo.getName().toLowerCase(), repo.getRepoOwner().getLogin());
                        Nexus.getInstance().getConfig().save();
                        event.respondWithPing("GitHub repository information for {0} stored under {1}. You can now use {2} instead of {3} for this repository", repo.getName(), event.removePing(repo.getRepoOwner().getLogin()), event.getCommandPrefix() + event.getCommand() + " <repo_name>", event.getCommandPrefix() + event.getCommand() + " <owner> <repo_name>");
                    } else {
                        event.respondWithPing("Only admins can save repository information.");
                    }
                    return true;
                }
            } else if (event.getArgs()[startIndex].equalsIgnoreCase("issue")) {
                if (event.getArgs().length == startIndex + 2) {
                    String issueNumber = event.getArgs()[startIndex + 1];
                    if (!StringUtil.isInt(issueNumber)) {
                        event.respondWithPing("{0} needs to be an integer.", issueNumber);
                        return true;
                    }
                    GitHubIssue issue;
                    try {
                        issue = GitHub.getGitHub().getIssue(repo, Integer.parseInt(issueNumber));
                    } catch (GitHubRepoNotFoundException e) {
                        event.respondWithPing("I couldn't find that for you. Either that repository doesn't have issues enabled, or issue #{0} doesn't exist.");
                        return true;
                    }

                    IssueState issueState = IssueState.getByIdent(issue.getState());
                    String state = issueState.format(issue.getState()).toUpperCase();
                    String body = issue.getBody();
                    if (body.length() > 30) {
                        body = body.substring(0, 70) + "...";
                    }

                    if (issue instanceof GitHubPullRequest) {
                        GitHubPullRequest pr = (GitHubPullRequest) issue;
                        if (pr.isMerged()) {
                            state = Colors.PURPLE + Colors.UNDERLINE + "MERGED";
                        }
                        String mergeData = (pr.isMerged() ? " (" + URLShortener.shorten("http://github.com/DSH105/HoloAPI/commit/" + pr.getMergeCommit()) + ")" : "");
                        event.respond(Colors.BOLD + "GitHub PR #" + pr.getNumber() + Colors.NORMAL + " - " + Colors.BLUE + Colors.BOLD + repo.getName() + Colors.NORMAL + " (" + Colors.BOLD + event.removePing(repo.getRepoOwner().getLogin()) + Colors.NORMAL + ") -  (" + URLShortener.shorten(pr.getUrl()) + ")");
                        event.respond("Reporter: {0}", event.removePing(pr.getReporter().getLogin()));
                        event.respond("Title: " + pr.getTitle());
                        event.respond("Body: " + body);
                        event.respond("Status: {0}" + mergeData + " | Comments: {1} | Review Comments: {2}", state, String.valueOf(pr.getComments()), String.valueOf(pr.getReviewComments()));
                        event.respond("Commits: {0} | Additions: " + Colors.GREEN + "{1} | Deletions: " + Colors.RED + "{2} | Files Changed: {3}", pr.getCommits() + "", pr.getAdditions() + "", pr.getDeletions() + "", pr.getChangedFiles() + "");
                        event.respond("Created: {0} | Updated: {1} | " + (issue.getDateClosed() != null ? " | Closed {2}" : ""), issue.getDateCreated(), issue.getDateUpdated(), issue.getDateClosed());
                    } else {
                        event.respond(Colors.BOLD + "GitHub Issue #" + issue.getNumber() + Colors.NORMAL + " - " + Colors.BLUE + Colors.BOLD+ repo.getName() + Colors.NORMAL + " (" + Colors.BOLD + event.removePing(repo.getRepoOwner().getLogin()) + Colors.NORMAL + ") -  (" + URLShortener.shorten(issue.getUrl()) + ")");
                        event.respond("Reporter: " + event.removePing(issue.getReporter().getLogin()));
                        event.respond("Title: " + issue.getTitle());
                        event.respond("Body: \"" + body + "\"");
                        event.respond("Status: {0} | Comments: {1}", state, String.valueOf(issue.getComments()));
                        event.respond("Created: {0} | Updated: {1} | " + (issue.getDateClosed() != null ? " | Closed {2}" : ""), issue.getDateCreated(), issue.getDateUpdated(), issue.getDateClosed());
                    }
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
            event.respond(Colors.BOLD + "GitHub" + Colors.NORMAL + " - " + Colors.BOLD + Colors.BLUE + repo.getName() + Colors.NORMAL + " (" + Colors.BOLD + event.removePing(repo.getRepoOwner().getLogin()) + Colors.NORMAL + ") - " + StringUtil.combineSplit(0, repo.getLanguages(), ", ") + " (" + repo.getUrl() + ")");
            event.respond("By {0}", StringUtil.combineSplit(0, activeCollaborators.toArray(new String[activeCollaborators.size()]), ", "));
            event.respond("Forks: {0} | Issues: {1}", String.valueOf(repo.getForksCount()), String.valueOf(repo.getOpenIssuesCount()));
            event.respond("Created {0} | Last Pushed {1}", repo.getDateCreated().split("T")[0], repo.getDateLastPushedTo().split("T")[0]);
            return true;
        }
        return false;
    }
}