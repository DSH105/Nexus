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

package com.dsh105.nexus.config;

import java.util.HashMap;
import java.util.Map;

public class GitHubConfig extends YamlConfig {

    private HashMap<String, String> nicks = new HashMap<>();

    public GitHubConfig() {
        super("github-config.yml");
        this.setDefaults();
        this.load();
    }

    @Override
    public void setDefaults() {
        this.options.put("github-key-Nexus", "change-me");
        this.options.put("github-repo-repoName", "change-to-author");
        this.options.put("github-oauth-client-id", "change-me");
        this.options.put("github-oauth-client-secret", "change-me");
        this.options.put("github-oauth-scope", "change-me");
        this.options.put("github-oauth-state", "change-me");
        this.options.put("gist-account-name", "");
        this.options.put("gist-account-password", "");
    }

    @Override
    public void loadData(Map<String, Object> loadedData) {
        super.loadData(loadedData);
        this.nicks = this.get("nicks", nicks);
        this.options.put("nicks", nicks);
    }

    @Override
    public void save() {
        this.set("nicks", nicks);
        super.save();
    }

    public HashMap<String, String> getAllStoredNicks() {
        return new HashMap<>(nicks);
    }

    public String getAccountNameFor(String nick) {
        return nicks.get(nick);
    }

    public void storeNick(String nick, String account) {
        nicks.put(nick, account);
    }

    public String getGitHubApiKey(String userLogin) {
        return get("github-key-" + userLogin, "");
    }

    public String getGistAccountName() {
        return get("gist-account-name", "");
    }

    public String getGistAccountPassword() {
        return get("gist-account-password", "");
    }

    public String getNexusGitHubApiKey() {
        return getGitHubApiKey("Nexus");
    }

    public String getGitHubOauthAppClientId() {
        return get("github-oauth-client-id", "");
    }

    public String getGitHubOauthAppClientSecret() {
        return get("github-oauth-client-secret", "");
    }

    public String getGitHubOauthAppScope() {
        return get("github-oauth-scope", "");
    }

    public String getGitHubOauthAppState() {
        return get("github-oauth-state", "");
    }
}