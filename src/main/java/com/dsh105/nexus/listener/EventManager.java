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
import com.dsh105.nexus.config.ChannelConfig;
import com.dsh105.nexus.util.StringUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventManager extends ListenerAdapter<Nexus> {

    public static final Pattern CORRECTION_PATTERN = Pattern.compile("s/([^/]+)/([^/]+)");

    @Override
    public void onMessage(MessageEvent<Nexus> event) throws Exception {
        String message = event.getMessage();
        String realName = event.getUser().getRealName();
        if (!event.getBot().getCommandManager().onCommand(event.getChannel(), event.getUser(), message, true)) {

            ChannelConfig channelConfig = event.getBot().getChannelConfiguration().getChannel(event.getChannel().getName());
            if (channelConfig != null) {
                Matcher matcher = CORRECTION_PATTERN.matcher(message);
                if (matcher.matches()) {
                    String lastMessage = channelConfig.getMessagesCache().getIfPresent(realName);
                    if (lastMessage != null) {
                        if (event.getBot().getChannelConfiguration().getChannel(event.getChannel().getName()).enableAutoCorrection()) {
                            event.getBot().sendIRC().message(event.getChannel().getName(), event.getUser().getNick() + " meant to say \"" + lastMessage.replace(matcher.group(1), Colors.BOLD + matcher.group(2)) + "\"");
                        }
                        channelConfig.getMessagesCache().invalidate(realName);
                        return;
                    }
                } else {
                    cache: {
                        for (String prefix : event.getBot().getConfig().getCommandPrefixes()) {
                            if (message.startsWith(prefix)) {
                                break cache;
                            }
                        }
                    }
                    channelConfig.getMessagesCache().put(realName, message);
                }
            }

            event.getBot().getResponseManager().trigger(event.getChannel(), event.getUser(), message);
        }
    }

    @Override
    public void onInvite(InviteEvent<Nexus> event) throws Exception {
        Nexus.LOGGER.info("Received invite to " + event.getChannel() + " from " + event.getUser());
        if (event.getBot().isAdmin(event.getUser())) {
            event.getBot().sendIRC().joinChannel(event.getChannel());
            event.getBot().sendIRC().message(event.getChannel(), event.getUser() + " wanted me in here.");
            Nexus.LOGGER.info("Channel invite accepted");
        } else {
            event.getBot().sendIRC().message(event.getUser(), "You are not allowed to invite me.");
            Nexus.LOGGER.info("Channel invite denied");
        }
    }

    @Override
    public void onNotice(NoticeEvent<Nexus> event) throws Exception {
        Nexus.LOGGER.info("Received notice: " + event.getNotice());
        // Attempt to retrieve static login information for a user
        Matcher matcher = Pattern.compile("Information on (.+?) \\(account (.+?)\\):").matcher(event.getNotice());
        while (matcher.find()) {
            event.getBot().getNicksConfig().storeNick(matcher.group(1), matcher.group(2));
        }
    }

    @Override
    public void onPrivateMessage(PrivateMessageEvent<Nexus> event) throws Exception {
        String message = event.getMessage();
        Nexus.LOGGER.info("Received PM from " + event.getUser().getNick() + ": " + message);
        if (!event.getBot().getCommandManager().onCommand(null, event.getUser(), message)) {
            /*if (!event.getBot().getResponseManager().trigger(null, event.getUser(), message)) {
                event.respond(Colors.RED + "Use " + Colors.BOLD + event.getBot().getConfig().getCommandPrefix() + "help " + Colors.BOLD + " for a command list");
            }*/
            event.getBot().getResponseManager().trigger(null, event.getUser(), message);
        }
    }

    @Override
    public void onJoin(JoinEvent<Nexus> event) throws Exception {
        if (event.getUser().getNick().equals(event.getBot().getUserBot().getNick())) {
            event.getBot().saveChannels();
            Nexus.LOGGER.info("Joined channel: " + event.getChannel().getName());
        }
    }

    @Override
    public void onPart(PartEvent<Nexus> event) throws Exception {
        if (event.getUser().getNick().equals(event.getBot().getUserBot().getNick())) {
            event.getBot().saveChannels();
            Nexus.LOGGER.info("Parted channel: " + event.getChannel().getName());
        }
    }

    @Override
    public void onConnect(ConnectEvent<Nexus> event) throws Exception {
        Nexus.LOGGER.info("Connected to IRC");
        event.getBot().onConnect();
    }

    @Override
    public void onDisconnect(DisconnectEvent<Nexus> event) throws Exception {
        Nexus.LOGGER.info("Disconnected from IRC");
    }

    @Override
    public void onKick(KickEvent<Nexus> event) throws Exception {
        event.getBot().saveChannels();
        if (event.getRecipient().getNick().equalsIgnoreCase(event.getBot().getNick())) {
            Nexus.LOGGER.info("Kicked from " + event.getChannel().getName() + " by " + event.getUser().getNick());
        }
    }

    @Override
    public void onVoice(VoiceEvent<Nexus> event) throws Exception {
        if (event.getRecipient().getNick().equalsIgnoreCase(event.getBot().getNick())) {
            Nexus.LOGGER.info("Voice" + (event.hasVoice() ? " given to " : " removed from ") + event.getRecipient().getNick() + " by " + event.getUser().getNick() + " in " + event.getChannel().getName());
        }
    }
}