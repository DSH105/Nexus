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
import com.dsh105.nexus.command.CommandGroup;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;

@Command(command = "remove",
        aliases = {"rm"},
        needsChannel = false,
        groups = CommandGroup.ADMIN,
        help = "Remove an existing dynamic command.",
        extendedHelp = {
                "{b}{p}{c} <command>{/b} - Remove an existing dynamic command."
        })
public class RemoveCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length != 1) {
            return false;
        }
        CommandModule module = event.getManager().getModuleFor(event.getArgs()[0]);
        if (module == null) {
            event.errorWithPing("{0} is not a valid command.", event.getArgs()[0]);
            return true;
        }
        if (!(module instanceof DynamicCommand)) {
            event.errorWithPing("{0} is not a dynamic command! Use {1} to disable commands globally.", module.info().command(), event.getCommandPrefix() + "chan GLOBAL " + module.info().command() + " disable");
            return true;
        }

        DynamicCommand dynamicCommand = (DynamicCommand) module;
        dynamicCommand.remove();
        event.respondWithPing("{0} successfully disabled and removed.", dynamicCommand.info().command());
        return true;
    }

    @Override
    public boolean adminOnly() {
        return true;
    }
}