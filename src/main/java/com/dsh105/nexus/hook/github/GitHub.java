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

package com.dsh105.nexus.hook.github;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.exception.github.*;
import com.dsh105.nexus.hook.github.gist.Gist;
import com.dsh105.nexus.hook.github.gist.GistFile;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.*;
import java.util.*;

public class GitHub {

    private HashMap<String, GitHubRepo> repositories = new HashMap<>();
    private HashMap<GitHubRepo, Long> expirationDates = new HashMap<>();
    private ArrayList<GitHubIssue> issues = new ArrayList<>();

    public GitHub() {
        new Timer(true).schedule(new RefreshTask(), 0, 300000);
    }

    public static GitHub getGitHub() {
        if (Nexus.getInstance().getConfig().getGitHubApiKey().isEmpty()) {
            throw new GitHubAPIKeyInvalidException("Invalid GitHub API key!");
        }
        GitHub gh = Nexus.getInstance().getGithub();
        if (gh.getApiRateLimit().getRemaining() <= 0) {
            throw new GitHubRateLimitExceededException("Rate limit for GitHub API exceeded. Further requests cannot be executed.");
        }
        return gh;
    }

    public String createGist(Exception e) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        Gist gist = new Gist(new GistFile(writer.toString()));
        return gist.create();
    }

    public GitHubRepo getRepo(String name) {
        if (repositories.get(name) != null) {
            return repositories.get(name);
        }
        try {
            HttpResponse<JsonNode> response = Unirest.get(getRepoApiUrl(name) + getAccessToken()).asJson();
            InputStream input = response.getRawBody();
            GitHubRepo repo = Nexus.JSON.read(input, GitHubRepo.class);
            try {
                repo.isPrivate = response.getBody().getObject().getBoolean("private");
            } catch (JSONException e) {
                // ignore it
            }
            if (repo == null || repo.getUrl() == null) {
                throw new GitHubRepoNotFoundException("Failed to locate GitHub Repo: " + name);
            }
            repo.repoOwner = getUser(response.getBody().getObject().getJSONObject("owner").getString("login"));
            repo.getCollaborators();
            repo.getLanguages();
            if (repo != null) {
                cache(repo);
            }
            return repo;
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubRepoNotFoundException("Failed to locate GitHub Repo: " + name, e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    public GitHubUser getUser(String userLogin) {
        try {
            return Nexus.JSON.read(Unirest.get(getUserUrl(userLogin)), GitHubUser.class);
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubUserNotFoundException("Failed to locate GitHub User: " + userLogin, e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    protected GitHubUser getOwnerOf(GitHubRepo repo) {
        return getUser(repo.getFullName().split("/")[0]);
    }

    protected GitHubUser getReporterOf(GitHubIssue issue) {
        try {
            HttpResponse<JsonNode> response = Unirest.get(getIssuesUrl(issue.repoFullName, issue.getNumber()) + getAccessToken()).asJson();
            return getUser(response.getBody().getObject().getJSONObject("user").getString("login"));
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubRepoNotFoundException("Failed to locate GitHub Repo: " + issue.repoFullName, e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    public GitHubIssue getIssue(GitHubRepo repo, int id) {
        Iterator<GitHubIssue> iter = new ArrayList<>(issues).iterator();
        while (iter.hasNext()) {
            GitHubIssue i = iter.next();
            if (repo.getFullName().equals(i.getRepo().getFullName()) && i.getNumber() == id) {
                return i;
            }
        }

        try {
            HttpResponse<JsonNode> response = Unirest.get(getIssuesUrl(repo.getFullName(), id) + getAccessToken()).asJson();
            InputStream input = response.getRawBody();
            GitHubIssue issue;
            try {
                if (response.getBody().getObject().get("pull_request") != null) {
                    issue = Nexus.JSON.read(Unirest.get(getPullsUrl(repo.getFullName(), id) + getAccessToken()), GitHubPullRequest.class);
                } else {
                    issue = Nexus.JSON.read(input, GitHubIssue.class);
                }
            } catch (JSONException e) {
                issue = Nexus.JSON.read(input, GitHubIssue.class);
            }
            issue.repo = repo;
            issue.repoFullName = repo.getFullName();
            issue.reportedBy = issue.getReporter();
            if (issue != null) {
                cache(issue);
            }
            return issue;
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubRepoNotFoundException("Failed to locate GitHub Repo: " + repo.getFullName(), e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    public GitHubIssue getIssue(String repoName, int id) {
        return getIssue(getRepo(repoName), id);
    }

    public GitHubHook[] getHooks(GitHubRepo repo) {
        return getHooks(repo.getFullName());
    }

    public GitHubHook[] getHooks(String name) {
        try {
            return Nexus.JSON.read(Unirest.get(getHooksUrl(name) + getAccessToken()), GitHubHook[].class);
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubRepoNotFoundException("Failed to locate GitHub Repo: " + name, e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    public GitHubHook getHook(GitHubRepo repo, int id) {
        return getHook(repo.getFullName(), id);
    }

    public GitHubHook getHook(String repo, int id) {
        for (GitHubHook hook : getHooks(repo)) {
            if (hook.getId() == id) {
                return hook;
            }
        }
        return null;
    }

    public GitHubHook getHook(GitHubRepo repo, String name) {
        return getHook(repo.getFullName(), name);
    }

    public GitHubHook getHook(String repo, String name) {
        for (GitHubHook hook : getHooks(repo)) {
            if (hook.getName().equals(name)) {
                return hook;
            }
        }
        return null;
    }

    public GitHubUser[] getCollaborators(GitHubRepo repo) {
        return getCollaborators(repo.getFullName());
    }

    public GitHubUser[] getCollaborators(String name) {
        try {
            return Nexus.JSON.read(Unirest.get(getCollaboratorsUrl(name) + getAccessToken()), GitHubUser[].class);
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubRepoNotFoundException("Failed to locate GitHub Repo: " + name, e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    public GitHubUser[] getContributors(GitHubRepo repo) {
        return getContributors(repo.getFullName());
    }

    public GitHubUser[] getContributors(String name) {
        try {
            return Nexus.JSON.read(Unirest.get(getContributorsUrl(name) + getAccessToken()), GitHubUser[].class);
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubRepoNotFoundException("Failed to locate GitHub Repo: " + name, e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    public String[] getLanguages(GitHubRepo repo) {
        return getLanguages(repo.getFullName());
    }

    public String[] getLanguages(String name) {
        try {
            Set<String> set = Unirest.get(getLanguagesUrl(name) + getAccessToken()).asJson().getBody().getObject().keySet();
            ArrayList<String> languages = new ArrayList<>();
            for (String language : set) {
                languages.add(language);
            }
            return languages.toArray(new String[languages.size()]);
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubRepoNotFoundException("Failed to locate GitHub Repo: " + name, e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    public void setIrcNotifications(GitHubRepo repo, GitHubEvent... events) {
        setIrcNotifications(repo.getFullName(), events);
    }

    public void setIrcNotifications(String repo, GitHubEvent... events) {
        if (Nexus.getInstance().getConfig().getGitHubApiKey().isEmpty()) {
            throw new GitHubAPIKeyInvalidException("Invalid GitHub API key!");
        }
        GitHubHook hook = getHook(repo, "irc");
        if (hook != null) {
            String s = "";
            for (GitHubEvent e : events) {
                s += s.isEmpty() ? "\"" + e.getJsonName() + "\"" : "," + "\"" + e.getJsonName() + "\"";
            }
            ArrayList<String> eventsList = new ArrayList<>();
            for (GitHubEvent e : events) {
                eventsList.add(e.getJsonName());
            }
            try {
                Unirest.patch(getHooksUrl(repo) + "/" + hook.getId() + getAccessToken())
                        .header("accept", "application/json")
                        .header("content-type", "application/json")
                        .body("{\"events\":[" + s + "]}").asJson();
            } catch (UnirestException e) {
                throw new GitHubException("Could not connect to GitHub API!", e);
            }
        } else {
            throw new GitHubHookNotFoundException("IRC Hook not found for GitHub Repo (" + repo + ")");
        }
    }

    public GitHubEvent[] getIrcNotifications(GitHubRepo repo) {
        return getIrcNotifications(repo.getFullName());
    }

    public GitHubEvent[] getIrcNotifications(String repo) {
        if (Nexus.getInstance().getConfig().getGitHubApiKey().isEmpty()) {
            throw new GitHubAPIKeyInvalidException("Invalid GitHub API key!");
        }
        GitHubHook hook = getHook(repo, "irc");
        if (hook != null) {
            try {
                ArrayList<GitHubEvent> events = new ArrayList<>();
                JSONArray array = Unirest.get(getHooksUrl(repo) + "/" + hook.getId() + getAccessToken()).asJson().getBody().getObject().getJSONArray("events");
                for (int i = 0; i < array.length(); i++) {
                    GitHubEvent e = GitHubEvent.getByJsonName(array.getString(i));
                    if (e != null) {
                        events.add(e);
                    }
                }
                return events.toArray(new GitHubEvent[events.size()]);
            } catch (UnirestException e) {
                throw new GitHubException("Could not connect to GitHub API!", e);
            }
        } else {
            throw new GitHubHookNotFoundException("IRC Hook not found for GitHub Repo (" + repo + ")");
        }
    }

    public String getApiUrl() {
        return "https://api.github.com";
    }

    public String getRepoApiUrl(String repoName) {
        return getApiUrl() + "/repos/" + repoName;
    }

    public String getForksUrl(String repoName) {
        return getRepoApiUrl(repoName) + "/forks";
    }

    public String getEventsUrl(String repoName) {
        return getRepoApiUrl(repoName) + "/events";
    }

    public String getIssueEventsUrl(String repoName) {
        return getRepoApiUrl(repoName) + "/issues/events";
    }

    public String getIssuesUrl(String repoName, int id) {
        return getRepoApiUrl(repoName) + "/issues/" + id;
    }

    public String getPullsUrl(String repoName, int id) {
        return getRepoApiUrl(repoName) + "/pulls/" + id;
    }

    public String getContributorsUrl(String repoName) {
        return getRepoApiUrl(repoName) + "/contributors";
    }

    public String getHooksUrl(String repoName) {
        return getRepoApiUrl(repoName) + "/hooks";
    }

    public String getCollaboratorsUrl(String repoName) {
        return getRepoApiUrl(repoName) + "/collaborators";
    }

    public String getLanguagesUrl(String repoName) {
        return getRepoApiUrl(repoName) + "/languages";
    }

    public String getUserUrl(String userLogin) {
        return getApiUrl() + "/users/" + userLogin;
    }

    private String getAccessToken() {
        return "?access_token=" + Nexus.getInstance().getConfig().getGitHubApiKey();
    }

    private GitHubRateLimit getApiRateLimit() {
        try {
            return Nexus.JSON.read(Unirest.get(getApiUrl() + "/rate_limit" + getAccessToken()), "rate", GitHubRateLimit.class);
        } catch (UnirestException e) {
            throw new GitHubException("Failed to connect to GitHub API!", e);
        }
    }

    public class RefreshTask extends TimerTask {

        @Override
        public void run() {
            HashMap<String, GitHubRepo> fullNameToRepoMapCopy = new HashMap<>(repositories);
            ArrayList<GitHubIssue> issuesCopy = new ArrayList<>(issues);
            for (Map.Entry<String, GitHubRepo> entry : fullNameToRepoMapCopy.entrySet()) {
                long expiration = expirationDates.get(entry.getValue());
                if (expiration > 0) {
                    // Only keep them in memory for a certain period of time
                    if (new Date().before(new Date(expiration))) {
                        GitHubRepo repo = getRepo(entry.getKey());
                        repositories.remove(entry.getKey());
                        expirationDates.remove(entry.getValue());
                        cache(repo);
                    }
                }
            }

            for (GitHubIssue issue : issuesCopy) {
                issues.remove(issue);
                cache(getIssue(issue.getRepo(), issue.getNumber()));
            }
        }
    }

    private void cache(GitHubRepo repo) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 15);
        repositories.put(repo.getFullName(), repo);
        expirationDates.put(repo, c.getTimeInMillis());
    }

    private void cache(GitHubIssue issue) {
        issues.add(issue);
    }
}
