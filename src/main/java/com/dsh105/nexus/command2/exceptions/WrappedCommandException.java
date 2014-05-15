package com.dsh105.nexus.command2.exceptions;

public class WrappedCommandException extends CommandException {

    public WrappedCommandException() {
        super();
    }

    public WrappedCommandException(String message) {
        super(message);
    }

    public WrappedCommandException(Throwable cause) {
        super(cause);
    }
}
