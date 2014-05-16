package com.dsh105.nexus.mock;

import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.command.module.action.AbstractActionCommand;

public class MockActionCommand extends AbstractActionCommand {

    public MockActionCommand() {
        this.setVerb("mock");
    }

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        return false;
    }
}
