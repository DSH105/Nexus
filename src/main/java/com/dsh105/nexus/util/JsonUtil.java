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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLConnection;

public class JsonUtil {

    private Gson gson = new Gson();
    private JsonParser parser = new JsonParser();

    public <T> T read(URLConnection con, Class<T> type) {
        try {
            return read(new BufferedReader(new InputStreamReader(con.getInputStream())), type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T read(URLConnection con, String section, Class<T> type) {
        try {
            return read(new BufferedReader(new InputStreamReader(con.getInputStream())), section, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T read(Reader reader, String section, Class<T> type) {
        return read(parser.parse(reader).getAsJsonObject().get(section), type);
    }

    public <T> T read(Reader reader, Class<T> type) {
        return gson.fromJson(reader, type);
    }

    public <T> T read(JsonElement reader, Class<T> type) {
        return gson.fromJson(reader, type);
    }
}