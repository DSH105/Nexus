package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "slap", needsChannel = false, help = "Pokes a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - poke someone!"})
public class PunchCommand extends AbstractActionCommand {
    public PunchCommand() {
        this.setVerb("poke");
    }
}
