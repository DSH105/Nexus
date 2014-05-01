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

package com.dsh105.nexus.command.module;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import org.pircbotx.Channel;
import org.pircbotx.User;

@Command(command = "exit", needsChannel = false, help = "Exit and shutdown Nexus", extendedHelp = "Use ;exit to shutdown the bot. Only admins may use this command.")
public class ExitCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.isInPrivateMessage()) {
            Nexus.getInstance().sendAction(event.getSender(), "glides off into the distance...");
        } else {
            Nexus.getInstance().sendAction(event.getChannel(), "glides off into the distance...");
        }
        while (Nexus.getInstance().getOutgoingQueueSize() > 0);
        Nexus.getInstance().shutdown(true);
        return true;
    }

    @Override
    public boolean hasPermission(Channel channel, User sender) {
        return false;
    }

    @Override
    public boolean hasPermission(User sender) {
        return false;
    }
}