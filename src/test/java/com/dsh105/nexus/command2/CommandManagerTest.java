package com.dsh105.nexus.command2;

import com.dsh105.nexus.command2.core.Command;
import org.junit.Test;
import org.pircbotx.Channel;
import org.pircbotx.User;

import static org.mockito.Mockito.mock;

public class CommandManagerTest {

    @Test
    public void test() {
        CommandManager commandManager = new CommandManager();
        commandManager.register(CommandManagerTest.class);

        User user = mock(User.class);
        Channel channel = mock(Channel.class);

        commandManager.onCommand(channel, user, "test");
    }

    @Command(
            name = "test",
            description = "some description"
    )
    public static void command(final CommandContext commandContext, final User user, final Channel channel) {
        System.out.println("This is a testcommand.");
    }
}