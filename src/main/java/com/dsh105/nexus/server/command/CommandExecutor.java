package com.dsh105.nexus.server.command;

import com.dsh105.nexus.server.NexusServer;

public interface CommandExecutor {

    public void onCommand(final NexusServer nexusServer, final Command command, final String label, final String[] args);
}
