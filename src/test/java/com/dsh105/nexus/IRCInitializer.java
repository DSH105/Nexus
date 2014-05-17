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
