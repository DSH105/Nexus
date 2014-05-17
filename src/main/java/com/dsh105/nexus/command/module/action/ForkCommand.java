package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "fork", needsChannel = false, help = "Fork a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - fork someone!"})
public class ForkCommand extends AbstractActionCommand {
    public ForkCommand() {
        this.setVerb("fork");
    }
}
