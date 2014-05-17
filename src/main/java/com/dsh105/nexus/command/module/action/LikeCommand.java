package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "slap", needsChannel = false, help = "Dislike a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - dislike someone!"})
public class LikeCommand extends AbstractActionCommand {
    public LikeCommand() {
        this.setVerb("dislike");
    }
}
