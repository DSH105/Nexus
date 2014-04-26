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
import com.dsh105.nexus.util.StringUtil;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.HashMap;

public class CommandManager extends ListenerAdapter<Nexus> {

    private HashMap<String, CommandModule> modules = new HashMap<>();

    public void registerDefaults() {

    }

    public void register(String subCommand, CommandModule module) {
        this.modules.put(subCommand, module);
        module.setCommand(subCommand);
    }

    public CommandModule getModuleFor(String commandArguments) {
        return this.modules.get(commandArguments);
    }

    @Override
    public void onMessage(MessageEvent<Nexus> event) {
        Channel channel = event.getChannel();
        User sender = event.getUser();
        String message = event.getMessage();

        String prefix = Nexus.getInstance().getConfig().getCommandPrefix();

        if (!message.startsWith(prefix)) {
            return;
        }

        String[] parts = message.substring(prefix.length()).split(" ");
        String command = parts[0].toLowerCase();
        String[] args = StringUtil.separate(1, parts);

        CommandModule module = this.getModuleFor(command);
        if (module != null && module.checkPerm(channel, sender)) {
            module.onCommand(channel, sender, args);
        }
    }

}