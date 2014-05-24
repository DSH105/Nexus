package com.dsh105.nexus.server.threading;

import com.dsh105.nexus.server.NexusServer;

public class ServerShutdownThread extends Thread {

    private final NexusServer nexusServer;

    public ServerShutdownThread(NexusServer nexusServer) {
        super("Shutdown Thread");

        this.nexusServer = nexusServer;
    }

    @Override
    public void run() {
        this.nexusServer.shutdownSafe();
    }
}
