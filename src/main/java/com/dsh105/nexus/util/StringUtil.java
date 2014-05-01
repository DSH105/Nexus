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

package com.dsh105.nexus.util;

public class StringUtil {

    public static boolean isInt(String string) {
        try {
            Integer.parseInt(string);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    public static String capitalise(String s) {
        String[] parts = s.split(" ");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].substring(0, 1) + parts[i].substring(1);
        }
        return combineSplit(0, parts, " ");
    }

    public static String[] separate(int startIndex, String... string) {
        if (startIndex >= string.length || string.length <= 0) {
            return new String[0];
        }
        String[] str = new String[string.length - startIndex];
        for (int i = startIndex; i < string.length; i++) {
            str[i] = string[i];
        }
        return str;
    }

    public static String combineSplit(int startIndex, String[] string, String separator) {
        if (string == null || startIndex >= string.length) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();
            for (int i = startIndex; i < string.length; i++) {
                builder.append(string[i]);
                builder.append(separator);
            }
            builder.delete(builder.length() - separator.length(), builder.length());
            return builder.toString();
        }
    }

    public static String[] splitArgs(int startIndex, String[] string, String separator) {
        String combined = combineSplit(startIndex, string, separator);
        if (combined.isEmpty()) {
            return new String[0];
        }
        return combined.split(separator);
    }
}