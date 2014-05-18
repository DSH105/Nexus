package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "decimate",
        needsChannel = false,
        helpGroups = "action",
        help = "Decimate a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - decimate someone!"})
public class DecimateCommand extends AbstractActionCommand {
    public DecimateCommand() {
        this.setVerb("decimate");
    }
}

