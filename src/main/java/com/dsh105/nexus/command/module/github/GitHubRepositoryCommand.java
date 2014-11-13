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
import com.dsh105.nexus.command.CommandGroup;
import com.dsh105.nexus.exception.github.GitHubNotFoundException;
import com.dsh105.nexus.exception.github.GitHubPullRequestMergeException;
import com.dsh105.nexus.hook.github.*;
import com.dsh105.nexus.util.AuthUtil;
import com.dsh105.nexus.util.StringUtil;
import com.dsh105.nexus.util.shorten.URLShortener;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.pircbotx.Colors;

import java.util.ArrayList;
import java.util.regex.Pattern;

@Command(command = "repo",
        aliases = {"githubrepository", "repository", "ghrepo"},
        needsChannel = false,
        groups = CommandGroup.GITHUB,
        help = "Management of GitHub repositories.",
        extendedHelp = {
                "The repo command contains various commands to manage GitHub repositories.",
                "{b}{p}{c} <name>{/b} - retrieves repository information for the given repo. Uses the sender's nick as the GitHub login",
                "{b}{p}{c} [owner] <name>{/b} - Retrieves repository information for the given repo and login.",
                "{b}{p}{c} <...> save{/b} - Save a repository and owner combination for later ease of command use.",
                "{b}{p}{c} <...> issue <number>{/b} - Retrieve issue information for a GitHub repository.",
                "{b}{p}{c} <...> fork{/b} - Fork a repository. Requires a GitHub API key (see {b}{p}ghkey{/b})",
                "{b}{p}{c} <...> set <option> <args>{/b} - Sets the value of the given event option for a repository.",
                "{b}{p}{c} <...> get <option>{/b} - Retrieves information on the given event option for a repository.",
                "Valid options are: irc"
        })
public class GitHubRepositoryCommand extends CommandModule {

