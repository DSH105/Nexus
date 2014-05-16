package com.dsh105.nexus.command;

import com.dsh105.nexus.mock.MockActionCommand;
import org.junit.Assert;
import org.junit.Test;

public class TestActionCommand {

    @Test
    public void testActionMessage() {
        MockActionCommand cmd = new MockActionCommand();
        Assert.assertEquals("mocks DSH105, JOPHESTUS and stuntguy3000 for failing", cmd.getActionText(new String[]{"DSH105,JOPHESTUS,stuntguy3000", "for", "failing"}));
    }
}
