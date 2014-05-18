package com.dsh105.nexus.command.module.general;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;

import java.util.UUID;

@Command(command = "uuid", needsChannel = false, help = "Validate a UUID.",
        extendedHelp = {"{b}{p}{c}{/b} <input> - Tests if input is a valid UUID."})
public class UUIDCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        String[] args = event.getArgs();

        if (args.length != 1) {
            return false;
        }

        try {
            UUID u = UUID.fromString(args[0]);
            event.respondWithPing("{0} is a valid UUID!", args[0]);
        } catch (Exception ex) {
            event.respondWithPing("{0} is not a valid UUID!", args[0]);
        }

        return true;
    }
}
