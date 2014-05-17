package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "whip", needsChannel = false, help = "Whip a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - Whip someone!"})
public class WhipCommand extends AbstractActionCommand {
    public WhipCommand() {
        this.setVerb("whip");
    }
}
