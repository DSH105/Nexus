package com.dsh105.nexus.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class NexusServer {

    /**
     * The Logger
     */
    private Logger logger = LogManager.getLogger("Server");

    /**
     * The NexusServer Instance
     */
    private static final NexusServer instance = new NexusServer();

    /**
     * The WebServer
     */
    private Server webServer;

    /**
     * The server-configuration
     */
    private Properties serverProperties;

    /**
     * Whether or not we're debugging
     */
    private boolean debugging = false;

    public NexusServer() {

    }

    public void start() {
        this.serverProperties = new Properties();

        try {
            this.serverProperties.load(new FileInputStream("NexusServer.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        debugging = this.serverProperties.getProperty("debug").equalsIgnoreCase("true");

        this.logger.info("Starting NexusServer");
        this.logger.info("Debug mode is " + (this.debugging ? "enabled" : "disabled"));

        createWebServer();
    }

    protected void createWebServer() {

    }
}
