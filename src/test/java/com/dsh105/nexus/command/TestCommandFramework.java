package com.dsh105.nexus.command;

import com.dsh105.nexus.IRCInitializer;
import com.dsh105.nexus.Nexus;
import com.dsh105.nexus.command2.CommandManager;
import com.dsh105.nexus.command2.example.ExampleCommand;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class TestCommandFramework {

    @Test
    public void testCommands() {
        IRCInitializer.createIrcEnvironment();

        Nexus nexus = mock(Nexus.class);

        com.dsh105.nexus.command2.CommandManager commandManager = new CommandManager();
        commandManager.register(ExampleCommand.class);

       // commandManager.onCommand(IRCInitializer.getMockedChannel(), IRCInitializer.getMockedUser(), "test", "someArg");
    }
}
