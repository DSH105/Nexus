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
import com.dsh105.nexus.command.module.general.RemindCommand;
import com.dsh105.nexus.config.GitHubConfig;
import com.dsh105.nexus.config.NicksConfig;
import com.dsh105.nexus.config.OptionsConfig;
import com.dsh105.nexus.hook.github.GitHub;
import com.dsh105.nexus.hook.jenkins.Jenkins;
import com.dsh105.nexus.listener.EventManager;
import com.dsh105.nexus.response.ResponseManager;
import com.dsh105.nexus.util.JsonUtil;
import com.dsh105.nexus.util.ShortLoggerFormatter;
import com.mashape.unirest.http.Unirest;
import org.ocpsoft.prettytime.PrettyTime;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.exception.IrcException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.*;

public class Nexus extends PircBotX {

    private static Nexus INSTANCE;
    public static Logger LOGGER = Logger.getLogger(Nexus.class.getName());
    public static JsonUtil JSON = new JsonUtil();
    public static PrettyTime PRETTY_TIME = new PrettyTime();
    public static String CONFIG_FILE_NAME = "options.yml";
    private OptionsConfig config;
    private GitHubConfig githubConfig;
    private NicksConfig nicksConfig;
    private CommandManager commandManager;
    private ResponseManager responseManager;
    private Jenkins jenkins;
    private GitHub github;
    private ConsoleReader consoleReader;

    public static void main(String[] args) throws Exception {
        System.out.println("Starting up Nexus, please wait...");

        System.out.println("Loading configuration...");
        OptionsConfig config = new OptionsConfig();
        GitHubConfig githubConfig = new GitHubConfig();
        NicksConfig nicksConfig = new NicksConfig();

        if (!config.isReady()) {
            LOGGER.info("Your config needs the 'ready' field to set to true.");
            System.exit(-1);
        }

        Configuration.Builder<Nexus> builder = new Configuration.Builder<Nexus>()
                .setName(config.getNick())
                .setLogin(config.getNick())
                .setVersion(config.getNick())
                .setAutoReconnect(true)
                .setAutoNickChange(true)
                .addListener(new EventManager())
                .setServerHostname(config.getServer())
                .setServerPort(config.getPort())
                .setShutdownHookEnabled(false)
                .setEncoding(Charset.forName("UTF-8"));
        if (!config.getAdminChannel().isEmpty()) {
            builder.addAutoJoinChannel(config.getAdminChannel());
        }
        if (!config.getNickServPassword().isEmpty()) {
            builder.setNickservPassword(config.getNickServPassword());
        }
        if (!config.getServerPassword().isEmpty()) {
            builder.setServerPassword(config.getServerPassword());
        }
        for (String channel : config.getChannels()) {
            builder.addAutoJoinChannel(channel);
        }
        Nexus bot = new Nexus(builder.buildConfiguration());
        bot.config = config;
        bot.githubConfig = githubConfig;
        bot.nicksConfig = nicksConfig;
        try {
            bot.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Nexus(Configuration<Nexus> configuration) {
        super(configuration);
        INSTANCE = this;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                endProcess();
            }
        });
    }

    private void registerLogger() {
        try {
            Logger root = Logger.getLogger("");
            root.setLevel(Level.INFO);

            FileHandler handler = new FileHandler("Nexus.log", true);
            handler.setLevel(Level.INFO);
            handler.setFormatter(new ShortLoggerFormatter(true));
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
            console.setFormatter(new ShortLoggerFormatter());
            if (registerWithRoot) {
                root.addHandler(console);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepare() {
        Unirest.setTimeouts(10000, 10000);
        Unirest.setDefaultHeader("user-agent", getConfig().get("user-agent", "Nexus"));

        this.registerLogger();

        LOGGER.info("Registering commands");
        commandManager = new CommandManager();
        commandManager.registerDefaults();
        commandManager.buildGroupMap();

        LOGGER.info("Preparing response manager");
        responseManager = new ResponseManager();
        responseManager.load();

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

        LOGGER.info("Preparing console reader");
        this.prepareConsoleReader();
        LOGGER.info("Done! Nexus is ready!");

        LOGGER.info("Attempting to connect to " + config.getServer() + " and join " + config.getChannels().size() + " channels.");
        try {
            this.startBot();
        } catch (IrcException | IOException ignored) {
        }

    }

    private void prepareConsoleReader() {
        consoleReader = new ConsoleReader();
        consoleReader.start();
    }

    public static void endProcess() {
        if (INSTANCE != null) {
            LOGGER.info("Shutting down Nexus...");
            INSTANCE.consoleReader.setRunning(false);
            INSTANCE.saveAll();
            LOGGER.info("Waiting for outgoing queue");
            while (INSTANCE.sendRaw().getOutgoingQueueSize() > 0);
            INSTANCE.shutdown(true);
            INSTANCE = null;
            System.exit(-1);
        }
    }

    public void saveAll() {
        LOGGER.info("Saving config files");
        this.getConfig().save();
        this.getNicksConfig().save();
        this.getGitHubConfig().save();
        LOGGER.info("Saving channels");
        //this.saveChannels();
        RemindCommand remindCommand = this.getCommandManager().getModuleOfType(RemindCommand.class);
        if (remindCommand != null) {
            LOGGER.info("Saving reminders");
            remindCommand.saveReminders();
        }
        LOGGER.info("Saving responses");
        responseManager.save();
    }

    public void saveChannels() {
        this.config.clearChannels();
        for (Channel channel : this.getUserBot().getChannels()) {
            this.config.addChannel(channel.getName());
        }
        this.config.save();
    }

    public void send(String target, String message) {
        Nexus.getInstance().sendRaw().rawLine("PRIVMSG " + target + " :" + message);
    }

    public String appendNick(String nick, String message) {
        if (getConfig().appendNicks()) {
            message = "(" + nick + ") " + message;
        }
        return message;
    }

    public Channel getChannel(String channelName) {
        for (Channel c : getUserBot().getChannels()) {
            if (c.getName().equalsIgnoreCase(channelName)) {
                return c;
            }
        }
        return null;
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

    public boolean isAdmin(String userNick) {
        return isChannelAdmin(userNick) || isNexusAdmin(userNick);
    }

    public boolean isAdmin(User user) {
        return isAdmin(user.getNick());
    }

    public boolean isChannelAdmin(User user) {
        return isChannelAdmin(user.getNick());
    }

    public boolean isChannelAdmin(String userNick) {
        Channel adminChannel = this.getChannel(this.getConfig().getAdminChannel());
        if (adminChannel != null) {
            for (User u : adminChannel.getOps()) {
                if (u.getNick().equalsIgnoreCase(userNick)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isNexusAdmin(User user) {
        return this.getConfig().getAdmins().contains(user.getNick());
    }

    public boolean isNexusAdmin(String userNick) {
        return this.getConfig().getAdmins().contains(userNick);
    }

    public ResponseManager getResponseManager() {
        return responseManager;
    }
}