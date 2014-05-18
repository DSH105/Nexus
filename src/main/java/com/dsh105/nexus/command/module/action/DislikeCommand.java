package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "dislike",
        needsChannel = false,
        helpGroups = "action",
        help = "Dislike a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - dislike someone!"})
public class DislikeCommand extends AbstractActionCommand {
    public DislikeCommand() {
        this.setVerb("dislike");
    }
}
