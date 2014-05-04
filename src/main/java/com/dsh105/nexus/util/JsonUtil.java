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

public class JSONUtil {

    public Gson gson = new Gson();
    public JsonParser parser = new JsonParser();

    public <T> T read(HttpRequest request, Class<T> type) throws UnirestException {
        return read(request.asJson().getRawBody(), type);
    }

    public <T> T read(HttpRequest request, String section, Class<T> type) throws UnirestException {
        return read(request.asJson().getRawBody(), section, type);
    }

    public <T> T read(InputStream input, Class<T> type) {
        return read(new BufferedReader(new InputStreamReader(input)), type);
    }

    public <T> T read(InputStream input, String section, Class<T> type) {
        return read(new BufferedReader(new InputStreamReader(input)), section, type);
    }

    public <T> T read(URLConnection con, Class<T> type) throws IOException {
        return read(con.getInputStream(), type);
    }

    public <T> T read(URLConnection con, String section, Class<T> type) throws IOException {
        return read(con.getInputStream(), section, type);
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