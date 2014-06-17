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

import org.pircbotx.Colors;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

public class ColorUtil {

    public static String[] serialise(String... toSerialise) {
        ArrayList<String> serialised = new ArrayList<>();
        ArrayList<String> colours = validColours();
        for (int index = 0; index < toSerialise.length; index++) {
            for (int i = 0; i < colours.size(); i++) {
                serialised.add(index, toSerialise[index].replace(colours.get(i), "&" + i));
            }
        }
        return serialised.toArray(new String[0]);
    }

    public static String[] deserialise(String... toDeserialise) {
        ArrayList<String> deserialised = new ArrayList<>();
        ArrayList<String> colours = validColours();
        for (int index = 0; index < toDeserialise.length; index++) {
            for (int i = 0; i < colours.size(); i++) {
                deserialised.add(index, toDeserialise[index].replace(colours.get(i), "&" + i));
            }
        }
        return deserialised.toArray(new String[0]);
    }

    private static ArrayList<String> validColours() {
        ArrayList<String> colours = new ArrayList<>();
        for (Field field : Colors.class.getDeclaredFields()) {
            if (field.getType().equals(String.class)) {
                try {
                    colours.add((String) field.get(null));
                } catch (IllegalAccessException ignored) {

                }
            }
        }
        return colours;
    }
}