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

public class GitHubIssue {

    private String html_url;
    private int number;
    private int title;
    private int state;
    private int comments;
    private String created_at;
    private String updated_at;
    private String closed_at;

    public String getUrl() {
        return html_url;
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
        return created_at;
    }

    public String getDateUpdated() {
        return updated_at;
    }

    public String getDateClosed() {
        return closed_at;
    }
}