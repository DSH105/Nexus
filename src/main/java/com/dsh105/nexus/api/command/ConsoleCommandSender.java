package com.dsh105.nexus.api.command;

import java.io.OutputStream;

public class ConsoleCommandSender extends CommandSender {

    public ConsoleCommandSender(final OutputStream outputStream) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public boolean isConsole() {
        return false;
    }

    @Override
    public boolean isIrcUser() {
        return false;
    }
}
