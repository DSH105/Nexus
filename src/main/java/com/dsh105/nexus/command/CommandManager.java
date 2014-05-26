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

package com.dsh105.nexus.command;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.config.ChannelConfiguration;
import com.dsh105.nexus.exception.general.DateParseException;
import com.dsh105.nexus.exception.github.GitHubAPIKeyInvalidException;
import com.dsh105.nexus.exception.github.GitHubHookNotFoundException;
import com.dsh105.nexus.exception.github.GitHubRateLimitExceededException;
import com.dsh105.nexus.hook.github.GitHub;
import com.dsh105.nexus.util.StringUtil;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.User;
import org.reflections.Reflections;

import java.util.*;

public class CommandManager {

    HashMap<String, ArrayList<CommandModule>> groupToModules = new HashMap<>();
    private ArrayList<CommandModule> modules = new ArrayList<>();

    public void registerDefaults() {
        Reflections reflections = new Reflections("com.dsh105.nexus.command.module");
        Set<Class<? extends CommandModule>> cmds = reflections.getSubTypesOf(CommandModule.class);
        for (Class<? extends CommandModule> cmd : cmds) {
            try {
                if (cmd.getAnnotation(Exclude.class) != null) {
                    // skip excluded commands
                    continue;
                }
                this.register(cmd.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void buildGroupMap() {
        for (CommandModule module : Nexus.getInstance().getCommandManager().getRegisteredCommands()) {
            for (String group : module.getCommandInfo().helpGroups()) {
                if (!group.equalsIgnoreCase("all")) {
                    ArrayList<CommandModule> existing = groupToModules.get(group);
                    if (existing == null) {
                        existing = new ArrayList<>();
                    }
                    existing.add(module);
                    groupToModules.put(group, existing);
                }
            }
        }
    }

    public void register(CommandModule module) {
        if (module.getCommandInfo() == null) {
            Nexus.LOGGER.warning("Failed to register command: " + module.getClass().getSimpleName() + ". Missing @Command annotation!");
            return;
        }
        this.modules.add(module);
    }

    public <T extends CommandModule> T getModuleOfType(Class<T> type) {
        for (CommandModule module : modules) {
            if (module.getClass().equals(type)) {
                return (T) module;
            }
        }
        return null;
    }

    public CommandModule getModuleFor(String commandArguments) {
        for (CommandModule module : modules) {
            if (module.getCommandInfo().command().equalsIgnoreCase(commandArguments) || Arrays.asList(module.getCommandInfo().aliases()).contains(commandArguments.toLowerCase())) {
                return module;
            }
        }
        return null;
    }

    public CommandModule matchModule(String commandArguments) {
        CommandModule possibleMatch = null;
        for (CommandModule module : modules) {
            if (module.getCommandInfo().command().equalsIgnoreCase(commandArguments)) {
                return module;
            }

            for (String alias : module.getCommandInfo().aliases()) {
                if (commandArguments.equalsIgnoreCase(alias) || alias.startsWith(commandArguments)) {
                    return module;
                }
            }

            if (module.getCommand().startsWith(commandArguments)) {
                possibleMatch = module;
            }
        }
        return possibleMatch;
    }

    public ArrayList<CommandModule> matchGroup(String commandArguments) {
        ArrayList<CommandModule> possibleMatch = null;
        for (Map.Entry<String, ArrayList<CommandModule>> entry : Nexus.getInstance().getCommandManager().getGroupsMap().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(commandArguments)) {
                return entry.getValue();
            }

            if (entry.getKey().startsWith(commandArguments)) {
                possibleMatch = entry.getValue();
            }
        }
        return possibleMatch;
    }

    public Collection<CommandModule> getRegisteredCommands() {
        return modules;
    }

    public HashMap<String, ArrayList<CommandModule>> getGroupsMap() {
        return new HashMap<>(groupToModules);
    }

    public boolean onCommand(Channel channel, User sender, String content) {
        String[] split = Colors.removeFormattingAndColors(content).substring(content.contains("\\") ? Nexus.getInstance().getConfig().getCommandPrefix().length() : 0).replaceAll("\\s+", " ").split(" ");
        return onCommand(channel, sender, split[0].toLowerCase(), StringUtil.splitArgs(1, split, " "));
    }

    public boolean onCommand(Channel channel, User sender, String command, String... args) {
        return onCommand(new CommandPerformEvent(channel, sender, command, args));
    }

    public boolean onCommand(User sender, String command, String... args) {
        return onCommand(new CommandPerformEvent(null, sender, command, args));
    }

    public boolean onCommand(CommandPerformEvent event) {
        try {
            CommandModule module = this.getModuleFor(event.getCommand());

            if (module != null) {
                ChannelConfiguration channelConfiguration = Nexus.getInstance().getChannelConfiguration();
                if (channelConfiguration.getChannel("GLOBAL").isDisabled(module.getCommand())) {
                    return true;
                }

                if (!event.isInPrivateMessage() && !Arrays.asList(module.getCommandInfo().helpGroups()).contains("admin")) {
                    if (channelConfiguration.getChannel(event.getChannel().getName()).isDisabled(module.getCommand())) {
                        return true;
                    }
                }
                
                if (module.checkPerm(event.getChannel(), event.getSender())) {
                    if (module.getCommandInfo().needsChannel() && event.isInPrivateMessage()) {
                        event.respond("You cannot perform {0} here.", event.getCommandPrefix() + module.getCommand() + " " + StringUtil.combineSplit(0, event.getArgs(), " "));
                        return true;
                    }
                    if (!module.onCommand(event)) {
                        event.errorWithPing("Use " + Nexus.getInstance().getConfig().getCommandPrefix() + "{0} for help (" + formatHelp(module) + ").", Nexus.getInstance().getConfig().getCommandPrefix() + "help " + event.getCommand());
                        return true;
                    } else {
                        return true;
                    }
                }
            }

        } catch (Exception e) {
            if (e instanceof GitHubAPIKeyInvalidException) {
                event.errorWithPing(e.getMessage() + " Use {0} to authenticate with GitHub through Nexus.", Nexus.getInstance().getConfig().getCommandPrefix() + "ghk");
            } else if (e instanceof DateParseException) {
                event.errorWithPing(e.getMessage());
            } else if (e instanceof GitHubRateLimitExceededException) {
                event.errorWithPing("Rate limit for this GitHub API Key exceeded. Further requests cannot be executed on the behalf of this user.");
            } else if (e instanceof GitHubHookNotFoundException) {
                event.errorWithPing(e.getMessage());
            } else {
                if (Nexus.getInstance().getGitHubConfig().getNexusGitHubApiKey().isEmpty()) {
                    e.printStackTrace();
                    event.errorWithPing("An error was encountered, but my Gist API key is invalid! The stacktrace has been posted to the console.");
                    return true;
                }
                event.errorWithPing("Houston, we have a problem! Here is a conveniently provided stacktrace: " + GitHub.getGitHub().createGist(e));
            }
            return true;
        }
        return false;
    }

    public String formatHelp(CommandModule module) {
        return format(module, module.getCommandInfo().help());
    }

    public String format(CommandModule module, String toFormat) {
        return toFormat.replace("{c}", module == null ? "" : module.getCommand()).replace("{p}", Nexus.getInstance().getConfig().getCommandPrefix()).replace("{b}", Colors.BOLD).replace("{/b}", Colors.NORMAL);
    }

    public String getHelpInfoFor(CommandPerformEvent event, CommandModule module) {
        String aliases = (module.getCommandInfo().aliases().length <= 0 ? "" : " (Aliases: " + Colors.BOLD + StringUtil.combineSplit(0, module.getCommandInfo().aliases(), ", ") + Colors.NORMAL + ")");
        String status = "";
        if (!event.isInPrivateMessage()) {
            status = Nexus.getInstance().getChannelConfiguration().getChannel(event.getChannel().getName()).isDisabled(module.getCommand()) ? "" : Colors.RED + " (Disabled - " + event.getChannel().getName() + ")";
        }
        return format(module, "{b}{p}{c}{/b} - " + module.getCommandInfo().help()) + aliases + status;
    }
}
