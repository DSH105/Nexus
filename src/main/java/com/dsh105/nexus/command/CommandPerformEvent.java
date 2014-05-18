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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandPerformEvent {

    private Channel channel;
    private User sender;
    private String command;
    private String[] args;
    private boolean inPrivateMessage;

    public CommandPerformEvent(Channel channel, User sender, String command, String... args) {
        if (channel == null) {
            this.sender = sender;
            this.command = command;
            this.args = args;
            this.inPrivateMessage = true;
        } else {
            this.channel = channel;
            this.sender = sender;
            this.command = command;
            this.args = args;
        }
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

    public String getCommandPrefix() {
        return Nexus.getInstance().getConfig().getCommandPrefix();
    }

    public String removePing(String nick) {
        return nick == null ? null : (nick.substring(0, 1) + '\u200b' + (nick.length() >= 2 ? nick.substring(1, nick.length()) : ""));
    }

    /*
     * Easy response methods
     */

    public void respondWithPing(String message, String... highlights) {
        respond(Colors.NORMAL, message, true, false, highlights);
    }

    public void respond(String message, String... highlights) {
        this.respond(Colors.NORMAL, message, false, false, highlights);
    }

    public void respond(String message, boolean forcePrivateMessage, String... highlights) {
        respond(Colors.NORMAL, message, false, forcePrivateMessage, highlights);
    }

    public void respond(String mainColor, String message, boolean appendNick, boolean forcePrivateMessage, String... highlights) {
        StringBuffer buffer = new StringBuffer();
        Matcher matcher = Pattern.compile("(\\{([0-9]+?)\\})").matcher(message);
        while (matcher.find()) {
            int index = Integer.parseInt(matcher.group(2));
            if (index >= highlights.length) {
                break;
            }
            String replacement = highlights[index];
            matcher.appendReplacement(buffer, Colors.BOLD + replacement + Colors.NORMAL + mainColor);
        }
        matcher.appendTail(buffer);
        String response = mainColor + buffer.toString();
        if (appendNick && !this.inPrivateMessage) {
            response = Nexus.getInstance().appendNick(this.sender.getNick(), response);
        }
        respond(response, forcePrivateMessage);
    }

    public void errorWithPing(String message, String... highlights) {
        this.respond(Colors.RED, message, true, false, highlights);
    }

    public void error(String message, String... highlights) {
        this.error(message, false, highlights);
    }

    public void error(String message, boolean forcePrivateMessage, String... highlights) {
        this.respond(Colors.RED, message, false, forcePrivateMessage, highlights);
    }

    public void respondWithPing(String message) {
        if (!this.inPrivateMessage) {
            message = Nexus.getInstance().appendNick(this.sender.getNick(), message);
        }
        this.respond(message, false);
    }

    public void respond(String message) {
        this.respond(message, false);
    }

    public void respond(String message, boolean forcePrivateMessage) {
        if (this.inPrivateMessage || forcePrivateMessage) {
            Nexus.getInstance().send(sender.getNick(), message);
        } else {
            Nexus.getInstance().send(channel.getName(), message);
        }
    }
}