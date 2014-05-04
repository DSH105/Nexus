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
import com.dsh105.nexus.exception.github.GitHubAPIKeyInvalidException;
import com.dsh105.nexus.exception.github.GitHubRateLimitExceededException;
import com.dsh105.nexus.hook.github.GitHub;
import com.dsh105.nexus.util.StringUtil;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.User;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class CommandManager {

    private ArrayList<CommandModule> modules = new ArrayList<>();

    public void registerDefaults() {
        Reflections reflections = new Reflections("com.dsh105.nexus.command.module");
        Set<Class<? extends CommandModule>> cmds = reflections.getSubTypesOf(CommandModule.class);
        for (Class<? extends CommandModule> cmd : cmds) {
            try {
                this.register(cmd.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
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
            if (module.getCommandInfo().command().equalsIgnoreCase(commandArguments)) {
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
            } else if (module.getCommand().startsWith(commandArguments)) {
                possibleMatch = module;
            }
        }
        return possibleMatch;
    }

    public Collection<CommandModule> getRegisteredCommands() {
        return modules;
    }

    public boolean onCommand(Channel channel, User sender, String content) {
        String[] split = content.substring(content.contains("\\") ? Nexus.getInstance().getConfig().getCommandPrefix().length() : 0).replaceAll("\\s+", " ").split(" ");
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
            if (module != null && module.checkPerm(event.getChannel(), event.getSender())) {
                if (module.getCommandInfo().needsChannel() && event.isInPrivateMessage()) {
                    event.respond("You cannot perform {0} here.", event.getCommandPrefix() + module.getCommand() + " " + StringUtil.combineSplit(0, event.getArgs(), " "));
                    return true;
                }
                if (!module.onCommand(event)) {
                    event.error("Use " + Nexus.getInstance().getConfig().getCommandPrefix() + "{0} for help.", Nexus.getInstance().getConfig().getCommandPrefix() + "help " + event.getCommand());
                    return true;
                    /*Suggestion suggestion = new Suggestion(event.getArgs()[1], module.getCommandInfo().subCommands());
                    if (suggestion.getSuggestions() != null && suggestion.getSuggestions().length() > 0) {
                        event.respondWithPing("Sub command not found. Did you mean: " + Colors.BOLD + suggestion.getSuggestions());
                        return true;
                    }*/
                } else return true;
            }
        } catch (Exception e) {
            if (e instanceof GitHubAPIKeyInvalidException) {
                event.respondWithPing(Colors.RED + "Failed to connect to GitHub. My API key is not configured!");
                return true;
            }
            if (e instanceof GitHubRateLimitExceededException) {
                event.respondWithPing(Colors.RED + "Rate limit for GitHub API exceeded. Further requests cannot be executed.");
            }
            event.respondWithPing(Colors.RED + "Houston, we have a problem! Here is a conveniently provided stacktrace: " + GitHub.getGitHub().createGist(e));
            return true;
        }
        return false;
    }
}