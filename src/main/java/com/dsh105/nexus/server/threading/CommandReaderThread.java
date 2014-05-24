package com.dsh105.nexus.server.threading;

import com.dsh105.nexus.server.NexusServer;
import com.dsh105.nexus.server.debug.Debugger;
import jline.console.ConsoleReader;
import jline.console.completer.FileNameCompleter;

import java.io.IOException;
import java.io.PrintWriter;

public class CommandReaderThread extends Thread {

    private final NexusServer nexusServer;
    protected ConsoleReader reader;

    public CommandReaderThread(NexusServer nexusServer) {
        super("CommandReader Thread");

        this.nexusServer = nexusServer;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Debugger.getInstance().log(2, "lolwat");
                reader = new ConsoleReader();
                reader.addCompleter(new FileNameCompleter());
                reader.setPrompt("> ");
                String line;
                PrintWriter out = new PrintWriter(System.out);

                while ((line = reader.readLine()) != null) {
                    if (this.nexusServer != null) {
                        this.nexusServer.handleConsoleCommand(line);
                    }
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.getTerminal().restore();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
