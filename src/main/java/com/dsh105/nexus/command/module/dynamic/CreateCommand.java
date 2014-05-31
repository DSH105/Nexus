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

package com.dsh105.nexus.command.module.dynamic;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.command.CommandGroup;
import com.dsh105.nexus.util.StringUtil;

import java.util.Arrays;

@Command(command = "create",
        aliases = {"cr"},
        needsChannel = false,
        groups = CommandGroup.ADMIN,
        help = "Create a dynamic command.",
        extendedHelp = {
                "{b}{p}{c} <command> [type] <response>{/b} - Create a command with a certain response on execution.",
                "{b}[type]{/b} - Optional command type -> action (performs action instead of response), command (performs a command), alias (adds an alias to a command)",
                "Optional response placeholders:",
                "- {b}%s{/b} -> Name of the command sender",
                "- {b}%c{/b} -> Name of the channel executed in. \'PM\' if in private message",
                "- {b}%a#:<default>{/b} -> Command argument, where # is the argument number. If the argument is not specified, the default is used",
        })
public class CreateCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length <= 0) {
            return false;
        }
        String command = event.getArgs()[0];
        if (event.getManager().getModuleFor(command) != null) {
            event.errorWithPing("A command already exists under {0}", command);
            return true;
        }

        String type = "NORMAL";
        int responseStartIndex = 1;
        if (event.getArgs().length >= 3) {
            if (isValidType(event.getArgs()[1])) {
                type = event.getArgs()[1].toUpperCase();
                responseStartIndex = 2;
            }
        }
        String response = StringUtil.combineSplit(responseStartIndex, event.getArgs(), " ");
        String help = "Dynamic command (" + type.toLowerCase() + ") -> " + response;

        if (type.equalsIgnoreCase("ALIAS")) {
            CommandModule module = event.getManager().getModuleFor(event.getArgs()[responseStartIndex]);
            if (module == null) {
                event.errorWithPing("Aliases cannot be added to a command that does not exist ({0}).", event.getArgs()[responseStartIndex]);
                return true;
            }
            if (!(module instanceof DynamicCommand)) {
                event.errorWithPing("Aliases cannot be added to {0}.", module.info().command());
                return true;
            }
            String[] aliases = StringUtil.splitArgs(responseStartIndex, event.getArgs(), " ");
            ((DynamicCommand) module).addAliases(aliases);
            event.respondWithPing("Alias" + (aliases.length == 1 ? "" : "es") + " ({0} added to command ({1})", StringUtil.buildSentenceList(aliases), event.getCommandPrefix() + command);
            return true;
        }

        DynamicCommand dynamicCommand = new DynamicCommandFactory().withCommand(command).withHelp(help).withExtendedHelp(help).withResponseOfType(response, type).prepare();
        event.getManager().register(dynamicCommand);
        dynamicCommand.save();
        event.respondWithPing("Command registered as {0}", event.getCommandPrefix() + command);
        return true;
    }

    private boolean isValidType(String type) {
        return Arrays.asList(new String[]{"NORMAL", "ACTION", "COMMAND", "ALIAS"}).contains(type.toUpperCase());
    }

    @Override
    public boolean adminOnly() {
        return true;
    }
}