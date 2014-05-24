package com.dsh105.nexus.server;

public class Main {

    public static void main(String[] args) {
        NexusServer server = NexusServer.getInstance();
        server.start();
       // Runtime.getRuntime().addShutdownHook(new ServerShutdownThread(server));
    }
}
