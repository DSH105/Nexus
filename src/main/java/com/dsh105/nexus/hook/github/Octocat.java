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

public class Octocat {
    private String name;
    private String page;
    private String image;
    private String author;
    private int number;
    private String authorURL;
    private String authorAvatar;

    public String getName() {
        return name;
    }

    public String getPage() {
        return page;
    }

    public String getImage() {
        return image;
    }

    public String getAuthor() {
        return author;
    }

    public int getNumber() {
        return number;
    }

    public String getAuthorURL() {
        return authorURL;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }
}
