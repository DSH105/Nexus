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
import org.pircbotx.Colors;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

public class ColorUtil {

    private static ArrayList<String> COLOURS;

    public static String serialise(String toSerialise) {
        return StringUtil.combineSplit(0, serialise(new String[] {toSerialise}), " ");
    }

    public static String[] serialise(String[] toSerialise) {
        ArrayList<String> serialised = new ArrayList<>();
        ArrayList<String> colours = validColours();
        for (int index = 0; index < toSerialise.length; index++) {
            String current = toSerialise[index];
            for (int i = colours.size(); i >= 0; i--) {
                current = current.replace(colours.get(i), "&" + i);
            }
            serialised.add(index, Colors.removeFormattingAndColors(current));
        }
        return serialised.toArray(new String[0]);
    }

    public static String deserialise(String toDeserialise) {
        return StringUtil.combineSplit(0, deserialise(new String[] {toDeserialise}), " ");
    }

    public static String[] deserialise(String[] toDeserialise) {
        ArrayList<String> deserialised = new ArrayList<>();
        ArrayList<String> colours = validColours();
        for (int index = 0; index < toDeserialise.length; index++) {
            String current = toDeserialise[index];
            for (int i = colours.size(); i >= 0; i--) {
                current = current.replace("&" + i, colours.get(i));
            }
            deserialised.add(index, current);
        }
        return deserialised.toArray(new String[0]);
    }

    public static ArrayList<String> validColours() {
        if (COLOURS == null || COLOURS.isEmpty()) {
            COLOURS = new ArrayList<>();
            for (Field field : Colors.class.getDeclaredFields()) {
                try {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    COLOURS.add((String) field.get(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return COLOURS;
    }
}