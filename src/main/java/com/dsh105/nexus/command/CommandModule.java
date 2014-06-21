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
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.User;

public abstract class CommandModule implements ICommandModule {

    public abstract boolean onCommand(CommandPerformEvent event);

    protected boolean checkPerm(Channel channel, User sender) {
        if (Nexus.getInstance().isAdmin(sender)) {
            return true;
        }
        if (adminOnly()) {
            Nexus.getInstance().sendIRC().message(sender.getNick(), Colors.RED + "I'm sorry. I'm afraid you can't do that.");
            return false;
        }
        return true;
    }

    public boolean adminOnly() {
        return false;
    }

    public Command info() {
        return this.getClass().getAnnotation(Command.class);
    }
}
