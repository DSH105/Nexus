package com.dsh105.nexus.command2.example;

import com.dsh105.nexus.command2.CommandContext;
import com.dsh105.nexus.command2.core.Command;
import org.pircbotx.Channel;
import org.pircbotx.User;

public class ExampleCommand {

    @Command(
            name = "test",
            description = "A test command"
    )
    public void someCoolCommand(final CommandContext commandContext, final User user, final Channel channel) {
        user.sendMessage("Test succeed!");
    }
}
