package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "like", needsChannel = false, help = "Like a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - like someone!"})
public class LikeCommand extends AbstractActionCommand {
    public LikeCommand() {
        this.setVerb("like");
    }
}
