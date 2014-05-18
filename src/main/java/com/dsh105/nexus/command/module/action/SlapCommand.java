package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "slap",
        needsChannel = false,
        helpGroups = "action",
        help = "Slaps a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - slap someone!"})
public class SlapCommand extends AbstractActionCommand {
    public SlapCommand() {
        this.setVerb("slap");
    }
}
