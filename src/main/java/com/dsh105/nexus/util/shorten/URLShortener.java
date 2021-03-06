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

package com.dsh105.nexus.util.shorten;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.exception.general.GenericUrlConnectionException;
import com.dsh105.nexus.util.JsonUtil;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class URLShortener {

    public static String shorten(String longUrl) {
        Nexus.LOGGER.info("Shortening URL via goo.gl (" + longUrl + ")");
        try {
            HttpResponse<JsonNode> response = Unirest.post("https://www.googleapis.com/urlshortener/v1/url")
                    .header("content-type", "application/json")
                    .body("{\"longUrl\": \"" + longUrl + "\"}").asJson();
            return JsonUtil.read(response.getRawBody(), ShortUrl.class).getId();
        } catch (UnirestException e) {
            throw new GenericUrlConnectionException("Failed to shorten URL: " + longUrl, e);
        }
    }

    public static String shortenGit(String longUrl) {
        Nexus.LOGGER.info("Shortening URL via git.io (" + longUrl + ")");
        try {
            HttpResponse<String> response = Unirest.post("http://git.io")
                    .field("url", longUrl)
                    .asString();
            return response.getHeaders().get("location").get(0);
        } catch (Exception e) {
            Nexus.LOGGER.severe("Failed to shorten URL via git.io: " + longUrl);
            return shorten(longUrl);
        }
    }
}