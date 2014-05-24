package com.dsh105.nexus.server.debug;

import java.io.PrintStream;
import java.text.MessageFormat;

public class Debugger {

    private static Debugger instance = new Debugger();

    public static Debugger getInstance() {
        return instance;
    }

    private int level;
    private PrintStream output;
    private boolean enabled = false;

    public Debugger() {
        this.level = -1;
        this.output = System.out;
    }

    public void log(int level, String message, Object... args) {
        if (level <= this.level && this.enabled) {
            MessageFormat format = new MessageFormat(message);
            this.output.println(format.format(args));
        }
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public PrintStream getOutput() {
        return this.output;
    }

    public void setOutput(PrintStream printStream) {
        this.output = printStream;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
