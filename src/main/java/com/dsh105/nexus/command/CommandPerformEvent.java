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

public class CommandPerformEvent {

    private Channel channel;
    private User sender;
    private String command;
    private String[] args;
    private boolean inPrivateMessage;

    public CommandPerformEvent(Channel channel, User sender, String command, String... args) {
        this.channel = channel;
        this.sender = sender;
        this.command = command;
        this.args = args;
    }

    public CommandPerformEvent(User sender, String command, String... args) {
        this.sender = sender;
        this.command = command;
        this.args = args;
        this.inPrivateMessage = true;
    }

    public Channel getChannel() {
        return channel;
    }

    public User getSender() {
        return sender;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }

    public boolean isInPrivateMessage() {
        return inPrivateMessage;
    }

    public void respond(String message) {
        this.respond(message, false);
    }

    public void respond(String message, boolean forcePrivateMessage) {
        if (this.inPrivateMessage || forcePrivateMessage) {
            this.sender.sendMessage(message);
        } else {
            Nexus.getInstance().sendMessage(this.channel, this.sender, message);
        }
    }
}