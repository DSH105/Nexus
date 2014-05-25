package com.dsh105.nexus.server.command;

public class Command {

    private final CommandExecutor executor;
    private final String name;
    private final String[] aliases;

    public Command(final CommandExecutor commandExecutor, final String name, final String... aliases) {
        this.executor = commandExecutor;
        this.name = name;
        this.aliases = aliases;
    }

    public CommandExecutor getExecutor() {
        return this.executor;
    }

    public String getName() {
        return this.name;
    }

    public String[] getAliases() {
        return this.aliases;
    }
}
