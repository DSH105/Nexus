package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "octokick", needsChannel = false, help = "Octokick a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - octokick someone!"})
public class OctokickCommand extends AbstractActionCommand {
    public OctokickCommand() {
        this.setVerb("octokick");
    }
}
