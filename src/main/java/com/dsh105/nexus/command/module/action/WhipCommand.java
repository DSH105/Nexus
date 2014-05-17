package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "slap", needsChannel = false, help = "Slaps a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - Slap someone!"})
public class WhipCommand extends AbstractActionCommand {
    public WhipCommand() {
        this.setVerb("slap");
    }
}
