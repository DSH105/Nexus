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
import com.dsh105.nexus.exception.general.DateParseException;
import com.dsh105.nexus.util.TimeUtil;
import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.ParseException;

public class GitHubRepo {

    @SerializedName("name")
    private String name;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("html_url")
    private String url;

    @SerializedName("description")
    private String description;

    @SerializedName("homepage")
    private String homepage;

    @SerializedName("has_wiki")
    private boolean hasWiki;

    @SerializedName("forks_count")
    private int forks;

    @SerializedName("open_issues_count")
    private int openIssues;

    @SerializedName("watchers_count")
    private int watchers;

    @SerializedName("stargazers_count")
    private int stargazers;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("pushed_at")
    private String pushedAt;

    @SerializedName("private")
    protected boolean isPrivate;

    protected GitHubUser repoOwner;
    protected GitHubUser[] collaborators;
    protected GitHubUser[] contributors;
    protected GitHubLanguage[] languages;
    protected String userLoginForAccessToken;

    public GitHubUser getRepoOwner() {
        return repoOwner;
    }

    public GitHubUser[] getCollaborators() {
        return collaborators;
    }

    public GitHubUser[] getContributors() {
        return contributors;
    }

    public GitHubLanguage[] getLanguages() {
        return languages;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public String getHomepage() {
        return homepage;
    }

    public boolean hasWiki() {
        return hasWiki;
    }

    public int getForksCount() {
        return forks;
    }

    public int getOpenIssuesCount() {
        return openIssues;
    }

    public int getStargazers() {
        return stargazers;
    }

    public String getDateCreated() {
        if (createdAt != null) {
            return TimeUtil.parseGitHubDate(createdAt);
        }
        return null;
    }

    public String getDateLastUpdated() {
        if (updatedAt != null) {
            return TimeUtil.parseGitHubDate(updatedAt);
        }
        return null;
    }

    public String getDateLastPushedTo() {
        if (pushedAt != null) {
            return TimeUtil.parseGitHubDate(pushedAt);
        }
        return null;
    }

}