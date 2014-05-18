package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "glare", needsChannel = false, help = "glare at a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - glare at someone!"})
public class GlareCommand extends AbstractActionCommand {
    public GlareCommand() {
        this.setVerb("glares at");
        this.setOverride(true);
    }
}
