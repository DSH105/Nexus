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

    public static String API_URL = "https://api.github.com";
    public static String REPO_API_URL = API_URL + "/repos/{name}";
    public static String USER_API_URL = API_URL + "/users/{name}";
    public static String RATE_LIMIT_API_URL = API_URL + "/rate_limit";
    public static String GISTS_API_URL = API_URL + "/gists";

    public static String ISSUES = "/issues/{id}";
    public static String PULLS = "/pulls/{id}";
    public static String CONTRIBUTORS = "/contributors";
    public static String HOOKS = "/hooks";
    public static String COLLABORATORS = "/collaborators";
    public static String LANGUAGES = "/languages";

    public static String getRepoApiUrl(String repoName) {
        return REPO_API_URL.replace("{name}", repoName);
    }

    public static String getIssuesUrl(String repoName, int id) {
        return getRepoApiUrl(repoName) + ISSUES.replace("{id}", String.valueOf(id));
    }

    public static String getPullsUrl(String repoName, int id) {
        return getRepoApiUrl(repoName) + PULLS.replace("{id}", String.valueOf(id));
    }

    public static String getContributorsUrl(String repoName) {
        return getRepoApiUrl(repoName) + CONTRIBUTORS;
    }

    public static String getHooksUrl(String repoName) {
        return getRepoApiUrl(repoName) + HOOKS;
    }

    public static String getCollaboratorsUrl(String repoName) {
        return getRepoApiUrl(repoName) + COLLABORATORS;
    }

    public static String getLanguagesUrl(String repoName) {
        return getRepoApiUrl(repoName) + LANGUAGES;
    }

    public static String getUserUrl(String userLogin) {
        return USER_API_URL.replace("{name}", userLogin);
    }

    private HashMap<String, GitHubRepo> repositories = new HashMap<>();
    private HashMap<GitHubRepo, Long> expirationDates = new HashMap<>();
    private ArrayList<GitHubIssue> issues = new ArrayList<>();

    public GitHub() {
        new Timer(true).schedule(new RefreshTask(), 0, 300000);
    }

    public static GitHub getGitHub() {
        /*if (Nexus.getInstance().getConfig().getGitHubApiKey().isEmpty()) {
            throw new GitHubAPIKeyInvalidException("Invalid GitHub API key!");
        }*/
        GitHub gh = Nexus.getInstance().getGithub();

        return gh;
    }

    public String createGist(Exception e) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        Gist gist = new Gist(new GistFile(writer.toString()));
        return gist.create();
    }

    public String getAccessToken(String userLogin, boolean onlyAllowTokenAccess) {
        String accessToken = Nexus.getInstance().getConfig().getGitHubApiKey(userLogin);
        if (accessToken.isEmpty() && onlyAllowTokenAccess) {
            throw new GitHubAPIKeyInvalidException("GitHub API key for " + userLogin + " is invalid. Please provide one to access this part of the GitHub API");
        }
        // Make sure that we have a valid API key to use. Provide the default Nexus API key if this user doesn't have one.
        return "?access_token=" + (accessToken.isEmpty() ? Nexus.getInstance().getConfig().getNexusGitHubApiKey() : accessToken);
    }

    public String getAccessToken(String userLogin) {
        return getAccessToken(userLogin, false);
    }

    private GitHubRateLimit getApiRateLimit(String accessToken) {
        try {
            return Nexus.JSON.read(Unirest.get(RATE_LIMIT_API_URL + (accessToken.startsWith("?access_token=") ? accessToken : (accessToken.isEmpty() ? "" : "?access_token=" + accessToken))), "rate", GitHubRateLimit.class);
        } catch (UnirestException e) {
            throw new GitHubException("Failed to connect to GitHub API!", e);
        }
    }

    public HttpResponse<JsonNode> makeRequest(String urlPath, String userLogin) throws UnirestException {
        return makeRequest(urlPath, userLogin, false);
    }

    protected HttpResponse<JsonNode> makeRequest(String urlPath, String userLogin, boolean assumeAccess) throws UnirestException {
        return makeRequest(urlPath, userLogin, assumeAccess, false);
    }

    protected HttpResponse<JsonNode> makeRequest(String urlPath, String userLogin, boolean assumeAccess, boolean onlyAllowTokenAccess) throws UnirestException {
        String accessToken = getAccessToken(userLogin, onlyAllowTokenAccess);
        // check if the api key has expired - overused
        if (getApiRateLimit(accessToken).getRemaining() <= 0) {
            throw new GitHubRateLimitExceededException("Rate limit for GitHub API exceeded. Further requests cannot be executed.");
        }

        HttpResponse<JsonNode> response = Unirest.get(urlPath + accessToken).asJson();
        if (!assumeAccess) {
            try {
                String checkAccess = response.getBody().getObject().getString("message");
                if (checkAccess != null && (checkAccess.equalsIgnoreCase("NOT FOUND") || checkAccess.equalsIgnoreCase("BAD CREDENTIALS"))) {
                    throw new GitHubAPIKeyInvalidException("GitHub API key for " + userLogin + " is invalid. Please provide one to access this part of the GitHub API");
                }
                return response;
            } catch (JSONException e) {
            }
        }
        return response;
    }

    public GitHubRepo getRepo(String name, String userLogin) {
        if (repositories.get(name) != null) {
            return repositories.get(name);
        }
        try {
            HttpResponse<JsonNode> response = makeRequest(getRepoApiUrl(name), userLogin);
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
            repo.repoOwner = getUser(response.getBody().getObject().getJSONObject("owner").getString("login"), userLogin);
            repo.collaborators = getCollaborators(repo, userLogin);
            repo.contributors = getContributors(repo, userLogin);
            repo.languages = getLanguages(repo, userLogin);
            repo.accessToken = userLogin;
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

    public GitHubUser getUser(String ghUserLogin, String userLogin) {
        try {
            return Nexus.JSON.read(makeRequest(getUserUrl(ghUserLogin), userLogin).getRawBody(), GitHubUser.class);
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubUserNotFoundException("Failed to locate GitHub User: " + ghUserLogin, e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    protected GitHubUser getReporterOf(GitHubIssue issue, String userLogin) {
        try {
            HttpResponse<JsonNode> response = makeRequest(getIssuesUrl(issue.repoFullName, issue.getNumber()), userLogin);
            return getUser(response.getBody().getObject().getJSONObject("user").getString("login"), userLogin);
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubRepoNotFoundException("Failed to locate GitHub Repo: " + issue.repoFullName, e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    public GitHubIssue getIssue(GitHubRepo repo, int id, String userLogin) {
        Iterator<GitHubIssue> iter = new ArrayList<>(issues).iterator();
        while (iter.hasNext()) {
            GitHubIssue i = iter.next();
            if (repo.getFullName().equals(i.getRepo().getFullName()) && i.getNumber() == id) {
                return i;
            }
        }

        try {
            HttpResponse<JsonNode> response = makeRequest(getIssuesUrl(repo.getFullName(), id), userLogin);
            InputStream input = response.getRawBody();
            GitHubIssue issue;
            try {
                if (response.getBody().getObject().get("pull_request") != null) {
                    issue = Nexus.JSON.read(makeRequest(getPullsUrl(repo.getFullName(), id), userLogin).getRawBody(), GitHubPullRequest.class);
                } else {
                    issue = Nexus.JSON.read(input, GitHubIssue.class);
                }
            } catch (JSONException e) {
                issue = Nexus.JSON.read(input, GitHubIssue.class);
            }
            issue.repo = repo;
            issue.repoFullName = repo.getFullName();
            issue.reportedBy = getReporterOf(issue, userLogin);
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

    public GitHubIssue getIssue(String repoName, int id, String userLogin) {
        return getIssue(getRepo(repoName, userLogin), id, userLogin);
    }

    protected GitHubUser[] getCollaborators(GitHubRepo repo, String userLogin) {
        return getCollaborators(repo.getFullName(), userLogin);
    }

    protected GitHubUser[] getCollaborators(String name, String userLogin) {
        try {
            return Nexus.JSON.read(makeRequest(getCollaboratorsUrl(name), userLogin, true).getRawBody(), GitHubUser[].class);
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubRepoNotFoundException("Failed to locate GitHub Repo: " + name, e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    protected GitHubUser[] getContributors(GitHubRepo repo, String userLogin) {
        return getContributors(repo.getFullName(), userLogin);
    }

    protected GitHubUser[] getContributors(String name, String userLogin) {
        try {
            // Anything using this method should already have checked API key validity
            return Nexus.JSON.read(makeRequest(getContributorsUrl(name), userLogin, true).getRawBody(), GitHubUser[].class);
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubRepoNotFoundException("Failed to locate GitHub Repo: " + name, e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    protected String[] getLanguages(GitHubRepo repo, String userLogin) {
        return getLanguages(repo.getFullName(), userLogin);
    }

    protected String[] getLanguages(String name, String userLogin) {
        try {
            // Anything using this method should already have checked API key validity
            Set<String> set = makeRequest(getLanguagesUrl(name), userLogin).getBody().getObject().keySet();
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

    public GitHubHook[] getHooks(GitHubRepo repo, String userLogin) {
        return getHooks(repo.getFullName(), userLogin);
    }

    public GitHubHook[] getHooks(String name, String userLogin) {
        try {
            return Nexus.JSON.read(makeRequest(getHooksUrl(name), userLogin, false, true).getRawBody(), GitHubHook[].class);
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubRepoNotFoundException("Failed to locate GitHub Repo: " + name, e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    public GitHubHook getHook(GitHubRepo repo, int id, String userLogin) {
        return getHook(repo.getFullName(), id, userLogin);
    }

    public GitHubHook getHook(String repo, int id, String userLogin) {
        for (GitHubHook hook : getHooks(repo, userLogin)) {
            if (hook.getId() == id) {
                return hook;
            }
        }
        return null;
    }

    public GitHubHook getHook(GitHubRepo repo, String name, String userLogin) {
        return getHook(repo.getFullName(), name, userLogin);
    }

    public GitHubHook getHook(String repo, String name, String userLogin) {
        for (GitHubHook hook : getHooks(repo, userLogin)) {
            if (hook.getName().equals(name)) {
                return hook;
            }
        }
        return null;
    }

    public void setIrcNotifications(GitHubRepo repo, String userLogin, GitHubEvent... events) {
        setIrcNotifications(repo.getFullName(), userLogin, events);
    }

    public void setIrcNotifications(String repo, String userLogin, GitHubEvent... events) {
        GitHubHook hook = getHook(repo, "irc", userLogin);
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
                Unirest.patch(getHooksUrl(repo) + "/" + hook.getId() + getAccessToken(userLogin, true))
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

    public GitHubEvent[] getIrcNotifications(GitHubRepo repo, String userLogin) {
        return getIrcNotifications(repo.getFullName(), userLogin);
    }

    public GitHubEvent[] getIrcNotifications(String repo, String userLogin) {
        GitHubHook hook = getHook(repo, "irc", userLogin);
        if (hook != null) {
            try {
                ArrayList<GitHubEvent> events = new ArrayList<>();
                JSONArray eventsJsonArray = makeRequest(getHooksUrl(repo) + "/" + hook.getId(), userLogin, false, true).getBody().getObject().getJSONArray("events");
                for (int i = 0; i < eventsJsonArray.length(); i++) {
                    GitHubEvent e = GitHubEvent.getByJsonName(eventsJsonArray.getString(i));
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

    public class RefreshTask extends TimerTask {

        @Override
        public void run() {
            HashMap<String, GitHubRepo> fullNameToRepoMapCopy = new HashMap<>(repositories);
            ArrayList<GitHubIssue> issuesCopy = new ArrayList<>(issues);
            for (Map.Entry<String, GitHubRepo> entry : fullNameToRepoMapCopy.entrySet()) {
                long expiration = expirationDates.get(entry.getValue());
                if (expiration > 0) {
                    repositories.remove(entry.getKey());
                    expirationDates.remove(entry.getValue());
                    // Only keep them in memory for a certain period of time
                    if (new Date().before(new Date(expiration))) {
                        GitHubRepo repo = getRepo(entry.getKey(), entry.getValue().accessToken);
                        cache(repo);
                    }
                }
            }

            for (GitHubIssue issue : issuesCopy) {
                issues.remove(issue);
                cache(getIssue(issue.getRepo(), issue.getNumber(), issue.repo.accessToken));
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
