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

import com.dsh105.nexus.command.CommandGroup;
import com.dsh105.nexus.command.CommandManager;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.module.dynamic.DynamicCommand;
import com.dsh105.nexus.command.module.general.RemindCommand;
import com.dsh105.nexus.config.ChannelConfiguration;
import com.dsh105.nexus.config.GitHubConfig;
import com.dsh105.nexus.config.NicksConfig;
import com.dsh105.nexus.config.OptionsConfig;
import com.dsh105.nexus.hook.github.GitHub;
import com.dsh105.nexus.hook.jenkins.Jenkins;
import com.dsh105.nexus.listener.EventManager;
import com.dsh105.nexus.response.ResponseManager;
import com.dsh105.nexus.util.ColorUtil;
import com.dsh105.nexus.util.ShortLoggerFormatter;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.ocpsoft.prettytime.PrettyTime;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.exception.IrcException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class Nexus extends PircBotX {

    public static Logger LOGGER = Logger.getLogger(Nexus.class.getName());
    public static PrettyTime PRETTY_TIME = new PrettyTime();
    public static String CONFIG_FILE_NAME = "options.yml";
    private static Nexus INSTANCE;
    private ConsoleReader consoleReader;
    protected ChannelLogHandler channelLogHandler;
    private ChannelConfiguration channelConfiguration;
    private OptionsConfig config;
    private GitHubConfig githubConfig;
    private NicksConfig nicksConfig;

    private CommandManager commandManager;
    private ResponseManager responseManager;

    private Jenkins jenkins;
    private GitHub github;

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

    public static void main(String[] args) throws Exception {
        createBot();
    }

    public static void createBot() {
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
        bot.channelConfiguration = new ChannelConfiguration();
        bot.config = config;
        bot.githubConfig = githubConfig;
        bot.nicksConfig = nicksConfig;
        try {
            bot.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void endProcess() {
        if (INSTANCE != null) {
            try {
                LOGGER.info("Shutting down Nexus...");
                INSTANCE.channelLogHandler.close();
                INSTANCE.saveAll();
                try {
                    if (Jenkins.getJenkins() != null && Jenkins.getJenkins().TASK != null) {
                        Jenkins.getJenkins().TASK.cancel();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Unirest.shutdown();
                } catch (Exception e) {
                    LOGGER.severe("Failed to shutdown Unirest");
                    e.printStackTrace();
                }
                try {
                    INSTANCE.consoleReader.reader.getTerminal().restore();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                INSTANCE.consoleReader.setRunning(false);
                try {
                    INSTANCE.shutdown(true);
                } catch (Exception ignored) {
                }
                INSTANCE = null;
                LOGGER.info("System exiting...");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.exit(-1);
            }
        }
    }

    public static Nexus getInstance() {
        return INSTANCE;
    }

    private void registerLogger() {
        try {
            File f = new File("logs");
            if (!f.exists()) {
                f.mkdirs();
            }
            Logger root = Logger.getLogger("");
            root.setLevel(Level.INFO);

            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            FileHandler handler = new FileHandler("logs" + File.separator + "Nexus-" + date + ".log", true);
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

    private void registerChannelLogger() {
        if (channelLogHandler != null) {
            return;
        }
        String logChannel = config.getLogChannel();
        if (logChannel.isEmpty()) {
            return;
        }

        Logger root = Logger.getLogger("");

        channelLogHandler = new ChannelLogHandler(logChannel);
        channelLogHandler.setLevel(Level.INFO);
        channelLogHandler.setFormatter(new ShortLoggerFormatter(true));
        root.addHandler(channelLogHandler);
    }

    private void prepare() {
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        Unirest.setTimeouts(10000, 10000);
        Unirest.setDefaultHeader("user-agent", config.get("user-agent", "Nexus"));

        this.registerLogger();

        LOGGER.info("Registering commands");
        commandManager = new CommandManager();
        commandManager.registerDefaults();

        LOGGER.info("Preparing response manager");
        responseManager = new ResponseManager();
        responseManager.load();

        String jenkinsUrl = this.config.getJenkinsUrl();
        if (!jenkinsUrl.isEmpty()) {
            LOGGER.info("Initiating Jenkins hook");
            try {
                Unirest.get(jenkinsUrl + "api/json").asJson();
                this.jenkins = new Jenkins(jenkinsUrl);
            } catch (UnirestException e) {
                LOGGER.severe("Jenkins appears to be offline!");
            }
        }
        LOGGER.info("Initiating GitHub hook");
        this.github = new GitHub();
        RemindCommand remindCommand = commandManager.getModuleOfType(RemindCommand.class);
        if (remindCommand != null) {
            LOGGER.info("Loading saved reminders");
            remindCommand.loadReminders();
        }

        // Scala stuff
        LOGGER.info("Initializing Scala commands...");
        // TODO: 'ere be scala commands

        LOGGER.info("Preparing console reader");
        this.prepareConsoleReader();

        // Prepare colour serialisation stuff
        ColorUtil.validColours();
        LOGGER.info("Done! Nexus is ready!");

        LOGGER.info("Attempting to connect to " + config.getServer() + " and join " + config.getChannels().size() + " channels.");
        try {
            this.startBot();
        } catch (IrcException | IOException ignored) {
        }

    }

    public void onConnect() {
        registerChannelLogger();

        if (!config.getStartupMessage().isEmpty()) {
            sendIRC().message(config.getAdminChannel(), config.getStartupMessage());
        }
    }

    private void prepareConsoleReader() {
        consoleReader = new ConsoleReader();
        consoleReader.setDaemon(true);
        consoleReader.start();
    }

    public void saveAll() {
        LOGGER.info("Saving config files");
        config.save();
        nicksConfig.save();
        githubConfig.save();

        //LOGGER.info("Saving channels");
        //this.saveChannels();
        RemindCommand remindCommand = commandManager.getModuleOfType(RemindCommand.class);
        if (remindCommand != null) {
            LOGGER.info("Saving reminders");
            remindCommand.saveReminders();
        }

        LOGGER.info("Saving dynamic commands");
        if (commandManager.getGroupsMap().get(CommandGroup.DYNAMIC) != null) {
            for (CommandModule module : commandManager.getGroupsMap().get(CommandGroup.DYNAMIC)) {
                if (module instanceof DynamicCommand) {
                    ((DynamicCommand) module).save();
                }
            }
        }

        LOGGER.info("Saving responses");
        responseManager.save();
    }

    public void saveChannels() {
        this.config.clearChannels();
        for (Channel channel : this.getUserBot().getChannels()) {
            this.config.addChannel(channel.getName());
            this.channelConfiguration.getChannel(channel.getName());
        }
        this.config.save();
    }

    public String appendNick(String nick, String message) {
        if (config.appendNicks()) {
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

    public OptionsConfig getConfig() {
        return config;
    }

    public GitHubConfig getGitHubConfig() {
        return githubConfig;
    }

    public NicksConfig getNicksConfig() {
        return nicksConfig;
    }

    public ChannelConfiguration getChannelConfiguration() {
        return channelConfiguration;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public boolean initiateJenkinsConnection() {
        return initiateJenkinsConnection(this.config.getJenkinsUrl());
    }

    public boolean initiateJenkinsConnection(String jenkinsUrl) {
        if (Jenkins.getJenkins() != null) {
            return true;
        }
        if (!Jenkins.testConnection()) {
            return false;
        }
        this.jenkins = new Jenkins(jenkinsUrl);
        return true;
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
        Channel adminChannel = this.getChannel(config.getAdminChannel());
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
        return config.getAdmins().contains(user.getNick());
    }

    public boolean isNexusAdmin(String userNick) {
        return config.getAdmins().contains(userNick);
    }

    public ResponseManager getResponseManager() {
        return responseManager;
    }
}
