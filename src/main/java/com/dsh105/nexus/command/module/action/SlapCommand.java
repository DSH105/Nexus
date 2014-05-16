package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandPerformEvent;

@Command(command = "slap", needsChannel = false, help = "Slaps a user",
        extendedHelp = {"{b}{p}{c}{/b} <value> - Converts the entered value to either dogecoin or usd."})
public class SlapCommand extends AbstractActionCommand {
    public SlapCommand() {
        this.setVerb("slap");
    }

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        return false;
    }
}
