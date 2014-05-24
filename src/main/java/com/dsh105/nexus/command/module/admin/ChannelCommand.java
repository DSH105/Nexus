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
import com.dsh105.nexus.config.ChannelConfig;
import com.dsh105.nexus.util.StringUtil;
import org.apache.commons.lang3.BooleanUtils;

import java.util.ArrayList;

@Command(command = "channel",
        aliases = "chan",
        needsChannel = false,
        helpGroups = "admin",
        help = "Disable and enable commands in certain channels",
        extendedHelp = {
                "{b}{p}{c}{/b} <channel> <command> <on/off> - Disable (off) or enable (on) a command in a channel.",
                "{b}{p}{c}{/b} <channel> - View disabled commands for a channel."
        })
public class ChannelCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length < 1 || event.getArgs().length > 3) {
            event.errorWithPing("Please specify both a channel and a command to disable ({0}).", event.getCommandPrefix() + event.getCommand() + " <channel> <command>");
            return true;
        }

        String channel = event.getArgs()[0];
        ChannelConfig channelConfig = Nexus.getInstance().getChannelConfiguration().getChannel(channel);
        if (channelConfig == null) {
            event.errorWithPing("Commands cannot be disabled in a channel I am not present in ({0}).", channel);
            return true;
        }
        channel = channelConfig.getChannelName();

        if (event.getArgs().length == 1) {
            ArrayList<String> disabledCommands = channelConfig.getDisabledCommands();
            if (disabledCommands.isEmpty()) {
                event.respondWithPing("All commands are enabled in {0}.", channel);
            } else {
                event.respondWithPing("Disabled commands in {0}: " + StringUtil.join(disabledCommands, ", "), channel);
            }
            return true;
        }

        String command = event.getArgs()[1];

        if (!command.equalsIgnoreCase("ALL")) {
            // Check if the command is valid - Module matching isn't supported here
            CommandModule module = Nexus.getInstance().getCommandManager().getModuleFor(command);
            if (module == null) {
                event.errorWithPing("{0} is not a valid command! Use {1} for help info.", command, event.getCommandPrefix() + "help");
                return true;
            }
        }

        if (event.getArgs().length == 3) {
            boolean enable = BooleanUtils.toBoolean(event.getArgs()[2], "enable", "disable");
            channelConfig.setCommandStatus(command, enable);
            event.respondWithPing("{0} " + (enable ? "enabled" : "disabled") + " in {1}.", (command.equalsIgnoreCase("ALL") ? "All commands" : event.getCommandPrefix() + command), channel);
            return true;
        }

        if (command.equalsIgnoreCase("ALL")) {
            event.errorWithPing("Please use {0} to see if a command is disabled in a channel.", event.getCommandPrefix() + event.getCommand() + "<channel> <command>");
        }
        boolean enabled = channelConfig.isEnabled(command);
        event.respondWithPing("{0} is currently " + (enabled ? "enabled" : "disabled") + " in {1}.", command, channel);
        return true;
    }

    @Override
    public boolean adminOnly() {
        return true;
    }
}