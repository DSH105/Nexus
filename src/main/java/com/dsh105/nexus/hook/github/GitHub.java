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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class GitHub {

    public GitHubRepo getRepo(String name) {
        GitHubRepo repo = null;
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(getApiUrl(name)).openConnection();
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

    public String getApiUrl(String repoName) {
        return "https://api.github.com/repos/" + repoName;
    }

    public String getForksUrl(String repoName) {
        return getApiUrl(repoName) + "/forks";
    }

    public String getEventsUrl(String repoName) {
        return getApiUrl(repoName) + "/events";
    }

    public String getIssueEventsUrl(String repoName) {
        return getApiUrl(repoName) + "/issues/events";
    }

    public String getIssuesUrl(String repoName, int id) {
        return getApiUrl(repoName) + "/issues/" + id;
    }

    public String getPullsUrl(String repoName, int id) {
        return getApiUrl(repoName) + "/pulls/" + id;
    }

    public String getContributorsUrl(String repoName) {
        return getApiUrl(repoName) + "/contributors";
    }
}