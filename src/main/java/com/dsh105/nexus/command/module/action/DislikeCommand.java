package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "slap", needsChannel = false, help = "Punch a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - punch someone!"})
public class DislikeCommand extends AbstractActionCommand {
    public DislikeCommand() {
        this.setVerb("punch");
    }
}
