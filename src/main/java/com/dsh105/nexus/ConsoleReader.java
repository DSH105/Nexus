/*
 * This file is part of Nexus.
 *
 * Nexus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nexus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nexus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dsh105.nexus;

import com.dsh105.nexus.Nexus;
import jline.console.completer.FileNameCompleter;

import java.io.IOException;
import java.io.PrintWriter;

public class ConsoleReader extends Thread {

    private boolean running = true;

    public boolean isRunning() {
        return running;
    }

    protected void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        while (running) {
            jline.console.ConsoleReader console = null;
            try {
                console = new jline.console.ConsoleReader();
                console.addCompleter(new FileNameCompleter());
                console.setPrompt("> ");
                String line;
                PrintWriter out = new PrintWriter(System.out);

                while ((line = console.readLine("")) != null) {
                    if (Nexus.getInstance() != null) {
                        if (line.equalsIgnoreCase("EXIT") || line.equalsIgnoreCase("END") || line.equalsIgnoreCase("STOP") || line.equalsIgnoreCase("QUIT")) {
                            Nexus.getInstance().endProcess();
                        } else if (line.toLowerCase().startsWith("join")) {
                            if (line.contains(" ")) {
                                String[] parts = line.split(" ");
                                String channel = parts[1];
                                Nexus.LOGGER.info("Attempting to join channel: " + channel);
                                Nexus.getInstance().sendRaw().rawLineNow("JOIN " + channel);
                            } else {
                                Nexus.LOGGER.info("Usage: join #channel");
                            }
                        }
                    }
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (console != null) {
                    try {
                        console.getTerminal().restore();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}