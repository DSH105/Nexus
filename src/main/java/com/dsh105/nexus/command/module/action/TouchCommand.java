package com.dsh105.nexus.command.module.action;

import com.dsh105.nexus.command.Command;

@Command(command = "touch",
        needsChannel = false,
        helpGroups = "action",
        help = "Touch a user",
        extendedHelp = {"{b}{p}{c}{/b} <user> - touch someone!"})
public class TouchCommand extends AbstractActionCommand {
    public TouchCommand() {
        this.setVerb("touche");
    }
}
