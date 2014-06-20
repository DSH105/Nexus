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
import com.dsh105.nexus.command.module.dynamic.DynamicCommand;
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

    private HashMap<CommandGroup, ArrayList<CommandModule>> groupToModules = new HashMap<>();
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

        // load dynamic commands
        DynamicCommand.loadCommands();
    }

    public void register(CommandModule module) {
        if (module.info() == null) {
            Nexus.LOGGER.warning("Failed to register command: " + module.getClass().getSimpleName() + ". Missing @Command annotation!");
            return;
        }
        this.modules.add(module);
        for (CommandGroup group : module.info().groups()) {
            ArrayList<CommandModule> groupModules = groupToModules.get(group);
            if (groupModules == null) {
                groupModules = new ArrayList<>();
            }
            groupModules.add(module);
            groupToModules.put(group, groupModules);
        }
    }

    public void unregister(CommandModule module) {
        this.modules.remove(module);
        for (CommandGroup group : module.info().groups()) {
            ArrayList<CommandModule> groupModules = groupToModules.get(group);
            if (groupModules != null) {
                groupModules.remove(module);
            }
            groupToModules.put(group, groupModules);
        }
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
        return getModuleFor(modules, commandArguments);
    }

    public CommandModule getModuleFor(ArrayList<CommandModule> moduleList, String commandArguments) {
        for (CommandModule module : moduleList) {
            if (module.info().command().equalsIgnoreCase(commandArguments) || Arrays.asList(module.info().aliases()).contains(commandArguments.toLowerCase())) {
                return module;
            }
        }
        return null;
    }

    public CommandModule matchModule(String commandArguments) {
        return matchModule(modules, commandArguments);
    }

    public CommandModule matchModule(ArrayList<CommandModule> moduleList, String commandArguments) {
        if (commandArguments.isEmpty() || commandArguments.length() <= 0) {
            return null;
        }
        CommandModule possibleMatch = null;
        for (CommandModule module : moduleList) {
            if (module.info().command().equalsIgnoreCase(commandArguments)) {
                return module;
            }

            for (String alias : module.info().aliases()) {
                if (commandArguments.equalsIgnoreCase(alias) || alias.startsWith(commandArguments)) {
                    return module;
                }
            }

            if (module.info().command().toLowerCase().startsWith(commandArguments.toLowerCase())) {
                possibleMatch = module;
            }
        }
        return possibleMatch;
    }

    public ArrayList<CommandModule> matchGroup(String commandArguments) {
        ArrayList<CommandModule> possibleMatch = null;
        for (Map.Entry<CommandGroup, ArrayList<CommandModule>> entry : Nexus.getInstance().getCommandManager().getGroupsMap().entrySet()) {
            if (entry.getKey().toString().equalsIgnoreCase(commandArguments)) {
                return entry.getValue();
            }

            if (entry.getKey().toString().toLowerCase().startsWith(commandArguments.toLowerCase())) {
                possibleMatch = entry.getValue();
            }
        }
        return possibleMatch;
    }

    public Collection<CommandModule> getRegisteredCommands() {
        return new ArrayList<>(modules);
    }

    public HashMap<CommandGroup, ArrayList<CommandModule>> getGroupsMap() {
        return new HashMap<>(groupToModules);
    }

    public boolean onCommand(Channel channel, User sender, String content, boolean onlyWithPrefix) {
        String commandPrefix = null;
        for (String prefix : Nexus.getInstance().getConfig().getCommandPrefixes()) {
            if (content.startsWith(prefix)) {
                commandPrefix = prefix;
                break;
            }
        }
        if (onlyWithPrefix && commandPrefix == null) {
            return false;
        }
        String[] split = content.substring(commandPrefix != null ? commandPrefix.length() : 0).replaceAll("\\s+", " ").split(" ");
        return onCommand(channel, sender, split[0].toLowerCase(), StringUtil.splitArgs(1, split, " "));
    }

    public boolean onCommand(Channel channel, User sender, String content) {
        return this.onCommand(channel, sender, content, false);
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

            if (module == null) {
                ArrayList<CommandModule> dynamicModules = getGroupsMap().get(CommandGroup.DYNAMIC);
                if (dynamicModules != null && !dynamicModules.isEmpty()) {
                    module = matchModule(dynamicModules, event.getCommand());
                }
            }

            if (module != null) {
                ChannelConfiguration channelConfiguration = Nexus.getInstance().getChannelConfiguration();
                if (channelConfiguration.getChannel("GLOBAL").isDisabled(module.info().command())) {
                    return true;
                }

                if (!event.isInPrivateMessage() && !Arrays.asList(module.info().groups()).contains(CommandGroup.ADMIN)) {
                    if (channelConfiguration.getChannel(event.getChannel().getName()).isDisabled(module.info().command())) {
                        return true;
                    }
                }

                if (module.checkPerm(event.getChannel(), event.getSender())) {
                    if (module.info().needsChannel() && event.isInPrivateMessage()) {
                        Nexus.LOGGER.info(event.getSender().getNick() + " was denied usage of command via " + (event.isInPrivateMessage() ? "PM" : event.getChannel().getName()) + ": " + event.getCommand() + " " + StringUtil.combineSplit(0, event.getArgs(), " "));
                        event.respond("You cannot perform {0} here.", event.getCommandPrefix() + module.info().command() + " " + StringUtil.combineSplit(0, event.getArgs(), " "));
                        return true;
                    }
                    Nexus.LOGGER.info(event.getSender().getNick() + " used command via " + (event.isInPrivateMessage() ? "PM" : event.getChannel().getName()) + ": " + event.getCommand() + " " + StringUtil.combineSplit(0, event.getArgs(), " "));
                    if (!module.onCommand(event)) {
                        event.errorWithPing("Use " + Nexus.getInstance().getConfig().getCommandPrefix() + "{0} for help (" + formatHelp(module) + ").", Nexus.getInstance().getConfig().getCommandPrefix() + "help " + event.getCommand());
                    }
                    return true;
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
                try {
                    event.errorWithPing("Houston, we have a problem! Here is a conveniently provided stacktrace: " + GitHub.getGitHub().createGist(e));
                } catch (Exception e1) {
                    event.errorWithPing("An error was encountered, but my Gist API key is invalid! The stacktrace has been posted to the console.");
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }

    public String formatHelp(CommandModule module) {
        return format(module, module.info().help());
    }

    public String format(CommandModule module, String toFormat) {
        return toFormat.replace("{c}", module == null ? "" : module.info().command()).replace("{p}", Nexus.getInstance().getConfig().getCommandPrefix()).replace("{b}", Colors.BOLD).replace("{/b}", Colors.NORMAL);
    }

    public String getHelpInfoFor(CommandPerformEvent event, CommandModule module) {
        String aliases = (module.info().aliases().length <= 0 ? "" : " (Aliases: " + Colors.BOLD + StringUtil.combineSplit(0, module.info().aliases(), ", ") + Colors.NORMAL + ")");
        String status = "";
        ChannelConfiguration channelConfiguration = Nexus.getInstance().getChannelConfiguration();
        if (!event.isInPrivateMessage()) {
            status = channelConfiguration.getChannel(event.getChannel().getName()).isDisabled(module.info().command()) ? Colors.RED + " (Disabled - " + event.getChannel().getName() + ")" : "";
        }
        if (channelConfiguration.getChannel("GLOBAL").isDisabled(module.info().command())) {
            status = Colors.RED + " (Disabled globally)";
        }
        return format(module, "{b}{p}{c}{/b} - " + module.info().help()) + aliases + status;
    }
}
