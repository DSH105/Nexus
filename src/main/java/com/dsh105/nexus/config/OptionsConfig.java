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

public class OptionsConfig extends YamlConfig {

    private ArrayList<String> channels = new ArrayList<>();
    private ArrayList<String> admins = new ArrayList<>();

    public OptionsConfig() {
        super(Nexus.CONFIG_FILE_NAME);
        channels = new ArrayList<>();
        this.setDefaults();
        this.load();
    }

    @Override
    public void setDefaults() {
        System.out.println(Nexus.ADMIN_CHANNEL);
        channels.add(Nexus.ADMIN_CHANNEL);
        admins.add("DSH105");
        this.options.put("server", "irc.esper.net");
        this.options.put("port", 5555);
        this.options.put("server-password", "");
        this.options.put("account-password", "");
        this.options.put("command-prefix", ";");
        this.options.put("nick", "Nexus");
        this.options.put("admin-channel", Nexus.ADMIN_CHANNEL);
        this.options.put("append-nicks", true);
        this.options.put("jenkins-url", "");
        this.options.put("jenkins-token", "");
        this.options.put("response-chance", "");
        this.options.put("github-key", "");
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

    public String getAccountPassword() {
        return get("account-password", "");
    }

    public String getServerPassword() {
        return get("server-password", "");
    }

    public String getCommandPrefix() {
        return get("command-prefix", ";");
    }

    public String getNick() {
        return get("nick", "Nexus");
    }

    public String getAdminChannel() {
        return get("admin-channel", Nexus.ADMIN_CHANNEL);
    }

    public boolean appendNicks() {
        return get("append-nicks", true);
    }

    public String getJenkinsUrl() {
        return get("jenkins-url", "");
    }

    public String getJenkinsToken() {
        return get("jenkins-token", "");
    }

    public int getResponseChance() {
        return get("response-chance", 5);
    }

    public String getGitHubApiKey() {
        return get("github-key", "");
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
}