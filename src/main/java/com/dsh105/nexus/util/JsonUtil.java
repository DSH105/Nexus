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
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

import java.io.*;
import java.net.URLConnection;

public class JsonUtil {

    private static Gson GSON = new Gson();
    private static JsonParser PARSER = new JsonParser();

    public static <T> T read(HttpRequest request, Class<T> type) throws UnirestException {
        return read(request.asJson().getRawBody(), type);
    }

    public static <T> T read(HttpRequest request, String section, Class<T> type) throws UnirestException {
        return read(request.asJson().getRawBody(), section, type);
    }

    public static <T> T read(InputStream input, Class<T> type) {
        return read(new BufferedReader(new InputStreamReader(input)), type);
    }

    public static <T> T read(InputStream input, String section, Class<T> type) {
        return read(new BufferedReader(new InputStreamReader(input)), section, type);
    }

    public static <T> T read(URLConnection con, Class<T> type) throws IOException {
        return read(con.getInputStream(), type);
    }

    public static <T> T read(URLConnection con, String section, Class<T> type) throws IOException {
        return read(con.getInputStream(), section, type);
    }

    public static <T> T read(Reader reader, String section, Class<T> type) {
        return read(PARSER.parse(reader).getAsJsonObject().get(section), type);
    }

    public static <T> T read(Reader reader, Class<T> type) {
        return GSON.fromJson(reader, type);
    }

    public static <T> T read(JsonElement reader, Class<T> type) {
        return GSON.fromJson(reader, type);
    }

    public static Gson getGson() {
        return GSON;
    }

    public static JsonParser getParser() {
        return PARSER;
    }
}