    @Override
    public boolean onCommand(final CommandPerformEvent event) {
        if (event.getArgs().length == 0) {
            event.getManager().onCommand(event.getChannel(), event.getSender(), "repo DSH105 Nexus");
            return true;
        }
        int startCheck = 2;
        String subCommands = "set|get|save|issue|fork";
        boolean subMatchesSecond = event.getArgs().length >= startCheck && Pattern.compile("(" + subCommands + ")").matcher(event.getArgs()[startCheck - 1]).matches();
        boolean subMatchesThird = event.getArgs().length >= (startCheck + 1) && Pattern.compile("(" + subCommands + ")").matcher(event.getArgs()[startCheck]).matches();

        String repoName = subMatchesSecond ? event.getArgs()[0] : ((subMatchesThird || event.getArgs().length == 2) ? event.getArgs()[1] : event.getArgs()[0]);
        String owner = subMatchesSecond ? event.getSender().getNick() : ((subMatchesThird || event.getArgs().length == 2) ? event.getArgs()[0] : event.getSender().getNick());

        repoName = repoName.replace('\\', '/');
        if (repoName.contains("/") && repoName.split("/").length == 2) {
            String[] parts = repoName.split("/");
            owner = parts[0];
            repoName = parts[1];
        } else if (owner == null) {
            owner = event.getSender().getNick();
        }

        GitHubRepo repo;
        try {
            repo = GitHub.getGitHub().getRepo(owner + "/" + repoName, AuthUtil.getIdent(event.getSender()));
        } catch (GitHubNotFoundException e) {
            try {
                String owner2 = repoName;
                repoName = owner;
                owner = owner2;

                repo = GitHub.getGitHub().getRepo(owner + "/" + repoName, AuthUtil.getIdent(event.getSender()));
            } catch (GitHubNotFoundException e2) {
                String storedCase = Nexus.getInstance().getGitHubConfig().get("github-repo-" + owner.toLowerCase(), ""); // temporarily use the owner so that the error message outputs correctly
                if (!storedCase.isEmpty()) {
                    repoName = owner;
                    owner = storedCase;
                    try {
                        repo = GitHub.getGitHub().getRepo(owner + "/" + repoName, AuthUtil.getIdent(event.getSender()));
                    } catch (GitHubNotFoundException e3) {
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
                            GitHub.getGitHub().setIrcNotifications(repo, AuthUtil.getIdent(event.getSender()), events.toArray(new GitHubEvent[events.size()]));
                            String eventsStr = "";
                            for (GitHubEvent e : events) {
                                eventsStr += (eventsStr.isEmpty()) ? e.getJsonName() : ", " + e.getJsonName();
                            }
                            event.respondWithPing("IRC Notifications for GitHub repository ({0}) set to: {1}", repo.getFullName(), eventsStr);
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
                        for (GitHubEvent e : GitHub.getGitHub().getIrcNotifications(repo, AuthUtil.getIdent(event.getSender()))) {
                            eventsStr += (eventsStr.isEmpty()) ? e.getJsonName() : ", " + e.getJsonName();
                        }
                        event.respondWithPing("IRC notifications for GitHub repository ({0}) are: {1}", repo.getFullName(), eventsStr.isEmpty() ? Colors.BOLD + "empty" : eventsStr);
                        return true;
                    } else {
                        event.errorWithPing("Invalid repository setting entered: {0}.", event.getArgs()[startIndex + 1]);
                        return true;
                    }
                }
            } else if (event.getArgs()[startIndex].equalsIgnoreCase("save")) {
                if (event.getArgs().length >= startIndex + 1) {
                    if (Nexus.getInstance().isAdmin(event.getSender())) {
                        Nexus.getInstance().getGitHubConfig().set("github-repo-" + repo.getName().toLowerCase(), repo.getRepoOwner().getLogin());
                        Nexus.getInstance().getGitHubConfig().save();
                        event.respondWithPing("GitHub repository information for {0} stored under {1}. You can now use {2} instead of {3} for this repository", repo.getName(), event.removePing(repo.getRepoOwner().getLogin()), event.getCommandPrefix() + event.getCommand() + " <repo_name>", event.getCommandPrefix() + event.getCommand() + " <owner> <repo_name>");
                    } else {
                        event.respondWithPing("Only admins can save repository information.");
                    }
                    return true;
                }
            } else if (event.getArgs()[startIndex].equalsIgnoreCase("issue")) {
                if (event.getArgs().length == startIndex + 2 || event.getArgs().length == startIndex + 3) {
                    String issueNumber = event.getArgs()[startIndex + 1];
                    GitHubIssue issue;
                    try {
                        issue = GitHub.getGitHub().getIssue(repo, StringUtil.toInteger(issueNumber), AuthUtil.getIdent(event.getSender()));
                    } catch (GitHubNotFoundException e) {
                        event.errorWithPing("Issue {0} doesn't exist at {1} (or that repository doesn't have issues enabled).", "#" + issueNumber, repo.getFullName());
                        return true;
                    }
                    if (event.getArgs().length == startIndex + 2) {
                        IssueState issueState = IssueState.getByIdent(issue.getState());
                        String state = issueState.format(issue.getState()).toUpperCase();
                        /*String body = issue.getBody();
                        if (body.length() > 30) {
                            body = body.substring(0, 70) + "...";
                        }*/

                        if (issue instanceof GitHubPullRequest) {
                            GitHubPullRequest pr = (GitHubPullRequest) issue;
                            if (pr.isMerged()) {
                                state = Colors.PURPLE + Colors.UNDERLINE + "MERGED";
                            }
                            String mergeData = (pr.isMerged() ? " (" + URLShortener.shortenGit("http://github.com/DSH105/HoloAPI/commit/" + pr.getMergeCommit()) + ")" : "");
                            event.respond(Colors.BOLD + "GitHub PR #" + pr.getNumber() + Colors.NORMAL + " - " + Colors.BLUE + Colors.BOLD + repo.getName() + Colors.NORMAL + " (" + Colors.BOLD + event.removePing(repo.getRepoOwner().getLogin()) + Colors.NORMAL + ") -  (" + URLShortener.shortenGit(pr.getUrl()) + ")");
                            event.respond("Reporter: {0}", event.removePing(pr.getReporter().getLogin()));
                            event.respond("Title: " + pr.getTitle());
                            //event.respond("Body: " + body);
                            event.respond("Status: {0}" + mergeData + " | Comments: {1} | Review Comments: {2}", state, String.valueOf(pr.getComments()), String.valueOf(pr.getReviewComments()));
                            event.respond("Commits: {0} | Additions: " + Colors.GREEN + "{1} | Deletions: " + Colors.RED + "{2} | Files Changed: {3}", pr.getCommits() + "", pr.getAdditions() + "", pr.getDeletions() + "", pr.getChangedFiles() + "");
                            event.respond("Created: {0} | Updated: {1}" + (issue.getDateClosed() != null ? " | Closed: {2}" : ""), issue.getDateCreated(), issue.getDateUpdated(), issue.getDateClosed());
                        } else {
                            event.respond(Colors.BOLD + "GitHub Issue #" + issue.getNumber() + Colors.NORMAL + " - " + Colors.BLUE + Colors.BOLD + repo.getName() + Colors.NORMAL + " (" + Colors.BOLD + event.removePing(repo.getRepoOwner().getLogin()) + Colors.NORMAL + ") -  (" + URLShortener.shortenGit(issue.getUrl()) + ")");
                            event.respond("Reporter: " + event.removePing(issue.getReporter().getLogin()));
                            event.respond("Title: " + issue.getTitle());
                            //event.respond("Body: \"" + body + "\"");
                            event.respond("Status: {0} | Comments: {1}", state, String.valueOf(issue.getComments()));
                            event.respond("Created: {0} | Updated: {1}" + (issue.getDateClosed() != null ? " | Closed: {2}" : ""), issue.getDateCreated(), issue.getDateUpdated(), issue.getDateClosed());
                        }
                        return true;
                    } else if (event.getArgs().length == startIndex + 3) {
                        if (event.getArgs()[startIndex + 2].equalsIgnoreCase("merge")) {
                            if (!(issue instanceof GitHubPullRequest)) {
                                event.errorWithPing("Issue #" + issue.getNumber() + " is not a pull request!");
                                return true;
                            }
                            GitHubPullRequest pr = (GitHubPullRequest) issue;
                            try {
                                GitHub.getGitHub().mergePullRequest(pr, AuthUtil.getIdent(event.getSender()));
                                event.respondWithPing("Pull Request {0} merged (" + Colors.GREEN + "{1} " + Colors.RED + "{2} in {3} file" + (pr.getChangedFiles() == 1 ? "" : "s") + ".)", "#" + pr.getNumber(), "+" + pr.getAdditions(), "-" + pr.getDeletions(), "" + pr.getChangedFiles());
                            } catch (GitHubPullRequestMergeException e) {
                                event.respondWithPing("Failed to merge pull request ({0}). Reason: " + e.getMessage(), pr.getRepo().getFullName() + " #" + pr.getNumber());
                            }
                            return true;
                        }
                    }
                }
            } else if (event.getArgs()[startIndex].equalsIgnoreCase("fork")) {
                final String toFork = repo.getFullName();
                GitHub.getGitHub().forkAsync(repo, AuthUtil.getIdent(event.getSender()), new Callback<JsonNode>() {
                    @Override
                    public void completed(HttpResponse<JsonNode> response) {
                        event.respondWithPing("Repository ({0}) forked to {1} on your behalf (" + URLShortener.shortenGit(response.getBody().getObject().getString("html_url")) + ").", toFork, response.getBody().getObject().getString("full_name"));
                    }

                    @Override
                    public void failed(UnirestException e) {
                        event.respondWithPing("Failed to fork repository ({0}). Reason: " + GitHub.getGitHub().createGist(e), toFork);
                    }

                    @Override
                    public void cancelled() {
                        event.respondWithPing("Failed to fork repository ({0}). Reason: {1}", toFork, "Request cancelled");
                    }
                });
                return true;
            }
        }
        if (repo != null) {
            boolean sendPm = false;
            if (repo.isPrivate()) {
                if (!repo.getRepoOwner().getLogin().equals(event.getSender().getNick())) {
                    // Pretend it's not even there... >:)
                    event.errorWithPing("The GitHub repository {0} could not be found! :(", repoName + "/" + owner);
                    return true;
                }
                event.respondWithPing("That repository is private. Please check your private messages for repository information.");
                sendPm = true;
            }
            ArrayList<String> activeCollaborators = new ArrayList<>();
            ArrayList<String> contributors = new ArrayList<>();
            for (GitHubUser user : repo.getContributors()) {
                contributors.add(user.getLogin());
            }
            for (GitHubUser user : repo.getCollaborators()) {
                // Filter out anyone who hasn't actually contributed yet
                if (contributors.contains(user.getLogin()) || user.getLogin().equalsIgnoreCase(repo.getRepoOwner().getLogin())) {
                    activeCollaborators.add(event.removePing(user.getLogin()));
                }
            }

            int total = 0;
            for (GitHubLanguage l : repo.getLanguages()) {
                total += l.getBytes();
            }

            String languages = "";
            for (GitHubLanguage l : repo.getLanguages()) {
                languages += (languages.isEmpty() ? "" : ", ") + l.getName() + " (" + Math.round(((double) l.getBytes() / (double) total) * 100) + "%)";
            }
            event.respond(Colors.BOLD + "GitHub" + Colors.NORMAL + " - " + Colors.BOLD + Colors.BLUE + repo.getName() + Colors.NORMAL + " (" + Colors.BOLD + event.removePing(repo.getRepoOwner().getLogin()) + Colors.NORMAL + ") - " + languages + " - (" + URLShortener.shortenGit(repo.getUrl()) + ")", sendPm);
            event.respond("Forks: {0} | Issues: {1} | Stars: {2}", sendPm, String.valueOf(repo.getForksCount()), String.valueOf(repo.getOpenIssuesCount()), String.valueOf(repo.getStargazers()));
            event.respond("Created: {0} | Last Pushed: {1}", sendPm, repo.getDateCreated(), repo.getDateLastPushedTo());
            if (activeCollaborators.isEmpty()) {
                event.respond("A valid token is required to view collaborator information.", sendPm);
            } else {
                event.respond("Collaborators: {0}", sendPm, StringUtil.combineSplit(0, activeCollaborators.toArray(new String[activeCollaborators.size()]), ", "));
            }
            return true;
        }
        return false;
    }
}
