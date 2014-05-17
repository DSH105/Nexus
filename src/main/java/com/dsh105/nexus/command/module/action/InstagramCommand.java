package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "instagram", needsChannel = false, help = "Instagram a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - Instagram someone!"})
public class InstagramCommand extends AbstractActionCommand {
    public InstagramCommand() {
        this.setVerb("Instagram");
    }
}
