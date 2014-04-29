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
import com.dsh105.nexus.exception.GitHubAPIKeyInvalidException;
import com.dsh105.nexus.exception.GitHubException;
import com.dsh105.nexus.exception.GitHubRepoNotFoundException;
import com.dsh105.nexus.exception.IrcHookNotFoundException;
import com.dsh105.nexus.hook.github.gist.Gist;
import com.dsh105.nexus.hook.github.gist.GistFile;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GitHub {

    public String createGist(Exception e) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        Gist gist = new Gist(new GistFile(writer.toString()));
        return gist.create();
    }

    public GitHubRepo getRepo(String name) {
        try {
            HttpResponse<JsonNode> response = Unirest.get(getRepoApiUrl(name)).asJson();
            GitHubRepo repo = Nexus.JSON.read(response.getRawBody(), GitHubRepo.class);
            repo.isPrivate = response.getBody().getObject().getBoolean("private");
            repo.repoOwner = Nexus.JSON.read(response.getRawBody(), "owner", GitHubUser.class);
            return repo;
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubRepoNotFoundException("Failed to locate GitHub Repo: " + name, e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    public GitHubUser getOwnerOf(GitHubRepo repo) {
        try {
            return Nexus.JSON.read(Unirest.get(getRepoApiUrl(repo.getName())), GitHubUser.class);
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubRepoNotFoundException("Failed to locate GitHub Repo: " + repo.getName(), e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    public boolean isPrivate(GitHubRepo repo) {
        try {
            return Unirest.get(getRepoApiUrl(repo.getName())).asJson().getBody().getObject().getBoolean("private");
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubRepoNotFoundException("Failed to locate GitHub Repo: " + repo.getName(), e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    public GitHubIssue getIssue(GitHubRepo repo, int id) {
        return getIssue(repo.getName(), id);
    }

    public GitHubIssue getIssue(String name, int id) {
        try {
            return Nexus.JSON.read(Unirest.get(getIssuesUrl(name, id)), GitHubIssue.class);
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubRepoNotFoundException("Failed to locate GitHub Repo: " + name, e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    public GitHubHook[] getHooks(GitHubRepo repo) {
        return getHooks(repo.getName());
    }

    public GitHubHook[] getHooks(String name) {
        try {
            return Nexus.JSON.read(Unirest.get(getHooksUrl(name) + "?access_token=" + Nexus.getInstance().getConfig().getGitHubApiKey()), GitHubHook[].class);
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubRepoNotFoundException("Failed to locate GitHub Repo: " + name, e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    public GitHubHook getHook(GitHubRepo repo, int id) {
        return getHook(repo.getName(), id);
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
        return getHook(repo.getName(), name);
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
        return getCollaborators(repo.getName());
    }

    public GitHubUser[] getCollaborators(String name) {
        try {
            return Nexus.JSON.read(Unirest.get(getCollaboratorsUrl(name)), GitHubUser[].class);
        } catch (UnirestException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw new GitHubRepoNotFoundException("Failed to locate GitHub Repo: " + name, e);
            }
            throw new GitHubException("Error connecting to GitHub API! ", e);
        }
    }

    public void setIrcNotifications(GitHubRepo repo, GitHubEvent... events) {
        setIrcNotifications(repo.getRepoOwner().getLogin() + "/" + repo.getName(), events);
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
                Unirest.patch(getHooksUrl(repo) + "/" + hook.getId() + "?access_token=" + Nexus.getInstance().getConfig().getGitHubApiKey())
                        .header("accept", "application/json")
                        .header("content-type", "application/json")
                        .body("{\"events\":[" + s + "]}").asJson();
            } catch (UnirestException e) {
                throw new GitHubException("Could not connect to GitHub API!", e);
            }
        } else {
            throw new IrcHookNotFoundException("IRC Hook not found for GitHub Repo (" + repo + ")");
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
}