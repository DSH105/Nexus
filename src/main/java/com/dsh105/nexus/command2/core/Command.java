package com.dsh105.nexus.command2.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    String name();

    String[] aliases() default "";

    String usage() default "";

    String description();

    String flags() default "";

    int minArgs() default 0;

    int maxArgs() default -1;

    String help() default "";

    boolean anyFlags() default false;
}
