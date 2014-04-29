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
import com.dsh105.nexus.exception.GitHubException;
import com.dsh105.nexus.exception.GitHubRepoNotFoundException;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class GitHubRepo {

    private String name;
    private String html_url;
    private String description;
    private String homepage;
    private String language;
    private boolean has_wiki;
    private int forks_count;
    private int open_issues_count;
    private int watchers_count;
    private String created_at;
    private String updated_at;
    private String pushed_at;

    protected GitHubUser repoOwner;
    protected GitHubUser[] collaborators;
    protected boolean isPrivate;

    public GitHubRepo() {
        collaborators = getCollaborators();
    }

    public GitHubUser getRepoOwner() {
        if (repoOwner == null) {
            repoOwner = Nexus.getInstance().getGithub().getOwnerOf(this);
        }
        return repoOwner;
    }

    public GitHubUser[] getCollaborators() {
        if (collaborators == null) {
            collaborators = Nexus.getInstance().getGithub().getCollaborators(this);
        }
        return collaborators;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return html_url;
    }

    public String getDescription() {
        return description;
    }

    public String getHomepage() {
        return homepage;
    }

    public String getLanguage() {
        return language;
    }

    public boolean hasWiki() {
        return has_wiki;
    }

    public int getForksCount() {
        return forks_count;
    }

    public int getOpenIssuesCount() {
        return open_issues_count;
    }

    public String getDateCreated() {
        return created_at;
    }

    public String getDateLastUpdated() {
        return updated_at;
    }

    public String getDateLastPushedTo() {
        return pushed_at;
    }

}