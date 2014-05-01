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

    @SerializedName("html_url")
    private String url;

    @SerializedName("number")
    private int number;

    @SerializedName("title")
    private int title;

    @SerializedName("state")
    private int state;

    @SerializedName("comments")
    private int comments;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("closed_at")
    private String closedAt;

    public String getUrl() {
        return url;
    }

    public int getNumber() {
        return number;
    }

    public int getTitle() {
        return title;
    }

    public int getState() {
        return state;
    }

    public int getComments() {
        return comments;
    }

    public String getDateCreated() {
        return createdAt;
    }

    public String getDateUpdated() {
        return updatedAt;
    }

    public String getDateClosed() {
        return closedAt;
    }
}