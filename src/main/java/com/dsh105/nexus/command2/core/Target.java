package com.dsh105.nexus.command2.core;

import com.dsh105.nexus.command2.CommandTarget;

public @interface Target {

    CommandTarget value() default CommandTarget.USER;
}
