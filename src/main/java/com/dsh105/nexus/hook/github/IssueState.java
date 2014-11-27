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

import org.pircbotx.Colors;

public enum IssueState {

    OPEN("open", Colors.GREEN, Colors.UNDERLINE),
    CLOSED("closed", Colors.RED, Colors.UNDERLINE);

    private String ident;
    private String[] colours;

    IssueState(String ident, String... colours) {
        this.ident = ident;
        this.colours = colours;
    }

    public static IssueState getByIdent(String ident) {
        for (IssueState s : IssueState.values()) {
            if (s.ident.equals(ident)) {
                return s;
            }
        }
        return null;
    }

    public String format(String toFormat) {
        StringBuilder builder = new StringBuilder();
        for (String s : colours) {
            builder.append(s);
        }
        return builder.append(toFormat).toString();
    }

    public String[] getColours() {
        return colours;
    }

    public String getIdent() {
        return ident;
    }
}