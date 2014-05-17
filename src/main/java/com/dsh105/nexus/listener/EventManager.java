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

package com.dsh105.nexus.listener;

import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.util.StringUtil;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventManager extends ListenerAdapter<Nexus> {

    @Override
    public void onMessage(MessageEvent<Nexus> event) throws Exception {
        String message = event.getMessage();
        String commandPrefix = Nexus.getInstance().getConfig().getCommandPrefix();
        if (message.startsWith(commandPrefix)) {
            String[] split = message.substring(commandPrefix.length()).replaceAll("\\s+", " ").split(" ");
            Nexus.getInstance().getCommandManager().onCommand(event.getChannel(), event.getUser(), split[0].toLowerCase(), StringUtil.splitArgs(1, split, " "));
        }

        Nexus.getInstance().getResponseManager().trigger(event.getChannel(), event.getUser(), message);
    }

    @Override
    public void onInvite(InviteEvent<Nexus> event) throws Exception {
        Nexus.LOGGER.info("Received invite to " + event.getChannel() + " from " + event.getUser());
        if (Nexus.getInstance().isAdmin(event.getUser())) {
            event.getBot().sendIRC().joinChannel(event.getChannel());
            event.getBot().send(event.getChannel(), event.getUser() + " wanted me in here.");
            Nexus.LOGGER.info("Channel invite accepted");
        } else {
            event.getBot().send(event.getUser(), "You are not allowed to invite me.");
            Nexus.LOGGER.info("Channel invite denied");
        }
    }

    @Override
    public void onNotice(NoticeEvent<Nexus> event) throws Exception {
        Nexus.LOGGER.info("Received notice: " + event.getNotice());
        // Attempt to retrieve static login information for a user
        Matcher matcher = Pattern.compile("Information on (.+?) \\(account (.+?)\\):").matcher(event.getNotice());
        while (matcher.find()) {
            Nexus.getInstance().getNicksConfig().storeNick(matcher.group(1), matcher.group(2));
        }
    }

    @Override
    public void onPrivateMessage(PrivateMessageEvent<Nexus> event) throws Exception {
        Nexus.LOGGER.info("Received PM from " + event.getUser().getNick() + ": " + event.getMessage());
        if (!Nexus.getInstance().getCommandManager().onCommand(null, event.getUser(), event.getMessage())) {
            Nexus.getInstance().getResponseManager().trigger(null, event.getUser(), event.getMessage());
        }
    }

    @Override
    public void onJoin(JoinEvent<Nexus> event) throws Exception {
        Nexus.getInstance().saveChannels();
        Nexus.LOGGER.info("Joining channel: " + event.getChannel().getName());
    }

    @Override
    public void onPart(PartEvent<Nexus> event) throws Exception {
        Nexus.getInstance().saveChannels();
        Nexus.LOGGER.info("Parting channel: " + event.getChannel().getName());
    }

    @Override
    public void onConnect(ConnectEvent<Nexus> event) throws Exception {
        Nexus.LOGGER.info("Connected to IRC");
    }

    @Override
    public void onDisconnect(DisconnectEvent<Nexus> event) throws Exception {
        Nexus.LOGGER.info("Disconnected from IRC");
    }

    @Override
    public void onKick(KickEvent<Nexus> event) throws Exception {
        if (event.getRecipient().getNick().equalsIgnoreCase(Nexus.getInstance().getNick())) {
            Nexus.LOGGER.info("Kicked from " + event.getChannel() + " by " + event.getUser().getNick());
        }
    }

    @Override
    public void onVoice(VoiceEvent<Nexus> event) throws Exception {
        if (event.getRecipient().getNick().equalsIgnoreCase(Nexus.getInstance().getNick())) {
            Nexus.LOGGER.info("Voice " + (event.hasVoice() ? " given to " : " removed from ") + event.getRecipient().getNick() + " by " + event.getUser().getNick() + "in " + event.getChannel());
        }
    }
}