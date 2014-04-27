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
import com.dsh105.nexus.exception.IrcHookNotFoundException;
import com.dsh105.nexus.hook.github.gist.Gist;
import com.dsh105.nexus.hook.github.gist.GistFile;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GitHub {

    public String createGist(Exception e) {
        PrintWriter writer = new PrintWriter(new StringWriter());
        e.printStackTrace(writer);
        Gist gist = new Gist(new GistFile(writer.toString()));
        return gist.create();
    }

    public GitHubRepo getRepo(String name) {
        GitHubRepo repo = null;
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(getRepoApiUrl(name)).openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setUseCaches(false);
            repo = Nexus.JSON.read(con, GitHubRepo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return repo;
    }

    public GitHubIssue getIssue(String name, int id) {
        GitHubIssue issue = null;
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(getIssuesUrl(name, id)).openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setUseCaches(false);
            issue = Nexus.JSON.read(con, GitHubIssue.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return issue;
    }

    public GitHubHook[] getHooks(String name) {
        GitHubHook[] hooks= null;
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(getHooksUrl(name)).openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setUseCaches(false);
            con.setRequestProperty("Authorization", Nexus.getInstance().getConfig().getGitHubApiKey());
            hooks = Nexus.JSON.read(con, GitHubHook[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hooks;
    }

    public void setIrcNotifications(String githubRepoName, GitHubEvent... events) {
        GitHubHook hook = null;
        for (GitHubHook h : getHooks(githubRepoName)) {
            if (h.getName().equals("irc")) {
                hook = h;
            }
        }
        if (hook != null) {
            ArrayList<String> jsonEvents = new ArrayList<>();
            for (GitHubEvent event : GitHubEvent.values()) {
                jsonEvents.add(event.getJsonName());
            }
            HashMap<String, ArrayList<String>> map = new HashMap<>();
            map.put("events", jsonEvents);
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(getHooksUrl(githubRepoName) + "/" + hook.getId()).openConnection();
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                con.setUseCaches(false);
                con.setRequestMethod("PATCH");
                con.setRequestProperty("Content-type", "application/json");
                con.setRequestProperty("Authorization", Nexus.getInstance().getConfig().getGitHubApiKey());
                JsonObject eventsJson = new JsonObject();
                eventsJson.addProperty("events", Arrays.asList(events).toString());
                con.connect();
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(eventsJson.toString());
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IrcHookNotFoundException();
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
}