package com.dsh105.nexus.script;

import com.dsh105.nexus.Nexus;
import org.junit.Test;
import org.mockito.Mockito;

public class ScalaTest {

    @Test
    public void test() {
        Nexus mock = Mockito.mock(Nexus.class);
        ScalaScriptManager scalaScriptManager = new ScalaScriptManager(mock);

        System.out.println(scalaScriptManager.getLang());
    }
}
