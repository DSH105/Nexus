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
import org.pircbotx.User;

public abstract class CommandModule {

    private String command;

    public String getCommand() {
        return command;
    }

    protected void setCommand(String command) {
        this.command = command;
    }

    public abstract boolean onCommand(Channel channel, User sender, String[] args);

    public abstract String getHelp();

    protected boolean checkPerm(Channel channel, User sender) {
        if (channel.getName().equals(Nexus.ADMIN_CHANNEL) && channel.isOp(sender)) {
            return true;
        }
        return hasPermission(channel, sender);
    }

    public boolean hasPermission(Channel channel, User sender) {
        return true;
    }
}