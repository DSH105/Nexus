package com.dsh105.nexus.server;

import com.dsh105.nexus.server.debug.Debugger;
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

        debugging = this.serverProperties.getProperty("debug.enabled").equalsIgnoreCase("true");
        int debugLevel = Integer.parseInt(this.serverProperties.getProperty("debug.level"));
        Debugger.getInstance().setEnabled(debugging);

        this.logger.info("Starting NexusServer");
        this.logger.info("Debug mode is " + (this.debugging ? "enabled" : "disabled"));

        createWebServer();
    }

    protected void createWebServer() {
        int port = Integer.parseInt(this.serverProperties.getProperty("port"));
        String webApp = this.serverProperties.getProperty("webapp.path");
        String webAppContextPath = this.serverProperties.getProperty("webapp.context");

        Debugger.getInstance().log(1, "Loading webapp from {0} at url {1}", webApp, webAppContextPath);

        this.webServer = new Server(port);


    }
}
