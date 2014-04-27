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

package com.dsh105.nexus;

import com.dsh105.nexus.command.CommandManager;
import com.dsh105.nexus.config.OptionsConfig;
import com.dsh105.nexus.hook.github.GitHub;
import com.dsh105.nexus.hook.github.GitHubEvent;
import com.dsh105.nexus.hook.jenkins.Jenkins;
import com.dsh105.nexus.listener.EventManager;
import com.dsh105.nexus.response.ResponseManager;
import com.dsh105.nexus.util.JsonUtil;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Nexus extends PircBotX {

    private static Nexus INSTANCE;
    public static Logger LOGGER = Logger.getLogger(Nexus.class.getName());
    public static JsonUtil JSON = new JsonUtil();
    public static String CONFIG_FILE_NAME = "options.txt";
    public static String ADMIN_CHANNEL = "#dsh106";
    private OptionsConfig config;
    private CommandManager commandManager;
    private ResponseManager responseManager;
    private Jenkins jenkins;
    private GitHub github;

    public static void main(String[] args) throws Exception {
        new Nexus();
    }

    public Nexus() {
        INSTANCE = this;
        this.registerLogger();
        config = new OptionsConfig();
        ADMIN_CHANNEL = config.getAdminChannel();
        commandManager = new CommandManager();
        commandManager.registerDefaults();
        responseManager = new ResponseManager();
        this.setName(this.getConfig().getNick());
        this.setLogin("Nexus");
        this.setVersion("Nexus");
        this.setVerbose(false);
        setAutoReconnectChannels(true);
        this.connect();
        this.identify(this.config.getAccountPassword());
        this.registerListeners();
        System.out.println(this.getConfig().getChannels());
        if (!this.config.getJenkinsUrl().isEmpty() && !this.config.getJenkinsToken().isEmpty()) {
            this.jenkins = new Jenkins();
        }

        if (!this.config.getGitHubApiKey().isEmpty()) {
            this.github = new GitHub();
        }
    }

    private void registerListeners() {
        this.getListenerManager().addListener(new EventManager());
    }

    private void registerLogger() {
        try {
            FileHandler handler = new FileHandler("Nexus.log");
            handler.setLevel(Level.ALL);
            handler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(handler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connect() {
        try {
            this.connect(this.config.getServer(), this.config.getPort(), this.config.getAccountPassword());
            for (String channel : this.config.getChannels()) {
                this.joinChannel(channel);
            }
        } catch (NickAlreadyInUseException e) {
            LOGGER.severe("That nickname is already in use!");
        } catch (IrcException | IOException ignored) {
        }
    }

    @Override
    public void joinChannel(String channel) {
        super.joinChannel(channel);
        this.saveChannels();
    }

    public void saveChannels() {
        this.config.clearChannels();
        for (Channel channel : this.getUserBot().getChannels()) {
            this.config.addChannel(channel.getName());
        }
        this.config.save();
    }

    @Override
    public void sendMessage(Channel chan, User target, String message) {
        this.sendMessage(chan, appendNick(target.getNick(), message));
    }

    public String appendNick(String nick, String message) {
        if (getConfig().appendNicks()) {
            message = "(" + nick + ") " + message;
        }
        return message;
    }

    public static Nexus getInstance() {
        return INSTANCE;
    }

    public OptionsConfig getConfig() {
        return config;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public Jenkins getJenkins() {
        return jenkins;
    }

    public GitHub getGithub() {
        return github;
    }

    public boolean isAdmin(User user) {
        return this.getChannel(this.getConfig().getAdminChannel()).getOps().contains(user) || this.getConfig().getAdmins().contains(user.getNick());
    }

    public ResponseManager getResponseManager() {
        return responseManager;
    }
}