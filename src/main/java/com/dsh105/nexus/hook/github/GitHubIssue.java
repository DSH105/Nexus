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

import com.google.gson.annotations.SerializedName;

public class GitHubIssue {

    protected GitHubRepo repo;

    @SerializedName("html_url")
    private String url;

    @SerializedName("number")
    private int number;

    @SerializedName("title")
    private String title;

    @SerializedName("body")
    private String body;

    @SerializedName("state")
    private String state;

    @SerializedName("comments")
    private int comments;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("closed_at")
    private String closedAt;

    protected String repoFullName;
    protected GitHubUser reportedBy;

    public GitHubRepo getRepo() {
        return repo;
    }

    public GitHubUser getReporter() {
        if (reportedBy == null) {
            reportedBy = GitHub.getGitHub().getReporterOf(this);
        }
        return reportedBy;
    }

    public String getUrl() {
        return url;
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getState() {
        return state;
    }

    public int getComments() {
        return comments;
    }

    public String getDateCreated() {
        if (createdAt != null) {
            return createdAt.split("T")[0];
        }
        return null;
    }

    public String getDateUpdated() {
        if (updatedAt != null) {
            return updatedAt.split("T")[0];
        }
        return null;
    }

    public String getDateClosed() {
        if (closedAt != null) {
            return closedAt.split("T")[0];
        }
        return null;
    }
}