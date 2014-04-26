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
import com.dsh105.nexus.command.CommandModule;
import org.pircbotx.Channel;
import org.pircbotx.User;

public class HelpCommand extends CommandModule {

    @Override
    public void onCommand(Channel channel, User sender, String[] args) {
        for (CommandModule module : Nexus.getInstance().getCommandManager().getRegisteredCommands()) {
            sender.sendMessage(Nexus.getInstance().getConfig().getCommandPrefix() + module.getCommand() + " - " + module.getHelp());
        }
    }

    @Override
    public String getHelp() {
        return "Show this help information";
    }
}