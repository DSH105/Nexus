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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class StringUtil {

    private static final String EMPTY = "";

    public static String removePing(String nick) {
        return nick == null ? null : (nick.substring(0, 1) + '\u200b' + (nick.length() >= 2 ? nick.substring(1, nick.length()) : ""));
    }

    /**
     * Tests if the given String is an Integer
     *
     * @param string the String to be checked
     * @return true if Integer
     */
    public static boolean isInt(String string) {
        try {
            Integer.parseInt(string);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    /**
     * Tests if the given String is an Double
     *
     * @param string the String to be checked
     * @return true if Double
     */
    public static boolean isDouble(String string) {
        try {
            Double.parseDouble(string);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    /**
     * Capitalizes the first letter of a String
     *
     * @param string the String to be capitalized
     * @return capitalized String
     */
    public static String capitalise(String string) {
        String[] parts = string.split(" ");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1);
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

    /**
     * Capitalizes the first letter of a String
     *
     * @param startIndex the index to start at
     * @param string     the String to be split
     * @param separator  the value to split the string by
     * @return capitalized String
     */
    public static String[] splitArgs(int startIndex, String[] string, String separator) {
        String combined = combineSplit(startIndex, string, separator);
        if (combined.isEmpty()) {
            return new String[0];
        }
        return combined.split(separator);
    }

    /**
     * Joins the elements of an iterator together separated by the given separator.
     *
     * @param iterator
     * @param separator
     * @return
     */
    public static String join(Iterator iterator, char separator) {
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return EMPTY;
        }
        Object first = iterator.next();
        if (!iterator.hasNext()) {
            return first.toString();
        }

        StringBuilder buf = new StringBuilder(256);
        if (first != null) {
            buf.append(first);
        }

        while (iterator.hasNext()) {
            buf.append(separator);
            Object obj = iterator.next();
            if (obj != null) {
                buf.append(obj);
            }
        }

        return buf.toString();
    }

    /**
     * Joins the elements of an iterator together separated by the given separator.
     *
     * @param iterator
     * @param separator
     * @return
     */
    public static String join(Iterator iterator, String separator) {
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return EMPTY;
        }
        Object first = iterator.next();
        if (!iterator.hasNext()) {
            return first.toString();
        }

        StringBuilder buf = new StringBuilder(256);
        if (first != null) {
            buf.append(first);
        }

        while (iterator.hasNext()) {
            if (separator != null) {
                buf.append(separator);
            }
            Object obj = iterator.next();
            if (obj != null) {
                buf.append(obj);
            }
        }
        return buf.toString();
    }

    /**
     * Joins the elements of a collection together separated by the given separator.
     *
     * @param collection
     * @param separator
     * @return
     */
    public static String join(Collection collection, char separator) {
        if (collection == null) {
            return null;
        }
        return join(collection.iterator(), separator);
    }

    /**
     * Joins the elements of a collection together separated by the given separator.
     *
     * @param collection
     * @param separator
     * @return
     */
    public static String join(Collection collection, String separator) {
        if (collection == null) {
            return null;
        }
        return join(collection.iterator(), separator);
    }

    /**
     * Joins the elements of an array together separated by the given separator.
     *
     * @param array
     * @param separator
     * @return
     */
    public static String join(Object[] array, char separator) {
        if (array == null) {
            return null;
        }
        return join(Arrays.asList(array), separator);
    }

    /**
     * Joins the elements of an array together separated by the given separator.
     *
     * @param array
     * @param separator
     * @return
     */
    public static String join(Object[] array, String separator) {
        if (array == null) {
            return null;
        }
        return join(Arrays.asList(array), separator);
    }
}