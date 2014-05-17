package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "punch", needsChannel = false, help = "Punch a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - punch someone!"})
public class PunchCommand extends AbstractActionCommand {
    public PunchCommand() {
        this.setVerb("punche");
    }
}
