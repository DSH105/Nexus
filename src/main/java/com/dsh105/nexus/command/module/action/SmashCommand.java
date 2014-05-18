package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "smash",
        needsChannel = false,
        helpGroups = "action",
        help = "Smash a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - smash someone!"})
public class SmashCommand extends AbstractActionCommand {
    public SmashCommand() {
        this.setVerb("smashe");
    }
}
