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
import com.dsh105.nexus.config.GitHubConfig;
import com.dsh105.nexus.command.module.general.RemindCommand;
import com.dsh105.nexus.config.NicksConfig;
import com.dsh105.nexus.config.OptionsConfig;
import com.dsh105.nexus.hook.github.GitHub;
import com.dsh105.nexus.hook.jenkins.Jenkins;
import com.dsh105.nexus.listener.EventManager;
import com.dsh105.nexus.response.ResponseManager;
import com.dsh105.nexus.util.JsonUtil;
import com.dsh105.nexus.util.ShortLoggerFormatter;
import com.mashape.unirest.http.Unirest;
import jline.console.ConsoleReader;
import jline.console.completer.FileNameCompleter;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.*;

public class Nexus extends PircBotX {

    private static Nexus INSTANCE;
    public static Logger LOGGER = Logger.getLogger(Nexus.class.getName());
    public static JsonUtil JSON = new JsonUtil();
    public static String CONFIG_FILE_NAME = "options.yml";
    private OptionsConfig config;
    private GitHubConfig githubConfig;
    private NicksConfig nicksConfig;
    private CommandManager commandManager;
    private ResponseManager responseManager;
    private Jenkins jenkins;
    private GitHub github;

    public static void main(String[] args) throws Exception {
        System.out.println("Starting up Nexus, please wait...");
        new Nexus();
    }

    public Nexus() {
        INSTANCE = this;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                endProcess();
            }
        });
        Unirest.setTimeouts(10000, 10000);

        this.registerLogger();

        LOGGER.info("Loading config files");
        config = new OptionsConfig();
        githubConfig = new GitHubConfig();
        nicksConfig = new NicksConfig();

        Unirest.setDefaultHeader("user-agent", getConfig().get("user-agent", "Nexus"));

        LOGGER.info("Registering event listeners");
        this.registerListeners();

        LOGGER.info("Registering commands");
        commandManager = new CommandManager();
        commandManager.registerDefaults();

        LOGGER.info("Preparing response manager");
        responseManager = new ResponseManager();

        this.setName(this.getConfig().getNick());
        this.setLogin(this.getConfig().getNick());
        this.setVersion(this.getConfig().getNick());
        this.setVerbose(false);
        this.setAutoReconnectChannels(true);
        if (this.config.getAccountPassword() != null && !this.config.getAccountPassword().isEmpty()) {
            this.identify(this.config.getAccountPassword());
        }
        this.connect();

        if (!this.config.getJenkinsUrl().isEmpty()) {
            LOGGER.info("Initiating Jenkins hook");
            this.jenkins = new Jenkins();
        }
        LOGGER.info("Initiating GitHub hook");
        this.github = new GitHub();
        RemindCommand remindCommand = this.getCommandManager().getModuleOfType(RemindCommand.class);
        if (remindCommand != null) {
            LOGGER.info("Loading saved reminders");
            remindCommand.loadReminders();
        }
        this.sendMessage(this.getChannel(this.getConfig().getAdminChannel()), "I'm back! ;D");

        for (Channel channel : this.getChannels()) {
            for (User u : channel.getUsers()) {

            }
        }
        LOGGER.info("Done! Nexus is ready!");

        ConsoleReader console = null;
        try {
            console = new ConsoleReader();
            console.addCompleter(new FileNameCompleter());
            console.setPrompt("> ");
            String line;
            PrintWriter out = new PrintWriter(System.out);

            while ((line = console.readLine("")) != null) {
                if (INSTANCE != null) {
                    if (line.equalsIgnoreCase("EXIT") || line.equalsIgnoreCase("END") || line.equalsIgnoreCase("STOP") || line.equalsIgnoreCase("QUIT")) {
                        endProcess();
                    }
                }
                out.flush();
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if (console != null) {
                try {
                    console.getTerminal().restore();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void endProcess() {
        if (INSTANCE != null) {
            LOGGER.info("Shutting down Nexus...");
            INSTANCE.saveAll();
            LOGGER.info("Waiting for outgoing queue");
            while (Nexus.getInstance().getOutgoingQueueSize() > 0);
            INSTANCE.shutdown(true);
            INSTANCE = null;
            System.exit(0);
        }
    }

    private void registerListeners() {
        this.getListenerManager().addListener(new EventManager());
    }

    private void registerLogger() {
        try {
            Logger root = Logger.getLogger("");
            root.setLevel(Level.INFO);
            Formatter formatter = new ShortLoggerFormatter();

            FileHandler handler = new FileHandler("Nexus.log", true);
            handler.setLevel(Level.ALL);
            handler.setFormatter(formatter);
            root.addHandler(handler);

            ConsoleHandler console = null;
            for (Handler h : root.getHandlers()) {
                if (h instanceof ConsoleHandler) {
                    console = (ConsoleHandler) h;
                    break;
                }
            }
            boolean registerWithRoot = false;
            if (console == null) {
                console = new ConsoleHandler();
                registerWithRoot = true;
            }
            console.setLevel(Level.INFO);
            console.setFormatter(formatter);
            if (registerWithRoot) {
                root.addHandler(console);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connect() {
        try {
            if (!this.config.isReady()) {
                LOGGER.info("Your config needs the 'ready' field to set to true.");
                System.exit(-1);
            }

            LOGGER.info("Attempting to connect to " + this.config.getServer());

            this.connect(this.config.getServer(), this.config.getPort(), this.config.getAccountPassword());
            for (String channel : this.config.getChannels()) {
                this.joinChannel(channel);
            }
        } catch (NickAlreadyInUseException e) {
            LOGGER.severe("That nickname is already in use!");
        } catch (IrcException | IOException ignored) {
        }
    }

    public void saveAll() {
        LOGGER.info("Saving config files");
        this.getConfig().save();
        this.getGitHubConfig().save();
        LOGGER.info("Saving channels");
        this.saveChannels();
        RemindCommand remindCommand = this.getCommandManager().getModuleOfType(RemindCommand.class);
        if (remindCommand != null) {
            LOGGER.info("Saving reminders");
            remindCommand.saveReminders();
        }
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

    public GitHubConfig getGitHubConfig() {
        return githubConfig;
    }

    public NicksConfig getNicksConfig() {
        return nicksConfig;
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
        return isChannelAdmin(user) || isNexusAdmin(user);
    }

    public boolean isChannelAdmin(User user) {
        return this.getChannel(this.getConfig().getAdminChannel()).getOps().contains(user);
    }

    public boolean isNexusAdmin(User user) {
        return this.getConfig().getAdmins().contains(user.getNick());
    }

    public ResponseManager getResponseManager() {
        return responseManager;
    }
}