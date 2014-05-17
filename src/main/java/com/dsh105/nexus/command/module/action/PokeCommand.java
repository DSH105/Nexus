package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "slap", needsChannel = false, help = "Whips a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - Whip someone!"})
public class PokeCommand extends AbstractActionCommand {
    public PokeCommand() {
        this.setVerb("whip");
    }
}
