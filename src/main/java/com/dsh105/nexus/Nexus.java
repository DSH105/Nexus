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
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Nexus extends PircBotX {

    private static Nexus INSTANCE;
    public static Logger LOGGER = Logger.getLogger(Nexus.class.getName());
    protected static String CONFIG_FILE_NAME = "options.yml";
    public static String ADMIN_CHANNEL = "#nexus";
    private Config config;
    private CommandManager commandManager;

    public static void main(String[] args) throws Exception {
        new Nexus();
    }

    public Nexus() {
        INSTANCE = this;
        this.registerLogger();
        config = Config.load(CONFIG_FILE_NAME);
        commandManager = new CommandManager();
        commandManager.registerDefaults();
        this.setName("Nexus");
        this.connect();
        this.identify(this.config.getAccountPassword());
        this.registerListeners();
    }

    private void registerListeners() {
        this.getListenerManager().addListener(this.commandManager);
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

    public void saveChannels() {
        this.config.getChannels().clear();
        for (Channel channel : this.getUserBot().getChannels()) {
            this.config.getChannels().add(channel.getName());
        }
        this.config.save();
    }

    public static Nexus getInstance() {
        return INSTANCE;
    }

    public Config getConfig() {
        return config;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}