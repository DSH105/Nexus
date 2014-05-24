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

package com.dsh105.nexus;

import org.pircbotx.Channel;
import org.pircbotx.User;

import java.util.Arrays;
import java.util.HashSet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IRCInitializer {

    private static boolean mocked = false;

    private static User mockedUser;
    private static Channel mockedChannel;

    public static void createIrcEnvironment() {
        mockedChannel = mock(Channel.class);
        when(mockedChannel.getName()).thenReturn("Nexus-test-channel");
        when(mockedChannel.getTopic()).thenReturn("McLover-channel");

        mockedUser = mock(User.class);
        when(mockedUser.getNick()).thenReturn("Nexus-test");
        when(new HashSet<>(mockedUser.getChannels())).thenReturn(new HashSet<>(Arrays.asList(mockedChannel)));
        when(mockedUser.getRealName()).thenReturn("McLover");
        when(mockedUser.getServer()).thenReturn("i.am.rich");
    }

    public static Channel getMockedChannel() {
        if (mockedChannel == null) {
            throw new RuntimeException("Mocked Channel is NULL! Please run IRCInitializer.createIrcEnvironment(); first!");
        }
        return mockedChannel;
    }

    public static User getMockedUser() {
        if (mockedUser == null) {
            throw new RuntimeException("Mocked User is NULL! Please run IRCInitializer.createIrcEnvironment(); first!");
        }
        return mockedUser;
    }
}
