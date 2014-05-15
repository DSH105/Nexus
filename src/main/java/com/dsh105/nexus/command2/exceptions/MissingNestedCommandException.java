package com.dsh105.nexus.command2.exceptions;

public class MissingNestedCommandException extends CommandUsageException {

    public MissingNestedCommandException(String message, String usage) {
        super(message, usage);
    }
}
