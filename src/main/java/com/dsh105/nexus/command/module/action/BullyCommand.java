package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "bully",
        needsChannel = false,
        helpGroups = "action",
        help = "Bully a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - bully someone!"})
public class BullyCommand extends AbstractActionCommand {
    public BullyCommand() {
        this.setVerb("bullie");
    }
}
