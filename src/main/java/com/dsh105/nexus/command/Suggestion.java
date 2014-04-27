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

package com.dsh105.nexus.command;

public class Suggestion {

    private String message;
    private String suggestions;

    public Suggestion(String message, String... possibleSuggestions) {
        this.message = message;
        for (String s : possibleSuggestions) {
            if (message.startsWith(s)) {
                suggestions += suggestions.isEmpty() ? s : ", " + s;
            }
        }
    }

    public String getMessage() {
        return message;
    }

    public String getSuggestions() {
        return suggestions;
    }
}