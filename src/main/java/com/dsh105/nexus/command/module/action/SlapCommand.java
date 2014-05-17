package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandPerformEvent;

@Command(command = "slap", needsChannel = false, help = "Slaps a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - Slap someone!"})
public class SlapCommand extends AbstractActionCommand {
    public SlapCommand() {
        this.setVerb("slap");
    }
}
