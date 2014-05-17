package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "kiss", needsChannel = false, help = "Kiss a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - kiss someone!"})
public class KissCommand extends AbstractActionCommand {
    public KissCommand() {
        this.setVerb("kiss");
    }
}
