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

import com.dsh105.nexus.Nexus;
import org.apache.commons.lang3.Validate;
import org.pircbotx.Channel;
import org.pircbotx.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class StringUtil {

    private static final String EMPTY = "";

    public static String munge(String nick) {
        return nick == null ? null : (nick.substring(0, 1) + '\u200b' + (nick.length() >= 2 ? nick.substring(1, nick.length()) : ""));
    }

    public static String mungeMessage(String destination, String entireMessage) {
        Channel channel = Nexus.getInstance().getChannel(destination);
        if (channel == null) {
            return entireMessage.replace(destination, StringUtil.munge(destination)).replace(destination.toLowerCase(), StringUtil.munge(destination.toLowerCase()));
        }

        for (User user : channel.getUsers()) {
            entireMessage = entireMessage.replace(user.getNick(), StringUtil.munge(user.getNick())).replace(user.getNick().toLowerCase(), StringUtil.munge(user.getNick().toLowerCase()));
        }
        return entireMessage;
    }

    /**
     * Attempts to convert a string into an integer value using Regex
     *
     * @param string the String to be checked
     * @return Integer.MIN_VALUE if unable to convert
     * @throws java.lang.NumberFormatException
     */
    public static int toInteger(String string) throws NumberFormatException{
        try {
            return Integer.parseInt(string.replaceAll("[^\\d]", ""));
        } catch (NumberFormatException e) {
            throw new NumberFormatException(string + " isn't a number!");
        }
    }

    /**
     * Attempts to convert a string into an double value using Regex
     *
     * @param string the String to be checked
     * @return Double.MIN_VALUE if unable to convert
     * @throws java.lang.NumberFormatException
     */
    public static double toDouble(String string) {
        try {
            return Double.parseDouble(string.replaceAll(".*?([\\d.]+).*", "$1"));
        } catch (NumberFormatException e) {
            throw new NumberFormatException(string + " isn't a number!");
        }
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

    /**
     * Builds a sentence list from an array of strings.
     * Example: {"one", "two", "three"} returns "one, two and three".
     *
     * @param words The string array to build into a list,
     * @return String representing the list.
     */
    public static String buildSentenceList(String[] words) {
        Validate.notEmpty(words);
        if (words.length == 1) {
            return words[0];
        } else if (words.length == 2) {
            return combineSplit(0, words, " and ");
        } else {
            // This is where the fun starts!
            String[] initial = Arrays.copyOfRange(words, 0, words.length - 1);
            String list = combineSplit(0, initial, ", ");
            list += " and " + words[words.length - 1];
            return list;
        }
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
