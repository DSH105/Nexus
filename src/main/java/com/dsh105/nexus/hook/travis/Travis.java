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

package com.dsh105.nexus.hook.travis;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.exception.github.*;
import com.dsh105.nexus.exception.travis.TravisAPIKeyInvalidException;
import com.dsh105.nexus.hook.github.*;
import com.dsh105.nexus.hook.github.gist.Gist;
import com.dsh105.nexus.hook.github.gist.GistFile;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class Travis {

    public static String API_URL = "https://api.travis-ci.org";
    public static String ACCOUNTS_API_URL = "/accounts";

    public static Travis getTravis() {
        //return Nexus.getInstance().getTravis();
        return null;
    }

    public String getAccessToken(String userLogin) {
        String githubToken = GitHub.getGitHub().getAccessToken(userLogin, true);
        try {
            return Unirest.post(API_URL + "/auth/github").field("github_token", githubToken).asJson().getBody().getObject().getString("access_token");
        } catch (UnirestException e) {
            throw new TravisAPIKeyInvalidException("Please provide a GitHub API key (via the ghkey command) to access the Travis integration.");
        }
    }

    public HttpResponse<JsonNode> makeRequest(String urlPath, String userLogin) throws UnirestException {
        return makeRequest(urlPath, userLogin, false);
    }

    protected HttpResponse<JsonNode> makeRequest(String urlPath, String userLogin, boolean assumeAccess) throws UnirestException {
        Nexus.LOGGER.info("Connecting to " + urlPath + " with ACCESS_TOKEN of " + userLogin);
        String accessToken = getAccessToken(userLogin);
        HttpResponse<JsonNode> response = Unirest.get(urlPath).header("accept", "application/vnd.travis-ci.2+json").header("Authorization", "token " + accessToken).asJson();
        if (!assumeAccess) {
            if (response.getBody().toString().equalsIgnoreCase("no access token supplied")) {
                throw new TravisAPIKeyInvalidException("Please provide a GitHub API key (via the ghkey command) to access the Travis integration.");
            }
        }
        return response;
    }
}
