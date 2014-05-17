package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "slap", needsChannel = false, help = "Snapchat a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - Snapchat someone!"})
public class SnapchatCommand extends AbstractActionCommand {
    public SnapchatCommand() {
        this.setVerb("Snapchat");
    }
}
