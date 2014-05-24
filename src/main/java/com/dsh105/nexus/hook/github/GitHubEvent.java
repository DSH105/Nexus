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

public enum GitHubEvent {

    ALL("*"),
    COMMENT_COMMIT("commit_comment"),
    CREATE,
    DELETE,
    DEPLOYMENT_STATUS,
    DEPLOYMENT,
    FORK,
    WIKI_UPDATE("gollum"),
    COMMENT_ISSUE("issue_comment"),
    ISSUE("issues"),
    MEMBER,
    PAGE_BUILD,
    REPOSITORY_STATUS("public"),
    PULL_REQUEST_REVIEW_COMMENT,
    PULL_REQUEST,
    PUSH,
    RELEASE,
    STATUS,
    TEAM_ADD,
    WATCH;

    private String jsonName;

    GitHubEvent() {
        this.jsonName = this.toString().toLowerCase();
    }

    GitHubEvent(String jsonName) {
        this.jsonName = jsonName;
    }

    public static GitHubEvent getByJsonName(String name) {
        for (GitHubEvent event : GitHubEvent.values()) {
            if (event.getJsonName().equalsIgnoreCase(name)) {
                return event;
            }
        }
        return null;
    }

    public String getJsonName() {
        return jsonName;
    }
}