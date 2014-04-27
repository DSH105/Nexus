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
import com.dsh105.nexus.command.module.HelpCommand;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.User;

import java.util.ArrayList;
import java.util.Collection;

public class CommandManager {

    private ArrayList<CommandModule> modules = new ArrayList<>();

    public void registerDefaults() {
        this.register(new HelpCommand());
    }

    public void register(CommandModule module) {
        if (module.getCommandInfo() == null) {
            Nexus.LOGGER.warning("Failed to register command: " + module.getClass().getSimpleName() + ". Missing @Command annotation!");
            return;
        }
        this.modules.add(module);
    }

    public CommandModule getModuleFor(String commandArguments) {
        for (CommandModule module : modules) {
            if (module.getCommandInfo().command().equalsIgnoreCase(commandArguments)) {
                return module;
            }
        }
        return null;
    }

    public Collection<CommandModule> getRegisteredCommands() {
        return modules;
    }

    public boolean onCommand(Channel channel, User sender, String command, String... args) {
        return onCommand(new CommandPerformEvent(channel, sender, command, args));
    }

    public boolean onCommand(User sender, String command, String... args) {
        return onCommand(new CommandPerformEvent(sender, command, args));
    }

    public boolean onCommand(CommandPerformEvent event) {
        CommandModule module = this.getModuleFor(event.getCommand());
        if (module != null && module.checkPerm(event.getChannel(), event.getSender())) {
            if (module.getCommandInfo().needsChannel() && event.isInPrivateMessage()) {
                event.respond("You cannot perform " + Colors.BOLD + Colors.UNDERLINE + Nexus.getInstance().getConfig().getCommandPrefix() + module.getCommand() + " " + event.getArgs() + Colors.NORMAL + " here.");
                return true;
            }
            if (!module.onCommand(event)) {
                Suggestion suggestion = new Suggestion(event.getArgs()[1], module.getCommandInfo().subCommands());
                if (suggestion.getSuggestions().length() > 0) {
                    event.respond("Sub command not found. Did you mean: " + Colors.BOLD + suggestion.getSuggestions());
                    return true;
                }
            } else return true;
        }
        return false;
    }
}