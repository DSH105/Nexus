package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "poke",
        needsChannel = false,
        helpGroups = "action",
        help = "Poke a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - poke someone!"})
public class PokeCommand extends AbstractActionCommand {
    public PokeCommand() {
        this.setVerb("poke");
    }
}
