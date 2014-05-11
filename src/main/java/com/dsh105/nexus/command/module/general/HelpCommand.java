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

package com.dsh105.nexus.command.module.general;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.util.StringUtil;
import org.pircbotx.Colors;

@Command(command = "help", needsChannel = false, help = "Show this help information", extendedHelp = "Use {p}{c} <command> for more information on a specific command.")
public class HelpCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length == 1) {
            CommandModule module = Nexus.getInstance().getCommandManager().matchModule(event.getArgs()[0]);
            if (module == null) {
                event.errorWithPing("Could not match {0} to a command.", event.getArgs()[0]);
                return true;
            }
            if (!event.isInPrivateMessage()) {
                event.respondWithPing("Check your private messages for help information.");
            }
            event.respond("Help info for {0}{1}:", true, event.getCommandPrefix(), module.getCommand());
            event.respond("(Aliases for {0}: {1})", true, module.getCommand(), StringUtil.combineSplit(0, module.getCommandInfo().aliases(), ", "));
            for (String part : module.getCommandInfo().extendedHelp()) {
                event.respond(part.replace("{c}", module.getCommand()).replace("{p}", event.getCommandPrefix()).replace("{b}", Colors.BOLD).replace("{/b}", Colors.NORMAL), true);
            }
            return true;
        }
        if (!event.isInPrivateMessage()) {
            event.respondWithPing("Check your private messages for help information.");
        }
        for (CommandModule module : Nexus.getInstance().getCommandManager().getRegisteredCommands()) {
            event.respond(Colors.BOLD + Nexus.getInstance().getConfig().getCommandPrefix() + module.getCommand() + Colors.NORMAL + " - " + module.getCommandInfo().help() + (module.getCommandInfo().aliases().length <= 0 ? "" : " (Aliases: " + Colors.BOLD + StringUtil.combineSplit(0, module.getCommandInfo().aliases(), ", ") + Colors.NORMAL + ")"), true);
        }
        return true;
    }
}