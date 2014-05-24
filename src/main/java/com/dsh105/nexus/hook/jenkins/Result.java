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

package com.dsh105.nexus.hook.jenkins;

import org.pircbotx.Colors;

public enum Result {

    SUCCESS("blue", Colors.GREEN, Colors.UNDERLINE),
    UNSTABLE("yellow", Colors.OLIVE, Colors.UNDERLINE),
    FAILURE("red", Colors.RED, Colors.UNDERLINE),
    NOT_BUILT("notbuilt", Colors.LIGHT_GRAY, Colors.UNDERLINE),;

    private String ident;
    private String[] colours;

    Result(String ident, String... colours) {
        this.ident = ident;
        this.colours = colours;
    }

    public String format(String toFormat) {
        StringBuilder builder = new StringBuilder();
        for (String s : getColours()) {
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

    public static Result getByIdent(String ident) {
        for (Result r : Result.values()) {
            if (r.getIdent().equals(ident)) {
                return r;
            }
        }
        return null;
    }
}