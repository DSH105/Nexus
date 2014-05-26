package com.dsh105.nexus.server;

import com.dsh105.nexus.server.debug.Debugger;
import com.dsh105.nexus.server.threading.ServerShutdownThread;
import com.dsh105.nexus.server.threading.SleepForeverThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    /**
     * Whether or not the server is running
     */
    private boolean running = true;

    public NexusServer() {
        new SleepForeverThread();
        start();
    }

    protected boolean start() {
        long startTime = System.currentTimeMillis();

       /** CommandReaderThread commandReaderThread = new CommandReaderThread(this);
        commandReaderThread.setDaemon(true);
        commandReaderThread.start();  */

        // The properties
        try {
            this.logger.info("Loading properties...");

            this.serverProperties = new Properties();
            this.serverProperties.setProperty("debug.enabled", "false");
            this.serverProperties.setProperty("debug.level", "5");
            this.serverProperties.setProperty("port", "8080");
            this.serverProperties.setProperty("webapp.path", "<PATH_HERE>");
            this.serverProperties.setProperty("webapp.context", "<PATH_HERE>");

            File propertyFile = new File(getRoot(), "nexus-server.properties");
            if(!propertyFile.exists()) {
                FileOutputStream fileOutputStream = new FileOutputStream(propertyFile);
                this.serverProperties.store(fileOutputStream, "NexusServer settings");
            }

            FileInputStream fileInputStream = new FileInputStream(propertyFile);
            this.serverProperties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * Initialize the debugger
         */
        debugging = this.serverProperties.getProperty("debug.enabled").equalsIgnoreCase("true");
        int debugLevel = Integer.parseInt(this.serverProperties.getProperty("debug.level"));
        Debugger.getInstance().setEnabled(debugging);
        Debugger.getInstance().setLevel(debugLevel);

        /**
         * Let the world know what we're doing
         */
        this.logger.info("Starting NexusServer...");
        this.logger.info("Debug mode is " + (this.debugging ? "enabled" : "disabled"));

        this.logger.info("Done (" + (System.currentTimeMillis() - startTime) + "ms)!");

        createWebServer();

        Runtime.getRuntime().addShutdownHook(new ServerShutdownThread(this));

        return true;
    }

    protected void createWebServer() {
        int port = Integer.parseInt(this.serverProperties.getProperty("port"));
        String webApp = this.serverProperties.getProperty("webapp.path");
        String webAppContextPath = this.serverProperties.getProperty("webapp.context");

        Debugger.getInstance().log(1, "Loading webapp from {0} at url {1}", webApp, webAppContextPath);

        this.webServer = new Server(port);
        try {
            this.webServer.start();
            this.logger.info("Started the server on port: " + port);

            this.webServer.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        this.logger.info("Shutting down...");

        try {
            this.webServer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.running = false;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void handleConsoleCommand(final String command) {
        logger.info("Not implemented yet!");
    }

    public File getRoot() {
        return new File(".");
    }

    public static NexusServer getInstance() {
        return instance;
    }
}
