package com.dsh105.nexus.server.threading;

import com.dsh105.nexus.server.NexusServer;

public class ServerThread extends Thread {

    private final NexusServer nexusServer;

    public ServerThread(NexusServer nexusServer) {
        super("Server Thread");

        this.nexusServer = nexusServer;
    }

    @Override
    public void run() {
        this.nexusServer.run();
    }
}
