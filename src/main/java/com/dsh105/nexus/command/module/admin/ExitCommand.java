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

package com.dsh105.nexus.command.module.admin;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;

@Command(command = "exit",
        needsChannel = false,
        helpGroups = "admin",
        help = "Exit and shutdown Nexus",
        extendedHelp = {
                "Use {b}{p}{c}{/b} to shutdown the bot. Only admins may use this command."
        })
public class ExitCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.isInPrivateMessage()) {
            Nexus.getInstance().sendIRC().action(event.getSender().getNick(), "glides off into the distance...");
        } else {
            Nexus.getInstance().sendIRC().action(event.getChannel().getName(), "glides off into the distance...");
        }
        Nexus.endProcess();
        return true;
    }

    @Override
    public boolean adminOnly() {
        return true;
    }
}