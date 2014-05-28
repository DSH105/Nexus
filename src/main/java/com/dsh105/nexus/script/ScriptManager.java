package com.dsh105.nexus.script;

import scala.tools.nsc.Settings;

import java.io.PrintWriter;
import java.io.StringWriter;

public  abstract class ScriptManager {

    public abstract String getLang();

    public abstract void compile(final Script script);

    public void test() {
        Settings settings = new Settings();

        StringWriter writer = new StringWriter();
        PrintWriter writer1 = new PrintWriter(writer);
    }
}
