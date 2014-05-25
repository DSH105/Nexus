package com.dsh105.nexus.server.command;

import com.dsh105.nexus.server.NexusServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {

    private static final Logger logger = LogManager.getLogger("CommandManager");

    private static final CommandManager instance = new CommandManager();

    private static final Map<String, Command> commands = new HashMap<>();

    public CommandManager() {}

    public static CommandManager getInstance() {
        return instance;
    }

    public void register(final NexusServer nexusServer, final String label, final CommandExecutor executor) {
        this.register(nexusServer, label, null, executor);
    }

    public void register(final NexusServer nexusServer, final String label, final String[] aliases, final CommandExecutor executor) {
        Command command = new Command(executor, label, aliases);

        if (commands.containsKey(label)) {
            logger.warn("Already registered a command with name \"" + label + "\"");
            return;
        }

        commands.put(label, command);

        if (aliases == null)
            return;

        for (String alias : aliases) {
            register(nexusServer, alias, executor);
        }
    }

    public void handleCommand(final NexusServer server, final String label) {
        Command command = commands.get(label);

        if (command == null)
            return;

        String[] args = label.split(" ");
        String commandLabel = args[0];

        System.arraycopy(args, 1, args, 0, args.length - 1);

        command.getExecutor().onCommand(server, command, commandLabel, args);
    }
}
