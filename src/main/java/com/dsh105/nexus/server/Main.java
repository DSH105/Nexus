package com.dsh105.nexus.server;

import com.dsh105.nexus.server.threading.ServerShutdownThread;

public class Main {

    public static void main(String[] args) {
        NexusServer server = NexusServer.getInstance();
        server.startServerThread();
        Runtime.getRuntime().addShutdownHook(new ServerShutdownThread(server));
    }
}
