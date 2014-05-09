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

import com.dsh105.nexus.Nexus;

import java.util.ArrayList;
import java.util.Map;

public class OptionsConfig extends YamlConfig {

    private ArrayList<String> channels = new ArrayList<>();
    private ArrayList<String> admins = new ArrayList<>();

    public OptionsConfig() {
        super(Nexus.CONFIG_FILE_NAME);
        this.setDefaults();
        this.load();
    }

    @Override
    public void setDefaults() {
        this.options.put("ready", false);
        this.options.put("server", "irc.esper.net");
        this.options.put("port", 5555);
        this.options.put("server-password", "");
        this.options.put("account-password", "");
        this.options.put("command-prefix", "\\");
        this.options.put("nick", "Nexus");
        this.options.put("admin-channel", "");
        this.options.put("append-nicks", true);
        this.options.put("jenkins-url", "change-me");
        this.options.put("jenkins-token-jobName", "change-me");
        this.options.put("response-chance", "");
        this.options.put("github-key-Nexus", "change-me");
        this.options.put("github-repo-repoName", "change-to-author");
        this.options.put("github-account-name", "");
        this.options.put("github-account-password", "");
        this.options.put("github-oauth-authorise-applink", "change-me");
        this.options.put("github-oauth-token-applink", "change-me");
        this.options.put("gist-account-name", "");
        this.options.put("gist-account-password", "");
        this.options.put("user-agent", "Nexus");
        //this.options.put("trello-key", "");
        if (this.getAdminChannel() != null && !this.getAdminChannel().isEmpty()) {
            channels.add(this.getAdminChannel());
        }
        admins.add("DSH105");
        this.channels = this.get("channels", channels);
        this.admins = this.get("admins", admins);
        this.options.put("channels", channels);
        this.options.put("admins", admins);
    }

    @Override
    public void loadData(Map<String, Object> loadedData) {
        super.loadData(loadedData);
        this.channels = this.get("channels", channels);
        this.admins = this.get("admins", admins);
        this.options.put("channels", channels);
        this.options.put("admins", admins);
    }

    @Override
    public void save() {
        this.set("channels", channels);
        this.set("admins", admins);
        super.save();
    }

    public String getServer() {
        return get("server", "irc.esper.net");
    }

    public int getPort() {
        return get("port", 5555);
    }

    public boolean isReady() {
        return get("ready", false);
    }

    public String getAccountPassword() {
        return get("account-password", "");
    }

    public String getServerPassword() {
        return get("server-password", "");
    }

    public String getCommandPrefix() {
        return get("command-prefix", "\\");
    }

    public String getNick() {
        return get("nick", "Nexus");
    }

    public String getAdminChannel() {
        return get("admin-channel", "");
    }

    public boolean appendNicks() {
        return get("append-nicks", true);
    }

    public String getJenkinsUrl() {
        return get("jenkins-url", "");
    }

    public int getResponseChance() {
        return get("response-chance", 5);
    }

    public String getGitHubApiKey(String userLogin) {
        return get("github-key-" + userLogin, "");
    }

    public void clearChannels() {
        this.channels.clear();
    }

    public void addChannel(String channel) {
        this.channels.add(channel);
    }

    public void removeChannel(String channel) {
        this.channels.remove(channel);
    }

    public ArrayList<String> getChannels() {
        return new ArrayList<>(channels);
    }

    public void clearAdmins() {
        this.channels.clear();
    }

    public void addAdmin(String user) {
        this.admins.add(user);
    }

    public void removeAdmin(String user) {
        this.admins.remove(user);
    }

    public ArrayList<String> getAdmins() {
        return new ArrayList<>(admins);
    }

    public String getGitHubAccountName() {
        return get("github-account-name", "");
    }

    public String getGitHubAccountPassword() {
        return get("github-account-password", "");
    }

    public String getGistAccountName() {
        return get("gist-account-name", "");
    }

    public String getGistAccountPassword() {
        return get("gist-account-password", "");
    }

    public String getTrelloApiKey() {
        return get("trello-key", "");
    }

    public String getNexusGitHubApiKey() {
        return getGitHubApiKey("Nexus");
    }

    public String getGitHubOauthAppAuthoriseLink() {
        return get("github-oauth-authorise-applink", "");
    }

    public String getGitHubOauthAppTokenLink() {
        return get("github-oauth-token-applink", "");
    }
}