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

public class TimeUtil {

    /**
     * Parses a String into a long value
     *
     * @param input the String to be parsed
     * @return parsed value
     */
    public static long parse(String input) {
        long result = 0;
        String number = "";
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isDigit(c)) {
                number += c;
            } else if (Character.isLetter(c) && !number.isEmpty()) {
                result += convert(Integer.parseInt(number), c);
                number = "";
            }
        }
        return result;
    }

    /**
     * Converts the given Integer into the specified time unit
     *
     * @param value the value to be converted
     * @param unit the specified time unit
     * @return converted value
     */
    private static long convert(int value, char unit) {
        switch(unit) {
            case 'd' : return value * 1000*60*60*24;
            case 'h' : return value * 1000*60*60;
            case 'm' : return value * 1000*60;
            case 's' : return value * 1000;
        }
        return 0;
    }
}