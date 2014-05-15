package com.dsh105.nexus.command2.exceptions;

public class CommandUsageException extends CommandException {

    protected final String USAGE;

    public CommandUsageException(String message, String usage) {
        super(message);
        this.USAGE = usage;
    }

    public String getUsage() {
        return this.USAGE;
    }
}
