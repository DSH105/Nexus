package com.dsh105.nexus.command.module.script;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandGroup;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;

@Command(command = "script",
        aliases = {"sc"},
        needsChannel = false,
        groups = CommandGroup.ADMIN,
        help = "Script a command.",
        extendedHelp = "No."
)
public class ScriptCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length <= 0) {
            return false;
        }

        return true;
    }
}
