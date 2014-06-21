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
import com.dsh105.nexus.command.CommandGroup;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;

@Command(command = "part",
        aliases = "leave",
        needsChannel = false,
        groups = CommandGroup.ADMIN,
        help = "Make Nexus part a channel.",
        extendedHelp = {
                "{b}{p}{c} <channel>{/b} - Make Nexus leave a channel."
        })
public class PartCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length != 1) {
            return false;
        }

        String channelName = event.getArgs()[0];
        if (!channelName.startsWith("#")) {
            channelName = "#" + channelName;
        }

        event.respondWithPing("Attempting to part " + channelName);
        Nexus.getInstance().sendRaw().rawLine("PART " + channelName);
        return true;
    }

    @Override
    public boolean adminOnly() {
        return true;
    }
}